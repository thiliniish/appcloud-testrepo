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
import org.wso2.carbon.cloud.signup.workflow.fileuploader.filereader.ArchiveConverter;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.filereader.FileAccessor;
import org.wso2.carbon.cloud.signup.workflow.fileuploader.utils.Utils;

import java.io.IOException;

/**
 * Changes the Business process files' configurations based on the tenant information
 */
public class ConfigureBpel {

    private static final Log log = LogFactory.getLog(ConfigureBpel.class);

    /**
     * This method configures the parameters and files that needs to be changed for the BPEL files
     *
     * @param userName of the tenant
     * @return the name of the final archive file
     * @throws ParseException
     * @throws IOException
     */
    public String configureBpel(String userName) throws ParseException, IOException {

        //obtaining the file location of the file needed to be changed according to the tenant information
        String carbonHome = Constants.CARBON_HOME;
        String fileName = "";
        String bpelSouceFileLocation;
        Utils utilObject = new Utils();
        String errorMessage = "";
        String fileToReturn = "";

        //instantiating the file accessor class to manipulate the bpel files
        FileAccessor fileAccessor = new FileAccessor();
        ArchiveConverter convertToArchive = new ArchiveConverter();
        try {

            //retrieving the source location of the bpel files in the server
            bpelSouceFileLocation = carbonHome + ConfigFileReader
                    .retrieveConfigAttribute("fileLocations", "BPEL_FILES_SOURCE_LOCATION");

            //retrieving the tenant domain for the file
            String tenant = utilObject.getTenantDomain(userName);
            if (log.isDebugEnabled()) {
                log.debug("The tenant domain for the user " + userName + " is " + tenant +
                          " for the sign up workflow bps file uploader");
            }

            //retrieving the file where the tenant specific details needs to be replaced in
            String userSignUpEprFile = bpelSouceFileLocation + ConfigFileReader
                    .retrieveConfigAttribute("tenantSpecificBPSFiles",
                                             "BPEL_SIGNUP_SERVICE_EPR_FILE");

            String userSignUpProcessFile = bpelSouceFileLocation + ConfigFileReader
                    .retrieveConfigAttribute("tenantSpecificBPSFiles",
                                             "BPEL_SIGNUP_PROCESS_EPR_FILE");

            //retrieving the contents of the tenant specific bpel file.
            String userSignUpEprFiletxt = fileAccessor.fileReader(userSignUpEprFile);
            String userSignUpProcessFiletxt = fileAccessor.fileReader(userSignUpProcessFile);

            if (log.isDebugEnabled()) {
                log.debug("Replacing the tenant specific content in the file for the user " +
                          userName +
                          " for the sign up workflow bps file uploader");
            }
            userSignUpEprFiletxt = utilObject.replaceUrls(userSignUpEprFiletxt);
            userSignUpProcessFiletxt = utilObject.replaceUrls(userSignUpProcessFiletxt);

            //overwriting the tenant specific content to the existing file
            fileAccessor.replaceFile(userSignUpEprFile, userSignUpEprFiletxt);
            fileAccessor.replaceFile(userSignUpProcessFile, userSignUpProcessFiletxt);

            //creating the BPEL archive file
            fileName = convertToArchive.createBpelArchive(tenant, bpelSouceFileLocation, userName);

            //reverting files back to original State
            fileAccessor.revertFiles(userSignUpEprFile);
            fileAccessor.revertFiles(userSignUpProcessFile);

        } catch (ParseException parseException) {
            errorMessage =
                    "An error occurred while parsing the configuration file for the self sign up file uploader " +
                    "feature for the user " +
                    userName;
            log.error(errorMessage, parseException);
            throw new ParseException(0, parseException);
        } catch (IOException ioException) {
            errorMessage =
                    "An error occurred while reading the parsed the configuration file for the self sign up file " +
                    "uploader feature for the user " +
                    userName;
            log.error(errorMessage, ioException);
            throw new IOException(errorMessage, ioException);
        }
        if (!"".equalsIgnoreCase(fileName)) {
            fileToReturn = fileName;
        }
        return fileToReturn;
    }
}
