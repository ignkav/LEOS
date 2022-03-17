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
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.support.xml.NumberProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.DOC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPOINT;

@Service
public class ExplanatoryProcessorImpl implements  ExplanatoryProcessor{
    private XmlContentProcessor xmlContentProcessor;
    protected NumberProcessor numberProcessor;
    private final ElementProcessor<Explanatory> elementProcessor;
    protected final XmlTableOfContentHelper xmlTableOfContentHelper;
    private Provider<StructureContext> structureContextProvider;

    @Autowired
    public ExplanatoryProcessorImpl(XmlContentProcessor xmlContentProcessor, NumberProcessor numberProcessor, ElementProcessor<Explanatory> elementProcessor,
                                    Provider<StructureContext> structureContextProvider, XmlTableOfContentHelper xmlTableOfContentHelper) {
        super();
        this.xmlContentProcessor = xmlContentProcessor;
        this.numberProcessor = numberProcessor;
        this.elementProcessor = elementProcessor;
        this.structureContextProvider = structureContextProvider;
        this.xmlTableOfContentHelper = xmlTableOfContentHelper;
    }

    private byte[] getContent(Explanatory explanatory) {
        final Content content = explanatory.getContent().getOrError(() -> "Explanatory content is required!");
        return content.getSource().getBytes();
    }

    @Override
    public byte[] insertExplanatoryBlock(Explanatory document, String elementId, String tagName, boolean before) {
        return new byte[0];
    }

    @Override
    public byte[] deleteExplanatoryBlock(Explanatory document, String elementId, String tagName) throws Exception {
        return new byte[0];
    }

    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception {
        return null;
    }

    @Override
    public Element getMergeOnElement(Explanatory document, String elementContent, String elementName, String elementId) throws Exception {
        return null;
    }

    @Override
    public Element getTocElement(Explanatory document, String elementId, List<TableOfContentItemVO> toc) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "ElementId is required.");

        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getTocElement(contentBytes, elementId, toc, Arrays.asList(SUBPARAGRAPH));
    }

    @Override
    public byte[] mergeElement(Explanatory document, String elementContent, String elementName, String elementId) {
        return new byte[0];
    }

    @Override
    public byte[] updateExplanatoryBlock(Explanatory document, String elementId, String tagName, String elementFragment) {
        byte[] updatedContent = null;
        if (xmlContentProcessor.needsToBeIndented(elementFragment)) {
            byte[] contentBytes = getContent(document);
            List<TableOfContentItemVO> toc = xmlTableOfContentHelper.buildTableOfContent(DOC, contentBytes, TocMode.NOT_SIMPLIFIED);
            updatedContent = xmlContentProcessor.indentElement(contentBytes, tagName, elementId, elementFragment, toc);
        } else {
            updatedContent = elementProcessor.updateElement(document, elementFragment, tagName, elementId);
        }
        return updateExplanatoryContent(elementId, tagName, updatedContent);
    }


    private byte[] updateExplanatoryContent(String elementId, String tagName, byte[] xmlContent) {
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
}
