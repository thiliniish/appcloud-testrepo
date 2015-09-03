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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.commons.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Represent the request api processor for data services
 */
public class DataServiceBillingRequestProcessor extends AbstractBillingRequestProcessor {

    private static final Log log = LogFactory.getLog(DataServiceBillingRequestProcessor.class);
    private static String basicAuthHeader;

    static {
        try {
            /**
             * Initializing Basic Auth header out of the username and the
             * password extracted out form the billing
             * configuration.
             */
            basicAuthHeader = initBasicAuthHeader();
        } catch (Exception e) {
            log.error("Error while initializing upload request entity", e);
        }
    }

    public DataServiceBillingRequestProcessor(HttpClientConfig httpClientConfig) {
        super(httpClientConfig);
    }

    private static String initBasicAuthHeader() throws CloudBillingException {
        String userName = CloudBillingUtils.getBillingConfiguration().getDSConfig().getUser();
        String password = CloudBillingUtils.getBillingConfiguration().getDSConfig().getPassword();
        try {
            return "Basic " +
                   DatatypeConverter.printBase64Binary((userName + ":" + password).getBytes(BillingConstants.ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while initializing encoding basic auth header", e);
        }
    }

    public String doGet(String url) throws CloudBillingException {
        GetMethod get = new GetMethod(url);
        try {
            String trustStorePath = CloudBillingUtils.getBillingConfiguration().getSSOConfig().getKeyStorePath();
            String password = CloudBillingUtils.getBillingConfiguration().getSSOConfig().getTrustStorePassword();
            System.setProperty(BillingConstants.TRUSTSTORE_NAME_PROPERTY, trustStorePath);
            System.setProperty(BillingConstants.TRUSTSTORE_PASSWORD_PROPERTY, password);

            if (basicAuthHeader == null || "".equals(basicAuthHeader)) {
                throw new IllegalStateException("Data Service Billing Processor is not initialized properly");
            }
            get.addRequestHeader("Authorization", basicAuthHeader);
            get.addRequestHeader(BillingConstants.HTTP_FOLLOW_REDIRECT, "true");

            int statusCode = this.getHttpClient().executeMethod(get);
            if (log.isDebugEnabled()) {
                log.debug("HTTP status code of get request " + url.trim() + " is " + statusCode);
            }
            return get.getResponseBodyAsString();
        } catch (Exception e) {
            String msg = "Error while getting data from " + url;
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        } finally {
            get.releaseConnection();
        }
    }

    public void doUpload() throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported for Data Service Billing request "
                                                + "processor");
    }

    @Override
    public String doPost(String url, String jsonPayload) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by Data Service Billing Request " +
                                                "Processor");
    }

    @Override
    public String doPost(String url, NameValuePair[] keyValuePair) throws CloudBillingException {
        PostMethod post = new PostMethod(url);
        // indicate accept response body in JSON
        String trustStorePath = CloudBillingUtils.getBillingConfiguration().getSSOConfig().getKeyStorePath();
        String password = CloudBillingUtils.getBillingConfiguration().getSSOConfig().getTrustStorePassword();
        System.setProperty(BillingConstants.TRUSTSTORE_NAME_PROPERTY, trustStorePath);
        System.setProperty(BillingConstants.TRUSTSTORE_PASSWORD_PROPERTY, password);
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, BillingConstants.HTTP_RESPONSE_TYPE_JSON);


        post.setRequestBody(keyValuePair);

        String result = null;
        int response;
        for (int index = 0; index <= 10; index++) {
            try {
                if (log.isDebugEnabled() && index > 0) {
                    log.debug("Retrying : " + index + " to get data from " + url);
                }
                response = this.getHttpClient().executeMethod(post);

                if (response == 404) {
                    String eMessage = "Failed with HTTP error code : " + response + ". Billing API End Point is " +
                                      "incorrect.";
                    log.error(eMessage);
                    throw new CloudBillingException(eMessage);
                } else if (response == 401) {
                    String eMessage = "Failed with HTTP error code : " + response + ". Billing Username or Password" +
                                      " is incorrect.";
                    log.error(eMessage);
                    throw new CloudBillingException(eMessage);

                } else if (response == 202) {
                    //Reason : https://wso2.org/jira/browse/DS-886
                    return "202";
                } else if (response != 200) {
                    String eMessage = "Failed with HTTP error code : " + response;
                    log.error(eMessage);
                    throw new CloudBillingException(eMessage);
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
}
