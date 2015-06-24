

/**
 * CloudUserManager.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v10  Built on : Sep 04, 2013 (02:02:54 UTC)
 */

    package org.wso2.carbon.cloud.user.manager.stub;

    /*
     *  CloudUserManager java interface
     */

    public interface CloudUserManager {
          

        /**
          * Auto generated method signature
          * 
                    * @param getTenantDisplayNames1
                
             * @throws org.wso2.carbon.cloud.user.manager.stub.CloudUserManagerSQLExceptionException : 
         */

         
                     public org.wso2.carbon.cloud.user.manager.beans.xsd.TenantInfoBean getTenantDisplayNames(

                        java.lang.String user2)
                        throws java.rmi.RemoteException
             
          ,org.wso2.carbon.cloud.user.manager.stub.CloudUserManagerSQLExceptionException;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param getTenantDisplayNames1
            
          */
        public void startgetTenantDisplayNames(

            java.lang.String user2,

            final org.wso2.carbon.cloud.user.manager.stub.CloudUserManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        /**
          * Auto generated method signature
          * 
                    * @param checkForDefaultRole5
                
         */

         
                     public boolean checkForDefaultRole(

                        java.lang.String userName6)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param checkForDefaultRole5
            
          */
        public void startcheckForDefaultRole(

            java.lang.String userName6,

            final org.wso2.carbon.cloud.user.manager.stub.CloudUserManagerCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    