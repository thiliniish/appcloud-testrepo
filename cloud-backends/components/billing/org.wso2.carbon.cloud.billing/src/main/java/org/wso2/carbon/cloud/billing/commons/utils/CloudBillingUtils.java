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

package org.wso2.carbon.cloud.billing.commons.utils;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.internal.ServiceDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Cloud billing common utilises
 */
public final class CloudBillingUtils {


    private CloudBillingUtils() {
    }

    /**
     * Checks the current carbon server is the coordinator of the cluster
     *
     * @return leader boolean
     */
    public static boolean isCurrentServerLeader() {

        /* Get all the Hazelcast instances in the current JVM.
           In case of carbon server this is always either one
           or zero. */
        Iterator<HazelcastInstance> iter = Hazelcast.getAllHazelcastInstances().iterator();

        if (iter.hasNext()) { // cluster mode
            HazelcastInstance instance = iter.next();
            return instance.getCluster().getMembers().iterator().next().localMember();
        } else {
            return true; // standalone mode
        }
    }

    /**
     * Encoding url parameters with UTF-8
     *
     * @param parameter parameter
     * @return trimmed & UTF-8 encoded parameter
     * @throws UnsupportedEncodingException
     */
    public static String encodeUrlParam(String parameter) throws UnsupportedEncodingException {
        return URLEncoder.encode(parameter.trim(), BillingConstants.ENCODING);
    }

    /**
     * Retrieves a governance registry resource in a given location of a given tenant
     *
     * @param tenantDomain tenant domain
     * @param resourceUrl resource url
     * @return registry resource
     * @throws CloudBillingException
     */
    public static Resource getRegistryResource(String tenantDomain, String resourceUrl) throws CloudBillingException {
        return getRegistryResource(tenantDomain, resourceUrl, BillingConstants.GOVERNANCE_REGISTRY);
    }

    /**
     * Retrieves a registry resource in a given location of a given tenant
     *
     * @param tenantDomain tenant domain
     * @param resourceUrl  resource url
     * @param registryType registry type
     * @return registry resource
     * @throws CloudBillingException
     */
    public static Resource getRegistryResource(String tenantDomain, String resourceUrl, String registryType)
            throws CloudBillingException {
        Resource resource;
        try {
            TenantManager tenantManager = ServiceDataHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID && tenantId != -1) {
                //Start a new tenant flow
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                // load the registry
                ServiceDataHolder.getInstance().gerTenantRegistryLoader().loadTenantRegistry(tenantId);
                Registry registry = getRegistryInstance(registryType, tenantId);

                if (registry.resourceExists(resourceUrl.trim())) {
                    resource = registry.get(resourceUrl.trim());
                    return resource;
                } else {
                    throw new CloudBillingException(
                            "Unable to find the registry resource in the given location: " + resourceUrl
                                    + " for tenant domain: " + tenantDomain);
                }
            } else {
                throw new CloudBillingException(
                        "Error while retrieving tenant id for tenant domain: " + tenantDomain);
            }
        } catch (RegistryException | UserStoreException e) {
            throw new CloudBillingException(
                    "Error occurred while accessing registry resources for tenant " + tenantDomain + ".", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Return registry object according to the given registry type
     *
     * @param registryType
     * @param tenantId
     * @return registry object
     * @throws RegistryException
     */
    private static Registry getRegistryInstance(String registryType, int tenantId) throws RegistryException {
        Registry registry;
        switch (registryType) {
            case BillingConstants.GOVERNANCE_REGISTRY:
                registry = ServiceDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
                break;
            case BillingConstants.CONFIG_REGISTRY:
                registry = ServiceDataHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
                break;
            default:
                registry = ServiceDataHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
        }
        return registry;
    }

    /**
     * Return custom protocol with added protocol versions for a scheme
     *
     * @param scheme              scheme
     * @param enabledProtocolVersions protocol versions
     * @return modified protocol
     */
    public static Protocol getCustomProtocol(String scheme, String enabledProtocolVersions) {

        if (StringUtils.isBlank(scheme)) {
            throw new IllegalArgumentException("Schema for protocol cannot be null or empty");
        }
        Protocol baseProtocol = Protocol.getProtocol(scheme);

        if (StringUtils.isBlank(enabledProtocolVersions)) {
            return baseProtocol;
        }
        int defaultPort = baseProtocol.getDefaultPort();
        ProtocolSocketFactory baseFactory = baseProtocol.getSocketFactory();
        ProtocolSocketFactory customFactory =
                new CustomHTTPSSocketFactory(baseFactory, enabledProtocolVersions.trim().split("\\s*,\\s*"));

        return new Protocol(scheme, customFactory, defaultPort);
    }

    /**
     * Adding a governance registry resource in a given location of a given tenant
     *
     * @param tenantDomain tenant domain
     * @param resourceUrl  resource url
     * @return boolean
     * @throws CloudBillingException
     */
    public static Boolean putRegistryResource(String tenantDomain, String resourceUrl, Resource resource)
            throws CloudBillingException {
        return putRegistryResource(tenantDomain, resourceUrl, resource, BillingConstants.GOVERNANCE_REGISTRY);
    }

    /**
     * Adding a registry resource in a given location of a given tenant
     *
     * @param tenantDomain tenant domain
     * @param resourceUrl  resource url
     * @param registryType registry type
     * @return boolean
     * @throws CloudBillingException
     */
    public static Boolean putRegistryResource(String tenantDomain, String resourceUrl, Resource resource,
                                              String registryType) throws CloudBillingException {
        if (resource == null) {
            throw new CloudBillingException(
                    "Unable to update the registry, resource provided is null" + " for tenant domain: " + tenantDomain);
        }
        try {
            TenantManager tenantManager = ServiceDataHolder.getInstance().getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID && tenantId != -1) {
                //Start a new tenant flow
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                // load the registry
                ServiceDataHolder.getInstance().gerTenantRegistryLoader().loadTenantRegistry(tenantId);
                Registry registry = getRegistryInstance(registryType, tenantId);
                if (registry.resourceExists(resourceUrl.trim())) {
                    registry.put(resourceUrl, resource);
                    return registry.resourceExists(resourceUrl.trim());
                } else {
                    throw new CloudBillingException(
                            "Unable to find the registry resource in the given location: " + resourceUrl +
                            " for tenant domain: " + tenantDomain);
                }
            } else {
                throw new CloudBillingException("Error while retrieving tenant id for tenant domain: " + tenantDomain);
            }
        } catch (RegistryException | UserStoreException e) {
            throw new CloudBillingException(
                    "Error occurred while accessing registry resources for tenant " + tenantDomain + ".", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}