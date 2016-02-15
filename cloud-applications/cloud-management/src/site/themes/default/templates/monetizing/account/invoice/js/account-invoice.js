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

$(document).ready(function () {
    var invoiceId = $('#invoiceId').val();
    var accountId = $('#accountId').val();
    $.ajax({
        url: "../blocks/monetizing/account/invoice/ajax/get.jag",
        data: {
            action: "getInvoice",
            invoiceId: invoiceId,
            accountId: accountId
        },
        success: function (result) {
            result = JSON.parse(result);
            if(!result.error) {
                var invoiceObj = result.data;
                var orderedInvoiceList = invoiceObj.invoiceItems;
                var fullTotal = 0;
                var fullDiscount = 0;
                var fullAmount = 0;

                for (var index in orderedInvoiceList) {
                    // Append an extra row to the table according to the subscription
                    $("#payments-tbody").append($('<tr> <td colspan="6"><b>' + "API - " +
                        invoiceObj.subscriptionData[index][0] +'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'
                        + "Rate Plan - " +  invoiceObj.subscriptionData[index][1]
                        + '</b></td></tr>'));

                    for (var i = 0; i < orderedInvoiceList[index].length; i++) {
                        var invoiceItem = orderedInvoiceList[index][i];
                        if (invoiceItem.amount < 0) {
                            invoiceItem.chargeName = invoiceItem.chargeName + " -- Proration Credit"
                        }
                        var displayDiscount = invoiceItem.discount;
                        if (invoiceItem.discount == 0) {
                            displayDiscount == "";
                        }
                        $("#payments-tbody").append($('<tr>'
                            + '<td>' + invoiceItem.chargeDate + '</td>'
                            + '<td>' + invoiceItem.chargeName + '</td>'
                            + '<td>' + invoiceItem.servicePeriod + '</td>'
                            + '<td id="amount' + i + '">' + invoiceItem.amount + ' ' + "USD" + '</td>'
                            + '<td id="discount' + i + '">' + displayDiscount + ' ' + "USD" + '</td>'
                            + '<td id="total' + i + '">' + (invoiceItem.amount + invoiceItem.discount).toFixed(2) + ' ' + "USD" + '</td>'
                        ));
                        fullDiscount += parseFloat(invoiceItem.discount);
                        fullAmount += parseFloat(invoiceItem.amount);
                        fullTotal += parseFloat(invoiceItem.amount + invoiceItem.discount);
                        if (invoiceItem.amount < 0) {
                            document.getElementById(("amount" + i)).style.color = "red";
                        }
                        if (invoiceItem.discount < 0) {
                            document.getElementById("discount" + i).style.color = "red";
                        }
                        if ((invoiceItem.amount + invoiceItem.discount) < 0) {
                            document.getElementById("total" + i).style.color = "red";
                        }
                    }
                }
                if (fullDiscount < 0) {
                    document.getElementById('fDiscount').style.color = "red";
                }

                //setting the total pricing values
                $('#fTotal').text(fullTotal.toFixed(2) + "USD");
                $('#fDiscount').text(fullDiscount.toFixed(2) + ' ' + "USD");
                $('#fAmount').text(fullAmount.toFixed(2) + ' ' + "USD");

                //setting the Basic Information
                $('#lblOrg').text(invoiceObj.accountName);
                $('#curr').text("USD");
                $('#lblInvoiceNum').text(invoiceObj.invoiceNumber);
                $('#lblInvoiceDate').text(invoiceObj.invoiceDate);
                $('#lblInvoiceAmount').text(invoiceObj.amount + ' ' + "USD");
            }
            else{
                showErrorMessage(result);
            }
        }
    });
});

function showErrorMessage(result){
    noty({
        theme:'wso2',
        layout: 'topCenter',
        type: 'error',
        closeWith: ['button','click'],
        text: result.message,
        buttons: [{addClass: 'btn btn-default', text: 'Ok', onClick: function(){
            window.location.href = ( result.redirectionURL);}
        }],
        animation: {
            open: {height: 'toggle'}, // jQuery animate function property object
            close: {height: 'toggle'}, // jQuery animate function property object
            easing: 'swing', // easing
            speed: 500 // opening & closing animation speed
        }
    });
}
