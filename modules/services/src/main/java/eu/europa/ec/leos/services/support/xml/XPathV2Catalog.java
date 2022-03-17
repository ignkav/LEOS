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
package eu.europa.ec.leos.services.support.xml;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import org.springframework.stereotype.Component;

@Component
@Instance(instances = {InstanceType.COUNCIL})
public class XPathV2Catalog extends XPathCatalog {

    @Override
    public String getXPathRefOrigin() {
        return "/akomaNtoso//meta/proprietary/leos:refOrigin";
    }

    @Override
    public String getXPathAttachments() {
        return "//attachments";
    }

    @Override
    public String getXPathDocumentRef() {
        return "//attachments/attachment/documentRef";
    }

    @Override
    public String getXPathDocumentRefByHrefAttr(String elementRef) {
        return String.format("//attachments/attachment/documentRef[@href=\"%s\"]", elementRef);
    }

    @Override
    public String getXPathObjectId() {
        return "/akomaNtoso//meta/proprietary/leos:objectId";
    }

    @Override
    public String getXPathRef() {
        return "/akomaNtoso//meta/proprietary/leos:ref";
    }

    @Override
    public String getXPathRefOriginForCloneRefAttr() {
        return "/akomaNtoso//meta/proprietary/leos:refOriginForClone/@ref";
    }

    @Override
    public String getXPathRefOriginForCloneOriginalMilestone() {
        return "/akomaNtoso//meta/proprietary/leos:refOriginForClone/originMilestone";
    }

    @Override
    public String getXPathRefOriginForCloneIscRef() {
        return "/akomaNtoso//meta/proprietary/leos:refOriginForClone/iscRef";
    }

    @Override
    public String getXPathRefOriginForCloneObjectId() {
        return "/akomaNtoso//meta/proprietary/leos:refOriginForClone/objectId";
    }

    @Override
    public String getXPathLastElement(String tagName) {
        return "//" + tagName + "[last()]";
    }

    @Override
    public String getXPathHeading() {
        return "//heading[1]";
    }

    @Override
    public String getXPathDocTemplate() {
        return "/akomaNtoso//meta/proprietary/leos:docTemplate";
    }

    @Override
    public String getXPathRelevantElements() {
        return "/akomaNtoso//meta/proprietary/leos:relevantElements";
    }

    @Override
    public String getXPathComments() {
        return "/akomaNtoso//meta/proprietary/leos:comments";
    }

    @Override
    public String getXPathClonedProposals() {
        return "/akomaNtoso//meta/proprietary/leos:clonedProposals";
    }

    @Override
    public String getXPathClonedProposal() {
        return "/akomaNtoso//meta/proprietary/leos:clonedProposal";
    }

    @Override
    public String getXPathCPMilestoneRef() {
        return "/akomaNtoso//meta/proprietary/leos:clonedProposals/leos:milestoneRef";
    }

    @Override
    public String getXPathCPMilestoneRefByNameAttr(String legFileName) {
        return "/akomaNtoso//meta/proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]";
    }

    @Override
    public String getXPathCPMilestoneRefClonedProposalRef(String legFileName) {
        return "/akomaNtoso//meta/proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]/clonedProposalRef";
    }

    @Override
    public String getXPathCPMilestoneRefClonedProposalRefByRefAttr(String legFileName, String clonedProposalId) {
        return "/akomaNtoso//meta/proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]/clonedProposalRef[@ref=\""+clonedProposalId+"\"]";
    }

    @Override
    public String getXPathDocType() {
        return "/akomaNtoso//meta/references/TLCReference[@name=\"docType\"]";
    }
}
