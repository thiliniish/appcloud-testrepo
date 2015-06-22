package org.wso2.carbon.cloudmgt.bam.integration;

import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloudmgt.common.CloudMgtException;
import org.wso2.carbon.cloudmgt.users.service.UserManagementException;
import org.wso2.carbon.cloudmgt.users.util.AppFactoryConfiguration;
import org.wso2.carbon.cloudmgt.users.util.UserMgtUtil;

public class BamDataPublisher {

	private String TENANT_USER_STREAM = "org.wso2.carbon.cloud.tenantUser";
	private String APP_USER_STREAM = "org.wso2.carbon.cloud.appUser";

	private String TENANT_USER_STREAM_VERSION = "1.0.0";
	private String APP_USER_STREAM_VERSION = "1.0.0";
	private boolean ENABLE_DATA_PUBLISHING;

	private AsyncDataPublisher asyncDataPublisher;

	private String tenantUserStream = "{" + " 'name': '" + TENANT_USER_STREAM
			+ "'," + " 'version': '" + TENANT_USER_STREAM_VERSION + "',"
			+ " 'nickName': 'Tenant User Information',"
			+ " 'description': 'This stream will store tenant users to BAM',"
			+ "   'payloadData':["
			+ "    {'name':'tenantId', 'type':'string'},"
			+ "    {'name':'user',  'type':'string' },"
			+ "    {'name':'action', 'type':'string'},"
			+ "    {'name':'timeStamp','type':'double'}" + "    ]" + "    }";

	private String appUserStream = "{" + " 'name': '" + APP_USER_STREAM + "',"
			+ " 'version': '" + APP_USER_STREAM_VERSION + "',"
			+ " 'nickName': 'Application User Information',"
			+ " 'description': 'This stream will store app users to BAM',"
			+ "   'payloadData':["
			+ "    {'name':'applicationName','type':'string'},"
			+ "    {'name':'applicationKey','type':'string'},"
			+ "    {'name':'timeStamp','type':'double'},"
			+ "    {'name':'tenantId', 'type':'string'},"
			+ "    {'name':'action', 'type':'string'},"
			+ "    {'name':'user',  'type':'string' }" + "    ]" + "    }";

	private static Log log = LogFactory.getLog(BamDataPublisher.class);

	public BamDataPublisher() {
		try {
			AppFactoryConfiguration config;

			config = UserMgtUtil.getConfiguration();

			String EnableStatPublishing = config
					.getFirstProperty("BAM.EnableStatPublishing");
			String bamServerURLS = config.getFirstProperty("BAM.BAMServerURL");

			// Check for multiple URLS separated by ","
			String bamServerURL[] = null;
			if (bamServerURLS != null) {
				bamServerURL = bamServerURLS.split(",");
			}

			String bamServerUserName = config
					.getFirstProperty("BAM.BAMUserName");
			String bamServerPassword = config
					.getFirstProperty("BAM.BAMPassword");

			if (EnableStatPublishing != null
					&& EnableStatPublishing.equals("true")) {
				ENABLE_DATA_PUBLISHING = true;

				if (bamServerURL == null || bamServerURLS.length() <= 0) {
					throw new CloudMgtException("Can not find BAM Server URL");
				} else {
					for (String url : bamServerURL) {
						asyncDataPublisher = new AsyncDataPublisher(url,
								bamServerUserName, bamServerPassword);

					}
				}

			}

		} catch (CloudMgtException e) {
			String errorMsg = "Unable to create Data publisher "
					+ e.getMessage();
			log.error(errorMsg, e);
		} catch (UserManagementException e) {
			String errorMsg = "Unable to create Data publisher "
					+ e.getMessage();
			log.error(errorMsg, e);
		}
	}

	public void PublishTenantUserUpdateEvent(String tenantId, String username,
			String action, double timestamp) throws CloudMgtException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();
		if (!asyncDataPublisher.isStreamDefinitionAdded(TENANT_USER_STREAM,
				TENANT_USER_STREAM_VERSION)) {

			asyncDataPublisher.addStreamDefinition(tenantUserStream,
					TENANT_USER_STREAM, TENANT_USER_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { tenantId, username, action,
				timestamp });

		try {

			publishEvents(event, TENANT_USER_STREAM, TENANT_USER_STREAM_VERSION);

		} catch (AgentException e) {
			String msg = "Failed to publish tenant user update event";
			log.error(msg, e);
			throw new CloudMgtException(msg, e);
		} catch (InterruptedException e) {
			String msg = "Failed to publish tenant user update event";
			log.error(msg, e);
			throw new CloudMgtException(msg, e);
		}

	}

	public void PublishUserUpdateEvent(String appName, String appKey,
			double timestamp, String tenantId, String action, String username)
			throws CloudMgtException {

		if (!ENABLE_DATA_PUBLISHING) {
			return;
		}

		Event event = new Event();
		if (!asyncDataPublisher.isStreamDefinitionAdded(APP_USER_STREAM,
				APP_USER_STREAM_VERSION)) {

			asyncDataPublisher.addStreamDefinition(appUserStream,
					APP_USER_STREAM, APP_USER_STREAM_VERSION);
		}

		event.setTimeStamp(System.currentTimeMillis());
		event.setMetaData(null);
		event.setCorrelationData(null);
		event.setPayloadData(new Object[] { appName, appKey, timestamp,
				tenantId, username, action });

		try {

			publishEvents(event, APP_USER_STREAM, APP_USER_STREAM_VERSION);

		} catch (AgentException e) {
			String msg = "Failed to publish app user update event";
			log.error(msg, e);
			throw new CloudMgtException(msg, e);
		} catch (InterruptedException e) {
			String msg = "Failed to publish app user update event";
			log.error(msg, e);
			throw new CloudMgtException(msg, e);
		}

	}

	public void publishEvents(Event event, String Stream, String version)
			throws AgentException, InterruptedException {

		asyncDataPublisher.publish(Stream, version, event);
		asyncDataPublisher.stop();

	}

}
