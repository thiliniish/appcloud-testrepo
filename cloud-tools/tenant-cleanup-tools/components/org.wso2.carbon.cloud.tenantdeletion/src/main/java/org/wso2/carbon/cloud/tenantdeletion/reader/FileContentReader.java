/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * This class reads the files needed for the workflow process.
 */

public class FileContentReader implements Serializable {

    private static final Log LOG = LogFactory.getLog(FileContentReader.class);
    private static final long serialVersionUID = 1L;

    /**
     * This methods reads and returns the content of the file.
     *
     * @param fileName is the name of the file to be read
     * @return the content of the file
     */
    public String fileReader(String fileName) {

        //variable to store the text that is being read
        StringBuilder fileContent = new StringBuilder();
        InputStreamReader inputStream = null;
        BufferedReader reader = null;
        String errorMessage;
        try {
            inputStream = new InputStreamReader(new FileInputStream(fileName), Charset.defaultCharset());
            reader = new BufferedReader(inputStream);
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line);
                fileContent.append("\r\n");
            }
            reader.close();
        } catch (FileNotFoundException e) {
            errorMessage = "The file " + fileName + " could not be located";
            LOG.error(errorMessage, e);
        } catch (IOException e) {
            errorMessage = "Error occurred while reading the file " + fileName;
            LOG.error(errorMessage, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    errorMessage = "Error occurred while reading the file " + fileName;
                    LOG.error(errorMessage, e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    errorMessage = "Error occurred while reading the file " + fileName;
                    LOG.error(errorMessage, e);
                }
            }
        }
        //returning the file contents
        return fileContent.toString();
    }
}
