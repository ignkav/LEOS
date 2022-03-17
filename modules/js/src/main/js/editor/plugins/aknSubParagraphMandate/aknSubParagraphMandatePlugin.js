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
define(function aknSubParagraphMandatePluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosHierarchicalElementTransformerStamp = require("plugins/leosHierarchicalElementTransformer/hierarchicalElementTransformer");
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");

    var pluginName = "aknSubParagraphMandate";
    var ENTER_KEY = 13;
    var SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;
    var UNDERLINE = CKEDITOR.CTRL + 85;
    var TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED, TRISTATE_OFF = CKEDITOR.TRISTATE_OFF;

    var pluginDefinition = {
        init: function init(editor) {
            leosKeyHandler.on({
                editor : editor,
                eventType : 'key',
                key : ENTER_KEY,
                action : _onEnterKey
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
                key : UNDERLINE,
                action : _onCtrlUKey
            });
            editor.on("toDataFormat", _transformSubParagraph, null, null, 15);
            editor.on('selectionChange', function(event) {
                var tableCommand = event.editor.getCommand('table');
                if (tableCommand) {
                    tableCommand.setState(_isElementInsideUnNumberedPar(editor) ? TRISTATE_DISABLED : TRISTATE_OFF);
                }
            }, null, null, 100);
        }
    };

    function _onEnterKey(context) {
        context.event.cancel();
    }

    function _onShiftEnterKey(context) {
        context.event.cancel();
    }

    function _onCtrlUKey(context) {
        context.event.cancel();
    }

    function _transformSubParagraph(event) {
        if (event.data.dataValue.includes("<subparagraph>")) {
            event.data.dataValue = (event.data.dataValue + '?').replace("<subparagraph>", "").replace("</subparagraph>?", "");
        }
    }

    function _isElementInsideUnNumberedPar(editor) {
        var selection = editor.getSelection();
        if (!selection) {
            return false;
        }

        var currentElement = leosKeyHandler.getSelectedElement(selection);
        if (!currentElement) {
            return false;
        }

        var paragraphElement = currentElement.getAscendant('paragraph');
        var firstChildElementName = paragraphElement && paragraphElement.getFirst().getName && paragraphElement.getFirst().getName();
        return (firstChildElementName && firstChildElementName !== 'num');


    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    var leosHierarchicalElementTransformer = leosHierarchicalElementTransformerStamp({
        firstLevelConfig: {
            akn: 'subparagraph',
            html: 'ol[data-akn-name=aknSubParagraphMandate]',
            attr: [{
                html: "data-akn-name=aknSubParagraphMandate"
            }]
        },
        rootElementsForFrom: ["subparagraph"],
        contentWrapperForFrom: "subparagraph",
        rootElementsForTo: ["ol", "li"]
    });

    var transformationConfig = leosHierarchicalElementTransformer.getTransformationConfig();

    // return plugin module
    var pluginModule = {
        name: pluginName,
        transformationConfig: transformationConfig
    };

    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    return pluginModule;
});