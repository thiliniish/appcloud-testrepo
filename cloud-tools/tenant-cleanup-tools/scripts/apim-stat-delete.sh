#!/bin/bash

#Script to run automated sql queries

#Declaring mysql DB connection 

MASTER_DB_USER='BamStatUser'
MASTER_DB_PASSWD='password'
MASTER_DB_PORT='3160'
MASTER_DB_HOST='dbHost'
MASTER_DB_NAME='dbApimStats'


printf "\n\n Api stat data deleting started...\n\n"

while read -r LINE || [[ -n $LINE ]]; do

tenantDomain=`echo ${LINE}`

SQL_Query_Destination="delete from API_DESTINATION_SUMMARY where context like '/t/${tenantDomain}/%' "
SQL_Query_Fault="delete from API_FAULT_SUMMARY where context like '/t/${tenantDomain}/%' "
SQL_Query_Request="delete from API_REQUEST_SUMMARY where context like '/t/${tenantDomain}/%' "
SQL_Query_Response="delete from API_RESPONSE_SUMMARY where context like '/t/${tenantDomain}/%' "
SQL_Query_Resource="delete from API_Resource_USAGE_SUMMARY where context like '/t/${tenantDomain}/%' "
SQL_Query_Version="delete from API_VERSION_USAGE_SUMMARY where context like '/t/${tenantDomain}/%' "
SQL_Query_Throttle="delete from API_THROTTLED_OUT_SUMMARY where context like '/t/${tenantDomain}/%' "

printf "Stat Deletion started for tenant: ${tenantDomain}\n\n"

mysql -u${MASTER_DB_USER} -p${MASTER_DB_PASSWD} -h${MASTER_DB_HOST} -D${MASTER_DB_NAME} <<EOF
$SQL_Query_Destination;
$SQL_Query_Fault;
$SQL_Query_Request;
$SQL_Query_Response;
$SQL_Query_Resource;
$SQL_Query_Version;
$SQL_Query_Throttle;
EOF
printf "Stat Deletion completed for tenant: ${tenantDomain}\n\n"

done < TenantDomainFile.txt 
echo "Api Stat data cleanup completed..."
