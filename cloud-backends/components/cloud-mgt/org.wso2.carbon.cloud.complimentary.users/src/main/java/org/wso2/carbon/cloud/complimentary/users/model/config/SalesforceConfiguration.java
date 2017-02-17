/*
 * Copyright (c) 2016, WSO2 Inc. All Rights Reserved.
 */
package org.wso2.carbon.cloud.complimentary.users.model.config;

/**
 * SalesForce class.
 *
 * @since 1.0.0
 */
public class SalesforceConfiguration {

    private String token;
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
