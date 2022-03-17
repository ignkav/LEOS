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
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.document.ProposalService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

@Component
@Scope("prototype")
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class CollectionContextProposal extends CollectionContext {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionContextProposal.class);

    CollectionContextProposal(TemplateService templateService,
                              PackageService packageService,
                              ProposalService proposalService,
                              Provider<MemorandumContext> memorandumContextProvider,
                              Provider<BillContext> billContextProvider) {
        super(templateService, packageService, proposalService, memorandumContextProvider, billContextProvider);
    }

    protected void createDefaultExplanatories(LeosPackage leosPackage, ProposalMetadata metadata) {
    }

    protected void createExplanatoryMilestones(LeosPackage leosPackage) {
    }

    public void executeCreateExplanatory() {
    }

    public void useExplanatory(String explId) {
    }

    public void executeRemoveExplanatory() {
    }
}
