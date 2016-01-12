# Cloud - Extrenal GSN Games

# Custom Authentication Mediator For GSN Games - 21st December 2015

This custom handler and mediator classes are written for the use of GSN Games in an API manager 1.9.1 instance.
This is intended for the authentication of API calls received at API gateway.
Deploying this requires to follow these below steps

(1.) Add a new Message Formatter to <APIM-HOME>/repository/conf/axis2/axis2.xml and  <APIM-HOME>/repository/conf/axis2/tenant-axis2.xml
in <axisconfig>
        <messageFormatters>
            <messageFormatter contentType="application/hal+json" class="org.apache.synapse.commons.json.JsonStreamFormatter"/>


(2.) Add a new Message Builder to <APIM-HOME>/repository/conf/axis2/axis2.xml and  <APIM-HOME>/repository/conf/axis2/tenant-axis2.xml in
<axisconfig>
        <messageBuilders>
           <messageBuilder contentType="application/hal+json" class="org.apache.synapse.commons.json.JsonStreamBuilder"/>

(3.) Add the below configuration in to <APIM_HOME>/repository/resources/api_templates/velocity_template.xml following <handlers xmlns="http://ws.apache.org/ns/synapse">
    #if ( $apiContext.contains("add the tenant domain within "/Tenant Domain/"   Format") )
                     <handler class="org.wso2.carbon.cloud.external.gsngames.customhandler.CopyAuthHeader" />
                     #end

(4.) Build and Deploy the gsngames-authentication-mediator-1.0.0.jar into <APIM-HOME>/repository/components/lib/

(5.) Start APIM by sh <APIM-HOME>/bin/wso2server.sh start

(6.) Log into APIM management console and add the CustomMediatorSequence.xml to /_system/governance/apimgt/customsequences/in/CustomMediatorSequence.xml
replace the secretKey property values with the secret keys used.

(7.) save the sequence and deploy and invoke api from the APIM gateway.
