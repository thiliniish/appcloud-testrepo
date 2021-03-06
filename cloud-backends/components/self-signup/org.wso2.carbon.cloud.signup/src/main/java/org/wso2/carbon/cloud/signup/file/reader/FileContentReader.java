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
package org.wso2.carbon.cloud.signup.file.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.cloud.signup.constants.SignUpWorkflowConstants;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 * This class reads the files needed for the workflow process.
 */

public class FileContentReader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FileContentReader.class);
    private String errorMessage;

    public FileContentReader() {

    }

    /**
     * This methods reads and returns the content of the file.
     *
     * @param fileName is the name of the file to be read
     * @return the content of the file
     * @throws WorkflowException
     */
    public String fileReader(String fileName) throws WorkflowException {

        log.info("Reading the file " + fileName);
        //variable to store the text that is being read
        String fileContent = "";
        InputStreamReader inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream =  new InputStreamReader(new FileInputStream(fileName),
                                                                         SignUpWorkflowConstants.DEFAULT_ENCODING);
            reader = new BufferedReader(inputStream);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\r\n");
            }
            fileContent = stringBuilder.toString();
        } catch (FileNotFoundException e) {
            errorMessage = "The file " + fileName + " could not be located";
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } catch (IOException e) {
            errorMessage = "Error occurred while reading the file " + fileName;
            log.error(errorMessage, e);
            throw new WorkflowException(errorMessage, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                errorMessage = "Error occurred while reading the file " + fileName;
                log.error(errorMessage, e);
                throw new WorkflowException(errorMessage, e);
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                errorMessage = "Error occurred while reading the file " + fileName;
                log.error(errorMessage, e);
                throw new WorkflowException(errorMessage, e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Returning the " + fileName + " file contents");
        }
        return fileContent;
    }
}
