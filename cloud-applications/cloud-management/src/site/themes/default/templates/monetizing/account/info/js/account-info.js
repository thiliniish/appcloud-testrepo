/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

// UI Design related implementation
$('#cloud-menu-popover,#cloud-menu-popover-xs').on('shown.bs.popover', function () {
    $('.cloud-block-invert-sub-true').click(function () {
        var itemPosition = $(this).position(),
            containerWidth = ($('.anim-container').width()) - 14,
            containerHeight = ($('.anim-container').height()) - 14,
            clone = $(this).clone(false);

        $(this).addClass('clickedParent');
        $(clone).children('.forward-btn').remove();
        $(clone).children('.back-btn').show();
        $(clone).children('.back-btn, .cloud-block-invert-icon').wrapAll('<div class="temp-wrap">');
        $(clone).children('.temp-wrap').append('<div class="clearfix"></div>');
        $(clone).children('.cloud-block-invert-icon').addClass('active');
        $(clone).css({
            'position': 'absolute',
            'top': itemPosition.top,
            'left': itemPosition.left
        }).appendTo('.anim-container').animate({
            width: containerWidth,
            height: containerHeight,
            'top': 0,
            'left': 0
        }, {
            duration: 200,
            complete: function () {
                var subActions = $(this).children('.sub-actions'),
                    listHeight = 224;
                subActions.show();
                if (subActions.hasClass('sub-actions')) {

                    $(this).animate({
                        height: listHeight
                    }, {
                        duration: 200
                    });
                    $(this).parent().animate({
                        height: listHeight + 14
                    }, {
                        duration: 200
                    });
                }
                $(this).on('click', '.back-btn', clickBackBtn);
            }
        });
        $('.temp-wrap').children().children('.cloud-name').hide();
        $('.temp-wrap').children('.cloud-block-invert-icon').children().children('span').show();
    });
    function clickBackBtn() {
        var pa = $(this).parent(),
            grandpa = $(pa).parent(),
            greatGrandpa = $(grandpa).parent();

        $(this).hide();

        $(grandpa).animate({
            width: '50px',
            height: '50px',
            'top': $('.clickedParent').position().top,
            'left': $('.clickedParent').position().left
        }, {
            duration: 200,
            complete: function () {
                $(greatGrandpa).css('height', 'auto');
                $(grandpa).remove();
                $(this).children('.forward-btn').show();
                $('.clickedParent').removeClass('clickedParent');
            }
        });
    }
});

$('.side-pane-trigger').click(function () {
    var rightPane = $('.right-pane');
    var leftPane = $('.left-pane');
    if (rightPane.hasClass('visible')) {
        rightPane.animate({"left": "0em"}, "slow").removeClass('visible');
        leftPane.animate({"left": "-18em"}, "slow");
        $(this).find('i').removeClass('fa-arrow-left').addClass('fa-reorder');
    } else {
        rightPane.animate({"left": "18em"}, "slow").addClass('visible');
        leftPane.animate({"left": "0em"}, "slow");
        $(this).find('i').removeClass('fa-reorder').addClass('fa-arrow-left');
    }
});

/**
 *
 * Ths code segment will hide all the accordion content
 * and bind a click event to fire when clicked on accordion
 * header to show content
 *
 * @type {*|jQuery}
 */
var allPanels = $('.accordion .box-content').hide();
$('.accordion .box-header').click(function () {
    $this = $(this);
    $target = $this.next();

    if (!$target.hasClass('active')) {
        $target.addClass('active').slideDown();
        $this.find('i').removeClass('fw-right').addClass('fw-down');
    } else {
        $target.removeClass('active').slideUp();
        $this.find('i').removeClass('fw-down').addClass('fw-right')
    }
    return false;
});

// End of UI design related implementation

var selectedUserId = null;
var usersList = null;
var selectedAccountNumber = null;
var selectedApiIndex = 0;
var apiList = null;
var changeTypeData = null;

$(document).ready(function () {
    // Getting the User list Related Data
    usersList = getUsers();
    updateBasicInfo();
    // Getting the API's for the selected Users
    apiList = getApisListOfUser();

    // Initialization of the user name select2
    var userNameSelect = $("#ds-user-name").select2({
        placeholder: "Search or Select Subscriber",
        data: usersList,
        maximumInputLength: 5,
        selectOnBlur: true
    });
    // If the user is selected from the subscribers page
    var email = $("#emailAddress").val();
    if (email != null) {
        for (var index in usersList) {
            if (usersList[index].email == email) {
                selectedUserId = index;
                selectedAccountNumber = usersList[index].accountId;
                $("#ds-user-name").val(selectedUserId).change();
                updateBasicInfo();
            }
        }
    }
    getSubscriberInfo();
    getSubscriptionInfo();
    // Trigger when selection is made in select2
    userNameSelect.on("change", function (e) {
        selectedUserId = $(this).val();
        apiList = getApisListOfUser();
        getUsers();
        updateBasicInfo();
        getSubscriberInfo();
        getSubscriptionInfo();

    });
});

$("#chkbox-complimentary").click(function () {
    var dataObj = {};
    if (usersList[selectedUserId].complimentary != (document.getElementById("chkbox-complimentary").checked)) {
        if (document.getElementById("chkbox-complimentary").checked) {
            dataObj.message = "Do you want to change to a complimentary account ?";
            dataObj.action = "makeSubscriberAccountTest";
            ShowComplimentaryMessage(dataObj);
        } else {
            dataObj.message = "Do you want to cancel the complimentary account ?";
            dataObj.action = "makeTestSubscriberAccountRegular";
            ShowComplimentaryMessage(dataObj);
        }
    } else {
        document.getElementById("chkbox-complimentary").checked = usersList[selectedUserId].complimentary;
    }
});


function getUsers() {
    jagg.syncPost("../blocks/monetizing/account/info/ajax/get.jag", {
        action: "get-subscribers-of-tenant"
    }, function (result) {
        if (!result.error) {
            usersList = result;
            selectedUserId = usersList[0].id;
        } else {
            showErrorMessage(result);
        }
    });
    return usersList;
}

function getApisListOfUser() {
    var apiList = null;
    if (selectedUserId != null) {
        jagg.syncPost("../blocks/monetizing/account/info/ajax/get.jag", {
            action: "get-api-list-from-username",
            username: usersList[selectedUserId].username

        }, function (result) {
            if (!result.error) {
                selectedAccountNumber = result.accountNumber;
                if (selectedAccountNumber != null) {
                    if (result.apiList.length != 0) {
                        apiList = result.apiList;
                        $("#customer-status").text("Paying Customer");
                    } else {
                        apiList = null;
                        $("#customer-status").text("Customer dose not have any subscribed APIs");
                    }
                } else {
                    apiList = [{id: 0, text: "No Subscribed APIs"}];
                    $("#customer-status").text("Customer dose not have any subscribed APIs");
                }
            } else {
                selectedAccountNumber = null;
                showErrorMessage(result);
            }
        });
    }
    return apiList;
}

function getSubscriberInfo() {
    if (selectedAccountNumber != null) {
        jagg.post("../blocks/monetizing/account/info/ajax/get.jag", {
            action: "get-billing-account",
            accountId: selectedAccountNumber
        }, function (result) {
            result = JSON.parse(result);
            if (!result.error) {
                updateSubscriberData(result.data);
                var dataObj = JSON.parse(result.data);
                getPaymentInfo(dataObj.data.chargeInformation);
            } else {
                showErrorMessage(result);
            }
        });
    } else {
        updateSubscriberData(null);
    }
};

function getPaymentInfo(data) {
    if (selectedAccountNumber != null) {
        if (data != null) {
            updatePaymentData(data);
        }
    } else {
        updatePaymentData(null);
    }
}

function getSubscriptionInfo() {
    if (selectedAccountNumber != null) {
        jagg.post("../blocks/monetizing/account/info/ajax/get.jag", {
            action: "get-subscription-info",
            "accountId": selectedAccountNumber
        }, function (result) {
            updateSubscriptionData(result);
        });
    }
};

function updateBasicInfo() {
    //Basic Info updating from the userList data
    $("#subscriber-name").text(usersList[selectedUserId].text);
    $("#complimentary-account").text(usersList[selectedUserId].complimentary);
    $("#email").text(usersList[selectedUserId].email);
    // TO DO execution of below check is removed temporarily since this is not implemented
    // document.getElementById("chkbox-complimentary").checked = usersList[selectedUserId].complimentary;
}

function updateSubscriberData(result) {
    result = JSON.parse(result);

    if (result != null) {
        $(".Monetization-Data").show();
        //Account Summery
        $("#account-name").text(result.data.accountSummary.accountName);
        $("#account-balance").text(result.data.accountSummary.accountBalance);

        //Invoice Data
        var invoiceList = [];
        var dataObj = result.data;
        for (var index in dataObj.invoicesInformation) {
            var invoiceInfoObj =JSON.parse(dataObj.invoicesInformation[index]);
            var data = {
                "date": invoiceInfoObj.date,
                "invoice-num": invoiceInfoObj.InvoiceId,
                "target-date": invoiceInfoObj.TargetDate,
                "amount": invoiceInfoObj.Amount,
                "status": invoiceInfoObj.paid,
                "id": invoiceInfoObj.InvoiceId
            };
            invoiceList.push(data);
        }
        if ($.fn.DataTable.isDataTable("#invoice-info")) {
            jQuery("#invoice-info").dataTable().fnDestroy();
        }

        $("#invoice-info").DataTable({
            responsive: true,
            "data": invoiceList,
            "paging": false,
            "searching": false,

            "columns": [
                {"data": "date", "width": "10%"},
                {
                    "data": "invoice-num", "width": "5%",
                    "render": function (data, type, full, meta) {
                        return  full['invoice-num'];
                    }
                },
                {"data": "target-date", "width": "20%"},
                {"data": "amount", "width": "20%", "sClass": "dt-body-center  dt-head-center"},
                {"data": "status", "width": "20%", "sClass": "dt-body-center  dt-head-center"}
            ],
        });
    } else {
        $(".Monetization-Data").hide();
    }
}

function updatePaymentData(result) {
    var chargeInformation = result;
    for (i = 0; i < chargeInformation.length; i++) {
        chargeInformation[i] = JSON.parse(chargeInformation[i]);
    }
    $("#payments-info").DataTable({
        responsive: true,
        "data": chargeInformation,
        "columns": [
            {"data": "type", "width": "20%", "sClass": "dt-body-center  dt-head-center"},
            {"data": "effectiveDate", "width": "20%", "sClass": "dt-body-center  dt-head-center"},
            {"data": "paymentNumber", "width": "20%", "sClass": "dt-body-center  dt-head-center"},
            {"data": "invoiceNumber.", "width": "20%", "sClass": "dt-body-right dt-head-center"},
            {"data": "Status", "width": "20%", "sClass": "dt-body-center  dt-head-center"}
        ]
    });
}

$("#cloud-menu-popover,#cloud-menu-popover-xs").on("hidden.bs.popover", function () {
    $(".anim-container").children(".clearfix").nextAll().remove();
});

function updateSubscriptionData(result) {
    result = JSON.parse(result);
    var subscriptionList = [];
    if (!result.error) {
        for (var index in result.data) {
            var data = {
                "api-name": result.data[index].AM_API_NAME,
                "api-version": result.data[index].AM_API_VERSION,
                "app-name": result.data[index].AM_APP_NAME,
                "rate-plan-name": result.data[index].RATE_PLAN_NAME,
                "start-date": result.data[index].START_DATE,
                "end-date": result.data[index].END_DATE,
            };
            subscriptionList.push(data);
        }
        //destroy the dataTable if its exist and update it
        if ($.fn.DataTable.isDataTable("#sub-info")) {
            jQuery("#sub-info").dataTable().fnDestroy();
        }
        $("#sub-info").DataTable({
            responsive: true,
            "data": subscriptionList,
            "columns": [
                {"data": "api-name", "width": "15%", "sClass": "dt-body-center dt-head-center"},
                {"data": "api-version", "width": "10%", "sClass": "dt-body-center dt-head-center"},
                {"data": "app-name", "width": "15%", "sClass": "dt-body-center  dt-head-center"},
                {"data": "rate-plan-name", "width": "30%", "sClass": "dt-body-center  dt-head-center"},
                {"data": "start-date", "width": "10%", "sClass": "dt-body-center dt-head-center"},
                {"data": "end-date", "width": "10%", "sClass": "dt-body-center  dt-head-center"}
            ]
        });
    } else {
        showErrorMessage(result);
    }
}

function showErrorMessage(result) {
    noty({
        theme: 'wso2',
        layout: 'topCenter',
        type: 'error',
        closeWith: ['button', 'click'],
        text: result.message,
        buttons: [{
            addClass: 'btn btn-default', text: 'OK', onClick: function () {
                window.location.href = ( result.redirectionURL);
            }
        }],
        animation: {
            open: {height: 'toggle'}, // jQuery animate function property object
            close: {height: 'toggle'}, // jQuery animate function property object
            easing: 'swing', // easing
            speed: 500 // opening & closing animation speed
        }
    });
}

function ShowComplimentaryMessage(dataObj) {
    noty({
        theme: 'wso2',
        layout: 'topCenter',
        type: 'confirm',
        closeWith: ['button', 'click'],
        modal: true,
        text: dataObj.message,
        buttons: [
            {
                addClass: 'btn btn-default', text: 'Cancel', onClick: function ($noty) {
                $noty.close();
            }
            },
            {
                addClass: 'btn btn-primary', text: 'OK', onClick: function ($noty) {
                jagg.syncPost("../blocks/monetizing/subscriber/update/ajax/update.jag", {
                    action: dataObj.action,
                    subscriber: JSON.stringify(usersList[selectedUserId])

                }, function (result) {
                    $noty.close();
                    if (!result.error) {
                        noty({
                            theme: 'wso2',
                            layout: 'topCenter',
                            text: "Successfully changed the account type",
                            type: 'success',
                            animation: {
                                open: {height: 'toggle'}, // jQuery animate function property object
                                close: {height: 'toggle'}, // jQuery animate function property object
                                easing: 'swing', // easing
                                speed: 500 // opening & closing animation speed
                            },
                            closeWith: ['button', 'click']
                        });
                        getUsers();
                        updateBasicInfo();
                    } else {
                        showErrorMessage(result);
                    }
                });
            }
            }
        ],
        animation: {
            open: {height: 'toggle'}, // jQuery animate function property object
            close: {height: 'toggle'}, // jQuery animate function property object
            easing: 'swing', // easing
            speed: 500 // opening & closing animation speed
        }
    });
}

function goToInvoicePage(accountId, invoiceId) {
    var formInvoice = $('<form action="monetization-invoice.jag" method="post">' +
    '<input type="hidden" name="invoiceId" value="' + invoiceId + '"/>' +
    '<input type="hidden" name="accountId" value="' + accountId + '"/>' +
    '</form>');
    $('body').append(formInvoice);
    $(formInvoice).submit();
}
