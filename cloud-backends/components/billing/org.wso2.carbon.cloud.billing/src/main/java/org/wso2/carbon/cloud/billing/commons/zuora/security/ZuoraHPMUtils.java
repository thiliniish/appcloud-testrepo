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

package org.wso2.carbon.cloud.billing.commons.zuora.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.cloud.billing.commons.config.HostedPageConfig;
import org.wso2.carbon.cloud.billing.commons.config.ZuoraConfig;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingSecurityException;
import org.wso2.carbon.cloud.billing.processor.utils.ProcessorUtils;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

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

    private static final String ERROR_VALIDATE_HASH = "Error while validating hash.";
    private static final String ERROR_GENERATE_HASH = "Error while generating hash.";
    private static final String ERROR_VALIDATE_SIGNATURE = "Error while validating the signature.";
    private static final String ERROR_PREPARE_PARAMS = "Error while preparing parameters.";
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
    private static final String API_ACCESS_KEY_ID = "apiAccessKeyId";
    private static final String API_SECRET_ACCESS_KEY = "apiSecretAccessKey";
    private static final String ENCODER = "UTF-8";
    private static final String BOUNCY_CASTLE_PROVIDER = "BC";
    private static final String RSA_ENCRYPT_DECRYPT_FUNCTION = "RSA/ECB/PKCS1Padding";
    private static final String TYPE_JSON = "application/json";
    private static String url;
    private static String endPoint;
    private static String username;
    private static String password;
    private static String publicKeyString;
    private static String pageId;
    private static String locale;
    private static String paymentGateway;
    private static Key publicKeyObject = null;

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
        } catch (JSONException e) {
            throw new CloudBillingSecurityException(ERROR_PREPARE_PARAMS + " JSON object creation failed: ", e);
        } catch (IOException e) {
            throw new CloudBillingSecurityException(ERROR_PREPARE_PARAMS + " IO exception while generating signature:" +
                                                    " ", e);
        } catch (Exception e) {
            throw new CloudBillingSecurityException(ERROR_PREPARE_PARAMS, e);
        }
    }

    /**
     * Validate signature using Hosted Page configuration
     *
     * @param signature      - signature need to validate
     * @param expirationTime - expired time in millisecond after the signature is created
     * @throws CloudBillingSecurityException
     */
    public static void validSignature(String signature, String expirationTime) throws CloudBillingSecurityException {
        // Decrypt signature.
        long expiredAfter = Long.parseLong(expirationTime);
        byte[] decoded = Base64.decodeBase64(signature.getBytes(Charset.forName(ENCODER)));
        try {
            Cipher encryptor = Cipher.getInstance(RSA_ENCRYPT_DECRYPT_FUNCTION);
            encryptor.init(Cipher.DECRYPT_MODE, publicKeyObject);
            String decryptedSignature = new String(encryptor.doFinal(decoded));

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
                    (tokenSignature)
                || StringUtils.isBlank(timestampSignature) || StringUtils.isBlank(pageIdSignature)) {
                throw new CloudBillingSecurityException("Signature is not complete.");
            }

            boolean isPageIdValid = false;

            if (pageId.equals(pageIdSignature)) {
                isPageIdValid = true;
            }

            if (!isPageIdValid) {
                throw new CloudBillingSecurityException("Page Id in signature is invalid.");
            }

            if ((new Date()).getTime() > (Long.parseLong(timestampSignature) + expiredAfter)) {
                throw new CloudBillingSecurityException("Signature is expired.");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_SIGNATURE + " Algorithm not found: " +
                                                    RSA_ENCRYPT_DECRYPT_FUNCTION, e);
        } catch (NoSuchPaddingException e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_SIGNATURE + " No such padding exception for " +
                                                    "cipher: " +
                                                    RSA_ENCRYPT_DECRYPT_FUNCTION, e);
        } catch (IllegalBlockSizeException e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_SIGNATURE + " Illegal block size exception for " +
                                                    "cipher: " +
                                                    RSA_ENCRYPT_DECRYPT_FUNCTION, e);
        } catch (BadPaddingException e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_SIGNATURE + " Bad padding exception for cipher: " +
                                                    RSA_ENCRYPT_DECRYPT_FUNCTION, e);
        } catch (InvalidKeyException e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_SIGNATURE + " Invalid key exception for cipher: " +
                                                    RSA_ENCRYPT_DECRYPT_FUNCTION, e);
        } catch (Exception e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_SIGNATURE, e);
        }
    }

    public static String generateHash(String data, String mdAlgorithm) throws CloudBillingSecurityException {
        try {
            Security.addProvider(new BouncyCastleProvider());
            MessageDigest mda = MessageDigest.getInstance(mdAlgorithm, BOUNCY_CASTLE_PROVIDER);
            byte[] encodedData = Base64.encodeBase64(mda.digest(data.getBytes()));

            if (encodedData != null) {
                return new String(encodedData, Charset.forName(ENCODER));
            } else {
                throw new CloudBillingSecurityException("Encoded data cannot be null");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CloudBillingSecurityException(ERROR_GENERATE_HASH + " Algorithm not found: " + mdAlgorithm, e);
        } catch (NoSuchProviderException e) {
            throw new CloudBillingSecurityException(ERROR_GENERATE_HASH + " No provider found: " +
                                                    BOUNCY_CASTLE_PROVIDER, e);
        } catch (Exception e) {
            throw new CloudBillingSecurityException(ERROR_GENERATE_HASH, e);
        }
    }

    public static boolean validateHash(String data, String hash, String mdAlgorithm)
            throws CloudBillingSecurityException {
        try {
            Security.addProvider(new BouncyCastleProvider());
            MessageDigest mda = MessageDigest.getInstance(mdAlgorithm, BOUNCY_CASTLE_PROVIDER);
            byte[] digestData = mda.digest(data.getBytes());

            return MessageDigest.isEqual(digestData, Base64.decodeBase64(hash.getBytes(Charset.forName(ENCODER))));
        } catch (NoSuchAlgorithmException e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_HASH + " Algorithm not found: " + mdAlgorithm, e);
        } catch (NoSuchProviderException e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_HASH + " No provider found: " +
                                                    BOUNCY_CASTLE_PROVIDER, e);
        } catch (Exception e) {
            throw new CloudBillingSecurityException(ERROR_VALIDATE_HASH, e);
        }
    }

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
    }

    private static JSONObject generateSignature(String pageId) throws CloudBillingException, JSONException,
                                                                      IOException {
        HttpClient httpClient = new HttpClient();
        PostMethod postRequest = new PostMethod(endPoint);
        postRequest.addRequestHeader(API_ACCESS_KEY_ID, username);
        postRequest.addRequestHeader(API_SECRET_ACCESS_KEY, password);
        postRequest.addRequestHeader("Accept", TYPE_JSON);

        RequestEntity requestEntity = new StringRequestEntity(buildJsonRequest(pageId), TYPE_JSON, ENCODER);
        postRequest.setRequestEntity(requestEntity);

        String response = ProcessorUtils.executeHTTPMethodWithRetry(httpClient, postRequest, 10);
        JSONObject result = new JSONObject(response);
        if (!result.getBoolean("success")) {
            throw new CloudBillingSecurityException("Fail to generate signature. The reason is " + result.getString
                    ("reasons"));
        }
        return result;
    }

    private static String buildJsonRequest(String pageId) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("uri", url);
        json.put("method", "POST");
        json.put("pageId", pageId);
        return json.toString();
    }
}