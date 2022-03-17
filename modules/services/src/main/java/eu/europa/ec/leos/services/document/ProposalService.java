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


import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;

import java.util.List;
import java.util.Map;

public interface ProposalService {

    Proposal createProposal(String templateId, String path, ProposalMetadata metadata, byte[] content);

    Proposal createProposalFromContent(String path, ProposalMetadata metadata, byte[] content);

    Proposal createClonedProposalFromContent(String path, ProposalMetadata metadata,
                                       CloneProposalMetadataVO cloneProposalMetadataVO, byte[] content);

    Proposal findProposal(String id);

    Proposal updateProposal(Proposal proposal, ProposalMetadata metadata, VersionType versionType, String comment);

    Proposal updateProposal(Proposal proposal, ProposalMetadata metadata);

    Proposal updateProposal(String id, Map<String, Object> properties);
    
    Proposal updateProposal(String proposalId, byte[] updatedBytes);

    Proposal addComponentRef(Proposal proposal, String href, LeosCategory leosCategory);

    Proposal addComponent(Proposal proposal, String id, LeosCategory leosCategory);

    Proposal updateProposalWithMilestoneComments(Proposal proposal, List<String> milestoneComments, VersionType versionType, String comment);

    Proposal updateProposalWithMilestoneComments(String proposalId, List<String> milestoneComments);

    Proposal removeComponentRef(Proposal proposal, String href, LeosCategory leosCategory);

    void updateProposalAsync(String id, String comment);

    Proposal findProposalByPackagePath(String path);

    Proposal createVersion(String id, VersionType versionType, String comment);

    Proposal findProposalByRef(String ref);

    CloneProposalMetadataVO getClonedProposalMetadata(byte[] xmlContent);

    void removeClonedProposalMetadata(String proposalId, String clonedProposalId, CloneProposalMetadataVO cloneProposalMetadataVO);
}
