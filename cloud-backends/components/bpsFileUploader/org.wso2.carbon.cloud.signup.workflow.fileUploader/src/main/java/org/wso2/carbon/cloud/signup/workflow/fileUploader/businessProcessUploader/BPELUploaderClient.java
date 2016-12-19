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

package org.wso2.carbon.cloud.signup.workflow.fileUploader.businessProcessUploader;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.bpel.stub.upload.types.UploadedFileItem;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.constants.Constants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.rmi.RemoteException;

/**
 * This Class is the client that accesses the BPELUploader service stub and enables to access the methods of that service
 */

public class BPELUploaderClient {

    private static final Log log = LogFactory.getLog(BPELUploaderClient.class);
    private final String serviceName = Constants.BPEL_UPLOADER_SERVICE_NAME;
    private BPELUploaderStub serviceAdminStub;
    private String endPoint;
    private String errorMessage;

    /**
     * This method creates the bpel uploader client
     *
     * @param backEndUrl    if the url of the bps server
     * @param sessionCookie is the session cookie generated from the SAML token
     * @param userName      is the userName of the tenant
     * @throws AxisFault
     */
    public BPELUploaderClient(String backEndUrl, String sessionCookie, String userName)
            throws AxisFault {

        try {
            //endpoint to access the bpeluploader service
            this.endPoint = backEndUrl + "/services/" + serviceName;
            serviceAdminStub = new BPELUploaderStub(endPoint);

            if (log.isDebugEnabled()) {
                log.debug("Authenticating the stub using the session cookie for the user " + userName);
            }
            ServiceClient serviceClient;
            Options option;
            serviceClient = serviceAdminStub._getServiceClient();
            option = serviceClient.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                               sessionCookie);

            if (log.isDebugEnabled()) {
                log.debug("successfully initialized the BPELUploaderClient for the user " + userName);
            }
        } catch (AxisFault axisFault) {
            errorMessage =
                    "Error while initializing the BPELUploaderClient for the user " + userName;
            log.error(errorMessage, axisFault);
            throw new AxisFault(errorMessage);
        }
    }

    /**
     * Method to set the properties to uploaded BPEL archive
     *
     * @param dataHandler is the data handler
     * @param fileName    is the file name of the archived bpel file
     * @param fileType    is the type of archive
     * @return
     */
    public UploadedFileItem getUploadedFileItem(DataHandler dataHandler, String fileName,
                                                String fileType) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the information of the uploaded file item " + fileName);
        }
        UploadedFileItem uploadedFileItem = new UploadedFileItem();
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(fileName);
        uploadedFileItem.setFileType(fileType);
        return uploadedFileItem;
    }

    /**
     * Method to upload the BPEL archive using the BPEL uploader admin service
     *
     * @param packageName is the name of the archived package containing the BPEL files
     * @param resourceDir is the location where the archive file is saved
     * @param userName    is the username of the tenant
     * @return if the deploymenet of the BPEL file to the server was a success
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     */
    public boolean deployPackage(String packageName, String resourceDir, String userName)
            throws RemoteException, InterruptedException {
        boolean success = false;
        //setting the filename to be uploaded
        try {
            String sampleArchiveName = packageName + ".zip";

            if (log.isDebugEnabled()) {
                log.debug("Resource location:" + resourceDir + File.separator + sampleArchiveName);
            }

            DataSource bpelDataSource = (DataSource) new FileDataSource(
                    resourceDir + File.separator + sampleArchiveName);
            UploadedFileItem[] uploadedFileItems = new UploadedFileItem[1];

            //Adding the bpel files to the list to be uploaded
            uploadedFileItems[0] =
                    getUploadedFileItem(new DataHandler(bpelDataSource), sampleArchiveName, "zip");

            log.info("Deploying " + sampleArchiveName + " for the user " + userName +
                     " for the self sign up feature file uploader");

            serviceAdminStub.uploadService(uploadedFileItems);
            success = true;
        } catch (RemoteException remoteException) {
            errorMessage =
                    "An error occurred while deploying the files to the BPS server for the self sign up feature for the user " +
                    userName;
            log.error(errorMessage, remoteException);
            throw new RemoteException(errorMessage, remoteException);
        }
        return success;
    }
}
