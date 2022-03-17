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
define(function leosPointCrossHeadingPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var log = require("logger");
    var leosPointCrossHeadingHelper = require("./leosPointCrossHeadingHelper");
    var CKEDITOR = require("promise!ckEditor");
    var pluginName = "leosPointCrossHeading";
    var TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED;
    var POINT_CMD_NAME = "leosCrossHeadingPoint";

    var iconPointCrossheading = 'icons/numberedlist.png';

    var pluginDefinition = {
        init : function init(editor) {
            log.debug("Initializing Crossheading <-> Point plugin...");

            editor.ui.addButton(POINT_CMD_NAME, {
                label: 'Crossheading <-> Point',
                command: POINT_CMD_NAME,
                toolbar: 'crossheadingList',
                icon: this.path + iconPointCrossheading
            });

            editor.addCommand(POINT_CMD_NAME, {
                exec: function(editor) {
                    _execCmd(this, editor);
                },
                refresh: function( editor, path ) {
                    _refresh(this, editor, path);
                }
            });

            editor.on('selectionChange', _selectionChange);
        }
    };

    pluginTools.addPlugin(pluginName, pluginDefinition);

    function _execCmd(cmd, editor) {
        cmd.refresh( editor, editor.elementPath() );

        let selection = editor.getSelection();

        let path = editor.elementPath();
        let elt = path.lastElement;
        let id = elt.getAttribute("id");
        if (cmd.state != TRISTATE_DISABLED) {
            _updateEditor(cmd, editor, elt);
        }
        // Clean up, restore selection and update toolbar button states.
        editor.focus();
        editor.forceNextSelectionCheck();
        let rootElt = editor.element.find("li[id='" + id + "']").$[0];
        let range = selection.getRanges()[0];
        range.setStartAt( new CKEDITOR.dom.node(rootElt), CKEDITOR.POSITION_BEFORE_END );
        range.setEndAt( new CKEDITOR.dom.node(rootElt), CKEDITOR.POSITION_BEFORE_END );
        range.select();
    }

    function _refresh(cmd, editor, path) {
        let element = path.lastElement;
        if (element && element.hasAttribute("data-akn-element")
            && element.getAttribute("data-akn-element") == "point") {
            let currentConfig = leosPointCrossHeadingHelper.getCurrentConfigFromAttributes(editor, element);
            if (currentConfig.rootEltCrossheadingType == "list") {
                cmd.setState(CKEDITOR.TRISTATE_OFF);
            } else {
                cmd.setState(CKEDITOR.TRISTATE_ON);
            }
        } else {
            cmd.setState(CKEDITOR.TRISTATE_DISABLED);
        }
    }

    function _updateEditor(cmd, editor, elt) {
        _updateContent(cmd, editor, elt);
    }

    function _updateContent(cmd, editor, elt) {
        var newConfig = {};
        if (cmd.state == CKEDITOR.TRISTATE_OFF) {
            newConfig.rootEltCrossheadingType = 'none';
        } else {
            newConfig.rootEltCrossheadingType = 'list';
        }
        var options = {
            callback: function() {
                leosPointCrossHeadingHelper.updateRootEltAttributes(editor, newConfig, elt);
            }
        };
        editor.setData(editor.getData(), options);
    }

    function _selectionChange(event) {
        event.editor.getCommand(POINT_CMD_NAME).refresh( event.editor, event.editor.elementPath() );
    }

    // return plugin module
    var pluginModule = {
        name : pluginName
    };

    return pluginModule;
});