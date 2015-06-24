
/**
 * CloudUserManagerSQLExceptionException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v10  Built on : Sep 04, 2013 (02:02:54 UTC)
 */

package org.wso2.carbon.cloud.user.manager.stub;

public class CloudUserManagerSQLExceptionException extends java.lang.Exception{

    private static final long serialVersionUID = 1435160989114L;
    
    private org.wso2.carbon.cloud.user.manager.CloudUserManagerSQLException faultMessage;

    
        public CloudUserManagerSQLExceptionException() {
            super("CloudUserManagerSQLExceptionException");
        }

        public CloudUserManagerSQLExceptionException(java.lang.String s) {
           super(s);
        }

        public CloudUserManagerSQLExceptionException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public CloudUserManagerSQLExceptionException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(org.wso2.carbon.cloud.user.manager.CloudUserManagerSQLException msg){
       faultMessage = msg;
    }
    
    public org.wso2.carbon.cloud.user.manager.CloudUserManagerSQLException getFaultMessage(){
       return faultMessage;
    }
}
    