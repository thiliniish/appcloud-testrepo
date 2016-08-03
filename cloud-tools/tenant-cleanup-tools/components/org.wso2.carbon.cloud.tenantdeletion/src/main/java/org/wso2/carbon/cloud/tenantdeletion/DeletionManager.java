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

package org.wso2.carbon.cloud.tenantdeletion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.cloud.tenantdeletion.beans.DeleteJob;
import org.wso2.carbon.cloud.tenantdeletion.constants.DeletionConstants;
import org.wso2.carbon.cloud.tenantdeletion.emails.EmailManager;
import org.wso2.carbon.cloud.tenantdeletion.service.UpdateTenants;
import org.wso2.carbon.cloud.tenantdeletion.timer.DeletionCompleteTimer;
import org.wso2.carbon.cloud.tenantdeletion.timer.DeletionStartFlagTimer;
import org.wso2.carbon.cloud.tenantdeletion.timer.DependencyCheckTimer;
import org.wso2.carbon.cloud.tenantdeletion.timer.FindCoordinatorTimer;
import org.wso2.carbon.cloud.tenantdeletion.utils.DataAccessManager;
import org.wso2.carbon.cloud.tenantdeletion.utils.TenantDeletionMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Represents class for deletion flag listener.
 */
public class DeletionManager {

    private static final Log LOG = LogFactory.getLog(DeletionManager.class);
    private static final DeletionManager instance = new DeletionManager();
    private Timer timer;
    private List<DeleteJob> deleteObjectList;
    private String deletionLimit;
    private String serverKey;

    /**
     * Creates Deletion Manager Instance
     */
    private DeletionManager() {

    }

    /**
     * Returns DeletionManager instance.
     *
     * @return StartDelete instance
     */
    public static DeletionManager getInstance() {
        return instance;
    }

    /**
     * Gets the server map and sets the object list to be deleted and set the dependency timer to check dependency
     * flags
     */
    public void startDeletion() {
        //Coordinator timer is closed since this method invokes after finding the coordinator
        timer.cancel();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deletion process started");
        }
        UpdateTenants updateTenants = new UpdateTenants();
        //Even if some tenants like "wso2con" doesn't log in frequently we can't delete them because we'll use this tenant during the Con
        updateTenants.removeExclusionListFromDeleteList();
        //Even if paid tenants don't log in frequently we can't delete them
        updateTenants.removePaidUsersFromDeleteList();

        serverKey = ServerConfiguration.getInstance().getFirstProperty(DeletionConstants.SERVER_KEY);
        Map<String, List<DeleteJob>> serverHashMap = TenantDeletionMap.getInstance().getServerMap();
        if (serverHashMap.isEmpty()) {
            LOG.debug("Server map is empty.");
        }
        if (serverHashMap.containsKey(serverKey)) {
            //Gets delete objects from the map
            deleteObjectList = serverHashMap.get(serverKey);
            timer = new Timer();
            timer.schedule(new DependencyCheckTimer(), 0, DeletionConstants.DEPENDENCY_CHECK_WAITING_TIME);
        }
    }

    /**
     * Trigger deletion process after the Dependencies are met
     */
    public void delete() {
        timer.cancel();
        startDeletionProcess();

        //After Finishing loop, timer task will be started again if deleteObject list is not empty
        if (!deleteObjectList.isEmpty()) {
            timer = new Timer();
            timer.schedule(new DependencyCheckTimer(), DeletionConstants.DEPENDENCY_CHECK_WAITING_TIME);
        } else {
            timer = new Timer();
            timer.schedule(new DeletionCompleteTimer(), 0, DeletionConstants.DEPENDENCY_CHECK_COMPLETE_TIME);
        }
    }

    /**
     * Start the Deletion process
     */
    private void startDeletionProcess() {
        DataAccessManager dataAccessManager = DataAccessManager.getInstance();
        int counter = 0;
        while (!deleteObjectList.isEmpty()) {
            DeleteJob deleteEntity = deleteObjectList.get(counter);
            String[] dependencyTypes = deleteEntity.getDependency().split(DeletionConstants.SEPARATOR);
            //if there are no dependencies, deletion process can be started
            if (dependencyTypes[0].isEmpty() && dependencyTypes.length == 1) {
                invokeDeleteMethod(deleteEntity);
                deleteObjectList.remove(counter);
                continue;
            }
            for (int i = 0; i < dependencyTypes.length; i++) {
                if (dependencyTypes[i].isEmpty()) {
                    String errorMessage = "Error in Tenant_Deletion.xml. Dependency tags contain null values (,,)";
                    LOG.error(errorMessage);
                    sendErrorEmail(deleteEntity, errorMessage, serverKey);
                    continue;
                }
                //Dependency check. for example all api should be deleted to start deletion of APIs.
                boolean isDependencySatisfied = dataAccessManager.getDeletionStatus(dependencyTypes[i]);
                if (!(isDependencySatisfied)) {
                    break;
                }
                //start deletion if all dependencies are satisfied
                if (i == dependencyTypes.length - 1) {
                    invokeDeleteMethod(deleteEntity);
                    deleteObjectList.remove(counter);
                    counter--;
                }
            }
            // Loop will be terminated if all delete objects were tried to delete
            if (deleteObjectList.size() == (counter + 1)) {
                break;
            }
            //increment counter by one
            counter++;
        }
    }

    /**
     * Loads the class object associated with the class with the given string name and invoke delete method
     *
     * @param deleteObject delete object
     */
    private void invokeDeleteMethod(DeleteJob deleteObject) {
        try {
            Class<?> aClass = Class.forName(deleteObject.getClassName());
            Object obj = aClass.newInstance();
            Method method = aClass.getDeclaredMethod(DeletionConstants.DELETE, String.class);
            DataAccessManager dataAccessManager = DataAccessManager.getInstance();
            int limit = dataAccessManager.getDeletionLimit();
            if (limit != 0) {
                deletionLimit = Integer.toString(limit);
            }
            method.invoke(obj, deletionLimit);
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Error occurred while initiating object", e);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            String errorMessage =
                    "No Class name found as " + deleteObject.getClassName() + " or method name was not found";
            sendErrorEmail(deleteObject, e.toString(), serverKey);
            LOG.error(errorMessage, e);
        }
    }

    /**
     * Schedules conf timer to check the tenant deletion start flag
     */
    public void setStartFlagTimer() {
        timer = new Timer();
        timer.schedule(new DeletionStartFlagTimer(), 0, DeletionConstants.DELETION_START_WAITING_TIME);
    }

    /**
     * Schedules conf timer task to find the coordinator of the cluster
     */
    public void isCoordinator() {
        //StartFlag timer is closed since this method invokes after the start deletion flag is raised
        timer.cancel();
        timer = new Timer();
        timer.schedule(new FindCoordinatorTimer(), 0, DeletionConstants.COORDINATOR_CHECK_WAITING_TIME);
    }

    /**
     * Resets deletion flags to indicate deletion is finished and triggers the email notification
     */
    public void finishDeletionProcess(Map<String, Integer> tenantMap) {
        //Reset deletion flags to zero
        DataAccessManager.getInstance().resetDatabaseFlags();
        EmailManager emailManager = new EmailManager();
        //Send deletion report as an email
        emailManager.configureDeletionCompleteEmail(tenantMap);
        resetStartFlagTimer();

    }

    /**
     * If the deletion is completed due to all tenant already been deleted database flags will be
     * resetted without tiggering the email notifications
     */
    public void finishDeletionWithoutEmail() {
        //Reset deletion flags to zero
        LOG.info("Deletion process completed. Not sending email since no tenants were deleted");
        DataAccessManager.getInstance().resetDatabaseFlags();
        resetStartFlagTimer();
    }

    /**
     * Reset timer by scheduling the timer to check deletion flag
     */
    public void resetStartFlagTimer() {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new DeletionStartFlagTimer(), 0, DeletionConstants.DELETION_START_WAITING_TIME);
    }

    /**
     * Sends an email regarding the error occurred while tenant deletion process
     *
     * @param deleteObject delete object
     */
    public void sendErrorEmail(DeleteJob deleteObject, String error, String serverKey) {
        EmailManager emailManager = new EmailManager();
        emailManager.configureDeletionErrorEmail(error, deleteObject, serverKey);
    }

    /**
     * Stops current timer
     */
    public void stopTimer() {
        timer.cancel();
    }
}