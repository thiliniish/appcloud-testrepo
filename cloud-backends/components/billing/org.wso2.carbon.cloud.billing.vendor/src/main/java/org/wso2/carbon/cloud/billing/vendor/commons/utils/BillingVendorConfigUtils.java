/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.cloud.billing.vendor.commons.utils;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.vendor.commons.config.BillingVendorConfig;
import org.wso2.carbon.cloud.billing.vendor.internal.ServiceDataHolder;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Billing configuration utility class
 */
public class BillingVendorConfigUtils {
    private static SecretResolver secretResolver;
    private static volatile BillingVendorConfig billingVendorConfig;

    private BillingVendorConfigUtils() {
    }

    /**
     * Get billing configuration billing.xml
     *
     * @return Billing config
     */
    public static BillingVendorConfig getBillingVendorConfiguration() {
        if (billingVendorConfig == null) {
            synchronized (BillingVendorConfigUtils.class) {
                if (billingVendorConfig == null) {
                    billingVendorConfig = loadBillingVendorConfig();
                }
            }
        }
        return billingVendorConfig;
    }

    /**
     * Convert xml to DOM
     *
     * @param file file
     * @return DOM
     * @throws CloudBillingException
     */
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

    /**
     * Load billing configuration
     *
     * @return Billing config
     */
    private static BillingVendorConfig loadBillingVendorConfig() {
        try {
            String configLocation = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                                    BillingConstants.CLOUD_CONFIG_FOLDER + File.separator +
                                    BillingConstants.BILLING_VENDOR_CONFIG_FILE_NAME;
            File billingVendorConfig = new File(configLocation);
            Document doc = convertToDocument(billingVendorConfig);
            secureResolveDocument(doc);

            /* Un-marshaling Billing Management configuration */
            JAXBContext cdmContext = JAXBContext.newInstance(BillingVendorConfig.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            return (BillingVendorConfig) unmarshaller.unmarshal(doc);
        } catch (CloudBillingException | JAXBException e) {
            throw new IllegalArgumentException("Error occurred while initializing Billing config", e);
        }
    }

    /**
     * secure vault enable documents securely resolve elements
     *
     * @param doc DOC
     */
    private static void secureResolveDocument(Document doc) {
        Element element = doc.getDocumentElement();
        if (element != null) {
            secureLoadElement(element);
        }
    }

    /**
     * Securely load elements
     *
     * @param element xml element
     */
    private static void secureLoadElement(Element element) {
        Attr secureAttr = element.getAttributeNodeNS(BillingConstants.SecureValueProperties.SECURE_VAULT_NS,
                                                     BillingConstants.SecureValueProperties
                                                             .SECRET_ALIAS_ATTRIBUTE_NAME_WITH_NAMESPACE);
        if (secureAttr != null) {
            element.setTextContent(loadFromSecureVault(secureAttr.getValue()));
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
     * Load from secure vault
     *
     * @param alias alias used
     * @return CallBackHandler
     */
    private static synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver
                    .init(ServiceDataHolder.getInstance().getSecretCallbackHandlerService().getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

}
