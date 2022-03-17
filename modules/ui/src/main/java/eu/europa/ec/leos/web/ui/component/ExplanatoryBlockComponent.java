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
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.data.util.GeneratedPropertyContainer;
import com.vaadin.v7.data.util.PropertyValueGenerator;
import com.vaadin.v7.data.util.converter.Converter;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.ui.event.view.collection.DeleteExplanatoryRequest;
import eu.europa.ec.leos.ui.event.view.collection.SaveExplanatoryMetaDataRequest;
import eu.europa.ec.leos.web.event.view.explanatory.OpenExplanatoryEvent;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.component.collaborators.GridWithEditorListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@SpringComponent
@ViewScope
public class ExplanatoryBlockComponent extends CustomComponent {
    private static final long serialVersionUID = 8779532739907751262L;

    private GridWithEditorListener explanatoriesGrid;
    private MessageHelper messageHelper;
    private UserHelper userHelper;
    private EventBus eventBus;
    private ConfigurationHelper cfgHelper;

    enum COLUMN {
        TITLE("title"),
        LANGUAGE("language"),
        UPDATEDON("updatedOn"),
        UPDATEDBY("updatedBy"),
        OPEN("open"),
        ACTION("action");

        private String key;
        private static final String[] keys = Stream.of(values()).map(ExplanatoryBlockComponent.COLUMN::getKey).toArray(String[]::new);

        COLUMN(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public static String[] getKeys() {
            return keys;
        }
    }

    @Autowired
    public ExplanatoryBlockComponent(MessageHelper messageHelper, EventBus eventBus, UserHelper userHelper, ConfigurationHelper cfgHelper) {
        this.messageHelper = messageHelper;
        this.userHelper = userHelper;
        this.eventBus = eventBus;
        this.cfgHelper = cfgHelper;
        initGrid();
    }

    public void populateData(List<DocumentVO> vos) {
        Container container = explanatoriesGrid.getContainerDataSource();
        container.removeAllItems(); // Refresh all
        if (vos != null && !vos.isEmpty()) {
            Comparator<DocumentVO> explanatory = Comparator.<DocumentVO, Date>comparing(o -> o.getUpdatedOn()).reversed();
            List<DocumentVO> sortedExplanatories = new ArrayList<>(vos);
            sortedExplanatories.sort(explanatory);
            sortedExplanatories.forEach(container::addItem);
            explanatoriesGrid.setHeightByRows(vos.size() < 5 ? vos.size() : 5);
        } else {
            explanatoriesGrid.setHeightByRows(1);
        }
    }

    private void initGrid() {
        explanatoriesGrid = new GridWithEditorListener();
        Container.Indexed container = createDataContainer();
        explanatoriesGrid.setContainerDataSource(container);
        explanatoriesGrid.setSelectionMode(com.vaadin.v7.ui.Grid.SelectionMode.NONE);

        explanatoriesGrid.setColumns("title", "language", "updatedOn", "updatedBy", "open", "action");

        com.vaadin.v7.ui.Grid.Column titleColumn = explanatoriesGrid.getColumn(COLUMN.TITLE.getKey()).setExpandRatio(5).setRenderer(new ComponentRenderer());
        com.vaadin.v7.ui.Grid.Column languageColumn = explanatoriesGrid.getColumn(COLUMN.LANGUAGE.getKey()).setExpandRatio(0);
        com.vaadin.v7.ui.Grid.Column updatedOnColumn = explanatoriesGrid.getColumn("updatedOn").setExpandRatio(1);
        com.vaadin.v7.ui.Grid.Column updatedByColumn = explanatoriesGrid.getColumn("updatedBy").setExpandRatio(0).setConverter(new UserDisplayConverter());
        com.vaadin.v7.ui.Grid.Column openColumn = explanatoriesGrid.getColumn(COLUMN.OPEN.getKey()).setExpandRatio(3).setRenderer(new ComponentRenderer());
        com.vaadin.v7.ui.Grid.Column actionColumn = explanatoriesGrid.getColumn(COLUMN.ACTION.getKey()).setExpandRatio(1).setRenderer(new ComponentRenderer());

        com.vaadin.v7.ui.Grid.HeaderRow mainHeader = explanatoriesGrid.getDefaultHeaderRow();
        mainHeader.getCell(titleColumn.getPropertyId()).setHtml(messageHelper.getMessage("collection.block.explanatory.title.caption"));
        mainHeader.getCell(languageColumn.getPropertyId()).setHtml(messageHelper.getMessage("collection.caption.language"));
        mainHeader.getCell(updatedOnColumn.getPropertyId()).setHtml(messageHelper.getMessage("collection.block.explanatory.last.updated.on.caption"));
        mainHeader.getCell(updatedByColumn.getPropertyId()).setHtml(messageHelper.getMessage("collection.block.explanatory.last.updated.by.caption"));
        mainHeader.getCell(openColumn.getPropertyId()).setHtml("");
        mainHeader.getCell(actionColumn.getPropertyId()).setHtml("");

        explanatoriesGrid.setHeightMode(com.vaadin.v7.shared.ui.grid.HeightMode.ROW);
        explanatoriesGrid.setWidth(100, Unit.PERCENTAGE);
        explanatoriesGrid.setResponsive(true);
        setCompositionRoot(explanatoriesGrid);
    }

    private Container.Indexed createDataContainer() {
        // Initialize Containers
        BeanItemContainer dataContainer = new BeanItemContainer<>(DocumentVO.class);
        GeneratedPropertyContainer generatedPropertyContainer = new GeneratedPropertyContainer(dataContainer);
        // Override authority column with a generated property for editing requires a component
        generatedPropertyContainer.addGeneratedProperty(COLUMN.TITLE.getKey(), new PropertyValueGenerator<Component>() {
            @Override
            public Component getValue(Item item, Object itemId, Object propertyId) {
                return createTitleEditor(item, (DocumentVO) itemId, propertyId);
            }

            @Override
            public Class<Component> getType() {
                return Component.class;
            }
        });

        dataContainer.addNestedContainerProperty(COLUMN.LANGUAGE.getKey());
        dataContainer.addNestedContainerProperty("updatedOn");
        dataContainer.addNestedContainerProperty("updatedBy");

        generatedPropertyContainer.addGeneratedProperty(COLUMN.OPEN.getKey(), new PropertyValueGenerator<Component>() {
            @Override
            public Component getValue(Item item, Object itemId, Object propertyId) {
                return createOpenButton(item, (DocumentVO) itemId, propertyId);
            }

            @Override
            public Class<Component> getType() {
                return Component.class;
            }
        });

        generatedPropertyContainer.addGeneratedProperty(COLUMN.ACTION.getKey(), new PropertyValueGenerator<Component>() {
            @Override
            public Component getValue(Item item, Object itemId, Object propertyId) {
                return createDeleteButton((DocumentVO) itemId);
            }

            @Override
            public Class<Component> getType() {
                return Component.class;
            }
        });


        return generatedPropertyContainer;
    }

    private Button createOpenButton(Item item, DocumentVO vo, Object propertyId) {
        Button open = new Button();
        open.setCaption(messageHelper.getMessage("leos.button.open"));
        open.setId(vo.getTitle());

        open.addClickListener(event -> {
            openExplanatory(vo);
        });
        open.setIcon(FontAwesome.ARROW_RIGHT);
        open.addStyleName("open-button");
        return open;
    }

    private EditBoxComponent createTitleEditor(Item item, DocumentVO vo, Object propertyId) {
        EditBoxComponent title = new EditBoxComponent();

        title.setPlaceholder(messageHelper.getMessage("collection.block.explanatory.title.prompt"));
        title.setValue(vo.getTitle());
        title.setDescription(vo.getTitle());
        title.addValueChangeListener(event -> {
            vo.setTitle(event.getValue());
            saveData(vo);
        });
        title.setEnabled(true);
        return title;
    }
    private Button createDeleteButton(DocumentVO vo) {
        Button deleteButton = new Button();
        deleteButton.setPrimaryStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteButton.setIcon(VaadinIcons.MINUS_CIRCLE);
        String deletableTemplate = cfgHelper.getProperty("leos.explanatory.deletable.template");
        if(!vo.getTemplate().equals(deletableTemplate)) {
            deleteButton.setEnabled(false);
        }
        deleteButton.addStyleName("delete-button");
        deleteButton.addClickListener(event -> {
            deleteExplanatory(vo);
        });
        return deleteButton;
    }

    private void openExplanatory(DocumentVO vo) {
        eventBus.post(new OpenExplanatoryEvent(vo));
    }

    private void deleteExplanatory(DocumentVO vo) {
        eventBus.post(new DeleteExplanatoryRequest(vo));
    }

    private void saveData(DocumentVO vo) {
        eventBus.post(new SaveExplanatoryMetaDataRequest(vo));
    }

    public class UserDisplayConverter implements Converter<String, String> {

        @Override
        public String convertToModel(String value, Class<? extends String> targetType, Locale locale) throws ConversionException {
            throw new ConversionException("Not Implemented Method");
        }

        @Override
        public String convertToPresentation(String value, Class<? extends String> targetType, Locale locale) throws ConversionException {
            User user = value != null ? userHelper.getUser(value) : null;
            return (user != null) ? user.getName() : null;
        }

        @Override
        public Class<String> getModelType() {
            return String.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }

}
