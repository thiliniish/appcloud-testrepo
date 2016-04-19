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
 * Select2 Galician translation
 * 
 * Author: Leandro Regueiro <leandro.regueiro@gmail.com>
 */
(function ($) {
    "use strict";

    $.extend($.fn.select2.defaults, {
        formatNoMatches: function () {
            return "Non se atoparon resultados";
        },
        formatInputTooShort: function (input, min) {
            var n = min - input.length;
            if (n === 1) {
                return "Engada un carácter";
            } else {
                return "Engada " + n + " caracteres";
            }
        },
        formatInputTooLong: function (input, max) {
            var n = input.length - max;
            if (n === 1) {
                return "Elimine un carácter";
            } else {
                return "Elimine " + n + " caracteres";
            }
        },
        formatSelectionTooBig: function (limit) {
            if (limit === 1 ) {
                return "Só pode seleccionar un elemento";
            } else {
                return "Só pode seleccionar " + limit + " elementos";
            }
        },
        formatLoadMore: function (pageNumber) {
            return "Cargando máis resultados...";
        },
        formatSearching: function () {
            return "Buscando...";
        }
    });
})(jQuery);
