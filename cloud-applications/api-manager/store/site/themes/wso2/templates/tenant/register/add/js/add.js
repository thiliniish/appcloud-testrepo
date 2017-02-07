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
    var password = $("#userPassword").val();
    var confirmationKey = $("#confirmationKey").val();
    var firstName = $("#firstName").val();
    var lastName = $("#lastName").val();

    jagg.post("/site/blocks/tenant/register/add/ajax/add.jag", {
                  action: "importInvitedUser",
                  adminPassword: password,
                  confirmationKey: confirmationKey,
                  firstName: firstName,
                  lastName: lastName
              },
              function (result) {
                  disableSpinner();
                  result = JSON.parse(result);
                  if (!result.error) {
                      loginWithoutScreen();
                  } else {
                      jagg.message({
                                       content: 'Error occurred while registering the account.',
                                       type: 'error',
                                       cbk: function () {
                                           window.location.href = requestURL + '?' + urlPrefix;
                                       }
                                   });
                  }
              });
}

function checkConfirmation() {
  var isConfirmed = $("#isConfirmed").val();
  if (isConfirmed != "true") {
      jagg.message({
            content: "You have either already clicked the link that was emailed to you or it has expired.",
            type: 'error',
            cbk: function () {
                window.location.href = requestURL + '?' + urlPrefix;
            }
       });
       $('#registerForm').hide();
   }
}

function loginWithoutScreen() {
    var username = $("#email").val() + "@" + tenant;
    var password = $("#userPassword").val();
    var redirectUrl = requestURL + '?' + urlPrefix;
    jagg.syncPost("/site/blocks/user/login/ajax/login.jag",
                  { action: "login",
                      username: username,
                      password: password},
                  function (result) {
                      if (result.error == false) {
                          window.location.href = redirectUrl;
                      } else {
                          jagg.message({
                                           content: "Error occurred while registering the account.",
                                           type: 'error',
                                           cbk: function () {
                                               window.location.href = redirectUrl;
                                           }
                                       });
                          $('#registerForm').hide();
                      }
                  }
    );
}

$(document).ready(function ($) {
    var clickwithblur = false;
    var isKeyup = false;
    checkConfirmation();

    jQuery.validator.setDefaults({
                                     errorElement: 'span'
                                 });

    $('#registerForm').validate({
                                    onfocusout: false,
                                    onkeyup: false,
                                    debug: true,
                                    rules: {
                                        userPassword: {
                                            required: true
                                        },
                                        lastName : {
                                            required: true
                                        },
                                        firstName : {
                                            required :true
                                        }
                                    },
                                    submitHandler: function (form) {
                                        doSubmit();
                                    }
                                });

    $('#lastName').blur(function () {
        document.activeElement;
        if (!clickwithblur) {
            $('#lastName').valid();
        }
    });

    $('#firstName').blur(function () {
        document.activeElement;
        if (!clickwithblur) {
            $('#firstName').valid();
        }
    });

    $('#userPassword').blur(function () {
        $('.password-meter').hide();
        isKeyup = false;
        if (!clickwithblur) {
            $('#userPassword').valid();
        }
    });

    $("#userPassword").keyup(function () {
        isKeyup = true;
        $('#userPassword').valid();
    });

    $('#userPassword').focus(function () {
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


