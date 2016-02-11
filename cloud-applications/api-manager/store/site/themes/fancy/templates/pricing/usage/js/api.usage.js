/**
 *
 * Helper function to add Days to a given date
 *
 * @param days - number of days
 * @returns {Date} - changed date
 */
Date.prototype.addDays = function (days) {
    var dat = new Date(this.valueOf());
    dat.setDate(dat.getDate() + days);
    return dat;
}

var apiData = getAPIData();
var selectedAPI = 'ALL';
var appData = ['ALL'];
var selectedApp = 'ALL';

var endDate = new Date();
var startDate = endDate.addDays(-30);
var selectedEndDate;
var selectedStartDate, isYear;
setSelectedDates(endDate, startDate);

$('.side-pane-trigger').click(function () {
    var rightPane = $('.right-pane');
    var leftPane = $('.left-pane');
    if (rightPane.hasClass('visible')) {
        rightPane.animate({"left": "0em"}, "slow").removeClass('visible');
        leftPane.animate({"left": "-18em"}, "slow");
        $(this).find('i').removeClass('fa-arrow-left').addClass('fa-reorder');
    } else {
        rightPane.animate({"left": "18em"}, "slow").addClass('visible');
        leftPane.animate({"left": "0em"}, "slow");
        $(this).find('i').removeClass('fa-reorder').addClass('fa-arrow-left');
    }
});

/**
 * Select Element for API Usage API list
 **/
var apiUsageAPIList = $('#usage-ds-api-list').select2({
    placeholder: "Search or Select API",
    data: apiData,
    maximumInputLength: 5,
    selectOnBlur: true,
    allowClear: true
});

/**
 * Select Element for API Usage Application list
 **/
var apiUsageApplicationList = $('#usage-ds-application').select2({
    priority: 1,
    placeholder: "Search or Select Application",
    data: appData,
    maximumInputLength: 10,
    selectOnBlur: true,
});

/**
 * Change events for Select 2 elements
 **/
apiUsageAPIList.on("change", function () {
    selectedAPI = $(this).val();
    if (selectedAPI == 'ALL') {
        $('#api-usage-ds-api-list').empty().trigger('change');
        $("#usage-ds-application").empty().append('<option value="ALL">ALL</option>').val('ALL').trigger('change');
        apiUsageApplicationList.select2({data: applicationData});
    } else {
        var applicationData = getAppData(selectedAPI);
        $("#usage-ds-application").empty().append('<option value="' + applicationData[0] + '">' + applicationData[0] + '</option>').val(applicationData[1]).trigger('change');
        console.log(applicationData);
        apiUsageApplicationList.select2({data: applicationData});
    }
    loadDataToChartObjectForGivenTime(usageChart);
});

apiUsageApplicationList.on("change", function () {
    selectedApp = $(this).val();
    loadDataToChartObjectForGivenTime(usageChart);
});


/**
 * Initialization of the API usage chart
 */
var usageChart = c3.generate({
    bindto: '#usage-comparison',
    data: {
        x: 'x',
        columns: [],
        type: 'bar',
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
            type: 'category',
        },
        y: {
            type: 'category',
        }
    }
});
$('.usage-comparison .btn-week').click(function (e) {
    highlightSelected(this);
    endDate = new Date();
    startDate = endDate.addDays(-7);
    setSelectedDates(endDate, startDate);
    addDatesToCurrentAndSet(7, 'days')
    loadDataToChartObjectForGivenTime(usageChart);

});
$('.usage-comparison .btn-month').click(function (e) {
    highlightSelected(this);
    endDate = new Date();
    startDate = endDate.addDays(-30);
    setSelectedDates(endDate, startDate);
    addDatesToCurrentAndSet(1, 'months');
    loadDataToChartObjectForGivenTime(usageChart);

});
$('.usage-comparison .btn-year').click(function (e) {
    highlightSelected(this);
    endDate = new Date();
    startDate = endDate.addDays(-365);
    setSelectedDates(endDate, startDate);
    addDatesToCurrentAndSet(1, 'years');
    isYear = true;
    loadDataToChartObjectForGivenTime(usageChart);

});
function highlightSelected(t) {
    var hClass = "chart-controller-btn-selected";
    $(t).addClass(hClass);
    $(t).siblings().removeClass(hClass);
}
function addDatesToCurrentAndSet(amount, type) {
    $('.btn-calender span').html(moment().subtract(amount, type).format('DD-MM-YYYY')
        + ' - ' + moment().format('DD-MM-YYYY'));
    $('.btn-calender').data('daterangepicker').setStartDate(moment().subtract(amount, type));
    $('.btn-calender').data('daterangepicker').setEndDate(moment());
}
function cb(start, end) {
    $('.btn-calender span').html(start.format('DD-MM-YYYY') + ' - ' + end.format('DD-MM-YYYY'));
}

$('.btn-calender').daterangepicker({
    "singleDatePicker": false,
    "opens": "left",
    "startDate": moment().subtract(29, 'days'),
    "endDate": moment()
}, cb);

$('.datepicker-container .fw-calendar').click(function () {
    $('.btn-calender').data('daterangepicker').toggle();
})

$('.btn-calender').on('apply.daterangepicker', function (ev, picker) {
    startDate = new Date(picker.startDate.format('YYYY-MM-DD')),
        endDate = new Date(picker.endDate.format('YYYY-MM-DD'));
    setSelectedDates(endDate, startDate);
    loadDataToChartObjectForGivenTime(usageChart);
    highlightSelected(this);
});

loadDataToChartObjectForGivenTime(usageChart);

var monthNames = ["January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"];

function getMonthList(from, to) {
    var arr = [];
    var datFrom = new Date('1 ' + from);
    var datTo = new Date('1 ' + to);
    var fromYear = datFrom.getFullYear();
    var toYear = datTo.getFullYear();
    var diffYear = (12 * (toYear - fromYear)) + datTo.getMonth();

    for (var i = datFrom.getMonth(); i <= diffYear; i++) {
        arr.push(monthNames[i % 12] + " " + Math.floor(fromYear + (i / 12)));
    }

    return arr;
}

function loadDataToChartObjectForGivenTime(chartObj) {
    getJsonData(function (response) {
        response = jQuery.parseJSON(response);
        var usageObj = response.message.Entries.Entry;
        var dates = ['x'];
        var value = ['API'];
        if (usageObj === undefined || usageObj == "" || usageObj == null) {
            chartObj.unload();
            $("#show-usage").hide();
            $('#usageLabel').text("No usage data available for " + selectedStartDate + " - " + selectedEndDate);
        } else {
            $('#usageLabel').text("");
            $('#show-usage').show();
            var dateArr = getDates(startDate, endDate);
            var usageDateObj = {};
            //put zero usage for for all the dates
            for (var i = 0; i < dateArr.length; i++) {
                usageDateObj[dateArr[i]] = 0;
            }
            for (var x = 0; x < usageObj.length; x++) {
                var formattedMonth = (usageObj[x].Month).replace(/\b(\d{1})\b/g, '0$1');
                if (isYear == true) {
                    var yearMonth = usageObj[x].Year + "-" + formattedMonth;
                } else {
                    var formattedDay = (usageObj[x].Day).replace(/\b(\d{1})\b/g, '0$1');
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

function getDates(startDate, stopDate) {
    var dateArray = new Array();
    var currentDate = startDate;
    if (isYear) {
        while (currentDate <= stopDate) {
            var formattedDate = moment(new Date(currentDate)).format('YYYY-MM');
            dateArray.push(formattedDate);
            currentDate = moment(currentDate).add('months', 1);
        }
    } else {
        while (currentDate <= stopDate) {
            var formattedDate = moment(new Date(currentDate)).format('YY-MM-DD');
            dateArray.push(formattedDate);
            currentDate = currentDate.addDays(1);
        }
    }
    return dateArray;
};

function isLastDayOfMonth(dt) {
    var test = new Date(dt.getTime());
    test.setDate(test.getDate() + 1);
    return test.getDate() === 1;
}

/**
 *
 * Ajax Wrapper function to load json data
 *
 * @param fileName - This is basically the ajax url
 * @param callback - success callback when the ajax returns
 */
function getJsonData(callback) {
    $.ajax({
        url: "../../blocks/pricing/usage/ajax/get.jag",
        data: {
            "action": "get-usage",
            "api": selectedAPI,
            "appName": selectedApp,
            "startDate": selectedStartDate,
            "endDate": selectedEndDate
        },
        success: callback
    });
}

function getAPIData() {
    var apiNames = [];
    $.ajax({
        url: "../../blocks/pricing/usage/ajax/get.jag",
        data: {
            "action": "get-apis"
        },
        async: false,
        success: function (result) {
            result = jQuery.parseJSON(result);
            if (!result.error) {
                apiNames = result.message;

            }
        }
    });
    return apiNames;
}

function setSelectedDates(endDate, startDate) {
    if ((endDate.getMonth() + 1) < 10) {
        selectedEndDate = endDate.getUTCFullYear() + "-0" + (endDate.getMonth() + 1) + "-"
            + endDate.getDate();
    } else {
        selectedEndDate = endDate.getUTCFullYear() + "-" + (endDate.getMonth() + 1) + "-"
            + endDate.getDate();
    }
    if ((startDate.getMonth() + 1) < 10) {
        selectedStartDate = startDate.getUTCFullYear() + "-0" + (startDate.getMonth() + 1) + "-"
            + startDate.getDate();
    } else {
        selectedStartDate = startDate.getUTCFullYear() + "-" + (startDate.getMonth() + 1) + "-"
            + startDate.getDate();
    }
}

function getAppData(api) {
    var appNames = [];
    $.ajax({
        url: "../../blocks/pricing/usage/ajax/get.jag",
        data: {
            "action": "get-apps",
            "api": api
        },
        async: false,
        success: function (result) {
            result = jQuery.parseJSON(result);
            if (!result.error) {
                appNames = result.message;
            }
        }
    });
    return appNames;
}