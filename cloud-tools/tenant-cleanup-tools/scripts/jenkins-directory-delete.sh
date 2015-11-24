#!/bin/bash

echo "Deleting Tenant’s jenkins cleanup Started..."
while read -r LINE || [[ -n $LINE ]]; do
    tenantDomain=`echo ${LINE}`
    echo "Tenant : ${tenantDomain}"
    echo "Deleting Directory..."
    if [ -d "jobs/${tenantDomain}" ]
    then
        rm -r jobs/${tenantDomain}
        echo -e "\nTenant : ${tenantDomain} directory deletion... [COMPLETED]\n"
    else
        echo -e "\nNo directory found for : ${tenantDomain} \n"
    fi
    echo -e "\nTenant : ${tenantDomain} ... [COMPLETED]\n"
done < TenantDomainFile.txt
echo "tenants jenkins directory cleanup completed..."

