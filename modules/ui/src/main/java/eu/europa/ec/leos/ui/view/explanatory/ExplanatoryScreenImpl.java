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
package eu.europa.ec.leos.ui.view.explanatory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.TreeData;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.event.DragStartListener;
import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.support.TableOfContentHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.AccordionPane;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.component.doubleCompare.DoubleComparisonComponent;
import eu.europa.ec.leos.ui.component.markedText.MarkedTextComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentComponent;
import eu.europa.ec.leos.ui.component.toc.TableOfContentItemConverter;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.event.StateChangeEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceMatchResponseEvent;
import eu.europa.ec.leos.ui.event.search.SearchTextResponseEvent;
import eu.europa.ec.leos.ui.event.security.SecurityTokenRequest;
import eu.europa.ec.leos.ui.event.security.SecurityTokenResponse;
import eu.europa.ec.leos.ui.event.toc.DisableEditTocEvent;
import eu.europa.ec.leos.ui.event.toc.ExpandTocSliderPanel;
import eu.europa.ec.leos.ui.event.toc.InlineTocCloseRequestEvent;
import eu.europa.ec.leos.ui.extension.*;
import eu.europa.ec.leos.ui.view.ComparisonDisplayMode;
import eu.europa.ec.leos.ui.view.ScreenLayoutHelper;
import eu.europa.ec.leos.ui.view.TriFunction;
import eu.europa.ec.leos.ui.window.milestone.MilestoneExplorer;
import eu.europa.ec.leos.ui.window.toc.TocEditor;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.component.ComparisonResponseEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.document.*;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent.Action;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.component.ExplanatoryComponent;
import eu.europa.ec.leos.web.ui.component.ContentPane;
import eu.europa.ec.leos.web.ui.component.SearchDelegate;
import eu.europa.ec.leos.web.ui.component.actions.ExplanatoryActionsMenuBar;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import eu.europa.ec.leos.web.ui.themes.LeosTheme;
import eu.europa.ec.leos.web.ui.window.IntermediateVersionWindow;
import eu.europa.ec.leos.web.ui.window.TimeLineWindow;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import javax.inject.Provider;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITAL;

@SpringComponent
@ViewScope
@DesignRoot("ExplanatoryScreenDesign.html")
@StyleSheet({"vaadin://../assets/css/explanatory.css" + LeosCacheToken.TOKEN})
abstract class ExplanatoryScreenImpl extends VerticalLayout implements ExplanatoryScreen {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ExplanatoryScreenImpl.class);
    
    public static SimpleDateFormat dataFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    protected final EventBus eventBus;
    protected final UserHelper userHelper;
    protected final SecurityContext securityContext;
    protected MessageHelper messageHelper;
    protected ConfigurationHelper cfgHelper;
    protected InstanceTypeResolver instanceTypeResolver;
    protected TocEditor tocEditor;

    protected TimeLineWindow<Explanatory> timeLineWindow;
    protected HorizontalSplitPanel explanatorySplit;
    protected HorizontalSplitPanel contentSplit;
    protected Label explanatoryTitle;

    // dummy init to avoid design exception
    protected ScreenLayoutHelper screenLayoutHelper = new ScreenLayoutHelper(null, null);
    protected SliderPanel leftSlider = new SliderPanelBuilder(new VerticalLayout()).build();
    
    protected ComparisonComponent<Explanatory> comparisonComponent;
    protected ExplanatoryComponent explanatoryDoc;
    protected HorizontalLayout mainLayout;
    protected VerticalLayout explanatoryLayout;
    protected LeosDisplayField explanatoryContent;
    
    protected TableOfContentComponent tableOfContentComponent = new TableOfContentComponent();
    protected AccordionPane accordionPane;
    protected Accordion accordion;
    protected VersionsTab<Explanatory> versionsTab;
    
    protected ExplanatoryActionsMenuBar explanatoryActionsMenuBar;
    protected Label versionInfoLabel;
    protected Button refreshNoteButton;
    protected Button refreshButton;
    protected Button searchButton;

    protected LeosEditorExtension<LeosDisplayField> leosEditorExtension;
    protected ActionManagerExtension<LeosDisplayField> actionManagerExtension;
    protected UserCoEditionExtension<LeosDisplayField, String> userCoEditionExtension;
    private AnnotateExtension<LeosDisplayField, String> annotateExtension;

    protected Provider<StructureContext> structureContextProvider;
    private PackageService packageService;
    private SearchDelegate searchDelegate;

    @Value("${leos.coedition.sip.enabled}")
    private boolean coEditionSipEnabled;

    @Value("${leos.coedition.sip.domain}")
    private String coEditionSipDomain;

    @Autowired
    LeosPermissionAuthorityMapHelper authorityMapHelper;

    @Autowired
    ExplanatoryScreenImpl(MessageHelper messageHelper, EventBus eventBus, SecurityContext securityContext, UserHelper userHelper,
            ConfigurationHelper cfgHelper, TocEditor tocEditor, InstanceTypeResolver instanceTypeResolver, VersionsTab<Explanatory> versionsTab,
            Provider<StructureContext> structureContextProvider, PackageService packageService) {
        LOG.trace("Initializing explanatory screen...");
        Validate.notNull(messageHelper, "MessageHelper must not be null!");
        this.messageHelper = messageHelper;
        Validate.notNull(eventBus, "EventBus must not be null!");
        this.eventBus = eventBus;
        Validate.notNull(securityContext, "SecurityContext must not be null!");
        this.securityContext = securityContext;
        Validate.notNull(userHelper, "UserHelper must not be null!");
        this.userHelper = userHelper;
        Validate.notNull(cfgHelper, "Configuration helper must not be null!");
        this.cfgHelper = cfgHelper;
        Validate.notNull(tocEditor, "TocEditor must not be null!");
        this.tocEditor = tocEditor;
        Validate.notNull(instanceTypeResolver, "instanceTypeResolver must not be null!");
        this.instanceTypeResolver = instanceTypeResolver;
        Validate.notNull(instanceTypeResolver, "versionsTab must not be null!");
        this.versionsTab = versionsTab;
        Validate.notNull(structureContextProvider, "structureContextProvider must not be null!");
        this.structureContextProvider = structureContextProvider;
        Validate.notNull(structureContextProvider, "packageService must not be null!");
        this.packageService = packageService;

        // dummy init to avoid design exception
        timeLineWindow = new TimeLineWindow<>(messageHelper, eventBus);
        Design.read(this);
        init();
    }
    
    @Override
    public void setTitle(String title) {
        StringBuilder combinedTitle = new StringBuilder();
        combinedTitle.append(messageHelper.getMessage("document.explanatory.title.default"));
        explanatoryTitle.setValue(StringEscapeUtils.escapeHtml4(combinedTitle.toString()));
        explanatoryTitle.setWidth("100%");
    }
    
    @Override
    public void setContent(String content) {
        explanatoryContent.setValue(addTimestamp(content));
        refreshNoteButton.setVisible(false);
    }
    
    void init() {
        tableOfContentComponent = new TableOfContentComponent(messageHelper, eventBus, securityContext, cfgHelper, tocEditor, structureContextProvider);
        accordion.addTab(tableOfContentComponent, messageHelper.getMessage("toc.title"), VaadinIcons.CHEVRON_DOWN);
        accordion.addTab(versionsTab, messageHelper.getMessage("document.accordion.versions"), VaadinIcons.CHEVRON_RIGHT);
        
        accordion.addListener(event -> {
            final Component selected = ((Accordion) event.getSource()).getSelectedTab();
            for (int i = 0; i < accordion.getComponentCount(); i++) {
                TabSheet.Tab tab = accordion.getTab(i);
                if (tab.getComponent().getClass().equals(selected.getClass())) {
                    tab.setIcon(VaadinIcons.CHEVRON_DOWN);
                } else {
                    tab.setIcon(VaadinIcons.CHEVRON_RIGHT);
                }
            }
        });

        contentSplit.setId(ScreenLayoutHelper.CONTENT_SPLITTER);
        explanatorySplit.setId(ScreenLayoutHelper.TOC_SPLITTER);
        screenLayoutHelper = new ScreenLayoutHelper(eventBus, Arrays.asList(contentSplit, explanatorySplit));
        screenLayoutHelper.addPane(explanatoryDoc, 1, true);
        screenLayoutHelper.addPane(accordionPane, 0, true);

        securityContext.getUser().getLogin();
        User loggedUser = userHelper.getUser(securityContext.getUser().getLogin());


        new MathJaxExtension<>(explanatoryContent);
        new RefToLinkExtension<>(explanatoryContent);
        userCoEditionExtension = new UserCoEditionExtension<>(explanatoryContent, messageHelper, securityContext, cfgHelper);

        refreshNoteButton();
        refreshButton();
        searchDelegate = new SearchDelegate(searchButton, explanatoryLayout, messageHelper, eventBus, explanatoryContent);

        markAsDirty();
    }

    @Override
    public void attach() {
        eventBus.register(this);
        eventBus.register(screenLayoutHelper);
        super.attach();
    }
    
    @Override
    public void detach() {
        searchDelegate.detach();
        super.detach();
        eventBus.unregister(screenLayoutHelper);
        eventBus.unregister(this);
    }
    
    @Override
    public void showElementEditor(final String elementId, final String elementTagName, final String elementFragment) {
        CreateEventParameter eventParameterObject = new CreateEventParameter(elementId, elementTagName, elementFragment, LeosCategory.COUNCIL_EXPLANATORY.name(), securityContext.getUser(),
                authorityMapHelper.getPermissionsForRoles(securityContext.getUser().getRoles()));
        eventBus.post(instanceTypeResolver.createEvent(eventParameterObject));
    }
    
    @Override
    public void refreshElementEditor(final String elementId, final String elementTagName, final String elementFragment) {
        eventBus.post(new RefreshElementEvent(elementId, elementTagName, elementFragment));
    }

    @Override
    public void showTimeLineWindow(List<Explanatory> documentVersions) {
        timeLineWindow = new TimeLineWindow<Explanatory>(securityContext, messageHelper, eventBus, userHelper, documentVersions);
        UI.getCurrent().addWindow(timeLineWindow);
        timeLineWindow.center();
        timeLineWindow.focus();
    }

    @Override
    public void updateTimeLineWindow(List<Explanatory> documentVersions) {
        if (timeLineWindow != null && timeLineWindow.header != null) { //TODO added avoid NPE. Until Timeline will be cleaned up
            timeLineWindow.updateVersions(documentVersions);
            timeLineWindow.focus();
        }
    }

    @Override
    public void displayComparison(HashMap<ComparisonDisplayMode, Object> htmlResult){
        eventBus.post(new ComparisonResponseEvent(htmlResult, LeosCategory.COUNCIL_EXPLANATORY.name().toLowerCase()));
    }

    @Override
    public void showIntermediateVersionWindow() {
        IntermediateVersionWindow intermediateVersionWindow = new IntermediateVersionWindow(messageHelper, eventBus);
        UI.getCurrent().addWindow(intermediateVersionWindow);
        intermediateVersionWindow.center();
        intermediateVersionWindow.focus();
    }

    @Override
    public void setDocumentVersionInfo(VersionInfoVO versionInfoVO) {
        this.versionInfoLabel.setValue(messageHelper.getMessage("document.version.caption", versionInfoVO.getDocumentVersion(), versionInfoVO.getLastModifiedBy(), versionInfoVO.getEntity(), versionInfoVO.getLastModificationInstant()));
    }

    private void refreshNoteButton() {
        refreshNoteButton.setCaptionAsHtml(true);
        refreshNoteButton.setCaption(messageHelper.getMessage("document.request.refresh.msg"));
        refreshNoteButton.setIcon(LeosTheme.LEOS_INFO_YELLOW_16);
        refreshNoteButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); //Document might be updated.
            }
        });
    }

    // create text refresh button
    private void refreshButton() {
        refreshButton.addClickListener(new ClickListener() {
            private static final long serialVersionUID = 3714441703159576377L;

            @Override
            public void buttonClick(ClickEvent event) {
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent(false)); //Document might be updated.
            }
        });
    }
    
    @Override
    public void setToc(List<TableOfContentItemVO> tocItemVoList) {
        TreeData<TableOfContentItemVO> tocData = TableOfContentItemConverter.buildTocData(tocItemVoList);
        tableOfContentComponent.setTableOfContent(tocData);
    }

    @Subscribe
    public void handleElementState(StateChangeEvent event) {
        if(event.getState() != null) {
            explanatoryActionsMenuBar.setSaveVersionEnabled(event.getState().isState());
            refreshButton.setEnabled(event.getState().isState());
            refreshNoteButton.setEnabled(event.getState().isState());
            searchDelegate.handleElementState(event);
        }
    }
    
    @Override
    public void setPermissions(DocumentVO explanatory){
        boolean enableUpdate = securityContext.hasPermission(explanatory, LeosPermission.CAN_UPDATE);
        explanatoryActionsMenuBar.setSaveVersionVisible(enableUpdate);
        tableOfContentComponent.setPermissions(enableUpdate);
        searchButton.setVisible(enableUpdate);

        // add extensions only if the user has the permission.
        if(enableUpdate) {
            if(leosEditorExtension == null) {
                leosEditorExtension = new LeosEditorExtension<LeosDisplayField>(explanatoryContent, eventBus, cfgHelper, structureContextProvider.get().getTocItems(), structureContextProvider.get().getNumberingConfigs(), null, getDocuments(explanatory), explanatory.getMetadata().getInternalRef());
            }
            if(actionManagerExtension == null) {
                actionManagerExtension = new ActionManagerExtension<LeosDisplayField>(explanatoryContent, instanceTypeResolver.getInstanceType(), eventBus, structureContextProvider.get().getTocItems());
            }
        }
    }

    @Override
    public void initAnnotations(DocumentVO explanatory, String proposalRef, String connectedEntity) {
        annotateExtension = new AnnotateExtension<>(explanatoryContent, eventBus, cfgHelper, null, AnnotateExtension.OperationMode.NORMAL, false, true, proposalRef, connectedEntity);
    }
    
    @Override
    public void sendUserPermissions(List<LeosPermission> userPermissions) {
        eventBus.post(new FetchUserPermissionsResponse(userPermissions));
    }

    private String addTimestamp(String docContentText) {
        /* KLUGE: In order to force the update of the docContent on the client side
         * the unique seed is added on every docContent update, please note markDirty
         * method did not work, this was the only solution worked.*/
        String seed = "<div style='display:none' >" +
                new Date().getTime() +
                "</div>";
        return docContentText + seed;
    }
    
    @Subscribe
    public void fetchToken(SecurityTokenRequest event){
        eventBus.post(new SecurityTokenResponse(securityContext.getAnnotateToken(event.getUrl())));
    }

    @Override
    public void scrollToMarkedChange(String elementId) {
    }
    
    @Override
    public void updateUserCoEditionInfo(List<CoEditionVO> coEditionVos, String presenterId) {
        this.getUI().access(() -> {
            tableOfContentComponent.updateUserCoEditionInfo(coEditionVos, presenterId);
            userCoEditionExtension.updateUserCoEditionInfo(coEditionVos, presenterId);
        });
    }

    @Override
    public void displayDocumentUpdatedByCoEditorWarning() {
        this.getUI().access(() -> {
            refreshNoteButton.setVisible(true);
        });
    }
    
    protected boolean componentEnabled(Class className){
        return screenLayoutHelper.isPaneEnabled(className);
    }

    @Override
    public void checkElementCoEdition(List<CoEditionVO> coEditionVos, User user, final String elementId, final String elementTagName, final Action action, final Object actionEvent) {
        StringBuilder coEditorsList = new StringBuilder();
        coEditionVos.stream().filter((x) -> InfoType.ELEMENT_INFO.equals(x.getInfoType()) && x.getElementId().equals(elementId))
                .sorted(Comparator.comparing(CoEditionVO::getUserName).thenComparingLong(CoEditionVO::getEditionTime)).forEach(x -> {
                    StringBuilder userDescription = new StringBuilder();
                    if (!x.getUserLoginName().equals(user.getLogin())) {
                        userDescription.append("<a href=\"")
                                .append(StringUtils.isEmpty(x.getUserEmail()) ? "" : (coEditionSipEnabled ? new StringBuilder("sip:").append(x.getUserEmail().replaceFirst("@.*", "@" + coEditionSipDomain)).toString()
                                        : new StringBuilder("mailto:").append(x.getUserEmail()).toString()))
                                .append("\">").append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity())
                                .append(")</a>");
                    } else {
                        userDescription.append(x.getUserName()).append(" (").append(StringUtils.isEmpty(x.getEntity()) ? "-" : x.getEntity()).append(")");
                    }
                    coEditorsList.append("&nbsp;&nbsp;-&nbsp;")
                            .append(messageHelper.getMessage("coedition.tooltip.message", userDescription, dataFormat.format(new Date(x.getEditionTime()))))
                            .append("<br>");
                });
        if (!StringUtils.isEmpty(coEditorsList)) {
            confirmCoEdition(coEditorsList.toString(), elementId, action, actionEvent);
        } else {
            if (action == Action.DELETE) {
                eventBus.post(new CheckDeleteLastEditingTypeEvent(elementId, actionEvent));
            } else {
                eventBus.post(actionEvent);
            }
        }
    }

    private void confirmCoEdition(String coEditorsList, String elementId, Action action, Object actionEvent) {
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.title"),
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.message", coEditorsList),
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.confirm"),
                messageHelper.getMessage("coedition." + action.getValue() + ".element.confirmation.cancel"), null);
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
        confirmDialog.getContent().setHeightUndefined();
        confirmDialog.setHeightUndefined();
        confirmDialog.show(getUI(), dialog -> {
            if (dialog.isConfirmed()) {
                if (action == Action.DELETE) {
                    eventBus.post(new CheckDeleteLastEditingTypeEvent(elementId, actionEvent));
                } else {
                    eventBus.post(actionEvent);
                }
            } else {
                eventBus.post(new CancelActionElementRequestEvent(elementId));
            }
        }, true);
    }

    @Override
    public void showAlertDialog(String messageKey) {
        ConfirmDialog confirmDialog = ConfirmDialog.getFactory().create(
                messageHelper.getMessage(messageKey + ".alert.title"),
                messageHelper.getMessage(messageKey + ".alert.message"),
                messageHelper.getMessage(messageKey + ".alert.confirm"), null, null);
        confirmDialog.setContentMode(ConfirmDialog.ContentMode.HTML);
        confirmDialog.getContent().setHeightUndefined();
        confirmDialog.setHeightUndefined();
        confirmDialog.getCancelButton().setVisible(false);
        confirmDialog.show(getUI(), dialog -> {}, true);
    }

    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
    }
    
    @Override
    public void setDownloadStreamResourceForVersion(StreamResource streamResource, String documentId){
        versionsTab.setDownloadStreamResourceForVersion(streamResource, documentId);
    }

    protected void changeLayout(LayoutChangeRequestEvent event, Object obj) {
        if (obj instanceof MarkedTextComponent || obj instanceof DoubleComparisonComponent) {
            if (event.getOriginatingComponent() == ComparisonComponent.class) {
                if (!event.getPosition().equals(ColumnPosition.OFF)) {
                    comparisonComponent.setContent((ContentPane)obj);
                } else {
                    obj = null;
                    comparisonComponent.setContent(null);
                }
            }
            screenLayoutHelper.changePosition(event.getPosition(), event.getOriginatingComponent());
        }
    }
    
    @Subscribe
    public void handleDisableEditToc(DisableEditTocEvent event) {
        leftSlider.collapse();
        explanatoryLayout.setEnabled(true);
        eventBus.post(new InlineTocCloseRequestEvent());
        mainLayout.removeComponent(leftSlider);
        annotateExtension.setoperationMode(AnnotateExtension.OperationMode.NORMAL);
    }
    
    @Subscribe
    public void expandTocSliderPanel(ExpandTocSliderPanel event) {
        leftSlider = buildTocLeftSliderPanel();
        mainLayout.addComponent(leftSlider, 0);
        explanatoryLayout.setEnabled(false);
        leftSlider.expand();
        annotateExtension.setoperationMode(AnnotateExtension.OperationMode.READ_ONLY);
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
        gridLayout.setMargin(true);
        gridLayout.setStyleName("leos-left-slider-gridlayout");
        List<TocItem> tocItemsList = structureContextProvider.get().getTocItems();
        String[] elementlist = new String[]{RECITAL, CITATION, CROSSHEADING};
        for (TocItem type : tocItemsList) {
            if (!type.isRoot() && type.isDraggable()) {
                Label itemLabel = new Label(TableOfContentHelper.getDisplayableTocItem(type, messageHelper));
                itemLabel.setStyleName("leos-drag-item");

                DragSourceExtension<Label> dragSourceExtension = new DragSourceExtension<>(itemLabel);
                dragSourceExtension.addDragStartListener((DragStartListener<Label>) event -> {
                    String number = null, heading = null, content = "";
                    if (OptionsType.MANDATORY.equals(type.getItemNumber()) || OptionsType.OPTIONAL.equals(type.getItemNumber())) {
                        number = messageHelper.getMessage("toc.item.type.number");
                    }
                    if (OptionsType.MANDATORY.equals(type.getItemHeading())) {
                        heading = messageHelper.getMessage("toc.item.type." + type.getAknTag().value().toLowerCase() + ".heading");
                    }
                    if (type.isContentDisplayed()) { // TODO: Use a message property to compose the default content text here and in the XMLHelper templates for
                        // each element
                        content = Arrays.stream(elementlist).filter(element -> type.getAknTag().value().equalsIgnoreCase(element)).findAny().isPresent()
                                ? StringUtils.capitalize(type.getAknTag().value() + "...") : "Text...";
                    }
                    TableOfContentItemVO dragData = new TableOfContentItemVO(type, Cuid.createCuid(), null, number, null, heading, null,
                            null, null, null, content);
                    Set<TableOfContentItemVO> draggedItems = new HashSet<>();
                    draggedItems.add(dragData);
                    dragSourceExtension.setDragData(draggedItems);
                });

                dragSourceExtension.setEffectAllowed(EffectAllowed.COPY_MOVE);
                gridLayout.addComponent(itemLabel);
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

    public boolean isTocEnabled() {
        return screenLayoutHelper.isTocPaneEnabled();
    }

    private List<XmlDocument> getDocuments(DocumentVO documentVo) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentVo.getId());
        return packageService.findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, false);
    }
    @Override
    public void setDataFunctions(List<VersionVO> allVersions,
                                 BiFunction<Integer, Integer, List<Explanatory>> majorVersionsFn, Supplier<Integer> countMajorVersionsFn,
                                 TriFunction<String, Integer, Integer, List<Explanatory>> minorVersionsFn, Function<String, Integer> countMinorVersionsFn,
                                 BiFunction<Integer, Integer, List<Explanatory>> recentChangesFn, Supplier<Integer> countRecentChangesFn) {
        versionsTab.setDataFunctions(allVersions, minorVersionsFn, countMinorVersionsFn,
                recentChangesFn, countRecentChangesFn, true);
    }
    
    public void refreshVersions(List<VersionVO> allVersions, boolean isComparisonMode) {
        versionsTab.refreshVersions(allVersions, isComparisonMode);
    }
    
    @Override
    public void showMilestoneExplorer(LegDocument legDocument, String milestoneTitle, String proposalRef) {
        MilestoneExplorer milestoneExplorer = new MilestoneExplorer(legDocument, milestoneTitle, proposalRef, messageHelper, eventBus, cfgHelper, securityContext, userHelper);
        UI.getCurrent().addWindow(milestoneExplorer);
        milestoneExplorer.center();
        milestoneExplorer.focus();
    }
    
    @Override
    public void scrollTo(String elementId) {
        com.vaadin.ui.JavaScript.getCurrent().execute("LEOS.scrollTo('" + elementId + "');");
    }

    @Override
    public void showMatchResults(Long searchId, List<SearchMatchVO> results) {
        eventBus.post(new SearchTextResponseEvent(searchId, results));
    }

    @Override
    public void refineSearch(Long searchId, int matchedIndex, boolean isReplaced) {
        eventBus.post(new ReplaceMatchResponseEvent(searchId, matchedIndex, isReplaced));
    }

    @Override
    public void closeSearchBar() {
        searchDelegate.closeSearchBarComponent();
    }
}
