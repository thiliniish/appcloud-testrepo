#!/bin/bash

#Script to run automated sql queries

#Declaring mysql DB connection 

MASTER_DB_USER='RssMgtUser'
MASTER_DB_PASSWD='password'
MASTER_DB_PORT='3160'
MASTER_DB_HOST='dhHost'
MASTER_DB_NAME='dbRssMgt'
DEV_TEST_PROD_DB_USER='cloudrssadmin'
DEV_DB_PASSWORD='devDBPassword'
DEV_HOST='devHost'
TEST_DB_PASSWORD='testDBPassword'
TEST_HOST='testHost'
PROD_DB_PASSWORD='prodDBPassword'
PROD_HOST='prodHost'

#array to temporary keep the names of the databases
databaseArray=()
#file to keep the names of the databases
destdir=/tmp/app-databaseList.txt

while read -r LINE || [[ -n $LINE ]]; do
    tenantID=`echo ${LINE}`
    echo "$tmp"
    tmp=$(mysql -u$MASTER_DB_USER -p$MASTER_DB_PASSWD -h$MASTER_DB_HOST $MASTER_DB_NAME -se "SELECT DISTINCT NAME from RM_DATABASE WHERE TENANT_ID=$tenantID")
    if [ -z "$tmp" ]
    then 
      echo "No Databases Found for ${tenantID}!"
    else
      printf "Databases of tenant $tenantID to be deleted : $tmp\n"
      databaseArray+=$tmp"\n"
    fi
done < TenantIDFile.txt 
#database names are written to a file
if [ -f "$destdir" ]
then 
  printf "$databaseArray" > "$destdir"
fi

printf "\n\nDatabase Deletion Started\n"
#drop databases in dev/prod/test environments
while read -r LINE || [[ -n $LINE ]]; do
  dbName=`echo ${LINE}`
  printf "\nDeleting Database $dbName from dev Environment\n"
  mysql -u$DEV_TEST_PROD_DB_USER -p$DEV_DB_PASSWORD -h$DEV_HOST -e "drop database $dbName"

  printf "Deleting Database $dbName from prod Environment\n"
  mysql -u$DEV_TEST_PROD_DB_USER -p$PROD_DB_PASSWORD -h$PROD_HOST -e "drop database $dbName"

  printf "Deleting Database $dbName from test Environment\n"
  mysql -u$DEV_TEST_PROD_DB_USER -p$TEST_DB_PASSWORD -h$TEST_HOST -e "drop database $dbName"

done < /tmp/app-databaseList.txt

printf "\n\nDatabase Deletion Completed\n"


