#!/bin/bash
####################################
# To  build the cloud packs and jars
####################################
PATH='/opt/java/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/opt/mvn/apache-maven-3.0.5/bin'
echo "##### Start updating puppet master with cloud jars and packs #####"

#Define file name to read details from
INSTRUCTIONS_FILE=file.txt

#Define variables
CLOUD_HOME='/opt/cloud'
PUPPET_BASE='/mnt/appfactory/modules'
CLOUDMGT_XML_PATH='files/configs/repository/conf/cloud'
CLOUDMGT_APP_PATH='files/jaggeryapps'
DROPINS_PATH='files/configs/repository/components/dropins'

#Pull latest changes from git
cd ${CLOUD_HOME}
echo "#####  Pull changes from git repo  #####"
git fetch --all
git checkout cloud-staging
git pull

#Return to the script location
cd -

#Start processing the files
echo "===================== Start Processing $INSTRUCTIONS_FILE ====================="

#Store old IFS value & set new IFS value
OLDIFS=$IFS
IFS=","

#Start while
while read line
do
	#Check if commented
	if [[ $line =~ ^#+ ]]; then
		echo "##### Skipping the commented line in file.... #####"
		continue
	fi

	#set count to 0
 	i=0

 	#Start loop
 	for attribute in $line
   	do
    		#Start case check
    		case "$i" in
     		"0")
      		BASE_TYPE=$attribute
      		;;
     		"1")
      		GIT_PATH=$attribute
      		;;
     		"2")
      		PUPPET_PATH_TYPE=$attribute
      		;;
     		"3")
      		BUILD_FLAG=$attribute
      		;;
     		"4")
      		FILE_NAME=$attribute
      		;;
     		"5")
      		NEW_FILE_NAME=$attribute
      		;;
    		esac
    		#End case check

		#Increment count
    		i=$(($i+1))
 	done
 	#End loop

	echo "#######   Starting Puppet addition for $FILE_NAME   #######"

	FILE_PATH=${CLOUD_HOME}/${GIT_PATH}
	#Continue to maven build if build flag is set
 	if $BUILD_FLAG ; then
  		#Procced to maven build
		echo "####### Starting maven build #######"
  		cd ${FILE_PATH}
  		if mvn clean install ;  then
    			echo "Maven Clean successful for $FILE_NAME"
			FILE_PATH=${FILE_PATH}/target/${FILE_NAME}
  		else
    			echo "Something went wrong while building $FILE_NAME"
			echo "#######   Skipping puppet addition for $FILE_NAME due to build failure   #######"
  			continue
  		fi
		echo "####### Maven build finished for $FILE_NAME #######"
 	fi

	#Set puppet home
	PUPPET_HOME=$PUPPET_BASE/$BASE_TYPE
        #Set puppet sub path
	eval PUPPET_SUB_PATH="\$$PUPPET_PATH_TYPE"
	PUPPET_PATH=$PUPPET_HOME/$PUPPET_SUB_PATH

	#Copy to puppet path
	cp ${FILE_PATH} ${PUPPET_PATH}/${NEW_FILE_NAME}
	echo "#######   Ending Puppet addition for $FILE_NAME   #######"

done <$INSTRUCTIONS_FILE
echo "===================== End Processing $INSTRUCTIONS_FILE ====================="
#End while
#Reset the IFS value
IFS=$OLDIFS

#Changing ownership
echo "#####  Changing ownership to puppet user #####"
chown -R puppet:puppet ${PUPPET_BASE}/*

echo "##### Updated puppet master with cloud jars and packs #####"
