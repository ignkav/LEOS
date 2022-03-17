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
/*
* Copyright 2019 European Commission
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.ui.view.document;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.StreamResource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.doubleCompare.DoubleComparisonComponent;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.ui.window.toc.TocEditor;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.component.MenuBarComponent;
import eu.europa.ec.leos.web.ui.component.actions.LegalTextActionsMenuBar;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Provider;
import java.util.List;

@ViewScope
@SpringComponent
@Instance(InstanceType.COUNCIL)
public class MandateDocumentScreenImpl extends DocumentScreenImpl {
    private static final long serialVersionUID = 6711728542602337765L;

    private final DoubleComparisonComponent<Bill> doubleComparisonComponent;

    @Autowired
    MandateDocumentScreenImpl(UserHelper userHelper, SecurityContext securityContext, EventBus eventBus, ConfigurationHelper cfgHelper,
            MessageHelper messageHelper, TocEditor tocEditor, InstanceTypeResolver instanceTypeResolver,
            MenuBarComponent menuBarComponent, LeosPermissionAuthorityMapHelper authorityMapHelper, LegalTextActionsMenuBar legalTextActionMenuBar, ComparisonComponent<Bill> comparisonComponent,
            VersionsTab<Bill> versionsTab, Provider<StructureContext> structureContextProvider, PackageService packageService) {
        super(userHelper, securityContext, eventBus, cfgHelper, messageHelper, tocEditor, instanceTypeResolver, menuBarComponent, authorityMapHelper,
                legalTextActionMenuBar, comparisonComponent, versionsTab, structureContextProvider, packageService);

        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false);
        doubleComparisonComponent = new DoubleComparisonComponent<>(exportOptions, eventBus, messageHelper, securityContext);
    }

    @Override
    public void init() {
        super.init();
        buildDocumentPane();
        legalTextActionMenuBar.setChildComponentClass(DoubleComparisonComponent.class);
        legalTextPaneComponent.addPaneToLayout(comparisonComponent, 2, false);
        legalTextPaneComponent.layoutChildComponents();
        new SoftActionsExtension<>(legalTextPaneComponent.getContent());
    }

    @Override
    public void enableTocEdition(List<TableOfContentItemVO> tableOfContentItemVoList) {
        legalTextPaneComponent.handleTocEditRequestEvent(tableOfContentItemVoList, tocEditor);
    }

    @Subscribe
    void changePosition(LayoutChangeRequestEvent event) {
        changeLayout(event, doubleComparisonComponent);
    }

    @Override
    public void populateMarkedContent(String comparedContent, String comparedInfo, Bill original, Bill current) {
        ExportVersions<Bill> exportVersions = new ExportVersions<>(original, current);
        doubleComparisonComponent.populateMarkedContent(comparedContent, LeosCategory.BILL, comparedInfo, exportVersions);
        doubleComparisonComponent.setSimpleComparison();
    }
    
    @Override
    public void populateDoubleComparisonContent(String comparedContent, String comparedInfo, Bill original, Bill intermediate, Bill current) {
        ExportVersions<Bill> exportVersions = new ExportVersions<>(original, intermediate, current);
        doubleComparisonComponent.populateDoubleComparisonContent(comparedContent, LeosCategory.BILL, comparedInfo, exportVersions);
        doubleComparisonComponent.setDoubleComparison();
    }

    @Override
    public void showVersion(String content, String versionInfo){
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class));
        doubleComparisonComponent.populateDoubleComparisonContent(content.replaceAll("(?i) id=\"", " id=\"doubleCompare-"), LeosCategory.BILL, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }
    
    @Override
    public void cleanComparedContent() {
        final String versionInfo = messageHelper.getMessage("document.compare.version.caption.double");
        doubleComparisonComponent.populateDoubleComparisonContent("", LeosCategory.BILL, versionInfo, null);
        doubleComparisonComponent.removeComparison();
    }
    
    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        doubleComparisonComponent.setDownloadStreamResourceForExport(streamResource);
    }
    
    @Override
    public void setDownloadStreamResourceForMenu(DownloadStreamResource streamResource) {
        legalTextActionMenuBar.setDownloadStreamResource(streamResource);
    }
    
    @Override
    public void setDownloadStreamResourceForXmlFiles(StreamResource streamResource) {
        doubleComparisonComponent.setDownloadStreamResourceForXmlFiles(streamResource);
    }
    
    @Override
    public void scrollToMarkedChange(String elementId) {
    }

    @Override
    public void setPermissions(DocumentVO bill, boolean isClonedProposal) {
        super.setPermissions(bill, false);
        boolean enableExportPackage = securityContext.hasPermission(bill, LeosPermission.CAN_WORK_WITH_EXPORT_PACKAGE);
        legalTextPaneComponent.enableExportPackage(enableExportPackage);
        doubleComparisonComponent.enableExportPackage(enableExportPackage);
        legalTextActionMenuBar.setDownloadVersionVisible(true);
        legalTextActionMenuBar.setDownloadVersionWithAnnotationsVisible(true);
    }

    @Override
    public void populateCloneProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO) {
    }
}
