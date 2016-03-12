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
import org.codehaus.jackson.map.ObjectMapper;
import org.wso2.carbon.cloud.billing.commons.BillingConstants;
import org.wso2.carbon.cloud.billing.commons.zuora.client.utils.ZuoraClientUtils;
import org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;

/**
 * Zuora product client extends the support of ZuoraServiceStub to create/update/delete/query Product
 */
public class ZuoraProductClient extends ZuoraClient {

	private static final String PRODUCT_CREATION_ERROR = "occurred while creating customer product";
	private static final String PRODUCT_RATEPLAN_CREATION_ERROR = "occurred while creating product rate plan";
	private static final String PRODUCT_UPDATE_ERROR = "occurred while updating the customer product";
	private static final String PRODUCT_DELETION_ERROR = "occurred while deleting customer product";
	private static final String PRODUCT_QUERY_BY_NAME_ERROR = "occurred while querying customer product by name";

	private static final String ERROR_JSON_OBJ_INVALID_PRODUCT = "{\"code\": null,\"codeSpecified\": true,\"field\": " +
	"null,\"fieldSpecified\": false,\"message\": \"Invalid product name. \",\"messageSpecified\": true}";

	public ZuoraProductClient() throws CloudBillingZuoraException {
		super();
	}

	/**
	 * Method for create Product
	 *
	 * @param productInfo product information json object
	 * @return result JsonObject
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 */
	public JsonObject createProduct(JsonObject productInfo) throws CloudBillingZuoraException {
		String productName = productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_NAME).toString().replaceAll("\"", "");
		String productCategory =
				productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_CATEGORY).toString().replaceAll("\"", "");
		Date effectiveStartDate = new Date();
		Date effectiveEndDate = new Date(BillingConstants.EFFECTIVE_END_DATE);
		try {
			// Create the Zuora Product zObject
			Product product = new Product();
			product.setName(productName);
			product.setCategory(productCategory);
			product.setEffectiveStartDate(effectiveStartDate);
			product.setEffectiveEndDate(effectiveEndDate);
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
		Date effectiveStartDate = new Date();
		Date effectiveEndDate = new Date(BillingConstants.EFFECTIVE_END_DATE);
		String productName =
				productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_NAME).toString().replaceAll("\"", "");
		String productRatePlanName =
				productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_NAME).toString()
				                   .replaceAll("\"", "");
		String productRatePlanChargeTierPrice =
				productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_PRICE).toString()
				                   .replaceAll("\"", "");
		String description = productRatePlanInfo.get(BillingConstants.JSON_OBJ_PRODUCT_RATEPLAN_DESCRIPTION).toString()
		                                        .replaceAll("\"", "");
		try {
			// Create ProductRatePlan
			ProductRatePlan productRatePlan = new ProductRatePlan();
			productRatePlan.setEffectiveStartDate(effectiveStartDate);
			productRatePlan.setEffectiveEndDate(effectiveEndDate);
			productRatePlan.setName(productRatePlanName);
			productRatePlan.setDescription(description);

			Product product = getProduct(productName);
			ID productId = product.getId();
			productRatePlan.setProductId(productId);

			zuoraClientUtils.create(productRatePlan);
			ID productRatePlanId = productRatePlan.getId();

			// Create ProductRatePlanCharge - recurring charges
			ProductRatePlanCharge productRatePlanCharge = new ProductRatePlanCharge();
			productRatePlanCharge.setBillCycleDay(BillingConstants.RATEPLAN_CHARGE_BILLCYCLEDAY);
			productRatePlanCharge.setBillingPeriod(BillingConstants.RATEPLAN_CHARGE_BILLING_PERIOD);
			productRatePlanCharge.setBillingPeriodAlignment(BillingConstants.RATEPLAN_CHARGE_ALIGNMENT);
			productRatePlanCharge.setChargeModel(BillingConstants.RATEPLAN_CHARGE_MODEL);
			productRatePlanCharge.setChargeType(BillingConstants.RATEPLAN_CHARGE_TYPE);
			productRatePlanCharge.setName(BillingConstants.RATEPLAN_CHARGE_NAME_MONTHLY_SUBSCRIPTION);
			productRatePlanCharge.setAccountingCode(BillingConstants.RATEPLAN_CHARGE_ACCOUNTING_CODE);
			productRatePlanCharge.setTriggerEvent(BillingConstants.RATEPLAN_CHARGE_TRIGGER_EVENT);
			productRatePlanCharge.setProductRatePlanId(productRatePlanId);

			// Create ProductRatePlanChargeTier
			ProductRatePlanChargeTier chargeTier = new ProductRatePlanChargeTier();
			chargeTier.setCurrency(BillingConstants.RATEPLAN_CHARGETIER_CURRENCY);
			chargeTier.setPrice(BigDecimal.valueOf(Integer.parseInt(productRatePlanChargeTierPrice)));
			chargeTier.setPriceFormat(BillingConstants.RATEPLAN_CHARGETIER_PRICE_FORMAT);
			chargeTier.setStartingUnit(BigDecimal.valueOf(1));

			ProductRatePlanChargeTierData chargeTierData = new ProductRatePlanChargeTierData();
			chargeTierData.setProductRatePlanChargeTier(new ProductRatePlanChargeTier[] { chargeTier });
			productRatePlanCharge.setProductRatePlanChargeTierData(chargeTierData);
			SaveResult result = zuoraClientUtils.create(productRatePlanCharge);
			if (result != null && !result.getSuccess()) {
				throw new CloudBillingZuoraException(zuoraClientUtils.getZuoraErrorMessage(result));
			} else {
				return objectToJson(result);
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
				errorResponse.addProperty("success", false);
				errorResponse.addProperty("successSpecified", true);
				errorResponse.addProperty("errorsSpecified", true);
				JsonObject[] errorObjs = new JsonObject[] {
						new JsonParser().parse(ERROR_JSON_OBJ_INVALID_PRODUCT).getAsJsonObject()
				};
				errorResponse.add("errors", new Gson().toJsonTree(errorObjs));
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
	 * Method for update Product
	 *
	 * @param productInfo json object
	 * @return result JsonObject
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 */
	public JsonObject updateProduct(JsonObject productInfo) throws CloudBillingZuoraException {
		String productName = productInfo.get(BillingConstants.JSON_OBJ_PRODUCT_NAME).toString().replaceAll("\"", "");
		try {
			ObjectMapper mapper = new ObjectMapper();
			Product productUpdated = mapper.readValue(productInfo.toString(), Product.class);
			if (productUpdated.getId() == null) {
				ID id = getProduct(productUpdated.getName()).getId();
				productUpdated.setId(id);
			}
			SaveResult saveResult = zuoraClientUtils.update(productUpdated);
			return objectToJson(saveResult);
		} catch (InvalidTypeFault e) {
			String errorCode = e.getFaultMessage().getInvalidTypeFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("Invalid type fault error " + PRODUCT_UPDATE_ERROR + productName,
			                                     errorCode, e);
		} catch (UnexpectedErrorFault e) {
			String errorCode = e.getFaultMessage().getUnexpectedErrorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("Unexpected Error Fault error " + PRODUCT_UPDATE_ERROR + productName,
			                                     errorCode, e);
		} catch (InvalidQueryLocatorFault e) {
			String errorCode = e.getFaultMessage().getInvalidQueryLocatorFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("InvalidQueryLocatorFault " + PRODUCT_UPDATE_ERROR + productName,
			                                     errorCode, e);
		} catch (MalformedQueryFault e) {
			String errorCode = e.getFaultMessage().getMalformedQueryFault().getFaultCode().toString();
			throw new CloudBillingZuoraException("MalformedQueryFault " + PRODUCT_UPDATE_ERROR + productName, errorCode,
			                                     e);
		} catch (IOException e) {
			throw new CloudBillingZuoraException("IOException " + PRODUCT_UPDATE_ERROR + productName, e);
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
	 * Query zuora product rate plan by name
	 *
	 * @param productRatePlanName product rate plan name
	 * @return Zoura product rate plan
	 * @throws org.wso2.carbon.cloud.billing.exceptions.CloudBillingZuoraException
	 * @throws java.rmi.RemoteException
	 * @throws com.zuora.api.wso2.stub.InvalidQueryLocatorFault
	 * @throws com.zuora.api.wso2.stub.MalformedQueryFault
	 * @throws com.zuora.api.wso2.stub.UnexpectedErrorFault
	 */
	private ProductRatePlan getProductRatePlan(String productRatePlanName)
			throws CloudBillingZuoraException, RemoteException, InvalidQueryLocatorFault, MalformedQueryFault,
			       UnexpectedErrorFault {
		String query = ZuoraClientUtils.prepareZQuery(BillingConstants.QUERY_ZUORA_PRODUCTRATEPLAN_BY_NAME,
		                                              new String[] { productRatePlanName });
		QueryResult result = zuoraClientUtils.query(query, null);
		if ((result.getRecords())[0] != null) {
			return (ProductRatePlan) result.getRecords()[0];
		} else {
			return null;
		}
	}

}

