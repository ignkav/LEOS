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
package eu.europa.ec.leos.ui.component.doubleCompare;

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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.ui.component.LeosDisplayField;
import eu.europa.ec.leos.ui.component.markedText.MarkedTextNavigationHelper;
import eu.europa.ec.leos.ui.event.EnableSyncScrollRequestEvent;
import eu.europa.ec.leos.ui.event.SyncScrollPrefixRequestEvent;
import eu.europa.ec.leos.ui.event.doubleCompare.DocuWriteExportRequestEvent;
import eu.europa.ec.leos.ui.event.view.DownloadXmlFilesRequestEvent;
import eu.europa.ec.leos.ui.extension.MathJaxExtension;
import eu.europa.ec.leos.ui.extension.ScrollPaneExtension;
import eu.europa.ec.leos.ui.extension.SliderPinsExtension;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.ui.window.export.AnnexExportPackageWindow;
import eu.europa.ec.leos.ui.window.export.LegalTextExportPackageWindow;
import eu.europa.ec.leos.web.event.component.MarkedTextNavigationRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
import eu.europa.ec.leos.web.ui.component.ContentPane;
import eu.europa.ec.leos.ui.window.export.ExportPackageWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.simplefiledownloader.SimpleFileDownloader;

import java.util.HashMap;
import java.util.Map;

@ViewScope
@SpringComponent
public class DoubleComparisonComponent<T extends XmlDocument> extends CustomComponent implements ContentPane {
    
    private static final long serialVersionUID = -826802129383432798L;
    private static final Logger LOG = LoggerFactory.getLogger(DoubleComparisonComponent.class);
    private static final String LEOS_RELATIVE_FULL_WDT = "100%";

    private EventBus eventBus;
    private MessageHelper messageHelper;
    private SecurityContext securityContext;
    private LeosDisplayField doubleComparisonContent;
    private Button exportToDocuwriteButton;
    private Button exportPackageButton;
    private Button syncScrollSwitch;
    private Button markedTextNextButton;
    private Button markedTextPrevButton;
    private Button downloadXmlFilesButton;
    private SimpleFileDownloader xmlFilesDownloader;
    private SimpleFileDownloader exportToDocuwriteDownloader;
    private Label versionLabel;
    private final ExportOptions exportOptions;

    public DoubleComparisonComponent(ExportOptions exportOptions, EventBus eventBus, MessageHelper messageHelper, SecurityContext securityContext) {
        this.exportOptions = exportOptions;
        this.eventBus = eventBus;
        this.messageHelper = messageHelper;
        this.securityContext = securityContext;

        setSizeFull();
        VerticalLayout doubleComparisonLayout = new VerticalLayout();
        doubleComparisonLayout.setSizeFull();
        doubleComparisonLayout.setSpacing(false);
        doubleComparisonLayout.setMargin(false);
        
        // create toolbar
        doubleComparisonLayout.addComponent(buildDoubleComparisonToolbar());
        // create content
        final Component comparisonContent = buildDoubleComparisonContent();

        doubleComparisonLayout.addComponent(comparisonContent);
        doubleComparisonLayout.setExpandRatio(comparisonContent, 1.0f);
        setCompositionRoot(doubleComparisonLayout);
        
        initExportToDocuwriteDownloader();
        initXmlFilesDownloader();
        hideCompareButtons();
        hideExportPackageButton();
        hideExportToDocuwriteButton();
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }
    
    @Override
    public void detach() {
        super.detach();
        eventBus.unregister(this);
    }
    
    private Component buildDoubleComparisonToolbar() {
        LOG.debug("Building double comparison toolbar...");

        // create text toolbar layout
        final HorizontalLayout toolsLayout = new HorizontalLayout();
        toolsLayout.setId("doubleComparisonToolbar");
        toolsLayout.setStyleName("leos-doubleComparison-bar");

        // set toolbar style
        toolsLayout.setWidth(LEOS_RELATIVE_FULL_WDT);

        //create sync scroll
        syncScrollSwitch();
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

        //create version selector
        versionLabel = new Label();
        versionLabel.setSizeUndefined();
        versionLabel.setContentMode(ContentMode.HTML);
        toolsLayout.addComponent(versionLabel);
        toolsLayout.setComponentAlignment(versionLabel, Alignment.MIDDLE_CENTER);
        toolsLayout.setExpandRatio(versionLabel,1.0f);

        downloadXmlFilesButton = downloadXmlFilesButton();
        toolsLayout.addComponent(downloadXmlFilesButton);
        toolsLayout.setComponentAlignment(downloadXmlFilesButton, Alignment.MIDDLE_RIGHT);

        exportPackageButton = exportPackageButton();
        toolsLayout.addComponent(exportPackageButton);
        toolsLayout.setComponentAlignment(exportPackageButton, Alignment.MIDDLE_RIGHT);

        // create print button
        exportToDocuwriteButton = exportToDocuwriteButton();
        toolsLayout.addComponent(exportToDocuwriteButton);
        toolsLayout.setComponentAlignment(exportToDocuwriteButton, Alignment.MIDDLE_RIGHT);

        Button closeButton = closeMarkedTextComponent();
        toolsLayout.addComponent(closeButton);
        toolsLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_RIGHT);
        
        return toolsLayout;
    }
    
    private Button closeMarkedTextComponent() {
        Button closeButton = new Button();
        closeButton.setDescription(messageHelper.getMessage("version.compare.close.button.description"));
        closeButton.addStyleName("link leos-toolbar-button");
        closeButton.setIcon(VaadinIcons.CLOSE_CIRCLE);
        closeButton.addClickListener(event -> eventBus.post(new ComparisonEvent(false)));
        return closeButton;
    }

    private void syncScrollSwitch() {
        VaadinIcons syncIcon = VaadinIcons.EXCHANGE;
        syncScrollSwitch = new Button();
        syncScrollSwitch.setIcon(syncIcon);
        syncScrollSwitch.setStyleName("link");
        syncScrollSwitch.addStyleName("leos-toolbar-button enable-sync");
        syncScrollSwitch.setData(true);
        syncScrollSwitch.setDescription(messageHelper.getMessage("leos.button.tooltip.disable.sync"), ContentMode.HTML);
        
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
    
    private void updateStyle(Button button, boolean syncState) {
        if(syncState) {
            button.removeStyleName("disable-sync");
            button.addStyleName("enable-sync");
        } else {
            button.removeStyleName("enable-sync");
            button.addStyleName("disable-sync");
        }
    }

    private Component buildDoubleComparisonContent() {
        doubleComparisonContent = new LeosDisplayField();
        doubleComparisonContent.setSizeFull();
        doubleComparisonContent.setId("leos-double-comparison-content");
        doubleComparisonContent.setStyleName("leos-double-comparison-content");
        
        new MathJaxExtension<>(doubleComparisonContent);
        new SoftActionsExtension<>(doubleComparisonContent);
        ScrollPaneExtension scrollPaneExtension = new ScrollPaneExtension(doubleComparisonContent, eventBus);
        scrollPaneExtension.getState().containerSelector = ".leos-double-comparison-content";

        SliderPinsExtension<LeosDisplayField> sliderPins = new SliderPinsExtension<>(doubleComparisonContent, getSelectorStyleMap());
        MarkedTextNavigationHelper navHelper = new MarkedTextNavigationHelper(sliderPins);
        this.eventBus.register(navHelper);

        return doubleComparisonContent;
    }
    
    private Button downloadXmlFilesButton() {
        Button downloadXmlFilesButton = new Button();
        downloadXmlFilesButton.setDescription(messageHelper.getMessage("leos.button.tooltip.download.xmlfiles"));
        downloadXmlFilesButton.addStyleName("link leos-toolbar-button");
        downloadXmlFilesButton.setIcon(VaadinIcons.CLOUD_DOWNLOAD);
        return downloadXmlFilesButton;
    }

    private Button exportPackageButton() {
        Button exportPackageButton = new Button();
        exportPackageButton.setDescription(messageHelper.getMessage("leos.button.tooltip.export.package"));
        exportPackageButton.setStyleName("link");
        exportPackageButton.addStyleName("leos-toolbar-button");
        exportPackageButton.setIcon(VaadinIcons.FILE_ADD);
        exportPackageButton.addClickListener((Button.ClickListener) event -> {
            ExportPackageWindow exportPackageWindow;
            if (exportOptions.getFileType().equals(Bill.class)) {
                exportPackageWindow = new LegalTextExportPackageWindow(messageHelper, eventBus, exportOptions);
            } else if (exportOptions.getFileType().equals(Annex.class)) {
                exportPackageWindow = new AnnexExportPackageWindow(messageHelper, eventBus, exportOptions);
            } else {
                throw new IllegalArgumentException("Document type not valid!!!");
            }
            UI.getCurrent().addWindow(exportPackageWindow);
            exportPackageWindow.center();
            exportPackageWindow.focus();
        });
        return exportPackageButton;
    }

    private Button exportToDocuwriteButton() {
        Button exportToDocuwriteButton = new Button();
        exportToDocuwriteButton.setDescription(messageHelper.getMessage("leos.button.tooltip.export.docuwrite"));
        exportToDocuwriteButton.addStyleName("link leos-toolbar-button");
        exportToDocuwriteButton.setIcon(VaadinIcons.DOWNLOAD);
        return exportToDocuwriteButton;
    }
    
    private void initExportToDocuwriteDownloader() {
        exportToDocuwriteDownloader = new SimpleFileDownloader();
        addExtension(exportToDocuwriteDownloader);
    
        exportToDocuwriteButton.addClickListener((Button.ClickListener) event -> {
            eventBus.post(new DocuWriteExportRequestEvent(exportOptions));
        });
    }
    
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        exportToDocuwriteDownloader.setFileDownloadResource(streamResource);
        exportToDocuwriteDownloader.download();
    }
    
    private void initXmlFilesDownloader() {
        xmlFilesDownloader = new SimpleFileDownloader();
        addExtension(xmlFilesDownloader);
    
        downloadXmlFilesButton.addClickListener((Button.ClickListener) event -> {
            eventBus.post(new DownloadXmlFilesRequestEvent(exportOptions));
        });
        showDownloadXmlFilesButton(false);
    }
    
    public void setDownloadStreamResourceForXmlFiles(StreamResource streamResource) {
        xmlFilesDownloader.setFileDownloadResource(streamResource);
        xmlFilesDownloader.download();
    }
    
    public void showDownloadXmlFilesButton(boolean visible){
        if(visible){
            boolean canDownload = securityContext.hasPermission(null, LeosPermission.CAN_DOWNLOAD_XML_COMPARISON);
            downloadXmlFilesButton.setVisible(canDownload);
        } else {
            downloadXmlFilesButton.setVisible(false);
        }
    }

    private Map<String, String> getSelectorStyleMap(){
        Map<String, String> selectorStyleMap = new HashMap<>();
        selectorStyleMap.put(".leos-double-compare-removed-intermediate","pin-leos-double-compare-removed-intermediate-hidden");
        selectorStyleMap.put(".leos-double-compare-added-intermediate","pin-leos-double-compare-added-intermediate-hidden");
        selectorStyleMap.put(".leos-double-compare-removed-original","pin-leos-double-compare-removed-original-hidden");
        selectorStyleMap.put(".leos-double-compare-added-original", "pin-leos-double-compare-added-original-hidden");
        selectorStyleMap.put(".leos-double-compare-removed","pin-leos-double-compare-removed-hidden");
        selectorStyleMap.put(".leos-double-compare-added", "pin-leos-double-compare-added-hidden");
        selectorStyleMap.put(".leos-content-removed", "pin-leos-content-removed-hidden");
        selectorStyleMap.put(".leos-content-new", "pin-leos-content-new-hidden");
        selectorStyleMap.put(".leos-content-removed-cn", "pin-leos-content-removed-hidden");
        selectorStyleMap.put(".leos-content-new-cn", "pin-leos-content-new-hidden");
        return selectorStyleMap;
    }

    public void populateMarkedContent(String markedContentText, LeosCategory leosCategory, String comparedInfo, ExportVersions<T> exportVersions) {
        populateDoubleComparisonContent(markedContentText.replaceAll("(?i) id=\"", " id=\"marked-"), leosCategory, comparedInfo, exportVersions);
    }

    public void populateDoubleComparisonContent(String comparisonContent, LeosCategory leosCategory, String comparedInfo, ExportVersions<T> exportVersions) {
        this.exportOptions.setExportVersions(exportVersions);
        doubleComparisonContent.addStyleName(leosCategory.name().toLowerCase());
        doubleComparisonContent.setValue(comparisonContent);
        versionLabel.setValue(comparedInfo);
        eventBus.post(new EnableSyncScrollRequestEvent(true));
    }

    private void hideCompareButtons() {
        markedTextNextButton.setVisible(false);
        markedTextPrevButton.setVisible(false);
        showDownloadXmlFilesButton(false);
    }

    private void showCompareButtons() {
        markedTextNextButton.setVisible(true);
        markedTextPrevButton.setVisible(true);
        showDownloadXmlFilesButton(true);
    }

    private void hideExportPackageButton() {
        exportPackageButton.setVisible(false);
    }

    private void showExportPackageButton() {
        exportPackageButton.setVisible(exportPackageButton.isEnabled());
    }

    public void enableExportPackage(boolean enableExportPackage) {
        exportPackageButton.setEnabled(enableExportPackage);
    }

    private void hideExportToDocuwriteButton() {
        exportToDocuwriteButton.setVisible(false);
    }
    
    private void showExportToDocuwriteButton() {
        exportToDocuwriteButton.setVisible(true);
    }
    
    public void setDoubleComparison() {
        showCompareButtons();
        showExportPackageButton();
        showExportToDocuwriteButton();
        doubleComparisonContent.removeStyleName("leos-simple-comparison-content");
        eventBus.post(new SyncScrollPrefixRequestEvent("doubleCompare-"));
    }
    
    public void setSimpleComparison() {
        showCompareButtons();
        showExportPackageButton();
        showExportToDocuwriteButton();
        doubleComparisonContent.addStyleName("leos-simple-comparison-content");
        eventBus.post(new SyncScrollPrefixRequestEvent("marked-"));
    }
    
    public void removeComparison() {
        hideCompareButtons();
        hideExportPackageButton();
        hideExportToDocuwriteButton();
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
}
