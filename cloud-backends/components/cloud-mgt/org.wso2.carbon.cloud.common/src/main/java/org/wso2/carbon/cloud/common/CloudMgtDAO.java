package org.wso2.carbon.cloud.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class acts ass the database access class for the cloudmgt operations.
 */
public class CloudMgtDAO {
    private static final String selectEmailFromTempInvitee = "SELECT email FROM TEMP_INVITEE WHERE uuid=(?)";
    private static final String selectEmailFromTempRegistration =
            "SELECT email FROM TEMP_REGISTRATION WHERE uuid=(?)";
    private static final String SELECT_ROLES_FROM_TEMP_INVITEE =
            "SELECT roles FROM TEMP_INVITEE WHERE tenantDomain=(?) AND email=(?)";
    private static final String INSERT_INTO_TEMP_INVITEE =
            "INSERT INTO TEMP_INVITEE VALUES (? , ? , ? , ?, CURRENT_TIMESTAMP, ?) ON " +
            "DUPLICATE KEY UPDATE uuid=(?), roles=(?), isSelfSigned=(?) , dateTime = CURRENT_TIMESTAMP";

    /**
     * This method returns the emails for the self-registered and the invited users from the cloud_mgt database
     *
     * @param uuid          is the uuid sent in the registration link to the user
     * @param isInvitedUser determines if this is an invited user or a self registered user
     * @return the email corresponding to the uuid
     * @throws CloudMgtException
     */
    public String getEmailForUUID(String uuid, boolean isInvitedUser) throws CloudMgtException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        String email = null;

        try {
            conn = CloudMgtDBConnectionManager
                    .getDbConnection();
            if (conn != null) {
                if (isInvitedUser) {
                    ps = conn.prepareStatement(selectEmailFromTempInvitee);
                } else {
                    ps = conn.prepareStatement(selectEmailFromTempRegistration);
                }
                ps.setString(1, uuid);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    email = resultSet.getString("email");
                }
            }
        } catch (SQLException e) {
            throw new CloudMgtException("Failed to retrieve email for the uuid " + uuid, e);
        } catch (CloudMgtException e) {
            throw new CloudMgtException("Failed to get database connection for the cloudmgt database ", e);
        } finally {
            CloudMgtDBConnectionManager.closeAllConnections(ps, conn, resultSet);
        }
        return email;
    }
}
