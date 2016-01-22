function doSubmit() {
    jagg.message({content: 'Please Wait. Your request is being processed..', type: 'success'});
    jagg.post("/site/blocks/pricing/account/create/ajax/create.jag", {
        action: "createAccount",
        firstName: $("#firstName").val(),
        lastName: $("#lastName").val(),
        address1: $("#addressLine1").val(),
        address2: $("#addressLine2").val(),
        city: $("#city").val(),
        state: $("#state").val(),
        zipCode: $("#postalCode").val(),
        country: $("#country").val(),
        refId: $("#refId").val(),
        signature: $("#signature").val(),
        field_passthrough1: $("#field_passthrough1").val(),
        workflowReference: $("#workflowReference").val(),
        email: $("#email").val()

    }, function (result) {
        if (!result.error) {
            var tenant = $("#tenant").val();
            var selectedApp = $("#selectedApp").val();
            var workflowReference = $("#workflowReference").val();
            window.location.href = requestURL + "site/pages/pricing/manage-account.jag?tenant=" + encodeURIComponent(tenant)
                + "&selectedApp=" + encodeURIComponent(selectedApp) + "&workflowReference=" + encodeURIComponent(workflowReference) + "&action=subscribed";
        } else {
            jagg.message({content: result.message, type: "error"});
        }
    }, "json");
}

$(document).ready(function ($) {

    $('#proceedBtn').prop('disabled', true);
    $('#proceedBtn').removeClass('btn-default');
    $('#optionsRadios1').change(function () {
        if (this.checked) {
            $('#proceedBtn').prop('disabled', false);
            $('#proceedBtn').addClass('btn-default');
        } else {
            $('#proceedBtn').prop('disabled', true);
            $('#proceedBtn').removeClass('btn-default');
        }
    });
});
