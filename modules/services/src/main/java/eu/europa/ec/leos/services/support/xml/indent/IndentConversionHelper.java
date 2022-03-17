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

import eu.europa.ec.leos.services.support.xml.XmlHelper;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.TocItemUtils;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.TRANSFORM;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftOrigin;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper.getTagValueFromTocItemVo;

@Component
public class IndentConversionHelper {
    private static final String INDENT_PLACEHOLDER_ID_PREFIX = "indented_";
    public static final String[] NUMBERED_ITEMS = {PARAGRAPH, POINT, INDENT};
    public static final String[] NUMBERED_AND_LEVEL_ITEMS = {PARAGRAPH, POINT, LEVEL, INDENT};
    public static final String[] UNUMBERED_ITEMS = {SUBPARAGRAPH, SUBPOINT};
    public static final String[] PARAGRAPH_LEVEL_ITEMS = {SUBPARAGRAPH, PARAGRAPH};

    public Pair<TableOfContentItemVO, Boolean> convertIndentedItem(List<TocItem> tocItems, TableOfContentItemVO originalItem
            , boolean isNumbered, IndentedItemType beforeIndentItemType, int originalIndentLevel, boolean isIndent) {
        IndentedItemType originalIndentItemType = originalItem.getIndentOriginType() == null ? beforeIndentItemType : originalItem.getIndentOriginType();
        IndentedItemType targetIndentItemType;

        if (getTagValueFromTocItemVo(originalItem).equals(LEVEL)) {
            return new Pair<>(originalItem, false);
        }

        int currentIndentLevel = getIndentedItemIndentLevel(originalItem);
        boolean paragraphIndentLevel = (!isIndent && !isNumbered && currentIndentLevel == 1)
                || (!isIndent && isNumbered && currentIndentLevel == 0)
                || (isIndent && !isNumbered && originalIndentLevel == 0);

        boolean restored = false;
        if (isNumbered) {
            // Should be converted to a point
            // Now we must know if it's a point-alinea or a single point

            if (originalItem.getChildItems().isEmpty()) {
                // For sure, convert it to a single point or single paragraph
                targetIndentItemType = paragraphIndentLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
            } else if ((originalItem.getChildItems().size() == 1) &&
                    (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPOINT) ||
                            beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH))) {
                // Should be converted to a single point
                targetIndentItemType = paragraphIndentLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
            } else {
                targetIndentItemType = paragraphIndentLevel ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT;
            }
        } else {
            targetIndentItemType = paragraphIndentLevel ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT;
        }

        if (targetIndentItemType.equals(beforeIndentItemType)) {
            return new Pair<>(originalItem, targetIndentItemType.equals(originalIndentItemType));
        }

        switch (targetIndentItemType) {
            case FIRST_SUBPOINT:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToFirstSubpoint(tocItems, originalItem, beforeIndentItemType);
                    restored = true;
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildFirstSubpointFromSubpoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildFirstSubpointFromSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildFirstSubpointFromFirstSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildFirstSubpointFromParagraph(tocItems, originalItem, originalIndentLevel);
                } else {
                    originalItem = buildFirstSubpointFromPoint(tocItems, originalItem, originalIndentLevel);
                }
                break;
            case OTHER_SUBPOINT:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToSubpoint(tocItems, originalItem, beforeIndentItemType);
                    restored = true;
                } else if (beforeIndentItemType.equals(IndentedItemType.POINT)) {
                    originalItem = buildSubpointFromPoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildSubpointFromSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildSubpointFromFirstSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildSubpointFromParagraph(tocItems, originalItem, originalIndentLevel);
                }  else {
                    originalItem = buildSubpointFromFirstSubpoint(originalItem, originalIndentLevel);
                }
                break;
            case POINT:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToPoint(tocItems, originalItem, beforeIndentItemType);
                    restored = true;
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildPointFromSubpoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildPointFromSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildPointFromFirstSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildPointFromParagraph(tocItems, originalItem, originalIndentLevel);
                } else {
                    originalItem = buildPointFromFirstSubpoint(originalItem, originalIndentLevel);
                }
                break;
            case FIRST_SUBPARAGRAPH:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToFirstSubparagraph(tocItems, originalItem, beforeIndentItemType);
                    restored = true;
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildFirstSubparagraphFromSubpoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildFirstSubparagraphFromSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPOINT)) {
                    originalItem = buildFirstSubparagraphFromFirstSubpoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildFirstSubparagraphFromParagraph(tocItems, originalItem, originalIndentLevel);
                } else {
                    originalItem = buildFirstSubparagraphFromPoint(tocItems, originalItem, originalIndentLevel);
                }
                break;
            case OTHER_SUBPARAGRAPH:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToSubparagraph(tocItems, originalItem, beforeIndentItemType);
                    restored = true;
                } else if (beforeIndentItemType.equals(IndentedItemType.POINT)) {
                    originalItem = buildSubparagraphFromPoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildSubparagraphFromSubpoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildSubparagraphFromFirstSubparagraph(originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.PARAGRAPH)) {
                    originalItem = buildSubparagraphFromParagraph(tocItems, originalItem, originalIndentLevel);
                }  else {
                    originalItem = buildSubparagraphFromFirstSubpoint(tocItems, originalItem, originalIndentLevel);
                }
                break;
            case PARAGRAPH:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToParagraph(tocItems, originalItem, beforeIndentItemType);
                    restored = true;
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPOINT)) {
                    originalItem = buildParagraphFromSubpoint(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    originalItem = buildParagraphFromSubparagraph(tocItems, originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
                    originalItem = buildParagraphFromFirstSubparagraph(originalItem, originalIndentLevel);
                } else if (beforeIndentItemType.equals(IndentedItemType.POINT)) {
                    originalItem = buildParagraphFromPoint(tocItems, originalItem, originalIndentLevel);
                } else {
                    originalItem = buildParagraphFromFirstSubpoint(tocItems, originalItem, originalIndentLevel);
                }
                break;
        }
        return new Pair<>(originalItem,restored);
    }

    public TableOfContentItemVO convertOtherItem(List<TocItem> tocItems, TableOfContentItemVO originalItem) {
        //Only possible conversions here:
        // 1. From point to first-subpoint
        // 2. From first-subpoint to point
        // 3. From paragraph to first-subparagraph
        // 2. From first-subparagraph to paragraph

        String tagName = getTagValueFromTocItemVo(originalItem);

        if ((tagName.equals(LIST) || tagName.equals(ARTICLE)) && !originalItem.getChildItemsView().isEmpty()) {
            return originalItem;
        } else if (tagName.equals(LIST) && originalItem.getChildItemsView().isEmpty()) {
            // It means that there is an empty list that should be removed
            originalItem.getParentItem().removeChildItem(originalItem);
            originalItem = originalItem.getParentItem();
            tagName = getTagValueFromTocItemVo(originalItem);
        }
        if (tagName.equals(LEVEL) ||tagName.equalsIgnoreCase(MAIN_BODY)) {
            return originalItem;
        }

        boolean paragraphLevel = ArrayUtils.contains(PARAGRAPH_LEVEL_ITEMS, tagName);
        int originalIndentLevel = (originalItem.isIndented()) ? originalItem.getIndentOriginIndentLevel() : getIndentedItemIndentLevel(originalItem);
        IndentedItemType beforeIndentItemType = originalItem.getContent().startsWith("<" + SUBPOINT) ? IndentedItemType.FIRST_SUBPOINT
                : originalItem.getContent().contains("<" + SUBPARAGRAPH) ? IndentedItemType.FIRST_SUBPARAGRAPH : paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
        IndentedItemType originalIndentItemType = originalItem.getIndentOriginType() == null ? beforeIndentItemType : originalItem.getIndentOriginType();
        IndentedItemType targetIndentItemType;

        if (originalItem.getChildItems().isEmpty()) {
            // For sure, convert it to a single point
            targetIndentItemType = paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
        } else if ((originalItem.getChildItems().size() == 1) &&
                (beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPOINT) || beforeIndentItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) ) {
            // Should be converted to a single point
            targetIndentItemType = paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;
        } else {
            targetIndentItemType = paragraphLevel ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT;
        }

        if (targetIndentItemType.equals(beforeIndentItemType)) {
            return originalItem;
        }

        switch (targetIndentItemType) {
            case FIRST_SUBPOINT:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToFirstSubpoint(tocItems, originalItem, beforeIndentItemType);
                    isRestored(originalItem, IndentedItemType.FIRST_SUBPOINT);
                } else {
                    originalItem = buildFirstSubpointFromPoint(tocItems, originalItem, originalIndentLevel);
                }
                break;
            case POINT:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToPoint(tocItems, originalItem, beforeIndentItemType);
                    isRestored(originalItem, IndentedItemType.POINT);
                } else {
                    originalItem = buildPointFromFirstSubpoint(originalItem, originalIndentLevel);
                }
                break;
            case FIRST_SUBPARAGRAPH:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToFirstSubparagraph(tocItems, originalItem, beforeIndentItemType);
                    isRestored(originalItem, IndentedItemType.FIRST_SUBPARAGRAPH);
                } else {
                    originalItem = buildFirstSubparagraphFromParagraph(tocItems, originalItem, originalIndentLevel);
                }
                break;
            case PARAGRAPH:
                if (originalIndentItemType.equals(targetIndentItemType)) {
                    originalItem = restoreToParagraph(tocItems, originalItem, beforeIndentItemType);
                    isRestored(originalItem, IndentedItemType.PARAGRAPH);
                } else {
                    originalItem = buildParagraphFromFirstSubparagraph(originalItem, originalIndentLevel);
                }
                break;
        }

        return originalItem;
    }

    boolean isRestored(TableOfContentItemVO item, IndentedItemType newIndentedItemType) {
        int newDepth = getIndentedItemIndentLevel(item);
        if (!item.isIndented()) {
            return false;
        }
        if (newDepth == item.getIndentOriginIndentLevel() && newIndentedItemType.equals(item.getIndentOriginType())) {
            item.setIndentOriginType(IndentedItemType.RESTORED);
            return true;
        }
        return false;
    }

    private TableOfContentItemVO restoreToSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, IndentedItemType beforeIndentItemType) {
        // Restore to its original state, thus, it has been transformed before, reset it
        if (hasTocItemSoftAction(originalItem, TRANSFORM)) {
            originalItem.setSoftActionAttr(null);
            originalItem.setSoftActionRoot(null);
        }

        TableOfContentItemVO firstSubpoint;
        TableOfContentItemVO firstSubparagraph;
        int originalPosition;
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
                // Original state was an subpoint, thus subpoint has become first child of point

                // If it has more than one child or does not contain the subpoint, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubpoint = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                        return originalItem;
                    }
                }

                // Ok, checking is done, remove the parent point
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
                // Parent should a point here no need to check if it's a list
                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.removeChildItem(firstSubpoint);
                originalItem.getParentItem().addChildItem(originalPosition, firstSubpoint);

                restoreMoveFromAttrs(originalItem, firstSubpoint);

                originalItem = firstSubpoint;

                break;
            case POINT:
                // Original state was a subpoint, thus subpoint was transformed to a point

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                originalItem.setTocItem(subpointTocItem);

                // Remove numbering
                originalItem.setNumber(null);
                originalItem.setOriginNumAttr(null);
                originalItem.setNumTagIndex(null);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                addSubpointContent(originalItem, originalItem);

                break;
            case OTHER_SUBPARAGRAPH:
                // Original state was a subpoint, thus subpoint was transformed to an other subparagraph

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                originalItem.setTocItem(subpointTocItem);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                removeSubparagraphContent(originalItem);
                addSubpointContent(originalItem, originalItem);

                break;
            case FIRST_SUBPARAGRAPH:
                // Original state was an subpoint, thus subpoint converted to a subparagraph has become first child of point

                // If it has more than one child or does not contain the subparagraph, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubparagraph = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                        return originalItem;
                    }
                }

                // Ok, checking is done, remove the parent point
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
                // Parent should a point here no need to check if it's a list
                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.removeChildItem(firstSubparagraph);
                originalItem.getParentItem().addChildItem(originalPosition, firstSubparagraph);

                // Convert subparagraph to subpoint
                firstSubparagraph.setTocItem(subpointTocItem);
                removeSubparagraphContent(firstSubparagraph);
                addSubpointContent(firstSubparagraph, firstSubparagraph);

                restoreMoveFromAttrs(originalItem, firstSubparagraph);

                originalItem = firstSubparagraph;

                break;
            case PARAGRAPH:
                // Original state was a subpoint, thus subpoint was transformed to a paragraph

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the paragraph
                originalItem.setTocItem(subpointTocItem);

                // Remove numbering
                originalItem.setNumber(null);
                originalItem.setOriginNumAttr(null);
                originalItem.setNumTagIndex(null);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                addSubpointContent(originalItem, originalItem);

                break;
        }
        return originalItem;
    }

    private TableOfContentItemVO restoreToPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, IndentedItemType beforeIndentItemType) {
        // Restore to its original state, thus, it has been transformed before, reset it
        if (hasTocItemSoftAction(originalItem, TRANSFORM)) {
            originalItem.setSoftActionAttr(null);
            originalItem.setSoftActionRoot(null);
        }
        TableOfContentItemVO firstSubpoint;
        TableOfContentItemVO firstSubparagraph;
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
                // Original state was a point, thus point has a subpoint as first child

                // If it has more than one child or does not contain the subpoint, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubpoint = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                        return originalItem;
                    }
                }

                removeSubpointContent(originalItem);
                removeTransformPrefix(originalItem);

                // Ok, checking is done, remove the first subpoint
                originalItem.removeChildItem(firstSubpoint);
                break;
            case OTHER_SUBPOINT:
                // Original state was a point, thus point was transformed to a subpoint

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                originalItem.setTocItem(pointTocItem);
                removeSubpointContent(originalItem);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                break;
            case FIRST_SUBPARAGRAPH:
                // Original state was a point
                // paragraph has a subparagraph as first child

                // If it has more than one child or does not contain the subparagraph, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubparagraph = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                        return originalItem;
                    }
                }

                removeSubparagraphContent(originalItem);
                removeTransformPrefix(originalItem);

                // Convert paragraph to point
                originalItem.setTocItem(pointTocItem);

                // Ok, checking is done, remove the first subparagraph
                originalItem.removeChildItem(firstSubparagraph);
                break;
            case OTHER_SUBPARAGRAPH:
                // Original state was a point, thus point was transformed to a subparagraph

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert to point
                originalItem.setTocItem(pointTocItem);
                removeSubparagraphContent(originalItem);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                break;
            case PARAGRAPH:
                // Original state was a point, thus point was transformed to a paragraph

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert to point
                originalItem.setTocItem(pointTocItem);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                break;
        }
        return originalItem;
    }

    private TableOfContentItemVO restoreToFirstSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, IndentedItemType beforeIndentItemType) {
        // Restore to its original state, thus, it has been transformed before, reset it
        if (hasTocItemSoftAction(originalItem, TRANSFORM)) {
            originalItem.setSoftActionAttr(null);
            originalItem.setSoftActionRoot(null);
        }
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        TableOfContentItemVO firstSubpoint;
        TableOfContentItemVO point;
        int originalPosition;

        switch (beforeIndentItemType) {
            case POINT:
                // Was a point, thus convert and restore to subpoint as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                firstSubpoint = buildTransItemFromItem(originalItem);
                firstSubpoint.setTocItem(subpointTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    firstSubpoint.setId(originalItem.getSoftTransFrom());
                    if (hasTocItemSoftAction(originalItem, MOVE_FROM)) {
                        firstSubpoint.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Remove numbering
                firstSubpoint.setNumber(null);
                firstSubpoint.setOriginNumAttr(null);
                firstSubpoint.setNumTagIndex(null);

                // Sets vtdindex to null
                firstSubpoint.setVtdIndex(null);

                // Add first child
                originalItem.addChildItem(0, firstSubpoint);

                addSubpointContent(originalItem, firstSubpoint);
                addSubpointContent(firstSubpoint, firstSubpoint);

                break;
            case OTHER_SUBPOINT:
                // Was a subpoint, thus restore the subpoint as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                point = buildTransItemFromItem(originalItem);
                removeTransformPrefix(point);
                if (hasTocItemSoftAction(point, TRANSFORM)) {
                    point.setSoftActionAttr(null);
                    point.setSoftActionRoot(null);
                }

                if (originalItem.isIndented()) {
                    point.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel(),
                            originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
                }
                point.setTocItem(pointTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    originalItem.setId(originalItem.getSoftTransFrom());
                    if (hasTocItemSoftAction(originalItem, MOVE_FROM)) {
                        originalItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Sets vtdindex to null
                point.setVtdIndex(null);

                // Add first child
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.getParentItem().addChildItem(originalPosition, point);
                point.addChildItem(0, originalItem);
                moveChildren(originalItem, point);

                addSubpointContent(originalItem, originalItem);
                addSubpointContent(point, originalItem);

                originalItem = point;

                break;
            case PARAGRAPH:
                // Was a paragraph, thus convert and restore to subparagraph converted to subpoint as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                firstSubpoint = buildTransItemFromItem(originalItem);
                firstSubpoint.setTocItem(subpointTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    firstSubpoint.setId(originalItem.getSoftTransFrom());
                    if (hasTocItemSoftAction(originalItem, MOVE_FROM)) {
                        firstSubpoint.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Remove numbering
                firstSubpoint.setNumber(null);
                firstSubpoint.setOriginNumAttr(null);
                firstSubpoint.setNumTagIndex(null);

                // Sets vtdindex to null
                firstSubpoint.setVtdIndex(null);

                // Add first child
                originalItem.addChildItem(0, firstSubpoint);

                //Convret originalItem to point
                originalItem.setTocItem(pointTocItem);

                addSubpointContent(originalItem, firstSubpoint);
                addSubpointContent(firstSubpoint, firstSubpoint);

                break;
            case OTHER_SUBPARAGRAPH:
                // Was a subpoint, thus restore the subparagraph converted to subpoint as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                point = buildTransItemFromItem(originalItem);
                removeTransformPrefix(point);
                if (hasTocItemSoftAction(point, TRANSFORM)) {
                    point.setSoftActionAttr(null);
                    point.setSoftActionRoot(null);
                }

                point.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel(),
                    originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
                point.setTocItem(pointTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    originalItem.setId(originalItem.getSoftTransFrom());
                    if (hasTocItemSoftAction(originalItem, MOVE_FROM)) {
                        originalItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Sets vtdindex to null
                point.setVtdIndex(null);

                // Add first child
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.getParentItem().addChildItem(originalPosition, point);
                point.addChildItem(0, originalItem);
                moveChildren(originalItem, point);
                removeSubparagraphContent(originalItem);

                addSubpointContent(originalItem, originalItem);
                addSubpointContent(point, originalItem);

                originalItem = point;

                break;
        }
        return originalItem;
    }

    private TableOfContentItemVO buildPointFromFirstSubpoint(TableOfContentItemVO originalItem, int originalIndentLevel) {
        TableOfContentItemVO firstSubpoint;

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubpoint = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                return originalItem;
            }
        }

        originalItem.populateIndentInfo(IndentedItemType.FIRST_SUBPOINT, originalIndentLevel
                , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());

        if (IndentedItemType.FIRST_SUBPOINT.equals(originalItem.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPARAGRAPH.equals(originalItem.getIndentOriginType())) {
            originalItem.setSoftTransFrom(firstSubpoint.getId());
            if (originalItem.getSoftActionAttr() == null && hasTocItemSoftOrigin(originalItem, EC)) {
                originalItem.setSoftActionAttr(TRANSFORM);
                originalItem.setSoftActionRoot(true);
            }
        }
        removeSubpointContent(originalItem);
        removeTransformPrefix(originalItem);

        // Ok, checking is done, remove the first subpoint
        originalItem.removeChildItem(firstSubpoint);
        return originalItem;
    }

    private TableOfContentItemVO buildSubpointFromFirstSubpoint(TableOfContentItemVO originalItem, int originalIndentLevel) {
        TableOfContentItemVO firstSubpoint;

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubpoint = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                return originalItem;
            }
        }

        if (hasTocItemSoftAction(originalItem, MOVE_FROM) && originalItem.isSoftActionRoot()) {
            firstSubpoint.setSoftActionRoot(true);
        }

        if (originalItem.isIndented()) {
            firstSubpoint.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        } else if (!firstSubpoint.isIndented()) {
            firstSubpoint.populateIndentInfo(IndentedItemType.FIRST_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(firstSubpoint);

        if (IndentedItemType.FIRST_SUBPOINT.equals(firstSubpoint.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPARAGRAPH.equals(firstSubpoint.getIndentOriginType())) {
            firstSubpoint.setSoftTransFrom(firstSubpoint.getId());
            removeTransformPrefix(originalItem);
            firstSubpoint.setId(originalItem.getId());
            if (firstSubpoint.getSoftActionAttr() == null && hasTocItemSoftOrigin(firstSubpoint, EC)) {
                firstSubpoint.setSoftActionAttr(TRANSFORM);
                firstSubpoint.setSoftActionRoot(true);
            }
        }

        // Ok, checking is done, remove the parent point
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
        // Parent should be a point here no need to check if it's a list
        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.removeChildItem(firstSubpoint);
        originalItem.getParentItem().addChildItem(originalPosition, firstSubpoint);
        originalItem = firstSubpoint;

        return originalItem;
    }

    private TableOfContentItemVO buildSubpointFromPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        originalItem.populateIndentInfo(IndentedItemType.POINT, originalIndentLevel
              , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());

        // Ok, checking is done, convert the point
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        originalItem.setTocItem(subpointTocItem);

        // Remove numbering
        originalItem.setNumber(null);
        originalItem.setOriginNumAttr(null);
        originalItem.setNumTagIndex(null);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);

        addSubpointContent(originalItem, originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubpointFromPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }
        originalItem.populateIndentInfo(IndentedItemType.POINT, originalIndentLevel
                , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());

        // Ok, checking is done, convert the point
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        TableOfContentItemVO firstSubpoint = buildTransItemFromItem(originalItem);

        if (originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPARAGRAPH) && originalItem.getSoftTransFrom() != null) {
            removeTransformPrefix(originalItem);
            firstSubpoint.setId(originalItem.getSoftTransFrom());
        } else {
            removeTransformPrefix(firstSubpoint);
            addTransformPrefix(originalItem);
        }

        firstSubpoint.setTocItem(subpointTocItem);

        // Remove numbering
        firstSubpoint.setNumber(null);
        firstSubpoint.setOriginNumAttr(null);
        firstSubpoint.setNumTagIndex(null);

        // Sets vtdindex to null
        firstSubpoint.setVtdIndex(null);

        // Add first child
        originalItem.addChildItem(0, firstSubpoint);

        addSubpointContent(originalItem, firstSubpoint);
        addSubpointContent(firstSubpoint, firstSubpoint);

        return originalItem;
    }

    Pair<TableOfContentItemVO, Boolean> forceBuildFirstSubpointFromPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        if (getTagValueFromTocItemVo(originalItem).equals(LEVEL)) {
            return new Pair<>(originalItem, false);
        }

        if (originalItem.isIndented() && originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPOINT)) {
            originalItem = restoreToFirstSubpoint(tocItems, originalItem, IndentedItemType.POINT);
            return new Pair<>(originalItem, true);
        }

        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        TableOfContentItemVO firstSubpoint = buildTransItemFromItem(originalItem);

        if (originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPARAGRAPH) && originalItem.getSoftTransFrom() != null) {
            removeTransformPrefix(originalItem);
            firstSubpoint.setId(originalItem.getSoftTransFrom());
        } else {
            removeTransformPrefix(firstSubpoint);
            addTransformPrefix(originalItem);
        }

        firstSubpoint.setTocItem(subpointTocItem);

        // Remove numbering
        firstSubpoint.setNumber(null);
        firstSubpoint.setOriginNumAttr(null);
        firstSubpoint.setNumTagIndex(null);

        // Sets vtdindex to null
        firstSubpoint.setVtdIndex(null);

        // Add first child
        originalItem.addChildItem(0, firstSubpoint);

        addSubpointContent(originalItem, firstSubpoint);
        addSubpointContent(firstSubpoint, firstSubpoint);

        return new Pair<>(originalItem, true);
    }

    Pair<TableOfContentItemVO, Boolean> forceBuildFirstSubparagraphFromParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        removeParagraphContent(originalItem);
        if (getTagValueFromTocItemVo(originalItem).equals(LEVEL)) {
            return new Pair<>(originalItem, false);
        }

        if (originalItem.isIndented() && originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPARAGRAPH)) {
        originalItem = restoreToFirstSubparagraph(tocItems, originalItem, IndentedItemType.PARAGRAPH);
            return new Pair<>(originalItem, true);
        }

        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        TableOfContentItemVO firstSubparagraph = buildTransItemFromItem(originalItem);

        if (originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPOINT) && originalItem.getSoftTransFrom() != null) {
            removeTransformPrefix(originalItem);
            firstSubparagraph.setId(originalItem.getSoftTransFrom());
        } else {
            removeTransformPrefix(firstSubparagraph);
            addTransformPrefix(originalItem);
        }

        firstSubparagraph.setTocItem(subparagraphTocItem);

        // Remove numbering
        firstSubparagraph.setNumber(null);
        firstSubparagraph.setOriginNumAttr(null);
        firstSubparagraph.setNumTagIndex(null);

        // Sets vtdindex to null
        firstSubparagraph.setVtdIndex(null);

        // Add first child
        originalItem.addChildItem(0, firstSubparagraph);

        addSubparagraphContent(originalItem, firstSubparagraph);
        addSubparagraphContent(firstSubparagraph, firstSubparagraph);

        return new Pair<>(originalItem, true);
    }

    private TableOfContentItemVO buildFirstSubpointFromSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        // Ok, checking is done, convert the subpoint
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        TableOfContentItemVO point = buildTransItemFromItem(originalItem);
        point.setTocItem(pointTocItem);

        if (!originalItem.isIndented()) {
            point.populateIndentInfo(IndentedItemType.OTHER_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        } else if (originalItem.isIndented()) {
            point.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        }

        // Sets vtdindex to null
        point.setVtdIndex(null);

        // Add first child
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.getParentItem().addChildItem(originalPosition, point);
        point.addChildItem(0, originalItem);
        moveChildren(originalItem, point);

        removeTransformPrefix(originalItem);

        addSubpointContent(originalItem, originalItem);
        addSubpointContent(point, originalItem);

        originalItem = point;

        return originalItem;
    }

    private TableOfContentItemVO buildPointFromSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        originalItem.populateIndentInfo(IndentedItemType.OTHER_SUBPOINT, originalIndentLevel
                , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());

        // Ok, checking is done, convert the point
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        originalItem.setTocItem(pointTocItem);

        removeSubpointContent(originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildParagraphFromSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.OTHER_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert to paragraph
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        originalItem.setTocItem(paragraphTocItem);

        removeSubpointContent(originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildParagraphFromFirstSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        TableOfContentItemVO firstSubpoint;
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubpoint = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                return originalItem;
            }
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.FIRST_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        if (originalItem.getIndentOriginType().equals(IndentedItemType.POINT)) {
            removeTransformPrefix(originalItem);
        }

        if (IndentedItemType.FIRST_SUBPOINT.equals(originalItem.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPARAGRAPH.equals(originalItem.getIndentOriginType())) {
            originalItem.setSoftTransFrom(firstSubpoint.getId());
            if (originalItem.getSoftActionAttr() == null && hasTocItemSoftOrigin(originalItem, EC)) {
                originalItem.setSoftActionAttr(TRANSFORM);
                originalItem.setSoftActionRoot(true);
            }
        }
        removeSubpointContent(originalItem);

        //Convert point to paragraph
        originalItem.setTocItem(paragraphTocItem);

        // Ok, checking is done, remove the first subpoint
        originalItem.removeChildItem(firstSubpoint);
        return originalItem;
    }

    private TableOfContentItemVO buildParagraphFromPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.POINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert to paragraph
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        originalItem.setTocItem(paragraphTocItem);

        return originalItem;
    }

    private TableOfContentItemVO buildSubparagraphFromPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.POINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the paragraph
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        originalItem.setTocItem(subparagraphTocItem);

        // Remove numbering
        originalItem.setNumber(null);
        originalItem.setOriginNumAttr(null);
        originalItem.setNumTagIndex(null);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);

        addSubparagraphContent(originalItem, originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildSubparagraphFromSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.OTHER_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the paragraph
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        originalItem.setTocItem(subparagraphTocItem);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);

        removeSubpointContent(originalItem);
        addSubparagraphContent(originalItem, originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildSubparagraphFromFirstSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        TableOfContentItemVO firstSubpoint;

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubpoint = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                return originalItem;
            }
        }

        if (hasTocItemSoftAction(originalItem, MOVE_FROM) && originalItem.isSoftActionRoot()) {
            firstSubpoint.setSoftActionRoot(true);
        }

        if (originalItem.isIndented()) {
            firstSubpoint.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        } else if (!firstSubpoint.isIndented()) {
            firstSubpoint.populateIndentInfo(IndentedItemType.FIRST_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(firstSubpoint);

        if (IndentedItemType.FIRST_SUBPOINT.equals(firstSubpoint.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPARAGRAPH.equals(firstSubpoint.getIndentOriginType())) {
            firstSubpoint.setSoftTransFrom(firstSubpoint.getId());
            removeTransformPrefix(originalItem);
            firstSubpoint.setId(originalItem.getId());
            if (firstSubpoint.getSoftActionAttr() == null && hasTocItemSoftOrigin(firstSubpoint, EC)) {
                firstSubpoint.setSoftActionAttr(TRANSFORM);
                firstSubpoint.setSoftActionRoot(true);
            }
        }

        // Ok, checking is done, remove the parent paragraph
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
        // Parent should be a paragraph here no need to check if it's a list
        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.removeChildItem(firstSubpoint);
        originalItem.getParentItem().addChildItem(originalPosition, firstSubpoint);

        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        firstSubpoint.setTocItem(subparagraphTocItem);

        removeSubpointContent(firstSubpoint);
        addSubparagraphContent(firstSubpoint, firstSubpoint);

        originalItem = firstSubpoint;

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubparagraphFromPoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.POINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the point
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        TableOfContentItemVO firstSubparagraph = buildTransItemFromItem(originalItem);

        if (originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPOINT) && originalItem.getSoftTransFrom() != null) {
            removeTransformPrefix(originalItem);
            firstSubparagraph.setId(originalItem.getSoftTransFrom());
        } else {
            removeTransformPrefix(firstSubparagraph);
            addTransformPrefix(originalItem);
        }

        firstSubparagraph.setTocItem(subparagraphTocItem);

        // Remove numbering
        firstSubparagraph.setNumber(null);
        firstSubparagraph.setOriginNumAttr(null);
        firstSubparagraph.setNumTagIndex(null);

        // Sets vtdindex to null
        firstSubparagraph.setVtdIndex(null);

        // Add first child
        originalItem.addChildItem(0, firstSubparagraph);

        originalItem.setTocItem(paragraphTocItem);

        addSubparagraphContent(originalItem, firstSubparagraph);
        addSubparagraphContent(firstSubparagraph, firstSubparagraph);

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubparagraphFromSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        // Ok, checking is done, convert the subpoint
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        TableOfContentItemVO paragraph = buildTransItemFromItem(originalItem);
        paragraph.setTocItem(paragraphTocItem);

        if (!originalItem.isIndented()) {
            paragraph.populateIndentInfo(IndentedItemType.OTHER_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        } else if (originalItem.isIndented()) {
            paragraph.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        }

        // Sets vtdindex to null
        paragraph.setVtdIndex(null);

        // Add first child
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.getParentItem().addChildItem(originalPosition, paragraph);
        paragraph.addChildItem(0, originalItem);
        moveChildren(originalItem, paragraph);

        removeTransformPrefix(originalItem);

        originalItem.setTocItem(subparagraphTocItem);
        removeSubpointContent(originalItem);

        addSubparagraphContent(originalItem, originalItem);
        addSubparagraphContent(paragraph, originalItem);

        originalItem = paragraph;

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubparagraphFromFirstSubpoint(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        TableOfContentItemVO firstSubpoint;
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else {
            firstSubpoint = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                return originalItem;
            }
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.FIRST_SUBPOINT, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        removeSubpointContent(originalItem);
        removeSubpointContent(firstSubpoint);

        addSubparagraphContent(originalItem, firstSubpoint);
        addSubparagraphContent(firstSubpoint, firstSubpoint);

        //Convert point to paragraph
        originalItem.setTocItem(paragraphTocItem);
        firstSubpoint.setTocItem(subparagraphTocItem);

        return originalItem;
    }

    private TableOfContentItemVO buildPointFromSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.OTHER_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert to point
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        originalItem.setTocItem(pointTocItem);

        removeSubparagraphContent(originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildPointFromFirstSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        removeParagraphContent(originalItem);
        TableOfContentItemVO firstSubparagraph;
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubparagraph = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.FIRST_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        removeTransformPrefix(originalItem);
        if (IndentedItemType.FIRST_SUBPOINT.equals(originalItem.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPARAGRAPH.equals(originalItem.getIndentOriginType())) {
            originalItem.setSoftTransFrom(firstSubparagraph.getId());
            if (originalItem.getSoftActionAttr() == null && hasTocItemSoftOrigin(originalItem, EC)) {
                originalItem.setSoftActionAttr(TRANSFORM);
                originalItem.setSoftActionRoot(true);
            }
        }
        removeSubparagraphContent(originalItem);

        //Convert paragraph to point
        originalItem.setTocItem(pointTocItem);

        // Ok, checking is done, remove the first subparagraph
        originalItem.removeChildItem(firstSubparagraph);
        return originalItem;
    }

    private TableOfContentItemVO buildPointFromParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        removeParagraphContent(originalItem);
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.PARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert to point
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        originalItem.setTocItem(pointTocItem);

        return originalItem;
    }

    private TableOfContentItemVO buildSubpointFromParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        removeParagraphContent(originalItem);
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.PARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the paragraph
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        originalItem.setTocItem(subpointTocItem);

        // Remove numbering
        originalItem.setNumber(null);
        originalItem.setOriginNumAttr(null);
        originalItem.setNumTagIndex(null);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);

        addSubpointContent(originalItem, originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildSubpointFromSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.OTHER_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the paragraph
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        originalItem.setTocItem(subpointTocItem);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);

        removeSubparagraphContent(originalItem);
        addSubpointContent(originalItem, originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildSubpointFromFirstSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        removeParagraphContent(originalItem);
        TableOfContentItemVO firstSubparagraph;

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubparagraph = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        if (hasTocItemSoftAction(originalItem, MOVE_FROM) && originalItem.isSoftActionRoot()) {
            firstSubparagraph.setSoftActionRoot(true);
        }

        if (originalItem.isIndented()) {
            firstSubparagraph.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        } else if (!firstSubparagraph.isIndented()) {
            firstSubparagraph.populateIndentInfo(IndentedItemType.FIRST_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(firstSubparagraph);

        if (IndentedItemType.FIRST_SUBPOINT.equals(firstSubparagraph.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPARAGRAPH.equals(firstSubparagraph.getIndentOriginType())) {
            firstSubparagraph.setSoftTransFrom(firstSubparagraph.getId());
            removeTransformPrefix(originalItem);
            firstSubparagraph.setId(originalItem.getId());
            if (firstSubparagraph.getSoftActionAttr() == null && hasTocItemSoftOrigin(firstSubparagraph, EC)) {
                firstSubparagraph.setSoftActionAttr(TRANSFORM);
                firstSubparagraph.setSoftActionRoot(true);
            }
        }

        // Ok, checking is done, remove the parent paragraph
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
        // Parent should be a paragraph here no need to check if it's a list
        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.removeChildItem(firstSubparagraph);
        originalItem.getParentItem().addChildItem(originalPosition, firstSubparagraph);

        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        firstSubparagraph.setTocItem(subpointTocItem);

        removeSubparagraphContent(firstSubparagraph);
        addSubpointContent(firstSubparagraph, firstSubparagraph);

        originalItem = firstSubparagraph;

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubpointFromParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        removeParagraphContent(originalItem);
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.PARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the point
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        TableOfContentItemVO firstSubpoint = buildTransItemFromItem(originalItem);

        if (originalItem.getIndentOriginType().equals(IndentedItemType.FIRST_SUBPARAGRAPH) && originalItem.getSoftTransFrom() != null) {
            removeTransformPrefix(originalItem);
            firstSubpoint.setId(originalItem.getSoftTransFrom());
        } else {
            removeTransformPrefix(firstSubpoint);
            addTransformPrefix(originalItem);
        }

        firstSubpoint.setTocItem(subpointTocItem);

        // Remove numbering
        firstSubpoint.setNumber(null);
        firstSubpoint.setOriginNumAttr(null);
        firstSubpoint.setNumTagIndex(null);

        // Sets vtdindex to null
        firstSubpoint.setVtdIndex(null);

        // Add first child
        originalItem.addChildItem(0, firstSubpoint);

        originalItem.setTocItem(pointTocItem);

        addSubparagraphContent(originalItem, firstSubpoint);
        addSubparagraphContent(firstSubpoint, firstSubpoint);

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubpointFromSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        // Ok, checking is done, convert the subpoint
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);
        TableOfContentItemVO point = buildTransItemFromItem(originalItem);
        point.setTocItem(pointTocItem);

        if (!originalItem.isIndented()) {
            point.populateIndentInfo(IndentedItemType.OTHER_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        } else if (originalItem.isIndented()) {
            point.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        }

        // Sets vtdindex to null
        point.setVtdIndex(null);

        // Add first child
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.getParentItem().addChildItem(originalPosition, point);
        point.addChildItem(0, originalItem);
        moveChildren(originalItem, point);

        removeTransformPrefix(originalItem);

        originalItem.setTocItem(subpointTocItem);
        removeSubparagraphContent(originalItem);

        addSubpointContent(originalItem, originalItem);
        addSubpointContent(point, originalItem);

        originalItem = point;

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubpointFromFirstSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        removeParagraphContent(originalItem);
        TableOfContentItemVO firstSubparagraph;
        TocItem pointTocItem = TocItemUtils.getTocItemByName(tocItems, POINT);
        TocItem subpointTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPOINT);

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else {
            firstSubparagraph = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.FIRST_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        removeSubparagraphContent(originalItem);
        removeSubparagraphContent(firstSubparagraph);

        addSubpointContent(originalItem, firstSubparagraph);
        addSubpointContent(firstSubparagraph, firstSubparagraph);

        //Convert point to paragraph
        originalItem.setTocItem(pointTocItem);
        firstSubparagraph.setTocItem(subpointTocItem);

        return originalItem;
    }

    private TableOfContentItemVO restoreToSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, IndentedItemType beforeIndentItemType) {
        // Restore to its original state, thus, it has been transformed before, reset it
        if (hasTocItemSoftAction(originalItem, TRANSFORM)) {
            originalItem.setSoftActionAttr(null);
            originalItem.setSoftActionRoot(null);
        }
        TableOfContentItemVO firstSubpoint;
        TableOfContentItemVO firstSubparagraph;
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        int originalPosition;

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
                // Original state was an subparagraph,
                // thus subparagraph converted to a subpoint has become first child of point

                // If it has more than one child or does not contain the subpoint, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubpoint = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                        return originalItem;
                    }
                }

                // Ok, checking is done, remove the parent point
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
                // Parent should a point here no need to check if it's a list
                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.removeChildItem(firstSubpoint);
                originalItem.getParentItem().addChildItem(originalPosition, firstSubpoint);

                // Convert subpoint to subparagraph
                firstSubpoint.setTocItem(subparagraphTocItem);
                removeSubpointContent(firstSubpoint);
                addSubparagraphContent(firstSubpoint, firstSubpoint);

                restoreMoveFromAttrs(originalItem, firstSubpoint);

                originalItem = firstSubpoint;

                break;
            case POINT:
                // Original state was a subparagraph, thus subparagraph was transformed to a point

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the paragraph
                originalItem.setTocItem(subparagraphTocItem);

                // Remove numbering
                originalItem.setNumber(null);
                originalItem.setOriginNumAttr(null);
                originalItem.setNumTagIndex(null);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                addSubparagraphContent(originalItem, originalItem);

                break;
            case OTHER_SUBPOINT:
                // Original state was a subparagraph, thus subparagraph was transformed to a subpoint

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the paragraph
                originalItem.setTocItem(subparagraphTocItem);

                // Remove numbering
                originalItem.setNumber(null);
                originalItem.setOriginNumAttr(null);
                originalItem.setNumTagIndex(null);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                removeSubpointContent(originalItem);
                addSubparagraphContent(originalItem, originalItem);

                break;
            case FIRST_SUBPARAGRAPH:
                // Original state was an subparagraph, thus subparagraph has become first child of paragraph

                // If it has more than one child or does not contain the subparagraph, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else {
                    firstSubparagraph = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                        return originalItem;
                    }
                }

                // Ok, checking is done, remove the parent point
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
                // Parent should a point here no need to check if it's a list
                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.removeChildItem(firstSubparagraph);
                originalItem.getParentItem().addChildItem(originalPosition, firstSubparagraph);

                restoreMoveFromAttrs(originalItem, firstSubparagraph);

                originalItem = firstSubparagraph;

                break;
            case PARAGRAPH:
                // Original state was a subparagraph, thus subparagraph was transformed to a paragraph

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the paragraph
                originalItem.setTocItem(subparagraphTocItem);

                // Remove numbering
                originalItem.setNumber(null);
                originalItem.setOriginNumAttr(null);
                originalItem.setNumTagIndex(null);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                addSubparagraphContent(originalItem, originalItem);

                break;
        }
        return originalItem;
    }

    private void restoreMoveFromAttrs(TableOfContentItemVO sourceItem, TableOfContentItemVO destItem) {
        if (hasTocItemSoftAction(sourceItem, MOVE_FROM) && destItem.getSoftActionAttr() == null) {
            destItem.setSoftActionAttr(sourceItem.getSoftActionAttr());
            destItem.setSoftMoveFrom(sourceItem.getSoftMoveFrom());
            destItem.setSoftActionRoot(sourceItem.isSoftActionRoot());
            destItem.setSoftDateAttr(sourceItem.getSoftDateAttr());
            destItem.setSoftUserAttr(sourceItem.getSoftUserAttr());
        }
    }


    private TableOfContentItemVO restoreToParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, IndentedItemType beforeIndentItemType) {
        // Restore to its original state, thus, it has been transformed before, reset it
        if (hasTocItemSoftAction(originalItem, TRANSFORM)) {
            originalItem.setSoftActionAttr(null);
            originalItem.setSoftActionRoot(null);
        }
        TableOfContentItemVO firstSubpoint;
        TableOfContentItemVO firstSubparagraph;
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);

        switch (beforeIndentItemType) {
            case FIRST_SUBPOINT:
                // Original state was a paragraph,
                // point has a subpoint as first child

                // If it has more than one child or does not contain the subpoint, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubpoint = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                        return originalItem;
                    }
                }

                removeSubpointContent(originalItem);
                removeTransformPrefix(originalItem);

                // Convert point to paragraph
                originalItem.setTocItem(paragraphTocItem);

                // Ok, checking is done, remove the first subpoint
                originalItem.removeChildItem(firstSubpoint);
                break;
            case OTHER_SUBPOINT:
                // Original state was a paragraph, thus paragraph was transformed to a subpoint

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                originalItem.setTocItem(paragraphTocItem);
                removeSubpointContent(originalItem);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                break;
            case POINT:
                // Original state was a paragraph, thus paragraph was transformed to a point

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                originalItem.setTocItem(paragraphTocItem);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                break;
            case FIRST_SUBPARAGRAPH:
                // Original state was a paragraph, thus paragraph has a subparagraph as first child

                // If it has more than one child or does not contain the subparagraph, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubparagraph = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                        return originalItem;
                    }
                }

                removeSubparagraphContent(originalItem);
                removeTransformPrefix(originalItem);

                // Ok, checking is done, remove the first subpoint
                originalItem.removeChildItem(firstSubparagraph);
                break;
            case OTHER_SUBPARAGRAPH:
                // Original state was a paragraph, thus paragraph was transformed to a subparagraph

                // If it has children, conversion is not possible
                if (!originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the paragraph
                originalItem.setTocItem(paragraphTocItem);
                removeSubparagraphContent(originalItem);

                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                break;
        }
        return originalItem;
    }

    private TableOfContentItemVO restoreToFirstSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, IndentedItemType beforeIndentItemType) {
        // Restore to its original state, thus, it has been transformed before, reset it
        if (hasTocItemSoftAction(originalItem, TRANSFORM)) {
            originalItem.setSoftActionAttr(null);
            originalItem.setSoftActionRoot(null);
        }
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        TableOfContentItemVO firstSubparagraph;
        TableOfContentItemVO paragraph;
        int originalPosition = 0;

        switch (beforeIndentItemType) {
            case POINT:
                // Was a point, thus convert and restore to subpoint as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                firstSubparagraph = buildTransItemFromItem(originalItem);
                firstSubparagraph.setTocItem(subparagraphTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    firstSubparagraph.setId(originalItem.getSoftTransFrom());
                    if (hasTocItemSoftAction(originalItem, MOVE_FROM)) {
                        firstSubparagraph.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Remove numbering
                firstSubparagraph.setNumber(null);
                firstSubparagraph.setOriginNumAttr(null);
                firstSubparagraph.setNumTagIndex(null);

                // Sets vtdindex to null
                firstSubparagraph.setVtdIndex(null);

                // Add first child
                originalItem.addChildItem(0, firstSubparagraph);

                // Convert point to paragraph
                originalItem.setTocItem(paragraphTocItem);

                addSubparagraphContent(originalItem, firstSubparagraph);
                addSubparagraphContent(firstSubparagraph, firstSubparagraph);

                break;
            case FIRST_SUBPOINT:
                // Original state was an first subparagraph,
                // thus subparagraph converted to a subpoint has become first child of point

                // If it has more than one child or does not contain the subpoint, conversion is not possible
                TableOfContentItemVO firstSubpoint;
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                } else if (originalItem.getChildItems().size() > 1) {
                    return originalItem;
                } else {
                    firstSubpoint = originalItem.getChildItems().get(0);
                    if (!getTagValueFromTocItemVo(firstSubpoint).equals(SUBPOINT)) {
                        return originalItem;
                    }
                }

                // Convert subpoint to subparagraph
                originalItem.setTocItem(paragraphTocItem);
                firstSubpoint.setTocItem(subparagraphTocItem);
                removeSubpointContent(firstSubpoint);
                addSubparagraphContent(firstSubpoint, firstSubpoint);

                break;
            case OTHER_SUBPOINT:
                // Was a subpoint, thus restore the subpoint and convert it to a subparagraph as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);

                paragraph = buildTransItemFromItem(originalItem);
                removeTransformPrefix(paragraph);
                if (hasTocItemSoftAction(paragraph, TRANSFORM)) {
                    paragraph.setSoftActionAttr(null);
                    paragraph.setSoftActionRoot(null);
                }

                if (originalItem.isIndented()) {
                    paragraph.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel(),
                            originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
                }
                paragraph.setTocItem(paragraphTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    originalItem.setId(originalItem.getSoftTransFrom());
                    if (hasTocItemSoftAction(originalItem, MOVE_FROM)) {
                        originalItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Sets vtdindex to null
                paragraph.setVtdIndex(null);

                // Add first child
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.getParentItem().addChildItem(originalPosition, paragraph);
                paragraph.addChildItem(0, originalItem);
                moveChildren(originalItem, paragraph);

                // Convert subpoint to subparagraph
                originalItem.setTocItem(subparagraphTocItem);
                removeSubpointContent(originalItem);

                addSubparagraphContent(originalItem, originalItem);
                addSubparagraphContent(paragraph, originalItem);

                originalItem = paragraph;

                break;
            case PARAGRAPH:
                // Was a paragraph, thus convert and restore to subparagraph as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the point
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                firstSubparagraph = buildTransItemFromItem(originalItem);
                firstSubparagraph.setTocItem(subparagraphTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    firstSubparagraph.setId(originalItem.getSoftTransFrom());
                    firstSubparagraph.setSoftTransFrom(null);
                    firstSubparagraph.setSoftActionRoot(false);
                    if (hasTocItemSoftAction(firstSubparagraph, MOVE_FROM)) {
                        firstSubparagraph.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Remove numbering
                firstSubparagraph.setNumber(null);
                firstSubparagraph.setOriginNumAttr(null);
                firstSubparagraph.setNumTagIndex(null);

                // Sets vtdindex to null
                firstSubparagraph.setVtdIndex(null);

                // Add first child
                originalItem.addChildItem(0, firstSubparagraph);

                addSubparagraphContent(originalItem, firstSubparagraph);
                addSubparagraphContent(firstSubparagraph, firstSubparagraph);

                break;
            case OTHER_SUBPARAGRAPH:
                // Was a subparagraph, thus restore the subparagraph as first child

                // If it has no children, conversion is not possible
                if (originalItem.getChildItems().isEmpty()) {
                    return originalItem;
                }

                // Ok, checking is done, convert the paragraph
                // Remove "transformed" prefix
                removeTransformPrefix(originalItem);
                paragraph = buildTransItemFromItem(originalItem);
                removeTransformPrefix(paragraph);
                if (hasTocItemSoftAction(paragraph, TRANSFORM)) {
                    paragraph.setSoftActionAttr(null);
                    paragraph.setSoftActionRoot(null);
                }

                if (originalItem.isIndented()) {
                    paragraph.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel(),
                            originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
                }
                paragraph.setTocItem(paragraphTocItem);
                if (originalItem.getSoftTransFrom() != null) {
                    originalItem.setId(originalItem.getSoftTransFrom());
                    if (hasTocItemSoftAction(originalItem, MOVE_FROM)) {
                        originalItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getSoftTransFrom());
                    }
                }

                // Sets vtdindex to null
                paragraph.setVtdIndex(null);

                // Add first child
                originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

                originalItem.getParentItem().removeChildItem(originalItem);
                originalItem.getParentItem().addChildItem(originalPosition, paragraph);
                paragraph.addChildItem(0, originalItem);
                moveChildren(originalItem, paragraph);

                addSubparagraphContent(originalItem, originalItem);
                addSubparagraphContent(paragraph, originalItem);

                originalItem = paragraph;

                break;
        }
        return originalItem;
    }

    private TableOfContentItemVO buildParagraphFromFirstSubparagraph(TableOfContentItemVO originalItem, int originalIndentLevel) {
        removeParagraphContent(originalItem);
        TableOfContentItemVO firstSubparagraph;

        // If it has more than one child or does not contain the subparagraph, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubparagraph = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.FIRST_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        if (IndentedItemType.FIRST_SUBPARAGRAPH.equals(originalItem.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPOINT.equals(originalItem.getIndentOriginType())) {
            originalItem.setSoftTransFrom(firstSubparagraph.getId());
            if (originalItem.getSoftActionAttr() == null && hasTocItemSoftOrigin(originalItem, EC)) {
                originalItem.setSoftActionAttr(TRANSFORM);
                originalItem.setSoftActionRoot(true);
            }
        }
        removeSubparagraphContent(originalItem);

        // Ok, checking is done, remove the first subparagraph
        originalItem.removeChildItem(firstSubparagraph);
        return originalItem;
    }

    private TableOfContentItemVO buildSubparagraphFromFirstSubparagraph(TableOfContentItemVO originalItem, int originalIndentLevel) {
        removeParagraphContent(originalItem);
        TableOfContentItemVO firstSubparagraph;

        // If it has more than one child or does not contain the subpoint, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        } else if (originalItem.getChildItems().size() > 1) {
            return originalItem;
        } else {
            firstSubparagraph = originalItem.getChildItems().get(0);
            if (!getTagValueFromTocItemVo(firstSubparagraph).equals(SUBPARAGRAPH)) {
                return originalItem;
            }
        }

        if (hasTocItemSoftAction(originalItem, MOVE_FROM) && originalItem.isSoftActionRoot()) {
            firstSubparagraph.setSoftActionRoot(true);
        }

        if (originalItem.isIndented()) {
            firstSubparagraph.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        } else if (!firstSubparagraph.isIndented()) {
            firstSubparagraph.populateIndentInfo(IndentedItemType.FIRST_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(firstSubparagraph);

        if (IndentedItemType.FIRST_SUBPOINT.equals(firstSubparagraph.getIndentOriginType()) ||
                IndentedItemType.FIRST_SUBPARAGRAPH.equals(firstSubparagraph.getIndentOriginType())) {
            firstSubparagraph.setSoftTransFrom(firstSubparagraph.getId());
            removeTransformPrefix(originalItem);
            firstSubparagraph.setId(originalItem.getId());
            if (firstSubparagraph.getSoftActionAttr() == null && hasTocItemSoftOrigin(firstSubparagraph, EC)) {
                firstSubparagraph.setSoftActionAttr(TRANSFORM);
                firstSubparagraph.setSoftActionRoot(true);
            }
        }

        // Ok, checking is done, remove the parent paragraph
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);
        // Parent should be a paragraph here no need to check if it's a list
        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.removeChildItem(firstSubparagraph);
        originalItem.getParentItem().addChildItem(originalPosition, firstSubparagraph);

        removeSubpointContent(firstSubparagraph);
        addSubparagraphContent(firstSubparagraph, firstSubparagraph);

        originalItem = firstSubparagraph;

        return originalItem;
    }

    private TableOfContentItemVO buildSubparagraphFromParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        removeParagraphContent(originalItem);
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.PARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the paragraph
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        originalItem.setTocItem(subparagraphTocItem);

        // Remove numbering
        originalItem.setNumber(null);
        originalItem.setOriginNumAttr(null);
        originalItem.setNumTagIndex(null);

        // Remove "transformed" prefix -> For comparaison, id must be the same
        removeTransformPrefix(originalItem);

        addSubparagraphContent(originalItem, originalItem);

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubparagraphFromParagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        removeParagraphContent(originalItem);
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.PARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the point
        TocItem subparagraphTocItem = TocItemUtils.getTocItemByName(tocItems, SUBPARAGRAPH);
        TableOfContentItemVO firstSubparagraph = buildTransItemFromItem(originalItem);

        removeTransformPrefix(firstSubparagraph);
        addTransformPrefix(originalItem);

        firstSubparagraph.setTocItem(subparagraphTocItem);

        // Remove numbering
        firstSubparagraph.setNumber(null);
        firstSubparagraph.setOriginNumAttr(null);
        firstSubparagraph.setNumTagIndex(null);

        // Sets vtdindex to null
        firstSubparagraph.setVtdIndex(null);

        if (IndentedItemType.FIRST_SUBPOINT.equals(originalItem.getIndentOriginType())) {
            firstSubparagraph.setId(originalItem.getSoftTransFrom());
            removeTransformPrefix(originalItem);
        }

        // Add first child
        originalItem.addChildItem(0, firstSubparagraph);

        addSubparagraphContent(originalItem, firstSubparagraph);
        addSubparagraphContent(firstSubparagraph, firstSubparagraph);

        return originalItem;
    }

    private TableOfContentItemVO buildFirstSubparagraphFromSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has no children, conversion is not possible
        if (originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        // Ok, checking is done, convert the subparagraph
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        TableOfContentItemVO paragraph = buildTransItemFromItem(originalItem);
        paragraph.setTocItem(paragraphTocItem);

        if (!originalItem.isIndented()) {
            paragraph.populateIndentInfo(IndentedItemType.OTHER_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        } else if (originalItem.isIndented()) {
            paragraph.populateIndentInfo(originalItem.getIndentOriginType(), originalItem.getIndentOriginIndentLevel()
                    , originalItem.getIndentOriginNumId(), originalItem.getIndentOriginNumValue(), originalItem.getIndentOriginNumOrigin());
        }

        // Sets vtdindex to null
        paragraph.setVtdIndex(null);

        // Add first child
        int originalPosition = originalItem.getParentItem().getChildItems().indexOf(originalItem);

        originalItem.getParentItem().removeChildItem(originalItem);
        originalItem.getParentItem().addChildItem(originalPosition, paragraph);
        paragraph.addChildItem(0, originalItem);
        moveChildren(originalItem, paragraph);

        removeTransformPrefix(originalItem);

        if (IndentedItemType.FIRST_SUBPOINT.equals(originalItem.getIndentOriginType())) {
            paragraph.setId(originalItem.getSoftTransFrom());
        }

        addSubparagraphContent(originalItem, originalItem);
        addSubparagraphContent(paragraph, originalItem);

        originalItem = paragraph;

        return originalItem;
    }

    private TableOfContentItemVO buildParagraphFromSubparagraph(List<TocItem> tocItems, TableOfContentItemVO originalItem, int originalIndentLevel) {
        // If it has children, conversion is not possible
        if (!originalItem.getChildItems().isEmpty()) {
            return originalItem;
        }

        if (!originalItem.isIndented()) {
            originalItem.populateIndentInfo(IndentedItemType.OTHER_SUBPARAGRAPH, originalIndentLevel
                    , originalItem.getElementNumberId(), originalItem.getNumber(), originalItem.getOriginNumAttr());
        }

        // Ok, checking is done, convert the paragraph
        TocItem paragraphTocItem = TocItemUtils.getTocItemByName(tocItems, PARAGRAPH);
        originalItem.setTocItem(paragraphTocItem);

        removeSubparagraphContent(originalItem);

        return originalItem;
    }

    private TableOfContentItemVO removeTransformPrefix(TableOfContentItemVO item) {
        if (item.getId().startsWith(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX) && hasTocItemSoftOrigin(item, EC)) {
            item.setId(item.getId().substring(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX.length()));
            if (hasTocItemSoftAction(item, TRANSFORM)) {
                item.setSoftActionAttr(null);
                item.setSoftActionRoot(null);
            }
        }
        if (item.getId().startsWith(INDENT_PLACEHOLDER_ID_PREFIX) && hasTocItemSoftOrigin(item, CN)) {
            item.setId(item.getId().substring(INDENT_PLACEHOLDER_ID_PREFIX.length()));
        }
        return item;
    }

    private TableOfContentItemVO addTransformPrefix(TableOfContentItemVO item) {
        if (!item.getId().startsWith(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)
                && !item.getId().startsWith(XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX)
                && hasTocItemSoftOrigin(item, EC)) {
            item.setId(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + item.getId());
            if (item.getSoftActionAttr() == null) {
                item.setSoftActionAttr(TRANSFORM);
                item.setSoftActionRoot(true);
            }
        } else if (!item.getId().startsWith(INDENT_PLACEHOLDER_ID_PREFIX) && hasTocItemSoftOrigin(item, CN)) {
            item.setId(INDENT_PLACEHOLDER_ID_PREFIX + item.getId());
        }
        return item;
    }

    private TableOfContentItemVO addIndentPrefix(TableOfContentItemVO item) {
        if (!item.getId().startsWith(INDENT_PLACEHOLDER_ID_PREFIX)) {
            item.setId(INDENT_PLACEHOLDER_ID_PREFIX + item.getId());
        }
        return item;
    }

    TableOfContentItemVO buildTransItemFromItem(TableOfContentItemVO originalItem) {
        TableOfContentItemVO transItem;
        if (hasTocItemSoftOrigin(originalItem, EC)) {
            if (originalItem.getId().startsWith(XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)
                    || originalItem.getId().startsWith(XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX)) {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(), originalItem.getNumTagIndex(),
                        originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(), originalItem.getVtdIndex(), null, null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            } else {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(), originalItem.getNumTagIndex(),
                        originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(), originalItem.getVtdIndex(), null, null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            }
        } else {
            if (originalItem.getId().startsWith(INDENT_PLACEHOLDER_ID_PREFIX)) {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(), originalItem.getNumTagIndex(),
                        originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(), originalItem.getVtdIndex(), null, null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            } else {
                transItem = new TableOfContentItemVO(originalItem.getTocItem(), INDENT_PLACEHOLDER_ID_PREFIX + originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(), originalItem.getOriginNumAttr(), originalItem.getHeading(), originalItem.getNumTagIndex(),
                        originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(), originalItem.getVtdIndex(), null, null, originalItem.getContent(), originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                        originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
            }
        }

        if (transItem.getSoftActionAttr() == null && hasTocItemSoftOrigin(originalItem, EC)) {
            transItem.setSoftActionAttr(TRANSFORM);
            transItem.setSoftActionRoot(true);
        }
        return transItem;
    }

    public int getIndentedItemIndentLevel(TableOfContentItemVO item) {
        return getItemIndentLevel(item, 0);
    }

    int getItemIndentLevel(TableOfContentItemVO item, int startingDepth) {
        TableOfContentItemVO parent = item.getParentItem();
        while (parent != null) {
            if (ArrayUtils.contains(NUMBERED_AND_LEVEL_ITEMS, XmlTableOfContentHelper.getTagValueFromTocItemVo(parent))) {
                startingDepth++;
            }
            parent = parent.getParentItem();
        }

        return startingDepth;
    }


    private TableOfContentItemVO addSubpointContent(TableOfContentItemVO item, TableOfContentItemVO subpoint) {
        byte [] startTag = IndentXmlHelper.buildStartTag(subpoint, SUBPOINT);
        removeSubpointContent(item);
        item.setContent(new String(startTag, StandardCharsets.UTF_8) + item.getContent() + "</" + SUBPOINT + ">");
        return item;
    }

    private TableOfContentItemVO addSubparagraphContent(TableOfContentItemVO item, TableOfContentItemVO subparagraph) {
        byte [] startTag = IndentXmlHelper.buildStartTag(subparagraph, SUBPARAGRAPH);
        removeSubparagraphContent(item);
        item.setContent(new String(startTag, StandardCharsets.UTF_8) + item.getContent() + "</" + SUBPARAGRAPH + ">");
        return item;
    }

    private TableOfContentItemVO removeParagraphContent(TableOfContentItemVO item) {
        if (item.getContent().contains("<" + PARAGRAPH)) {
            item.setContent(IndentXmlHelper.extractContentFromUnumberedParagraph(item));
        }
        return item;
    }

    private TableOfContentItemVO removeSubpointContent(TableOfContentItemVO item) {
        if (item.getContent().startsWith("<" + SUBPOINT)) {
            item.setContent(item.getContent().replaceAll("<[/]?" + SUBPOINT + "[\\s]?[^>]*>", ""));
        }
        return item;
    }

    private TableOfContentItemVO removeSubparagraphContent(TableOfContentItemVO item) {
        if (item.getContent().contains("<" + SUBPARAGRAPH)) {
            item.setContent(item.getContent().replaceAll("<[/]?" + SUBPARAGRAPH + "[\\s]?[^>]*>", ""));
        }
        return item;
    }

    private void moveChildren(TableOfContentItemVO source, TableOfContentItemVO target) {
        List<TableOfContentItemVO> children = new ArrayList<>();
        children.addAll(source.getChildItems());

        for (TableOfContentItemVO child : children) {
            source.removeChildItem(child);
            target.addChildItem(child);
        }
    }
}
