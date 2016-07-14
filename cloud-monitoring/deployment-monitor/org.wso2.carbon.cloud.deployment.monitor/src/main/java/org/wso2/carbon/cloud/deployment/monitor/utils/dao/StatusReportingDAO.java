package org.wso2.carbon.cloud.deployment.monitor.utils.dao;

import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureRecord;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.FailureSummary;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.LiveStatus;
import org.wso2.carbon.cloud.deployment.monitor.utils.dto.SuccessRecord;

/**
 * DAO Interface
 */
public interface StatusReportingDAO {

    void addSuccessRecord(SuccessRecord successRecord);

    int addFailureRecord(FailureRecord failureRecord);

    void addFailureSummary(FailureSummary failureSummary);

    void updateLiveStatus(LiveStatus liveStatus);

}
