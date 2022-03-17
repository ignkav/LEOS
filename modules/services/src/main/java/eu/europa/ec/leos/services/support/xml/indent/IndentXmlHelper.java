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
package eu.europa.ec.leos.services.support.xml.indent;

import eu.europa.ec.leos.services.support.IdGenerator;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_UNUMBERED_PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.XMLID;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_TYPE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_INDENT_ORIGIN_NUM_ID_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.insertOrUpdateAttributeValue;

public class IndentXmlHelper {
    public static byte[] buildNumFromIndentedElement(TableOfContentItemVO item) {
        if (item.getNumber() == null) {
            return new byte[0];
        }
        StringBuilder numTagStr = new StringBuilder("<" + NUM);

        // Insert Origin
        if (item.getOriginNumAttr() != null) {
            numTagStr.append(" " + LEOS_ORIGIN_ATTR + "=\"" + item.getOriginNumAttr() + "\"");
        }

        // Insert ID
        if (item.getElementNumberId() != null) {
            numTagStr.append(" " + XMLID + "=\"" + item.getElementNumberId() + "\"");
        } else {
            numTagStr.append(" " + XMLID + "=\"" + IdGenerator.generateId(NUM.substring(0, 3), 7) + "\"");
        }

        numTagStr.append(">");

        // Insert value
        numTagStr.append(item.getNumber());

        // Close tag
        numTagStr.append("</" + NUM + ">");

        return numTagStr.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] buildStartTag(TableOfContentItemVO item, String tagName) {
        StringBuilder startTagStr = new StringBuilder("<" + tagName);

        // Insert Origin
        if (item.getOriginAttr() != null) {
            startTagStr.append(" " + LEOS_ORIGIN_ATTR + "=\"" + item.getOriginAttr() + "\"");
        }

        // Insert ID
        if (item.getId() != null) {
            startTagStr.append(" " + XMLID + "=\"" + item.getId() + "\"");
        } else {
            startTagStr.append(" " + XMLID + "=\"" + IdGenerator.generateId(tagName.substring(0, 3), 7) + "\"");
        }

        startTagStr.append(">");

        return startTagStr.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] setIndentAttributes(byte[] contentBytes, TableOfContentItemVO item) {
        StringBuilder content = new StringBuilder(new String(contentBytes, StandardCharsets.UTF_8));
        if (item.isIndented()) {
            insertOrUpdateAttributeValue(content, LEOS_INDENT_ORIGIN_TYPE_ATTR, item.getIndentOriginType().name());
            insertOrUpdateAttributeValue(content, LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR, String.valueOf(item.getIndentOriginIndentLevel()));
            insertOrUpdateAttributeValue(content, LEOS_INDENT_ORIGIN_NUM_ATTR, item.getIndentOriginNumValue());
            insertOrUpdateAttributeValue(content, LEOS_INDENT_ORIGIN_NUM_ID_ATTR, item.getIndentOriginNumId());
            insertOrUpdateAttributeValue(content, LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR, item.getIndentOriginNumOrigin());
            if (item.getTocItem().getAknTag().name().equalsIgnoreCase(PARAGRAPH) && item.getNumber() == null) {
                insertOrUpdateAttributeValue(content, LEOS_INDENT_UNUMBERED_PARAGRAPH, "true");
            }
        }
        return content.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static String extractContentFromUnumberedParagraph(TableOfContentItemVO item) {
        String content = item.getContent();

        // First remove paragraph tag
        content = content.replaceAll("<[/]?" + PARAGRAPH + "[\\s]?[^>]*>", "");

        if (content.contains("<" + SUBPARAGRAPH)) {
            int startTag = content.indexOf("<" + SUBPARAGRAPH);
            int endTag = content.indexOf("</" + SUBPARAGRAPH);
            if (startTag != -1 && endTag != -1) {
                content = content.substring(startTag, endTag) + "</" + SUBPARAGRAPH + ">";
            }
        } else {
            int startTag = content.indexOf("<" + CONTENT);
            int endTag = content.indexOf("</" + CONTENT);
            if (startTag != -1 && endTag != -1) {
                content = content.substring(startTag, endTag) + "</" + CONTENT + ">";
            }
        }

        return content;
    }
}
