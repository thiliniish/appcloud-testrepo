var events = [];
var page;

var EVENT_PUBLISH_PERIOD = 120000;

function initializeUserActivity(currentPage) {
	page = currentPage;
	addUserActivity(page, "load");
}

function addUserActivity(item, action) {
	var event={};
	event.item = item;
	event.action = action;
	event.timestamp = $.now();
    event.pageName = page;
    events[events.length] = event;
}

function publishEvents(pageUnload) {
    if(pageUnload) {
    	addUserActivity(page, "page-unload");
    } else {
    	addUserActivity(page, "same-page");
    }

    var copied = events;
    events = [];

    var syncPostUrl = "";
    var pageUrl = document.URL;
    var lastChar = pageUrl.charAt(pageUrl.length - 1);

    if(lastChar === "/"){
        syncPostUrl = "userActivity";
    }
    else{
        syncPostUrl = "../../userActivity";
    }

    if(page != "SignUp Page"){
        jagg.syncPost(syncPostUrl, {
                            action:"userActivity",
                            events:JSON.stringify(copied)
                    }, function (result) {
                    }, function (jqXHR, textStatus, errorThrown) {
                    });
    }
    else{
        if($("#companyName").attr('value') != "" && $("#companyName").attr('value') != null){
            jagg.syncPost(syncPostUrl, {
                            action:"userActivitySignUp",
                            events:JSON.stringify(copied),
                            tenantDomain:$("#companyName").attr('value')
                     }, function (result) {
                     }, function (jqXHR, textStatus, errorThrown) {
                     });
        }
    }

    if (!pageUnload) {
        setTimeout(function() {
                publishEvents(false);
        } , EVENT_PUBLISH_PERIOD);
    }
    return;
}

var addClickEvents = function(e){
      var target = $(e.target);
      if (target.is("input") || target.is("select") || target.is("textarea") || target.is("a") || target.is("i")
            || target.is("span")) {
    	  var item = target.attr("name") ? target.attr("name") : target.attr("id");
    	  if (!item) {
    		  item = 'noname';
    	  }
   	      addUserActivity(item, "click");
      }
 }


$(document).ready(function($){

     $(document).click(addClickEvents);
  	 setTimeout(function() {
         publishEvents(false);
	       } , EVENT_PUBLISH_PERIOD);
});


$(window).bind('beforeunload', function() {
	publishEvents(true);
});



