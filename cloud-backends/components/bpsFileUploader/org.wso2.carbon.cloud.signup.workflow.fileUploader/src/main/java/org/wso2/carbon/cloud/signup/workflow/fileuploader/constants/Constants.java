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

package org.wso2.carbon.cloud.signup.workflow.fileuploader.constants;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * This class consists of the constants for the file uploader component.
 */
public class Constants {

    public static final String CONFIG_FILE_NAME = "bpsConfigFile.json";
    public static final String HT_UPLOADER_SERVICE_NAME = "HumanTaskUploader";
    public static final String BPEL_UPLOADER_SERVICE_NAME = "BPELUploader";
    public static final int DELAY_TIME = 15000;
    public static final String CARBON_HOME = CarbonUtils.getCarbonHome() + File.separator;
    public static final String CHARACTER_ENCODING = "UTF-8";

}
