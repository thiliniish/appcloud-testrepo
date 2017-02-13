$(document).ready(function ($) {
    generateResponse();
});

function generateResponse() {

    jagg.post("/site/blocks/tenant/register/confirm/ajax/confirm.jag", {
            action: "confirmUser",
            confirm: $("#confirm").attr('value'),
            isInvitee: $("#isInvitee").attr('value'),
            isStoreInvitee: $("#isStoreInvitee").attr('value')
        },
        function (result) {
            result = JSON.parse(result);
            if (!result.error) {
                var message = result.data;
                if (message == "expired") {
                    jagg.message({
                        type: 'error',
                        content: 'You have either already clicked the link that was emailed to you or it has expired',
                        cbk: function () {
                            window.location.href = requestURL + '?' + urlPrefix;
                        }
                    });
                } else if (message == "add-tenant.jag") {
                    var confirmation = result.registration.confirmationKey;
                    var email = result.registration.email;
                    var redirectURL = message + '?' + urlPrefix + "&confirmation=" + confirmation + "&email=" + email;
                    var isInvitee = $.parseJSON($('#isInvitee').val());
                    console.log(isInvitee)
                    if(isInvitee) {
                        redirectURL = redirectURL + "&isInvitee=true"
                    } else {
                        redirectURL = redirectURL + "&isStoreInvitee=true"
                    }
                    window.location.href = redirectURL;
                } else {
                    var organization = $("#tenant").attr('value');
                    jagg.message({
                                     content: organization + ' is hosted in WSO2 Cloud. Looks like the email is already registered at wso2.com. Please use the same password to <a href=' + message + '>log in</a>.',
                                     type: 'success',
                                     cbk: function () {
                                         window.location.href = message;
                                     },
                                     cbkBtnText: 'Login'
                                 });
                }
            } else {
                var redirectUrl = requestURL + '?' + urlPrefix;
                jagg.message({
                    content: "Error occurred while registering the account.", type: 'error', cbk: function () {
                        window.location.href = redirectUrl;
                    }
                });
            }
        });
}
