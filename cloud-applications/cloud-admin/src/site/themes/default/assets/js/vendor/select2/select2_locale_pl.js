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
 * Select2 Polish translation.
 * 
 * Author: Jan Kondratowicz <jan@kondratowicz.pl>
 */
(function ($) {
    "use strict";
    
    var pl_suffix = function(n) {
        if(n == 1) return "";
        if((n%100 > 1 && n%100 < 5) || (n%100 > 20 && n%10 > 1 && n%10 < 5)) return "i";
        return "ów";
    };

    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () {
            return "Brak wyników.";
        },
        formatInputTooShort: function (input, min) {
            var n = min - input.length;
            return "Wpisz jeszcze " + n + " znak" + pl_suffix(n) + ".";
        },
        formatInputTooLong: function (input, max) {
            var n = input.length - max;
            return "Wpisana fraza jest za długa o " + n + " znak" + pl_suffix(n) + ".";
        },
        formatSelectionTooBig: function (limit) {
            return "Możesz zaznaczyć najwyżej " + limit + " element" + pl_suffix(limit) + ".";
        },
        formatLoadMore: function (pageNumber) {
            return "Ładowanie wyników...";
        },
        formatSearching: function () {
            return "Szukanie...";
        }
    });
})(jQuery);