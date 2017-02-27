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

package org.wso2.carbon.cloud.ssl.security.service.module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.openssl.PEMReader;
import org.wso2.carbon.cloud.ssl.security.service.FileEncryptionServiceConstants;
import org.wso2.carbon.cloud.ssl.security.service.exceptions.SSLSecurityServiceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Class to manage X509 Certificates
 */
public class X509CertificateManager {
    private static final Log log = LogFactory.getLog(X509CertificateManager.class);
    private X509Certificate x509Certificate;
    private PublicKey publicKey;
    private List<X509Certificate> intermediateCerts = new ArrayList<X509Certificate>();
    private X509Certificate rootCertificate;

    public X509CertificateManager(String fileContent, String publicKeyFileContent)
            throws CertificateException, IOException, InvalidAlgorithmParameterException, SSLSecurityServiceException {
        try {
            initX509Certificate(fileContent);
            initCertChain(publicKeyFileContent);
            generatePublicKey(publicKeyFileContent);
        } catch (IOException ex) {
            String errorMessage = "Error thrown when initializing X509 Certificate.";
            log.error(errorMessage, ex);
            throw new IOException(errorMessage, ex);
        } catch (CertificateException | InvalidAlgorithmParameterException ex) {
            String errorMessage = "Error occurred when generating public key from the file content";
            log.error(errorMessage, ex);
            throw ex;
        }
    }

    /**
     * Initialize X509 Certificate.
     *
     * @param fileContent SSL file content.
     * @throws IOException
     * @throws RuntimeException
     */
    private void initX509Certificate(String fileContent) throws IOException, RuntimeException {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        PEMReader pemReader = new PEMReader(new StringReader(fileContent));
        try {
            Object readerObject = pemReader.readObject();
            if (readerObject instanceof X509Certificate) {
                this.x509Certificate = (X509Certificate) readerObject;
            } else {
                String errorMessage = "Provided certificate does not support in current environment.";
                throw new RuntimeException(errorMessage);
            }
        } catch (IOException ex) {
            String errorMessage = "IOException is thrown when reading the file content by the pemReader";
            log.error(errorMessage, ex);
            throw new IOException(errorMessage, ex);
        }
    }

    private void generatePublicKey(String keyContent) throws CertificateException, InvalidAlgorithmParameterException {

        try {
            CertificateFactory certificateFactory =
                    CertificateFactory.getInstance(FileEncryptionServiceConstants.CERTIFICATE_ALGORITHM);
            X509Certificate keyFile = (X509Certificate) certificateFactory
                    .generateCertificate(new ByteArrayInputStream(keyContent.getBytes(StandardCharsets.UTF_8)));
            this.publicKey = keyFile.getPublicKey();
            if (!publicKey.getAlgorithm().equals(FileEncryptionServiceConstants.KEY_ENCRYPTION_ALGORITHM)) {
                throw new InvalidAlgorithmParameterException("Error thrown because key algorithm of public key " +
                                                             "file is not supported, expected encryption " +
                                                             "algorithm is RSA, provided key algorithm:" +
                                                             publicKey.getAlgorithm());
            }
        } catch (CertificateException ex) {
            String errorMessage = "Error occurred when getting instance of X.509";
            log.error(errorMessage, ex);
            throw new CertificateException(errorMessage);
        }

    }

    /**
     * Check whether the certificate is checked within the dates specified in security
     *
     * @return boolean stats whether the certificate valid or invalid
     */
    public boolean checkDateValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        try {
            this.x509Certificate.checkValidity();
            return true;
        } catch (CertificateExpiredException ex) {
            String errorMessage = "Error occurred when checking the validity of the certificate. " +
                                  "Provided certificate is expired on " + x509Certificate.getNotAfter();
            log.error(errorMessage, ex);
            throw new CertificateExpiredException(errorMessage);
        } catch (CertificateNotYetValidException ex) {
            String errorMessage = "Error occurred when checking the validity of the certificate. Provided " +
                                  "certificate is not valid until : " + x509Certificate.getNotBefore();
            log.error(errorMessage, ex);
            throw new CertificateNotYetValidException(errorMessage);
        }
    }

    /**
     * Verifying whether the certificate is checked with chain file
     *
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchProviderException
     * @throws SignatureException
     */
    public void isCertificateVerified()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
                   SignatureException {
        try {
            this.x509Certificate.verify(this.publicKey);
        } catch (CertificateException ex) {
            String errorMessage = "Error occurred when verifying the certificate.";
            log.error(errorMessage, ex);
            throw new CertificateException(errorMessage);
        } catch (NoSuchAlgorithmException ex) {
            String errorMessage = "Error occurred when verifying the certificate. Provided key algorithm " +
                                  publicKey.getAlgorithm() + "is not supported";
            log.error(errorMessage, ex);
            throw new NoSuchAlgorithmException(errorMessage);
        } catch (InvalidKeyException ex) {
            String errorMessage = "Error occurred when verifying the certificate. Provided key is not " +
                                  "matching with the certificate.";
            log.error(errorMessage, ex);
            throw new InvalidKeyException(errorMessage);
        } catch (NoSuchProviderException | SignatureException ex) {
            String errorMessage = "Error occurred when verifying the certificate.";
            log.error(errorMessage, ex);
            throw ex;
        }
    }

    /**
     * Check whether the SSL Certificate is a self-signed certificate
     */
    public void isSelfSigned()
            throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
                   SignatureException {

        PublicKey certificatePublicKey = this.x509Certificate.getPublicKey();
        try {
            x509Certificate.verify(certificatePublicKey);
        } catch (CertificateException | NoSuchProviderException | SignatureException e) {
            String errorMessage = "Error occurred when checking whether the certificate is self-signed.";
            log.debug(errorMessage, e);
            throw e;
        } catch (NoSuchAlgorithmException e) {
            String errorMessage = "Error occurred when checking whether the certificate is self-signed. " +
                                  "Algorithm of key " + certificatePublicKey.getAlgorithm() + " is not supported.";
            log.debug(errorMessage, e);
            throw new NoSuchAlgorithmException(errorMessage);
        } catch (InvalidKeyException e) {
            String errorMessage = "Error occurred when checking whether the certificate is self-signed. " +
                                  "Certificate public key is not valid.";
            log.debug(errorMessage, e);
            throw new InvalidKeyException(errorMessage);
        }
    }

    public boolean checkingIssuedDns(String url) throws CertificateParsingException {
        try {
            Collection<List<?>> subjectAlternativeNamesCollection = this.x509Certificate.getSubjectAlternativeNames();
            List<String> issuedDnsList = new ArrayList<String>();

            Object[] subjectAlternativeNamesArray = subjectAlternativeNamesCollection.toArray();

            for (Object listObject : subjectAlternativeNamesArray) {
                if (listObject instanceof List) {
                    try {
                        List<String> list = (List<String>) listObject;
                        issuedDnsList.add(list.get(1));
                    } catch (ClassCastException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Class cast exception occurred. Trying to get rest of the SANS.");
                        }
                    }
                }
            }

            //Checking in regular issued dns
            boolean issuedStatus = issuedDnsList.contains(url);

            if (!issuedStatus) {
                //Checking issued dns in wildcards
                for (String issuedDns : issuedDnsList) {
                    if (issuedDns.startsWith("*")) {
                        String[] splittedGivenDns = Arrays.copyOfRange(url.split("\\."), 1, url.split("\\.").length);
                        String[] splittedIssuedDns =
                                Arrays.copyOfRange(issuedDns.split("\\."), 1, issuedDns.split("\\.").length);
                        issuedStatus = Arrays.equals(splittedGivenDns, splittedIssuedDns);
                        if (issuedStatus) {
                            return true;
                        }
                    }
                }

            }
            return issuedStatus;

        } catch (CertificateParsingException ex) {
            String errorMessage = "Error occurred while obtaining issued dns list.";
            log.error(errorMessage, ex);
            throw new CertificateParsingException();
        }
    }

    /**
     * Returns the RSA Public key.
     *
     * @return PublicKey
     */
    public RSAPublicKey getPublicKey() {
        return (RSAPublicKey) x509Certificate.getPublicKey();
    }

    /**
     * Certificate chain should have only the intermediate certificates and the CA root certificate. It should not
     * contain end/target certificate.This method extracts the intermediate certificates and CA root certificate from
     * uploaded file content. if end/target certificate also present in the uploaded content , then an exception will be
     * thrown
     *
     * @param certChain Intermediate certificates + CA root certificate
     * @throws SSLSecurityServiceException
     */
    private void initCertChain(String certChain) throws SSLSecurityServiceException {
        InputStream inStream = new ByteArrayInputStream(certChain.getBytes(StandardCharsets.UTF_8));
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509", "BC");
            Collection certs = cf.generateCertificates(inStream);
            if (certs == null || certs.size() == 0) {
                throw new SSLSecurityServiceException("Invalid certificate chain file.");
            }

            for (Object obj : certs) {
                X509Certificate cert = (X509Certificate) obj;
                if (cert.getSubjectX500Principal().equals(x509Certificate.getSubjectX500Principal())) {
                    throw new SSLSecurityServiceException("Invalid certificate chain file.Certificate chain file" +
                                                                  " contains the domain/end certificate.");
                }
                //subject and issue value are equals for CA root certificate
                if (cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
                    rootCertificate = cert;
                } else {
                    intermediateCerts.add(cert);
                }
            }

            if (rootCertificate == null) {
                String errorMsg = "Root certificate is not provided in the chain file";
                throw new SSLSecurityServiceException(errorMsg);
            }
        } catch (CertificateException | NoSuchProviderException e) {
            String errorMsg = "Error occurred while validating the certificate chain";
            throw new SSLSecurityServiceException(errorMsg, e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }
        }
    }

    /**
     * Validates the public certificate against given certificate chain. if validation fails exception will be thrown.
     *
     * @return PKIXCertPathValidatorResult Validation Result
     * @throws SSLSecurityServiceException
     */
    public PKIXCertPathValidatorResult validateCertChain()
            throws SSLSecurityServiceException {
        PKIXCertPathValidatorResult result = null;
        //certificate chain should have target/end cert + intermediate certs
        List<X509Certificate> certChain = new ArrayList<X509Certificate>();
        certChain.add(x509Certificate);
        certChain.addAll(intermediateCerts);

        try {
            // create certificate path
            CertificateFactory fact = CertificateFactory.getInstance("X.509", "BC");
            CertPath certPath = fact.generateCertPath(certChain);
            //create trust set to verify the certificate chain. Trust set should have the CA root of the certificate
            // chain
            Set trust = Collections.singleton(new TrustAnchor(rootCertificate, null));
            // perform validation
            CertPathValidator validator = CertPathValidator.getInstance("PKIX", "BC");
            PKIXParameters param = new PKIXParameters(trust);
            //Since we don't maintain CRL(Certificate Revocation List) list, turn off CRL validation
            param.setRevocationEnabled(false);
            result = (PKIXCertPathValidatorResult) validator.validate(certPath, param);
        } catch (CertificateException | InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                CertPathValidatorException | NoSuchProviderException e) {
            String errorMsg = "Error occurred while validate the certificate chain";
            throw new SSLSecurityServiceException(errorMsg, e);
        }
        return result;
    }
}
