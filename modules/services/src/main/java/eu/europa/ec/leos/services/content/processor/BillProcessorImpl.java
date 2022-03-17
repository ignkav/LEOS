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

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.support.xml.NumberProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlHelper;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.TocItemUtils;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLAUSE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITAL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPOINT;

@Service
public class BillProcessorImpl implements BillProcessor {

    protected XmlContentProcessor xmlContentProcessor;
    protected ElementProcessor elementProcessor;
    protected NumberProcessor numberProcessor;
    protected MessageHelper messageHelper;
    protected Provider<StructureContext> structureContextProvider;
    protected final XmlTableOfContentHelper xmlTableOfContentHelper;

    @Autowired
    BillProcessorImpl(XmlContentProcessor xmlContentProcessor, ElementProcessor elementProcessor, XmlTableOfContentHelper xmlTableOfContentHelper,
            NumberProcessor numberProcessor, MessageHelper messageHelper, Provider<StructureContext> structureContextProvider) {
        this.xmlContentProcessor = xmlContentProcessor;
        this.elementProcessor = elementProcessor;
        this.numberProcessor = numberProcessor;
        this.messageHelper = messageHelper;
        this.structureContextProvider = structureContextProvider;
        this.xmlTableOfContentHelper = xmlTableOfContentHelper;
    }

    public byte[] insertNewElement(Bill document, String elementId, boolean before, String tagName) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");

        final Element parentElement;
        final String template;
        byte[] updatedContent;
        List<TocItem> items = structureContextProvider.get().getTocItems();

        switch (tagName) {
            case CITATION:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, CITATION), messageHelper);
                updatedContent = insertNewElement(document, elementId, before, tagName, template);
                break;
            case RECITAL:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, RECITAL), "#", messageHelper);
                updatedContent = insertNewElement(document, elementId, before, tagName, template);
                updatedContent = numberProcessor.renumberRecitals(updatedContent);
                break;
            case ARTICLE:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, ARTICLE), "#", "Article heading...", messageHelper);
                updatedContent = insertNewElement(document, elementId, before, tagName, template);
                updatedContent = numberProcessor.renumberArticles(updatedContent);
                break;
            case PARAGRAPH:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, PARAGRAPH), "#", messageHelper);
                if (isNumberedParagraph(document, elementId)) { ;
                    updatedContent = insertNewElement(document, elementId, before, tagName, template);
                    updatedContent = xmlContentProcessor.insertAffectedAttributeIntoParentElements(updatedContent, elementId);
                    updatedContent = numberProcessor.renumberArticles(updatedContent);
                } else {
                    updatedContent = insertNewElement(document, elementId, before, tagName, template.replaceAll("<num.*?</num>", ""));
                }
                break;
            case SUBPARAGRAPH:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), tagName, elementId);
                if (isFirstSubParagraph(document, parentElement.getElementId(), parentElement.getElementTagName(), elementId)) {
                    updatedContent = insertNewElement(document, parentElement.getElementId(), before, parentElement.getElementTagName());
                } else if (!PARAGRAPH.equals(parentElement.getElementTagName()) || isNumberedParagraph(document, parentElement.getElementId())) {
                    template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, SUBPARAGRAPH), messageHelper);
                    updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                } else {
                    throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
                }
                break;
            case POINT:
            case INDENT:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, tagName), "#", messageHelper);
                updatedContent = insertNewElement(document, elementId, before, tagName, template);
                updatedContent = xmlContentProcessor.insertAffectedAttributeIntoParentElements(updatedContent, elementId);
                updatedContent = numberProcessor.renumberArticles(updatedContent);
                break;
            case SUBPOINT:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), tagName, elementId);
                updatedContent = insertNewElement(document, parentElement.getElementId(), before, parentElement.getElementTagName());
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
        }

        updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        return updatedContent;
    }

    public byte[] deleteElement(Bill document, String elementId, String tagName, User user) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
        byte[] updatedContent;
        switch (tagName) {
            case CLAUSE:
            case CITATION:
                updatedContent = elementProcessor.deleteElement(document, elementId, tagName);
                break;
            case RECITAL:
                updatedContent = elementProcessor.deleteElement(document, elementId, tagName);
                updatedContent = numberProcessor.renumberRecitals(updatedContent);
                break;
            case ARTICLE:
            case PARAGRAPH:
            case SUBPARAGRAPH:
                updatedContent = elementProcessor.deleteElement(document, elementId, tagName);
                updatedContent = numberProcessor.renumberArticles(updatedContent);
                break;
            case POINT:
            case SUBPOINT:
            case INDENT:
                updatedContent = elementProcessor.deleteElement(document, elementId, tagName);
                updatedContent = numberProcessor.renumberParagraph(updatedContent);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
        }

        updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        return updatedContent;
    }

    private byte[] insertNewElement(Bill document, String elementId, boolean before, String tagName, String template) {
        final byte[] contentBytes = getContent(document);
        byte[] updatedBytes = xmlContentProcessor.insertElementByTagNameAndId(contentBytes, template, tagName, elementId, before);
        return updatedBytes;
    }
    
    private boolean isNumberedParagraph(Bill document, String elementId) {
        return null != xmlContentProcessor.getChildElement(getContent(document), PARAGRAPH, elementId, Arrays.asList(NUM), 1);
    }

    private boolean isFirstSubParagraph(Bill document, String elementId, String tagName, String subParElementId) {
        Element firstSubParElement = xmlContentProcessor.getChildElement(getContent(document), tagName, elementId, Arrays.asList(SUBPARAGRAPH), 1);
        return firstSubParElement != null && subParElementId.equals(firstSubParElement.getElementId());
    }

    @Override
    public byte[] updateElement(Bill document, String elementName, String elementId, String elementContent) throws Exception{
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
        Validate.notNull(elementContent, "ElementContent is required.");

        byte[] updatedContent = null;
        if (xmlContentProcessor.needsToBeIndented(elementContent)) {
            byte[] contentBytes = getContent(document);
            List<TableOfContentItemVO> toc = xmlTableOfContentHelper.buildTableOfContent(BILL, contentBytes, TocMode.NOT_SIMPLIFIED);
            updatedContent = xmlContentProcessor.indentElement(contentBytes, elementName, elementId, elementContent, toc);
            if (updatedContent != null) {
                updatedContent = numberProcessor.renumberRecitals(updatedContent);
                updatedContent = numberProcessor.renumberArticles(updatedContent);
                updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
            }
        } else {
            updatedContent = elementProcessor.updateElement(document, elementContent, elementName, elementId);
        }
        return updatedContent;
    }

    @Override
    public Element getTocElement(final Bill document, final String elementId, final List<TableOfContentItemVO> toc) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getTocElement(contentBytes, elementId, toc, Arrays.asList(SUBPARAGRAPH));
    }

    @Override
    public byte[] mergeElement(Bill document, String elementContent, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
    
        final byte[] contentBytes = getContent(document);
        byte[] updatedContent = xmlContentProcessor.mergeElement(contentBytes, elementContent, elementName, elementId);
        if (updatedContent != null) {
//            final BillMetadata metadata = document.getMetadata().getOrError(() -> "Document metadata is required!");
         //   updatedContent = numberProcessor.renumberArticles(updatedContent, metadata.getLanguage());
            updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        }
        return updatedContent;
    }

    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception{
        Validate.notNull(docContent, "Document is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
    
        return xmlContentProcessor.getSplittedElement(docContent, elementContent, elementName, elementId);
    }

    @Override
    public Element getMergeOnElement(Bill document, String elementContent, String elementName, String elementId) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getMergeOnElement(contentBytes, elementContent, elementName, elementId);
    }

    private byte[] getContent(Bill document) {
        final Content content = document.getContent().getOrError(() -> "Document content is required!");
        return content.getSource().getBytes();
    }
}
