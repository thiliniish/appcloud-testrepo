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
            {"data": "planName", "width": "10%"},
            {"data": "dailyLimit", "width": "20%", "sClass": "dt-body-right"},
            {"data": "price", "width": "20%", "sClass": "dt-body-right"},
            {"data": "overage", "orderable": false, "width": "20%", "sClass": "dt-body-right"},
            {"data": "billingActive", "orderable": false, "width": "20%"},
            {
                "data": null, "orderable": false, "width": "20%", "sClass": "dt-body-center",
                "render": function (data, type, full, meta) {
                    if (full['billingActive'] != 'FREE') {
                        return "<a class='editroles' onclick='return goToAccountInfo(" +
                            "\"" + full['planName'] + "\",\"" + full['dailyLimit'] + "\",\"" + full['price'] + "\",\""
                            + full['overage'] + "\")' ><i class='fw fw-edit'></i></a>";
                    } else {
                        return "<i class='fw fw-edit' style='color: #999'></i>"
                    }
                }
            }
        ],
        "order": [[2, "desc"]]
    });
}
function goToAccountInfo(planName, dailyLimit, price, overage) {
    window.location.href = "monetization-add-payment-plan.jag?planName=" + planName + "&dailyLimit=" + dailyLimit
        + "&price=" + price + "&overage=" + overage;
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
