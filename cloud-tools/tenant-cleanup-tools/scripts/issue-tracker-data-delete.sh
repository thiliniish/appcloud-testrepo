#!/bin/bash

#Script to run automated sql queries

#Declaring mysql DB connection 

MASTER_DB_USER='IssueTrackerUser'
MASTER_DB_PASSWD='password'
MASTER_DB_PORT='3160'
MASTER_DB_HOST='host'
MASTER_DB_NAME='dbIssueTracker'

printf "\n\nDeleting Issue Tracker data started\n\n"

printf "Deleting comments\n"

while read -r LINE || [[ -n $LINE ]]; do
tenantID=`echo ${LINE}`
printf "\nDeleting comments of tenant : ${tenantID}\n"
mysql -u${MASTER_DB_USER} -p${MASTER_DB_PASSWD} -D${MASTER_DB_NAME} -h${MASTER_DB_HOST} -e "delete from COMMENT where ORGANIZATION_ID=${tenantID}"
done < TenantIDFile.txt
printf "\n\nDeleting comments...COMPLETED.\n"

printf "\nDeleting issues\n"

while read -r LINE || [[ -n $LINE ]]; do
tenantID=`echo ${LINE}`
printf "\nDeleting issues of tenant : ${tenantID}\n"
mysql -u${MASTER_DB_USER} -p${MASTER_DB_PASSWD} -D${MASTER_DB_NAME} -h${MASTER_DB_HOST} -e "delete from ISSUE where ORGANIZATION_ID='${tenantID}'"
done < TenantIDFile.txt
printf "\nDeleting issues...COMPLETED.\n"

printf "\n\nDeleting projects and versions\n"

while read -r LINE || [[ -n $LINE ]]; do
tenantID=`echo ${LINE}`
printf "\nDeleting projects and versions of tenant : ${tenantID}\n"
SQL_Query_Version="delete from VERSION where VERSION.PROJECT_ID in (select distinct PROJECT_ID from PROJECT where PROJECT.ORGANIZATION_ID=${tenantID})"
SQL_Query_Project="delete from PROJECT where ORGANIZATION_ID=${tenantID}"
mysql -u${MASTER_DB_USER} -p${MASTER_DB_PASSWD} -h${MASTER_DB_HOST} -D${MASTER_DB_NAME} <<EOF
$SQL_Query_Version;
$SQL_Query_Project;
EOF
done < TenantIDFile.txt 
printf "\nDeleting projects and versions...COMPLETED.\n"

printf "\n\nIssue Tracker Deletion COMPLETED\n\n"


