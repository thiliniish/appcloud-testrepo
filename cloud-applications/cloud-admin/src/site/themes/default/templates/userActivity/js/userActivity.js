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

var columnNumbers;

function include(file)
{
    var script  = document.createElement('script');
    script.src  = file;
    script.type = 'text/javascript';
    script.defer = true;
    document.getElementsByTagName('head').item(0).appendChild(script);
}

include('../themes/default/templates/userActivity/js/userActivityChart.js');

$(document).ready(function () {
    $("#userForm").validate({
        onfocusout: false,
        onkeyup : false,

        submitHandler: function() {
            doSubmitUserHistory();
        }
    });
    $("#fromDate").datepicker({
        dateFormat: 'yy-mm-dd',
        numberOfMonths: 1
    });
    $("#toDate").datepicker({
        dateFormat: 'yy-mm-dd',
        numberOfMonths: 1
    });
    $("#userForm2").validate({
        onfocusout: false,
        onkeyup : false,

        submitHandler: function() {
            doSubmitUserActivity();
        }
    });
    $("#userForm3").validate({
        onfocusout: false,
        onkeyup : false,

        submitHandler: function() {
            doSubmitCategaryUserHistory();
        }
    });
    $('#category').click(function(){
        $("#graph").hide();
    });

});


function getUserActivityEmailValues(userActivityEmailValue) {
    jagg.post("../blocks/userActivity/ajax/userActivity.jag", {
        action:"userActivityEmail",
        userActivityEmailValue : userActivityEmailValue
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            var resultJSON = JSON.parse(result.data);
            var dates = resultJSON.Response.Date;
            if(dates == undefined){
                $("#graph").hide();
                jagg.message( {content:'<strong>Error!</strong> Please Enter An Valid Email Address .', type:'error'});
            } else {
                drawGraph(resultJSON);
                $("#graph").show();
            }
        } else {
            $('.message_box').empty();
            jagg.message( {content:'<strong>Error!</strong>' + result.message , type:'error'});
        }
    });
}

function getUserActivityCategories(category,fromDate,toDate) {

    jagg.post("../blocks/userActivity/ajax/userActivity.jag", {
        action:"userActivityEmailCategories",
        category : category,
        fromDate :fromDate,
        toDate : toDate
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            drawTableCategory(result.data);
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!</strong> ' + result.message });
        }
    });
}

function drawTableCategory(result){
    var resultJSON = JSON.parse(result);
    var user = resultJSON.Users.User;
    if(user == undefined) {
        $('.message_box').empty();
        var table = $("#displayDateCatogariesTable");
        table.empty();
        table.html('<h3 align=center class="cleanable" >No User Activity data is available</h3>');
    }else{
        $("#displayDateCatogariesTable").empty();
        initializeTable(resultJSON);
    }
}

function dateFormat(fromDate){
    var date = new Date(fromDate);
    var day = date.getDate();
    var monthIndex = date.getMonth();
    var year = date.getFullYear();
    date = year + "-" + (monthIndex+1) + "-" + day;
    return date;
}

function initializeTable(resultJSON){
    var user=null;
    var startDate = $("#fromDate");
    var fromDate=new Date(startDate.attr('value'));
    var toDate= new Date($("#toDate").attr('value'));
    columnNumbers=(toDate-fromDate)/(1000*60*60*24);

    mytable = $('<table class="table table-striped table-bordered"></table>').attr({ id: "basicTable" });

    for (var i = 0; i < 1; i++) {
        var row = $('<tr></tr>').attr({ class: ["thead"].join(' ') }).appendTo(mytable);
        for (var j = -1; j <= columnNumbers; j++) {
            fromDate.setDate(fromDate.getDate()+j);
            if(j==-1){
                $('<th></th>').text("User Name").appendTo(row);
            }else{
                $('<th></th>').text(dateFormat(fromDate)).appendTo(row);
            }
        }
    }

    var logDate;
    var numberOfUser= resultJSON.Users.User.length;
    var object;
    var l;
    for (i = 0; i < numberOfUser; i++) {
        var row1 = $('<tr></tr>').attr({ class: ["tbody"].join(' ') }).appendTo(mytable);
        var t=0;
        var newDate=new Date(startDate.attr('value'));
        user = resultJSON.Users.User[i];

        if(user.Dates.Date.length==undefined){
            l=1;
        }else{
            l=user.Dates.Date.length;
        }
        var date =newDate;

        for (j = 0; j <= columnNumbers; j++) {
            var columnId=-1;
            for(var y = 0; y < l; y++){
                object=user.Dates.Date[y];
                if(user.Dates.Date.length==undefined){
                    logDate=new Date(user.Dates.Date.Value);
                }else{
                    logDate=new Date(user.Dates.Date[y].Value);
                }
                var dateObject=new Date(date);
                if(t==0){
                    $('<td></td>').text(user.Name).appendTo(row1);
                }else {
                    if(+logDate==+dateObject){
                        columnId=y;
                    }
                }
                t++;
            }
            if(columnId==-1){
                $('<td></td>').text("-").appendTo(row1);
            }else{
                if(user.Dates.Date.length==undefined){
                    $('<td></td>').text(user.Dates.Date.Count).appendTo(row1);
                }else{
                    $('<td></td>').text(user.Dates.Date[columnId].Count).appendTo(row1);
                }
            }
            date=newDate.setDate(newDate.getDate()+1);
        }
    }
    $("#displayDateCatogariesTable").empty();
    mytable.appendTo("#displayDateCatogariesTable");
}

function doSubmitUserActivity() {

    var userActivityEmail = $("#userActivityEmailValue").attr('value');
    $('.message_box').empty();
    getUserActivityEmailValues(userActivityEmail);

}

function doSubmitCategaryUserHistory() {

    var number = document.getElementById("categorySelect").selectedIndex;
    var fromDate= $("#fromDate").attr('value');
    var toDate= $("#toDate").attr('value');
    var category=$("option")[number].value;
    $('.message_box').empty();

    getUserActivityCategories(category,fromDate,toDate);

}

function displayDataForCategaries(result){

    var noOfEntries;
    var $usageListContainer1 = $('#usageListContainer1');
    var $tenantContainer1 = $('#tenantContainer1');
    var tenantEntries;
    var entry;

    tenantEntries=result;


    if(tenantEntries.Tenants.Tenant==undefined){
        $tenantContainer1.empty();
        $usageListContainer1.empty();
        $tableRecretaionTest = $('<h3 align=center class="cleanable" >No tenant data is available</h3>');
        $usageListContainer1.append($tableRecretaionTest);

    }else{

        $usageListContainer1.empty();
        var $tableRecretaionTest=$('<li class="list_row_item first_list_row_item">'+
            '<ul class="list_row first_list_row ">'+
            '<li class="list_col usage cursover_remover" style="width: 150px;">'+
            '<h2 class="list_col_heading" style="padding-left: 11px; font-size: 14px">Tenant Username</h2>'+
            '</li>'+
            '<li class="list_col usage cursover_remover" style="width: 150px;">'+
            '<h2 class="list_col_heading" style="padding-left: 11px; font-size: 14px">Log Date</h2>'+
            '</li>'+
            '<li class="list_col usage cursover_remover" style="width: 150px;">'+
            '<h2 class="list_col_heading" style="padding-left: 11px; font-size: 14px">Activity Count</h2>'+
            '</li>'+
            '</ul>'+
            '</li>');


        $usageListContainer1.append($tableRecretaionTest);

        if(tenantEntries.Tenants.Tenant.length==undefined){
            $tenantContainer1.empty();
            noOfEntries=1;
            entry = tenantEntries.Tenants.Tenant;

            displayTableForCategories(entry,noOfEntries);

        }else{
            noOfEntries=tenantEntries.Tenants.Tenant.length;
            $tenantContainer1.empty();
            for (var i=0; i <noOfEntries ; i++ ) {
                entry = tenantEntries.Tenants.Tenant[i];
                displayTableForCategories(entry,noOfEntries);
            }
        }
    }
}

function displayTableForCategories(entry){
    var fromDate=new Date($("#fromDate").attr('value'));
    var toDate= new Date($("#toDate").attr('value'));
    columnNumbers=(toDate-fromDate)/(1000*60*60*24);

    var msg=entry;
    var dataDate=new Date(msg.log_date);

    var $tenantContainer1 = $('#tenantContainer1');
    var today = new Date("2015-04-08");

    if(+dataDate==+today){


    }
    var $tableColoumnName=$('<li class="list_row_item cleanable" data-tenant= data-name=>'+
        '<ul class="list_row" id=>'+
        '<li class="list_col  team_role">'+
        '<div style="padding-left: 10px; padding-top: 20px;" class="list_col_content">'+msg.payload_username+'</div>'+
        '</li>'+
        '<li class="list_col  team_role">'+
        '<div style="padding-left: 10px; padding-top: 20px;" class="list_col_content">'+msg.log_date+'</div>'+
        '</li>'+
        '<li  class="list_col  team_role">'+
        '<div style="padding-left: 21px;padding-top: 15px;" class="list_col_content">'+msg.payload_count+'</div>'+
        '</li>'+
        '</ul>'+
        '</li>');

    $tenantContainer1.append($tableColoumnName);
}
