package org.wso2.carbon.cloud.userstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.Map;

/**
 * Custom LDAP based user store implementation for wso2 cloud this class is used to replace the '@' symbol.
 */
public class WSO2CloudUserStoreManager extends CloudUserStoreManager {

    private static Log log = LogFactory.getLog(WSO2CloudUserStoreManager.class);

    public WSO2CloudUserStoreManager() {
    }

    public WSO2CloudUserStoreManager(RealmConfiguration realmConfig,
                                     Map<String, Object> properties, ClaimManager claimManager,
                                     ProfileConfigurationManager profileManager, UserRealm realm,
                                     Integer tenantId) throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm,
                tenantId);
    }

    public WSO2CloudUserStoreManager(RealmConfiguration realmConfig,
                                     ClaimManager claimManager,
                                     ProfileConfigurationManager profileManager)
            throws UserStoreException {
        super(realmConfig, claimManager, profileManager);
    }
    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {
        userName = doConvert(userName);
        if(!(isUserInRole(userName,"default")) && (getTenantId() == MultitenantConstants.SUPER_TENANT_ID) ){
            String[] roles = { "default" };
            updateRoleListOfUser(userName, null, roles);
            log.info("Default Role assigned for user : " + userName);
        }
        return super.doAuthenticate(userName, credential);
    }

    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {
        return super.isUserInRole(doConvert(userName), roleName);
    }

    private String doConvert(String userName) throws UserStoreException {
        StringBuilder convertedUser = new StringBuilder(userName);
        if (userName == null) {
            throw new UserStoreException("User name can not be null.");
        }
        if (userName.contains("@")) {
            int index = userName.indexOf("@");
            convertedUser.setCharAt(index, '.');
        }
        return convertedUser.toString();
    }
}
