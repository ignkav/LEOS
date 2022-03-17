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
import eu.europa.ec.leos.services.support.xml.XmlHelper;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.TocItemUtils;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.TRANSFORM;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftOrigin;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper.getTagValueFromTocItemVo;
import static eu.europa.ec.leos.services.support.xml.indent.IndentConversionHelper.NUMBERED_ITEMS;
import static eu.europa.ec.leos.services.support.xml.indent.IndentConversionHelper.PARAGRAPH_LEVEL_ITEMS;
import static eu.europa.ec.leos.services.support.xml.indent.IndentConversionHelper.UNUMBERED_ITEMS;

@Component
public class IndentApplyRules {
    private static final Logger LOG = LoggerFactory.getLogger(IndentApplyRules.class);
    static final int MAX_POINT_INDENT_LEVEL = 4;

    @Autowired
    IndentConversionHelper indentConversionHelper;

    void applyIndentRulesToChildren(final TableOfContentItemVO indentedItem, final TableOfContentItemVO indentChildrenParent) {
        if (IndentRules.getIndentChildrenRule().equals(IndentRules.ChildrenRule.ALL)) {
            return;
        } else if (IndentRules.getIndentChildrenRule().equals(IndentRules.ChildrenRule.NONE)) {
            // If indent, children should become children of the original indented item parent (if numbered)
            // If indent, children should become children of the new indented item parent (if not numbered)

            // We should skip the first child (= first subpoint) because it contains the point's content
            int startPosition = 1;

            List<TableOfContentItemVO> children = buildNewListBeforeMoving(indentedItem, 0);

            for (int i = startPosition; i < children.size(); i++) {
                TableOfContentItemVO child = children.get(i);
                if (i == startPosition && getTagValueFromTocItemVo(child).equals(LIST) && !indentChildrenParent.getChildItemsView().isEmpty()) {
                    TableOfContentItemVO lastChild = indentChildrenParent.getChildItemsView().get(indentChildrenParent.getChildItemsView().size() - 1);
                    if (getTagValueFromTocItemVo(lastChild).equals(LIST)) {
                        mergeTwoLists(lastChild, child);
                    } else {
                        indentedItem.removeChildItem(child);
                        indentChildrenParent.addChildItem(child);
                    }
                } else {
                    indentedItem.removeChildItem(child);
                    indentChildrenParent.addChildItem(child);
                }
            }
        } else {
            LOG.debug("Indent only 'FIRST' child not implemented yet");
        }
    }

    int applyOutdentRulesToChildren(final TableOfContentItemVO indentedItem, final TableOfContentItemVO targetItem, boolean isNumbered
            , List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, int targetPosition
            , final boolean lastCycle) {
        if (IndentRules.getOutdentChildrenRule().equals(IndentRules.ChildrenRule.ALL)) {
            return targetPosition;
        } else if (IndentRules.getOutdentChildrenRule().equals(IndentRules.ChildrenRule.FIRST)) {
            // If outdented, only the first item of a list is outdented
            if (isNumbered) {
                List<TableOfContentItemVO> children = buildNewListBeforeMoving(indentedItem, 1);
                applyOutdentRuleForFirstChild(children, tocItems, numberingConfigs, lastCycle);
            } else {
                // If not numbered, children are children of target
                // We should skip the first child because it contains the point's content (first subpoint)
                List<TableOfContentItemVO> children = buildNewListBeforeMoving(indentedItem, 1);
                int index = targetPosition + 1;

                // Try to find a good list candidate
                TableOfContentItemVO childBefore = (index > 0) ? targetItem.getChildItemsView().get(index - 1) : null;
                TableOfContentItemVO childAfter = (index < targetItem.getChildItemsView().size())
                        ? targetItem.getChildItemsView().get(index) : null;
                for (int i = 0; i < children.size(); i++) {
                    TableOfContentItemVO child = children.get(i);
                    if (i == 0 && getTagValueFromTocItemVo(child).equals(LIST)
                            && childBefore != null && getTagValueFromTocItemVo(childBefore).equals(LIST)) {
                        // First child is a list
                        mergeTwoLists(childBefore, child);
                    } else if (i == (children.size() - 1) && getTagValueFromTocItemVo(child).equals(LIST)
                            && childAfter != null && getTagValueFromTocItemVo(childAfter).equals(LIST)) {
                        // Last child is a list
                        mergeTwoLists(childAfter, child);
                    } else {
                        indentedItem.removeChildItem(child);
                        if ((index + i) < targetItem.getChildItems().size()) {
                            targetItem.addChildItem(index + i, child);
                        } else {
                            targetItem.addChildItem(child);
                        }
                        targetPosition++;
                    }
                }
                applyOutdentRuleForFirstChild(children, tocItems, numberingConfigs, lastCycle);
            }
        } else {
            LOG.debug("Outdent Rule 'NONE' children not implemented yet");
        }
        return targetPosition;
    }

    Pair<TableOfContentItemVO, Integer> applyOutdentRulesToSiblings(final TableOfContentItemVO indentedItem, final TableOfContentItemVO targetItem
            , boolean isNumbered, int targetPosition, int originalIndentLevel, final List<TableOfContentItemVO> nextSiblings, TableOfContentItemVO originalParent
            ,List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, final boolean lastCycle) {
        if (isNumbered) {
            TableOfContentItemVO lastChild = (indentedItem.getChildItems().size() > 0) ? indentedItem.getChildItemsView().get(indentedItem.getChildItems().size() -1 ) : null;
            for (int i = 0; i < nextSiblings.size(); i++) {
                TableOfContentItemVO sibling = nextSiblings.get(i);
                String siblingTagName = getTagValueFromTocItemVo(sibling);
                if (i==0 && lastChild != null && getTagValueFromTocItemVo(lastChild).equals(LIST) && !ArrayUtils.contains(UNUMBERED_ITEMS, siblingTagName)) {
                    if (getTagValueFromTocItemVo(sibling).equals(LIST)) {
                        mergeTwoLists(lastChild, sibling);
                        checkListOrigin(lastChild);
                    } else {
                        while (i < nextSiblings.size() && !ArrayUtils.contains(UNUMBERED_ITEMS, siblingTagName)) {
                            sibling.getParentItem().removeChildItem(sibling);
                            lastChild.addChildItem(sibling);
                            checkListOrigin(lastChild);
                            i++;
                            if (i < nextSiblings.size()) {
                                sibling = nextSiblings.get(i);
                            }
                        }
                    }
                } else {
                    if (ArrayUtils.contains(NUMBERED_ITEMS, siblingTagName)) {
                        // At that time, we must add it to a list

                        // Get list best candidate
                        TableOfContentItemVO listCandidate = sibling.getParentItem();
                        if (sibling.getParentItem().equals(originalParent)) {
                            originalParent = originalParent.getParentItem();
                        }

                        if (listCandidate.getChildItems().indexOf(sibling) == 0) {
                            // Found good candidate: move all list to the indentedItem
                            listCandidate.getParentItem().removeChildItem(listCandidate);
                            indentedItem.addChildItem(listCandidate);
                        } else {
                            listCandidate = buildEmptyList(tocItems);
                            indentedItem.addChildItem(listCandidate);
                            for (TableOfContentItemVO listSibling : nextSiblings) {
                                listSibling.getParentItem().removeChildItem(listSibling);
                                listCandidate.addChildItem(listSibling);
                                checkListOrigin(listCandidate);
                            }
                        }
                        // No more siblings, as there are only points in a list
                        break;
                    } else {
                        sibling.getParentItem().removeChildItem(sibling);
                        indentedItem.addChildItem(sibling);
                    }
                }
            }
        } else {
            // If indented item becomes a subpoint, siblings should moved next to the indented item, and target item should be the parent
            int i = targetPosition + 1;
            for (TableOfContentItemVO sibling : nextSiblings) {
                if (ArrayUtils.contains(NUMBERED_ITEMS, getTagValueFromTocItemVo(sibling))
                        && !getTagValueFromTocItemVo(targetItem).equals(LIST)) {
                    // Should check that target child's element at current position is a list
                    TableOfContentItemVO nextTargetChild = i < targetItem.getChildItemsView().size() ? targetItem.getChildItemsView().get(i) : targetItem.getChildItemsView().get(targetItem.getChildItemsView().size()-1);
                    if (XmlTableOfContentHelper.getTagValueFromTocItemVo(nextTargetChild).equals(XmlHelper.LIST)) {
                        // Ok found candidate, that's the last element of the target item
                        sibling.getParentItem().removeChildItem(sibling);
                        nextTargetChild.addChildItem(sibling);
                        checkListOrigin(nextTargetChild);
                    } else {
                        // Build a list at last position
                        TableOfContentItemVO parentItem = sibling.getParentItem();
                        TableOfContentItemVO newList = buildEmptyList(tocItems);
                        if (getTagValueFromTocItemVo(parentItem).equals(LIST) && !hasTocItemSoftAction(parentItem, TRANSFORM)) {
                            if (parentItem.getChildItems().indexOf(sibling) == 0) {
                                newList = parentItem;
                                if (newList.equals(originalParent)) {
                                    originalParent = originalParent.getParentItem();
                                }
                                newList.getParentItem().removeChildItem(newList);
                            }
                        }
                        sibling.getParentItem().removeChildItem(sibling);
                        if (i < targetItem.getChildItemsView().size()) {
                            targetItem.addChildItem(i, newList);
                        } else {
                            targetItem.addChildItem(newList);
                        }
                        newList.addChildItem(sibling);
                        checkListOrigin(newList);
                        targetPosition++;
                    }
                } else {
                    sibling.getParentItem().removeChildItem(sibling);
                    if (i < targetItem.getChildItemsView().size()) {
                        targetItem.addChildItem(i, sibling);
                    } else {
                        targetItem.addChildItem(sibling);
                    }
                    targetPosition++;
                }
                i++;
            }
        }
        if (originalIndentLevel > getTargetIndentLevel(targetItem)) {
            applyOutdentRuleForFirstChild(nextSiblings, tocItems, numberingConfigs, lastCycle);
        }

        return new Pair<>(originalParent, targetPosition);
    }

    void applyOutdentRuleForFirstChild(List<TableOfContentItemVO> items, List<TocItem> tocItems
            , List<NumberingConfig> numberingConfigs, boolean lastCycle) {
        if (!IndentRules.getOutdentChildrenRule().equals(IndentRules.ChildrenRule.FIRST)) {
            return;
        }
        // Ok all siblings have been moved to the target, not check if there is a list, and move only first element
        TableOfContentItemVO firstList = getFirstChildList(items);
        List<TableOfContentItemVO> nextChildren = new ArrayList<>();
        int listPosition = 0;
        int sizeOfFirstList = 0;
        TableOfContentItemVO firstItemOfTheFirstList = null;
        TableOfContentItemVO listOfThefirstItemOfTheFirstList = null;

        if (firstList != null && !firstList.getChildItemsView().isEmpty()) {
            listPosition = items.indexOf(firstList);
            if (listPosition > 0) {
                for (int i = 0; i < listPosition; i++) {
                    // Convert subpoints to subparagraphs or subpoints to subparagraphs before list
                    TableOfContentItemVO item = items.get(i);
                    if (!getTagValueFromTocItemVo(item).equals(LEVEL)) {
                        item.populateIndentInfo(getTagValueFromTocItemVo(item).equals(SUBPARAGRAPH) ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT
                                , indentConversionHelper.getIndentedItemIndentLevel(item) + 1
                                , item.getElementNumberId()
                                , item.getNumber()
                                , item.getOriginNumAttr());
                    }
                    indentConversionHelper.convertIndentedItem(tocItems, item, false
                            , getTagValueFromTocItemVo(item).equals(SUBPARAGRAPH) ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT
                            , indentConversionHelper.getIndentedItemIndentLevel(item) + 1
                            , false);
                }
            }
            firstItemOfTheFirstList = firstList.getChildItemsView().get(0);
            sizeOfFirstList = firstList.getChildItems().size();
        } else if (firstList == null && items.size() >= 1 && getTagValueFromTocItemVo(items.get(0).getParentItem()).equals(LIST)) {
            firstItemOfTheFirstList = items.get(0);
            sizeOfFirstList = items.size();
        }
        if (firstItemOfTheFirstList == null && items.size() >= 1) {
            // Means that there are only subparagraphs or subpoints
            for (TableOfContentItemVO item : items) {
                if (!getTagValueFromTocItemVo(item).equals(LEVEL)) {
                    item.populateIndentInfo(getTagValueFromTocItemVo(item).equals(SUBPARAGRAPH) ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT
                            , indentConversionHelper.getIndentedItemIndentLevel(item) + 1
                            , item.getElementNumberId()
                            , item.getNumber()
                            , item.getOriginNumAttr());
                }
                indentConversionHelper.convertIndentedItem(tocItems, item, false
                        , getTagValueFromTocItemVo(item).equals(SUBPARAGRAPH) ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT
                        , indentConversionHelper.getIndentedItemIndentLevel(item) + 1
                        , false);
            }
            return;
        } else if (firstItemOfTheFirstList == null) {
            return;
        }

        // Move all the next elements except the first one
        // First take care of the remaining elements in the list
        if (sizeOfFirstList > 1 || firstItemOfTheFirstList.containsItem(LIST)) {
            if (firstItemOfTheFirstList.containsItem(LIST)) {
                // Add other elements to list of the first item of the list
                // Is last element a list ?
                if (getTagValueFromTocItemVo(firstItemOfTheFirstList.getChildItems().get(firstItemOfTheFirstList.getChildItems().size() - 1)).equals(LIST)) {
                    listOfThefirstItemOfTheFirstList = firstItemOfTheFirstList.getChildItems().get(firstItemOfTheFirstList.getChildItems().size() - 1);
                } else {
                    TableOfContentItemVO newList = buildEmptyList(tocItems);
                    firstItemOfTheFirstList.addChildItem(newList);
                    listOfThefirstItemOfTheFirstList = newList;
                }
            } else if (indentConversionHelper.getIndentedItemIndentLevel(firstItemOfTheFirstList) != firstItemOfTheFirstList.getIndentOriginIndentLevel()){
                TableOfContentItemVO newList = buildEmptyList(tocItems);
                firstItemOfTheFirstList.addChildItem(0, newList);
                listOfThefirstItemOfTheFirstList = newList;
            }
            if (listOfThefirstItemOfTheFirstList != null) {
                nextChildren = buildNewListBeforeMoving(listOfThefirstItemOfTheFirstList, 0);
                if (firstList != null) {
                    moveFromOneListToAnother(firstList, listOfThefirstItemOfTheFirstList, 1);
                } else {
                    moveFromOneListToAnother(items.subList(1, items.size()), listOfThefirstItemOfTheFirstList);
                }
                checkListOrigin(listOfThefirstItemOfTheFirstList);
            }
        }
        boolean paragraphLevel = ArrayUtils.contains(PARAGRAPH_LEVEL_ITEMS, getTagValueFromTocItemVo(firstItemOfTheFirstList));
        IndentedItemType beforeIndentedItemType;
        if (paragraphLevel) {
            beforeIndentedItemType = firstItemOfTheFirstList.getContent().contains("<" + SUBPARAGRAPH) ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.PARAGRAPH;
        } else {
            beforeIndentedItemType = firstItemOfTheFirstList.getContent().startsWith("<" + SUBPOINT) ? IndentedItemType.FIRST_SUBPOINT : IndentedItemType.POINT;
        }
        if (!getTagValueFromTocItemVo(firstItemOfTheFirstList).equals(LEVEL)) {
            firstItemOfTheFirstList.populateIndentInfo(beforeIndentedItemType
                    , indentConversionHelper.getIndentedItemIndentLevel(firstItemOfTheFirstList) + 1
                    , firstItemOfTheFirstList.getElementNumberId()
                    , firstItemOfTheFirstList.getNumber()
                    , firstItemOfTheFirstList.getOriginNumAttr());
        }

        int firstItemListPosition;
        Pair<TableOfContentItemVO, Boolean> result = new Pair<>(firstItemOfTheFirstList, false);
        if (sizeOfFirstList <= 1 && (listPosition < items.size() - 1)) {
            if (!paragraphLevel && !firstItemOfTheFirstList.getContent().startsWith("<" + SUBPOINT)) {
                result = indentConversionHelper.forceBuildFirstSubpointFromPoint(tocItems, firstItemOfTheFirstList
                        , indentConversionHelper.getIndentedItemIndentLevel(firstItemOfTheFirstList) + 1);
            } else if (paragraphLevel && !firstItemOfTheFirstList.getContent().contains("<" + SUBPARAGRAPH)) {
                result = indentConversionHelper.forceBuildFirstSubparagraphFromParagraph(tocItems, firstItemOfTheFirstList
                        , indentConversionHelper.getIndentedItemIndentLevel(firstItemOfTheFirstList) + 1);
            }
            firstItemOfTheFirstList = result.left();
            if (!ArrayUtils.contains(NUMBERED_ITEMS, getTagValueFromTocItemVo(items.get(0)))) {
                firstItemListPosition = firstItemOfTheFirstList.getChildItems().size()-1;
            } else {
                firstItemListPosition = firstItemOfTheFirstList.getChildItems().indexOf(listOfThefirstItemOfTheFirstList);
            }
        } else {
            result = indentConversionHelper.convertIndentedItem(tocItems, firstItemOfTheFirstList, true
                    , beforeIndentedItemType
                    , indentConversionHelper.getIndentedItemIndentLevel(firstItemOfTheFirstList) + 1
                    , false);
            firstItemOfTheFirstList = result.left();
            firstItemListPosition = firstItemOfTheFirstList.getChildItems().indexOf(listOfThefirstItemOfTheFirstList);
        }
        updateNumbering(firstItemOfTheFirstList, result.right(), numberingConfigs, lastCycle);

        if (firstList != null) {
            // Take care of other elements after list
            if (listPosition < items.size() - 1) {
                for (int i = listPosition + 1; i < items.size(); i++) {
                    TableOfContentItemVO item = items.get(i);
                    item.getParentItem().removeChildItem(item);
                    if (firstItemListPosition + 1 < firstItemOfTheFirstList.getChildItems().size()) {
                        firstItemOfTheFirstList.addChildItem(firstItemListPosition + 1, item);
                    } else {
                        firstItemOfTheFirstList.addChildItem(item);
                    }
                    firstItemListPosition ++;
                }
            }
        }
        updateNumberingForChildren(firstItemOfTheFirstList, lastCycle, numberingConfigs);
        if (indentConversionHelper.getIndentedItemIndentLevel(firstItemOfTheFirstList) < firstItemOfTheFirstList.getIndentOriginIndentLevel()) {
            applyOutdentRuleForFirstChild(nextChildren, tocItems, numberingConfigs, lastCycle);
        }
        checkListOrigin(firstItemOfTheFirstList.getParentItem());
    }

    void checkListOrigin(TableOfContentItemVO list) {
        if (getTagValueFromTocItemVo(list).equals(LIST) && !list.getChildItemsView().isEmpty()) {
            if (hasTocItemSoftOrigin(list, CN) && (isListContainsOnlyOriginItems(list, EC) || !isListContainsOnlyOriginItems(list, CN))) {
                list.getParentItem().setAffected(true);
                list.setOriginAttr(EC);
            } else if (hasTocItemSoftOrigin(list, EC) && isListContainsOnlyOriginItems(list, CN)) {
                list.getParentItem().setAffected(true);
                list.setOriginAttr(CN);
            }
        } else if (list.containsItem(LIST)) {
            for (TableOfContentItemVO child: list.getChildItemsView()) {
                if (getTagValueFromTocItemVo(child).equals(LIST)) {
                    checkListOrigin(child);
                }
            }
        }
    }

    private boolean isListContainsOnlyOriginItems(TableOfContentItemVO list, String origin) {
        boolean containsOnlyOriginItems = true;
        for (TableOfContentItemVO child: list.getChildItemsView()) {
            if (origin.equals(EC) && (child.getOriginNumAttr() == null || child.getOriginNumAttr().equals(CN))) {
                containsOnlyOriginItems = false;
            } else if (origin.equals(CN) && (child.getOriginNumAttr() != null && child.getOriginNumAttr().equals(EC))) {
                containsOnlyOriginItems = false;
            }
        }
        return containsOnlyOriginItems;
    }

    public int getTargetIndentLevel(TableOfContentItemVO item) {
        int depth = 1;
        return indentConversionHelper.getItemIndentLevel(item, depth);
    }

    TableOfContentItemVO getFirstChildList(List<TableOfContentItemVO> items) {
        TableOfContentItemVO firstList = null;
        Iterator<TableOfContentItemVO> iterator = items.iterator();
        while (iterator.hasNext() && firstList == null) {
            TableOfContentItemVO child = iterator.next();
            if (getTagValueFromTocItemVo(child).equals(LIST)) {
                firstList = child;
            }
        }
        return firstList;
    }

    List<TableOfContentItemVO> buildNewListBeforeMoving(TableOfContentItemVO source, int startingIndex) {
        List<TableOfContentItemVO> children = new ArrayList<>();
        if (source.getChildItems().size() > startingIndex) {
            children.addAll(source.getChildItems().subList(startingIndex, source.getChildItems().size()));
        }
        return children;
    }

    void moveFromOneListToAnother(TableOfContentItemVO source, TableOfContentItemVO target, int startingIndex) {
        List<TableOfContentItemVO> children = buildNewListBeforeMoving(source, startingIndex);
        moveFromOneListToAnother(children, target);
    }

    private void moveFromOneListToAnother(List<TableOfContentItemVO> sources, TableOfContentItemVO target) {
        for (TableOfContentItemVO child : sources) {
            child.getParentItem().removeChildItem(child);
            target.addChildItem(child);
        }
    }

    void mergeTwoLists(TableOfContentItemVO primeList, TableOfContentItemVO secondList) {
        if (primeList.getVtdIndex() == null) {
            primeList.setVtdIndex(secondList.getVtdIndex());
            primeList.setId(secondList.getId());
            primeList.setOriginAttr(secondList.getOriginAttr());
            primeList.setSoftActionAttr(secondList.getSoftActionAttr());
            primeList.setSoftUserAttr(secondList.getSoftUserAttr());
            primeList.setSoftDateAttr(secondList.getSoftDateAttr());
            if (hasTocItemSoftAction(secondList, MOVE_FROM)) {
                primeList.setSoftMoveFrom(secondList.getSoftMoveFrom());
            }
        } else if (hasTocItemSoftOrigin(primeList, CN) && hasTocItemSoftOrigin(secondList, EC)) {
            primeList.setOriginAttr(secondList.getOriginAttr());
        }
        secondList.getParentItem().removeChildItem(secondList);

        moveFromOneListToAnother(secondList, primeList, 0);
    }

    TableOfContentItemVO buildEmptyList(List<TocItem> tocItems) {
        TableOfContentItemVO list = null;
        TocItem listTocItem = TocItemUtils.getTocItemByName(tocItems, LIST);
        if (listTocItem != null) {
            list = new TableOfContentItemVO(listTocItem, IdGenerator.generateId(LIST.substring(0, 3), 7), CN, null, CN, null, null,
                    null, null, null, null);
        }
        return list;
    }

    private void updateNumberingForChildren(TableOfContentItemVO item, final boolean lastCycle, List<NumberingConfig> numberingConfigs) {
        for (TableOfContentItemVO child : item.getChildItemsView()) {
            if ((ArrayUtils.contains(NUMBERED_ITEMS, getTagValueFromTocItemVo(child)))
                    && child.isIndented()) {
                updateNumbering(child, false, numberingConfigs, lastCycle);
            } else if ((ArrayUtils.contains(NUMBERED_ITEMS, getTagValueFromTocItemVo(child)))
                    && hasTocItemSoftOrigin(child, CN)) {
                child.setOriginNumAttr(CN);
            }
            updateNumberingForChildren(child, lastCycle, numberingConfigs);
        }
    }

    void updateNumbering(TableOfContentItemVO item, boolean restored, List<NumberingConfig> numberingConfigs, boolean lastCycle) {
        int newDepth = indentConversionHelper.getIndentedItemIndentLevel(item);
        if (ArrayUtils.contains(NUMBERED_ITEMS, getTagValueFromTocItemVo(item))) {
            if (newDepth == item.getIndentOriginIndentLevel() && !IndentedItemType.OTHER_SUBPOINT.equals(item.getIndentOriginType())
                    && !IndentedItemType.OTHER_SUBPARAGRAPH.equals(item.getIndentOriginType())) {
                item.setNumber(item.getIndentOriginNumValue());
                item.setOriginNumAttr(item.getIndentOriginNumOrigin());
                if ((item.getOriginNumAttr() != null) && (item.getParentItem().getOriginAttr() == null || !item.getParentItem().getOriginAttr().equals(item.getOriginNumAttr()))
                        && item.getOriginNumAttr().equals(EC) && lastCycle) {
                    item.getParentItem().setOriginAttr(EC);
                }
                item.setAffected(true);
                if (restored) {
                    item.setIndentOriginType(IndentedItemType.RESTORED);
                }
            } else {
                int configuredIndentNumConfigDepth = TocItemUtils.getDepthByNumberingType(numberingConfigs, NumberingType.INDENT);
                boolean isIndent = newDepth == configuredIndentNumConfigDepth;
                if (isIndent) {
                    item.setNumber("-");
                } else {
                    if (newDepth == 0) {
                        if (item.getIndentOriginIndentLevel() == 0 && item.getNumber() != null) {
                            item.setNumber("#");
                        } else if (item.getIndentOriginIndentLevel() > 0) {
                            // Get paragraph siblings and checks if they are numbered
                            List<TableOfContentItemVO> children = item.getParentItem().getChildItemsView();
                            int position = item.getParentItem().getChildItemsView().indexOf(item);
                            TableOfContentItemVO sibling = null;
                            if (position == 0 && children.size() > 1) {
                                sibling = children.get(1);
                            } else if (position > 0) {
                                sibling = children.get(0);
                            }
                            if ((sibling != null && sibling.getNumber() == null) || getTagValueFromTocItemVo(item.getParentItem()).equalsIgnoreCase(MAIN_BODY)) {
                                item.setNumber(null);
                            } else {
                                item.setNumber("#");
                            }
                        }
                    } else {
                        item.setNumber("#");
                    }
                }
                item.setAffected(true);
                item.setOriginNumAttr(CN);
            }
            item.setElementNumberId(item.getIndentOriginNumId());
        } else if (newDepth == item.getIndentOriginIndentLevel() && restored) {
            item.setIndentOriginType(IndentedItemType.RESTORED);
        }
    }
}
