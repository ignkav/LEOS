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

public abstract class XPathCatalog {

    public abstract String getXPathRefOrigin();
    public abstract String getXPathRefOriginForCloneRefAttr();
    public abstract String getXPathRefOriginForCloneOriginalMilestone();
    public abstract String getXPathRefOriginForCloneIscRef();
    public abstract String getXPathRefOriginForCloneObjectId();
    public abstract String getXPathAttachments();
    public abstract String getXPathDocumentRef();
    public abstract String getXPathDocumentRefByHrefAttr(String elementRef);
    public abstract String getXPathObjectId();
    public abstract String getXPathRef();
    public abstract String getXPathLastElement(String tagName);
    public abstract String getXPathHeading();
    public abstract String getXPathDocTemplate();
    public abstract String getXPathRelevantElements();
    public abstract String getXPathComments();
    public abstract String getXPathClonedProposals();
    public abstract String getXPathClonedProposal();
    public abstract String getXPathCPMilestoneRef();
    public abstract String getXPathCPMilestoneRefByNameAttr(String legFileName);
    public abstract String getXPathCPMilestoneRefClonedProposalRef(String legFileName);
    public abstract String getXPathCPMilestoneRefClonedProposalRefByRefAttr(String legFileName, String clonedProposalId);
    public abstract String getXPathDocType();
}
