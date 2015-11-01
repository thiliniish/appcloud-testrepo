/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.apimgt.apideletion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.cloud.apimgt.apideletion.internal.ServiceHolder;
import org.wso2.carbon.cloud.apimgt.apideletion.util.ApiDeleterConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class APIDeleter implements Runnable {

    private static final Log log = LogFactory.getLog(APIDeleter.class);
    @Override
    public void run() {
        try {
            /*
            Although ApiDeletionServerStartListener invokes this thread after listening to the server start, some services
            may not be fully started at the time of thread invocation. Therefore the thread sleeps for a given number of
            seconds in order to make sure the server startup is completed.
            The sleep time period is entered as a system property.
            This sleep time is determined after inspecting the server start up time.
            */
            int sleepTime = Integer.parseInt(System.getProperty(ApiDeleterConstants.NAP_TIME));
            log.info("API deletion thread sleeping for " + sleepTime + " milliseconds.");
            Thread.sleep(sleepTime);
            APIDeleter apiDeleter = new APIDeleter();
            log.info("Deleting APIs Started.");
            apiDeleter.delete();
        } catch (UserStoreException e) {
            log.error("Error occurred while deleting the apis.", e);
        } catch (InterruptedException e) {
            log.error("Error occurred while deleting the apis.", e);
        }
    }

    /**
     * Method to delete APIs of given tenants.
     *
     * @throws UserStoreException
     */
    private void delete() throws InterruptedException, UserStoreException {
        //read and get tenant domains from the file.
        List<String> tenantDomains = readFile(System.getProperty(ApiDeleterConstants.TENANT_FILE));
        Map<String, Integer> tenantDomainIdMap = new HashMap<String, Integer>();
        //if an exception occurred or no tenants in the file.
        if (tenantDomains.isEmpty()) {
            log.info("No tenants to be deleted.");
            return;
        }
        //load super tenant in the new thread to get tenants for tenant domain names.
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
            //retrieve tenants for tenant domain names.
            for (String tenantDomain : tenantDomains) {
                int tenantID = tenantManager.getTenantId(tenantDomain);
                if (tenantID != MultitenantConstants.SUPER_TENANT_ID && tenantID != -1) {
                    tenantDomainIdMap.put(tenantDomain, tenantID);
                } else {
                    log.warn("No tenant found for domain name: " + tenantDomain + ".");
                }
            }
            log.info("Size of the tenant list = " + tenantDomainIdMap.size() + ".");
        } catch (UserStoreException e) {
            log.error("Error occurred while retrieving tenants.", e);
            throw e;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        //if no tenant retrieved for given tenant domains, return.
        if (tenantDomainIdMap.isEmpty()) {
            log.info("No tenants to be deleted.");
            return;
        }
        log.info("Api deletion started for " + tenantDomainIdMap.size() + " tenants.");
        for (Map.Entry<String, Integer> entry : tenantDomainIdMap.entrySet()) {
            String tenantDomain = entry.getKey();
            int tenantID = entry.getValue();
            log.info("Api deletion started for tenant " + tenantDomain + "[" + tenantID + "]");
            try {
                //Start a new tenant flow
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantID);
                String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantID).getRealmConfiguration()
                        .getAdminUserName();
                ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantID);
                //get tenant's api artifacts from the registry
                Registry registry = ServiceHolder.getRegistryService().getGovernanceUserRegistry(adminName, tenantID);
                GenericArtifactManager manager = new GenericArtifactManager(registry, ApiDeleterConstants.API);
                GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
                GenericArtifact[] artifacts = manager.getAllGenericArtifacts();
                if (!registry.resourceExists(APIConstants.API_ROOT_LOCATION) || (artifacts == null) || (artifacts.length
                        == 0)) {
                    log.info("No apis are available for tenant " + tenantDomain + "[" + tenantID + "]");
                    continue;
                }
                for (GenericArtifact artifact : artifacts) {
                    try {
                        String apiId;
                        String providerName;
                        API api = APIUtil.getAPI(artifact, registry);
                        apiId = api.getId().toString();
                        providerName = api.getId().getProviderName().replace("-AT-", ApiDeleterConstants.AT_SYMBOL);
                        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(providerName);
                        log.info("Api provider " + providerName + " is retrieved for " + apiId + "of tenant "
                                + tenantDomain + "[" + tenantID + "]");
                        Set<Subscriber> subscribers = apiProvider.getSubscribersOfAPI(api.getId());
                        //remove subscriptions if there are any.
                        if (!subscribers.isEmpty()) {
                            Iterator subscribersIterator = subscribers.iterator();
                            while (subscribersIterator.hasNext()) {
                                log.info("Subscription deletion started for " + apiId + "of tenant " + tenantDomain + "["
                                                + tenantID + "]");
                                Subscriber subscriber = (Subscriber) subscribersIterator.next();
                                Set<SubscribedAPI> subscribedAPIs = APIManagerFactory.getInstance()
                                        .getAPIConsumer(providerName).getSubscribedAPIs(subscriber);
                                Iterator subscribedApiIterator = subscribedAPIs.iterator();
                                while (subscribedApiIterator.hasNext()) {
                                    SubscribedAPI subscribedAPI = (SubscribedAPI) subscribedApiIterator.next();
                                    //if the subscribed api is the api under consideration, delete the
                                    // application(this will remove subscriptions)
                                    if (subscribedAPI.getApiId().toString().equals(apiId)) {
                                        Application application = subscribedAPI.getApplication();
                                        APIManagerFactory.getInstance().getAPIConsumer(providerName)
                                                .removeApplication(application);
                                    }
                                }
                                log.info("Subscription deletion completed for " + apiId + "of tenant " + tenantDomain
                                        + "[" + tenantID + "]");
                            }
                        } else {
                            log.info("No subscriptions for " + apiId + "of tenant " + tenantDomain + "[" + tenantID + "]");
                        }
                        //delete the api after subscriptions and applications are deleted.
                        apiProvider.deleteAPI(api.getId());
                        log.info("Deletion successful for api :" + apiId + " of tenant " + tenantDomain + "[" + tenantID
                                + "]");
                     /*
                      The program will continue if there is an exception. The reason is there can be corrupted tenants or
                      apis which tend throw exceptions. The deletion process should not stop in such scenarios.
                      */
                    } catch (APIManagementException e) {
                        log.error("Error while deleting apis of tenant " + tenantDomain + "[" + tenantID + "]", e);
                    } catch (Exception e) {
                        log.error("Unexpected error occurred while deleting apis" + " of tenant " + tenantDomain + "["
                                + tenantID + "]", e);
                    }
                    //sleep 5 seconds before starting next to avoid connection exhaustion.
                    Thread.sleep(5000);
                }
            } catch (RegistryException e) {
                log.error("Error while getting artifacts for  " + tenantDomain, e);
            } catch (UserStoreException e) {
                log.error("Error while getting artifacts for  " + tenantDomain, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
                log.info("Tenant API Deletion is completed for  tenant " + tenantDomain + "[" + tenantID + "]");
            }
        }
        log.info("Api deletion completed for all the " + tenantDomainIdMap.size() + " tenants.");
    }

    /**
     * Method to read the tenant file.
     *
     * @param tenantFile path to the tenant file
     */

    private List<String> readFile(String tenantFile) {
        List<String> tenants = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(tenantFile));
            String line;
            while ((line = reader.readLine()) != null) {
                tenants.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            log.error("Could not find the tenant file at the given location.", e);
        } catch (IOException e) {
            log.error("Input/Output error occurred while reading the tenant file.", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred while closing the buffered reader.", e);
            }
        }
        return tenants;
    }
}