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

package org.wso2.carbon.cloud.billing.common.zuora.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.cloud.billing.common.config.HostedPageConfig;
import org.wso2.carbon.cloud.billing.common.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.common.zuora.security.utils.BypassSSLSocketFactory;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * HPM utility to generate and validate signature
 */
public class ZuoraHPMUtils {

    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String TENANT_ID = "tenantId";
    private static final String ID = "id";
    private static final String PAGE_ID = "pageId";
    private static final String TOKEN = "token";
    private static final String SIGNATURE = "signature";
    private static final String KEY = "key";
    private static final String URL = "url";
    private static final String PAYMENT_GATEWAY = "paymentGateway";
    private static final String STYLE = "style";
    private static final String SUBMIT_ENABLED = "submitEnabled";
    private static final String LOCALE = "locale";
    private static final String RETAIN_VALUES = "retainValues";
    private static final String ENDPOINT = "endPoint";
    private static final String PUBLIC_KEY = "publicKey";
    private static final String API_ACCESS_KEY_ID = "apiAccessKeyId";
    private static final String API_SECRET_ACCESS_KEY = "apiSecretAccessKey";
    private static final String ENCODER = "UTF-8";
    private static final String BOUNCY_CASTLE_PROVIDER = "BC";
    private static final String RSA_ENCRYPT_DECRYPT_FUNCTION = "RSA/ECB/PKCS1Padding";
    private static final String TYPE_JSON = "application/json";

    private static final Log log = LogFactory.getLog(ZuoraHPMUtils.class);

    private static String url = "";
    private static String endPoint = "";
    private static String username = "";
    private static String password = "";
    private static String publicKeyString = "";
    private static String pageId = "";
    private static String locale = "";
    private static String paymentGateway = "";
    private static Key publicKeyObject = null;

    private static boolean sslCertificateValid = CloudBillingUtils.getBillingConfiguration().getZuoraConfig()
            .isSslCertificateValid();

    private static void generatePublicKeyObject() throws IOException {
        PEMReader pemReader = new PEMReader(new StringReader("-----BEGIN PUBLIC KEY-----\n"
                                                             + publicKeyString + "\n-----END PUBLIC KEY-----"));
        publicKeyObject = (Key) pemReader.readObject();
        pemReader.close();
    }

    private static void loadConfig() throws IOException, JSONException {

        ZuoraConfig zuoraConfig = CloudBillingUtils.getBillingConfiguration().getZuoraConfig();
        username = zuoraConfig.getUser();
        password = zuoraConfig.getPassword();
        HostedPageConfig hostedPageConfig = zuoraConfig.getHostedPageConfig();
        endPoint = hostedPageConfig.getEndPoint();
        publicKeyString = hostedPageConfig.getPublicKey();
        paymentGateway = hostedPageConfig.getPaymentGateway();
        pageId = hostedPageConfig.getPageId();
        locale = hostedPageConfig.getLocale();
        url = hostedPageConfig.getUrl();

        generatePublicKeyObject();
    }

    /**
     * Fill params.
     *
     * @throws Exception
     */
    public static String prepareParams() throws Exception {

        loadConfig();

        JSONObject result = generateSignature(pageId);
        JSONObject params = new JSONObject();

        params.put(TENANT_ID, result.getString(TENANT_ID));
        params.put(ID, pageId);
        params.put(TOKEN, result.getString(TOKEN));
        params.put(SIGNATURE, result.getString(SIGNATURE));
        params.put(KEY, publicKeyString);
        params.put(URL, url);
        params.put(PAYMENT_GATEWAY, paymentGateway);
        params.put(STYLE, "inline");
        params.put(SUBMIT_ENABLED, "false");
        params.put(LOCALE, locale);
        params.put(RETAIN_VALUES, "true");

        return params.toString();
    }

    //TODO take out
    private static JSONObject generateSignature(String pageId) throws Exception {
        HttpClient httpClient = new HttpClient();
        PostMethod postRequest = new PostMethod(endPoint);
        postRequest.addRequestHeader(API_ACCESS_KEY_ID, username);
        postRequest.addRequestHeader(API_SECRET_ACCESS_KEY, password);
        postRequest.addRequestHeader("Accept", TYPE_JSON);

        RequestEntity requestEntity = new StringRequestEntity(buildJsonRequest(pageId), TYPE_JSON, ENCODER);
        postRequest.setRequestEntity(requestEntity);

        // Re-try 10 times in case the server is too busy to give you response in time.
        int loop = 0;
        while (loop++ < 10) {
            int response = httpClient.executeMethod(postRequest);

            if (response == 404) {
                throw new Exception("Failed with HTTP error code : " + response + ". ZUORA Signature API End Point is" +
                                    " incorrect.");
            } else if (response == 401) {
                throw new Exception("Failed with HTTP error code : " + response + ". ZUORA Login's Username or " +
                                    "Password is incorrect.");
            } else if (response != 200) {
                throw new Exception("Failed with HTTP error code : " + response + ". ZUORA Login's Username or " +
                                    "Password is incorrect.");
            }

            if (postRequest.getResponseBody().length > 0) {
                break;
            }
        }

        // Parse the response returned from ZUORA Signature API End Point
        byte[] res = postRequest.getResponseBody();
        String s = new String(res);
        JSONObject result = new JSONObject(s);
        if (!result.getBoolean("success")) {
            throw new Exception("Fail to generate signature. The reason is " + result.getString("reasons"));
        }
        return result;
    }

    private static String buildJsonRequest(String pageId) throws NullPointerException, JSONException {
        JSONObject json = new JSONObject();

        //Remove to use the default certificate
        if (!sslCertificateValid && url.toLowerCase().contains("https")) {
            log.warn("Bypassing self-signed certificate. " +
                     "if you have a valid certificate under the jdk. Please set the 'SSLCertificateValid' to true in" +
                     " billing.xml");
            Protocol.registerProtocol("https", new Protocol("https", new BypassSSLSocketFactory(), DEFAULT_HTTPS_PORT));
        }
        json.put("uri", url);
        json.put("method", "POST");
        json.put("pageId", pageId);
        return json.toString();
    }

    /**
     * Validate signature using Hosted Page configuration
     *
     * @param signature      - signature need to validate
     * @param expirationTime - expired time in millisecond after the signature is created
     * @throws Exception
     */
    public static void validSignature(String signature, String expirationTime) throws Exception {
        // Decrypt signature.
        //TODO remove generic exceptions
        long expiredAfter = Long.parseLong(expirationTime);
        byte[] decoded = Base64.decodeBase64(signature.getBytes(Charset.forName(ENCODER)));
        Cipher encryptor = Cipher.getInstance(RSA_ENCRYPT_DECRYPT_FUNCTION);
        encryptor.init(Cipher.DECRYPT_MODE, publicKeyObject);
        String decryptedSignature = new String(encryptor.doFinal(decoded));

        // Validate signature.
        if (StringUtils.isBlank(decryptedSignature)) {
            throw new Exception("Signature is empty.");
        }

        StringTokenizer st = new StringTokenizer(decryptedSignature, "#");
        String url_signature = st.nextToken();
        String tenantId_signature = st.nextToken();
        String token_signature = st.nextToken();
        String timestamp_signature = st.nextToken();
        String pageId_signature = st.nextToken();

        if (StringUtils.isBlank(url_signature) || StringUtils.isBlank(tenantId_signature) || StringUtils.isBlank
                (token_signature)
            || StringUtils.isBlank(timestamp_signature) || StringUtils.isBlank(pageId_signature)) {
            throw new Exception("Signature is not complete.");
        }

        boolean isPageIdValid = false;

        if (pageId.equals(pageId_signature)) {
            isPageIdValid = true;
        }

        if (!isPageIdValid) {
            throw new Exception("Page Id in signature is invalid.");
        }

        if ((new Date()).getTime() > (Long.parseLong(timestamp_signature) + expiredAfter)) {
            throw new Exception("Signature is expired.");
        }
    }

    public static String generateHash(String data, String mdAlgorithm)
            throws Exception { //TODO remove generic exceptions
        Security.addProvider(new BouncyCastleProvider());

        MessageDigest mda = MessageDigest.getInstance(mdAlgorithm, BOUNCY_CASTLE_PROVIDER);
        byte[] encodedData = Base64.encodeBase64(mda.digest(data.getBytes()));

        if (encodedData != null) {
            return new String(encodedData, Charset.forName(ENCODER));
        } else {
            throw new Exception("Encoded data cannot be null");
        }

    }

    public static boolean validateHash(String data, String hash, String mdAlgorithm)
            throws NoSuchProviderException, NoSuchAlgorithmException {

        Security.addProvider(new BouncyCastleProvider());
        MessageDigest mda = MessageDigest.getInstance(mdAlgorithm, BOUNCY_CASTLE_PROVIDER);
        byte[] digestData = mda.digest(data.getBytes());

        return (MessageDigest.isEqual(digestData, Base64.decodeBase64(hash.getBytes(Charset.forName(ENCODER)))));
    }

}