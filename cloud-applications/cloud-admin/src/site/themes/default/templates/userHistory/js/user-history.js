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

function getUserTrialHistory(tenantDomain) {
    jagg.post("../blocks/userHistory/ajax/userHistory.jag", {
            action:"checkTenantDetails",
            tenantDomain : tenantDomain
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            $('.message_box').empty();
            displayTable(result.data);
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!, </strong> ' + result.message });
        }
    });
}

function doSubmitUserHistory() {
    var tenantDomain = $("#tenantDomain").attr('value');
    getUserTrialHistory(tenantDomain);
}

function displayTable(result){
    var resultJSON = JSON.parse(result);
    var tenantDomain;
    var subscription;
    var type;
    var status;
    var startDate;
    var endDate;
    var tenant = resultJSON.Tenants.Tenant;
    var table = $('#userHistoryTable');
    table.DataTable().clear().draw();

    if (tenant == undefined) {
        return;
    }
    var tenantListSize = tenant.length;
    if (tenantListSize == undefined) {
        tenantDomain = resultJSON.Tenants.Tenant.TENANT_DOMAIN;
        subscription = resultJSON.Tenants.Tenant.SUBSCRIPTION;
        type = resultJSON.Tenants.Tenant.TYPE;
        status = resultJSON.Tenants.Tenant.STATUS;
        startDate = resultJSON.Tenants.Tenant.START_DATE;
        endDate = resultJSON.Tenants.Tenant.END_DATE;

        table.DataTable().row.add([
            tenantDomain, subscription,type,status,startDate,endDate
        ]).draw();
    } else {
        for(var i=0; i<tenantListSize; i++){
            tenantDomain = resultJSON.Tenants.Tenant[0].TENANT_DOMAIN;
            subscription = resultJSON.Tenants.Tenant[0].SUBSCRIPTION;
            type = resultJSON.Tenants.Tenant[0].TYPE;
            status = resultJSON.Tenants.Tenant[0].STATUS;
            startDate = resultJSON.Tenants.Tenant[0].START_DATE;
            endDate = resultJSON.Tenants.Tenant[0].END_DATE;

            table.DataTable().row.add([
                tenantDomain, subscription,type,status,startDate,endDate
            ]).draw();
        }
    }
}

$(document).ready(function () {

    $("#userForm").validate({
        onfocusout: false,
        onkeyup : false,
        submitHandler: function() {
            doSubmitUserHistory();
        }
    });
    $('#userHistoryTable').DataTable({
        bFilter: false,
        bPaginate: false,
        bLengthChange: false
    });
});
