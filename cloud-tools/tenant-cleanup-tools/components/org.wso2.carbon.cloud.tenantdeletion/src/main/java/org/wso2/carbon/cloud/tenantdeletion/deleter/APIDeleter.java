/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.deleter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.cloud.tenantdeletion.utils.TenantDeletionMap;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Map;
import java.util.Set;

/**
 * Represents the tenant api deletion operation
 */
public class APIDeleter {

    private static final Log LOG = LogFactory.getLog(APIDeleter.class);
    private Registry registry;
    private String tenantDomain;
    private int tenantID;

    /**
     * Method to startDeletion APIs of given tenants.
     *
     * @param deletionLimit Number of tenants to be cleaned up in a single round
     */
    public void delete(String deletionLimit) {
        Map<String, Integer> tenantMap;
        boolean deletionCompleted = TenantDeletionMap.getInstance().checkDeletionCompleted(DeletionConstants.API);
        if (!deletionCompleted) {
            if (StringUtils.isNotEmpty(deletionLimit)) {
                int limit = Integer.parseInt(deletionLimit);
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.API, limit);
            } else {
                tenantMap = TenantDeletionMap.getInstance().getInactiveTenantMap(DeletionConstants.API);
            }
            if (tenantMap != null && !tenantMap.isEmpty()) {
                startDeletion(tenantMap);
                LOG.info("Api deletion completed for all the " + tenantMap.size() + " tenants.");
            } else {
                LOG.info("No APIs to be deleted");
            }
        } else {
            LOG.info("Api Deletion Completed Already");
        }
        DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.API);
    }

    /**
     * Deletion start method for the class.
     * Initializes the tenant flow and starts deleting the APIs
     *
     * @param tenantMap Map of tenant Domain, tenant Id to be delete APIs
     */
    private void startDeletion(Map<String, Integer> tenantMap) {
        LOG.info("Api deletion started for " + tenantMap.size() + " tenants.");

        for (Map.Entry<String, Integer> entry : tenantMap.entrySet()) {
            tenantDomain = entry.getKey();
            tenantID = entry.getValue();
            LOG.info("Api deletion started for tenant: " + tenantDomain + "[" + tenantID + "]");
            try {
                //Start conf new tenant flow
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantID);
                String adminName = ServiceHolder.getInstance().getRealmService().getTenantUserRealm(tenantID)
                                                .getRealmConfiguration().getAdminUserName();
                ServiceHolder.getInstance().getTenantRegLoader().loadTenantRegistry(tenantID);
                //Get tenant's api artifacts from the registry
                registry =
                        ServiceHolder.getInstance().getRegistryService().getGovernanceUserRegistry(adminName, tenantID);
                GenericArtifactManager manager =
                        new GenericArtifactManager(registry, DeletionConstants.LOWERCASEAPI);

                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                GenericArtifact[] artifacts = manager.getAllGenericArtifacts();
                if (!registry.resourceExists(APIConstants.API_ROOT_LOCATION) || ArrayUtils.isEmpty(artifacts)) {
                    LOG.info("No apis are available for tenant: " + tenantDomain + "[" + tenantID + "]");
                    setDeletionStatus(tenantDomain, DeletionConstants.DELETION_SUCCESS_STATUS);
                    continue;
                }
                for (GenericArtifact artifact : artifacts) {
                    deleteAPI(artifact);
                }
                setDeletionStatus(tenantDomain, DeletionConstants.DELETION_SUCCESS_STATUS);
                LOG.info("Tenant API Deletion is completed for  tenant: " + tenantDomain + "[" + tenantID + "]");
            } catch (RegistryException | UserStoreException e) {
                LOG.error("Error while getting artifacts for  " + tenantDomain, e);
                setDeletionStatus(tenantDomain, DeletionConstants.DELETION_ERROR_STATUS);
            } catch (Exception e) {
                LOG.error(e);
                setDeletionStatus(tenantDomain, DeletionConstants.DELETION_ERROR_STATUS);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * API Delete method for a specified Artifact for the tenant
     *
     * @param artifact GenericArtifact element
     */
    private void deleteAPI(GenericArtifact artifact) {
        try {
            String providerName;
            API api = APIUtil.getAPI(artifact, registry);
            String apiId = api.getId().toString();
            providerName = api.getId().getProviderName().replace("-AT-", DeletionConstants.AT_SYMBOL);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(providerName);
            LOG.info(
                    "Api provider " + providerName + " is retrieved for " + apiId + "of tenant: " + tenantDomain + "[" +
                    tenantID + "]");
            Set<Subscriber> subscribers = apiProvider.getSubscribersOfAPI(api.getId());
            //Remove subscriptions if there are any.
            if (!subscribers.isEmpty()) {
                for (Subscriber subscriber : subscribers) {
                    LOG.info("Subscription deletion started for " + apiId + "of tenant: " + tenantDomain +
                             "[" + tenantID + "]");
                    Set<SubscribedAPI> subscribedAPIs =
                            APIManagerFactory.getInstance().getAPIConsumer(providerName).getSubscribedAPIs(subscriber);
                    for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                        //If the subscribed api is the api under consideration, startDeletion the
                        // application(this will remove subscriptions)
                        if (subscribedAPI.getApiId().toString().equals(apiId)) {
                            Application application = subscribedAPI.getApplication();
                            APIManagerFactory.getInstance().getAPIConsumer(providerName).removeApplication(application);
                        }
                    }
                    LOG.info("Subscription deletion completed for " + apiId + "of tenant: " + tenantDomain + "[" +
                             tenantID + "]");
                }
            } else {
                LOG.info("No subscriptions for " + apiId + "of tenant: " + tenantDomain + "[" + tenantID +
                         "]");
            }
            //Delete the api after subscriptions and applications are deleted.
            apiProvider.deleteAPI(api.getId());
            LOG.info("Deletion successful for api :" + apiId + " of tenant: " + tenantDomain + "[" + tenantID + "]");

            /**
             * The program will continue if there is an exception. The reason is there can be corrupted tenants or
             * apis which tend throw exceptions. The deletion process should not stop in such scenarios.
             */

            //Sleep for the given nap time before starting next to avoid connection exhaustion.
            String napTime = System.getProperty(DeletionConstants.API_DELETION_NAP_TIME);
            if (StringUtils.isNotEmpty(napTime)) {
                Thread.sleep(Integer.parseInt(napTime));
            } else {
                Thread.sleep(DeletionConstants.NAP_TIME);
            }
        } catch (APIManagementException e) {
            LOG.error("Error while deleting apis of tenant: " + tenantDomain + "[" + tenantID + "]", e);
            setDeletionStatus(tenantDomain, DeletionConstants.DELETION_ERROR_STATUS);
        } catch (InterruptedException e) {
            LOG.error("Error while sleeping the thread  ", e);
            setDeletionStatus(tenantDomain, DeletionConstants.DELETION_ERROR_STATUS);
        }
    }

    /**
     * Updates database tenant deletion flag for given tenant domain
     *
     * @param tenantDomain tenant domain
     * @param status       deletion status (0,1,2)
     */
    private void setDeletionStatus(String tenantDomain, int status) {
        DataAccessManager.getInstance().raiseDeletionFlag(DeletionConstants.API, tenantDomain, status);
    }
}
