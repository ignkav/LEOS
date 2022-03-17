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

import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.notification.cloneProposal.ClonedProposalNotification;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.converter.ProposalConverterService;
import eu.europa.ec.leos.services.document.PostProcessingDocumentService;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import eu.europa.ec.leos.services.support.url.CollectionUrlBuilder;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.File;
import java.util.Date;
import java.util.List;

import static eu.europa.ec.leos.domain.cmis.LeosCategory.PROPOSAL;

@Service
public class CollectionServiceImpl implements CollectionService {
    private static final Logger LOG = LoggerFactory.getLogger(CollectionServiceImpl.class);

    private final Provider<CollectionContextService> proposalContextProvider;
    private ProposalConverterService proposalConverterService;
    private PostProcessingDocumentService postProcessingDocumentService;
    private final NotificationService notificationService;
    private SecurityContext securityContext;

    private CollectionUrlBuilder urlBuilder;
    private MessageHelper messageHelper;
    private XmlContentProcessor xmlContentProcessor;

    @Value("${notification.functional.mailbox}")
    private String notificationRecepient;

    @Autowired
    public CollectionServiceImpl(
            Provider<CollectionContextService> proposalContextProvider,
            PostProcessingDocumentService postProcessingDocumentService,
            ProposalConverterService proposalConverterService,
            NotificationService notificationService,
            SecurityContext securityContext, CollectionUrlBuilder urlBuilder,
            MessageHelper messageHelper, XmlContentProcessor xmlContentProcessor) {
        this.proposalContextProvider = proposalContextProvider;
        this.proposalConverterService = proposalConverterService;
        this.postProcessingDocumentService = postProcessingDocumentService;
        this.notificationService = notificationService;
        this.securityContext = securityContext;
        this.urlBuilder = urlBuilder;
        this.messageHelper = messageHelper;
        this.xmlContentProcessor = xmlContentProcessor;
    }

    private DocumentVO createDocumentVOFromLegfile(File legDocument) {
        Validate.notNull(legDocument, "Leg document is required");

        DocumentVO propDocument = proposalConverterService.createProposalFromLegFile(legDocument, new DocumentVO(PROPOSAL), true);

        CollectionContextService context = proposalContextProvider.get();
        context.useTemplate(propDocument.getMetadata().getDocTemplate());
        return propDocument;
    }

    private void addTemplateInContext(CollectionContextService context, DocumentVO documentVO) {
        context.useTemplate(documentVO.getMetadata().getDocTemplate());
        if (documentVO.getChildDocuments() != null) {
            for (DocumentVO docChild : documentVO.getChildDocuments()) {
                addTemplateInContext(context, docChild);
            }
        }
    }
    
    @Override
    public CreateCollectionResult createCollection(File legDocument) throws CreateCollectionException {

        String proposalUrl;
        String proposalId;

        CollectionIdsAndUrlsHolder idsAndUrlsHolder = new CollectionIdsAndUrlsHolder();
        DocumentVO propDocument = createDocumentVOFromLegfile(legDocument);
        
        CollectionContextService context = proposalContextProvider.get();
        context.useDocument(propDocument);
        context.useIdsAndUrlsHolder(idsAndUrlsHolder);
        addTemplateInContext(context, propDocument);
        
        Proposal proposal = context.executeImportProposal();
        
        proposalId = proposal.getMetadata().get().getRef();
        proposalUrl = urlBuilder.buildProposalViewUrl(proposalId);
        idsAndUrlsHolder.setProposalId(proposalId);
        idsAndUrlsHolder.setProposalUrl(proposalUrl);

        return new CreateCollectionResult(idsAndUrlsHolder, true, null);
    }
    
    @Override
    public CreateCollectionResult cloneCollection(File legDocument, String iscRef, String targetUser, String connectedEntity) throws CreateCollectionException {
        String proposalUrl;
        String proposalId;
        String cmisObjectId;

        CollectionIdsAndUrlsHolder idsAndUrlsHolder = new CollectionIdsAndUrlsHolder();
        DocumentVO propDocument = createDocumentVOFromLegfile(legDocument);

        //set metadata to cloned proposal
        CloneProposalMetadataVO cloneProposalMetadataVO = new CloneProposalMetadataVO();
        cloneProposalMetadataVO.setClonedProposal(Boolean.TRUE);
        cloneProposalMetadataVO.setOriginRef(iscRef);
        cloneProposalMetadataVO.setClonedFromRef(propDocument.getRef());
        cloneProposalMetadataVO.setClonedFromObjectId(propDocument.getId());
        cloneProposalMetadataVO.setLegFileName(legDocument.getName());
        cloneProposalMetadataVO.setTargetUser(targetUser);
        cloneProposalMetadataVO.setRevisionStatus(messageHelper.getMessage("clone.proposal.revision.status"));

        CollectionContextService context = proposalContextProvider.get();
        context.useDocument(propDocument);
        context.useIdsAndUrlsHolder(idsAndUrlsHolder);
        context.useIscRef(iscRef);
        context.useCloneProposal(true);
        context.useConnectedEntity(connectedEntity);
        context.useClonedProposalMetadataVO(cloneProposalMetadataVO);
        addTemplateInContext(context, propDocument);

        Result<?> result = postProcessingDocumentService.saveOriginalProposalIdToClonedProposal(propDocument, legDocument.getName(), iscRef);
        if (result.isError()) {
            CreateCollectionError error = new CreateCollectionError(result.getErrorCode().get().ordinal(),
                    messageHelper.getMessage("clone.proposal.metadata.preserve.error"));
            return new CreateCollectionResult(idsAndUrlsHolder, false, error);
        }

        Proposal proposal = context.executeImportProposal();
        proposalId = proposal.getMetadata().get().getRef();
        cmisObjectId = proposal.getId();
        proposalUrl = urlBuilder.buildProposalViewUrl(proposalId);
        idsAndUrlsHolder.setProposalId(proposalId);
        idsAndUrlsHolder.setProposalUrl(proposalUrl);

        //set clone creation date to original proposal
        cloneProposalMetadataVO.setCreationDate(Date.from(proposal.getInitialCreationInstant()));

        result = postProcessingDocumentService.saveClonedProposalIdToOriginalProposal(propDocument, idsAndUrlsHolder, cloneProposalMetadataVO);
        if (result.isError()) {
            //In case of error delete the cloned proposal.
            context.useProposal(proposal);
            try {
                LOG.debug("Deleting cloned proposal as metadata update operation failed");
                context.executeDeleteProposal();
            } catch (Exception e) {
                LOG.error("Error deleting the cloned proposal", e);
            }
            CreateCollectionError error = new CreateCollectionError(result.getErrorCode().get().ordinal(),
                    messageHelper.getMessage("clone.proposal.metadata.preserve.error"));
            return new CreateCollectionResult(idsAndUrlsHolder, true, error);
        } else {
            postProcessingDocumentService.updatePostCloneMetadataProperties(cmisObjectId, cloneProposalMetadataVO);
        }
        try {
            //Send CNS notification
            notificationService.sendNotification(new ClonedProposalNotification(notificationRecepient,
                    messageHelper.getMessage("clone.proposal.notification.title.iscRef", iscRef),
                    legDocument.getName(), proposalUrl, iscRef));
        } catch (Exception e) {
            LOG.error("CNS notification exception. Service is not available at the moement.", e);
        }

        return new CreateCollectionResult(idsAndUrlsHolder, true, null);

    }

    @Override
    public List<CloneProposalMetadataVO> getClonedProposalsMetadataVO(String proposalId, String legDocumentName) {
        return xmlContentProcessor.getClonedProposalsMetadataVO(proposalId, legDocumentName);
    }

}