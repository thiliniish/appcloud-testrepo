/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.billing.processor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.DataServiceConfig;
import org.wso2.carbon.cloud.billing.commons.config.HttpClientConfig;
import org.wso2.carbon.cloud.billing.commons.config.SSOConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.processor.utils.ProcessorUtils;

import javax.xml.bind.DatatypeConverter;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * Represent the request api processor for data services
 */
public class DataServiceBillingRequestProcessor extends AbstractBillingRequestProcessor {

    private static final Log LOGGER = LogFactory.getLog(DataServiceBillingRequestProcessor.class);
    private static DataServiceConfig dataServiceConfig = BillingConfigUtils.getBillingConfiguration().getDSConfig();
    private static SSOConfig ssoConfig = BillingConfigUtils.getBillingConfiguration().getSSOConfig();

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
            LOGGER.error("Error while initializing upload request entity", e);
        }
    }

    public DataServiceBillingRequestProcessor(HttpClientConfig httpClientConfig) {
        super(httpClientConfig);
    }

    /**
     * Initialize the billing request basin auth header
     *
     * @return base 64 encoded auth header
     * @throws CloudBillingException
     */
    private static String initBasicAuthHeader() throws CloudBillingException {
        String userName = dataServiceConfig.getUser();
        String password = dataServiceConfig.getPassword();
        try {
            return "Basic " +
                    DatatypeConverter.printBase64Binary((userName + ":" + password).getBytes(BillingConstants.ENCODING));
        } catch (UnsupportedEncodingException e) {
            throw new CloudBillingException("Error occurred while initializing encoding basic auth header", e);
        }
    }

    /**
     * Get request
     *
     * @param url            URL
     * @param acceptType     Accept header
     * @param nameValuePairs query params
     * @return GET request response
     * @throws CloudBillingException
     */
    @Override
    public String doGet(String url, String acceptType, NameValuePair[] nameValuePairs) throws CloudBillingException {
        setTrustStoreParams();
        GetMethod get = new GetMethod(url);
        // default accept response body in XML
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ? BillingConstants.HTTP_TYPE_APPLICATION_XML : acceptType;
        get.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);
        get.addRequestHeader(BillingConstants.HTTP_REQ_HEADER_AUTHZ, basicAuthHeader);
        get.addRequestHeader(BillingConstants.HTTP_FOLLOW_REDIRECT, "true");
        if (ArrayUtils.isNotEmpty(nameValuePairs)) {
            get.setQueryString(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), get, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Upload not supported
     *
     * @throws CloudBillingException
     */
    @Override
    public void doUpload(String url, String acceptType, File file) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported for Data Service Billing request "
                + "processor");
    }

    /**
     * Data service POST request
     *
     * @param url         URL
     * @param acceptType  Accept header
     * @param jsonPayload payload
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doPost(String url, String acceptType, String jsonPayload) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by Data Service Billing Request " +
                "Processor");
    }

    /**
     * Data service POST request
     *
     * @param url            URL
     * @param acceptType     Accept header
     * @param nameValuePairs name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doPost(String url, String acceptType, NameValuePair[] nameValuePairs) throws CloudBillingException {
        setTrustStoreParams();
        PostMethod post = new PostMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ? BillingConstants.HTTP_TYPE_APPLICATION_JSON : acceptType;
        post.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);
        post.addRequestHeader(BillingConstants.HTTP_CONTENT_TYPE, BillingConstants.HTTP_TYPE_APPLICATION_URL_ENCODED);
        if (ArrayUtils.isNotEmpty(nameValuePairs)) {
            post.setRequestBody(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), post, DEFAULT_CONNECTION_RETRIES);
    }

    @Override
    public String doPut(String url, String acceptType, String jsonPayload) throws CloudBillingException {
        throw new UnsupportedOperationException("This method is not supported by Data Service Billing Request " +
                "Processor");
    }

    /**
     * Data service PUT request
     *
     * @param url            URL
     * @param acceptType     Accept header
     * @param nameValuePairs name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doPut(String url, String acceptType, NameValuePair[] nameValuePairs) throws
            CloudBillingException {
        setTrustStoreParams();
        PutMethod put = new PutMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ? BillingConstants.HTTP_TYPE_APPLICATION_JSON : acceptType;
        put.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);

        put.addRequestHeader(BillingConstants.HTTP_CONTENT_TYPE, BillingConstants.HTTP_TYPE_APPLICATION_URL_ENCODED);
        if (ArrayUtils.isNotEmpty(nameValuePairs)) {
            put.setQueryString(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), put, DEFAULT_CONNECTION_RETRIES);
    }

    /**
     * Data service PUT request
     *
     * @param url            URL
     * @param acceptType     Accept header
     * @param nameValuePairs name value pair
     * @return response
     * @throws CloudBillingException
     */
    @Override
    public String doDelete(String url, String acceptType, NameValuePair[] nameValuePairs) throws
            CloudBillingException {
        setTrustStoreParams();
        DeleteMethod delete = new DeleteMethod(url);
        // default accept response body in JSON
        String acceptTypeHeader = StringUtils.isBlank(acceptType) ? BillingConstants.HTTP_TYPE_APPLICATION_JSON : acceptType;
        delete.addRequestHeader(BillingConstants.HTTP_RESPONSE_TYPE_ACCEPT, acceptTypeHeader);
        delete.addRequestHeader(BillingConstants.HTTP_CONTENT_TYPE, BillingConstants.HTTP_TYPE_APPLICATION_URL_ENCODED);
        if (ArrayUtils.isNotEmpty(nameValuePairs)) {
            delete.setQueryString(nameValuePairs);
        }
        return ProcessorUtils.executeHTTPMethodWithRetry(this.getHttpClient(), delete, DEFAULT_CONNECTION_RETRIES);
    }

    private void setTrustStoreParams() {
        System.setProperty(BillingConstants.TRUST_STORE_NAME_PROPERTY, ssoConfig.getTrustStorePath());
        System.setProperty(BillingConstants.TRUST_STORE_PASSWORD_PROPERTY, ssoConfig.getTrustStorePassword());
    }

    /**
     * Utility method to check the status. This can only be used
     * when the DS's particular request is configured to send back the request status
     * <p/>
     *
     * @param jsonResponse json response String
     *                     {
     *                     "REQUEST_STATUS": "SUCCESSFUL"
     *                     }
     * @return boolean value
     */
    public static boolean isJsonResponseSuccess(String jsonResponse) {
        if (StringUtils.isBlank(jsonResponse)) {
            return false;
        }
        JsonElement element = new JsonParser().parse(jsonResponse.trim());
        if (!element.isJsonObject()) {
            return false;
        }
        JsonObject result = element.getAsJsonObject();
        return result.get(BillingConstants.DS_REQUEST_STATUS) != null && BillingConstants.DS_REQUEST_STATUS_SUCCESS
                .equals(result.get(BillingConstants.DS_REQUEST_STATUS).getAsString());
    }

    /**
     * Utility method to check the status. This can only be used
     * when the DS's particular request is configured to send back the request status
     * <p/>
     * ex status response
     * <axis2ns7:REQUEST_STATUS xmlns:axis2ns7="http://ws.wso2.org/dataservice">SUCCESSFUL</axis2ns7:REQUEST_STATUS>
     *
     * @param response response string
     * @return boolean
     * @throws XMLStreamException
     */
    public static boolean isXMLResponseSuccess(String response) throws XMLStreamException {
        OMElement resultOME = AXIOMUtil.stringToOM(response);
        return resultOME != null && BillingConstants.DS_REQUEST_STATUS.equals(resultOME.getLocalName())
                && StringUtils.isNotBlank(resultOME.getText())
                && BillingConstants.DS_REQUEST_STATUS_SUCCESS.equals(resultOME.getText().trim());
    }
}
