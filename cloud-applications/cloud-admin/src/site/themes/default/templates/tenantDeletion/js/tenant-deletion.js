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

function exportTableToCSV(){

    var table = document.getElementById("tenantDeletionList").innerHTML;
    var data = table.replace(/<thead>/g, '')
        .replace(/<\/thead>/g, '')
        .replace(/<tfoot>/g, '')
        .replace(/<\/tfoot>/g, '')
        .replace(/<tbody>/g, '')
        .replace(/<td>/g, '')
        .replace(/<\/tbody>/g, '')
        .replace(/<\/tr>/g, '\n')
        .replace(/<\/td>/g, ',')
        .replace(/Tenant ID/g, '')
        .replace(/Tenant Domain/g, '')
        .replace(/Email/g, '');

    var reg = /\<.*>/gi;
    data = data.replace(reg,"")
        .replace(/\s+/g, '');

    var detailList = data.split(",");
    var csvData = "";

    for(var i=1; i<detailList.length;i++){
        if(i%3==0){
            csvData+= detailList[i-1]+","+"\r\n";
        }
        else{
            csvData+= detailList[i-1]+",";
        }
    }

    var downloadLink = document.createElement('a');
    downloadLink.download = "Tenant-Deletion-List.csv";
    downloadLink.href = "data:application/csv,"+ escape(csvData);
    downloadLink.click();

}

$(document).ready(function () {
    $('#tenantDeletionList').DataTable({
        "processing": true,
        "serverSide": true,
        "ajax" : "../blocks/tenantDeletion/ajax/tenantDeletion.jag"
    });

});

