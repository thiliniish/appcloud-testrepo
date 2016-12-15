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

package org.wso2.carbon.cloud.signup.workflow.fileUploader.fileReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.configReader.ConfigFileReader;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.constants.Constants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class creates the archive files
 */
public class ArchiveConverter {

    private static final Log log = LogFactory.getLog(ArchiveConverter.class);
    private String errorMessage;

    public void createNewArchive(List<String> files, String fileName, String fileLocation,
                                 String userName) throws FileNotFoundException, IOException {
        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream;
        FileInputStream fileInputStream;
        try {
            fileOutputStream = new FileOutputStream(fileLocation + fileName + ".zip");
            if (log.isDebugEnabled()) {
                log.debug(fileLocation + fileName + ".zip");
            }
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

            for (String filePath : files) {
                try {
                    File input = new File(filePath);
                    fileInputStream = new FileInputStream(input);
                    ZipEntry zipEntry = new ZipEntry(input.getName());
                    zipOutputStream.putNextEntry(zipEntry);

                    byte[] temp = new byte[4 * 1024];
                    int size = 0;

                    //writes as a stream of bytes.
                    while ((size = fileInputStream.read(temp)) != -1) {
                        zipOutputStream.write(temp, 0, size);
                    }
                    zipOutputStream.flush();
                    fileInputStream.close();
                } catch (FileNotFoundException e) {
                    errorMessage = "The file " + filePath + " could not be found";
                    log.error(errorMessage, e);
                    throw new FileNotFoundException(errorMessage);
                } catch (IOException e) {

                    errorMessage = "Could not read the contents of the file " + filePath +
                                   " could not be found";
                    throw new IOException(errorMessage, e);
                }
            }
            zipOutputStream.close();

            if (log.isDebugEnabled()) {
                log.debug("Successfully Zipped the file to " + fileLocation + fileName + ".zip");
            }
        } catch (FileNotFoundException e) {
            errorMessage = "The file could not be found " + fileName;
            log.error(errorMessage, e);
            throw new FileNotFoundException(errorMessage);
        } catch (IOException e) {
            errorMessage = "Unable to read the contents of the file " + fileName;
            log.error(errorMessage, e);
            throw new IOException(errorMessage);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    log.info("Stream was closed successfully");
                }
            } catch (Exception ex) {
                errorMessage = "Error while closing the stream for the user" + userName;
                log.error(errorMessage);
                log.error(errorMessage, ex);
            }
        }
    }

    /**
     * This method obtains the files needed to create a BPEL archive file
     *
     * @param tenantDomain     is the domain of the tenant
     * @param bpelFileLocation is the location where the bpel files are located
     * @param userName         is the username of the tenant
     * @return the name of the created archive
     * @throws ParseException
     * @throws IOException
     */
    public String createBpelArchive(String tenantDomain, String bpelFileLocation, String userName)
            throws ParseException, IOException {

        String archiveName = "";
        String carbonHome = Constants.CARBON_HOME;
        List<String> files = new ArrayList<String>();

        try {
            //location to which the bpel archive file needs to be saved to
            String bpelArchiveFileLocation = carbonHome + ConfigFileReader
                    .retrieveConfigAttribute("archiveConfigurations",
                                             "BPEL_ARCHIVE_FILE_LOCATION") + File.separator;

            //The name the bpel archive file will be named as
            String bpelFIleName = ConfigFileReader
                    .retrieveConfigAttribute("archiveConfigurations", "BPEL_ARCHIVE_FILE_NAME");
            archiveName = bpelFIleName + tenantDomain;

            //The files needed to create the bpel archive
            JSONObject bpelFilesObject =
                    ConfigFileReader.retrieveConfigProperty("BPSFiles", tenantDomain);
            JSONArray bpelFileArray = (JSONArray) bpelFilesObject.get("bpelFiles");

            //adding the files to be archived to a list
            for (int i = 0; i < bpelFileArray.size(); i++) {
                files.add(bpelFileLocation + bpelFileArray.get(i));

                if (log.isDebugEnabled()) {
                    log.debug(bpelFileLocation + bpelFileArray.get(i) +
                              " was added successfully for the user " + userName +
                              " of the tenant domain " + tenantDomain);
                }
            }

            createNewArchive(files, archiveName, bpelArchiveFileLocation, userName);

        } catch (ParseException parseException) {
            errorMessage =
                    "An error occurred while parsing the configuration file for the self sign up feature for the user" +
                    " " +
                    userName;
            log.error(errorMessage, parseException);
            throw new ParseException(0, parseException);
        } catch (IOException ioException) {
            errorMessage =
                    "An error occurred while reading the parsed the configuration file for the self sign up feature " +
                    "for the user " +
                    userName;
            log.error(errorMessage, ioException);
            throw new IOException(errorMessage, ioException);
        }
        return archiveName;
    }

    /**
     * This method obtains the files needed to create a Human Task archive file
     *
     * @param tenantDomain         is the tenant domain
     * @param htFileSourceLocation is the file location of the human task files
     * @param userName             is the username of the tenant
     * @return the filename of the human task archive
     * @throws ParseException
     * @throws IOException
     */
    public String createHtArchive(String tenantDomain, String htFileSourceLocation, String userName)
            throws ParseException, IOException {

        String archiveName = "";
        String humanTaskArchiveFileLocation;
        String carbonHome = Constants.CARBON_HOME;
        List<String> files = new ArrayList<String>();
        try {

            //retrieving the location to which the archive file will be saved.
            humanTaskArchiveFileLocation = carbonHome + ConfigFileReader
                    .retrieveConfigAttribute("archiveConfigurations", "HT_ARCHIVE_FILE_LOCATION") +
                                           File.separator;

            //The name to be given for the human task archive to be created.
            String humanTaskFileName = ConfigFileReader
                    .retrieveConfigAttribute("archiveConfigurations", "HT_ARCHIVE_FILE_NAME");
            archiveName = humanTaskFileName + tenantDomain;

            //Getting the files that need to be added in order to create the ht archive file
            JSONObject htFileObject =
                    ConfigFileReader.retrieveConfigProperty("BPSFiles", tenantDomain);
            JSONArray htFileArray = (JSONArray) htFileObject.get("humanTaskFiles");

            // Adding the files to the list
            for (int i = 0; i < htFileArray.size(); i++) {
                files.add(htFileSourceLocation + htFileArray.get(i));
                log.debug(htFileSourceLocation + htFileArray.get(i) +
                          " was added successfully for the user " + userName +
                          " of the tenant domain " + tenantDomain);
            }
            createNewArchive(files, archiveName, humanTaskArchiveFileLocation, userName);

            //file to be reverted
            FileAccessor fileAccessor = new FileAccessor();
            String userApprovalTaskWsdlFile = htFileSourceLocation + ConfigFileReader
                    .retrieveConfigAttribute("tenantSpecificBPSFiles",
                                             "HT_USER_APPROVAL_TASK_WSDL_FILE");
            fileAccessor.revertFiles(userApprovalTaskWsdlFile);
        } catch (ParseException parseException) {
            errorMessage =
                    "An error occurred while parsing the configuration file for the self sign up feature for the user" +
                    " " +
                    userName;
            log.error(errorMessage, parseException);
            throw new ParseException(0, parseException);
        } catch (IOException ioException) {
            errorMessage =
                    "An error occurred while reading the parsed the configuration file for the self sign up feature " +
                    "for the user " +
                    userName;
            log.error(errorMessage, ioException);
            throw new IOException(errorMessage, ioException);
        }
        return archiveName;
    }
}
