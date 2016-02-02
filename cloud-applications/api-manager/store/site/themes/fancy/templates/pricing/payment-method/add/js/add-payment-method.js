var params;

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

function showPage() {
    var zuoraDiv = document.getElementById('zuora_payment');
    zuoraDiv.innerHTML = "";
    Z.render(params, null, callback);
}

function submitPage() {
    jagg.message({content: 'Please Wait. Your request is being processed..', type: 'info'});
    Z.submit();
}

function generateParameters() {
    var workflowReference = $("#workflowReference").attr('value');
    var tenant = $("#tenant").attr('value');
    jagg.post("/site/blocks/pricing/payment-method/add/ajax/add.jag", {
        action: "generateParams",
        workflowReference: workflowReference,
        tenant: tenant
    }, function (result) {
        if (!result.error) {
            params = result.params;
            showPage();
        } else {
            jagg.message({content: result.message, type: "error"});
        }
    }, "json");
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

$(document).ready(function ($) {
    var error = decodeURIComponent(($("#errorObj").attr('value')));
    var errorObj = JSON.parse(error);

    if (errorObj.error) {
        jagg.message({content: errorObj.errorMessage, type: "error"});
    }
    generateParameters();
});