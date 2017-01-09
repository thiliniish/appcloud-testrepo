/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

//Variables
var usersWithRoles = [];
var pendingUsersWithRoles = [];
var checkedUsers = [];
var checkedInvitations = [];
var selectedCount = 0;
var selectedInvitationCount = 0;
var pageNumberForMembers = 1;
var pageNumberForInvitations = 1;
var totalPagesForMembers = 1;
var totalPagesForInvitations = 1;
var appOwners = [];
var userCheckBoxId = "ck_";
var invitationCheckBoxId = "ick_";
var allRoles = [];
var allRolesForInvitations = [];
var finalUsers = [];//globally declared
var finalInvitations = [];

//<Start> UI Validation functions

/* Validates a single email address given as an input parameter */
function ValidateEmail(inputText) {
    var mailformat = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/;
    if (mailformat.test(inputText)) {
        return true;
    }
    else {
        return false;
    }
}

/* Enable buttons in user list panel*/
function enable(btnId) {
    var classes = $('#' + btnId).attr('class');
    if (classes.indexOf('disable') !== -1) {
        $('#' + btnId).removeClass("disable");
    }
}

/* Disable buttons in user list panel*/
function disable(btnId) {
    var classes = $('#' + btnId).attr('class');
    if (classes.indexOf('disable') === -1) {
        $('#' + btnId).addClass("disable");
    }
}

/*Enable or Disable buttons relevant to user invitations*/
function toggleEditDeleteButtons(enableParam) {
    if (enableParam) {
        //enable delete and edit
        enable('jsroleAssignPopup');
        enable('removeUsers');
    } else {
        //disable edit and delete buttons
        disable('jsroleAssignPopup');
        disable('removeUsers');
    }
}

/*Enable action buttons relevant to user invitations*/
function enableActionButtonsForInvitations(enableParam) {
    if (enableParam) {
        //enable delete and edit
        enable('jsInvitationRoleAssignPopup');
        enable('revokeInvitation');
    } else {
        //disable edit and delete buttons
        disable('jsInvitationRoleAssignPopup');
        disable('revokeInvitation');
    }
}

/* Selects relevant checkbox */
function selectCheckBox(id, isInvited) {
    var checkBoxId = userCheckBoxId;
    if (isInvited) {
        checkBoxId = invitationCheckBoxId;
    }
    var isChecked = $('#' + checkBoxId + id).is(':checked');
    if (isChecked) {
        $('#' + checkBoxId + id).removeAttr('checked');
    } else {
        $('#' + checkBoxId + id).attr('checked', true);
    }
    enableButton($('#' + checkBoxId + id).is(':checked'), isInvited);
}

/* Enable relevant buttons */
function enableButton(ischecked, isInvited) {
    if (ischecked) {
        if (isInvited) {
            selectedInvitationCount++;
            enableActionButtonsForInvitations(true);
        } else {
            selectedCount++;
            toggleEditDeleteButtons(true);
        }
    } else {
        if (isInvited) {
            selectedInvitationCount--;
            if (selectedInvitationCount === 0) {
                enableActionButtonsForInvitations(false);
            }
        } else {
            selectedCount--;
            if (selectedCount === 0) {
                toggleEditDeleteButtons(false);
            }
        }
    }
}

//<End> UI validation functions

/* Get list of blocked suscriptions */
function getBlockedSubscriptions() {
    var blockedSubs;
    jagg.syncPost("../blocks/tenant/users/add/ajax/add.jag", {
            action: "getBlockedSubscriptions"
        },
        function (result) {
            blockedSubs = result;
        });
    return blockedSubs;
}

/*Update user roles in popup list for users */
function updateRolePopups() {
    var $jsroleAssignPopupForm = $('#jsroleAssignPopupForm');
    var $jsroleAssign = '';
    var blockSubscriptions = getBlockedSubscriptions();
    //getting the roles of each subscriptions
    for (var i = 0; i < type.length; i++) {
        if (blockSubscriptions.indexOf(type[i].id) < 0 || blockSubscriptions == null) {
            var tempRoleArray = type[i].roleTypes;
            for (var count = 0; count < tempRoleArray.length; count++) {
                $jsroleAssign = $jsroleAssign + '<div class="input_row">' +
                    '<label class="text-box-overrider" data-role=' + tempRoleArray[count].roleName + '><span class="checkbox-unchecked"></span>' +
                    tempRoleArray[count].displayRoles + '</label></div>';
            }
        }
    }
    $jsAssignButton = $('<div class="btn_row"><button class="btn main small" type="button" id="saveUserRoles">Save</button><a href="#" class="popover_close small">Close</a> </div>');
    $jsroleAssignPopupForm.append($jsroleAssign);
    $jsroleAssignPopupForm.append($jsAssignButton);
}

/* Update user roles in popup list for invitations */
function updateRolePopupsForInvitations() {
    var $jsInvitationRoleAssignPopupForm = $('<form class="form-container" id="jsinvitationPopupForm">');
    var $jsInvitationRoleAssign = '';
    var blockedSubscriptions = getBlockedSubscriptions();
    //getting the roles of each subscriptions
    for (var i = 0; i < type.length; i++) {
        if (blockedSubscriptions.indexOf(type[i].id) < 0 || blockedSubscriptions == null) {
            var tempRoleArray = type[i].roleTypes;
            for (var count = 0; count < tempRoleArray.length; count++) {
                $jsInvitationRoleAssign = $jsInvitationRoleAssign + '<div class="input_row">' + '<label ' +
                    'class="text-box-overrider-invitations" data-role=' + tempRoleArray[count].displayRoles + '><span ' +
                    'class="checkbox-unchecked"></span>' + tempRoleArray[count].displayRoles + '</label></div>';
            }
        }
    }
    $jsRoleAssignButton = $('<div class="btn_row"><button class="btn main small" type="button" id="updateInvitations">Update</button><a href="#" class="popover_close small">Close</a> </div></form>');
    $jsInvitationRoleAssignPopupForm.append($jsInvitationRoleAssign);
    $jsInvitationRoleAssignPopupForm.append($jsRoleAssignButton);
    var $jsInvitationPopupFormDiv = $('#jsinvitationPopupFormDiv');
    $jsInvitationPopupFormDiv.append($jsInvitationRoleAssignPopupForm);
}

/* Update user list */
function updateTable() {
    var tenantDomain = $('#tenantDomain').val();
    $('.cleanable').remove();
    jagg.syncPost("../blocks/tenant/users/get/ajax/get.jag", {
        action: "getUsersofTenant",
        tenantDomain: tenantDomain
    }, function (result) {
        var recordCount = 0;
        if (result !== undefined) {
            usersWithRoles = result;
            recordCount = usersWithRoles.length;
        }
        if (recordCount > 0) {
            var countString = '';
            if (recordCount === 1) {
                $('#membersCount').replaceWith('<h2 id="membersCount" class="big push_bottom_40">1 Member in Organization</h2>');
            } else {
                $('#membersCount').replaceWith('<h2 id="membersCount" class="big push_bottom_40">' + recordCount + ' Members in Organization</h2>');
            }
            createTable(usersWithRoles);
        } else {
            $('#userListContainer').html('<li class="noData-message">no data</li>');
        }
    }, function (jqXHR, textStatus, errorThrown) {
        $('#userListContainer').html('<li class="noData-message">Error Occurred while updating user list. Please' +
            ' contact WSO2 Cloud team for help</li>');
    });
}

/* Update invitation list */
function updateInvitationTable(isBasicPageLoad) {
    var tenantDomain = $('#tenantDomain').val();
    //Get pending invitation details
    jagg.syncPost("../blocks/tenant/users/get/ajax/get.jag", {
        action: "getPendingUsers",
        tenantDomain: tenantDomain
    }, function (response) {
        if (response.error == false) {
            var result = response.result;
            var recordCount = 0;
            if (result !== undefined) {
                pendingUsersWithRoles = result;
                recordCount = pendingUsersWithRoles.length;
            }
            if (recordCount > 0) {
                if (recordCount === 1) {
                    $('#pendingMembersCount').replaceWith('<h2 id="pendingMembersCount" class="big push_bottom_40">1 ' +
                        'Pending Invitation</h2>');
                } else {
                    $('#pendingMembersCount').replaceWith('<h2 id="pendingMembersCount" class="big ' +
                        'push_bottom_40">' + recordCount + ' Pending Invitations</h2>');
                }
            }
            if (isBasicPageLoad && recordCount > 0) {
                if (recordCount == 1) {
                    $('#invitationCount').text(recordCount + " invitation is pending... ");
                } else {
                    $('#invitationCount').text(recordCount + " invitations are pending... ");
                }
                $('#pendingInvitationsMessage').show();
            }
            if (!isBasicPageLoad) {
                if (recordCount == 0) {
                    $('#pendingInvitations').hide();
                } else {
                    createPendingUserTable(pendingUsersWithRoles);
                }
            }
        }
    }, function (jqXHR, textStatus, errorThrown) {
        $('#pendingUserListContainer').html('<li class="noData-message">Error Occurred while updating invitation' +
            ' list. Please contact WSO2 Cloud team for help</li>');
    });
}

/*Creates pending invitations table */
function createPendingUserTable(pendingUsersWithRolesArray) {
    var maxItemsInPage = Math.floor(parseFloat($('#maxItemsPerPage').val()));
    $('.cleanableInvitation').remove();
    var $pendingUserListContainer = $('#pendingUserListContainer');
    /*getting the total number of pages using max items in a page and checking if the number of invitations are equal
     to the max items in a page,else add a new page to the total*/
    totalPagesForInvitations = ((pendingUsersWithRolesArray.length % maxItemsInPage) == 0) ? (pendingUsersWithRolesArray.length / maxItemsInPage) : (pendingUsersWithRolesArray.length / maxItemsInPage) + 1;
    totalPagesForInvitations = Math.floor(totalPagesForInvitations);
    if (totalPagesForInvitations > 1) {
        $('.pageFooterInvitations').show();
        $('.pageFooterInvitations').bootpag({
            total: totalPagesForInvitations,
            page: pageNumberForInvitations
        }).show();
    } else {
        $('.pageFooterInvitations').hide();
    }
    var isInvited = true;
    //looping until the max number of invitations in a page is added
    for (var i = ((pageNumberForInvitations - 1) * maxItemsInPage); i < pendingUsersWithRolesArray.length && i < (pageNumberForInvitations * maxItemsInPage); i++) {
        var pendingUserRoles = pendingUsersWithRolesArray[i];
        var checkBoxString = '<input id="ick_' + i + '" type="checkbox" name="action_check_invitation" ' +
            'class="action_check_invitation" ' +
            'data-roles="' + pendingUserRoles.roles + '" data-user="' + pendingUserRoles.email + '" />';
        //Show pending invitations
        var $pendingUserListContainerRow = $('<li class="list_row_item cleanableInvitation" data-email="' + pendingUserRoles.email + '" ' +
            'data-name="' + pendingUserRoles.email + '">' +
            '<ul class="list_row" id="' + pendingUserRoles.email + '">' +
            '<li class="list_col first_list_col item_select">' +
            '<div class="list_col_content">' +
            checkBoxString +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_member">' +
            '<div class="list_col_content">' +
            '<div class="image_list">' +
            '<dl>' +
            '<dt>' + pendingUserRoles.email + '</dt>' +
            '<dd class="img"><span class="icon-user"></span></dd>' +
            '<dd>' + pendingUserRoles.email + '</dd>' +
            '</dl>' +
            '</div>' +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col  team_role">' +
            '<div class="list_col_content">' + pendingUserRoles.roles.toString() + '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_last_login">' +
            '<div class="list_col_content">' +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_controller">' +
            '<div class="list_col_heading">&nbsp;</div>' +
            '<div class="list_col_content">&nbsp;</div>' +
            '</li>' +
            '</ul>' +
            '</li>');

        $pendingUserListContainer.append($pendingUserListContainerRow);
        enableActionButtonsForInvitations(false);
    }
    selectedInvitationCount = 0;
    $('#pendingInvitations').show();
    $('#pendingInvitationsMessage').hide();
    if (totalPagesForInvitations > 1) {
        $('.pageFooterInvitations').bootpag({
            total: totalPagesForInvitations
        }).on("page", function (event, num) {
            pageNumberForInvitations = num;
            createPendingUserTable(pendingUsersWithRoles);
        });
    } else {
        $('.pageFooterInvitations').hide();
    }
    $('#pendingUserListContainer .action_check_invitation').click(function () {
        manageRoleCheckListForInvitations();
    });
    $('.action_check_invitation').click(function () {
        enableButton(this.checked, isInvited);
    });
}

/* This method will create the members list table */
function createTable(usersWithRolesArray) {
    var adminRoleDisplayName = $('#adminRoleDisplayName').val();
    var maxItemsInPage = Math.floor(parseFloat($('#maxItemsPerPage').val()));
    appOwners = [];
    $('.cleanable').remove();
    var $userListContainer = $('#userListContainer');
    var chkDisableStr = "";
    var permittedToInvite = $('#permittedToInvite').val();
    if (permittedToInvite == "false") {
        chkDisableStr = "style = 'display:none'";
    }
    /*getting the total number of pages using max users in a page
     checking if the number of members are equal to the max members in a page,else add a new page to the total*/
    totalPagesForMembers = ((usersWithRolesArray.length % maxItemsInPage) == 0) ? (usersWithRolesArray.length / maxItemsInPage) : (usersWithRolesArray.length / maxItemsInPage) + 1;
    totalPagesForMembers = Math.floor(totalPagesForMembers);
    if (totalPagesForMembers > 1) {
        $('.pagefooter').bootpag({
            total: totalPagesForMembers,
            page: pageNumberForMembers
        }).show();
    } else {
        $('.pagefooter').hide();
    }
    var isInvited = false;
    //looping until the max number of users in a page is added
    for (var i = ((pageNumberForMembers - 1) * maxItemsInPage); i < usersWithRolesArray.length && i < (pageNumberForMembers * maxItemsInPage); i++) {
        var userRoles = usersWithRolesArray[i];
        var isAdminUser = false;
        var checkBoxString = "";
        if (userRoles.displayRoles.length > 0) {
            for (var j = 0; j < userRoles.displayRoles.length; j++) {
                if (adminRoleDisplayName == userRoles.displayRoles[j]) {
                    isAdminUser = true;
                }
            }
        }
        if (!isAdminUser) {
            checkBoxString = '<input id="ck_' + i + '" type="checkbox" name="action_check" class="action_check" data-roles="' + userRoles.roles + '" data-user="' + userRoles.userName + '"' + chkDisableStr + ' />';
        }

        var $userListContainerRow = $('<li class="list_row_item cleanable" data-email="' + userRoles.email + '" data-name="' + userRoles.userDisplayName + '">' +
            '<ul class="list_row" id="' + userRoles.userName + '">' +
            '<li class="list_col first_list_col item_select">' +
            '<div class="list_col_content">' +
            checkBoxString +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_member">' +
            '<div class="list_col_content">' +
            '<div class="image_list">' +
            '<dl>' +
            '<dt>' + userRoles.displayName + '</dt>' +
            '<dd class="img"><span class="icon-user"></span></dd>' +
            '<dd>' + userRoles.email + '</dd>' +
            '</dl>' +
            '</div>' +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col  team_role">' +
            '<div class="list_col_content">' + userRoles.displayRoles.toString() + '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_last_login">' +
            '<div class="list_col_content">' +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_controller">' +
            '<div class="list_col_heading">&nbsp;</div>' +
            '<div class="list_col_content">&nbsp;</div>' +
            '</li>' +
            '</ul>' +
            '</li>');

        if (userRoles.roles.indexOf("appowner") != -1) {
            $('.action_check', $userListContainerRow).attr('data-deletable', 'false');
            appOwners.push(userRoles.userName);
        }
        $userListContainer.append($userListContainerRow);
        toggleEditDeleteButtons(false);
        selectedCount = 0;
    }
    $('#userListContainer .action_check').click(function () {
            manageRoleCheckList();
        }
    );
    $('.action_check').click(function () {
        enableButton(this.checked, isInvited);
    });
}

/*This method will create the pending invitations table */
function createPendingUserTable(pendingUsersWithRolesArray) {
    $('.cleanableInvitation').remove();
    var $pendingUserListContainer = $('#pendingUserListContainer');
    /*getting the total number of pages using max items in a page and checking if the number of invitations are equal
     to the max items in a page,else add a new page to the total*/
    totalPagesForInvitations = ((pendingUsersWithRolesArray.length % maxItemsInPage) == 0) ? (pendingUsersWithRolesArray.length / maxItemsInPage) : (pendingUsersWithRolesArray.length / maxItemsInPage) + 1;
    totalPagesForInvitations = Math.floor(totalPagesForInvitations);
    if (totalPagesForInvitations > 1) {
        $('.pageFooterInvitations').show();
        $('.pageFooterInvitations').bootpag({
            total: totalPagesForInvitations,
            page: pageNumberForInvitations
        }).show();
    } else {
        $('.pageFooterInvitations').hide();
    }
    var isInvited = true;
    //looping until the max number of invitations in a page is added
    for (var i = ((pageNumberForInvitations - 1) * maxItemsInPage); i < pendingUsersWithRolesArray.length && i < (pageNumberForInvitations * maxItemsInPage); i++) {
        var pendingUserRoles = pendingUsersWithRolesArray[i];
        var checkBoxString = '<input id="ick_' + i + '" type="checkbox" name="action_check_invitation" ' +
            'class="action_check_invitation" ' +
            'data-roles="' + pendingUserRoles.roles + '" data-user="' + pendingUserRoles.email + '" />';
        //Show pending invitations
        var $pendingUserListContainerRow = $('<li class="list_row_item cleanableInvitation" data-email="' + pendingUserRoles.email + '" ' +
            'data-name="' + pendingUserRoles.email + '">' +
            '<ul class="list_row" id="' + pendingUserRoles.email + '">' +
            '<li class="list_col first_list_col item_select">' +
            '<div class="list_col_content">' +
            checkBoxString +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_member">' +
            '<div class="list_col_content">' +
            '<div class="image_list">' +
            '<dl>' +
            '<dt>' + pendingUserRoles.email + '</dt>' +
            '<dd class="img"><span class="icon-user"></span></dd>' +
            '<dd>' + pendingUserRoles.email + '</dd>' +
            '</dl>' +
            '</div>' +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col  team_role">' +
            '<div class="list_col_content">' + pendingUserRoles.roles.toString() + '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_last_login">' +
            '<div class="list_col_content">' +
            '</div>' +
            '</li>' +
            '<li onclick="selectCheckBox(' + i + ' , ' + isInvited + ')" class="list_col team_controller">' +
            '<div class="list_col_heading">&nbsp;</div>' +
            '<div class="list_col_content">&nbsp;</div>' +
            '</li>' +
            '</ul>' +
            '</li>');

        $pendingUserListContainer.append($pendingUserListContainerRow);
        enableActionButtonsForInvitations(false);
    }
    selectedInvitationCount = 0;
    $('#pendingInvitations').show();
    $('#pendingInvitationsMessage').hide();
    if (totalPagesForInvitations > 1) {
        $('.pageFooterInvitations').bootpag({
            total: totalPagesForInvitations
        }).on("page", function (event, num) {
            pageNumberForInvitations = num;
            createPendingUserTable(pendingUsersWithRoles);
        });
    } else {
        $('.pageFooterInvitations').hide();
    }
    $('#pendingUserListContainer .action_check_invitation').click(function () {
        manageRoleCheckListForInvitations();
    });
    $('.action_check_invitation').click(function () {
        enableButton(this.checked, isInvited);
    });
}

/* Get role details given role name */
var getRoleByName = function (roleName, isInvited) {
    var roleObjList = allRoles;
    var roleObj;
    if (isInvited) {
        roleObjList = allRolesForInvitations;
    }
    for (var i = 0; i < roleObjList.length; i++) {
        if (roleObjList[i].role_name == roleName) {
            roleObj = roleObjList[i];
        }
    }
    return roleObj;
};

/* Manages role check lists for users */
var manageRoleCheckList = function () {
    allRoles = [];
    $('#jsroleAssignPopupForm label').each(function () {
        var trole = $(this).attr('data-role');
        if (trole != undefined) {
            allRoles.push({dom_obj: this, role_name: trole, users: []});
        }
    });
    //allRoles.push({dom_obj:{}, role_name:'appowner',users:[] });

    //Push users to specific roles
    var numberOfUsersChecked = 0;
    var isInvited = false;
    $('#userListContainer .action_check').each(function () {
        if ($(this).is(':checked')) {
            numberOfUsersChecked++;
            var userName = $(this).attr('data-user').replace(/^\s\s*/, '').replace(/\s\s*$/, '');
            var tmrole = $(this).attr('data-roles');
            if (tmrole != undefined && tmrole.length > 0) {
                var allRolesForUser = tmrole.split('\n');
                for (var i = 0; i < allRolesForUser.length; i++) {
                    var role = allRolesForUser[i].replace(/^\s\s*/, '').replace(/\s\s*$/, '');
                    getRoleByName(role, isInvited).users.push(userName);
                }
            }
        }

    });
    //Now use the allRows array to show the role checkboxes..
    if (numberOfUsersChecked > 0) {
        for (var i = 0; i < allRoles.length; i++) {
            if (allRoles[i].users.length == numberOfUsersChecked) { //So every user has this role
                $('span', allRoles[i].dom_obj).replaceWith('<span class="checkbox-checked"></span>');
            } else if (allRoles[i].users.length == 0) {
                $('span', allRoles[i].dom_obj).replaceWith('<span class="checkbox-unchecked"></span>');
            } else if (allRoles[i].users.length != 0 && allRoles[i].users.length < numberOfUsersChecked) {
                $('span', allRoles[i].dom_obj).replaceWith('<span class="checkbox-half"></span>');
            }
        }
    } else {
        for (var i = 0; i < allRoles.length; i++) {
            $('span', allRoles[i].dom_obj).replaceWith('<span class="checkbox-unchecked"></span>');
        }
    }
};

/* Manages role check lists for invitations */
var manageRoleCheckListForInvitations = function () {
    allRolesForInvitations = [];

    $('#jsinvitationPopupForm label').each(function () {
        var role = $(this).attr('data-role');
        if (role != undefined) {
            allRolesForInvitations.push({
                dom_obj: this,
                role_name: role,
                users: []
            });
        }
    });
    //Push invitees to specific roles
    var numberOfInvitationsChecked = 0;
    var isInvited = true;
    $('#pendingUserListContainer .action_check_invitation').each(function () {
        if ($(this).is(':checked')) {
            numberOfInvitationsChecked++;
            var username = $(this).attr('data-user').replace(/^\s\s*/, '').replace(/\s\s*$/, '');
            var role = $(this).attr('data-roles');
            if (role != undefined && role.length > 0) {
                var allRolesForInvitation = role.split(',');
                for (var i = 0; i < allRolesForInvitation.length; i++) {
                    var role = allRolesForInvitation[i].replace(/^\s\s*/, '').replace(/\s\s*$/, '');
                    var roleObj = getRoleByName(role, isInvited);
                    roleObj.users.push(username);
                }
            }
        }
    });
    //Display checkboxes based on allRolesForInvitations
    if (numberOfInvitationsChecked > 0) {
        for (var i = 0; i < allRolesForInvitations.length; i++) {
            if (allRolesForInvitations[i].users.length == numberOfInvitationsChecked) { //Every invitation has this role
                $('span', allRolesForInvitations[i].dom_obj).replaceWith('<span class="checkbox-checked"></span>');
            } else if (allRolesForInvitations[i].users.length == 0) {
                $('span', allRolesForInvitations[i].dom_obj).replaceWith('<span class="checkbox-unchecked"></span>');
            } else if (allRolesForInvitations[i].users.length != 0 && allRolesForInvitations[i].users.length < numberOfInvitationsChecked) {
                $('span', allRolesForInvitations[i].dom_obj).replaceWith('<span class="checkbox-half"></span>');
            }
        }
    } else {
        for (var i = 0; i < allRolesForInvitations.length; i++) {
            $('span', allRolesForInvitations[i].dom_obj).replaceWith('<span class="checkbox-unchecked"></span>');
        }
    }
};

$(document).ready(function () {
    var isInvitationSent = $('#isInvitationSent').val();
    if (isInvitationSent == "true") {
        jagg.message({
            type: 'success',
            content: 'You have successfully sent the member invitations.',
            type: 'success',
            cbk: function () {
                window.location.href = "../pages/user.jag";
            }
        });
    }
    // hide pending invitations related details
    $('#pendingInvitationsMessage').hide();
    $('#pendingInvitations').hide();
    $('.pageFooterInvitations').hide();

    updateTable();
    // If isBasicPageLoad is true, only the message with available invitation count is shown
    // otherwise the expanded table is shown
    var isBasicPageLoad = true;
    updateInvitationTable(isBasicPageLoad);
    updateRolePopups();
    updateRolePopupsForInvitations();

    if (totalPagesForMembers > 1) {
        $('.pagefooter').bootpag({
            total: totalPagesForMembers
        }).on("page", function (event, num) {
            pageNumberForMembers = num;
            createTable(usersWithRoles);
        });
    } else {
        $('.pagefooter').hide();
    }
    var permittedToInvite = $('#permittedToInvite').val();
    if (permittedToInvite == "false") {
        $('#editDelDiv').hide();
        $('#btnAddMembers').hide();
        $('#select_all_check').parent().hide();
    }
    $('#jsroleAssignPopup').each(function () {
        $(this).qtip({
            content: {
                text: $(this).next()
            },
            style: {
                classes: 'popover_box short_box',
                widget: false,
                def: false,
                tip: false
            },
            hide: {
                fixed: true,
                event: null,
                effect: function (offset) {
                    $(this).slideUp(200);
                }
            },
            show: {
                event: 'click',
                effect: function (offset) {
                    if (selectedCount !== 0) {

                        $(this).slideDown(200);
                    }
                }
            },
            events: {
                show: function (event, api) {
                    api.elements.target.addClass('active');
                    var $el = $(api.elements.target[0]);
                    $el.qtip('option', 'position.my', ($el.data('popover-my-position') == undefined) ? 'top right' : $el.data('popover-my-position'));
                    $el.qtip('option', 'position.at', ($el.data('popover-target-position') == undefined) ? 'bottom right' : $el.data('popover-target-position'));
                    $('.popover_content', $(this)).removeClass("hide");
                    manageRoleCheckList();
                },
                hide: function (event, api) {
                    api.elements.target.removeClass('active');
                }
            }
        });
    }).bind('click', function (event) {
        event.preventDefault();
        return false;
    });
    $('#jsInvitationRoleAssignPopup').each(function () {
        $(this).qtip({
            content: {
                text: $(this).next()
            },
            style: {
                classes: 'popover_box short_box',
                widget: false,
                def: false,
                tip: false
            },
            hide: {
                fixed: true,
                event: null,
                effect: function (offset) {
                    $(this).slideUp(200);
                }
            },
            show: {
                event: 'click',
                effect: function (offset) {
                    if (selectedInvitationCount !== 0) {
                        $(this).slideDown(200);
                    }
                }
            },
            events: {
                show: function (event, api) {
                    api.elements.target.addClass('active');
                    var $el = $(api.elements.target[1]);
                    $el.qtip('option', 'position.my', ($el.data('popover-my-position') == undefined) ? 'top right' : $el.data('popover-my-position'));
                    $el.qtip('option', 'position.at', ($el.data('popover-target-position') == undefined) ? 'bottom right' : $el.data('popover-target-position'));
                    $('.popover_content', $(this)).removeClass("hide");
                    manageRoleCheckListForInvitations();
                },
                hide: function (event, api) {
                    api.elements.target.removeClass('active');
                }
            }
        });
    }).bind('click', function (event) {
        event.preventDefault();
        return false;
    });

    $('.text-box-overrider').click(function () {
        var $span = $('span', this);
        var role = $(this).attr('data-role');
        var isInvited = false;
        checkedUsers = [];
        if (allRoles.length > 0) {
            $('#userListContainer .action_check').each(function () {
                    if ($(this).is(':checked')) {
                        checkedUsers.push($(this).attr('data-user'));
                    }
                }
            );
            if ($span.hasClass('checkbox-checked')) {
                $('span', this).replaceWith('<span class="checkbox-unchecked"></span>');
                getRoleByName(role, isInvited).users = [];
            } else if ($span.hasClass('checkbox-unchecked')) {
                if (getRoleByName(role, isInvited).halfUsers != null && getRoleByName(role, isInvited).halfUsers != undefined) {
                    $('span', this).replaceWith('<span class="checkbox-half"></span>');
                    getRoleByName(role, isInvited).users = getRoleByName(role, isInvited).halfUsers;
                } else {
                    $('span', this).replaceWith('<span class="checkbox-checked"></span>');
                    getRoleByName(role, isInvited).users = checkedUsers;
                }
            } else if ($span.hasClass('checkbox-half')) {
                $('span', this).replaceWith('<span class="checkbox-checked"></span>');
                getRoleByName(role, isInvited).halfUsers = checkedUsers;
            }
        } else {
            if ($span.hasClass('checkbox-checked')) {
                $('span', this).replaceWith('<span class="checkbox-unchecked"></span>');
            } else if ($span.hasClass('checkbox-unchecked')) {
                $('span', this).replaceWith('<span class="checkbox-half"></span>');
            } else if ($span.hasClass('checkbox-half')) {
                $('span', this).replaceWith('<span class="checkbox-checked"></span>');
            }
        }
    });

    $('.text-box-overrider-invitations').click(function () {
        var $span = $('span', this);
        var role = $(this).attr('data-role');
        var isInvited = true;
        checkedInvitations = [];
        if (allRolesForInvitations.length > 0) {
            $('#pendingUserListContainer .action_check_invitation').each(function () {
                if ($(this).is(':checked')) {
                    checkedInvitations.push($(this).attr('data-user'));
                }
            });
            if ($span.hasClass('checkbox-checked')) {
                $('span', this).replaceWith('<span class="checkbox-unchecked"></span>');
                getRoleByName(role, isInvited).users = [];
            } else if ($span.hasClass('checkbox-unchecked')) {
                if (getRoleByName(role, isInvited).halfUsers != null && getRoleByName(role, isInvited).halfUsers != undefined) {
                    $('span', this).replaceWith('<span class="checkbox-half"></span>');
                    getRoleByName(role, isInvited).halfusers = getRoleByName(role, isInvited).halfUsers;
                } else {
                    $('span', this).replaceWith('<span class="checkbox-checked"></span>');
                    getRoleByName(role, isInvited).users = checkedInvitations;
                }
            } else if ($span.hasClass('checkbox-half')) {
                $('span', this).replaceWith('<span class="checkbox-checked"></span>');
                getRoleByName(role, isInvited).users = checkedInvitations;
            }
        } else {
            if ($span.hasClass('checkbox-checked')) {
                $('span', this).replaceWith('<span class="checkbox-unchecked"></span>');
            } else if ($span.hasClass('checkbox-unchecked')) {
                $('span', this).replaceWith('<span class="checkbox-half"></span>');
            } else if ($span.hasClass('checkbox-half')) {
                $('span', this).replaceWith('<span class="checkbox-checked"></span>');
            }
        }
    });

    $('#select_all_check').click(function () {
        $('#userListContainer .action_check').each(function () {
                if ($('#select_all_check').is(':checked')) {
                    $(this).attr('checked', 'checked');
                    selectedCount++;
                    toggleEditDeleteButtons(true);
                } else {
                    $(this).removeAttr('checked');
                    selectedCount = 0;
                    toggleEditDeleteButtons(false);
                }
            }
        );
        manageRoleCheckList();
    }).removeAttr('checked');
    toggleEditDeleteButtons(false);//disabling edit and delete button on load

    $('#select_all_invitations_check').click(function () {
        $('#pendingUserListContainer .action_check_invitation').each(function () {
            if ($('#select_all_invitations_check').is(':checked')) {
                $(this).attr('checked', 'checked');
                selectedInvitationCount++;
                enableActionButtonsForInvitations(true);
            } else {
                $(this).removeAttr('checked');
                selectedInvitationCount = 0;
                enableActionButtonsForInvitations(false);
            }
        });
        manageRoleCheckListForInvitations();
    }).removeAttr('checked');

    //disabling edit and delete button for invitations on load
    enableActionButtonsForInvitations(false);

    $('#saveUserRoles').click(function () {
        // iterate through allRoles and save the users with there roles
        finalUsers = [];
        var isInvited = false;
        covertAllRoles(isInvited);
        detectRoleChanges();
        updateUsers();
        $(this).parents('.qtip').qtip("hide");
    });

    $('#updateInvitations').click(function () {
        // iterate through allRoles and save the invitees with their roles
        finalInvitations = [];
        var isInvited = true;
        covertAllRoles(isInvited);
        detectRoleChangesForInvitations();
        updateInvitations();
        $(this).parents('.qtip').qtip("hide");
    });

    $('#removeUsers').click(function () {
        checkedUsers = [];
        jagg.removeMessage('teampageId');
        $('#userListContainer .action_check').each(function () {
                if ($(this).is(':checked')) {
                    checkedUsers.push($(this).attr('data-user'));
                }
            }
        );
        var isOwner = false;
        for (var i in appOwners) {
            if (checkedUsers.indexOf(appOwners[i]) != -1) {
                isOwner = true;
                break;
            }
        }
        if (!isOwner) {
            jagg.popMessage({
                type: 'confirm', title: 'Delete Users', content: 'Are you sure you want to delete members?',
                okCallback: function () {
                    doDeleteUser(checkedUsers);
                }, cancelCallback: function () {
                }
            });
        } else {
            jagg.message({
                content: "Users with 'Application Owner' role cannot be deleted. Please remove them from the selected users list.",
                type: 'warning', id: 'teampageId'
            });
        }
    });

    $('#revokeInvitation').click(function () {
        checkedInvitations = [];
        jagg.removeMessage('teampageId');
        $('#pendingUserListContainer .action_check_invitation').each(function () {
            if ($(this).is(':checked')) {
                checkedInvitations.push($(this).attr('data-user'));
            }
        });
        jagg.popMessage({
            type: 'confirm',
            title: 'Revoke Invitations',
            content: 'Are you sure you want to revoke invitations?',
            okCallback: function () {
                doRevokeInvitation(checkedInvitations);
            },
            cancelCallback: function () {
            }
        });
    });

    function covertAllRoles(isInvited) {
        //convert all roles array in to processable array in update user method
        var allRoleList = allRoles;
        var checkedUserList = checkedUsers;
        if (isInvited) {
            allRoleList = allRolesForInvitations;
            checkedUserList = checkedInvitations;
        }
        for (var checkedUser in checkedUserList) {
            var user = {};
            user.name = checkedUserList[checkedUser];
            user.roles = [];
            if (isInvited) {
                finalInvitations.push(user);
            } else {
                finalUsers.push(user);
            }
        }
        for (var index in allRoleList) {
            var tempRole = allRoleList[index];
            var tmpRoleName = tempRole.role_name;
            var tmpUsers = tempRole.users; //array
            var halfUsers = tempRole.halfUsers;
            var userFromFinalUsers = [];
            for (var userIndex in tmpUsers) {
                if (isInvited) {
                    userFromFinalUsers = getUserFromFinalUsers(tmpUsers[userIndex], isInvited);
                } else {
                    userFromFinalUsers = getUserFromFinalUsers(tmpUsers[userIndex], isInvited);
                }
                if (userFromFinalUsers === null) {
                    var user = {};
                    var roles = [];
                    user.name = tmpUsers[userIndex];
                    roles.push(tmpRoleName);
                    user.roles = roles;
                    if (isInvited) {
                        finalInvitations.push(user);
                    } else {
                        finalUsers.push(user);
                    }
                } else {
                    userFromFinalUsers.roles.push(tmpRoleName);
                }
            }
            for (var halfUserIndex in halfUsers) {
                if (isInvited) {
                    userFromFinalUsers = getUserFromFinalUsers(halfUsers[halfUserIndex], isInvited);
                } else {
                    userFromFinalUsers = getUserFromFinalUsers(halfUsers[halfUserIndex], isInvited);
                }
                if (userFromFinalUsers === null) {
                    var user = {};
                    var roles = [];
                    user.name = halfUsers[halfUserIndex];
                    roles.push(tmpRoleName);
                    user.roles = roles;
                    if (isInvited) {
                        finalInvitations.push(user);
                    } else {
                        finalUsers.push(user);
                    }
                } else {
                    userFromFinalUsers.roles.push(tmpRoleName);
                }
            }
        }
    }

    function detectRoleChanges() {
        var isInvited = false;
        for (u in finalUsers) {
            doDetectChangesNew(finalUsers[u].name, isInvited);
        }
    }

    function detectRoleChangesForInvitations() {
        var isInvited = true;
        for (index in finalInvitations) {
            doDetectChangesNew(finalInvitations[index].name, isInvited);
        }
    }

    function getUserFromFinalUsers(name, isInvited) {
        var finalUserList = finalUsers;
        if (isInvited) {
            finalUserList = finalInvitations;
        }
        for (var index in finalUserList) {
            if (finalUserList[index].name === name) {
                return finalUserList[index];
                break;
            }
        }
        return null;
    }

    function getUserFromUserList(name) {
        for (var index in usersWithRoles) {
            if (usersWithRoles[index].userName === name) {
                return usersWithRoles[index];
                break;
            }
        }
        return null;
    }

    function getInvitationFromInvitationList(email) {
        for (var index in pendingUsersWithRoles) {
            if (pendingUsersWithRoles[index].email === email) {
                return pendingUsersWithRoles[index];
                break;
            }
        }
        return null;
    }

    function doDeleteUser(checkedUsers) {
        var l = checkedUsers.length;
        for (var i in checkedUsers) {
            deleteUser(checkedUsers[i], i, l);
        }
    }

    function doRevokeInvitation(checkedInvitations) {
        var checkedInvitationsCount = checkedInvitations.length;
        for (var i in checkedInvitations) {
            revokeInvitation(checkedInvitations[i], i, checkedInvitationsCount);
        }
    }

    function doDetectChangesNew(username, isInvited) {
        var startingRoles, user;
        var tmpUser = getUserFromFinalUsers(username, isInvited);
        var endingRoles = tmpUser.roles || null;
        if (isInvited) {
            user = getInvitationFromInvitationList(username);
            startingRoles = user.roles;
        } else {
            user = getUserFromUserList(username);
            startingRoles = user.roles.split('\n');
        }
        var tempRolesToAdd = [];
        var tempRolesToDelete = [];

        if (endingRoles != null) {
            for (var i in startingRoles) {
                if (endingRoles.indexOf(startingRoles[i]) === -1) {
                    tempRolesToDelete.push(startingRoles[i]);
                }
            }
            for (var j in endingRoles) {
                if (startingRoles.indexOf(endingRoles[j]) === -1) {
                    tempRolesToAdd.push(endingRoles[j]);
                }
            }
        } else {
            tempRolesToDelete = startingRoles;
        }
        tmpUser.rolesToDelete = tempRolesToDelete;
        tmpUser.rolesToAdd = tempRolesToAdd;
    }

    function updateUsers() {
        for (var u in finalUsers) {
            var user = finalUsers[u];
            doUpdateUser(user.name, user.rolesToDelete, user.rolesToAdd, u);
        }
    }

    function updateInvitations() {
        for (var index in finalInvitations) {
            var invitation = finalInvitations[index];
            doUpdateInvitation(invitation.name, invitation.rolesToDelete, invitation.rolesToAdd, index);
        }
    }

    function doUpdateUser(username, rolesToDel, rolestoAd, count) {
        jagg.post("../blocks/tenant/users/add/ajax/add.jag", {
                action: "updateUserRoles",
                userName: username,
                rolesToDelete: rolesToDel.toString(),
                rolesToAdd: rolestoAd.toString()
            },
            function (result) {
                if (result != undefined || result == true) {
                    var x = (finalUsers.length - 1)
                    if (count == x) {
                        window.setTimeout(function () {
                            updateTable();

                        }, 300);
                    }
                    toggleEditDeleteButtons(false);
                    $('#select_all_check').prop('checked', false);
                    return result;
                } else if (result == false) {

                    return result;
                }
            },
            function (jqXHR, textStatus, errorThrown) {
                $('#userListContainer').html('<li class="noData-message">Error Occurred while updating user list. Please' +
                    ' contact WSO2 Cloud team for help</li>');
            });
    }

    function doUpdateInvitation(email, rolesToDelete, rolesToAdd, count) {
        var isInvited = true;
        //Check if final roles list is empty
        var tmpInvitation = getUserFromFinalUsers(email, isInvited);
        var finalRoles = tmpInvitation.roles || null;
        if (finalRoles == null || (finalRoles.length === 0)) {
            jagg.message({
                type: 'success',
                content: 'Please select at least one role for ' + email + ' in order to update...',
                id: "updateMsg" + count,
                type: 'error',
                cbk: function () {
                    updateInvitationTable(false);
                    jagg.removeMessage("updateMsg" + count);
                }
            });
            return;
        }
        jagg.message({
            type: 'success',
            content: 'Please wait while member invitation for ' + email + ' is being updated...',
            id: "updateMsg" + count
        });
        jagg.post("../blocks/tenant/users/add/ajax/add.jag", {
                action: "updateUserInvitation",
                email: email,
                rolesToDelete: rolesToDelete.toString(),
                rolesToAdd: rolesToAdd.toString()
            },
            function (result) {
                if (result != undefined || result == true) {
                    var x = (finalInvitations.length - 1);
                    jagg.removeMessage("updateMsg" + count);
                    if (count == x) {
                        window.setTimeout(function () {
                            var isBasicPageLoad = false;
                            updateInvitationTable(isBasicPageLoad);
                        }, 300);
                    }
                    enableActionButtonsForInvitations(false);
                    $('#select_all_invitations_check').prop('checked', false);
                    return result;

                } else if (result == false) {
                    jagg.removeMessage("updateMsg" + count);
                    return result;
                }
            },
            function (jqXHR, textStatus, errorThrown) {
                jagg.removeMessage("updateMsg" + count);
            });
    }
    $('#search_members').keyup(function (event) {
        doSearch($(this).val());
    });

    function doSearch(searchtext) {
        var searchUsers = [];
        for (var index in usersWithRoles) {
            var userRoles = usersWithRoles[index];
            var userEmail = (userRoles.email).toLowerCase();
            var userName = (userRoles.displayName).toLowerCase();
            var pattern = new RegExp(searchtext.toLowerCase());
            if ((pattern.test(userEmail)) || (pattern.test(userName))) {
                searchUsers.push(userRoles);
            }
        }
        createTable(searchUsers);
    }

    /*delete a given user*/
    function deleteUser(username, count, length) {
        jagg.post("../blocks/tenant/users/add/ajax/add.jag", {
                action: "deleteUserFromTenant",
                userName: username
            },
            function (result) {
                if (count == (length - 1)) {
                    window.setTimeout(function () {
                        pageNumberForMembers = 1;
                        updateTable();
                    }, 300);
                }
            },
            function (jqXHR, textStatus, errorThpageNumberForMembersrown) {
                $('#userListContainer').html('<li class="noData-message">Error Occurred while updating user list. Please' +
                    ' contact WSO2 Cloud team for help</li>');
            });
    }

    /*Revoke a given invitation*/
    function revokeInvitation(email, count, length) {
        jagg.post("../blocks/tenant/users/add/ajax/add.jag", {
                action: "revokeInvitation",
                email: email
            },
            function (response) {
                var responseObj = JSON.parse(response);
                if (responseObj.error == false) {
                    if (count == (length - 1)) {
                        window.setTimeout(function () {
                            pageNumberForInvitations = 1;
                            updateInvitationTable(false);
                        }, 300);
                    }
                }
            },
            function (jqXHR, textStatus, errorThrown) {
                $('#pendingUserListContainer').html('<li class="noData-message">Error Occurred while updating invitation' +
                    ' list. Please contact WSO2 Cloud team for help</li>');
            });
    }
});