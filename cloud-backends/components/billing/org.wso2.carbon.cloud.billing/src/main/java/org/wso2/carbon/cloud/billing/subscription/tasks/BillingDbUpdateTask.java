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

package org.wso2.carbon.cloud.billing.subscription.tasks;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.internal.ServiceDataHolder;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.utils.CloudBillingServiceUtils;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.user.api.UserStoreException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *
 */
public class BillingDbUpdateTask implements Task {

    private static final Log LOGGER = LogFactory.getLog(BillingDbUpdateTask.class);
    private static final String ERROR_MSG = "Error while executing task: Billing database update for un-subscribed " +
                                            "tenants ";
    private Map<String, String> properties;
    private BillingRequestProcessor requestProcessor;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        requestProcessor = BillingRequestProcessorFactory
                .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
                                            BillingConfigUtils.getBillingConfiguration().getDSConfig()
                                                    .getHttpClientConfig());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        try {
            String response = requestProcessor.doGet(properties.get(BillingConstants.PENDING_DISABLES_URL_KEY));
            OMElement elements = AXIOMUtil.stringToOM(response);

            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            while (entries.hasNext()) {
                OMElement entry = (OMElement) entries.next();
                String tenantDomain = ((OMElement) entry
                        .getChildrenWithName(new QName(BillingConstants.PENDING_DISABLE_TENANT_DOMAIN)).next())
                        .getText();
                String subscription = ((OMElement) entry
                        .getChildrenWithName(new QName(BillingConstants.PENDING_DISABLE_SUBSCRIPTION)).next())
                        .getText();
                String startDateString = ((OMElement) entry
                        .getChildrenWithName(new QName(BillingConstants.PENDING_DISABLE_START_DATE)).next()).getText();
                String endDateString = ((OMElement) entry
                        .getChildrenWithName(new QName(BillingConstants.PENDING_DISABLE_END_DATE)).next()).getText();

                int tenantId = ServiceDataHolder.getInstance().getRealmService().getTenantManager().getTenantId
                        (tenantDomain);

                updateDatabaseEntry(tenantDomain, subscription, startDateString, endDateString, tenantId);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Tenant subscriptions disabling task executed successfully, information " +
                             "is as follows: " + elements);
            }
        } catch (CloudBillingException e) {
            LOGGER.error(ERROR_MSG + " while executing http request: ", e);
        } catch (XMLStreamException e) {
            LOGGER.error(ERROR_MSG + " while response parsing: ", e);
        } catch (UserStoreException e) {
            LOGGER.error(ERROR_MSG + " while acquiring tenantId: ", e);
        }
    }

    private void updateDatabaseEntry(String tenantDomain, String subscription, String startDate, String endDate,
                                     int tenantId)
            throws CloudBillingException {

        updateSubscriptionsTable(tenantDomain, subscription);
        updateBillingStatusTable(tenantDomain, subscription, endDate);
        addToHistoryTable(tenantDomain, subscription, startDate, endDate, tenantId);
    }

    /**
     * @param tenantDomain String
     * @param subscription String api_cloud/app_cloud
     * @param startDate    String subscription start date
     * @param endDate      String subscription end date
     * @throws CloudBillingException
     */
    private void addToHistoryTable(String tenantDomain, String subscription, String startDate, String endDate,
                                   int tenantId)
            throws CloudBillingException {
        String accountId = CloudBillingServiceUtils.getAccountIdForTenant(tenantDomain);
        String url = properties.get(BillingConstants.BILLING_HISTORY_URL_KEY);

        NameValuePair[] payload = {
                new NameValuePair(BillingConstants.TENANT_DOMAIN_QUERY_PARAM, tenantDomain),
                new NameValuePair(BillingConstants.SUBSCRIPTION_QUERY_PARAM, subscription),
                new NameValuePair(BillingConstants.ACCOUNT_NUMBER_QUERY_PARAM, accountId),
                new NameValuePair(BillingConstants.TYPE_QUERY_PARAM, "PAID"),
                new NameValuePair(BillingConstants.STATUS_QUERY_PARAM, "DISABLED"),
                new NameValuePair(BillingConstants.START_DATE, startDate),
                new NameValuePair(BillingConstants.END_DATE, endDate),
                new NameValuePair(BillingConstants.TENANT_ID_QUERY_PARAM, Integer.toString(tenantId))
        };

        String response = requestProcessor.doPost(url, payload);
        if (String.valueOf(HttpURLConnection.HTTP_ACCEPTED).equals(response) && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully added into BILLING_HISTORY after disabling subscription: " + subscription
                         + " for tenant: " + tenantDomain);
        }
    }

    /**
     * @param tenantDomain String
     * @param subscription String api_cloud/app_cloud
     * @throws CloudBillingException
     */
    private void updateSubscriptionsTable(String tenantDomain, String subscription) throws CloudBillingException {
        String url = properties.get(BillingConstants.UPDATE_SUBSCRIPTION_STATUS_URL_KEY);

        NameValuePair[] payload = {
                new NameValuePair(BillingConstants.CLOUD_TYPE_QUERY_PARAM, subscription),
                new NameValuePair(BillingConstants.TENANT_DOMAIN_QUERY_PARAM, tenantDomain),
                new NameValuePair(BillingConstants.STATUS_QUERY_PARAM, "0")
        };

        String response = requestProcessor.doPost(url, payload);
        if (String.valueOf(HttpURLConnection.HTTP_ACCEPTED).equals(response) && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully updated SUBSCRIPTIONS table after disabling subscription: " + subscription
                         + " for tenant: " + tenantDomain);
        }
    }

    /**
     * @param tenantDomain String
     * @param subscription String api_cloud/app_cloud
     * @param endDate      String "2015-02-20+05:30"
     * @throws CloudBillingException
     */
    private void updateBillingStatusTable(String tenantDomain, String subscription, String endDate)
            throws CloudBillingException {
        String url = properties.get(BillingConstants.DISABLE_TENANT_URL_KEY);

        //Dropping the timezone appended by the data service's mysql.DATE to string conversion
        //ex: "2015-02-20+05:30" --> "2015-02-20";
        String endDateString = endDate.substring(0, 10);

        NameValuePair[] payload = {
                new NameValuePair(BillingConstants.TENANT_DOMAIN_QUERY_PARAM, tenantDomain),
                new NameValuePair(BillingConstants.SUBSCRIPTION_QUERY_PARAM, subscription),
                new NameValuePair(BillingConstants.END_DATE_QUERY_PARAM, endDateString)
        };
        String response = requestProcessor.doPost(url, payload);
        if (String.valueOf(HttpURLConnection.HTTP_ACCEPTED).equals(response) && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully updated BILLING_STATUS table after disabling subscription: " + subscription
                         + " for tenant: " + tenantDomain);
        }
    }
}