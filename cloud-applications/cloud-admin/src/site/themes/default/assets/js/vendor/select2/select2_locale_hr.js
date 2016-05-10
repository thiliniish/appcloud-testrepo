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
 * Select2 Croatian translation.
 *
 * Author: Edi Modrić <edi.modric@gmail.com>
 */
(function ($) {
    "use strict";

    var specialNumbers = {
        1: function(n) { return (n % 100 != 11 ? "znak" : "znakova"); },
        2: function(n) { return (n % 100 != 12 ? "znaka" : "znakova"); },
        3: function(n) { return (n % 100 != 13 ? "znaka" : "znakova"); },
        4: function(n) { return (n % 100 != 14 ? "znaka" : "znakova"); }
    };

    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () { return "Nema rezultata"; },
        formatInputTooShort: function (input, min) {
            var n = min - input.length;
            var nMod10 = n % 10;

            if (nMod10 > 0 && nMod10 < 5) {
                return "Unesite još " + n + " " + specialNumbers[nMod10](n);
            }

            return "Unesite još " + n + " znakova";
        },
        formatInputTooLong: function (input, max) {
            var n = input.length - max;
            var nMod10 = n % 10;

            if (nMod10 > 0 && nMod10 < 5) {
                return "Unesite " + n + " " + specialNumbers[nMod10](n) + " manje";
            }

            return "Unesite " + n + " znakova manje";
        },
        formatSelectionTooBig: function (limit) { return "Maksimalan broj odabranih stavki je " + limit; },
        formatLoadMore: function (pageNumber) { return "Učitavanje rezultata..."; },
        formatSearching: function () { return "Pretraga..."; }
    });
})(jQuery);
