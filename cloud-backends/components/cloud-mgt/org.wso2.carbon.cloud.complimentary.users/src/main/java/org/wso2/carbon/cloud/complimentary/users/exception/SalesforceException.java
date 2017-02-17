/*
 * Copyright (c) 2017, WSO2 Inc. All Rights Reserved.
 */

package org.wso2.carbon.cloud.complimentary.users.exception;

/**
 * Used to wrap the actual exception with a custom message which occurs when trying to retrieve data from Salesforce.
 *
 * @since 1.0.0
 */
public class SalesforceException extends Exception {

    public SalesforceException(Throwable cause) {
        super(cause);
    }

    public SalesforceException(String msg) {
        super(msg);
    }

    public SalesforceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
