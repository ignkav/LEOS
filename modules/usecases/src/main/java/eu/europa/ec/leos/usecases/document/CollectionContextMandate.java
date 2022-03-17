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
package eu.europa.ec.leos.usecases.document;

import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.TemplateService;
import io.atlassian.fugue.Option;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

import java.util.List;

import static eu.europa.ec.leos.domain.cmis.LeosCategory.COUNCIL_EXPLANATORY;

@Component
@Scope("prototype")
@Instance(InstanceType.COUNCIL)
public class CollectionContextMandate extends CollectionContext {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionContextMandate.class);

    private final ExplanatoryService explanatoryService;

    private final Provider<ExplanatoryContext> explanatoryContextProvider;

    private final MessageHelper messageHelper;

    private String explId;

    CollectionContextMandate(TemplateService templateService,
                             PackageService packageService,
                             ProposalService proposalService, Provider<ExplanatoryContext> explanatoryContextProvider,
                             Provider<MemorandumContext> memorandumContextProvider,
                             Provider<BillContext> billContextProvider, ExplanatoryService explanatoryService, MessageHelper messageHelper) {
        super(templateService, packageService, proposalService, memorandumContextProvider, billContextProvider);
        this.explanatoryContextProvider = explanatoryContextProvider;
        this.explanatoryService = explanatoryService;
        this.messageHelper = messageHelper;
    }

    protected void createDefaultExplanatories(LeosPackage leosPackage, ProposalMetadata metadata) {
        ExplanatoryContext explanatoryContext = explanatoryContextProvider.get();
        explanatoryContext.usePackage(leosPackage);

        explanatoryContext.useTemplate("CE-001");
        explanatoryContext.usePurpose(purpose);
        explanatoryContext.useType(metadata.getType());
        explanatoryContext.useTitle(messageHelper.getMessage("document.default.explanatory.title.default.1"));
        explanatoryContext.useActionMessageMap(actionMsgMap);
        explanatoryContext.useCollaborators(proposal.getCollaborators());
        Explanatory explanatory = explanatoryContext.executeCreateExplanatory();
        proposal = proposalService.addComponentRef(proposal, explanatory.getName(), COUNCIL_EXPLANATORY);

        explanatoryContext.useTemplate("CE-002");
        explanatoryContext.usePurpose(purpose);
        explanatoryContext.useType(metadata.getType());
        explanatoryContext.useTitle(messageHelper.getMessage("document.default.explanatory.title.default.2"));
        explanatoryContext.useActionMessageMap(actionMsgMap);
        explanatoryContext.useCollaborators(proposal.getCollaborators());
        explanatory = explanatoryContext.executeCreateExplanatory();
        proposal = proposalService.addComponentRef(proposal, explanatory.getName(), COUNCIL_EXPLANATORY);
    }

    protected void createExplanatoryMilestones(LeosPackage leosPackage) {
        final List<Explanatory> explanatories = packageService.findDocumentsByPackagePath(leosPackage.getPath(), Explanatory.class, false);
        explanatories.forEach(explanatory -> {
            ExplanatoryContext explanatoryContext = explanatoryContextProvider.get();
            explanatoryContext.useExplanatoryId(explanatory.getId());
            explanatoryContext.useVersionComment(versionComment);
            explanatoryContext.useMilestoneComment(milestoneComment);
            explanatoryContext.executeCreateMilestone();
        });
    }

    public void executeCreateExplanatory(){
        LeosPackage leosPackage = packageService.findPackageByDocumentId(proposal.getId());
        ExplanatoryContext explanatoryContext = explanatoryContextProvider.get();
        explanatoryContext.usePackage(leosPackage);
        explanatoryContext.useTemplate(categoryTemplateMap.get(COUNCIL_EXPLANATORY).getName());
        explanatoryContext.usePurpose(purpose);
        Option<ProposalMetadata> metadataOption = proposal.getMetadata();
        Validate.isTrue(metadataOption.isDefined(), "Proposal metadata is required!");
        ProposalMetadata metadata = metadataOption.get();
        explanatoryContext.useType(metadata.getType());
        explanatoryContext.useActionMessageMap(actionMsgMap);
        explanatoryContext.useCollaborators(proposal.getCollaborators());
        Explanatory explanatory = explanatoryContext.executeCreateExplanatory();
        proposalService.addComponentRef(proposal, explanatory.getName(), COUNCIL_EXPLANATORY);
        proposalService.createVersion(proposal.getId(), VersionType.INTERMEDIATE, actionMsgMap.get(ContextAction.DOCUMENT_CREATED));
    }

    public void useExplanatory(String explId) {
        Validate.notNull(explId, "Proposal 'explId' is required!");
        LOG.trace("Using Proposal explanatory id [explId={}]", explId);
        this.explId = explId;
    }

    public void executeRemoveExplanatory() {
        LOG.trace("Executing 'Remove council explanatory' use case...");
        Validate.notNull(leosPackage, "Leos package is required!");
        Explanatory explanatory = explanatoryService.findExplanatory(explId);
        explanatoryService.deleteExplanatory(explanatory);
        Proposal proposal = proposalService.findProposalByPackagePath(leosPackage.getPath());
        proposal = proposalService.removeComponentRef(proposal, explanatory.getName(), COUNCIL_EXPLANATORY);
        proposalService.updateProposal(proposal.getId(), proposal.getContent().get().getSource().getBytes());
    }
}
