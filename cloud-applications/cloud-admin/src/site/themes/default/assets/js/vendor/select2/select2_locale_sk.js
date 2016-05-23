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
 * Select2 Slovak translation.
 *
 * Author: David Vallner <david@vallner.net>
 */
(function ($) {
    "use strict";
    // use text for the numbers 2 through 4
    var smallNumbers = {
        2: function(masc) { return (masc ? "dva" : "dve"); },
        3: function() { return "tri"; },
        4: function() { return "štyri"; }
    }
    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () { return "Nenašli sa žiadne položky"; },
        formatInputTooShort: function (input, min) {
            var n = min - input.length;
            if (n == 1) {
                return "Prosím zadajte ešte jeden znak";
            } else if (n <= 4) {
                return "Prosím zadajte ešte ďalšie "+smallNumbers[n](true)+" znaky";
            } else {
                return "Prosím zadajte ešte ďalších "+n+" znakov";
            }
        },
        formatInputTooLong: function (input, max) {
            var n = input.length - max;
            if (n == 1) {
                return "Prosím zadajte o jeden znak menej";
            } else if (n <= 4) {
                return "Prosím zadajte o "+smallNumbers[n](true)+" znaky menej";
            } else {
                return "Prosím zadajte o "+n+" znakov menej";
            }
        },
        formatSelectionTooBig: function (limit) {
            if (limit == 1) {
                return "Môžete zvoliť len jednu položku";
            } else if (limit <= 4) {
                return "Môžete zvoliť najviac "+smallNumbers[limit](false)+" položky";
            } else {
                return "Môžete zvoliť najviac "+limit+" položiek";
            }
        },
        formatLoadMore: function (pageNumber) { return "Načítavajú sa ďalšie výsledky..."; },
        formatSearching: function () { return "Vyhľadávanie..."; }
    });
})(jQuery);
