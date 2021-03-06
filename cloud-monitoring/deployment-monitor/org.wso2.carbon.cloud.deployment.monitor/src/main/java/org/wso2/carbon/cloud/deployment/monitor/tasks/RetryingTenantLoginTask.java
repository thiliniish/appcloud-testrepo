/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.deployment.monitor.tasks;

import org.wso2.deployment.monitor.api.DeploymentMonitorTask;
import org.wso2.deployment.monitor.api.RunStatus;
import org.wso2.deployment.monitor.core.model.ServerGroup;
import org.wso2.deployment.monitor.impl.task.TenantLoginTask;

import java.util.Properties;

/**
 * Retrying Implementation for {@link TenantLoginTask}
 */
public class RetryingTenantLoginTask extends AbstractRetryingTask implements DeploymentMonitorTask {

    @Override public RunStatus runTask(ServerGroup serverGroup, Properties properties) {
        TenantLoginTask tenantLoginTask = new TenantLoginTask();
        RunStatus status = tenantLoginTask.execute(serverGroup, properties);
        if (status.isSuccess()) {
            return status;
        } else {
            return retryTask(serverGroup, properties, "Tenant Login Task Failed for : " + status.getServerGroupName(),
                    status);
        }
    }

    @Override public RunStatus execute(ServerGroup serverGroup, Properties customParams) {
        return runTask(serverGroup, customParams);
    }
}
