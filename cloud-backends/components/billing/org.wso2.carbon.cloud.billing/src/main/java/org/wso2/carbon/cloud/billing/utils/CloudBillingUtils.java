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

package org.wso2.carbon.cloud.billing.utils;

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.cloud.billing.common.BillingConstants;
import org.wso2.carbon.cloud.billing.common.CloudBillingException;
import org.wso2.carbon.cloud.billing.common.config.BillingConfig;
import org.wso2.carbon.cloud.billing.common.config.Plan;
import org.wso2.carbon.cloud.billing.common.config.Subscription;
import org.wso2.carbon.cloud.billing.internal.ServiceDataHolder;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.Iterator;

/**
 * Model to represent Utilities for Cloud Billing module
 */

public class CloudBillingUtils {

    private static final Log log = LogFactory.getLog(CloudBillingUtils.class);
    private static SecretResolver secretResolver;
    private static volatile BillingConfig billingConfig;
    private static volatile String configObj;
    private static BillingRequestProcessor dsBRProcessor = BillingRequestProcessorFactory.getBillingRequestProcessor
            (BillingRequestProcessorFactory.ProcessorType.DATA_SERVICE,
             CloudBillingUtils.getBillingConfiguration()
                     .getDSConfig()
                     .getHttpClientConfig());


    public static String getAccountIdForTenant(String tenantDomain) throws CloudBillingException {
        String getAccountUrl = CloudBillingUtils.getBillingConfiguration().getDSConfig().getTenantAccount();

        getAccountUrl = getAccountUrl.replace(BillingConstants.TENANT_DOMAIN_PARAM, tenantDomain);
        String response = dsBRProcessor.doGet(getAccountUrl);
        try {
            if (response != null && !response.equals("")) {
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
            String msg = "Unable to get the OMElement from " + response;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }

    }

    public static BillingConfig getBillingConfiguration() {
        if (billingConfig == null) {
            synchronized (CloudBillingUtils.class) {
                if (billingConfig == null) {
                    billingConfig = loadBillingConfig();
                }
            }
        }
        return billingConfig;
    }

    public static String getConfigInJson() {
        if (configObj == null) {
            synchronized (CloudBillingUtils.class) {
                if (configObj == null) {
                    Gson gson = new Gson();
                    configObj = gson.toJson(getBillingConfiguration());
                    if (log.isDebugEnabled()) {
                        log.debug("Configuration read to json: " + configObj);
                    }
                }
            }
        }
        return configObj;
    }

    public static boolean validateRatePlanId(String serviceId, String productRatePlanId) {
        Plan[] plans = getSubscriptions(serviceId);
        for (Plan plan : plans) {
            if (plan.getId().equals(productRatePlanId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validateServiceId(String serviceId) {
        Subscription[] subscriptions = billingConfig.getZuoraConfig().getSubscriptions();
        for (Subscription subscription : subscriptions) {
            if (serviceId.equals(subscription.getId())) {
                return true;
            }
        }
        return false;
    }

    public static Plan getSubscriptionForId(String subscriptionId, String id)
            throws CloudBillingException, XMLStreamException {
        Plan plan = getSubscriptionInfo(subscriptionId, id);
        OMElement subscriptionElements;
        if (plan == null) {
            String subscriptionMappingResponse;
            subscriptionMappingResponse = getSubscriptionMapping(id);
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
        for (Subscription sub : billingConfig.getZuoraConfig().getSubscriptions()) {
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

    public static Document convertToDocument(File file) throws CloudBillingException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new CloudBillingException("Error occurred while parsing file, while converting " +
                                            "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    private static BillingConfig loadBillingConfig() {
        try {
            String configLocation = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                    BillingConstants.CONFIG_FOLDER + File.separator +
                                    BillingConstants.CONFIG_FILE_NAME;
            File billingConfig = new File(configLocation);
            Document doc = CloudBillingUtils.convertToDocument(billingConfig);
            CloudBillingUtils.secureResolveDocument(doc);

            /* Un-marshaling Billing Management configuration */
            JAXBContext cdmContext = JAXBContext.newInstance(BillingConfig.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            return (BillingConfig) unmarshaller.unmarshal(doc);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error occurred while initializing Billing config", e);
        }
    }

    public static Plan[] getSubscriptions(String subscriptionId) {
        Subscription[] subscriptions = billingConfig.getZuoraConfig().getSubscriptions();
        Plan[] plans = null;
        for (Subscription subscription : subscriptions) {
            if (subscriptionId.equalsIgnoreCase(subscription.getId())) {
                plans = subscription.getPlans();
            }
        }
        return plans;
    }

    private static synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver.init(ServiceDataHolder.getInstance().getSecretCallbackHandlerService()
                                        .getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

    private static void secureResolveDocument(Document doc) {
        Element element = doc.getDocumentElement();
        if (element != null) {
            secureLoadElement(element);
        }
    }

    private static void secureLoadElement(Element element) {
        Attr secureAttr =
                element.getAttributeNodeNS(BillingConstants.SecureValueProperties.SECURE_VAULT_NS,
                                           BillingConstants.SecureValueProperties
                                                   .SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE);
        if (secureAttr != null) {
            element.setTextContent(CloudBillingUtils.loadFromSecureVault(secureAttr.getValue()));
            element.removeAttributeNode(secureAttr);
        }
        NodeList childNodes = element.getChildNodes();
        int count = childNodes.getLength();
        Node tmpNode;
        for (int i = 0; i < count; i++) {
            tmpNode = childNodes.item(i);
            if (tmpNode instanceof Element) {
                secureLoadElement((Element) tmpNode);
            }
        }
    }

    /**
     * Method to get that the billing functionality enable/disable status
     *
     * @return billing enable/disable status
     */
    public static boolean isBillingEnable() {
        return billingConfig.isBillingEnable();
    }

    private static String getSubscriptionMapping(String ratePlanId) throws CloudBillingException {
        String url = CloudBillingUtils.getBillingConfiguration().getDSConfig().getSubscriptionMapping();
        url = url + "?newSubscriptionId=" + ratePlanId;
        return dsBRProcessor.doGet(url);
    }
}
