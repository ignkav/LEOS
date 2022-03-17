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
define(function aknLevelNumPluginModule(require) {
    "use strict";

    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var pluginName = "aknLevelNum";

    var pluginDefinition = {
        icons: pluginName.toLowerCase(),
        init : function init(editor) {
            editor.on("levelIndent", _renumberOnIndent);
            editor.on("levelOutdent", _renumberOnOutdent);
            editor.on("instanceReady",_removeTextNodes);
            editor.on("instanceReady",_setOriginalLevelNumberAttrs);
            editor.on('afterCommandExec', _removeTextNodes, null, null, 1);
        }
    };

    const EC = "ec";
    const CN = "cn";
    const LEOS_ORIGIN = "leos:origin";
    const NUM = "num";
    const AKN_LEVEL_NUM = "aknLevelNum";
    const SOFT_REMOVED_STYLE = "leos-content-soft-removed";
    const DATA_AKN_NAME =  "data-akn-name";
    const DATA_NUM_ORIGIN = "data-num-origin";
    var DATA_AKN_ORIGIN_DEPTH_ATTR = "data-akn-origin-depth";
    var DATA_AKN_ORIGIN_NUM_ATTR = "data-akn-origin-num";

    function _removeTextNodes(evt) {
        var editor = evt.editor;
        editor.editable().getChildren().toArray().forEach(function (element) {
            if (element.type === CKEDITOR.NODE_TEXT) {
                element.remove();
            }
        })
        editor.fire("focus");
    }

    function _setOriginalLevelNumberAttrs(evt) {
        var editor = evt.editor;
        editor.editable().getChildren().toArray().forEach(function (element) {
            if (EC === element.getAttribute(DATA_NUM_ORIGIN) && AKN_LEVEL_NUM === element.getAttribute(DATA_AKN_NAME)) {
                element.setAttribute(DATA_AKN_ORIGIN_DEPTH_ATTR, _getLevelDepth(element.getText()));
                element.setAttribute(DATA_AKN_ORIGIN_NUM_ATTR, element.getText());
            }
        })
        editor.fire("focus");
    }

    function _renumberOnIndent(evt) {
        const nextNum = _getNextIndentNum(evt.data);
        evt.data.nextNum = nextNum;
        return evt.data;
    }

    function _renumberOnOutdent(evt) {
        const $prevLevels = evt.data.prevLevels;
        let depthAfterOutdent = evt.data.currLvlDepth - 1;

        let alphaNum = _getNextAlphaNum($prevLevels, depthAfterOutdent);
        const nextNum = _getNextNum(depthAfterOutdent, evt.data.prevLvlNum, alphaNum);
        evt.data.nextNum = nextNum;

        return evt.data;
    }

    function _getNextAlphaNum($prevLevels, depth) {
        const alphaRegex= /[a-z]+/;
        const negRegex= /-+\d+/;
        let alphaNum = "a";

        $prevLevels.each(function (index, level) {
            let numVal = _getLevelNum(level);
            let prevLvlDepth = _getLevelDepth(numVal);
            const numArr = numVal.split(".");
            numArr.pop();
            if (prevLvlDepth == depth && numArr.length >= depth) {
                if (alphaRegex.test(numArr[depth - 1])) {
                    let matched = numArr[depth - 1].match(alphaRegex);
                    alphaNum = nextChar(matched[0]);
                } else if (negRegex.test(numVal) || $(level).find(NUM).attr(LEOS_ORIGIN) === CN) {
                    alphaNum = "";
                }
                return false;
            } else if($(level).find(NUM).attr(LEOS_ORIGIN) === CN && depth >= prevLvlDepth){ //case when the parent if immediate parent if already converted
                let matched = numArr[numArr.length - 1].match(alphaRegex);
                alphaNum = nextChar(matched[0]);
                return false;
            }
        });
        return alphaNum;
    }

    function _getNextNum(depth, lvlNum, alphaNum) {
        let number;
        const alphaRegex= /[a-z]+/;
        const negRegex= /-+\d+/;
        const numArr = lvlNum.split(".");
        numArr.pop();
        depth = numArr.length < depth ? _getLevelDepth(lvlNum) : depth;
        if(alphaRegex.test(numArr[depth - 1])) {
            number = numArr[depth - 1].substr(0, numArr[depth - 1].length - 1);
        } else if(negRegex.test(numArr[depth - 1])) {
            number = numArr[depth - 1];
        } else {
            number = alphaNum == "" ? Number(numArr[depth - 1]) + 1 : Number(numArr[depth - 1]);
        }
        numArr[depth - 1] = number.toString();
        const copyArr = numArr.slice(0, depth);
        return copyArr.join(".").concat(alphaNum).concat(".");
    }

    function _isOriginEC(level) {
        return EC === $(level).find(NUM).attr(LEOS_ORIGIN);
    }

    function _isEcElementPresentAtNextLevel($nextLevels, currLvlDepth) {
        let ecElementPresent = false;
        $nextLevels.each(function(index, level) {
            let numVal = _getLevelNum(level);
            let depth = _getLevelDepth(numVal);
            if(_isOriginEC(level)) {
                if(depth === currLvlDepth + 1) {
                    ecElementPresent = true;
                    return false;
                }
            } else if (_isOriginEC(level) && depth <= currLvlDepth) {
                return false; //exit from loop
            }
        });
        return ecElementPresent;
    }

    function _getNextIndentNum(data) {
        const $prevLevels = data.prevLevels;
        const $nextLevels = data.nextLevels;
        const depthAfterIndent = data.currLvlDepth + 1;
        let nextNum;

        let ecElementPresent = _isEcElementPresentAtNextLevel($nextLevels, data.currLvlDepth);
        if(!ecElementPresent) {
            if(data.prevLvlDepth === data.currLvlDepth) {
                nextNum = data.prevLvlNum.concat("1.");
            } else {
                let alphaNum = _getNextAlphaNum($prevLevels, depthAfterIndent);
                nextNum = _getNextNum(depthAfterIndent, data.prevLvlNum, alphaNum);
            }
        } else {
            if(data.currLvlDepth < data.prevLvlDepth) {
                let alphaNum = _getNextAlphaNum($prevLevels, depthAfterIndent);
                nextNum = _getNextNum(depthAfterIndent, data.prevLvlNum, alphaNum);
            } else {
                $nextLevels.each(function (index, level) {
                    let numVal = _getLevelNum(level);
                    let depth = _getLevelDepth(numVal);
                    if (depth === depthAfterIndent) {
                        const numArr = numVal.split(".");
                        numArr.pop();
                        let number = Number(numArr[depthAfterIndent - 1]);

                        if (EC === $(level).find(NUM).attr(LEOS_ORIGIN)) {
                            numArr[depthAfterIndent - 1] = (-Math.abs(number)).toString();
                        } else if (CN === $(level).find(NUM).attr(LEOS_ORIGIN)) {
                            number = number < 0 ? (number - 1) : (number + 1);
                            numArr[depthAfterIndent - 1] = number.toString();
                        }

                        const copyArr = numArr.slice(0, depthAfterIndent);
                        nextNum = copyArr.join(".").concat(".");
                        return false;
                    } else if(depth < data.currLvlDepth) {
                        return false;
                    }
                });
            }
        }
        return nextNum;
    }

    function _getLevelDepth(listNum) {
        let depth = 0;
        if(listNum != null && listNum.includes(".")) {
            const arr = listNum.split(".");
            depth = arr.length - 1;
        }
        return depth;
    }

    function _getLevelNum(level) {
        if (level) {
            let $level;
            if (!(level instanceof jQuery)) {
                $level = $(level);
            }
            var $num = $level.find(NUM);
            var $content = $num.contents()
                .filter(function () {
                    return (this.nodeType === CKEDITOR.NODE_TEXT ||
                        !(this.nodeType === CKEDITOR.NODE_ELEMENT && this.classList.contains(SOFT_REMOVED_STYLE)));
                });
            return $content.text();
        }
    }

    function nextChar(c) {
        return c < "z" ? String.fromCharCode(c.charCodeAt(0) + 1) : "#";
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    var transformationConfig = {
       akn : "num",
       html : "p[data-akn-name=aknLevelNum]",
       attr : [  {
           akn : "leos:origin",
           html : "data-num-origin"
       },{
           akn : "leos:originalECNumber",
           html : "data-akn-origin-num"
       },{
           akn : "leos:originalECDepth",
           html : "data-akn-origin-depth"
       }, {
           akn : "xml:id",
           html : "data-akn-num-id"
       }, {
           html : "contenteditable=false"
       }, {
           html: "data-akn-name=aknLevelNum"
       } ],
       sub : {
           akn : "text",
           html : "p/text"
       }

    };
    // return plugin module
    var pluginModule = {
        name : pluginName,
        transformationConfig : transformationConfig
    };

    pluginTools.addTransformationConfigForPlugin(transformationConfig, pluginName);

    return pluginModule;
});