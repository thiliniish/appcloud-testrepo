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

package org.wso2.carbon.cloud.rolemgt.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.rolemgt.common.RoleMgtConfiguration;
import org.wso2.carbon.cloud.rolemgt.common.RoleMgtConfigurationBuilder;
import org.wso2.carbon.cloud.rolemgt.common.RoleMgtConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * @scr.component name="role.mgt.component" immediate=true
 */
public class RoleMgtServiceComponent {
    private static Log log = LogFactory.getLog(RoleMgtServiceComponent.class);

    /**
     * Method to activate bundle.
     *
     * @param ctxt OSGi component context.
     */
    protected void activate(ComponentContext ctxt) {
        BundleContext bundleContext = ctxt.getBundleContext();
        RoleMgtConfiguration configuration;
        try {
            String fileLocation = CarbonUtils.getCarbonConfigDirPath() +
                    File.separator + RoleMgtConstants.CONFIG_FOLDER +
                    File.separator + RoleMgtConstants.CONFIG_FILE_NAME;
            configuration = new RoleMgtConfigurationBuilder(fileLocation).buildRoleMgtConfiguration();
            bundleContext.registerService(RoleMgtConfiguration.class.getName(), configuration, null);
            if (log.isDebugEnabled()) {
                log.debug("Role Management Common bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error while activating Role Management Common bundle", e);
        }
    }

    /**
     * Method to deactivate bundle.
     *
     * @param ctxt OSGi component context.
     */
    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Role Mgt bundle is deactivated ");
        }
    }
}