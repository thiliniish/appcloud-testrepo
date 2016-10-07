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

    private static volatile Class billingVendorClass;
    private static volatile Object billingVendorClassInstance;
    private static final Log LOGGER = LogFactory.getLog(BillingVendorInvoker.class);


    private BillingVendorInvoker() {
    }

    /**
     * Load the cloud billing vendor util class
     *
     * @throws org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException
     */
    public static CloudBillingServiceProvider loadBillingVendor() throws CloudBillingException {
        String billingVendorClassName = CloudBillingServiceUtils.getBillingVendorServiceUtilClass();
        if (billingVendorClass == null || billingVendorClassInstance == null) {
            synchronized (BillingVendorInvoker.class) {
                if (billingVendorClass == null || billingVendorClassInstance == null) {
                    try {
                        //Class vendorClass = Class.forName(billingVendorClassName);
                        //LOGGER.info("-------------------------------- vendor cls : " + vendorClass.getClass());
                        billingVendorClass = Class.forName(billingVendorClassName);
                        //billingVendorClass.cast(vendorClass.getClass());
                        LOGGER.info("-------------------------------- billingVendorClass cls : " +
                                           billingVendorClass.getClass());
	                    billingVendorClassInstance =  billingVendorClass.newInstance();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new CloudBillingException(
                                "Error while loading cloud billing vendor class : " + billingVendorClassName + ". " +
                                e);
                    }
                }
            }
        }
        return (CloudBillingServiceProvider)billingVendorClassInstance;
    }

    /**
     * Load the cloud billing vendor util class for monetization
     *
     * @param consArg cloud billing vendor constructor argument
     * @throws org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException
     */
    public static CloudBillingServiceProvider loadBillingVendorForMonetization(String consArg) throws
                                                                                               CloudBillingException {
        String billingVendorClassName = CloudBillingServiceUtils.getBillingVendorServiceUtilClass();
        if (billingVendorClass == null || billingVendorClassInstance == null) {
            synchronized (BillingVendorInvoker.class) {
                if (billingVendorClass == null || billingVendorClassInstance == null) {
                    try {
                        //billingVendorClass = Class.forName(billingVendorClassName);
                        Class vendorClass = Class.forName(billingVendorClassName);
                        billingVendorClass.cast(vendorClass);
                        Constructor billingVendorConstructor = billingVendorClass.getConstructor(
                                new Class[] { String.class });
                        billingVendorClassInstance = billingVendorConstructor.newInstance(new Object[] { consArg });
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                            NoSuchMethodException | InvocationTargetException e) {
                        throw new CloudBillingException(
                                "Error while loading cloud billing vendor class : " + billingVendorClassName +
                                "for Monetization");
                    }
                }
            }
        }
        return (CloudBillingServiceProvider) billingVendorClassInstance;
    }

    /**
     * Invoke the cloud billing vendor util class method
     *
     * @param method cloud billing vendor method
     * @return billing vendor util method response Json string object
     * @throws org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingException
     */
    public static Object invokeMethod(String method) throws CloudBillingException {
        loadBillingVendor();
        //no parameter
        Class noparams[] = { };
        //call the billing vendor class method
        try {
            Method billingVendorMethod = billingVendorClass.getDeclaredMethod(method, noparams);
            billingVendorClassInstance = billingVendorClass.newInstance();
            return billingVendorMethod.invoke(billingVendorClassInstance);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException
                e) {
            throw new CloudBillingException("Error while invoking cloud billing vendor method : " + method + ". " + e
                    .getCause());
        }
    }
}
