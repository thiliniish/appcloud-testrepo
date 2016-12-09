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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.constants.Constants;
import org.wso2.carbon.humantask.stub.upload.HumanTaskUploaderStub;
import org.wso2.carbon.humantask.stub.upload.types.UploadedFileItem;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.rmi.RemoteException;

/**
 * This Class is the client that accesses the HTUploader service stub and enables to access the methods of that service
 */
public class HumanTaskUploaderClient {

    private static final Log log = LogFactory.getLog(HumanTaskUploaderClient.class);
    private final String serviceName = Constants.HT_UPLOADER_SERVICE_NAME;
    private HumanTaskUploaderStub serviceAdminStub;
    private String endPoint;
    String errorMessage;

    /**
     * Creates the client needed to upload the human task using the admin service.
     *
     * @param backEndUrl    the url of the BPS
     * @param sessionCookie is the session cookie for the tenant
     * @param userName      is the username of the tenant
     * @throws AxisFault
     */
    public HumanTaskUploaderClient(String backEndUrl, String sessionCookie, String userName)
            throws AxisFault {

        //endpoint to access the human task service
        this.endPoint = backEndUrl + "/services/" + serviceName;
        serviceAdminStub = new HumanTaskUploaderStub(endPoint);

        log.info("Authenticating the stub using the session cookie for the user " + userName);
        ServiceClient serviceClient;
        Options option;
        serviceClient = serviceAdminStub._getServiceClient();
        option = serviceClient.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                           sessionCookie);

        log.info(
                "successfully initialized the Human task uploader client for the user " + userName);

    }

    /**
     * This methods returns the files in the uploadable format.
     *
     * @param dataHandler
     * @param fileName
     * @param fileType
     * @return
     */
    private UploadedFileItem getUploadedFileItem(DataHandler dataHandler, String fileName,
                                                 String fileType) {
        UploadedFileItem uploadedFileItem = new UploadedFileItem();
        uploadedFileItem.setDataHandler(dataHandler);
        uploadedFileItem.setFileName(fileName);
        uploadedFileItem.setFileType(fileType);
        return uploadedFileItem;
    }

    /**
     * This method deploys the human task archive to the BPS
     *
     * @param packageName is the name of the archived package containing the HT files
     * @param resourceDir is the directory location of the Human task file
     * @param userName    is the username of the tenant
     * @return the result of the uploading process
     * @throws java.rmi.RemoteException
     * @throws InterruptedException
     */
    public boolean deployPackage(String packageName, String resourceDir, String userName)
            throws RemoteException, InterruptedException {
        boolean success = false;

        //setting the filename to be uploaded of the human task file
        String sampleArchiveName = packageName + ".zip";

        if (log.isDebugEnabled()) {
            log.debug("Resource location of the human task file :" + resourceDir + File.separator +
                      sampleArchiveName);
        }
        DataSource bpelDataSource =
                (DataSource) new FileDataSource(resourceDir + File.separator + sampleArchiveName);
        UploadedFileItem[] uploadedFileItems = new UploadedFileItem[1];

        //Adding the bpel files to the list to be uploaded
        uploadedFileItems[0] =
                getUploadedFileItem(new DataHandler(bpelDataSource), sampleArchiveName, "zip");

        log.info("Deploying " + sampleArchiveName + "for the user " + userName +
                 " for the self sign up feature file uploader");
        try {
            serviceAdminStub.uploadHumanTask(uploadedFileItems);
            success = true;
        } catch (RemoteException remoteException) {
            errorMessage =
                    "An error occurred while deploying the files to the BPS server for the self sign up feature for " +
                    "the user " +
                    userName;
            log.error(errorMessage, remoteException);
            throw new RemoteException(errorMessage, remoteException);
        }
        return success;
    }
}
