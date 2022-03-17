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
import eu.europa.ec.leos.ui.event.DownloadDocxVersionRequestEvent;
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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;

import javax.inject.Provider;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    void downloadDocxVersion(DownloadDocxVersionRequestEvent event) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("http://127.0.0.1:5000/parser");
            String xml = "<akomaNtoso xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\" xmlns:leos=\"urn:eu:europa:ec:leos\"> <bill name=\"istatymas\"> <meta> <identification source=\"https://www.e-tar.lt/portal/lt/legalAct/TAR.B4FA4C56B8D5/asr\"> <FRBRWork> <FRBRthis value=\"/akn/lt/act/istatymas/2012-09-18/XI-2220/!main\"/> <FRBRuri value=\"/akn/lt/act/istatymas/2012-09-18/XI-2220\"/> <FRBRdate date=\"2012-09-18\" name=\"Priėmimo data\"/> <FRBRauthor as=\"#\" href=\"#\"/> <FRBRcountry value=\"lt\"/> </FRBRWork> <FRBRExpression> <FRBRthis value=\"/akn/lt/act/istatymas/2021-09-30/XI-2220/lit@/!main\"/> <FRBRuri value=\"/akn/lt/act/istatymas/2021-09-30/XI-2220/lit@\"/> <FRBRdate date=\"2012-09-18\" name=\"\"/> <FRBRauthor as=\"#\" href=\"#\"/> <FRBRlanguage language=\"lt\"/> </FRBRExpression> <FRBRManifestation> <FRBRthis value=\"/akn/lt/act/istatymas/2021-09-30/XI-2220/lit@/!main.xml\"/> <FRBRuri value=\"/akn/lt/act/istatymas/2021-09-30/XI-2220/lit@.xml\"/> <FRBRdate date=\"2012-09-18\" name=\"\"/> <FRBRauthor as=\"#\" href=\"#\"/> </FRBRManifestation> </identification> <analysis source=\"https://www.e-tar.lt/portal/lt/legalAct/TAR.B4FA4C56B8D5/asr\"> <activeModifications> <textualMod eId=\"pmod_1\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_2\" type=\"insertation\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_3\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_4\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_5\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_6\" type=\"insertation\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_7\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_8\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_9\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_10\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_11\" type=\"insertation\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_12\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> <textualMod eId=\"pmod_13\" type=\"substitution\"> <source href=\"\"/> <destination href=\"\"/> <new href=\"\"/> </textualMod> </activeModifications> </analysis> <references source=\"#source\"> <activeRef eId=\"activeRef_1\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XI-2314, 2012-11-06, Žin., 2012, Nr. 132-6642 (2012-11-15), i. k. 1121010ISTA0XI-2314 Lietuvos Respublikos teisėkūros pagrindų įstatymo 25 ir 26 straipsnių pakeitimo įstatymas\"/> <activeRef eId=\"activeRef_2\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XII-415, 2013-06-27, Žin., 2013, Nr. 76-3832 (2013-07-16), i. k. 1131010ISTA0XII-415 Lietuvos Respublikos teisėkūros pagrindų įstatymo 19, 20 straipsnių pakeitimo ir papildymo įstatymas\"/> <activeRef eId=\"activeRef_3\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XII-1411, 2014-12-11, paskelbta TAR 2014-12-22, i. k. 2014-20435 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 6 ir 19 straipsnių pakeitimo įstatymas\"/> <activeRef eId=\"activeRef_4\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XII-1539, 2015-03-12, paskelbta TAR 2015-03-18, i. k. 2015-03950 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 26 straipsnio pakeitimo įstatymas\"/> <activeRef eId=\"activeRef_5\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-1743, 2018-12-11, paskelbta TAR 2018-12-18, i. k. 2018-20716 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 10 straipsnio pakeitimo įstatymas\"/> <activeRef eId=\"activeRef_6\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-2134, 2019-05-28, paskelbta TAR 2019-05-31, i. k. 2019-08731 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 2, 3, 5, 6, 7, 8, 10 straipsnių ir ketvirtojo skirsnio pakeitimo įstatymas\"/> <activeRef eId=\"activeRef_7\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-2220, 2019-06-13, paskelbta TAR 2019-06-25, i. k. 2019-10162 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 15 straipsnio pakeitimo ir Įstatymo papildymo 16-1 straipsniu įstatymas\"/> <activeRef eId=\"activeRef_8\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-2986, 2020-05-28, paskelbta TAR 2020-06-11, i. k. 2020-12791 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 15, 16 straipsnių pakeitimo ir 16-1 straipsnio pripažinimo netekusiu galios įstatymas\"/> <activeRef eId=\"activeRef_9\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-3045, 2020-06-11, paskelbta TAR 2020-06-25, i. k. 2020-13961 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 6 straipsnio pakeitimo ir Įstatymo papildymo 16-2 straipsniu įstatymas\"/> <activeRef eId=\"activeRef_10\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-3085, 2020-06-23, paskelbta TAR 2020-06-29, i. k. 2020-14360 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 20 straipsnio pakeitimo įstatymas\"/> <activeRef eId=\"activeRef_11\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-3171, 2020-06-26, paskelbta TAR 2020-07-10, i. k. 2020-15503 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 papildymo 5-1, 5-2 ir 5-3 straipsniais įstatymas\"/> <activeRef eId=\"activeRef_12\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIII-3243, 2020-06-30, paskelbta TAR 2020-07-16, i. k. 2020-15880 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 20 straipsnio pakeitimo ir Įstatymo papildymo ketvirtuoju-1 skirsniu įstatymas\"/> <activeRef eId=\"activeRef_13\" href=\"\" showAs=\"Lietuvos Respublikos Seimas, Įstatymas Nr. XIV-857, 2021-12-23, paskelbta TAR 2022-01-04, i. k. 2022-00065 Lietuvos Respublikos teisėkūros pagrindų įstatymo Nr. XI-2220 18 straipsnio pakeitimo įstatymas\"/> <TLCPerson eId=\"source\" href=\"/akn/ontology/person/somebody\" showAs=\"Somebody\"/> <TLCLocation eId=\"vilnius\" href=\"/akn/ontology/location/lt/vilnius\" showAs=\"Vilnius\"/> <TLCPerson eId=\"gitanasNauseda\" href=\"/akn/ontology/person/lt/gitanasNauseda\" showAs=\"Gitanas Nauseda\"/> <TLCRole eId=\"respublikosPrezidentas\" href=\"/akn/ontology/role/lt/respublikosPrezidentas\" showAs=\"Respublikos Prezidentas\"/> </references> <proprietary source=\"~leos\"><leos:docPurpose xml:id=\"proprietary__docpurpose\">teseė</leos:docPurpose><leos:docStage xml:id=\"proprietary__docstage\">...</leos:docStage><leos:template xml:id=\"proprietary_template\">IST01</leos:template><leos:ref>bill_cl0gp1jx0000iko54zekszcyx</leos:ref><leos:docTemplate xml:id=\"proprietary_docTemplate\">IST01</leos:docTemplate><leos:docType xml:id=\"proprietary__doctype\">...</leos:docType><leos:objectId/><leos:docVersion>0.1.0</leos:docVersion></proprietary></meta> <preface xml:id=\"preface\"> <longTitle leos:editable=\"true\" xml:id=\"longtitle\"> <p xml:id=\"akn_n0B7h9\"> <docTitle xml:id=\"longtitle__p__doctitle\"> </docTitle> <p leos:editable=\"true\" xml:id=\"akn_y5J33H\"><b xml:id=\"akn_j4XWs7\">LIETUVOS RESPUBLIKOS</b></p> <docPurpose leos:editable=\"true\" xml:id=\"preface_docpurpose\">teseė</docPurpose> <p leos:editable=\"true\" xml:id=\"akn_N5rP3j\"><b xml:id=\"akn_gI1pjk\">Į S T A T Y M A S</b></p> <eol xml:id=\"akn_EmZmUx\"/> <docDate leos:editable=\"true\" xml:id=\"longtitle__p__docdate\">2012 m. rugsėjo 18</docDate> d. Nr. <docNumber leos:editable=\"true\" xml:id=\"longtitle__p__docnumber\">XI-2220</docNumber> <eol xml:id=\"akn_2EIqPi\"/> <location eId=\"location_1\" leos:editable=\"true\" refersTo=\"#vilnius\" xml:id=\"longtitle__p__location\">Vilnius</location> <docType xml:id=\"preface_doctype\">...</docType><docStage xml:id=\"preface_docStage\">...</docStage></p> </longTitle> </preface> <preamble xml:id=\"preamble\"> <citations leos:editable=\"true\" xml:id=\"cits\"> <citation leos:editable=\"false\" refersTo=\"~legalBasis\" xml:id=\"cit_1\"> <p xml:id=\"cit_1__p\">Lietuvos Respublikos Seimas,</p> </citation> <citation leos:editable=\"true\" xml:id=\"cit_2\"> <p xml:id=\"cit_2__p\">tekstas(...)</p> </citation> <citation leos:editable=\"true\" xml:id=\"cit_3\"> <p xml:id=\"cit_3__p\">p r i i m a šį įstatymą.</p> </citation> </citations> </preamble> <body xml:id=\"body\"> <p xml:id=\"akn_DB08hy\"> <section leos:deletable=\"true\" leos:editable=\"true\" xml:id=\"akn_section_1\"><num leos:deletable=\"false\" leos:editable=\"true\" xml:id=\"akn_section_1\">PIRMASIS SKIRSNIS</num><eol xml:id=\"akn_vV7zqq\"/><heading xml:id=\"section_1__heading\">BENDROSIOS NUOSTATOS</heading></section> </p> <article leos:deletable=\"true\" leos:editable=\"true\" xml:id=\"art_1\"> <num leos:editable=\"false\" xml:id=\"art_1__num\">1 straipsnis.</num> <heading xml:id=\"art_1__heading\">Įstatymo paskirtis</heading> <paragraph xml:id=\"art_1__para_1\"> <num xml:id=\"art_1__para_1__num\">1.</num> <content xml:id=\"art_1__para_1__content\"> <p xml:id=\"art_1__para_1__content__p\">Šis įstatymas nustato teisėkūros principus, teisėkūros stadijas, valstybės ir savivaldybių institucijų ir įstaigų, kitų teisėkūroje dalyvaujančių asmenų teises ir pareigas.</p> </content> </paragraph> <paragraph xml:id=\"art_1__para_2\"> <num leos:editable=\"false\" xml:id=\"art_1__para_2__num\">2.</num> <content xml:id=\"art_1__para_2__content__p\"> <p xml:id=\"art_1_A5IXaV\">Šis įstatymas netaikomas Lietuvos Respublikos tarptautinėms sutartims, išskyrus šių sutarčių registravimą ir skelbimą.</p> </content> </paragraph> <paragraph xml:id=\"art_1__para_3\"> <num leos:editable=\"false\" xml:id=\"art_1__para_3__num\">3.</num> <content xml:id=\"art_1__para_3__content\"> <p xml:id=\"art_1_2jXEWd\">Šis įstatymas netaikomas teisės taikymo aktams, išskyrus nuostatas dėl teisės aktų registravimo, skelbimo ir įsigaliojimo.</p> </content> </paragraph> <paragraph xml:id=\"art_1__para_4\"> <num leos:editable=\"false\" xml:id=\"art_1__para_4__num\">4.</num> <content xml:id=\"art_1__para_4__content\"> <p xml:id=\"art_1_NYbHfx\">Kai piliečiai įgyvendina Lietuvos Respublikos Konstitucijoje (toliau – Konstitucija) įtvirtintą įstatymų leidybos iniciatyvos teisę, peticijos teisę ir teisę teikti Lietuvos Respublikos Seimui (toliau – Seimas) sumanymą keisti ar papildyti Konstituciją, šis įstatymas netaikomas. Referendumui siūlomas įstatymo ar kito teisės akto projektas turi atitikti šiame įstatyme nustatytus formos, struktūros, turinio ir kalbos reikalavimus.</p> </content> </paragraph> <paragraph xml:id=\"art_1__para_5\"> <num leos:editable=\"false\" xml:id=\"art_1__para_5__num\">5.</num> <content xml:id=\"art_1__para_5__content\"> <p xml:id=\"art_1_19kSce\">Kai piliečiai įgyvendina Lietuvos Respublikos Konstitucijoje (toliau – Konstitucija) įtvirtintą įstatymų leidybos iniciatyvos teisę, peticijos teisę ir teisę teikti Lietuvos Respublikos Seimui (toliau – Seimas) sumanymą keisti ar papildyti Konstituciją, šis įstatymas netaikomas. Referendumui siūlomas įstatymo ar kito teisės akto projektas turi atitikti šiame įstatyme nustatytus formos, struktūros, turinio ir kalbos reikalavimus.</p> </content> </paragraph> </article> <article leos:deletable=\"true\" leos:editable=\"true\" xml:id=\"art_2\"> <num xml:id=\"art_2__num\">2 straipsnis.</num> <heading xml:id=\"art_2__heading\">Pagrindinės šio įstatymo sąvokos</heading> <paragraph xml:id=\"art_2__para_1\"> <num xml:id=\"art_2__para_1__num\">1.</num> <content xml:id=\"art_2__para_1__content\"> <p xml:id=\"art_2_NpDl3t\"><b xml:id=\"art_2_c4PLtn\">Galiojančio teisinio reguliavimo poveikio ex post vertinimas </b> – teisinio reguliavimo taikymo ir veikimo vertinimas.</p> <mod xml:id=\"art_2__para_1__mod_6\"/> </content> </paragraph> <paragraph xml:id=\"art_2__para_2\"> <num xml:id=\"art_2__para_2__num\">2.</num><mod xml:id=\"art_2__para_2__mod_6\"/> <content xml:id=\"art_2__para_2__content\"> <p xml:id=\"art_2_gARlXV\"><b xml:id=\"art_2_RLXjni\">Įstaiga</b> – iš valstybės ar savivaldybės biudžeto arba valstybės pinigų fondo išlaikoma įstaiga, turinti teisės aktuose nustatytus įgaliojimus rengti teisės aktų projektus, taip pat Lietuvos bankas.</p> </content> </paragraph> <paragraph xml:id=\"art_2__para_3\"> <num xml:id=\"art_2__para_3__num\">3.</num><mod xml:id=\"art_2__para_3__mod_6\"/> <content xml:id=\"art_2__para_3__content\"> <p xml:id=\"art_2_82JKTo\"><b xml:id=\"art_2_sbNE4o\">Konsultavimasis su visuomene</b> – teisėkūros iniciatyvas pareiškiančių, teisės aktų projektus rengiančių, teisės aktus priimančių ir (ar) galiojančio teisinio reguliavimo poveikio ex post vertinimą atliekančių subjektų veiksmai, apimantys teisėkūros iniciatyvų, teisės aktų projektų pateikimą visuomenei susipažinti, informavimą apie galiojančio teisinio reguliavimo poveikio ex post vertinimą, taip pat gautų asmenų pasiūlymų vertinimą ir šio vertinimo rezultatų paskelbimą. Apie konsultavimosi pradžią visuomenei yra paskelbiama. </p> </content> </paragraph> <paragraph xml:id=\"art_2__para_4\"> <num xml:id=\"art_2__para_4__num\">4.</num><mod xml:id=\"art_2__para_4__mod_6\"/> <content xml:id=\"art_2__para_4__content\"> <p xml:id=\"art_2_MIUWII\"><b xml:id=\"art_2_kwI1Pe\">Numatomo teisinio reguliavimo poveikio vertinimas</b> – numatomo teisinio reguliavimo teigiamų ir neigiamų pasekmių nustatymas.</p> </content> </paragraph> <paragraph xml:id=\"art_2__para_5\"> <num xml:id=\"art_2__para_5__num\">5.</num><mod xml:id=\"art_2__para_5__mod_6\"/> <content xml:id=\"art_2__para_5__content\"> <p xml:id=\"art_2_N4TtB3\"><b xml:id=\"art_2_oP2ORk\">Suvestinė teisės akto redakcija</b> – tam tikru metu galiojusio, galiojančio ar priimto, atitinkamo subjekto pasirašyto ir paskelbto, bet dar neįsigaliojusio teisės akto tekstas, parengtas remiantis oficialiu pirminio teisės akto tekstu ir tą teisės aktą keičiančių ar papildančių teisės aktų oficialiais tekstais ir apimantis visus tame teisės akte įtvirtinto teisinio reguliavimo pakeitimus.</p> </content> </paragraph> <paragraph xml:id=\"art_2__para_6\"> <num xml:id=\"art_2__para_6__num\">6.</num><mod xml:id=\"art_2__para_6__mod_6\"/> <content xml:id=\"art_2__para_6__content\"> <p xml:id=\"art_2_iX8v5f\"><b xml:id=\"art_2_4LCN4M\">Teisėkūra</b> – procesas, apimantis teisėkūros iniciatyvų pareiškimą, teisės aktų projektų rengimą, teisės aktų priėmimą, pasirašymą ir skelbimą.</p> </content> </paragraph> <paragraph xml:id=\"art_2__para_7\"> <num xml:id=\"art_2__para_7__num\">7.</num><mod xml:id=\"art_2__para_7__mod_6\"/> <content xml:id=\"art_2__para_7__content\"> <p xml:id=\"art_2_qAO8EL\"><b xml:id=\"art_2_yEu1TV\">Teisėkūros iniciatyva</b> – pasiūlymas nustatyti naują ar keisti esamą teisinį reguliavimą nurodant šio teisinio reguliavimo nustatymo ar keitimo tikslus ir pagrindines teisinio reguliavimo nuostatas.</p> </content> </paragraph> <paragraph xml:id=\"art_2__para_8\"> <num xml:id=\"art_2__para_8__num\">8.</num><mod xml:id=\"art_2__para_8__mod_6\"/> <content xml:id=\"art_2__para_8__content\"> <p xml:id=\"art_2_ihQ9NN\"><b xml:id=\"art_2_ihQxkb\">Teisės akto projekto lydimieji dokumentai</b> – šiame įstatyme ar kituose teisės aktuose numatyti privalomi parengti ir kartu su teisės akto projektu teisės aktą priimančiam subjektui privalomi pateikti dokumentai, taip pat kiti su teisės akto projektu susiję dokumentai.</p> </content> </paragraph> <paragraph xml:id=\"art_2__para_9\"> <num xml:id=\"art_2__para_9__num\">9.</num><mod xml:id=\"art_2__para_9__mod_6\"/> <content xml:id=\"art_2__para_9__content\"> <p xml:id=\"art_2_Yx2nbX\"><b xml:id=\"art_2_28SbgJ\">Teisės aktų registras</b> – pagrindinis valstybės registras, kuriame registruojami šiame įstatyme nurodyti registro objektai, teisės aktų nustatyta tvarka renkami, kaupiami, apdorojami, sisteminami, saugomi ir teikiami registro duomenys, atliekami kiti teisės aktuose nustatyti registro duomenų tvarkymo veiksmai.</p> </content> </paragraph> <paragraph xml:id=\"art_2__para_10\"><mod xml:id=\"art_2__para_10__mod_6\"/> <num xml:id=\"art_2__para_10__num\">10.</num> <content xml:id=\"art_2__para_10__content\"> <p xml:id=\"art_2_6OVi78\">Neteko galios 2020-04-01.</p> </content> </paragraph> </article> </body> <conclusions eId=\"conclusions\" xml:id=\"akn_QeJVDy\"> <formula eId=\"conclusions__formula_1\" name=\"enactingFormula\" xml:id=\"akn_3CIrI0\"> <p xml:id=\"akn_t24XnQ\">Skelbiu šį Lietuvos Respublikos Seimo priimtą įstatymą.</p> </formula> <p xml:id=\"akn_gElam9\"> <signature xml:id=\"conclusions__block_1__signature_2\"> <role eId=\"role_1\" refersTo=\"#respublikosPrezidentas\" xml:id=\"akn_qK1ok8\">RESPUBLIKOS PREZIDENTĖ</role> <person as=\"#respublikosPrezidentas\" eId=\"person_1\" refersTo=\"#daliaGrybauskaite\" xml:id=\"akn_pgXgIi\">DALIA GRYBAUSKAITĖ</person> </signature> </p> <p xml:id=\"akn_hkCeoX\"/> </conclusions> </bill> </akomaNtoso>";
            final Memorandum chosenDocument = memorandumService.findMemorandumVersion(event.getVersionId());
            final String fileName = chosenDocument.getMetadata().get().getRef() + "_v" + chosenDocument.getVersionLabel() + ".docx";
            String content = getEditableXml(chosenDocument);
            
            StringEntity stringEntity = new StringEntity(content, "UTF-8");
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse response = client.execute(httpPost);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            response.getEntity().writeTo(baos);
            byte[] bytes = baos.toByteArray();

            
    
            DownloadStreamResource downloadStreamResource = new DownloadStreamResource(fileName, new ByteArrayInputStream(bytes));
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