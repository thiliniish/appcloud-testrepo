var params;
var isSecondaryPaymentMethod = Boolean(getRequestParam("secondaryPayment"));
var field_passthrough1;
var cardDetails = {};

$(document).ready(function ($) {
    document.getElementById("cardDetails").style.visibility = "hidden";
    document.getElementById("submitbtn").disabled = true;
    document.getElemntById("submitbtn").style. = "#999";
    if (!isSecondaryPaymentMethod) {
        var error = decodeURIComponent(($("#errorObj").attr('value')));
        var errorObj = JSON.parse(error);
        if (errorObj.error) {
            jagg.message({content: errorObj.errorMessage, type: "error"});
        }
    }
    generateParameters();
    $("#redeembtn1").click(function () {
        getCheckoutHandler();
    });
});

var callback = function (response) {
    if (!response.success) {
        $('.message_box').empty();
        jagg.message({
            content: JSON.stringify(response), type: 'error', cbk: function () {
                window.location.href = requestURL + "site/pages/list-apis.jag?tenant=" + encodeURIComponent(tenant);
            }
        });
    }
};

function getRequestParam(name) {
    if (name = (new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)')).exec(location.search))
        return decodeURIComponent(name[1]);
}

function submitPage() {
    var secondaryCC = null;
    var strUrlBasicParam = Object.keys(publicParams).map(function (key) {
        return encodeURIComponent(key) + '=' + encodeURIComponent(publicParams[key]);
    }).join('&');

    var strUrlCardParams = Object.keys(cardDetails).map(function (key) {
        return encodeURIComponent(key) + '=' + encodeURIComponent(cardDetails[key]);
    }).join('&');

    secondaryCC = publicParams.field_passthrough4;
    if (secondaryCC != null && secondaryCC == "secondary-card") {
        addPaymentMethod();
    } else {
        actionString = "&action=createAccount&";
        window.top.location.href = "../../../site/pages/pricing/manage-account.jag?tenant=" +
        encodeURIComponent(tenant) + actionString + window.location.search.split('&')[2] + "&" + strUrlBasicParam
        + "&" + strUrlCardParams;
    }
}

var publicParams = {};
function generateParameters() {
    var workflowReference = $("#workflowReference").attr('value');
    var tenant = $("#tenant").attr('value');
    var accountId = $("#accountId").attr('value');
    var result = jagg.post("/site/blocks/pricing/payment-method/add/ajax/add.jag", {
        action: "generateParameters",
        workflowReference: workflowReference,
        tenant: tenant
    }, function (result) {
        if (!result.error) {
            publicParams = result.params;
            field_passthrough1 = publicParams.field_passthrough1;
            getCheckoutHandler();
            if (accountId != null) {
                publicParams.field_accountId = accountId;
                publicParams.field_passthrough4 = "secondary-card";
                publicParams.field_passthrough3 = tenant;
            }
        } else {
            jagg.message({content: result.message, type: "error"});
        }
    }, "json");
}

function addPaymentMethod() {
    jagg.syncPost("/site/blocks/pricing/payment-method/add/ajax/add.jag", {
        action: "addPaymentMethod",
        tokenId: cardDetails.field_passthrough5
    }, function (results) {
        console.log(results.result);
        actionString = "&action=viewPaymentMethod";
        window.top.location.href = "../../../site/pages/pricing/manage-account.jag?tenant=" + encodeURIComponent(tenant) + actionString +
        '&fieldPassthrough1=' + encodeURIComponent(results.result);
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


//A Custom function is needed
(function () {
    // Your base, I'm in it!
    var originalAddClassMethod = jQuery.fn.addClass;

    jQuery.fn.addClass = function () {
        // Execute the original method.
        var result = originalAddClassMethod.apply(this, arguments);

        // trigger a custom event
        jQuery(this).trigger('elementClassChanged');

        // return the original result
        return result;
    }
})();


$('.myaffix').affix();

var previousWidth = $('.myaffix').css('width');

$('.myaffix').bind('elementClassChanged', function (e) {
    if (e.currentTarget.classList.contains('affix')) {
        $('.myaffix').css('width', parseInt(previousWidth));
    } else if (e.currentTarget.classList.contains('affix-top')) {
        $('.myaffix').removeAttr('style');
    }
});

function getCheckoutHandler() {
    var handler = StripeCheckout.configure({
        key: field_passthrough1,
        image: 'http://b.content.wso2.com/sites/all/cloudmicro/images/icon-wso2.jpg',
        locale: 'auto',
        zipCode: true,
        allowRememberMe: false,
        billingAddress: true,
        panelLabel: 'Submit',
        token: function (response) {
            // You can access the token ID with `token.id`.
            // Get the token ID to your server-side code for use.
            document.getElementById("redeembtn1").style.visibility = "hidden";
            document.getElementById("cardDetails").style.visibility = "visible";
            document.getElementById("submitbtn").disabled = false;
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
            cardDetails.field_passthrough5 = response.id;
        }
    });
    handler.open({
        name: 'Credit Card Information',
        description: 'Processed securely through Stripe',
        email: $("#email").attr('value')
    });

    // Close Checkout on page navigation:
    window.addEventListener('popstate', function () {
        handler.close();
    });
}
