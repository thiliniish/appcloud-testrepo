/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

$(document).ready(function () {
    getWelcomeMsg();
});

function subscription(type, domain) {
    // I have not done this in a separate thread as it is not supported by older versions of firefox and chrome.
    jagg.post("../blocks/subscriptions/ajax/subscriptions.jag", {
            action: "addCloudUserSubscription",
            type: type,
            domain: domain
        },
        function (result) {

        },
        function (jqXHR, textStatus, errorThrown) {
        });
    return true;
}

function redirectToCloud(url) {
    location.href = url;
}

function getWelcomeMsg() {
    var username = $("#username").attr('value');
    jagg.post("../blocks/subscriptions/ajax/subscriptions.jag", {
        action: "getWelcomeMsg",
        username: username
    }, function (result) {
        var welcomeMsg = result;
        $('#welcomeMsg').html('<h1>'+ welcomeMsg + '</h1> <div class="description">Select a Cloud to get ' +
        'started.</div>');
    });
}
