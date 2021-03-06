/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.cloud.heartbeat.monitor.core.notification.utils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Password authentication implemented in this class extending Authenticator
 */
public class MailAuthenticator extends Authenticator {
    String user;
    String pw;

    /**
     * Initializes Mail authenticator
     * @param username User name
     * @param password Password
     */
    public MailAuthenticator (String username, String password) {
        super();
        this.user = username;
        this.pw = password;
    }

    /**
     * Returns password authentication
     * @return Password authentication
     */
    public PasswordAuthentication getPasswordAuthentication()  {
        return new PasswordAuthentication(user, pw);
    }
}
