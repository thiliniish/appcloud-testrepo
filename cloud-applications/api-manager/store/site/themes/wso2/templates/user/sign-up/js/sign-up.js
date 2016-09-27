$(document).ready(function () {

    $("#sign-up").validate({
        submitHandler: function (form) {
            document.getElementById("spinner").style.display = '';
            var email = document.getElementById("email").value;
            var allFieldsValues = email;
            var tenantDomain = document.getElementById('hiddenTenantDomain').value;
            var fullUserName;
            if (tenantDomain == "null" || tenantDomain == "carbon.super") {
                fullUserName = email;
            } else {
                fullUserName = email + "@"
                    + tenantDomain;
            }
            jagg.post("/site/blocks/user/sign-up/ajax/user-add.jag", {
                action: "addUser",
                username: fullUserName,
                password: "default",
                allFieldsValues: allFieldsValues
            }, function (result) {
                document.getElementById("spinner").style.display = 'none';
                if (result.error == false) {
                    if (result.showWorkflowTip) {
                        jagg.message({content:"You will receive an email. Please follow its instructions to proceed",type:"info",
                            cbk:function() {
                                $("#signUpRedirectForm input[name=redirector]").val("home");
                                $("#signUpRedirectForm input[name=tenantDomain]").val(tenantDomain);
                                $('#signUpRedirectForm').submit();
                            }
                        });
                    } else {
                        jagg.message({content:"You will receive an email. Please follow its instructions to proceed",type:"info",
                            cbk:function() {
                                $("#signUpRedirectForm input[name=redirector]").val("home");
                                $("#signUpRedirectForm input[name=tenantDomain]").val(tenantDomain);
                                $('#signUpRedirectForm').submit();
                            }
                        });
                    }
                } else {
                    jagg.message({content:result.message,type:"error",
                        cbk:function() {
                            $("#signUpRedirectForm input[name=redirector]").val("login");
                            $("#signUpRedirectForm input[name=tenantDomain]").val(tenantDomain);
                            $('#signUpRedirectForm').submit();
                        }

                    });
                }
            }, "json");
        }
    });

    $("#newPassword").keyup(function () {
        $(this).valid();
    });
    $('#newPassword').focus(function () {
        $('#password-help').show();
        $('.password-meter').show();
    });
    $('#newPassword').blur(function () {
        $('#password-help').hide();
        $('.password-meter').hide();
    });
});

var showMoreFields = function () {
    $('#moreFields').show();
    $('#moreFieldsLink').hide();
    $('#hideFieldsLink').show();
}
var hideMoreFields = function () {
    $('#moreFields').hide();
    $('#hideFieldsLink').hide();
    $('#moreFieldsLink').show();
}