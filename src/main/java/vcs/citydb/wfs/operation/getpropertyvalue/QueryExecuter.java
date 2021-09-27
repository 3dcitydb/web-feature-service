package vcs.citydb.wfs.operation.getpropertyvalue;

import net.opengis.ows._1.ExceptionReport;
import net.opengis.wfs._2.GetPropertyValueType;
import net.opengis.wfs._2.ResultTypeType;
import net.opengis.wfs._2.TruncatedResponse;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.AbstractObjectType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.exporter.database.content.DBSplittingResult;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriteException;
import org.citydb.core.query.Query;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.InterruptEvent;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import vcs.citydb.wfs.config.Constants;
import vcs.citydb.wfs.config.WFSConfig;
import vcs.citydb.wfs.exception.WFSException;
import vcs.citydb.wfs.exception.WFSExceptionCode;
import vcs.citydb.wfs.exception.WFSExceptionReportHandler;
import vcs.citydb.wfs.kvp.KVPConstants;
import vcs.citydb.wfs.paging.PageRequest;
import vcs.citydb.wfs.paging.PagingCacheManager;
import vcs.citydb.wfs.util.xml.NamespaceFilter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryExecuter implements EventHandler {
	private final PropertyValueWriter writer;
	private final WorkerPool<DBSplittingResult> databaseWorkerPool;
	private final Object eventChannel;
	private final DatabaseConnectionPool connectionPool;
	private final CityGMLBuilder cityGMLBuilder;
	private final InternalConfig internalConfig;

	private final long countDefault;
	private final boolean computeNumberMatched;
	private final boolean usePaging;
	private final PagingCacheManager pagingCacheManager;
	private final SchemaMapping schemaMapping;
	private final QueryBuilder queryBuilder;
	private final EventDispatcher eventDispatcher;

	private WFSException wfsException;
	private volatile boolean shouldRun = true;

	public QueryExecuter(
			PropertyValueWriter writer,
			WorkerPool<DBSplittingResult> databaseWorkerPool,
			Object eventChannel,
			DatabaseConnectionPool connectionPool,
			CityGMLBuilder cityGMLBuilder,
			InternalConfig internalConfig,
			WFSConfig wfsConfig) {
		this.writer = writer;
		this.databaseWorkerPool = databaseWorkerPool;
		this.eventChannel = eventChannel;
		this.connectionPool = connectionPool;
		this.cityGMLBuilder = cityGMLBuilder;
		this.internalConfig = internalConfig;

		countDefault = wfsConfig.getConstraints().getCountDefault();
		computeNumberMatched = wfsConfig.getConstraints().isComputeNumberMatched();
		usePaging = wfsConfig.getConstraints().isUseResultPaging();
		pagingCacheManager = ObjectRegistry.getInstance().lookup(PagingCacheManager.class);
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		queryBuilder = new QueryBuilder(connectionPool.getActiveDatabaseAdapter(), schemaMapping);

		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
	}

	public void executeQuery(GetPropertyValueType wfsRequest, QueryExpression queryExpression, NamespaceFilter namespaceFilter, PageRequest pageRequest, Query dummy, HttpServletRequest request) throws WFSException {
		boolean purgeConnectionPool = false;
		boolean invalidatePageRequest = false;

		// get standard request parameters
		long count = wfsRequest.isSetCount() && wfsRequest.getCount().longValue() < countDefault ? wfsRequest.getCount().longValue() : countDefault;
		long startIndex = wfsRequest.isSetStartIndex() ? wfsRequest.getStartIndex().longValue() : 0;
		ResultTypeType resultType = wfsRequest.getResultType();

		// create paging request
		if (pageRequest == null) {
			pageRequest = usePaging && count != Constants.COUNT_DEFAULT ?
					pagingCacheManager.create(wfsRequest, queryExpression, namespaceFilter) :
					PageRequest.dummy();
		}

		try (Connection connection = initConnection()) {
			long numberMatched = getNumberMatched(queryExpression, resultType, connection);
			long numberReturned = numberMatched != Constants.UNKNOWN_NUMBER_MATCHED ?
					Math.min(Math.max(numberMatched - startIndex, 0), count) :
					getNumberReturned(queryExpression, count, startIndex, pageRequest.getPageNumber(), connection);

			// get property values
			if (numberReturned > 0 && resultType == ResultTypeType.RESULTS) {
				Select query = queryBuilder.buildQuery(queryExpression, startIndex, count, numberReturned, pageRequest.getPageNumber());
				setExporterContext(queryExpression, dummy);

				try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(query, connection);
					 ResultSet rs = stmt.executeQuery()) {
					writer.startValueCollection(numberMatched, numberReturned,
							pageRequest.getPageNumber() > 0 ? pageRequest.previous(request) : null,
							numberReturned == count && numberMatched - startIndex - numberReturned > 0 ? pageRequest.next(request) : null);

					if (rs.next()) {
						long initialId, currentId;
						long nextId = initialId = rs.getLong(MappingConstants.ID);

						long propertyOffset = queryExpression.getPropertyOffset();
						writer.setInitialPropertyOffset((int) propertyOffset);

						int propertyCount;
						long sequenceId = 0;

						do {
							currentId = nextId;
							int objectClassId = rs.getInt(MappingConstants.OBJECTCLASS_ID);

							propertyCount = 0;
							do {
								// check property offset for first feature
								if (sequenceId == 0 && propertyOffset-- > 0)
									continue;

								propertyCount++;
							} while (rs.next() && (nextId = rs.getLong(MappingConstants.ID)) == currentId);

							// skip first feature if no properties shall be exported
							if (sequenceId == 0 && propertyCount == 0)
								continue;

							AbstractObjectType<?> type = schemaMapping.getAbstractObjectType(objectClassId);
							if (type == null) {
								throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED,
										"Failed to map the object class id '" + objectClassId + "' to an object type (ID: " + currentId + ").");
							}

							// put feature on worker queue
							writer.setPropertyCount(sequenceId, propertyCount);
							DBSplittingResult splitter = new DBSplittingResult(currentId, type, sequenceId++);
							databaseWorkerPool.addWork(splitter);
						} while (shouldRun && currentId != nextId);

						if (usePaging) {
							if (queryExpression.supportsPagingByStartId()) {
								queryExpression.setStartId(currentId);
							}

							propertyOffset = propertyCount;
							if (currentId == initialId) {
								propertyOffset += queryExpression.getPropertyOffset();
							}

							queryExpression.setPropertyOffset(propertyOffset);
						}
					}
				}

				// shutdown database worker pool
				databaseWorkerPool.shutdownAndWait();

				// write truncated response if a worker pool has thrown an error
				if (wfsException != null) {
					writer.writeTruncatedResponse(getTruncatedResponse(wfsException, request));
					invalidatePageRequest = true;
				}
			} else {
				// no results returned
				writer.startValueCollection(numberMatched, 0, null,
						resultType == ResultTypeType.HITS & numberMatched > 0 ? pageRequest.first(request) : null);

				invalidatePageRequest = numberMatched == 0 && pageRequest.getPageNumber() == 0;
			}

			writer.endValueCollection();

			// update paging cache
			if (usePaging && !invalidatePageRequest) {
				try {
					pagingCacheManager.update(pageRequest);
				} catch (IOException e) {
					invalidatePageRequest = true;
					throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "Failed to update the paging cache.", e);
				}
			}
		} catch (SQLException e) {
			purgeConnectionPool = true;
			invalidatePageRequest = true;
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal SQL error occurred whilst querying the database.", e);
		} catch (FeatureWriteException | InterruptedException e) {
			invalidatePageRequest = true;
			throw new WFSException(WFSExceptionCode.OPERATION_PROCESSING_FAILED, "A fatal error occurred whilst marshalling the response document.", e);
		} finally {
			// purge connection pool to remove possibly defect connections
			if (purgeConnectionPool)
				connectionPool.purge();

			// invalidate paging cache
			if (usePaging && invalidatePageRequest)
				pagingCacheManager.remove(pageRequest);

			eventDispatcher.removeEventHandler(this);
		}
	}

	private void setExporterContext(QueryExpression queryExpression, Query dummy) {
		// set flag for coordinate transformation
		internalConfig.setTransformCoordinates(queryExpression.getTargetSrs().getSrid() != connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());

		// update filter configuration
		dummy.copyFrom(queryExpression);
	}

	private TruncatedResponse getTruncatedResponse(WFSException wfsException, HttpServletRequest request) {
		WFSExceptionReportHandler reportHandler = new WFSExceptionReportHandler(cityGMLBuilder);
		ExceptionReport exceptionReport = reportHandler.getExceptionReport(wfsException, KVPConstants.GET_PROPERTY_VALUE, request, true);

		TruncatedResponse truncatedResponse = new TruncatedResponse();
		truncatedResponse.setExceptionReport(exceptionReport);
		
		return truncatedResponse;
	}

	private long getNumberMatched(QueryExpression queryExpression, ResultTypeType resultType, Connection connection) throws WFSException, SQLException {
		if (!queryExpression.isSetNumberMatched()) {
			if (computeNumberMatched || resultType == ResultTypeType.HITS) {
				Select query = queryBuilder.buildNumberMatchedQuery(queryExpression);
				try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(query, connection);
					 ResultSet rs = stmt.executeQuery()) {
					queryExpression.setNumberMatched(rs.next() ? rs.getLong(1) : 0);
				}
			} else
				queryExpression.setNumberMatched(Constants.UNKNOWN_NUMBER_MATCHED);
		}

		return queryExpression.getNumberMatched();
	}

	private long getNumberReturned(QueryExpression queryExpression, long count, long startIndex, long pageNumber, Connection connection) throws WFSException, SQLException {
		Select query = queryBuilder.buildNumberReturnedQuery(queryExpression, count, startIndex, pageNumber);
		try (PreparedStatement stmt = connectionPool.getActiveDatabaseAdapter().getSQLAdapter().prepareStatement(query, connection);
			 ResultSet rs = stmt.executeQuery()) {
			return rs.next() ? Math.max(rs.getLong(1) - queryExpression.getPropertyOffset(), 0) : 0;
		}
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
