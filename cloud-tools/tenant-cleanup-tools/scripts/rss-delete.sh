#!/bin/bash

#Script to run automated sql queries

#Declaring mysql DB connection 

MASTER_DB_USER='RssMgtUser'
MASTER_DB_PASSWD='password'
MASTER_DB_PORT='3160'
MASTER_DB_HOST='host'
MASTER_DB_NAME='dbRssMgt'

while read -r LINE || [[ -n $LINE ]]; do

tenantID=`echo ${LINE}`
#sql queries
RSS_User="delete from RM_DATABASE_USER where TENANT_ID=${tenantID}"
RSS_Database="delete from RM_DATABASE where TENANT_ID=${tenantID}"
RSS_Privilege_Template="delete from RM_DB_PRIVILEGE_TEMPLATE where TENANT_ID=${tenantID}"

printf "RSS data deletion started for tenant: ${tenantID}\n\n"

mysql -u${MASTER_DB_USER} -p${MASTER_DB_PASSWD} -h$MASTER_DB_HOST -D${MASTER_DB_NAME} <<EOF
$RSS_User;
$RSS_Database;
$RSS_Privilege_Template;
EOF
printf "RSS data Deletion completed for tenant: ${tenantID}\n\n"

done < TenantIDFile.txt 



