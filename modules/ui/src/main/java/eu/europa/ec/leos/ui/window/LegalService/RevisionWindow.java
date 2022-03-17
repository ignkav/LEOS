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
package eu.europa.ec.leos.ui.window.LegalService;

import com.google.common.eventbus.EventBus;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.ui.event.CloneProposalRequestEvent;
import eu.europa.ec.leos.ui.model.MilestonesVO;
import eu.europa.ec.leos.web.model.UserVO;
import eu.europa.ec.leos.web.ui.component.UserSearchComponent;
import eu.europa.ec.leos.web.ui.window.AbstractWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevisionWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(RevisionWindow.class);


    private Label label;
    private UserSearchComponent userSearchComponent;
    private MilestonesVO milestoneVO;
    private UserVO userVO;
    private Button createButton;

    public RevisionWindow(MessageHelper messageHelper, EventBus eventBus, MilestonesVO milestoneVO) {
        super(messageHelper, eventBus);
        this.milestoneVO = milestoneVO;
        setCaption(messageHelper.getMessage("document.share.milestone.window.title"));
        prepareWindow();
    }

    private void prepareWindow() {
        setWidth("620px");
        setHeight("250px");
        addStyleName("shareMilestoneWindow");

        FormLayout windowLayout = new FormLayout();
        windowLayout.setSizeFull();
        windowLayout.setMargin(false);
        windowLayout.setSpacing(true);
        setBodyComponent(windowLayout);
        buildLayout(windowLayout);
        addButton(buildCreateButton());
    }

    private void buildLayout(FormLayout windowLayout) {

        userSearchComponent = getUserEditor();
        userSearchComponent.setCaption(messageHelper.getMessage("document.share.milestone.target.user.caption"));
        userSearchComponent.setWidth(80, Unit.PERCENTAGE);
        windowLayout.addComponent(userSearchComponent);
        userSearchComponent.setEnabled(true);

        label = new Label();
        label.setValue(messageHelper.getMessage("document.share.milestone.label"));
        label.setWidth(100, Unit.PERCENTAGE);
        windowLayout.addComponent(label);
        label.setContentMode(ContentMode.HTML);
        label.addStyleName("sendRevisionLabel");
    }

    private UserSearchComponent getUserEditor() {
        userSearchComponent = new UserSearchComponent(messageHelper, eventBus);
        userSearchComponent.addValueChangeListener(event -> {
            this.userVO = ((UserVO) event.getProperty().getValue());
            enalbeSendButton(userVO);
        });
        return userSearchComponent;
    }

    private Button buildCreateButton() {
        createButton = new Button(messageHelper.getMessage("document.share.milestone.button"));
        createButton.addStyleName("primary");
        createButton.addClickListener(event -> {
            eventBus.post(new CloneProposalRequestEvent(milestoneVO, userVO));
            close();
        });
        enalbeSendButton(userVO);
        return createButton;
    }

    private void enalbeSendButton(UserVO userVO){
        if(userVO != null && userVO.getLogin() != null){
            createButton.setEnabled(true);
        }else{
            createButton.setEnabled(false);
        }
    }

}