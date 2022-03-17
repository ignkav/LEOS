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

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.cmis.domain.ContentImpl;
import eu.europa.ec.leos.cmis.domain.SourceImpl;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosExportStatus;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.*;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.CheckinCommentVO;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.event.DocumentUpdatedByCoEditorEvent;
import eu.europa.ec.leos.model.event.ExportPackageCreatedEvent;
import eu.europa.ec.leos.model.event.UpdateUserInfoEvent;
import eu.europa.ec.leos.model.explanatory.ExplanatoryStructureType;
import eu.europa.ec.leos.model.messaging.UpdateInternalReferencesMessage;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.content.ReferenceLabelService;
import eu.europa.ec.leos.services.content.SearchService;
import eu.europa.ec.leos.services.content.processor.ExplanatoryProcessor;
import eu.europa.ec.leos.services.content.processor.DocumentContentService;
import eu.europa.ec.leos.services.content.processor.ElementProcessor;
import eu.europa.ec.leos.services.content.processor.TransformationService;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.util.CheckinCommentUtil;
import eu.europa.ec.leos.services.export.*;
import eu.europa.ec.leos.services.messaging.UpdateInternalReferencesProducer;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.store.ExportPackageService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.event.*;
import eu.europa.ec.leos.ui.event.doubleCompare.DocuWriteExportRequestEvent;
import eu.europa.ec.leos.ui.event.doubleCompare.DoubleCompareRequestEvent;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataResponse;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataResponse;
import eu.europa.ec.leos.ui.event.search.*;
import eu.europa.ec.leos.ui.event.toc.*;
import eu.europa.ec.leos.ui.event.view.DownloadXmlFilesRequestEvent;
import eu.europa.ec.leos.ui.model.AnnotateMetadata;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.ui.view.AbstractLeosPresenter;
import eu.europa.ec.leos.ui.view.CommonDelegate;
import eu.europa.ec.leos.ui.view.ComparisonDelegate;
import eu.europa.ec.leos.ui.view.ComparisonDisplayMode;
import eu.europa.ec.leos.usecases.document.ExplanatoryContext;
import eu.europa.ec.leos.usecases.document.BillContext;
import eu.europa.ec.leos.usecases.document.CollectionContext;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.NotificationEvent.Type;
import eu.europa.ec.leos.web.event.component.*;
import eu.europa.ec.leos.web.event.view.document.*;
import eu.europa.ec.leos.web.event.window.CancelElementEditorEvent;
import eu.europa.ec.leos.web.event.window.CloseElementEditorEvent;
import eu.europa.ec.leos.web.event.window.ShowTimeLineWindowEvent;
import eu.europa.ec.leos.web.model.TocAndAncestorsVO;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.navigation.Target;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import io.atlassian.fugue.Option;
import io.atlassian.fugue.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.*;

@Component
@Scope("prototype")
class ExplanatoryPresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(ExplanatoryPresenter.class);

    private final ExplanatoryScreen explanatoryScreen;
    private final ExplanatoryService explanatoryService;
    private final ElementProcessor<Explanatory> elementProcessor;
    private final ExplanatoryProcessor explanatoryProcessor;
    private final DocumentContentService documentContentService;
    private final UrlBuilder urlBuilder;
    private final ComparisonDelegate<Explanatory> comparisonDelegate;
    private final UserHelper userHelper;
    private final MessageHelper messageHelper;
    private final Provider<CollectionContext> proposalContextProvider;
    private final CoEditionHelper coEditionHelper;
    private final ExportService exportService;
    private final Provider<BillContext> billContextProvider;
    private final Provider<StructureContext> structureContextProvider;
    private final ReferenceLabelService referenceLabelService;
    private final UpdateInternalReferencesProducer updateInternalReferencesProducer;
    private final TransformationService transformationService;
    private final LegService legService;
    private final ProposalService proposalService;
    private final SearchService searchService;
    private final ExportPackageService exportPackageService;
    private final NotificationService notificationService;
    private final CloneContext cloneContext;

    private String strDocumentVersionSeriesId;
    private String documentId;
    private String documentRef;
    private Element elementToEditAfterClose;
    private boolean comparisonMode;
    private String proposalRef;
    private String connectedEntity;
    private final CommonDelegate<Explanatory> commonDelegate;

    private CloneProposalMetadataVO cloneProposalMetadataVO;

    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Autowired
    ExplanatoryPresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus,
                         ExplanatoryScreen explanatoryScreen,
                         ExplanatoryService explanatoryService, PackageService packageService, ExportService exportService,
                         Provider<BillContext> billContextProvider, Provider<ExplanatoryContext> explanatoryContextProvider, ElementProcessor<Explanatory> elementProcessor,
                         ExplanatoryProcessor explanatoryProcessor, DocumentContentService documentContentService, UrlBuilder urlBuilder,
                         ComparisonDelegate<Explanatory> comparisonDelegate, UserHelper userHelper,
                         MessageHelper messageHelper, ConfigurationHelper cfgHelper, Provider<CollectionContext> proposalContextProvider,
                         CoEditionHelper coEditionHelper, EventBus leosApplicationEventBus, UuidHelper uuidHelper,
                         Provider<StructureContext> structureContextProvider, ReferenceLabelService referenceLabelService, WorkspaceService workspaceService,
                         UpdateInternalReferencesProducer updateInternalReferencesProducer, TransformationService transformationService, LegService legService,
                         ProposalService proposalService, SearchService searchService, ExportPackageService exportPackageService,
                         NotificationService notificationService, CommonDelegate<Explanatory> commonDelegate, CloneContext cloneContext) {
        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        LOG.trace("Initializing explanatory presenter...");
        this.explanatoryScreen = explanatoryScreen;
        this.explanatoryService = explanatoryService;
        this.elementProcessor = elementProcessor;
        this.explanatoryProcessor = explanatoryProcessor;
        this.documentContentService = documentContentService;
        this.urlBuilder = urlBuilder;
        this.comparisonDelegate = comparisonDelegate;
        this.userHelper = userHelper;
        this.messageHelper = messageHelper;
        this.proposalContextProvider = proposalContextProvider;
        this.coEditionHelper = coEditionHelper;
        this.exportService = exportService;
        this.billContextProvider = billContextProvider;
        this.structureContextProvider = structureContextProvider;
        this.referenceLabelService = referenceLabelService;
        this.updateInternalReferencesProducer = updateInternalReferencesProducer;
        this.transformationService = transformationService;
        this.legService = legService;
        this.proposalService = proposalService;
        this.searchService = searchService;
        this.exportPackageService = exportPackageService;
        this.notificationService = notificationService;
        this.commonDelegate = commonDelegate;
        this.cloneContext = cloneContext;
    }

    @Override
    public void enter() {
        super.enter();
        init();
    }

    @Override
    public void detach() {
        super.detach();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    private void init() {
        try {
            populateWithProposalRefAndConnectedEntity();
            populateViewData(TocMode.SIMPLIFIED);
            populateVersionsData();
        } catch (Exception exception) {
            LOG.error("Exception occurred in init(): ", exception);
            eventBus.post(new NotificationEvent(Type.INFO, "unknown.error.message"));
        }
    }

    private void populateWithProposalRefAndConnectedEntity() {
        Explanatory explanatory = getDocument();
        if (explanatory != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(explanatory.getId());
            Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
            proposalRef = proposal.getMetadata().get().getRef();
            connectedEntity = userHelper.getCollaboratorConnectedEntityByLoggedUser(proposal.getCollaborators());
            byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
            if(proposal != null && proposal.isClonedProposal()) {
                populateCloneProposalMetadataVO(xmlContent);
            }
        }
    }

    private void populateCloneProposalMetadataVO(byte[] xmlContent) {
        cloneProposalMetadataVO = proposalService.getClonedProposalMetadata(xmlContent);
        explanatoryScreen.populateCloneProposalMetadataVO(cloneProposalMetadataVO);
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private void resetCloneProposalMetadataVO() {
        explanatoryScreen.populateCloneProposalMetadataVO(null);
        cloneContext.setCloneProposalMetadataVO(null);
    }

    private String getDocumentRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.EXPLANATORY_REF.name());
    }

    private Explanatory getDocument() {
        documentRef = getDocumentRef();
        Explanatory explanatory = explanatoryService.findExplanatoryByRef(documentRef);
        strDocumentVersionSeriesId = explanatory.getVersionSeriesId();
        documentId = explanatory.getId();
        structureContextProvider.get().useDocumentTemplate(explanatory.getMetadata().getOrError(() -> "Explanatory metadata is required!").getDocTemplate());
        return explanatory;
    }

    private void populateViewData(TocMode mode) {
        try{
            Explanatory explanatory = getDocument();
            Option<ExplanatoryMetadata> explanatoryMetadata = explanatory.getMetadata();
            if (explanatoryMetadata.isDefined()) {
                explanatoryScreen.setTitle(messageHelper.getMessage("document.explanatory.title.default"));
            }
            explanatoryScreen.setDocumentVersionInfo(getVersionInfo(explanatory));
            explanatoryScreen.setContent(getEditableXml(explanatory));
            explanatoryScreen.setToc(getTableOfContent(explanatory, mode));
            DocumentVO explanatoryVO = createExplanatoryVO(explanatory);
            explanatoryScreen.updateUserCoEditionInfo(coEditionHelper.getCurrentEditInfo(explanatory.getVersionSeriesId()), id);
            explanatoryScreen.setPermissions(explanatoryVO);
            explanatoryScreen.initAnnotations(explanatoryVO, proposalRef, connectedEntity);
        }
        catch (Exception ex) {
            LOG.error("Error while processing document", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    private void populateVersionsData() {
        final List<VersionVO> allVersions = explanatoryService.getAllVersions(documentId, documentRef);
        explanatoryScreen.setDataFunctions(
                allVersions,
                this::majorVersionsFn, this::countMajorVersionsFn,
                this::minorVersionsFn, this::countMinorVersionsFn,
                this::recentChangesFn, this::countRecentChangesFn);
    }

    @Subscribe
    public void updateVersionsTab(DocumentUpdatedEvent event) {
        final List<VersionVO> allVersions = explanatoryService.getAllVersions(documentId, documentRef);
        explanatoryScreen.refreshVersions(allVersions, comparisonMode);
    }

    private Integer countMinorVersionsFn(String currIntVersion) {
        return explanatoryService.findAllMinorsCountForIntermediate(documentRef, currIntVersion);
    }

    private List<Explanatory> minorVersionsFn(String currIntVersion, int startIndex, int maxResults) {
        return explanatoryService.findAllMinorsForIntermediate(documentRef, currIntVersion, startIndex, maxResults);
    }

    private Integer countMajorVersionsFn() {
        return explanatoryService.findAllMajorsCount(documentRef);
    }

    private List<Explanatory> majorVersionsFn(int startIndex, int maxResults) {
        return explanatoryService.findAllMajors(documentRef, startIndex, maxResults);
    }

    private Integer countRecentChangesFn() {
        return explanatoryService.findRecentMinorVersionsCount(documentId, documentRef);
    }

    private List<Explanatory> recentChangesFn(int startIndex, int maxResults) {
        return explanatoryService.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    private List<TableOfContentItemVO> getTableOfContent(Explanatory explanatory, TocMode mode) {
        return explanatoryService.getTableOfContent(explanatory, mode);
    }

    @Subscribe
    void downloadActualVersion(DownloadActualVersionRequestEvent event) {
        try {
            XmlDocument original = documentContentService.getOriginalExplanatory(getDocument());
            ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Explanatory.class, false);
            exportOptions.setExportVersions(new ExportVersions<>(original, getDocument()));
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();

            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";
            byte[] exportedBytes = exportService.createDocuWritePackage(jobFileName, proposalId, exportOptions);
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
            explanatoryScreen.setDownloadStreamResourceForMenu(downloadStreamResource);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
        }
    }

    @Subscribe
    void downloadXmlFiles(DownloadXmlFilesRequestEvent event) {
        File zipFile = null;
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final Map<String, Object> contentToZip = new HashMap<>();

            final ExportVersions<Explanatory> exportVersions = event.getExportOptions().getExportVersions();
            final Explanatory current = exportVersions.getCurrent();
            final Explanatory original = exportVersions.getOriginal();
            final Explanatory intermediate = exportVersions.getIntermediate();

            final String leosComparedContent;
            final String docuWriteComparedContent;
            final String comparedInfo;
            if(intermediate != null){
                comparedInfo = messageHelper.getMessage("version.compare.double", original.getVersionLabel(), intermediate.getVersionLabel(), current.getVersionLabel());
                contentToZip.put(intermediate.getMetadata().get().getRef() + "_v" + intermediate.getVersionLabel() + ".xml",
                        intermediate.getContent().get().getSource().getBytes());
                leosComparedContent = comparisonDelegate.doubleCompareHtmlContents(original, intermediate, current, true);
                docuWriteComparedContent = legService.doubleCompareXmlContents(original, intermediate, current, false);
            } else {
                comparedInfo = messageHelper.getMessage("version.compare.simple", original.getVersionLabel(), current.getVersionLabel());
                leosComparedContent = comparisonDelegate.getMarkedContent(original, current);
                docuWriteComparedContent = legService.simpleCompareXmlContents(original, current, true);
            }
            final String zipFileName = original.getMetadata().get().getRef() + "_" + comparedInfo + ".zip";

            contentToZip.put(current.getMetadata().get().getRef() + "_v" + current.getVersionLabel() + ".xml", current.getContent().get().getSource().getBytes());
            contentToZip.put(original.getMetadata().get().getRef() + "_v" + original.getVersionLabel() + ".xml", original.getContent().get().getSource().getBytes());
            contentToZip.put("comparedContent_leos.xml", leosComparedContent);
            contentToZip.put("comparedContent_docuwrite.xml", docuWriteComparedContent);
            zipFile = ZipPackageUtil.zipFiles(zipFileName, contentToZip);

            final byte[] zipBytes = FileUtils.readFileToByteArray(zipFile);
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(zipFile.getName(), new ByteArrayInputStream(zipBytes));
            explanatoryScreen.setDownloadStreamResourceForXmlFiles(downloadStreamResource);
            LOG.info("Xml files for Explanatory {}, downloaded in {} milliseconds ({} sec)", current.getName(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloadXmlFiles", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", e.getMessage()));
        }  finally {
            if(zipFile != null) {
                zipFile.delete();
            }
        }
    }

    @Subscribe
    void downloadXmlVersion(DownloadXmlVersionRequestEvent event) {
        try {
            final Explanatory chosenDocument = explanatoryService.findExplanatoryVersion(event.getVersionId());
            final String fileName = chosenDocument.getMetadata().get().getRef() + "_v" + chosenDocument.getVersionLabel() + ".xml";

            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(fileName, new ByteArrayInputStream(chosenDocument.getContent().get().getSource().getBytes()));
            explanatoryScreen.setDownloadStreamResourceForVersion(downloadStreamResource, chosenDocument.getId());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloadXmlVersion", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", e.getMessage()));
        }
    }

    @Subscribe
    void exportToDocuWrite(DocuWriteExportRequestEvent event) {
        try {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";
            byte[] exportedBytes = exportService.createDocuWritePackage(jobFileName, proposalId, event.getExportOptions());
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
            explanatoryScreen.setDownloadStreamResourceForExport(downloadStreamResource);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
        }
    }

    @Subscribe
    void exportToDocuWriteCleanVersion(ExportToDocuWriteCleanVersion event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_CLEAN_" + System.currentTimeMillis() + ".zip";
            ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Explanatory.class, false, true);
            byte[] exportedBytes = exportService.createDocuWritePackage(jobFileName, proposalId, exportOptions);
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
            explanatoryScreen.setDownloadStreamResourceForMenu(downloadStreamResource);
            LOG.info("The actual version of CLEANED Bill for proposal {}, downloaded in {} milliseconds ({} sec)", proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
        }
    }

    @Subscribe
    void createExportPackageForActualVersion(CreateExportPackageActualVersionRequestEvent event) {
        ExportDocument exportDocument = null;
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final Explanatory currentDocument = getDocument();
            XmlDocument original = documentContentService.getOriginalExplanatory(currentDocument);
            ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Explanatory.class, false);
            exportOptions.setExportVersions(new ExportVersions(original, currentDocument));
            exportOptions.setRelevantElements(event.getRelevantElements());
            exportOptions.setComments(getCommentsForExportPackage(event.getTitle(), exportOptions));
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";
            byte[] exportedBytes = exportService.createExportPackage(jobFileName, proposalId, exportOptions);
            exportDocument = exportPackageService.createExportDocument(proposalId, exportOptions.getComments(), exportedBytes);
            notificationService.sendNotification(proposalRef, exportDocument.getId());
            exportDocument = exportPackageService.updateExportDocument(exportDocument.getId(), LeosExportStatus.NOTIFIED);
            eventBus.post(new NotificationEvent("document.export.package.window.title", "document.export.package.creation.success", Type.TRAY));
            LOG.info("Export Package {} for proposal {} created in {} milliseconds ({} sec)", exportDocument.getName(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while generating Export Package", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.package.error.message", e.getMessage()));
        } finally {
            if (exportDocument != null) {
                leosApplicationEventBus.post(new ExportPackageCreatedEvent(proposalRef, exportDocument));
            }
        }
    }

    @Subscribe
    void createExportPackageCleanVersion(CreateExportPackageCleanVersionRequestEvent event) {
        ExportDocument exportDocument = null;
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            ExportOptions exportOptions = new ExportDW(ExportOptions.Output.WORD, Explanatory.class, false, true);
            exportOptions.setRelevantElements(event.getRelevantElements());
            exportOptions.setComments(getCommentsForExportPackage(event.getTitle(), exportOptions));
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_CLEAN_" + System.currentTimeMillis() + ".zip";
            byte[] exportedBytes = exportService.createExportPackage(jobFileName, proposalId, exportOptions);
            exportDocument = exportPackageService.createExportDocument(proposalId, exportOptions.getComments(), exportedBytes);
            notificationService.sendNotification(proposalRef, exportDocument.getId());
            exportDocument = exportPackageService.updateExportDocument(exportDocument.getId(), LeosExportStatus.NOTIFIED);
            eventBus.post(new NotificationEvent("document.export.package.window.title", "document.export.package.creation.success", Type.TRAY));
            LOG.info("Export Package {} for proposal {} created in {} milliseconds ({} sec)", exportDocument.getName(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while generating Export Package", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.package.error.message", e.getMessage()));
        } finally {
            if (exportDocument != null) {
                leosApplicationEventBus.post(new ExportPackageCreatedEvent(proposalRef, exportDocument));
            }
        }
    }

    @Subscribe
    void createExportPackage(CreateExportPackageRequestEvent event) {
        ExportDocument exportDocument = null;
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            ExportOptions exportOptions = event.getExportOptions();
            exportOptions.setComments(getCommentsForExportPackage(event.getTitle(), exportOptions));
            LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            String proposalId = context.getProposalId();
            final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";
            byte[] exportedBytes = exportService.createExportPackage(jobFileName, proposalId, exportOptions);
            exportDocument = exportPackageService.createExportDocument(proposalId, exportOptions.getComments(), exportedBytes);
            notificationService.sendNotification(proposalRef, exportDocument.getId());
            exportDocument = exportPackageService.updateExportDocument(exportDocument.getId(), LeosExportStatus.NOTIFIED);
            eventBus.post(new NotificationEvent("document.export.package.window.title", "document.export.package.creation.success", Type.TRAY));
            LOG.info("Export Package {} for proposal {} created in {} milliseconds ({} sec)", exportDocument.getName(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while generating Export Package", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "export.package.error.message", e.getMessage()));
        } finally {
            if (exportDocument != null) {
                leosApplicationEventBus.post(new ExportPackageCreatedEvent(proposalRef, exportDocument));
            }
        }
    }

    private List<String> getCommentsForExportPackage(String title, ExportOptions exportOptions) {
        List<String> comments = new ArrayList<>();
        comments.add(title);
        comments.add(messageHelper.getMessage("document.export.package.creation.comment.explanatory"));
        if (exportOptions.isComparisonMode()) {
            StringBuilder versionsComment = new StringBuilder();
            versionsComment.append(exportOptions.getExportVersions().getOriginal() != null ? getCommentForVersion(exportOptions.getExportVersions().getOriginal()) : "");
            versionsComment.append(exportOptions.getExportVersions().getIntermediate() != null ? " vs " + getCommentForVersion(exportOptions.getExportVersions().getIntermediate()) : "");
            versionsComment.append(exportOptions.getExportVersions().getCurrent() != null ? " vs " + getCommentForVersion(exportOptions.getExportVersions().getCurrent()) : "");
            comments.add(versionsComment.toString());
        } else {
            comments.add(getCommentForVersion(getDocument()));
        }
        return comments;
    }

    private String getCommentForVersion(XmlDocument document) {
        StringBuilder versionsComment = new StringBuilder(document.getVersionLabel());
        if (document.getVersionType().equals(VersionType.INTERMEDIATE) && document.getVersionComment() != null) {
            final CheckinCommentVO checkinCommentVO = CheckinCommentUtil.getJavaObjectFromJson(document.getVersionComment());
            versionsComment.append(" (").append(checkinCommentVO.getTitle()).append(") ");
        }
        return versionsComment.toString();
    }

    @Subscribe
    void getDocumentVersionsList(VersionListRequestEvent<Explanatory> event) {
        List<Explanatory> explanatoryVersions = explanatoryService.findVersions(documentId);
        eventBus.post(new VersionListResponseEvent<Explanatory>(new ArrayList<>(explanatoryVersions)));
    }

    private String getEditableXml(Explanatory document) {
        documentContentService.useCloneProposalMetadataVO(cloneProposalMetadataVO);
        return documentContentService.toEditableContent(document,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext);
    }

    private byte[] getContent(Explanatory explanatory) {
        final Content content = explanatory.getContent().getOrError(() -> "Explanatory content is required!");
        return content.getSource().getBytes();
    }


    @Subscribe
    void handleCloseDocument(CloseDocumentEvent event) {
        LOG.trace("Handling close document request...");

        //if unsaved changes remain in the session, first ask for confirmation
        if(isExplanatoryUnsaved()){
            eventBus.post(new ShowConfirmDialogEvent(event, null));
            return;
        }

        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
        eventBus.post(new NavigationRequestEvent(Target.PREVIOUS));
    }

    private boolean isExplanatoryUnsaved(){
        return getExplanatoryFromSession() != null;
    }

    private Explanatory getExplanatoryFromSession() {
        return (Explanatory) httpSession.getAttribute("explanatory#" + getDocumentRef());
    }

    @Subscribe
    void handleCloseBrowserRequest(CloseBrowserRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    @Subscribe
    void handleCloseScreenRequest(CloseScreenRequestEvent event) {
        if (explanatoryScreen.isTocEnabled()) {
            eventBus.post(new CloseEditTocEvent());
        } else {
            eventBus.post(new CloseDocumentEvent());
        }
    }

    @Subscribe
    void refreshDocument(RefreshDocumentEvent event) {
        populateViewData(event.getTocMode());
    }

    @Subscribe
    void refreshToc(RefreshTocEvent event) {
        try {
            Explanatory explanatory = getDocument();
            explanatoryScreen.setToc(getTableOfContent(explanatory, event.getTocMode()));
        } catch (Exception ex) {
            LOG.error("Error while refreshing TOC", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void deleteElement(DeleteElementRequestEvent event){
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            Explanatory explanatory = getDocument();
            String tagName = event.getElementTagName();
            byte[] updatedXmlContent = explanatoryProcessor.deleteExplanatoryBlock(explanatory, event.getElementId(), tagName);

            // save document into repository
            explanatory = explanatoryService.updateExplanatory(explanatory, updatedXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.explanatory.block.deleted"));
            if (explanatory != null) {
                eventBus.post(new NotificationEvent(Type.INFO, "document.explanatory.block.deleted", tagName.equalsIgnoreCase(LEVEL) ? StringUtils.capitalize(POINT) : StringUtils.capitalize(tagName)));
                eventBus.post(new RefreshDocumentEvent());
                eventBus.post(new DocumentUpdatedEvent());
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
                updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(explanatory.getId(), explanatory.getMetadata().get().getRef(), id));
            }
            LOG.info("Element '{}' in Explanatory {} id {}, deleted in {} milliseconds ({} sec)", event.getElementId(), explanatory.getName(), explanatory.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        }
        catch (Exception ex){
            LOG.error("Exception while deleting element operation for ", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void insertElement(InsertElementRequestEvent event){
        Stopwatch stopwatch = Stopwatch.createStarted();
        String tagName = event.getElementTagName();
        Explanatory explanatory = getDocument();
        byte[] updatedXmlContent = explanatoryProcessor.insertExplanatoryBlock(explanatory, event.getElementId(), tagName, InsertElementRequestEvent.POSITION.BEFORE.equals(event.getPosition()));

        explanatory = explanatoryService.updateExplanatory(explanatory, updatedXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.explanatory.block.inserted"));
        if (explanatory != null) {
            eventBus.post(new NotificationEvent(Type.INFO, "document.explanatory.block.inserted",  tagName.equalsIgnoreCase(LEVEL) ? StringUtils.capitalize(POINT) : StringUtils.capitalize(tagName)));
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(explanatory.getId(), explanatory.getMetadata().get().getRef(), id));
        }
        LOG.info("New Element of type '{}' inserted in Explanatory {} id {}, in {} milliseconds ({} sec)", tagName, explanatory.getName(), explanatory.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void mergeElement(MergeElementRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            String elementId = event.getElementId();
            String tagName = event.getElementTagName();
            String elementContent = event.getElementContent();

            Explanatory explanatory = getDocument();
            Element mergeOnElement = explanatoryProcessor.getMergeOnElement(explanatory, elementContent, tagName, elementId);
            if (mergeOnElement != null) {
                byte[] newXmlContent = explanatoryProcessor.mergeElement(explanatory, elementContent, tagName, elementId);
                explanatory = explanatoryService.updateExplanatory(explanatory, newXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.element.updated", org.apache.commons.lang3.StringUtils.capitalize(tagName)));
                if (explanatory != null) {
                    elementToEditAfterClose = mergeOnElement;
                    eventBus.post(new CloseElementEvent());
                    eventBus.post(new NotificationEvent(Type.INFO, "document.content.updated"));
                    eventBus.post(new DocumentUpdatedEvent());
                    leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
                }
            } else {
                explanatoryScreen.showAlertDialog("operation.element.not.performed");
            }
            LOG.info("Element '{}' merged into '{}' in Explanatory {} id {}, in {} milliseconds ({} sec)", elementId, mergeOnElement.getElementId(), explanatory.getName(), explanatory.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error in mergeElement", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
        }
    }

    @Subscribe
    void checkElementCoEdition(CheckElementCoEditionEvent event) {
        try {
            if (event.getAction().equals(CheckElementCoEditionEvent.Action.MERGE)) {
                Explanatory explanatory = getDocument();
                Element mergeOnElement = explanatoryProcessor.getMergeOnElement(explanatory, event.getElementContent(), event.getElementTagName(), event.getElementId());
                if (mergeOnElement != null) {
                    explanatoryScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                            mergeOnElement.getElementId(), mergeOnElement.getElementTagName(), event.getAction(), event.getActionEvent());
                } else {
                    explanatoryScreen.showAlertDialog("operation.element.not.performed");
                }
            } else {
                Explanatory explanatory = getDocument();
                Element tocElement = explanatoryProcessor.getTocElement(explanatory, event.getElementId(), getTableOfContent(explanatory, TocMode.SIMPLIFIED));
                explanatoryScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                        tocElement.getElementId(), tocElement.getElementTagName(), event.getAction(), event.getActionEvent());
            }
        } catch (Exception e) {
            LOG.error("Unexpected error in checkElementCoEdition", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
        }
    }

    @Subscribe
    void cancelElementEditor(CancelElementEditorEvent event) {
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);

        //load content from session if exists
        Explanatory explanatoryFromSession = getExplanatoryFromSession();
        if(explanatoryFromSession != null) {
            explanatoryScreen.setContent(getEditableXml(explanatoryFromSession));
        }else{
            eventBus.post(new RefreshDocumentEvent());
        }
        LOG.debug("User edit information removed");
    }


    @Subscribe
    void editElement(EditElementRequestEvent event){
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        elementToEditAfterClose = null;
        LOG.trace("Handling edit element request... for {},id={}",elementTagName , elementId );
        try {
            //show confirm dialog if there is any unsaved replaced text
            //it can be detected from the session attribute
            if(isExplanatoryUnsaved()){
                eventBus.post(new ShowConfirmDialogEvent(event, new CancelElementEditorEvent(event.getElementId(),event.getElementTagName())));
                return;
            }

            Explanatory explanatory = getDocument();
            String element = elementProcessor.getElement(explanatory, elementTagName, elementId);
            coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
            explanatoryScreen.showElementEditor(elementId, elementTagName, element);
        }
        catch (Exception ex){
            LOG.error("Exception while edit element operation for ", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void saveElement(SaveElementRequestEvent event){
        Stopwatch stopwatch = Stopwatch.createStarted();
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        String elementContent = event.getElementContent();
        elementToEditAfterClose = null;
        LOG.trace("Handling save element request... for {},id={}",elementTagName , elementId );

        try {
            Explanatory explanatory = getDocument();
            byte[] updatedXmlContent = explanatoryProcessor.updateExplanatoryBlock(explanatory, elementId, elementTagName, elementContent);
            if (updatedXmlContent == null) {
                explanatoryScreen.showAlertDialog("operation.element.not.performed");
                return;
            }

            if (explanatory != null) {
                Pair<byte[], Element> splittedContent = null;
                if (!event.isSaveAndClose() && checkIfCloseElementEditor(elementTagName, event.getElementContent())) {
                    splittedContent = explanatoryProcessor.getSplittedElement(updatedXmlContent, event.getElementContent(), elementTagName, elementId);
                    if (splittedContent != null) {
                        elementToEditAfterClose = splittedContent.right();
                        if(splittedContent.left() != null){
                            updatedXmlContent = splittedContent.left();
                        }
                        eventBus.post(new CloseElementEvent());
                    }
                }
                explanatory = explanatoryService.updateExplanatory(explanatory, updatedXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.explanatory.block.updated"));
                if (splittedContent == null) {
                    String newElementContent = elementProcessor.getElement(explanatory, elementTagName, elementId);
                    explanatoryScreen.refreshElementEditor(elementId, elementTagName, newElementContent);
                }
                eventBus.post(new NotificationEvent(Type.INFO, "document.content.updated"));
                eventBus.post(new DocumentUpdatedEvent());
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            }
            LOG.info("Element '{}' in Explanatory {} id {}, saved in {} milliseconds ({} sec)", elementId, explanatory.getName(), explanatory.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception ex) {
            LOG.error("Exception while save explanatory operation", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    boolean checkIfCloseElementEditor(String elementTagName, String elementContent) {
        switch (elementTagName) {
            case SUBPARAGRAPH:
            case CONTENT:
            case SUBPOINT:
                return elementContent.contains("<" + elementTagName + ">");
            case PARAGRAPH:
                return elementContent.contains("<paragraph>") || elementContent.contains("<subparagraph>");
            case LEVEL:
                return elementContent.contains("<level>") || elementContent.contains("<subparagraph>");
            case POINT:
                return elementContent.contains("<alinea>");
            case INDENT:
                return elementContent.contains("<alinea>");
            default:
                return false;
        }
    }

    @Subscribe
    void closeExplanatoryBlock(CloseElementEditorEvent event){
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
        LOG.debug("User edit information removed");
        eventBus.post(new RefreshDocumentEvent());
        if (elementToEditAfterClose != null) {
            explanatoryScreen.scrollTo(elementToEditAfterClose.getElementId());
            eventBus.post(new EditElementRequestEvent(elementToEditAfterClose.getElementId(), elementToEditAfterClose.getElementTagName()));
        }
    }

    @Subscribe
    void showTimeLineWindow(ShowTimeLineWindowEvent event) {
        List<Explanatory> documentVersions = explanatoryService.findVersions(documentId);
        explanatoryScreen.showTimeLineWindow(documentVersions);
    }

    @Subscribe
    void cleanComparedContent(CleanComparedContentEvent event) {
        explanatoryScreen.cleanComparedContent();
    }

    @Subscribe
    void showVersion(ShowVersionRequestEvent event) {
        final Explanatory version = explanatoryService.findExplanatoryVersion(event.getVersionId());
        final String versionContent = comparisonDelegate.getDocumentAsHtml(version);
        final String versionInfo = getVersionInfoAsString(version);
        explanatoryScreen.showVersion(versionContent, versionInfo);
    }

    @Subscribe
    public void fetchMilestoneByVersionedReference(FetchMilestoneByVersionedReferenceEvent event) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        LegDocument legDocument = legService.findLastLegByVersionedReference(leosPackage.getPath(), event.getVersionedReference());
        explanatoryScreen.showMilestoneExplorer(legDocument, String.join(",", legDocument.getMilestoneComments()), proposalRef);
    }

    @Subscribe
    void getCompareContentForTimeLine(CompareTimeLineRequestEvent event) {
        final Explanatory oldVersion = explanatoryService.findExplanatoryVersion(event.getOldVersion());
        final Explanatory newVersion = explanatoryService.findExplanatoryVersion(event.getNewVersion());
        final ComparisonDisplayMode displayMode = event.getDisplayMode();
        HashMap<ComparisonDisplayMode, Object> result = comparisonDelegate.versionCompare(oldVersion, newVersion, displayMode);
        explanatoryScreen.displayComparison(result);
    }

    @Subscribe
    void compare(CompareRequestEvent event) {
        final Explanatory oldVersion = explanatoryService.findExplanatoryVersion(event.getOldVersionId());
        final Explanatory newVersion = explanatoryService.findExplanatoryVersion(event.getNewVersionId());
        String comparedContent = comparisonDelegate.getMarkedContent(oldVersion, newVersion);
        final String comparedInfo = messageHelper.getMessage("version.compare.simple", oldVersion.getVersionLabel(), newVersion.getVersionLabel());
        explanatoryScreen.populateComparisonContent(comparedContent, comparedInfo, oldVersion, newVersion);
    }

    @Subscribe
    void doubleCompare(DoubleCompareRequestEvent event) {
        final Explanatory original = explanatoryService.findExplanatoryVersion(event.getOriginalProposalId());
        final Explanatory intermediate = explanatoryService.findExplanatoryVersion(event.getIntermediateMajorId());
        final Explanatory current = explanatoryService.findExplanatoryVersion(event.getCurrentId());
        String resultContent = comparisonDelegate.doubleCompareHtmlContents(original, intermediate, current, true);
        final String comparedInfo = messageHelper.getMessage("version.compare.double", original.getVersionLabel(), intermediate.getVersionLabel(), current.getVersionLabel());
        explanatoryScreen.populateDoubleComparisonContent(resultContent, comparedInfo, original, intermediate, current);
    }

    @Subscribe
    void versionRestore(RestoreVersionRequestEvent event) {
        final Explanatory version = explanatoryService.findExplanatoryVersion(event.getVersionId());
        final byte[] resultXmlContent = getContent(version);
        explanatoryService.updateExplanatory(getDocument(), resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.restore.version", version.getVersionLabel()));

        List<Explanatory> documentVersions = explanatoryService.findVersions(documentId);
        explanatoryScreen.updateTimeLineWindow(documentVersions);
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
    }

    private String getVersionInfoAsString(XmlDocument document) {
        final VersionInfoVO versionInfo = getVersionInfo(document);
        final String versionInfoString = messageHelper.getMessage(
                "document.version.caption",
                versionInfo.getDocumentVersion(),
                versionInfo.getLastModifiedBy(),
                versionInfo.getEntity(),
                versionInfo.getLastModificationInstant()
        );
        return versionInfoString;
    }

    @Subscribe
    public void changeComparisionMode(ComparisonEvent event) {
        comparisonMode = event.isComparsionMode();
        if (comparisonMode) {
            explanatoryScreen.cleanComparedContent();
            if (!explanatoryScreen.isComparisonComponentVisible()) {
                LayoutChangeRequestEvent layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class);
                eventBus.post(layoutEvent);
            }
        } else {
            LayoutChangeRequestEvent layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.OFF, ComparisonComponent.class);
            eventBus.post(layoutEvent);
        }
        updateVersionsTab(new DocumentUpdatedEvent());
    }

    @Subscribe
    public void showIntermediateVersionWindow(ShowIntermediateVersionWindowEvent event) {
        explanatoryScreen.showIntermediateVersionWindow();
    }

    @Subscribe
    public void saveIntermediateVersion(SaveIntermediateVersionEvent event) {
        final Explanatory explanatory = explanatoryService.createVersion(documentId, event.getVersionType(), event.getCheckinComment());
        eventBus.post(new NotificationEvent(Type.INFO, "document.major.version.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, explanatory.getVersionSeriesId(), id));
        populateViewData(TocMode.SIMPLIFIED);
    }

    @Subscribe
    void saveToc(SaveTocRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Explanatory explanatory = getDocument();
        ExplanatoryStructureType explanatoryStructureType = getStructureType();
        explanatory = explanatoryService.saveTableOfContent(explanatory, event.getTableOfContentItemVOs(), explanatoryStructureType, messageHelper.getMessage("operation.toc.updated"), user);

        eventBus.post(new NotificationEvent(Type.INFO, "toc.edit.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(explanatory.getId(), explanatory.getMetadata().get().getRef(), id));
        LOG.info("Toc saved in Explanatory {} id {}, in {} milliseconds ({} sec)", explanatory.getName(), explanatory.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    public void getUserPermissions(FetchUserPermissionsRequest event) {
        Explanatory explanatory = getDocument();
        List<LeosPermission> userPermissions = securityContext.getPermissions(explanatory);
        explanatoryScreen.sendUserPermissions(userPermissions);
    }

    @Subscribe
    public void fetchSearchMetadata(SearchMetadataRequest event){
        eventBus.post(new SearchMetadataResponse(Collections.emptyList()));
    }

    @Subscribe
    public void fetchMetadata(DocumentMetadataRequest event){
        AnnotateMetadata metadata = new AnnotateMetadata();
        Explanatory explanatory = getDocument();
        metadata.setVersion(explanatory.getVersionLabel());
        metadata.setId(explanatory.getId());
        metadata.setTitle(explanatory.getTitle());
        eventBus.post(new DocumentMetadataResponse(metadata));
    }

    @Subscribe
    void mergeSuggestion(MergeSuggestionRequest event) {
        Explanatory document = getDocument();
        byte[] resultXmlContent = elementProcessor.replaceTextInElement(document, event.getOrigText(), event.getNewText(), event.getElementId(), event.getStartOffset(), event.getEndOffset());
        if (resultXmlContent == null) {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
            return;
        }
        document = explanatoryService.updateExplanatory(document, resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.merge.suggestion"));
        if (document != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.success"), MergeSuggestionResponse.Result.SUCCESS));
        }
        else {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
        }
    }

    @Subscribe
    void mergeSuggestions(MergeSuggestionsRequest event) {
        Explanatory explanatory = getDocument();
        commonDelegate.mergeSuggestions(explanatory, event, elementProcessor, explanatoryService::updateExplanatory);
    }

    private VersionInfoVO getVersionInfo(XmlDocument document) {
        String userId = document.getLastModifiedBy();
        User user = userHelper.getUser(userId);

        return new VersionInfoVO(
                document.getVersionLabel(),
                user.getName(), user.getDefaultEntity() != null ? user.getDefaultEntity().getOrganizationName() : "",
                dateFormatter.format(Date.from(document.getLastModificationInstant())),
                document.getVersionType());
    }

    private DocumentVO createExplanatoryVO(Explanatory explanatory) {
        DocumentVO explanatoryVO =
                new DocumentVO(explanatory.getId(),
                        explanatory.getMetadata().exists(m -> m.getLanguage() != null) ? explanatory.getMetadata().get().getLanguage() : "EN",
                        LeosCategory.COUNCIL_EXPLANATORY,
                        explanatory.getLastModifiedBy(),
                        Date.from(explanatory.getLastModificationInstant()));

        if (explanatory.getMetadata().isDefined()) {
            ExplanatoryMetadata metadata = explanatory.getMetadata().get();
            explanatoryVO.getMetadata().setInternalRef(metadata.getRef());
        }
        if(!explanatory.getCollaborators().isEmpty()) {
            explanatoryVO.addCollaborators(explanatory.getCollaborators());
        }
        return explanatoryVO;
    }

    @Subscribe
    void updateProposalMetadata(DocumentUpdatedEvent event) {
        if (event.isModified()) {
            CollectionContext context = proposalContextProvider.get();
            context.useChildDocument(documentId);
            context.executeUpdateProposalAsync();
        }
    }

    @Subscribe
    public void onInfoUpdate(UpdateUserInfoEvent updateUserInfoEvent) {
        if(isCurrentInfoId(updateUserInfoEvent.getActionInfo().getInfo().getDocumentId())) {
            if (!id.equals(updateUserInfoEvent.getActionInfo().getInfo().getPresenterId())) {
                eventBus.post(new NotificationEvent(leosUI, "coedition.caption", "coedition.operation." + updateUserInfoEvent.getActionInfo().getOperation().getValue(),
                        Type.TRAY, updateUserInfoEvent.getActionInfo().getInfo().getUserName()));
            }
            LOG.debug("Explanatory Presenter updated the edit info -" + updateUserInfoEvent.getActionInfo().getOperation().name());
            explanatoryScreen.updateUserCoEditionInfo(updateUserInfoEvent.getActionInfo().getCoEditionVos(), id);
        }
    }

    private boolean isCurrentInfoId(String versionSeriesId) {
        return versionSeriesId.equals(strDocumentVersionSeriesId);
    }

    @Subscribe
    public void documentUpdatedByCoEditor(DocumentUpdatedByCoEditorEvent documentUpdatedByCoEditorEvent) {
        if (isCurrentInfoId(documentUpdatedByCoEditorEvent.getDocumentId()) &&
                !id.equals(documentUpdatedByCoEditorEvent.getPresenterId())) {
            eventBus.post(new NotificationEvent(leosUI, "coedition.caption", "coedition.operation.update", Type.TRAY,
                    documentUpdatedByCoEditorEvent.getUser().getName()));
            explanatoryScreen.displayDocumentUpdatedByCoEditorWarning();
        }
    }
    
    @Subscribe
    void editInlineToc(InlineTocEditRequestEvent event) {
        Explanatory explanatory = getDocument();
        coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, null, InfoType.TOC_INFO);
        explanatoryScreen.enableTocEdition(getTableOfContent(explanatory, TocMode.NOT_SIMPLIFIED));
    }

    @Subscribe
    void closeInlineToc(InlineTocCloseRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.TOC_INFO);
        LOG.debug("User edit information removed");
    }

    @Subscribe
    void fetchTocAndAncestors(FetchCrossRefTocRequestEvent event) {
        Explanatory explanatory = getDocument();
        List<String> elementAncestorsIds = null;
        if (event.getElementIds() != null && event.getElementIds().size() > 0) {
            try {
                elementAncestorsIds = explanatoryService.getAncestorsIdsForElementId(explanatory, event.getElementIds());
            } catch (Exception e) {
                LOG.warn("Could not get ancestors Ids", e);
            }
        }
        // we are combining two operations (get toc + get selected element ancestors)
        final Map<String, List<TableOfContentItemVO>> tocItemList = packageService.getTableOfContent(explanatory.getId(), TocMode.SIMPLIFIED_CLEAN);
        eventBus.post(new FetchCrossRefTocResponseEvent(new TocAndAncestorsVO(tocItemList, elementAncestorsIds, messageHelper)));
    }

    @Subscribe
    void fetchElement(FetchElementRequestEvent event) {
        XmlDocument document = workspaceService.findDocumentByRef(event.getDocumentRef(), XmlDocument.class);
        String contentForType = elementProcessor.getElement(document, event.getElementTagName(), event.getElementId());
        String wrappedContentXml = wrapXmlFragment(contentForType != null ? contentForType : "");
        InputStream contentStream = new ByteArrayInputStream(wrappedContentXml.getBytes(StandardCharsets.UTF_8));
        contentForType = transformationService.toXmlFragmentWrapper(contentStream, urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()),
                securityContext.getPermissions(document));

        eventBus.post(new FetchElementResponseEvent(event.getElementId(), event.getElementTagName(), contentForType, event.getDocumentRef()));
    }

    @Subscribe
    void fetchReferenceLabel(ReferenceLabelRequestEvent event) {
        // Validate
        if (event.getReferences().size() < 1) {
            eventBus.post(new NotificationEvent(Type.ERROR, "unknown.error.message"));
            LOG.error("No reference found in the request from client");
            return;
        }

        final byte[] sourceXmlContent = getDocument().getContent().get().getSource().getBytes();
        Result<String> updatedLabel = referenceLabelService.generateLabelStringRef(event.getReferences(), getDocumentRef(), event.getCurrentElementID(), sourceXmlContent, event.getDocumentRef(), true);
        eventBus.post(new ReferenceLabelResponseEvent(updatedLabel.get(), event.getDocumentRef()));
    }

    private void refreshView(String elementType) {
        eventBus.post(new NavigationRequestEvent(Target.ANNEX, getDocumentRef()));
        eventBus.post(new NotificationEvent(Type.INFO, "explanatory.structure.changed.message." + elementType));
    }

    @Subscribe
    void searchTextInDocument(SearchTextRequestEvent event) {
        Explanatory explanatory = (Explanatory) httpSession.getAttribute("explanatory#" + getDocumentRef());
        if (explanatory == null) {
            explanatory = getDocument();
        }
        List<SearchMatchVO> matches = Collections.emptyList();
        try {
            matches = searchService.searchText(getContent(explanatory), event.getSearchText(), event.matchCase, event.completeWords);
        } catch (Exception e) {
            eventBus.post(new NotificationEvent(Type.ERROR, "Error while searching{1}", e.getMessage()));
        }

        //Do we reset session etc if there was partial replace earlier.
        explanatoryScreen.showMatchResults(event.searchID, matches);
    }

    @Subscribe
    void saveAndCloseAfterReplace(SaveAndCloseAfterReplaceEvent event){
        // save document into repository
        Explanatory explanatory = getDocument();

        Explanatory explanatoryFromSession = (Explanatory) httpSession.getAttribute("explanatory#" + getDocumentRef());
        httpSession.removeAttribute("explanatory#" + getDocumentRef());

        explanatory = explanatoryService.updateExplanatory(explanatory, explanatoryFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (explanatory != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void replaceAllTextInDocument(ReplaceAllMatchRequestEvent event) {
        Explanatory explanatoryFromSession = getExplanatoryFromSession();
        if (explanatoryFromSession == null) {
            explanatoryFromSession = getDocument();
        }

        byte[] updatedContent = searchService.replaceText(
                getContent(explanatoryFromSession),
                event.getSearchText(),
                event.getReplaceText(),
                event.getSearchMatchVOs());

        Explanatory explanatoryUpdated = copyIntoNew(explanatoryFromSession, updatedContent);
        httpSession.setAttribute("explanatory#" + getDocumentRef(), explanatoryUpdated);
        explanatoryScreen.setContent(getEditableXml(explanatoryUpdated));
        eventBus.post(new ReplaceAllMatchResponseEvent(true));
    }

    private Explanatory copyIntoNew(Explanatory source, byte[] updatedContent) {
        Content contentFromSession = source.getContent().get();
        Content.Source updatedSource = new SourceImpl(new ByteArrayInputStream(updatedContent));
        Content contentObj = new ContentImpl(
                contentFromSession.getFileName(),
                contentFromSession.getMimeType(),
                updatedContent.length,
                updatedSource
        );
        Option<Content> updatedContentOptionObj = Option.option(contentObj);
        return new Explanatory(
                source.getId(),
                source.getName(),
                source.getCreatedBy(),
                source.getCreationInstant(),
                source.getLastModifiedBy(),
                source.getLastModificationInstant(),
                source.getVersionSeriesId(),
                source.getCmisVersionLabel(),
                source.getVersionLabel(),
                source.getVersionComment(),
                source.getVersionType(),
                source.isLatestVersion(),
                source.getTitle(),
                source.getCollaborators(),
                source.getMilestoneComments(),
                updatedContentOptionObj,
                source.getMetadata()
        );
    }

    @Subscribe
    void saveAfterReplace(SaveAfterReplaceEvent event){
        // save document into repository
        Explanatory explanatory = getDocument();

        Explanatory explanatoryFromSession = (Explanatory) httpSession.getAttribute("explanatory#" + getDocumentRef());

        explanatory = explanatoryService.updateExplanatory(explanatory, explanatoryFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (explanatory != null) {
            httpSession.setAttribute("explanatory#"+getDocumentRef(), explanatory);
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void replaceOneTextInDocument(ReplaceMatchRequestEvent event) {
        if (event.getSearchMatchVO().isReplaceable()) {
            Explanatory explanatoryFromSession = getExplanatoryFromSession();
            if (explanatoryFromSession == null) {
                explanatoryFromSession = getDocument();
            }

            byte[] updatedContent = searchService.replaceText(
                    getContent(explanatoryFromSession),
                    event.getSearchText(),
                    event.getReplaceText(),
                    Arrays.asList(event.getSearchMatchVO()));

            Explanatory explanatoryUpdated = copyIntoNew(explanatoryFromSession, updatedContent);
            httpSession.setAttribute("explanatory#" + getDocumentRef(), explanatoryUpdated);
            explanatoryScreen.setContent(getEditableXml(explanatoryUpdated));
            explanatoryScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), true);
        } else {
            explanatoryScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), false);
        }
    }

    @Subscribe
    void closeSearchBar(SearchBarClosedEvent event) {
        //Cleanup the session etc
        explanatoryScreen.closeSearchBar();
        httpSession.removeAttribute("explanatory#"+getDocumentRef());
        eventBus.post(new RefreshDocumentEvent());
    }

    private ExplanatoryStructureType getStructureType() {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems().stream().
                filter(tocItem -> (tocItem.getAknTag().value().equalsIgnoreCase(ExplanatoryStructureType.LEVEL.getType()))).collect(Collectors.toList());
        return tocItems.size() > 0 ? ExplanatoryStructureType.valueOf(tocItems.get(0).getAknTag().value().toUpperCase()) : null;
    }
}
