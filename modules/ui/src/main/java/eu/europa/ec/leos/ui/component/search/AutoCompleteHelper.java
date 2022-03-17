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

import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.autocomplete.AutocompleteExtension;
import org.vaadin.olli.ClientStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* This class server as glue to uses Client stroage extension to store and read localstorage in user browser
    and populate the autocomplete extension with the same data*/
class AutoCompleteHelper {
    private static final Logger LOG = LoggerFactory.getLogger(AutoCompleteHelper.class);
    public static final String SEARCHES_STORAGE_KEY = "PAST_SEARCHES";
    public static final String DELIMITER = ";;";
    private ClientStorage clientStorage;
    private ArrayList<String> prevSearches = new ArrayList<>();

    AutoCompleteHelper(TextField search, TextField replace ){
        extendTextField(search);
        initClientStorage();
    }

    void initClientStorage() {
        clientStorage = new ClientStorage(supported -> {
            if (!supported) {
                LOG.error("No localstorage supported. Autocomplete would not work!!");
            }
        });
    }

    ClientStorage getClientStorage(){
        if(clientStorage == null){
            throw new IllegalStateException("Client storage object is not initialized");
        }
        return clientStorage;
    }
    void extendTextField(TextField textField){
        //Apply extension and set suggestion generator
        AutocompleteExtension<String> autoCompleteExtension = new AutocompleteExtension<>(textField);
        autoCompleteExtension.setSuggestionListSize(10);
        autoCompleteExtension.setSuggestionGenerator(this::getSearchSuggestions);
        autoCompleteExtension.addSuggestionSelectListener(selectEvent-> textField.setValue(selectEvent.getSelectedValue()));
        textField.addBlurListener(event -> {
            autoCompleteExtension.hideSuggestions();
            storeSearchInLocalStorage(textField.getValue());
        });
        textField.addFocusListener(event -> autoCompleteExtension.showSuggestions());
    }

    // Suggestion generator function, returns a list of suggestions for a user query
    List<String> getSearchSuggestions(String query, int cap) {
        getPreviousSearchesFromStorage();//CHECK.. this might be async
        return prevSearches.stream().filter(p -> p.toLowerCase()
                .contains(query.toLowerCase()))
                .limit(cap).collect(Collectors.toList());
    }

    void storeSearchInLocalStorage(String value) {
        prevSearches.remove(value);
        prevSearches.add(0, value);
        clientStorage.setLocalItem(SEARCHES_STORAGE_KEY, prevSearches.stream()
                .limit(10)
                .collect(Collectors.joining(DELIMITER)));
        LOG.debug("Value added in local storage :{}", value);
    }

    private void getPreviousSearchesFromStorage(){
        clientStorage.getLocalItem(SEARCHES_STORAGE_KEY, value -> {
            LOG.debug("Value retrieved from local storage :{}", value);
            if(value != null) {
                prevSearches.clear();
                prevSearches.addAll(Arrays.asList(StringUtils.split(value, DELIMITER)));
            }
        });
    }

    public void onAttach() {
        getPreviousSearchesFromStorage();
    }

    public void onDetach() {

    }
}
