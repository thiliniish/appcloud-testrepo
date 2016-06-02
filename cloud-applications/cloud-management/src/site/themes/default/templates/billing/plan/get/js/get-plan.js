$(document).ready(function () {
    // Check for billing enable/disable mode
    var isBillingEnabled = $("#isBillingEnabled").attr('value');
    var marketing = $("#marketing").attr('value');
    var ratePlansObj = $("#ratePlansObj").attr('value');
    var selectedPlanId = $("#selectedPlanId").attr('value');
    var ratePlans = $("#ratePlans").attr('value');

    if (isBillingEnabled) {
        $(".tiny").hover(function (e) {
            $(".tiny").removeClass("selected");
            $(this).addClass("selected");
        });

        $('[data-toggle="tooltip"]').tooltip();
        $("[data-toggle=popover]").popover();
        $(".ctrl-asset-type-switcher").popover({
            html: true,
            content: function () {
                return $('#content-asset-types').html();
            }
        });

        $(".ctrl-filter-type-switcher").popover({
            html: true,
            content: function () {
                return $('#content-filter-types').html();
            }
        });

        $('#nav').affix({
            offset: {
                top: $('header').height()
            }
        });

        if (marketing && ratePlans == 'null') {
            var ratePlanDetails = JSON.parse(ratePlansObj);
            for (var index in ratePlanDetails.entry) {
                if (ratePlanDetails.entry[index].id == selectedPlanId) {
                    createAccount(selectedPlanId, ratePlanDetails.entry[index].name, ratePlanDetails.entry[index].maxAccounts,
                        ratePlanDetails.entry[index].maxDailyUsage, ratePlanDetails.entry[index].overUsage,
                        ratePlanDetails.entry[index].monthlyRental.replace("$",""), false);
                    break;
                }
            }
        }
    } else {
        var cloudMgtURL = $("#cloudmgtURL").attr('value');
        var unavailableErrorPage = $("#unavailableErrorPage").attr('value');
        window.location.href = cloudMgtURL + unavailableErrorPage;
    }
});

function disableButton(id) {
    //document.getElementById("spinner_"+id).style.display = '';
    document.getElementById("selectbtn_" + id).setAttribute("disabled", true);
}

function enableButton(id) {
    document.getElementById("spinner_" + id).style.display = 'none';
    document.getElementById("selectbtn_" + id).removeAttribute("disabled", "disabled");
}

function createAccount(id, currRatePlan, accounts, callsPerDay, callsAbovePlan, monthlyRental, isFromChangePlan) {
    disableButton(id);
    var cloudmgtURL = $("#cloudmgtURL").attr('value');
    jagg.post("../blocks/billing/plan/get/ajax/get.jag", {
            action: "goToCreateAccount",
            productRatePlanId: id,
            currRatePlan: currRatePlan,
            accounts: accounts,
            callsPerDay: callsPerDay,
            callsAbovePlan: callsAbovePlan,
            monthlyRental: monthlyRental,
            isFromChangePlan: isFromChangePlan
        },
        function (result) {
            enableButton(id);
            if (result.indexOf("add-payment-method") >= 0) {
                window.location.href = result;
            } else if (result.indexOf("add-billing-account") >= 0) {
                goToPaymentConfirmationPageFromChangePlan();
            } else if (result.indexOf("null") < 0) {
                jagg.message({type: 'error', content: result});
            } else {
                jagg.message({type: 'error', content: result});
                window.location.href = "";
            }
        },
        function (jqXHR, textStatus, errorThrown) {
            enableButton(id);
            $('.message_box').empty();
            if (marketing) {
                window.location.href = cloudmgtURL + "/site/pages/contact-us.jag";
            }
            jagg.message({
                content: "Unable to change the Plan at the moment. Please contact WSO2 Cloud Team for help",
                type: 'error',
                cbk: function () {
                    window.location.href = cloudmgtURL + "/site/pages/contact-us.jag";
                }
            });
        }
    );
}

function goToPaymentConfirmationPageFromChangePlan() {
    var formContactInfo = $('<form action="add-billing-account.jag" method="post">' +
        '<input type="hidden" name="responseFrom" value = "isFromChangePlan"/>' +
        '</form>');
    $('body').append(formContactInfo);
    $(formContactInfo).submit();
}