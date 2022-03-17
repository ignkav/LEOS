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
package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.common.ErrorCode;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import eu.europa.ec.leos.services.support.xml.XPathCatalog;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_ORIGIN_ATTR;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class PostProcessingProposalServiceImpl extends PostProcessingDocumentService {
    private static final Logger LOG = LoggerFactory.getLogger(PostProcessingProposalServiceImpl.class);

    private ProposalService proposalService;
    private BillService billService;
    private UserService userService;
    private SecurityContext securityContext;
    private MessageHelper messageHelper;

    @Value("${leos.cmis.repository.sysadmin}")
    private String repositorySysadmin;

    @Autowired
    PostProcessingProposalServiceImpl(XmlContentProcessor xmlContentProcessor, ProposalService proposalService,
                                      BillService billService, UserService userService, XPathCatalog xPathCatalog,
                                      SecurityContext securityContext, MessageHelper messageHelper) {
        super(xmlContentProcessor, xPathCatalog);
        this.proposalService = proposalService;
        this.billService = billService;
        this.userService = userService;
        this.securityContext = securityContext;
        this.messageHelper = messageHelper;
    }

    @Override
    public Result<String> processDocument(DocumentVO documentVO) {
        if (documentVO.getCategory().equals(LeosCategory.PROPOSAL)) {
            byte[] updatedDocContent = preserveDocumentReference(documentVO.getSource());
            documentVO.setSource(updatedDocContent);
            for (DocumentVO doc : documentVO.getChildDocuments()) {
                try {
                    if (!doc.getCategory().equals(LeosCategory.PROPOSAL)) {
                        byte[] docContent = doc.getSource();
                        if (doc.getCategory().equals(LeosCategory.BILL)) {
                            updatedDocContent = preserveDocumentReference(docContent);
                            doc.setSource(updatedDocContent);

                            for (DocumentVO annex : doc.getChildDocuments()) {
                                byte[] annexContent = annex.getSource();
                                byte[] updatedDocContentAnnex = preserveDocumentReference(annexContent);
                                annex.setSource(updatedDocContentAnnex);
                            }
                        } else {
                            updatedDocContent = preserveDocumentReference(docContent);
                            doc.setSource(updatedDocContent);
                        }
                    }
                } catch (Exception e) {
                    return new Result<>(e.getMessage(), ErrorCode.EXCEPTION);
                }
            }
        }
        return new Result<>("OK", null);
    }

    @Override
    public Result<?> saveOriginalProposalIdToClonedProposal(DocumentVO documentVO, String legFileName, String iscRef) {
        if (documentVO.getCategory().equals(LeosCategory.PROPOSAL)) {
            byte[] updatedDocContent = preserveOriginalDocumentProperties(documentVO.getSource(), legFileName, iscRef);

            documentVO.setSource(updatedDocContent);
            for (DocumentVO doc : documentVO.getChildDocuments()) {
                try {
                    if (!doc.getCategory().equals(LeosCategory.PROPOSAL)) {
                        byte[] docContent = doc.getSource();
                        if (doc.getCategory().equals(LeosCategory.BILL)) {
                            updatedDocContent = preserveOriginalDocumentProperties(docContent, legFileName, iscRef);
                            updatedDocContent = xmlContentProcessor.setAttributeForAllChildren(updatedDocContent, BILL, Collections.emptyList(), LEOS_ORIGIN_ATTR, EC);
                            doc.setSource(updatedDocContent);

                            for (DocumentVO annex : doc.getChildDocuments()) {
                                byte[] updatedDocContentAnnex = preserveOriginalDocumentProperties(annex.getSource(), legFileName, iscRef);
                                updatedDocContentAnnex = xmlContentProcessor.setAttributeForAllChildren(updatedDocContentAnnex, DOC, Collections.emptyList(), LEOS_ORIGIN_ATTR, EC);
                                annex.setSource(updatedDocContentAnnex);
                            }
                        } else {
                            updatedDocContent = preserveOriginalDocumentProperties(docContent, legFileName, iscRef);
                            updatedDocContent = xmlContentProcessor.setAttributeForAllChildren(updatedDocContent, DOC, Collections.emptyList(), LEOS_ORIGIN_ATTR, EC);
                            doc.setSource(updatedDocContent);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("Error occurred while saving metadata to cloned proposal", e);
                    return new Result<>(e.getMessage(), ErrorCode.EXCEPTION);
                }
            }
        }
        return new Result<>("OK", null);
    }

    @Override
    public Result<?> saveClonedProposalIdToOriginalProposal(DocumentVO documentVO, CollectionIdsAndUrlsHolder
            idsAndUrlsHolder, CloneProposalMetadataVO cloneProposalMetadataVO) {
        if (documentVO.getCategory().equals(LeosCategory.PROPOSAL)) {
            User loggedUser = securityContext.getUser();
            Collection<? extends GrantedAuthority> loggedInUserAuthorities = SecurityContextHolder.getContext().
                    getAuthentication().getAuthorities();
            try {
                //Switch user to sysadmin
                userService.switchUser(repositorySysadmin);
                Proposal originalProposal = proposalService.findProposal(documentVO.getId());
                byte[] xmlContent = originalProposal.getContent().getOrThrow(() ->
                        new IllegalArgumentException("Proposal not found")).getSource().getBytes();
                byte[] updatedProposalContent = preserveClonedDocumentProperties(xmlContent,
                        idsAndUrlsHolder.getProposalId(), cloneProposalMetadataVO);
                documentVO.setSource(updatedProposalContent);
                //update original proposal with cloned metadata properties
                proposalService.updateProposal(originalProposal.getId(), updatedProposalContent);
            } catch (Exception e) {
                LOG.error("Error occurred while saving cloned metadata to original proposal", e);
                return new Result<>(e.getMessage(), ErrorCode.EXCEPTION);
            } finally {
                //Switch back to logged-in user
                userService.switchUserWithAuthorities(loggedUser.getLogin(), loggedInUserAuthorities);
            }
        }
        return new Result<>("OK", null);
    }

    @Override
    public void updatePostCloneMetadataProperties(String id, CloneProposalMetadataVO cloneProposalMetadataVO) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CmisProperties.CLONED_FROM.getId(), cloneProposalMetadataVO.getClonedFromRef());
        proposalService.updateProposal(id, properties);
    }
}
