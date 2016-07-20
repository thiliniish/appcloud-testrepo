function disableSubmitAndEnableSpinner() {
    document.getElementById("spinner").style.display = '';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color', '#F9BFBB');
    submitButton.disabled = true;
}

function disableSpinner() {
    document.getElementById("spinner").style.display = 'none';
}

function doSubmit() {
    disableSubmitAndEnableSpinner();
    var adminPassword = $("#password").attr('value');
    var cloudmgtURL = $("#cloudmgtURL").attr('value');
    var confirmationKey = $("#confirmationKey").attr('value');
    var isInvitee = $("#isInvitee").attr('value');
    var firstName = $("#firstName").attr('value');
    var lastName = $("#lastName").attr('value');
    var companyName = $("#companyName").attr('value');
    if (isInvitee == "true") {
        jagg.post("../blocks/tenant/users/add/ajax/add.jag", {
                action: "importInvitedUser",
                adminPassword: adminPassword,
                confirmationKey: confirmationKey,
                firstName: firstName,
                lastName: lastName
            },
            function (result) {
                result = JSON.parse(result);
                if (!result.error) {
                    loginWithoutIdpScreen();
                }else{
                    jagg.message({
                        content: 'Error occurred while registering the account. Please contact WSO2 Cloud Team for help.',
                        type: 'error',
                        cbk: function () {
                            window.location.href = cloudmgtURL;
                        }
                    });
                }
            });
        disableSpinner();
    } else {
        jagg.post("../blocks/tenant/register/add/ajax/add.jag", {
                action: "registerOrg",
                adminPassword: adminPassword,
                companyName: companyName,
                usagePlan: 'Demo',
                confirmationKey: confirmationKey,
                firstName: firstName,
                lastName: lastName
            },
            function (result) {
                if (result == false || result.trim() == "false") {
                    jagg.message({
                        content: 'Error occurred while registering the account. Please contact WSO2 Cloud Team for help.',
                        type: 'error',
                        cbk: function () {
                            window.location.href = cloudmgtURL;
                        }
                    });
                } else {
                    loginWithoutIdpScreen();
                }
                disableSpinner();
            },
            function (jqXHR, textStatus, errorThrown) {
                jagg.message({
                    content: 'Error occurred while registering the account. Please contact WSO2 Cloud Team for help.',
                    type: 'error',
                    cbk: function () {
                        window.location.href = cloudmgtURL;
                    }
                });
                disableSpinner();
            });
    }
}

function checkConfirmation() {
    var key = $("#confirmationKey").attr('value');
    var cloudmgtURL = $("#cloudmgtURL").attr('value');
    if (key == 'null' || key == "") {
        jagg.message({
            content: "You have either already clicked the link that was emailed to you or it has expired.",
            type: 'error',
            cbk: function () {
                window.location.href = cloudmgtURL;
            }
        });
        $('#registerForm').hide();
    }
}

function getProfile() {
    var userName = $("#userName").attr('value');
    jagg.post("../blocks/user/profile/ajax/profile.jag", {
            action: "getProfile",
            user: userName
        },
        function (result) {
            json = JSON.parse(result);
            displayClaims(json);
        },
        function (jqXHR, textStatus, errorThrown) {
            jagg.message({
                type: 'error',
                content: "Unable to retrieve the user profile at the moment. Please contact WSO2 Cloud Team for help."
            });
        })
}

function loginWithoutIdpScreen() {
    var cloudmgtURL = $("#cloudmgtURL").attr('value');
    jagg.syncPost("../blocks/user/authenticate/ajax/login.jag", {
            action: "getSectoken",
            email: $("#email").attr('value'),
            password: "" + $("#password").attr('value')
        },
        function (result) {
            $('<input />').attr('type', 'hidden')
                .attr('name', "sectoken")
                .attr('value', "" + result.sectoken)
                .appendTo('#OneTimeLoginSubmitForm');
            $('#OneTimeLoginSubmitForm').submit();
        },
        function (jqXHR, textStatus, errorThrown) {
            jagg.message({
                content: jqXHR.responseText, type: 'error', cbk: function () {
                    window.location.href = cloudmgtURL;
                }
            });
        }
    );
}

function displayClaims(claims) {
    if (claims.firstname != null | claims.firstname != undefined) {
        document.getElementById("firstName").value = claims.firstname;
    }
    if (claims.lastname != null | claims.lastname != undefined) {
        document.getElementById("lastName").value = claims.lastname;
    }
}

$(document).ready(function ($) {
    var clickwithblur = false;
    var isKeyup = false;
    initializeUserActivity("SignUp Page");
    checkConfirmation();

    var isUserAvail = $("#isUserAvailable").attr('value');
    if (isUserAvail == 'true') {
        getProfile();
    }

    jQuery.validator.setDefaults({
        errorElement: 'span'
    });

    jQuery.validator.addMethod("validateDomain", function (value, element) {
        var isSuccess = false;
        $.ajax({
            url: "../blocks/tenant/register/add/ajax/add.jag",
            data: {
                action: "checkDomainAvailability",
                companyName: value
            },
            async: false,
            success: function (msg) {
                msg = msg.replace(/[\r\n]/g, "");
                if (msg == "true") {
                    isSuccess = true;
                }
            }
        });
        return isSuccess;
    }, function (value, element) {
        return "Company name \" " + element.value + " \" is already taken. Please provide some other name."
    });

    jQuery.validator.addMethod("validatePassword", function (value, element) {
        var isUserAvail = $("#isUserAvailable").attr('value');
        if (isUserAvail != 'true' || (isUserAvail == 'true' && isKeyup)) {
            return true;
        }
        var email = $("#email").attr('value');
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
    }, "Invalid password, please try again.");

    jQuery.validator.addMethod("validateCompanyName", function (value, element) {
        var isValid = validateOrganizationNameAlphaNumeric(value);
        return isValid;
    }, "Please use only English letters, numbers and whitespaces");

    $('#registerForm').validate({
        onfocusout: false,
        onkeyup: false,
        rules: {
            password: {
                required: true,
                validatePassword: true
            },
            companyName: {
                validateCompanyName: true,
                validateDomain: true
            }
        },
        submitHandler: function (form) {
            doSubmit();
        }
    });

    $('#companyName').blur(function () {
        document.activeElement;
        if (!clickwithblur)
            $('#companyName').valid();
    });

    $('#lastName').blur(function () {
        document.activeElement;
        if (!clickwithblur)
            $('#lastName').valid();
    });

    $('#firstName').blur(function () {
        document.activeElement;
        if (!clickwithblur)
            $('#firstName').valid();
    });

    $('#password').blur(function () {
        $('.password-meter').hide();
        isKeyup = false;
        if (!clickwithblur) {
            $('#password').valid();
        }
    });

    $("#password").keyup(function () {
        isKeyup = true;
        $('#password').valid();
    });

    $('#password').focus(function () {
        $('#password-help').show();
        $('.password-meter').show();
    });

    $('#submitbtn').mousedown(function () {
        clickwithblur = true;
    });

    $('#submitbtn').mouseup(function () {
        clickwithblur = false;
    });

});
