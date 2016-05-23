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

function sendTenantDomanExtendingValues(userName,password) {
   jagg.post("../blocks/logIn/ajax/logIn.jag", {
       action:"logIn",
       userName : userName,
       password : password
   },
       function (result) {
           if( result.trim() != "false"){
               window.location.assign("javascript:location.href='/cloudadmin/site/pages/index.jag'");
               $('.message_box').empty();
           } else {
               $('.message_box').empty();
               jagg.message({content:'<strong>Error!</strong> Login Failed ! Please Retry!', type:'error'});
           }
       });
}

function doSubmit() {

    var userName = $("#username").attr('value');
    var password = $("#password").attr('value');
    $('.message_box').empty();
    sendTenantDomanExtendingValues(userName,password);
}
