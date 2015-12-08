var params;

var callback = function (response) {
    var cloudmgtURL =  $("#cloudmgtURL").attr('value');
    if(!response.success) {
        $('.message_box').empty();
        jagg.message({content:JSON.stringify(response), type:'error',cbk:function(){
            window.location.href = cloudmgtURL + "/site/pages/index.jag";
        }
        });
    }
};

function showPage() {
    var zuoraDiv = document.getElementById('zuora_payment');
    zuoraDiv.innerHTML="";
    Z.render(params, null, callback);
}

function submitPage() {
    disable();
    Z.submit();
    enable();
}
function disable() {
    document.getElementById("spinner").style.display = '';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color','#F9BFBB');
    submitButton.disabled = true;
}

function enable() {
    document.getElementById("spinner").style.display = 'none';
    var submitButton = document.getElementById('submitbtn');
    $('#submitbtn').css('background-color','#428BCA');
    submitButton.disabled = false;
}

function generateParameters (){

    var workflowReference =  $("#workflowReference").attr('value');
    jagg.syncPost("/site/blocks/pricing/payment-method/add/ajax/add.jag", {
        action: "generateParams",
        workflowReference: workflowReference
    }, function (result) {
        params = result;
        if(accountId != "") {
            params.field_accountId =  accountId;
            params.field_passthrough5 = "secondary-card";
        }
        showPage();
    }, function (jqXHR, textStatus, errorThrown) {
        $('.message_box').empty();
        jagg.message({content:"Unable to add a new payment method at the moment. Please contact WSO2 Cloud Team for help", type:'error',cbk:function() {
            var cloudMgtURL =  $("#cloudmgtURL").attr('value');
            window.location.href = cloudMgtURL+"/site/pages/contact-us.jag";
        }
        });
    });
}

$(document).ready(function($){
    // Check for billing enable/disable mode
    var isBillingEnabled = true;
    if (isBillingEnabled) {
        //showErrorMessage();
        generateParameters();
        var clickwithblur = false;
        $( "#submitbtn" ).click(function() {

            submitPage();
        });
        /*$( "#redeembtn" ).click(function() {
            calculateDiscount();
        });
        $('#coupon').keydown(function(event) {
            if(event.keyCode === 13) {
                calculateDiscount();
                return false;
            }
        });*/
        $('#submitbtn').mousedown(function(){
            clickwithblur = true;
        });
        $('#submitbtn').mouseup(function(){
            clickwithblur = false;
        });
        $('#backbtn').click(function() {
            if(confirm("Are you sure you want to navigate away from this page?"))
            {
                history.go(-1);
            }
            return false;
        });
        $('[data-toggle="tooltip"]').tooltip();

        $("[data-toggle=popover]").popover();

        $(".ctrl-asset-type-switcher").popover({
            html : true,
            content: function() {
                return $('#content-asset-types').html();
            }
        });

        $(".ctrl-filter-type-switcher").popover({
            html : true,
            content: function() {
                return $('#content-filter-types').html();
            }
        });

        $('#nav').affix({
            offset: {
                top: $('header').height()
            }
        });
    } else {
        var cloudMgtURL = $("#cloudmgtURL").attr('value');
        var unavailableErrorPage = $("#unavailableErrorPage").attr('value');
        window.location.href = cloudMgtURL + unavailableErrorPage;
    }

});