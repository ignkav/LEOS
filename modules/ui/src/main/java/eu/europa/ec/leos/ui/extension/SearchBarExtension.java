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
package eu.europa.ec.leos.ui.extension;

import com.google.common.eventbus.EventBus;
import com.vaadin.annotations.JavaScript;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonObject;
import eu.europa.ec.leos.ui.event.search.SeachSelectionChanedEvent;
import eu.europa.ec.leos.web.support.LeosCacheToken;

import java.util.Date;

@JavaScript({"vaadin://../js/ui/extension/searchBarConnector.js" + LeosCacheToken.TOKEN })
public class SearchBarExtension extends LeosJavaScriptExtension {

    private static final long serialVersionUID = 1L;
    private EventBus eventBus;

    public SearchBarExtension(AbstractClientConnector target, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        registerServerSideAPI();
        extend(target);
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        eventBus.unregister(this);
        super.detach();
    }

    @Override
    protected SearchBarState getState() {
        return (SearchBarState) super.getState();
    }

    @Override
    protected SearchBarState getState(boolean markAsDirty) {
        return (SearchBarState) super.getState(markAsDirty);
    }

    public void updateClient(SearchBarState state) {
        SearchBarState clientState = getState(false);

        clientState.searchRequestId = state.searchRequestId;
        clientState.searchStatus = state.searchStatus;
        clientState.matches = state.matches;
        clientState.selectedMatch = state.selectedMatch;

        clientState.wholeWords = state.wholeWords;
        clientState.matchCase = state.matchCase;
        clientState.searchText = state.searchText;

        clientState.replaceText = state.replaceText;
        clientState.replaceStatus = state.replaceStatus;

        getState(true).dirtyTimestamp = new Date().getTime();
    }

    private void registerServerSideAPI() {
        addFunction("selectionChanged", (JavaScriptFunction) arguments -> {
            LOG.trace("Selection Changed...");
            JsonObject data = arguments.get(0);
            try {
                int selectedMatch = Integer.parseInt(data.getString("selectedMatch"));
                LOG.debug("Selection Changed... {}", selectedMatch);
                if(selectedMatch >= 0) {
                    eventBus.post(new SeachSelectionChanedEvent(selectedMatch));
                }
            } catch (Exception ex) {
                LOG.error("Exception when changing selection element!", ex);
            }
        });
    }
}
