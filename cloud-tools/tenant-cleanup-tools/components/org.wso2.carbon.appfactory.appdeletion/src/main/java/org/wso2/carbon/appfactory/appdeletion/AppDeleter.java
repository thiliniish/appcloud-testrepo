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
package org.wso2.carbon.appfactory.appdeletion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.appdeletion.internal.ServiceHolder;
import org.wso2.carbon.appfactory.appdeletion.util.AppDeleterConstants;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationInfoService;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dto.Application;
import org.wso2.carbon.appfactory.tenant.mgt.beans.UserInfoBean;
import org.wso2.carbon.appfactory.tenant.mgt.service.TenantManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppDeleter implements Runnable{

	private static final Log log = LogFactory.getLog(AppDeleter.class);

	public void run() {
		try {
            /*
            Although AppDeletionServerStartListener invokes this thread after listening to the server start, some services
            may not be fully started at the time of thread invocation.  Therefore the thread sleeps for a given number of
            seconds in order to make sure the server startup is completed.
            The sleep time period is entered as a system property.
            This sleep time is determined after inspecting the server start up time.
            */
			int napTime = Integer.parseInt(System.getProperty(AppDeleterConstants.NAP_TIME));
			log.info("App deletion thread sleeps for " + napTime + " milliseconds.");
			Thread.sleep(napTime);
			AppDeleter appDeleter = new AppDeleter();
			log.info("Deleting Apps started.");
			appDeleter.delete();
		} catch (UserStoreException e) {
			log.error("Error occurred while deleting apps.", e);
		} catch (InterruptedException e) {
			log.error("Error occurred while deleting apps.", e);
		}
	}

	/**
	 * Method to delete applications of given tenants
	 *
	 * @throws UserStoreException
	 */
    public void delete() throws UserStoreException {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
        //this variable keeps the number of attempted tenants
        int totalAttemptedTenants = 0;
        List<String> tenantDomains = readFile(System.getProperty(AppDeleterConstants.TENANT_FILE));
        //if an exception occurred or no tenants in the file
        if (tenantDomains.isEmpty()) {
            log.info("No tenants to be deleted.");
            return;
        } else {
            log.info("Total tenant domains in the list: " + tenantDomains.size());
		}
		//get the tenants for the given tenant domain names and delete their applications
        for (String tenantDomain : tenantDomains) {
            int tenantID;
            try {
                tenantID = tenantManager.getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.info("Error occurred while retrieving tenantId for domain name: " + tenantDomain, e);
                continue;
			}
			//if a tenant is not available for given tenant domain skip to the next iteration
			if (tenantID == MultitenantConstants.SUPER_TENANT_ID || tenantID == -1) {
				log.info("Tenant not found for domain name: " + tenantDomain);
				continue;
			}
			//increment the number of attempted tenants by 1
			totalAttemptedTenants++;
			log.info("App deletion started for tenant " + tenantDomain + "[" + tenantID + "]");
			try {
				//Start a new tenant flow
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext()
				                       .setTenantDomain(tenantDomain);
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantID);
				//Services are loaded from the service holder
				ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantID);
				ApplicationInfoService applicationInfoService = ServiceHolder.getAppinfoService();
				TenantManagementService tenantManagementService =
						ServiceHolder.getTenantManagementService();
				UserInfoBean[] userInfoBeans = tenantManagementService.getUsersofTenant();
				if (userInfoBeans == null) {
					log.info("No users for tenant : " + tenantDomain + "[" + tenantID + "]");
					continue;
				}
				//Applications are removed user-wise
				for (UserInfoBean user : userInfoBeans) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(user.getUserName());
                    log.info("Deleting apps of user :" + user.getUserName() + " of tenant " + tenantDomain + ".");
                    Application[] applications = applicationInfoService
                            .getApplicationsCreatedByUser(user.getUserName() + AppDeleterConstants.AT_SYMBOL + tenantDomain);
                    if (applications == null || applications.length == 0) {
                        log.info("No applications to be deleted for user :" + user.getUserName() + " of tenant "
                                + tenantDomain + ".");
                        continue;
					}
					for (Application application : applications) {
						log.info("Trying to delete application :" + application.getName());
                        applicationInfoService.deleteApplication(application,
                                user.getUserName() + AppDeleterConstants.AT_SYMBOL + tenantDomain, tenantDomain);
                        log.info("Deletion successful for application : " + application.getName() +
								" of user: " + user.getUserName() +" of tenant "+ tenantDomain + ".");
					}
					log.info("Application deletion successful for user :" + user.getUserName()+ " of tenant "+tenantDomain + ".");
				}
				log.info("Deletion successful for tenant " + tenantDomain + "[" + tenantID + "]");
			} catch (AppFactoryException e) {
				log.error("Error occurred while deleting applications for  " + tenantDomain, e);
			} catch (Exception e) {
				log.error(e);
			} finally {
				PrivilegedCarbonContext.endTenantFlow();
				log.info("Application deletion is completed for tenant " + tenantDomain + "[" + tenantID + "]");
			}
		}
		log.info("Application deletion is completed for " + totalAttemptedTenants + " tenants.");
	}

	/**
	 * Method to read tenant file.
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
