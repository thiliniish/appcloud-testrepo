/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
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

function getDateFromDateTime(date){
    var dateString = date.split(' ');
    var day = new Date(dateString[0]);
    return day;
}

function getdateDifference(fromDate,toDate){
    var date1 = new Date(fromDate);
    var date2 = new Date(toDate);
    var timeDiff = Math.abs(date2.getTime() - date1.getTime());
    var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
    return diffDays;
}

function drawChart(accountUsage,fromDate,toDate){

    var $charDataContainer= $('.charData');
    $charDataContainer.append('<section class="separator cleanable"><div class="content"><div id="placeholder"></div>'+
    '</div></section>');

    var dataArray= new Array();
    var dataLimitArray= new Array();
    var oldArray = new Array();
    fromDate =getDateFromDateTime(fromDate);
    toDate = getDateFromDateTime(toDate);
    var currentMaxCalls =accountUsage.entry[0].maxUsage;
    for(var index=0; index < accountUsage.entry.length; index++ ){
        var accountEntry = accountUsage.entry[index];
        var date= getDateFromDateTime(accountEntry.date);
        dataArray.push( [date,accountEntry.calls] );

        if(accountEntry.maxUsage != currentMaxCalls){
            oldArray.push([date,currentMaxCalls] );
            dataLimitArray.push(oldArray);
            oldArray= new Array();
            currentMaxCalls = accountEntry.maxUsage;
            oldArray.push( [date,currentMaxCalls] );
        }else{
            oldArray.push( [date,currentMaxCalls] );
        }

    }
    if(oldArray.length != 0){
        dataLimitArray.push(oldArray);
    }
    var plotData= new Array();
    //setting the Usage Limit data
    for(index = 0; index < dataLimitArray.length; index++){
        plotData.push({label: "Usage Limit -"+dataLimitArray[index][0][1] , data: dataLimitArray[index], color: '#ED561B'});
    }
    plotData.push({label: "API Calls", data: dataArray,
        points: { symbol: "circle", fillColor: "#058DC7", radius: 3, show: true,fill: true },
        color: '#058DC7'
    });
    //adding chart data
    var plot = $.plot($("#placeholder"), plotData,{
        xaxis: {
            mode: "time",
            timeformat: "%m/%d",
            max:toDate,
            min:fromDate,
            tickSize: [1, "day"],
            axisLabel: "Date",
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial',
            axisLabelPadding: 10,
            zoomRange: [0, null],
            panRange: [fromDate, toDate]

        },
        yaxis: {
            axisLabel: 'API Calls',
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
            axisLabelPadding: 5,
            zoomRange: [0, null],
            panRange: [0, null]
        },
        zoom: {
            interactive: true
        },
        pan: {
            interactive: true
        },
        series: {
            lines: { show: true }
        },
        grid: {
            hoverable: true,
            borderWidth: 1
        }

    });
    // adding zoom out button
    $("<div class='button' style='position:absolute;left:75px;top:10px'>zoom out</div>")
        .appendTo($("#placeholder"))
        .click(function (event) {
            event.preventDefault();
            plot.zoomOut();
        });

    // panning arrows
    function addArrow(dir, left, top, offset) {
        $("<img class='button' src='../themes/default/new-assets/img/arrow-" + dir + ".gif' style='position:absolute; left:" + left + "px;top:" + top + "px'>")
            .appendTo($("#placeholder"))
            .click(function (e) {
                e.preventDefault();
                plot.pan(offset);
            });
    }
    //setting up the arrows
    addArrow("left", 70, 47, { left: -100 });
    addArrow("right", 100, 47, { left: 100 });
    addArrow("up", 85, 32, { top: -100 });
    addArrow("down", 85, 60, { top: 100 });

    function convertToDate(timestamp) {
        var newDate = new Date(timestamp);
        var dateString = (newDate.getMonth()+1) + "/" + newDate.getDate();
        return dateString;
    }

    var previousPoint = null;

    function showTooltip(x, y, contents, z) {
        $('<div id="flot-tooltip">' + contents + '</div>').css({
            top: y - 30,
            left: x - 135,
            'border-color': z,
        }).appendTo("body").fadeIn(200);
    }

    $("#placeholder").bind("plothover", function (event, pos, item) {
        if (item) {
            if ((previousPoint != item.dataIndex) || (previousLabel != item.series.label)) {
                previousPoint = item.dataIndex;
                previousLabel = item.series.label;
                $("#flot-tooltip").remove();

                var x = convertToDate(item.datapoint[0]),
                    y = item.datapoint[1];
                    z = item.series.color;

                showTooltip(item.pageX, item.pageY,
                    "<b>" + item.series.label + " - "+ y + "</b><br />  On " + x , z);
            }
        } else {
            $("#flot-tooltip").remove();
            previousPoint = null;
        }
    });
}
