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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.cloud.tenantdeletion.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

/**
 * Class to read xml configuration files (tenant_deletion.xml) using xpath
 */
public class ConfigReader {
	private final static Log log = LogFactory.getLog(ConfigReader.class);
	private static volatile ConfigReader instance;
	private Document document = null;

	private ConfigReader() {
		String CARBON_HOME = CarbonUtils.getCarbonHome() + File.separator;
		File inputFile = new File(CARBON_HOME + "repository/conf/tenant_deletion.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			document = dBuilder.parse(inputFile);
		} catch (ParserConfigurationException e) {
			log.error("Error while creating document builder", e);
		} catch (SAXException e) {
			log.error("Error while parsing XML file", e);
		} catch (IOException e) {
			log.error("File not found error " + e);
		}
	}

	/**
	 * Returns cofig reader instance, if  instance is null creates an instance
	 * @return Config reader
	 */
	public static ConfigReader getInstance() {
		if (instance == null) {
			synchronized (ConfigReader.class) {
				if (instance == null) {
					instance = new ConfigReader();
				}
			}
		}
		return instance;
	}

	/**
	 * Reads tenant_deletion.xml and returns datasource using XML parsing
	 * @param xPath xPath to read datasource name
	 * @return datasourse name
	 */
	public String getDatasourceName(String xPath) {
		String datasourceName = null;
		try {
			document.getDocumentElement().normalize();
			XPath xPathInstance = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xPathInstance.compile(xPath).evaluate(document, XPathConstants.NODESET);
			//xPath will be reading from tenant_deletion.xml file (datasources/carbon-datasource). There is only one
			// element node as carbon-datasource. So nodeList size is one.
			if (nodeList.getLength() == 1) {
				if (nodeList.item(0).getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nodeList.item(0);
					datasourceName = eElement.getTextContent();
				}
			}
		} catch (XPathExpressionException e) {
			log.error("Xpath error occurred while compiling " + e);
		}
		return datasourceName;
	}
}