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
package eu.europa.ec.leos.ui.view.memorandum;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.VaadinServletService;
import eu.europa.ec.leos.cmis.domain.ContentImpl;
import eu.europa.ec.leos.cmis.domain.SourceImpl;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.model.event.DocumentUpdatedByCoEditorEvent;
import eu.europa.ec.leos.model.event.UpdateUserInfoEvent;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.content.SearchService;
import eu.europa.ec.leos.services.content.TemplateConfigurationService;
import eu.europa.ec.leos.services.content.processor.DocumentContentService;
import eu.europa.ec.leos.services.content.processor.ElementProcessor;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.ui.component.ComparisonComponent;
import eu.europa.ec.leos.ui.event.CloseBrowserRequestEvent;
import eu.europa.ec.leos.ui.event.CloseScreenRequestEvent;
import eu.europa.ec.leos.ui.event.DownloadXmlVersionRequestEvent;
import eu.europa.ec.leos.ui.event.FetchMilestoneByVersionedReferenceEvent;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.DocumentMetadataResponse;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataRequest;
import eu.europa.ec.leos.ui.event.metadata.SearchMetadataResponse;
import eu.europa.ec.leos.ui.event.search.ReplaceAllMatchRequestEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceAllMatchResponseEvent;
import eu.europa.ec.leos.ui.event.search.ReplaceMatchRequestEvent;
import eu.europa.ec.leos.ui.event.search.SaveAfterReplaceEvent;
import eu.europa.ec.leos.ui.event.search.SaveAndCloseAfterReplaceEvent;
import eu.europa.ec.leos.ui.event.search.SearchBarClosedEvent;
import eu.europa.ec.leos.ui.event.search.SearchTextRequestEvent;
import eu.europa.ec.leos.ui.event.search.ShowConfirmDialogEvent;
import eu.europa.ec.leos.ui.event.toc.CloseEditTocEvent;
import eu.europa.ec.leos.ui.event.view.DownloadXmlFilesRequestEvent;
import eu.europa.ec.leos.ui.model.AnnotateMetadata;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.ui.view.AbstractLeosPresenter;
import eu.europa.ec.leos.ui.view.CommonDelegate;
import eu.europa.ec.leos.ui.view.ComparisonDelegate;
import eu.europa.ec.leos.ui.view.ComparisonDisplayMode;
import eu.europa.ec.leos.usecases.document.CollectionContext;
import eu.europa.ec.leos.vo.coedition.InfoType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.NotificationEvent.Type;
import eu.europa.ec.leos.web.event.component.CleanComparedContentEvent;
import eu.europa.ec.leos.web.event.component.CompareRequestEvent;
import eu.europa.ec.leos.web.event.component.CompareTimeLineRequestEvent;
import eu.europa.ec.leos.web.event.component.LayoutChangeRequestEvent;
import eu.europa.ec.leos.web.event.component.RestoreVersionRequestEvent;
import eu.europa.ec.leos.web.event.component.ShowVersionRequestEvent;
import eu.europa.ec.leos.web.event.component.VersionListRequestEvent;
import eu.europa.ec.leos.web.event.component.VersionListResponseEvent;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.ComparisonEvent;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchUserGuidanceRequest;
import eu.europa.ec.leos.web.event.view.document.FetchUserPermissionsRequest;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionRequest;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionResponse;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionsRequest;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.SaveIntermediateVersionEvent;
import eu.europa.ec.leos.web.event.view.document.ShowIntermediateVersionWindowEvent;
import eu.europa.ec.leos.web.event.window.CancelElementEditorEvent;
import eu.europa.ec.leos.web.event.window.CloseElementEditorEvent;
import eu.europa.ec.leos.web.event.window.ShowTimeLineWindowEvent;
import eu.europa.ec.leos.web.model.VersionInfoVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.support.xml.DownloadStreamResource;
import eu.europa.ec.leos.web.ui.navigation.Target;
import eu.europa.ec.leos.web.ui.screen.document.ColumnPosition;
import io.atlassian.fugue.Option;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
class MemorandumPresenter extends AbstractLeosPresenter {

    private static final Logger LOG = LoggerFactory.getLogger(MemorandumPresenter.class);

    private final MemorandumScreen memorandumScreen;
    private final MemorandumService memorandumService;
    private final ElementProcessor<Memorandum> elementProcessor;
    private final DocumentContentService documentContentService;
    private final UrlBuilder urlBuilder;
    private final TemplateConfigurationService templateConfigurationService;
    private final ComparisonDelegate<Memorandum> comparisonDelegate;
    private final UserHelper userHelper;
    private final MessageHelper messageHelper;
    private final Provider<CollectionContext> proposalContextProvider;
    private final CoEditionHelper coEditionHelper;
    private final Provider<StructureContext> structureContextProvider;
    private final LegService legService;
    private final ProposalService proposalService;
    private final SearchService searchService;
    private final CommonDelegate<Memorandum> commonDelegate;
    private final CloneContext cloneContext;

    private String strDocumentVersionSeriesId;
    private String documentId;
    private String documentRef;
    private boolean comparisonMode;
    private String proposalRef;
    private String connectedEntity;

    private CloneProposalMetadataVO cloneProposalMetadataVO;
    
    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @Autowired
    MemorandumPresenter(SecurityContext securityContext, HttpSession httpSession, EventBus eventBus,
                        MemorandumScreen memorandumScreen,
                        MemorandumService memorandumService,
                        ElementProcessor<Memorandum> elementProcessor,
                        DocumentContentService documentContentService,
                        UrlBuilder urlBuilder,
                        TemplateConfigurationService templateConfigurationService,
                        ComparisonDelegate<Memorandum> comparisonDelegate,
                        UserHelper userHelper, MessageHelper messageHelper,
                        Provider<CollectionContext> proposalContextProvider,
                        CoEditionHelper coEditionHelper, EventBus leosApplicationEventBus, UuidHelper uuidHelper,
                        Provider<StructureContext> structureContextProvider,
                        PackageService packageService,
                        WorkspaceService workspaceService, LegService legService,
                        ProposalService proposalService,
                        SearchService searchService, CommonDelegate<Memorandum> commonDelegate,
                        CloneContext cloneContext) {
        super(securityContext, httpSession, eventBus, leosApplicationEventBus, uuidHelper, packageService, workspaceService);
        LOG.trace("Initializing memorandum presenter...");
        this.memorandumScreen = memorandumScreen;
        this.memorandumService = memorandumService;
        this.elementProcessor = elementProcessor;
        this.documentContentService = documentContentService;
        this.urlBuilder = urlBuilder;
        this.templateConfigurationService = templateConfigurationService;
        this.comparisonDelegate = comparisonDelegate;
        this.userHelper = userHelper;
        this.messageHelper = messageHelper;
        this.proposalContextProvider = proposalContextProvider;
        this.coEditionHelper = coEditionHelper;
        this.structureContextProvider = structureContextProvider;
        this.legService = legService;
        this.proposalService = proposalService;
        this.searchService = searchService;
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
            populateViewData();
            populateVersionsData();
        } catch (Exception exception) {
            LOG.error("Exception occurred in init(): ", exception);
            eventBus.post(new NotificationEvent(Type.INFO, "unknown.error.message"));
        }
    }
    
    private void populateWithProposalRefAndConnectedEntity() {
        Memorandum memorandum = getDocument();
        if (memorandum != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(memorandum.getId());
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
        memorandumScreen.populateCloneProposalMetadataVO(cloneProposalMetadataVO);
        cloneContext.setCloneProposalMetadataVO(cloneProposalMetadataVO);
    }

    private void resetCloneProposalMetadataVO() {
        memorandumScreen.populateCloneProposalMetadataVO(null);
        cloneContext.setCloneProposalMetadataVO(null);
    }

    private String getDocumentRef() {
        return (String) httpSession.getAttribute(id + "." + SessionAttribute.MEMORANDUM_REF.name());
    }

    private Memorandum getDocument() {
        documentRef = getDocumentRef();
        Memorandum memorandum = memorandumService.findMemorandumByRef(documentRef);
        strDocumentVersionSeriesId = memorandum.getVersionSeriesId();
        documentId = memorandum.getId();
        structureContextProvider.get().useDocumentTemplate(memorandum.getMetadata().getOrError(() -> "Memorandum metadata is required!").getDocTemplate());
        return memorandum;
    }

    private void populateViewData() {
        try{
            Memorandum memorandum = getDocument();
            memorandumScreen.setTitle("Explanatory Memorandum"); //FIXME Temporary implementation waiting for Memorandum title feature development
            memorandumScreen.setDocumentVersionInfo(getVersionInfo(memorandum));
            String content = getEditableXml(memorandum);
            memorandumScreen.setContent(content);
            memorandumScreen.setToc(getTableOfContent(memorandum));
            DocumentVO memorandumVO = createMemorandumVO(memorandum);
            memorandumScreen.setPermissions(memorandumVO);
            memorandumScreen.initAnnotations(memorandumVO, proposalRef, connectedEntity);
            memorandumScreen.updateUserCoEditionInfo(coEditionHelper.getCurrentEditInfo(memorandum.getVersionSeriesId()), id);
        }
        catch (Exception ex) {
            LOG.error("Error while processing document", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }
    
    private void populateVersionsData() {
        final List<VersionVO> allVersions = memorandumService.getAllVersions(documentId, documentRef);
        memorandumScreen.setDataFunctions(
                allVersions,
                this::majorVersionsFn, this::countMajorVersionsFn,
                this::minorVersionsFn, this::countMinorVersionsFn,
                this::recentChangesFn, this::countRecentChangesFn);
    }
    
    @Subscribe
    public void updateVersionsTab(DocumentUpdatedEvent event) {
        final List<VersionVO> allVersions = memorandumService.getAllVersions(documentId, documentRef);
        memorandumScreen.refreshVersions(allVersions, comparisonMode);
    }
    
    private Integer countMinorVersionsFn(String currIntVersion) {
        return memorandumService.findAllMinorsCountForIntermediate(documentRef, currIntVersion);
    }
    
    private List<Memorandum> minorVersionsFn(String currIntVersion, int startIndex, int maxResults) {
        return memorandumService.findAllMinorsForIntermediate(documentRef, currIntVersion, startIndex, maxResults);
    }
    
    private Integer countMajorVersionsFn() {
        return memorandumService.findAllMajorsCount(documentRef);
    }
    
    private List<Memorandum> majorVersionsFn(int startIndex, int maxResults) {
        return memorandumService.findAllMajors(documentRef, startIndex, maxResults);
    }
    
    private Integer countRecentChangesFn() {
        return memorandumService.findRecentMinorVersionsCount(documentId, documentRef);
    }
    
    private List<Memorandum> recentChangesFn(int startIndex, int maxResults) {
        return memorandumService.findRecentMinorVersions(documentId, documentRef, startIndex, maxResults);
    }

    private List<TableOfContentItemVO> getTableOfContent(Memorandum memorandum) {
        return memorandumService.getTableOfContent(memorandum, TocMode.NOT_SIMPLIFIED);
    }

    @Subscribe
    void getDocumentVersionsList(VersionListRequestEvent<Memorandum> event) {
        List<Memorandum> memoVersions = memorandumService.findVersions(documentId);
        eventBus.post(new VersionListResponseEvent(new ArrayList<>(memoVersions)));
    }
    
    private String getEditableXml(Memorandum memorandum) {
        securityContext.getPermissions(memorandum);
        documentContentService.useCloneProposalMetadataVO(cloneProposalMetadataVO);
        return documentContentService.toEditableContent(memorandum,
                urlBuilder.getWebAppPath(VaadinServletService.getCurrentServletRequest()), securityContext);
    }

    @Subscribe
    void handleCloseDocument(CloseDocumentEvent event) {
        LOG.trace("Handling close document request...");

        //if unsaved changes remain in the session, first ask for confirmation
        if(isMemorandumUnsaved()){
            eventBus.post(new ShowConfirmDialogEvent(event, null));
            return;
        }
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
        eventBus.post(new NavigationRequestEvent(Target.PREVIOUS));
    }

    private boolean isMemorandumUnsaved(){
        return getMemorandumFromSession() != null;
    }
    private Memorandum getMemorandumFromSession() {
        return (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());
    }

    @Subscribe
    void handleCloseBrowserRequest(CloseBrowserRequestEvent event) {
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, null, InfoType.DOCUMENT_INFO);
        resetCloneProposalMetadataVO();
    }

    @Subscribe
    void handleCloseScreenRequest(CloseScreenRequestEvent event) {
        if (memorandumScreen.isTocEnabled()) {
            eventBus.post(new CloseEditTocEvent());
        } else {
            eventBus.post(new CloseDocumentEvent());
        }
    }

    @Subscribe
    void refreshDocument(RefreshDocumentEvent event){
        populateViewData();
    }

    @Subscribe
    void checkElementCoEdition(CheckElementCoEditionEvent event) {
        memorandumScreen.checkElementCoEdition(coEditionHelper.getCurrentEditInfo(strDocumentVersionSeriesId), user,
                event.getElementId(), event.getElementTagName(), event.getAction(), event.getActionEvent());
    }


    @Subscribe
    void cancelElementEditor(CancelElementEditorEvent event) {
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);

        //load content from session if exists
        Memorandum memorandumFromSession = getMemorandumFromSession();
        if(memorandumFromSession != null) {
            memorandumScreen.setContent(getEditableXml(memorandumFromSession));
        }else{
            eventBus.post(new RefreshDocumentEvent());
        }
        LOG.debug("User edit information removed");
    }


    @Subscribe
    void editElement(EditElementRequestEvent event){
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        
        LOG.trace("Handling edit element request... for {},id={}",elementTagName , elementId );

        try {
            //show confirm dialog if there is any unsaved replaced text
            //it can be detected from the session attribute
            if(isMemorandumUnsaved()){
                eventBus.post(new ShowConfirmDialogEvent(event, new CancelElementEditorEvent(event.getElementId(),event.getElementTagName())));
                return;
            }
            Memorandum memorandum = getDocument();
            String element = elementProcessor.getElement(memorandum, elementTagName, elementId);
            coEditionHelper.storeUserEditInfo(httpSession.getId(), id, user, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
            memorandumScreen.showElementEditor(elementId, elementTagName, element);
        }
        catch (Exception ex){
            LOG.error("Exception while edit element operation for memorandum", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void saveElement(SaveElementRequestEvent event) {
        String elementId = event.getElementId();
        String elementTagName = event.getElementTagName();
        LOG.trace("Handling save element request... for {},id={}",elementTagName , elementId );

        try {
            Memorandum memorandum = getDocument();
            byte[] newXmlContent = elementProcessor.updateElement(memorandum, event.getElementContent(), elementTagName, elementId);
            if (newXmlContent == null) {
                memorandumScreen.showAlertDialog("operation.element.not.performed");
                return;
            }

            memorandum = memorandumService.updateMemorandum(memorandum, newXmlContent, VersionType.MINOR, messageHelper.getMessage("operation." + elementTagName + ".updated"));

            if (memorandum != null) {
                String elementContent = elementProcessor.getElement(memorandum, elementTagName, elementId);
                memorandumScreen.refreshElementEditor(elementId, elementTagName, elementContent);
                eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
                eventBus.post(new NotificationEvent(Type.INFO, "document.content.updated"));
                memorandumScreen.scrollToMarkedChange(elementId);
                leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            }
        } catch (Exception ex) {
            LOG.error("Exception while save  memorandum operation", ex);
            eventBus.post(new NotificationEvent(Type.INFO, "error.message", ex.getMessage()));
        }
    }

    @Subscribe
    void closeElementEditor(CloseElementEditorEvent event){
        String elementId = event.getElementId();
        coEditionHelper.removeUserEditInfo(id, strDocumentVersionSeriesId, elementId, InfoType.ELEMENT_INFO);
        LOG.debug("User edit information removed");
        eventBus.post(new RefreshDocumentEvent());
    }

    @Subscribe
    public void getUserGuidance(FetchUserGuidanceRequest event) {
        // KLUGE temporary hack for compatibility with new domain model
        Memorandum memorandum = memorandumService.findMemorandum(documentId);
        String jsonGuidance = templateConfigurationService.getTemplateConfiguration(memorandum.getMetadata().get().getDocTemplate(), "guidance");
        memorandumScreen.setUserGuidance(jsonGuidance);
    }

    @Subscribe
    void mergeSuggestion(MergeSuggestionRequest event) {
        Memorandum document = getDocument();
        byte[] resultXmlContent = elementProcessor.replaceTextInElement(document, event.getOrigText(), event.getNewText(), event.getElementId(), event.getStartOffset(), event.getEndOffset());
        if (resultXmlContent == null) {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
            return;
        }
        document = memorandumService.updateMemorandum(document, resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.merge.suggestion"));
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
    public void mergeSuggestions(MergeSuggestionsRequest event) {
        Memorandum document = getDocument();
        commonDelegate.mergeSuggestions(document, event, elementProcessor, memorandumService::updateMemorandum);
    }

    @Subscribe
    public void getUserPermissions(FetchUserPermissionsRequest event) {
        Memorandum memorandum = getDocument();
        List<LeosPermission> userPermissions = securityContext.getPermissions(memorandum);
        memorandumScreen.sendUserPermissions(userPermissions);
    }

    @Subscribe
    public void fetchSearchMetadata(SearchMetadataRequest event){
        eventBus.post(new SearchMetadataResponse(Collections.emptyList()));
    }

    @Subscribe
    public void fetchMetadata(DocumentMetadataRequest event){
        AnnotateMetadata metadata = new AnnotateMetadata();
        Memorandum memorandum = getDocument();
        metadata.setVersion(memorandum.getVersionLabel());
        metadata.setId(memorandum.getId());
        metadata.setTitle(memorandum.getTitle());
        eventBus.post(new DocumentMetadataResponse(metadata));
    }

    @Subscribe
    void showTimeLineWindow(ShowTimeLineWindowEvent event) {
        List documentVersions = memorandumService.findVersions(documentId);
        memorandumScreen.showTimeLineWindow(documentVersions);
    }
    
    @Subscribe
    void downloadXmlFiles(DownloadXmlFilesRequestEvent event) {
        File zipFile = null;
        try {
            final Map<String, Object> contentToZip = new HashMap<>();
            
            final ExportVersions<Memorandum> exportVersions = event.getExportOptions().getExportVersions();
            final Memorandum current = exportVersions.getCurrent();
            final Memorandum original = exportVersions.getOriginal();
            final Memorandum intermediate = exportVersions.getIntermediate();
            
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
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(zipFileName, new ByteArrayInputStream(zipBytes));
            memorandumScreen.setDownloadStreamResourceForXmlFiles(downloadStreamResource);
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloadXmlFiles", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", e.getMessage()));
        } finally {
            if(zipFile != null) {
                zipFile.delete();
            }
        }
    }
    
    @Subscribe
    void downloadXmlVersion(DownloadXmlVersionRequestEvent event) {
        try {
            final Memorandum chosenDocument = memorandumService.findMemorandumVersion(event.getVersionId());
            final String fileName = chosenDocument.getMetadata().get().getRef() + "_v" + chosenDocument.getVersionLabel() + ".xml";
    
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(fileName, new ByteArrayInputStream(chosenDocument.getContent().get().getSource().getBytes()));
            memorandumScreen.setDownloadStreamResourceForVersion(downloadStreamResource, chosenDocument.getId());
        } catch (Exception e) {
            LOG.error("Unexpected error occurred while downloadXmlVersion", e);
            eventBus.post(new NotificationEvent(Type.ERROR, "error.message", e.getMessage()));
        }
    }

    @Subscribe
    void versionRestore(RestoreVersionRequestEvent event) {
        String versionId = event.getVersionId();
        Memorandum version = memorandumService.findMemorandumVersion(versionId);
        byte[] resultXmlContent = getContent(version);
        memorandumService.updateMemorandum(getDocument(), resultXmlContent, VersionType.MINOR, messageHelper.getMessage("operation.restore.version", version.getVersionLabel()));

        List documentVersions = memorandumService.findVersions(documentId);
        memorandumScreen.updateTimeLineWindow(documentVersions);
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new DocumentUpdatedEvent()); //Document might be updated.
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
    }
    
    @Subscribe
    void cleanComparedContent(CleanComparedContentEvent event) {
        memorandumScreen.cleanComparedContent();
    }
    
    @Subscribe
    void showVersion(ShowVersionRequestEvent event) {
        final Memorandum version = memorandumService.findMemorandumVersion(event.getVersionId());
        final String versionContent = comparisonDelegate.getDocumentAsHtml(version);
        final String versionInfo = getVersionInfoAsString(version);
        memorandumScreen.showVersion(versionContent, versionInfo);
    }
    
    @Subscribe
    public void fetchMilestoneByVersionedReference(FetchMilestoneByVersionedReferenceEvent event) {
        LeosPackage leosPackage = packageService.findPackageByDocumentId(documentId);
        LegDocument legDocument = legService.findLastLegByVersionedReference(leosPackage.getPath(), event.getVersionedReference());
        memorandumScreen.showMilestoneExplorer(legDocument, String.join(",", legDocument.getMilestoneComments()), proposalRef);
    }
    
    @Subscribe
    void compare(CompareRequestEvent event) {
        final Memorandum oldVersion = memorandumService.findMemorandumVersion(event.getOldVersionId());
        final Memorandum newVersion = memorandumService.findMemorandumVersion(event.getNewVersionId());
        String comparedContent = comparisonDelegate.getMarkedContent(oldVersion, newVersion);
        final String comparedInfo = messageHelper.getMessage("version.compare.simple", oldVersion.getVersionLabel(), newVersion.getVersionLabel());
        memorandumScreen.populateComparisonContent(comparedContent, comparedInfo, oldVersion, newVersion);
    }
    
    @Subscribe
    void compareUpdateTimelineWindow(CompareTimeLineRequestEvent event) {
        String oldVersionId = event.getOldVersion();
        String newVersionId = event.getNewVersion();
        ComparisonDisplayMode displayMode = event.getDisplayMode();
        HashMap<ComparisonDisplayMode, Object> result = comparisonDelegate.versionCompare(memorandumService.findMemorandumVersion(oldVersionId), memorandumService.findMemorandumVersion(newVersionId), displayMode);
        memorandumScreen.displayComparison(result);        
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
        LayoutChangeRequestEvent layoutEvent;
        if (comparisonMode) {
            memorandumScreen.cleanComparedContent();
            layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.DEFAULT, ComparisonComponent.class);
        } else {
            layoutEvent = new LayoutChangeRequestEvent(ColumnPosition.OFF, ComparisonComponent.class);
        }
        eventBus.post(layoutEvent);
        updateVersionsTab(new DocumentUpdatedEvent());
    }

    @Subscribe
    public void showIntermediateVersionWindow(ShowIntermediateVersionWindowEvent event) {
        memorandumScreen.showIntermediateVersionWindow();
    }

    @Subscribe
    public void saveIntermediateVersion(SaveIntermediateVersionEvent event) {
        Memorandum memorandum = memorandumService.createVersion(documentId, event.getVersionType(), event.getCheckinComment());
        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, "document.major.version.saved"));
        eventBus.post(new DocumentUpdatedEvent());
        leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, memorandum.getVersionSeriesId(), id));
        populateViewData();
    }
    
    private byte[] getContent(Memorandum memorandum) {
        final Content content = memorandum.getContent().getOrError(() -> "Memorandum content is required!");
        return content.getSource().getBytes();
    }

    private VersionInfoVO getVersionInfo(XmlDocument document){
        String userId = document.getLastModifiedBy();
        User user = userHelper.getUser(userId);

        return new VersionInfoVO(
                document.getVersionLabel(),
                user.getName(), user.getDefaultEntity() != null ? user.getDefaultEntity().getOrganizationName(): "",
                dateFormatter.format(Date.from(document.getLastModificationInstant())),
                document.getVersionType());
    }

    private DocumentVO createMemorandumVO(Memorandum memorandum) {
        DocumentVO memorandumVO = new DocumentVO(memorandum.getId(),
                memorandum.getMetadata().exists(m -> m.getLanguage() != null) ? memorandum.getMetadata().get().getLanguage() : "EN",
                LeosCategory.MEMORANDUM,
                memorandum.getLastModifiedBy(),
                Date.from(memorandum.getLastModificationInstant()));
        if (memorandum.getMetadata().isDefined()) {
            MemorandumMetadata metadata = memorandum.getMetadata().get();
            memorandumVO.getMetadata().setInternalRef(metadata.getRef());
        }
        if(!memorandum.getCollaborators().isEmpty()) {
            memorandumVO.addCollaborators(memorandum.getCollaborators());
        }
        
        return memorandumVO;
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
                        NotificationEvent.Type.TRAY, updateUserInfoEvent.getActionInfo().getInfo().getUserName()));
            }
            LOG.debug("Memorandum Presenter updated the edit info -" + updateUserInfoEvent.getActionInfo().getOperation().name());
            memorandumScreen.updateUserCoEditionInfo(updateUserInfoEvent.getActionInfo().getCoEditionVos(), id);
        }
    }
    
    private boolean isCurrentInfoId(String versionSeriesId) {
        return versionSeriesId.equals(strDocumentVersionSeriesId);
    }
    
    @Subscribe
    private void documentUpdatedByCoEditor(DocumentUpdatedByCoEditorEvent documentUpdatedByCoEditorEvent) {
        if (isCurrentInfoId(documentUpdatedByCoEditorEvent.getDocumentId()) &&
                !id.equals(documentUpdatedByCoEditorEvent.getPresenterId())) {
            eventBus.post(new NotificationEvent(leosUI, "coedition.caption", "coedition.operation.update", NotificationEvent.Type.TRAY,
                    documentUpdatedByCoEditorEvent.getUser().getName()));
            memorandumScreen.displayDocumentUpdatedByCoEditorWarning();
        }
    }

    @Subscribe
    void searchTextInDocument(SearchTextRequestEvent event) {
        Memorandum memorandum = (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());
        if (memorandum == null) {
            memorandum = getDocument();
        }
        List<SearchMatchVO> matches = Collections.emptyList();
        try {
            matches = searchService.searchText(getContent(memorandum), event.getSearchText(), event.matchCase, event.completeWords);
        } catch (Exception e) {
            eventBus.post(new NotificationEvent(Type.ERROR, "Error while searching{1}", e.getMessage()));
        }

        memorandumScreen.showMatchResults(event.searchID, matches);
    }

    @Subscribe
    void replaceAllTextInDocument(ReplaceAllMatchRequestEvent event) {
        Memorandum memorandumFromSession = getMemorandumFromSession();
        if (memorandumFromSession == null) {
            memorandumFromSession = getDocument();
        }

        byte[] updatedContent = searchService.replaceText(
                getContent(memorandumFromSession),
                event.getSearchText(),
                event.getReplaceText(),
                event.getSearchMatchVOs());

        Memorandum memorandumUpdated = copyIntoNew(memorandumFromSession, updatedContent);
        httpSession.setAttribute("memorandum#" + getDocumentRef(), memorandumUpdated);
        memorandumScreen.setContent(getEditableXml(memorandumUpdated));
        eventBus.post(new ReplaceAllMatchResponseEvent(true));
    }

    private Memorandum copyIntoNew(Memorandum source, byte[] updatedContent) {
        Content contentFromSession = source.getContent().get();
        Content.Source updatedSource = new SourceImpl(new ByteArrayInputStream(updatedContent));
        Content contentObj = new ContentImpl(
                contentFromSession.getFileName(),
                contentFromSession.getMimeType(),
                updatedContent.length,
                updatedSource
        );
        Option<Content> updatedContentOptionObj = Option.option(contentObj);
        return new Memorandum(
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
    void saveAndCloseAfterReplace(SaveAndCloseAfterReplaceEvent event){
        // save document into repository
        Memorandum memorandum = getDocument();

        Memorandum memorandumFromSession = (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());
        httpSession.removeAttribute("memorandum#" + getDocumentRef());

        memorandum = memorandumService.updateMemorandum(memorandum, memorandumFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (memorandum != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void saveAfterReplace(SaveAfterReplaceEvent event){
        // save document into repository
        Memorandum memorandum = getDocument();

        Memorandum memorandumFromSession = (Memorandum) httpSession.getAttribute("memorandum#" + getDocumentRef());

        memorandum = memorandumService.updateMemorandum(memorandum, memorandumFromSession.getContent().get().getSource().getBytes(),
                VersionType.MINOR, messageHelper.getMessage("operation.search.replace.updated"));
        if (memorandum != null) {
            httpSession.setAttribute("memorandum#"+getDocumentRef(), memorandum);
            eventBus.post(new DocumentUpdatedEvent());
            leosApplicationEventBus.post(new DocumentUpdatedByCoEditorEvent(user, strDocumentVersionSeriesId, id));
            eventBus.post(new NotificationEvent(Type.INFO, "document.replace.success"));
        }
    }

    @Subscribe
    void replaceOneTextInDocument(ReplaceMatchRequestEvent event) {
        if (event.getSearchMatchVO().isReplaceable()) {
            Memorandum memorandumFromSession = getMemorandumFromSession();
            if (memorandumFromSession == null) {
                memorandumFromSession = getDocument();
            }

            byte[] updatedContent = searchService.replaceText(
                    getContent(memorandumFromSession),
                    event.getSearchText(),
                    event.getReplaceText(),
                    Arrays.asList(event.getSearchMatchVO()));

            Memorandum memorandumUpdated = copyIntoNew(memorandumFromSession, updatedContent);
            httpSession.setAttribute("memorandum#" + getDocumentRef(), memorandumUpdated);
            memorandumScreen.setContent(getEditableXml(memorandumUpdated));
            memorandumScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), true);
        } else {
            memorandumScreen.refineSearch(event.getSearchId(), event.getMatchIndex(), false);
        }
    }

    @Subscribe
    void closeSearchBar(SearchBarClosedEvent event) {
        //Cleanup the session etc
        memorandumScreen.closeSearchBar();
        httpSession.removeAttribute("memorandum#"+getDocumentRef());
        eventBus.post(new RefreshDocumentEvent());
    }
}