/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License
 */
package org.wso2.carbon.cloud.throttling.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.commons.throttle.core.ThrottleFactory;
import org.wso2.carbon.cloud.throttling.common.CloudThrottlingException;
import org.wso2.carbon.cloud.throttling.common.Constants;
import org.wso2.carbon.cloud.throttling.tasks.CacheCleaner;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/*
* This class holds data related to throttle handler
* */
public class ThrottleDataHolder {

    private static final Log LOG = LogFactory.getLog(ThrottleDataHolder.class);
    private static final String CLOUD_TYPE = Constants.SUBSCRIPTION_API_CLOUD;
    private static final ThrottleDataHolder INSTANCE = new ThrottleDataHolder();
    /*
    * The concurrent access control group id
    * */
    private static String id;
    private static Timer cacheCleanerTimer;
    private static Timer throttleOutHandlerTimer;      //todo
    private static OMElement policyElement = null;
    /*
    * Used to keep throttle objects per tenant as to avoid confusion in high concurrency
    * */
    private static Map<String, Throttle> tenantThrottleMap = new ConcurrentHashMap<String, Throttle>();

    private ThrottleDataHolder() {
    }

    public static ThrottleDataHolder getInstance() {
        return INSTANCE;
    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        ThrottleDataHolder.id = id;
    }

    public static String getCloudType() {
        return CLOUD_TYPE;
    }

    /*
    * Use to get the throttle object for a given tenant domain. It creates if not already created.
    * @param String tenantDomain
    * @return Throttle throttle object
    * @throws CloudThrottlingException
    * */
    public Throttle getThrottle(String tenantDomain) throws CloudThrottlingException {
        /* load the policy xml from the file system */
        if (policyElement == null) {
            policyElement = getPolicy();
        }
        try {
            if (policyElement != null) {
                Throttle throttle = null;
                // Get the throttle object for the tenantDomain
                if (tenantThrottleMap.containsKey(tenantDomain)) {
                    throttle = tenantThrottleMap.get(tenantDomain);
                } else {
                    // Creates the throttle object from the policy
                    throttle = ThrottleFactory.createMediatorThrottle(
                            PolicyEngine.getPolicy(policyElement));
                    tenantThrottleMap.put(tenantDomain, throttle);
                }
                return throttle;

            } else {
                throw new CloudThrottlingException("Unable to load throttling policy from policy key " + Constants.POLICY_KEY + " policyElement is null");
            }

        } catch (ThrottleException e) {
            throw new CloudThrottlingException("Error processing the throttling policy", e);
        }
    }

    /*
    * This method read the policy file from the file system and creates an OMElement
    * @return OMElement policy
    * @throws CloudThrottlingException if en exception throws
    * */
    private OMElement getPolicy() throws CloudThrottlingException {
        /* Get policy file from the file system*/
        String policyConfigLocation = CarbonUtils.getCarbonConfigDirPath() + Constants.POLICY_KEY;
        File tenantTiersConfig = new File(policyConfigLocation);
        //create the parser
        XMLStreamReader parser = null;
        try {
            parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(tenantTiersConfig));
        } catch (XMLStreamException e) {
            throw new CloudThrottlingException("Error Reading tenant throttle policy from resource " + Constants.POLICY_KEY, e);
        } catch (FileNotFoundException e) {
            throw new CloudThrottlingException("Tenant tier policy file is not found.", e);
        }
        //create the builder
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        //get the root element of the XML
        OMElement policyElement = builder.getDocumentElement();
        policyElement.build();

        return policyElement;

    }

    /*
    * Start a background task to cleanup the caches
    * */
    public void initCacheCleaner() {
        cacheCleanerTimer = new Timer("Cloud Throttle handler - Cache Cleaner Timer");
        cacheCleanerTimer.scheduleAtFixedRate(new CacheCleaner(), Constants.CACHE_TIME_DELAY, Constants.CACHE_TIME_DELAY);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache cleaner task is started.");
        }
    }

    /*
    * Stop a background task to cleanup the caches
    * */
    public void stopCacheCleaner() {
        cacheCleanerTimer.cancel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutting down Cache cleaner task.");
        }
    }

    /*
    * Start a background task to handle throttle out requests.   todo
    * */
    public void initThrottleOutHandler() {
//        throttleOutHandlerTimer = new Timer("Cloud Throttle handler - Throttle Out Handler Timer");
//        throttleOutHandlerTimer.scheduleAtFixedRate(new ThrottleOutHandler(), Constants.EMAIL_ALERT_DELAY, Constants.EMAIL_ALERT_DELAY);
//        if (LOG.isDebugEnabled()) {
//            LOG.debug("Throttle Out Handler task is started.");
//        }
    }

    /*
    * Stop a background task to cleanup the caches
    * */
    public void stopThrottleOutHandler() {
        throttleOutHandlerTimer.cancel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Shutting down Throttle Out Handler task.");
        }
    }

    /*
    * Holds subscription types
    * */
    public static enum SubscriptionType {
        PAID("PAID"), TRIAL("TRIAL"), FREE("TRIAL");
        private final String name;

        SubscriptionType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
