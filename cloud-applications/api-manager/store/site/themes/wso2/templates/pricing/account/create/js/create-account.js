function doSubmit() {
    var isValid = validateForm();
    if (isValid) {
        jagg.message({content: 'Please wait. Your request is being processed..', type: 'info'});
        jagg.post("/site/blocks/pricing/account/create/ajax/create.jag", {
            action: "createAccount",
            organization: $("#organization").val(),
            firstName: $("#firstName").val(),
            lastName: $("#lastName").val(),
            address1: $("#addressLine1").val(),
            address2: $("#addressLine2").val(),
            city: $("#city").val(),
            state: $("#state").val(),
            zipCode: $("#postalCode").val(),
            country: $("#country option:selected").text(),
            refId: $("#refId").val(),
            signature: $("#signature").val(),
            field_passthrough1: $("#field_passthrough1").val(),
            workflowReference: $("#workflowReference").val(),
            email: $("#email").val(),
            field_passthrough5: $("#field_passthrough5").val()
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
}

function validateForm() {
    var validInputObj = {"isValid": true};
    validateRequiredInput('organization', validInputObj);
    validateRequiredInput('firstName', validInputObj);
    validateRequiredInput('lastName', validInputObj);
    validateRequiredInput('email', validInputObj);
    validateRequiredInput('addressLine1', validInputObj);
    validateRequiredInput('addressLine2', validInputObj);
    validateRequiredInput('country', validInputObj);
    validateRequiredInput('city', validInputObj);
    validateRequiredInput('state', validInputObj);
    validateRequiredInput('postalCode', validInputObj);
    return validInputObj.isValid;
}

function validateRequiredInput(id, validInputObj) {
    var error = "";
    var illegalChars = /([~!#$;%^*+={}\|\\<>\"\'\/,])/;
    if ($("#" + id).val() == "") {
        validInputObj.isValid = false;
        error = i18n.t('validationMsgs.fieldRequired');
    } else if (/(["\'])/g.test($("#" + id).val())) {
        validInputObj.isValid = false;
        error = i18n.t('validationMsgs.illegalChars') + '( " \' )';
    } else if ($("#" + id).val().search(illegalChars) != -1) {
        validInputObj.isValid = false;
        error = i18n.t('validationMsgs.illegalChars');
    }
    handleError(id, error);
}

function validateInput(id, validInputObj) {
    var error = "";
    var illegalChars = /([~!#$;%^*+={}\|\\<>\"\'\/,])/;
    if ($("#" + id).val() != "" && /(["\'])/g.test($("#" + id).val())) {
        validInputObj.isValid = false;
        error = i18n.t('validationMsgs.illegalChars') + '( " \' )';
    } else if ($("#" + id).val() != "" && $("#" + id).val().search(illegalChars) != -1) {
        validInputObj.isValid = false;
        error = i18n.t('validationMsgs.illegalChars');
    }
    handleError(id, error);
}

function handleError(id, error) {
    if (error != "") {
        $("#" + id).addClass('error');
        if (!$("#" + id).next().hasClass('error')) {
            $("#" + id).parent().append('<label class="error">' + error + '</label>');
        } else {
            $("#" + id).next().show().html(error);
        }
    } else {
        $("#" + id).removeClass('error');
        $("#" + id).next().hide();
    }
}

$(document).ready(function ($) {

    $('.test').tooltip();

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
