var paymentData, jsonObj;
var accountId;
var lastPayment;
var lastPaymentDate;

$(document).ready(function () {
    $.ajax({
        url: "../../blocks/pricing/account/info/ajax/get.jag",
        data: {
            "action": "get-billing-account"
        },
        success: function (result) {
            result = jQuery.parseJSON(result);
            var tenantDomain = result.tenantDomain;
            if (!result.error) {
                var accountObj = jQuery.parseJSON(result.message);
                var dataObj = accountObj.data;
                var accountSummary = dataObj.accountSummary;
                var contactDetails = dataObj.contactDetails;
                var invoiceDetails = dataObj.invoicesInformation;
                var paymentMethodDetails = dataObj.defaultPaymentDetails;
                var subscriptionDetails = dataObj.subscriptionDetails;
                var chargeInformation = dataObj.chargeInformation;
                var ZERO_PAYMENT_VALUE = 0;
                accountId = accountObj.id;
                contactInfo = dataObj.contactDetails;
                accountName = accountSummary.accountName;
                lastPayment = accountSummary.lastPaymentAmount;
                if (lastPayment == null) {
                    lastPayment = ZERO_PAYMENT_VALUE;
                }

                for (i = 0; i < invoiceDetails.length; i++) {
                    invoiceDetails[i] = JSON.parse(invoiceDetails[i]);
                }

                // set account summary
                $('#tenantDomain').val(tenantDomain);
                $('#accBalance').text(accountSummary.accountBalance);
                $('#accName').text(accountName);
                $('#defaultPaymentMethod').val(paymentMethodDetails.paymentId);
                $('#accountName').val(accountName);

                // set contact info
                $('#fname').text(contactDetails.firstName);
                $('#state').text(contactDetails.state);
                $('#lname').text(contactDetails.lastName);
                $('#postalcode').text(contactDetails.postalcode);
                $('#address').text(contactDetails.address1 + " " + contactDetails.address1);
                $('#address1').val(contactDetails.address1);
                $('#address2').val(contactDetails.address2);
                $('#country').text(contactDetails.country);
                $('#city').text(contactDetails.city);
                $('#email').text(contactDetails.email);

                // set credit card info
                $('#paymentMethodType').text(paymentMethodDetails.paymentMethodType);
                $('#paymentType').text(paymentMethodDetails.paymentType);
                $('#ccNum').text(paymentMethodDetails.cardNumber);
                var creditCardExpirationMonth = paymentMethodDetails.expirationMonth;
                var creditCardExpirationYear = paymentMethodDetails.expirationYear;
                if (creditCardExpirationMonth == null || creditCardExpirationYear == null) {
                    $('#ccExpiry').text('');
                } else {
                    $('#ccExpiry').text(creditCardExpirationMonth + " / " + creditCardExpirationYear);
                }


                $("#invoice-info").DataTable({
                    responsive: true,
                    "data": invoiceDetails,
                    "columns": [
                        {"data": "date", "width": "20%", "sClass": "dt-body-right"},
                        {
                            "data": "InvoiceId", "width": "20%", "sClass": "dt-body-right",
                            "render": function (data, type, full, meta) {
                                return "<a class='editroles' onclick='return goToInvoicePage(\"" + full['InvoiceId'] + "\",\""
                                    + tenantDomain + "\")'' >" + full['InvoiceId'] + "</a> ";
                            }
                        },
                        {"data": "TargetDate", "width": "20%", "sClass": "dt-body-right"},
                        {
                            "data": "Amount", "width": "15%", "sClass": "dt-body-right",
                            "render": function (data, type, full, meta) {
                                return full['Amount'] / 100;
                            }
                        },
                        {
                            "data": "paid", "width": "15%", "sClass": "dt-body-right",
                            "render": function (data, type, full, meta) {
                                if(full['paid']){
                                    return "PAID";
                                }
                                else {
                                    return "Pending Payment";
                                }
                            }
                        }
                    ]
                });

                for (i = 0; i < chargeInformation.length; i++) {
                    chargeInformation[i] = JSON.parse(chargeInformation[i]);
                }

                $("#payments-info").DataTable({
                    responsive: true,
                    "data": chargeInformation,
                    "columns": [
                        {"data": "type", "width": "20%", "sClass": "dt-body-right"},
                        {"data": "effectiveDate", "width": "20%", "sClass": "dt-body-right"},
                        {"data": "paymentNumber", "width": "20%", "sClass": "dt-body-right"},
                        {
                            "data": "invoiceNumber.", "width": "20%", "sClass": "dt-body-right",
                            "render": function (data, type, full, meta) {
                                var paidInvoices = full['invoiceNumber'];
                                return paidInvoices;
                            }
                        },
                        {"data": "Status", "width": "20%", "sClass": "dt-body-right"}
                    ]
                });
                /**
                 *
                 * Ths code segment will hide all the accordion content
                 * and bind a click event to fire when clicked on accordion
                 * header to show content
                 *
                 * @type {*|jQuery}
                 */
                var allPanels = $(".accordion .box-content").show();
                $(".accordion .box-header").click(function () {
                    $this = $(this);
                    $target = $this.next();
                    if (!$target.hasClass("active")) {
                        $target.addClass("active").slideDown();
                        $this.find("i").removeClass("fw-right").addClass("fw-down");
                    } else {
                        $target.removeClass("active").slideUp();
                        $this.find("i").removeClass("fw-down").addClass("fw-right")
                    }
                    return false;
                });
            } else {
                $(".message_box").empty();
                jagg.message({
                    content: "Unable to load the account details at the moment. Please contact WSO2 Cloud Team for help",
                    type: "error",
                    cbk: function () {
                        window.location.href = result.cloudmgtURL + "/site/pages/contact-us.jag";
                    }
                });

            }
        }
    });
});
function viewPaymentMethods(obj) {
    var form = $('<form action="manage-account.jag?tenant=' + obj.getElementById("tenantDomain").value  + '&fieldPassthrough1=' + encodeURIComponent(obj.getElementById("defaultPaymentMethod").value) +'"' + 'method="post">' +
        '<input type="hidden" name="action" value="viewPaymentMethod"/>' +
        '</form>');
    $('body').append(form);
    $(form).submit();
};
function addNewPaymentMethod(obj) {
    var form = $('<form action="manage-account.jag?secondaryPayment=true&tenant=' + obj.getElementById("tenantDomain").value
        + '"' + 'method="post">' +
        '<input type="hidden" name="accountId" value="' + obj.getElementById("accountId").value + '"/>' +
        '<input type="hidden" name="action" value="paymentMethod"/>' +
        '</form>');
    $('body').append(form);
    $(form).submit();
};
function updateContactInfo(obj) {
    $('body').append(formContactInfo);
    var formContactInfo = $('<form action="manage-account.jag?tenant=' + obj.getElementById("tenantDomain").value  + '"'
    + 'method="post">' +
    '<input type="hidden" name="accountName" value = "' + obj.getElementById("accName").innerHTML + '"/>' +
    '<input type="hidden" id ="firstName" name="firstName" value = "' + obj.getElementById("fname").innerHTML + '"/>' +
    '<input type="hidden" name="lastName" value = "' + obj.getElementById("lname").innerHTML + '"/>' +
    '<input type="hidden" name="city" value = "' + obj.getElementById("city").innerHTML + '"/>' +
    '<input type="hidden" name="country" value = "' + obj.getElementById("country").innerHTML + '"/>' +
    '<input type="hidden" name="address1" value = "' + obj.getElementById("address1").value + '"/>' +
    '<input type="hidden" name="address2"value = "' + obj.getElementById("address2").value + '"/>' +
    '<input type="hidden" name="state" value = "' + obj.getElementById("state").innerHTML + '"/>' +
    '<input type="hidden" name="postalcode" value="' + obj.getElementById("postalcode").innerHTML + '"/>' +
    '<input type="hidden" name="email" value="' + obj.getElementById("email").innerHTML + '"/>' +
    '<input type="hidden" name="action" value = "editUserInfo"/>' +
    '</form>')
    $(formContactInfo).submit();
};
function goToInvoicePage(id, tenantDomain) {
    var formInvoice = $('<form action="invoice.jag?tenant=' + tenantDomain + '"' + ' method="post">' +
        '<input type="hidden" name="invoiceId" value="' + id + '"/>' +
        '<input type="text" name="accountNm" value="' + $('input#accountName').val() + '"/>' +
        '</form>');
    $('body').append(formInvoice);
    $(formInvoice).submit();
};



