var accountId;
var currency;
function getCurrencyUsed() {
    $.ajax({
        url: "../../blocks/pricing/account/invoice/ajax/get.jag",
        data: {
            "action": "getCurrency"
        },
        async: false,
        success: function (result) {
            currency = result.trim();
        }
    });
}

function getRequestParam(name) {
    if (name = (new RegExp("[?&]" + encodeURIComponent(name) + "=([^&]*)")).exec(location.search))
        return decodeURIComponent(name[1]);
}

function gotoAccountPage() {
    var tenantDomain = getRequestParam("tenant");
    window.location.href = "account-summary.jag?tenant=" + tenantDomain;
}

$(document).ready(function () {
    var invoiceId = $('input#invoiceId').val();
    getCurrencyUsed();
    $.ajax({
        url: "../../blocks/pricing/account/invoice/ajax/get.jag",
        data: {
            action: "getInvoice",
            id: invoiceId
        },
        success: function (data) {
            var data = jQuery.parseJSON(data);
            var invoiceObj = data.message;
            var invoiceItems = invoiceObj.invoiceItems;
            var fullTotal = 0;
            var fullDiscount = 0;
            var fullAmount = 0;

            for (var i = 0; i < invoiceItems.length; i++) {
                var invoiceItem = invoiceItems[i];
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
                    + '<td id="amount' + i + '">' + invoiceItem.amount + ' ' + currency + '</td>'
                    + '<td id="discount' + i + '">' + displayDiscount + ' ' + currency + '</td>'
                    + '<td id="total' + i + '">' + (invoiceItem.amount + invoiceItem.discount).toFixed(2) + ' ' + currency + '</td>'
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
            if (fullDiscount < 0) {
                document.getElementById('fDiscount').style.color = "red";
            }

            //setting the total pricing values
            $('#fTotal').text(fullTotal.toFixed(2) + currency);
            $('#fDiscount').text(fullDiscount.toFixed(2) + ' ' + currency);
            $('#fAmount').text(fullAmount.toFixed(2) + ' ' + currency);

            //setting the Basic Information
            $('#lblOrg').text(invoiceObj.accountName);
            $('#curr').text(currency);
            $('#lblInvoiceNum').text(invoiceObj.invoiceNumber);
            $('#lblInvoiceDate').text(invoiceObj.invoiceDate);
            $('#lblInvoiceAmount').text(invoiceObj.amount + ' ' + currency);

        }, error: function (jqXHR, textStatus, errorThrown) {
            $('.message_box').empty();
            jagg.message({
                content: "Unable to load the Invoice data at the moment. Please contact WSO2 Cloud Team for help",
                type: 'error',
                cbk: function () {
                    window.location.href = cloudMgtURL + "/site/pages/contact-us.jag";
                }
            });
        }
    });

});
