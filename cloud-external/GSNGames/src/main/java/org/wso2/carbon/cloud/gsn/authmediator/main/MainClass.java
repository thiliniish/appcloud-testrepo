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
package org.wso2.carbon.cloud.gsn.authmediator.main;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainClass {
	public static void main(String[] args)
			throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		StringBuilder secretKey = new StringBuilder("0123456789abcdef");
		String timeStamp = "1450263114";
		String sessionId = "newSessionId";
		String body = "/weather?timestamp=1450263114";

		secretKey.append(timeStamp);
		secretKey.append(sessionId);

		String path = URLDecoder.decode(body,"UTF-8");
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.update(path.getBytes());
		String md5Body = String.format("%032X", new BigInteger(1, messageDigest.digest()));

		String algorithm = "HMACSHA256";
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.toString().getBytes(), algorithm);
		Mac mac = Mac.getInstance(algorithm);
		mac.init(secretKeySpec);
		byte[] byteString = mac.doFinal(md5Body.getBytes());
		String calculatedSignature = String.format("%032X", new BigInteger(1, byteString));
		System.out.println(calculatedSignature);
	}
}
