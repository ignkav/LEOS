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
define(function leosIndentMandatePluginModule(require) {
    "use strict";

    $.fn.appendToWithIndex = function (to,index) {
        if (! to instanceof jQuery) {
            to=$(to);
        };
        if (index===0) {
            $(this).prependTo(to)
        } else {
            $(this).insertAfter(to.children().eq(index-1));
        }
    };

    // load module dependencies
    let CKEDITOR = require("promise!ckEditor");
    let LOG = require("logger");
    let pluginTools = require("plugins/pluginTools");
    let leosPluginUtils = require("plugins/leosPluginUtils");

    const NUM = "num";
    const NUMBERED_ITEM = "point, indent, paragraph";
    const NUMBERED_LEVEL_ITEM = "point, indent, paragraph, level";
    const UNUMBERED_ITEM = "alinea, subparagraph";
    const POINT = "point";
    const SUBPOINT = "alinea";
    const SUBPARAGRAPH = "subparagraph";
    const PARAGRAPH = "paragraph";
    const ITEM_HTML = "li";
    const LIST = "list";
    const DATA_AKN_NUM = "data-akn-num";
    const ATTR_INDENT_LEVEL = "data-indent-level";
    const ATTR_INDENT_NUMBERED = "data-indent-numbered";
    const LEVEL = "level";
    const ITEMS_SELECTOR = LIST + "," + NUMBERED_ITEM + "," + UNUMBERED_ITEM;
    let modeRealTimeIndent = true;

    let indentationStatus = {
        original: {
            num: undefined,
            level: -1,
            parent: '',
            realPosition: -1,
            position: -1,
            numbered: true
        },
        current: {
            level: -1,
            prevNumbered: [],
            move: 0,
            numbered: true
        }
    };

    function resetIndentStatus() {
        indentationStatus.original.num = undefined;
        indentationStatus.original.level = -1;
        indentationStatus.original.parent = '';
        indentationStatus.original.position = -1;
        indentationStatus.original.realPosition = -1;
        indentationStatus.original.numbered = true;
        indentationStatus.current.prevNumbered = [];
        indentationStatus.current.level = indentationStatus.original.level;
        indentationStatus.current.move = 0;
        indentationStatus.current.numbered = true;
    }

    const pluginName = "leosIndentMandate";

    const TRISTATE_DISABLED = CKEDITOR.TRISTATE_DISABLED,
        TRISTATE_OFF = CKEDITOR.TRISTATE_OFF;

    let pluginDefinition = {
        requires: 'indent',
        init: function init(editor) {
            let globalHelpers = CKEDITOR.plugins.indent;
            modeRealTimeIndent = true;

            resetIndentStatus();
            
            // Register commands.
            globalHelpers.registerCommands( editor, {
                aknindentlist: new commandDefinition( editor, 'aknindentlist', true ),
                aknoutdentlist: new commandDefinition( editor, 'aknoutdentlist' )
            } );

            function commandDefinition(editor) {
                globalHelpers.specificDefinition.apply( this, arguments );

                // Indent and outdent lists with TAB/SHIFT+TAB key. Indenting can
                // be done for any list item that isn't the first child of the parent.
                editor.on('key', function(evt) {
                    let path = editor.elementPath();

                    if (editor.mode != 'wysiwyg')
                        return;

                    if (evt.data.keyCode == this.indentKey) {
                        // Prevent of getting context of empty path (#424)(https://dev.ckeditor.com/ticket/17028).
                        if (!path) {
                            return;
                        }
                        const ol = $(this.getContext(path).$);

                        // Don't indent if in first list item of the parent.
                        if (this.isIndent && (!_shouldIndent(ol)))
                            return;
                        if ((!this.isIndent) && (!_shouldOutdent(ol)))
                            return;

                        editor.execCommand(this.relatedGlobal);

                        evt.cancel();
                    }
                }, this);

                this.jobs[this.isIndent ? 10 : 30] = {
                    refresh: this.isIndent ?
                        function(editor, path) {
                            _initIndentStatus(editor);
                            if (this.getContext(path) == null) {
                                return;
                            }
                            const ol = $(this.getContext(path).$);

                            if (!_shouldIndent(ol)) {
                                return TRISTATE_DISABLED;
                            } else {
                                return TRISTATE_OFF;
                            }
                        } : function(editor, path) {
                            _initIndentStatus(editor);
                            if (this.getContext(path) == null) {
                                return;
                            }
                            const ol = $(this.getContext(path).$);

                            if (!_shouldOutdent(ol)) {
                                return TRISTATE_DISABLED;
                            } else {
                                return TRISTATE_OFF;
                            }
                        },

                    exec: CKEDITOR.tools.bind(aknIndent, this)
                };

                editor.on('instanceReady', function(evt) {
                    _initIndentStatus(editor);
                }, this);
            }

            CKEDITOR.tools.extend(commandDefinition.prototype, globalHelpers.specificDefinition.prototype, {
                // Elements that, if in an elementpath, will be handled by this
                // command. They restrict the scope of the plugin.
                context: {ol: 1}
            });
        }
    };

    function aknIndent(editor) {
        const prevLevel = indentationStatus.current.level;
        // Do Indent
        if (this.isIndent && (indentationStatus.current.level < leosPluginUtils.MAX_LIST_LEVEL || !indentationStatus.current.numbered)) {
            if (indentationStatus.current.move < 0) {
                indentationStatus.current.numbered = indentationStatus.current.prevNumbered.pop();

            } else {
                indentationStatus.current.prevNumbered.push(indentationStatus.current.numbered);
                indentationStatus.current.numbered = !indentationStatus.current.numbered;
            }

            indentationStatus.current.move++;
            if (!indentationStatus.current.numbered) {
                indentationStatus.current.level++;
            }
        }
        // Do Outdent
        else if (indentationStatus.current.level > 0 || indentationStatus.current.numbered) {
            if (indentationStatus.current.move > 0) {
                indentationStatus.current.numbered = indentationStatus.current.prevNumbered.pop();

            } else {
                indentationStatus.current.prevNumbered.push(indentationStatus.current.numbered);
                indentationStatus.current.numbered = !indentationStatus.current.numbered;
            }

            indentationStatus.current.move--;
            if (indentationStatus.current.numbered) {
                indentationStatus.current.level--;
            }
        }

        let newNumValue = indentationStatus.original.num;
        if (indentationStatus.current.move != 0) {
            newNumValue = indentationStatus.current.numbered ? '#' : undefined;
            _doIndent(editor);
            _setIndentAttributes(editor, this.isIndent);
        } else {
            indentationStatus.current.level = indentationStatus.original.level;
            _resetIndent(editor, this.isIndent);
            _resetIndentAttributes(editor);
        }
        _setCurrentNumValue(newNumValue, editor);

        return 0;
    }

    function _doIndent(editor) {
        let source = $(editor.element.$);

        editor.fire("indent");

        // If first alinea, should be considered as the point itself
        if (_isFirstChild(editor) && _isSubpoint(editor)) {
            source = $(editor.element.$).parents(NUMBERED_ITEM).first();
        }

        let diff = indentationStatus.current.level - indentationStatus.original.level;
        if (!indentationStatus.current.numbered
            && indentationStatus.original.numbered
            && (!source.parents(LEVEL).length || (_isSubpoint(editor) && _isFirstChild(editor)))) {
            source.css({'margin-left': 40 * (diff - 1) - 5});
        } else if (indentationStatus.original.level == 1 && !indentationStatus.original.numbered
            && !source.parents(LEVEL).length) {
            source.css({'margin-left': 40 * (diff + 1) - 5});
        } else {
            source.css({'margin-left': 40 * diff - 5});
        }
        _doIndentOtherItems(editor, source);
    }

    function _doIndentOtherItems(editor, source) {
        let diff = indentationStatus.current.level - indentationStatus.original.level;

        // For indent, children should not be indented
        if (_isSubpoint(editor) && _isFirstChild(editor) && indentationStatus.current.move > 0) {
            const childrenElts = source.children(ITEMS_SELECTOR);
            if (childrenElts.length) {
                const levelDiff = indentationStatus.current.numbered ? -diff : -diff + 1;
                $.each(childrenElts, function(key, child){
                    if ($(child).prop("tagName").toLowerCase() == LIST) {
                        const children = $(child).children(NUMBERED_ITEM);
                        $.each(children, function (key, listChild) {
                            if (diff != 0) {
                                $(listChild).css({'margin-left': 40 * levelDiff - 5});
                            } else {
                                $(listChild).css({'margin-left': - 5});
                            }
                        });
                    } else if ($(child).prop("tagName").toLowerCase() == SUBPOINT ||
                        $(child).prop("tagName").toLowerCase() == SUBPARAGRAPH) {
                        if (diff != 0) {
                            $(child).css({'margin-left': 40 * levelDiff - 5});
                        } else {
                            $(child).css({'margin-left': - 5});
                        }
                    }
                });
            }
        }
        // If outdenting, next siblings should be outdented only when there is a change of level
        if (indentationStatus.current.move < 0) {
            const nextElts = source.nextAll(ITEMS_SELECTOR);
            if (nextElts.length) {
                $.each(nextElts, function(key, next){
                    const levelDiff = indentationStatus.current.numbered ? 1 : 0;
                    if ($(next).prop("tagName").toLowerCase() == LIST) {
                        const children = $(next).children(NUMBERED_ITEM);
                        $.each(children, function(key, child){
                            if (diff != 0) {
                                $(child).css({'margin-left': 40 * (diff+levelDiff) - 5});
                            } else {
                                $(child).css({'margin-left': - 5});
                            }
                        });
                    } else {
                        if (diff != 0) {
                            $(next).css({'margin-left': 40 * (diff+levelDiff) - 5});
                        } else {
                            $(next).css({'margin-left': - 5});
                        }
                    }
                });
            }
        }
    }

    function _resetIndent(editor, isIndent) {
        editor.fire("resetIndent");

        let source = $(editor.element.$);
        if (_isSubpoint(editor) && _isFirstChild(editor)) {
            source = $(editor.element.$).parents(NUMBERED_ITEM).first();
        }
        source.css({'margin-left': -5});
        _resetIndentOtherItems(editor, source, isIndent);
    }

    function _resetIndentOtherItems(editor, source, isIndent) {
        // if point with alinea and list, the children should not be indented
        if (_isSubpoint(editor) && _isFirstChild(editor) && !isIndent) {
            const list = source.children(LIST).first();
            if (list.length) {
                const children = list.children(NUMBERED_ITEM);
                $.each(children, function(key, child){
                    $(child).css({'margin-left': -5});
                });
            }
            const subpoints = source.children(UNUMBERED_ITEM);
            if (subpoints.length) {
                $.each(subpoints, function(key, child){
                    $(child).css({'margin-left': -5});
                });
            }
        }
        // If second, third, ... alinea, siblings alinea should be outdented
        else if (_isSubpoint(editor) && isIndent) {
            const alineaNextSiblings = source.nextAll(UNUMBERED_ITEM);
            $.each(alineaNextSiblings, function(key, sibling){
                $(sibling).css({'margin-left': -5});
            });
            const list = source.nextAll(LIST);
            if (list.length) {
                const children = list.children(NUMBERED_ITEM);
                $.each(children, function(key, child){
                    $(child).css({'margin-left': -5});
                });
            }
        }
    }

    function _setIndentAttributes(editor, isIndent) {
        let li = $(editor.element.$).find(ITEM_HTML).first();
        if (li.length) {
            li.attr(ATTR_INDENT_LEVEL, indentationStatus.current.level);
            li.attr(ATTR_INDENT_NUMBERED, indentationStatus.current.numbered);
        }
    }

    function _resetIndentAttributes(editor) {
        let li = $(editor.element.$).find(ITEM_HTML).first();
        if (li.length) {
            li.removeAttr(ATTR_INDENT_LEVEL);
            li.removeAttr(ATTR_INDENT_NUMBERED);
            indentationStatus.current.numbered = indentationStatus.original.numbered;
            indentationStatus.current.prevNumbered = [];
        }
    }

    function _initIndentStatus(editor) {
        if (indentationStatus.original.level == -1) {
            indentationStatus.original.level = _getIndentLevel(editor);
            let source = $(editor.element.$);
            if (_isSubpoint(editor) && _isFirstChild(editor)) {
                source = $(editor.element.$).parents(NUMBERED_ITEM).first();
            }
            indentationStatus.original.realPosition = source.index();
            indentationStatus.original.position = source.prevAll(ITEMS_SELECTOR).length;
            indentationStatus.original.parent = source.parent().attr('id');
            indentationStatus.current.level = indentationStatus.original.level;
            indentationStatus.original.num = _getCurrentNumValue(editor);
            indentationStatus.original.numbered = !(_isSubpoint(editor) && !_isFirstChild(editor));
            indentationStatus.current.numbered = indentationStatus.original.numbered;
        }
    }

    function _shouldOutdent(ol) {
        if (indentationStatus.current.move > 0) {
            return true;
        }

        if (!ol.length || (_isFirstList() && indentationStatus.current.numbered)
            || (indentationStatus.current.level == 1 && !indentationStatus.current.numbered && !ol.parents(PARAGRAPH).length)) {
            return false;
        } else {
            return true;
        }
    }

    function _shouldIndent(ol) {
        if (indentationStatus.current.move < 0) {
            return true;
        }

        if (!ol.length
            || !(_checkParentAndPosition())
            || (_isListDepthMoreThanThreshold(leosPluginUtils.MAX_LEVEL_LIST_DEPTH))) {
            return false;
        } else {
            return true;
        }
    }

    function _checkParentAndPosition() {
        let prevSibling = undefined;

        const currentParent = $('#' + indentationStatus.original.parent);
        if (!indentationStatus.original.position && currentParent.prop("tagName").toLowerCase() != LEVEL) {
            return false;
        }

        let parentChildren = currentParent.children(NUMBERED_ITEM);
        if (!indentationStatus.original.numbered) {
            parentChildren = currentParent.children(ITEMS_SELECTOR);
        }
        prevSibling = $(parentChildren[indentationStatus.original.position - 1]);

        let currentParentDepth = 0;
        if (!prevSibling.length && currentParent.prop("tagName").toLowerCase() == LEVEL) {
            currentParentDepth = 1;
        } else if (!prevSibling.length) {
            return false;
        } else {
            currentParentDepth = _getDepth(prevSibling);
        }

        if (currentParentDepth > indentationStatus.current.level) {
            return true;
        } else if (currentParentDepth == indentationStatus.current.level && !indentationStatus.current.numbered) {
            return true;
        } else {
            return false;
        }
    }

    function _getDepth(item) {
        if (item.prop("tagName").toLowerCase() == PARAGRAPH) {
            let children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            } else {
                return 1;
            }
        }
        if (item.prop("tagName").toLowerCase() == SUBPARAGRAPH) {
            let children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            } else {
                return 1;
            }
        }
        if (item.prop("tagName").toLowerCase() == POINT) {
            let children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            }
        }
        if (item.prop("tagName").toLowerCase() == LIST) {
            let children = item.children(ITEMS_SELECTOR);
            if (children.length) {
                item = children.eq(children.length-1);
            }
        }
        const currentLevel = item.parents(NUMBERED_LEVEL_ITEM).length;
        let depth = currentLevel;
        const descendantPoints = item.find(NUMBERED_ITEM);
        for (var i = 0; i < descendantPoints.length; i++) {
            var currentChild = descendantPoints.eq(i);
            const level = currentChild.parents(NUMBERED_LEVEL_ITEM).length;
            if (level > depth) {
                depth = level;
            }
        }
        if (item.prop("tagName").toLowerCase() != SUBPOINT  &&
            item.prop("tagName").toLowerCase() != SUBPARAGRAPH) {
            depth++;
        }

        return depth;
    }

    function _getIndentLevel(editor) {
        const currentPoint = _getPoint(editor);
        const $points = currentPoint.parents(NUMBERED_LEVEL_ITEM);
        return $points.length;
    }

    function _getCurrentNumValue(editor) {
        const rootElt = $(editor.element.$);
        let pointInHtml = (_isSubpoint(editor) && _isFirstChild(editor)) ? rootElt.parents(NUMBERED_ITEM).first() : rootElt.find(ITEM_HTML).first();
        let currentNum = pointInHtml.children(NUM).first();
        if (currentNum.length) {
            return currentNum.html();
        } else {
            return pointInHtml.attr(DATA_AKN_NUM);
        }
    }

    function _setCurrentNumValue(newValue, editor) {
        const rootElt = $(editor.element.$);
        let pointInHtml = (_isSubpoint(editor) && _isFirstChild(editor)) ? rootElt.parents(NUMBERED_ITEM).first() : rootElt.find(ITEM_HTML).first();
        let currentNum = pointInHtml.children(NUM).first();
        if (currentNum.length) {
            if (typeof newValue === 'undefined') {
                currentNum.html("");
            } else {
                currentNum.html(newValue);
            }
        } else {
            if (typeof newValue === 'undefined') {
                pointInHtml.removeAttr(DATA_AKN_NUM);
            } else {
                pointInHtml.attr(DATA_AKN_NUM, newValue);
            }
        }
    }

    function _isSubpoint(editor) {
        const div = $(editor.element.$);
        if (div.length) {
            return (div.nextAll(ITEMS_SELECTOR).length > 0 && div.nextAll(NUMBERED_ITEM).length == 0) ||
                (div.prevAll(ITEMS_SELECTOR).length > 0 && div.prevAll(NUMBERED_ITEM).length == 0) ||
                (div.parent(NUMBERED_ITEM).length > 0);
        }
        return false;
    }

    function _getPoint(editor) {
        const rootElt = $(editor.element.$);
        return _isSubpoint(editor) && _isFirstChild(editor) ? rootElt.parents(NUMBERED_ITEM).first() : rootElt;
    }

    function _isListDepthMoreThanThreshold(maxLevel) {
        return ((indentationStatus.current.level > maxLevel) || (indentationStatus.current.level == maxLevel + 1 && !indentationStatus.current.numbered));
    }

    function _isFirstChild(editor) {
        return ($(editor.element.$).prevAll(ITEMS_SELECTOR).length == 0 && $(editor.element.$).parent().prop("tagName").toLowerCase() != LEVEL);
    }

    function _isFirstList() {
        return (indentationStatus.current.level == 0);
    }

    pluginTools.addPlugin(pluginName, pluginDefinition);

    // return plugin module
    let pluginModule = {
        name: pluginName
    };

    return pluginModule;
});
