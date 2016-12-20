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

package org.wso2.carbon.cloud.signup.workflow.fileUploader.humanTaskUploader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.configReader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.constants.Constants;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.fileReader.ArchiveConverter;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.fileReader.FileAccessor;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.utils.Utils;

import java.io.IOException;

/**
 * Changes the Human Task files' configurations based on the tenant information
 */
public class ConfigureHumanTask {

    private static final Log log = LogFactory.getLog(ConfigureHumanTask.class);

    /**
     * This method configures the parameters and files that needs to be changed for the Human Taask files
     *
     * @param userName is the username of the tenant
     * @return the archive name of the human task
     * @throws ParseException
     * @throws IOException
     */
    public String configureHumanTask(String userName) throws ParseException, IOException {
        String carbonHome = Constants.CARBON_HOME;
        String htSouceFileLocation;
        String fileName;
        String errorMessage;
        FileAccessor fileAccessor = new FileAccessor();
        ArchiveConverter convertToArchive = new ArchiveConverter();
        Utils utilObject = new Utils();
        try {

            //obtaining the file location of the file needed to be changed according to the tenant information
            htSouceFileLocation = carbonHome + ConfigFileReader
                    .retrieveConfigAttribute("fileLocations", "HT_FILES_SOURCE_LOCATION");

            //Obtaining the file where the tenant specific contents need to be replaced in
            String userApprovalTaskWsdlFile = htSouceFileLocation + ConfigFileReader
                    .retrieveConfigAttribute("tenantSpecificBPSFiles",
                                             "HT_USER_APPROVAL_TASK_WSDL_FILE");

            String tenant = utilObject.getTenantDomain(userName);
            if (log.isDebugEnabled()) {
                log.debug("tenant domain for the user " + userName + " is " + tenant +
                          " for the sign up workflow bps file uploader");
            }

            //retrieving the contents of the tenant specific human task file.
            String userApprovalTaskWsdlFileText = fileAccessor.fileReader(userApprovalTaskWsdlFile);

            if (log.isDebugEnabled()) {
                log.debug("replacing the tenant specific content in the file");
            }
            userApprovalTaskWsdlFileText = utilObject.replaceUrls(userApprovalTaskWsdlFileText);

            fileAccessor.replaceFile(userApprovalTaskWsdlFile, userApprovalTaskWsdlFileText);

            //creating the human task archive
            fileName = convertToArchive.createHtArchive(tenant, htSouceFileLocation, userName);

            //file to be reverted
            fileAccessor.revertFiles(userApprovalTaskWsdlFile);
        } catch (ParseException parseException) {
            errorMessage =
                    "An error occurred while parsing the configuration file for the self sign up feature for the user " +
                    userName;
            log.error(errorMessage, parseException);
            throw new ParseException(0, parseException);
        } catch (IOException ioException) {
            errorMessage =
                    "An error occurred while reading the parsed the configuration file for the self sign up feature for the user " +
                    userName;
            log.error(errorMessage, ioException);
            throw new IOException(errorMessage, ioException);
        }
        return fileName;
    }
}
