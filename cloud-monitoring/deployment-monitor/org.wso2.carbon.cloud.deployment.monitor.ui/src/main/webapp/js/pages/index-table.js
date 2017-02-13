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

    $.ajax({
        type: "GET",
        url: "http://localhost:9090/status/current/all",
        dataType: "json",
        success: function(results){
            drawCurrentStatus(results);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown){
            alert("Error");
        }
    });
});

function drawCurrentStatus(servers) {
    var tableHtml = '<table cellspacing="0" border="0" class="content">' +
        '<tr class="heading">' +
        '<td class="service">Service</td>' +
        '<td class="status"></td>' +
        '<td class="date-time">Last Updated</td>' +
        '</tr>';
    $.each(servers, function (index, item) {
        var tr = '<tr class="parent-row">' +
            '<td class="service">' + item.server + '</td>';

        if(item.status == "UP"){
            tr = tr + '<td class="status"><img src="images/live.png" alt="live"></td>';
        } else if(item.status == "DOWN") {
            tr = tr + '<td class="status"><img src="images/failure.png" alt="failure"></td>';
        } else if(item.status == "DISRUPTIONS"){
            tr = tr + '<td class="status"><img src="images/live-with-error.png" alt="warning"></td>';
        } else if(item.status == "MAINTENANCE"){
            tr = tr + '<td class="status"><img src="images/live-with-error.png" alt="warning"></td>';
        }
        tr = tr + '<td class="date-time-sub">' + item.lastUpdated + '</td>' +
            '</tr>';
        tableHtml = tableHtml + tr;
    });
    tableHtml = tableHtml + '</table>';
    $("#service-status").html(tableHtml);
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