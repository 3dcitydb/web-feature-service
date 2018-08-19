package vcs.citydb.wfs.operation.getfeature;

import net.opengis.ows._1.ExceptionReport;
import net.opengis.wfs._2.GetFeatureType;
import net.opengis.wfs._2.ResultTypeType;
import net.opengis.wfs._2.TruncatedResponse;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.query.Query;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.PredicateName;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;
import org.citydb.registry.ObjectRegistry;
import org.citydb.sqlbuilder.select.Select;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionReportHandler;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QueryExecuter implements EventHandler {
	private final FeatureWriter writer;
	private final long count;
	private final ResultTypeType resultType;
	private final WorkerPool<DBSplittingResult> databaseWorkerPool;
	private final Object eventChannel;
	private final DatabaseConnectionPool connectionPool;
	private final CityGMLBuilder cityGMLBuilder;
	private final Config config;

	private final SchemaMapping schemaMapping;
	private final QueryBuilder queryBuilder;
	private final EventDispatcher eventDispatcher;
	
	private WFSException wfsException;
	private volatile boolean shouldRun = true;

	public QueryExecuter(GetFeatureType wfsRequest,
			FeatureWriter writer,
			WorkerPool<DBSplittingResult> databaseWorkerPool,
			Object eventChannel,
			DatabaseConnectionPool connectionPool,
			CityGMLBuilder cityGMLBuilder,
			WFSConfig wfsConfig,
			Config config) {
		this.writer = writer;
		this.databaseWorkerPool = databaseWorkerPool;
		this.eventChannel = eventChannel;
		this.connectionPool = connectionPool;
		this.cityGMLBuilder = cityGMLBuilder;
		this.config = config;

		// get standard request parameters
		long maxFeatureCount = wfsConfig.getConstraints().getCountDefault();
		count = wfsRequest.isSetCount() && wfsRequest.getCount().longValue() < maxFeatureCount ? wfsRequest.getCount().longValue() : maxFeatureCount;		
		resultType = wfsRequest.getResultType();

		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		queryBuilder = new QueryBuilder(connectionPool.getActiveDatabaseAdapter(), schemaMapping);

		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
	}

	public void executeQuery(List<QueryExpression> queryExpressions, Query dummy, HttpServletRequest request) throws WFSException {
		Select query = queryBuilder.buildQuery(queryExpressions);

		boolean isMultipleQueryRequest = queryExpressions.size() > 1;
		boolean countBreak = false;
		long returnedFeature = 0;
		int currentQuery = -1;

		boolean purgeConnectionPool = false;
		boolean isWriteSingleFeature = !isMultipleQueryRequest 
				&& queryExpressions.get(0).isGetFeatureById() 
				&& resultType == ResultTypeType.RESULTS;
		writer.setWriteSingleFeature(isWriteSingleFeature);

		try (Connection connection = initConnection();
			 PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(query, connection);
			 ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				long matchAll = rs.getLong("match_all");
				long returnAll = resultType == ResultTypeType.RESULTS ? Math.min(matchAll, count) : 0;		

				if (isWriteSingleFeature && returnAll != 1) {
					isWriteSingleFeature = false;
					writer.setWriteSingleFeature(false);
				}

				if (!isWriteSingleFeature)
					writer.startFeatureCollection(matchAll, returnAll);
				setExporterContext(queryExpressions.get(0), dummy, isMultipleQueryRequest);

				if (resultType == ResultTypeType.RESULTS) {
					do {
						if (isMultipleQueryRequest) {
							int queryNo = rs.getInt("query_no");

							if (queryNo != currentQuery) {							
								if (currentQuery != -1) {
									// join database workers
									databaseWorkerPool.join();

									writer.endFeatureCollection();
								}

								// add feature collections for intermediate queries without matches
								while (currentQuery < queryNo - 1) {
									writer.startFeatureCollection(0, 0);
									writer.endFeatureCollection();
									currentQuery++;
								}

								currentQuery = queryNo;
								long matchQuery = rs.getInt("match_query");
								long returnQuery = returnedFeature + matchQuery <= returnAll ? matchQuery : returnAll - returnedFeature;							
								writer.startFeatureCollection(matchQuery, returnQuery);

								if (currentQuery > 0)
									setExporterContext(queryExpressions.get(currentQuery), dummy, isMultipleQueryRequest);
							}						
						}

						if (returnedFeature != count) {
							long id = rs.getLong("id");
							int objectClassId = rs.getInt("objectclass_id");
							
							AbstractObjectType<?> type = schemaMapping.getAbstractObjectType(objectClassId);
							if (type == null)
								throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to map the object class id '" + objectClassId + "' to an object type (ID: " + id + ").");

							// put feature on worker queue 
							DBSplittingResult splitter = new DBSplittingResult(id, type);
							databaseWorkerPool.addWork(splitter);

							returnedFeature++;
						} else {
							countBreak = true;
							break;
						}

					} while (shouldRun && rs.next());

					// join database workers
					databaseWorkerPool.join();

					if (isMultipleQueryRequest) {
						writer.endFeatureCollection();

						// add feature collections for queries without matches or returns
						while (++currentQuery < queryExpressions.size()) {
							long matchQuery = 0;

							if (countBreak) {
								Select hitsQuery = queryBuilder.buildHitsQuery(queryExpressions.get(currentQuery));

								try (PreparedStatement hitsStmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(hitsQuery, connection);
									 ResultSet hitsRs = hitsStmt.executeQuery()) {
									if (hitsRs.next())
										matchQuery = hitsRs.getLong(1);
								}
							}

							writer.startFeatureCollection(matchQuery, 0);
							writer.endFeatureCollection();
						}
					}
				}
				
				if (!isWriteSingleFeature) {
					// write additional objects
					writer.writeAdditionalObjects();
					writer.endAdditionalObjects();

					// write truncated response if a worker pool has thrown an error 
					if (wfsException != null)
						writer.writeTruncatedResponse(getTruncatedResponse(wfsException, request));

					writer.endFeatureCollection();
				}

			} else {
				// no results returned
				if (!isWriteSingleFeature) {
					if (isMultipleQueryRequest) {
						writer.startFeatureCollection(0, 0);
						for (int i = 0; i < queryExpressions.size(); i++) {
							writer.startFeatureCollection(0, 0);
							writer.endFeatureCollection();
						}

						writer.endFeatureCollection();
					} else {
						writer.startFeatureCollection(0, 0);
						writer.endFeatureCollection();
					}
				} 

				else {
					// the getFeatureById query requires a special not found exception message
					QueryExpression getFeatureById = queryExpressions.get(0);
					Predicate predicate = getFeatureById.getSelection().getPredicate();
					if (predicate.getPredicateName() == PredicateName.ID_OPERATOR) {
						String identifier = ((ResourceIdOperator)predicate).getResourceIds().iterator().next();
						throw new WFSException(WFSExceptionCode.NOT_FOUND, "There is no feature with identifier '" + identifier + "'.", identifier);
					}

					throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "Failed to generate NotFound exception message.");
				}
			}

		} catch (SQLException e) {
			purgeConnectionPool = true;
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal SQL error occurred whilst querying the database.", e);		
		} catch (FeatureWriteException | InterruptedException e) {
			throw new WFSException(WFSExceptionCode.INTERNAL_SERVER_ERROR, "A fatal error occurred whilst marshalling the response document.", e);
		} finally {
			// purge connection pool to remove possibly defect connections
			if (purgeConnectionPool)
				connectionPool.purge();

			eventDispatcher.removeEventHandler(this);
		}
	}

	private void setExporterContext(QueryExpression queryExpression, Query dummy, boolean isMultipleQueryRequest) {
		// enable xlink references in multiple query responses
		config.getInternal().setRegisterGmlIdInCache(isMultipleQueryRequest);

		// set flag for coordinate transformation
		config.getInternal().setTransformCoordinates(queryExpression.getTargetSRS().getSrid() != connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());

		// update filter configuration
		dummy.copyFrom(queryExpression);
	}
	
	private TruncatedResponse getTruncatedResponse(WFSException wfsException, HttpServletRequest request) {
		WFSExceptionReportHandler reportHandler = new WFSExceptionReportHandler(cityGMLBuilder);
		ExceptionReport exceptionReport = reportHandler.getExceptionReport(wfsException, request, true);

		TruncatedResponse truncatedResponse = new TruncatedResponse();
		truncatedResponse.setExceptionReport(exceptionReport);
		
		return truncatedResponse;
	}

	private Connection initConnection() throws SQLException {
		Connection connection = connectionPool.getConnection();
		connection.setAutoCommit(false);

		return connection;
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel) {
			wfsException = new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, ((InterruptEvent)event).getLogMessage(), ((InterruptEvent)event).getCause());
			shouldRun = false;
		}
	}

}
