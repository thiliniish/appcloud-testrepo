function updateContactInfo(tenantDomain, docObj) {
    var cloudmgtURL = docObj.getElementById("cloudmgtURL").value;
    jagg.message({
        content: 'Please wait. Your request is being processed..', type: 'success'
    });
    $.ajax({
        url: "../../blocks/pricing/account/update/ajax/update.jag",
        data: {
            action: "updateAccount",
            firstName: docObj.getElementById("firstname").value,
            lastName: docObj.getElementById("lastname").value,
            address1: docObj.getElementById("addressline1").value,
            address2: docObj.getElementById("addressline2").value,
            city: docObj.getElementById("city").value,
            state: docObj.getElementById("state-province-region").value,
            zipCode: docObj.getElementById("postal-zip").value,
            country: docObj.getElementById("country").value,
            email: docObj.getElementById("email").value
        },
        success: function (data) {
            window.location.href = "account-summary.jag?tenant=" + tenantDomain;
        }, error: function (jqXHR, textStatus, errorThrown) {
            $('.message_box').empty();
            jagg.message({
                content: "Unable to complete the task at the moment. Please contact WSO2 Cloud Team for help",
                type: 'error',
                cbk: function () {
                    window.location.href = cloudmgtURL + "/site/pages/contact-us.jag";
                }
            });
        }
    });
}
 





