#!/bin/bash

echo "Deleting git repos started..."
while read -r LINE || [[ -n $LINE ]]; do
    tenantId=`echo ${LINE}`
    #echo $tenantId
    echo "Tenant : ${tenantId}"
    echo "Deleting Development/as repository..."
    curl -s -v -X POST -H 'content-type: application/json;' --data "{\"name\": \"Development/as/${tenantId}.git\"}" 'https://s2gituser:PASSWORD@s2git.cloud.wso2.com/rpc?req=DELETE_REPOSITORY'
    echo "Deleting Development/as repository... [DONE]"
    echo -e "\nDeleting Testing/as repository..."
    curl -s -v -X POST -H 'content-type: application/json;' --data "{\"name\": \"Testing/as/${tenantId}.git\"}" 'https://s2gituser:PASSWORD@s2git.cloud.wso2.com/rpc?req=DELETE_REPOSITORY'
    echo "Deleting Testing/as repository... [DONE]"
    echo -e "\nDeleting Production/as repository..."
    curl -s -v -X POST -H 'content-type: application/json;' --data "{\"name\": \"Production/as/${tenantId}.git\"}" 'https://s2gituser:PASSWORD@s2git.cloud.wso2.com/rpc?req=DELETE_REPOSITORY'
    echo "Deleting Testing/as repository... [DONE]"
    echo -e "\nTenant : ${tenantId} ... [COMPLETED]\n"
done < TenantIdFile.txt
echo "S2Git Cleanup completed..."

