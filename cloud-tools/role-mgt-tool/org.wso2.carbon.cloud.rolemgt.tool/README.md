# cloud

# Role Management tool to add, update, delete roles in existing tenants

(1.) Build the bundle 'org.wso2.carbon.cloud.rolemgt.tool' in 'cloud/cloud-tools/role-mgt-tool' using maven.

(2.) Place the built bundle in 'repository/components/dropins' directory in the AS server of your cloud setup.

(3.) Create an XML configuration file named 'role-mgt' which is similar to the sample XML configuration specified in 'resources/role-mgt.xml'. 
     The XML file should include details about the roles to be added, updated or deleted. 
	
	Only 'add','update','delete' should be added within '<Action>' tags.

(4.) Place the created XML file (role-mgt.xml) within 'repository/conf/cloud' directory in the AS server of your cloud setup.

(5.) Then start the server. You can specify a range of tenants you want to update by passing '-Drange=<lowerBound>:<upperBound>'
     The lowerBound and upperBound indicate the range of tenant IDs you need to update.

