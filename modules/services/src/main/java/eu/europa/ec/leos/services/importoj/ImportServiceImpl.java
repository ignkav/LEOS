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
package eu.europa.ec.leos.services.importoj;

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.integration.ExternalDocumentProvider;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.support.xml.NumberProcessor;
import eu.europa.ec.leos.services.support.xml.XPathCatalog;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITAL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITALS;

@Service
public class ImportServiceImpl implements ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportServiceImpl.class);

    private ExternalDocumentProvider externalDocumentProvider;
    private ConversionHelper conversionHelper;
    private XmlContentProcessor xmlContentProcessor;
    private NumberProcessor numberProcessor;
    private XPathCatalog xPathCatalog;

    @Autowired
    public ImportServiceImpl(ExternalDocumentProvider externalDocumentProvider, ConversionHelper conversionHelper, XmlContentProcessor xmlContentProcessor,
                             NumberProcessor numberProcessor, XPathCatalog xPathCatalog) {
        this.externalDocumentProvider = externalDocumentProvider;
        this.conversionHelper = conversionHelper;
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberProcessor = numberProcessor;
        this.xPathCatalog = xPathCatalog;
    }

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public String getFormexDocument(String type, int year, int number) {
        return externalDocumentProvider.getFormexDocument(type, year, number);
    }

    @Override
    @Cacheable(value = "aknCache", cacheManager = "cacheManager")
    public String getAknDocument(String type, int year, int number) {
        return conversionHelper.convertFormexToAKN(getFormexDocument(type, year, number));
    }

    @Override
    public byte[] insertSelectedElements(Bill bill, byte[] importedContent, List<String> elementIds, String language) {
        LOG.info("Importing {} elements...", elementIds.size());
        byte[] documentContent = getContent(bill);
        long startTime = System.currentTimeMillis();
        for (String id : elementIds) {
            Element element = xmlContentProcessor.getElementById(importedContent, id);
            if (element == null) {
                throw new IllegalStateException("Strange situation! One of the IDs passed from FE is not present in the document. ID: " + id);
            }
            // Get id of the last element in the document
            String xPath = xPathCatalog.getXPathLastElement(element.getElementTagName());
            String elementId = xmlContentProcessor.getElementIdByPath(documentContent, xPath);
            String elementType = element.getElementTagName();

            // Do pre-processing on the selected elements
            String updatedElement = xmlContentProcessor.doImportedElementPreProcessing(element.getElementFragment(), elementType);
            if (elementType.equalsIgnoreCase(ARTICLE)) {
                updatedElement = this.numberProcessor.renumberImportedArticle(updatedElement, language);
            } else if (elementType.equalsIgnoreCase(RECITAL)) {
                updatedElement = this.numberProcessor.renumberImportedRecital(updatedElement);
            }
            updatedElement = updatedElement.replaceFirst(">", " leos:editable=\"true\" leos:deletable=\"true\">");

            // Insert selected element to the document
            if (elementId != null) {
                documentContent = xmlContentProcessor.insertElementByTagNameAndId(documentContent, updatedElement,
                        element.getElementTagName(), elementId, checkIfLastArticleIsEntryIntoForce(documentContent, element, elementId, language));
            } else if (elementType.equalsIgnoreCase(ARTICLE)) {
                documentContent = xmlContentProcessor.appendElementToTag(documentContent, BODY, updatedElement, true);
            } else if (elementType.equalsIgnoreCase(RECITAL)) {
                documentContent = xmlContentProcessor.appendElementToTag(documentContent, RECITALS, updatedElement, true);
            }
        }
        long endTime = System.currentTimeMillis();
        long insertTime = endTime - startTime;
        startTime = System.currentTimeMillis();

        // Renumber
        documentContent = this.numberProcessor.renumberRecitals(documentContent);
        documentContent = this.numberProcessor.renumberArticles(documentContent);
        endTime = System.currentTimeMillis();
        long numberingTime = endTime - startTime;
        startTime = System.currentTimeMillis();

        documentContent = xmlContentProcessor.doXMLPostProcessing(documentContent);
        endTime = System.currentTimeMillis();
        long postProcessingTime = endTime - startTime;

        LOG.info("Elements Imported. insertTime {} ms, numberingTime {} ms, postProcessingTime {} ms", insertTime, numberingTime, postProcessingTime);
        return documentContent;
    }

    // check if the last article in the document has heading Entry into force, if yes articles imported before EIF article
    private boolean checkIfLastArticleIsEntryIntoForce(byte[] documentContent, Element element, String elementId, String language) {
        boolean isLastElementEIF = false;
        String lastElement = xmlContentProcessor.getElementByNameAndId(documentContent, element.getElementTagName(), elementId);
        // Disable namespace parsing for VTD as xml fragment passed may not contain namespace information
        String headingElementValue = xmlContentProcessor.getElementValue(lastElement.getBytes(StandardCharsets.UTF_8), xPathCatalog.getXPathHeading(), false);
        if (checkIfHeadingIsEntryIntoForce(headingElementValue, language)) {
            isLastElementEIF = true;
        }
        return isLastElementEIF;
    }

    // Gets the heading message from locale
    private boolean checkIfHeadingIsEntryIntoForce(String headingElementValue, String language) {
        boolean isHeadingMatched = false;
        if (headingElementValue != null && !headingElementValue.isEmpty()) {
            isHeadingMatched = messageHelper.getMessage("legaltext.article.entryintoforce.heading").replaceAll("\\h+", "")
                    .equalsIgnoreCase(StringUtils.trimAllWhitespace(headingElementValue.replaceAll("\\h+", "")));
        }
        return isHeadingMatched;
    }

    private byte[] getContent(Bill bill) {
        final Content content = bill.getContent().getOrError(() -> "Document content is required!");
        return content.getSource().getBytes();
    }

}
