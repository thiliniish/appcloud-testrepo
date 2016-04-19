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

function displayUsagePlanTable(resultJSON) {
    var accountNumber;
    var tenantDomain;
    var tenants = resultJSON.Tenants.Tenant;
    var usagePlanTable = $('#UsagePlanTable');
    usagePlanTable.DataTable().clear().draw();
    //if there are no Tenants in the resultJSON
    if (tenants == undefined){
        return;
    }
    var tenantListSize = tenants.length;
    //if there is only one tenant in the resultJSON
    if(tenantListSize == undefined){
        accountNumber = resultJSON.Tenants.Tenant.ACCOUNT_NUMBER;
        tenantDomain = resultJSON.Tenants.Tenant.TENANT_DOMAIN;
        usagePlanTable.DataTable().row.add([
            accountNumber, tenantDomain
        ]).draw();
    } else {
        for(var i=0; i<tenantListSize; i++){
            accountNumber = resultJSON.Tenants.Tenant[i].ACCOUNT_NUMBER;
            tenantDomain = resultJSON.Tenants.Tenant[i].TENANT_DOMAIN;
            usagePlanTable.DataTable().row.add([
                accountNumber, tenantDomain
            ]).draw();
        }
    }
}

function fillRatePlanOptions(){
    jagg.post("../blocks/billingStatistics/ajax/billingStatistics.jag", {
        action:"getRatePlanNames"
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var select = document.getElementById("ratePlanSelect");
            var ratePlans = result.data;
            for ( var i = 0; i <ratePlans.length ;i++){
                var option = ratePlans[i];
                var startString = option.split("_")[0];
                var element = document.createElement("option");
                if(startString != "coupon") {
                    element.textContent = option;
                    element.value = option;
                    select.appendChild(element);
                }
            }
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!, </strong>' + result.message});
        }
    });
}

function getPlanList(){

    var dropdown = document.getElementById('ratePlanSelect');
    var selectedRatePlan = dropdown.value;
    jagg.message({type:'success',content:'Your request has been submitted. Please wait...'});

    jagg.post("../blocks/billingStatistics/ajax/billingStatistics.jag", {
        action:"getRatePlanList",
        selectedRatePlan : selectedRatePlan
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var resultJSON = JSON.parse(result.data);
            displayUsagePlanTable(resultJSON);
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!, </strong>' + result.message });
        }
    });
}

$(document).ready(function () {
    fillRatePlanOptions();
    $('#billingPAIDTable').DataTable({
        "processing": true,
        "serverSide": true,
        "ajax" : "../blocks/billingStatistics/ajax/billingTable.jag"
    });
    $('#billingTRIALTable').DataTable();
    $('#UsagePlanTable').DataTable({
        bFilter: false,
        bPaginate: false,
        bLengthChange: false,
        "processing": true
    });
    $("a[data-toggle=\"tab\"]").on("shown.bs.tab", function () {
        $($.fn.dataTable.tables(true)).DataTable()
            .columns.adjust()
            .responsive.recalc();
    });
});


