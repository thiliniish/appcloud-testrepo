package org.wso2.carbon.cloud.rolemgt.common;

/**
 * Created by dilhasha on 4/25/16.
 */
public class RoleMgtException extends Exception{
    public RoleMgtException() {
    }

    public RoleMgtException(String s) {
        super(s);
    }

    public RoleMgtException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public RoleMgtException(Throwable throwable) {
        super(throwable);
    }
}