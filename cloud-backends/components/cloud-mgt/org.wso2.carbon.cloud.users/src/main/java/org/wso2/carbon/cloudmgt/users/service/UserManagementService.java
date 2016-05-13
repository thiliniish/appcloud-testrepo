package org.wso2.carbon.cloudmgt.users.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.cloudmgt.bam.integration.BamDataPublisher;
import org.wso2.carbon.cloudmgt.common.CloudConstants;
import org.wso2.carbon.cloudmgt.users.beans.UserInfoBean;
import org.wso2.carbon.cloudmgt.users.util.DatabaseManager;
import org.wso2.carbon.cloudmgt.users.util.UserMgtUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class UserManagementService extends AbstractAdmin {
	private static final String CLAIMS_EMAILADDRESS = "http://wso2.org/claims/emailaddress";
	private static final String CLAIMS_FIRSTNAME = "http://wso2.org/claims/givenname";
	private static final String CLAIMS_LASTNAME = "http://wso2.org/claims/lastname";
	private static Log log = LogFactory.getLog(UserManagementService.class);
	private static DatabaseManager dbManager = new DatabaseManager(
			CloudConstants.DATA_SOURCE_NAME);

	/**
	 * Update roles of the user
	 * 
	 * @param userName
	 *            user to update
	 * @param rolesToBeAdded
	 *            roles to add
	 * @param rolesToBeRemoved
	 *            roles to delete
	 * @return
	 * @throws UserManagementException
	 * @throws UserStoreException
	 */
	private boolean updateRolesOfUser(String userName, String[] rolesToBeAdded,
			String[] rolesToBeRemoved) throws UserStoreException,
			UserManagementException {

		CarbonContext currentContext = CarbonContext
				.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();

		UserStoreManager userStoreManager = UserMgtUtil.getRealmService()
				.getTenantUserRealm(tenantId).getUserStoreManager();
		userStoreManager.updateRoleListOfUser(userName, rolesToBeRemoved,
				rolesToBeAdded);

		return true;

	}

	/**
	 * Service method to update roles of existing user in the tenant
	 * 
	 * @param userName
	 * @param rolesToBeAdded
	 * @param rolesToBeRemoved
	 * @return
	 * @throws UserManagementException
	 */
	public boolean updateUserRoles(String userName, String[] rolesToBeAdded,
			String[] rolesToBeRemoved) throws UserManagementException {

		try {
			boolean result = updateRolesOfUser(userName, rolesToBeAdded,
					rolesToBeRemoved);
			return result;
		} catch (UserStoreException e) {
			String msg = "User addition to tenant "
					+ CarbonContext.getThreadLocalCarbonContext()
							.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}

	}

	/**
	 * Add new roles to an existing user in the tenant
	 *
	 * @param userName
	 *            user name of the user to add roles
	 * @param rolesToBeAdded
	 *            roles to add
	 * @return true if the operation is success
	 * @throws UserManagementException
	 */
	public boolean addUserRoles(String userName, String[] rolesToBeAdded)
			throws UserManagementException {
		try {
			// 222 add to tenant
			boolean result = updateRolesOfUser(userName, rolesToBeAdded, null);

			return result;
		} catch (UserStoreException e) {
			String msg = "User addition to tenant "
					+ CarbonContext.getThreadLocalCarbonContext()
							.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}

	}

	public void addDefaultLoginRole(String user) throws UserManagementException {
		UserStoreManager userStoreManager;
		try {
			userStoreManager = UserMgtUtil.getRealmService()
					.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
					.getUserStoreManager();
			if (!isUserInRole(user, CloudConstants.CLOUD_DEFAULT_ROLE)) {
				String[] roles = { CloudConstants.CLOUD_DEFAULT_ROLE };
				userStoreManager.updateRoleListOfUser(user, null, roles);
			}
		} catch (UserStoreException e) {
			String msg = "Error adding the default role to the user "
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}

	}

	/**
	 * Checks whether a user is in a given role
	 *
	 * @param user user
	 * @param role role
	 * @return true, if the user is in the given role
	 * @throws UserManagementException
	 */
	private boolean isUserInRole(String user, String role) throws UserManagementException {
		try {
			UserStoreManager userStoreManager = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
					.getUserStoreManager();
			String[] rolesOfUser = userStoreManager.getRoleListOfUser(user);
			for (String roleOfUser : rolesOfUser) {
				if (roleOfUser.equalsIgnoreCase(role)) {
					return true;
				}
			}
		} catch (UserStoreException e) {
			String msg = "Error occurred while checking whether the user : " + user + " is in role : " + role;
			throw new UserManagementException(msg, e);
		}
		return false;
	}

	public boolean isExistingUser(String user) throws UserManagementException {
		UserStoreManager userStoreManager;
		try {
			userStoreManager = UserMgtUtil.getRealmService()
					.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
					.getUserStoreManager();
			return userStoreManager.isExistingUser(user);
		} catch (UserStoreException e) {
			String msg = "Failed to check the user availability "
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}
	}

	/**
	 * Remove special roles from the user, here everyone role is not removed
	 * 
	 * @param userName
	 *            user name of the user to remove
	 * @return
	 * @throws UserManagementException
	 */
	public boolean removeUserFromTenant(String userName)
			throws UserManagementException {

		CarbonContext currentContext = CarbonContext
				.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();
		UserStoreManager userStoreManager;
		try {
			boolean result = dbManager.deleteUserFromTenantUserMapping(
					userName, currentContext.getTenantDomain());
			if (!result) {
				String msg = "User deletion from tenant "
						+ currentContext.getTenantDomain()
						+ " failed due to : User not deleted from database";
				log.error(msg);
				throw new UserManagementException(msg);
			}
			userStoreManager = UserMgtUtil.getRealmService()
					.getTenantUserRealm(tenantId).getUserStoreManager();
			String[] currentRoles = userStoreManager
					.getRoleListOfUser(userName);
			ArrayList<String> rolesToDelete = new ArrayList<String>();
			for (String role : currentRoles) {
				if ("everyone".equals(role)
						|| "Internal/everyone".equals(role)
						|| CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME
								.equals(role)) {
					continue;
				}
				rolesToDelete.add(role);
			}
			userStoreManager.updateRoleListOfUser(userName,
					rolesToDelete.toArray(new String[rolesToDelete.size()]),
					new String[0]);
			updateBAMStats(userName, "DELETE");
			return true;
		} catch (UserStoreException e) {
			String msg = "User deletion from tenant "
					+ currentContext.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		} catch (SQLException e) {
			String msg = "User deletion from tenant "
					+ currentContext.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}

	}

	/**
	 * get all users of the tenant
	 * 
	 * @return UserInfoBean arrays is returned which contains all required data
	 *         such as Fname,Lname,email address
	 * @throws UserManagementException
	 */
	public UserInfoBean[] getUsersofTenant() throws UserManagementException {
		CarbonContext currentContext = CarbonContext
				.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();
		HashMap<String, UserInfoBean> userMap = new HashMap<String, UserInfoBean>();
		try {
			UserStoreManager userStoreManager = UserMgtUtil.getRealmService()
					.getTenantUserRealm(tenantId).getUserStoreManager();
			String[] Roles = userStoreManager.getRoleNames();

			for (String role : Roles) {

				if (!UserMgtUtil.everyOneRoleName.equals(role)) {
					String[] users = userStoreManager.getUserListOfRole(role);
					for (String user : users) {

						if (!userMap.containsKey(user)
								&& !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME
										.equals(user)) {
							userMap.put(user,
									UserMgtUtil.getUserInfoBean(user, tenantId));
						}
					}
				}
			}
			Collection<UserInfoBean> userInfoBeans = userMap.values();
			return userInfoBeans
					.toArray(new UserInfoBean[userInfoBeans.size()]);

		} catch (UserStoreException e) {
			String msg = "Retrieving users of tenant "
					+ currentContext.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);

		}

	}

	public String getUserEmail(String userName) throws UserStoreException,
			UserManagementException {
		UserInfoBean userInfo = getUserInfo(userName);
		if (userInfo.getEmail() != null) {
			return userInfo.getEmail();
		} else {
			int tenantId = CarbonContext.getThreadLocalCarbonContext()
					.getTenantId();
			userName = UserMgtUtil.getRealmService().getTenantManager()
					.getTenant(tenantId).getAdminName();
			return getUserInfo(userName).getEmail();
		}
	}

	/**
	 * get information of a single user specified by the user name
	 * 
	 * @param userName
	 *            user to get the information
	 * @return
	 * @throws UserManagementException
	 */
	public UserInfoBean getUserInfo(String userName)
			throws UserManagementException {
		try {
			CarbonContext context = CarbonContext.getThreadLocalCarbonContext();
			int tenantId = context.getTenantId();
			return UserMgtUtil.getUserInfoBean(userName, tenantId);
		} catch (UserManagementException e) {
			String msg = "Retrieving user of tenant "
					+ CarbonContext.getThreadLocalCarbonContext()
							.getTenantDomain() + " failed due to "
					+ e.getMessage();
			log.error(msg);
			throw new UserManagementException(msg, e);
		}
	}

	public int getTenantId(String tenantDomain) throws UserManagementException {
		try {
			return UserMgtUtil.getRealmService().getTenantManager()
					.getTenantId(tenantDomain);
		} catch (UserStoreException e) {
			String msg = "Retrieving tenant Id of tenant " + tenantDomain
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}
	}
	
	
	 

	public boolean importUserTotheTenant(String user, String defaultPassword,
			String email, String firstName, String lastName) throws UserManagementException {

		String[] defaultUserRole = { CloudConstants.CLOUD_DEFAULT_ROLE };
		CarbonContext threadLocalCarbonContext = CarbonContext
				.getThreadLocalCarbonContext();
		UserStoreManager userStoreManager;
		try {
			userStoreManager = threadLocalCarbonContext.getUserRealm()
					.getUserStoreManager();
		} catch (UserStoreException e) {
			String msg = "Importing users to tenant "
					+ threadLocalCarbonContext.getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}
		Map<String, String> claims = new HashMap<String, String>();
		claims.put(CLAIMS_EMAILADDRESS, email);
		claims.put(CLAIMS_FIRSTNAME, firstName);
		claims.put(CLAIMS_LASTNAME, lastName);
		try {
			if (userStoreManager.isExistingUser(user)) {
				userStoreManager.updateRoleListOfUser(user, new String[0],
						defaultUserRole);
			} else {
				userStoreManager.addUser(user, defaultPassword,
						defaultUserRole, claims, null, true);
			}
			updateBAMStats(user, "ADD");
		} catch (UserStoreException e) {

			String msg = "Importing users to tenant "
					+ threadLocalCarbonContext.getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
		}

		return true;
	}

	public boolean importUsersTotheTenant(String[] users, String defaultPassword)
			throws UserManagementException {

		String[] defaultUserRoles = UserMgtUtil.getConfiguration()
				.getProperties(CloudConstants.TENANT_ROLES_DEFAULT_USER_ROLE);
		CarbonContext threadLocalCarbonContext = CarbonContext
				.getThreadLocalCarbonContext();

		UserStoreManager userStoreManager = null;
		try {
			userStoreManager = threadLocalCarbonContext.getUserRealm()
					.getUserStoreManager();
		} catch (UserStoreException e) {
			String msg = "Importing users to tenant "
					+ threadLocalCarbonContext.getTenantDomain()
					+ " failed due to " + e.getMessage();
			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}

		HashMap<String, String> claims = new HashMap<String, String>();
		//claims.put(CLAIMS_FIRSTLOGIN, "true");

		StringBuilder failedUsers = null;
		for (String user : users) {
			try {
				if (userStoreManager.isExistingUser(user)) {
					userStoreManager.updateRoleListOfUser(user, new String[0],
							defaultUserRoles);
					continue;
				}
				claims.put(CLAIMS_EMAILADDRESS, user);
				userStoreManager.addUser(user, defaultPassword,
						defaultUserRoles, claims, null, true);

				updateBAMStats(user, "ADD");

			} catch (UserStoreException e) {
				if (failedUsers == null) {
					failedUsers = new StringBuilder(user);
				} else {
					failedUsers.append("," + user);
				}
				String msg = "Importing users to tenant "
						+ threadLocalCarbonContext.getTenantDomain()
						+ " failed due to " + e.getMessage();
				log.error(msg);
			}
		}

		if (failedUsers != null) {
			String errorMsg = "Error importing users: "
					+ failedUsers.toString() + " to tenant "
					+ threadLocalCarbonContext.getTenantDomain();
			throw new UserManagementException(errorMsg);
		}

		return true;
	}

	public boolean checkUserActionPermission(String permission,String userName) {
		CarbonContext currentContext = CarbonContext
				.getThreadLocalCarbonContext();
		int tenantId = currentContext.getTenantId();
		
		try {
			boolean isUserAuthorized = UserMgtUtil.getRealmService()
					.getTenantUserRealm(tenantId).getAuthorizationManager()
					.isUserAuthorized(userName, permission, CloudConstants.PERMISSION_EXECUTE);
			return isUserAuthorized;

		} catch (UserStoreException e) {
			String msg = "Check user action due to " + e.getMessage();
			log.error(msg);

		}
		return false;

	}

	/**
	 * Update stats on BAM
	 * 
	 * @param userName
	 *            user name
	 * @param action
	 *            action
	 * @throws UserManagementException
	 *             //
	 */
	public void updateBAMStats(String userName, String action)
			throws UserManagementException {
		BamDataPublisher bamDataPublisher = new BamDataPublisher();
		try {
			bamDataPublisher.PublishTenantUserUpdateEvent(
					""
							+ CarbonContext.getThreadLocalCarbonContext()
									.getTenantId(), userName, action,
					System.currentTimeMillis());
		} catch (Exception e) {
			String msg = e.getMessage();
			if (("DELETE").equals(action)) {
				msg = "Failed to publish data to BAM on user delete event for tenant "
						+ CarbonContext.getThreadLocalCarbonContext()
								.getTenantDomain()
						+ " due to "
						+ e.getMessage();
			} else if (("ADD").equals(action)) {
				msg = "Failed to publish data to BAM on user add event for tenant "
						+ CarbonContext.getThreadLocalCarbonContext()
								.getTenantDomain()
						+ " due to "
						+ e.getMessage();

			}

			log.error(msg);
			throw new UserManagementException(e.getMessage(), e);
		}
	}

    /**
     * remove role from tenant
     *
     * @param roleToBeRemoved
     *            role to be removed from tenant
     * @return true if the operation is success
     * @throws UserStoreException
     * @throws UserManagementException
     */
    public boolean removeTenantRole (String roleToBeRemoved) throws UserStoreException, UserManagementException {

            CarbonContext currentContext = CarbonContext.getThreadLocalCarbonContext();
            int tenantId = currentContext.getTenantId();
             //remove role if tenant is not super tenant
            if(tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                UserStoreManager userStoreManager = UserMgtUtil.getRealmService()
                        .getTenantUserRealm(tenantId).getUserStoreManager();
                userStoreManager.deleteRole(roleToBeRemoved);
                return true;
            }
        return false;
    }

	/**
	 * This method obtains the tenant display name for the given tenant domain.
	 *
	 * @param tenantDomain
	 * @return the tenant's display name
	 * @throws UserManagementException
	 */
	public String getTenantDisplayName(String tenantDomain) throws UserManagementException {
		String tenantDisplayName = "";
		try {
			if (!"".equals(tenantDomain) && tenantDomain != null) {
				tenantDisplayName = dbManager.getTenantDisplayName(tenantDomain);
			}
		} catch (SQLException e) {
			String message =
					"An error occurred while retrieving the tenant display name for the tenant domain " +
					tenantDomain;
			log.error(message);
			throw new UserManagementException(e.getMessage(), e);
		}
		return tenantDisplayName;
	}
}
