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
 * Select2 Basque translation.
 *
 * Author: Julen Ruiz Aizpuru <julenx at gmail dot com>
 */
(function ($) {
    "use strict";

    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () {
          return "Ez da bat datorrenik aurkitu";
        },
        formatInputTooShort: function (input, min) {
          var n = min - input.length;
          if (n === 1) {
            return "Idatzi karaktere bat gehiago";
          } else {
            return "Idatzi " + n + " karaktere gehiago";
          }
        },
        formatInputTooLong: function (input, max) {
          var n = input.length - max;
          if (n === 1) {
            return "Idatzi karaktere bat gutxiago";
          } else {
            return "Idatzi " + n + " karaktere gutxiago";
          }
        },
        formatSelectionTooBig: function (limit) {
          if (limit === 1 ) {
            return "Elementu bakarra hauta dezakezu";
          } else {
            return limit + " elementu hauta ditzakezu soilik";
          }
        },
        formatLoadMore: function (pageNumber) {
          return "Emaitza gehiago kargatzen...";
        },
        formatSearching: function () {
          return "Bilatzen...";
        }
    });
})(jQuery);
