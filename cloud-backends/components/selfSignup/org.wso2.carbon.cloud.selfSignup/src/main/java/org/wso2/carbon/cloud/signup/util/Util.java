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
package org.wso2.carbon.cloud.signup.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * Consists of all the util methods
 */

public class Util implements Serializable {
    private static final Log log = LogFactory.getLog(Util.class);

    /**
     * This method generates a unique identifier for the user
     *
     * @return the unique identifier.
     */
    public String generateUUID()

    {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        return uuidString;
    }

    /**
     * This method splits the string using the forward slash.
     *
     * @param fullName
     * @return
     */
    public String splitSlashes(String fullName) {

        String[] userArray = fullName.split("/");
        String name = userArray[1];
        return name;
    }

    /**
     * This method manipulates the roles sent from the api manager configuration and returns the list of roles
     *
     * @param roleList is the list of roles assigned by the tenant to the user
     * @return the manipulated roles array
     */
    public ArrayList<String> getRoles(List<String> roleList) {
        int length = roleList.size();
        ArrayList<String> rolesArrayList = new ArrayList<String>();
        Util utilObject = new Util();

        for (int counter = 0; counter < length; counter++) {

            String item = roleList.get(counter);
            String wordToReturn = utilObject.splitSlashes(item);
            rolesArrayList.add(counter, wordToReturn);

        }

        return rolesArrayList;
    }

}
