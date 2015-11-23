#!/bin/bash
while read -r LINE || [[ -n $LINE ]]; do
        tenantDomain=`echo ${LINE}`
        alias="appserver${tenantDomain}"
        echo "\n \n ---------------------- Tenant : ${tenantDomain} -----------------------"
        echo "Removing subscriptions of tenant ${tenantDomain}"
        curl -X POST -v -H "Content-Type: application/json" -d ${alias} -k -v -u 'cloudadmin:Password' https://localhost:9443/stratos/admin/cartridge/unsubscribe/tenant/${tenantDomain}
        echo "\n Removing subscriptions...[DONE]"
done < TenantDomainFile.txt
echo "\n Stratos subscription removal completed..."

