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
 * Test cases will cover view account info, get billing invoices, update contact info and view payment details.
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
    @Test(description = "View Account information of the Billing enabled tenant")
    public void viewAccountInfo() throws Exception {
        log.info("Started running test case billing account information.");
        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);

        billingAccountInfo = getAccountInfo();
        Assert.assertEquals(billingAccountInfo.getString(CloudIntegrationConstants.SUCCESS),
                            CloudIntegrationConstants.TRUE, "Value mismatch should be true.");
    }

    private JSONObject getAccountInfo() throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "get-billing-account");
        String accountInfoUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_ACCOUNT_INFO_URL_SFX;
        Map result = HttpHandler.doPostHttps(accountInfoUrl, params, authenticatorClient.getSessionCookie(), false);
        Assert.assertNotNull(result, "User Should have a billing account");
        return new JSONObject(result.get(CloudIntegrationConstants.RESPONSE).toString());
    }

    /**
     * Check if invoices are generated to a specific tenant
     *
     * @throws Exception
     */
    @Test(description = "Get invoices of the Billing enabled tenant", dependsOnMethods = { "viewAccountInfo" })
    public void viewInvoices() throws Exception {
        log.info("Started running test case invoice information.");
	    JSONObject accountInfoArray = (JSONObject) billingAccountInfo.get("data");
	    JSONObject invoiceArray = accountInfoArray.getJSONObject("invoicesInformation");
        Assert.assertNotNull(invoiceArray, "Account dose not contain any invoices");
        //for (int i = 0; i < invoiceArray.length(); i++) {
	        Iterator keys = invoiceArray.keys();
	        while (keys.hasNext()) {
            Assert.assertNotNull(accountInfoArray.getJSONObject("invoicesInformation")., "Invoice Number cannot" +
                                                                                               " be empty");
            String invoiceId = invoiceArray.getJSONObject(i).get("InvoiceId").toString();
            String invoiceNumber = invoiceArray.getJSONObject(i).get("invoiceNumber").toString();

            Map<String, String> params = new HashMap<>();
            params.put("id", invoiceId);
            String getInvoiceUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_INVOICE_URL_SFX;
            Map result = HttpHandler.doPostHttps(getInvoiceUrl, params, authenticatorClient.getSessionCookie(), false);
            Assert.assertNotNull(result, "User Should have a billing account");
            JSONObject invoiceObject = new JSONObject(result.get(CloudIntegrationConstants.RESPONSE).toString());
            Assert.assertEquals(invoiceObject.getString("invoiceNumber"), invoiceNumber,
                                "invoice number should be equal");
        }
    }

    /**
     * Update the user contact details
     *
     * @throws Exception
     */
    @Test(description = "Update the contact information of the Billing enabled tenant", dependsOnMethods = {
            "viewAccountInfo" })
    public void updateContactInfo() throws Exception {
        log.info("Started running test case update the contact information.");
        String errorMessage = "Error updating the user contact information.";
        JSONObject newContactInfo = new JSONObject();
        newContactInfo.put("firstName", "firstName33");
        newContactInfo.put("lastName", "lastName33");
        newContactInfo.put("address1", "address133");
        //newContactInfo.put("address2", "address233");
        newContactInfo.put("city", "city33");
        //newContactInfo.put("state", "state33");
        //newContactInfo.put("postalcode", "zipCode33");
        newContactInfo.put("country", "Sri Lanka33");
        newContactInfo.put("email", "test2@wso2.com33");

        Assert.assertEquals(invokeUpdateContactInfo(newContactInfo), "Your contact information is successfully added",
                            errorMessage);

        JSONObject updatedAccountInfo = getAccountInfo();
	    //log.info("-------------------------------11111 updatedAccountInfo :: "+updatedAccountInfo.toString());
	    /*JSONObject updatedData = updatedAccountInfo.getJSONObject("data");
	    JSONObject updatedContactInfo = updatedData.getJSONObject("contactDetails");*/
	    JSONObject updatedContactInfo = updatedAccountInfo.getJSONObject("data");
	    //log.info("-------------------------------22222 data :: "+updatedContactInfo.toString());
	    log.info("-------------------------------33333 contactDetails :: "+updatedContactInfo.getJSONObject("contactDetails"));
        Iterator keys = newContactInfo.keys();
        //verifying if the update is success
        log.info("----------------------------------------------------------");
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = newContactInfo.getString(key);
            /*if (key.equalsIgnoreCase("email")) {
                key = "workEmail";
            }*/
            if (!key.equalsIgnoreCase("responseFrom")) {
	            log.info("----- "+(updatedContactInfo.getJSONObject("contactDetails")).getString(key)+"  :: "+value);
                Assert.assertEquals((updatedContactInfo.getJSONObject("contactDetails")).getString(key), value,
                                    (errorMessage + key));
            }
        }
	    log.info("----------------------------------------------------------");
    }

    private String invokeUpdateContactInfo(JSONObject contactInfo) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "createAccount");
        params.put(CloudIntegrationConstants.PARAMETER_KEY_RESPONSE_FROM, "Edit_User_Info");
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
        Map result =
                HttpHandler.doPostHttps(addAccountDetailsUrl, params, authenticatorClient.getSessionCookie(), false);
	    log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< response : "+result.get(CloudIntegrationConstants.RESPONSE).toString());
        return result.get(CloudIntegrationConstants.RESPONSE).toString();
    }

    /**
     * View payments details of the billing account
     *
     * @throws Exception
     */
    @Test(description = "View payments of the Billing enabled tenant", dependsOnMethods = { "viewAccountInfo" })
    public void viewPayments() throws Exception {
        log.info("Started running test case payments information.");
        JSONArray paymentsArray = (JSONArray) billingAccountInfo.get("payments");
        Assert.assertNotNull(paymentsArray, "Account dose not contains any payments.");
        for (int i = 0; i < paymentsArray.length(); i++) {
            Assert.assertNotNull(paymentsArray.getJSONObject(i).get("paidInvoices"),
                                 "Account does not contains any paid invoices.");
        }
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        // Reverting the changes
        if (billingAccountInfo != null) {
            String response = invokeUpdateContactInfo(billingAccountInfo.getJSONObject("contactDetails"));
            String errorMsg = "Error occurred while updating the billing contact Info.";
            Assert.assertEquals(response, "Your contact information is successfully added", errorMsg);
        }
        authenticatorClient.logout();
        super.cleanup();
    }
}
