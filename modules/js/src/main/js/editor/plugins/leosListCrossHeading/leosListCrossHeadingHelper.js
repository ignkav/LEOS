/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
; // jshint ignore:line
define(function leosListCrossHeadingHelper(require) {
    "use strict";

    let DATA_AKN_LIST_TYPE = "data-list-type";
    let DATA_AKN_LIST_VALUE = "data-list-value";

    function _getCurrentConfigFromAttributes(editor, element) {
        var currentConfig = {};
        if (!element || element === undefined) {
            return currentConfig;
        }
        if (element.hasAttribute(DATA_AKN_LIST_TYPE)) {
            currentConfig.rootEltBulletType = element.getAttribute(DATA_AKN_LIST_TYPE);
        } else {
            currentConfig.rootEltBulletType = 'none';
        }
        if (element.hasAttribute(DATA_AKN_LIST_TYPE)) {
            currentConfig.rootEltBulletValue = element.getAttribute(DATA_AKN_LIST_VALUE);
        } else {
            currentConfig.rootEltBulletValue = '';
        }
        return currentConfig;
    }

    function _updateRootEltAttributes(editor, currentConfig, element) {
        let id = element.getAttribute("data-akn-id");
        var rootElt = editor.element.find("p[data-akn-id='" + id + "']").$[0];
        if (currentConfig.rootEltBulletType == "none") {
            rootElt.removeAttribute(DATA_AKN_LIST_TYPE);
            rootElt.removeAttribute(DATA_AKN_LIST_VALUE);
        } else {
            rootElt.setAttribute(DATA_AKN_LIST_TYPE, currentConfig.rootEltBulletType);
            rootElt.setAttribute(DATA_AKN_LIST_VALUE, currentConfig.rootEltBulletValue);
        }
    }

    return {
        getCurrentConfigFromAttributes: _getCurrentConfigFromAttributes,
        updateRootEltAttributes : _updateRootEltAttributes
    };
});