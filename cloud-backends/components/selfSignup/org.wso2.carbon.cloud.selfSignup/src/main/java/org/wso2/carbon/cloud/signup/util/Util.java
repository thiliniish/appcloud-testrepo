/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.signup.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.cloud.signup.configReader.ConfigFileReader;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.cloud.signup.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Consists of all the util methods
 */

public class Util implements Serializable {
    private static final Log log = LogFactory.getLog(Util.class);

    /**
     * This method generates a unique identifier for the user
     *
     * @return the unique identifier.
     */
    public String generateUUID()

    {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        return uuidString;
    }

    /**
     * This method splits the string using the forward slash.
     *
     * @param fullName
     * @return
     */
    public String splitSlashes(String fullName) {

        String[] userArray = fullName.split("/");
        String name = userArray[1];
        return name;
    }

    /**
     * This method manipulates the roles sent from the api manager configuration and returns the list of roles
     *
     * @param roleList is the list of roles assigned by the tenant to the user
     * @return the manipulated roles array
     */
    public ArrayList<String> getRoles(List<String> roleList) {
        int length = roleList.size();
        ArrayList<String> rolesArrayList = new ArrayList<String>();
        Util utilObject = new Util();

        for (int counter = 0; counter < length; counter++) {

            String item = roleList.get(counter);
            String wordToReturn = utilObject.splitSlashes(item);
            rolesArrayList.add(counter, wordToReturn);

        }

        return rolesArrayList;
    }

    /**
     * Get the custom store url of given tenant.
     *
     * @param tenantDomain Tenant Domain
     * @return if custom URL available then return custom URL else return null
     * @throws WorkflowException
     */
    private String getCustomStoreURL(String tenantDomain) throws WorkflowException {
        String customURL = null;
        try {
            //get custom url mapping from super tenant registry
            int tenantId = MultitenantConstants.SUPER_TENANT_ID;
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(
                    tenantId);
            String path = "customurl/api-cloud/" + tenantDomain + "/urlMapping/" + tenantDomain;
            if (registry.resourceExists(path)) {
                Resource resource = registry.get(path);
                String jsonData = new String((byte[]) resource.getContent());

                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
                JSONObject storeConfig = (JSONObject) jsonObject.get("store");
                customURL = storeConfig.get("customUrl").toString();
            }
        } catch (RegistryException e) {
            String errorMsg = "Error while reading custom url config from registry for tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg, e);
        } catch (ParseException e) {
            String errorMsg = "Error while parsing custom url config of tenant :" + tenantDomain;
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg, e);
        }
        return customURL;
    }

    /**
     * Get the confirmation link to send in mail.
     *
     * @param tenantDomain Tenant Domain
     * @param uuid         UUID
     * @return Confirmation Link
     * @throws WorkflowException
     */
    public String getConfirmationLink(String tenantDomain, String uuid) throws WorkflowException {
        String link = ConfigFileReader.retrieveConfigAttribute("URLs", "cloudMgtVerificationUrl");
        //if custom store url available construct the link using it.
        String customStoreURL = getCustomStoreURL(tenantDomain);
        String defaultStoreURL = ConfigFileReader.retrieveConfigAttribute("URLs", "defaultStoreUrl");
        if (customStoreURL != null && !customStoreURL.equals(defaultStoreURL)) {
            String verificationPagePath = ConfigFileReader.retrieveConfigAttribute("URLs", "verificationPagePath");
            link = "https://" + customStoreURL + verificationPagePath;
        }
        link = link + "?confirmation=" + uuid + "&isStoreInvitee=true&tenant=" + tenantDomain;
        return link;
    }
}
