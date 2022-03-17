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
define(function searchBarExtensionModule(require) {
    "use strict";

    // load module dependencies
    var CONFIG = require("core/leosConfig");
    var UTILS = require("core/leosUtils");
    var log = require("logger");
    var $ = require("jquery");
    var postal = require("postal");

    // configuration
    var CHANNEL_CFG = CONFIG.channels.document;

    // handle extension initialization
    function _initExtension(connector) {
        log.debug("Initializing search bar extension...");
        // restrict scope to the extended target
        connector.target = UTILS.getParentElement(connector);

        _setupDocumentChannel(connector);
        _setupNavigationButtons(connector);

        connector.onStateChange = _connectorStateChangeListener;
        connector.onUnregister = _connectorUnregistrationListener;
    }

    function _setupDocumentChannel(connector) {
        connector.documentChannel = postal.channel(CHANNEL_CFG.name);
    }

    function _setupNavigationButtons(connector) {
        $('#prevBtn').on('click', _triggerNav.bind(undefined, connector, 'PREV'));
        $('#nextBtn').on('click', _triggerNav.bind(undefined, connector, 'NEXT'));
    }

    function _triggerNav(connector, direction, event) {
        if (!$(event.currentTarget).hasClass("v-disabled")) {
            connector.documentChannel.publish('search.navigate', {
                    direction: direction,
                    state: Object.assign({}, connector.getState(false)),
                    callbackFn: connector.selectionChanged
                });
        }
    }

    // handle connector unregistration from server-side
    function _connectorUnregistrationListener() {
        log.debug("Unregistering Search Bar extension...");
        let connector = this;
        if(connector.documentChannel) {
            connector.documentChannel.publish('search.bar.closed', connector.getState(false));
            connector.documentChannel = null;
        }
    }

    function _connectorStateChangeListener() {
        log.debug("Search Bar extension state changed...");
        let connector = this;
        let state = Object.assign({}, connector.getState());

        //Below piece of code controls the recreations of highlights by sending only update on search FINISH
        //Also delaying events as sometimes search is done for each char typed,
        // we just wait user to end typing before triggering document highlights
        if ((!connector.prevState || (connector.prevState.searchRequestId !== state.searchRequestId))
                && state.searchStatus === 'FINISHED') {

            if(connector.updateTimer) { clearTimeout(connector.updateTimer); }

            let bindFn = (function delayedFn(conenctor, state) {
                connector.documentChannel.publish('search.updated', state);

                //scroll to first
                if(parseInt(state.selectedMatch)<=0) {
                    connector.documentChannel.publish('search.navigate', {
                        direction: "PREV",//if current selection is 0, then going to prev will set it to first
                        state: state,
                        callbackFn: _updateServerForSelectionChange.bind(conenctor)
                    });
                    connector.prevState = state;
                }else if( state.replaceStatus === 'FINISHED' ){
                    connector.documentChannel.publish('search.navigate', {
                        direction: "CURRENT",//if replace is done, then going to current will set it to next match
                        state: state
                    });
                    connector.prevState = state;
                }
            }).bind(undefined, connector, state);

            connector.updateTimer = setTimeout(bindFn, 500);
        }
    }

    //update selection on server side. It would allow buttons to be updated.
    function _updateServerForSelectionChange(data){
        let connector = this;
        try {
            connector.selectionChanged(data);
        }catch(err){
            log.error(`Error while calling server for selection change..${err}`);
        }
    }

    function _searchBarClosed() {
        log.debug("Search bar closed called...");
        const connector = this;
        if(connector && connector.documentChannel) {
            connector.documentChannel.publish('search.bar.closed', connector.getState(false));
        }
    }

    return {
        init: _initExtension
    };
});
