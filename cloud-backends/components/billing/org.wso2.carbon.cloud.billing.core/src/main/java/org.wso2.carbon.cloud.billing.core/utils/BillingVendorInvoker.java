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

package org.wso2.carbon.cloud.billing.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.core.commons.CloudBillingServiceProvider;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Billing vendor utility class loader and method invoker
 */
public class BillingVendorInvoker {

    private static final Log LOGGER = LogFactory.getLog(BillingVendorInvoker.class);
    private static volatile Class<?> billingVendorClass;
    private static volatile Object billingVendorClassInstance;

    private BillingVendorInvoker() {
    }

    /**
     * Load the cloud billing vendor util class
     *
     * @throws CloudBillingException
     */
    public static CloudBillingServiceProvider loadBillingVendor() throws CloudBillingException {
        String billingVendorClassName = CloudBillingServiceUtils.getBillingVendorServiceUtilClass();
        if (billingVendorClass == null || billingVendorClassInstance == null) {
            synchronized (BillingVendorInvoker.class) {
                if (billingVendorClass == null || billingVendorClassInstance == null) {
                    try {
                        Class<?> tempBillingVendorClass = Class.forName(billingVendorClassName);
                        Constructor[] constructors = tempBillingVendorClass.getDeclaredConstructors();
                        Constructor constructor = null;
                        for (Constructor tempConstructor : constructors) {
                            constructor = tempConstructor;
                            if (constructor.getGenericParameterTypes().length == 0) {
                                break;
                            }
                        }
                        if (constructor != null) {
                            billingVendorClass = tempBillingVendorClass;
                            billingVendorClassInstance = constructor.newInstance();
                        } else {
                            LOGGER.error("Error occurred while starting the service : " + billingVendorClassName +
                                         " Constructor was not found.");
                        }

                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        LOGGER.error(e);
                        throw new CloudBillingException(
                                "Error while loading cloud billing vendor class : " + billingVendorClassName + ". " +
                                e);
                    } catch (InvocationTargetException e) {
                        LOGGER.error(e.getTargetException());
                        LOGGER.error(e.getTargetException().getCause().getMessage());
                        throw new CloudBillingException(
                                "Error while loading cloud billing vendor class : " + billingVendorClassName + ". " +
                                e);
                    }
                }
            }
        }
        return (CloudBillingServiceProvider) billingVendorClassInstance;
    }

    /**
     * Load the cloud billing vendor util class for monetization
     * TODO : There should be a instance for each argument, hence creating instance for each call.
     * TODO : Need to have to cache to keep the instances
     *
     * @throws CloudBillingException
     */
    public static CloudBillingServiceProvider loadBillingVendorForMonetization(String tenantDomain)
            throws CloudBillingException {
        //String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String billingVendorClassName = CloudBillingServiceUtils.getBillingVendorServiceUtilClass();
        CloudBillingServiceProvider instance;

        try {
            Class<?> vendorClass = Class.forName(billingVendorClassName);
            Constructor billingVendorConstructor = vendorClass.getConstructor(String.class);
            instance = (CloudBillingServiceProvider) billingVendorConstructor.newInstance(tenantDomain);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new CloudBillingException(
                    "Error while loading cloud billing vendor class : " + billingVendorClassName + "for Monetization");
        }
        return instance;
    }

    /**
     * Invoke the cloud billing vendor util class method
     *
     * @param method cloud billing vendor method
     * @return billing vendor util method response Json string object
     * @throws org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException
     */
    public static Object invokeMethod(String method, String params) throws CloudBillingException {
        loadBillingVendor();
        //no parameter
        Class noparams[] = {};
        //String parameter
        Class[] paramString = new Class[1];
        paramString[0] = String.class;
        //call the billing vendor class method
        try {
            if (params == null) {
                Method billingVendorMethod = billingVendorClassInstance.getClass().getDeclaredMethod(method, noparams);
                return billingVendorMethod.invoke(billingVendorClassInstance);
            } else {
                Method billingVendorMethod =
                        billingVendorClassInstance.getClass().getDeclaredMethod(method, paramString);
                return billingVendorMethod.invoke(billingVendorClassInstance, params);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new CloudBillingException(
                    "Error while invoking cloud billing vendor method : " + method + ". " + e.getCause());
        }
    }
}
