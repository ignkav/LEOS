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
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.ExplanatoryService;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_REMOVED_CLASS;

@Service
@Instance(InstanceType.COUNCIL)
public class MandateDocumentContentServiceImpl extends DocumentContentServiceImpl {

    @Autowired
    public MandateDocumentContentServiceImpl(TransformationService transformationService,
                                             ContentComparatorService compareService, AnnexService annexService, BillService billService,
                                             MemorandumService memorandumService, ExplanatoryService explanatoryService, XmlContentProcessor xmlContentProcessor) {
        super(transformationService, compareService, annexService, billService, memorandumService, explanatoryService, xmlContentProcessor);
    }

    @Override
    public String toEditableContent(XmlDocument xmlDocument, String contextPath, SecurityContext securityContext) {
        String[] contentsToCompare = getContentsToCompare(xmlDocument, contextPath, securityContext);
        if(contentsToCompare != null) {
            switch (contentsToCompare.length) {
                case 2:
                    String currentDocumentEditableXml = contentsToCompare[0];
                    String originalDocumentEditableXml = contentsToCompare[1];
                    return compareService.compareContents(new ContentComparatorContext.Builder(originalDocumentEditableXml, currentDocumentEditableXml)
                            .withAttrName(ATTR_NAME)
                            .withRemovedValue(CONTENT_SOFT_REMOVED_CLASS)
                            .withAddedValue(CONTENT_SOFT_ADDED_CLASS)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .build());

                case 1:
                    return contentsToCompare[0];
                default:
                    LOG.error("Invalid number of documents returned");
                    return null;
            }
        }
        return null;
    }

    @Override
    public boolean isMemorandumComparisonRequired(byte[] contentBytes) {
        return false;
    }

    @Override
    public boolean isCouncilExplanatoryComparisonRequired(byte[] contentBytes) {
        return false;
    }

}
