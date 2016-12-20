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

package org.wso2.carbon.cloud.billing.core.usage.apiusage;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.billing.core.beans.usage.AccountUsage;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.commons.config.BillingConfigManager;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.core.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.core.usage.UsageProcessor;
import org.wso2.carbon.cloud.billing.core.usage.UsageProcessorContext;
import org.wso2.carbon.cloud.billing.core.usage.apiusage.utils.APIUsageProcessorUtil;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Class to process API Cloud Usage
 */
public class APICloudUsageProcessor implements UsageProcessor {

    private static String amendmentsUrl =
            BillingConfigManager.getBillingConfiguration().getDataServiceConfig().getCloudBillingServiceUri() +
            BillingConstants.DS_API_URI_AMENDMENTS;
    private BillingRequestProcessor billingRequestProcessor;

    /**
     * Constructor
     */
    public APICloudUsageProcessor() {
        this.billingRequestProcessor = BillingRequestProcessorFactory.getInstance()
                .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE);
    }

    /**
     * Process and retrieve tenant usage from APIM
     *
     * @param context context
     * @return account usage
     * @throws CloudBillingException
     */
    public AccountUsage[] process(UsageProcessorContext context) throws CloudBillingException {
        if (StringUtils.isNotBlank(context.getAccountId())) {
            String startDate = context.getStartDate();
            String endDate = context.getEndDate();
            boolean hasAmendments = hasAmendments(context.getAccountId(), startDate, endDate);
            String amendmentResponse = getAmendmentForPaymentPlans(context.getAccountId());
            return APIUsageProcessorUtil.getTenantUsageFromAPIM(context.getResponse(), context.getAccountId(),
                                                                hasAmendments, amendmentResponse);
        } else {
            return APIUsageProcessorUtil.getTenantUsageFromAPIM(context.getResponse());
        }
    }

    /**
     * Checks whether an account has amendments
     *
     * @param accountId
     * @param startDate
     * @param endDate
     * @return
     * @throws CloudBillingException
     */
    private boolean hasAmendments(String accountId, String startDate, String endDate) throws CloudBillingException {
        String response;
        try {
            response = getAmendmentForPaymentPlans(accountId);
            OMElement elements = AXIOMUtil.stringToOM(response);
            Iterator<?> entries = elements.getChildrenWithName(new QName(BillingConstants.ENTRY));
            int size = 0;
            while (entries.hasNext()) {
                entries.next();
                size = size + 1;
                //if there are more than one amendment then return true
                if (size == 2) {
                    return true;
                }
            }
            return false;
        } catch (XMLStreamException e) {
            throw new CloudBillingException("Error while reading xml response from data service", e);
        }
    }

    /**
     * Retrieve amendments for payment plans
     *
     * @param accountId
     * @return
     * @throws CloudBillingException
     */
    private String getAmendmentForPaymentPlans(String accountId) throws CloudBillingException {
        NameValuePair[] nameValuePairs = new NameValuePair[] { new NameValuePair("ACCOUNT_NUMBER", accountId),
                new NameValuePair("SUBSCRIPTION", BillingConstants.API_CLOUD_SUBSCRIPTION_ID) };
        return billingRequestProcessor.doGet(amendmentsUrl, null, nameValuePairs);
    }
}
