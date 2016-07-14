$(document).ready(function () {

    var otEmail = $("#otEmail").val();
    if (otEmail != "null") {
        $('#userForm').hide();
        document.getElementById("email").value = otEmail;
        doSubmit();
    }
    jQuery.validator.setDefaults({
        errorElement: 'span'
    });
    jQuery.validator.addMethod("emailValid", function (value, element) {
        //var isSuccess = false;
        var isSuccess = validateEmail(value);
        return isSuccess;
    }, "Please enter a Valid email address. Please use only letters (a-z), numbers, and periods.");
    $('#userForm').validate({
        rules: {
            email: {
                required: true,
                email: true
            }
        },
        submitHandler: function (form) {
            doSubmit();
        }
    });
    $("#resend-email").click(function (event) {
        event.preventDefault();
        resendInvitationEmail();
    });
    $('#resent-success').hide();
});

function validateEmail() {
    //var isEmailValid = true;
    var email = $("#email").val();
    var patternForEmailValidation = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    var isEmailValid = patternForEmailValidation.test(email);
    if (email.indexOf("+") != -1) {
        isEmailValid = false;
    }
    if (isEmailValid) {
        $('#email').val(email);
    }
    return isEmailValid;
}

$(function () {
    $('#help-options').click(function () {
        $('#more-options').show();
        return false;
    });
});

function disable() {
    document.getElementById("spinner").style.display = '';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color', '#F9BFBB');
    submitButton.disabled = true;
}

function enable() {
    document.getElementById("spinner").style.display = 'none';
    //var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color', '#428BCA');
    //submitButton.disabled = false;
}

function doSubmit() {
    var email = $("#email").attr('value');
    var cloudmgtURL = $("#cloudmgtURL").attr('value');
    var token = $("#sourceToken").attr('value');
    disable();
    $('#resent-success').hide();
    var isValidMail = validateEmail();
    if (isValidMail) {
        jagg.post("../blocks/tenant/register/invite/ajax/invite.jag", {
                action: "isExistingUser",
                username: email
            },
            function (result) {
                result = result.replace(/(\r\n|\n|\r)/gm, "");
                if (result == "false") {
                    jagg.post("../blocks/tenant/register/invite/ajax/invite.jag", {
                            action: "sendInvite",
                            email: email,
                            token: token
                        },
                        function (result) {
                            $(".content-starter h1").text("Check Your Email");
                            $(".content-starter h1").css('color', 'white');
                            $("#email-sent-to").replaceWith("<br><h5> We have sent you the welcome email to <b>"+email+
                                "</b>. Please click the link in the email to continue signing up.</h5>");
                            $(".helper_text h5").css({'font-size': '20px', 'text-align': 'left'});
                            $("#helper_text").hide();
                            $("#success-message").show();
                            $(".content-section-wrapper").hide();
                        },
                        function (jqXHR, textStatus, errorThrown) {
                            jagg.message({
                                content: ' Error Sending the registration email ', type: 'error', cbk: function () {
                                    window.location.href = "index.jag";
                                }
                            });
                            enable();
                        });
                } else {
                    jagg.message({
                        content: 'Looks like you have an account with wso2.com - So please just use the password ' +
                        'that you have there to log in <br/>If you do not remember the password that you have at ' +
                        'wso2.com - click  <a href="initiate.jag">here</a>  to reset and we will send you a new' +
                        ' password.',
                        type: 'success',
                        cbk: function () {
                            window.location.href = "index.jag";
                        }
                    });
                    enable();
                }
            },
            function (jqXHR, textStatus, errorThrown) {
                jagg.message({
                    content: ' Error Sending the registration email ', type: 'error', cbk: function () {
                        window.location.href = "index.jag";
                    }
                });
                enable();
            });
    } else {
        jagg.message({
            content: 'Looks like you have given an invalid email Address - ' + email + " Email should be of length " +
            "3-30 in alphanumeric characters except '+'",
            type: 'error',
            cbk: function () {
                window.location.href = "index.jag";
            }
        });
        enable();
    }
}

function resendInvitationEmail() {
    var email = $("#email").attr('value');

    $.post("../blocks/tenant/register/invite/ajax/invite.jag", {
            action: "reInvite",
            email: email
        },
        function (result) {
            $('#resend-mail').text("We have re-sent your email. Please check your inbox.");
        },
        function (jqXHR, textStatus, errorThrown) {
            $('#resend-mail').text("Error re-sending email.Please Please contact support.");
        });
    $('#resent-success').show();

    return true;
}

function getUserEmail() {
    cloudmgtURL = $("#cloudmgtURL").attr('value');
    userEmail = document.getElementById('email').value;
    var link = cloudmgtURL + "/site/pages/contact-us.jag?registration-help=true&email=" + userEmail;
    document.getElementById('contact-us-link').setAttribute('href', link);
    return true;
}