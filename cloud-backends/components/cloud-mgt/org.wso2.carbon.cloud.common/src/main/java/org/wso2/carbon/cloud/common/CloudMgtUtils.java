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

package org.wso2.carbon.cloud.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * This is a utility class used for the cloudmgt common component
 */
public class CloudMgtUtils {
    private static final Log LOG = LogFactory.getLog(CloudMgtUtils.class);

    /**
     * This method retrieves the maximum number of retries allowed for the cloud invitee emails
     *
     * @return
     * @throws CloudMgtException
     */
    public int getCloudInviteeRetryCount() throws CloudMgtException {
        String fileLocation = CarbonUtils.getCarbonConfigDirPath() +
                              File.separator + CloudMgtConstants.CONFIG_FOLDER +
                              File.separator + CloudMgtConstants.CONFIG_FILE_NAME;
        int cloudInviteeRetryCount;
        try {
            CloudMgtConfiguration configuration =
                    new CloudMgtConfigurationBuilder(fileLocation).buildCloudMgtConfiguration();
            if (configuration.getFirstProperty(CloudMgtConstants.CLOUD_INVITEE_RETRY_COUNT_PROPERTY) != null) {
                cloudInviteeRetryCount = Integer.parseInt(
                        configuration.getFirstProperty(CloudMgtConstants.CLOUD_INVITEE_RETRY_COUNT_PROPERTY));
            } else {
                LOG.error("Unable to read the retry count for the cloud invitees from the cloud-mgt.xml");
                throw new CloudMgtException("Unable to read the retry count for the cloud invitees");
            }
        } catch (CloudMgtException e) {
            LOG.error("An error occurred while reading the configuration file " + fileLocation);
            throw new CloudMgtException("Unable to get the retry count for the cloud invitees");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("The retry count for the cloud invitees is " + cloudInviteeRetryCount);
        }
        return cloudInviteeRetryCount;
    }

}
