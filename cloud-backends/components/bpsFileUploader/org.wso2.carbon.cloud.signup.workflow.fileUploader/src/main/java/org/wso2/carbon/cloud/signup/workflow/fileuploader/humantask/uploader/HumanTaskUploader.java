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

package org.wso2.carbon.cloud.signup.workflow.fileuploader.humantask.uploader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.configreader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.constants.Constants;

import java.io.IOException;

/**
 * This Class handles the task of uploading of configuring the changes for the Human Task file to the BPS server in
 * to the tenant's domain
 */
public class HumanTaskUploader {
    private static final Log log = LogFactory.getLog(HumanTaskUploader.class);
    private String errorMessage;

    /**
     * This method acts as one that configures all needed parameters and processes to get the human task file
     * prepared to be uploaded
     *
     * @param session    is the session cookie generated from the SAML token
     * @param username   is the username of the tenant
     * @param backEndUrl is the url of the BPS server
     * @return if it was successful
     * @throws InterruptedException
     * @throws ParseException
     * @throws IOException
     */
    public boolean uploadHumanTask(String session, String username, String backEndUrl)
            throws InterruptedException, ParseException,
                   IOException {
        //Loading the configuration properties
        boolean result = false;
        try {
            //location of the human task archive file to be uploaded to the BPS
            String humanTaskLocation = Constants.CARBON_HOME + ConfigFileReader.retrieveConfigAttribute(
                    "archiveConfigurations", "HT_ARCHIVE_FILE_LOCATION");

            //instantiating the configureHumanTask class
            ConfigureHumanTask htConfig = new ConfigureHumanTask();
            String htFileName = htConfig.configureHumanTask(username);

            HumanTaskUploaderClient serviceAdminClient =
                    new HumanTaskUploaderClient(backEndUrl, session, username);

            //calling the method to upload the human task

            try {
                serviceAdminClient.deployPackage(htFileName, humanTaskLocation, username);
                result = true;
            } catch (InterruptedException e) {
                errorMessage =
                        "An exception occurred while uploading the BPEL to tbe bps server for the self sign up " +
                        "feature for the user " +
                        username;
                log.error(errorMessage, e);
                throw new InterruptedException(errorMessage);
            }
        } catch (ParseException parseException) {
            errorMessage =
                    "An error occurred while parsing the configuration file for the self sign up feature for the user" +
                    " " +
                    username;
            log.error(errorMessage, parseException);
            throw new ParseException(0, parseException);
        } catch (IOException ioException) {
            errorMessage =
                    "An error occurred while reading the parsed the configuration file for the self sign up feature " +
                    "for the user " +
                    username;
            log.error(errorMessage, ioException);
            throw new IOException(errorMessage, ioException);
        }
        return result;
    }
}

