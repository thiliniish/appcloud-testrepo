$(document).ready(function ($) {
    generateResponse();

});

function generateResponse() {

    jagg.post("../blocks/tenant/register/confirm/ajax/confirm.jag", {
            action: "confirmUser",
            confirm: $("#confirm").attr('value'),
            isInvitee: $("#isInvitee").attr('value'),
            isStoreInvitee: $("#isStoreInvitee").attr('value')
    },
        function (result) {
            var message = result.replace(/(\r\n|\n|\r)/gm, "");
            if (message == "expired") {
                jagg.message({
                    type: 'error',
                    content: 'You have either already clicked the link that was emailed to you or it must have been expired',
                    cbk: function () {
                        window.location.href = "../pages/index.jag";
                    }
                });
            } else if (message == "add-tenant.jag") {
                window.location.href = result;
            } else {
                jagg.message({
                    content: 'You have successfully joined the Organization. Please login <a href=' + message +
                    '>here</a>.', type: 'success', cbk: function () {
                        window.location.href = message;
                    }, cbkBtnText: 'Login'
                });
            }
        },
        function (jqXHR, textStatus, errorThrown) {
            jagg.message({
                content: COMMON_ERROR_MESSAGE, type: 'error', cbk: function () {
                    window.location.href = "index.jag";
                }
            });
        });
}
