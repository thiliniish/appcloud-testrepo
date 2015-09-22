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
import java.util.Map;

/**
 *
 * This is to test adding, viewing, changing and deleting
 * functionalities of the cloud billing feature.
 *
 * Since it checks only the added payments it's require to have a valid payment
 * subscription for that tenant.
 */
public class PaymentMethodsTestCase extends CloudIntegrationTest {

    private JaggeryAppAuthenticatorClient authenticatorClient;
    private boolean loginStatus;

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
     * Since it's require to load the iFrame (selenium web drive ui tests)
     * to add the credit card details here it's only checking the
     * parameters generated to query the iframe
     *
     * @throws Exception
     */
    @Test(description = "This will check generated parameters to get the iframe.")
    public void addPaymentMethod() throws Exception {
        String serviceId = CloudIntegrationTestUtils.getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_SERVICE_ID);
        String productPlanId = CloudIntegrationTestUtils
                .getPropertyValue(CloudIntegrationConstants.BILLING_PAYMENT_PRODUCT_RATE_PLAN_ID);

        Assert.assertTrue(loginStatus, "Tenant login failed.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "generateParams");
        //put into the configuration
        params.put("serviceId", serviceId);
        params.put("productRatePlanId", productPlanId);
        String paymentMethodAddUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_PAYMENT_METHOD_ADD_URL_SFX;
        String result = HttpHandler.doPostHttps(paymentMethodAddUrl, params);
        JSONObject resultObj = new JSONObject(result);

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
    @Test(description = "View payment methods of the tenant")
    public void viewPaymentMethods() throws Exception {
        Assert.assertTrue(loginStatus, "Tenant login failed.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "get-payment-methods");
        String paymentMethodInfoUrl = cloudMgtServerUrl + CloudIntegrationConstants.CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX;
        String result = HttpHandler.doPostHttps(paymentMethodInfoUrl, params);
        JSONObject resultObj = new JSONObject(result);
        Assert.assertEquals(resultObj.getString("success"), "true", "Value mismatch should be true.");
        Assert.assertNotNull(resultObj.getJSONArray("creditCards"), "Json array of credit cards should not be null.");
    }

/*    @Test(priority = 2, description = "Change payment method.")
    public void changePaymentMethod() throws Exception {
        //Assert.assertTrue(loginStatus, "Tenant login failed.");
    }

    @Test(priority = 3, description = "Delete payment method")
    public void deletePaymentMethod() throws Exception {
        //Should be the value taken from the added payment method from the test case
        String paymentMethodId = "2c92c0f84fd7e46c014fea59b4f32605";

        Assert.assertTrue(loginStatus, "Tenant login failed.");
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "removePaymentMethod");
        params.put("paymentMethodId", paymentMethodId);
        String paymentMethodRemovalUrl = cloudMgtServerUrl + CloudConstants.CLOUD_BILLING_PAYMENT_METHOD_INFO_URL_SFX;
        String result = HttpHandler.doPostHttps(paymentMethodRemovalUrl, params);
        JSONObject resultObj = new JSONObject(result);

        if (!"true".equals(resultObj.getString("success"))) {
            Assert.fail("Payment method removal failure. error: " + resultObj.getString("reasons"));
        }
    }*/

    @AfterClass(alwaysRun = true)
    public void destroy() throws IOException {
        authenticatorClient.logout();
        super.cleanup();
    }
}
