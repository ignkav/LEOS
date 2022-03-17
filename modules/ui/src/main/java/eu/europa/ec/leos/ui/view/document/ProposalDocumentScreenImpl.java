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
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.component.markedText.MarkedTextComponent;
import eu.europa.ec.leos.ui.component.versions.VersionsTab;
import eu.europa.ec.leos.ui.extension.SoftActionsExtension;
import eu.europa.ec.leos.ui.window.toc.TocEditor;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.view.document.CreateEventParameter;
import eu.europa.ec.leos.web.event.view.document.InstanceTypeResolver;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.component.MenuBarComponent;
import eu.europa.ec.leos.web.ui.component.actions.LegalTextActionsMenuBar;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Provider;
import java.util.List;

@ViewScope
@SpringComponent
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class ProposalDocumentScreenImpl extends DocumentScreenImpl {
    private static final long serialVersionUID = 3983015438446410548L;

    private MarkedTextComponent<Bill> markedTextComponent;

    private CloneContext cloneContext;

    @Autowired
    ProposalDocumentScreenImpl(UserHelper userHelper, SecurityContext securityContext, CloneContext cloneContext, EventBus eventBus, ConfigurationHelper cfgHelper,
                               MessageHelper messageHelper, TocEditor tocEditor, InstanceTypeResolver instanceTypeResolver,
                               MenuBarComponent menuBarComponent, LeosPermissionAuthorityMapHelper authorityMapHelper, LegalTextActionsMenuBar legalTextActionMenuBar,
                               ComparisonComponent<Bill> comparisonComponent, VersionsTab<Bill> versionsTab, Provider<StructureContext> structureContextProvider,
                               PackageService packageService, MarkedTextComponent<Bill> markedTextComponent) {
        super(userHelper, securityContext, eventBus, cfgHelper, messageHelper, tocEditor, instanceTypeResolver, menuBarComponent, authorityMapHelper,
                legalTextActionMenuBar, comparisonComponent, versionsTab, structureContextProvider, packageService);
        ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Bill.class, false);
        markedTextComponent.setExportOptions(exportOptions);
        this.markedTextComponent = markedTextComponent;
        this.cloneContext = cloneContext;
    }
    
    @Override
    public void init() {
        super.init();
        buildDocumentPane();
        legalTextActionMenuBar.setChildComponentClass(MarkedTextComponent.class);
        legalTextPaneComponent.addPaneToLayout(comparisonComponent, 2, false);
        legalTextPaneComponent.layoutChildComponents();
        legalTextActionMenuBar.setDownloadVersionVisible(false);
        legalTextActionMenuBar.setDownloadVersionWithAnnotationsVisible(false);
        new SoftActionsExtension<>(legalTextPaneComponent.getContent());
    }

    @Override
    public void showElementEditor(final String elementId, final String elementTagName, final String elementFragment, String alternatives) {
        CreateEventParameter eventParameterObjet = new CreateEventParameter(elementId, elementTagName, elementFragment,
                LeosCategory.BILL.name(), securityContext.getUser(),
                authorityMapHelper.getPermissionsForRoles(securityContext.getUser().getRoles()));
        eventParameterObjet.setAlternative(alternatives);
        eventParameterObjet.setCloneProposal(cloneContext.isClonedProposal());
        eventBus.post(instanceTypeResolver.createEvent(eventParameterObjet));
    }

    @Override
    public void enableTocEdition(List<TableOfContentItemVO> tocItemVoList) {
        legalTextPaneComponent.handleTocEditRequestEvent(tocItemVoList, tocEditor);
    }
    
    @Subscribe
    void changePosition(LayoutChangeRequestEvent event) {
        changeLayout(event, markedTextComponent);
    }

    @Override
    public void showVersion(String content, String versionInfo){
        changePosition(new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class));
        markedTextComponent.populateMarkedContent(content, LeosCategory.BILL, versionInfo, null);
        markedTextComponent.hideCompareButtons();
    }

    @Override
    public void populateMarkedContent(String comparedContent, String comparedInfo, Bill original, Bill current) {
        ExportVersions<Bill> exportVersions = new ExportVersions<>(original, current);
        markedTextComponent.populateMarkedContent(comparedContent, LeosCategory.BILL, comparedInfo, exportVersions);
        markedTextComponent.showCompareButtons();
    }
    
    @Override
    public void cleanComparedContent() {
        final String versionInfo = messageHelper.getMessage("document.compare.version.caption.simple");
        markedTextComponent.populateMarkedContent("", LeosCategory.BILL, versionInfo, null);
        markedTextComponent.hideCompareButtons();
    }
    
    @Override
    public void populateDoubleComparisonContent(String comparedContent, String versionInfo, Bill original, Bill intermediate, Bill current) {
        throw new IllegalArgumentException("Operation not valid");
    }
    
    @Override
    public void setDownloadStreamResourceForXmlFiles(StreamResource streamResource) {
        markedTextComponent.setDownloadStreamResourceForXmlFiles(streamResource);
    }
    
    @Override
    public void scrollToMarkedChange(String elementId) {
        markedTextComponent.scrollToMarkedChange(elementId);
    }

    @Override
    public void populateCloneProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO) {
        legalTextPaneComponent.populateCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    @Override
    public void setDownloadStreamResourceForExport(StreamResource streamResource) {
        markedTextComponent.setDownloadStreamResourceForExport(streamResource);
    }
}
