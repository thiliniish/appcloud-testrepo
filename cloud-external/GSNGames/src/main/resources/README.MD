# Cloud - Extrenal GSN Games

# Custom Authentication Mediator For GSN Games - 21st December 2015

This custom handler and mediator classes are written for the use of GSN Games in an API manager 1.9.1 instance.
This is intended for the authentication of API calls received at API gateway.
Deploying this requires to follow these below steps

(1.) Since this is tenant specific we need to add the handler to apis which are created by the specific tenant therefore we need
to change the <APIM-HOME>/repository/resources/api_templates/velocity_template.xml file to do so add
<handlers xmlns="http://ws.apache.org/ns/synapse">
    #if ( $apiContext.contains("/<GSNTenantName>/") )
        <handler class="org.wso2.carbon.cloud.gsn.customhandler.CopyAuthHeader" />
    #end

(2.) Add a new Message Formatter to <APIM-HOME>/repository/conf/axis2/axis2.xml and  <APIM-HOME>/repository/conf/axis2/tenant-axis2.xml
in <axisconfig>
        <messageFormatters>
            <messageFormatter contentType="application/hal+json" class="org.apache.synapse.commons.json.JsonStreamFormatter"/>


(3.) Add a new Message Builder to <APIM-HOME>/repository/conf/axis2/axis2.xml and  <APIM-HOME>/repository/conf/axis2/tenant-axis2.xml in
<axisconfig>
        <messageBuilders>
            <messageBuilder contentType="application/x-www-form-urlencoded" class="org.apache.synapse.commons.builders.XFormURLEncodedBuilder"/>

(4.) Add the below configuration in to <APIM_HOME>/repository/resources/api_templates/velocity_template.xml following <handlers xmlns="http://ws.apache.org/ns/synapse">
    #if ( $apiContext.contains("add the tenant domain within "/Tenant Domain/"   Format") )
                     <handler class="org.wso2.carbon.cloud.gsn.customhandler.CopyAuthHeader" />
                     #end

(5.) Build and Deploy the GSNGames-1.0.0.jar into <APIM-HOME>/repository/components/lib/

(6.) Start APIM by sh <APIM-HOME>/bin/wso2server.sh start

(7.) Log into APIM management console and add the CustomMediatorSequence.xml to /_system/governance/apimgt/customsequences/in/CustomMediatorSequence.xml
replace the secretKey property values with the secret keys used.

(8.) save the sequence and deploy and invoke api from the APIM gateway.
