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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import com.vaadin.ui.UI;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.ui.window.export.LegalTextExportPackageWindow;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.ui.window.export.ExportPackageWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.event.DragStartListener;

import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.support.TableOfContentHelper;
import eu.europa.ec.leos.services.support.xml.XmlHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.event.StateChangeEvent;
import eu.europa.ec.leos.ui.event.toc.DisableEditTocEvent;
import eu.europa.ec.leos.ui.event.toc.ExpandTocSliderPanel;
import eu.europa.ec.leos.ui.event.toc.InlineTocCloseRequestEvent;
import eu.europa.ec.leos.ui.extension.ActionManagerExtension;
import eu.europa.ec.leos.ui.extension.AnnotateExtension;
import eu.europa.ec.leos.ui.extension.LeosEditorExtension;
import eu.europa.ec.leos.ui.extension.MathJaxExtension;
import eu.europa.ec.leos.ui.extension.RefToLinkExtension;
import eu.europa.ec.leos.ui.extension.UserCoEditionExtension;
import eu.europa.ec.leos.ui.extension.UserGuidanceExtension;
import eu.europa.ec.leos.vo.toc.AlternateConfig;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.ui.component.actions.LegalTextActionsMenuBar;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;

@StyleSheet({"vaadin://../assets/css/bill.css" + LeosCacheToken.TOKEN})
// css not present in VAADIN folder so not published by server. Anything not published need to be served by static servlet.
public class LegalTextComponent extends CustomComponent implements ContentPane {
    private static final long serialVersionUID = -7268025691934327898L;
    private static final Logger LOG = LoggerFactory.getLogger(LegalTextComponent.class);
    private static final String LEOS_RELATIVE_FULL_WDT = "100%";

    private EventBus eventBus;
    private MessageHelper messageHelper;
    private ConfigurationHelper cfgHelper;
    private SecurityContext securityContext;
    private UserHelper userHelper;

    private LeosDisplayField textContent;
    private LeosDisplayField docContent;
    private Label versionLabel;

    private HorizontalLayout mainLayout;
    private VerticalLayout legalTextLayout;
    private HorizontalLayout textToolBar;
    private LegalTextActionsMenuBar legalTextActionMenuBar;
    private Button textRefreshNote;
    private Button textRefreshButton;
    private Button searchButton;
    private SearchDelegate searchDelegate;
    private SliderPanel leftSlider;

    protected LeosEditorExtension<LeosDisplayField> leosEditorExtension;
    protected ActionManagerExtension<LeosDisplayField> actionManagerExtension;
    protected UserCoEditionExtension<LeosDisplayField, String> userCoEditionExtension;
    private AnnotateExtension<LeosDisplayField, String> annotateExtension;
    private Provider<StructureContext> structureContextProvider;
    private PackageService packageService;

    public LegalTextComponent(final EventBus eventBus, final MessageHelper messageHelper, ConfigurationHelper cfgHelper, SecurityContext securityContext,
            LegalTextActionsMenuBar legalTextActionMenuBar, Provider<StructureContext> structureContextProvider, PackageService packageService, UserHelper userHelper) {

        this.eventBus = eventBus;
        this.messageHelper = messageHelper;
        this.cfgHelper = cfgHelper;
        this.securityContext = securityContext;
        this.legalTextActionMenuBar = legalTextActionMenuBar;
        this.structureContextProvider = structureContextProvider;
        this.packageService = packageService;
        this.userHelper = userHelper;

        setId("legalTextComponent");
        setSizeFull();
        mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(false);
        mainLayout.setStyleName("leos-main-layout");
        
        legalTextLayout = buildLegalTextLayout();

        mainLayout.addComponent(legalTextLayout);
        mainLayout.setExpandRatio(legalTextLayout, 1);
        mainLayout.setComponentAlignment(legalTextLayout, Alignment.MIDDLE_CENTER);

        searchDelegate = new SearchDelegate(searchButton, legalTextLayout, messageHelper, eventBus, docContent);

        setCompositionRoot(mainLayout);
        markAsDirty();
    }

    private VerticalLayout buildLegalTextLayout() {
        VerticalLayout legalTextLayout = new VerticalLayout();
        setId("legalTextLayout");
        legalTextLayout.setSizeFull();

        // create toolbar
        textToolBar = buildLegalTextToolbar();
        legalTextLayout.addComponent(textToolBar);
        legalTextLayout.setSpacing(false);
        legalTextLayout.setMargin(false);
        
        // create content
        textContent = buildLegalTextContent();
        legalTextLayout.addComponent(textContent);
        legalTextLayout.setExpandRatio(textContent, 1.0f);
        return legalTextLayout;
    }

    public LeosDisplayField getContent() {
        return textContent;
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        searchDelegate.detach();
        super.detach();
        eventBus.unregister(this);
    }
    
    private SliderPanel buildTocLeftSliderPanel() {
        VerticalLayout tocItemsContainer = buildTocDragItems();
        tocItemsContainer.setWidth(105, Unit.PIXELS);
        tocItemsContainer.setSpacing(false);
        tocItemsContainer.setMargin(false);
        SliderPanel leftSlider = new SliderPanelBuilder(tocItemsContainer)
                .expanded(false)
                .mode(SliderMode.LEFT)
                .caption(messageHelper.getMessage("toc.slider.panel.tab.title"))
                .tabPosition(SliderTabPosition.BEGINNING)
                .zIndex(9980)
                .tabSize(0)
                .build();

        return leftSlider;
    }

    private VerticalLayout buildTocDragItems() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.setWidth(100, Unit.PERCENTAGE);
        gridLayout.setHeight(100, Unit.PERCENTAGE);
        gridLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        gridLayout.setSpacing(false);
        gridLayout.setMargin(false);
        List<TocItem> tocItemsList = structureContextProvider.get().getTocItems();
        if(tocItemsList != null) {
            for (TocItem tocItem : tocItemsList) {
                if (!tocItem.isRoot() && tocItem.isDraggable() && tocItem.isDisplay()) {
                    Label itemLabel = new Label(TableOfContentHelper.getDisplayableTocItem(tocItem, messageHelper));
                    itemLabel.setWidth(91, Unit.PIXELS);
                    itemLabel.setStyleName("leos-drag-item");

                    DragSourceExtension<Label> dragSourceExtension = new DragSourceExtension<>(itemLabel);
                    dragSourceExtension.addDragStartListener((DragStartListener<Label>) event -> {
                        String number = null, heading = null, content = "";
                        if (OptionsType.MANDATORY.equals(tocItem.getItemNumber()) || OptionsType.OPTIONAL.equals(tocItem.getItemNumber())) {
                            number = messageHelper.getMessage("toc.item.type.number");
                        }
                        if (OptionsType.MANDATORY.equals(tocItem.getItemHeading()) || OptionsType.OPTIONAL.equals(tocItem.getItemHeading())) {
                            heading = messageHelper.getMessage("toc.item.type." + tocItem.getAknTag().value().toLowerCase() + ".heading");
                        }
                        if (tocItem.isContentDisplayed()) { // TODO: Use a message property to compose the default content text here and in the XMLHelper templates for
                            // each element
                            content = (tocItem.getAknTag().value().equalsIgnoreCase(XmlHelper.RECITAL) || tocItem.getAknTag().value().equalsIgnoreCase(XmlHelper.CITATION))
                                    ? StringUtils.capitalize(tocItem.getAknTag().value() + "...") : "Text...";
                        }
                        TableOfContentItemVO dragData = new TableOfContentItemVO(tocItem, Cuid.createCuid(), null, number, null, heading, null,
                                null, null, null, content);
                        Set<TableOfContentItemVO> draggedItems = new HashSet<>();
                        draggedItems.add(dragData);
                        dragSourceExtension.setDragData(draggedItems);
                    });

                    dragSourceExtension.setEffectAllowed(EffectAllowed.COPY_MOVE);
                    gridLayout.addComponent(itemLabel);
                }
            }
        }
        VerticalLayout tocItemContainer = new VerticalLayout();
        tocItemContainer.setCaption(messageHelper.getMessage("toc.edit.window.items"));
        tocItemContainer.addStyleName("leos-left-slider-panel");

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidth(100, Unit.PERCENTAGE);
        toolbar.setSpacing(true);
        toolbar.setMargin(false);
        toolbar.setStyleName("leos-viewdoc-tocbar");
        toolbar.addStyleName("leos-slider-toolbar");
        
        Label sliderLabel = new Label(messageHelper.getMessage("toc.slider.panel.toolbar.title"), ContentMode.HTML);
        toolbar.addComponent(sliderLabel);
        
        tocItemContainer.addComponent(toolbar);
        tocItemContainer.addComponent(gridLayout);
        tocItemContainer.setExpandRatio(gridLayout, 1.0f);
        return tocItemContainer;
    }

    @Subscribe
    public void expandTocSliderPanel(ExpandTocSliderPanel event) {
        leftSlider = buildTocLeftSliderPanel();
        mainLayout.addComponent(leftSlider, 0);
        legalTextLayout.setEnabled(false);
        leftSlider.expand();
        annotateExtension.setoperationMode(AnnotateExtension.OperationMode.READ_ONLY);
    }
    
    @Subscribe
    public void handleDisableEditToc(DisableEditTocEvent event) {
        leftSlider.collapse();
        legalTextLayout.setEnabled(true);
        eventBus.post(new InlineTocCloseRequestEvent());
        mainLayout.removeComponent(leftSlider);
        annotateExtension.setoperationMode(AnnotateExtension.OperationMode.NORMAL);
    }

    public void removeAnnotateExtension() {
        if (annotateExtension != null) {
            annotateExtension.remove();
        }
    }

    private HorizontalLayout buildLegalTextToolbar() {
        LOG.debug("Building Legal Text toolbar...");

        // create text toolbar layout
        final HorizontalLayout toolsLayout = new HorizontalLayout();
        toolsLayout.setId("legalTextToolbar");
        toolsLayout.setStyleName("leos-viewdoc-docbar");
        toolsLayout.setSpacing(true);

        // set toolbar style
        toolsLayout.setWidth(LEOS_RELATIVE_FULL_WDT);
        toolsLayout.addStyleName("leos-viewdoc-padbar-both");

        legalTextActionMenuBar.setWidth(34, Unit.PIXELS);
        toolsLayout.addComponent(legalTextActionMenuBar);
        toolsLayout.setComponentAlignment(legalTextActionMenuBar, Alignment.MIDDLE_LEFT);

        // create toolbar spacer label to push components to the sides
        versionLabel = new Label();
        versionLabel.setSizeUndefined();
        versionLabel.setContentMode(ContentMode.HTML);
        toolsLayout.addComponent(versionLabel);
        toolsLayout.setComponentAlignment(versionLabel, Alignment.MIDDLE_CENTER);
        // toolbar spacer label will use all available space
        toolsLayout.setExpandRatio(versionLabel, 1.0f);

        // create blank Note Button
        textRefreshNote = legalTextNote();
        toolsLayout.addComponent(textRefreshNote);
        toolsLayout.setComponentAlignment(textRefreshNote, Alignment.MIDDLE_RIGHT);

        // add search and replace popup view
        searchButton = buildSearchButton();
        toolsLayout.addComponent(searchButton);

        // create text refresh button
        textRefreshButton = legalTextRefreshButton();
        toolsLayout.addComponent(textRefreshButton);
        toolsLayout.setComponentAlignment(textRefreshButton, Alignment.MIDDLE_RIGHT);

        return toolsLayout;
    }

    // create text refresh button
    private Button legalTextRefreshButton() {
        final Button textRefreshButton = new Button();
        textRefreshButton.setId("refreshDocument");
        textRefreshButton.setCaptionAsHtml(true);
        textRefreshButton.setIcon(VaadinIcons.REFRESH);
        textRefreshButton.setStyleName("link");
        textRefreshButton.addStyleName("leos-toolbar-button");
        textRefreshButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); // Document might be updated.
            }
        });

        return textRefreshButton;
    }

    // create text refresh button
    private Button legalTextNote() {
        final Button textRefreshNote = new Button();
        textRefreshNote.setId("refreshDocumentNote");
        textRefreshNote.setCaptionAsHtml(true);
        textRefreshNote.setStyleName("link");
        textRefreshNote.addStyleName("text-refresh-note");
        textRefreshNote.setCaption(messageHelper.getMessage("document.request.refresh.msg"));
        textRefreshNote.setIcon(LeosTheme.LEOS_INFO_YELLOW_16);
        textRefreshNote.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); // Document might be updated.
            }
        });
        return textRefreshNote;
    }

    private Button buildSearchButton() {
        Button searchButton = new Button();
        searchButton.setStyleName("link");
        searchButton.addStyleName("leos-toolbar-button");
        searchButton.setHeight(100, Unit.PERCENTAGE);
        searchButton.setIcon(VaadinIcons.SEARCH);

        return searchButton;
    }

    void initAnnotateExtension(final DocumentVO bill, final String proposalRef, final String connectedEntity) {
        annotateExtension = new AnnotateExtension<>(docContent, eventBus, cfgHelper, null, AnnotateExtension.OperationMode.NORMAL, false, true, proposalRef, connectedEntity);
    }

    private LeosDisplayField buildLegalTextContent() {
        LOG.debug("Building Legal Text content...");

        // create placeholder to display Legal Text content
        docContent = new LeosDisplayField();
        docContent.setSizeFull();
        docContent.setStyleName("leos-doc-content");
        docContent.setId("docContainer");

        // create content extensions
        new MathJaxExtension<>(docContent);
        new UserGuidanceExtension<>(docContent, eventBus);
        new RefToLinkExtension<>(docContent);
        userCoEditionExtension = new UserCoEditionExtension<>(docContent, messageHelper, securityContext, cfgHelper);

        return docContent;
    }

    public void setDocumentVersionInfo(VersionInfoVO versionInfoVO) {
        this.versionLabel.setValue(messageHelper.getMessage("document.version.caption", versionInfoVO.getDocumentVersion(),
                versionInfoVO.getLastModifiedBy(), versionInfoVO.getEntity(), versionInfoVO.getLastModificationInstant()));
    }

    public void populateContent(String docContentText) {
        /* KLUGE: In order to force the update of the docContent on the client side
         * the unique seed is added on every docContent update, please note markDirty
         * method did not work, this was the only solution worked.*/
        String seed = "<div style='display:none' >" +
                new Date().getTime() +
                "</div>";
        docContent.setValue(docContentText + seed);

        textRefreshNote.setVisible(false);
    }

    public UserCoEditionExtension<LeosDisplayField, String> getUserCoEditionExtension() {
        return userCoEditionExtension;
    }

    @Subscribe
    public void handleElementState(StateChangeEvent event) {
        if (event.getState() != null) {
            legalTextActionMenuBar.setSaveVersionEnabled(event.getState().isState());
            legalTextActionMenuBar.setImporterEnabled(event.getState().isState());
            textRefreshNote.setEnabled(event.getState().isState());
            textRefreshButton.setEnabled(event.getState().isState());
            searchDelegate.handleElementState(event);
        }
    }

    public void setPermissions(boolean enableUpdate, String instanceType, DocumentVO bill) {
        legalTextActionMenuBar.setDownloadCleanVersionVisible(instanceType.equals(InstanceType.COUNCIL.name()));
        legalTextActionMenuBar.setSaveVersionVisible(enableUpdate);
        legalTextActionMenuBar.setImporterVisible(enableUpdate);
        searchButton.setVisible(enableUpdate);
        // add extensions only if the user has the permission.
        if (enableUpdate) {
            if (leosEditorExtension == null) {
                List<TocItem> tocItems = structureContextProvider.get().getTocItems();
                List<NumberingConfig> numberingConfigs = structureContextProvider.get().getNumberingConfigs();
                List<AlternateConfig> alternateConfigs = structureContextProvider.get().getAlternateConfigs();
                leosEditorExtension = new LeosEditorExtension<>(docContent, eventBus, cfgHelper, tocItems, numberingConfigs, alternateConfigs, getDocuments(bill), bill.getMetadata().getInternalRef());
            }
            if (actionManagerExtension == null) {
                actionManagerExtension = new ActionManagerExtension<>(docContent, instanceType, eventBus, structureContextProvider.get().getTocItems());
            }
        }
    }

    public void displayDocumentUpdatedByCoEditorWarning() {
        textRefreshNote.setVisible(true);
    }

    private List<XmlDocument> getDocuments(DocumentVO bill) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(bill.getId());
        return packageService.findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, false);
    }

    public void closeSearchBarComponent() {
        searchDelegate.closeSearchBarComponent();
    }
}
