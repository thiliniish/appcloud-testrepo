package org.wso2.carbon.apimgt.custominterceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.uri.template.URITemplate;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Custom Interceptor written for APIM Admin API
 */
public class CustomPreAuthenticationInInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(CustomPreAuthenticationInInterceptor.class);

    public CustomPreAuthenticationInInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        String path = (String) message.get(Message.PATH_INFO);
        String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
        Dictionary<URITemplate, List<String>> whiteListedResourcePathsMap;

        //If Authorization headers are present anonymous URI check will be skipped
        ArrayList authHeaders = (ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS)))
                .get(RestApiConstants.AUTH_HEADER_NAME);
        if (authHeaders != null) {
            return;
        }
        ArrayList xWso2TenantHeaders = (ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS)))
                .get("X-WSO2-Tenant");
        String requestedTenant = null;

        if (xWso2TenantHeaders != null && xWso2TenantHeaders.size() > 0) {
            requestedTenant = xWso2TenantHeaders.get(0).toString();
        }

        //Check if the accessing URI is white-listed and then authorization is skipped
        try {
            whiteListedResourcePathsMap = RestApiUtil.getWhiteListedURIsToMethodsMap();
            Enumeration<URITemplate> uriTemplateSet = whiteListedResourcePathsMap.keys();

            while (uriTemplateSet.hasMoreElements()) {
                URITemplate uriTemplate = uriTemplateSet.nextElement();
                if (uriTemplate.matches(path, new HashMap<String, String>())) {
                    List<String> whiteListedVerbs = whiteListedResourcePathsMap.get(uriTemplate);
                    if (whiteListedVerbs.contains(httpMethod)) {
                        message.put(RestApiConstants.AUTHENTICATION_REQUIRED, false);
                        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                        if (requestedTenant != null && RestApiUtil.isTenantAvailable(requestedTenant)) {
                            if (MultitenantUtils.isEmailUserName()) {
                                carbonContext.setUsername(
                                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME + "@wso2.com" + "@"
                                                + requestedTenant);
                            } else {
                                carbonContext.setUsername(
                                        CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME + "@" + requestedTenant);
                            }
                            carbonContext.setTenantDomain(requestedTenant);
                            carbonContext.setTenantId(APIUtil.getTenantIdFromTenantDomain(requestedTenant));
                        } else {
                            carbonContext.setUsername(CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME);
                            carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                            carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                        }
                        return;
                    }
                }
            }
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Unable to retrieve/process white-listed URIs for REST API", e, logger);
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error while checking availability of tenant " + requestedTenant, e,
                    logger);
        }
    }
}

