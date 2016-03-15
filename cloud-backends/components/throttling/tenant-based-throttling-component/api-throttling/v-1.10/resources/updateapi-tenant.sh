#!/bin/bash
#This script will update apis created by the tenants
#Add the cloud throttle handler just after the APIAuthenticationHandler
#This file should be placed in <CARBON_SERVER>/repository/tenants

for file in .* *; do 
   for api in "$file/synapse-configs/default/api/"*.xml; do
       echo "$api";  
   sed -i '/org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler/ i <handler class="org.wso2.carbon.cloud.throttling.CloudThrottleHandler"><property name="id" value="B"\/><\/handler>' $api
    done
done
