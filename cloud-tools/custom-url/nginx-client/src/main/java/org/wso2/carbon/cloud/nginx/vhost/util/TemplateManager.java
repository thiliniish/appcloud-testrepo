/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.nginx.vhost.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.nginx.vhost.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Template reader manage the vhost templates
 */
public class TemplateManager {
	private static final Log log = LogFactory.getLog(TemplateManager.class);
	private Map<String, String> templateMap;

	public TemplateManager() throws IOException {
		log.info("Initializing TemplateManager.");
		this.templateMap = new HashMap<String, String>();
		//Setting api-store template
		templateMap
				.put(Constants.API_STORE_TEMPLATE_NAME, getTemplateContent(Constants.VHOST_STORE_TEMPLATE_FILE_PATH));
		//Setting api-gateway(http) template
		templateMap.put(Constants.HTTP_API_GATEWAY_TEMPLATE_NAME,
		                this.getTemplateContent(Constants.VHOST_GATEWAY_HTTP_TEMPLATE_FILE_PATH));
		//Setting api-gateway(https) template
		templateMap.put(Constants.HTTPS_API_GATEWAY_TEMPLATE_NAME,
		                this.getTemplateContent(Constants.VHOST_GATEWAY_HTTPS_TEMPLATE_FILE_PATH));
	}

	/**
	 * Get template content from the appropriate file.
	 *
	 * @param templateFilePath File path of the template.
	 * @return Template content of the file.
	 * @throws IOException
	 */
	private String getTemplateContent(String templateFilePath) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(templateFilePath));
			String currentLine;

			while ((currentLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(currentLine);
				stringBuilder.append("\n");
			}
		} catch (IOException ex) {
			String errorMessage = "Error occurred while reading template : " + templateFilePath;
			log.error(errorMessage, ex);
			throw new IOException(errorMessage, ex);
		}

		return stringBuilder.toString();
	}

	/**
	 *
	 * @param templateName
	 * @return
	 */
	public String getTemplate(String templateName) {
		return templateMap.get(templateName);
	}
}
