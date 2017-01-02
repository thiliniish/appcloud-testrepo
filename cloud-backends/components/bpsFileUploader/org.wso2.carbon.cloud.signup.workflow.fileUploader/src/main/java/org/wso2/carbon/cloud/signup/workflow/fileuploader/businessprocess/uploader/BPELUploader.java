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

package org.wso2.carbon.cloud.signup.workflow.fileuploader.businessprocess.uploader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.configreader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.constants.Constants;

import java.io.IOException;

/**
 * This Class handles the task of uploading of configuring the changes for the Business Process file to the BPS
 * server in to the tenant's domain
 */
public class BPELUploader {

    private static final Log log = LogFactory.getLog(BPELUploader.class);
    private String errorMessage;

    /**
     * This method acts as one that configures all needed parameters and processes to get the bpel file prepared to
     * be uploaded
     *
     * @param sessionCookie is the authorization header to be passed
     * @param username      is the username of the tenant
     * @param backEndUrl    the url of the bps server to where the files need to be uploded to
     * @return if the entire process was successful or not
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public boolean uploadBpel(String sessionCookie, String username, String backEndUrl)
            throws IOException, ParseException,
                   InterruptedException {
        boolean uploadResult = false;
        //Loading the configuration properties

        //location of the bpel archive file to be uploaded to the BPS
        String bpelLocation = Constants.CARBON_HOME + ConfigFileReader.retrieveConfigAttribute(
                "archiveConfigurations", "BPEL_ARCHIVE_FILE_LOCATION");

        //instantiating the configureBpel class
        ConfigureBpel config = new ConfigureBpel();

        String bpelFilename = config.configureBpel(username);

        if (!("".equalsIgnoreCase(bpelFilename))) {
            log.info("Completed the BPEL file configurations for the user " + username);

            BPELUploaderClient serviceAdminClient =
                    new BPELUploaderClient(backEndUrl, sessionCookie, username);

            //calling the method to upload the business process to the bps server
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Invoking the service admin client for the user " + username);
                }
                serviceAdminClient.deployPackage(bpelFilename, bpelLocation, username);
                uploadResult = true;
            } catch (InterruptedException e) {
                errorMessage =
                        "An exception occurred while uploading the BPEL to tbe bps server for the self sign up " +
                        "feature for the user " +
                        username;
                log.error(errorMessage, e);
                throw new InterruptedException(errorMessage);
            }
        } else {
            uploadResult = false;
            log.error("Error occurred while configure the bpel files for the user " + username);
        }
        return uploadResult;
    }
}

