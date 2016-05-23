/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.cloud.billing.commons.zuora.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zuora.api.*;
import com.zuora.api.object.Product;
import com.zuora.api.object.ProductRatePlan;
import com.zuora.api.object.ProductRatePlanCharge;
import com.zuora.api.object.ProductRatePlanChargeTier;
import com.zuora.api.wso2.stub.*;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.MonetizationConstants;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ZuoraClientUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Zuora product client extends the support of ZuoraServiceStub to create/update/delete/query Product
 */
public class ZuoraProductClient extends ZuoraClient {

	private static final String PRODUCT_CREATION_ERROR = "occurred while creating customer product";
	private static final String PRODUCT_RATEPLAN_CREATION_ERROR = "occurred while creating product rate plan";
	private static final String PRODUCT_RATEPLAN_UPDATE_ERROR = "occurred while updating the product rate plan";
	private static final String PRODUCT_DELETION_ERROR = "occurred while deleting customer product";
	private static final String PRODUCT_QUERY_BY_NAME_ERROR = "occurred while querying customer product by name";

	private static final String ERROR_JSON_OBJ_INVALID_PRODUCT = "{\"code\": null,\"codeSpecified\": true,\"field\": " +
	"null,\"fieldSpecified\": false,\"message\": \"Invalid product name. \",\"messageSpecified\": true}";
	private static final String ERROR_JSON_OBJ_PRODUCT_RATEPLAN_UPDATE_FAILURE =
			"{\"code\": null,\"codeSpecified\": true,\"field\": null,\"fieldSpecified\": false,\"message\": \"Product rate plan update failed. \",\"messageSpecified\": true}";

	public ZuoraProductClient() throws CloudBillingZuoraException {
		super();
	}

	/**
	 * Method for create Product
	 *
	 * @param productInfo product information json object
	 * @return result JsonObject
	 * {
	 *    "errors": null,
	 *    "errorsSpecified": false,
	 *    "id": {
	 *        "id": "2c92c0f9501d4f330150464f02ef0312"
	 *    },
	 *    "idSpecified": true,
	 *    "success": true,
	 *    "successSpecified": true
	 * }
	 * <p/>
	 * or
	 * <p/>
	 * {
	 *    "errors": [
	 *        {
	 *            "code": {
	 *                "value": "UNEXPECTED_ERROR"
	 *            },
	 *            "codeSpecified": true,
	 *            "field": null,
	 *            "fieldSpecified": false,
	 *            "message": "There was an unexpected problem with the call.",
	 *            "messageSpecified": true
	 *        }
	 *    ],
	 *    "errorsSpecified": true,
	 *    "id": null,
	 *    "idSpecified": false,
	 *    "success": false,
	 *    "successSpecified": true
	 * }
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 */
	public JsonObject createProduct(JsonObject productInfo) throws CloudBillingZuoraException {
		String productName = productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_NAME).toString().replaceAll("\"", "");
		String productCategory =
				productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_CATEGORY).toString().replaceAll("\"", "");
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(BillingConstants.EFFECTIVE_DATE_FORMAT);
			Date date = sdf.parse(BillingConstants.EFFECTIVE_END_DATE);
			Calendar calendarEndDate = Calendar.getInstance();
			calendarEndDate.setTime(date);
			// Create the Zuora Product zObject
			Product product = new Product();
			product.setName(productName);
			product.setCategory(productCategory);
			product.setEffectiveStartDate(Calendar.getInstance());
			product.setEffectiveEndDate(calendarEndDate);
			product.setCommission__c(Integer.toString(BillingConstants.PRODUCT_COMMISSION_DEFAUlT_VALUE));
			SaveResult result = zuoraClientUtils.create(product);
			if (result != null && !result.getSuccess()) {
				throw new CloudBillingZuoraException(zuoraClientUtils.getZuoraErrorMessage(result));
			} else {
				return objectToJson(result);
			}
		} catch (RemoteException e) {
			throw new CloudBillingZuoraException("Remote exception " + PRODUCT_CREATION_ERROR + productName, e);
		} catch (InvalidTypeFault e) {
			String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
			throw new CloudBillingZuoraException(
					"Invalid type fault error " + PRODUCT_CREATION_ERROR + productName, errorCode, e);
		} catch (UnexpectedErrorFault e) {
			String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("Unexpected Error Fault error " + PRODUCT_CREATION_ERROR + productName,
			                                     errorCode, e);
		} catch (IOException e) {
			throw new CloudBillingZuoraException("IOError " + PRODUCT_CREATION_ERROR + productName, e);
		} catch (Exception e) {
			throw new CloudBillingZuoraException("Exception " + PRODUCT_CREATION_ERROR + productName, e);
		}
	}

	/**
	 * Method for create product rate plan
	 *
	 * @param productRatePlanInfo JsonObject productRatePlanInfo
	 * @return result JsonObject
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 */
	public JsonObject createProductRatePlan(JsonObject productRatePlanInfo) throws CloudBillingZuoraException {
		String productName =
				productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_NAME).toString().replaceAll("\"", "");
		String productRatePlanName =
				productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_NAME).toString()
				                   .replaceAll("\"", "");
        String productRatePlanChargeTierPrice =
                productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_RECURRING_CHARGE).toString()
                                   .replaceAll("\"", "");
        String productRatePlanOverageChargePrice =
                productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_OVERAGE_CHARGE).toString()
                                   .replaceAll("\"", "");
        String description = productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_DESCRIPTION).toString()
		                                        .replaceAll("\"", "");
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(BillingConstants.EFFECTIVE_DATE_FORMAT);
			Date date = sdf.parse(BillingConstants.EFFECTIVE_END_DATE);
			Calendar calendarEndDate = Calendar.getInstance();
			calendarEndDate.setTime(date);
			Calendar effectiveStartDate = Calendar.getInstance();
			Calendar effectiveEndDate = calendarEndDate;

			// Create ProductRatePlan
			ProductRatePlan productRatePlan = new ProductRatePlan();
			productRatePlan.setEffectiveStartDate(effectiveStartDate);
			productRatePlan.setEffectiveEndDate(effectiveEndDate);
			productRatePlan.setName(productRatePlanName);
			productRatePlan.setDescription(description);

			Product product = getProduct(productName);
			ID productId = product.getId();
			productRatePlan.setProductId(productId);

			SaveResult saveResult = zuoraClientUtils.create(productRatePlan);
			ID productRatePlanId = saveResult.getId();

			// Create ProductRatePlanCharge - recurring charges
			ProductRatePlanCharge productRatePlanCharge = new ProductRatePlanCharge();
			productRatePlanCharge.setBillCycleDay(BillingConstants.RATEPLAN_CHARGE_BILLCYCLEDAY);
			productRatePlanCharge.setBillingPeriod(BillingConstants.RATEPLAN_CHARGE_BILLING_PERIOD);
			productRatePlanCharge.setBillingPeriodAlignment(BillingConstants.RATEPLAN_CHARGE_ALIGNMENT);
			productRatePlanCharge.setChargeModel(BillingConstants.RATEPLAN_CHARGE_MODEL);
			productRatePlanCharge.setChargeType(BillingConstants.RATEPLAN_CHARGE_TYPE);
			productRatePlanCharge.setName(BillingConstants.RATEPLAN_CHARGE_NAME_MONTHLY_SUBSCRIPTION);
			productRatePlanCharge.setTriggerEvent(BillingConstants.RATEPLAN_CHARGE_TRIGGER_EVENT);
			productRatePlanCharge.setProductRatePlanId(productRatePlanId);

			// Create ProductRatePlanCharge - over-usage charges
			ProductRatePlanCharge productOverUsageCharge = new ProductRatePlanCharge();
			productOverUsageCharge.setBillCycleDay(BillingConstants.RATEPLAN_CHARGE_BILLCYCLEDAY);
			productOverUsageCharge.setBillingPeriod(BillingConstants.RATEPLAN_CHARGE_BILLING_PERIOD);
			productOverUsageCharge.setBillingPeriodAlignment(BillingConstants.RATEPLAN_CHARGE_ALIGNMENT);
			productOverUsageCharge.setChargeModel(BillingConstants.RATEPLAN_CHARGE_TIER_PRICE_FORMAT);
			productOverUsageCharge.setChargeType(BillingConstants.RATEPLAN_CHARGE_TYPE_OVERUSAGE);
			productOverUsageCharge.setName(BillingConstants.RATEPLAN_CHARGE_NAME_OVERUSAGE);
			productOverUsageCharge.setTriggerEvent(BillingConstants.RATEPLAN_CHARGE_TRIGGER_EVENT);
			productOverUsageCharge.setUOM(MonetizationConstants.UNIT_OF_MEASURE_DISPLAY_NAME);
			productOverUsageCharge.setProductRatePlanId(productRatePlanId);

			// Create ProductRatePlanChargeTier
			ProductRatePlanChargeTier chargeTier = new ProductRatePlanChargeTier();
			chargeTier.setCurrency(BillingConstants.RATEPLAN_CHARGETIER_CURRENCY);
			chargeTier.setPrice(BigDecimal.valueOf(Double.parseDouble(productRatePlanChargeTierPrice)));
			chargeTier.setPriceFormat(BillingConstants.RATEPLAN_CHARGETIER_PRICE_FORMAT);
			chargeTier.setStartingUnit(BigDecimal.valueOf(1));

			ProductRatePlanChargeTierData chargeTierData = new ProductRatePlanChargeTierData();
			chargeTierData.setProductRatePlanChargeTier(new ProductRatePlanChargeTier[] { chargeTier });
			productRatePlanCharge.setProductRatePlanChargeTierData(chargeTierData);

			//Create the recurring charge rate Plan
			SaveResult result = zuoraClientUtils.create(productRatePlanCharge);
			if (result != null && !result.getSuccess()) {
				throw new CloudBillingZuoraException(zuoraClientUtils.getZuoraErrorMessage(result));
			}
			//Create the usage charge rate Plan
			chargeTier.setPrice(BigDecimal.valueOf(Double.parseDouble(productRatePlanOverageChargePrice)));
			chargeTierData.setProductRatePlanChargeTier(new ProductRatePlanChargeTier[] { chargeTier });
			productOverUsageCharge.setProductRatePlanChargeTierData(chargeTierData);
			SaveResult saveResultOverUsage = zuoraClientUtils.create(productOverUsageCharge);
			if (saveResultOverUsage != null && !saveResultOverUsage.getSuccess()) {
				throw new CloudBillingZuoraException(zuoraClientUtils.getZuoraErrorMessage(saveResultOverUsage));
			} else {
				return objectToJson(productRatePlanId);
			}
		} catch (RemoteException e) {
			throw new CloudBillingZuoraException("Remote exception " + PRODUCT_RATEPLAN_CREATION_ERROR + productName,
			                                     e);
		} catch (InvalidTypeFault e) {
			String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
			throw new CloudBillingZuoraException(
					"Invalid type fault error " + PRODUCT_RATEPLAN_CREATION_ERROR + productName, errorCode, e);
		} catch (UnexpectedErrorFault e) {
			String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException(
					"Unexpected Error Fault error " + PRODUCT_RATEPLAN_CREATION_ERROR + productName, errorCode, e);
		} catch (IOException e) {
			throw new CloudBillingZuoraException("IOError " + PRODUCT_RATEPLAN_CREATION_ERROR + productName, e);
		} catch (Exception e) {
			throw new CloudBillingZuoraException("Exception " + PRODUCT_RATEPLAN_CREATION_ERROR + productName, e);
		}
	}

	/**
	 * Method for query Product by name
	 *
	 * @param productName product name
	 * @return Product json object
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 */
	public JsonObject queryProductByName(String productName) throws CloudBillingZuoraException {
		try {
			Product product = getProduct(productName);
			return objectToJson(product);
		} catch (RemoteException e) {
			throw new CloudBillingZuoraException("Remote exception " + PRODUCT_QUERY_BY_NAME_ERROR + productName, e);
		} catch (InvalidQueryLocatorFault e) {
			String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException(
					"InvalidQueryLocatorFault " + PRODUCT_QUERY_BY_NAME_ERROR + productName, errorCode, e);
		} catch (MalformedQueryFault e) {
			String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("MalformedQueryFault " + PRODUCT_QUERY_BY_NAME_ERROR + productName,
			                                     errorCode, e);
		} catch (UnexpectedErrorFault e) {
			String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("UnexpectedErrorFault " + PRODUCT_QUERY_BY_NAME_ERROR + productName,
			                                     errorCode, e);
		} catch (IOException e) {
			throw new CloudBillingZuoraException("IOException " + PRODUCT_QUERY_BY_NAME_ERROR + productName, e);
		}
	}

	/**
	 * Method for delete product by name
	 *
	 * @param productName product name
	 * @return result JsonObject
	 * {
	 * "errors": null,
	 * "errorsSpecified": false,
	 * "id": {
	 * "id": "2c92c0f8501d4405015046de02cf0542"
	 * },
	 * "idSpecified": true,
	 * "success": true,
	 * "successSpecified": true
	 * }
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 */
	public JsonObject deleteProduct(String productName) throws CloudBillingZuoraException {
		try {
			Product product = getProduct(productName);
			if (product != null) {
				DeleteResult result = zuoraClientUtils.delete(BillingConstants.ZUORA_PRODUCT, product.getId());
				return objectToJson(result);
			} else {
				JsonObject errorResponse = new JsonObject();
				errorResponse.addProperty(BillingConstants.EEROR_RESPONSE_PROPERTY_SUCCESS, false);
				errorResponse.addProperty(BillingConstants.EEROR_RESPONSE_PROPERTY_SUCCESS_SPECIFIED, true);
				errorResponse.addProperty(BillingConstants.EEROR_RESPONSE_PROPERTY_ERRORS_SPECIFIED, true);
				JsonObject[] errorObjs = new JsonObject[] {
						new JsonParser().parse(ERROR_JSON_OBJ_INVALID_PRODUCT).getAsJsonObject()
				};
				errorResponse.add(BillingConstants.EEROR_RESPONSE_PROPERTY_ERRORS, new Gson().toJsonTree(errorObjs));
				return errorResponse;
			}

		} catch (RemoteException e) {
			throw new CloudBillingZuoraException("Remote exception " + PRODUCT_DELETION_ERROR + productName, e);
		} catch (InvalidQueryLocatorFault e) {
			String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("InvalidQueryLocatorFault " + PRODUCT_DELETION_ERROR + productName,
			                                     errorCode, e);
		} catch (MalformedQueryFault e) {
			String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("MalformedQueryFault " + PRODUCT_DELETION_ERROR + productName,
			                                     errorCode, e);
		} catch (UnexpectedErrorFault e) {
			String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("UnexpectedErrorFault " + PRODUCT_DELETION_ERROR + productName,
			                                     errorCode, e);
		} catch (InvalidValueFault e) {
			String errorCode = e.getFaultMessage().getInvalidValueFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("InvalidValueFault " + PRODUCT_DELETION_ERROR + productName, errorCode,
			                                     e);
		} catch (InvalidTypeFault e) {
			String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("InvalidTypeFault " + PRODUCT_DELETION_ERROR + productName, errorCode,
			                                     e);
		} catch (IOException e) {
			throw new CloudBillingZuoraException("IOException " + PRODUCT_DELETION_ERROR + productName, e);
		}
	}

	/**
	 * Method for query zuora product by name
	 *
	 * @param productName product name
	 * @return Zoura product
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 * @throws java.rmi.RemoteException
	 * @throws com.zuora.api.wso2.stub.InvalidQueryLocatorFault
	 * @throws com.zuora.api.wso2.stub.MalformedQueryFault
	 * @throws com.zuora.api.wso2.stub.UnexpectedErrorFault
	 */
	private Product getProduct(String productName)
			throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
			       UnexpectedErrorFault {
		String query = ZuoraClientUtils
				.prepareZQuery(BillingConstants.QUERY_ZUORA_PRODUCT_BY_NAME, new String[] { productName });
		QueryResult result = zuoraClientUtils.query(query, null);
		if ((result.getRecords())[0] != null) {
			return (Product) result.getRecords()[0];
		} else {
			return null;
		}
	}

	/**
	 * Query zuora product rate plan by using product rate plan name and product id
	 *
	 * @param productRatePlanName product rate plan name
	 * @param productId product id
	 * @return Zoura product rate plan
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 * @throws java.rmi.RemoteException
	 * @throws com.zuora.api.wso2.stub.InvalidQueryLocatorFault
	 * @throws com.zuora.api.wso2.stub.MalformedQueryFault
	 * @throws com.zuora.api.wso2.stub.UnexpectedErrorFault
	 */
	private ProductRatePlan getProductRatePlan(String productRatePlanName, String productId)
			throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
			       UnexpectedErrorFault {
		String query = ZuoraClientUtils.prepareZQuery(BillingConstants.QUERY_ZUORA_PRODUCTRATEPLAN_BY_NAME,
		                                              new String[] { productRatePlanName, productId });
		QueryResult result = zuoraClientUtils.query(query, null);
		if ((result.getRecords())[0] != null) {
			return (ProductRatePlan) result.getRecords()[0];
		} else {
			return null;
		}
	}

	/**
	 * Method for update Product rate plan and rate plan charges
	 *
	 * @param productInfo json object of product information
	 * @return response JsonObject
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 */
	public JsonObject updateProductRatePlan(JsonObject productInfo) throws CloudBillingZuoraException {
		String productName = productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_NAME).toString().replaceAll("\"", "");
		String productRatePlanName =
				productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_NAME).toString().replaceAll("\"", "");
		String productRatePlanRecurringCharge =
				productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_RECURRING_CHARGE).toString().replaceAll("\"", "");
		String productRatePlanOverageUsageCharge =
				productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_OVERAGE_CHARGE).toString()
				           .replaceAll("\"", "");
		String description =
				productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_DESCRIPTION).toString().replaceAll("\"", "");
		// Create error object
		JsonObject[] errorObjs;
		// Create error response
		JsonObject errorResponse = new JsonObject();
		errorResponse.addProperty(BillingConstants.EEROR_RESPONSE_PROPERTY_SUCCESS, false);
		errorResponse.addProperty(BillingConstants.EEROR_RESPONSE_PROPERTY_SUCCESS_SPECIFIED, true);
		errorResponse.addProperty(BillingConstants.EEROR_RESPONSE_PROPERTY_ERRORS_SPECIFIED, true);
		try {
			// Get the Zuora Product of the tenant
			Product product = getProduct(productName);
			SaveResult saveResult;
			if (product != null) {
				ID productId = product.getId();
				// Get the ProductRatePlan using rate plan name and the product id
				ProductRatePlan productRatePlan = getProductRatePlan(productRatePlanName, productId.toString());
				// update the product rate plan description
				if (!description.isEmpty()) {
					productRatePlan.setDescription(description);
					productRatePlan.setProductId(productId);
					saveResult = zuoraClientUtils.update(productRatePlan);
					if (saveResult != null && !saveResult.getSuccess()) {
						throw new CloudBillingZuoraException(zuoraClientUtils.getZuoraErrorMessage(saveResult));
					}
				}
				// Get the product rate plan id
				ID productRatePlanId = productRatePlan.getId();
				// Update recurring charge price
				if (!productRatePlanRecurringCharge.isEmpty()) {
					// Get ProductRatePlanCharge using rate plan charge name and product rate plan id - recurring charges
					ProductRatePlanCharge productRatePlanCharge =
							getProductRatePlanCharge(BillingConstants.RATEPLAN_CHARGE_NAME_MONTHLY_SUBSCRIPTION,
							                         productRatePlanId.toString());
					productRatePlanCharge.setProductRatePlanId(productRatePlanId);
					// Get the product rate plan charge id for recurring charges
					ID productRatePlanChargeId = productRatePlanCharge.getId();
					ProductRatePlanChargeTier chargeTier =
							getProductRatePlanChargeTier(productRatePlanChargeId.toString());
					chargeTier.setPrice(BigDecimal.valueOf(Double.parseDouble(productRatePlanRecurringCharge)));

					ProductRatePlanChargeTierData chargeTierData = new ProductRatePlanChargeTierData();
					chargeTierData.setProductRatePlanChargeTier(new ProductRatePlanChargeTier[] { chargeTier });
					productRatePlanCharge.setProductRatePlanChargeTierData(chargeTierData);
					saveResult = zuoraClientUtils.update(productRatePlanCharge);
					if (saveResult != null && !saveResult.getSuccess()) {
						throw new CloudBillingZuoraException(zuoraClientUtils.getZuoraErrorMessage(saveResult));
					}
				}
				// Update over usage charge price
				if (!productRatePlanOverageUsageCharge.isEmpty()) {
					// Get ProductRatePlanCharge using rate plan charge name and product rate plan id - over usage charges
					ProductRatePlanCharge productOverUsageCharge =
							getProductRatePlanCharge(BillingConstants.RATEPLAN_CHARGE_NAME_OVERUSAGE,
							                         productRatePlanId.toString());
					productOverUsageCharge.setProductRatePlanId(productRatePlanId);
					// Get the product rate plan charge id for over usage charges
					ID productOverUsageChargeId = productOverUsageCharge.getId();
					ProductRatePlanChargeTier overUsageChargeTier =
							getProductRatePlanChargeTier(productOverUsageChargeId.toString());
					overUsageChargeTier
							.setPrice(BigDecimal.valueOf(Double.parseDouble(productRatePlanOverageUsageCharge)));

					ProductRatePlanChargeTierData chargeTierData = new ProductRatePlanChargeTierData();
					chargeTierData
							.setProductRatePlanChargeTier(new ProductRatePlanChargeTier[] { overUsageChargeTier });
					productOverUsageCharge.setProductRatePlanChargeTierData(chargeTierData);
					saveResult = zuoraClientUtils.update(productOverUsageCharge);
					if (saveResult != null && !saveResult.getSuccess()) {
						throw new CloudBillingZuoraException(zuoraClientUtils.getZuoraErrorMessage(saveResult));
					}
				}
				return objectToJson(productRatePlanId);
			} else {
				errorObjs =
						new JsonObject[] { new JsonParser().parse(ERROR_JSON_OBJ_INVALID_PRODUCT).getAsJsonObject() };
				errorResponse.add(BillingConstants.EEROR_RESPONSE_PROPERTY_ERRORS, new Gson().toJsonTree(errorObjs));
				return errorResponse;
			}
		} catch (InvalidTypeFault e) {
			String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
			throw new CloudBillingZuoraException(
					"Invalid type fault error " + PRODUCT_RATEPLAN_UPDATE_ERROR + productName, errorCode, e);
		} catch (UnexpectedErrorFault e) {
			String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException(
					"Unexpected Error Fault error " + PRODUCT_RATEPLAN_UPDATE_ERROR + productName, errorCode, e);
		} catch (InvalidQueryLocatorFault e) {
			String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException(
					"InvalidQueryLocatorFault " + PRODUCT_RATEPLAN_UPDATE_ERROR + productName, errorCode, e);
		} catch (MalformedQueryFault e) {
			String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("MalformedQueryFault " + PRODUCT_RATEPLAN_UPDATE_ERROR + productName,
			                                     errorCode, e);
		} catch (IOException e) {
			throw new CloudBillingZuoraException("IOException " + PRODUCT_RATEPLAN_UPDATE_ERROR + productName, e);
		} catch (CloudBillingZuoraException e) {
			errorObjs = new JsonObject[] {
					new JsonParser().parse(ERROR_JSON_OBJ_PRODUCT_RATEPLAN_UPDATE_FAILURE)
					                .getAsJsonObject() };
			errorResponse.add(BillingConstants.EEROR_RESPONSE_PROPERTY_ERRORS, new Gson().toJsonTree(errorObjs));
			return errorResponse;
		}
	}

	/**
	 * Query zuora product rate plan charge by using product rate plan charge name and rate plan id
	 *
	 * @param productRatePlanChargeName product rate plan charge name
	 * @param productRatePlanId         product rate plan id
	 * @return Zoura product rate plan charge
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 * @throws java.rmi.RemoteException
	 * @throws com.zuora.api.wso2.stub.InvalidQueryLocatorFault
	 * @throws com.zuora.api.wso2.stub.MalformedQueryFault
	 * @throws com.zuora.api.wso2.stub.UnexpectedErrorFault
	 */
	private ProductRatePlanCharge getProductRatePlanCharge(String productRatePlanChargeName, String productRatePlanId)
			throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
			       UnexpectedErrorFault {
		String query = ZuoraClientUtils.prepareZQuery(BillingConstants.QUERY_ZUORA_PRODUCTRATEPLAN_CHARGE_BY_NAME,
		                                              new String[] { productRatePlanChargeName, productRatePlanId });
		QueryResult result = zuoraClientUtils.query(query, null);
		if ((result.getRecords())[0] != null) {
			return (ProductRatePlanCharge) result.getRecords()[0];
		} else {
			return null;
		}
	}

	/**
	 * Query zuora product rate plan charge tier by using product rate plan charge id
	 *
	 * @param productRatePlanChargeId product rate plan charge id
	 * @return Zoura product rate plan charge tier
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 * @throws java.rmi.RemoteException
	 * @throws com.zuora.api.wso2.stub.InvalidQueryLocatorFault
	 * @throws com.zuora.api.wso2.stub.MalformedQueryFault
	 * @throws com.zuora.api.wso2.stub.UnexpectedErrorFault
	 */
	private ProductRatePlanChargeTier getProductRatePlanChargeTier(String productRatePlanChargeId)
			throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
			       UnexpectedErrorFault {
		String query = ZuoraClientUtils.prepareZQuery(BillingConstants.QUERY_ZUORA_PRODUCTRATEPLAN_CHARGE_TIER,
		                                              new String[] { productRatePlanChargeId });
		QueryResult result = zuoraClientUtils.query(query, null);
		if ((result.getRecords())[0] != null) {
			return (ProductRatePlanChargeTier) result.getRecords()[0];
		} else {
			return null;
		}
	}
}

