/*
 * Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

$(document).ready(function () {
    var offset = -7;
    var date = new Date(new Date().getTime() + offset * 3600 * 1000).toUTCString().replace(/ GMT$/, " (GMT - 07:00)");
    $('#overview-time').text(date);

    var clientOffset = -420 + new Date().getTimezoneOffset();
    if (clientOffset == 0) {
        $('#client-timezone-offset').text('The shown time zone is the same as yours');
    }
    else {
        $('#client-timezone-offset').text('The shown time zone is ' + getTimeZoneDifference(clientOffset) + ' yours');
    }
    
    var uptimeServiceUrl = $('#uptime-service-url').val();

    $.ajax({
        type: "GET",
        url: uptimeServiceUrl,
        dataType: "json",
        success: function (results) {
            drawCurrentStatus(results);
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            return null;
        }
    });

});

//To refresh page
setTimeout(function(){
    window.location.reload(1);
}, 120000);

function drawCurrentStatus(servers) {
    var html = '';

    $.each(servers, function (index, item) {
        /**
         * Accordion headers
         */
        var raw = '<h3>';
        var statusImage;
        if (item.status == "UP") {
            statusImage = '<img class="status-image" src="images/status/up.png" alt="live"/>';
        } else if (item.status == "DOWN") {
            statusImage = '<img class="status-image" src="images/status/down.png" alt="failure"/>';
        } else if (item.status == "MAINTENANCE") {
            statusImage = '<img class="status-image" src="images/status/maintenance.png" alt="maintenance"/>';
        } else if (item.status == "DISRUPTIONS") {
            statusImage = '<img class="status-image" src="images/status/disruptions.png" alt="warning"/>';
        } else {
            statusImage = '<img class="status-image" src="images/status/not-available.png" alt="warning"/>';
        }
        raw = raw + statusImage;
        raw = raw + item.server;
        raw = raw + '<span class="last-update-date">Updated: ' + item.lastUpdated + '</span></h3>';

        /**
         * Content Summary Table
         */
        var summaryTable = '<div class="accordion-table"><div class="service-status">' +
            '<table cellspacing="0" border="0" class="content">' +
            '<tr class="heading">' +
            '<td class="status">' + statusImage + '</td>' +
            '<td class="service">Last Checked <br/> ' + item.lastUpdated + '</td>' +
            '<td class="uptime-last-7-days">Uptime Last 7 days <br/>' + item.sevenDayUptime + '</td>' +
            '</tr></table></div></div>';

        /**
         * Content Last Seven day history
         */
        var lastSevenDailyStatuses = item.sevenDayStatus;
        var lastSevenDays = '<table class="day-table"><tr>';

        //Dates
        var dates = "";
        for (var i = 0; i < lastSevenDailyStatuses.length; i++) {
            dates = dates + '<th class="day-table-td"> ' + lastSevenDailyStatuses[i].date + ' </th>';
        }
        lastSevenDays = lastSevenDays + dates + "</tr><tr>";

        //Statuses
        var statuses = "";
        for (var i = 0; i < lastSevenDailyStatuses.length; i++) {
            if (lastSevenDailyStatuses[i].status == "UP") {
                statuses = statuses + '<td class="day-table-td" title="Uptime: '
                    + lastSevenDailyStatuses[i].uptimePercentage + '%">' +
                    '<img class="status-image" src="images/status/up.png" alt="live"/></td>';
            } else if (lastSevenDailyStatuses[i].status == "DOWN") {
                statuses = statuses + '<td class="day-table-td" title="Uptime: '
                    + lastSevenDailyStatuses[i].uptimePercentage + '%">' +
                    '<img class="status-image" src="images/status/down.png" alt="failure"/></td>';
            } else if (lastSevenDailyStatuses[i].status == "MAINTENANCE") {
                statuses = statuses + '<td class="day-table-td" title="Uptime: '
                    + lastSevenDailyStatuses[i].uptimePercentage + '%">' +
                    '<img class="status-image" src="images/status/maintenance.png" alt="maintenance"/></td>';
            } else if (lastSevenDailyStatuses[i].status == "DISRUPTIONS") {
                statuses = statuses + '<td class="day-table-td" title="Uptime: '
                    + lastSevenDailyStatuses[i].uptimePercentage + '%">' +
                    '<img class="status-image" src="images/status/disruptions.png" alt="warning"/></td>';
            } else {
                statuses = statuses + '<td class="day-table-td">' +
                    '<img class="status-image" src="images/status/not-available.png" alt="warning"/></td>';
            }
        }
        lastSevenDays = lastSevenDays + statuses + "</tr></table>";

        raw = raw + '<div>' + summaryTable + lastSevenDays + '</div>';
        html = html + raw;
    });

    $("#accordion").html(html);

    $("#accordion").accordion({
        collapsible: true,
        active: false,
        heightStyle: "content"
    });
    $(function () {
        $(document).tooltip({
            position: {
                my: "center bottom-10",
                at: "center top",
                using: function (position, feedback) {
                    $(this).css(position);
                    $("<div>")
                        .addClass("arrow")
                        .addClass(feedback.vertical)
                        .addClass(feedback.horizontal)
                        .appendTo(this);
                }
            }
        });
    });
}

function getTimeZoneDifference(mins) {
    var sign = Math.abs(mins) / mins;

    mins = Math.abs(mins);
    var hrs = Math.floor(mins / 60);
    mins -= hrs * 60;

    var text = '';
    if (hrs) {
        text += hrs;
        if (mins) {
            text += ':' + mins + ' hours';
        }
        else {
            if (hrs == 1) {
                text += ' hour';
            }
            else {
                text += ' hours';
            }
        }
    }
    else {
        text += mins + ' minutes';
    }

    if (sign > 0) {
        text += ' ahead of';
    }
    else {
        text += ' behind';
    }
    return text;
}