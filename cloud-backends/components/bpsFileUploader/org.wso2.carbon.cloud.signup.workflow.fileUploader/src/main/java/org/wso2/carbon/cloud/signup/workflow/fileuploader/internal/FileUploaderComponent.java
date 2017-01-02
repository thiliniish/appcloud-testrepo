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

package org.wso2.carbon.cloud.signup.workflow.fileuploader.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.uploader.BPSFileUploader;

/**
 * @scr.component name="org.wso2.signup.workflow.fileuploader" immediate="true"
 */

public class FileUploaderComponent {

    private static final Log log = LogFactory.getLog(FileUploaderComponent.class);
    private ServiceRegistration bpsFileUploader;

    /**
     * This method will activate the osgi bundle
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        BundleContext bundleContext = componentContext.getBundleContext();

        bpsFileUploader = bundleContext
                .registerService(BPSFileUploader.class.getName(), new BPSFileUploader(), null);
        log.info("Activating the BPS File Uploader component");
    }

    /**
     * This method will deactivate the osgi bundle
     *
     * @param componentContext
     */
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating the File Uploader component");
        }
    }

}


