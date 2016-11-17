var enjoyhint_instance_tab = null;

$(document).ready(function () {
    $('ul.nav-tabs a').click(function (e) {
        e.preventDefault();
        $(this).tab('show');
    });
    //$('#tab0').show();

    if (isEnjoyHintEnabled()) {
        var storeStep = localStorage.getItem("storeStep");
        if ( storeStep === "subscribed") {
            runEnjoyHintScript(enjoyhint_instance_tab, ui_tab_script_data);
        }
    }
});

function setLocalStorageValue() {
    localStorage.setItem("isApiConsoleTabClicked", 'true');
};
