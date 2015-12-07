#!/bin/bash

printf "Deleting Tenant Git Directories Started...\n"
while read -r LINE || [[ -n $LINE ]]; do
    tenantDomain=`echo ${LINE}`
    printf "Tenant : ${tenantDomain}"
    printf "Deleting Directory"
    if [ -d "git/${tenantDomain}" ]
    then
        rm -r git/${tenantDomain}
        printf "\nTenant : ${tenantDomain} directory deletion [COMPLETED]\n"
    else
        printf "\nNo directory found for : ${tenantDomain} \n"
    fi
    if [ -d "git/~${tenantDomain}" ]
    then
        rm -r "git/~${tenantDomain}"
        printf "\nTenant : ${tenantDomain} forked directory deletion [COMPLETED]\n"
    else
        printf "\nNo forked directory found for : ${tenantDomain} \n"
    fi
    printf "\nTenant : ${tenantDomain} [COMPLETED]\n"
done < TenantDomainFile.txt
printf "Git Directory Cleanup completed."

