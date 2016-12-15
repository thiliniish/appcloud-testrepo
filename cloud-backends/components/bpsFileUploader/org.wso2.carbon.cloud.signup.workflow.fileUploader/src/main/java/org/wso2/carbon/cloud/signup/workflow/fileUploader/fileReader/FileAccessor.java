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

package org.wso2.carbon.cloud.signup.workflow.fileUploader.fileReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.constants.Constants;
import org.wso2.carbon.cloud.signup.workflow.fileUploader.utils.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * This class has all methods related to the accessing of the files
 */
public class FileAccessor {
    private static final Log log = LogFactory.getLog(FileAccessor.class);
    Utils utilObject = new Utils();
    private String errorMessage;

    /**
     * Reads the given file and returns its contents
     *
     * @param fileName is the name of the file to be read
     * @return the file content
     * @throws IOException
     */
    public String fileReader(String fileName) throws IOException {

        String oldtext = "";
        BufferedReader reader = null;
        try {

            //Reading the files contents
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileName), Constants.CHARACTER_ENCODING));
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\r\n");
            }
            oldtext = stringBuilder.toString();
            if (log.isDebugEnabled()) {
                log.debug("Returning the " + fileName + " file contents");
            }
        } catch (FileNotFoundException e) {
            errorMessage = "the file " + fileName + " was not found";
            log.error(errorMessage, e);
            throw new FileNotFoundException(errorMessage);
        } catch (IOException e) {
            errorMessage = "the file " + fileName + " could not be read";
            log.error(errorMessage, e);
            throw new IOException(errorMessage, e);
        } catch (Exception e) {
            errorMessage = "error occurred while accessing the file " + fileName;
            log.error(errorMessage, e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return oldtext;
    }

    /**
     * Replaces the text of the original file with the modified changes
     *
     * @param filepath the path where the file exists
     * @param text     the text to be overwritten with
     * @throws IOException
     */
    public void replaceFile(String filepath, String text) throws IOException {
        //FileWriter writer = new FileWriter(filepath);
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filepath), Constants.CHARACTER_ENCODING));
        try {
            log.info("Writing modified changes to the file " + filepath);
            writer.write(text);
            writer.close();

        } catch (FileNotFoundException e) {
            errorMessage = "The file " + filepath + " could not be located ";
            log.error(errorMessage, e);
            throw new FileNotFoundException(errorMessage);
        } catch (IOException e) {
            errorMessage = "the file " + filepath + " could not be read";
            log.error(errorMessage, e);
            throw new IOException(errorMessage, e);
        } catch (Exception e) {
            errorMessage = "error occurred while accessing the file " + filepath;
            log.error(errorMessage, e);
        } finally {
            writer.close();
        }

    }

    /**
     * Reverts the contents of the files to its original state.
     *
     * @param fileName is the name of the file to be reverted
     * @return if the reverting of the files was successful
     * @throws IOException
     */
    public boolean revertFiles(String fileName) throws IOException {

        if (log.isDebugEnabled()) {
            log.info("Reverting file contents of " + fileName);
        }
        boolean result = false;
        FileAccessor fileAccessor = new FileAccessor();
        try {
            String fileTxt = fileAccessor.fileReader(fileName);
            fileTxt = utilObject.revertReplaceUrls(fileTxt);
            fileAccessor.replaceFile(fileName, fileTxt);
            result = true;
        } catch (IOException e) {
            errorMessage = "The file " + fileName + " could not be read";
            log.error(errorMessage, e);
            throw new IOException(errorMessage, e);
        }
        return result;
    }
}
