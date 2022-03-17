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
package eu.europa.ec.leos.services.content.processor;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public abstract class DocumentContentServiceImpl implements DocumentContentService {

    protected static final Logger LOG = LoggerFactory.getLogger(DocumentContentServiceImpl.class);

    protected TransformationService transformationService;
    protected ContentComparatorService compareService;
    protected AnnexService annexService;
    protected BillService billService;
    protected MemorandumService memorandumService;
    protected ExplanatoryService explanatoryService;
    protected CloneProposalMetadataVO cloneProposalMetadataVO;
    protected XmlContentProcessor xmlContentProcessor;

    @Autowired
    public DocumentContentServiceImpl(TransformationService transformationService,
                                      ContentComparatorService compareService, AnnexService annexService,
                                      BillService billService, MemorandumService memorandumService, ExplanatoryService explanatoryService,
                                      XmlContentProcessor xmlContentProcessor) {
        this.transformationService = transformationService;
        this.compareService = compareService;
        this.annexService = annexService;
        this.billService = billService;
        this.memorandumService = memorandumService;
        this.explanatoryService = explanatoryService;
        this.xmlContentProcessor = xmlContentProcessor;
    }

    protected String[] getContentsToCompare(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext) {
        String currentDocumentEditableXml = getEditableXml(xmlDocument, contextPath, securityContext);
        XmlDocument originalDocument;
        byte[] contentBytes;

        switch (xmlDocument.getCategory()){
            case MEMORANDUM:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if(isMemorandumComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalMemorandum(xmlDocument);
                } else {
                    return new String[] {currentDocumentEditableXml};
                }
                break;
            case COUNCIL_EXPLANATORY:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if(isCouncilExplanatoryComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalExplanatory(xmlDocument);
                } else {
                    return new String[] {currentDocumentEditableXml};
                }
                break;
            case ANNEX:
                contentBytes = xmlDocument.getContent().get().getSource().getBytes();
                if(isAnnexComparisonRequired(contentBytes)) {
                    originalDocument = getOriginalAnnex(xmlDocument);
                } else {
                    return new String[] {currentDocumentEditableXml};
                }
                break;
            case BILL:
                originalDocument = getOriginalBill(xmlDocument);
                break;
            default:
                throw new UnsupportedOperationException("No transformation supported for this category");
        }
        String originalDocumentEditableXml = getEditableXml(originalDocument, contextPath, securityContext);
        return new String[] {currentDocumentEditableXml, originalDocumentEditableXml};
    }

    protected String getEditableXml(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext) {
        return transformationService.toEditableXml(getContentInputStream(xmlDocument), contextPath, xmlDocument.getCategory(), securityContext.getPermissions(xmlDocument));
    }

    @Override
    public XmlDocument getOriginalMemorandum(XmlDocument xmlDocument) {
        return memorandumService.findFirstVersion(xmlDocument.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalExplanatory(XmlDocument xmlDocument) {
        return explanatoryService.findFirstVersion(xmlDocument.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalAnnex(XmlDocument xmlDocument) {
        return annexService.findFirstVersion(xmlDocument.getMetadata().get().getRef());
    }

    @Override
    public XmlDocument getOriginalBill(XmlDocument xmlDocument) {
        return billService.findFirstVersion(xmlDocument.getMetadata().get().getRef());
    }

    @Override
    public void useCloneProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO) {
        this.cloneProposalMetadataVO = cloneProposalMetadataVO;
    }

    @Override
    public boolean isAnnexComparisonRequired(byte[] contentBytes) {
        return xmlContentProcessor.isAnnexComparisonRequired(contentBytes);
    }
}
