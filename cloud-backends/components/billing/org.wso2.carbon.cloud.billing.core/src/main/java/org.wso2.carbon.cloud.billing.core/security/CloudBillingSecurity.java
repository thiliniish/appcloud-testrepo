/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.billing.core.security;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.wso2.carbon.cloud.billing.core.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.core.exceptions.CloudBillingSecurityException;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 *
 */
public class CloudBillingSecurity {

    private static final String BOUNCY_CASTLE_PROVIDER = "BC";

    private CloudBillingSecurity() {

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
            byte[] encodedData =
                    Base64.encodeBase64(mda.digest(data.getBytes(Charset.forName(BillingConstants.ENCODING))));

            if (encodedData != null) {
                return new String(encodedData, Charset.forName(BillingConstants.ENCODING));
            } else {
                throw new CloudBillingSecurityException("Encoded data cannot be null");
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CloudBillingSecurityException("Error while generating hash.", e);
        }
    }
}
