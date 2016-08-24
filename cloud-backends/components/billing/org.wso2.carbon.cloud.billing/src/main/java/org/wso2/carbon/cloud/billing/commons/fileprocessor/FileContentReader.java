/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.billing.commons.fileprocessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class is used to read the content of files passed.
 */
public class FileContentReader {

    private static final Log LOGGER = LogFactory.getLog(FileContentReader.class);
    private String errorMessage;

    public FileContentReader() {

    }

    /**
     * This methods reads and returns the content of the file.
     *
     * @param fileName is the name of the file to be read
     * @return the content of the file
     * @throws CloudBillingException
     */
    public String fileReader(String fileName) throws CloudBillingException {

        //variable to store the text that is being read
        String fileContent = "";
        InputStreamReader inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = new InputStreamReader(new FileInputStream(fileName), BillingConstants.ENCODING);
            reader = new BufferedReader(inputStream);
            StringBuilder stringBuilder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(BillingConstants.LINE_BREAK);
                line = reader.readLine();
            }
            fileContent = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            errorMessage = "The file " + fileName + " could not be located";
            LOGGER.error(errorMessage);
            throw new CloudBillingException(errorMessage, e);
        } catch (IOException e) {
            errorMessage = "Error occurred while reading the file " + fileName;
            LOGGER.error(errorMessage);
            throw new CloudBillingException(errorMessage, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                errorMessage = "Error occurred while closing the buffered reader for the file " + fileName;
                LOGGER.error(errorMessage);
                throw new CloudBillingException(errorMessage, e);
            }

        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("returning the " + fileName + " file contents");
        }
        return fileContent;
    }
}
