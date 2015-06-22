/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.cloud.heartbeat.monitor.core.status;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.cloud.heartbeat.monitor.core.configuration.Node;
import org.wso2.cloud.heartbeat.monitor.utils.Constants;
import org.wso2.cloud.heartbeat.monitor.utils.DbConnectionManager;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author rajith
 * @version ${Revision}
 */
public class XMLBuilder {

    private static String ROOT_ELEMENT = "CLOUD_HB_MON_XML";
    private static String SERVER_ELEMENT = "SERVER";
    private static String NAME_ATTRIB = "NAME";
    private static String HOST_ATTRIB = "HOST";
    private static String TEST_ELEMENT = "TEST";
    private static String STATUS_ATTRIB = "STATUS";
    private static String REPORTED_ATTRIB = "REPORTED";
    private static String VERSION_ATTRIB = "VERSION";

    private static String TENANT_LOGIN_TEST = "tenant_login_test";

    //query related strings
    private static String STATUS_COLMN = "STATUS";
    private static String TIMESTAMP_COLMN = "DATETIME";

    public static Document createXML(Node root) throws ParserConfigurationException,
                                                       SQLException {

        Node modules = root.findChildNodeByName(Constants.MODULES);
        String hbVersion = (root.findChildNodeByName(Constants.LIVE_STATUS))
                .getProperty(Constants.HB_VERSION);


        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root element
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement(ROOT_ELEMENT);
        rootElement.setAttribute(VERSION_ATTRIB, hbVersion);
        doc.appendChild(rootElement);

        if (modules.getChildNodes() != null) {
            List<Node> moduleNodes = modules.getChildNodes();
            for (Node node : moduleNodes) {
                String serverName = CaseConverter.underscoreToTitleCase(node.getName());
                Element server = doc.createElement(SERVER_ELEMENT);
                rootElement.appendChild(server);

                Attr attribute = doc.createAttribute(HOST_ATTRIB);
                attribute.setValue(node.getProperty(Constants.HOST_NAME));
                server.setAttributeNode(attribute);

                attribute = doc.createAttribute(NAME_ATTRIB);
                attribute.setValue(CaseConverter.splitCamelCase(serverName));
                server.setAttributeNode(attribute);

                testStatusToElement(doc, server, serverName, TENANT_LOGIN_TEST);

                if (node.getChildNodes() != null) {
                    for (Node testNode : node.getChildNodes()) {
                        testStatusToElement(doc, server, serverName, testNode.getName());
                    }
                }
            }
        }

        return doc;
    }

    private static void testStatusToElement(Document doc, Element rootElement,
                                            String serverName, String testName)
            throws SQLException {

        DbConnectionManager dbConnectionManager = DbConnectionManager.getInstance();
        Connection connection = dbConnectionManager.getConnection();

        ResultSet resultSet = DbConnectionManager.getTestStatus(connection, serverName, CaseConverter
                .underscoreToTitleCase(testName));
        if (resultSet.next()) {
            Element test = doc.createElement(TEST_ELEMENT);

            Attr attr = doc.createAttribute(NAME_ATTRIB);
            attr.setValue(testName);
            test.setAttributeNode(attr);

            attr = doc.createAttribute(STATUS_ATTRIB);
            attr.setValue(String.valueOf(resultSet.getInt(STATUS_COLMN)));
            test.setAttributeNode(attr);

            attr = doc.createAttribute(REPORTED_ATTRIB);
            attr.setValue(String.valueOf((resultSet.getTimestamp(TIMESTAMP_COLMN)).getTime()));
            test.setAttributeNode(attr);

            rootElement.appendChild(test);
        }
    }
}
