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

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.Content.Source;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.Collaborator;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.LeosPermission;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.content.ReferenceLabelService;
import eu.europa.ec.leos.services.content.TemplateStructureService;
import eu.europa.ec.leos.services.content.processor.BillProcessor;
import eu.europa.ec.leos.services.content.processor.DocumentContentService;
import eu.europa.ec.leos.services.content.processor.ElementProcessor;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.importoj.ImportService;
import eu.europa.ec.leos.services.messaging.UpdateInternalReferencesProducer;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.support.xml.ref.Ref;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.test.support.model.ModelHelper;
import eu.europa.ec.leos.test.support.web.presenter.LeosPresenterTest;
import eu.europa.ec.leos.ui.event.search.ShowConfirmDialogEvent;
import eu.europa.ec.leos.ui.event.toc.InlineTocEditRequestEvent;
import eu.europa.ec.leos.ui.support.CoEditionHelper;
import eu.europa.ec.leos.vo.coedition.CoEditionActionInfo;
import eu.europa.ec.leos.vo.coedition.CoEditionActionInfo.Operation;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NavigationRequestEvent;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.event.view.document.CloseDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.DeleteElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchCrossRefTocRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ImportElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.InsertElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.event.window.CloseElementEditorEvent;
import eu.europa.ec.leos.web.model.DocType;
import eu.europa.ec.leos.web.model.SearchCriteriaVO;
import eu.europa.ec.leos.web.support.SessionAttribute;
import eu.europa.ec.leos.web.support.UrlBuilder;
import eu.europa.ec.leos.web.support.UuidHelper;
import eu.europa.ec.leos.web.support.user.UserHelper;
import eu.europa.ec.leos.web.ui.navigation.Target;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CITATIONS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITALS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class DocumentPresenterTest extends LeosPresenterTest {

    private static final String SESSION_ID = "sessionID";

    private static final String ARTICLE_TAG = ARTICLE;
    private static final String CITATIONS_TAG = CITATIONS;
    private static final String RECITALS_TAG = RECITALS;

    private static final String LEOS_ALTERNATIVE_ATTR = "leos:alternative";
    
    private static final String PRESENTER_ID = "ab51f419-c6b0-45cb-82ed-77a61099b58f";

    private SecurityContext securityContext;

    private User contextUser;

    private UuidHelper uuidHelper;

    @Mock
    private DocumentScreen documentScreen;

    @Mock
    private BillService billService;

    @Mock
    private ImportService importService;

    @Mock
    private UserHelper userHelper;

    @Mock
    private CoEditionHelper coEditionHelper;

    @Mock
    private BillProcessor billProcessor;

    @Mock
    private ElementProcessor<Bill> elementProcessor;

    @Mock
    private DocumentContentService documentContentService;
    
    @InjectMocks
    private DocumentPresenter documentPresenter ;

    @Mock
    private UrlBuilder urlBuilder;

    @Mock
    private MessageHelper messageHelper;
    
    @Mock
    private ReferenceLabelService referenceLabelService;

    @InjectMocks
    private StructureServiceImpl structureServiceImpl;
    
    @Mock
    private TemplateStructureService templateStructureService;

    @Mock
    private Provider<StructureContext> structureContextProvider;

    @Mock
    private StructureContext structureContext;

    @Mock
    private PackageService packageService;

    @Mock
    private UpdateInternalReferencesProducer updateInternalReferencesProducer;

    @Mock
    private ProposalService proposalService;

    @Mock
    private CloneContext cloneContext;

    private List<TocItem> tocItems;
    private List<NumberingConfig> numberingConfigs;
    private String docTemplate;
    private Map<TocItem, List<TocItem>> tocRules;

    @Before
    public void setup(){
        contextUser = ModelHelper.buildUser(45L, "login", "name");
        securityContext = mock(SecurityContext.class);
        when(securityContext.getUser()).thenReturn(contextUser);
        uuidHelper = mock(UuidHelper.class);
        when(uuidHelper.getRandomUUID()).thenReturn(PRESENTER_ID);
        
        super.setup();
    
        docTemplate = "BL-023";
        byte[] bytesFile = getFileContent("/structure-test.xml");
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
    
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocRules()).thenReturn(tocRules);
    }

    @Before
    public void init() {
        when(httpSession.getId()).thenReturn(SESSION_ID);
    }

    @Test
    public void testEnterDocumentView() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        String docName = "document name";
        String documentVersion="1.0.0";
        byte[] byteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "BL-023", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = new Bill(docId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                            documentVersion, "", documentVersion, "", VersionType.MINOR, true,
                            docName, collaborators, Arrays.asList(""),
                            Option.some(content), Option.some(billMetadata));
    
        String displayableContent = "document displayable content";
        List<TableOfContentItemVO> tableOfContentItemVoList = Collections.emptyList();
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);

        DocumentVO billVO = new DocumentVO(docId,"EN", LeosCategory.BILL, "login",  Date.from(Instant.now()));
        billVO.addCollaborators(collaborators);
        List<LeosPermission> permissions = Collections.emptyList();
        List<CoEditionVO> coEditionVos = Collections.emptyList();

        LeosPackage leosPackage = new LeosPackage(docId, "Proposal", "");
        String proposalId = "878";
        ProposalMetadata proposalMetadata = new ProposalMetadata("", "REGULATION for EC", "", "PR-00.xml", "EN", "", "proposal-id", "", "0.1.0");
        Proposal leosProposal = new Proposal(proposalId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                "", "", "", "", VersionType.MAJOR, true,
                "REGULATION for EC", collaborators, Arrays.asList(""), "login", Instant.now(),
                Option.some(content), Option.some(proposalMetadata), false, "", "", "");
        
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);

        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(userHelper.getUser("login")).thenReturn(user);
        when(securityContext.getPermissions(document)).thenReturn(permissions);
        when(documentContentService.toEditableContent(isA(XmlDocument.class), any(), any())).thenReturn(displayableContent);
        when(billService.getTableOfContent(document, TocMode.SIMPLIFIED)).thenReturn(tableOfContentItemVoList);
        
        when(packageService.findPackageByDocumentId(document.getId())).thenReturn(leosPackage);
        when(proposalService.findProposalByPackagePath(leosPackage.getPath())).thenReturn(leosProposal);
        when(userHelper.getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators())).thenReturn("SG");
        when(cloneContext.isClonedProposal()).thenReturn(false);

        // DO THE ACTUAL CALL
        documentPresenter.enter();

        verify(billService, Mockito.times(2)).findBillByRef(docRef);
        verify(documentContentService).useCloneProposalMetadataVO(any());
        verify(documentContentService).toEditableContent(any(XmlDocument.class), any(), any());
        verify(billService).getTableOfContent(document, TocMode.SIMPLIFIED);
        verify(billService).getAllVersions(any(), any());

        verify(documentScreen).refreshContent(displayableContent);
        verify(documentScreen).setDocumentTitle(docName);
        verify(documentScreen).setDocumentVersionInfo(any());
        verify(documentScreen).setToc(argThat(sameInstance(tableOfContentItemVoList)));
        verify(documentScreen).setPermissions(argThat(org.hamcrest.Matchers.hasProperty("id")), eq(false));
        verify(documentScreen).initAnnotations(argThat(org.hamcrest.Matchers.hasProperty("id")), any(), any());
        verify(documentScreen).updateUserCoEditionInfo(coEditionVos, PRESENTER_ID);
        verify(documentScreen).setDataFunctions(any(), any(), any(), any(), any(), any(), any());
        
        verify(packageService).findPackageByDocumentId(document.getId());
        verify(proposalService).findProposalByPackagePath(leosPackage.getPath());
        verify(userHelper).getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators());
        
        verifyNoMoreInteractions(billService, documentContentService, documentScreen, packageService, proposalService);
    }

    @Test
    public void testEnterDocumentView_clonedProposal() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_cloned.xml";
        String docId = "555";
        String docName = "document name";
        String documentVersion="1.0.0";
        byte[] byteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "BL-023", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = new Bill(docId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                documentVersion, "", documentVersion, "", VersionType.MINOR, true,
                docName, collaborators, Arrays.asList(""),
                Option.some(content), Option.some(billMetadata));

        String displayableContent = "document displayable content";
        List<TableOfContentItemVO> tableOfContentItemVoList = Collections.emptyList();
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);

        DocumentVO billVO = new DocumentVO(docId,"EN", LeosCategory.BILL, "login",  Date.from(Instant.now()));
        billVO.addCollaborators(collaborators);
        List<LeosPermission> permissions = Collections.emptyList();
        List<CoEditionVO> coEditionVos = Collections.emptyList();

        LeosPackage leosPackage = new LeosPackage(docId, "Proposal", "");
        String proposalId = "878";
        ProposalMetadata proposalMetadata = new ProposalMetadata("", "REGULATION for EC", "", "PR-00.xml", "EN", "", "proposal-id", "", "0.1.0");
        Proposal leosProposal = new Proposal(proposalId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                "", "", "", "", VersionType.MAJOR, true,
                "REGULATION for EC", collaborators, Arrays.asList(""), "login", Instant.now(),
                Option.some(content), Option.some(proposalMetadata), true, "", "", "");

        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);

        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(userHelper.getUser("login")).thenReturn(user);
        when(securityContext.getPermissions(document)).thenReturn(permissions);
        when(documentContentService.toEditableContent(isA(XmlDocument.class), any(), any())).thenReturn(displayableContent);
        when(billService.getTableOfContent(document, TocMode.SIMPLIFIED)).thenReturn(tableOfContentItemVoList);

        when(packageService.findPackageByDocumentId(document.getId())).thenReturn(leosPackage);
        when(proposalService.findProposalByPackagePath(leosPackage.getPath())).thenReturn(leosProposal);
        when(userHelper.getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators())).thenReturn("SG");
        when(cloneContext.isClonedProposal()).thenReturn(true);

        // DO THE ACTUAL CALL
        documentPresenter.enter();

        verify(billService, Mockito.times(2)).findBillByRef(docRef);
        verify(documentContentService).useCloneProposalMetadataVO(any());
        verify(documentContentService).toEditableContent(any(XmlDocument.class), any(), any());
        verify(billService).getTableOfContent(document, TocMode.SIMPLIFIED);
        verify(billService).getAllVersions(any(), any());

        verify(documentScreen).refreshContent(displayableContent);
        verify(documentScreen).setDocumentTitle(docName);
        verify(documentScreen).setDocumentVersionInfo(any());
        verify(documentScreen).setToc(argThat(sameInstance(tableOfContentItemVoList)));
        verify(documentScreen).setPermissions(argThat(org.hamcrest.Matchers.hasProperty("id")), eq(true));
        verify(documentScreen).initAnnotations(argThat(org.hamcrest.Matchers.hasProperty("id")), any(), any());
        verify(documentScreen).updateUserCoEditionInfo(coEditionVos, PRESENTER_ID);
        verify(documentScreen).populateCloneProposalMetadataVO(any());
        verify(documentScreen).setDataFunctions(any(), any(), any(), any(), any(), any(), any());

        verify(packageService).findPackageByDocumentId(document.getId());
        verify(proposalService).findProposalByPackagePath(leosPackage.getPath());
        verify(proposalService).getClonedProposalMetadata(byteContent);
        verify(cloneContext).setCloneProposalMetadataVO(any());
        verify(userHelper).getCollaboratorConnectedEntityByLoggedUser(leosProposal.getCollaborators());

        verifyNoMoreInteractions(billService, documentContentService, documentScreen, packageService, proposalService);
    }


    private Bill getMockedBill(Content content, String docId, String docTitle, String documentVersion, BillMetadata billMetadata, List<Collaborator> collaborators) {
        return new Bill(docId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                documentVersion, "", documentVersion, "", VersionType.MINOR, true,
                docTitle, collaborators, Arrays.asList(""),
                Option.some(content), Option.some(billMetadata));
    }
    
    @Test
    public void test_LoadCrossReferenceToc() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        String windowName = "";
        List<String> selectedNodeId = new ArrayList<>();
        selectedNodeId.add("xyz");

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "BL-023", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        Map<String, List<TableOfContentItemVO>> tableOfContentItemVoMap = Collections.emptyMap();
        List<String> ancestorsIds = Collections.emptyList();
        LeosPackage leosPackage = new LeosPackage(docId, "Proposal", "");
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(packageService.findPackageByDocumentId(document.getId())).thenReturn(leosPackage);
        when(packageService.getTableOfContent(docId, TocMode.SIMPLIFIED_CLEAN)).thenReturn(tableOfContentItemVoMap);
        when(billService.getAncestorsIdsForElementId(document, selectedNodeId)).thenReturn(ancestorsIds);

        // DO THE ACTUAL CALL
        documentPresenter.fetchTocAndAncestors(new FetchCrossRefTocRequestEvent(selectedNodeId));

        verify(billService).findBillByRef(docRef);
        verify(packageService).getTableOfContent(docId, TocMode.SIMPLIFIED_CLEAN);
        verify(billService).getAncestorsIdsForElementId(document, selectedNodeId);
        verify(documentScreen).setTocAndAncestors(argThat(sameInstance(tableOfContentItemVoMap)), argThat(sameInstance(ancestorsIds)));
        verifyNoMoreInteractions(billService, documentScreen);
    }

    @Test
    @Ignore
    public void testLeaveDocumentView() {

        String docRef = "bill_test.xml";

        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);

        // DO THE ACTUAL CALL
        //        documentPresenter.detachView();   // FIXME replace detach view with something else

        verify(httpSession).removeAttribute(anyString() + "." + SessionAttribute.BILL_REF.name());
    }

    @Test
    @Ignore
    public void testEnterDocumentView_should_showWarningMessage_when_noIdOnSession() {

        // DO THE ACTUAL CALL
        documentPresenter.enter();

        verify(eventBus).post(argThat(Matchers.<NavigationRequestEvent>hasProperty("target", equalTo(Target.HOME))));
        verify(eventBus).post(argThat(Matchers.<NotificationEvent>hasProperty("messageKey", equalTo("document.id.missing"))));
        verify(eventBus).post(argThat(Matchers.<NotificationEvent>hasProperty("type", equalTo(NotificationEvent.Type.WARNING))));
        verify(eventBus).post(argThat(Matchers.<NotificationEvent>hasProperty("args", equalTo(new Object[0]))));
    }

    @Test
    public void testCloseDocument() {

        Content content = mock(Content.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        String documentVersion="1.0.0";
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        final Bill document = getMockedBill(content, docId, "title", documentVersion, billMetadata, collaborators);

        when(httpSession.getAttribute(eq("bill#" + docRef))).thenReturn(null);
        when(httpSession.getAttribute(eq(documentPresenter.getId() + "." + SessionAttribute.BILL_REF.name()))).thenReturn(docRef);

        // DO THE ACTUAL CALL
        documentPresenter.closeDocument(new CloseDocumentEvent());
        verify(eventBus).post(argThat(Matchers.<NavigationRequestEvent>hasProperty("target", equalTo(Target.PREVIOUS))));
    }

    @Test
    public void testCloseDocumentUnsavedBillInSession() {

        Content content = mock(Content.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        String documentVersion="1.0.0";
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        final Bill document = getMockedBill(content, docId, "title", documentVersion, billMetadata, collaborators);

        when(httpSession.getAttribute(eq("bill#" + docRef))).thenReturn(document);
        when(httpSession.getAttribute(eq(documentPresenter.getId() + "." + SessionAttribute.BILL_REF.name()))).thenReturn(docRef);

        // DO THE ACTUAL CALL
        documentPresenter.closeDocument(new CloseDocumentEvent());
        verify(eventBus).post(any(ShowConfirmDialogEvent.class));
    }


    @Test
    public void testRefreshDocument() {
        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        String docName = "document name";
        String documentVersion="1.0.0";
        byte[] byteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(byteContent);
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "BL-023", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        Instant now = Instant.now();
        final Bill document = getMockedBill(content, docId, docName, documentVersion, billMetadata, collaborators);
        
        String displayableContent = "document displayable content";
        List<TableOfContentItemVO> tableOfContentItemVoList = Collections.emptyList();
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);
        DocumentVO billVO = new DocumentVO(docId,"EN", LeosCategory.BILL, "login",  Date.from(now));
        billVO.addCollaborators(collaborators);
        List<LeosPermission> permissions = Collections.emptyList();
        List<CoEditionVO> coEditionVos = Collections.emptyList();

        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(userHelper.getUser("login")).thenReturn(user);
        when(securityContext.getPermissions(document)).thenReturn(permissions);
        when(documentContentService.toEditableContent(isA(XmlDocument.class), any(), any())).thenReturn(displayableContent);
        when(billService.getTableOfContent(document, TocMode.SIMPLIFIED)).thenReturn(tableOfContentItemVoList);
        when(cloneContext.isClonedProposal()).thenReturn(false);

        // DO THE ACTUAL CALL
        documentPresenter.refreshDocument(new RefreshDocumentEvent());

        verify(billService).findBillByRef(docRef);
        verify(billService).getTableOfContent(document, TocMode.SIMPLIFIED);
        verify(documentContentService).toEditableContent(any(XmlDocument.class), any(), any());
        verify(documentContentService).useCloneProposalMetadataVO(any());
        
        verify(documentScreen).refreshContent(displayableContent);
        verify(documentScreen).setDocumentTitle(docName);
        verify(documentScreen).setDocumentVersionInfo(any());
        verify(documentScreen).setToc(argThat(sameInstance(tableOfContentItemVoList)));
        verify(documentScreen).setPermissions(argThat(org.hamcrest.Matchers.hasProperty("id")), eq(false));
        verify(documentScreen).initAnnotations(argThat(org.hamcrest.Matchers.hasProperty("id")), any(), any());
        verify(documentScreen).updateUserCoEditionInfo(coEditionVos, PRESENTER_ID);
        
        verifyNoMoreInteractions(billService, documentContentService, documentScreen);
    }

    @Test
    public void testEditArticle_should_showArticleEditor() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        String versionSeriesId = "1234";
        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = getMockedBill(content, docId, "title", versionSeriesId, billMetadata, collaborators);
    
        String articleId = "7474";
        String articleContent = "article content";
        CoEditionActionInfo actionInfo = new CoEditionActionInfo(true, Operation.STORE, null, new ArrayList<CoEditionVO>());

        when(httpSession.getAttribute(eq("bill#" + docRef))).thenReturn(null);
        when(httpSession.getAttribute(eq(documentPresenter.getId() + "." + SessionAttribute.BILL_REF.name()))).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(elementProcessor.getElement(document, ARTICLE_TAG, articleId)).thenReturn(articleContent);
        when(elementProcessor.getElementAttributeValueByNameAndId(document, LEOS_ALTERNATIVE_ATTR, ARTICLE_TAG, articleId)).thenReturn("false");
        when(coEditionHelper.getCurrentEditInfo(versionSeriesId)).thenReturn(actionInfo.getCoEditionVos());
        // DO THE ACTUAL CALL
        documentPresenter.editElement(new EditElementRequestEvent(articleId, ARTICLE_TAG));

        verify(billService).findBillByRef(docRef);
        verify(documentScreen).showElementEditor(articleId, ARTICLE_TAG, articleContent, "");
        verify(elementProcessor).getElementAttributeValueByNameAndId(document, LEOS_ALTERNATIVE_ATTR, ARTICLE_TAG, articleId);
        verifyNoMoreInteractions(billService, documentScreen);
    }

    @Test
    public void test_saveArticle_should_returnUpdatedDocument() throws Exception {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill originalDocument = getMockedBill(content, docId, "title", "",  billMetadata, collaborators);
    
        byte[] updatedDocumentContent = new byte[]{1, 2, 3};
        final Bill savedDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        final String articleId = "486";
        final String newArticleText = "new article text";
        final String checkinComment = "{\"title\":\"Article updated\",\"description\":\"Minor version\",\"checkinElement\":{\"actionType\":\"UPDATED\",\"elementId\":\"486\",\"elementTagName\":\"article\",\"elementLabel\":\"article 1 updated\",\"childElements\":[]}}";

        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(originalDocument);
        when(billProcessor.updateElement(originalDocument, ARTICLE_TAG, articleId, newArticleText)).thenReturn(updatedDocumentContent);
        when(billService.updateBill(originalDocument, updatedDocumentContent, checkinComment)).thenReturn(savedDocument);
        when(messageHelper.getMessage("operation.element.updated", StringUtils.capitalize(ARTICLE_TAG))).thenReturn("Article updated");
        when(messageHelper.getMessage("operation.checkin.minor")).thenReturn("Minor version");
        when(referenceLabelService.generateLabelStringRef(Arrays.asList(articleId), savedDocument.getMetadata().get().getRef(), updatedDocumentContent))
                .thenReturn(new Result<String>("article 1 updated", null));

        // DO THE ACTUAL CALL
        documentPresenter.saveElement(new SaveElementRequestEvent(articleId, ARTICLE_TAG, newArticleText, false));

        verify(billProcessor).updateElement(originalDocument, ARTICLE_TAG, articleId, newArticleText);
        verify(billService).updateBill(originalDocument, updatedDocumentContent, checkinComment);
        verify(billService).findBillByRef(docRef);
        verifyNoMoreInteractions(billService);
    }

    @Test
    public void test_deleteArticle_should_returnUpdatedDocument() throws Exception{
        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill originalDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        byte[] updatedDocumentContent = new byte[]{1, 2, 3};
        final Bill savedDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        final String articleTag = ARTICLE;
        final String articleId = "486";
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(originalDocument);
        when(billProcessor.deleteElement(originalDocument, articleId, articleTag, user)).thenReturn(updatedDocumentContent);
        when(billService.updateBill(originalDocument, updatedDocumentContent, messageHelper.getMessage("document." + ARTICLE_TAG + ".deleted"))).thenReturn(savedDocument);
        when(referenceLabelService.generateLabelStringRef(Arrays.asList(articleId), savedDocument.getMetadata().get().getRef(), updatedDocumentContent))
                    .thenReturn(new Result<>("deleted", null));

        // DO THE ACTUAL CALL
        documentPresenter.deleteElement(new DeleteElementRequestEvent(articleId, articleTag));

        verify(billProcessor).deleteElement(originalDocument, articleId, articleTag, user);
        verify(billService).findBillByRef(docRef);
        verify(billService).updateBill(originalDocument, updatedDocumentContent, messageHelper.getMessage("document." + ARTICLE_TAG + ".deleted"));
        verifyNoMoreInteractions(billService, elementProcessor);
    }

    @Test
    public void test_deleteArticle_should_NotDeleteArticle() throws Exception {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        List<Entity> entities = new ArrayList<Entity>();
        entities.add(new Entity("1", "DIGIT.B2", "DIGIT"));
        User user = ModelHelper.buildUser(45L, "login", "name", entities);

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        Bill originalDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        byte[] updatedDocumentContent = new byte[]{1, 2, 3};
    
        Bill savedDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        String articleTag = ARTICLE;
        String articleId = "486";
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(originalDocument);
        when(billProcessor.deleteElement(originalDocument, articleId, articleTag, user)).thenReturn(updatedDocumentContent);
        when(billService.updateBill(originalDocument, updatedDocumentContent, messageHelper.getMessage("document." + ARTICLE_TAG + ".deleted"))).thenReturn(savedDocument);
        when(referenceLabelService.generateLabelStringRef(Arrays.asList(articleId), savedDocument.getMetadata().get().getRef(), updatedDocumentContent))
                        .thenReturn(new Result<>("citations updated", null));
        // DO THE ACTUAL CALL
        documentPresenter.deleteElement(new DeleteElementRequestEvent(articleId, articleTag));

        verify(billProcessor).deleteElement(originalDocument, articleId, articleTag, user);
        verify(billService).findBillByRef(docRef);
        verify(billService).updateBill(originalDocument, updatedDocumentContent, messageHelper.getMessage("document." + ARTICLE_TAG + ".deleted"));
        verifyNoMoreInteractions(billService, elementProcessor);
    }

    @Test
    public void test_insertArticle_Before() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "BL-023", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill originalDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        final byte[] updatedDocumentContent = new byte[]{1, 2, 3};
        final Bill savedDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        final boolean before = true;
        final String articleId = "486";
        final String checkinComment = "{\"title\":\"Article inserted\",\"description\":\"Minor version\",\"checkinElement\":{\"actionType\":\"INSERTED\",\"elementId\":\"486\",\"elementTagName\":\"article\",\"elementLabel\":\"\",\"childElements\":[]}}";
        
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(originalDocument);
        when(billProcessor.insertNewElement(originalDocument, articleId, before, ARTICLE_TAG)).thenReturn(updatedDocumentContent);
        when(billService.updateBill(originalDocument, updatedDocumentContent, checkinComment)).thenReturn(savedDocument);
        when(messageHelper.getMessage("operation.element.inserted", StringUtils.capitalize(ARTICLE_TAG))).thenReturn("Article inserted");
        when(messageHelper.getMessage("operation.checkin.minor")).thenReturn("Minor version");
        when(referenceLabelService.generateLabel(Arrays.asList(new Ref("", articleId, "", null)), updatedDocumentContent))
                        .thenReturn(new Result<>("article inserted", null));

        // DO THE ACTUAL CALL
        documentPresenter.insertElement(new InsertElementRequestEvent(articleId, ARTICLE_TAG, InsertElementRequestEvent.POSITION.BEFORE));

        verify(billProcessor).insertNewElement(originalDocument, articleId, before, ARTICLE_TAG);
        verify(billService).findBillByRef(docRef);
        verify(billService).updateBill(originalDocument, updatedDocumentContent, checkinComment);

        verifyNoMoreInteractions(billService);
    }

    @Test
    public void testEditInlineToc() {
        Content content = mock(Content.class);
        Source source = mock(Source.class);

        byte[] originalByteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "BL-023", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill originalDocument = getMockedBill(content, "test", "title", "", billMetadata, collaborators);
        final byte[] updatedDocumentContent = new byte[]{1, 2, 3};
        CoEditionActionInfo actionInfo = new CoEditionActionInfo(true, Operation.STORE, null, new ArrayList<CoEditionVO>());

        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn("bill_test.xml");
        when(billService.findBillByRef("bill_test.xml")).thenReturn(originalDocument);
        List<TableOfContentItemVO> tocList = new ArrayList<TableOfContentItemVO>();
        when(billService.getTableOfContent(originalDocument, TocMode.NOT_SIMPLIFIED)).thenReturn(tocList);

       // Map<TocItem, List<TocItem>> tocRules = Collections.emptyMap();
        when(structureServiceImpl.getTocRules(docTemplate)).thenReturn(any());
        when(coEditionHelper.getCurrentEditInfo("test")).thenReturn(actionInfo.getCoEditionVos());

        // DO THE ACTUAL CALL
        documentPresenter.editInlineToc(new InlineTocEditRequestEvent());

        verify(billService).findBillByRef("bill_test.xml");
        verify(billService).getTableOfContent(originalDocument, TocMode.NOT_SIMPLIFIED);
    }

    @Test
    public void testEditCitations_should_showCitationsEditor() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        byte[] originalByteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        Bill document = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        String citationsId = "7474";
        String citationsContent = "<citations xml:id=\"7474\"><citation>testCit</citation></citations>";
        CoEditionActionInfo actionInfo = new CoEditionActionInfo(true, Operation.STORE, null, new ArrayList<CoEditionVO>());

        when(httpSession.getAttribute(eq("bill#" + docRef))).thenReturn(null);
        when(httpSession.getAttribute(eq(documentPresenter.getId() + "." + SessionAttribute.BILL_REF.name()))).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(elementProcessor.getElement(document, CITATIONS_TAG, citationsId)).thenReturn(citationsContent);
        when(elementProcessor.getElementAttributeValueByNameAndId(document, LEOS_ALTERNATIVE_ATTR, CITATIONS_TAG, citationsId)).thenReturn("false");
        when(coEditionHelper.getCurrentEditInfo("test")).thenReturn(actionInfo.getCoEditionVos());

        // DO THE ACTUAL CALL
        documentPresenter.editElement((new EditElementRequestEvent(citationsId, CITATIONS_TAG)));

        verify(billService).findBillByRef(docRef);
        verify(elementProcessor).getElement(document, CITATIONS_TAG, citationsId);
        verify(elementProcessor).getElementAttributeValueByNameAndId(document, LEOS_ALTERNATIVE_ATTR, CITATIONS_TAG, citationsId);
        verify(documentScreen).showElementEditor(citationsId, CITATIONS_TAG, citationsContent, "");

        verifyNoMoreInteractions(billService, documentScreen);
    }

    @Test
    public void testCloseCitationsEditor() {
        String docRef = "bill_test.xml";
        String docId = "555";
        String citationsId = "7474";
        Content content = mock(Content.class);
        String documentVersion="1.0.0";
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        final BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        final Bill document = getMockedBill(content, docId, "", documentVersion, billMetadata, collaborators);
        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);

        // DO THE ACTUAL CALL
        documentPresenter.closeElementEditor(new CloseElementEditorEvent(citationsId, CITATION));
        //verify(eventBus).post(argThat(Matchers.<RefreshDocumentEvent>hasProperty("messageKey", equalTo("document.content.updated"))));
    }

    @Test
    public void test_saveCitations_should_saveUpdatedDocument() throws Exception {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        Bill originalDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        final byte[] updatedDocumentContent = new byte[]{1, 2, 3};
        final Bill savedDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        final String citationsId = "7474";
        final String newCitationsContent = "<citations xml:id=\"7474\"><citation>testCit</citation><citation>newtestCit</citation></citations>";
        final String checkinComment = "{\"title\":\"Citations updated\",\"description\":\"Minor version\",\"checkinElement\":{\"actionType\":\"UPDATED\",\"elementId\":\"7474\",\"elementTagName\":\"citations\",\"elementLabel\":\"citations updated\",\"childElements\":[]}}";

        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(originalDocument);
        when(billProcessor.updateElement(originalDocument, CITATIONS_TAG, citationsId, newCitationsContent)).thenReturn(updatedDocumentContent);
        when(billService.updateBill(originalDocument, updatedDocumentContent, checkinComment)).thenReturn(savedDocument);
        when(elementProcessor.getElement(savedDocument, CITATIONS_TAG, citationsId)).thenReturn(newCitationsContent);
        when(messageHelper.getMessage("operation.element.updated", StringUtils.capitalize(CITATIONS_TAG))).thenReturn("Citations updated");
        when(messageHelper.getMessage("operation.checkin.minor")).thenReturn("Minor version");
        when(referenceLabelService.generateLabelStringRef(Arrays.asList(citationsId), savedDocument.getMetadata().get().getRef(), updatedDocumentContent))
                        .thenReturn(new Result<>("citations updated", null));

        // DO THE ACTUAL CALL
        documentPresenter.saveElement(new SaveElementRequestEvent(citationsId, CITATIONS_TAG, newCitationsContent, false));

        verify(billProcessor).updateElement(originalDocument, CITATIONS_TAG, citationsId, newCitationsContent);
        verify(billService).findBillByRef(docRef);
        verify(billService).updateBill(originalDocument, updatedDocumentContent, checkinComment);
        verify(eventBus).post(argThat(Matchers.<NotificationEvent>hasProperty("messageKey", equalTo("document.content.updated"))));
        verify(documentScreen).refreshElementEditor(citationsId, CITATIONS_TAG, newCitationsContent);

        verifyNoMoreInteractions(billService);
    }

    @Ignore // FIXME: this test is not working as this behaviour is removed from presenters
    @Test
    public void testSaveCitations_when_NoDocumentId_should_displayNotification() throws Exception {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        byte[] originalByteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        final byte[] updatedDocumentContent = new byte[]{1, 2};

        final String citationsId = "7474";
        final String citationsContent = "<citations xml:id=\"7474\"><citation>testCit</citation></citations>";
        final String newCitationsContent = "<citations xml:id=\"7474\"><citation>testCit</citation><citation>newtestCit</citation></citations>";
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(null);
        when(billService.findBillByRef(docRef)).thenReturn(null);
        when(billProcessor.updateElement(document, CITATIONS_TAG, citationsId, newCitationsContent)).thenReturn(updatedDocumentContent);

        // DO THE ACTUAL CALL
        documentPresenter.saveElement(new SaveElementRequestEvent(citationsId, CITATIONS_TAG, newCitationsContent, false));

        verify(billProcessor).updateElement(document, CITATIONS_TAG, citationsId, newCitationsContent);
        verifyNoMoreInteractions(billService, billProcessor);
    }

    @Test
    public void testEditRecitals_should_showRecitalsEditor() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        byte[] originalByteContent = new byte[]{1, 2, 3};

        when(source.getBytes()).thenReturn(originalByteContent);
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
    
        final String recitalsId = "7474";
        final String recitalsContent = "<recitals xml:id=\"7474\"><recital>testCit</recital></recitals>";
        final CoEditionActionInfo actionInfo = new CoEditionActionInfo(true, Operation.STORE, null, new ArrayList<CoEditionVO>());

        when(httpSession.getAttribute(eq("bill#" + docRef))).thenReturn(null);
        when(httpSession.getAttribute(eq(documentPresenter.getId() + "." + SessionAttribute.BILL_REF.name()))).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(elementProcessor.getElement(document, RECITALS_TAG, recitalsId)).thenReturn(recitalsContent);
        when(elementProcessor.getElementAttributeValueByNameAndId(document, LEOS_ALTERNATIVE_ATTR, RECITALS_TAG, recitalsId)).thenReturn("false");
        when(coEditionHelper.getCurrentEditInfo("test")).thenReturn(actionInfo.getCoEditionVos());

        // DO THE ACTUAL CALL
        documentPresenter.editElement((new EditElementRequestEvent(recitalsId, RECITALS_TAG)));

        verify(billService).findBillByRef(docRef);
        verify(elementProcessor).getElement(document, RECITALS_TAG, recitalsId);
        verify(elementProcessor).getElementAttributeValueByNameAndId(document, LEOS_ALTERNATIVE_ATTR, RECITALS_TAG, recitalsId);
        verify(documentScreen).showElementEditor(recitalsId, RECITALS_TAG, recitalsContent, "");

        verifyNoMoreInteractions(billService, documentScreen);
    }

    @Test
    public void testCloseRecitalsEditor() {
        String docRef = "bill_test.xml";
        String docId = "555";
        Content content = mock(Content.class);
        String documentVersion="1.0.0";
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));

        final BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        final Bill document = getMockedBill(content, docId, "title", documentVersion, billMetadata, collaborators);
    
        when(billService.findBill(docId)).thenReturn(document);
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);

        // DO THE ACTUAL CALL
        documentPresenter.closeElementEditor(new CloseElementEditorEvent("7474", "recital"));

        //verify(eventBus).post(argThat(Matchers.<RefreshDocumentEvent>hasProperty("messageKey", equalTo("document.content.updated"))));
    }
    
    @Test
    public void test_saveRecitals_should_saveUpdatedDocument() throws Exception {
        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = getMockedBill(content, docId, "title", "", billMetadata, collaborators);

        final byte[] updatedDocumentContent = new byte[]{1, 2, 3};
        final Bill savedDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        final String recitalsId = "7474";
        final String newRecitalsContent = "<recitals xml:id=\"7474\"><recital>testCit</recital><recital>newtestCit</recital></recitals>";
        final String checkinComment = "{\"title\":\"Recitals updated\",\"description\":\"Minor version\",\"checkinElement\":{\"actionType\":\"UPDATED\",\"elementId\":\"7474\",\"elementTagName\":\"recitals\",\"elementLabel\":\"recitals updated\",\"childElements\":[]}}";

        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(document);
        when(billProcessor.updateElement(document, RECITALS_TAG, recitalsId, newRecitalsContent)).thenReturn(updatedDocumentContent);
        when(billService.updateBill(document, updatedDocumentContent, checkinComment)).thenReturn(savedDocument);
        when(elementProcessor.getElement(savedDocument, RECITALS_TAG, recitalsId)).thenReturn(newRecitalsContent);
        when(messageHelper.getMessage("operation.element.updated", StringUtils.capitalize(RECITALS_TAG))).thenReturn("Recitals updated");
        when(messageHelper.getMessage("operation.checkin.minor")).thenReturn("Minor version");
        when(referenceLabelService.generateLabelStringRef(Arrays.asList(recitalsId), savedDocument.getMetadata().get().getRef(), updatedDocumentContent))
                                .thenReturn(new Result<>("recitals updated", null));
        // DO THE ACTUAL CALL
        documentPresenter.saveElement(new SaveElementRequestEvent(recitalsId, RECITALS_TAG, newRecitalsContent, false));

        verify(billProcessor).updateElement(document, RECITALS_TAG, recitalsId, newRecitalsContent);
        verify(billService).findBillByRef(docRef);
        verify(billService).updateBill(document, updatedDocumentContent, checkinComment);
        verify(eventBus).post(argThat(Matchers.<NotificationEvent>hasProperty("messageKey", equalTo("document.content.updated"))));
        verify(documentScreen).refreshElementEditor(recitalsId, RECITALS_TAG, newRecitalsContent);

        verifyNoMoreInteractions(billService);
    }

    @Ignore // FIXME: this test is not working as this behaviour is removed from presenters
    @Test
    public void testSaveRecitals_when_NoDocumentId_should_displayNotification() throws Exception {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill document = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        final byte[] updatedDocumentContent = new byte[]{1, 2, 3};
        final String recitalsId = "7474";
        final String recitalsContent = "<recitals xml:id=\"7474\"><recital>testCit</recital></recitals>";
        final String newRecitalsContent = "<recitals xml:id=\"7474\"><recital>testCit</recital><recital>newtestCit</recital></recitals>";
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(null);
        when(billProcessor.updateElement(document, CITATIONS_TAG, recitalsId, newRecitalsContent)).thenReturn(updatedDocumentContent);

        // DO THE ACTUAL CALL
        documentPresenter.saveElement(new SaveElementRequestEvent(recitalsId, RECITALS_TAG, newRecitalsContent, false));

        verify(billProcessor).updateElement(null, RECITALS_TAG, recitalsId, newRecitalsContent);
        verifyNoMoreInteractions(billService, billProcessor);
    }

    @Test
    public void test_importElement() {

        Content content = mock(Content.class);
        Source source = mock(Source.class);

        String docRef = "bill_test.xml";
        String docId = "555";
        String aknDocument = "<!--This AkomaNtoso document was created via a LegisWrite export.-->"
                + "<akomaNtoso xml:id=\"akn\">" +
                "<article xml:id=\"art486\">" +
                "<num xml:id=\"aknum\">Article 486</num>" +
                "<heading xml:id=\"aknhead\">1st article</heading>" +
                "</article>" +
                "<article xml:id=\"art486\">" +
                "<num xml:id=\"aknnum2\"></num>" +
                "<heading xml:id=\"aknhead2\">2th articl<authorialNote marker=\"101\" xml:id=\"a1\"><p xml:id=\"p1\">TestNote1</p></authorialNote>e</heading>" +
                "</article>" +
                "<article xml:id=\"art486\">" +
                "<heading xml:id=\"aknhead3\">3th article<authorialNote marker=\"90\" xml:id=\"a2\"><p xml:id=\"p2\">TestNote2</p></authorialNote></heading>" +
                "</article>" +
                "<article xml:id=\"art486\">" +
                "</article>" +
                "</akomaNtoso>";

        String articleId = "001";
        String articleTag = ARTICLE;
        String article = "<article>Text...</article>";

        SearchCriteriaVO searchCriteria = new SearchCriteriaVO(DocType.REGULATION, "2015", "25");
        List<String> elementIdList = new ArrayList<String>();
        elementIdList.add(articleId);

        when(source.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(content.getSource()).thenReturn(source);

        BillMetadata billMetadata =new BillMetadata("", "REGULATION", "", "BL-000.xml", "EN", "", "bill-id", "", "0.0.1");
        List<Collaborator> collaborators = new ArrayList<>();
        collaborators.add(new Collaborator("login", "OWNER", "SG"));
        final Bill originalDocument = getMockedBill(content, docId,  "title", "", billMetadata, collaborators);
        final byte[] updatedDocumentContent = new byte[]{4, 5, 6};
        final Bill savedDocument = getMockedBill(content, docId, "title", "", billMetadata, collaborators);
        
        when(httpSession.getAttribute(anyString() + "." + SessionAttribute.BILL_REF.name())).thenReturn(docRef);
        when(billService.findBillByRef(docRef)).thenReturn(originalDocument);
        when(importService.getAknDocument("reg", 2015, 25)).thenReturn(aknDocument);
        when(importService.insertSelectedElements(originalDocument, aknDocument.getBytes(), elementIdList, "EN")).thenReturn(updatedDocumentContent);
        when(billService.updateBill(originalDocument, updatedDocumentContent, messageHelper.getMessage("operation.import.element.inserted"))).thenReturn(savedDocument);

        // DO THE ACTUAL CALL
        documentPresenter.importElements(new ImportElementRequestEvent(searchCriteria, elementIdList));

        verify(importService).getAknDocument("reg", 2015, 25);
        verify(importService).insertSelectedElements(originalDocument, aknDocument.getBytes(), elementIdList, "EN");
        verify(billService).findBillByRef(docRef);
        verify(billService).updateBill(originalDocument, updatedDocumentContent, messageHelper.getMessage("operation.import.element.inserted"));

        verifyNoMoreInteractions(importService);
        verifyNoMoreInteractions(billService);
    }
    
    public byte[] getFileContent(String fileName) {
        try {
            InputStream inputStream = this.getClass().getResource(fileName).openStream();
            byte[] content = new byte[inputStream.available()];
            inputStream.read(content);
            inputStream.close();
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bytes from file: " + fileName);
        }
    }
}
