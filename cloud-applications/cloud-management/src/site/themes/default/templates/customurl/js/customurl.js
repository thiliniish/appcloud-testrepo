/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
$(document).ready(function() {
    $('#backToListingBtn').click(function() {
        $('#appCloudTab').attr("href", "#tableSection");
        $('#appCloudTab').attr("aria-controls", "tableSection");
        setCustomDomainDetailsForTenant();
        $('#cloudTabs a').tab('show');
    });
    $('#cloudTabs a').click(function(e) {
        e.preventDefault();
        if (cloudType == APP_CLOUD_TYPE || $(this).attr('href') == "#apiSection") {
            getCurrentUserMapping();
        } else if (cloudType == API_CLOUD_TYPE && $(this).attr('href') == "#tableSection") {
            setCustomDomainDetailsForTenant();
        }
        $(this).tab('show');
    });
    if (backUrl == "null") {
        $("#backToCloudBtn").css("display", "none");
    }
    $("#backToCloudBtn").click(function() {
        window.location.replace(backUrl);
    });
    defaultUIView();
    initializeUserActivity("Custom URL Page");
});

function defaultUIView() {
    if (cloudType == API_CLOUD_TYPE) {
        $("#subHeading").html("This serves as the base URL for all your public APIs");
        $('#appCloudTab').attr("href", "#tableSection");
        $('#appCloudTab').attr("aria-controls", "tableSection");
        $('#backToListingBtn').css("display", "block");
        getCurrentUserMapping();
    } else {
        $("#subHeading").html("This serves as the base URL for all your public applications");
        $('#apiCloudPresentation').removeClass('active');
        $('#apiSection').removeClass('active');
        $('#appCloudPresentation').addClass('active');
        if (cloudType == APP_CLOUD_TYPE) {
            $('#applicationSection').addClass('active');
            $('#appCloudTab').attr("href", "#applicationSection");
            $('#appCloudTab').attr("aria-controls", "applicationSection");
            $('#backToListingBtn').css("display", "none");
            setApplicationDetailsForTenant();
        } else {
            $('#tableSection').addClass('active');
            $('#appCloudTab').attr("href", "#tableSection");
            $('#appCloudTab').attr("aria-controls", "tableSection");
            $('#backToListingBtn').css("display", "block");
            setCustomDomainDetailsForTenant();
        }
    }
}

function apiCloudDefaultUIView() {
    //Store
    $("#storeModifyBtn").prop("disabled", false);
    $("#storeDomainVerifyBtn").prop("disabled", false);
    $("#storeDomain").prop("disabled", false);
    $("#storeProcessBtn").prop("disabled", true);
    $("#storeUrlChangeArea").css("display", "none");
    //Gateway
    $("#gatewayModifyBtn").prop("disabled", false);
    $("#gatewayDomainVerifyBtn").prop("disabled", false);
    $("#gatewayDomain").prop("disabled", false);
    $("#gatewayProcessBtn").prop("disabled", true);
    $("#gatewayUrlChangeArea").css("display", "none");
}

function appCloudDefaultUIView() {
    $("#applicationModifyBtn").prop("disabled", false);
    $("#applicationDomainVerifyBtn").prop("disabled", false);
    $("#applicationDomain").prop("disabled", false);
    $("#applicationProcessBtn").prop("disabled", true);
    $("#applicationUrlChangeArea").css("display", "none");
}

function showModifyUrlContentArea(node) {
    $("#" + node + "UrlChangeArea").css("display", "block");
    $("#" + node + "ModifyBtn").prop("disabled", true);
}

function enableSSLFileUpload(node) {
    $("#" + node + "SSLFileUploadLocation").css("display", "block");
    $("#" + node + "DomainVerifyBtn").prop("disabled", true);
    $("#" + node + "Domain").prop("disabled", true);
    $("#" + node + "VerifyUrlNotificationArea").html(
        "<span class='label label-success'>CNAME verified successfully</span>");
    $("#" + node + "ProcessBtn").prop("disabled", false);
}

function setCustomDomainDetailsForTenant() {
    jagg.post('../../site/blocks/customurl/ajax/customurl.jag', {
        action: 'getCustomDomainDetailsForTenant',
        cloudType: APP_CLOUD_TYPE
    }, function(result) {
        esult = $.trim(result);
        result = JSON.parse(result);
        if (result.length == 0) {
            $('#customDetailsTable').hide();
            $('#tableSection').html('<div class="alert alert-info">' +
                '<strong>You have not created any applications. Please visit App Cloud and create an application.' +
                '</strong></div>');
        } else {
            fillTable(result);
        }
    }, function(jqXHR, textStatus, errorThrown) {
        $('#customDetailsTable').hide();
        $('#tableSection').html('<div class="alert alert-danger">' +
            '<strong>Error occurred while connecting to App Cloud.</strong>' +
        '</div>');
    });
}

function setApplicationDetailsForTenant() {
    var currentMapping = customUrl;
    if (customUrl == "null") {
        currentMapping = defaultDomain;
    }
    $("#currentApplicationMapping").val(currentMapping);
    $("#appName").html(appName);
}

function displayApplicationSection(element) {
    var customDomainDetails = $(element).data("options");
    appName = customDomainDetails.appName;
    defaultDomain = customDomainDetails.defaultDomainURL;
    customUrl = customDomainDetails.customDomainURL;
    if (customUrl == "-") {
        customUrl = defaultDomain;
    }
    $("#currentApplicationMapping").val(customUrl);
    $("#appName").html(appName);
    $('#appCloudTab').attr("href", "#applicationSection");
    $('#appCloudTab').attr("aria-controls", "applicationSection");
    $('#cloudTabs a').tab('show');
    $("#applicationModifyBtn").prop("disabled", false);
    resetApplicationSection();
}

function resetApplicationSection() {
    $("#sslFileForm")[0].reset();
    $("#keyFileForm")[0].reset();
    $("#chainFileForm")[0].reset();
    var notificationAreaMessage = "<h4 class=\"italic light-grey transform-none\"> <b>IMPORTANT:</b> Before giving" +
        " the domain name here, go to your domain registrar's DNS panel and add a CNAME record pointing to <strong>" +
        appCloudPointingUrl + "</strong><span class=\"blue\"><a href=" + customUrlDocumentationLink +
        " target=\"_blank\"> Show me how >></a></span></h4>";
    $("#applicationVerifyUrlNotificationArea").html(notificationAreaMessage);
    $("#applicationDomain").val("");
    $("#applicationDomain").prop("disabled", false);
    $("#applicationDomainVerifyBtn").prop("disabled", false);
    $("#applicationUrlChangeArea").css("display", "none");
    $("#applicationSSLFileUploadLocation").css("display", "none");
    $("#applicationButtonBlock").css("display", "block");
}

function fillTable(customDomainDetails) {
    var tableBodyHTML = "";
    for (var i = 0; i < customDomainDetails.length; i++) {
        var customDomURL = "-";
        if (customDomainDetails[i].customDomainURL != null) {
            customDomURL = customDomainDetails[i].customDomainURL;
        }
        tableBodyHTML += "<tr>" +
            "<td style='word-wrap: break-word;'>" + customDomainDetails[i].appName + "</td>" +
            "<td style='word-wrap: break-word;'>" + customDomainDetails[i].defaultDomainURL +
            "</td>" +
            "<td style='word-wrap: break-word;'>" + customDomURL + "</td>" +
            "<td><a>" +
                "<span onclick='displayApplicationSection(this)' data-options='{\"appName\":\"" + customDomainDetails[i].appName + "\", \"defaultDomainURL\":\"" +
            customDomainDetails[i].defaultDomainURL + "\", \"customDomainURL\":\"" + customDomURL +"\"}' class='glyphicon glyphicon-edit'></span></a>" +
            "</td></tr>";
    }
    $('#tableBody').html(tableBodyHTML);
}

function publishCustomUrl(node) {
    if ($("#" + node + "SslFile")[0].files.length == 0 || $("#" + node + "KeyFile")[0].files.length == 0 ||
        $("#" + node + "ChainFile")[0].files.length == 0) {
        $("#" + node + "VerifyUrlNotificationArea").html(
            "<span class='label label-warning'>Please provide all required ssl files</span>");
        return false;
    }
    var cloudProfile = API_CLOUD_TYPE;
    var formData = new FormData();
    formData.append("customDomain", $("#" + node + "Domain").val());
    formData.append("sslFile", $("#" + node + "SslFile")[0].files[0]);
    formData.append("keyFile", $("#" + node + "KeyFile")[0].files[0]);
    formData.append("chainFile", $("#" + node + "ChainFile")[0].files[0]);
    if (node == 'application') {
        /*Set applications name as the node since certificates are saved as /customUrl/<tenantDomain>/<appName>/
         <tenantDomain>"-"<appName>".pem"*/
        node = appName;
        cloudProfile = APP_CLOUD_TYPE;
    }
    formData.append("action", 'publishVal');
    formData.append("node", node);
    formData.append("tenantDomain", "<%=tenantDomain%>");
    formData.append("cloudProfile", cloudProfile);
    $.ajax({
        url: '../../site/blocks/customurl/ajax/customurl.jag',
        type: "POST",
        data: formData,
        processData: false,
        contentType: false,
        async: false,
        success: function(response) {
            var result = JSON.parse(response);
            if (result.error != true) {
                var customUrl;
                if (node == appName) {
                    node = "application";
                    customUrl = $("#" + node + "Domain").val();
                }
                $("#" + node + "SSLFileUploadLocation").css("display", "none");
                $("#" + node + "ButtonBlock").css("display", "none");
                $("#" + node + "VerifyUrlNotificationArea").html("<span class='label label-success'>Custom URL mapping " +
                    "is successfully added.</span>" + "<br><br>" + "<span class='label label-warning'>Please note " +
                    "that it will take upto 10 minutes to update changes.</span>");
                if (node == "application") {
                    $("#currentApplicationMapping").val(customUrl);
                    setTimeout(function() {
                        appCloudDefaultUIView();
                        $("#applicationModifyBtn").prop("disabled", true);
                        resetApplicationSection();
                    }, 10000);
                } else {
                    getCurrentUserMapping();
                    setTimeout(function() {
                        apiCloudDefaultUIView();
                    }, 10000);
                }
            } else {
                if (node == appName) {
                    node = "application";
                }
                $("#" + node + "VerifyUrlNotificationArea").html("<span class='label label-danger'>" + result.message +
                    "</span><br>");
            }
        },
        error: function(jqXHR, textStatus, errorMessage) {
            if (node == appName) {
                node = "application";
            }
            $("#" + node + "VerifyUrlNotificationArea").html("<span class='label label-danger'>Internal Server " +
                "error occurred</span>");
        }
    });
}

function verifyCustomDomain(node) {
    var customUrl = $('#' + node + 'Domain').val();
    jagg.post('../../site/blocks/customurl/ajax/customurl.jag', {
        action: 'validateUrl',
        customDomain: customUrl,
        nodeType: node
    }, function(result) {
        result = $.trim(result);
        result = JSON.parse(result);
        if (result.error == "false") {
            enableSSLFileUpload(node);
        } else {
            $("#" + node + "VerifyUrlNotificationArea").html("<span class='label label-danger'>" + result
                    .message + "</span>");
        }
    }, function(jqXHR, textStatus, errorThrown) {
        $("#" + node + "VerifyUrlNotificationArea").html("<span class='label label-danger'>" + jqXHR.responseText
            + "</span>");
    });
}

function getCurrentUserMapping() {
    jagg.syncPost('../../site/blocks/customurl/ajax/customurl.jag', {
        action: 'getCurrentMapping',
        tenantDomain: "<%=tenantDomain%>",
        cloudType: 'api-cloud'
    }, function(result) {
        $("#currentStoreMapping").val(result.store.customUrl);
        $("#currentGatewayMapping").val(result.gateway.customUrl);
    }, function(jqXHR, textStatus, errorThrown) {});
}