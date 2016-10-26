var field_passthrough1;
var publicParam = {};
var cardDetails = {};
var monthlyRental = $("#monthlyRental").attr('value');
var productRatePlanId = $("#productRatePlanId").attr('value');
var accountId = $("#accountId").attr('value');

$(document).ready(function ($) {
    // Check for billing enable/disable mode
    var isBillingEnabled = $("#isBillingEnabled").attr('value');

    if (isBillingEnabled) {
        document.getElementById("cardDetails").style.visibility = "hidden";
        showErrorMessage();
        getKeys();
        getCheckoutHandler();
        var clickwithblur = false;
        $("#submitbtn").click(function () {
            submitPage();
        });
        $("#redeembtn").click(function () {
            calculateDiscount();
        });
        $("#btnAddCardDetails").click(function () {
            getCheckoutHandler();
        });
        $('#coupon').keydown(function (event) {
            if (event.keyCode === 13) {
                calculateDiscount();
                return false;
            }
        });
        $('#submitbtn').mousedown(function () {
            clickwithblur = true;
        });
        $('#submitbtn').mouseup(function () {
            clickwithblur = false;
        });
        $('#backbtn').click(function () {
            if (confirm("Are you sure you want to navigate away from this page?")) {
                history.go(-1);
            }
            return false;
        });
        $('[data-toggle="tooltip"]').tooltip();

        $("[data-toggle=popover]").popover();

        $(".ctrl-asset-type-switcher").popover({
            html: true,
            content: function () {
                return $('#content-asset-types').html();
            }
        });
        $(".ctrl-filter-type-switcher").popover({
            html: true,
            content: function () {
                return $('#content-filter-types').html();
            }
        });
        $('#nav').affix({
            offset: {
                top: $('header').height()
            }
        });
    } else {
        var cloudMgtURL = $("#cloudmgtURL").attr('value');
        var unavailableErrorPage = $("#unavailableErrorPage").attr('value');
        window.location.href = cloudMgtURL + unavailableErrorPage;
    }

});

function submitPage() {
    var secondaryCC = null;
    if (accountId != null && accountId != "") {
            secondaryCC = true;
    }
    var strUrlBasicParam = Object.keys(publicParam).map(function (key) {
        return encodeURIComponent(key) + '=' + encodeURIComponent(publicParam[key]);
    }).join('&');

    var strUrlCardParams = Object.keys(cardDetails).map(function (key) {
        return encodeURIComponent(key) + '=' + encodeURIComponent(cardDetails[key]);
    }).join('&');

    if (secondaryCC) {
       addPaymentMethod();
        window.top.location.href = "../../site/pages/payment-methods.jag?secondary-card=success";
    } else {
        window.top.location.href = "../../site/pages/add-billing-account.jag?responseFrom=Response_From_Submit_Page&success=true&"
            + strUrlBasicParam + "&" + strUrlCardParams;
    }
};

function addPaymentMethod() {
    jagg.syncPost("../blocks/billing/method/add/ajax/add.jag", {
        action: "addPaymentMethod",
        tokenId: cardDetails.field_passthrough4
    }, function (results) {
    // No action Needed
    }, function (jqXHR, textStatus, errorThrown) {
        $('.message_box').empty();
        jagg.message({
            content: "Unable to add a new payment method at the moment. Please contact WSO2 Cloud Team for help",
            type: 'error',
            cbk: function () {
                var cloudMgtURL = $("#cloudmgtURL").attr('value');
                window.location.href = cloudMgtURL + "/site/pages/contact-us.jag";
            }
        });
    });
}


function getKeys() {
    jagg.syncPost("../blocks/billing/method/add/ajax/add.jag", {
        action: "getParams",
        serviceId: "api_cloud",
        productRatePlanId: productRatePlanId
    }, function (results) {
        publicParam = results;

        if ('<%= accountId %>' != "") {
            //TODO this is for the update billing method
        }
        field_passthrough1 = results.field_passthrough1;

    }, function (jqXHR, textStatus, errorThrown) {
        $('.message_box').empty();
        jagg.message({
            content: "Unable to add a new payment method at the moment. Please contact WSO2 Cloud Team for help",
            type: 'error',
            cbk: function () {
                var cloudMgtURL = $("#cloudmgtURL").attr('value');
                window.location.href = cloudMgtURL + "/site/pages/contact-us.jag";
            }
        });
    });
}

function disable() {
    document.getElementById("spinner").style.display = '';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color', '#F9BFBB');
    submitButton.disabled = true;
}

function enable() {
    document.getElementById("spinner").style.display = 'none';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color', '#428BCA');
    submitButton.disabled = false;
}

function showErrorMessage() {
    var errorValue = $("#errorMessage").attr('value');
    if (errorValue != "NA") {
        $('.message_box').empty();
        jagg.message({content: errorValue, type: 'error'});
        return false;
    }
}

function calculateDiscount() {
    jagg.post("../blocks/billing/account/add/ajax/add.jag", {
            action: "calculateDiscount",
            couponData: $("#coupon").attr('value')
        },
        function (result) {
            document.getElementById("spinner").style.display = 'none';
            couponDiscount = result;
            if (result <= 0) {
                document.getElementById('couponValidator').innerHTML = "You have provided an invalid coupon.";
            } else {
                document.getElementById('couponValidator').innerHTML = " ";
            }

            $('#discountPara').text(Number(couponDiscount).toFixed(2));
            document.getElementById("payable-ammount").innerHTML = Number(monthlyRental - couponDiscount).toFixed(2);
        },
        function (jqXHR, textStatus, errorThrown) {
            $('.message_box').empty();
            jagg.message({
                content: 'Error in calculating discount. Please contact WSO2 Cloud Team for help',
                type: 'error',
                cbk: function () {
                    window.location.href = cloudmgtURL;
                }
            });
        });
}
function getCheckoutHandler() {
    var handler = StripeCheckout.configure({
        key: field_passthrough1,
        image: 'http://b.content.wso2.com/sites/all/cloudmicro/images/icon-wso2.jpg',
        locale: 'auto',
        billingAddress: true,
        panelLabel: 'Submit',
        token: function (response) {
            document.getElementById("btnAddCardDetails").style.visibility = "hidden";
            document.getElementById("cardDetails").style.visibility = "visible";
            $("#paymentType").text(response.card.brand);
            $("#ccName").text(response.card.name);
            $("#ccNum").text("************" + response.card.last4);
            $("#ccExpiary").text(response.card.exp_month + " / " + response.card.exp_year);

            cardDetails.creditCardAddress1 = response.card.address_line1;
            cardDetails.creditCardAddress2 = response.card.address_line2;
            cardDetails.creditCardPostalCode = response.card.address_zip;
            cardDetails.creditCardCountry = response.card.address_country;
            cardDetails.creditCardCity = response.card.address_city;
            cardDetails.creditCardState = response.card.address_state;
            cardDetails.field_passthrough4 = response.id;
        }
    });
    handler.open({
        name: 'WSO2 Cloud'
    });

    // Close Checkout on page navigation:
    window.addEventListener('popstate', function () {
        handler.close();
    });
}

