$(document).ready(function () {
    $.ajax({
        url: "../../blocks/pricing/payment-method/info/ajax/get.jag",
        data: {
            "action": "get-payment-methods"
        },
        success: function (result) {
            result = jQuery.parseJSON(result);
            if (!result.error) {
                var paymentMethodsObj = result.message.data;
                var creditCards = paymentMethodsObj.data;
                var defaultMethod = $('#defaultPaymentMethod').val();
                if ($.fn.DataTable.isDataTable('#payment-methods')) {
                    var table = $('#payment-methods').DataTable();
                    table.destroy();
                }
                $('#payment-methods').DataTable({
                    responsive: true,
                    "paging": false,
                    "data": creditCards,
                    "columns": [
                        {"data": "brand", "width": "15%", "sClass": "dt-body-right"},
                        {"data": "name", "width": "25%"},
                        {"data": "last4", "width": "18%", "sClass": "dt-body-right"},
                        {
                            "data": "null", "width": "15%", "sClass": "dt-body-right",
                            "render": function (data, type, full, meta) {
                                return +full['exp_month'] + ' / ' + full['exp_year'];
                            }
                        },
                        {
                            "data": "id", "width": "7%", "sClass": "dt-body-center",
                            "render": function (data, type, full, meta) {
                                if (full['id'] == defaultMethod) {
                                    return '<a class="editroles"><i class="fw fw-check"></i></a>';
                                } else {
                                    return '';
                                }
                            }
                        },
                        {
                            "data": "id", "orderable": false, "width": "15%",
                            "render": function (data, type, full, meta) {
                                if (full['id'] == defaultMethod) {
                                    return '';
                                } else {
                                    return "<a class='editroles' onclick='return makeDefaultMethod(\"" + full['id'] + "\")'' ><i class='fw fw-star'></i> Set as Primary</a> ";
                                }
                            }
                        },
                        {
                            "data": "id", "orderable": false, "width": "5%", "sClass": "dt-body-center",
                            "render": function (data, type, full, meta) {
                                return "<a class='editroles' onclick='return removePaymentMethod(\"" + full['id'] + "\")'' ><i class='fw fw-delete'></i></a> ";
                            }
                        }
                    ]
                });
            } else {
                $('.message_box').empty();
                jagg.message({
                    content: "Unable to load the payment methods at the moment. Please contact WSO2 Cloud Team for help",
                    type: 'error',
                    cbk: function () {
                        window.location.href = result.cloudmgtURL + "/site/pages/contact-us.jag";
                    }
                });
            }
        }
    });
});

function makeDefaultMethod(methodId) {
    $.ajax({
        url: "../../blocks/pricing/payment-method/info/ajax/get.jag",
        data: {
            "action": "setDefaultMethod",
            "paymentMethodId": methodId
        },
        success: function (data) {
            var result = jQuery.parseJSON(data);
            var tenantDomain = result.tenantDomain;
            if (!result.error) {
                jagg.message({
                    content: 'Successfully updated the default payment method.',
                    type: 'info',
                    cbk: function () {
                        var form = $('<form action="manage-account.jag?tenant=' + tenantDomain + '&fieldPassthrough1='+ methodId +'"' + 'method="post">' +
                            '<input type="hidden" name="action" value="viewPaymentMethod"/>' +
                            '</form>');
                        $('body').append(form);
                        $(form).submit();
                    }
                });
            } else {
                $('.message_box').empty();
                jagg.message({
                    content: "Unable to make the default payment methods at the moment. Please contact WSO2 Cloud Team for help",
                    type: 'error',
                    cbk: function () {
                        window.location.href = result.cloudmgtURL + "/site/pages/contact-us.jag";
                    }
                });
            }
        }
    });
};


function removePaymentMethod(methodId) {
    var defaultMethod = $('#defaultPaymentMethod').val();
    $.ajax({
        url: "../../blocks/pricing/payment-method/info/ajax/get.jag",
        data: {
            "action": "removePaymentMethod",
            "paymentMethodId": methodId
        },
        success: function (data) {
            var result = jQuery.parseJSON(data);
            var tenantDomain = result.tenantDomain;
            if (!result.error) {
                jagg.message({
                    content: 'Successfully removed the payment method.',
                    type: 'info',
                    cbk: function () {
                        var form = $('<form action="manage-account.jag?tenant=' + tenantDomain + '&fieldPassthrough1='+ defaultMethod +'"' + 'method="post">' +
                            '<input type="hidden" name="action" value="viewPaymentMethod"/>' +
                            '</form>');
                        $('body').append(form);
                        $(form).submit();
                    }
                });
            } else {
                $('.message_box').empty();
                jagg.message({
                    content: "Unable to remove the payment methods at the moment. Please contact WSO2 Cloud Team for help",
                    type: 'error',
                    cbk: function () {
                        window.location.href = result.cloudmgtURL + "/site/pages/contact-us.jag";
                    }
                });
            }
        }
    });
};


