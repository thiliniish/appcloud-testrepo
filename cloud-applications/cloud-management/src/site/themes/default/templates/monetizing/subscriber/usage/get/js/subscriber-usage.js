var isYear = false;
var userData = null;
var selectedUser = null;
var apiData = null;
var selectedApi = null;
var appData = null;
var selectedApp = null;
var endDate = null;
var startDate = null;
var selectedEndDate = null;
var selectedStartDate = null;

$(document).ready(function () {

    Date.prototype.addDays = function (days) {
        var date = new Date(this.valueOf());
        date.setDate(date.getDate() + days);
        return date;
    };
    isYear = false;
    userData = getUserData();
    selectedUser = userData[0].id;
    apiData = getAPIData(selectedUser);
    selectedApi = apiData[0].id;
    appData = getAppData(selectedUser, selectedApi);
    selectedApp = appData[0].id;
    endDate = new Date();
    startDate = endDate.addDays(-29);
    setSelectedDates(endDate, startDate);
    /**
     * Select Element for API Usage Users list
     **/
    var apiUsageUserList = $("#api-usage-ds-user-name").select2({
        placeholder: "Search or Select Subscriber",
        data: userData,
        maximumInputLength: 5,
        selectOnBlur: true
    });

    /**
     * Select Element for API Usage API list
     **/
    var apiUsageAPIList = $("#api-usage-ds-api-list").select2({
        placeholder: "Search or Select API",
        data: apiData,
        maximumInputLength: 5,
        selectOnBlur: true
    });

    /**
     * Select Element for API Usage Application list
     **/
    var apiUsageApplicationList = $("#api-usage-ds-application").select2({
        placeholder: "Search or Select Application",
        data: appData,
        maximumInputLength: 5,
        selectOnBlur: true
    });

    /**
     * Change events for Select 2 elements
     **/

    apiUsageUserList.on("change", function (e) {
        selectedUser = $(this).val();
        var apiData = getAPIData(selectedUser);
        if (apiData == null || apiData == "") {
            $("#api-usage-ds-api-list").empty();
            $("#api-usage-ds-api-list").select2({data: [{"text": "No subscribed apis", "id": "0"}]});
            $("#api-usage-ds-api-list").prop("disabled", true).trigger("change");
        }
        else {
            if ($("#api-usage-ds-api-list").prop("disabled")) {
                $("#api-usage-ds-api-list").prop("disabled", false);
            }
            apiUsageAPIList.empty();
            apiUsageAPIList.select2({data: apiData}).trigger("change");
        }
    });

    apiUsageAPIList.on("change", function () {
        selectedApi = $(this).val();
        if (selectedApi == null || selectedApi == "") {
            $("#api-usage-ds-application").empty();
            $("#api-usage-ds-application").prop("disabled", true);
            $("#api-usage-ds-application").select2({data: [{"text": "No applications", "id": "0"}]}).trigger("change");
        }
        else {
            selectedUser = $("#api-usage-ds-user-name").val();
            var applicationData = getAppData(selectedUser, selectedApi);
            if (applicationData == null || applicationData == "") {
                $("#api-usage-ds-application").empty();
                $("#api-usage-ds-application").prop("disabled", true);
                $("#api-usage-ds-application").select2({
                    data: [{
                        "text": "No applications",
                        "id": "0"
                    }]
                }).trigger("change");
            } else {
                if ($("#api-usage-ds-application").prop("disabled")) {
                    $("#api-usage-ds-application").prop("disabled", false);
                }
                apiUsageApplicationList.empty();
                apiUsageApplicationList.select2({data: applicationData}).trigger("change");
            }
        }
    });
    apiUsageApplicationList.on("change", function () {
        selectedApp = $(this).val();
        loadDataToChartObjectForGivenTime(apiUsageChart, true);
    });
    loadDataToChartObjectForGivenTime(apiUsageChart);
});


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

function getDates(startDate, stopDate) {
    var dateArray = new Array();
    var currentDate = startDate;
    if (isYear) {
        while (currentDate <= stopDate) {
            var formattedDate = moment(new Date(currentDate)).format("YYYY-MM");
            dateArray.push(formattedDate);
            currentDate = moment(currentDate).add("months", 1);
        }
    } else {
        while (currentDate <= stopDate) {
            var formattedDate = moment(new Date(currentDate)).format("YY-MM-DD");
            dateArray.push(formattedDate);
            currentDate = currentDate.addDays(1);
        }
    }
    return dateArray;
};

/**
 * Initialization of the API usage chart
 */
var apiUsageChart = c3.generate({
    bindto: "#api-usage",
    data: {
        x: "x",
        columns: [],
        type: "bar",
        labels: true
    },
    tooltip: {
        show: false
    },
    bar: {
        width: {
            ratio: 0.4
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
            type: "category",
            label: {
                text: "Date",
                position: "outer-middle"
            }
        },
        y: {
            type: "category",
            label: {
                text: "Count",
                position: "outer-middle"
            }
        }
    }
});

$(".api-usage .btn-week").click(function (e) {
    isYear = false;
    highlightSelected(this);
    endDate = new Date();
    startDate = endDate.addDays(-7);
    setSelectedDates(endDate, startDate);
    addDatesToCurrentAndSet(7, "days");
    loadDataToChartObjectForGivenTime(apiUsageChart);

});

$(".api-usage .btn-month").click(function (e) {
    isYear = false;
    highlightSelected(this);
    endDate = new Date();
    startDate = endDate.addDays(-29);
    setSelectedDates(endDate, startDate);
    addDatesToCurrentAndSet(29, "days");
    loadDataToChartObjectForGivenTime(apiUsageChart);
});

$(".api-usage .btn-year").click(function (e) {
    isYear = true;
    highlightSelected(this);
    endDate = new Date();
    startDate = endDate.addDays(-366);
    setSelectedDates(endDate, startDate);
    addDatesToCurrentAndSet(366, "days")
    loadDataToChartObjectForGivenTime(apiUsageChart);
});

function highlightSelected(t) {
    var hClass = "chart-controller-btn-selected";
    $(t).addClass(hClass);
    $(t).siblings().removeClass(hClass);
}

function addDatesToCurrentAndSet(amount, type) {
    $(".btn-calender span").html(moment().subtract(amount, type).format("YYYY-MM-DD")
        + " - " + moment().format("YYYY-MM-DD"));
    $(".btn-calender").data("daterangepicker").setStartDate(moment().subtract(amount, type));
    $(".btn-calender").data("daterangepicker").setEndDate(moment());
}

function cb(start, end) {
    $(".btn-calender span").html(start.format("YYYY-MM-DD") + " - " + end.format("YYYY-MM-DD"));
}
cb(moment().subtract(29, "days"), moment());

$(".btn-calender").daterangepicker({
    "singleDatePicker": false,
    "opens": "left",
    "startDate": moment().subtract(29, "days"),
    "endDate": moment()
}, cb);

$(".datepicker-container .fw-calendar").click(function () {
    $(".btn-calender").data("daterangepicker").toggle();
})
$(".btn-calender").on("apply.daterangepicker", function (ev, picker) {
    startDate = new Date(picker.startDate.format("YYYY-MM-DD")),
        endDate = new Date(picker.endDate.format("YYYY-MM-DD"));
    setSelectedDates(endDate, startDate);
    loadDataToChartObjectForGivenTime(apiUsageChart);
    highlightSelected(this);
});


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
                if (isYear == true) {
                    var yearMonth = usageObj[x].Year + "-" + formattedMonth;
                } else {
                    var formattedDay = (usageObj[x].Day).replace(/\b(\d{1})\b/g, "0$1");
                    var formattedYear = (usageObj[x].Year).toString().substr(2, 2);
                    var yearMonth = formattedYear + "-" + formattedMonth + "-" + formattedDay;
                }
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
 * Helper function to add Days to a given date
 *
 * @param days - number of days
 * @returns {Date} - changed date
 */

function isLastDayOfMonth(dt) {
    var test = new Date(dt.getTime());
    test.setDate(test.getDate() + 1);
    return test.getDate() === 1;
}

/**
 *
 * Ajax Wrapper function to load json data
 *
 * @param callback - success callback when the ajax returns
 */

function getJsonData(callback) {
    $.ajax({
        url: "../blocks/monetizing/subscriber/usage/get/ajax/get.jag",
        data: {
            "action": "getSubscriberUsage",
            "userId": selectedUser,
            "api": selectedApi,
            "applicationName": selectedApp,
            "fromDate": selectedStartDate,
            "toDate": selectedEndDate
        },
        success: callback
    });
}

function getAPIData(userId) {
    var apiNames = [];
    $.ajax({
        url: "../blocks/monetizing/subscriber/usage/get/ajax/get.jag",
        data: {
            "action": "getSubscribedApisOfUser",
            "userId": userId
        },
        async: false,
        success: function (result) {
            result = jQuery.parseJSON(result);
            if (!result.error) {
                apiNames = result.apiObj;
            }
        }
    });
    return apiNames;
};
function getUserData() {
    var users = [];
    $.ajax({
        url: "../blocks/monetizing/subscriber/usage/get/ajax/get.jag",
        data: {
            "action": "getApimSubscriberIdsOfTenant"
        },
        async: false,
        success: function (result) {
            result = jQuery.parseJSON(result);
            if (!result.error) {
                users = result.subObj;

            }
        }
    });
    return users;
};

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
function getAppData(userId, api) {
    var appNames = [];
    $.ajax({
        url: "../blocks/monetizing/subscriber/usage/get/ajax/get.jag",
        data: {
            "action": "getApiApplicationsOfUser",
            "api": api,
            "userId": userId
        },
        async: false,
        success: function (result) {
            result = jQuery.parseJSON(result);
            if (!result.error) {
                appNames = result.appObj;
            }
        }
    });
    return appNames;
}
