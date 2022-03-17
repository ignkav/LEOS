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
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.TocItemUtils;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.util.Collections;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.HEADING;

@Service
public class ElementProcessorImpl<T extends XmlDocument> implements ElementProcessor<T> {

    @Autowired
    private XmlContentProcessor xmlContentProcessor;
    @Autowired
    private Provider<StructureContext> structureContextProvider;
    @Autowired
    private CloneContext cloneContext;

    @Override
    public String getElement(XmlDocument document, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = document.getContent().get().getSource().getBytes();
        return xmlContentProcessor.getElementByNameAndId(contentBytes, elementName, elementId);
    }

    @Override
    public Element getSiblingElement(T document, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementName, "ElementName id is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getSiblingElement(contentBytes, elementName, elementId, Collections.emptyList(), false);
    }

    @Override
    public Element getChildElement(T document, String elementName, String elementId, List<String> elementTags, int position) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementName, "ElementName id is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.getChildElement(contentBytes, elementName, elementId, elementTags, position);
    }

    @Override
    public byte[] updateElement(T document, String elementContent, String elementName, String elementId) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");

        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        elementContent = removeEmptyHeading(document.getContent().get().getSource().getBytes(), elementContent, elementName, elementId, tocItems);
        // merge the updated content with the actual document and return updated document
        byte[] contentBytes = getContent(document);
        if(isClonedProposal()) {
            Pair<byte[], String> result = xmlContentProcessor.updateSoftMovedAttributes(contentBytes, elementContent);
            if(result.left() != null && result.left().length > 0) {
                contentBytes = result.left();
            } else if(result.right() != null && result.right().getBytes().length > 0) {
                elementContent = result.right();
            }
        }
        return xmlContentProcessor.replaceElementByTagNameAndId(contentBytes, elementContent, elementName, elementId);
    }

    @Override
    public byte[] deleteElement(T document, String elementId, String elementType) throws Exception {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(elementId, "Element id is required.");
    
        final byte[] contentBytes = getContent(document);
        return xmlContentProcessor.removeElementByTagNameAndId(contentBytes, elementType, elementId);
    }

    @Override
    public byte[] replaceTextInElement(T document, String origText, String newText, String elementId, int startOffset, int endOffset) {
        Validate.notNull(document, "Document is required.");
        Validate.notEmpty(origText, "Orginal Text is required");
        Validate.notNull(elementId, "Element Id is required");
        Validate.notNull(newText, "New Text is required");
        
        final byte[] byteXmlContent = getContent(document);
        return xmlContentProcessor.replaceTextInElement(byteXmlContent, origText, newText, elementId, startOffset, endOffset);
    }

    private String removeEmptyHeading(byte[] xmlContent, String newContent, String tagName, String idAttributeValue, List<TocItem> tocItems) {
        String elementTagName;
        if (tagName.equals(HEADING)) {
            elementTagName = xmlContentProcessor.getParentElement(xmlContent, tagName, idAttributeValue).getElementTagName();
        } else {
            elementTagName = tagName;
        }
        TocItem tocItem = TocItemUtils.getTocItemByName(tocItems, elementTagName);
        if (tocItem.getItemHeading() == OptionsType.OPTIONAL) {
            newContent = xmlContentProcessor.removeEmptyHeading(newContent);
        }
        return newContent;
    }

    public String getElementAttributeValueByNameAndId(T document, String attributeName, String tagName, String idAttributeValue) {
        Validate.notNull(document, "Document is required.");
        Validate.notNull(idAttributeValue, "Element id is required.");

        return xmlContentProcessor.getElementAttributeValueByNameAndId(document.getContent().get().getSource().getBytes(), attributeName, tagName, idAttributeValue);
    }
    
    private byte[] getContent(T document) {
        final Content content = document.getContent().getOrError(() -> "Annex content is required!");
        return content.getSource().getBytes();
    }

    private boolean isClonedProposal() {
        return cloneContext != null && cloneContext.isClonedProposal();
    }
}
