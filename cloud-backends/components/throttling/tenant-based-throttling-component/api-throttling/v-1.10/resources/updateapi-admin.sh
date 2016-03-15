#!/bin/bash
#This script will update apis created by the super admin
#Add the cloud throttle handler just after the APIAuthenticationHandler 
#This file should be placed in <CARBON_SERVER>/repository/deployment/server
#If this file is placed in <CARBON_SERVER>/repository/tenants/<tenant_id>, it will append the handler to the particular tenant's apis.
for api in "synapse-configs/default/api/"*.xml; do       
   sed -i '/org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler/ i <handler class="org.wso2.carbon.cloud.throttling.CloudThrottleHandler"><property name="id" value="B"\/><\/handler>' $api
done

