/*
 * 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright 2014 - 2016
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vcs.citydb.wfs.operation.getfeature;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.citydb.api.concurrent.SingleWorkerPool;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.xml.sax.SAXException;

import net.opengis.wfs._2.FeatureCollectionType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.ResultTypeType;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;

public class QueryExecuter {
	private final List<QueryExpression> queryExpressions;
	private final FeatureMemberWriterFactory writerFactory;
	private final long count;
	private final ResultTypeType resultType;
	private final WorkerPool<DBSplittingResult> databaseWorkerPool;
	private final SingleWorkerPool<SAXEventBuffer> writerPool;
	private final DatabaseConnectionPool connectionPool;
	private final Marshaller marshaller;
	private final ExportFilter exportFilter;
	private final Config exporterConfig;

	private final QueryBuilder queryBuilder;
	private final ObjectFactory wfsFactory;
	private final DatatypeFactory datatypeFactory;

	public QueryExecuter(GetFeatureType wfsRequest,
			List<QueryExpression> queryExpressions, 
			FeatureMemberWriterFactory writerFactory,
			WorkerPool<DBSplittingResult> databaseWorkerPool,
			SingleWorkerPool<SAXEventBuffer> writerPool,
			DatabaseConnectionPool connectionPool,
			JAXBBuilder jaxbBuilder,
			ExportFilter exportFilter,
			WFSConfig wfsConfig,
			Config exporterConfig) throws JAXBException, DatatypeConfigurationException {
		this.queryExpressions = queryExpressions;
		this.writerFactory = writerFactory;
		this.databaseWorkerPool = databaseWorkerPool;
		this.writerPool = writerPool;
		this.connectionPool = connectionPool;
		this.exportFilter = exportFilter;
		this.exporterConfig = exporterConfig;

		// get standard request parameters
		long maxFeatureCount = wfsConfig.getSecurity().getMaxFeatureCount();
		count = wfsRequest.isSetCount() && wfsRequest.getCount().longValue() < maxFeatureCount ? wfsRequest.getCount().longValue() : maxFeatureCount;		
		resultType = wfsRequest.getResultType();

		queryBuilder = new QueryBuilder();
		marshaller = jaxbBuilder.getJAXBContext().createMarshaller();
		datatypeFactory = DatatypeFactory.newInstance();
		wfsFactory = new ObjectFactory();		
	}

	public void executeQuery() throws WFSException {
		String query = queryBuilder.buildQuery(queryExpressions);

		boolean isMultipleQueryRequest = queryExpressions.size() > 1;
		boolean countBreak = false;
		long returnedFeature = 0;
		int currentQuery = -1;

		boolean purgeConnectionPool = false;
		boolean isWriteBareFeature = !isMultipleQueryRequest 
				&& queryExpressions.get(0).isGetFeatureById() 
				&& resultType == ResultTypeType.RESULTS;
		writerFactory.setWriteMemberProperty(!isWriteBareFeature);

		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = initConnection();			
			stmt = connection.prepareStatement(query.toString());
			fillPlaceHolders(queryExpressions, stmt);			
			rs = stmt.executeQuery();

			if (rs.next()) {
				long matchAll = rs.getLong("match_all");
				long returnAll = resultType == ResultTypeType.RESULTS ? Math.min(matchAll, count) : 0;	

				if (isWriteBareFeature && returnAll != 1) {
					isWriteBareFeature = false;
					writerFactory.setWriteMemberProperty(true);
				}

				if (!isWriteBareFeature)
					startFeatureCollection(matchAll, returnAll, false);
				setExporterContext(queryExpressions.get(0));

				if (resultType == ResultTypeType.RESULTS) {
					do {
						if (isMultipleQueryRequest) {
							int queryNo = rs.getInt("query_no");

							if (queryNo != currentQuery) {							
								if (currentQuery != -1) {
									// join database workers
									databaseWorkerPool.join();

									endFeatureCollection(true);
								}

								// add feature collections for intermediate queries without matches
								while (currentQuery < queryNo - 1) {
									writeFeatureCollection(0, true);
									currentQuery++;
								}

								currentQuery = queryNo;
								long matchQuery = rs.getInt("match_query");
								long returnQuery = returnedFeature + matchQuery <= returnAll ? matchQuery : returnAll - returnedFeature;							
								startFeatureCollection(matchQuery, returnQuery, true);

								if (currentQuery > 0)
									setExporterContext(queryExpressions.get(currentQuery));
							}						
						}

						if (returnedFeature != count) {
							long cityObjectId = rs.getLong("id");
							int classId = rs.getInt("objectclass_id");
							CityGMLClass featureType = Util.classId2cityObject(classId);

							// put feature on worker queue 
							DBSplittingResult splitter = new DBSplittingResult(cityObjectId, featureType);
							databaseWorkerPool.addWork(splitter);

							returnedFeature++;
						} else {
							countBreak = true;
							break;
						}

					} while (rs.next());

					// shutdown database worker pool
					databaseWorkerPool.shutdownAndWait();

					if (isMultipleQueryRequest) {
						endFeatureCollection(true);

						// add feature collections for queries without matches or returns
						while (++currentQuery < queryExpressions.size()) {
							long matchQuery = 0;

							if (countBreak) {
								rs.close();
								stmt.close();

								QueryBuilder builder = new QueryBuilder();
								stmt = connection.prepareStatement(builder.buildHitsQuery(queryExpressions.get(currentQuery)));
								rs = stmt.executeQuery();
								if (rs.next())
									matchQuery = rs.getLong(1);
							}

							writeFeatureCollection(matchQuery, true);
						}
					}
				}		

				if (!isWriteBareFeature)
					endFeatureCollection(false);

			} else {
				// no results returned
				if (!isWriteBareFeature) {
					if (isMultipleQueryRequest) {
						startFeatureCollection(0, 0, false);
						for (int i = 0; i < queryExpressions.size(); i++)
							writeFeatureCollection(0, true);						

						endFeatureCollection(false);
					} else
						writeFeatureCollection(0, false);
				} 
				else {
					QueryExpression getFeatureById = queryExpressions.get(0);
					String identifier = getFeatureById.getGmlIdFilter().getFilterState().get(0);					
					throw new WFSException(WFSExceptionCode.NOT_FOUND, "The specified feature identified by '" + identifier + "' was not found.", identifier);
				}
			}

		} catch (SQLException e) {
			purgeConnectionPool = true;
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal SQL error occurred whilst querying the database.", e);		
		} catch (JAXBException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal JAXB error occurred whilst marshalling the response document.", e);
		} catch (SAXException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal SAX error occurred whilst marshalling the response document.", e);
		} catch (InterruptedException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal error occurred whilst marshalling the response document.", e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close database resource", e);
				}
			}

			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close database resource", e);
				}
			}

			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to close database resource", e);
				}
			}

			// purge connection pool to remove possibly defect connections
			if (purgeConnectionPool)
				connectionPool.purge();
		}
	}

	private void fillPlaceHolders(List<QueryExpression> queryExpressions, PreparedStatement stmt) throws SQLException {
		int i = 1;
		
		for (QueryExpression queryExpression : queryExpressions) {
			GmlIdFilter filter = queryExpression.getGmlIdFilter();

			if (filter != null) {
				List<String> ids = filter.getFilterState();
				if (ids != null && !ids.isEmpty()) {
					for (String id : ids)
						stmt.setString(i++, id);
				}
			}
		}
	}

	private void setExporterContext(QueryExpression queryExpression) {
		// enable xlink references in multiple query responses
		exporterConfig.getInternal().setRegisterGmlIdInCache(queryExpressions.size() > 1);				

		// set target reference system for export
		exporterConfig.getInternal().setExportTargetSRS(connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem());
		exporterConfig.getInternal().setTransformCoordinates(false);

		// update filter configuration
		// TODO: replace with filter layer
		exportFilter.setFeatureClassFilter(queryExpression.getFeatureTypeFilter());
		exportFilter.setGmlIdFilter(queryExpression.getGmlIdFilter());
	}

	private void writeFeatureCollection(long matchNo, boolean withMemberProperty) throws JAXBException, SAXException {
		JAXBElement<?> output = null;

		FeatureCollectionType featureCollection = new FeatureCollectionType();
		featureCollection.setTimeStamp(getTimeStamp());
		featureCollection.setNumberMatched(String.valueOf(matchNo));
		featureCollection.setNumberReturned(BigInteger.valueOf(0));

		if (withMemberProperty) {
			MemberPropertyType member = new MemberPropertyType();
			member.getContent().add(wfsFactory.createFeatureCollection(featureCollection));
			output = wfsFactory.createMember(member);
		} else
			output = wfsFactory.createFeatureCollection(featureCollection);

		SAXEventBuffer buffer = new SAXEventBuffer();
		marshaller.marshal(output, buffer);
		writerPool.addWork(buffer);
	}

	private void startFeatureCollection(long matchNo, long returnNo, boolean withMemberProperty) throws JAXBException, SAXException {
		writeFeatureCollection(matchNo, returnNo, withMemberProperty, WriteMode.HEAD);
	}

	private void endFeatureCollection(boolean withMemberProperty) throws JAXBException, SAXException {
		writeFeatureCollection(0, 0, withMemberProperty, WriteMode.TAIL);
	}

	private void writeFeatureCollection(long matchNo, long returnNo, boolean withMemberProperty, WriteMode writeMode) throws JAXBException, SAXException {
		JAXBElement<?> output = null;

		FeatureCollectionType featureCollection = new FeatureCollectionType();

		featureCollection.setTimeStamp(getTimeStamp());
		featureCollection.setNumberMatched(String.valueOf(matchNo));
		featureCollection.setNumberReturned(BigInteger.valueOf(returnNo));

		if (withMemberProperty) {
			MemberPropertyType member = new MemberPropertyType();
			member.getContent().add(wfsFactory.createFeatureCollection(featureCollection));
			output = wfsFactory.createMember(member);
		} else
			output = wfsFactory.createFeatureCollection(featureCollection);

		SAXEventBuffer buffer = new SAXEventBuffer();
		SAXFragmentWriter fragmentWriter = new SAXFragmentWriter(new QName(Constants.WFS_NAMESPACE_URI, "FeatureCollection"), buffer, writeMode);

		marshaller.marshal(output, fragmentWriter); 
		writerPool.addWork(buffer);
	}

	private XMLGregorianCalendar getTimeStamp() {
		GregorianCalendar date = new GregorianCalendar();
		return datatypeFactory.newXMLGregorianCalendar(
				date.get(Calendar.YEAR), 
				date.get(Calendar.MONTH) + 1, 
				date.get(Calendar.DAY_OF_MONTH), 
				date.get(Calendar.HOUR_OF_DAY), 
				date.get(Calendar.MINUTE), 
				date.get(Calendar.SECOND), 
				DatatypeConstants.FIELD_UNDEFINED, 
				DatatypeConstants.FIELD_UNDEFINED);
	}

	private Connection initConnection() throws SQLException {
		Connection connection = connectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for connection
		if (connectionPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
			connectionPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(
					connection, 
					exporterConfig.getProject().getDatabase().getWorkspaces().getExportWorkspace());
		}

		// TODO: create temporary table for global appearances if needed

		return connection;
	}

}
