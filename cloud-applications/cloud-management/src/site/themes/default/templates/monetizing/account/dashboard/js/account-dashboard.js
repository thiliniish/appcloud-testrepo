/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var selectedEndDate = null;
var selectedStartDate = null;
$(document).ready(function () {
    Date.prototype.addDays = function (days) {
        var date = new Date(this.valueOf());
        date.setDate(date.getDate() + days);
        return date;
    };
    updateSubscriberDetails();
    updateProductDetails();
    var d = new Date();
    d.setMonth(d.getMonth() - 1);
    updateStatsDetails();
});

function updateSubscriberDetails() {
    jagg.post("../blocks/monetizing/account/info/ajax/get.jag", {
        action: "get-subscribers-of-tenant"
    }, function (result) {
        if (!result.error) {
            var usersList = JSON.parse(result);
            selectedUserId = usersList[0].id;
            $('#subscriber-count').text(usersList.length);
        } else {
            showErrorMessage(result);
        }
    });
}

function updateStatsDetails() {
    var endDate = new Date();
    var startDate = new Date();
    startDate.setMonth(endDate.getMonth() - 1);
    setSelectedDates(endDate, startDate);
    jagg.post("../blocks/monetizing/subscriber/usage/get/ajax/get.jag", {
        action: "getSubscriberUsage",
        "userId": "*",
        "api": "*",
        "applicationName": "*",
        "fromDate": selectedStartDate,
        "toDate": selectedEndDate
    }, function (result) {
        if (!result.error) {
        } else {
            showErrorMessage(result);
        }
    });

}
function setSelectedDates(endDate, startDate) {
    if ((endDate.getMonth() + 1) < 10) {
        selectedEndDate = endDate.getUTCFullYear() + "-0" + (endDate.getMonth() + 1);
    } else {
        selectedEndDate = endDate.getUTCFullYear() + "-" + (endDate.getMonth() + 1);
    }
    if ((endDate.getDate() ) < 10) {
        selectedEndDate = selectedEndDate + "-0" + endDate.getDate();
    } else {
        selectedEndDate = selectedEndDate + "-" + endDate.getDate();
    }
    if ((startDate.getMonth() + 1) < 10) {
        selectedStartDate = startDate.getUTCFullYear() + "-0" + (startDate.getMonth() + 1);
    } else {
        selectedStartDate = startDate.getUTCFullYear() + "-" + (startDate.getMonth() + 1);
    }
    if ((startDate.getDate() ) < 10) {
        selectedStartDate = selectedStartDate + "-0" + startDate.getDate();
    } else {
        selectedStartDate = selectedStartDate + "-" + startDate.getDate();
    }
}
/**
 * Initialization of the API usage chart
 */
var apiUsageChart = c3.generate({
    bindto: '#api-usage',
    data: {
        x: 'x',
        columns: [],
        type: 'bar'
    },
    tooltip: {
        show: false
    },
    bar: {
        width: {
            ratio: 0.5
        }
    },
    padding: {
        top: 20,
        right: 30,
        bottom: 0,
        left: 70,
    },
    axis: {
        x: {
            type: 'category',
        },
        y: {
            type: 'category',
        }
    }
});
var drawChart = loadDataToChartObjectForGivenTime(apiUsageChart);
function loadDataToChartObjectForGivenTime(chartObj) {
    getJsonData(function (response) {
        response = jQuery.parseJSON(response);
        var usageObj = response.usageObj;
        var dates = ["x"];
        var value = ["API"];
        if (usageObj === undefined || usageObj == "" || usageObj == null) {
            chartObj.unload();
            $("#show-usage").hide();
            if ($("#api-usage-ds-application").prop("disabled")) {
                $("#usageLabelTxt").text("No subscribed apis available for the selected user");
            } else {
                $("#usageLabelTxt").text("No usage data available for " + selectedStartDate + " - " + selectedEndDate);
            }
        } else {
            $("#usageLabelTxt").text("");
            $("#show-usage").show();
            var dateArr = getDates(new Date(selectedStartDate), new Date(selectedEndDate));
            var usageDateObj = {};
            //put zero usage for for all the dates
            for (var i = 0; i < dateArr.length; i++) {
                usageDateObj[dateArr[i]] = 0;
            }
            for (var x = 0; x < usageObj.length; x++) {
                var formattedMonth = (usageObj[x].Month).replace(/\b(\d{1})\b/g, "0$1");
                var formattedDay = (usageObj[x].Day).replace(/\b(\d{1})\b/g, "0$1");
                var yearMonth = formattedMonth + "/" + formattedDay;

                usageDateObj[yearMonth] = Number(usageDateObj[yearMonth]) + Number(usageObj[x].TotalCount);
            }
            for (var key in usageDateObj) {
                dates.push(key);
                value.push(usageDateObj[key]);
            }
            chartObj.load({
                columns: [dates, value],
            });
            chartObj.transform("bar");
        }
    })
}

/**
 *
 * Ajax Wrapper function to load json data
 *
 * @param callback - success callback when the ajax returns
 */

function getJsonData(callback) {
    var endDate = new Date();
    var startDate = new Date();
    startDate.setMonth(endDate.getMonth() - 1);
    setSelectedDates(endDate, startDate);
    $.ajax({
        url: "../blocks/monetizing/subscriber/usage/get/ajax/get.jag",
        data: {
            "action": "getSubscriberUsage",
            "userId": "*",
            "api": "*",
            "applicationName": "*",
            "fromDate": selectedStartDate,
            "toDate": selectedEndDate
        },
        success: callback
    });
}

function getDates(startDate, stopDate) {
    var dateArray = new Array();
    var currentDate = startDate;

    while (currentDate <= stopDate) {
        var formattedDate = moment(new Date(currentDate)).format("MM/DD");
        dateArray.push(formattedDate);

        currentDate = currentDate.addDays(1);
    }
    return dateArray;
}

function updateProductDetails() {
    jagg.post("../blocks/monetizing/account/dashboard/ajax/get.jag", {
        action: "get-product-plan-count"
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            $('#plan-count').text(result.data);
        } else {
            showErrorMessage(result);
        }
    });
}

function showErrorMessage(result) {
    noty({
        theme: 'wso2',
        layout: 'topCenter',
        type: 'error',
        closeWith: ['button', 'click'],
        text: result.message,
        buttons: [{
            addClass: 'btn btn-default', text: 'Ok', onClick: function () {
                window.location.href = ( result.redirectionURL);
            }
        }],
        animation: {
            open: {height: 'toggle'}, // jQuery animate function property object
            close: {height: 'toggle'}, // jQuery animate function property object
            easing: 'swing', // easing
            speed: 500 // opening & closing animation speed
        }
    });
};
$(".side-pane-trigger").click(function () {
    var rightPane = $(".right-pane");
    var leftPane = $(".left-pane");
    if (rightPane.hasClass("visible")) {
        rightPane.animate({"left": "0em"}, "slow").removeClass("visible");
        leftPane.animate({"left": "-18em"}, "slow");
        $(this).find("i").removeClass("fa-arrow-left").addClass("fa-reorder");
    } else {
        rightPane.animate({"left": "18em"}, "slow").addClass("visible");
        leftPane.animate({"left": "0em"}, "slow");
        $(this).find("i").removeClass("fa-reorder").addClass("fa-arrow-left");
    }
});