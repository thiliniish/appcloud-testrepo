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
 * Select2 Russian translation
 */
(function ($) {
    "use strict";

    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () { return "Совпадений не найдено"; },
        formatInputTooShort: function (input, min) { var n = min - input.length; return "Пожалуйста, введите еще " + n + " символ" + (n == 1 ? "" : ((n > 1)&&(n < 5) ? "а" : "ов")); },
        formatInputTooLong: function (input, max) { var n = input.length - max; return "Пожалуйста, введите на " + n + " символ" + (n == 1 ? "" : ((n > 1)&&(n < 5)? "а" : "ов")) + " меньше"; },
        formatSelectionTooBig: function (limit) { return "Вы можете выбрать не более " + limit + " элемент" + (limit == 1 ? "а" : "ов"); },
        formatLoadMore: function (pageNumber) { return "Загрузка данных..."; },
        formatSearching: function () { return "Поиск..."; }
    });
})(jQuery);
