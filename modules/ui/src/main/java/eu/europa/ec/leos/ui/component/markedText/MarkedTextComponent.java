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
package eu.europa.ec.leos.ui.component.markedText;

import com.google.common.eventbus.EventBus;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.event.EnableSyncScrollRequestEvent;
import eu.europa.ec.leos.ui.event.view.DownloadXmlFilesRequestEvent;
import eu.europa.ec.leos.ui.event.view.LegisWriteExportRequestEvent;
import eu.europa.ec.leos.ui.extension.MathJaxExtension;
import eu.europa.ec.leos.ui.extension.ScrollPaneExtension;
import eu.europa.ec.leos.ui.extension.SliderPinsExtension;
import eu.europa.ec.leos.web.event.component.MarkedTextNavigationRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
import eu.europa.ec.leos.web.ui.component.ContentPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.simplefiledownloader.SimpleFileDownloader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@ViewScope
@SpringComponent
public abstract class MarkedTextComponent<T extends XmlDocument> extends CustomComponent implements ContentPane {
    private static final long serialVersionUID = -826802129383432798L;
    private static final Logger LOG = LoggerFactory.getLogger(MarkedTextComponent.class);
    private static final String LEOS_RELATIVE_FULL_WDT = "100%";

    private final EventBus eventBus;
    private final MessageHelper messageHelper;
    private final SecurityContext securityContext;
    private ExportOptions exportOptions;
    private LeosDisplayField markedContent;
    private Button syncScrollSwitch;
    private Button markedTextNextButton;
    private Button markedTextPrevButton;
    private Button downloadXmlFilesButton;
    private SimpleFileDownloader simpleFileDownloader;
    private Label versionLabel;

    private Button exportToLegiswriteButton;
    private SimpleFileDownloader exportToLegiswriteDownloader;
    private Boolean alwaysHideExportToLegiswriteButton;

    @Autowired
    public MarkedTextComponent(EventBus eventBus, MessageHelper messageHelper, SecurityContext securityContext) {
        this.eventBus = eventBus;
        this.messageHelper = messageHelper;
        this.securityContext = securityContext;
        this.exportOptions = null;
        this.alwaysHideExportToLegiswriteButton = true;

        setSizeFull();
        VerticalLayout markedTextLayout = new VerticalLayout();
        markedTextLayout.setSizeFull();
        markedTextLayout.setSpacing(false);
        markedTextLayout.setMargin(false);
        
        // create toolbar
        markedTextLayout.addComponent(buildMarkedTextToolbar());
        // create content
        final Component textContent = buildMarkedTextContent();

        markedTextLayout.addComponent(textContent);
        markedTextLayout.setExpandRatio(textContent, 1.0f);
        setCompositionRoot(markedTextLayout);
    
        initexportToLegiswriteDownloader();
        initSimpleDownloader();
        hideCompareButtons();
    }
    
    private Button exportToLegiswriteButton() {
        Button exportToLegiswriteButton = new Button();
        exportToLegiswriteButton.setDescription(messageHelper.getMessage("leos.button.tooltip.export.legiswrite"));
        exportToLegiswriteButton.addStyleName("link leos-toolbar-button");
        exportToLegiswriteButton.setIcon(VaadinIcons.DOWNLOAD);
        return exportToLegiswriteButton;
    }

    private void initSimpleDownloader() {
        simpleFileDownloader = new SimpleFileDownloader();
        addExtension(simpleFileDownloader);
        showDownloadXmlFilesButton(false);
    }

    private void addDownloadXmlFilesButtonClickListener(){
        downloadXmlFilesButton.addClickListener((Button.ClickListener) event -> {
            eventBus.post(new DownloadXmlFilesRequestEvent(this.exportOptions));
        });
    }
    
    private void initexportToLegiswriteDownloader() {
        this.exportToLegiswriteDownloader = new SimpleFileDownloader();
        addExtension(this.exportToLegiswriteDownloader);
    }

    private void addLegiswriteButtonClickListener(){
        this.exportToLegiswriteButton.addClickListener((Button.ClickListener) event -> {
            eventBus.post(new LegisWriteExportRequestEvent(this.exportOptions));
        });
    }

    public void showDownloadXmlFilesButton(boolean visible){
        if(visible){
            boolean canDownload = securityContext.hasPermission(null, LeosPermission.CAN_DOWNLOAD_XML_COMPARISON);
            downloadXmlFilesButton.setVisible(canDownload);
        } else {
            downloadXmlFilesButton.setVisible(false);
        }
    }
    
    public void setDownloadStreamResourceForXmlFiles(StreamResource streamResource) {
        simpleFileDownloader.setFileDownloadResource(streamResource);
        simpleFileDownloader.download();
    }

    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        exportToLegiswriteDownloader.setFileDownloadResource(streamResource);
        exportToLegiswriteDownloader.download();
    }
    
    private Component buildMarkedTextToolbar() {
        LOG.debug("Building marked Text toolbar...");

        // create text toolbar layout
        final HorizontalLayout toolsLayout = new HorizontalLayout();
        toolsLayout.setId("markedTextToolbar");
        toolsLayout.setStyleName("leos-markedtext-bar");

        // set toolbar style
        toolsLayout.setWidth(LEOS_RELATIVE_FULL_WDT);

        //create sync scroll
        syncScrollSwitch = syncScrollSwitch();
        syncScrollSwitch.setDescription(messageHelper.getMessage("leos.button.tooltip.enable.sync"), ContentMode.HTML);
        toolsLayout.addComponent(syncScrollSwitch);
        toolsLayout.setComponentAlignment(syncScrollSwitch, Alignment.MIDDLE_LEFT);
        
        markedTextNextButton = markedTextNextNavigationButton();
        markedTextNextButton.setDescription(messageHelper.getMessage("version.changes.navigation.next"));
        toolsLayout.addComponent(markedTextNextButton);
        toolsLayout.setComponentAlignment(markedTextNextButton, Alignment.MIDDLE_LEFT);
        
        markedTextPrevButton = markedTextPrevNavigationButton();
        markedTextPrevButton.setDescription(messageHelper.getMessage("version.changes.navigation.prev"));
        toolsLayout.addComponent(markedTextPrevButton);
        toolsLayout.setComponentAlignment(markedTextPrevButton, Alignment.MIDDLE_LEFT);
        
        versionLabel = new Label();
        versionLabel.setSizeUndefined();
        versionLabel.setContentMode(ContentMode.HTML);
        toolsLayout.addComponent(versionLabel);
        toolsLayout.setComponentAlignment(versionLabel, Alignment.MIDDLE_CENTER);
        toolsLayout.setExpandRatio(versionLabel,1.0f);

        downloadXmlFilesButton = downloadXmlFilesButton();
        toolsLayout.addComponent(downloadXmlFilesButton);
        toolsLayout.setComponentAlignment(downloadXmlFilesButton, Alignment.MIDDLE_RIGHT);
        
        exportToLegiswriteButton = exportToLegiswriteButton();
        toolsLayout.addComponent(exportToLegiswriteButton);
        toolsLayout.setComponentAlignment(exportToLegiswriteButton, Alignment.MIDDLE_RIGHT);

        Button closeButton = closeMarkedTextComponent();
        toolsLayout.addComponent(closeButton);
        toolsLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);

        return toolsLayout;
    }
    
    private Button downloadXmlFilesButton() {
        Button downloadXmlFilesButton = new Button();
        downloadXmlFilesButton.setDescription(messageHelper.getMessage("leos.button.tooltip.download.xmlfiles"));
        downloadXmlFilesButton.addStyleName("link leos-toolbar-button");
        downloadXmlFilesButton.setIcon(VaadinIcons.CLOUD_DOWNLOAD);
        return downloadXmlFilesButton;
    }

    private Button closeMarkedTextComponent() {
        Button closeButton = new Button();
        closeButton.setDescription(messageHelper.getMessage("version.compare.close.button.description"));
        closeButton.addStyleName("link leos-toolbar-button");
        closeButton.setIcon(VaadinIcons.CLOSE_CIRCLE);
        closeButton.addClickListener(event -> eventBus.post(new ComparisonEvent(false)));
        return closeButton;
    }

    private Button markedTextPrevNavigationButton() {
        VaadinIcons markedTextPrevIcon = VaadinIcons.CARET_UP;
        final Button markedTextPrevButton = new Button();
        markedTextPrevButton.setIcon(markedTextPrevIcon);
        markedTextPrevButton.addStyleName("link leos-toolbar-button navigation-btn");
        markedTextPrevButton.addClickListener(event -> eventBus.post(new MarkedTextNavigationRequestEvent(MarkedTextNavigationRequestEvent.NAV_DIRECTION.PREV)));
        return markedTextPrevButton;
    }
    
    private Button markedTextNextNavigationButton() {
        VaadinIcons markedTextNextIcon = VaadinIcons.CARET_DOWN;
        final Button markedTextNextButton = new Button();
        markedTextNextButton.setIcon(markedTextNextIcon);
        markedTextNextButton.addStyleName("link leos-toolbar-button navigation-btn");
        markedTextNextButton.addClickListener(event -> eventBus.post(new MarkedTextNavigationRequestEvent(MarkedTextNavigationRequestEvent.NAV_DIRECTION.NEXT)));
        return markedTextNextButton;
    }
    
    private Button syncScrollSwitch() {
        VaadinIcons syncIcon = VaadinIcons.EXCHANGE;
        final Button syncScrollSwitch = new Button();
        syncScrollSwitch.setIcon(syncIcon);
        syncScrollSwitch.setStyleName("link");
        syncScrollSwitch.addStyleName("leos-toolbar-button enable-sync");
        syncScrollSwitch.setData(true);
        syncScrollSwitch.setDescription(messageHelper.getMessage("leos.button.tooltip.disable.sync"));
        
        syncScrollSwitch.addClickListener(event -> {
            Button button = event.getButton();
            boolean syncState = !(boolean) button.getData();
            updateStyle(button, syncState);
            eventBus.post(new EnableSyncScrollRequestEvent(syncState));
            button.setDescription(syncState
                    ? messageHelper.getMessage("leos.button.tooltip.disable.sync")
                    : messageHelper.getMessage("leos.button.tooltip.enable.sync"));
            
            button.setData(syncState);
        });
        return syncScrollSwitch;
    }
    
    private void updateStyle(Button button, boolean syncState) {
        if(syncState) {
            button.removeStyleName("disable-sync");
            button.addStyleName("enable-sync");
        } else {
            button.removeStyleName("enable-sync");
            button.addStyleName("disable-sync");
        }
    }

    private Component buildMarkedTextContent() {
        LOG.debug("Building Marked Text content...");

        // create placeholder to display marked content
        markedContent = new LeosDisplayField();
        markedContent.setSizeFull();
        markedContent.setId("leos-marked-content");
        markedContent.setStyleName("leos-marked-content");

        // create marked content extensions
        new MathJaxExtension<>(markedContent);
        ScrollPaneExtension scrollPaneExtension = new ScrollPaneExtension(markedContent, eventBus);
        scrollPaneExtension.getState().idPrefix = "marked-";
        scrollPaneExtension.getState().containerSelector = ".leos-marked-content";
        SliderPinsExtension<LeosDisplayField> sliderPins = new SliderPinsExtension<>(markedContent, getSelectorStyleMap());
        MarkedTextNavigationHelper navHelper = new MarkedTextNavigationHelper(sliderPins);
        this.eventBus.register(navHelper);//Registering helper object to eventBus. Presently this method is called only once if multiple invocation occurs in future will need to unregister the object on close of document.
        return markedContent;
    }

    protected Map<String, String> getSelectorStyleMap() {
        Map<String, String> selectorStyleMap = new HashMap<>();
        selectorStyleMap.put(".leos-marker-content-removed", "pin-leos-marker-content-removed-hidden");
        selectorStyleMap.put(".leos-marker-content-added", "pin-leos-marker-content-added-hidden");
        selectorStyleMap.put(".leos-content-removed", "pin-leos-content-removed-hidden");
        selectorStyleMap.put(".leos-content-new", "pin-leos-content-new-hidden");
        return selectorStyleMap;
    }

    public void populateMarkedContent(String markedContentText, LeosCategory leosCategory, String comparedInfo, ExportVersions<T> exportVersions) {
        if (this.exportOptions != null){
            this.exportOptions.setExportVersions(exportVersions);
        }
        markedContent.addStyleName(leosCategory.name().toLowerCase());
        markedContent.setValue(markedContentText.replaceAll("(?i) id=\"", " id=\"marked-"));
        versionLabel.setValue(comparedInfo);
        eventBus.post(new EnableSyncScrollRequestEvent(true));
    }

    private void hideExportToLegiswriteButton() {
        exportToLegiswriteButton.setVisible(false);
    }
    
    private void showExportToLegiswriteButton() {
        exportToLegiswriteButton.setVisible(true);
    }


    public void hideCompareButtons() {
        markedTextNextButton.setVisible(false);
        markedTextPrevButton.setVisible(false);
        showDownloadXmlFilesButton(false);
        hideExportToLegiswriteButton();
    }
    
    public void showCompareButtons() {
        syncScrollSwitch.setVisible(true);
        markedTextNextButton.setVisible(true);
        markedTextPrevButton.setVisible(true);
        showDownloadXmlFilesButton(true);
        if (!this.alwaysHideExportToLegiswriteButton){
            showExportToLegiswriteButton();
        }
    }

    public void scrollToMarkedChange(String elementId) {
        LOG.trace("Navigating to (elementId={})...", elementId);
        com.vaadin.ui.JavaScript.getCurrent().execute("LEOS.scrollToMarkedElement('" + elementId + "');");
    }
    
    @Override
    public float getDefaultPaneWidth(int numberOfFeatures, boolean tocPresent) {
        final float featureWidth;
        if (numberOfFeatures == 1) {
            featureWidth = 100f;
        } else {
            if (tocPresent) {
                featureWidth = 42.5f;
            } else {
                featureWidth = 50f;
            }
        }
        return featureWidth;
    }

    public void setExportOptions(ExportOptions exportOptions){
        this.exportOptions = exportOptions;
        this.addDownloadXmlFilesButtonClickListener();
        this.addLegiswriteButtonClickListener();
    }

    public void setAlwaysHideExportToLegiswriteButton(boolean alwaysHideExportToLegiswriteButton){
        this.alwaysHideExportToLegiswriteButton = alwaysHideExportToLegiswriteButton;
    }
}