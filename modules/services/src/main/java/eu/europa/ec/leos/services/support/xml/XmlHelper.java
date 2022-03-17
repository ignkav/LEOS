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

import com.google.common.collect.ImmutableMap;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.services.support.ByteArrayBuilder;
import eu.europa.ec.leos.services.support.IdGenerator;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlHelper {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String OPEN_TAG = "<";
    public static final String CLOSE_TAG = ">";
    public static final String OPEN_END_TAG = "</";
    public static final String CLOSE_END_TAG = "/>";
    private final static byte[] OPEN_END_TAG_BYTE = OPEN_END_TAG.getBytes(UTF_8);
    private final static byte[] CLOSE_TAG_BYTE = CLOSE_TAG.getBytes(UTF_8);

    public static final String MARKER_ATTRIBUTE = "marker";

    public static final String AKNBODY = "aknbody";
    public static final String DOC = "doc";
    public static final String BILL = "bill";
    public static final String AKOMANTOSO = "akomaNtoso";
    public static final String META = "meta";
    public static final String BLOCKCONTAINER = "blockContainer";
    public static final String AUTHORIAL_NOTE = "authorialNote";
    public static final String MATHJAX = "mathjax";
    public static final String MREF = "mref";
    public static final String REF = "ref";
    public static final String HREF = "href";
    public static final String BOLD = "b";
    public static final String ITALICS = "i";
    public static final String UNDERLINE = "u";
    public static final String SUP = "sup";
    public static final String SUB = "sub";
    public static final String INLINE = "inline";
    public static final String FORMULA = "formula";
    public static final String INTRO = "intro";
    public static final String HEADING = "heading";
    public static final String NUM = "num";
    public static final String P = "p";

    public static final String EC = "ec";
    public static final String CN = "cn";
    public static final String LS = "ls";

    public static final String PREFACE = "preface";
    public static final String PREAMBLE = "preamble";
    public static final String CITATIONS = "citations";
    public static final String CITATION = "citation";
    public static final String RECITALS = "recitals";
    public static final String RECITAL = "recital";
    public static final String BODY = "body";
    public static final String PART = "part";
    public static final String TITLE = "title";
    public static final String CHAPTER = "chapter";
    public static final String SECTION = "section";
    public static final String ARTICLE = "article";
    public static final String PARAGRAPH = "paragraph";
    public static final String SUBPARAGRAPH = "subparagraph";
    public static final String LIST = "list";
    public static final String POINT = "point";
    public static final String INDENT = "indent";
    public static final String SUBPOINT = "alinea";
    public static final String SUBPOINT_LABEL = "subparagraph";
    public static final String CLAUSE = "clause";
    public static final String CONCLUSIONS = "conclusions";
    public static final String MAIN_BODY = "mainBody";
    public static final String TBLOCK = "tblock";
    public static final String LEVEL = "level";
    public static final String CONTENT = "content";
    public static final String CROSSHEADING = "crossHeading";
    public static final String BLOCK = "block";
    public static final String EXPL_COUNCIL = "EXPL_COUNCIL";

    public static final String ID = "id";
    public static final String XMLID = "xml:id";
    public static final String LEOS_REF = "leos:ref";
    public static final String WHITESPACE = " ";
    public static final byte[] HEADING_BYTES = HEADING.getBytes(UTF_8);
    public static final byte[] HEADING_START_TAG = "<".concat(HEADING).concat(">").getBytes(UTF_8);
    public static final byte[] CROSS_HEADING_BYTES = CROSSHEADING.getBytes(UTF_8);
    public static final byte[] CROSS_HEADING_START_TAG = "<".concat(CROSSHEADING).concat(">").getBytes(UTF_8);
    public static final byte[] BLOCK_HEADING_BYTES = BLOCK.getBytes(UTF_8);
    public static final byte[] BLOCK_HEADING_START_TAG = "<".concat(BLOCK).concat(">").getBytes(UTF_8);
    public static final byte[] NUM_BYTES = NUM.getBytes(UTF_8);
    public static final byte[] NUM_START_TAG = "<".concat(NUM).concat(">").getBytes(UTF_8);

    public static final String LEOS_ORIGIN_ATTR = "leos:origin";
    public static final String LEOS_DELETABLE_ATTR = "leos:deletable";
    public static final String LEOS_EDITABLE_ATTR = "leos:editable";
    public static final String LEOS_AFFECTED_ATTR = "leos:affected";
    public static final String LEOS_CROSS_HEADING_BLOCK_NAME = "leos:name";
    public static final String LEOS_REF_BROKEN_ATTR = "leos:broken";
    public static final String LEOS_DEPTH_ATTR = "leos:depth";
    public static final String LEOS_ORIGINAL_DEPTH_ATTR = "leos:originaldepth";
    public static final String LEOS_LIST_TYPE_ATTR = "leos:list-type";
    public static final String LEOS_LIST_VALUE_ATTR = "leos:list-value";
    public static final String LEOS_CROSSHEADING_TYPE = "leos:crossheading-type";

    public static final String LEOS_SOFT_ACTION_ATTR = "leos:softaction";
    public static final String LEOS_SOFT_ACTION_ROOT_ATTR = "leos:softactionroot";
    public static final String LEOS_SOFT_MOVED_LABEL_ATTR = "leos:softmove_label";
    public static final String LEOS_SOFT_USER_ATTR = "leos:softuser";
    public static final String LEOS_SOFT_DATE_ATTR = "leos:softdate";
    public static final String LEOS_SOFT_MOVE_TO = "leos:softmove_to";
    public static final String LEOS_SOFT_MOVE_FROM = "leos:softmove_from";
    public static final String LEOS_SOFT_TRANS_FROM = "leos:softtrans_from";
    public static final String SOFT_MOVE_PLACEHOLDER_ID_PREFIX = "moved_";
    public static final String SOFT_DELETE_PLACEHOLDER_ID_PREFIX = "deleted_";
    public static final String SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX = "transformed_";
    public static final String TOGGLED_TO_NUM = "toggled_to_num";
    public static final String STATUS_IGNORED_ATTR = "status=\"ignored\"";

    public static final String CLONED_PROPOSAL_REF = "clonedProposalRef";
    public static final String CLONED_TARGET_USER = "targetUser";
    public static final String CLONED_CREATION_DATE = "creationDate";
    public static final String CLONED_STATUS = "status";

    public static final String LEOS_INDENT_LEVEL_ATTR = "leos:indent-level";
    public static final String LEOS_INDENT_NUMBERED_ATTR = "leos:indent-numbered";
    public static final String LEOS_INDENT_ORIGIN_TYPE_ATTR = "leos:indent-origin-type";
    public static final String LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR = "leos:indent-origin-indent-level";
    public static final String LEOS_INDENT_ORIGIN_NUM_ID_ATTR = "leos:indent-origin-num-id";
    public static final String LEOS_INDENT_ORIGIN_NUM_ATTR = "leos:indent-origin-num";
    public static final String LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR = "leos:indent-origin-num-origin";
    public static final String LEOS_INDENT_UNUMBERED_PARAGRAPH = "leos:indent-unumbered-paragraph";

    public static final int LEVEL_MAX_DEPTH = 7;

    public static final String EMPTY_STRING = "";
    public static final String NON_BREAKING_SPACE = "\u00A0";
    public static final String CLASS_ATTR = "class";

    private static final String ID_PLACEHOLDER = "${id}";
    private static final String ID_PLACEHOLDER_ESCAPED = "\\Q${id}\\E";
    private static final String NUM_PLACEHOLDER = "${num}";
    private static final String NUM_PLACEHOLDER_ESCAPED = "\\Q${num}\\E";
    private static final String HEADING_PLACEHOLDER = "${heading}";
    private static final String HEADING_PLACEHOLDER_ESCAPED = "\\Q${heading}\\E";
    private static final String CONTENT_TEXT_PLACEHOLDER = "${default.content.text}";
    private static final String CONTENT_TEXT_PLACEHOLDER_ESCAPED = "\\Q${default.content.text}\\E";

    public static final String LEVEL_NUM_SEPARATOR = ".";

    public static final List<String> ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING = Arrays.asList(ARTICLE, PARAGRAPH, SUBPARAGRAPH, POINT, SUBPOINT, INDENT, LEVEL);
    public static final List<String> POINT_ROOT_PARENT_ELEMENTS = Arrays.asList(ARTICLE, LEVEL);
    public static final List<String> INLINE_ELEMENTS = Arrays.asList(AUTHORIAL_NOTE, MATHJAX, MREF, REF, BOLD, ITALICS, UNDERLINE, SUP, SUB, INLINE);
    private static final List<String> ELEMENTS_TO_REMOVE_FROM_CONTENT = Arrays.asList(INLINE, AUTHORIAL_NOTE);
    public static final List<String> ELEMENTS_TO_HIDE_CONTENT = Arrays.asList(PREFACE, PREAMBLE, CITATIONS, RECITALS, BODY, MAIN_BODY);
    public static final List<String> SOFT_ACTIONS_PREFIXES = Arrays.asList(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, SOFT_DELETE_PLACEHOLDER_ID_PREFIX);

    public static final String XML_NAME = "name";
    public static final String XML_DOC_TYPE = "docType";
    public static final String XML_TLC_REFERENCE = "TLCReference";
    public static final String XML_SHOW_AS = "showAs";
    public static final String ANNEX = "Annex";

    public static byte[] buildTag(byte[] startTag, byte[] tagName, byte[] value) {
        ByteArrayBuilder tag = new ByteArrayBuilder();
        tag.append(startTag);
        tag.append(value);
        if (tagName != null) {
            tag.append(OPEN_END_TAG_BYTE);
            tag.append(tagName);
            tag.append(CLOSE_TAG_BYTE);
        }
        return tag.getContent();
    }

    public static String getTemplateWithExtractedContent(TocItem tocItem, String content, MessageHelper messageHelper) {
        String template;
        if ((content != null) && (content.indexOf("<content") != -1)) {
            template = getTemplate(tocItem, ImmutableMap.of(NUM, Collections.emptyMap(), HEADING, Collections.emptyMap(), CONTENT,
                    Collections.singletonMap("<content.*?" + CONTENT_TEXT_PLACEHOLDER_ESCAPED + "</p></content>", content.substring(content.indexOf("<content"), content.indexOf("</content>") + "</content>".length()))));
        } else {
            template = getTemplate(tocItem, ImmutableMap.of(NUM, Collections.emptyMap(), HEADING, Collections.emptyMap(), CONTENT,
                    Collections.singletonMap(CONTENT_TEXT_PLACEHOLDER_ESCAPED, getDefaultContentText(tocItem.getAknTag().value(), messageHelper))));
        }
        return template;
    }

    public static String extractContentFromTocItem(TableOfContentItemVO tocItem) {
        if (tocItem.getContent() == null) {
            return "";
        }
        Pair<Integer, Integer> indexes = getStartEndIndexesOfContent(tocItem);

        return tocItem.getContent().substring(indexes.left(), indexes.right());
    }

    public static String replaceContentFromTocItem(TableOfContentItemVO tocItem, String updatedContent) {
        StringBuilder newContent = new StringBuilder(tocItem.getContent() != null ? tocItem.getContent() : "");

        Pair<Integer, Integer> indexes = getStartEndIndexesOfContent(tocItem);

        newContent.replace(indexes.left(), indexes.right(), updatedContent);
        return newContent.toString();
    }

    private static Pair<Integer, Integer> getStartEndIndexesOfContent(TableOfContentItemVO tocItem) {
        String content = tocItem.getContent();
        boolean containTagContent = content.contains(OPEN_TAG + CONTENT) && content.contains(OPEN_END_TAG + CONTENT + CLOSE_TAG);
        String tagName = tocItem.getTocItem().getAknTag().value();
        boolean containTagName = content.contains(OPEN_TAG + tagName.toLowerCase()) && content.contains(OPEN_END_TAG + tagName.toLowerCase(Locale.ROOT) + CLOSE_TAG);
        if (!containTagName) {
            containTagName = content.contains(OPEN_TAG + tagName) && content.contains(OPEN_END_TAG + tagName + CLOSE_TAG);
        }
        int startIndexContent = 0;
        int endIndexContent = content.length();
        if (containTagContent) {
            startIndexContent = content.indexOf(CLOSE_TAG, content.indexOf(OPEN_TAG + CONTENT)) + 1;
            endIndexContent = content.indexOf(OPEN_END_TAG + CONTENT + CLOSE_TAG, startIndexContent);
        } else if (containTagName) {
            startIndexContent = content.indexOf(CLOSE_TAG, content.indexOf(OPEN_TAG + tagName)) + 1;
            endIndexContent = content.indexOf(OPEN_END_TAG + tagName + CLOSE_TAG, startIndexContent);
        }

        String tmpContent = content.substring(startIndexContent, endIndexContent);
        boolean containP = tmpContent.contains(OPEN_TAG + P)
                && tmpContent.contains(OPEN_END_TAG + P + CLOSE_TAG);
        if (containP) {
            int startPTag = content.indexOf(OPEN_TAG + P, startIndexContent) + 1;
            startIndexContent = content.indexOf(CLOSE_TAG, startPTag) + 1;
            endIndexContent = content.indexOf(OPEN_END_TAG + P + CLOSE_TAG, startIndexContent);
        }
        return new Pair<>(startIndexContent, endIndexContent);
    }

    public static String getTemplate(String tagName) {
        return "<" + tagName + " xml:id=\"" + IdGenerator.generateId(tagName.substring(0, 3), 7) + "\"></" + tagName + ">";
    }

    public static String getTemplate(TocItem tocItem, MessageHelper messageHelper) {
        return getTemplate(tocItem, ImmutableMap.of(NUM, Collections.emptyMap(), HEADING, Collections.emptyMap(),
                CONTENT, Collections.singletonMap(CONTENT_TEXT_PLACEHOLDER_ESCAPED, getDefaultContentText(tocItem.getAknTag().value(), messageHelper))));
    }

    public static String getTemplate(TocItem tocItem, String num, MessageHelper messageHelper) {
        return getTemplate(tocItem, ImmutableMap.of(NUM, Collections.singletonMap(NUM_PLACEHOLDER_ESCAPED, StringUtils.isNotEmpty(num) && tocItem.isNumWithType() ? StringUtils.capitalize(tocItem.getAknTag().value()) + " " + num : num),
                HEADING, Collections.singletonMap(HEADING_PLACEHOLDER_ESCAPED, StringUtils.EMPTY), CONTENT, Collections.singletonMap(CONTENT_TEXT_PLACEHOLDER_ESCAPED, getDefaultContentText(tocItem.getAknTag().value(), messageHelper))));
    }

    public static String getTemplate(TocItem tocItem, String num, String heading, MessageHelper messageHelper) {
        return getTemplate(tocItem, ImmutableMap.of(NUM, Collections.singletonMap(NUM_PLACEHOLDER_ESCAPED, StringUtils.isNotEmpty(num) && tocItem.isNumWithType() ? StringUtils.capitalize(tocItem.getAknTag().value()) + " " + num : num),
                HEADING, Collections.singletonMap(HEADING_PLACEHOLDER_ESCAPED, heading), CONTENT, Collections.singletonMap(CONTENT_TEXT_PLACEHOLDER_ESCAPED, getDefaultContentText(tocItem.getAknTag().value(), messageHelper))));
    }

    private static String getTemplate(TocItem tocItem, Map<String, Map<String, String>> templateItems) {
        StringBuilder template = tocItem.getTemplate() != null ? new StringBuilder(tocItem.getTemplate()) : getDefaultTemplate(tocItem);
        replaceAll(template, ID_PLACEHOLDER_ESCAPED, IdGenerator.generateId("akn_" + tocItem.getAknTag().value(), 7));

        replaceTemplateItems(template, NUM, tocItem.getItemNumber(), templateItems.get(NUM));
        replaceTemplateItems(template, HEADING, tocItem.getItemHeading(), templateItems.get(HEADING));
        replaceTemplateItems(template, CONTENT, OptionsType.MANDATORY, templateItems.get(CONTENT));

        return template.toString();
    }

    private static StringBuilder getDefaultTemplate(TocItem tocItem) {
        StringBuilder defaultTemplate = new StringBuilder("<" + tocItem.getAknTag().value() + " xml:id=\"" + ID_PLACEHOLDER + "\">");
        if (OptionsType.MANDATORY.equals(tocItem.getItemNumber()) || OptionsType.OPTIONAL.equals(tocItem.getItemNumber())) {
            defaultTemplate.append(tocItem.isNumberEditable() ? "<num>" + NUM_PLACEHOLDER + "</num>" : "<num leos:editable=\"false\">" + NUM_PLACEHOLDER + "</num>");
        }
        if (OptionsType.MANDATORY.equals(tocItem.getItemHeading()) || OptionsType.OPTIONAL.equals(tocItem.getItemHeading())) {
            defaultTemplate.append("<heading>" + HEADING_PLACEHOLDER + "</heading>");
        }
        defaultTemplate.append("<content><p>" + CONTENT_TEXT_PLACEHOLDER + "</p></content></" + tocItem.getAknTag().value() + ">");
        return defaultTemplate;
    }

    public static String createTransformedParaWithTocItem(TableOfContentItemVO tableOfContentItemVO, String tocItemContent, User user) {
        StringBuilder defaultTemplate = new StringBuilder("<" + tableOfContentItemVO.getTocItem().getAknTag().value()
                + "></" + tableOfContentItemVO.getTocItem().getAknTag().value() + ">");

        defaultTemplate = insertOrUpdateAttributeValue(defaultTemplate, XMLID, SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + tableOfContentItemVO.getId());
        defaultTemplate = insertOrUpdateAttributeValue(defaultTemplate, LEOS_SOFT_TRANS_FROM, tableOfContentItemVO.getId());
        defaultTemplate = insertOrUpdateAttributeValue(defaultTemplate, LEOS_ORIGIN_ATTR, tableOfContentItemVO.getOriginAttr());

        defaultTemplate = new StringBuilder(new String(updateSoftInfo(defaultTemplate.toString().getBytes(UTF_8), tableOfContentItemVO.getSoftActionAttr(), tableOfContentItemVO.isSoftActionRoot(), user,
                tableOfContentItemVO.getOriginAttr(), null, tableOfContentItemVO.getTocItem())));

        if (tableOfContentItemVO.getNumber() != null) {
            StringBuilder numberElement = new StringBuilder(tocItemContent);
            numberElement = insertOrUpdateAttributeValue(numberElement, XMLID, SOFT_DELETE_PLACEHOLDER_ID_PREFIX + tableOfContentItemVO.getElementNumberId());
            insertContentBeforeEndTag(tableOfContentItemVO, numberElement.toString(), defaultTemplate);
        }
        insertContentBeforeEndTag(tableOfContentItemVO, tableOfContentItemVO.getContent(), defaultTemplate);
        return defaultTemplate.toString();
    }

    private static void insertContentBeforeEndTag(TableOfContentItemVO tableOfContentItemVO, String tocItemContent, StringBuilder defaultTemplate) {
        int indexOfEndTag = defaultTemplate.indexOf("</" + tableOfContentItemVO.getTocItem().getAknTag().value() + ">");
        defaultTemplate.insert(indexOfEndTag, tocItemContent, 0, tocItemContent.length());
    }

    private static void replaceTemplateItems(StringBuilder template, String itemName, OptionsType itemOption, Map<String, String> templateItem) {
        if (OptionsType.MANDATORY.equals(itemOption)) {
            templateItem.forEach((itemPlaceHolder, itemValue) -> {
                replaceAll(template, itemPlaceHolder, StringUtils.isEmpty(itemValue) ? "" : itemValue);
            });
        } else if (OptionsType.OPTIONAL.equals(itemOption)) {
            templateItem.forEach((itemPlaceHolder, itemValue) -> {
                if (StringUtils.isEmpty(itemValue)) {
                    replaceAll(template, "<" + itemName + ".*?" + itemPlaceHolder + "</" + itemName + ">", "");
                } else {
                    replaceAll(template, itemPlaceHolder, itemValue);
                }
            });
        }
    }

    public static void replaceAll(StringBuilder sb, String toReplace, String replacement) {
        int start = 0;
        Matcher m = Pattern.compile(toReplace).matcher(sb);
        while (m.find(start)) {
            sb.replace(m.start(), m.end(), replacement);
            start = m.start() + replacement.length();
        }
    }

    private static String getDefaultContentText(String tocTagName, MessageHelper messageHelper) {
        String defaultTextContent = messageHelper.getMessage("toc.item.template." + tocTagName + ".content.text");
        if (defaultTextContent.equals("toc.item.template." + tocTagName + ".content.text")) {
            defaultTextContent = messageHelper.getMessage("toc.item.template.default.content.text");
        }
        return defaultTextContent;
    }

    static String getContentText(String tagContent) {
        if (StringUtils.isNotEmpty(tagContent)) {
            return tagContent.substring(tagContent.indexOf('>') + 1, tagContent.indexOf("</"));
        }
        return "";
    }

    private static final ArrayList<String> prefixTobeUsedForChildren = new ArrayList<String>(Arrays.asList(ARTICLE, RECITALS, CITATIONS));

    public static String determinePrefixForChildren(String tagName, String idOfNode, String parentPrefix) {
        return prefixTobeUsedForChildren.contains(tagName) ? idOfNode : parentPrefix;  //if(root Node Name is in Article/Reictals/Citations..set the prefix)
    }

    private static final ArrayList<String> nodeToSkip = new ArrayList<String>(Arrays.asList(META));

    public static boolean skipNodeAndChildren(String tagName) {
        return nodeToSkip.contains(tagName) ? true : false;
    }

    private static final ArrayList<String> tagNamesToSkip = new ArrayList<String>(Arrays.asList(AKOMANTOSO, BILL, "documentCollection", "doc", "attachments"));

    public static boolean skipNodeOnly(String tagName) {
        return tagNamesToSkip.contains(tagName) ? true : false;
    }

    private static final ArrayList<String> parentEditableNodes = new ArrayList<String>(Arrays.asList(ARTICLE, RECITALS, CITATIONS, BLOCKCONTAINER));

    public static boolean isParentEditableNode(String tagName) {
        return parentEditableNodes.contains(tagName) ? true : false;
    }

    private static final ArrayList<String> exclusionList = new ArrayList<String>(Arrays.asList(AUTHORIAL_NOTE, NUM, CLAUSE));

    public static boolean isExcludedNode(String tagName) {
        return exclusionList.contains(tagName) ? true : false;
    }

    public static EditableAttributeValue getEditableAttribute(String tagName, String attrVal) {
        if (isExcludedNode(tagName)) {
            return EditableAttributeValue.FALSE; // editable = false;
        } else if (attrVal != null) {
            return attrVal.equalsIgnoreCase("true") ? EditableAttributeValue.TRUE : EditableAttributeValue.FALSE;
        } else if (isParentEditableNode(tagName)) {
            return EditableAttributeValue.FALSE; // editable = false;
        } else {
            return EditableAttributeValue.UNDEFINED; // editable not present;
        }
    }

    public static String getDateAsXml() {
        return getDateAsXml(new GregorianCalendar());
    }

    public static String getDateAsXml(GregorianCalendar calendar) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar).toXMLFormat();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Cannot instantiate new XMLGregorianCalendar");
        }
    }

    public static String getTrimmedXmlId(String content) {
        //TODO: Make it generic to trim extra spaces for all the attributes inside a tag
        //look for extra spaces after the xml:id attributes (<authorialnote xml:id="authNote_1"  marker="1"><p xml:id="authNote_1_p" >Footnote</p></authorialnote>)
        Pattern patternXmlIds = Pattern.compile("xml:id=\"([^\"]*)\"\\s(\\s|>)");
        Matcher matcherXmlIds = patternXmlIds.matcher(content);
        while (matcherXmlIds.find()) {
            String[] result = content.split(matcherXmlIds.group());
            result[0] += matcherXmlIds.group().replaceFirst("\\s", "");
            content = String.join("", result);
        }
        return content;
    }

    public static byte[] updateSoftTransFromAttribute(byte[] tag, String newValue) {
        StringBuilder tagStr = new StringBuilder(new String(tag, UTF_8));
        insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_TRANS_FROM, newValue);
        return tagStr.toString().getBytes(UTF_8);
    }

    public static byte[] updateSoftInfo(byte[] tag, SoftActionType action, Boolean isSoftActionRoot, User user,
                                        String originAttrValue, String moveId, TocItem tocItem) {
        if (originAttrValue == null) {
            return tag;
        }

        StringBuilder tagStr = new StringBuilder(new String(tag, UTF_8));
        updateSoftAction(tagStr, action);

        if (action != null) {
            switch (action) {
                case DELETE:
                    insertOrUpdateAttributeValue(tagStr, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(tagStr, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
                    removeAttribute(tagStr, LEOS_SOFT_MOVED_LABEL_ATTR);
                    removeAttribute(tagStr, LEOS_SOFT_MOVE_FROM);
                    removeAttribute(tagStr, LEOS_SOFT_MOVE_TO);
                    break;
                case MOVE_TO:
                    insertOrUpdateAttributeValue(tagStr, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(tagStr, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
                    insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_TO, moveId);
                    insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_FROM, null);
                    break;
                case MOVE_FROM:
                    insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_TO, null);
                    insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_FROM, moveId);
                    break;
                case UNDELETE:
                    restoreOldId(tagStr);
                    if (tocItem.getAknTag().value() != ARTICLE) {
                        insertOrUpdateAttributeValue(tagStr, LEOS_EDITABLE_ATTR, null);
                        insertOrUpdateAttributeValue(tagStr, LEOS_DELETABLE_ATTR, null);
                    }
                    if (tocItem.getAknTag().value() == CLAUSE) {
                        insertOrUpdateAttributeValue(tagStr, LEOS_EDITABLE_ATTR, true);
                        insertOrUpdateAttributeValue(tagStr, LEOS_DELETABLE_ATTR, true);
                    }
                    break;
                default:
                    insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVED_LABEL_ATTR, null);
                    insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_FROM, null);
                    insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_TO, null);
            }
            insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_USER_ATTR, user != null ? user.getLogin() : null);
            insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_DATE_ATTR, getDateAsXml());
        } else {
            insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_USER_ATTR, null);
            insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_DATE_ATTR, null);

            insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVED_LABEL_ATTR, null);
            insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_FROM, null);
            insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_MOVE_TO, null);
        }

        insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_ACTION_ROOT_ATTR, isSoftActionRoot);

        return tagStr.toString().getBytes(UTF_8);
    }

    protected static StringBuilder restoreOldId(StringBuilder tagStr) {
        int idAttrPos = tagStr.indexOf(XMLID + "=\"deleted_");
        if (idAttrPos != -1) {
            int idValStartPos = tagStr.indexOf("=", idAttrPos) + 2;
            int idValEndPos = tagStr.indexOf("\"", idValStartPos);
            String currentIdVal = tagStr.substring(idValStartPos, idValEndPos);
            String restoredIdVal = currentIdVal.replace("deleted_", EMPTY_STRING);
            tagStr.replace(idValStartPos, idValStartPos + currentIdVal.length(), restoredIdVal);
        }
        return tagStr;
    }

    private static void updateSoftAction(StringBuilder tagStr, SoftActionType action) {
        int softAttrPos = tagStr.indexOf(LEOS_SOFT_ACTION_ATTR);
        if (softAttrPos != -1) {
            int softAttrValStartPos = tagStr.indexOf("=", softAttrPos) + 2;
            int softAttrValEndPos = tagStr.indexOf("\"", softAttrValStartPos);
            if (action != null) {
                tagStr.replace(softAttrValStartPos, softAttrValEndPos, action.getSoftAction());
            } else {
                tagStr.replace(softAttrPos, softAttrValEndPos + 1, EMPTY_STRING);
            }
        } else {
            int position = tagStr.indexOf(">");
            if (action != null && position != -1) {
                tagStr.insert(position, insertAttribute(LEOS_SOFT_ACTION_ATTR, action.getSoftAction()));
            }
        }
    }

    public static String insertAttribute(String attrTag, Object attrVal) {
        return attrVal != null ? (" ").concat(attrTag).concat("=\"").concat(attrVal.toString()).concat("\"") : EMPTY_STRING;
    }

    public static StringBuilder insertOrUpdateAttributeValue(StringBuilder tagStr, String attrName, Object attrValue) {
        if (tagStr != null && attrName != null) {
            int attributePosition = tagStr.substring(0, tagStr.indexOf(">") + 1).indexOf(attrName);
            if (attributePosition != -1) {
                int attrValStartPos = tagStr.indexOf("=", attributePosition) + 2;
                int attrValEndPos = tagStr.indexOf("\"", attrValStartPos);
                if (attrName.equalsIgnoreCase(CLASS_ATTR)) {
                    tagStr.insert(attrValEndPos, " ".concat((String) attrValue), 0, ((String) attrValue).length() + 1);
                } else {
                    tagStr = attrValue != null ? tagStr.replace(attrValStartPos, attrValEndPos, attrValue.toString()) :
                            tagStr.replace(attributePosition, attrValEndPos + 1, EMPTY_STRING);
                }
            } else {
                int position = tagStr.indexOf(">");
                if (position >= 0) {
                    tagStr.insert(position, insertAttribute(attrName, attrValue));
                }
            }
        }
        return tagStr;
    }

    public static StringBuilder removeAttribute(StringBuilder tagStr, String leosAttr) {
        if (tagStr != null && leosAttr != null) {
            int editableAttrPos = tagStr.indexOf(leosAttr);
            if (editableAttrPos != -1) {
                int editableAttrValStartPos = tagStr.indexOf("=", editableAttrPos) + 2;
                int editableAttrValEndPos = tagStr.indexOf("\"", editableAttrValStartPos) + 1;
                tagStr.delete(editableAttrPos - 1, editableAttrValEndPos);
            }
        }
        return tagStr;
    }

    public static StringBuilder removeAttributeInTag(StringBuilder tagStr, String leosAttr) {
        if (tagStr != null && leosAttr != null) {
            int editableAttrPos = tagStr.indexOf(leosAttr);
            int endTagPosition = tagStr.indexOf(">");
            if (editableAttrPos != -1 && (editableAttrPos < endTagPosition)) {
                int editableAttrValStartPos = tagStr.indexOf("=", editableAttrPos) + 2;
                int editableAttrValEndPos = tagStr.indexOf("\"", editableAttrValStartPos) + 1;
                tagStr.delete(editableAttrPos - 1, editableAttrValEndPos);
            }
        }
        return tagStr;
    }

    public static String removeTag(String itemContent) {
        for (String element : ELEMENTS_TO_REMOVE_FROM_CONTENT) {
            itemContent = itemContent.replaceAll("<" + element + ".*?</" + element + ">", "");
        }
        itemContent = itemContent.replaceAll("<[^>]+>", "");
        return itemContent.replaceAll("\\s+", " ").trim();
    }

    public static String removeEnclosingTags(String nodeAsStr, String tagName) {
        nodeAsStr = nodeAsStr.replaceFirst("<" + tagName + "(.|\\S|\\n)*?>", ""); //first tag
        nodeAsStr = nodeAsStr.replaceFirst("<\\/\\S+?>$", ""); //last tag
        return nodeAsStr;
    }

    public static String removeEnclosingTags(String nodeAsStr) {
        Pattern p = Pattern.compile("^<[^>]+>(.*)</[^ ]+>$", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(nodeAsStr);
        while (m.find()) {
            nodeAsStr = m.group(1);
        }
        return nodeAsStr;
    }

    public static String trimmedXml(String str) {
//        final String WHITESPACE_REGEX = "(^( )*|( )*$)";
        return str.replaceAll("\\s+", " ").trim();
    }

    public static String addLeosNamespace(String str) {
        return str.replaceFirst(">", " xmlns:leos=\"urn:eu:europa:ec:leos\">")
                .replaceFirst(">", " xmlns=\"http://docs\\.oasis-open\\.org/legaldocml/ns/akn/3\\.0\">");
    }

    public static String removeAllNameSpaces(String str) {
        return str.replaceAll(" xmlns=\"http://docs\\.oasis-open\\.org/legaldocml/ns/akn/3\\.0\"", "")
                .replaceAll(" xmlns:leos=\"urn:eu:europa:ec:leos\"", "")
                .replaceAll(" xmlns:fmx=\"http://formex.*?xd\"", "")
                .replaceAll(" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"", "")
                .replaceAll(" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"", "")
                .replaceAll("<\\?xml version=\"1\\.0\" encoding=\"UTF-8\"\\?>", "");
    }

    /**
     * Escape the string from only characters interfering with Xerces parsing: "<", ">" and "&". The rest of special characters
     * are left in their UTF representation.
     * In case a full escaping is needed use StringEscapeUtils.escapeHtml()
     */
    public static String escapeXml(String str) {
        return str.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("&", "&amp;")
//                .replaceAll("'", "&apos;")
//                .replaceAll("\"", "&quot;")
                ;
    }

    public static String getOpeningTag(String attrName, String attrValue) {
        return "<span " + attrName + "=\"" + attrValue + "\">";
    }

    public static String getClosingTag() {
        return "</span>";
    }

    public static GregorianCalendar convertStringDateToCalendar(String strDate) {
        try {
            GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
            gregorianCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(strDate));
            return gregorianCalendar;
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getXMLFormatDate() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()).toXMLFormat();
        } catch (Exception ex) {
            return null;
        }
    }

    public static String findString(String nodeAsString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(nodeAsString);
        String foundString = null;
        if (matcher.find()) {
            foundString = matcher.group(0);
        }
        return foundString;
    }

    // LEOS-2639: replace XML self-closing tags not supported in HTML
    public static String removeSelfClosingElements(String fragment) {
        String removeSelfClosingRegex = "<([^>^\\s]+)([^>]*)/>";
        return fragment.replaceAll(removeSelfClosingRegex, "<$1$2></$1>");
    }

    public static String extractNumber(String numberStr) {
        if (numberStr != null) {
            return (numberStr.contains(WHITESPACE)) ?
                    numberStr.substring(numberStr.indexOf(WHITESPACE) + 1, numberStr.length()) : numberStr;
        }
        return null;
    }

    public static String wrapXPathWithQuotes(String value) {
        String wrappedValue = value;
        String apostrophe = "'";
        String quote = "\"";

        if (value.contains(quote)) {
            wrappedValue = apostrophe + value + apostrophe;
        } else {
            wrappedValue = quote + value + quote;
        }
        return wrappedValue;
    }

    public static ImmutableTriple<String, Integer, Integer> getSubstringAvoidingTags(String text, int txtStartOffset, int txtEndOffset) {
        int xmlStartIndex = -1;
        int textCounter = -1;
        boolean stopCounting = false;
        for (char c : text.toCharArray()) {
            if (textCounter == txtStartOffset) {
                break;
            }
            if (c == '<') {
                stopCounting = true;
            } else if (c == '>') {
                stopCounting = false;
            } else if (!stopCounting) {
                textCounter++;
            }
            if (c != '\n') { //Skipp counting the newLine as a character
                xmlStartIndex++;
            }
        }
        text = text.substring(xmlStartIndex);

        int xmlEndIndex = xmlStartIndex;
        int textCounterI = txtStartOffset;
        stopCounting = false;
        for (char c : text.toCharArray()) {
            if (textCounterI == txtEndOffset) {
                break;
            }
            if (c == '<') {
                stopCounting = true;
            } else if (c == '>') {
                stopCounting = false;
            } else if (!stopCounting) {
                textCounterI++;
            }
            xmlEndIndex++;
        }
        String matchingText = text.substring(0, xmlEndIndex - xmlStartIndex);
        return new ImmutableTriple<>(matchingText, xmlStartIndex, xmlEndIndex);
    }

    public static String normalizeNewText(String origText, String newText) {
        return new StringBuilder(origText.startsWith(WHITESPACE) ? WHITESPACE : EMPTY_STRING)
                .append(org.apache.commons.lang3.StringUtils.normalizeSpace(newText))
                .append(origText.endsWith(WHITESPACE) ? WHITESPACE : EMPTY_STRING).toString();
    }
}
