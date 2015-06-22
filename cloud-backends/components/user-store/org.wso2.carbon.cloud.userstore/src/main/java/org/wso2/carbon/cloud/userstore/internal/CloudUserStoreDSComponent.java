package org.wso2.carbon.cloud.userstore.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.userstore.CloudUserStoreManager;
import org.wso2.carbon.cloud.userstore.WSO2CloudUserStoreManager;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="cloud.user.store.manager.dscomponent" immediate=true
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class CloudUserStoreDSComponent {
    private static Log log = LogFactory.getLog(CloudUserStoreDSComponent.class);
    private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {
        UserStoreManager cloudUserStoreManager = new CloudUserStoreManager();
        ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), cloudUserStoreManager, null);

        UserStoreManager wSO2CloudUserStoreManager = new WSO2CloudUserStoreManager();
        ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), wSO2CloudUserStoreManager, null);

        log.info("CloudUserStoreDSComponent bundle activated successfully.");
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("CloudUserStoreDSComponent is deactivated ");
        }
    }

    protected RealmService getRealmService() {
        return realmService;
    }

    protected void setRealmService(RealmService rlmService) {
        realmService = rlmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        realmService = null;
    }
}
