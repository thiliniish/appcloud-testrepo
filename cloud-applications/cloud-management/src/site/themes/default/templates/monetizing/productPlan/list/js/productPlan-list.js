$(document).ready(function () {
    getPlanList();
});

function getPlanList() {
    jagg.post("../blocks/monetizing/productPlan/list/ajax/list.jag", {
        action: "get-payment-plans-of-tenant"
    }, function (result) {
        result = JSON.parse(result);
        if (!result.error) {
            planList = result.data;
            updatePlanList(planList);

        } else {
            showErrorMessage(result);
        }
    });
}
function updatePlanList(data) {
    $('#plan-listing').DataTable({
        responsive: true,
        "data": data,
        "columns": [
            {"data": "planName", "width": "15%", "sClass": "dt-body-center  dt-head-center"},
            {"data": "price", "width": "15%", "sClass": "dt-body-center dt-head-center "},
            {"data": "dailyLimit", "width": "10%", "sClass": "dt-body-center  dt-head-center"},
            {"data": "overage", "orderable": false, "width": "15%", "sClass": "dt-body-center dt-head-center"},
            {"data": "overageLimit", "width": "15%", "sClass": "dt-body-center  dt-head-center"},
            {"data": "billingActive", "orderable": false, "width": "20%", "sClass": "dt-body-center  dt-head-center"},
            {
                "data": null, "orderable": false, "width": "10%", "sClass": "dt-body-center dt-head-center",
                "render": function (data, type, full, meta) {
                    if (full['billingActive'] != 'FREE') {
                        return "<a class='editroles' onclick='return goToAccountInfo(" +
                            "\"" + full['planName'] + "\",\"" + full['dailyLimit'] + "\",\"" + full['price'] + "\",\""
                            + full['overage'] + "\",\"" + full['overageLimit'] + "\")' ><i class='fw fw-edit'></i></a>";
                    } else {
                        return "<i class='fw fw-edit' style='color: #999'></i>"
                    }
                }
            }
        ],
        "order": [[2, "desc"]]
    });
}
function goToAccountInfo(planName, dailyLimit, price, overage, overageLimit) {
    window.location.href = "monetization-add-payment-plan.jag?planName=" + planName + "&dailyLimit=" + dailyLimit
    + "&price=" + price + "&overage=" + overage + "&overageLimit=" + overageLimit;
}

$(".side-pane-trigger").click(function () {
    var rightPane = $(".right-pane");
    var leftPane = $(".left-pane");
    if (rightPane.hasClass("visible")) {
        rightPane.animate({"left": "0em"}, "slow").removeClass("visible");
        leftPane.animate({"left": "-18em"}, "slow");
        $(this).find("i").removeClass("fa-arrow-left").addClass("fa-reorder");
    } else {
        rightPane.animate({"left": "18em"}, "slow").addClass("visible");
        leftPane.animate({"left": "0em"}, "slow");
        $(this).find("i").removeClass("fa-reorder").addClass("fa-arrow-left");
    }
});
