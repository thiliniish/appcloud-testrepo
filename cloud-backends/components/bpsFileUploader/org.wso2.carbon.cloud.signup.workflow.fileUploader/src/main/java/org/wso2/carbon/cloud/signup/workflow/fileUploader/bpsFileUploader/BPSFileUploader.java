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

package org.wso2.carbon.cloud.signup.workflow.fileUploader.bpsFileUploader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.businessProcessUploader.BPELUploader;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.configReader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.constants.Constants;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.humanTaskUploader.HumanTaskUploader;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.utils.Utils;

import java.io.IOException;

/**
 * This class is responsible for triggering the uploading process for the human task and the bpel file
 */
public class BPSFileUploader {
    private static final Log log = LogFactory.getLog(BPSFileUploader.class);

    /**
     * This method gets invoked when the files need to be uploaded through the jaggery application
     *
     * @param username      is the username of the tenant who needs to upload the files
     * @param sessionCookie is the cookie passed after SSO from the jaggery app
     * @return if the uploading of the two files were successfull
     * @throws InterruptedException
     * @throws ParseException
     * @throws IOException
     */

    public boolean bpsFileUploader(String username, String sessionCookie)
            throws InterruptedException, ParseException, IOException {
        //Loading the configuration properties

        String errorMessage;
        boolean htUploadResult;
        boolean bpelUploaderResult;
        boolean uploadedResult = false;
        try {
            String backEndUrl = ConfigFileReader
                    .retrieveConfigAttribute("configUrls", "BPS_BACKEND_URL");
            if (log.isDebugEnabled()) {
                log.debug(
                        "Obtaining the session cookie: " + sessionCookie + " for the user " + username);
            }

            Utils utilObject = new Utils();
            String tenantDomain = utilObject.getTenantDomain(username);

            //setting the tenant specific properties to the map
            utilObject.setTenantSpecificUrls(tenantDomain);

            BPELUploader bpelUploader = new BPELUploader();
            HumanTaskUploader humanTaskUploader = new HumanTaskUploader();
            bpelUploaderResult = bpelUploader.uploadBpel(sessionCookie, username, backEndUrl);
            if (bpelUploaderResult) {
                log.info("Successfully uploaded the BPEL file to the BPS for the user " + username);
            } else {
                log.error("Unable to upload the BPEL file to the BPS for the user " + username);
            }

            //Giving time for the bpel to be uploaded before the upload of the human task starts
            try {
                Thread.sleep(Constants.DELAY_TIME);
            } catch (InterruptedException e) {
                errorMessage =
                        "An error occurred while the thread was kept to be delayed for " + Constants.DELAY_TIME +
                        "milliseconds";
                log.error(errorMessage, e);
                throw new InterruptedException(errorMessage);
            }

            htUploadResult = humanTaskUploader.uploadHumanTask(sessionCookie, username, backEndUrl);
            if (htUploadResult) {
                log.info("Successfully uploaded the Human Task file to the BPS for the user " +
                         username);
            } else {
                log.error("Unable to upload the  Human Task file to the BPS for the user " +
                          username);
            }
            if (bpelUploaderResult && htUploadResult) {
                uploadedResult = true;
            } else if (bpelUploaderResult && !htUploadResult) {
                uploadedResult = false;
                log.error(
                        "There was an error in uploading the Human Task file to the server for the user " +
                        username);
            } else if (!bpelUploaderResult && htUploadResult) {
                uploadedResult = false;
                log.error(
                        "There was an error in uploading the BPEL file to the server for the user " +
                        username);
            } else {
                uploadedResult = false;
                log.error(
                        "There was an error in uploading the BPEL and Human Task file to the server for the user " +
                        username);
            }
        } catch (IOException ioException) {
            errorMessage =
                    "An error occurred while reading the parsed the configuration file for the self sign up feature file uploader for the user " +
                    username;
            log.error(errorMessage, ioException);
            throw new IOException(errorMessage, ioException);
        }
        return uploadedResult;
    }
}
