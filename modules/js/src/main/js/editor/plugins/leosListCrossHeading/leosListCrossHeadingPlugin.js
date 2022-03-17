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
define(function leosListCrossHeadingPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var log = require("logger");
    var leosListCrossHeadingHelper = require("./leosListCrossHeadingHelper");
    var CKEDITOR = require("promise!ckEditor");
    var pluginName = "leosListCrossHeading";
    var TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED;
    var BULLET_LIST_CMD_NAME = "leosCrossHeadingBulletList";
    var INDENT_LIST_CMD_NAME = "leosCrossHeadingIndentList";

    var iconBulletList = 'icons/bulletedlist.png';
    var iconIndentList = 'icons/indentedlist.png';

    var pluginDefinition = {
        init : function init(editor) {
            log.debug("Initializing List plugin...");

            editor.ui.addButton(BULLET_LIST_CMD_NAME, {
                label: 'Insert/Remove Bullet List',
                command: BULLET_LIST_CMD_NAME,
                toolbar: 'crossheadingList',
                icon: this.path + iconBulletList
            });

            editor.addCommand(BULLET_LIST_CMD_NAME, {
                exec: function(editor) {
                    _execCmd(this, editor);
                },
                refresh: function( editor, path ) {
                    _refresh(this, editor, path);
                }
            });

            editor.ui.addButton(INDENT_LIST_CMD_NAME, {
                label: 'Insert/Remove Indent List',
                command: INDENT_LIST_CMD_NAME,
                toolbar: 'crossheadingList',
                icon: this.path + iconIndentList
            });

            editor.addCommand(INDENT_LIST_CMD_NAME, {
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
        let id = elt.getAttribute("data-akn-id");
        if (cmd.state != TRISTATE_DISABLED) {
            _updateEditor(cmd, editor, elt);
        }
        // Clean up, restore selection and update toolbar button states.
        editor.focus();
        editor.forceNextSelectionCheck();
        let rootElt = editor.element.find("p[data-akn-id='" + id + "']").$[0];
        let range = selection.getRanges()[0];
        range.setStartAt( new CKEDITOR.dom.node(rootElt), CKEDITOR.POSITION_BEFORE_END );
        range.setEndAt( new CKEDITOR.dom.node(rootElt), CKEDITOR.POSITION_BEFORE_END );
        range.select();
    }

    function _refresh(cmd, editor, path) {
        let element = path.lastElement;
        if (element && (!element.hasAttribute("data-akn-name")
            || !element.getAttribute("data-akn-name") == "crossHeading")) {
            cmd.setState(CKEDITOR.TRISTATE_DISABLED);
        } else {
            // To be removed for indent implementation
            editor.getCommand('indent').setState(CKEDITOR.TRISTATE_DISABLED);
            /////////////////////////////////////////////
            let currentConfig = leosListCrossHeadingHelper.getCurrentConfigFromAttributes(editor, element);
            if (currentConfig.rootEltBulletType == "bullet" && cmd.name.toLowerCase() == BULLET_LIST_CMD_NAME.toLowerCase()) {
                cmd.setState(CKEDITOR.TRISTATE_ON);
            } else if (currentConfig.rootEltBulletType == "indent" && cmd.name.toLowerCase() == INDENT_LIST_CMD_NAME.toLowerCase()) {
                cmd.setState(CKEDITOR.TRISTATE_ON);
            } else {
                cmd.setState(CKEDITOR.TRISTATE_OFF);
            }
        }
    }

    function _updateEditor(cmd, editor, elt) {
        _updateContent(cmd, editor, elt);
    }

    function _updateContent(cmd, editor, elt) {
        var newConfig = {};
        if (cmd.name == BULLET_LIST_CMD_NAME.toLowerCase() && cmd.state == CKEDITOR.TRISTATE_OFF) {
            newConfig.rootEltBulletType = 'bullet';
            newConfig.rootEltBulletValue = '•';
        } else if (cmd.name == INDENT_LIST_CMD_NAME.toLowerCase() && cmd.state == CKEDITOR.TRISTATE_OFF) {
            newConfig.rootEltBulletType = 'indent';
            newConfig.rootEltBulletValue = '-';
        } else {
            newConfig.rootEltBulletType = 'none';
            newConfig.rootEltBulletValue = '';
        }
        var options = {
            callback: function() {
                leosListCrossHeadingHelper.updateRootEltAttributes(editor, newConfig, elt);
            }
        };
        _updateButtonState(cmd, editor);
        editor.setData(editor.getData(), options);
    }

    function _updateButtonState(cmd, editor) {
        if (cmd.name != BULLET_LIST_CMD_NAME.toLowerCase()) {
            editor.getCommand(BULLET_LIST_CMD_NAME).setState(CKEDITOR.TRISTATE_OFF);
        }
        if (cmd.name != INDENT_LIST_CMD_NAME.toLowerCase()) {
            editor.getCommand(INDENT_LIST_CMD_NAME).setState(CKEDITOR.TRISTATE_OFF);
        }
    }

    function _selectionChange(event) {
        event.editor.getCommand(BULLET_LIST_CMD_NAME).refresh( event.editor, event.editor.elementPath() );
        event.editor.getCommand(INDENT_LIST_CMD_NAME).refresh( event.editor, event.editor.elementPath() );
    }

    // return plugin module
    var pluginModule = {
        name : pluginName
    };

    return pluginModule;
});