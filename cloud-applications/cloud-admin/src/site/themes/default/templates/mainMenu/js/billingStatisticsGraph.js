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

function drawGraph(result){
    var resultJSON = JSON.parse(result);
    var tenantLength = resultJSON.Tenants.Tenant.length;
    var paidTenants = [];
    var trialTenants = [];
    var freeTenants = [];

    for(var i=0; i< tenantLength; i++){
        var tenant = resultJSON.Tenants.Tenant[i];
        if(tenant.TYPE == 'PAID'){
            paidTenants.push(tenant);
        } else if (tenant.TYPE == 'TRIAL'){
            trialTenants.push(tenant);
        } else if (tenant.TYPE == 'FREE'){
            freeTenants.push(tenant);
        }
    }

    var d1_1 = [];
    var d1_2 = [];
    var d1_3 = [];
    var year;
    var month;
    var dateValue;
    var tenantCount;

    for(i=0; i< paidTenants.length; i++){
        year = paidTenants[i].YEAR;
        month = paidTenants[i].MONTH;
        dateValue = getDateObject(year, month);
        tenantCount = paidTenants[i].COUNT;

        d1_1.push([dateValue,tenantCount]);
    }
    for(i=0; i< trialTenants.length; i++){
        year = trialTenants[i].YEAR;
        month = trialTenants[i].MONTH;
        dateValue = getDateObject(year, month);
        tenantCount = trialTenants[i].COUNT;

        d1_2.push([dateValue,tenantCount]);
    }
    for(i=0; i< freeTenants.length; i++){
        year = freeTenants[i].YEAR;
        month = freeTenants[i].MONTH;
        dateValue = getDateObject(year, month);
        tenantCount = freeTenants[i].COUNT;

        d1_3.push([dateValue,tenantCount]);
    }

    var data1 = [
        {
            Label: "Paid Users",
            data: d1_1,
            bars: {
                show: true,
                barWidth: 15*24*60*60*300,
                fill: true,
                lineWidth: 1,
                order: 1,
                fillColor:  "#803300"
            },
            color: "#000000 "
        },
        {
            Label: "Trial Users",
            data: d1_2,
            bars: {
                show: true,
                barWidth: 15*24*60*60*300,
                fill: true,
                lineWidth: 1,
                order: 2,
                fillColor:  "#000099"
            },
            color: "#000000"
        },
        {
            Label: "Free Users",
            data: d1_3,
            bars: {
                show: true,
                barWidth: 15*24*60*60*300,
                fill: true,
                lineWidth: 1,
                order: 3,
                fillColor:  "#006600"
            },
            color: "#000000"
        }
    ];

    var dateNow = new Date();
    var maximumXRange = new Date(dateNow.getTime());
    var previousYearDate = new Date(dateNow.setMonth(dateNow.getMonth()-12));
    var graph = $("#billingGraph");

    $.plot(graph, data1, {
        xaxis: {
            axisLabel: 'No of user registrations for a month',
            size: 12,
            min: previousYearDate.getTime(),
            max: maximumXRange.getTime(),
            mode: "time",
            tickSize: [1, "month"],
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
            axisLabelPadding: 5
        },
        yaxis: {
            axisLabel: 'User Count',
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
            axisLabelPadding: 5
        },
        grid: {
            hoverable: true,
            clickable: false,
            borderWidth: 1
        },
        legend: {
            labelBoxBorderColor: "none",
            position: "right"
        },
        series: {
            shadowSize: 5
        }
    });

    function showTooltip(x, y, contents, z) {
        $('<div id="flot-tooltip">' + contents + '</div>').css({
            top: y - 20,
            left: x - 90,
            'border-color': z
        }).appendTo("body").show();
    }

    function getMonthName(newTimestamp) {
        var d = new Date(newTimestamp);
        var numericMonth = d.getMonth();
        var monthArray = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

        return monthArray[numericMonth];
    }

    graph.bind("plothover", function (event, pos, item) {
        if (item) {
            if (previousPoint != item.datapoint) {
                previousPoint = item.datapoint;
                $("#flot-tooltip").remove();

                var originalPoint;

                if (item.datapoint[0] == item.series.data[0][3]) {
                    originalPoint = item.series.data[0][0];
                } else if (item.datapoint[0] == item.series.data[1][3]){
                    originalPoint = item.series.data[1][0];
                } else if (item.datapoint[0] == item.series.data[2][3]){
                    originalPoint = item.series.data[2][0];
                } else if (item.datapoint[0] == item.series.data[3][3]){
                    originalPoint = item.series.data[3][0];
                } else if (item.datapoint[0] == item.series.data[4][3]){
                    originalPoint = item.series.data[4][0];
                } else if (item.datapoint[0] == item.series.data[5][3]){
                    originalPoint = item.series.data[5][0];
                } else if (item.datapoint[0] == item.series.data[6][3]){
                    originalPoint = item.series.data[6][0];
                } else if (item.datapoint[0] == item.series.data[7][3]){
                    originalPoint = item.series.data[7][0];
                } else if (item.datapoint[0] == item.series.data[8][3]){
                    originalPoint = item.series.data[8][0];
                } else if (item.datapoint[0] == item.series.data[9][3]){
                    originalPoint = item.series.data[9][0];
                } else if (item.datapoint[0] == item.series.data[10][3]){
                    originalPoint = item.series.data[10][0];
                } else if (item.datapoint[0] == item.series.data[11][3]){
                    originalPoint = item.series.data[11][0];
                } else if (item.datapoint[0] == item.series.data[12][3]){
                    originalPoint = item.series.data[12][0];
                }

                var x = getMonthName(originalPoint);
                var y = item.datapoint[1];
                var z = item.series.color;

                showTooltip(item.pageX, item.pageY,
                    "<b>" + item.series.Label + "</b><br/> " + x + " = " + y, z);
            }
        } else {
            $("#flot-tooltip").remove();
            previousPoint = null;
        }
    });
}

function getDateObject(year, month){
    return (new Date(year,month-1)).getTime();
}