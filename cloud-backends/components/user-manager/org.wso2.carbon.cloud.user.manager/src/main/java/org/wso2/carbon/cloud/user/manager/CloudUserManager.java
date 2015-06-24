package org.wso2.carbon.cloud.user.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cloud.user.manager.beans.TenantInfoBean;
import org.wso2.carbon.cloud.user.manager.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

/**
 * Custom LDAP based user store implementation for cloud.
 */
public class CloudUserManager {

	private static Log log = LogFactory.getLog(CloudUserManager.class);
	public static final String CLOUD_MGT_DATASOURCE = "cloud_mgt";

	public TenantInfoBean getTenantDisplayNames(String user)
			throws SQLException {
		TenantInfoBean tenantInfo = new TenantInfoBean();
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		try {
			Hashtable<String, String> environment = new Hashtable<String, String>();
			environment.put("java.naming.factory.initial",
					"org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory");
			Context initContext = new InitialContext(environment);
			DataSource dataSource = (DataSource) initContext.lookup("jdbc/"
					+ CLOUD_MGT_DATASOURCE);
			if (dataSource != null) {
				conn = dataSource.getConnection();
				preparedStatement = conn
						.prepareStatement("SELECT * FROM ORGANIZATIONS WHERE"
								+ " tenantDomain IN (SELECT tenantDomain FROM TENANT_USER_MAPPING WHERE userName=?)");
				preparedStatement.setString(1, user);
				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					String tenantDomain = rs.getString("tenantDomain");
					String tenantDisplayName = rs.getString("displayName");
					tenantInfo.setTenantDisplayName(tenantDisplayName);
					tenantInfo.setTenantName(tenantDomain);
				}
			} else {
				log.info("Cannot Find a data source with the name : "
						+ CLOUD_MGT_DATASOURCE);
			}
		} catch (NamingException e) {
			log.error(e.getMessage());
		} catch (SQLException e) {
			log.error(e.getMessage());
		} finally {
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
		return tenantInfo;
	}

	public boolean checkForDefaultRole(String userName) {
		log.info("Checking Default Role for user : " + userName);
		if (isExistingUser(userName) && !isInDefaultRole(userName)) {
			log.info("Default Role not assigned for user : " + userName);
			addDefaultLoginRole(userName);
			return true;
		}
		return false;
	}

	private boolean isInDefaultRole(String userName) {
		UserStoreManager userStoreManager;
		try {
			userStoreManager = ServiceReferenceHolder.getRealmService()
					.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
					.getUserStoreManager();
			String[] rolesOfUser = userStoreManager.getRoleListOfUser(userName);
			for (String s : rolesOfUser) {
				if ("default".equals(s)) {
					return true;
				}
			}
		} catch (UserStoreException e) {
			String msg = "Failed to check users default " + " failed due to "
					+ e.getMessage();
			log.error(msg);
		}
		return false;
	}

	private boolean isExistingUser(String user) {
		UserStoreManager userStoreManager;
		try {
			userStoreManager = ServiceReferenceHolder.getRealmService()
					.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
					.getUserStoreManager();
			return userStoreManager.isExistingUser(user);
		} catch (UserStoreException e) {
			String msg = "Failed to check the user availability "
					+ " failed due to " + e.getMessage();
			log.error(msg);
		}
		return false;
	}

	private void addDefaultLoginRole(String user) {
		UserStoreManager userStoreManager;
		try {
			userStoreManager = ServiceReferenceHolder.getRealmService()
					.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
					.getUserStoreManager();

			String[] roles = { "default" };
			userStoreManager.updateRoleListOfUser(user, null, roles);
			log.info("Default Role assigned for user : " + user);
		} catch (UserStoreException e) {
			String msg = "Error adding the default role to the user "
					+ " failed due to " + e.getMessage();
			log.error(msg);
		}
	}

}
