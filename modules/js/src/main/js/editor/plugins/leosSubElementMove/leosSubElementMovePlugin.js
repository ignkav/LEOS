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
define(function leosSubElementMovePluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var $ = require("jquery");
    var LOG = require("logger");
    var UTILS = require("core/leosUtils");

    var pluginName = "leosSubElementMove";
    const DATA_ORIGIN = "data-origin";
    const EC_ORIGIN = "ec";
    const LS_ORIGIN = "ls";

    var pluginDefinition = {
        init: function init(editor) {
            editor.addCommand("moveTo", moveToCmd);
            addMoveToMenuItem(editor);

            editor.addCommand("moveHere", moveHereCmd);
            editor.addCommand("KeepSourceFormatting", KeepSourceFormattingCmd);
            //editor.addCommand("KeepText", KeepTextCmd);
            addMoveHereMenuItem(editor);

        }
    };

    function addMoveToMenuItem(editor) {
        if (editor.contextMenu) {
            LOG.debug("Adding Move to context menu item...");
            editor.addMenuGroup('moveToGroup');
            editor.addMenuItem('moveTo', {
                label: 'Move to',
                command: 'moveTo',
                group: 'moveToGroup'
            });

            editor.contextMenu.addListener(function (element) {
                if(editor.LEOS.isClonedProposal && ((element.hasAttribute(DATA_ORIGIN) &&
                    element.getAttribute(DATA_ORIGIN) === EC_ORIGIN)) &&
                    element.getAttribute("data-akn-attr-softaction") !== "move_to") {
                    var selection = editor.getSelection();
                    if (selection.isCollapsed()) {
                        editor.getMenuItem("moveTo").label = 'Move this '.concat(element.getAttribute('data-akn-element'))
                          .concat(' to...');
                        return { moveTo: CKEDITOR.TRISTATE_OFF };
                    }
                }
            });
        }
    }

    function addMoveHereMenuItem (editor) {
        if (editor.contextMenu) {
            LOG.debug("Adding Move here context menu item...");
            editor.addMenuGroup('moveHereGroup');
            editor.addMenuItems({
                moveHere: {
                    label: 'Move here',
                    command: 'moveHere',
                    group: 'moveHereGroup',
                    order: 1,
                    getItems : function() {
                        return {
                            KeepSourceFormatting : CKEDITOR.TRISTATE_OFF,
                            KeepText : CKEDITOR.TRISTATE_OFF,
                        };
                    }
                },
                KeepSourceFormatting: {
                    label: 'Keep source formatting',
                    command: 'KeepSourceFormatting',
                    group: 'moveHereGroup',
                }
            });

            editor.contextMenu.addListener(function (element) {
                if(editor.LEOS.isClonedProposal) {
                    var movedElement = _getMovedElement();
                    if(movedElement) {
                        editor.getMenuItem("KeepSourceFormatting").label = 'As ' + element.getAttribute('data-akn-element')
                        return { moveHere: CKEDITOR.TRISTATE_OFF };
                    }
                }
            });
        }
    }

    var moveToCmd = {
        exec: function executeCommandDefinition(editor) {
            var selection = editor.getSelection();
            var element = selection.getStartElement();
            UTILS.setItemInStorage("movedElement", element.getOuterHtml());
            element.setAttribute("class", "selectedMovedElement");
        }
    }

    var moveHereCmd = {
        exec: function executeCommandDefinition(editor) {
        }
    }

    var KeepSourceFormattingCmd = {
        exec: function executeCommandDefinition(editor) {
            var movedElement = _getMovedElement();
            if(movedElement) {
                var movedElementName = movedElement.getAttribute('data-akn-element');
                LOG.debug("Moved element is - " + movedElement.getAttribute('data-akn-element'));
                var selection = editor.getSelection();
                var element = selection.getStartElement();
                element.setText(movedElement.innerText);
                element.setAttribute('id', movedElement.getAttribute('id'));
                element.setAttribute("data-akn-num", element.getAttribute("data-akn-num"));
                element.setAttribute("data-num-origin", LS_ORIGIN);
                _setSoftMovedAttributes(element, editor);
                UTILS.clearItemStorage();
            }
        }
    }

/*    var KeepTextCmd = {
        exec: function executeCommandDefinition(editor) {
            //TODO: implement
            var movedElement = _getMovedElement();
            if(movedElement) {
                LOG.debug("Moved element is - " + movedElement.getAttribute('data-akn-element'));
            }
        }
    }*/

    function _getMovedElement() {
        var movedElement = UTILS.getItemStorage("movedElement");
        var wrapper = document.createElement('div');
        wrapper.innerHTML = movedElement;
        return wrapper.firstChild;
    }

    function _setSoftMovedAttributes(element, editor) {
        var currentDate = new Date();
        var dateString = currentDate.toDateString(); //TODO: Format date
        var idAttr = element.getAttribute("id");
        element.setAttribute("data-akn-attr-softaction", "move_from");
        element.setAttribute("data-akn-attr-softactionroot", "true");
        element.setAttribute("data-akn-attr-softuser", editor.LEOS.user.name);
        element.setAttribute("data-akn-attr-softdate", dateString);
        element.setAttribute("data-akn-attr-softmove_from", "moved_"+idAttr);
        //set id attr to temp_ temporarily
        element.setAttribute("id", "temp_"+idAttr);
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    // return plugin module
    var pluginModule = {
        name: pluginName,
    };

    return pluginModule;
});