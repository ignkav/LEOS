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
package eu.europa.ec.leos.web.ui.component;

import com.google.common.eventbus.EventBus;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.event.view.collection.SearchContextSelectionEvent;

public class SearchContextComponent extends HorizontalLayout {

    private static final long serialVersionUID = 1942600603461274213L;

    private MessageHelper messageHelper;
    private EventBus eventBus;

    private RadioButtonGroup<String> searchContextButtonGroup;
    private Label searchContextLabel;

    public SearchContextComponent(MessageHelper messageHelper, EventBus eventBus) {
        this.messageHelper = messageHelper;
        this.eventBus = eventBus;
        addSearchContextLabel();
        addSearchContextOptions();
    }

    private void addSearchContextLabel(){
        setWidth("100%");
        searchContextLabel = new Label();
        searchContextLabel.setPrimaryStyleName("ui-block-caption");
        searchContextLabel.setValue(messageHelper.getMessage("collaborator.search.context.label"));
        searchContextLabel.setVisible(true);
        addComponent(searchContextLabel);
        setComponentAlignment(searchContextLabel, Alignment.MIDDLE_LEFT);
    }

    private void addSearchContextOptions(){
        String eConsiliumFile = messageHelper.getMessage("collaborator.search.context.econsilium");
        String allFile = messageHelper.getMessage("collaborator.search.context.all");
        searchContextButtonGroup = new RadioButtonGroup<>();
        searchContextButtonGroup.setItems(eConsiliumFile, allFile);
        searchContextButtonGroup.setSelectedItem(eConsiliumFile);
        searchContextButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        searchContextButtonGroup.setVisible(true);
        searchContextButtonGroup.addValueChangeListener(event -> eventBus.post(new SearchContextSelectionEvent(searchContextButtonGroup.getSelectedItem().get())));
        addComponent(searchContextButtonGroup);
        setComponentAlignment(searchContextButtonGroup, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}
