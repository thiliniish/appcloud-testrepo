var jagg = jagg || {};
var messageTimer;
(function () {
    jagg.post = function(url, data, callback, error) {
        return jQuery.ajax({
                               type:"POST",
                               url:url,
                               data:data,
                               async:true,
                               cache:false,
                               success:callback,                           
                               error:error
        });
    };

    jagg.syncPost = function(url, data, callback, type) {
        return jQuery.ajax({
                               type: "POST",
                               url: url,
                               data: data,
                               async:false,
                               success: callback,
                               dataType:"json"
        });
    };

    jagg.sessionExpired = function (){
        var sessionExpired = false;
        jagg.syncPost("/site/blocks/user/login/ajax/sessionCheck.jag", { action:"sessionCheck" },
                 function (result) {
                     if(result!=null){
                         if (result.message == "timeout") {
                             sessionExpired = true;
                         }
                     }
                 }, "json");
        return sessionExpired;
    };

    jagg.sessionAwareJS = function(params){


        if(jagg.sessionExpired()){
            if(params.e != undefined){  //Canceling the href call
                if ( params.e.preventDefault ) {
                    params.e.preventDefault();

                // otherwise set the returnValue property of the original event to false (IE)
                } else {
                    params.e.returnValue = false;
                }
            }

            jagg.showLogin(params);
        }else if(params.callback != undefined && typeof params.callback == "function"){
             params.callback();
         }
    };


    /*
    usage
    Show info dialog
    jagg.message({type:'success',content:'Your Message'});

    Show warning
    jagg.message({content:'foo',type:'warning' });

    Show error dialog
    jagg.message({content:'foo',type:'error' });

    Any message type can have callback with a button
    cbk         = callback method
    cbkBtnText  = text to display with the button
    jagg.message({content:'foo',type:'error' });

    Showing custom message with custom content and custom call backs

     var $domContent = $('<div><button class="customButton">Custom Button</button></div>');
     $('.customButton',$domContent).click(function(){alert('do something here..')});
         jagg.message({
         type:'custom',
         content:$domContent
     });


     Hiding all messages
     jagg.removeMessage();

    Targeting a specific message
     jagg.message({content:'foo',type:'error',id:'uniqueId' });

     jagg.removeMessage('uniqueId');


    */


    jagg.message = function(params){

        if(params.type == "custom"){
            params.type = 'success';
            jagg.add_message(params.type, params.content);
            return;
        }

        var contentObj;
        if(params.cbk && typeof params.cbk == "function"){
            if(params.cbkBtnText == undefined){ params.cbkBtnText = "OK";}
            params.content = '<p>'+params.content+'</p><div><button class="btn main" type="button">'+params.cbkBtnText+'</button></div>';
        }
        contentObj = $('<p class="message-content-area">'+params.content+'</p>');
        if(params.cbk && typeof params.cbk == "function"){
            $('.btn',contentObj).click(params.cbk);
        }

        jagg.add_message(params.type, contentObj, params.id);
    };
    jagg.add_message = function(message_type, message_html,id) {
        var $appendTo;
        if(id != undefined){
            if($('#'+id).length > 0 ){
                $appendTo = $('.message-content-area','#'+id).parent();
            }
        }

        if($appendTo == undefined){
            if(typeof message_html == 'object'){
                var $message = $('<div class="container"><div class="row"><div class="col-lg-12"><div class="message '+message_type+'_message" style="display:none"><div class="content"><div class="left message-container"></div><a class="close_message right icon-remove-sign js_close_message"><i class="fa fa-times-circle"></i></a></div></div></div></div></div>');
                $('.message-container',$message).append(message_html);
                $(".message_box").append($message);
            }else{
                var $message = $('<div class="container"><div class="row"><div class="col-lg-12"><div class="message '+message_type+'_message" style="display:none"><div class="content"><div class="left message-container">'+message_html+'</div><a class="close_message right icon-remove-sign js_close_message"><i class="fa fa-times-circle"></i></a></div></div></div></div></div>');
                $(".message_box").append($message);
            }
            var $messageObj = $(".message_box .message:last-child");
            $messageObj.attr('id',id);
            $messageObj.slideDown(500);
        } else{
            if(typeof message_html == 'object'){
                $appendTo.append(message_html);
            }else{
                $appendTo.append($(message_html));
            }
        }
    };


    jagg.removeMessage = function(id){
        if(id!= undefined){
			$(".message_box #"+id).remove();
        }else{
			$(".message_box .message").remove();
        }
    };
    //jagg.popMessage({type:'confirm',title:'Model Title',content:'Model Content',okCallback:function(){alert('do this when ok')},cancelCallback:function(){alert('do this when cancel')}});

    //jagg.popMessage({content:'Message'});

    jagg.popMessage = function(params){
        $('.qtip').remove();
        var $modelObject = $('<div class="modal_content">'+
            '<div class="message_body error_details">'+
                '<table style="width:400px;">' +
                '<tr><td class="img" rowspan="2">' +
                    '<span class="icon-stack" style="margin-right: 10px;">'+
                    '<span class="fa-stack fa-lg">' +
                    '<i class="fa fa-circle fa-stack-1x fa-4x" style="font-size: 50px;color: rgba(208,0,0,0.63);"></i>' +
                    '<i class="fa fa-exclamation fa-stack-2x fa-2x" style="font-size:40px;color: #fff;margin-top:-2px;margin-left:5px;"></i>' +
                    '</span> '+
                    '</span>'+
                '</td><td class="modal_msg"></td></tr>' +
                '<tr><td class="" id="contentRowContainer" style="padding:10px 0"></td></tr>'+
                '</table> '+
            '</div>'+
            '<div class="message_action" id="buttonRowContainer">'+
            '</div>'+
            '</div>');
        var $buttonRow = '';
        var $contentRow = '';
        if(params.type == "verify"){
            //show ok and cancel buttons and register callbacks for each.
            $buttonRow = $('<ul class="inline_list">'+
                                '<li class="inline_item"><a href="#" class="btn sub big modal_cancel" >No</a></li>'+
                                '<li class="inline_item"><a href="#" class="btn main big modal_action">Yes</a></li>'+
                            '</ul>');

            $('.modal_action',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            $('.modal_cancel',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            if(typeof params.yesCallback == 'function'){
                $('.modal_action',$buttonRow).click(params.yesCallback);
            }
            if(typeof params.noCallback == 'function'){
                $('.modal_cancel',$buttonRow).click(params.noCallback);
            }
        }
        if(params.type == "confirm"){
            //show ok and cancel buttons and register callbacks for each.
            $buttonRow = $('<ul class="inline_list">'+
                                '<li class="inline_item"><a href="#" class="btn sub big modal_cancel" >Cancel</a></li>'+
                                '<li class="inline_item"><a href="#" class="btn main big modal_action">OK</a></li>'+
                '</ul>');

            $('.modal_action',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            $('.modal_cancel',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            if(typeof params.okCallback == 'function'){
                $('.modal_action',$buttonRow).click(params.okCallback);
            }
            if(typeof params.cancelCallback == 'function'){
                $('.modal_cancel',$buttonRow).click(params.cancelCallback);
            }
        }

        if(params.type == "ok" || params.type == "error"){
            //show ok and cancel buttons and register callbacks for each.
            $buttonRow = $('<ul class="inline_list">'+
                '<li class="inline_item"><a href="#" class="btn main big modal_action">OK</a></li>'+
                '</ul>');

            $('.modal_action',$buttonRow).click(function(){
                $('#qtip').qtip("hide");
            });
            if(typeof params.okCallback == 'function'){
                $('.modal_action',$buttonRow).click(params.okCallback);
            }

            $('.modal_msg',$modelObject ).remove();
            $('.img',$modelObject ).remove();
        }

        if( (typeof $buttonRow) == "object"){
            $('#buttonRowContainer',$modelObject).append($buttonRow);
        }else{
            $('#buttonRowContainer',$modelObject).html($buttonRow);
        }

        if( (typeof params.content == "object")){
            $('#contentRowContainer',$modelObject).append(params.content);
        }else{
            $('#contentRowContainer',$modelObject).html(params.content);
        }



        //text(params.btnText);
        $('.modal_msg',$modelObject).html(params.title);

        $('#qtip').qtip(
            {
                content: {
                    text: function(api){
                        return $modelObject;
                    },
                    prerender: true
                },

                position: {
                    my: 'center',
                    at: 'center',
                    target: $(window)
                },
                show: {
                    ready: true,
                    solo: true,
                    modal: {
                        blur: true,
                        escape: true
                    },
                    when: false
                },

                hide: true,
                style: {
                    classes: 'modal_box',
                    widget: false,
                    def: false
                }
            });

    };
	
	

		var e = jQuery.Event("keyup"); // or keypress/keydown
		e.keyCode = 27; // for Esc
		$(document).trigger(e); // trigger it on document
	
	
	$(document).keyup(function(e) {
		if (e.keyCode == 27) { // Esc
			jagg.removeMessage();
		}
	});

}());
