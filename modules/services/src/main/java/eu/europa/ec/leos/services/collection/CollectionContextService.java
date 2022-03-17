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
package eu.europa.ec.leos.services.collection;

import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.MetadataVO;
import eu.europa.ec.leos.model.user.Entity;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.collection.document.BillContextService;
import eu.europa.ec.leos.services.collection.document.ContextActionService;
import eu.europa.ec.leos.services.collection.document.MemorandumContextService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.TemplateService;
import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import eu.europa.ec.leos.services.support.url.CollectionUrlBuilder;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.domain.cmis.LeosCategory.BILL;
import static eu.europa.ec.leos.domain.cmis.LeosCategory.MEMORANDUM;
import static eu.europa.ec.leos.domain.cmis.LeosCategory.PROPOSAL;

@Component
@Scope("prototype")
public class CollectionContextService {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionContextService.class);

    private final TemplateService templateService;
    private final PackageService packageService;
    private final ProposalService proposalService;
    private final CollectionUrlBuilder urlBuilder;

    private final Provider<MemorandumContextService> memorandumContextProvider;
    private final Provider<BillContextService> billContextProvider;
    private SecurityContext securityContext;

    private final Map<LeosCategory, XmlDocument> categoryTemplateMap;

    private final Map<ContextActionService, String> actionMsgMap;

    private Proposal proposal = null;
    private String purpose;
    private String versionComment;
    private String milestoneComment;

    private DocumentVO propDocument;
    private String propChildDocument;
    private String proposalComment;
    private CollectionIdsAndUrlsHolder idsAndUrlsHolder;
    private String iscRef;
    private boolean cloneProposal = false;
    private String connectedEntity;
    private CloneProposalMetadataVO cloneProposalMetadataVO;

    CollectionContextService(TemplateService templateService,
                             PackageService packageService,
                             ProposalService proposalService,
                             CollectionUrlBuilder urlBuilder, Provider<MemorandumContextService> memorandumContextProvider,
                             Provider<BillContextService> billContextProvider,
                             SecurityContext securityContext) {
        this.templateService = templateService;
        this.packageService = packageService;
        this.proposalService = proposalService;
        this.urlBuilder = urlBuilder;
        this.memorandumContextProvider = memorandumContextProvider;
        this.billContextProvider = billContextProvider;
        this.securityContext = securityContext;
        this.categoryTemplateMap = new HashMap<>();
        this.actionMsgMap = new HashMap<>();
    }

    public void useTemplate(String name) {
        Validate.notNull(name, "Template name is required!");
        XmlDocument template = templateService.getTemplate(name);
        Validate.notNull(template, "Template not found! [name=%s]", name);

        LOG.trace("Using {} template... [id={}, name={}]", template.getCategory(), template.getId(), template.getName());
        categoryTemplateMap.put(template.getCategory(), template);
    }

    public void useActionMessage(ContextActionService action, String actionMsg) {
        Validate.notNull(actionMsg, "Action message is required!");
        Validate.notNull(action, "Context Action not found! [name=%s]", action);

        LOG.trace("Using action message... [action={}, name={}]", action, actionMsg);
        actionMsgMap.put(action, actionMsg);
    }

    public void useProposal(Proposal proposal) {
        Validate.notNull(proposal, "Proposal is required!");
        LOG.trace("Using Proposal... [proposal={}]", proposal);
        this.proposal = proposal;
    }

    public void useProposalId(String id) {
        Validate.notNull(id, "Proposal identifier is required!");
        LOG.trace("Using Proposal... [id={}]", id);
        proposal = proposalService.findProposal(id);
        Validate.notNull(proposal, "Proposal not found! [id=%s]", id);
    }

    public void usePurpose(String purpose) {
        Validate.notNull(purpose, "Proposal purpose is required!");
        LOG.trace("Using Proposal purpose... [purpose={}]", purpose);
        this.purpose = purpose;
    }

    public void useDocument(DocumentVO document) {
        Validate.notNull(document, "Proposal document is required!");
        propDocument = document;
    }

    public void useVersionComment(String comment) {
        Validate.notNull(comment, "Version comment is required!");
        this.versionComment = comment;
    }

    public void useMilestoneComment(String milestoneComment) {
        Validate.notNull(milestoneComment, "milestoneComment is required!");
        this.milestoneComment = milestoneComment;
    }

    public void useChildDocument(String documentId) {
        Validate.notNull(documentId, "Proposal child document is required!");
        propChildDocument = documentId;
    }

    public void useActionComment(String comment){
        Validate.notNull(comment, "Proposal comment is required!");
        proposalComment = comment;
    }

    public void useIdsAndUrlsHolder(CollectionIdsAndUrlsHolder idsAndUrlsHolder) {
        Validate.notNull(idsAndUrlsHolder, "idsAndUrlsHolder is required!");
        this.idsAndUrlsHolder = idsAndUrlsHolder;
    }
    
    public void useIscRef(String iscRef) {
        Validate.notNull(iscRef, "ISC reference is required!");
        this.iscRef = iscRef;
    }
    
    public void useCloneProposal(boolean cloneProposal) {
        this.cloneProposal = cloneProposal;
    }

    public void useConnectedEntity(String connectedEntity) {
        Validate.notNull(connectedEntity, "Connected entity is required!");
        this.connectedEntity = connectedEntity;
    }

    public void useClonedProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO) {
        Validate.notNull(cloneProposalMetadataVO, "cloned metadata vo is required!");
        this.cloneProposalMetadataVO = cloneProposalMetadataVO;
    }
    
    public Proposal executeImportProposal()  {

        LOG.trace("Executing 'Import Proposal' use case...");
        MetadataVO propMeta = propDocument.getMetadata();
        Validate.notNull(propMeta, "Proposal metadata is required!");
        Validate.notNull(propDocument.getChildDocuments(), "Proposal must contain child documents to import!");
        // create package
        LeosPackage leosPackage = packageService.createPackage();
        // use template
        Proposal proposalTemplate = cast(categoryTemplateMap.get(PROPOSAL));
        Validate.notNull(proposalTemplate, "Proposal template is required!");

        // get metadata from template
        Option<ProposalMetadata> metadataOption = proposalTemplate.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");
        purpose = propMeta.getDocPurpose();
        Validate.notNull(purpose, "Proposal purpose is required!");
        ProposalMetadata metadata = metadataOption.get().withPurpose(purpose);
        if(cloneProposal) {
            setConnectedEntity();
            proposal = proposalService.createClonedProposalFromContent(leosPackage.getPath(), metadata, cloneProposalMetadataVO,
                    propDocument.getSource());
        } else {
            Validate.notNull(propDocument.getSource(), "Proposal xml is required!");
            proposal = proposalService.createProposalFromContent(leosPackage.getPath(), metadata, propDocument.getSource());
        }

        // create child element
        for (DocumentVO docChild : propDocument.getChildDocuments()) {
            if ((docChild.getCategory() == MEMORANDUM) && (cast(categoryTemplateMap.get(MEMORANDUM)) != null)) {
                MemorandumContextService memorandumContext = memorandumContextProvider.get();
                memorandumContext.usePackage(leosPackage);
                // use template
                memorandumContext.useTemplate(cast(categoryTemplateMap.get(MEMORANDUM)));
                // We want to use the same purpose that was set in the wizard for all the documents.
                memorandumContext.usePurpose(purpose);
                memorandumContext.useDocument(docChild);
                memorandumContext.useActionMessageMap(actionMsgMap);
                memorandumContext.useType(metadata.getType());
                memorandumContext.usePackageTemplate(metadata.getTemplate());
                Memorandum memorandum = memorandumContext.executeImportMemorandum();
                proposal = proposalService.addComponentRef(proposal, memorandum.getName(), LeosCategory.MEMORANDUM);
                String memorandumRef = memorandum.getMetadata().get().getRef();
                idsAndUrlsHolder.setMemorandumId(memorandumRef);
                idsAndUrlsHolder.setMemorandumUrl(urlBuilder.buildMemorandumViewUrl(memorandumRef));
            } else if (docChild.getCategory() == BILL) {
                BillContextService billContext = billContextProvider.get();
                billContext.usePackage(leosPackage);
                // use template
                billContext.useTemplate(cast(categoryTemplateMap.get(BILL)));
                billContext.usePurpose(purpose);
                billContext.useDocument(docChild);
                billContext.useActionMessageMap(actionMsgMap);
                billContext.useIdsAndUrlsHolder(idsAndUrlsHolder);
                Bill bill = billContext.executeImportBill();
                proposal = proposalService.addComponentRef(proposal, bill.getName(), LeosCategory.BILL);
                String billRef = bill.getMetadata().get().getRef();
                idsAndUrlsHolder.setBillId(billRef);
                idsAndUrlsHolder.setBillUrl(urlBuilder.buildBillViewUrl(billRef));
            }
        }
        return proposalService.createVersion(proposal.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextActionService.DOCUMENT_CREATED));
    }

    private void setConnectedEntity() {
        Entity entity = new Entity(Cuid.createCuid(), connectedEntity, connectedEntity);
        User user = securityContext.getUser();
        user.setConnectedEntity(entity);
    }

    public void executeCreateProposal() {
        LOG.trace("Executing 'Create Proposal' use case...");

        LeosPackage leosPackage = packageService.createPackage();

        Proposal proposalTemplate = cast(categoryTemplateMap.get(PROPOSAL));
        Validate.notNull(proposalTemplate, "Proposal template is required!");

        Option<ProposalMetadata> metadataOption = proposalTemplate.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");

        Validate.notNull(purpose, "Proposal purpose is required!");
        ProposalMetadata metadata = metadataOption.get().withPurpose(purpose);

        Proposal proposal = proposalService.createProposal(proposalTemplate.getId(), leosPackage.getPath(), metadata, null);

        // TODO: To have other structure proposal
        if (cast(categoryTemplateMap.get(MEMORANDUM)) != null) {
            MemorandumContextService memorandumContext = memorandumContextProvider.get();
            memorandumContext.usePackage(leosPackage);
            memorandumContext.useTemplate(cast(categoryTemplateMap.get(MEMORANDUM)));
            memorandumContext.usePurpose(purpose);
            memorandumContext.useActionMessageMap(actionMsgMap);
            memorandumContext.useType(metadata.getType());
            memorandumContext.usePackageTemplate(metadata.getTemplate());
            Memorandum memorandum = memorandumContext.executeCreateMemorandum();
            proposal = proposalService.addComponentRef(proposal, memorandum.getName(), LeosCategory.MEMORANDUM);
        }

        BillContextService billContext = billContextProvider.get();
        billContext.usePackage(leosPackage);
        billContext.useTemplate(cast(categoryTemplateMap.get(BILL)));
        billContext.usePurpose(purpose);
        billContext.useActionMessageMap(actionMsgMap);
        Bill bill = billContext.executeCreateBill();
        proposalService.addComponentRef(proposal, bill.getName(), LeosCategory.BILL);
        proposalService.createVersion(proposal.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextActionService.DOCUMENT_CREATED));
    }

    public void executeUpdateProposal() {
        LOG.trace("Executing 'Update Proposal' use case...");

        Validate.notNull(proposal, "Proposal is required!");
        Validate.notNull(proposalComment, "Proposal comment is required!");

        Option<ProposalMetadata> metadataOption = proposal.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");

        Validate.notNull(purpose, "Proposal purpose is required!");
        ProposalMetadata metadata = metadataOption.get().withPurpose(purpose);

        proposal = proposalService.updateProposal(proposal, metadata, VersionType.MINOR, proposalComment);

        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());

        MemorandumContextService memorandumContext = memorandumContextProvider.get();
        memorandumContext.usePackage(leosPackage);
        memorandumContext.usePurpose(purpose);
        memorandumContext.useActionMessageMap(actionMsgMap);
        memorandumContext.executeUpdateMemorandum();

        BillContextService billContext = billContextProvider.get();
        billContext.usePackage(leosPackage);
        billContext.usePurpose(purpose);
        billContext.useActionMessageMap(actionMsgMap);
        billContext.executeUpdateBill();
    }

    public void executeDeleteProposal() {
        LOG.trace("Executing 'Delete Proposal' use case...");
        if(proposal != null && proposal.getId() != null) {
            LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());
            packageService.deletePackage(leosPackage);
        }
    }

    public void executeUpdateProposalAsync() {
        Validate.notNull(propChildDocument, "Proposal child document is required!");
        proposalService.updateProposalAsync(propChildDocument, proposalComment);
    }

    /**
     * Given a proposalId, fetch all documents related from CMIS and update them to the next major version.
     * The document version is not updated if it is already a major version.
     */
    public void executeCreateMilestone() {
        LOG.info("Creating major versions for all documents of [proposal={}", proposal.getId());

        // 1. Proposal
        List<String> milestoneComments = proposal.getMilestoneComments();
        milestoneComments.add(milestoneComment);
        if (proposal.getVersionType().equals(VersionType.MAJOR)) {
            proposal = proposalService.updateProposalWithMilestoneComments(proposal.getId(), milestoneComments);
            LOG.info("Major version {} already present. Updated only milestoneComment for [proposal={}]", proposal.getVersionLabel(), proposal.getId());
        } else {
            proposal = proposalService.updateProposalWithMilestoneComments(proposal, milestoneComments, VersionType.MAJOR, versionComment);
            LOG.info("Created major version {} for [proposal={}]", proposal.getVersionLabel(), proposal.getId());
        }

        // Update the last structure
        final LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());

        // 2. Memorandum
        final MemorandumContextService memorandumContext = memorandumContextProvider.get();
        memorandumContext.usePackage(leosPackage);
        memorandumContext.useVersionComment(versionComment);
        memorandumContext.useMilestoneComment(milestoneComment);
        memorandumContext.executeCreateMilestone();

        // 2. Bill + Annexes
        final BillContextService billContext = billContextProvider.get();
        billContext.usePackage(leosPackage);
        billContext.useVersionComment(versionComment);
        billContext.useMilestoneComment(milestoneComment);
        billContext.executeCreateMilestone();
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object obj) {
        return (T) obj;
    }

}
