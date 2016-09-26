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
package org.wso2.carbon.cloud.signup.constants;

/**
 * This class holds all the contant values in the program.
 */
public class SignUpWorkflowConstants {

    public static final String TENANT_EMAIL_PATH = "tenantEmail.txt";
    public static final String USER_EMAIL_PATH = "userEmail.txt";
    public static final String TENANT_REJECTION_EMAIL_PATH = "tenantRejectionEmail.txt";
    public static final String TENANT_APPROVAL_EMAIL_PATH = "tenantApprovalEmail.txt";
    public static final String TENANT_NOTIFICATION_EMAIL_PATH = "tenantNotificationEmail.txt";
    public static final String CONFIG_FILE_NAME = "signupConfigProperties.json";
    public static final String WORKFLOW_TYPE = "AM_USER_SIGNUP";
    public static final String CONFIG_FILE_LOCATION = "signUpConfig";
    public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String CUSTOMIZED = "customized_";
    public static final String CONTENT_ID = "Content-ID";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String EMAIL_CLAIM_URI = "http://wso2.org/claims/emailaddress";
}
