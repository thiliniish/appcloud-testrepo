/*
 * Copyright (c) 2017, WSO2 Inc. All Rights Reserved.
 */
package org.wso2.carbon.cloud.complimentary.users;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.complimentary.users.exception.CustomerException;
import org.wso2.carbon.cloud.complimentary.users.exception.SalesforceException;
import org.wso2.carbon.cloud.complimentary.users.model.config.SalesforceConfiguration;
import org.wso2.carbon.cloud.complimentary.users.utils.SalesforceConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * This class is where the business logic of the Customer validation service is implemented.
 *
 * @since 1.0.0
 */
public class CustomerManager {

    private static Log log = LogFactory.getLog(CustomerManager.class);
    private static SalesforceConfiguration config;

    public CustomerManager() {
    }

    public static void loadConfig(String username, String password, String token) throws IOException {
        config = new SalesforceConfiguration();
        config.setUsername(username);
        config.setPassword(password);
        config.setToken(token);
    }

    /**
     * Method to retrieve the list of products for which the customer has production support from Salesforce.
     *
     * @param opportunities List of Jira keys
     * @return List of products which have production support for the given jira
     * @throws SalesforceException Thrown if the backend resources produce errors
     */
    public static List<String> getProdProductsforOpportunities(List<String> opportunities) throws SalesforceException {
        if (opportunities.size() <= 0) {
            return new ArrayList<>();
        }
        SalesforceConnector salesforceConnector = new SalesforceConnector();
        salesforceConnector.login(config.getUsername(), config.getPassword(), config.getToken());
        @SuppressWarnings("unchecked")
        List<String> salesforceProducts = salesforceConnector.getProductsByOpportunitiesIds(opportunities);
        salesforceConnector.logout();
        return salesforceProducts;
    }

    /**
     * Method to check if given customer has production support for given product.
     *
     * @param opportunities List of production opportunities for customer
     * @param productName   Product name
     * @return Whether the customer has production support for product
     * @throws CustomerException
     */
    public static boolean hasProductionSupportForProduct(List<String> opportunities, String productName)
            throws CustomerException {
        try {
            List<String> products = getProdProductsforOpportunities(opportunities);
            for (int i = 0; i < products.size(); i++) {
                String product = products.get(i);
                if (product.contains(productName)) {
                    return true;
                }
            }
            return false;
        } catch (SalesforceException e) {
            throw new CustomerException(e);
        }
    }

    /**
     * Method to check if the complimentary account been claimed.
     *
     * @param opportunities List of production opportunities for customer
     * @return Whether the complimentary account has been claimed
     * @throws CustomerException
     */
    public static boolean hasClaimedComplimentarySubscription(List<String> opportunities) throws CustomerException {
        try {
            SalesforceConnector salesforceConnector = new SalesforceConnector();
            salesforceConnector.login(config.getUsername(), config.getPassword(), config.getToken());
            @SuppressWarnings("unchecked")
            boolean hasClaimedComplimentarySubscripton = salesforceConnector.
                    hasClaimedComplimentarySubscription(opportunities);
            salesforceConnector.logout();
            return hasClaimedComplimentarySubscripton;
        } catch (SalesforceException e) {
            throw new CustomerException(e);
        }
    }

    /**
     * Method to get who has claimed the subscription for given support account.
     *
     * @param opportunities List of production opportunities for customer
     * @return Name of the contact who claimed the subscription
     * @throws CustomerException
     */
    public static String getContactWhoClaimedSubscription(List<String> opportunities) throws CustomerException {
        try {
            SalesforceConnector salesforceConnector = new SalesforceConnector();
            salesforceConnector.login(config.getUsername(), config.getPassword(), config.getToken());
            @SuppressWarnings("unchecked")
            String contact = salesforceConnector.getContactWhoClaimedSubscription(opportunities);
            salesforceConnector.logout();
            return contact;
        } catch (SalesforceException e) {
            throw new CustomerException(e);
        }
    }

    /**
     * Method to get production opportunities for customer given the production Jira keys
     *
     * @param customerProdJiraKeys List of production support Jira keys for customer
     * @return List of production opportunities for customer
     * @throws CustomerException
     */
    public static List<String> getOpportunitiesByJira(String[] customerProdJiraKeys) throws CustomerException {
        try {
            SalesforceConnector salesforceConnector = new SalesforceConnector();
            salesforceConnector.login(config.getUsername(), config.getPassword(), config.getToken());
            List<String> opportunities = salesforceConnector.getOpportunitiesByJira(
                    Arrays.asList(customerProdJiraKeys));
            salesforceConnector.logout();
            return opportunities;
        } catch (SalesforceException e) {
            throw new CustomerException(e);
        }
    }

    /**
     * Method to update Salesforce account object indicating the complimentary account was claimed.
     *
     * @param opportunities List of production opportunities for customer
     * @param update        true or false
     * @throws CustomerException
     */
    public static void updateSalesforceAccountObject(List<String> opportunities, boolean update)
            throws CustomerException {
        try {
            SalesforceConnector salesforceConnector = new SalesforceConnector();
            salesforceConnector.login(config.getUsername(), config.getPassword(), config.getToken());
            String accountId = salesforceConnector.getAccountId(opportunities);
            salesforceConnector.updateSObject("Account", accountId, update);
            salesforceConnector.logout();
        } catch (SalesforceException e) {
            throw new CustomerException(e);
        }
    }

    /**
     * Method to update Salesforce contact object indicating whom claimed the complimentary account.
     *
     * @param contactId Id of contact who claimed the subscription
     * @param update    true or false
     * @throws CustomerException
     */
    public static void updateSalesforceContactObject(String contactId, boolean update) throws CustomerException {
        try {
            SalesforceConnector salesforceConnector = new SalesforceConnector();
            salesforceConnector.login(config.getUsername(), config.getPassword(), config.getToken());
            salesforceConnector.updateSObject("Contact", contactId, update);
            salesforceConnector.logout();
        } catch (SalesforceException e) {
            throw new CustomerException(e);
        }
    }

    /**
     * Method to get the contact Id from Salesforce given the email of the customer.
     *
     * @param opportunities List of production opportunities for customer
     * @param email         Email of customer
     * @return Id of customer
     * @throws CustomerException
     */
    public static String getContactId(List<String> opportunities, String email) throws CustomerException {
        try {
            SalesforceConnector salesforceConnector = new SalesforceConnector();
            salesforceConnector.login(config.getUsername(), config.getPassword(), config.getToken());
            String contactId = salesforceConnector.getContactId(opportunities, email);
            salesforceConnector.logout();
            return contactId;
        } catch (SalesforceException e) {
            throw new CustomerException(e);
        }
    }

}
