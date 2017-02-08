#!/bin/bash
####################################
# To  add the nginx client
####################################

echo "Creating directory structure to store Nginx Client"
mkdir nginxClient
cd nginxClient

echo "Checking out Nginx Client from git repository"
git init
git remote add -f origin https://github.com/wso2/cloud.git
echo "cloud-tools/custom-url/nginx-client" >> .git/info/sparse-checkout
git pull origin cloud-master

cd cloud-tools/custom-url/nginx-client
if mvn clean install ;  then
  echo "Maven Clean successful for nginx client"
else
  echo "Something went wrong while building nginx client"
  exit 1
fi

echo "Back up domain mapper if already exists"

SRCDIR="/mnt/domain-mapper"
DATE=-$(date +"%m-%d-%Y")-$(date +"%-T")
mv $SRCDIR $SRCDIR$DATE 

echo "Backed up domain mapper to " $SRCDIR$DATE

echo "Unzipping domain mapper zip to /mnt/domain-mapper" 

unzip -d /mnt target/cloud-domain-mapper-1.0.0.zip
echo "Move to domain-mapper"
mv /mnt/cloud-domain-mapper-1.0.0 $SRCDIR

echo "Please remember to update the following resources according to the environment"
echo " - aes-keystore.jck"
echo " - client-truststore.jks"
echo " - wso2carbon.jks"

echo "Please remember to update the config.properties according to the environment"

echo "Removing the temporary nginx client directory with source code"
rm -r ../../../../nginxClient
