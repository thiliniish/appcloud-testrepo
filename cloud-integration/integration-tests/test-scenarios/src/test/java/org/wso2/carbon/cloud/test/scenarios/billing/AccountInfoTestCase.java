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

package org.wso2.carbon.cloud.test.scenarios.billing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationConstants;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTest;
import org.wso2.carbon.cloud.integration.test.utils.CloudIntegrationTestUtils;
import org.wso2.carbon.cloud.integration.test.utils.external.HttpHandler;
import org.wso2.carbon.cloud.integration.test.utils.restclients.JaggeryAppAuthenticatorClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is to test the billing components account information.
 * Test cases will cover getting view account info, get billing invoices,update contact info and view usage.
 */
public class AccountInfoTestCase extends CloudIntegrationTest {

    private static final Log log = LogFactory.getLog(AccountInfoTestCase.class);
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private boolean loginStatus;
    private JSONObject billingAccountInfo;

    /**
     * Before test tenant user will get authenticated.
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
    }

    /**
     * Check the billing account details of a specific tenant
     *
     * @throws Exception
     */
    @Test(description = "View Account of the Billing enabled tenant")
    public void viewAccountInfo() throws Exception {
        log.info("started running test case billing account information");
        Assert.assertTrue(loginStatus, "Tenant login failed.");

        billingAccountInfo = getAccountInfo();
        Assert.assertEquals(billingAccountInfo.getString("success"), "true", "Value mismatch should be true.");
    }

    private JSONObject getAccountInfo() throws IOException, JSONException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "get-billing-account");
        String accountInfoUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_ACCOUNT_INFO_URL_SFX;
        Map result = HttpHandler.doPostHttps(accountInfoUrl, params, authenticatorClient.getSessionCookie());
        Assert.assertNotNull(result, "User Should have a billing account");
        return new JSONObject(result.get(CloudIntegrationConstants.RESPONSE).toString());
    }

    /**
     * Check if invoices are generated to a specific tenant
     *
     * @throws Exception
     */
    @Test(description = "get invoices of the Billing enabled tenant", dependsOnMethods = { "viewAccountInfo" })
    public void viewInvoices() throws Exception {
        log.info("started running test case invoice information");
        JSONArray invoiceArray = (JSONArray) billingAccountInfo.get("invoices");
        Assert.assertNotNull(invoiceArray, "Account dose not contain any invoices");
        for (int i = 0; i < invoiceArray.length(); i++) {
            Assert.assertNotNull(invoiceArray.getJSONObject(i).get("id"), "Invoice Number cannot be empty");
            String invoiceId = invoiceArray.getJSONObject(i).get("id").toString();
            String invoiceNumber = invoiceArray.getJSONObject(i).get("invoiceNumber").toString();

            Map<String, String> params = new HashMap<String, String>();
            params.put("id", invoiceId);
            String getInvoiceUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_INVOICE_URL_SFX;
            Map result = HttpHandler.doPostHttps(getInvoiceUrl, params, authenticatorClient.getSessionCookie());
            Assert.assertNotNull(result, "User Should have a billing account");
            JSONObject invoiceObject = new JSONObject(result.get(CloudIntegrationConstants.RESPONSE).toString());
            Assert.assertEquals(invoiceObject.getString("invoiceNumber"), invoiceNumber,
                                "invoice number should be equal");
        }
    }

    /**
     * Check if user contact details are properly updated in the zuora side
     *
     * @throws Exception
     */
    @Test(description = "get invoices of the Billing enabled tenant", dependsOnMethods = { "viewAccountInfo" })
    public void updateContactInfo() throws Exception {

        String errorMessage = "Error updating the contact information ";
        JSONObject newContactInfo = new JSONObject();
        newContactInfo.put("firstName", "firstName");
        newContactInfo.put("lastName", "lastName");
        newContactInfo.put("address1", "address1");
        newContactInfo.put("address2", "address2");
        newContactInfo.put("city", "city");
        newContactInfo.put("state", "state");
        newContactInfo.put("zipCode", "zipCode");
        newContactInfo.put("country", "Sri Lanka");
        newContactInfo.put("email", "test2@wso2.com");

        Assert.assertEquals(invokeUpdateContactInfo(newContactInfo), "Your contact information is successfully added",
                            errorMessage);

        JSONObject updatedContactInfo = getAccountInfo();
        Iterator keys = newContactInfo.keys();
        //verifying if the update is success
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = newContactInfo.getString(key);
            if (key.equalsIgnoreCase("email")) {
                key = "workEmail";
            }
            if (!key.equalsIgnoreCase("responseFrom")) {
                Assert.assertEquals((updatedContactInfo.getJSONObject("billToContact")).getString(key), value,
                                    (errorMessage + key));
            }
        }

    }

    private String invokeUpdateContactInfo(JSONObject contactInfo) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "createAccount");
        params.put("responseFrom", "Edit_User_Info");
        String organizationName =
                CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.TENANT_ADMIN_DOMAIN);
        params.put("orgName", organizationName);
        Iterator keys = contactInfo.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = contactInfo.getString(key);
            if (key.equalsIgnoreCase("workEmail")) {
                key = "email";
            }
            params.put(key, value);
        }

        String addAccountDetailsUrl =
                cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_ACCOUNT_DETAILS_ADD_URL_SFX;
        Map result = HttpHandler.doPostHttps(addAccountDetailsUrl, params, authenticatorClient.getSessionCookie());
        return result.get(CloudIntegrationConstants.RESPONSE).toString();
    }

    @AfterClass(alwaysRun = true)
    public void unDeployService() throws Exception {
        //reverting the changers
        if (billingAccountInfo != null) {
            String response = invokeUpdateContactInfo(billingAccountInfo.getJSONObject("billToContact"));
            if (!response.equalsIgnoreCase("Your contact information is successfully added")) {
                String msg = "Error has occurred when updating the billing contact Info ";
                log.error(msg);
                throw new Exception(msg);
            }
        }
        authenticatorClient.logout();
        super.cleanup();
    }
}
