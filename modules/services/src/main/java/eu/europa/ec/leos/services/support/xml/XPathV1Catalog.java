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
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class XPathV1Catalog extends XPathCatalog {

    public static final String NAMESPACE_AKN_NAME = "akn";
    public static final String NAMESPACE_AKN_URI = "http://docs.oasis-open.org/legaldocml/ns/akn/3.0";

    @Override
    public String getXPathRefOrigin() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOrigin";
    }

    @Override
    public String getXPathRefOriginForCloneRefAttr() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/@ref";
    }

    @Override
    public String getXPathRefOriginForCloneOriginalMilestone() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/akn:originMilestone";
    }

    @Override
    public String getXPathRefOriginForCloneIscRef() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/akn:iscRef";
    }

    @Override
    public String getXPathRefOriginForCloneObjectId() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:refOriginForClone/akn:objectId";
    }

    @Override
    public String getXPathAttachments() {
        return "//akn:attachments";
    }

    @Override
    public String getXPathDocumentRef() {
        return "//akn:attachments/akn:attachment/akn:documentRef";
    }

    @Override
    public String getXPathDocumentRefByHrefAttr(String elementRef) {
        return String.format("//akn:attachments/akn:attachment/akn:documentRef[@href=\"%s\"]", elementRef);
    }

    @Override
    public String getXPathObjectId() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:objectId";
    }

    @Override
    public String getXPathRef() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:ref";
    }

    @Override
    public String getXPathLastElement(String tagName) {
        return "//akn:" + tagName + "[last()]";
    }

    @Override
    public String getXPathHeading() {
        return "//akn:heading[1]";
    }

    @Override
    public String getXPathDocTemplate() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docTemplate";
    }

    @Override
    public String getXPathRelevantElements() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:relevantElements";
    }

    @Override
    public String getXPathComments() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:comments";
    }

    @Override
    public String getXPathClonedProposals() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals";
    }

    @Override
    public String getXPathClonedProposal() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposal";
    }

    @Override
    public String getXPathCPMilestoneRef() {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef";
    }

    @Override
    public String getXPathCPMilestoneRefByNameAttr(String legFileName) {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]";
    }

    @Override
    public String getXPathCPMilestoneRefClonedProposalRef(String legFileName) {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]/akn:clonedProposalRef";
    }

    @Override
    public String getXPathCPMilestoneRefClonedProposalRefByRefAttr(String legFileName, String clonedProposalId) {
        return "/akn:akomaNtoso//akn:meta/akn:proprietary/leos:clonedProposals/leos:milestoneRef[@name=\"" + legFileName + "\"]/akn:clonedProposalRef[@ref=\"" + clonedProposalId + "\"]";
    }

    @Override
    public String getXPathDocType() {
        return "/akn:akomaNtoso//akn:meta/akn:references/akn:TLCReference[@name=\"docType\"]";
    }

    public static String removeNamespaceFromXml(String xml) {
        return xml.replaceAll( NAMESPACE_AKN_NAME + ":", "");
    }

}
