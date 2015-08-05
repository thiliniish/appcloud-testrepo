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
package org.wso2.carbon.cloud.billing.processor;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.common.BillingConstants;
import org.wso2.carbon.cloud.billing.common.CloudBillingException;
import org.wso2.carbon.cloud.billing.common.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Represent the request api processor for zuora
 */
public class ZuoraBillingRequestProcessor extends AbstractBillingRequestProcessor {

    private static final Log log = LogFactory.getLog(ZuoraBillingRequestProcessor.class);
    private static String uploadURL = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getApiConfigs()
            .getUsage();

    public ZuoraBillingRequestProcessor(HttpClientConfig httpClientConfig) {
        super(httpClientConfig);
    }

    public void doUpload() throws CloudBillingException {
        PostMethod post = new PostMethod(uploadURL);
        // indicate accept response body in JSON
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);

        String apiAccessKeyId = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getUser();
        String apiSecretAccessKey = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getPassword();
        post.addRequestHeader(BillingConstants.API_ACCESS_KEY_ID, apiAccessKeyId);
        post.addRequestHeader(BillingConstants.API_SECRET_ACCESS_KEY, apiSecretAccessKey);

        /**
         * Initializing the multipart contents used by the usage file uploader functionality.
         */
        File file =
                new File(CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getUsageConfig()
                                 .getUsageUploadFileLocation());
        Part part;
        try {
            part = new FilePart(BillingConstants.FILE_PART_NAME, file);
        } catch (FileNotFoundException e) {
            String msg = "Error occurred while reading usage data";
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
        RequestEntity uploadReqEntity = new MultipartRequestEntity(new Part[]{part}, new HttpClientParams());
        post.setRequestEntity(uploadReqEntity);
        try {
            this.getHttpClient().executeMethod(post);
        } catch (Exception e) {
            String msg = "Error while uploading usage to " + uploadURL;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        } finally {
            post.releaseConnection();
        }
    }

    public String doGet(String url) throws CloudBillingException {
        GetMethod get = new GetMethod(url);
        try {
            String apiAccessKeyId = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getUser();
            String apiSecretAccessKey = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getPassword();
            get.addRequestHeader(BillingConstants.API_ACCESS_KEY_ID, apiAccessKeyId);
            get.addRequestHeader(BillingConstants.API_SECRET_ACCESS_KEY, apiSecretAccessKey);

            // indicate accept response body in JSON
            get.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);
            // for a GET call, chase redirects
            get.addRequestHeader(BillingConstants.HTTP_FOLLOW_REDIRECT, "true");

            int response;
            String result = null;

            for (int index = 0; index <= 10; index++) {
                try {
                    if (log.isDebugEnabled() && index > 0) {
                        log.debug("Retrying : " + index + " to get data from " + url);
                    }
                    response = this.getHttpClient().executeMethod(get);

                    if (response == 404) {
                        log.error("Failed with HTTP error code : " + response +
                                  ". ZUORA Signature API End Point is" + " incorrect.");
                        throw new CloudBillingException("Failed with HTTP error code : " + response +
                                                        ". ZUORA Signature API End Point is" + " incorrect.");
                    } else if (response == 401) {
                        log.error("Failed with HTTP error code : " + response +
                                  ". ZUORA Login's Username or " + "Password is incorrect.");
                        throw new CloudBillingException("Failed with HTTP error code : " + response +
                                                        ". ZUORA Signature API End Point is" + " incorrect.");
                    } else if (response != 200) {
                        log.error("Failed with HTTP error code : " + response);
                    } else {
                        if (get.getResponseBody().length > 0) {
                            result = get.getResponseBodyAsString();
                        }
                        break;
                    }
                } catch (HttpException e) {
                    String msg = "Error while getting data from " + url;
                    log.error(msg, e);
                    if (index == 10) {
                        throw new CloudBillingException(msg, e);
                    }
                } catch (IOException e) {
                    String msg = "Error while getting data from " + url;
                    log.error(msg, e);
                    if (index == 10) {
                        throw new CloudBillingException(msg, e);
                    }
                } catch (Exception e) {
                    String msg = "Error while getting data from " + url;
                    log.error(msg, e);
                    if (index == 10) {
                        throw new CloudBillingException(msg, e);
                    }
                }
                index = index + 1;
            }
            return result;
        } catch (Exception e) {
            String msg = "Error while getting data from " + url;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        } finally {
            get.releaseConnection();
        }
    }

    @Override
    public String doPost(String url, String jsonPayload) throws CloudBillingException {

        PostMethod post = new PostMethod(url);
        // indicate accept response body in JSON
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);

        String apiAccessKeyId = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getUser();
        String apiSecretAccessKey = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getPassword();
        post.addRequestHeader(BillingConstants.API_ACCESS_KEY_ID, apiAccessKeyId);
        post.addRequestHeader(BillingConstants.API_SECRET_ACCESS_KEY, apiSecretAccessKey);

        RequestEntity requestEntity;
        try {
            requestEntity = new StringRequestEntity(jsonPayload, BillingConstants.HTTP_RESPONSE_TYPE_JSON,
                                                    BillingConstants.ENCODING);
            post.setRequestEntity(requestEntity);
        } catch (UnsupportedEncodingException e) {
            String msg = "Error occured while encoding json payload" + jsonPayload;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
        String result = null;
        int response;
        for (int index = 0; index <= 10; index++) {
            try {
                if (log.isDebugEnabled() && index > 0) {
                    log.debug("Retrying : " + index + " to get data from " + url);
                }
                response = this.getHttpClient().executeMethod(post);

                if (response == 404) {
                    log.error("Failed with HTTP error code : " + response + ". ZUORA Signature API End Point is" +
                              " incorrect.");
                    throw new CloudBillingException("Failed with HTTP error code : " + response +
                                                    ". ZUORA Signature API End Point is" + " incorrect.");
                } else if (response == 401) {
                    log.error("Failed with HTTP error code : " + response + ". ZUORA Login's Username or " +
                              "Password is incorrect.");
                    throw new CloudBillingException("Failed with HTTP error code : " + response +
                                                    ". ZUORA Signature API End Point is" + " incorrect.");
                } else if (response != 200) {
                    log.error("Failed with HTTP error code : " + response);
                } else {
                    if (post.getResponseBody().length > 0) {
                        result = post.getResponseBodyAsString();
                    }
                    break;
                }
            } catch (HttpException e) {
                String msg = "Error while getting data from " + url;
                log.error(msg, e);
                if (index == 10) {
                    throw new CloudBillingException(msg, e);
                }
            } catch (IOException e) {
                String msg = "Error while getting data from " + url;
                log.error(msg, e);
                if (index == 10) {
                    throw new CloudBillingException(msg, e);
                }
            } catch (Exception e) {
                String msg = "Error while getting data from " + url;
                log.error(msg, e);
                if (index == 10) {
                    throw new CloudBillingException(msg, e);
                }
            }
            index = index + 1;
        }
        return result;
    }

    @Override
    public String doPost(String url, NameValuePair[] keyValuePair) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by Zuora Billing Request Processor");
    }
}