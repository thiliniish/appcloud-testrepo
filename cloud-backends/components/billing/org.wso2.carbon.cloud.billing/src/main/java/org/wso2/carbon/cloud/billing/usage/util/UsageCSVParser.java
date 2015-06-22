/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.billing.usage.util;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.beans.Usage;
import org.wso2.carbon.cloud.billing.common.BillingConstants;
import org.wso2.carbon.cloud.billing.common.CloudBillingException;
import org.wso2.carbon.cloud.billing.utils.CloudBillingUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the CSV parser which is needed to create the daily usage file for zuora accounts
 */

public class UsageCSVParser {

    private static final Log log = LogFactory.getLog(UsageCSVParser.class);

    public static void writeCSVData(Usage[] usage) throws CloudBillingException {
        String csvFile = CloudBillingUtils.getBillingConfiguration().getZuoraConfig().getUsageConfig()
                .getUsageUploadFileLocation();
        CSVWriter csvWriter;
        try {
            csvWriter =
                    new CSVWriter(new FileWriter(csvFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
            List<String[]> data = toStringArray(usage);
            csvWriter.writeAll(data);
            csvWriter.close();
            log.info("Successfully created the csv file in  " + csvFile);
        } catch (IOException e) {
            String msg = "Error while creating the csv file";
            log.error(msg, e);
            throw new CloudBillingException(msg, e);
        }
    }

    private static List<String[]> toStringArray(Usage[] usage) {
        List<String[]> records = new ArrayList<String[]>();
        //add header record
        records.add(new String[]{BillingConstants.ACCOUNT_ID, BillingConstants.UOM, BillingConstants.QTY,
                                 BillingConstants.STARTDATE, BillingConstants.ENDDATE, BillingConstants.SUBSCRIPTION_ID,
                                 BillingConstants.CHARGE_ID, BillingConstants.DESCRIPTION});
        //add usage records
        for (Usage u : usage) {
            records.add(new String[]{u.getAccountId(), u.getUom(), String.valueOf(u.getQty()), u.getStartDate(),
                                     u.getEndDate(), u.getSubscriptionId(), u.getChargeId(), u.getDescription()});
        }
        return records;
    }

}