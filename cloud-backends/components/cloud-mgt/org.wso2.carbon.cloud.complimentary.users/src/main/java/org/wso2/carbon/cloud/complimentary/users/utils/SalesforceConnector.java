/*
 * Copyright (c) 2017, WSO2 Inc. All Rights Reserved.
 */

package org.wso2.carbon.cloud.complimentary.users.utils;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.complimentary.users.exception.SalesforceException;

import java.util.ArrayList;
import java.util.List;


/**
 * SalesForceConnector class to connect to Sales force.
 *
 * @since 1.0.0
 */
public class SalesforceConnector {

    private static final Log logger = LogFactory.getLog(SalesforceConnector.class);
    private PartnerConnection conn = null;

    /**
     * This method is used to login to Salesforce.
     *
     * @param username Sales force username
     * @param password Sales force password for the username
     * @param token    Obtained from salesforce
     * @throws SalesforceException Error thrown when login fails
     */
    public void login(String username, String password, String token) throws SalesforceException {
        conn = getConnection(username, password, token);
        logger.debug("User: " + username + " successfully logged into salesforce.");
    }

    /**
     * This method is used to logout of Salesforce.
     */
    public void logout() {
        try {
            conn.logout();
        } catch (ConnectionException e) {
            logger.error("Failed to log out in Salesforce.", e);
        }
    }

    /**
     * This method is used to login and get a connection to Salesforce.
     *
     * @param username Sales force username
     * @param password Sales force password for the username
     * @param token    Obtained from salesforce
     * @return PartnerConnection Object
     * @throws SalesforceException
     */
    private PartnerConnection getConnection(String username, String password, String token) throws SalesforceException {
        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(username);
        config.setPassword(password.concat(token));
        try {
            return Connector.newConnection(config);
        } catch (ConnectionException e) {
            String msg = "Failed to get a connection, please check the user credentials.";
            throw new SalesforceException(msg, e);
        }
    }

    /**
     * This method is used to retrieve the list of opportunities for the JIRAs from Salesforce.
     *
     * @param jiraKeys List of Jira keys
     * @return returns a list of opportunities
     * @throws SalesforceException
     */
    public List<String> getOpportunitiesByJira(List<String> jiraKeys) throws SalesforceException {

        if (jiraKeys == null) {
            throw new SalesforceException("Jira Key cannot be empty!");
        }
        try {
            String searchQuery = getSearchQuery(jiraKeys);

            QueryResult queryResult = conn.query("SELECT Opportunity_Name__c FROM Support_Account__c WHERE " +
                    "JIRA_Key__c in (" + searchQuery + ")");
            if (queryResult != null && queryResult.getSize() > 0 && queryResult.getRecords() != null && queryResult
                    .getRecords().length > 0) {
                SObject[] records = queryResult.getRecords();
                List<String> opportunityIds = new ArrayList<>();
                for (int i = 0; i < records.length; i++) {
                    String opportunityId = records[i].getField("Opportunity_Name__c").toString();
                    opportunityIds.add(opportunityId);
                }
                return opportunityIds;
            } else {
                return new ArrayList<>();
            }
        } catch (ConnectionException e) {
            String msg = "Error connecting to Salesforce API: Get Opportunities By Jira!";
            throw new SalesforceException(msg, e);
        }
    }

    /**
     * This method is used to get the list of products with production support for the opportunity ids from Salesforce.
     *
     * @param opportunityIds List of opportunity ids
     * @return List of products with production support
     * @throws SalesforceException
     */
    public List<String> getProductsByOpportunitiesIds(List<String> opportunityIds) throws SalesforceException {

        String searchQuery = getSearchQuery(opportunityIds);
        if (searchQuery == null) {
            return new ArrayList<>();
        }
        try {
            QueryResult queryResult = conn.query("SELECT Family,Id,Name,ProductCode FROM Product2 WHERE Id in (" +
                    "SELECT Product2Id FROM OpportunityLineItem WHERE OpportunityId IN (" + searchQuery + "))");
            if (queryResult != null && queryResult.getSize() > 0 && queryResult.getRecords() != null && queryResult
                    .getRecords().length > 0) {
                SObject[] records = queryResult.getRecords();
                List<String> products = new ArrayList<>();
                for (int i = 0; i < records.length; i++) {
                    String productCode = records[i].getField("ProductCode").toString();
                    products.add(productCode);
                }
                return products;
            } else {
                return new ArrayList<>();
            }
        } catch (ConnectionException e) {
            String msg = "Error connecting to Salesforce API: Get Products by OpportunitiesIds!";
            throw new SalesforceException(msg, e);
        }
    }

    public boolean hasClaimedComplimentarySubscription(List<String> opportunityIds) throws SalesforceException {
        String searchQuery = getSearchQuery(opportunityIds);
        if (searchQuery == null) {
            return false;
        }
        try {
            QueryResult queryResult = conn.query("SELECT Id,Name,Complementary_Cloud_Subscriber__c FROM Account " +
                    "WHERE Id IN (SELECT AccountId FROM Opportunity WHERE Id IN (" + searchQuery + "))");
            if (queryResult != null && queryResult.getSize() > 0 && queryResult.getRecords() != null && queryResult
                    .getRecords().length > 0) {
                SObject[] records = queryResult.getRecords();
                return Boolean.parseBoolean(records[0].getField("Complementary_Cloud_Subscriber__c").toString());
            }
        } catch (ConnectionException e) {
            String msg = "Error connecting to Salesforce API: Get Products by OpportunitiesIds!";
            throw new SalesforceException(msg, e);
        }

        return false;
    }

    public String getContactWhoClaimedSubscription(List<String> opportunityIds) throws SalesforceException {
        String searchQuery = getSearchQuery(opportunityIds);
        if (searchQuery == null) {
            return null;
        }
        try {
            QueryResult queryResult = conn.query("SELECT Email,Complementary_Cloud_Subscriber__c FROM Contact WHERE" +
                    " AccountId IN (SELECT AccountId FROM Opportunity WHERE Id IN (" + searchQuery + "))");
            if (queryResult != null && queryResult.getSize() > 0 && queryResult.getRecords() != null && queryResult
                    .getRecords().length > 0) {
                SObject[] records = queryResult.getRecords();
                for (int i = 0; i < records.length; i++) {
                    boolean hasClaimed = Boolean.parseBoolean(records[i].
                            getField("Complementary_Cloud_Subscriber__c").toString());
                    if (hasClaimed) {
                        return records[i].getField("Email").toString();
                    }
                }
            }
        } catch (ConnectionException e) {
            String msg = "Error connecting to Salesforce API: Get Products by OpportunitiesIds!";
            throw new SalesforceException(msg, e);
        }

        return null;
    }

    private String getSearchQuery(List<String> searchParamsList) {
        if (searchParamsList == null || searchParamsList.size() <= 0) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        String id;
        for (int i = 0; i < searchParamsList.size(); ++i) {
            id = "'" + searchParamsList.get(i) + "'";
            if (i == searchParamsList.size() - 1) {
                buffer.append(id);
            } else {
                buffer.append(id + ", ");
            }
        }
        return buffer.toString();
    }

    public String getAccountId(List<String> opportunityIds) throws SalesforceException {
        String searchQuery = getSearchQuery(opportunityIds);
        if (searchQuery == null) {
            return null;
        }
        try {
            QueryResult queryResult = conn.query("SELECT AccountId FROM Opportunity WHERE Id IN (" + searchQuery + ")");
            if (queryResult != null && queryResult.getSize() > 0 && queryResult.getRecords() != null && queryResult
                    .getRecords().length > 0) {
                SObject[] records = queryResult.getRecords();
                return records[0].getSObjectField("AccountId").toString();
            }
        } catch (ConnectionException e) {
            String msg = "Error connecting to Salesforce API: Get Products by OpportunitiesIds!";
            throw new SalesforceException(msg, e);
        }

        return null;
    }

    public String getContactId(List<String> opportunityIds, String email) throws SalesforceException {
        String searchQuery = getSearchQuery(opportunityIds);
        if (searchQuery == null || email == null) {
            return null;
        }
        try {
            QueryResult queryResult = conn.query("SELECT Id,Email FROM Contact WHERE AccountId IN " +
                    "(SELECT AccountId FROM Opportunity WHERE Id IN (" + searchQuery + "))");
            if (queryResult != null && queryResult.getSize() > 0 && queryResult.getRecords() != null && queryResult
                    .getRecords().length > 0) {
                SObject[] records = queryResult.getRecords();
                for (int i = 0; i < records.length; i++) {
                    String contactEmail;
                    if (records[i].getSObjectField("Email") != null) {
                        contactEmail = records[i].getSObjectField("Email").toString();
                        if (email.equals(contactEmail)) {
                            return records[i].getSObjectField("Id").toString();
                        }
                    }
                }
            }
        } catch (ConnectionException e) {
            String msg = "Error connecting to Salesforce API: Get Products by OpportunitiesIds!";
            throw new SalesforceException(msg, e);
        }
        return null;
    }

    public void updateSObject(String sObjectType, String sObjectId, boolean complimentaryFieldValue) {
        try {
            SObject updatedSObject = new SObject();
            updatedSObject.setType(sObjectType);
            updatedSObject.setId(sObjectId);
            updatedSObject.setField("Complementary_Cloud_Subscriber__c", complimentaryFieldValue);
            SaveResult[] saveResults = conn.update(new SObject[]{updatedSObject});

            for (int j = 0; j < saveResults.length; j++) {
                if (saveResults[j].isSuccess()) {
                    logger.info("Item with an ID of " + saveResults[j].getId() + " was updated.");
                } else {
                    for (int i = 0; i < saveResults[j].getErrors().length; i++) {
                        Error err = saveResults[j].getErrors()[i];
                        logger.error("Errors were found on item " + j);
                        logger.error("Error code: " + err.getStatusCode().toString());
                        logger.error("Error message: " + err.getMessage());
                    }
                }
            }
        } catch (ConnectionException e) {
            logger.error(e.getMessage());
        }
    }

}
