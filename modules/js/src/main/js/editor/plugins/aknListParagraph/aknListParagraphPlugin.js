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
define(function aknListParagraphPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosHierarchicalElementTransformerStamp = require("plugins/leosHierarchicalElementTransformer/hierarchicalElementTransformer");
    var pluginName = "aknListParagraph";
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");
    var UTILS = require("core/leosUtils");

    var ENTER_KEY = 13;
    var SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;
    var ORIGIN_EC = "ec";

    var TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED, TRISTATE_OFF = CKEDITOR.TRISTATE_OFF;

    var pluginDefinition = {
        icons: pluginName.toLowerCase(),
        init : function init(editor) {
            editor.on("toDataFormat", _transformParagraph, null, null, 15);

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

            editor.on('selectionChange', function(event) {
                var tableCommand = event.editor.getCommand('table');
                if (tableCommand) {
                    let selection = event.editor.getSelection();
                    let element = leosKeyHandler.getSelectedElement(selection);
                    tableCommand.setState(_isSubparagraphElement(element) ? TRISTATE_DISABLED : TRISTATE_OFF);
                }
            }, null, null, 100);
        }
    };

    function _onEnterKey(context) {
        let selection = context.event.editor.getSelection();
        let element = leosKeyHandler.getSelectedElement(selection);
        if ((UTILS.getElementOrigin(element) === ORIGIN_EC) || _isSubparagraphElement(element)) {
            context.event.cancel();
        }
    }

    function _isSubparagraphElement(element) {
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

    function _onShiftEnterKey(context) {
        context.event.cancel();
    }

    function _transformParagraph(event) {
        if (!event.data.dataValue.includes("</list>") && event.data.dataValue.includes("<subparagraph>")) {
            event.data.dataValue = event.data.dataValue.replace("<subparagraph>", "").replace("</paragraph>", "").replace(/subparagraph>/g, "paragraph>");
        }
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    var leosHierarchicalElementTransformer = leosHierarchicalElementTransformerStamp({
        firstLevelConfig: {
            akn: 'paragraph',
            html: 'ol[data-akn-name=aknAnnexList]',
            attr: [{
                html: "data-akn-name=aknAnnexList"
            },{
                html: "data-akn-element=paragraph"
            }]
        },
        rootElementsForFrom: ["paragraph"],
        contentWrapperForFrom: "subparagraph",
        rootElementsForTo: ["ol","li"]
    });

    var transformationConfig = leosHierarchicalElementTransformer.getTransformationConfig();

    // return plugin module
    var pluginModule = {
        name : pluginName,
        transformationConfig : transformationConfig
    };

    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    return pluginModule;
});