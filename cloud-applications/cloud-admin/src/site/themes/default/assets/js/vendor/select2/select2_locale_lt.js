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
 * Select2 lithuanian translation.
 * 
 * Author: CRONUS Karmalakas <cronus dot karmalakas at gmail dot com>
 */
(function ($) {
    "use strict";

    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () { return "Atitikmenų nerasta"; },
        formatInputTooShort: function (input, min) {
        	var n = min - input.length,
        	    suffix = (n % 10 == 1) && (n % 100 != 11) ? 'į' : (((n % 10 >= 2) && ((n % 100 < 10) || (n % 100 >= 20))) ? 'ius' : 'ių');
        	return "Įrašykite dar " + n + " simbol" + suffix;
        },
        formatInputTooLong: function (input, max) {
        	var n = input.length - max,
        	    suffix = (n % 10 == 1) && (n % 100 != 11) ? 'į' : (((n % 10 >= 2) && ((n % 100 < 10) || (n % 100 >= 20))) ? 'ius' : 'ių');
        	return "Pašalinkite " + n + " simbol" + suffix;
        },
        formatSelectionTooBig: function (limit) {
        	var n = limit,
                suffix = (n % 10 == 1) && (n % 100 != 11) ? 'ą' : (((n % 10 >= 2) && ((n % 100 < 10) || (n % 100 >= 20))) ? 'us' : 'ų');
        	return "Jūs galite pasirinkti tik " + limit + " element" + suffix;
        },
        formatLoadMore: function (pageNumber) { return "Kraunama daugiau rezultatų..."; },
        formatSearching: function () { return "Ieškoma..."; }
    });
})(jQuery);
