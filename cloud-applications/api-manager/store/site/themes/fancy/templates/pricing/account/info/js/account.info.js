var invoiceData;
var paymentData, jsonObj;
var accountId;
var lastPayment;

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
                invoiceData = accountObj.invoices;
                paymentData = accountObj.payments;
                accountId = accountObj.basicInfo.id;
                lastPayment = accountObj.basicInfo.lastPaymentAmount;
                if (lastPayment == null) {
                    lastPayment = ZERO_PAYMENT_VALUE;
                }
                $("#accountId").val(accountId);
                $("#tenantDomain").val(tenantDomain);
                $("#address1").val(accountObj.billToContact.address1);
                $("#address2").val(accountObj.billToContact.address2);
                // set account summary
                $("#accName").text(accountObj.basicInfo.name);
                $("#lastPayment").text(lastPayment + " " + accountObj.basicInfo.currency);
                $("#accBalance").text(accountObj.basicInfo.balance);
                $("#lastPaymentDate").text(accountObj.basicInfo.lastPaymentDate);
                $("#lastInvoice").text(accountObj.basicInfo.lastInvoiceDate);
                // set contact info
                $("#fname").text(accountObj.billToContact.firstName);
                $("#state").text(accountObj.billToContact.state);
                $("#lname").text(accountObj.billToContact.lastName);
                $("#postalcode").text(accountObj.billToContact.zipCode);
                $("#address").text(accountObj.billToContact.address1 + " " + accountObj.billToContact.address2);
                $("#country").text(accountObj.billToContact.country);
                $("#city").text(accountObj.billToContact.city);
                $("#email").text(accountObj.billToContact.workEmail);
                // set credit card info
                $("#paymentMethodType").text(String(accountObj.basicInfo.defaultPaymentMethod.paymentMethodType)
                    .replace(/([A-Z])/g, ' $1'));
                $("#paymentType").text(accountObj.basicInfo.defaultPaymentMethod.creditCardType);
                $("#ccNum").text(accountObj.basicInfo.defaultPaymentMethod.creditCardNumber);
                $("#ccExpiary").text(accountObj.basicInfo.defaultPaymentMethod.creditCardExpirationMonth + " / " +
                    accountObj.basicInfo.defaultPaymentMethod.creditCardExpirationYear);
                $("#invoice-info").DataTable({
                    responsive: true,
                    "data": invoiceData,
                    "columns": [
                        {"data": "invoiceDate", "width": "20%", "sClass": "dt-body-right"},
                        {
                            "data": "invoiceNumber", "width": "20%", "sClass": "dt-body-right",
                            "render": function (data, type, full, meta) {
                                return "<a class='editroles' onclick='return goToInvoicePage(\"" + full['id'] + "\",\""
                                    + tenantDomain + "\")'' >" + full['invoiceNumber'] + "</a> ";
                            }
                        },
                        {"data": "dueDate", "width": "20%", "sClass": "dt-body-right"},
                        {"data": "amount", "width": "15%", "sClass": "dt-body-right"},
                        {"data": "balance", "width": "15%", "sClass": "dt-body-right"},
                        {"data": "status", "width": "10%", "sClass": "dt-body-right"}
                    ]
                });
                $("#payments-info").DataTable({
                    responsive: true,
                    "data": paymentData,
                    "columns": [
                        {"data": "paymentType", "width": "20%", "sClass": "dt-body-right"},
                        {"data": "effectiveDate", "width": "20%", "sClass": "dt-body-right"},
                        {"data": "paymentNumber", "width": "20%", "sClass": "dt-body-right"},
                        {
                            "data": "paidInvoices.", "width": "20%", "sClass": "dt-body-right",
                            "render": function (data, type, full, meta) {
                                var paidInvoices = full['paidInvoices'];
                                return paidInvoices[0].invoiceNumber;
                            }
                        },
                        {"data": "status", "width": "20%", "sClass": "dt-body-right"}
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
    var form = $('<form action="manage-account.jag?tenant=' + obj.getElementById("tenantDomain").value  + '"' + 'method="post">' +
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
    var formContactInfo = $('<form action="manage-account.jag?tenant=' + obj.getElementById("tenantDomain").value  + '"'
        + 'method="post">' +
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
    $('body').append(formContactInfo);
    $(formContactInfo).submit();
};
function goToInvoicePage(id, tenantDomain) {
    var formInvoice = $('<form action="invoice.jag?tenant=' + tenantDomain + '"' + ' method="post">' +
        '<input type="hidden" name="invoiceId" value="' + id + '"/>' +
        '</form>');
    $('body').append(formInvoice);
    $(formInvoice).submit();
};



