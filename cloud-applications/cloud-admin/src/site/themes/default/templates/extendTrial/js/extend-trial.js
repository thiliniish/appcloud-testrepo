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

var selectedValue;
var selection;
var submitType;

function doSubmitTenant() {
    submitType = 1;
    var selectedIndex = document.getElementById("subscrptionSelect1").selectedIndex;
    var subscription=$("option")[selectedIndex].value;
    var tenantDomain = $("#tenantDomain").attr('value');
    filterExtendType();
    extendTrial(String(tenantDomain),String(subscription), selection, selectedValue);

}

function filterExtendType() {

    if($("#HourText1").attr('value')!=""){
        selection = 1;
        selectedValue = $("#HourText1").attr('value');
    }
    else if ($("#DayText1").attr('value') != ""){
        selection = 2;
        selectedValue = $("#DayText1").attr('value');
    }
    else if ($("#WeekText1").attr('value') != ""){
        selection = 3;
        selectedValue = $("#WeekText1").attr('value');
    }
    else if ($("#MonthText1").attr('value') != ""){
        selection = 4;
        selectedValue = $("#MonthText1").attr('value');
    }
    else if ($("#dateExtendTenant").attr('value') != ""){
        selection = 5;
        selectedValue = $("#dateExtendTenant").attr('value');
    }
    else if($("#HourText2").attr('value')!=""){
        selection = 1;
        selectedValue = $("#HourText2").attr('value');
    }
    else if ($("#DayText2").attr('value') != ""){
        selection = 2;
        selectedValue = $("#DayText2").attr('value');
    }
    else if ($("#WeekText2").attr('value') != ""){
        selection = 3;
        selectedValue = $("#WeekText2").attr('value');
    }
    else if ($("#MonthText2").attr('value') != ""){
        selection = 4;
        selectedValue = $("#MonthText2").attr('value');
    }
    else if ($("#dateExtendEmail").attr('value') != ""){
        selection = 5;
        selectedValue = $("#dateExtendEmail").attr('value');
    }
}

function doSubmitEmail() {
    submitType =2;
    var subscriptionElement = document.getElementById('subscrptionSelect2'),
        subscription =subscriptionElement.value;

    var tenantDomainElement = document.getElementById('tenant-select'),
        tenantDomain=tenantDomainElement.value;
    filterExtendType();
    extendTrial(tenantDomain,subscription, selection, selectedValue);
}

function extendTrial(tenantDomain,subscription, selection, selectedValue) {

    jagg.post("../blocks/extendTrial/ajax/extendTrial.jag", {
        action:"getEndBillingSubscriptionDate",
        tenantDomain : tenantDomain,
        subscription : subscription
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var resultJSON = JSON.parse(result.data);
            var endDateElement = resultJSON.Dates.Date;

            if(endDateElement==undefined){
                jagg.message({content:'Error! Status of the Cloud Type: '+subscription+' is INVALID or PENDING for the Tenant Domain '+tenantDomain,type:'error'});
                return;
            }
            var endDate = endDateElement.Value;
            jagg.post("../blocks/extendTrial/ajax/extendTrial.jag", {
                action:"updateTenant",
                tenantDomain : tenantDomain,
                endDate : endDate,
                subscription : subscription,
                selection : selection,
                selectedValue : selectedValue
            }, function (result) {
                $('.message_box').empty();
                if(submitType==2){
                    getTenantFromEmail();

                } else if (submitType==1) {
                    getTenantFromDomain();
                }
                result = JSON.parse(result);
                if (!result.error) {
                    jagg.message({content:'User Trial Extended!',type:'success'});
                } else {
                    jagg.message({type:'error',content:'<strong>Error!, </strong>' + result.message});
                }

            });
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!, </strong>' + result.message});
        }
    });
}

function getTenantFromEmail(){
    var tenantDomainElement = document.getElementById('tenant-select'),
        tenantDomain=tenantDomainElement.value;
    checkTenantExist(String(tenantDomain));
}

function getTenantFromDomain(){
    var tenantDomain = $("#tenantDomain").attr('value');
    checkTenantExist(tenantDomain);
}

function checkTenantExist(tenantDomain) {
    jagg.post("../blocks/extendTrial/ajax/extendTrial.jag", {
        action:"checkTenant",
        tenantDomain : tenantDomain
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var validateTenant = $(".validateTenant");
            validateTenant.empty();
            var resultJSON = JSON.parse(result.data);
            var tenant = resultJSON.Tenants.Tenant;
            if(tenant == undefined){
                $('.message_box').empty();
                $('#submitbtn').prop('disabled', true);
                $('#submitbtn2').prop('disabled', true);
                $(".currentEndDate").empty();
                validateTenant.append("<left>Tenant Does not Exist.</left>.");
            } else {
                $('#submitbtn2').prop('disabled', false);
                $('#submitbtn').prop('disabled', false);

                showCurrentExpireDate(resultJSON);
            }
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!</strong>' + result.message});
        }
    });
}

function showCurrentExpireDate(resultJSON){

    var currentEndDate = $(".currentEndDate");
    currentEndDate.empty();
    var tenantsLength= resultJSON.Tenants.Tenant.length;
    if(tenantsLength == undefined){
        currentEndDate.append('Cloud Type : '+resultJSON.Tenants.Tenant.SUBSCRIPTION+', ExpiryDate : '+resultJSON.Tenants.Tenant.END_DATE +'</br>');
    } else {
        for ( var i = 0; i < tenantsLength ; i++ )
        {
            currentEndDate.append('Cloud Type : '+resultJSON.Tenants.Tenant[i].SUBSCRIPTION+', ExpiryDate : '+resultJSON.Tenants.Tenant[i].END_DATE +'</br>');
        }
    }
}

function checkEmailExist() {
    var email = $("#email").attr('value');
    jagg.post("../blocks/extendTrial/ajax/extendTrial.jag", {
        action:"checkEmailExist",
        email : email
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var $optionContainer = $('#tenant-select');
            $optionContainer.empty();
            var resultJSON = JSON.parse(result.data);

            var tenant = resultJSON.Tenants.Tenant;
            if (tenant == undefined || tenant === "" || tenant === "null") {
                var emailvaildation = $(".validateEmailExit");
                emailvaildation.empty();
                emailvaildation.append("<left>Email Does not Exist.</left>");
                $('.message_box').empty();
            } else {
                fillTenantOptionDropDown(tenant);
                $(".validateEmailExit").empty();
                $('.message_box').empty();
            }
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!</strong>' + result.message });
        }

    });
}

function fillTenantOptionDropDown(tenantEntry) {
    var $optionContainer = $('#tenant-select');
    $optionContainer.empty();
    var tenantArray = [];
    //data service returns results in {"Entries":{"Entry":[{"xxx":"333"}, {"yyy":"333"}]}} format.
    //If there is only one object it sends like {"Entries":{"Entry":{"yyy":"333"}}}
    //Entry can be an 'Array' or an 'Object'.
    if (tenantEntry instanceof Array) {
        tenantArray = tenantEntry;
    }
    //When an object is returned.
    else {
        tenantArray.push(tenantEntry);
    }
    var noOfTenants = tenantArray.length;
    for (var i = 0; i < noOfTenants; i++) {
        var $option = $('<option value="' + tenantArray[i].tenantDomain + '">' + tenantArray[i].tenantDomain + '</option>');
        $optionContainer.append($option);
    }
}

$(document).ready(function () {

    $("#HourText").show();
    $("#DayText").hide();
    $("#WeekText").hide();
    $("#MonthText").hide();
    $("#dateID1").hide();

    $("#dateExtendEmail").datepicker({
        minDate: 0,
        dateFormat: 'yy-mm-dd',
        numberOfMonths: 1
    });

    $("#dateExtendTenant").datepicker({
        minDate: 0,
        dateFormat: 'yy-mm-dd',
        numberOfMonths: 1

    });

    $("#extendUserbyTenant").validate({
        onfocusout: false,
        onkeyup : false,

        submitHandler: function() {
        doSubmitTenant();

        }
    });

    $("#extendUserbyEmail").validate({
        onfocusout: false,
        onkeyup : false,

        submitHandler: function() {
            doSubmitEmail();
        }
    });

    $('input:radio').on('click', function() {
        document.getElementById("HourText1").value='';
        document.getElementById("DayText1").value='';
        document.getElementById("WeekText1").value='';
        document.getElementById("MonthText1").value='';
        document.getElementById("dateExtendTenant").value='';
        document.getElementById("HourText2").value='';
        document.getElementById("DayText2").value='';
        document.getElementById("WeekText2").value='';
        document.getElementById("MonthText2").value='';
        document.getElementById("dateExtendEmail").value='';
    });


    $('input[type="radio"]').click(function(){
        if($(this).attr("value")=="two"){
            $(".currentEndDate").empty();
            $("#extendUserbyEmail").hide();
            $("#extendUserbyTenant").show();
        }
        if($(this).attr("value")=="one"){
            $("#extendUserbyTenant").hide();
            $("#extendUserbyEmail").show();
            $(".currentEndDate").empty();
            $("#HourText").hide();
        }
        if($(this).attr("value")=="Hours"){
            $("#dateID1").hide();
            $("#HourText").show();
            $("#DayText").hide();
            $("#WeekText").hide();
            $("#MonthText").hide();
        }if($(this).attr("value")=="Days"){
            $("#dateID1").hide();
            $("#HourText").hide();
            $("#DayText").show();
            $("#WeekText").hide();
            $("#MonthText").hide();
        }if($(this).attr("value")=="Weeks"){
            $("#dateID1").hide();
            $("#HourText").hide();
            $("#DayText").hide();
            $("#WeekText").show();
            $("#MonthText").hide();
        }if($(this).attr("value")=="Months"){
            $("#dateID1").hide();
            $("#HourText").hide();
            $("#DayText").hide();
            $("#WeekText").hide();
            $("#MonthText").show();
        }if($(this).attr("value")=="SpecificDate"){
            $("#dateID1").show();
            $("#HourText").hide();
            $("#DayText").hide();
            $("#WeekText").hide();
            $("#MonthText").hide();
        }if($(this).attr("value")=="Hours2"){
            $("#dateID2").hide();
            $("#HourTextID2").show();
            $("#DayTextID2").hide();
            $("#WeekTextID2").hide();
            $("#MonthTextID2").hide();
        }if($(this).attr("value")=="Days2"){
            $("#dateID2").hide();
            $("#HourTextID2").hide();
            $("#DayTextID2").show();
            $("#WeekTextID2").hide();
            $("#MonthTextID2").hide();
        }if($(this).attr("value")=="Weeks2"){
            $("#dateID2").hide();
            $("#HourTextID2").hide();
            $("#DayTextID2").hide();
            $("#WeekTextID2").show();
            $("#MonthTextID2").hide();
        }if($(this).attr("value")=="Months2"){
            $("#dateID2").hide();
            $("#HourTextID2").hide();
            $("#DayTextID2").hide();
            $("#WeekTextID2").hide();
            $("#MonthTextID2").show();
        }if($(this).attr("value")=="SpecificDate2"){
            $("#dateID2").show();
            $("#HourTextID2").hide();
            $("#DayTextID2").hide();
            $("#WeekTextID2").hide();
            $("#MonthTextID2").hide();
        }
    });
});

