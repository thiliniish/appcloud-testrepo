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
 * Select2 Icelandic translation.
 * 
 */
(function ($) {
    "use strict";

    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () { return "Ekkert fannst"; },
        formatInputTooShort: function (input, min) { var n = min - input.length; return "Vinsamlegast skrifið " + n + " staf" + (n == 1 ? "" : "i") + " í viðbót"; },
        formatInputTooLong: function (input, max) { var n = input.length - max; return "Vinsamlegast styttið texta um " + n + " staf" + (n == 1 ? "" : "i"); },
        formatSelectionTooBig: function (limit) { return "Þú getur aðeins valið " + limit + " atriði"; },
        formatLoadMore: function (pageNumber) { return "Sæki fleiri niðurstöður..."; }, 
        formatSearching: function () { return "Leita..."; }
    });
})(jQuery);
