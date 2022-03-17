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
package eu.europa.ec.leos.ui.component.search;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.data.Binder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import com.vaadin.ui.declarative.Design;
import eu.europa.ec.leos.domain.vo.ElementMatchVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.event.search.ReplaceMatchRequestEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceMatchResponseEvent;
import eu.europa.ec.leos.ui.event.search.SaveAfterReplaceEvent;
import eu.europa.ec.leos.ui.event.search.SaveAndCloseAfterReplaceEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceAllMatchRequestEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceAllMatchResponseEvent;
import eu.europa.ec.leos.ui.event.search.SeachSelectionChanedEvent;
import eu.europa.ec.leos.ui.event.search.SearchBarClosedEvent;
import eu.europa.ec.leos.ui.event.search.ShowConfirmDialogEvent;
import eu.europa.ec.leos.ui.event.search.SearchTextRequestEvent;
import eu.europa.ec.leos.ui.event.search.SearchTextResponseEvent;
import eu.europa.ec.leos.ui.extension.SearchBarExtension;
import eu.europa.ec.leos.ui.extension.SearchBarState;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.Collections;

@DesignRoot("SearchBarDesign.html")
public class SearchBar extends VerticalLayout {

    final static String SUCCESS_STYLE = "success";
    final static String FAILURE_STYLE = "failure";
    private static final long serialVersionUID = -731894290911926353L;
    private static final Logger LOG = LoggerFactory.getLogger(SearchBar.class);
    public static final String INPROGRESS = "INPROGRESS";
    public static final String FINISHED = "FINISHED";
    public static final String SKIPPED = "SKIPPED";
    public static final String SAVED = "SAVED";

    protected TextField searchBox;
    protected Button prevBtn;
    protected Button nextBtn;
    protected Button closeBtn;
    protected CheckBox matchCase;
    protected CheckBox wholeWords;
    protected HorizontalLayout replaceBar;
    protected Button showReplaceBarBtn;
    protected TextField replaceBox;

    protected Button replaceBtn;
    protected Button replaceAllBtn;
    protected Button saveBtn;
    protected Button saveAndCloseBtn;

    private final MessageHelper messageHelper;
    private final EventBus eventBus;
    private Binder<SearchBarState> binder;
    private final AutoCompleteHelper autoCompleteHelper;
    private MessageOverlayExtension searchBoxMessage;
    private final SearchBarState state;
    private final SearchBarExtension searchBarExtension;

    public SearchBar(MessageHelper messageHelper, EventBus eventBus) {
        LOG.trace("Initializing search component..");
        Validate.notNull(messageHelper, "MessageHelper must not be null!");
        this.messageHelper = messageHelper;
        Validate.notNull(eventBus, "EventBus must not be null!");
        this.eventBus = eventBus;
        state = new SearchBarState();

        Design.read(this);

        initSearchLayout();
        initReplaceLayout();

        //To send date to client side
        searchBarExtension = new SearchBarExtension(this, eventBus);
        //To handle autocomplete of search box
        autoCompleteHelper = new AutoCompleteHelper(searchBox, replaceBox);
        //To create a overlay on textbox
        searchBoxMessage = new MessageOverlayExtension(searchBox);

        this.addExtension(autoCompleteHelper.getClientStorage());
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
        autoCompleteHelper.onAttach();
        searchBox.focus();
    }

    @Override
    public void detach() {
        resetSearch();
        super.detach();
        autoCompleteHelper.onDetach();
        eventBus.unregister(this);
    }

    private void initSearchLayout() {
        initSearchOptions();
        initCancelButton();
        initReplaceBarButton();
    }

    private void initSearchOptions() {
        searchBox.setCaptionAsHtml(true);
        searchBox.setCaption(messageHelper.getMessage("document.search.caption"));
        searchBox.setValueChangeMode(ValueChangeMode.LAZY);
        searchBox.setValueChangeTimeout(700);

        binder = new Binder<>();
        String regex = "([^'\"]*'[^\"]*)|(\"[^']*)|([^'\"]*)"; //regex allows single quote or double quote but not both.
        binder.forField(searchBox)
                //.asRequired(messageHelper.getMessage("document.search.required.error"))
                .withValidator(new RegexpValidator(messageHelper.getMessage("document.search.invalid.error"), regex))
                .bind(SearchBarState::getSearchText, SearchBarState::setSearchText);

        searchBox.addValueChangeListener(event -> {
            if (binder.validate().isOk()) {
                if (StringUtils.isNotEmpty(state.getSearchText())) {
                    triggerSearch();
                } else {
                    resetSearch();
                }
            }
        });

        matchCase.setCaption(messageHelper.getMessage("document.search.caption.match.case"));
        binder.forField(matchCase).bind(SearchBarState::isMatchCase, SearchBarState::setMatchCase);
        matchCase.addValueChangeListener(event -> {
            if (StringUtils.isNotEmpty(state.getSearchText()) && binder.validate().isOk()) {
                triggerSearch();
            }
        });


        wholeWords.setCaption(messageHelper.getMessage("document.search.caption.words"));
        binder.forField(wholeWords).bind(SearchBarState::isWholeWords, SearchBarState::setWholeWords);

        wholeWords.addValueChangeListener(event -> {
            if (StringUtils.isNotEmpty(state.getSearchText()) && binder.validate().isOk()) {
                triggerSearch();
            }
        });

        binder.setBean(state);
    }

    private void initReplaceButton() {
        // create replace button
        replaceBtn.setCaption(messageHelper.getMessage("document.replace.button"));
        replaceBtn.setDescription(messageHelper.getMessage("document.replace.button"));

        replaceBtn.addClickListener(event -> {
            if (binder.validate().isOk()) {
                eventBus.post(new ReplaceMatchRequestEvent(
                    state.getSearchRequestId(),
                    state.getSearchText(),
                    state.getReplaceText() == null? "" : state.getReplaceText(),
                    state.getMatches().get(state.getSelectedMatch()),
                    state.getSelectedMatch()));
            }
        });
    }

    private void initReplaceAllButton() {
        // create replace all button
        replaceAllBtn.setCaption(messageHelper.getMessage("document.replace.all.button"));
        replaceAllBtn.setDescription(messageHelper.getMessage("document.replace.all.button"));

        replaceAllBtn.addClickListener(event -> {
            if (binder.validate().isOk()) {
                eventBus.post(new ReplaceAllMatchRequestEvent(
                    state.getSearchText(),
                    state.getReplaceText() == null ? "" : state.getReplaceText(),
                    state.getMatches()
                ));
            }
        });
    }

    private void initCancelButton() {
        closeBtn.setCaption(messageHelper.getMessage("document.cancel.button"));
        closeBtn.setDescription(messageHelper.getMessage("document.cancel.button.description"));
        closeBtn.addClickListener(e ->
        {
            if(state.getReplaceStatus().equals(FINISHED)) {
                //ask for confirmation
                showConfirmDialog(new ShowConfirmDialogEvent());
            }else {
                cancelSearch();
            }
        });
    }

    private void initReplaceLayout() {
        replaceBox.setCaption(messageHelper.getMessage("document.replace.caption"));
        binder.forField(replaceBox)
                //.asRequired(messageHelper.getMessage("document.replace.required.error"))
                .bind(SearchBarState::getReplaceText, SearchBarState::setReplaceText);
        replaceBox.addValueChangeListener(event -> {
            if (event.isUserOriginated() && binder.validate().isOk()) {
                    updateUI();
            }
        });
        initReplaceButton();
        initReplaceAllButton();
        initSaveNCloseButton();
        initSaveButton();
    }

    private void initSaveButton() {
        saveBtn.setCaption(messageHelper.getMessage("leos.button.save"));
        saveBtn.setDescription(messageHelper.getMessage("leos.button.save"));
        saveBtn.addClickListener(event -> {
            eventBus.post(new SaveAfterReplaceEvent());
            state.setReplaceStatus(SAVED);
            updateUI();
        });
    }

    private void initSaveNCloseButton() {
        saveAndCloseBtn.setCaption(messageHelper.getMessage("leos.button.save.and.close"));
        saveAndCloseBtn.setDescription(messageHelper.getMessage("leos.button.save.and.close"));
        saveAndCloseBtn.addClickListener(event -> {
            eventBus.post(new SaveAndCloseAfterReplaceEvent());
            eventBus.post(new SearchBarClosedEvent());
        });
    }

    private void resetSearch() {
        state.resetSearchState();
        searchBox.clear();
        replaceBox.clear();
        searchBoxMessage.setMessage("");
        clearSearchBoxError();
        updateUI();
    }

    private void clearSearchBoxError() {
        searchBox.setComponentError(null);
        replaceBox.setComponentError(null);
        searchBox.removeStyleNames(SUCCESS_STYLE, FAILURE_STYLE);
    }

    public void showReplaceBar(boolean visible) {
        replaceBar.setVisible(visible);
        showReplaceBarBtn.removeStyleNames("active", "inactive");
        showReplaceBarBtn.addStyleName(visible ? "active" : "inactive");
    }

    private void initReplaceBarButton() {
        showReplaceBarBtn.setDescription(messageHelper.getMessage("document.replace.caption.show.bar"));
        showReplaceBarBtn.addClickListener(event -> {
            showReplaceBar(!replaceBar.isVisible());
        });
    }

    public boolean isReplaceBarVisible() {
        return replaceBar.isVisible();
    }

    //search state..

    private void triggerSearch() {
        state.newSearch();
        updateUI();
        eventBus.post(new SearchTextRequestEvent(state.getSearchRequestId(), state.getSearchText(), state.isMatchCase(), state.isWholeWords()));
    }

    @Subscribe
    private void replaceAllFinished(ReplaceAllMatchResponseEvent result){
        //make a copy of matches and selectedMatch
        List<SearchMatchVO> matchesNonReplaceable = state.getMatches().stream().filter(s -> !s.isReplaceable()).collect(
            Collectors.toList());

        state.newSearch();

        if(matchesNonReplaceable.size() > 0) {
            state.setSelectedMatch(0);
            state.setMatches(matchesNonReplaceable);
        }

        state.setReplaceStatus(FINISHED);
        state.setSearchStatus(FINISHED);

        updateUI();
    }
    /**
     * This method should be called after a successful replace has been performed
     * Here the matches info will be modified to reflect the new start and end indexes
     * @param result replace done event
     */
    @Subscribe
    private void replaceFinished(ReplaceMatchResponseEvent result){
        if (!state.getSearchRequestId().equals(result.getSearchId())) {
            return;// it means there was a new request fired when previous was in progress
        }

        int selectedMatch = state.getSelectedMatch();

        //removed the replaced search text match
        SearchMatchVO sVO;

        if(result.isReplaced()) {
            sVO = state.getMatches().remove(state.getSelectedMatch());
        }else {
            sVO = state.getMatches().get(state.getSelectedMatch());
            selectedMatch++;
        }
        //make a copy of matches and selectedMatch
        List<SearchMatchVO> matches = new ArrayList<>(state.getMatches());

        //initialise the search matches info
        state.newSearch();

        int matchesRemaining = matches.size();

        //for empty string, it can be returned as null
        String replaceText = state.getReplaceText() == null ? "" : state.getReplaceText();

        //the difference between replace text and search text gives us how many positions the remaining text has moved
        //if replace text is longer then change value will be positive meaning the matches in the current elements
        int change = replaceText.length() - state.getSearchText().length();

        //update positions of elements that have replaced texts
        List<String> elementIdsReplaced = sVO.getMatchedElements().stream().filter(ElementMatchVO::isEditable).map(ElementMatchVO::getElementId).collect(
                Collectors.toList());

        for (int i = selectedMatch; i < matchesRemaining; i++) {
            SearchMatchVO matchesSVO = matches.get(i);
            matchesSVO.getMatchedElements().stream()
                    .filter(matchedElement -> elementIdsReplaced.contains(matchedElement.getElementId()))
                    .filter(matchedElement -> matchedElement.getMatchStartIndex() > 0)
                    .forEach(matchedElement -> {
                        matchedElement
                                .setMatchStartIndex(matchedElement.getMatchStartIndex() + change);
                        matchedElement
                                .setMatchEndIndex(matchedElement.getMatchEndIndex() + change);
                    });
        }

        state.setMatches(matches);

        //preserve the selected match. The value should remain same before replace unless it is the last one. In that case, it should be reset to 0
        if (selectedMatch >= matchesRemaining) {
            state.setSelectedMatch(0);
        } else {
            state.setSelectedMatch(selectedMatch);
        }

        state.setSearchStatus(FINISHED);
        if(result.isReplaced()) {
            state.setReplaceStatus(FINISHED);
        }else{
            state.setReplaceStatus(SKIPPED);
        }
        updateUI();
    }

    @Subscribe
    private void searchFinished(SearchTextResponseEvent result) {
        if (!state.getSearchRequestId().equals(result.getSearchId())) {
            return;// it means there was a new request fired when previous was in progress
        }

        state.setMatches((result.getMatches().size() > 0) ? result.getMatches() : Collections.emptyList());
        state.setSearchStatus(FINISHED);
        updateUI();
    }

    // On update of state, all buttons are controlled by this method
    public void updateUI() {
        //Start of Search
        LOG.debug("Buttons Updated");
        if (state.getSearchStatus().equals(INPROGRESS)) {
            disableAllButtons();
            searchBoxMessage.setMessage("");
        }
        //End of Search
        else if (state.getSearchStatus().equals(FINISHED)) {
            if (state.getMatches().size() > 0) {
                searchBox.removeStyleName(FAILURE_STYLE);
                searchBox.addStyleName(SUCCESS_STYLE);
                searchBoxMessage.setMessage(String.format("%d of %d", 1 + state.getSelectedMatch(), state.getMatches().size()));

                //If something is selected and something to replace
                if (state.getSelectedMatch() >= 0) {
                    if(state.getMatches().get(state.getSelectedMatch()).isReplaceable()){
                        replaceBtn.setEnabled(true);
                    }
                    replaceAllBtn.setEnabled(true);
                }
                if (state.getSelectedMatch() <= 0) {
                    prevBtn.setEnabled(false);
                } else {
                    prevBtn.setEnabled(true);
                }
                if (state.getSelectedMatch() >= state.getTotalMatch() - 1) {
                    nextBtn.setEnabled(false);
                } else {
                    nextBtn.setEnabled(true);
                }
                if (state.getSelectedMatch() > 0 && state.getSelectedMatch() < state.getTotalMatch() - 1) {
                    prevBtn.setEnabled(true);
                    nextBtn.setEnabled(true);
                }
            } else {
                searchBox.removeStyleName(SUCCESS_STYLE);
                searchBox.addStyleName(FAILURE_STYLE);
                searchBoxMessage.setMessage(messageHelper.getMessage("document.search.message.no.results"));
                disableAllButtons();
            }
        }


        if (state.getReplaceStatus().equals(FINISHED)){
                saveBtn.setEnabled(true);
                saveAndCloseBtn.setEnabled(true);
        }else{
            saveBtn.setEnabled(false);
            saveAndCloseBtn.setEnabled(false);
        }
        searchBarExtension.updateClient(state);
    }

    // this would come from client side operations
    @Subscribe
    void selectionUpdated(SeachSelectionChanedEvent seachSelectionChanedEvent){
        state.setSelectedMatch(seachSelectionChanedEvent.getSelectedMatch());
        updateUI();
    }

    void disableAllButtons() {
        nextBtn.setEnabled(false);
        prevBtn.setEnabled(false);
        replaceBtn.setEnabled(false);
        replaceAllBtn.setEnabled(false);
        saveBtn.setEnabled(false);
        saveAndCloseBtn.setEnabled(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if(!enabled) {
            cancelSearch();
        }
    }

    private void cancelSearch() {
        if(!state.getReplaceStatus().equals(FINISHED)) {
            resetSearch();
            eventBus.post(new SearchBarClosedEvent());
        }
    }

    @Subscribe
    public void showConfirmDialogEventHandler(ShowConfirmDialogEvent event){
        showConfirmDialog(event);
    }

    private void showConfirmDialog(ShowConfirmDialogEvent event) {
        ConfirmDialog.show(getUI(),
                messageHelper.getMessage("document.search.cancel.title"),
                messageHelper.getMessage("document.search.cancel.message"),
                messageHelper.getMessage("document.search.cancel.confirm"),
                messageHelper.getMessage("leos.button.cancel"),
                new ConfirmDialog.Listener() {
                    private static final long serialVersionUID = -1441968814274639475L;

                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            resetSearch();
                            eventBus.post(new SearchBarClosedEvent());
                            if(event.getPostConfirmEvent() != null){
                                eventBus.post(event.getPostConfirmEvent());
                            }
                        } else if (dialog.isCanceled()) {
                            if(event.getPostCancelEvent() != null){
                                eventBus.post(event.getPostCancelEvent());
                            }
                        }
                    }
                });
    }

    public void barClosed() {
    //Do nothing as of now
    }
}
