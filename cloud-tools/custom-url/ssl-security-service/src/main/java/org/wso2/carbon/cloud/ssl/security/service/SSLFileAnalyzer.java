/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.ssl.security.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.ssl.security.service.exceptions.SSLSecurityServiceException;
import org.wso2.carbon.cloud.ssl.security.service.module.RSAPrivateKeyManager;
import org.wso2.carbon.cloud.ssl.security.service.module.X509CertificateManager;
import org.wso2.carbon.core.AbstractAdmin;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Admin Service to analyze SSLFiles to validate them against the chain files
 */
public class SSLFileAnalyzer extends AbstractAdmin {

    private static Log log = LogFactory.getLog(SSLFileAnalyzer.class);
    private X509CertificateManager x509CertificateManager;
    private RSAPrivateKeyManager rsaPrivateKeyManager;

    /**
     * Initializing certificate and chain file content
     */
    public void init(String sslFileContent, String chainFile, String keyContent)
            throws CertificateException, IOException, InvalidAlgorithmParameterException, SSLSecurityServiceException {
        try {
            this.x509CertificateManager = new X509CertificateManager(sslFileContent, chainFile);
            this.rsaPrivateKeyManager = new RSAPrivateKeyManager(keyContent);
        } catch (CertificateException | InvalidAlgorithmParameterException | SSLSecurityServiceException ex) {
            String errorMessage = "Error occurred when initializing ssl certificate.";
            log.error(errorMessage, ex);
            throw ex;
        } catch (IOException ex) {
            String errorMessage = "Error occurred when reading the submitted content.";
            log.error(errorMessage, ex);
            throw ex;
        }
    }

    /**
     * Checks whether the submitted ssl certificate validity time period expired
     *
     * @return String json payload of the status
     */
    public String isSSLFileDateVerified() {
        try {
            this.x509CertificateManager.checkDateValidity();
            return "{'error':'false'}";
        } catch (CertificateExpiredException ex) {
            String message = "Error thrown when validating certificate.";
            log.error(message, ex);
            return "{'error':'true', 'message':'Provided certificate has expired.'}";
        } catch (CertificateNotYetValidException ex) {
            String message = "Error thrown when checking date validity.";
            log.error(message, ex);
            return "{'error':'true', 'message':'Provided certificate is not yet valid.'}";
        }

    }

    /**
     * Checks whether the submitted ssl certificate verified against provided chain file(public key)
     *
     * @return String json payload of the status
     */
    public String isPublicKeyValid() {
        try {
            this.x509CertificateManager.isCertificateVerified();
            return "{'error':'false'}";
        } catch (CertificateException ex) {
            String message = "Issues have been detected on certificate when verifying it against provided public key ";
            log.error(message, ex);
            return "{'error':'true', 'message':'Provided certificate have expired.'}";
        } catch (NoSuchAlgorithmException ex) {
            String message = "Provided cryptographic algorithm is not supported by the environment. " +
                             "Make sure key encryption algorithm to be RSA.";
            log.error(message, ex);
            return "{'error':'true', 'message':'" + message + "'}";
        } catch (InvalidKeyException ex) {
            String message = "Provided key is invalid due to fact as invalid encoding, wrong length or uninitialized";
            log.error(message, ex);
            return "{'error':'true', 'message':'Provided certificate have expired.'}";
        } catch (NoSuchProviderException ex) {
            String message = "Required security provider is not available in the environment";
            log.error(message, ex);
            return "{'error':'true', 'message':'" + message + "'}";
        } catch (SignatureException ex) {
            String message = "Signature of provided key is not valid";
            log.error(message, ex);
            return "{'error':'true', 'message':'" + message + "'}";
        }

    }

    /**
     * Checks whether the submitted ssl certificate is self signed.
     *
     * @return String json payload of the status of the validation
     */
    public String isCertificateSelfSigned() {
        try {
            this.x509CertificateManager.isSelfSigned();
            return "{'error':'true', 'message':'Provided certificate is sel-signed.'}";
        } catch (Exception ex) {
            String errorMessage = "Provided certificate is not self-signed";
            log.debug(errorMessage, ex);
            return "{'error':'false' , 'message':'" + errorMessage + "'}";
        }

    }

    /**
     * Checks whether the submitted ssl certificate is issued for the provided url
     *
     * @return String json payload of the status
     */
    public String validateIssuedUrl(String url) {
        try {
            if (this.x509CertificateManager.checkingIssuedDns(url)) {
                return "{'error':'false'}";
            } else {
                return "{'error':'true', 'message':'The certificate have not issued for : " + url + "'}";
            }
        } catch (CertificateParsingException ex) {
            String errorMessage = "Error occurred while validating issued url against certificate issued dns list.";
            log.debug(errorMessage, ex);
            return "{'error':'true', 'message':''" + errorMessage + "}";
        }
    }

    /**
     * Verify whether private key matches the public key.
     *
     * @return String json payload of the status
     */
    public String matchRSAKeys() {
        RSAPublicKey publicKey = x509CertificateManager.getPublicKey();
        RSAPrivateKey privateKey = rsaPrivateKeyManager.getPrivateKey();
        if (publicKey.getModulus().compareTo(privateKey.getModulus()) == 0) {
            return "{'error' : false }";
        }
        String errorMsg = "Given private key does not match with the public key.";
        log.error(errorMsg);
        return "{'error': true , 'message':'" + errorMsg + "'}";
    }

    /**
     * Validate the certificate against the certificate chain.
     *
     * @return String json payload of the status
     */
    public String validateCertChain() {
        try {
            x509CertificateManager.validateCertChain();
            return "{'error' : false }";
        } catch (SSLSecurityServiceException e) {
            log.error(e.getMessage(), e);
            return "{'error': true , 'message':'Error occurred while validating the certificate chain.' }";
        }
    }
}
