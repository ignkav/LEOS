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
define(function leosUtilsModule(require) {
    "use strict";

    // load module dependencies
    require("dateFormat");
    var CKEDITOR = require("promise!ckEditor");
    var REGEX_ORIGIN = new RegExp("[leos:|data-](\\w-?)*origin");
    
    var COUNCIL_INSTANCE = "COUNCIL";
    // configuration

    var PARAGRAPH_POINT_TAG = "LI";
    var SUBPARAGRAPH_SUBPOINT_TAG = "P";
    var TABLE_TAG = "TABLE";
    var TABLE_CELL_TAG = "TD";
    var TABLE_CELL_HEADER_TAG = "TH";
    var LINE_BREAK_TAG = "BR";
    var BOLD_TEXT_TAG = "STRONG";
    var EMPHATIZED_TEXT_TAG = "EM";
    var SUB_TEXT_TAG = "SUB";
    var SUP_TEXT_TAG = "SUP";
    var HEADING_TAG = "H2";

    function _getParentElement(connector) {
        var element = null;
        if (connector) {
            var id = connector.getParentId();
            element = connector.getElement(id);
        }
        return element;
    }


    function _getElementOrigin(element) {
        var attrs = element instanceof CKEDITOR.dom.element ? element.$.attributes : element.attributes;
        for (var idx = 0; idx < attrs.length; idx++) {
            if (attrs[idx].name.match(REGEX_ORIGIN)) {
                return attrs[idx].value;
            }
        }
        return null;
    }

    function _registerEmptyTrimSelector(tocItemsList) {
        jQuery.extend(jQuery.expr[':'], {
            emptyTrim: function (el) {
                var elementsToBeChecked = [PARAGRAPH_POINT_TAG, SUBPARAGRAPH_SUBPOINT_TAG];
                var childElementsToBeChecked = [LINE_BREAK_TAG, BOLD_TEXT_TAG, EMPHATIZED_TEXT_TAG, SUB_TEXT_TAG, SUP_TEXT_TAG];
                if (el.tagName === HEADING_TAG) {
                    var tocItem = _getParentTocItem(el, tocItemsList);
                    if (tocItem && tocItem.itemHeading === "MANDATORY") {
                        elementsToBeChecked.push(HEADING_TAG);
                    }
                }
                if (elementsToBeChecked.includes(el.tagName) && !$.trim(el.innerText)) {
                    if ((el.tagName === PARAGRAPH_POINT_TAG || el.tagName === SUBPARAGRAPH_SUBPOINT_TAG)
                            && (el.parentElement.tagName === TABLE_CELL_TAG || el.parentElement.tagName === TABLE_CELL_HEADER_TAG)) {
                        return false;
                    } else if (el.children.length > 0 && _containsOnlyChildrenOf(el, childElementsToBeChecked)
                            && ((el.previousElementSibling && el.previousElementSibling.tagName !== TABLE_TAG)
                                || !el.previousElementSibling)) {
                        return true;
                    } else if (el.children.length > 0 && el.children[0].tagName === HEADING_TAG) {
                        return el.children[0].innerText.trim().length === 0;
                    } else {
                        return el.children.length === 0;
                    }
                }
                return false;
            }
        });
    }

    function _getParentTocItem(el, tocItemsList) {
        var currentElem = el;
        var parentTocItem;
        do {
            parentTocItem = tocItemsList.find(function (elem) {
                return elem.aknTag.toLowerCase() === currentElem.parentElement.tagName.toLowerCase();
            });
            currentElem = currentElem.parentElement;
        } while (!parentTocItem && currentElem.parentElement);
        return parentTocItem;
    }

    function _containsOnlyChildrenOf(element, tagElements) {
        for (var j = 0; j < element.children.length; j++) {
            if (!tagElements.includes(element.children[j].tagName)) { return false; }
        }
        return true;
    }

    function _setItemInStorage(key, value) {
        var storage = window.sessionStorage;
        if(storage) {
            storage.setItem(key, value);
        }
    }

    function _getItemStorage(key) {
        var storage = window.sessionStorage;
        if(storage) {
            return storage.getItem(key);
        }
        return "";
    }

    function _clearItemStorage() {
        var storage = window.sessionStorage;
        if(storage) {
            storage.clear();
        }
    }

    return {
        getParentElement: _getParentElement,
        getElementOrigin : _getElementOrigin,
        registerEmptyTrimSelector: _registerEmptyTrimSelector,
        setItemInStorage : _setItemInStorage,
        getItemStorage : _getItemStorage,
        clearItemStorage : _clearItemStorage,
        COUNCIL_INSTANCE : COUNCIL_INSTANCE
    };
});
