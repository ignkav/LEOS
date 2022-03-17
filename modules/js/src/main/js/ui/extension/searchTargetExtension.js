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
define(function searchTargetExtensionModule(require) {
    "use strict";

    // load module dependencies
    var CONFIG = require("core/leosConfig");
    var UTILS = require("core/leosUtils");
    var log = require("logger");
    var $ = require("jquery");
    var postal = require("postal");
    var RangeObject = require("range");

    var sliderPins = require("sliderPins");
    var contentScroller = require("contentScroller");

    // configuration
    var DOCUMENT_CHANNEL_CFG = CONFIG.channels.document;

    // handle extension initialization
    function _initExtension(connector) {
        log.debug("Initializing Search Target extension...");

        _setupDocumentChannel(connector);
        _registerDocumentChannelSubscriptions(connector);

        connector.onUnregister = _connectorUnregistrationListener;

        //Slider pin initiation
        connector.target = UTILS.getParentElement(connector);
        connector.pinConfigMap = _getSelectorStyleMap();
        _updateParentPosition(connector.target);

        connector.highlights = [];
    }

    function _registerDocumentChannelSubscriptions(connector) {
        connector.searchSubscriptions = [];

        if (connector.documentChannel) {
            connector.searchSubscriptions.push(connector.documentChannel.subscribe("search.updated", _searchUpdated.bind(undefined, connector)));
            connector.searchSubscriptions.push(connector.documentChannel.subscribe("search.navigate", _navigate.bind(undefined, connector)));
            connector.searchSubscriptions.push(connector.documentChannel.subscribe("search.bar.closed", _searchBarClosed.bind(undefined, connector)));
        }
    }

    function _searchUpdated(connector, state) {
        log.debug(`Search Target extension search update: ${state.searchRequestId}, status: ${state.searchStatus}, matches :${state.matches.length}...`);

        if (connector.highlights.length > 0) {
            _removeHighlights(connector);
        }

        if (state.searchStatus === 'FINISHED') {
            //Cancel any previous timer
            if (connector.pinTimer) clearTimeout(connector.pinTimer);

            sliderPins.destroy(connector.target);

            _createHighlights(connector, state);
            //  delay execution to avoid issues with target update
            connector.pinTimer = setTimeout(sliderPins.create, 1000, connector.target, connector.pinConfigMap);
        }
    }

    function _searchBarClosed(connector, state) {
        if (connector.highlights.length > 0) {
            _removeHighlights(connector);
            sliderPins.destroy(connector.target);
        }
    }

    function _createHighlights(connector, state) {
        log.debug("Creating search highlights...");

        //Find matches
        state.matches.forEach((match, index) => {
            try {
                const white = /^\s*$/;
                let normalizedRange = getRangeFromMatch(match, connector);
                if (normalizedRange) {
                    let $nodes = $(normalizedRange.textNodes())
                        .filter(function (i) {
                            return !white.test(this.nodeValue);
                        });

                    let hl = $("<search-highlight class='highlight'></search-highlight>");
                    hl.addClass(match.replaceable ? "replaceable" : "non-replaceable");

                    let $highlightTags = $nodes.wrap(hl).parent();
                    $highlightTags
                        .first()
                        .addClass("primary")
                        .attr("data-match-index", index)[0]
                        .match = match;

                    connector.highlights = connector.highlights.concat($highlightTags.toArray());
                }
            } catch (err) {
                log.error(`Failed to highlight match: ${match}`);
            }
        });
    }

    /* This function returns a normalized range from a match
        It irst tries to locate the starting node and ending node in DOM and create a DOM range.
        this DOM range is later converted to normalized range(by annotator library).
        Normalized ranges allow to extract text nodes from range easily.
        We need text nodes to wrap them in custom elements*/
    function getRangeFromMatch(match, connector) {
        let $rootElement = $(connector.target);

        const startNode = $rootElement.find(`#${match.matchedElements[0].elementId}`)[0];
        var startOffset = match.matchedElements[0].matchStartIndex;

        const endMatchedNode = match.matchedElements[match.matchedElements.length - 1];
        const endNode = $rootElement.find(`#${endMatchedNode.elementId}`)[0];
        var endOffset = endMatchedNode.matchEndIndex;

        if (startNode && endNode) {
            try {
                let s = getOffsetContainingNode(startNode, startOffset);
                // in cases like [textNode]^[NextNode], we should select NextNode if a sibling is present.
                if (s.n.textContent.length === s.o && s.n.nextSibling) {
                    s.n = s.n.nextSibling;
                    s.o = 0;
                }

                let e = getOffsetContainingNode(endNode, endOffset);
                const range = document.createRange();

                range.setStart(s.n, s.o);
                range.setEnd(e.n, e.o);

                return RangeObject.sniff(range).normalize();
            } catch (err) {
                console.error(`Error while constructing range: offset Node ${err.getErrorMessage()} for ${match}`);
                throw err;
            }
        } else {
            console.error(`start node ${match.matchedElements[0].elementId}:${startNode} or end node ${endMatchedNode.elementId}:${endNode}  not found!! `)
        }
        return null;
    }

    function getOffsetContainingNode(element, offset) {
        if (offset > element.textContent.length) {
            throw new Error("offset outside Text length");
        }
        return traverse(element, offset);
    }

    //This method use text/char counting to find innermost node where offset lies
    function traverse(element, offset) {
        if(element.nodeName.toUpperCase() ==='AUTHORIALNOTE'){
            element = element.nextSibling;
        }
        if (offset > element.textContent.length) {//offset outside this element. check next sibling
            offset = offset - element.textContent.length;
            if(element.nextSibling) {
                return traverse(element.nextSibling, offset);
            } else{
                log.error(`It should never come here ${element}`);
            }
        } else if (offset <= element.textContent.length) {//offset lies here.find more precise child
            if (element.nodeType === Node.TEXT_NODE) {
                return {n: element, o: offset}
            }
            if (element.hasChildNodes()) {
                return traverse(element.firstChild, offset)
            }
        }
    }

    function _removeHighlights(connector) {
        var highlights = connector.highlights || [];
        log.debug(`removing search highlights... ${highlights.length}`);

        let results = [];
        try {
            for (let j = 0, len = highlights.length; j < len; j++) {
                let h = highlights[j];
                let highlightParent = h.parentNode;
                if (highlightParent != null) {
                    results.push($(h).replaceWith(function () {
                        return $(this).contents();
                    }));
                    highlightParent.normalize();//join splitted strings if possible
                }
            }
        } catch (err) {
            console.error(`Error while removing highlights ${err}`)
        }

        connector.highlights = [];
        return highlights;
    }

    function _updateParentPosition(target) {
        target.parentNode.style.position = "relative";
    }

    // handle connector unregistration from server-side
    function _connectorUnregistrationListener() {
        log.debug("Unregistering Search Target extension...");
        var connector = this;
        sliderPins.destroy(connector.target);

        _teardownDocumentChannel(connector);

        // clean connector
        connector.target = null;
        connector.highlights = null;
    }

    function _setupDocumentChannel(connector) {
        connector.documentChannel = postal.channel(DOCUMENT_CHANNEL_CFG.name);
    }

    function _teardownDocumentChannel(connector) {
        // clear  channel
        if (connector.searchSubscriptions) {
            connector.searchSubscriptions.forEach(function (subscription) {
                subscription.unsubscribe();
            });
            connector.searchSubscriptions = null;
            connector.documentChannel = null
        }
    }

    function _getSelectorStyleMap() {
        var pinConfigMap = {'search-highlight.primary': 'pin-search-highlight'};
        return pinConfigMap;
    }

    /* this method get the server side selection and moves to next selection
        This could be done stateless way but that provides several compilcations.
     */
    function _navigate(connector, data) {
        let direction = data.direction;
        let state = data.state;

        let selectionOldIndex = parseInt(state.selectedMatch);
        let selectionNewIndex = direction === "PREV"
            ? (selectionOldIndex <= 0
                ? 0
                : selectionOldIndex - 1)
            : (direction === "CURRENT" ? selectionOldIndex : selectionOldIndex + 1);

        log.debug(`Search Target extension navigate from ${selectionOldIndex} to ${selectionNewIndex}`);

        let _primaryHighlights = connector.highlights.filter((hl) => hl.classList.contains("primary"));
        _primaryHighlights = direction === "PREV" ? _primaryHighlights.reverse() : _primaryHighlights;    //reversing the array in case of previous to traverse pins from bottom to top
        let selectedMatchNew = _primaryHighlights.find(hl => hl.dataset.matchIndex == selectionNewIndex);
        let selectedMatchOld = _primaryHighlights.find(hl => hl.dataset.matchIndex == selectionOldIndex)

        if (selectedMatchNew) {
            contentScroller.scrollTo(selectedMatchNew, connector.target, function (el) {
                connector.highlights.forEach((hl) => hl.classList.remove("selected-match"));
                el.classList.add("selected-match")
                if (data.callbackFn) {
                    data.callbackFn.call(null, {selectedMatch: el.dataset.matchIndex});
                }
            }.bind(this), false);
        }
    }

    return {
        init: _initExtension
    };
});
