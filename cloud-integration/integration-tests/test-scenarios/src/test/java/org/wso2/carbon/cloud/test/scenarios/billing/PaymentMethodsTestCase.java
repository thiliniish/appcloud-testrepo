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

import java.util.HashMap;
import java.util.Map;

/**
 * This is to test adding, viewing, changing and deleting
 * functionalities of the cloud billing payment method feature.
 * Since it checks only the added payments it's require to have a valid payment
 * subscription for that tenant. Also two payments methods to test the change payment method functionality.
 */
public class PaymentMethodsTestCase extends CloudIntegrationTest {

    private static final Log log = LogFactory.getLog(PaymentMethodsTestCase.class);
    private JaggeryAppAuthenticatorClient authenticatorClient;
    private boolean loginStatus;

    /**
     * Before test tenant user will get authenticated.
     *
     * @throws Exception
     */
    @BeforeClass(alwaysRun = true) public void setEnvironment() throws Exception {
        authenticatorClient = new JaggeryAppAuthenticatorClient(cloudMgtServerUrl);
        loginStatus = authenticatorClient.login(tenantAdminUserName, tenantAdminPassword);
    }

    /**
     * Since it's require to load the iFrame (selenium web drive ui tests)
     * to add the credit card details here it's only checking the
     * parameters generated to query the iframe
     *
     * @throws Exception
     */
    @Test(description = "This will check generated parameters to get the iframe.") public void addPaymentMethod()
            throws Exception {
        log.info("Started running test case parameter generation of payment methods to get the iframe.");
        String serviceId = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_SERVICE_ID);
        String productPlanId = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_PRODUCT_RATE_PLAN_ID);

        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "generateParams");
        params.put(CloudIntegrationConstants.PARAMETER_KEY_SERVICE_ID, serviceId);
        params.put(CloudIntegrationConstants.PARAMETER_KEY_PRODUCT_RATE_PLAN_ID, productPlanId);
        String paymentMethodAddUrl = cloudMgtServerUrl +
                                     CloudIntegrationConstants.CLOUD_BILLING_PAYMENT_METHOD_ADD_URL_SFX;
        Map resultMap = HttpHandler
                .doPostHttps(paymentMethodAddUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj =
                new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());

        //validate parameter values
        Assert.assertNotNull(resultObj.getString("token"));
        Assert.assertNotNull(resultObj.getString("url"));
        Assert.assertNotNull(resultObj.getString("tenantId"));
        Assert.assertNotNull(resultObj.getString("paymentGateway"));
        Assert.assertNotNull(resultObj.getString("key"));
        Assert.assertNotNull(resultObj.getString("id"));
        Assert.assertNotNull(resultObj.getString("signature"));
        Assert.assertNotNull(resultObj.getString("field_passthrough1"));
        Assert.assertNotNull(resultObj.getString("field_passthrough2"));
        Assert.assertNotNull(resultObj.getString("field_passthrough3"));
        Assert.assertNotNull(resultObj.getString("style"));
        Assert.assertNotNull(resultObj.getString("locale"));
        Assert.assertNotNull(resultObj.getString("retainValues"));
        Assert.assertNotNull(resultObj.getString("submitEnabled"));
    }

    /**
     * Check already existing payment method information of the tenant
     *
     * @throws Exception
     */
    @Test(description = "View payment methods of the tenant") public void viewPaymentMethods()
            throws Exception {
        log.info("Started running test case view payment method.");
        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "get-payment-methods");
        String paymentMethodInfoUrl = cloudMgtServerUrl +
                                      CloudIntegrationConstants.CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX;
        Map resultMap = HttpHandler
                .doPostHttps(paymentMethodInfoUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj =
                new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.getString(CloudIntegrationConstants.SUCCESS), CloudIntegrationConstants.TRUE,
                            "Value mismatch should be true.");
        Assert.assertNotNull(resultObj.getJSONArray("creditCards"), "Json array of credit cards should not be null.");
    }

    /**
     * Change the payment method. Set the secondary payment method as default one.
     *
     * @throws Exception
     */
    @Test(priority = 2, description = "Change payment method.")
    public void changePaymentMethod() throws Exception {
        log.info("Started running test case change payment method.");
        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
        String errorMessage = "Error changing the payment method.";
        String secondaryPaymentMethodId = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_SECONDARY_PAYMENT_METHOD_ID);
        String result = setSecondaryPaymentMethodAsDefault(secondaryPaymentMethodId);
        Assert.assertEquals(result, CloudIntegrationConstants.TRUE, errorMessage);
    }

    /**
     * This method will set the secondary payment method as default payment method.
     *
     * @param paymentMethodId credit card id
     * @return status of the setDefaultMethod action
     * @throws Exception
     */
    private String setSecondaryPaymentMethodAsDefault(String paymentMethodId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "setDefaultMethod");
        params.put("paymentMethodId", paymentMethodId);
        String paymentMethodInfoUrl =
                cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX;
        Map resultMap = HttpHandler.doPostHttps(paymentMethodInfoUrl, params, authenticatorClient.getSessionCookie(), false);
        return resultMap.get(CloudIntegrationConstants.RESPONSE).toString();
    }

    /**
     * Delete a payment method
     *
     * @throws Exception
     */
    @Test(priority = 3, description = "Delete payment method.")
    public void deletePaymentMethod() throws Exception {
        log.info("Started running test case delete payment method.");
        String paymentMethodId = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_SECONDARY_PAYMENT_METHOD_ID);
        String errorMessage = "Error removing the payment method.";
        Assert.assertTrue(loginStatus, CloudIntegrationConstants.LOGIN_ERROR_MESSAGE);
        Map<String, String> params = new HashMap<>();
        params.put(CloudIntegrationConstants.PARAMETER_KEY_ACTION, "removePaymentMethod");
        params.put("paymentMethodId", paymentMethodId);
        String paymentMethodRemovalUrl =
                cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX;
        Map resultMap =
                HttpHandler.doPostHttps(paymentMethodRemovalUrl, params, authenticatorClient.getSessionCookie(), false);
        JSONObject resultObj = new JSONObject(resultMap.get(CloudIntegrationConstants.RESPONSE).toString());
        Assert.assertEquals(resultObj.getString(CloudIntegrationConstants.SUCCESS), CloudIntegrationConstants.TRUE,
                            errorMessage);
    }

    @AfterClass(alwaysRun = true) public void destroy() throws Exception {
        log.info("Making primary payment method back to default payment method.");
        String errorMessage = "Error Making primary payment method back to default payment method.";
        String primaryPaymentMethodId = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_PRIMARY_PAYMENT_METHOD_ID);
        String result = setSecondaryPaymentMethodAsDefault(primaryPaymentMethodId);
        Assert.assertEquals(result, CloudIntegrationConstants.TRUE, errorMessage);
        authenticatorClient.logout();
        super.cleanup();
    }
}
