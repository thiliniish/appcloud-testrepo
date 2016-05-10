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

var ie = (function(){
       var undef, v = 3, div = document.createElement('div');

       while (
           div.innerHTML = '<!--[if gt IE '+(++v)+']><i></i><![endif]-->',
           div.getElementsByTagName('i')[0]
       );

       return v> 4 ? v : undef;
   }());
if( ie <= 9){
    createPlaceholders();
}
function createPlaceholders() {
    var inputs = jQuery("input[type=text],input[type=email],input[type=tel],input[type=url]");
    inputs.each(
            function() {
                var _this = jQuery(this);
                this.placeholderVal = _this.attr("placeholder");
                _this.val(this.placeholderVal);
                if (this.placeholderVal != "") {
                    _this.addClass("placeholderClass");
                }
            }
            )
            .bind("focus", function() {
        var _this = jQuery(this);
        var val = jQuery.trim(_this.val());
        if (val == this.placeholderVal || val == "") {
            _this.val("");
            _this.removeClass("placeholderClass");
        }
    })
            .bind("blur", function() {
        var _this = jQuery(this);
        var val = jQuery.trim(_this.val());
        if (val == this.placeholderVal || val == "") {
            _this.val(this.placeholderVal);
            _this.addClass("placeholderClass");
        }

    });
}