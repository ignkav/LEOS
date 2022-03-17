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
define(function leosHierarchicalElementShiftEnterHandlerModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var CKEDITOR = require("promise!ckEditor");
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");
    var pluginName = "leosHierarchicalElementShiftEnterHandler";
    var leosPluginUtils = require("plugins/leosPluginUtils");

    var LOG = require("logger");
    var SHIFT_ENTER = CKEDITOR.SHIFT + 13;
    var ENTER_KEY = 13;

    var SHIFT_ENTER_ALLOWED = CKEDITOR.TRISTATE_OFF;
    var SHIFT_ENTER_NOT_ALLOWED = CKEDITOR.TRISTATE_DISABLED;
    var SHIFT_ENTER_STATUS;

    var DATA_AKN_NUM = "data-akn-num";
    var DATA_AKN_CONTENT_ID = "data-akn-content-id";
    var DATA_AKN_MP_ID = "data-akn-mp-id";
    var DATA_AKN_WRAPPED_CONTENT_ID = "data-akn-wrapped-content-id";
    var DATA_AKN_NAME = "data-akn-name";
    var DATA_AKN_ELEMENT = "data-akn-element";
    var SUBPARAGRAPH = "subparagraph";
    
    var CMD_NAME = "leosHierarchicalElementShiftEnterHandler";

    var pluginDefinition = {
        icons: pluginName.toLowerCase(),
        init : function init(editor) {
            editor.ui.addButton(pluginName, {
                label: 'Soft enter',
                command: CMD_NAME,
                toolbar: 'shiftenter'
            });

            var shiftEnterCommand = editor.addCommand(CMD_NAME, {
                exec: function(editor) {
                    _onShiftEnterKeyCommand(this, editor);
                }
            });

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : SHIFT_ENTER,
                action : _onShiftEnterKey
            });

            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : ENTER_KEY,
                action : _onEnterKey
            });

            editor.on("change", _handleCKEvent, null, shiftEnterCommand);
            editor.on("selectionChange", _handleCKEvent, null, shiftEnterCommand);
            $(editor.element.$).on("keyup mouseup", null, [editor, shiftEnterCommand], _handleJQueryEvent);
        }
    };

    var _handleJQueryEvent = function _handleJQueryEvent(event) {
        var evtDataArray = event.data;
        var editor = evtDataArray[0];
        var cmd = evtDataArray[1];
        _setCurrentShiftEnterStatus(editor, cmd);
    }

    var _handleCKEvent = function _handleCKEvent(event) {
        var editor = event.editor;
        var cmd = event.listenerData;
        _setCurrentShiftEnterStatus(editor, cmd);
    }

    var _setCurrentShiftEnterStatus = function _setCurrentShiftEnterStatus(editor, cmd) {
        SHIFT_ENTER_STATUS = SHIFT_ENTER_NOT_ALLOWED;
        if (isShiftEnterAllowedInThisContext(editor)) {
            SHIFT_ENTER_STATUS = SHIFT_ENTER_ALLOWED;
        }
        cmd.setState(SHIFT_ENTER_STATUS);
    }

    function _onShiftEnterKeyCommand(cmd, editor) {
        LOG.debug("SHIFT_ENTER button clicked");
        _executeShiftEnter(editor);
    }

    function _onEnterKey(context) {
        LOG.debug("ENTER button clicked");
        var elementType = context.editor.LEOS.elementType;
        var selection = context.editor.getSelection();
        if (_isElementInsideTable(selection.getStartElement())) {
            context.event.cancel();
        } else if (elementType && (elementType === 'level' || elementType === 'paragraph') && (_isStartElementOrderedListOrContent(selection))) {
            _executeShiftEnter(context.editor);
        }
    }

    function _isElementInsideTable(element) {
        return element && element.getAscendant('table');
    }

    function _isStartElementOrderedListOrContent(selection) {
        var startElement = selection.getStartElement();
        return startElement
            && (startElement.getAscendant('ol') && startElement.getAscendant('ol').getAttribute(DATA_AKN_NAME) !== 'aknAnnexOrderedList')
            ||  startElement.getAttribute(DATA_AKN_NAME) === 'aknContent';
    }

    function _executeShiftEnter(editor) {
        var selection = editor.getSelection();
        var startElement = leosKeyHandler.getSelectedElement(selection);
        var startElementName = startElement.getName && startElement.getName();
        // grab the content from selection to the end of the current inline content
        var contentAfterShiftEnter = getContentAfterShiftEnter(editor);
        // if the current inline content is not wrap in p, wrap it if it is not heading
        var wrappingP = startElementName === 'h2' ? startElement : wrapCurrentInlineContent(startElement, editor);
        // insert new subparagraph with extracted content in the next line
        contentAfterShiftEnter.insertAfter(wrappingP);
        // make selection at the beginning of the new subparagraph
        setNewSelection(editor, contentAfterShiftEnter);
        
        editor.fire("change");
    }

    function _onShiftEnterKey(context) {
        var event = context.event;
        var editor = event.editor;

        if (isShiftEnterAllowedInThisContext(editor)) {
            LOG.debug("SHIFT_ENTER event intercepted: ", event);
            _executeShiftEnter(editor);
        }
        else {
            LOG.debug("SHIFT_ENTER event intercepted but cancelled: ", event);
        }
        event.cancel();
    }

    var getInlineWrapper = function getInlineWrapper(el) {
        var inlineWrapper;
        var elementList = ['p', 'th', 'td', 'li'];
        for (var element of elementList) {
            inlineWrapper = el.getAscendant(element, true);
            if (inlineWrapper) { break; }
        }
        return inlineWrapper;
    };

    var _isElementEmpty = function _isElementEmpty(el) {
        return !el.getChildCount() ||
            ((el.getChildren().getItem(0).type === CKEDITOR.NODE_TEXT) &&
                    (el.getChildren().getItem(0).getText().trim().replace(/\u200B/g, '') === ""));
    };

    var wrapCurrentInlineContent = function wrapCurrentInlineContent(el, editor) {
        var inlineWrapper = getInlineWrapper(el);
        if (inlineWrapper != null) {
            var inlineWrapperName = inlineWrapper.getName && inlineWrapper.getName();
            if ((inlineWrapperName === 'li') || (inlineWrapperName === 'th') || (inlineWrapperName === 'td')) {
                var blockChild = _getBlockElementIfExists(inlineWrapper);
                var rangeContent = getFirstRange(editor).clone();

                (blockChild) ? rangeContent.setStartAfter(blockChild) :
                    rangeContent.setStart(inlineWrapper, 0);

                var content = rangeContent.extractContents();
                var pElement = new CKEDITOR.dom.element('p');
                if ((inlineWrapperName === 'th') || (inlineWrapperName === 'td')) {
                    pElement.setAttribute(DATA_AKN_NAME, 'aknParagraph');
                } else if ((inlineWrapper.getAttribute(DATA_AKN_CONTENT_ID) != null)
                    && (inlineWrapper.getAttribute(DATA_AKN_MP_ID) != null)) {
                    pElement.setAttribute(DATA_AKN_WRAPPED_CONTENT_ID, inlineWrapper.getAttribute(DATA_AKN_CONTENT_ID));
                    pElement.setAttribute(DATA_AKN_MP_ID, inlineWrapper.getAttribute(DATA_AKN_MP_ID));
                }

                if (leosKeyHandler.isContentEmptyTextNode(content)) {
                    pElement.appendBogus();
                } else {
                    pElement.append(content);
                }

                var nestedBlock = getNestedBlockElement(inlineWrapper);
                if (nestedBlock) {
                    pElement.insertBefore(nestedBlock);
                } else {
                    inlineWrapper.append(pElement);
                }

                inlineWrapper = pElement;
            } else if (inlineWrapperName === 'p' && _isElementEmpty(inlineWrapper)
                    && _isElementInsideTable(inlineWrapper)) {
                inlineWrapper.appendBogus();
            }
        }
        return inlineWrapper;
    };

    var getContentAfterShiftEnter = function getContentAfterShiftEnter(editor) {
        var selection = editor.getSelection();
        var startElement = selection.getStartElement();
        var startElementName = startElement.getName && startElement.getName();
        var startElementParent = startElement.getParent();
        var firstRange = selection.getRanges()[0];
        var fromShiftEnterRange = getRangeAfterShiftEnter(firstRange, startElement);
        var content = fromShiftEnterRange.extractContents();
        var pElement = new CKEDITOR.dom.element('p');
        if (startElementName === 'h2') {
            var headingElement = content.getChildren().getItem(0);
            pElement.appendText(headingElement.getText());
        } else {
            if (startElementName === 'p' && startElement.hasAttribute(DATA_AKN_NAME)) {
                pElement.setAttribute(DATA_AKN_NAME, startElement.getAttribute(DATA_AKN_NAME));
            } else if (_isElementInsideTable(startElementParent)) {
                pElement.setAttribute(DATA_AKN_NAME, 'aknParagraph');
            }
            pElement.setAttribute(DATA_AKN_ELEMENT, SUBPARAGRAPH);

            if (leosKeyHandler.isContentEmptyTextNode(content) && _isStartElementOrderedListOrContent(selection)) {
                pElement.appendBogus();
            } else {
                pElement.append(content);
            }
        }
        return pElement;
    };

    var getRangeAfterShiftEnter = function getRangeAfterShiftEnter(firstRange, startElement) {
        var fromShiftEnterRange = firstRange.clone();
        var inlineWrapper = getInlineWrapper(startElement);
        var nestedBlock;
        if (inlineWrapper && inlineWrapper.getName() === 'li') {
            nestedBlock = getNestedBlockElement(inlineWrapper);
        }
        if (nestedBlock) {
            fromShiftEnterRange.setEndBefore(nestedBlock, 0);
        } else if (inlineWrapper) {
            fromShiftEnterRange.setEndAt(inlineWrapper,  CKEDITOR.POSITION_BEFORE_END);
        }
        return fromShiftEnterRange;
    };

    var getNestedBlockElement = function getNestedBlockElement(liElement) {
        return liElement.findOne("ol") || liElement.findOne("ul") || liElement.findOne("table");
    };
    
    var getFirstRange = function getFirstRange(editor) {
        var selection = editor.getSelection();
        return selection.getRanges()[0];
    };

    var setNewSelection = function setNewSelection(editor, content) {
        var rangeToSelect, firstChildElement = content.getFirst();
        rangeToSelect = editor.createRange();
        rangeToSelect.setStart(firstChildElement, 0);
        rangeToSelect.setEnd(firstChildElement, 0);
        rangeToSelect.collapse(true);
        rangeToSelect.select();
    };

    var _getBlockElement = function _getBlockElement(element) {
        while (element.type != CKEDITOR.NODE_ELEMENT || !element.isBlockBoundary()) {
            element = element.getParent();
        }
        return element.$;
    };

    function _getBlockElementIfExists(element) {
        var childList = element.getChildren();
        return (childList.length && (childList.getItem(0).type === CKEDITOR.NODE_ELEMENT)) ? childList.getItem(0) : undefined;
    }
    
    function _isLevelElementWithHeadingAndContent(element) {
        var elementName = element.getName && element.getName();
        return elementName === "h2" && leosPluginUtils.isAnnexList(element.getAscendant("ol"), true) &&
            element.getNext() && element.getNext().type === CKEDITOR.NODE_ELEMENT && element.getNext().getName() === "p";
    }
    
    function _isSubparMandateElement(element) {
        return element && 'aknSubParagraphMandate' === element.getAttribute(DATA_AKN_NAME);
    }
    
    function _isSubparMandateElementInsideUnNumberedPar(element) {
        var currentElement = element.getAscendant('ol');
        if (_isSubparMandateElement(currentElement)) {
            do {
                var elementName = currentElement.getName && currentElement.getName();
                if (elementName === 'paragraph') {
                    var firstChildElementName = currentElement.getFirst().getName && currentElement.getFirst().getName();
                    if (firstChildElementName !== 'num') {
                        return true;
                    }
                    break;
                }
            } while (currentElement = currentElement.getParent());
        }
        return false;
    }

    function _isAnnexSubparagraphElement(element) {
        var elementName = element.getName && element.getName();
        if (elementName === 'p') {
            var parentElement = element.getAscendant('ol');
            if (parentElement && 'aknAnnexList' === parentElement.getAttribute('data-akn-name')
                && 'paragraph' === parentElement.getAttribute('data-akn-element')) {
                return true;
            }
        }
        return false;
    }
    
    //Shift-enter is allowed when not present in an unnumbered paragraph
    var isShiftEnterAllowedInThisContext = function isShiftEnterAllowedInThisContext(editor) {
        var selection = editor.getSelection();
        if (!selection) {
            return false;
        }

        // If selection on several block elements, avoid problems and block it
        if (selection.getRanges().length > 0 && (_getBlockElement(getFirstRange(editor).startContainer) != _getBlockElement(getFirstRange(editor).endContainer))) {
            return false;
        }

        // If element is inside table soft-enter has to be enabled
        if (_isElementInsideTable(selection.getStartElement())) {
            return true;
        }

        var currentElement = leosKeyHandler.getSelectedElement(selection);
        if (!currentElement) {
            return false;
        }

        // If element is level with heading and content shift-enter is disabled
        if (_isLevelElementWithHeadingAndContent(currentElement)) {
            return false;
        }
        
        // If element is subpar mandate inside unnumbered paragraph shift-enter is disabled
        if (_isSubparMandateElementInsideUnNumberedPar(currentElement)) {
            return false;
        }

        // If element is annex subparagraph soft-enter has to be disabled
        if (_isAnnexSubparagraphElement(selection.getStartElement())) {
            return false;
        }
        
        // If element is empty shift enter should be forbidden
        if (leosKeyHandler.isContentEmptyTextNode(currentElement)) {
            return false;
        }
        
        if (!getInlineWrapper(currentElement)) {
            return false;
        }
        
        // in order to check if the shift enter is allowed in current selection, take the start element and
        // check ancestors one by one and compare them against allowed_elements
        do {
            var elementName = currentElement.getName && currentElement.getName();
            // Added in case of the unnumbered paragraph: shift-enter should be disabled
            if ((elementName === "li") && (currentElement.getAttribute("data-akn-name") === "aknNumberedParagraph") && (currentElement.getAttribute(DATA_AKN_NUM) === null)) {
                return false;
            }
            if (elementName === "ol") {
                break;
            }
        } while (currentElement = currentElement.getParent());
        
        return true;
    };

    pluginTools.addPlugin(pluginName, pluginDefinition);

    return pluginDefinition;
});