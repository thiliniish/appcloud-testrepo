/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * jQuery plugin to detect clicks outside an element
 * And popover close by given link
 * 
 * developed by Nuwan Sameera (nuwansh)
 * Licensed under the MIT (MIT-LICENSE.txt)  licenses.
 * 
 */

(function($){
  $.fn.outsideClick = function(options){
    var opts = options

    return this.each(function(element){

      var self = this
      var $self = $(this)

      var close_button = $self.find('.cancel')

      $(document).on("mousedown.popover", function(e){
        elemIsParent = $.contains(self, e.target);

        if(e.target == self || e.target == opts.outerEvent.target || elemIsParent){ 
          return
        }else{
          opts.clickHandler.toggleClass("active")
          $self.hide();
        }

        // Remove bind 
        $(document).off('mousedown.popover');
        close_button.off()
      })
      
      // Click close button
      close_button.on('click.closePopover', function(e){
        opts.clickHandler.toggleClass("active")
        $self.hide()

        // remove Both callbacks
        $(this).off(); $(document).off("mousedown.popover")

        e.preventDefault()
      })

   })

  }
})(jQuery);
