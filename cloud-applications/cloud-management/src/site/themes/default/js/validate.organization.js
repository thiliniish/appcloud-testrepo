function validateOrganizationNameAlphaNumeric(organizationName) {
    var isOrganizationNameValid = false;

    //Regular expression to check for whitespaces and alpha numberic characters only in the organization name.
    var patternForAlphaNumericChars = /^[a-zA-Z0-9 ]*$/;
    var isValidChar = patternForAlphaNumericChars.test(organizationName);
    if (isValidChar) {
        isOrganizationNameValid = true;
    }
    return isOrganizationNameValid;
}
