var login = login || {};
(function () {
    var loginbox = login.loginbox || (login.loginbox = {});

    loginbox.login = function (username, password, url, tenant) {
        username = username + "@" + tenant;

        jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"login", username:username, password:password},
                 function (result) {
                     if (result.error == false) {
                         if (redirectToHTTPS && redirectToHTTPS != "" && redirectToHTTPS != "{}" &&redirectToHTTPS != "null") {
                             window.location.href = redirectToHTTPS;
                         } else if(url){
                             window.location.href = url;
                         }else{
                             var redirectURL = siteContext;
                             if(!isCustomURL) {
                                 redirectURL = redirectURL + "?" + urlPrefix;
                             }
                             window.location.href = redirectURL;
                         }
                     } else {
                         $('#loginErrorMsg').show();
                         $('#password').val('');
                         //$('#loginErrorMsg').html(result.message).prepend('<strong>'+i18n.t("errorMsgs.login")+'</strong><br />');
                         $('#loginErrorMsg').html('<i class="icon fw fw-error"></i><strong> '+i18n.t("Error!")+' </strong>' + result.message + '<button type="button" class="close" aria-label="close" data-dismiss="alert"><span aria-hidden="true"><i class="fw fw-cancel"></i></span></button>');
                     }
                 }, "json");
    };

    loginbox.logout = function () {
        jagg.post("/site/blocks/user/login/ajax/login.jag", {action:"logout"}, function (result) {
            if (result.error == false) {
            	  window.location.href= requestURL + "?" + urlPrefix;
            } else {
                jagg.message({content:result.message,type:"error"});
                window.location.reload();
            }
        }, "json");
    };



}());


$(document).ready(function () {
	
	$('#username').focus();
    $('#username').keydown(function(event) {
        if (event.which == 13) {
            event.preventDefault();
            login();
        }
    });
    $('#password').keydown(function(event) {
        if (event.which == 13) {
            event.preventDefault();
            login();
        }
    });
    
    var registerEventsForLogin = function(){
        $('#mainLoginForm input').off('keydown');
         $('#mainLoginForm input').keydown(function(event) {
         if (event.which == 13) {
                var goto_url =$.cookie("goto_url");
                event.preventDefault();
                login.loginbox.login($("#username").val(), $("#password").val(), goto_url,$("#tenant").val());

            }
        });

        //$('#loginBtn').off('click');
         $('#loginBtn').click(
            function() {
                var goto_url = $.cookie("goto_url");
                login.loginbox.login($("#username").val(), $("#password").val(), goto_url,$("#tenant").val());
            }
         );
    };
    var showLoginForm = function(event){
		    if(event != undefined){
	            event.preventDefault();
	        }
	        if(!isSecure){
	            $('#loginRedirectForm').submit();
	            return;
	        }
	
	        //$('#messageModal').html($('#login-data').html());
	        //$('#messageModal').modal('show');
	        if ($(this).attr("href")) {
	        	$.cookie("goto_url",$(this).attr("href"));
	        } else {
	        	if ($('#tenant').val() && $('#tenant').val() != "null") { 
	        		$.cookie("goto_url",siteContext + '?tenant=' + $('#tenant').val());
	        	} else {
	        		$.cookie("goto_url",siteContext);
	        	}
	        }
	        
	        $('#username').focus();
	
	         registerEventsForLogin();
	         
	         var loginUrl = siteContext + '/site/pages/login.jag';
	         
	         if (!isCustomURL && tenant && tenant != 'null') {
	        	 loginUrl = siteContext + '/site/pages/login.jag?tenant=' + tenant;
	         }
	         
	         window.location.href = loginUrl;	
    };
    
    login.loginbox.showLoginForm = showLoginForm;


    $("#goBackBtn").click(function () {
    	var loginUrl = siteContext;        
        if ($('#tenant').val() != null && $('#tenant').val() != "null") {
        	loginUrl = siteContext + '?tenant='+$('#tenant').val();
        }
    	window.location.href = loginUrl;
    });
    
    $("#logout-link").click(function () {
        if (userLoginType != 'basic' && ssoEnabled=='true' || oidcEnabled=='true') {
            location.href = requestURL + '/site/pages/logout.jag';
        } else {
            login.loginbox.logout();
        }
    });

    $(".need-login").click(showLoginForm);
    $('#login-link').click(showLoginForm);

    if(isSecure && showLogin==true){
        showLogin = false;
        showLoginForm();
    }

});

//Theme Selection Logic
function applyTheme(elm){
    $('#themeToApply').val($(elm).attr("data-theme"));
    $('#subthemeToApply').val($(elm).attr("data-subtheme"));
    $('#themeSelectForm').submit();
}

function getAPIPublisherURL(){
    jagg.post("/site/blocks/user/login/ajax/login.jag", { action:"getAPIPublisherURL"},
        function (result) {
            if (!result.error) {
                    location.href = result.url;

            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
}


function login() {
	var goto_url = $.cookie("goto_url");
    login.loginbox.login($("#username").val(), $("#password").val(), goto_url,$("#tenant").val());
}



