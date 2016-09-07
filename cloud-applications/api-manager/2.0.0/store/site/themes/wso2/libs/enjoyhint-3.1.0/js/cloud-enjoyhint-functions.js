/* This function check whether the enjoyhint is enabled or not. If enabled include the configuration
 files link jag and return true */
function isEnjoyHintEnabled() {
    var isInteractiveTutorialEnabled = localStorage.getItem("interactiveTutorialEnabled");
    return isInteractiveTutorialEnabled === "true";
}

// Function to run enjoyhint instance
function runEnjoyHintScript(enjoyhint_instance, script_data) {
    // Start running Enjoyhint instance
    enjoyhint_instance = new EnjoyHint({});
    enjoyhint_instance.setScript(script_data);
    enjoyhint_instance.runScript();
}

// Method to remove all the local storage values when closing the tutorial
function removeLocalStorageVariables() {
    var isInteractiveTutorialEnabled = localStorage.getItem("interactiveTutorialEnabled");
    if (null != isInteractiveTutorialEnabled) {
        localStorage.removeItem("interactiveTutorialEnabled");
    }
    var isEnjoyHintEnabledWithMenu = localStorage.getItem("isEnjoyHintEnabledWithMenu");
    if (null != isEnjoyHintEnabledWithMenu) {
        localStorage.removeItem("isEnjoyHintEnabledWithMenu");
    }
    var apiNameExists = localStorage.getItem("apiName");
    if (null != apiNameExists) {
        localStorage.removeItem("apiName");
    }
    var isWorldBankApiExist = localStorage.getItem("worldBankApiExist");
    if (null != isWorldBankApiExist) {
        localStorage.removeItem("worldBankApiExist");
    }
    var isApiConsoleTabClicked = localStorage.getItem("isApiConsoleTabClicked");
    if (null != isApiConsoleTabClicked) {
        localStorage.removeItem("isApiConsoleTabClicked");
    }
}
