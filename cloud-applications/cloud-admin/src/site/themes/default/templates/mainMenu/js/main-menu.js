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

// Following 5 lines of code is added for reloading the index page if URL contains only /cloudadmin. Since
var url = window.location.href;
var urlSplits = url.split("/");
if (urlSplits[urlSplits.length-1] == "") {
    window.location.assign("javascript:location.href='/cloudadmin/site/pages/index.jag'");
}

function getBillingSubscriptionAnalysisData() {

    jagg.post("../blocks/mainMenu/ajax/mainMenu.jag", {
        action:"getBillingSubscriptionAnalysisData"
    },
    function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            $('.message_box').empty();
            drawGraph(result.data);
        } else {
            $('.message_box').empty();
            jagg.message(
                {type:'error',content:'<strong>Error!, </strong> Error While Getting Billing Subscription Data.'});
        }
    });
}

function getUserCount() {

    jagg.post("../blocks/mainMenu/ajax/mainMenu.jag", {
            action:"getUserData"
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            $('.message_box').empty();
            var resultJSON = JSON.parse(result.data);
            var entry = resultJSON.Entries.Entry;
            if(entry == undefined){
                $('#paidCount').text("-");
                $('#trialCount').text("-");
                $('#freeCount').text("-");
            } else {
                $('#paidCount').text(entry[0].COUNT);
                $('#trialCount').text(entry[1].COUNT);
                $('#freeCount').text(entry[2].COUNT);
            }
        } else {
            $('.message_box').empty();
            jagg.message({type:'error',content:'<strong>Error!, </strong>' + result.message});
        }
    });
}

function getTenantDeletionUserCount() {

    jagg.post("../blocks/mainMenu/ajax/mainMenu.jag", {
            action:"getTenantDeletionUserCount"
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            $('.message_box').empty();
            var resultJSON = JSON.parse(result.data);
            var entry = resultJSON.Entries.Entry;
            if(entry == undefined){
                $('#deleteCount').text("-");
            } else {
                $('#deleteCount').text(entry.COUNT);
            }
        } else {
            $('.message_box').empty();
            jagg.message({
                type:'error',content:'<strong>Error!, </strong>' + result.message});
            }
        }
    );
}

$(document).ready(function () {
    getBillingSubscriptionAnalysisData();
    getUserCount();
    getTenantDeletionUserCount();
});
