package org.wso2.carbon.cloud.custom.auth.header;

import org.wso2.msf4j.MicroservicesRunner;

/**
 * Main Application Class.
 */
public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner(8080).deploy(new AuthHeaderService()).start();
    }
}
