/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

function doSubmitDate() {
    var year = $("#year").attr('value');
    var month = $("#month").attr('value');
    var isYearOnly = false;
    if($("#useYearOnly").is(":checked")) {
        isYearOnly = true;
    }
    jagg.post("../blocks/dasDataPurge/ajax/dasDataPurge.jag", {
        action: "purgeDataBasedOnDate",
        year: year,
        month: month,
        isYearOnly: isYearOnly
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var response = result.data;
            if (response == true) {
                $("#dateBasedPurgeNotification").html("<span class='label label-success'>" + "Data Purge has" +
                    " successfully " +
                    "completed." + "</span>");
            } else {
                $("#dateBasedPurgeNotification").html("<span class='label label-danger'>" + "Data Purge was not" + 
                    " successful." + "</span>");
            }
        } else {
            jagg.message({type:'error', content:'<strong>Error!</strong>' + result.message});
        }
    });
}

function doSubmitTenant() {
    var tenantDomain = $("#tenantDomain").attr('value');

    jagg.post("../blocks/dasDataPurge/ajax/dasDataPurge.jag", {
        action: "purgeDataBasedOnTenant",
        tenantDomain: tenantDomain
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var response = result.data;
            if (response == true) {
                $("#tenantBasedPurgeNotification").html("<span class='label label-success'>" + "Data Purge has" +
                    " successfully " + "completed."+"</span>");
            } else {
                $("#tenantBasedPurgeNotification").html("<span class='label label-danger'>" + "Data Purge was not" +
                    " successful." + "</span>");
            }
        } else {
            jagg.message({type:'error',content:'<strong>Error!</strong>' + result.message});
        }
    });
}

$(document).ready(function () {
    $('input[type="radio"]').click(function() {
        if ($(this).attr("value") == "two") {
            $("#purgeDataByDate").hide();
            $("#purgeDataByTenant").show();
            $("#tenantBasedPurgeNotification").html("");
        }
        if ($(this).attr("value") == "one") {
            $("#purgeDataByTenant").hide();
            $("#purgeDataByDate").show();
            $("#dateBasedPurgeNotification").html("");
        }
    });

    $('#useYearOnly').click(function () {
        if($(this).is(":checked")){
            $('#month').prop( "disabled", true );
        } else {
            $('#month').prop( "disabled", false );
        }
    });

});