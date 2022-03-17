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
define(function aknLevelContentMandatePluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");

    var pluginName = "aknLevelContentMandate";
    var ENTER_KEY = 13;
    var SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;
    var UNDERLINE = CKEDITOR.CTRL + 85;

    var pluginDefinition = {
        init: function init(editor) {
            editor.on("toDataFormat", _transformSubParagraph, null, null, 15);

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
        }
    };
    
    function _transformSubParagraph(event) {
        // Example of structure to normalize, from:
        //      <content><mp>...</mp></content>
        //      <table>...</table>
        //      <p>...</p>
        // To:
        //      <subparagraph><content><mp>...</mp><content></subparagraph>
        //      <subparagraph><content><table>...</table></content></subparagraph>
        //      <subparagraph><content><p>...</p></content></subparagraph>
        if (event.data.dataValue.includes("</content><p>") || event.data.dataValue.includes("</content><table")) {
            // wrap the new p, or table with a content
            event.data.dataValue = event.data.dataValue.replace("<p>", "<content><p>").replace("</p>", "</p></content>")
                                        .replace("<table", "<content><table").replace("</table>", "</table></content>");
            // wrap both <content>s with a subparagraph
            event.data.dataValue = event.data.dataValue.replace(/<content/g, "<subparagraph><content")
                .replace(/<\/content>/g, "</content></subparagraph>");
        }
        if (event.data.dataValue.includes("</content><content>")) {
            // wrap both <content>s with a subparagraph
            event.data.dataValue = event.data.dataValue.replace(/<content/g, "<subparagraph><content")
                .replace(/<\/content>/g, "</content></subparagraph>");
        }
    }

    function _onEnterKey(context) {
        context.event.cancel();
    }

    function _onShiftEnterKey(context) {
        context.event.cancel();
    }

    function _onCtrlUKey(context) {
        context.event.cancel();
    }
    
    pluginTools.addPlugin(pluginName, pluginDefinition);

    var transformationConfig = {
        akn: "content",
        html: "p[data-akn-name=aknContent]",
        attr: [{
            akn : "leos:origin",
            html : "data-content-origin"
        }, {
            akn: "xml:id",
            html: "id"
        }, {
            akn: "leos:editable",
            html: "data-akn-attr-editable"
        }, {
            html: "data-akn-name=aknContent"
        }],
        sub: {
            akn: "mp",
            html: "p[data-akn-name=aknContent]",
            attr: [{
                akn: "xml:id",
                html: "data-akn-mp-id"
            }],
            sub: {
                akn: "text",
                html: "p/text"
            }
        }
    };
    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    // return plugin module
    var pluginModule = {
        name: pluginName,
        transformationConfig: transformationConfig
    };

    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    return pluginModule;
});