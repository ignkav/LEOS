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
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.support.xml.NumberProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlHelper;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.NumberingType;
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
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPOINT;

@Service
class AnnexProcessorImpl implements AnnexProcessor {

    private XmlContentProcessor xmlContentProcessor;
    protected NumberProcessor numberProcessor;
    private final ElementProcessor<Annex> elementProcessor;
    protected final XmlTableOfContentHelper xmlTableOfContentHelper;
    private MessageHelper messageHelper;
    private Provider<StructureContext> structureContextProvider;

    @Autowired
    public AnnexProcessorImpl(XmlContentProcessor xmlContentProcessor, NumberProcessor numberProcessor, ElementProcessor<Annex> elementProcessor, 
            MessageHelper messageHelper, Provider<StructureContext> structureContextProvider, XmlTableOfContentHelper xmlTableOfContentHelper) {
        super();
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberProcessor = numberProcessor;
        this.elementProcessor = elementProcessor;
        this.messageHelper = messageHelper;
        this.structureContextProvider = structureContextProvider;
        this.xmlTableOfContentHelper = xmlTableOfContentHelper;
    }
    
    @Override
    public byte[] deleteAnnexBlock(Annex document, String elementId, String tagName) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
        
        byte[] xmlContent = elementProcessor.deleteElement(document, elementId, tagName);
        return updateAnnexContent(elementId, tagName, xmlContent);
    }
    
    @Override
    public byte[] insertAnnexBlock(Annex document, String elementId, String tagName, boolean before) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");

        final Element parentElement;
        final String template;
        byte[] updatedContent;
        List<TocItem> items = structureContextProvider.get().getTocItems();

        switch (tagName) {
            case LEVEL:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, LEVEL), "#", messageHelper);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                updatedContent = xmlContentProcessor.insertDepthAttribute(updatedContent, tagName, elementId);
                updatedContent = numberProcessor.renumberLevel(updatedContent);
                break;
            case ARTICLE:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, ARTICLE), "#", "Article heading...", messageHelper);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                updatedContent = numberProcessor.renumberArticles(updatedContent);
                break;
            case PARAGRAPH:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, tagName), messageHelper);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                break;
            case SUBPARAGRAPH:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), tagName, elementId);
                if (isFirstSubParagraph(document, parentElement.getElementId(), parentElement.getElementTagName(), elementId)) {
                    updatedContent = insertAnnexBlock(document, parentElement.getElementId(), parentElement.getElementTagName(), before);
                } else if (!PARAGRAPH.equals(parentElement.getElementTagName())) {
                    template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, SUBPARAGRAPH), messageHelper);
                    updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                } else {
                    throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
                }
                break;
            case POINT:
            case INDENT:
                template = XmlHelper.getTemplate(TocItemUtils.getTocItemByNameOrThrow(items, tagName), "#", messageHelper);
                updatedContent = xmlContentProcessor.insertElementByTagNameAndId(getContent(document), template, tagName, elementId, before);
                updatedContent = xmlContentProcessor.insertAffectedAttributeIntoParentElements(updatedContent, elementId);
                updatedContent = numberProcessor.renumberLevel(updatedContent);
                updatedContent = numberProcessor.renumberParagraph(updatedContent);
                break;
            case CONTENT:
            case SUBPOINT:
                parentElement = xmlContentProcessor.getParentElement(getContent(document), tagName, elementId);
                updatedContent = insertAnnexBlock(document, parentElement.getElementId(), parentElement.getElementTagName(), before);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation for tag: " + tagName);
        }

        updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        return updatedContent;
    }

    private boolean isFirstSubParagraph(Annex annex, String elementId, String tagName, String subParElementId) {
        Element firstSubParElement = xmlContentProcessor.getChildElement(getContent(annex), tagName, elementId, Arrays.asList(SUBPARAGRAPH), 1);
        return firstSubParElement != null && subParElementId.equals(firstSubParElement.getElementId());
    }

    @Override
    public byte[] updateAnnexBlock(Annex annex, String elementId, String tagName, String elementFragment) {
        byte[] updatedContent = null;
        if (xmlContentProcessor.needsToBeIndented(elementFragment)) {
            byte[] contentBytes = getContent(annex);
            List<TableOfContentItemVO> toc = xmlTableOfContentHelper.buildTableOfContent(DOC, contentBytes, TocMode.NOT_SIMPLIFIED);
            updatedContent = xmlContentProcessor.indentElement(contentBytes, tagName, elementId, elementFragment, toc);
        } else {
            updatedContent = elementProcessor.updateElement(annex, elementFragment, tagName, elementId);
        }
        return updateAnnexContent(elementId, tagName, updatedContent);
    }

    private byte[] updateAnnexContent(String elementId, String tagName, byte[] xmlContent) {
        if (hasDepth(tagName)) {
            xmlContent = xmlContentProcessor.insertDepthAttribute(xmlContent, tagName, elementId);
            xmlContent = numberProcessor.renumberLevel(xmlContent);
        } else if (Arrays.asList(PARAGRAPH, SUBPARAGRAPH, POINT, INDENT, SUBPOINT).contains(tagName)) {
            xmlContent = numberProcessor.renumberParagraph(xmlContent);
            if (Arrays.asList(POINT, INDENT, SUBPOINT, SUBPARAGRAPH).contains(tagName)) {
                xmlContent = numberProcessor.renumberLevel(xmlContent);
            }
        } else if (tagName.equals(ARTICLE)) {
            xmlContent = numberProcessor.renumberArticles(xmlContent);
        }
        return xmlContentProcessor.doXMLPostProcessing(xmlContent);
    }

    private boolean hasDepth(String tagName) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems().stream().
        filter(tocItem -> (tocItem.getAknTag().value().equalsIgnoreCase(tagName) && 
                tocItem.getNumberingType().value().equals(NumberingType.ARABIC_POSTFIX_DEPTH.value()))).collect(Collectors.toList());
        return tocItems.size() > 0 && tocItems.get(0).getNumberingType().value().equals(NumberingType.ARABIC_POSTFIX_DEPTH.value());
    }
    
    private byte[] getContent(Annex annex) {
        final Content content = annex.getContent().getOrError(() -> "Annex content is required!");
        return content.getSource().getBytes();
    }
    
    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception{
        Validate.notNull(docContent, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");

        return xmlContentProcessor.getSplittedElement(docContent, elementContent, elementName, elementId);
    }
    
    @Override
    public Element getMergeOnElement(Annex document, String elementContent, String elementName, String elementId) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getMergeOnElement(contentBytes, elementContent, elementName, elementId);
    }

    @Override
    public Element getTocElement(final Annex document, final String elementId, final List<TableOfContentItemVO> toc) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getTocElement(contentBytes, elementId, toc, Arrays.asList(SUBPARAGRAPH));
    }

    @Override
    public byte[] mergeElement(Annex document, String elementContent, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementContent, "ElementContent is required.");
        Validate.notNull(elementName, "ElementName is required.");
        Validate.notNull(elementId, "ElementId is required.");
        
        final byte[] contentBytes = getContent(document);
        byte[] updatedContent = xmlContentProcessor.mergeElement(contentBytes, elementContent, elementName, elementId);
        if (updatedContent != null) {
            updatedContent = xmlContentProcessor.doXMLPostProcessing(updatedContent);
        }
        return updatedContent;
    }

    @Override
    public LevelItemVO getLevelItemVO(Annex document, String elementId, String elementTagName) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getLevelItemVo(contentBytes, elementId, elementTagName);
    }
}
