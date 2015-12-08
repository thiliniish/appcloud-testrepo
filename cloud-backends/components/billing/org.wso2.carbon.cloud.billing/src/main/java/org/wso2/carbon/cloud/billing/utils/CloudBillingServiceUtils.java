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

package org.wso2.carbon.cloud.billing.utils;

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.Plan;
import org.wso2.carbon.cloud.billing.commons.config.Subscription;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.commons.utils.CloudBillingUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.usage.apiusage.APICloudUsageManager;
import javax.xml.namespace.QName;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

/**
 * Model to represent Utilities for Cloud Billing module
 */

public class CloudBillingServiceUtils {


    private static final Log LOGGER = LogFactory.getLog(CloudBillingServiceUtils.class);
    private static volatile String configObj;
    private static BillingRequestProcessor dsBRProcessor = BillingRequestProcessorFactory.getBillingRequestProcessor
            (BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
             BillingConfigUtils.getBillingConfiguration()
                     .getDSConfig()
                     .getHttpClientConfig());
    private static APICloudUsageManager usageManager = new APICloudUsageManager();
    private static String getAccountUrl = BillingConfigUtils.getBillingConfiguration().getDSConfig()
                                                  .getCloudBillingServiceUrl()
                                          + BillingConstants.DS_API_URI_TENANT_ACCOUNT;

    private CloudBillingServiceUtils() {
    }

    /**
     * Retrieve zuora account id for tenant
     *
     * @param tenantDomain tenant domain
     * @return account id
     * @throws CloudBillingException
     */
    public static String getAccountIdForTenant(String tenantDomain) throws CloudBillingException {

        String response = dsBRProcessor.doGet(getAccountUrl.replace(BillingConstants.TENANT_DOMAIN_PARAM,
                                                                    tenantDomain), null);
        try {
            if (response != null && !response.isEmpty()) {
                OMElement elements = AXIOMUtil.stringToOM(response);
                if (elements.getFirstElement() == null || elements.getFirstElement().getFirstElement() == null) {
                    return null;
                } else {
                    return elements.getFirstElement().getFirstElement().getText();
                }
            } else {
                return null;
            }

        } catch (XMLStreamException e) {
            throw new CloudBillingException("Unable to get the OMElement from " + response, e);
        }

    }

    /**
     * Get configuration on json
     *
     * @return json object
     */
    public static String getConfigInJson() {
        if (configObj == null) {
            synchronized (CloudBillingServiceUtils.class) {
                if (configObj == null) {
                    Gson gson = new Gson();
                    configObj = gson.toJson(BillingConfigUtils.getBillingConfiguration());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Configuration read to json: " + configObj);
                    }
                }
            }
        }
        return configObj;
    }

    /**
     * Validate rate plan id
     *
     * @param serviceId         service Id
     * @param productRatePlanId rate plan id
     * @return validation boolean
     */
    public static boolean validateRatePlanId(String serviceId, String productRatePlanId) {
        Plan[] plans = getSubscriptions(serviceId);
        for (Plan plan : plans) {
            if (plan.getId().equals(productRatePlanId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate service Id
     *
     * @param serviceId service id
     * @return validation boolean
     */
    public static boolean validateServiceId(String serviceId) {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        for (Subscription subscription : subscriptions) {
            if (serviceId.equals(subscription.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get rate plan for rate planId
     *
     * @param subscriptionId subscription id (ex:api_cloud)
     * @param ratePlanId     rate plan id
     * @return rate plan
     * @throws CloudBillingException
     */
    public static Plan getSubscriptionForId(String subscriptionId, String ratePlanId)
            throws CloudBillingException, XMLStreamException {
        Plan plan = getSubscriptionInfo(subscriptionId, ratePlanId);
        OMElement subscriptionElements;
        if (plan == null) {
            String subscriptionMappingResponse;
            subscriptionMappingResponse = getSubscriptionMapping(ratePlanId);
            if (subscriptionMappingResponse != null) {
                subscriptionElements = AXIOMUtil.stringToOM(subscriptionMappingResponse);
                Iterator<?> iterator =
                        subscriptionElements.getChildrenWithName(new QName(BillingConstants.RATE_PLAN_ID));
                while (iterator.hasNext()) {
                    OMElement ratePlanIdElement = (OMElement) iterator.next();
                    String productRatePlanId = ratePlanIdElement.getText();
                    plan = getSubscriptionInfo(subscriptionId, productRatePlanId);
                }
            }
        }
        return plan;
    }


    private static Plan getSubscriptionInfo(String subscriptionId, String id) throws CloudBillingException {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        for (Subscription sub : subscriptions) {
            if (subscriptionId.equalsIgnoreCase(sub.getId())) {
                for (Plan plan : sub.getPlans()) {
                    if ((plan.getId()).equals(id)) {
                        return plan;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get plans for Id
     *
     * @param subscriptionId subscription id(ex:api_cloud)
     * @return rate plans in billing.xml
     */
    public static Plan[] getSubscriptions(String subscriptionId) {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        Plan[] plans = null;
        for (Subscription subscription : subscriptions) {
            if (subscriptionId.equalsIgnoreCase(subscription.getId())) {
                plans = subscription.getPlans();
            }
        }
        return plans;
    }

    /**
     * Retrieve account usage
     *
     * @param tenantDomain tenant domain
     * @param productName  product name
     * @param startDate    start date (date range for usage)
     * @param endDate      end data (date range for usage)
     * @return account usage array
     * @throws CloudBillingException
     */
    public static AccountUsage[] getTenantUsageDataForGivenDateRange(String tenantDomain, String productName,
                                                                     String startDate, String endDate)
            throws CloudBillingException {
        return usageManager.getTenantUsageDataForGivenDateRange(tenantDomain, productName, startDate, endDate);
    }


    public static String getZuoraProductIdForServiceId(String serviceId) throws CloudBillingException {
        Subscription[] subscriptions = BillingConfigUtils.getBillingConfiguration().getZuoraConfig().getSubscriptions();
        for (Subscription subscription : subscriptions) {
            if (subscription.getId().equals(serviceId)) {
                return subscription.getProductId();
            }
        }
        throw new CloudBillingException("No zuora product id found for serviceId: " + serviceId);
    }

    /**
     * Method to get that the billing functionality enable/disable status
     *
     * @return billing enable/disable status
     */
    public static boolean isBillingEnabled() {
        return BillingConfigUtils.getBillingConfiguration().isBillingEnabled();
    }

    private static String getSubscriptionMapping(String ratePlanId) throws CloudBillingException {
        String url = BillingConfigUtils.getBillingConfiguration().getDSConfig().getCloudBillingServiceUrl() + BillingConstants.DS_API_URI_MAPPING_FOR_SUBSCRIPTION;
        NameValuePair[] nameValuePairs = new NameValuePair[] { new NameValuePair("NEW_SUBSCRIPTION_ID", ratePlanId)};
        return dsBRProcessor.doGet(url, nameValuePairs);
    }

}