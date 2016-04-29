$(document).ready(function ($) {
    isUpdate();
    var clickwithblur;
    $('#addPaymentPlans').validate({
        onfocusout: false,
        onkeyup: false,
        rules: {
            overageCharge: {
                required: true,
                number: true
            },
            overageLimit: {
                required: true,
                number: true
            },
            planDescription: {
                required: true,
                number: true
            },
            dailyUsage: {
                required: true,
                number: true
            }
        },
        submitHandler: function (form) {
            doSubmit();
        }
    });
});
function doSubmit() {
    document.getElementById("spinner").style.display = '';
    document.getElementById("btn_addPaymentPlan").disabled = true;
    var planName = $("#planName").attr('value');
    var pricing = $("#pricing").attr('value');
    var overageCharge = $("#overageCharge").attr('value');
    var overageLimit = $("#overageLimit").attr('value');
    var dailyUsage = $("#dailyUsage").attr('value');
    var planDescription;
    jagg.syncPost("../blocks/monetizing/productPlan/update/ajax/update.jag", {
            action: "is-update-payment-plans-of-tenant",
            planName: planName
        },
        function (result) {
            if (result) {
                planDescription = planName + " Plan.";
                jagg.post("../blocks/monetizing/productPlan/update/ajax/update.jag", {
                        action: "update-payment-plans-of-tenant",
                        planName: planName,
                        pricing: pricing,
                        planDescription: planDescription,
                        overageCharge: overageCharge,
                        dailyUsage: dailyUsage,
                        overageLimit: overageLimit
                    },
                    function (result) {
                        result = JSON.parse(result);
                        if (!result.error) {
                            document.getElementById("spinner").style.display = 'none';
                            document.getElementById("btn_addPaymentPlan").disabled = false;
                            var message = "Successfully updated the payment plan";
                            showMessage(message, "success", result.redirectionURL);
                        } else {
                            showMessage(result.message, "error", result.redirectionURL);
                        }
                    });
            } else {
                planDescription = planName + " Plan updated.";
                jagg.post("../blocks/monetizing/productPlan/add/ajax/add.jag", {
                        action: "add-payment-plans-of-tenant",
                        planName: planName,
                        pricing: pricing,
                        planDescription: planDescription,
                        overageCharge: overageCharge,
                        dailyUsage: dailyUsage,
                        overageLimit: overageLimit
                    },
                    function (result) {
                        result = JSON.parse(result);
                        if (!result.error) {
                            document.getElementById("spinner").style.display = 'none';
                            document.getElementById("btn_addPaymentPlan").disabled = false;
                            var message = "Successfully added the payment plan";
                            showMessage(message, "success", result.redirectionURL);
                        } else {
                            showMessage(result.message, "error", result.redirectionURL);
                        }
                    });
            }
        });
}

function isUpdate() {
    var planName = $("#planName").attr('value');
    jagg.syncPost("../blocks/monetizing/productPlan/update/ajax/update.jag", {
            action: "is-update-payment-plans-of-tenant",
            planName: planName
        },
        function (result) {
            if (result) {
                $("#btn_addPaymentPlan").text(" Update");
            } else {
                $("#btn_addPaymentPlan").text(" Add");
            }
        });
}

function showMessage(message, type, url) {
    noty({
        theme: 'wso2',
        layout: 'topCenter',
        type: type,
        closeWith: ['button', 'click'],
        text: message,
        buttons: [{
            addClass: 'btn btn-default', text: 'OK', onClick: function () {
                window.location.href = (url);
            }
        }],
        animation: {
            open: {height: 'toggle'}, // jQuery animate function property object
            close: {height: 'toggle'}, // jQuery animate function property object
            easing: 'swing', // easing
            speed: 500 // opening & closing animation speed
        }
    });
}
$(".side-pane-trigger").click(function () {
    var rightPane = $(".right-pane");
    var leftPane = $(".left-pane");
    if (rightPane.hasClass("visible")) {
        rightPane.animate({"left": "0em"}, "slow").removeClass("visible");
        leftPane.animate({"left": "-18em"}, "slow");
        $(this).find("i").removeClass("fa-arrow-left").addClass("fa-reorder");
    } else {
        rightPane.animate({"left": "18em"}, "slow").addClass("visible");
        leftPane.animate({"left": "0em"}, "slow");
        $(this).find("i").removeClass("fa-reorder").addClass("fa-arrow-left");
    }
});
