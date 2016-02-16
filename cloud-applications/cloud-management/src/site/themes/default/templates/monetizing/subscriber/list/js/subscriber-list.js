$(document).ready(function () {
    $.ajax({
        url: "../blocks/monetizing/subscriber/list/ajax/list.jag",
        data: {
            "action": "getSubscribersOfTenant"
        },
        success: function (result) {
            var result = jQuery.parseJSON(result);
            $("#subList").DataTable({
                "data": result.subObj,
                "columns": [
                    {"data": "displayName", "width": "25%",
                        "render": function (data, type, full, meta) {
                            return "<a class='editroles' onclick='return goToAccountInfo(" +
                                " \"" + full['email'] + "\")'' ><u>" + full['displayName']
                                + "</u></a> ";
                        }
                    },
                    {"data": "email", "width": "35%"},
                    {"data": "subscriptionType", "width": "20%"},
                    {"data": function (data, type, full) {
                            var accId = data.accountId;
                            if (accId != null && accId != "" && accId != "undefined") {
                                return "Available";
                            } else {
                                return "N/A"
                            }
                        },
                        "defaultContent": "", "width": "20%"
                    }
                ]
            });
        }
    });
});

function goToAccountInfo(email){
    window.location.href = "subscriber-account-summary.jag?email-address=" + email;
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