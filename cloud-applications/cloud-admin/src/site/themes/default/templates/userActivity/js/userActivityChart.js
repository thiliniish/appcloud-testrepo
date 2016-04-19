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

function drawGraph(resultJSON){

    var dateLength = resultJSON.Response.Date.length;
    var dates = resultJSON.Response.Date;
    var d1_1 = [];
    var logDate;
    var dateValue;
    var payloadCount;

    if(dateLength == undefined){
        logDate = dates.log_date;
        dateValue = new Date(logDate).getTime();
        payloadCount = dates.payload_count;

        d1_1.push([dateValue,payloadCount]);
    }
    for(var i=0; i< dateLength; i++){
        logDate = dates[i].log_date;
        dateValue = new Date(logDate).getTime();
        payloadCount = dates[i].payload_count;

        d1_1.push([dateValue,payloadCount]);
    }
    var data1 = [
        {
            data: d1_1,
            bars: {
                show: true,
                barWidth: 15*24*60*60*10,
                fill: true,
                lineWidth: 1,
                order: 3,
                fillColor:  "#4572A7"
            },
            color: "#4572A7"
        }
    ];

    var dateNow = new Date();
    var maximumXRange = new Date(dateNow.getTime());
    var previousMonthDate = new Date(dateNow.setMonth(dateNow.getMonth()-1));


    $.plot($("#graph"), data1, {
        xaxis: {
            min: previousMonthDate.getTime(),
            max: maximumXRange.getTime(),
            mode: "time",
            tickSize: [2, "day"]
        },
        yaxis: {
            axisLabel: 'Activity Count',
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
            shadowSize: 1
        }
    });

    function showTooltip(x, y, contents, z) {
        $('<div id="flot-tooltip">' + contents + '</div>').css({
            top: y - 20,
            left: x - 90,
            'border-color': z,
        }).appendTo("body").show();
    }

    $("#graph").bind("plothover", function (event, pos, item) {
        if (item) {
            if (previousPoint != item.datapoint) {
                previousPoint = item.datapoint;
                $("#flot-tooltip").remove();

                var y = item.datapoint[1];
                var z = item.series.color;

                showTooltip(item.pageX, item.pageY, "<b> Count </b><br/> " + y, z);
            }
        } else {
            $("#flot-tooltip").remove();
            previousPoint = null;
        }
    });
}