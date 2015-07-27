#!/usr/bin/python

import sys, argparse
import MySQLdb
import xml.etree.ElementTree as ET
from suds.transport.http import HttpAuthenticated
from suds.client import Client

# BPS DB connection parameters
db_host=""
db_user=""
db_password=""
db_name=""

if db_host=="" or db_user=="" or db_password=="" or db_name=="":
    print "*** Error: BPS db connection parameters not set"
    sys.exit()

# AppFactory service parameters
url=""
username=""
password=""

if url=="" or username=="" or password=="":
    print "***  Error: AppFactory TenantInfraStructureInitializerService parameters not set"
    sys.exit()
else:
    infraInitServiceEPR=url + "/services/AppFactoryTenantInfraStructureInitializerService?wsdl"

# Initialize the AppFactoryTenantInfraStructureInitializerService client
transport = HttpAuthenticated(username=username, password=password)
client = Client(infraInitServiceEPR, transport=transport)

# Function to create tenant in a given stratos environment
def initStratosEnv(args):  
    tenant_domain=args.tenantDomain
    if args.stratosEnv=="dev":
        stratos_env="Development"
    elif args.stratosEnv=="test":
        stratos_env="Testing"
    elif args.stratosEnv=="prod":
        stratos_env="Production"
    
    db = MySQLdb.connect(db_host, db_user, db_password, db_name)
    cursor = db.cursor()
    sql="SELECT DATA FROM ODE_MESSAGE WHERE DATA LIKE '%CreateTenantRequest%" + tenant_domain + "%'";
    try:
        message="null"
        cursor.execute(sql)
        result=cursor.fetchall()
        if len(result)==0:
            print "*** Tenant information for domain '" + tenant_domain + "' not found in the BPS db"
        elif len(result)>1:
            print "*** Multiple records found for domain '" + tenant_domain + "', recover tenant manually"
        else:
            message=result[0][0]
    except:
        print "*** Error: unable to fetch data from BPS db"
    
    # Cannot find the soap message from the BPS db cz;
    # 1. No message was found for the entered tenant domain
    # 2. Multipe messages were found for the entered tenant domain
    if message=="null":
      sys.exit()
    
    # Parse the CreateTenantRequest to an XML element
    root=ET.fromstring(message)
    requestElement=root.find("*//{http://wso2.org/bps/sample}CreateTenantRequest")
    
    # Initialize the TenantInfoBean by parsing the XML message
    tenantInfo=client.factory.create('ns0:TenantInfoBean')
    tenantInfo.admin=requestElement.find("./{http://wso2.org/bps/sample}admin").text
    tenantInfo.firstname=requestElement.find("./{http://wso2.org/bps/sample}firstName").text
    tenantInfo.lastname=requestElement.find("./{http://wso2.org/bps/sample}lastName").text
    tenantInfo.adminPassword=requestElement.find("./{http://wso2.org/bps/sample}adminPassword").text
    tenantInfo.tenantDomain=requestElement.find("./{http://wso2.org/bps/sample}tenantDomain").text
    tenantInfo.tenantId=requestElement.find("./{http://wso2.org/bps/sample}tenantId").text
    tenantInfo.createdDate=requestElement.find("./{http://wso2.org/bps/sample}createdDate").text
    tenantInfo.email=requestElement.find("./{http://wso2.org/bps/sample}email").text
    tenantInfo.successKey=requestElement.find("./{http://wso2.org/bps/sample}successKey").text
    tenantInfo.originatedService=requestElement.find("./{http://wso2.org/bps/sample}originatedService").text
    tenantInfo.usagePlan=requestElement.find("./{http://wso2.org/bps/sample}usagePlan").text
    tenantInfo.active=requestElement.find("./{http://wso2.org/bps/sample}active").text
    
    result=client.service.initializeCloudManager(tenantInfo, stratos_env)
    db.close()

# Function to provision Jenkins resources for a tenant
def initJenkins(args):
    result=client.service.initializeBuildManager(args.tenantDomain, "Demo")

# Function to provision Git resources for a tenant
def initGitRepo(args):
    result=client.service.initializeRepositoryManager(args.tenantDomain, "Demo")

def main():
    # create the top-level parser
    parser=argparse.ArgumentParser()
    subparsers=parser.add_subparsers()
    
    # Create parser for command 'stratos'
    parser_addTenant=subparsers.add_parser('stratos', help="Create tenant in specified Stratos environment")
    parser_addTenant.add_argument('tenantDomain', help="Domain name of the tenant that needs to be recovered")
    parser_addTenant.add_argument('stratosEnv', choices=['dev', 'test', 'prod'], help="Straots environment the recovery should run")
    parser_addTenant.set_defaults(func=initStratosEnv)
    
    # Create parser for command 'jenkins'
    parser_addTenant=subparsers.add_parser('jenkins', help="Create tenant specific resources in Jenkins")
    parser_addTenant.add_argument('tenantDomain', help="Domain name of the tenant that needs to be recovered")
    parser_addTenant.set_defaults(func=initJenkins)
    
    # Create parser for commain 'git'
    parser_addTenant=subparsers.add_parser('git', help="Create tenant specific resources in Git")
    parser_addTenant.add_argument('tenantDomain', help="Domain name of the tenant that needs to be recovered")
    parser_addTenant.set_defaults(func=initGitRepo)
	
    args = parser.parse_args()
    args.func(args)
	
if  __name__ =='__main__':main()
