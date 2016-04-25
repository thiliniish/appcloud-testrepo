$(document).ready(function ($) {
    var eligibleForMonetization = $("#eligibleForMonetization").attr('value');
    if (eligibleForMonetization == "false") {
        var requirementMessage = document.getElementById("requirement-Message").textContent;
        showRequirementMessage(requirementMessage);
    }
    var clickwithblur = false;
    jQuery.validator.addMethod("validatePassword", function (value) {
        var userName = $("#userName").attr('value');
        var isValid = false;
        $.ajax({
            url: "../blocks/user/authenticate/ajax/login.jag",
            type: "POST",
            data: {
                action: "validatePassword",
                userName: userName,
                password: value
            },
            async: false,
            success: function (msg) {
                msg = msg.replace(/[\r\n]/g, "");
                if (msg == 'true') {
                    isValid = true;
                }
            }
        });
        return isValid;
    }, "Password is not correct.");
    $('#enableMonetizationForm').validate({
        onfocusout: false,
        onkeyup: false,
        rules: {
            password: {
                required: true,
                validatePassword: true,
                minlength: 5
            }
        },
        messages: {
            password: {
                minlength: "Minimum is 5 characters ",
                validatePassword: "Password is not correct. "
            }
        },
        submitHandler: function (form) {
            doSubmit();
        }
    });
    $('#password').blur(function () {
        $('#password').valid();
    });

});
function doSubmit() {
    document.getElementById("spinner").style.display = '';
    document.getElementById("btn_enable-monetization").disabled = true;
    var tenantPassword = $("#password").attr('value');
    if (tenantPassword != "") {
        $.ajax({
            url: "../blocks/monetizing/publisher/enable/ajax/enable.jag",
            data: {
                "action": "enableMonetization",
                "tenantPassword": tenantPassword
            },
            success: function (result) {
                    result = JSON.parse(result);
                    document.getElementById("spinner").style.display = 'none';
                    document.getElementById("btn_enable-monetization").disabled = false;
                    showMessage(result.message, "success", result.redirectionURL);
            },
            error:  function (result) {
                result = JSON.parse(result.responseText);
                showMessage(result.message, "error", result.redirectionURL);
            }
        });
    }
}

function showRequirementMessage(message) {
    var paymentPlanUrl = $("#paymentPlanUrl").attr('value');
    var cloudmgtURI = $("#cloudmgtURI").attr('value');
    noty({
        theme: 'wso2',
        layout: 'topCenter',
        type: 'confirm',
        closeWith: ['button', 'click'],
        modal: true,
        text: message,
        buttons: [
            {
                addClass: 'btn btn-default', text: 'Cancel', onClick: function ($noty) {
                $noty.close();
            }
            },
            {
                addClass: 'btn btn-primary', text: 'OK', onClick: function ($noty) {
                window.location.href = cloudmgtURI + paymentPlanUrl;
            }
            }
        ],
        animation: {
            open: {height: 'toggle'}, // jQuery animate function property object
            close: {height: 'toggle'}, // jQuery animate function property object
            easing: 'swing', // easing
            speed: 500 // opening & closing animation speed
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
            addClass: 'btn btn-default', text: 'Ok', onClick: function () {
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