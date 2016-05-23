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
 * Select2 Traditional Chinese translation
 */
(function ($) {
    "use strict";
    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () { return "沒有找到相符的項目"; },
        formatInputTooShort: function (input, min) { var n = min - input.length; return "請再輸入" + n + "個字元";},
        formatInputTooLong: function (input, max) { var n = input.length - max; return "請刪掉" + n + "個字元";},
        formatSelectionTooBig: function (limit) { return "你只能選擇最多" + limit + "項"; },
        formatLoadMore: function (pageNumber) { return "載入中..."; },
        formatSearching: function () { return "搜尋中..."; }
    });
})(jQuery);
