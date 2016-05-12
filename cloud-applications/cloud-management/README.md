# Following changes are required in the cloud-mgt.xml in order to remove appfactory.xml dependency from cloudmgt app.

(1.) The following element 'ApplicationDeployment' needs to be copied to cloud-mgt.xml from appfactory.xml

    <ApplicationDeployment>
           <DeploymentStage name="Development">
              <CanCommit>true</CanCommit>
                <TenantRoles>
                    <Role name="admin">
                        <Permission>
                            CreateApplication/CreateApplication:invoke-service,
                            /permission/admin/tenant/user/mgt
                        </Permission>
                    </Role>
                    <Role name="default">
                      <Permission>
                        /permission/admin/login,
                        /permission/protected/manage/monitor/tenants
                      </Permission>
                    </Role>
                    <Role name="everyone">
                        <Permission>
                            deny:/_system/governance/dependencies/:REGISTRY_GET,
                            deny:/_system/governance/dependencies/:REGISTRY_PUT,
                            deny:/_system/governance/dependencies/:REGISTRY_DELETE,
                            deny:/_system/governance/dependencies/:authorize
                        </Permission>
                    </Role>
                    <Role name="developer">
                        <Permission>
                            /permission/admin/login,
                            /permission/admin/manage/modify/webapp,
                            /permission/admin/configure/rssmanager,
                            /permission/admin/manage/resources/ws-api,
                            /permission/admin/configure/datasources,
                            /permission/admin/manage/resources/govern/lifecycles,
                            /permission/admin/monitor/logging,
    						/permission/admin/appfactory/resources/create/Development,
    						/permission/admin/appfactory/resources/read/Development,
                    		/permission/admin/appfactory/resources/update/Development
                        </Permission>
                    </Role>
    	    		<Role name="qa">
    					<Permission>
    						/permission/admin/login,
    						/permission/admin/manage/resources/ws-api,
    						/permission/admin/configure/datasources,
    						/permission/admin/appfactory/resources/read/Development,
    					</Permission>
    				</Role>
                    <Role name="appowner">
                        <Permission>
                            /permission/admin/login,
                            /permission/admin/manage/modify/webapp,
                            /permission/admin/configure/rssmanager,
                            /permission/admin/manage/resources/ws-api,
                            /permission/admin/configure/datasources,
                            /permission/admin/manage/resources/govern/lifecycles,
    						/permission/admin/appfactory/resources/create/Development,
                    		/permission/admin/appfactory/resources/update/Development,
    						/permission/admin/appfactory/resources/read/Development,
    						/permission/admin/configure/security,
                            /permission/admin/monitor/logging
                        </Permission>
                    </Role>
                </TenantRoles>
                <Deployer>
                    <ApplicationType name="*">

                        <ClassName>org.wso2.carbon.appfactory.jenkins.deploy.JenkinsArtifactDeployer</ClassName>
                        <Endpoint>https://sc.dev.cloudstaging.wso2.com:9443/services/</Endpoint>
                        <RepositoryProvider>
                            <Property name="Class">
                                org.wso2.carbon.appfactory.s4.integration.GITBlitBasedGITRepositoryProvider
                            </Property>
                            <Property name="BaseURL">https://s2git.cloudstaging.wso2.com/</Property>
                            <Property name="URLPattern">{@stage}/as</Property>
                            <Property name="AdminUserName">s2gituser</Property>
                            <Property name="AdminPassword">s2gituser</Property>
                        </RepositoryProvider>
                        <Properties>
                            <Property name="minInstances">1</Property>
                            <Property name="maxInstances">1</Property>
                            <Property name="shouldActivate"></Property>
                            <Property name="alias">appserver</Property>
                            <!--mac182dev is the value -->
                            <Property name="cartridgeType">appserver</Property>
                            <Property name="deploymentPolicy">stratos_deployment</Property>
                            <Property name="autoscalePolicy">stratos_autoscale</Property>
                            <!--mac182dev is the value -->
                            <Property name="repoURL"></Property>
                            <Property name="dataCartridgeType"></Property>
                            <Property name="dataCartridgeAlias"></Property>
                        </Properties>

                    </ApplicationType>
                </Deployer>
               <AllowDomainMapping>false</AllowDomainMapping>
               <StratosLBUrl>http://dev.lb.cloudapps.com</StratosLBUrl>
                <AllowDirectDeploy>true</AllowDirectDeploy>
                <!--if autodeployment is enabled, the selected versions to autodeploy will be built and deployed after every commit-->
                <AutomaticDeployment enabled="true">
                    <!--time period to poll the repository-->
                    <PollingPeriod>6</PollingPeriod>
                </AutomaticDeployment>
                <DeploymentArtifact>
                    <TriggerBuild>
                        <ArtifactStoragePolicy>Latest</ArtifactStoragePolicy>
                        <!-- Latest, Tagged artifact will be deployed -->
                    </TriggerBuild>
                </DeploymentArtifact>
                <Promote>
                    <DeploymentPolicy>Latest</DeploymentPolicy>
                    <TargetStage>Testing</TargetStage>
                    <!-- Tagged/Promoted artifact will be deployed to the promoted stage-->
                </Promote>
                <CreateArtifact>Always</CreateArtifact>
                <ProcessEndPoint>https://process.cloudstaging.wso2.com/services/DeployToStage</ProcessEndPoint>
                <DeploymentServerURL>https://asdevelopment.cloudstaging.wso2.com/services/</DeploymentServerURL>
                <LogServerURL>https://asdevelopment.cloudstaging.wso2.com/services/</LogServerURL>
                <MountPoint>dev</MountPoint>
                <BaseAccessUrl>https://asdevelopment.cloudstaging.wso2.com</BaseAccessUrl>
                <GregServerURL>https://registry.dev.cloudstaging.wso2.com:9443/services/</GregServerURL>
                <Buildable>true</Buildable>
                <StorageServerUrl>https://storage.cloudstaging.wso2.com/services/</StorageServerUrl>
                <RssName>Development</RssName>

            </DeploymentStage>

            <DeploymentStage name="Testing">
              <CanCommit>false</CanCommit>
                <TenantRoles>
                    <Role name="admin">
                        <Permission>
                            CreateApplication/CreateApplication:invoke-service,
                            /permission/admin/tenant/user/mgt
                        </Permission>
                    </Role>
                    <Role name="default">
                      <Permission>
                        /permission/admin/login,
                        /permission/protected/manage/monitor/tenants
                      </Permission>
                    </Role>
                    <Role name="everyone">
                        <Permission>
                            deny:/_system/governance/dependencies/:REGISTRY_GET,
                            deny:/_system/governance/dependencies/:REGISTRY_PUT,
                            deny:/_system/governance/dependencies/:REGISTRY_DELETE,
                            deny:/_system/governance/dependencies/:authorize
                        </Permission>
                    </Role>
                    <Role name="qa">
                        <Permission>
                            /permission/admin/login,
                            /permission/admin/manage/modify/webapp,
                            /permission/admin/configure/rssmanager,
                            /permission/admin/manage/resources/ws-api,
                            /permission/admin/configure/datasources,
                            /permission/admin/manage/resources/govern/lifecycles,
                            /permission/admin/monitor/logging,
    						/permission/admin/appfactory/resources/create/Testing,
    						/permission/admin/appfactory/resources/update/Testing,
    						/permission/admin/appfactory/resources/read/Testing
                        </Permission>
                    </Role>
    				<Role name="developer">
    					<Permission>
    						/permission/admin/login,
    						/permission/admin/manage/resources/ws-api,
    						/permission/admin/configure/datasources,
    						/permission/admin/appfactory/resources/read/Testing
    					</Permission>
    				</Role>
                    <Role name="devops">
    		           <Permission>
    						/permission/admin/login,
    		                /permission/admin/manage/resources/ws-api,
    			        	/permission/admin/configure/datasources,
    						/permission/admin/appfactory/resources/read/Testing
    			       </Permission>
    		        </Role>
                    <Role name="appowner">
                        <Permission>
                            /permission/admin/login,
                            /permission/admin/manage/modify/webapp,
                            /permission/admin/configure/rssmanager,
                            /permission/admin/manage/resources/ws-api,
                            /permission/admin/configure/datasources,
                            /permission/admin/manage/resources/govern/lifecycles,
    						/permission/admin/appfactory/resources/update/Testing,
    						/permission/admin/appfactory/resources/create/Testing,
    						/permission/admin/appfactory/resources/read/Testing,
    						/permission/admin/configure/security,
    						/permission/admin/monitor/logging
                        </Permission>
                    </Role>
                </TenantRoles>
                <Deployer>
                    <ApplicationType name="*">

                        <ClassName>org.wso2.carbon.appfactory.jenkins.deploy.JenkinsArtifactDeployer</ClassName>
                        <Endpoint>https://sc.test.cloudstaging.wso2.com:9443/services/</Endpoint>
                        <RepositoryProvider>
                            <Property name="Class">
                                org.wso2.carbon.appfactory.s4.integration.GITBlitBasedGITRepositoryProvider
                            </Property>
                            <Property name="BaseURL">https://s2git.cloudstaging.wso2.com/</Property>
                            <Property name="URLPattern">{@stage}/as</Property>
                            <Property name="AdminUserName">s2gituser</Property>
                            <Property name="AdminPassword">s2gituser</Property>
                        </RepositoryProvider>
                        <Properties>
                            <Property name="minInstances">1</Property>
                            <Property name="maxInstances">1</Property>
                            <Property name="shouldActivate"></Property>
                            <Property name="alias">appserver</Property>
                            <!--mac182dev is the value -->
                            <Property name="cartridgeType">appserver</Property>
                            <!--mac182dev is the value -->
                            <Property name="deploymentPolicy">stratos_deployment</Property>
                            <Property name="autoscalePolicy">stratos_autoscale</Property>
                            <Property name="repoURL"></Property>
                            <Property name="dataCartridgeType"></Property>
                            <Property name="dataCartridgeAlias"></Property>
                        </Properties>

                    </ApplicationType>
                </Deployer>

              <AllowDomainMapping>false</AllowDomainMapping>
              <StratosLBUrl>http://test.lb.cloudapps.com</StratosLBUrl>

                <AllowDirectDeploy>true</AllowDirectDeploy>
                <AutomaticDeployment enabled="false">
                    <PollingPeriod>6</PollingPeriod>
                </AutomaticDeployment>
                <DeploymentArtifact>
                    <TriggerBuild>
                        <ArtifactStoragePolicy>Tagged</ArtifactStoragePolicy>
                        <!-- Latest, Marked-->
                    </TriggerBuild>
                </DeploymentArtifact>
                <Promote>
                    <DeploymentPolicy>Promoted</DeploymentPolicy>
                    <!-- Latest/Tagged artifact will be deployed to the promoted stage-->
                    <TargetStage>Production</TargetStage>
                </Promote>
                <Demote>
                    <TargetStage>Development</TargetStage>
                </Demote>
                <CreateArtifact>UserInput</CreateArtifact>
                <ProcessEndPoint>https://process.cloudstaging.wso2.com/services/DeployToStage</ProcessEndPoint>
                <DeploymentServerURL>https://astesting.cloudstaging.wso2.com/services/</DeploymentServerURL>
                <LogServerURL>https://astesting.cloudstaging.wso2.com/services/</LogServerURL>

                <!-- This is the BAM server that does the logging -->
                <MountPoint>test</MountPoint>
                <BaseAccessUrl>https://astesting.cloudstaging.wso2.com</BaseAccessUrl>
                <GregServerURL>https://registry.test.cloudstaging.wso2.com:9443/services/</GregServerURL>
                <Buildable>false</Buildable>
                <StorageServerUrl>https://storage.cloudstaging.wso2.com/services/</StorageServerUrl>
                <RssName>Testing</RssName>

            </DeploymentStage>

            <DeploymentStage name="Production">
              <CanCommit>false</CanCommit>
                <TenantRoles>
                    <Role name="admin">
                        <Permission>
                            CreateApplication/CreateApplication:invoke-service,
                            /permission/admin/tenant/user/mgt
                        </Permission>
                    </Role>
                    <Role name="default">
                      <Permission>
                        /permission/admin/login,
                        /permission/protected/manage/monitor/tenants
                      </Permission>
                    </Role>
                    <Role name="everyone">
                        <Permission>
                            deny:/_system/governance/dependencies/:REGISTRY_GET,
                            deny:/_system/governance/dependencies/:REGISTRY_PUT,
                            deny:/_system/governance/dependencies/:REGISTRY_DELETE,
                            deny:/_system/governance/dependencies/:authorize
                        </Permission>
                    </Role>
                    <Role name="devops">
                        <Permission>
                            /permission/admin/login,
                            /permission/admin/manage/modify/webapp,
                            /permission/admin/configure/rssmanager,
                            /permission/admin/manage/resources/ws-api,
                            /permission/admin/configure/datasources,
                            /permission/admin/manage/resources/govern/lifecycles,
                            /permission/admin/monitor/logging,
    						            /permission/admin/appfactory/resources/create/Production,
    						            /permission/admin/appfactory/resources/update/Production,
    						            /permission/admin/appfactory/resources/read/Production
                        </Permission>
                    </Role>
                    <Role name="appowner">
                        <Permission>
                            /permission/admin/login,
                            /permission/admin/configure/security,
    						            /permission/admin/configure/datasources,
    						            /permission/admin/manage/resources/ws-api,
    						            /permission/admin/appfactory/resources/create/Production,
    						            /permission/admin/appfactory/resources/read/Production,
                            /permission/admin/appfactory/resources/update/Production,
                            /permission/admin/stratos/domainMappings/manage,
                            /permission/admin/stratos/domainMappings/view
                        </Permission>
                    </Role>
                </TenantRoles>
                <Deployer>
                    <ApplicationType name="*">

                        <ClassName>org.wso2.carbon.appfactory.jenkins.deploy.JenkinsArtifactDeployer</ClassName>
                        <Endpoint>https://sc.cloudstaging.wso2.com:9443/services/</Endpoint>
                        <RepositoryProvider>
                            <Property name="Class">
                                org.wso2.carbon.appfactory.s4.integration.GITBlitBasedGITRepositoryProvider
                            </Property>
                            <Property name="BaseURL">https://s2git.cloudstaging.wso2.com/</Property>
                            <Property name="URLPattern">{@stage}/as</Property>
                            <Property name="AdminUserName">s2gituser</Property>
                            <Property name="AdminPassword">s2gituser</Property>
                        </RepositoryProvider>
                        <Properties>
                            <Property name="minInstances">1</Property>
                            <Property name="maxInstances">1</Property>
                            <Property name="shouldActivate"></Property>
                            <Property name="alias">appserver</Property>
                            <!--mac182dev is the value -->
                            <Property name="cartridgeType">appserver</Property>
                            <!--mac182dev is the value -->
                            <Property name="deploymentPolicy">stratos_deployment</Property>
                            <Property name="autoscalePolicy">stratos_autoscale</Property>
                            <Property name="repoURL"></Property>
                            <Property name="dataCartridgeType"></Property>
                            <Property name="dataCartridgeAlias"></Property>
                        </Properties>

                    </ApplicationType>
                </Deployer>

              <AllowDomainMapping>false</AllowDomainMapping>
              <StratosLBUrl>http://prod.lb.cloudapps.com</StratosLBUrl>

                <AllowDirectDeploy>true</AllowDirectDeploy>
                <AutomaticDeployment enabled="false">
                    <PollingPeriod>6</PollingPeriod>
                </AutomaticDeployment>
                <DeploymentArtifact>
                    <PersistedArtifacts>Tagged</PersistedArtifacts>
                </DeploymentArtifact>

                <Demote>
                    <TargetStage>Testing</TargetStage>
                </Demote>
                <CreateArtifact>Never</CreateArtifact>
                <ProcessEndPoint>https://process.cloudstaging.wso2.com/services/DeployToStage</ProcessEndPoint>
                <!-- This is the BAM server that does the logging -->
                <DeploymentServerURL>https://asproduction.cloudstaging.wso2.com/services/</DeploymentServerURL>
                <LogServerURL>https://asproduction.cloudstaging.wso2.com/services/</LogServerURL>
                <MountPoint>prod</MountPoint>
                <BaseAccessUrl>https://asproduction.cloudstaging.wso2.com</BaseAccessUrl>
                <GregServerURL>https://registry.prod.cloudstaging.wso2.com:9443/services/</GregServerURL>
                <Semantic>Production</Semantic>
                <Buildable>false</Buildable>
                <StorageServerUrl>https://storage.cloudstaging.wso2.com/services/</StorageServerUrl>
                <RssName>Production</RssName>
            </DeploymentStage>
        </ApplicationDeployment>
