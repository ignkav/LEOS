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

define(function aknLevelMandatePluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var leosHierarchicalElementTransformerStamp = require("plugins/leosHierarchicalElementTransformer/hierarchicalElementTransformer");
    var pluginName = "aknLevelMandate";
    var leosKeyHandler = require("plugins/leosKeyHandler/leosKeyHandler");

    const ENTER_KEY = 13;
    const SHIFT_ENTER = CKEDITOR.SHIFT + ENTER_KEY;
    var UNDERLINE = CKEDITOR.CTRL + 85;

    const EC = "ec";
    const CN = "cn";
    const LEOS_ORIGIN = "leos:origin";
    const DATA_ORIGIN = "data-origin";
    const NUM = "num";
    const SOFT_REMOVED_STYLE = "leos-content-soft-removed";

    var pluginDefinition = {
        icons: pluginName.toLowerCase(),
        init : function init(editor) {
            editor.on("toHtml", removeInitialSnapshot, null, null, 100);
            editor.on("toHtml", _wrapContentWithSubparagraph, null, null, 5);
            editor.on("toDataFormat", _unWrapContentFromSubparagraph, null, null, 15);
            editor.on("levelIndent", _renumberOnIndent);
            editor.on("levelOutdent", _renumberOnOutdent);
            
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

    function _onEnterKey(context) {
        var selection = context.event.editor.getSelection();
        var startElement = leosKeyHandler.getSelectedElement(selection);
        if((getSelectedRange(context) && !getSelectedRange(context).collapsed) || startElement.getName() === 'h2' || EC === startElement.getAttribute('data-wrapped-content-origin')) {
            context.event.cancel();
        }
    }

    function _onShiftEnterKey(context) {
        var selection = context.event.editor.getSelection();
        var startElement = leosKeyHandler.getSelectedElement(selection);
        if((getSelectedRange(context) && !getSelectedRange(context).collapsed) || startElement.getName() === 'h2' || EC === startElement.getAttribute('data-wrapped-content-origin')) {
            context.event.cancel();
        }
    }

    function _onCtrlUKey(context) {
        context.event.cancel();
    }

    function removeInitialSnapshot(event) {
        if (event.editor.undoManager.snapshots.length > 0) {
            if (event.editor.undoManager.snapshots[0].contents.indexOf("level") < 0) {
                event.editor.undoManager.snapshots.shift();
            }
        }
    }
    
    function _wrapContentWithSubparagraph(event) {
        var level = (event.data.dataValue instanceof CKEDITOR.htmlParser.element) ? event.data.dataValue.findOne("level") : null;
        if (level) {
            var content = level.findOne("content");
            if (content) {
                level.findOne("content").wrapWith(new CKEDITOR.htmlParser.element("subparagraph", {}));
            }
        }
    }
    
    function _unWrapContentFromSubparagraph(event) {
        if (!event.data.dataValue.includes("</list>") && (event.data.dataValue.match(new RegExp("<subparagraph", "g")) || []).length === 1) {
            event.data.dataValue = event.data.dataValue.replace(/<subparagraph.*><content/, "<content").replace("<\/subparagraph>", "");
        }
    }

    function _renumberOnIndent(evt) {
        const nextNum = (evt.data.prevLvlDepth === evt.data.currLvlDepth) ? _getNextIndentNum(evt.data.currLvlNum) :
            _getNextNum(evt.data.currLvlDepth + 1, evt.data.prevLvlNum);
        evt.data.nextNum = nextNum;
        return evt.data;
    }

    function _renumberOnOutdent(evt) {
        const nextNum = _getNextNum(evt.data.currLvlDepth - 1, evt.data.prevLvlNum);
        evt.data.nextNum = nextNum;
        return evt.data;
    }

    function _getNextNum(depth, levelNum) {
        const alphaRegex= /[a-z]+/;
        const numArr = levelNum.split(".");
        numArr.pop();
        if (alphaRegex.test(numArr[depth - 1])) {
            let matched = numArr[depth - 1].match(alphaRegex);
            numArr[depth -1] = numArr[depth -1].replace(matched[0], nextChar(matched[0]));
        }else{
            const number = Number(numArr[depth - 1]) + 1;
            numArr[depth - 1] = number.toString();
        }
        const copyArr = numArr.slice(0, depth);
        return copyArr.join(".").concat(".");
    }

    function _getNextIndentNum(listNum) {
        const numArr = listNum.split(".");
        numArr.pop();
        const lastNum = numArr[numArr.length - 1];
        numArr[numArr.length - 1] = (lastNum > 1) ? (lastNum - 1).toString() : lastNum.toString();
        numArr.push("1.");
        return numArr.join(".");
    }

    function nextChar(c) {
        return c < "z" ? String.fromCharCode(c.charCodeAt(0) + 1) : "#";
    }

    var getSelectedRange = function getSelectedRange(context) {
        var selection = context.event.editor.getSelection(), ranges = selection && selection.getRanges();
        var firstRange;
        if (ranges && ranges.length > 0) {
            firstRange = ranges[0];
        }
        return firstRange;
    };

    pluginTools.addPlugin(pluginName, pluginDefinition);

    var leosHierarchicalElementTransformer = leosHierarchicalElementTransformerStamp({

        firstLevelConfig: {
            akn: 'level',
            html: 'ol[data-akn-name=aknAnnexList]',
            attr: [{
                html: "data-akn-name=aknAnnexList"
            }, {
                akn: 'leos:depth',
                html: 'data-akn-depth'
            }]
        },
        rootElementsForFrom: ["level"],
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