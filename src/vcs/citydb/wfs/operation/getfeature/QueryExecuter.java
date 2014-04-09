/*
 * This file is part of the 3D City Database Web Feature Service
 * http://www.3dcitydb.org/
 * 
 * Copyright (c) 2014
 * virtualcitySYSTEMS GmbH
 * Tauentzienstrasse 7b/c
 * 10789 Berlin, Germany
 * http://www.virtualcitysystems.de/
 * 
 * The 3D City Database Web Feature Service is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 */
package vcs.citydb.wfs.operation.getfeature;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

import net.opengis.wfs._2.FeatureCollectionType;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.MemberPropertyType;
import net.opengis.wfs._2.ObjectFactory;
import net.opengis.wfs._2.ResultTypeType;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXFragmentWriter;
import org.citygml4j.util.xml.SAXFragmentWriter.WriteMode;
import org.xml.sax.SAXException;

import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import de.tub.citydb.api.concurrent.SingleWorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import de.tub.citydb.modules.common.filter.ExportFilter;
import de.tub.citydb.util.Util;

public class QueryExecuter {
	private final List<QueryExpression> queryExpressions;
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
			WorkerPool<DBSplittingResult> databaseWorkerPool,
			SingleWorkerPool<SAXEventBuffer> writerPool,
			DatabaseConnectionPool connectionPool,
			JAXBBuilder jaxbBuilder,
			ExportFilter exportFilter,
			Config exporterConfig) throws JAXBException, DatatypeConfigurationException {
		this.queryExpressions = queryExpressions;
		this.databaseWorkerPool = databaseWorkerPool;
		this.writerPool = writerPool;
		this.connectionPool = connectionPool;
		this.exportFilter = exportFilter;
		this.exporterConfig = exporterConfig;

		// get standard request parameters
		count = wfsRequest.isSetCount() ? wfsRequest.getCount().longValue() : Long.MAX_VALUE;		
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

		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			connection = initConnection();			
			stmt = connection.createStatement();				
			rs = stmt.executeQuery(query);

			if (rs.next()) {
				long matchAll = rs.getLong("match_all");
				long returnAll = resultType == ResultTypeType.RESULTS ? Math.min(matchAll, count) : 0;		

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
								} else
									currentQuery = 0;

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

								QueryBuilder builder = new QueryBuilder();
								rs = stmt.executeQuery(builder.buildHitsQuery(queryExpressions.get(currentQuery)));
								if (rs.next())
									matchQuery = rs.getLong(1);
							}

							writeFeatureCollection(matchQuery, true);
						}
					}
				}		

				endFeatureCollection(false);

			} else {
				// no results returned
				writeFeatureCollection(0, false);
			}

		} catch (SQLException e) {
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
