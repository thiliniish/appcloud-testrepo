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

package org.wso2.carbon.cloud.billing.commons.zuora.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.config.HostedPageConfig;
import org.wso2.carbon.cloud.billing.commons.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.commons.utils.BillingConfigUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingSecurityException;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessor;
import org.wso2.carbon.cloud.billing.processor.BillingRequestProcessorFactory;
import org.wso2.carbon.cloud.billing.processor.ZuoraBillingRequestProcessor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
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

    private static final String TENANT_ID = "tenantId";
    private static final String ID = "id";
    private static final String TOKEN = "token";
    private static final String SIGNATURE = "signature";
    private static final String KEY = "key";
    private static final String URL = "url";
    private static final String PAYMENT_GATEWAY = "paymentGateway";
    private static final String STYLE = "style";
    private static final String SUBMIT_ENABLED = "submitEnabled";
    private static final String LOCALE = "locale";
    private static final String RETAIN_VALUES = "retainValues";
    private static final String BOUNCY_CASTLE_PROVIDER = "BC";
    private static final String RSA_ENCRYPT_DECRYPT_FUNCTION = "RSA/ECB/PKCS1Padding";
    private static String url;
    private static String endPoint;
    private static String publicKeyString;
    private static String pageId;
    private static String locale;
    private static String paymentGateway;
    private static Key publicKeyObject = null;

    private static BillingRequestProcessor zuoraApi = BillingRequestProcessorFactory.getInstance()
            .getBillingRequestProcessor(BillingRequestProcessorFactory.ProcessorType.ZUORA_RSA);
    private ZuoraHPMUtils() {
    }

    /**
     * Fill params.
     *
     * @throws CloudBillingException
     */
    public static String prepareParams() throws CloudBillingException {

        try {
            loadConfig();
            generatePublicKeyObject();

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
        } catch (JSONException | IOException e) {
            throw new CloudBillingException("Error while preparing parameters.", e);
        }
    }

    /**
     * Validate signature using Hosted Page configuration
     *
     * @param signature      - signature need to validate
     * @param expirationTime - expired time in millisecond after the signature is created
     * @throws CloudBillingSecurityException
     */
    public static void validateSignature(String signature, String expirationTime) throws CloudBillingSecurityException {
        // Decrypt signature.
        long expiredAfter = Long.parseLong(expirationTime);
        byte[] decoded = Base64.decodeBase64(signature.getBytes(Charset.forName(BillingConstants.ENCODING)));
        try {
            Cipher encryptor = Cipher.getInstance(RSA_ENCRYPT_DECRYPT_FUNCTION);
            encryptor.init(Cipher.DECRYPT_MODE, publicKeyObject);
            String decryptedSignature = new String(encryptor.doFinal(decoded), Charset.forName(BillingConstants.ENCODING));

            // Validate signature.
            if (StringUtils.isBlank(decryptedSignature)) {
                throw new CloudBillingSecurityException("Signature is empty.");
            }

            StringTokenizer st = new StringTokenizer(decryptedSignature, "#");
            String urlSignature = st.nextToken();
            String tenantIdSignature = st.nextToken();
            String tokenSignature = st.nextToken();
            String timestampSignature = st.nextToken();
            String pageIdSignature = st.nextToken();

            if (StringUtils.isBlank(urlSignature) || StringUtils.isBlank(tenantIdSignature) || StringUtils.isBlank
                    (tokenSignature) || StringUtils.isBlank(timestampSignature) || StringUtils.isBlank
                    (pageIdSignature)) {
                throw new CloudBillingSecurityException("Signature is not complete.");
            }

            if (!pageId.equals(pageIdSignature)) {
                throw new CloudBillingSecurityException("Page Id in signature is invalid.");
            }

            if ((new Date()).getTime() > (Long.parseLong(timestampSignature) + expiredAfter)) {
                throw new CloudBillingSecurityException("Signature is expired.");
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException | InvalidKeyException e) {
            throw new CloudBillingSecurityException("Error while validating the signature.", e);
        }
    }

    /**
     * Generate MDA hash value for the data with the given MDA hash algorithm
     *
     * @param data        data which need a hash
     * @param mdAlgorithm MDA algorithm
     * @return hash string
     * @throws CloudBillingSecurityException
     */
    public static String generateHash(String data, String mdAlgorithm) throws CloudBillingSecurityException {
        try {
            Security.addProvider(new BouncyCastleProvider());
            MessageDigest mda = MessageDigest.getInstance(mdAlgorithm, BOUNCY_CASTLE_PROVIDER);
            byte[] encodedData = Base64.encodeBase64(mda.digest(data.getBytes(Charset.forName(BillingConstants.ENCODING))));

            if (encodedData != null) {
                return new String(encodedData, Charset.forName(BillingConstants.ENCODING));
            } else {
                throw new CloudBillingSecurityException("Encoded data cannot be null");
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CloudBillingSecurityException("Error while generating hash.", e);
        }
    }

    /**
     * Validate the hash with the data for the given algorithm
     *
     * @param data        data
     * @param hash        hash string
     * @param mdAlgorithm MDA algorithm which used to generate the hash
     * @return validation boolean
     * @throws CloudBillingSecurityException
     */
    public static boolean validateHash(String data, String hash, String mdAlgorithm)
            throws CloudBillingSecurityException {
        try {
            Security.addProvider(new BouncyCastleProvider());
            MessageDigest mda = MessageDigest.getInstance(mdAlgorithm, BOUNCY_CASTLE_PROVIDER);
            byte[] digestData = mda.digest(data.getBytes(Charset.forName(BillingConstants.ENCODING)));

            return MessageDigest.isEqual(digestData, Base64.decodeBase64(hash.getBytes(Charset.forName(BillingConstants.ENCODING))));
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CloudBillingSecurityException("Error while validating hash.", e);
        }
    }

    /**
     * Generate public key string.
     *
     * @throws IOException
     */
    private static void generatePublicKeyObject() throws IOException {
        PEMReader pemReader = new PEMReader(new StringReader("-----BEGIN PUBLIC KEY-----\n"
                                                             + publicKeyString + "\n-----END PUBLIC KEY-----"));
        publicKeyObject = (Key) pemReader.readObject();
        pemReader.close();
    }

    /**
     * Load configuration which need to populate which need to generate client token
     */
    private static void loadConfig() {

        ZuoraConfig zuoraConfig = BillingConfigUtils.getBillingConfiguration().getZuoraConfig();
        HostedPageConfig hostedPageConfig = zuoraConfig.getHostedPageConfig();
        endPoint = hostedPageConfig.getEndPointUri();
        publicKeyString = hostedPageConfig.getPublicKey();
        paymentGateway = hostedPageConfig.getPaymentGateway();
        pageId = hostedPageConfig.getPageId();
        locale = hostedPageConfig.getLocale();

        if (zuoraApi instanceof ZuoraBillingRequestProcessor) {
            HttpClient httpClient = ((ZuoraBillingRequestProcessor) zuoraApi).getHttpClient();
            url = httpClient.getHostConfiguration().getHostURL() + hostedPageConfig.getUri();
        } else {
            throw new IllegalArgumentException("Unsupported instance of BillingRequestProcessor");
        }
    }

    /**
     * Query the signature for the pageId
     *
     * @param pageId pageID
     * @return Json object of the signature
     * @throws CloudBillingException
     */
    private static JSONObject generateSignature(String pageId) throws CloudBillingException {
        try {
            String response = zuoraApi.doPost(endPoint, BillingConstants.HTTP_TYPE_APPLICATION_JSON, buildJsonRequest(pageId));
            JSONObject result = new JSONObject(response);
            if (!result.getBoolean(BillingConstants.ZUORA_RESPONSE_SUCCESS)) {
                throw new CloudBillingException("Fail to generate signature. The reason is " + result.getString("reasons"));
            }
            return result;
        } catch (JSONException e) {
            throw new CloudBillingException("Error while generating signature");
        }
    }

    /**
     * Build json request
     *
     * @param pageId pageId
     * @return json body
     * @throws CloudBillingException
     */
    private static String buildJsonRequest(String pageId) throws CloudBillingException {
        JSONObject json = new JSONObject();
        try {
            json.put("uri", url);
            json.put("method", "POST");
            json.put("pageId", pageId);
            return json.toString();
        } catch (JSONException e) {
            throw new CloudBillingException("Exception while building json request");
        }
    }
}