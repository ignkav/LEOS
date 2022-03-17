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
define(function leosInlineEditorPluginModule(require) {
    "use strict";
    /*
     * This plugin should be the first one to be loaded.
     */
    
    // load module dependencies
    var pluginTools = require("plugins/pluginTools");
    var pluginName = "leosInlineEditor";

    var pluginDefinition = {

        init : function init(editor) {

            var contentHeight = _getContentHeight(editor.element);
            var _mousePosition = [];
            var docContainer = document.getElementById("docContainer");
            docContainer.addEventListener('mousedown', _onMouseDown);

            editor.on("change", function(event) {
                var newContentHeight = _getContentHeight(event.editor.element);
                if(contentHeight != newContentHeight) {
                    event.editor.fire("contentChange");
                    contentHeight = newContentHeight;
                }
            });

            function _getContentHeight(element) {
            	if (element) {
            		var editorElem = element.$;
            		return editorElem.getBoundingClientRect().height;
            	} else {
            		return 0;
            	}
            }

            editor.on('blur', function (evt) {
                if (evt.editor.LEOS.implicitSaveEnabled && !evt.editor.LEOS.bookmarkNavigatorClicked) {
                	if (_isMouseOutsideEditor(evt.editor.container.$) && !_isMouseOnDocumentScrollbar()) {
                        if (evt.editor.checkDirty()) {
                            var isSaved = editor.fire("save", {
                                data: editor.getData()
                            });

                            if (isSaved) {
                                editor.fire("close");
                            }
                        }
                        else {
                            _paddParent(evt.editor.element);
                            editor.fire("close");
                        }
                    }
                } else {
                    evt.editor.LEOS.bookmarkNavigatorClicked = false;
                }
            });

            function _isMouseOutsideEditor(element) {
                var positionEditor = _getElementPosition(element);
                var top = positionEditor[1];
                var bottom = top + parseInt(window.getComputedStyle(element, null).height);
                var left = positionEditor[0];
                var right = left + parseInt(window.getComputedStyle(element, null).width);

                var mouseX = _mousePosition[0];
                var mouseY = _mousePosition[1];

    			return mouseX <= left || mouseX >= right || mouseY <= top || mouseY >= bottom;
			}

            function _isMouseOnDocumentScrollbar() {
                var positionEditor = _getElementPosition(docContainer);
                var bottom = positionEditor[1] + parseInt(window.getComputedStyle(docContainer, null).height) - 20;
                var right = positionEditor[0] + parseInt(window.getComputedStyle(docContainer, null).width) - 20;

                var mouseX = _mousePosition[0] - docContainer.scrollLeft;
                var mouseY = _mousePosition[1] - docContainer.scrollTop;

                return mouseX >= right || mouseY >= bottom;
            }

            function _getElementPosition(element) {
                var x = 0; 
                var y = 0;
                do {
                    x += element.offsetLeft;
                    y += element.offsetTop;
                    element = element.offsetParent
                } while (element);
                return [x,y];
            }

            function _onMouseDown(event) {
                var posx = 0;
                var posy = 0;
                if (event.pageX || event.pageY) {
                    posx = event.pageX + docContainer.scrollLeft;
                    posy = event.pageY + docContainer.scrollTop;
                }
                _mousePosition = [posx, posy];
			}

            editor.on('focus', function(evt) {
                if(evt.editor.element.getParent().getChild(0).equals(evt.editor.element)
                    && evt.editor.element.getParent().getName() != 'list') {
                    _paddParent(evt.editor.element);
                } else {
                    _paddElement(evt.editor.element);
                }
                if (evt.editor.LEOS.elementType === "num" && evt.editor.LEOS.type === "annex") {
                    $(document.getElementsByClassName('cke_top')).css("margin-left", "-89px");
                }
            });

            editor.on('close', function(evt) {
                var placeholder = evt.editor.placeholder;
                _resetPaddingNumElement(placeholder);
                docContainer.removeEventListener('mousedown', _onMouseDown);
            });

            function _paddElement(element) {
                let ICON_COUNT_THRESHOLD_SECOND_LINE = 19;
                let iconCount = _getIconsCount(editor);
                if (iconCount <= ICON_COUNT_THRESHOLD_SECOND_LINE) {
                    element.removeClass("leos-editor-focus-double");
                    element.addClass("leos-editor-focus");
                } else {
                    element.removeClass("leos-editor-focus");
                    element.addClass("leos-editor-focus-double");
                    _paddNumElement(element);
                }
            }

            function _paddParent(element) {
                let ICON_COUNT_THRESHOLD_SECOND_LINE = 19;
                let EDITOR_MARGIN_OFFSET = 40;
                let iconCount = _getIconsCount(editor);
                if (iconCount <= ICON_COUNT_THRESHOLD_SECOND_LINE) {
                    element.getParent().setStyle('padding-top', EDITOR_MARGIN_OFFSET + 'px');
                } else {
                    element.getParent().setStyle('padding-top', 2 * EDITOR_MARGIN_OFFSET + 'px');
                }
            }

            function _getIconsCount(editor) {
                let icons = 0;
                editor.toolbar.forEach(function (data) {
                    if (data.items) {
                        icons += data.items.length;
                    }
                });
                return icons;
            }

            function _paddNumElement(element) {
                if (!element.getParent().is("article") && (element.getPrevious() instanceof CKEDITOR.dom.element) &&
                        element.getPrevious().is("num")) {
                    element.getPrevious().setStyle("padding-top", "66pt");
                }
            }

            function _resetPaddingNumElement(placeholder) {
                if (!$(placeholder).parent().is("article") && $(placeholder).prev().is("num")) {
                    $(placeholder).prev().css({
                        "padding-top": "6pt"
                    });
                }
            }
        }
    };

    pluginTools.addPlugin(pluginName, pluginDefinition);

    // return plugin module
    var pluginModule = {
        name : pluginName
    };

    return pluginModule;
});