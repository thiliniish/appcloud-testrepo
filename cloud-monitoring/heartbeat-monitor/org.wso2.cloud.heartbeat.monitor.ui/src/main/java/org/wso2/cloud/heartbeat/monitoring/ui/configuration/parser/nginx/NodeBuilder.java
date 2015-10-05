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

package org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx;

import org.wso2.cloud.heartbeat.monitoring.ui.configuration.Node;
import org.wso2.cloud.heartbeat.monitoring.ui.configuration.parser.nginx.utils.Constants;
import org.wso2.cloud.heartbeat.monitoring.ui.utils.HeartbeatExceptions;

public class NodeBuilder {

    /**
     * @param aNode   Node object whose name set.
     * @param content should be something similar to following.
     *                <p/>
     *                abc d;
     *                efg h;
     *                # comment
     *                ij { # comment
     *                klm n;
     *                <p/>
     *                pq {
     *                rst u;
     *                }
     *                }
     * @return fully constructed Node
     */
    public static Node buildNode(Node aNode, String content) throws HeartbeatExceptions {

        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // avoid line comments
            if (!line.startsWith(Constants.NGINX_COMMENT)) {

                // skip comments in-line
                if (line.contains(Constants.NGINX_COMMENT)) {
                    line = line.substring(0, line.indexOf(Constants.NGINX_COMMENT));
                }

                // another node is detected and it is not a variable starting from $
                if (line.contains(Constants.NGINX_NODE_START_BRACE) &&
                    !line.contains(Constants.NGINX_VARIABLE)) {

                    try {
                        Node childNode = new Node();
                        childNode.setName(
                                line.substring(0, line.indexOf(Constants.NGINX_NODE_START_BRACE))
                                    .trim());

                        StringBuilder sb = new StringBuilder();

                        int matchingBraceTracker = 1;

                        while (!line.contains(Constants.NGINX_NODE_END_BRACE) ||
                               matchingBraceTracker != 0) {
                            i++;
                            if (i == lines.length) {
                                break;
                            }
                            line = lines[i];
                            if (line.contains(Constants.NGINX_NODE_START_BRACE)) {
                                matchingBraceTracker++;
                            }
                            if (line.contains(Constants.NGINX_NODE_END_BRACE)) {
                                matchingBraceTracker--;
                            }
                            sb.append(line + "\n");
                        }

                        childNode = buildNode(childNode, sb.toString());
                        aNode.appendChild(childNode);

                    } catch (Exception e) {
                        String msg =
                                "Heartbeat - Monitor - Malformatted element is defined in the configuration file. [" +
                                i + "] \n";
                        throw new HeartbeatExceptions(msg + line, e);
                    }

                }
                // this is a property
                else {
                    if (!line.isEmpty() && !Constants.NGINX_NODE_END_BRACE.equals(line)) {
                        String[] prop = line.split(Constants.NGINX_SPACE_REGEX);
                        String value = line.substring(prop[0].length(),
                                                      line.indexOf(Constants.NGINX_LINE_DELIMITER))
                                           .trim();
                        try {
                            aNode.addProperty(prop[0], value);
                        } catch (Exception e) {
                            String msg =
                                    "Heartbeat - Monitor - Malformatted property is defined in the configuration file. [" +
                                    i + "] \n";
                            throw new HeartbeatExceptions(msg + line, e);
                        }
                    }
                }

            }
        }

        return aNode;

    }
}
