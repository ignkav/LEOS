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
package eu.europa.ec.leos.ui.view.collection;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosExportStatus;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.*;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.MetadataVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.event.ExportPackageCreatedEvent;
import eu.europa.ec.leos.model.event.ExportPackageDeletedEvent;
import eu.europa.ec.leos.model.event.ExportPackageUpdatedEvent;
import eu.europa.ec.leos.model.event.MilestoneCreatedEvent;
import eu.europa.ec.leos.model.event.MilestoneUpdatedEvent;
import eu.europa.ec.leos.model.event.UpdateUserInfoEvent;
import eu.europa.ec.leos.model.messaging.UpdateInternalReferencesMessage;
import eu.europa.ec.leos.model.notification.collaborators.AddCollaborator;
import eu.europa.ec.leos.model.notification.collaborators.EditCollaborator;
import eu.europa.ec.leos.model.notification.collaborators.RemoveCollaborator;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.permissions.Role;
import eu.europa.ec.leos.repository.RepositoryContext;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.collection.CollectionService;
import eu.europa.ec.leos.services.collection.CreateCollectionResult;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.document.SecurityService;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.services.export.ExportDW;
import eu.europa.ec.leos.services.export.ExportLW;
import eu.europa.ec.leos.services.export.ExportLeos;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportService;
import eu.europa.ec.leos.services.messaging.UpdateInternalReferencesProducer;
import eu.europa.ec.leos.services.milestone.MilestoneService;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.store.ExportPackageService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.TemplateService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.user.UserService;
import eu.europa.ec.leos.ui.event.CreateMilestoneRequestEvent;
import eu.europa.ec.leos.ui.event.CreateMilestoneEvent;
import eu.europa.ec.leos.ui.event.CreateRevisionRequestEvent;
import eu.europa.ec.leos.ui.event.CloneProposalRequestEvent;
import eu.europa.ec.leos.ui.event.CloseScreenRequestEvent;
import eu.europa.ec.leos.ui.event.DeleteExportPackageEvent;
import eu.europa.ec.leos.ui.event.FetchMilestoneEvent;
import eu.europa.ec.leos.ui.event.NotifyExportPackageEvent;
import eu.europa.ec.leos.ui.event.UpdateCommentsExportPackageEvent;
import eu.europa.ec.leos.ui.event.DownloadExportPackageEvent;
import eu.europa.ec.leos.ui.event.view.collection.AddCollaboratorRequest;
import eu.europa.ec.leos.ui.event.view.collection.CreateAnnexRequest;
import eu.europa.ec.leos.ui.event.view.collection.CreateExplanatoryRequest;
import eu.europa.ec.leos.ui.event.view.collection.DeleteAnnexEvent;
import eu.europa.ec.leos.ui.event.view.collection.DeleteAnnexRequest;
import eu.europa.ec.leos.ui.event.view.collection.DeleteCollectionEvent;
import eu.europa.ec.leos.ui.event.view.collection.DeleteCollectionRequest;
import eu.europa.ec.leos.ui.event.view.collection.DeleteExplanatoryEvent;
import eu.europa.ec.leos.ui.event.view.collection.DeleteExplanatoryRequest;
import eu.europa.ec.leos.ui.event.view.collection.DownloadMandateEvent;
import eu.europa.ec.leos.ui.event.view.collection.DownloadProposalEvent;
import eu.europa.ec.leos.ui.event.view.collection.EditCollaboratorRequest;
import eu.europa.ec.leos.ui.event.view.collection.ExportMandateEvent;
import eu.europa.ec.leos.ui.event.view.collection.ExportProposalEvent;
import eu.europa.ec.leos.ui.event.view.collection.RemoveCollaboratorRequest;
import eu.europa.ec.leos.ui.event.view.collection.SaveAnnexMetaDataRequest;
import eu.europa.ec.leos.ui.event.view.collection.SaveExplanatoryMetaDataRequest;
import eu.europa.ec.leos.ui.event.view.collection.SearchContextEvent;
import eu.europa.ec.leos.ui.event.view.collection.SearchUserInContextEvent;
import eu.europa.ec.leos.ui.event.view.collection.SearchUserRequest;
import eu.europa.ec.leos.ui.model.ExportPackageVO;
import eu.europa.ec.leos.ui.model.MilestonesVO;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.ui.view.AbstractLeosPresenter;
import eu.europa.ec.leos.ui.window.milestone.MilestoneExplorer;
import eu.europa.ec.leos.usecases.document.BillContext;
import eu.europa.ec.leos.usecases.document.CollectionContext;
import eu.europa.ec.leos.usecases.document.ContextAction;
import eu.europa.ec.leos.vo.catalog.CatalogItem;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.component.WindowClosedEvent;
import eu.europa.ec.leos.web.event.view.annex.OpenAnnexEvent;
import eu.europa.ec.leos.web.event.view.document.CollaboratorsUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.CollaboratorsUpdatedEvent.Operation;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.OpenLegalTextEvent;
import eu.europa.ec.leos.web.event.view.explanatory.OpenExplanatoryEvent;
import eu.europa.ec.leos.web.event.view.memorandum.OpenMemorandumEvent;
import eu.europa.ec.leos.web.event.window.SaveMetaDataRequestEvent;
import eu.europa.ec.leos.web.model.UserVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.component.MoveAnnexEvent;
import eu.europa.ec.leos.web.ui.navigation.Target;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.security.LeosPermission.CAN_ADD_REMOVE_COLLABORATOR;

@Component
@Scope("prototype")
class CollectionPresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionPresenter.class);

    private final CollectionScreen collectionScreen;
    private final Provider<CollectionContext> proposalContextProvider;
    private final Provider<BillContext> billContextProvider;
    private final Provider<RepositoryContext> repositoryContextProvider;
    private final AnnexService annexService;
    private final ExplanatoryService explanatoryService;
    private final BillService billService;
    private final PackageService packageService;
    private final UserHelper userHelper;
    private final ExportService exportService;
    private final MilestoneService milestoneService;
    private final SecurityService securityService;
    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final MessageHelper messageHelper;
    private final CoEditionHelper coEditionHelper;
    private final LeosPermissionAuthorityMapHelper authorityMapHelper;
    private final ProposalService proposalService;
    private final UpdateInternalReferencesProducer updateInternalReferencesProducer;
    private final CollectionService collectionService;
    private UserService userService;
    private final ExportPackageService exportPackageService;

    private String proposalId;
    private String proposalVersionSeriesId;
    private String proposalRef;
    private Set<MilestonesVO> milestonesVOs = new TreeSet<>(Comparator.comparing(MilestonesVO::getUpdatedDate).reversed());
    private List<CloneProposalMetadataVO> cloneProposalMetadataVOs;
    private final StampedLock milestonesVOsLock = new StampedLock();
    private Set<String> docVersionSeriesIds;
    private Set<ExportPackageVO> exportPackages = new TreeSet<>(Comparator.comparing(ExportPackageVO::getDate).reversed());
    private final StampedLock exportPackageLock = new StampedLock();
    private CloneProposalMetadataVO cloneProposalMetadataVO;

    @Value("${leos.clone.originRef}")
    private String cloneOriginRef;

    @Autowired
    CollectionPresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus,
                                   EventBus leosApplicationEventBus,
                                   CollectionScreen collectionScreen,
                                   Provider<CollectionContext> proposalContextProvider,
                                   Provider<BillContext> billContextProvider,
                                   Provider<RepositoryContext> repositoryContextProvider, AnnexService annexService,
                                   BillService billService,
                                   ExplanatoryService explanatoryService,
                                   PackageService packageService,
                                   MilestoneService milestoneService,
                                   UserHelper userHelper,
                                   ExportService exportService,
                                   SecurityService securityService,
                                   NotificationService notificationService, MessageHelper messageHelper,
                                   TemplateService templateService, CoEditionHelper coEditionHelper,
                                   LeosPermissionAuthorityMapHelper authorityMapHelper, UuidHelper uuidHelper, ProposalService proposalService, WorkspaceService workspaceService,
                                   UpdateInternalReferencesProducer updateInternalReferencesProducer, CollectionService collectionService, UserService userService,
                                   ExportPackageService exportPackageService) {
        
        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        
        this.collectionScreen = collectionScreen;
        this.proposalContextProvider = proposalContextProvider;
        this.billContextProvider = billContextProvider;
        this.repositoryContextProvider = repositoryContextProvider;
        this.annexService = annexService;
        this.explanatoryService = explanatoryService;
        this.billService = billService;
        this.packageService = packageService;
        this.milestoneService = milestoneService;
        this.userHelper = userHelper;
        this.exportService = exportService;
        this.securityService = securityService;
        this.notificationService = notificationService;
        this.messageHelper = messageHelper;
        this.templateService = templateService;
        this.coEditionHelper = coEditionHelper;
        this.authorityMapHelper = authorityMapHelper;
        this.proposalService = proposalService;
        this.updateInternalReferencesProducer = updateInternalReferencesProducer;
        this.collectionService = collectionService;
        this.userService = userService;
        this.exportPackageService = exportPackageService;
    }
    
    @Override
    public void enter() {
        super.enter();
        populateData();
        populateExportPackages();
    }
    
    private void populateData() {
        if(getProposalRef() != null) {
            proposalRef = getProposalRef();
            Proposal proposal = proposalService.findProposalByRef(proposalRef);
            if(proposal != null) {
                proposalId = proposal != null ? proposal.getId() : proposalId;
                byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
                if(proposal != null && proposal.isClonedProposal()) {
                    cloneProposalMetadataVO = proposalService.getClonedProposalMetadata(xmlContent);
                }
            }
        }
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
        List<XmlDocument> documents = packageService.findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, false);
        List<LegDocument> legDocuments = packageService.findDocumentsByPackageId(leosPackage.getId(), LegDocument.class, false, false);
        legDocuments.sort(Comparator.comparing(LegDocument::getLastModificationInstant).reversed());
        
        DocumentVO proposalVO = createViewObject(documents);
        proposalVO.setCloneProposalMetadataVO(cloneProposalMetadataVO);
        collectionScreen.populateData(proposalVO);

        long stamp = milestonesVOsLock.writeLock();
        try {
            milestonesVOs.clear();
            legDocuments.forEach(document -> milestonesVOs.add(getMilestonesVO(document)));
            collectionScreen.populateMilestones(milestonesVOs);
        } finally {
            milestonesVOsLock.unlockWrite(stamp);
        }

        collectionScreen.updateUserCoEditionInfo(coEditionHelper.getAllEditInfo(), user);
    }

    private DocumentVO createViewObject(List<XmlDocument> documents) {
        DocumentVO proposalVO = new DocumentVO(LeosCategory.PROPOSAL);
        List<DocumentVO> annexVOList = new ArrayList<>();
        docVersionSeriesIds = new HashSet<>();
        //We have the latest version of the document, no need to search for them again
        for (XmlDocument document : documents) {
            switch (document.getCategory()) {
                case PROPOSAL: {
                    Proposal proposal = (Proposal) document;
                    MetadataVO metadataVO = createMetadataVO(proposal);
                    proposalVO.setMetaData(metadataVO);
                    proposalVO.addCollaborators(proposal.getCollaborators());
                    proposalVersionSeriesId = proposal.getVersionSeriesId();
                    break;
                }case COUNCIL_EXPLANATORY: {
                    Explanatory explanatory = (Explanatory) document;
                    DocumentVO explanatoryVO = getExplanatroyVO(explanatory);
                    explanatoryVO.addCollaborators(explanatory.getCollaborators());
                    explanatoryVO.getMetadata().setInternalRef(explanatory.getMetadata().getOrError(() -> "Explanatory metadata is not available!").getRef());
                    explanatoryVO.setVersionSeriesId(explanatory.getVersionSeriesId());
                    explanatoryVO.setTemplate(explanatory.getMetadata().getOrError(() -> "Explanatory metadata is not available!").getTemplate());
                    proposalVO.addChildDocument(explanatoryVO);
                    docVersionSeriesIds.add(explanatory.getVersionSeriesId());
                    break;
                }  case MEMORANDUM: {
                    Memorandum memorandum = (Memorandum) document;
                    DocumentVO memorandumVO = getMemorandumVO(memorandum);
                    proposalVO.addChildDocument(memorandumVO);
                    memorandumVO.addCollaborators(memorandum.getCollaborators());
                    memorandumVO.getMetadata().setInternalRef(memorandum.getMetadata().getOrError(() -> "Memorandum metadata is not available!").getRef());
                    memorandumVO.setVersionSeriesId(memorandum.getVersionSeriesId());
                    docVersionSeriesIds.add(memorandum.getVersionSeriesId());
                    break;
                }
                case BILL: {
                    Bill bill = (Bill) document;
                    DocumentVO billVO = getLegalTextVO(bill);
                    proposalVO.addChildDocument(billVO);
                    billVO.addCollaborators(bill.getCollaborators());
                    billVO.getMetadata().setInternalRef(bill.getMetadata().getOrError(() -> "Legal text metadata is not available!").getRef());
                    billVO.setVersionSeriesId(bill.getVersionSeriesId());
                    docVersionSeriesIds.add(bill.getVersionSeriesId());
                    break;
                }
                case ANNEX: {
                    Annex annex = (Annex) document;
                    DocumentVO annexVO = createAnnexVO(annex);
                    annexVO.addCollaborators(annex.getCollaborators());
                    annexVO.getMetadata().setInternalRef(annex.getMetadata().getOrError(() -> "Annex metadata is not available!").getRef());
                    annexVOList.add(annexVO);
                    annexVO.setVersionSeriesId(annex.getVersionSeriesId());
                    docVersionSeriesIds.add(annex.getVersionSeriesId());
                    break;
                }
                default:
                    LOG.debug("Do nothing for rest of the categories like MEDIA, CONFIG & LEG");
                    break;
            }
        }

        annexVOList.sort(Comparator.comparingInt(DocumentVO::getDocNumber));
        DocumentVO legalText = proposalVO.getChildDocument(LeosCategory.BILL);
        if (legalText != null) {
            for (DocumentVO annexVO : annexVOList) {
                legalText.addChildDocument(annexVO);
            }
        }

        return proposalVO;
    }
    
    private MilestonesVO getMilestonesVO(LegDocument legDocument) {
        cloneProposalMetadataVOs = collectionService.getClonedProposalsMetadataVO(proposalId, legDocument.getName());
        MilestonesVO milestonesVO = new MilestonesVO(legDocument.getMilestoneComments(),
                Date.from(legDocument.getCreationInstant()),
                Date.from(legDocument.getLastModificationInstant()),
                messageHelper.getMessage("milestones.column.status.value." + legDocument.getStatus().name()),
                legDocument.getName());

        if (cloneProposalMetadataVOs != null && !cloneProposalMetadataVOs.isEmpty()) {
            List<MilestonesVO> clonedMilestonesVOS = new ArrayList<>();
            cloneProposalMetadataVOs.forEach(clonedProposalMetadataVO -> {
                List<String> titles = new ArrayList<>();
                titles.add(messageHelper.getMessage("clone.proposal.revision.sent") + userService.getUser(clonedProposalMetadataVO.getTargetUser()).getName());
                MilestonesVO milestoneVO = new MilestonesVO(titles, clonedProposalMetadataVO.getCreationDate(),
                        null, clonedProposalMetadataVO.getRevisionStatus(), null);
                milestoneVO.setClone(true);
                clonedMilestonesVOS.add(milestoneVO);
            });
            milestonesVO.setClonedMilestones(clonedMilestonesVOS);
        }
        return milestonesVO;
    }

    private void populateExportPackages() {
        if (collectionScreen.isExportPackageBlockVisible()) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
            List<ExportDocument> exportDocuments = packageService.findDocumentsByPackageId(leosPackage.getId(), ExportDocument.class, false, false);
            long stamp = exportPackageLock.writeLock();
            try {
                exportPackages.clear();
                exportDocuments.forEach(exportDocument -> exportPackages.add(getExportPackageVO(exportDocument)));
                collectionScreen.populateExportPackages(exportPackages);
            } finally {
                exportPackageLock.unlockWrite(stamp);
            }
        }
    }

    @Subscribe
    private void exportPackageCreated(ExportPackageCreatedEvent exportPackageCreatedEvent) {
        if (collectionScreen.isExportPackageBlockVisible() &&
                exportPackageCreatedEvent.getProposalRef().equals(proposalRef)) {
            long stamp = exportPackageLock.writeLock();
            try {
                exportPackages.add(getExportPackageVO(exportPackageCreatedEvent.getExportDocument()));
                collectionScreen.populateExportPackages(exportPackages);
            } finally {
                exportPackageLock.unlockWrite(stamp);
            }
        }
    }

    @Subscribe
    private void exportPackageUpdated(ExportPackageUpdatedEvent exportPackageUpdatedEvent) {
        if (collectionScreen.isExportPackageBlockVisible() &&
                exportPackageUpdatedEvent.getProposalRef().equals(proposalRef)) {
            long stamp = exportPackageLock.writeLock();
            try {
                ExportPackageVO exportPackageEvent = getExportPackageVO(exportPackageUpdatedEvent.getExportDocument());
                ExportPackageVO exportPackageToBeUpdated = exportPackages.stream().filter(
                        exportPackageVO -> exportPackageVO.getVersionId().equals(exportPackageEvent.getVersionId()))
                            .findAny().orElse(null);
                if ((exportPackageToBeUpdated != null) && (!exportPackageToBeUpdated.equals(exportPackageEvent)) &&
                        (ExportPackageVO.compareVersions(exportPackageEvent.getVersionLabel(), exportPackageToBeUpdated.getVersionLabel()) >= 0) &&
                        exportPackages.remove(exportPackageToBeUpdated)) { // To maintain order in treeset is needed remove and add element (not only just update)
                    exportPackages.add(exportPackageEvent);
                    collectionScreen.populateExportPackages(exportPackages);
                }
            } finally {
                exportPackageLock.unlockWrite(stamp);
            }
        }
    }

    @Subscribe
    private void exportPackageDeleted(ExportPackageDeletedEvent exportPackageDeletedEvent) {
        if (collectionScreen.isExportPackageBlockVisible() &&
                exportPackageDeletedEvent.getProposalRef().equals(proposalRef)) {
            long stamp = exportPackageLock.writeLock();
            try {
                ExportPackageVO exportPackageToBeDeleted = exportPackages.stream().filter(
                        exportPackageVO -> exportPackageVO.getVersionId().equals(exportPackageDeletedEvent.getVersionId()))
                            .findAny().orElse(null);
                if (exportPackageToBeDeleted != null) {
                    exportPackages.remove(exportPackageToBeDeleted);
                    collectionScreen.populateExportPackages(exportPackages);
                }
            } finally {
                exportPackageLock.unlockWrite(stamp);
            }
        }
    }

    @Subscribe
    private void deleteExportPackage(DeleteExportPackageEvent deleteExportPackageEvent) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            exportPackageService.deleteExportDocument(deleteExportPackageEvent.getId());
            leosApplicationEventBus.post(new ExportPackageDeletedEvent(proposalRef, deleteExportPackageEvent.getVersionsId()));
            LOG.info("Export Package {} for proposal {} deleted in {} milliseconds ({} sec)", deleteExportPackageEvent.getId(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while deleting Export Package", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.block.export.package.action.delete.error", e.getMessage()));
        }
    }

    @Subscribe
    private void notifyExportPackage(NotifyExportPackageEvent notifyExportPackageEvent) {
        ExportDocument exportDocument = null;
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            byte[] updatedContent = exportService.updateExportPackageWithComments(notifyExportPackageEvent.getId());
            exportDocument = exportPackageService.updateExportDocument(notifyExportPackageEvent.getId(), updatedContent);
            notificationService.sendNotification(proposalRef, exportDocument.getId());
            exportDocument = exportPackageService.updateExportDocument(exportDocument.getId(), LeosExportStatus.NOTIFIED);
            LOG.info("Export Package {} for proposal {} notified in {} milliseconds ({} sec)", notifyExportPackageEvent.getId(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while notifiying Export Package", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.block.export.package.action.notify.error", e.getMessage()));
        } finally {
            if (exportDocument != null) {
                leosApplicationEventBus.post(new ExportPackageUpdatedEvent(proposalRef, exportDocument));
            }
        }
    }

    @Subscribe
    private void downloadExportPackage(DownloadExportPackageEvent previewExportPackageEvent) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final Map<String, byte[]> exportPackageContent = exportService.getExportPackageContent(previewExportPackageEvent.getId(), ".docx");
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(exportPackageContent.keySet().stream().findFirst().get(),
                    new ByteArrayInputStream(exportPackageContent.values().stream().findFirst().get()));
            collectionScreen.setExportPackageStreamResource(downloadStreamResource);
            LOG.info("Export Package {} for proposal {} downloaded in {} milliseconds ({} sec)", previewExportPackageEvent.getId(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloading Export Package", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.block.export.package.action.download.error", e.getMessage()));
        }
    }

    @Subscribe
    private void updateCommentsExportPackage(UpdateCommentsExportPackageEvent updateCommentsExportPackageEvent) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            ExportDocument exportDocument = exportPackageService.updateExportDocument(updateCommentsExportPackageEvent.getId(), updateCommentsExportPackageEvent.getComments());
            leosApplicationEventBus.post(new ExportPackageUpdatedEvent(proposalRef, exportDocument));
            LOG.info("Export Package {} for proposal {} comments updated in {} milliseconds ({} sec)", updateCommentsExportPackageEvent.getId(), proposalRef, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while updating comments for Export Package", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.block.export.package.update.comments.error", e.getMessage()));
        }
    }

    @Subscribe
    private void setSearchContext(SearchContextEvent searchContextEvent) {
        collectionScreen.setSearchContextHolder(searchContextEvent.isShowSearchContext());
    }

    private ExportPackageVO getExportPackageVO(ExportDocument exportDocument) {
        return new ExportPackageVO(exportDocument.getId(), exportDocument.getVersionSeriesId(),
                exportDocument.getCmisVersionLabel(), exportDocument.getComments(),
                Date.from(exportDocument.getLastModificationInstant()),
                messageHelper.getMessage("collection.block.export.package.column.status.value." + exportDocument.getStatus().name()));
    }

    // FIXME refine
    private DocumentVO getExplanatroyVO(Explanatory explanatory) {
        DocumentVO explanatoryVO = new DocumentVO(explanatory.getId(),
                explanatory.getMetadata().exists(e -> e.getLanguage() != null) ? explanatory.getMetadata().get().getLanguage() : "EN",
                LeosCategory.COUNCIL_EXPLANATORY,
                explanatory.getLastModifiedBy(),
                Date.from(explanatory.getLastModificationInstant()));

        if (explanatory.getMetadata().isDefined()) {
            ExplanatoryMetadata metadata = explanatory.getMetadata().get();
            explanatoryVO.setTitle(metadata.getTitle());
        }

        return explanatoryVO;
    }

    // FIXME refine
    private DocumentVO getMemorandumVO(Memorandum memorandum) {
        return new DocumentVO(memorandum.getId(),
                         memorandum.getMetadata().exists(m -> m.getLanguage() != null) ? memorandum.getMetadata().get().getLanguage() : "EN",
                         LeosCategory.MEMORANDUM,
                         memorandum.getLastModifiedBy(),
                         Date.from(memorandum.getLastModificationInstant()));
    }

    // FIXME refine
    private DocumentVO getLegalTextVO(Bill bill) {
        return new DocumentVO(bill.getId(),
                         bill.getMetadata().exists(m -> m.getLanguage() != null) ? bill.getMetadata().get().getLanguage() : "EN",
                         LeosCategory.BILL,
                         bill.getLastModifiedBy(),
                         Date.from(bill.getLastModificationInstant()));
    }

    // FIXME refine
    private DocumentVO createAnnexVO(Annex annex) {
        DocumentVO annexVO =
                new DocumentVO(annex.getId(),
                        annex.getMetadata().exists(m -> m.getLanguage() != null) ? annex.getMetadata().get().getLanguage() : "EN",
                          LeosCategory.ANNEX,
                          annex.getLastModifiedBy(),
                          Date.from(annex.getLastModificationInstant()));

        if (annex.getMetadata().isDefined()) {
            AnnexMetadata metadata = annex.getMetadata().get();
            annexVO.setDocNumber(metadata.getIndex());
            annexVO.setTitle(metadata.getTitle());
            annexVO.getMetadata().setNumber(metadata.getNumber());
        }

        return annexVO;
    }

    private MetadataVO createMetadataVO(Proposal proposal) {
        ProposalMetadata metadata = proposal.getMetadata().getOrError(() -> "Proposal metadata is not available!");
        return new MetadataVO(metadata.getStage(), metadata.getType(), metadata.getPurpose(), metadata.getTemplate(), metadata.getLanguage());
    }

    @Subscribe
    void saveMetaData(SaveMetaDataRequestEvent event) {
        LOG.trace("Saving proposal metadata...");
        CollectionContext context = proposalContextProvider.get();
        context.useProposal(proposalId);
        context.usePurpose(event.getMetaDataVO().getDocPurpose());
        String comment = messageHelper.getMessage("operation.metadata.updated");
        context.useActionMessage(ContextAction.METADATA_UPDATED, comment);
        context.useActionComment(comment);
        context.executeUpdateProposal();
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "metadata.edit.saved"));
        // TODO optimize refresh of data on the screen
        // populateData();
    }

    @Subscribe
    void saveAnnexMetaData(SaveAnnexMetaDataRequest event) {
        // 1. get Annex
        Annex annex = annexService.findAnnex(event.getAnnex().getId());
        AnnexMetadata metadata = annex.getMetadata().getOrError(()-> "Annex metadata not found!");
        AnnexMetadata updatedMetadata = metadata.withTitle(event.getAnnex().getTitle());

        // 2. save metadata
        annexService.updateAnnex(annex, updatedMetadata, VersionType.MINOR, messageHelper.getMessage("collection.block.annex.metadata.updated"));
        eventBus.post(new DocumentUpdatedEvent());
        // 3.update ui
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "collection.block.annex.metadata.updated"));
        populateData();
    }

    @Subscribe
    void createAnnex(CreateAnnexRequest event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
            Bill bill = billService.findBillByPackagePath(leosPackage.getPath());
            BillMetadata metadata = bill.getMetadata().getOrError(() -> "Bill metadata is required!");
            BillContext billContext = billContextProvider.get();
            billContext.usePackage(leosPackage);
            billContext.useTemplate(bill);
            billContext.usePurpose(metadata.getPurpose());
            billContext.useActionMessage(ContextAction.ANNEX_METADATA_UPDATED, messageHelper.getMessage("collection.block.annex.metadata.updated"));
            billContext.useActionMessage(ContextAction.ANNEX_ADDED, messageHelper.getMessage("collection.block.annex.added"));
            billContext.useActionMessage(ContextAction.DOCUMENT_CREATED, messageHelper.getMessage("operation.document.created"));
            // KLUGE temporary workaround
            // We will have to change the ui to allow the user to select the annex child template that s/he wants.
            CatalogItem templateItem = templateService.getTemplateItem(metadata.getDocTemplate());
            String annexTemplate = templateItem.getItems().get(0).getId();
            billContext.useAnnexTemplate(annexTemplate);
            billContext.executeCreateBillAnnex();
            eventBus.post(new DocumentUpdatedEvent());
            populateData();
            LOG.info("Created Annex {} for Bill {} id {}, in {} milliseconds ({} sec)", billContext.getAnnexId(), bill.getName(), bill.getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while creating the annex", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.block.annex.add.error", e.getMessage()));
        }
    }

    @Subscribe
    void deleteAnnexRequest(DeleteAnnexRequest event) {
        collectionScreen.confirmAnnexDeletion(event.getAnnex());
    }

    @Subscribe
    void deleteAnnex(DeleteAnnexEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        // 1. delete Annex
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);

        BillContext billContext = billContextProvider.get();
        billContext.useAnnex(event.getAnnex().getId());
        billContext.usePackage(leosPackage);
        billContext.useActionMessage(ContextAction.ANNEX_METADATA_UPDATED, messageHelper.getMessage("collection.block.annex.metadata.updated"));
        billContext.useActionMessage(ContextAction.ANNEX_DELETED, messageHelper.getMessage("collection.block.annex.removed"));
        billContext.executeRemoveBillAnnex();
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(proposalId, event.getAnnex().getMetadata().getInternalRef(), id));
        eventBus.post(new DocumentUpdatedEvent());
        // 2. update ui
        populateData();
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "annex.deleted"));
        LOG.info("Deleted Annex {} id {}, in {} milliseconds ({} sec)", event.getAnnex().getMetadata().getInternalRef(), event.getAnnex().getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void moveAnnex(MoveAnnexEvent event) {
        //1.call Service to reorder and update annexes
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);

        BillContext billContext = billContextProvider.get();
        billContext.usePackage(leosPackage);
        billContext.useMoveDirection(event.getDirection().toString());
        billContext.useAnnex(event.getAnnexVo().getId());
        billContext.useActionMessage(ContextAction.ANNEX_METADATA_UPDATED, messageHelper.getMessage("collection.block.annex.metadata.updated"));
        billContext.executeMoveAnnex();
        eventBus.post(new DocumentUpdatedEvent());
        //2. update screen to show update
        populateData();
    }

    @Subscribe
    void saveExplanatoryMetaData(SaveExplanatoryMetaDataRequest event) {
        // 1. get explanatory
        Explanatory explanatory = explanatoryService.findExplanatory(event.getExplanatory().getId());
        ExplanatoryMetadata metadata = explanatory.getMetadata().getOrError(()-> "Explanatory metadata not found!");
        ExplanatoryMetadata updatedMetadata = metadata.withTitle(event.getExplanatory().getTitle());

        // 2. save metadata
        explanatoryService.updateExplanatory(explanatory, updatedMetadata, VersionType.MINOR, messageHelper.getMessage("collection.block.explanatory.metadata.updated"));

        // 2. save metadata
        eventBus.post(new DocumentUpdatedEvent());
        // 3.update ui
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "collection.block.explanatory.metadata.updated"));
        populateData();
    }

    @Subscribe
    void createExplanatory(CreateExplanatoryRequest event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
            Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
            ProposalMetadata metadata = proposal.getMetadata().getOrError(() -> "Proposal metadata is required!");

            CollectionContext context = proposalContextProvider.get();
            context.useTemplate("CE-003");

            context.usePurpose(metadata.getPurpose());
            context.useProposal(proposalId);
            context.useActionMessage(ContextAction.EXPLANATORY_ADDED, messageHelper.getMessage("collection.block.explanatory.added"));
            context.executeCreateExplanatory();

            eventBus.post(new DocumentUpdatedEvent());
            populateData();
            LOG.info("Created explanatory for Mandate, in {} milliseconds ({} sec)", stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while creating the explanatory", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.block.explanatory.add.error", e.getMessage()));
        }
    }

    @Subscribe
    void deleteExplanatoryRequest(DeleteExplanatoryRequest event) {
        collectionScreen.confirmExplanatoryDeletion(event.getExplanatory());
    }

    @Subscribe
    void deleteExplanatory(DeleteExplanatoryEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        // 1. delete explanatory
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
        CollectionContext collectionContext = proposalContextProvider.get();
        collectionContext.useExplanatory(event.getExplanatory().getId());
        collectionContext.usePackage(leosPackage);
        collectionContext.useActionMessage(ContextAction.EXPLANATORY_METADATA_UPDATED, messageHelper.getMessage("collection.block.explanatory.metadata.updated"));
        collectionContext.useActionMessage(ContextAction.EXPLANATORY_DELETED, messageHelper.getMessage("collection.block.explanatory.removed"));
        collectionContext.executeRemoveExplanatory();
        updateInternalReferencesProducer.send(new UpdateInternalReferencesMessage(proposalId, event.getExplanatory().getMetadata().getInternalRef(), id));
        eventBus.post(new DocumentUpdatedEvent());
        // 2. update ui
        populateData();
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "explanatory.deleted"));
        LOG.info("Deleted explanatory {} id {}, in {} milliseconds ({} sec)", event.getExplanatory().getMetadata().getInternalRef(), event.getExplanatory().getId(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    private String getProposalRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.PROPOSAL_REF.name());
    }

    @Subscribe
    void openMemorandum(OpenMemorandumEvent event) {
        final String documentRef = event.getMemorandum().getMetadata().getInternalRef();
        RepositoryContext repositoryContext = repositoryContextProvider.get();
        repositoryContext.populateVersionsWithoutVersionLabel(Memorandum.class, documentRef);
        eventBus.post(new NavigationRequestEvent(Target.MEMORANDUM, documentRef));
    }

    @Subscribe
    void openCouncilExplanatory(OpenExplanatoryEvent event) {
        final String documentRef = event.getCouncilExplanatory().getMetadata().getInternalRef();
        RepositoryContext repositoryContext = repositoryContextProvider.get();
        repositoryContext.populateVersionsWithoutVersionLabel(Explanatory.class, documentRef);
        eventBus.post(new NavigationRequestEvent(Target.COUNCIL_EXPLANATORY, documentRef));
    }

    @Subscribe
    void openLegalText(OpenLegalTextEvent event) {
        final String documentRef = event.getLegalText().getMetadata().getInternalRef();
        RepositoryContext repositoryContext = repositoryContextProvider.get();
        repositoryContext.populateVersionsWithoutVersionLabel(Bill.class, documentRef);
        eventBus.post(new NavigationRequestEvent(Target.LEGALTEXT, documentRef));
    }

    @Subscribe
    void openAnnex(OpenAnnexEvent event) {
        final String documentRef = event.getAnnex().getMetadata().getInternalRef();
        RepositoryContext repositoryContext = repositoryContextProvider.get();
        repositoryContext.populateVersionsWithoutVersionLabel(Annex.class, documentRef);
        eventBus.post(new NavigationRequestEvent(Target.ANNEX, documentRef));
    }

    @Subscribe
    void deleteCollectionRequest(DeleteCollectionRequest event) {
        collectionScreen.confirmCollectionDeletion();
    }

    @Subscribe
    void createMilestoneRequest(CreateMilestoneRequestEvent event){
        collectionScreen.openCreateMilestoneWindow();
    }

    @Subscribe
    void createMilestone(CreateMilestoneEvent event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            final String milestoneComment = event.getMilestoneComment();
            final String versionComment = messageHelper.getMessage("milestone.versionComment");
            final CollectionContext context = proposalContextProvider.get();
            createMajorVersions(proposalId, milestoneComment, versionComment, context);
            String milestoneProposalId = context.getUpdatedProposalId();
            saveLegFileInCMIS(milestoneProposalId, milestoneComment);
            populateData();
            eventBus.post(new NavigationRequestEvent(Target.PROPOSAL, getProposalRef()));
            LOG.info("Milestone created for proposal {} id {}, in {} milliseconds ({} sec)", getProposalRef(), proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch(WebServiceException wse) {
            LOG.error("External system not available due to WebServiceException", wse);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "document.create.milestone.url.error", wse.getMessage()));
        } catch (Exception e){
            LOG.error("Milestone creation request failed.", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "document.create.milestone.error", e.getMessage()));
        }
    }

    @Subscribe
    public void addNewMilestone(MilestoneCreatedEvent milestoneCreatedEvent){
        if(milestoneCreatedEvent.getProposalVersionSeriesId().equals(proposalVersionSeriesId)){
            MilestonesVO newMilestonesVO = getMilestonesVO(milestoneCreatedEvent.getLegDocument());
            long stamp = milestonesVOsLock.writeLock();
            try {
                if(milestonesVOs.add(newMilestonesVO)){
                    collectionScreen.populateMilestones(milestonesVOs);
                    eventBus.post(new NotificationEvent(leosUI, "milestone.caption", "milestone.creation", NotificationEvent.Type.TRAY, StringEscapeUtils.escapeHtml4(newMilestonesVO.getTitle())));
                }
            } finally {
                milestonesVOsLock.unlockWrite(stamp);
            }
        }
    }

    @Subscribe
    public void updateMilestones(MilestoneUpdatedEvent milestoneUpdatedEvent) {
        MilestonesVO eventMilestone = getMilestonesVO(milestoneUpdatedEvent.getLegDocument());
        long stamp = milestonesVOsLock.writeLock();
        try{
            MilestonesVO milestoneToBeUpdated = milestonesVOs.stream().filter(milestonesVO -> milestonesVO.getLegDocumentName().equals(eventMilestone.getLegDocumentName()))
                                                             .findAny().orElse(null);
            if(milestoneToBeUpdated != null && !milestoneToBeUpdated.equals(eventMilestone)){
                milestoneToBeUpdated.setStatus(eventMilestone.getStatus());
                milestoneToBeUpdated.setUpdatedDate(eventMilestone.getUpdatedDate());
                collectionScreen.populateMilestones(milestonesVOs);
                eventBus.post(new NotificationEvent(leosUI, "milestone.caption", "milestone.updated", NotificationEvent.Type.TRAY, StringEscapeUtils.escapeHtml4(milestoneToBeUpdated.getTitle())));
            }
        } finally {
             milestonesVOsLock.unlockWrite(stamp);
        }
    }

    @Subscribe
    void createMilestoneRevisionRequest(CreateRevisionRequestEvent event){
        collectionScreen.openCreateRevisionWindow(event.getVo());
    }

    @Subscribe
    void sendForRevisionRequest(CloneProposalRequestEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
        LegDocument legDocument = packageService.findDocumentByPackagePathAndName(leosPackage.getPath(), event.getMilestonesVO().getLegDocumentName(), LegDocument.class);
        String loggedInUser = securityContext.getUser().getLogin();
        Collection<? extends GrantedAuthority> loggedInUserAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        try {
            File content = new File(event.getMilestonesVO().getLegDocumentName());
            try (FileOutputStream fos = new FileOutputStream(content)) {
                fos.write(legDocument.getContent().get().getSource().getBytes());
            } catch (IOException ioe) {
                LOG.error("Error Occurred while reading the Leg file: " + ioe.getMessage(), ioe);
            }
            userService.switchUser(event.getUserVO().getLogin());
            CreateCollectionResult createCollectionResult = collectionService.cloneCollection(content, cloneOriginRef, event.getUserVO().getLogin(), event.getUserVO().getEntity());
            if (createCollectionResult != null && createCollectionResult.getError() != null) {
                LOG.error("Error Occurred while cloning proposal from the Leg file: " + createCollectionResult.getError().getMessage());
                eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "milestone.clone.failed"));
            } else {
                eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "milestone.clone.success"));
            }
            LOG.info("Proposal '{}' sent for revision to user '{}' in {} milliseconds ({} sec)", proposalId, event.getUserVO().getLogin(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception ex) {
            LOG.error("Error Occurred while cloning proposal from the Leg file: " + ex.getMessage(), ex);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "milestone.clone.failed"));
        } finally {
            userService.switchUserWithAuthorities(loggedInUser, loggedInUserAuthorities);
            populateData();
        }
    }

    /**
     * Create a major version in CMIS for all documents of this proposal: bill, memorandum, annexes, etc.
     * If already a major version, do not create it.
     * @param proposalId the proposal Id
     * @param milestoneComment the milestone title
     * @param versionComment the version comment
     * @param context the proposal context
     */
    private void createMajorVersions(String proposalId, String milestoneComment, String versionComment, CollectionContext context) {
        context.useProposal(proposalId);
        context.useMilestoneComment(milestoneComment);
        context.useVersionComment(versionComment);
        context.executeCreateMilestone();
    }

    private void saveLegFileInCMIS(String proposalId, String milestoneComment) throws Exception {
        LegDocument newLegDocument = milestoneService.createMilestone(proposalId, milestoneComment);
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.create.milestone.created"));
        leosApplicationEventBus.post(new MilestoneCreatedEvent(proposalVersionSeriesId, newLegDocument));
        LOG.trace("Milestone creation successfully requested leg");
    }

    @Subscribe
    void deleteCollection(DeleteCollectionEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        CollectionContext context = proposalContextProvider.get();
        context.useProposal(proposalId);
        context.executeDeleteProposal();
        if(cloneProposalMetadataVO != null && cloneProposalMetadataVO.isClonedProposal()) {
            String originalProposalId = cloneProposalMetadataVO.getClonedFromObjectId();
            proposalService.removeClonedProposalMetadata(originalProposalId, proposalRef, cloneProposalMetadataVO);
            LOG.info("Cloned proposal metadata with proposal ref {} is cleaned up from original proposal with id {}", proposalRef, originalProposalId);
        }
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "collection.deleted"));
        eventBus.post(new NavigationRequestEvent(Target.WORKSPACE));
        LOG.info("Proposal {} id {} deleted, in {} milliseconds ({} sec)", proposalRef, proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
    }

    @Subscribe
    void exportProposal(ExportProposalEvent event) {
        File downloadFile = null;
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            ExportOptions exportOptions = event.getExportOptions();
            if(exportOptions instanceof  ExportLeos){
                eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "export.pdf.unavailable"));
            }
            else if (exportOptions instanceof ExportLW){
                String jobId = exportService.exportToToolboxCoDe(proposalId, exportOptions);
                eventBus.post(new NotificationEvent("collection.caption.menuitem.export",
                        "collection.exported",
                        NotificationEvent.Type.TRAY,
                        exportOptions.getExportOutputDescription(),
                        user.getEmail(),
                        jobId));
            } else {
                throw new Exception("Bad export format option");
            }
            LOG.info("Proposal {} id {} exported {}, in {} milliseconds ({} sec)", getProposalRef(), proposalId, exportOptions.getExportOutput(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch(WebServiceException wse) {
            LOG.error("External system not available due to WebServiceException: {}", wse.getMessage());
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.export.url.error", wse.getMessage()));
        } catch (Exception e) {
            LOG.error("Unexpected error occured while sending job to ToolBox: {}", e.getMessage());
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.export.error", e.getMessage()));
        } finally {
            if(downloadFile != null) {
                downloadFile.delete();
            }
        }
    }

    @Subscribe
    void exportMandate(ExportMandateEvent event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
            BillContext context = billContextProvider.get();
            context.usePackage(leosPackage);
            if (proposalId != null) {
                final String jobFileName = "Proposal_" + proposalId + "_AKN2DW_" + System.currentTimeMillis() + ".zip";
                byte[] exportedBytes = exportService.createDocuWritePackage(jobFileName, proposalId, event.getExportOptions());
                DownloadStreamResource downloadStreamResource = new DownloadStreamResource(jobFileName, new ByteArrayInputStream(exportedBytes));
                collectionScreen.setDownloadStreamResource(downloadStreamResource);
            }
            LOG.info("Document {} exported, in {} milliseconds ({} sec)", event.getExportOptions().getFileType(), stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while using ExportService", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "export.docuwrite.error.message", e.getMessage()));
        }
    }

    private String getJobFileName() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Proposal_");
        strBuilder.append(proposalId);
        strBuilder.append(".zip");
        return strBuilder.toString();
    }

    private void prepareDownloadPackage(File packageFile) throws IOException {
        if (packageFile != null) {
            final byte[] fileBytes = FileUtils.readFileToByteArray(packageFile);
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(packageFile.getName(), new ByteArrayInputStream(fileBytes));
            collectionScreen.setDownloadStreamResource(downloadStreamResource);
            eventBus.post(new NotificationEvent("menu.download.caption", "collection.downloaded", NotificationEvent.Type.TRAY));
            LOG.trace("Successfully prepared proposal for download");
        }
    }

    @Subscribe
    void prepareProposalDownloadPackage(DownloadProposalEvent event) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        String jobFileName = getJobFileName();
        File packageFile = null;
        try {
            packageFile = exportService.createCollectionPackage(jobFileName, proposalId, new ExportLW(ExportOptions.Output.WORD));
            prepareDownloadPackage(packageFile);
            LOG.info("Proposal {} id {} downloaded, in {} milliseconds ({} sec)", getProposalRef(), proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Error while creating download proposal package", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.downloaded.error", e.getMessage()));
        } finally {
            if (packageFile != null && packageFile.exists()) {
                packageFile.delete();
            }
        }
    }

    @Subscribe
    void prepareMandateDownloadPackage(DownloadMandateEvent event) {
        String jobFileName = getJobFileName();
        File packageFile = null;
        try {
            packageFile = exportService.createCollectionPackage(jobFileName, proposalId, new ExportDW(ExportOptions.Output.WORD));
            prepareDownloadPackage(packageFile);
        } catch (Exception e) {
            LOG.error("Error while creating download proposal package", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collection.downloaded.error", e.getMessage()));
        } finally {
            if (packageFile != null && packageFile.exists()) {
                packageFile.delete();
            }
        }
    }

    @Subscribe
    void closeProposalView(CloseScreenRequestEvent event) {
        eventBus.post(new NavigationRequestEvent(Target.PREVIOUS));
    }

    @Subscribe
    void searchUser(SearchUserRequest event) {
        List<User> users = userHelper.searchUsersByKey(event.getSearchKey());
        collectionScreen.proposeUsers(users.stream().map(UserVO::new).collect(Collectors.toList()));
    }

    @Subscribe
    void searchUserInContext(SearchUserInContextEvent event) {
        List<User> users = userHelper.searchUsersInContextByKey(event.getSearchKey(), event.getSearchContext());
        collectionScreen.proposeUsers(users.stream().map(UserVO::new).collect(Collectors.toList()));
    }

    @Subscribe
    void addCollaborator(AddCollaboratorRequest event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            UserVO user = event.getCollaborator().getUser();
            Role role = event.getCollaborator().getRole();
            LOG.trace("Adding collaborator...{}, with authority {}", user.getLogin(), role);
            getXmlDocuments().forEach(doc -> updateCollaborators(user, role, doc, false));
    
            notificationService.sendNotification(new AddCollaborator(user, user.getEntity(), role.getName(), proposalId, event.getProposalURL()));
            eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "collaborator.message.added", user.getName(),
                    messageHelper.getMessage(role.getMessageKey())));
            leosApplicationEventBus.post(new CollaboratorsUpdatedEvent(id, proposalVersionSeriesId, Operation.ADDED));
            LOG.info("Collaborator '{}' inserted to proposal {} id {}, in {} milliseconds ({} sec)", user.getLogin(), getProposalRef(), proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while removeCollaborator", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "generic.error.message", e.getMessage()));
        }
    }

    @Subscribe
    void removeCollaborator(RemoveCollaboratorRequest event) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            UserVO user = event.getCollaborator().getUser();
            Role role = event.getCollaborator().getRole();
            List<XmlDocument> documents = getXmlDocuments();
            LOG.trace("Removing collaborator...{}, with authority {}", user.getLogin(), role);
            if (checkCollaboratorIsNotLastOwner(documents, role)) {
                documents.forEach(doc -> updateCollaborators(user, role, doc, true));
                notificationService.sendNotification(new RemoveCollaborator(user, user.getEntity(), role.getName(), proposalId, event.getProposalURL()));
                eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "collaborator.message.removed", user.getName()));
            } else {
                eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collaborator.message.last.owner.removed",
                        messageHelper.getMessage(role.getMessageKey())));
            }
            leosApplicationEventBus.post(new CollaboratorsUpdatedEvent(id, proposalVersionSeriesId, Operation.REMOVED));
            LOG.info("Collaborator '{}' removed prom proposal {} id {}, in {} milliseconds ({} sec)", user.getLogin(), getProposalRef(), proposalId, stopwatch.elapsed(TimeUnit.MILLISECONDS), stopwatch.elapsed(TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while removeCollaborator", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "generic.error.message", e.getMessage()));
        }
    }

    @Subscribe
    void editCollaborator(EditCollaboratorRequest event) {
        UserVO user = event.getCollaborator().getUser();
        Role role = event.getCollaborator().getRole();
        List<XmlDocument> documents = getXmlDocuments();
        String collaboratorRole = documents.get(0).getCollaborators().stream()
                .filter(c -> user.getLogin().equals(c.getLogin()))
                .map(Collaborator::getRole)
                .findFirst()
                .orElse(null);
        Role oldRole = authorityMapHelper.getRoleFromListOfRoles(collaboratorRole);
        LOG.trace("Updating collaborator...{}, old authority {}, with new authority {}", user.getLogin(), oldRole, role);
        try {
            if (checkCollaboratorIsNotLastOwner(documents, oldRole)) {
                documents.forEach(doc -> updateCollaborators(user, role, doc, false));
                notificationService.sendNotification(new EditCollaborator(user, user.getEntity(), role.getName(), proposalId, event.getProposalURL()));
                eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "collaborator.message.edited", user.getName(),
                        messageHelper.getMessage(oldRole.getMessageKey()),
                        messageHelper.getMessage(role.getMessageKey())));
            } else {
                eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "collaborator.message.last.owner.edited",
                        messageHelper.getMessage(oldRole.getMessageKey())));
            }
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while editing Collaborator", e);
            eventBus.post(new NotificationEvent(NotificationEvent.Type.ERROR, "generic.error.message", e.getMessage()));
        }
        leosApplicationEventBus.post(new CollaboratorsUpdatedEvent(id, proposalVersionSeriesId, Operation.EDITED));
    }

    //Update document based on action(add/edit/remove)
    private void updateCollaborators(UserVO user, Role role, XmlDocument doc, boolean isRemoveAction) {
        List<Collaborator> collaborators = doc.getCollaborators();
        collaborators.removeIf(c -> c.getLogin().equals(user.getLogin()) &&
                (c.getEntity() == null || c.getEntity().equals(user.getSelectedEntity().getName())));
        if (!isRemoveAction) {
            //pick selectedEntity or first found entity if no selectedEntity defined
            String entity;
            if(user.getSelectedEntity() != null) {
                entity = user.getSelectedEntity().getName();
            } else {
                entity = user.getEntities().get(0) != null ? user.getEntities().get(0).getName() : null;
            }
            collaborators.add(new Collaborator(user.getLogin(), role.getName(), entity));
        }
        securityService.updateCollaborators(doc.getId(), collaborators, doc.getClass());
    }

    //Check if in collaborators there is only one author
    private boolean checkCollaboratorIsNotLastOwner(List<XmlDocument> documents, Role role) {
        boolean isLastOwner = false;
        boolean canAddRemoveCollaborators = false;
        for(String permission : role.getPermissions().getPermissions()){
            if(CAN_ADD_REMOVE_COLLABORATOR.name().equals(permission)){
                canAddRemoveCollaborators = true;
            }
        }
        if (role.isCollaborator() && canAddRemoveCollaborators) {
            List<Collaborator> collaborators = documents.get(0).getCollaborators();
            long frequency = collaborators.stream()
                    .filter(collaborator -> role.getName().equals(collaborator.getRole()))
                    .count();
            if (frequency == 1) {
                isLastOwner = true;
            }
        }
        return !isLastOwner;
    }

    private List<XmlDocument> getXmlDocuments() {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
        return packageService.findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, false);
    }

    @Subscribe
    private void onCollaboratorsUpdate(CollaboratorsUpdatedEvent event) {
        if (event.getDocumentId().equals(proposalVersionSeriesId) &&
                (event.getOperation().equals(Operation.EDITED) || event.getOperation().equals(Operation.REMOVED) || 
                        (event.getOperation().equals(Operation.ADDED) && !event.getPresenterId().equals(id)))) {
            populateData();
            populateExportPackages();
        }
    }

    @Subscribe
    void updateProposalMetadata(DocumentUpdatedEvent event) {
        if (event.isModified()) {
            CollectionContext context = proposalContextProvider.get();
            context.useChildDocument(proposalId);
            context.useActionComment(messageHelper.getMessage("operation.metadata.updated"));
            context.executeUpdateProposalAsync();
        }
    }

    @Subscribe
    public void onInfoUpdate(UpdateUserInfoEvent updateUserInfoEvent) {
        if(isCurrentInfoId(updateUserInfoEvent.getActionInfo().getInfo().getDocumentId())) {
            if (!user.getLogin().equals(updateUserInfoEvent.getActionInfo().getInfo().getUserLoginName())) {
                eventBus.post(new NotificationEvent(leosUI, "coedition.caption", "coedition.operation." + updateUserInfoEvent.getActionInfo().getOperation().getValue(),
                        NotificationEvent.Type.TRAY, updateUserInfoEvent.getActionInfo().getInfo().getUserName()));
            }
            LOG.debug("Proposal Presenter updated the edit info -" + updateUserInfoEvent.getActionInfo().getOperation().name());
            collectionScreen.updateUserCoEditionInfo(coEditionHelper.getAllEditInfo(), user);
        }
    }
    
    @Subscribe
    public void fetchMilestone(FetchMilestoneEvent event) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposalId);
        LegDocument legDocument = packageService.findDocumentByPackagePathAndName(leosPackage.getPath(), event.getLegFileName(), LegDocument.class);
        collectionScreen.showMilestoneExplorer(legDocument, event.getMilestoneTitle(), getProposalRef());
    }
    
    private boolean isCurrentInfoId(String versionSeriesId) {
        return docVersionSeriesIds.contains(versionSeriesId);
    }

    @Subscribe
    public void afterClosedWindow(WindowClosedEvent<MilestoneExplorer> windowClosedEvent) {
        LOG.debug("Caught windowClosedEvent for MilestoneExplorer");
    }
}
