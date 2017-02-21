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

    private static final Log log = LogFactory.getLog(SalesforceConnector.class);
    private PartnerConnection conn = null;

    /**
     * Method to login to Salesforce.
     *
     * @param username Sales force username
     * @param password Sales force password for the username
     * @param token    Obtained from salesforce
     * @throws SalesforceException Error thrown when login fails
     */
    public void login(String username, String password, String token) throws SalesforceException {
        conn = getConnection(username, password, token);
        log.debug("User: " + username + " successfully logged into salesforce.");
    }

    /**
     * Method to logout of Salesforce.
     */
    public void logout() {
        try {
            conn.logout();
        } catch (ConnectionException e) {
            log.error("Failed to log out from Salesforce.", e);
        }
    }

    /**
     * Method to login and get a connection to Salesforce.
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
            String msg = "Failed to get a connection. Please check the user credentials.";
            throw new SalesforceException(msg, e);
        }
    }

    /**
     * Method to retrieve the list of opportunities for the JIRAs from Salesforce.
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
                    String opportunityId = records[i].getField(Constants.OPPORTUNITY_NAME_CUSTOM_FIELD).toString();
                    opportunityIds.add(opportunityId);
                }
                return opportunityIds;
            } else {
                return new ArrayList<>();
            }
        } catch (ConnectionException e) {
            String msg = "Error while getting opportunities by Jira.";
            throw new SalesforceException(msg, e);
        }
    }

    /**
     * Method to get the list of products with production support for the opportunity ids from Salesforce.
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
                    String productCode = records[i].getField(Constants.PRODUCT_CODE_FIELD).toString();
                    products.add(productCode);
                }
                return products;
            } else {
                return new ArrayList<>();
            }
        } catch (ConnectionException e) {
            String msg = "Error while getting products by opportunity Ids.";
            throw new SalesforceException(msg, e);
        }
    }

    /**
     * Metod to check if the complimentary account has been claimed.
     *
     * @param opportunityIds List of opportunity ids
     * @return Whether the complimentary account was claimed or not
     * @throws SalesforceException
     */
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
                return Boolean.parseBoolean(records[0].getField(Constants.COMPLIMENTARY_CLOUD_SUBSCRIBER_CUSTOM_FIELD).
                        toString());
            }
        } catch (ConnectionException e) {
            String msg = "Error while getting if the complimentary account has been claimed.";
            throw new SalesforceException(msg, e);
        }

        return false;
    }

    /**
     * Method to get email of contact who claimed the complimentary subscription.
     *
     * @param opportunityIds List of opportunity ids
     * @return Email of contact who claimed the subscription
     * @throws SalesforceException
     */
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
                            getField(Constants.COMPLIMENTARY_CLOUD_SUBSCRIBER_CUSTOM_FIELD).toString());
                    if (hasClaimed) {
                        return records[i].getField("Email").toString();
                    }
                }
            }
        } catch (ConnectionException e) {
            String msg = "Error while getting Id of contact who claimed subscription.";
            throw new SalesforceException(msg, e);
        }

        return null;
    }

    /**
     * Method to get the search query given the search parameters list.
     *
     * @param searchParamsList List of search parameters
     * @return Search query
     */
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

    /**
     * Method to get Id of account related to the list of opportunities.
     *
     * @param opportunityIds List of opportunity ids
     * @return Id of account
     * @throws SalesforceException
     */
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
                return records[0].getSObjectField(Constants.ACCOUNT_ID_FIELD).toString();
            }
        } catch (ConnectionException e) {
            String msg = "Error while getting account Id for list of opportunity Ids.";
            throw new SalesforceException(msg, e);
        }

        return null;
    }

    /**
     * Method to get Id of contact given the email.
     *
     * @param opportunityIds List of opportunity ids
     * @param email          Email address
     * @return Id of contact
     * @throws SalesforceException
     */
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
                    if (records[i].getSObjectField(Constants.EMAIL_FIELD) != null) {
                        contactEmail = records[i].getSObjectField(Constants.EMAIL_FIELD).toString();
                        if (email.equals(contactEmail)) {
                            return records[i].getSObjectField(Constants.ID_FIELD).toString();
                        }
                    }
                }
            }
        } catch (ConnectionException e) {
            String msg = "Error while getting Id of contact for email: " + email;
            throw new SalesforceException(msg, e);
        }
        return null;
    }

    /**
     * Method to update a Salesforce object.
     *
     * @param sObjectType             Salesforce object type
     * @param sObjectId               Salesforce Object Id
     * @param complimentaryFieldValue true or false
     */
    public void updateSObject(String sObjectType, String sObjectId, boolean complimentaryFieldValue) throws
            SalesforceException {
        try {
            SObject updatedSObject = new SObject();
            updatedSObject.setType(sObjectType);
            updatedSObject.setId(sObjectId);
            updatedSObject.setField(Constants.COMPLIMENTARY_CLOUD_SUBSCRIBER_CUSTOM_FIELD, complimentaryFieldValue);
            SaveResult[] saveResults = conn.update(new SObject[]{updatedSObject});
            for (int j = 0; j < saveResults.length; j++) {
                if (saveResults[j].isSuccess()) {
                    log.info("Item with an ID of " + saveResults[j].getId() + " was updated.");
                } else {
                    for (int i = 0; i < saveResults[j].getErrors().length; i++) {
                        Error err = saveResults[j].getErrors()[i];
                        log.error("Error on item: " + j);
                        log.error("Error code: " + err.getStatusCode().toString());
                        log.error("Error message: " + err.getMessage());
                    }
                }
            }
        } catch (ConnectionException e) {
            String msg = "Error while updating Salesforce " + sObjectType + " type object with Id " + sObjectId + ".";
            throw new SalesforceException(msg, e);
        }
    }

}
