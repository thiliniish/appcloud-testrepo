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

package org.wso2.cloud.heartbeat.monitoring.ui.utils;

/**
 * This class is used to convert values in
 */
public class StringConverter {

    /**
     *  splits the camel case value to a string and replaces them with a space
     * @param valueToReplace
     * @return
     */
    public static String splitCamelCase(String valueToReplace) {
        return valueToReplace.replaceAll(
                String.format("%s|%s|%s",
                              "(?<=[A-Z])(?=[A-Z][a-z])",
                              "(?<=[^A-Z])(?=[A-Z])",
                              "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

    /**
     * Converting underscore values to titleCase values
     * @param underscoreCaseString
     * @return
     */
    public static String underscoreToTitleCase(String underscoreCaseString){
        String[] parts = underscoreCaseString.split("_");
        StringBuilder camelCaseString = new StringBuilder();
        for (String part : parts){
            camelCaseString.append(toProperCase(part));
        }
        return camelCaseString.toString();
    }

    /**
     * Converts a string to proper case
     * @param s
     * @return
     */
    private static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
               s.substring(1).toLowerCase();
    }

}
