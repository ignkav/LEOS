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
package eu.europa.ec.leos.ui.window.export;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.vaadin.data.Binder;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.themes.ValoTheme;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.RelevantElements;
import eu.europa.ec.leos.ui.event.CreateExportPackageActualVersionRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageCleanVersionRequestEvent;
import eu.europa.ec.leos.ui.event.CreateExportPackageRequestEvent;
import eu.europa.ec.leos.web.ui.window.AbstractWindow;

public abstract class ExportPackageWindow extends AbstractWindow {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ExportPackageWindow.class);

    private static final int TITLE_MAX_LENGTH = 100;

    private Binder<RelevantElementsVO> relevantElementsBinder;
    private Binder<TitleVO> titleBinder;
    private ExportOptions exportOptions;
    private RadioButtonGroup cleanVersionRadioButtonGroup;

    public ExportPackageWindow(MessageHelper messageHelper, EventBus eventBus, ExportOptions exportOptions) {
        super(messageHelper, eventBus, "document.export.package.button.close");
        this.exportOptions = exportOptions;
        setCaption(messageHelper.getMessage("document.export.package.window.title"));
        buildWindow();
    }

    private void buildWindow() {
        setWidth("520px");
        setHeight("250px");

        FormLayout windowLayout = new FormLayout();
        windowLayout.setSizeFull();
        windowLayout.setMargin(false);
        windowLayout.setSpacing(true);
        setBodyComponent(windowLayout);
        buildLayout(windowLayout);

        addButton(buildSendButton());
    }

    private void buildLayout(FormLayout windowLayout) {
        CheckBoxGroup<RelevantElements> relevantElementsCheckBoxGroup = buildRelevantElementsCheckBoxGroup();
        windowLayout.addComponent(relevantElementsCheckBoxGroup);
        TextArea titleArea = buildTitleArea();
        windowLayout.addComponent(titleArea);
        if (exportOptions == null) {
            cleanVersionRadioButtonGroup = buildCleanVersionRadioButtonGroup();
            windowLayout.addComponent(cleanVersionRadioButtonGroup);
        }
    }

    abstract List<RelevantElements> getRelevantElementsOptions();

    abstract RelevantElements getRelevantElementsFromSelected(Set<RelevantElements> relevantElementsSelected);

    private CheckBoxGroup<RelevantElements> buildRelevantElementsCheckBoxGroup() {
        CheckBoxGroup<RelevantElements> relevantElementsCheckBoxGroup = new CheckBoxGroup<>(messageHelper.getMessage("document.export.package.relevant.caption"),
                DataProvider.ofCollection(getRelevantElementsOptions()));
        relevantElementsCheckBoxGroup.setItemCaptionGenerator(element -> messageHelper.getMessage("document.export.package.relevant." + element.toString().toLowerCase() + ".caption"));
        relevantElementsCheckBoxGroup.addValueChangeListener(event -> {
            if (event.isUserOriginated() && event.getValue().contains(RelevantElements.ALL)) {
                if (!event.getOldValue().contains(RelevantElements.ALL)) {
                    for (RelevantElements relevantElement: getRelevantElementsOptions()) {
                        if (!relevantElement.equals(RelevantElements.ANNOTATIONS)) {
                            relevantElementsCheckBoxGroup.deselect(relevantElement);
                        }
                    }
                    relevantElementsCheckBoxGroup.select(RelevantElements.ALL);
                } else if ((!event.getValue().contains(RelevantElements.ANNOTATIONS) && !event.getOldValue().contains(RelevantElements.ANNOTATIONS))
                    || (event.getValue().contains(RelevantElements.ANNOTATIONS) && event.getOldValue().contains(RelevantElements.ANNOTATIONS))){
                    relevantElementsCheckBoxGroup.deselect(RelevantElements.ALL);
                }
            }
        });

        relevantElementsBinder = new Binder<>();
        relevantElementsBinder.forField(relevantElementsCheckBoxGroup).asRequired()
                .bind(RelevantElementsVO::getRelevantElements, RelevantElementsVO::setRelevantElements);
        relevantElementsBinder.setBean(new RelevantElementsVO(EnumSet.of(RelevantElements.ALL)));

        return relevantElementsCheckBoxGroup;
    }

    private TextArea buildTitleArea() {
        TextArea titleArea = new TextArea();
        titleArea.setCaption(messageHelper.getMessage("document.export.package.title.caption"));
        titleArea.setWidth(100, Unit.PERCENTAGE);
        titleArea.setRows(3);
        titleArea.setMaxLength(TITLE_MAX_LENGTH);
        titleArea.setPlaceholder(messageHelper.getMessage("document.export.package.title.placeholder"));
        titleArea.setEnabled(true);
        titleArea.setRequiredIndicatorVisible(true);

        titleBinder = new Binder<>();
        titleBinder.forField(titleArea).asRequired()
                .withValidator(val -> !StringUtils.isBlank(val), "")
                .bind(TitleVO::getTitle, TitleVO::setTitle);
        titleBinder.setBean(new TitleVO(""));

        return titleArea;
    }

    private RadioButtonGroup buildCleanVersionRadioButtonGroup() {
        RadioButtonGroup<Boolean> cleanVersionRadioButtonGroup = new RadioButtonGroup<>(messageHelper.getMessage("document.export.package.clean.version.caption"));
        cleanVersionRadioButtonGroup.setItems(Boolean.FALSE, Boolean.TRUE);
        cleanVersionRadioButtonGroup.setValue(Boolean.FALSE);
        cleanVersionRadioButtonGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        cleanVersionRadioButtonGroup.setDescription(messageHelper.getMessage("document.export.package.clean.version.description"));
        cleanVersionRadioButtonGroup.setItemCaptionGenerator(item -> item ? messageHelper.getMessage("document.export.package.clean.version.option.true")
                    : messageHelper.getMessage("document.export.package.clean.version.option.false"));
        return cleanVersionRadioButtonGroup;
    }

    private Button buildSendButton() {
        Button sendButton = new Button(messageHelper.getMessage("document.export.package.button.send"));
        sendButton.addStyleName("primary");
        sendButton.addClickListener(event -> {
            if (titleBinder.validate().isOk() && relevantElementsBinder.validate().isOk()) {
                String title = titleBinder.getBean().getTitle();
                RelevantElements relevantElements = getRelevantElementsFromSelected(relevantElementsBinder.getBean().getRelevantElements());
                Boolean isWithAnnotations = relevantElementsBinder.getBean().getRelevantElements().contains(RelevantElements.ANNOTATIONS);
                if (exportOptions != null) {
                    exportOptions.setWithFilteredAnnotations(isWithAnnotations);
                    exportOptions.setRelevantElements(relevantElements);
                    eventBus.post(new CreateExportPackageRequestEvent(title, exportOptions));
                } else if (Boolean.parseBoolean(cleanVersionRadioButtonGroup.getValue().toString())) {
                    eventBus.post(new CreateExportPackageCleanVersionRequestEvent(title, relevantElements, isWithAnnotations));
                } else {
                    eventBus.post(new CreateExportPackageActualVersionRequestEvent(title, relevantElements, isWithAnnotations));
                }
                close();
            }
        });
        return sendButton;
    }

    class TitleVO {
        private String title;

        public TitleVO(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    class RelevantElementsVO {
        private Set<RelevantElements> relevantElements;

        public RelevantElementsVO(Set<RelevantElements> relevantElements) {
            this.relevantElements = relevantElements;
        }

        public Set<RelevantElements> getRelevantElements() {
            return this.relevantElements;
        }

        public void setRelevantElements(Set<RelevantElements> relevantElements) {
            this.relevantElements = relevantElements;
        }
    }
}