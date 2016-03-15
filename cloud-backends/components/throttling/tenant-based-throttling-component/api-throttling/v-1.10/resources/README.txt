This component is responsible for tenant level throttling in API Clooud.
Please follow the steps below to deploy in cloud.

1. Add handler configuration to velocy_templates.xml in pubstore/repository/resources/api_templates/

#if($handler.className == 'org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler')
<handler class="org.wso2.carbon.cloud.throttling.CloudThrottleHandler">
<property name="id" value="B"/>
</handler>
#end


2. Add tenant-based-throttling-component_1.0.0.jar to dropins in Gateway(GW1, GW2, GWMGT)

3. Create directory in <GW_HOME>/repository/conf/throttling

4. Copy tenant_tier_policies.xml to <GW_HOME>/repository/conf/throttling

Migration steps for existing apis

5. Copy updateapi-tenant.sh to repository/tenants directory and execute

6. Copy updateapi-admin.sh to repository/tenants directory and execute

7. Do svn commit
