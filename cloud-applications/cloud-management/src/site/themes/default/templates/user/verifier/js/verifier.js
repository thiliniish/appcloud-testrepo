//function cleanup() {
//    $('#form_div').hide();
//    $('#helper_text').hide();
//    var submitButton = document.getElementById('submitbtn');
//    $('#submitbtn').css('background-color', '#F9BFBB');
//    submitButton.disabled = true;
//}

function doSubmit() {
    disable();
    var username = $("#username").attr('value');
    var email = $("#email").attr('value');
    var password = $("#password").attr('value');
    var confirmationKey = $("#confirmationKey").attr('value');
    jagg.post("../blocks/user/change/ajax/user.jag", {
            action: "updatePasswordWithUserInput",
            username: username,
            email: email,
            password: password,
            confirmationKey: confirmationKey
        },
        function (result) {
            $('#userForm').hide();
            $('#helper_text').hide();
            var json = JSON.parse(result.replace(/[\r\n]/g, ""));
            if (!json.error) {
                jagg.message({
                    content: 'You have successfully reset your password. Please log in using your new password.',
                    type: 'success',
                    cbk: function () {
                        window.location.href = "index.jag";
                    }
                });
            } else {
                jagg.message({
                    content: 'Error occurred while resetting your password. Please try again after few minutes.' +
                    ' If you still have issues, please contact us <a href="mailto:cloud@wso2.com">(cloud@wso2.com)</a>',
                    type: 'error',
                    cbk: function () {
                        window.location.href = "index.jag";
                    }
                });
            }

        },
        function (jqXHR, textStatus, errorThrown) {
            $('#userForm').hide();
            $('#helper_text').hide();
            jagg.message({
                content: 'Error occurred while resetting your password. Please try again after few minutes.' +
                ' If you still have issues, please contact us <a href="mailto:cloud@wso2.com">(cloud@wso2.com)</a>',
                type: 'error',
                cbk: function () {
                    window.location.href = "index.jag";
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

//function enable() {
//    document.getElementById("spinner").style.display = 'none';
//    var submitButton = document.getElementById('submitbtn');
//    $('#submitbtn').css('background-color', '#428BCA');
//    submitButton.disabled = false;
//}

$(document).ready(function ($) {
    jQuery.validator.setDefaults({
        errorElement: 'span'
    });

    $('#userForm').validate({
        rules: {
            password: {
                required: true,
                minlength: 8
            }
        },
        messages: {
            password: {
                minlength: "Minimum is 8 characters "
            }
        },

        submitHandler: function (form) {
            doSubmit();
        }
    });
    $("#password").keyup(function () {
        $('#password').valid();
    });
    $('#password').focus(function () {
        $('#password-help').show();
        $('.password-meter').show();
    });
    $('#password').blur(function () {
        $('#password-help').hide();
        $('.password-meter').hide();
        $('#password').valid();
    });
});
