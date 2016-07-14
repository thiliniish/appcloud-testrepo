package org.wso2.carbon.cloud.deployment.monitor.utils.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureRecord;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.LiveStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.SuccessRecord;
import org.wso2.deployment.monitor.utils.database.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

/**
 * StatusReportingDAOImpl
 */
public class StatusReportingDAOImpl implements StatusReportingDAO {

    private static final Logger logger = LoggerFactory.getLogger(StatusReportingDAOImpl.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override public void addSuccessRecord(SuccessRecord successRecord) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Success Record {}-{}", successRecord.getServer(), successRecord.getTaskName());
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.ADD_SUCCESS_RECORD);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            statement.setString(1, sdf.format(successRecord.getTimestamp()));
            statement.setString(2, successRecord.getTaskName());
            statement.setString(3, successRecord.getServer());
            statement.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error occurred while adding Success Record", e);
        } finally {
            DatabaseManager.closeAllConnections(connection, statement, null);
        }
    }

    @Override public int addFailureRecord(FailureRecord failureRecord) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Failure Record {}-{}", failureRecord.getServer(), failureRecord.getTaskName());
        }
        Connection connection = null;
        PreparedStatement insert = null;
        try {
            connection = DatabaseManager.getConnection();
            insert = connection.prepareStatement(QueryConstants.ADD_FAILURE_RECORD, Statement.RETURN_GENERATED_KEYS);
            insert.setString(1, sdf.format(failureRecord.getTimestamp()));
            insert.setString(2, failureRecord.getTaskName());
            insert.setString(3, failureRecord.getServer());
            insert.setString(4, failureRecord.getError());
            insert.executeUpdate();

            ResultSet generatedKeys = insert.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error occurred while adding Failure Record", e);
        } finally {
            DatabaseManager.closeAllConnections(connection, insert, null);
        }
        return 0;
    }

    @Override public void addFailureSummary(FailureSummary failureSummary) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding Failure Summary {}-{}: Downtime {}s", failureSummary.getServer(),
                    failureSummary.getTaskName(), failureSummary.getDownTime() / 1000);
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.ADD_FAILURE_SUMMARY);
            statement.setString(1, failureSummary.getTaskName());
            statement.setString(2, failureSummary.getServer());
            statement.setInt(3, failureSummary.getStartID());
            statement.setInt(4, failureSummary.getEndID());
            statement.setDate(5, new Date(failureSummary.getDate().getTime()));
            statement.setString(6, sdf.format(failureSummary.getStartTime()));
            statement.setString(7, sdf.format(failureSummary.getEndTime()));
            statement.setInt(8, (int) (failureSummary.getDownTime() / 1000));
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while adding Failure Summary", e);
        } finally {
            DatabaseManager.closeAllConnections(connection, statement, null);
        }
    }

    @Override public void updateLiveStatus(LiveStatus liveStatus) {
        if (logger.isDebugEnabled()) {
            logger.debug("Updating Live Status {}-{} : Status : {}", liveStatus.getServer(), liveStatus.getTaskName(),
                    liveStatus.getStatus());
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DatabaseManager.getConnection();
            statement = connection.prepareStatement(QueryConstants.UPDATE_LIVE_STATUS);
            statement.setString(1, liveStatus.getServer());
            statement.setString(2, liveStatus.getTaskName());
            statement.setString(3, liveStatus.getStatus());
            statement.setString(4, liveStatus.getStatus());
            statement.setString(5, sdf.format(new Date(System.currentTimeMillis())));
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error occurred while adding Live Status", e);
        } finally {
            DatabaseManager.closeAllConnections(connection, statement, null);
        }
    }
}
