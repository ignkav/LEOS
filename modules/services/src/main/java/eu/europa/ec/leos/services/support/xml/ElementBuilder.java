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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.*;

public class ElementBuilder {
    
    //  TODO create a builder  ElementBuilder builder =
    public static Element createArticle(Document document, String articleId) {
        Element element = document.createElement(ARTICLE);
        Attr attr = document.createAttribute(XMLID);
        attr.setValue(articleId);
        element.setAttributeNode(attr);

//        attr = document.createAttribute(LEOS_DELETABLE_ATTR);
//        attr.setValue("false");
//        element.setAttributeNode(attr);
//
//        attr = document.createAttribute(LEOS_EDITABLE_ATTR);
//        attr.setValue("true");
//        element.setAttributeNode(attr);
        
        element.appendChild(createElement(document, NUM, "num_1", "Article 4"));
        element.appendChild(createElement(document, HEADING, "heading_1", "Definitions"));
        return element;
    }
    
    public static Element createElement(Document document, String tagName, String elementId, String content) {
        Element element = document.createElement(tagName);
        
        Attr attr = document.createAttribute(XMLID);
        attr.setValue(elementId);
        element.setAttributeNode(attr);
        
        element.appendChild(document.createTextNode(content));
        
        return element;
    }
    
//    public static Node createElement(Document document, Node current, String elementName, String elementContent) {
//        Element element = document.createElement(elementName);
//        element.setTextContent(elementContent);
//        Node newNode = XmlUtils.addFirstChild(element, current);
//        return newNode;
//    }
}
