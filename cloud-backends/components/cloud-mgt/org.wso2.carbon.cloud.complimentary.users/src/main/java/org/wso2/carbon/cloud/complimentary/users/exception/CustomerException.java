/*
 * Copyright (c) 2017, WSO2 Inc. All Rights Reserved.
 */
package org.wso2.carbon.cloud.complimentary.users.exception;

/**
 * Used to wrap the actual exception with a custom message.
 *
 * @since 1.0.0
 */
public class CustomerException extends Exception {

    public CustomerException(String msg) {
        super(msg);
    }

    public CustomerException(String msg, Throwable e) {
        super(msg, e);
    }

    public CustomerException(Throwable e) {
        super(e);
    }
}
