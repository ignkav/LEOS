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

import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.support.xml.XmlHelper;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang.ArrayUtils;
import org.jgroups.util.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.getItemFromTocById;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.isTocItemFirstChild;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SUBPOINT;
import static eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper.getTagValueFromTocItemVo;
import static eu.europa.ec.leos.services.support.xml.indent.IndentConversionHelper.NUMBERED_AND_LEVEL_ITEMS;
import static eu.europa.ec.leos.services.support.xml.indent.IndentConversionHelper.NUMBERED_ITEMS;
import static eu.europa.ec.leos.services.support.xml.indent.IndentConversionHelper.PARAGRAPH_LEVEL_ITEMS;
import static eu.europa.ec.leos.services.support.xml.indent.IndentConversionHelper.UNUMBERED_ITEMS;

@Component
public class IndentHelper {
    private static final Logger LOG = LoggerFactory.getLogger(IndentHelper.class);

    @Autowired
    IndentApplyRules indentApplyRules;

    @Autowired
    IndentConversionHelper indentConversionHelper;

    @Autowired
    XmlTableOfContentHelper xmlTableOfContentHelper;

    public TableOfContentItemVO doIndentForTargetIndentLevel(int targetIndentLevel
            , Boolean isNumbered
            , TableOfContentItemVO indentedItem
            , List<TocItem> tocItems
            , List<NumberingConfig> numberingConfigs) throws IllegalArgumentException {

        String indentedTagName = getTagValueFromTocItemVo(indentedItem);
        boolean paragraphLevel = ArrayUtils.contains(PARAGRAPH_LEVEL_ITEMS, indentedTagName);

        // 1. Find the type of indented item
        IndentedItemType indentedItemType = paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;

        //  If first subpoint then the parent's point should indented
        if (ArrayUtils.contains(UNUMBERED_ITEMS, indentedTagName)
                && isTocItemFirstChild(indentedItem.getParentItem(), indentedItem)
                && !getTagValueFromTocItemVo(indentedItem.getParentItem()).equals(LEVEL)) {
            indentedItem = indentedItem.getParentItem();
            indentedItemType = paragraphLevel ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT;
        } else if (ArrayUtils.contains(UNUMBERED_ITEMS, indentedTagName)) {
            indentedItemType = paragraphLevel ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT;
        }

        // 2. Find original depth and target depth
        int originalIndentLevel = indentConversionHelper.getIndentedItemIndentLevel(indentedItem);
        Boolean isIndent;
        if (targetIndentLevel != originalIndentLevel) {
            isIndent = (targetIndentLevel - originalIndentLevel) > 0;
        } else {
            if ((!isNumbered) && !indentedItemType.equals(IndentedItemType.OTHER_SUBPOINT) && !indentedItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                isIndent = false;
            } else {
                isIndent = true;
            }
        }

        int counter;
        if (!isIndent) {
            counter = originalIndentLevel - targetIndentLevel;
            counter += (!isNumbered) && !indentedItemType.equals(IndentedItemType.OTHER_SUBPOINT)
                    && !indentedItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH) ? 1 : 0;
        } else {
            counter = targetIndentLevel - originalIndentLevel;
            counter += isNumbered && (indentedItemType.equals(IndentedItemType.OTHER_SUBPOINT)
                    || indentedItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) ? 1 : 0;
        }

        for (int i = counter; i > 0; i--) {
            originalIndentLevel = indentConversionHelper.getIndentedItemIndentLevel(indentedItem);
            // Get indented item type
            indentedTagName = getTagValueFromTocItemVo(indentedItem);
            paragraphLevel = ArrayUtils.contains(PARAGRAPH_LEVEL_ITEMS, indentedTagName);

            // 1. Find the type of indented item
            indentedItemType = paragraphLevel ? IndentedItemType.PARAGRAPH : IndentedItemType.POINT;

            //  If first subpoint then the parent's point should indented
            if ((indentedTagName.equals(POINT) || indentedTagName.equals(INDENT))
                    && indentedItem.getContent().startsWith("<" + SUBPOINT)) {
                indentedItemType = IndentedItemType.FIRST_SUBPOINT;
            }
            if ((indentedTagName.equals(PARAGRAPH))
                    && indentedItem.getContent().contains("<" + SUBPARAGRAPH)) {
                indentedItemType = IndentedItemType.FIRST_SUBPARAGRAPH;
            }
            if (ArrayUtils.contains(UNUMBERED_ITEMS, indentedTagName)
                    && isTocItemFirstChild(indentedItem.getParentItem(), indentedItem)
                    && !getTagValueFromTocItemVo(indentedItem.getParentItem()).equals(LEVEL)) {
                indentedItem = indentedItem.getParentItem();
                indentedItemType = paragraphLevel ? IndentedItemType.FIRST_SUBPARAGRAPH : IndentedItemType.FIRST_SUBPOINT;
            } else if (ArrayUtils.contains(UNUMBERED_ITEMS, indentedTagName)) {
                indentedItemType = paragraphLevel ? IndentedItemType.OTHER_SUBPARAGRAPH : IndentedItemType.OTHER_SUBPOINT;
            }
            int originalPosition = getIndentedItemPosition(indentedItem, indentedItemType);

            Pair<TableOfContentItemVO, Integer> targetAndPosition = getIndentTargetAndPosition(indentedItem,
                    isIndent,
                    indentedItemType,
                    isNumbered,
                    originalIndentLevel,
                    originalPosition,
                    tocItems);
            if (targetAndPosition == null) {
                continue;
            }
            TableOfContentItemVO targetItem = targetAndPosition.left();
            int targetPosition = targetAndPosition.right();

            TableOfContentItemVO indentChildrenParent = null;
            List<TableOfContentItemVO> nextDirectSiblings = new ArrayList<>();
            List<TableOfContentItemVO> nextOutOfTheListSiblings = new ArrayList<>();
            if (!isIndent) {
                // 1. Get next siblings
                nextDirectSiblings = getDirectNextSiblings(indentedItem);
                nextOutOfTheListSiblings = getOutOfTheListNextSiblings(indentedItem, isNumbered);
            } else {
                // 1. Get children parent
                int indexOfIndentedItem = indentedItem.getParentItem().getChildItemsView().indexOf(indentedItem);
                if (indexOfIndentedItem > 0) {
                    indentChildrenParent = indentedItem.getParentItem().getChildItemsView().get(indexOfIndentedItem - 1);
                }
            }

            // 2. Do indent
            TableOfContentItemVO originalParent = indentedItem.getParentItem();
            indentedItem.getParentItem().removeChildItem(indentedItem);
            if (targetPosition < targetItem.getChildItemsView().size()) {
                targetItem.addChildItem(targetPosition, indentedItem);
            } else {
                targetItem.addChildItem(indentedItem);
            }

            if (!isIndent) {
                // 3. In/outdentation of Children
                if (indentedItemType.equals(IndentedItemType.FIRST_SUBPOINT) || indentedItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) { // Means there are children
                    targetPosition = indentApplyRules.applyOutdentRulesToChildren(indentedItem, targetItem, isNumbered
                            , tocItems, numberingConfigs, targetPosition, i == 1);
                }

                // 4. Outdenting siblings
                if (!nextDirectSiblings.isEmpty()) {
                    Pair<TableOfContentItemVO, Integer> result = indentApplyRules.applyOutdentRulesToSiblings(indentedItem, targetItem, isNumbered
                            , targetPosition, originalIndentLevel, nextDirectSiblings, originalParent, tocItems, numberingConfigs, i == 1);
                    originalParent = result.left();
                    targetPosition = result.right();
                }
                if (!nextOutOfTheListSiblings.isEmpty()) {
                    Pair<TableOfContentItemVO, Integer> result = indentApplyRules.applyOutdentRulesToSiblings(indentedItem, targetItem, isNumbered
                            , targetPosition, originalIndentLevel-1, nextOutOfTheListSiblings, originalParent, tocItems, numberingConfigs, i == 1);
                    originalParent = result.left();
                }
            } else {
                // 3. In/outdentation of Children
                if (indentedItemType.equals(IndentedItemType.FIRST_SUBPOINT) || indentedItemType.equals(IndentedItemType.FIRST_SUBPARAGRAPH)) { // Means there are children
                    if (indentChildrenParent != null) {
                        indentApplyRules.applyIndentRulesToChildren(indentedItem, indentChildrenParent);
                    }
                }
            }

            indentedItem.populateIndentInfo(indentedItemType, originalIndentLevel, indentedItem.getElementNumberId(), indentedItem.getNumber(), indentedItem.getOriginNumAttr());

            Pair<TableOfContentItemVO, Boolean> convertResult = indentConversionHelper.convertIndentedItem(tocItems, indentedItem, isNumbered, indentedItemType, originalIndentLevel, isIndent);
            indentedItem = convertResult.left();
            boolean restored = convertResult.right();

            indentApplyRules.updateNumbering(indentedItem, restored, numberingConfigs, i == 1);
            indentConversionHelper.convertOtherItem(tocItems, targetItem);
            indentConversionHelper.convertOtherItem(tocItems, originalParent);

            checkMergingLists(originalParent);
            checkMergingLists(targetItem);
            checkMergingLists(indentedItem);
            checkListsOrigin(indentedItem, targetItem, originalParent);
        }

        return indentedItem;
    }

    private void checkMergingLists(TableOfContentItemVO item) {
        if (item.containsItem(LIST)) {
            TableOfContentItemVO previousChild = null;
            for (TableOfContentItemVO child: item.getChildItems()) {
                if (previousChild != null && getTagValueFromTocItemVo(previousChild).equals(LIST)
                        && getTagValueFromTocItemVo(child).equals(LIST)) {
                    indentApplyRules.mergeTwoLists(previousChild, child);
                    break;
                }
                previousChild = child;
            }
        }
    }

    private Pair<TableOfContentItemVO, Integer> getIndentTargetAndPosition(TableOfContentItemVO indentedItem
            , boolean isIndent
            , IndentedItemType indentedItemType
            , boolean isNumbered
            , int originalIndentLevel
            , int originalPosition
            , List<TocItem> tocItems) {

        TableOfContentItemVO targetItem = null;
        boolean originalIsNumbered = !indentedItemType.equals(IndentedItemType.OTHER_SUBPOINT) && !indentedItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH);
        if (!isIndent) {
            if (isNumbered && originalIsNumbered && originalIndentLevel > 0) {
                // Gets parent (LIST) of parent (POINT) of original parent (LIST)
                targetItem = indentedItem.getParentItem().getParentItem().getParentItem();
            } else if (!isNumbered && originalIsNumbered) {
                // Gets parent (POINT) of original parent (LIST)
                targetItem = indentedItem.getParentItem().getParentItem();
            } else if (isNumbered && !originalIsNumbered) {
                // Gets parent (LIST) of original parent (POINT)
                targetItem = indentedItem.getParentItem().getParentItem();
            } else if (!isNumbered && originalIndentLevel > 0) {
                // Gets parent (POINT) of parent (LIST) of original parent (POINT)
                targetItem = indentedItem.getParentItem().getParentItem().getParentItem();
            }
            if ((targetItem == null) || hasTocItemSoftAction(targetItem, MOVE_TO)) {
                return null;
            }
        } else {
            if (isNumbered && originalIndentLevel == indentApplyRules.MAX_POINT_INDENT_LEVEL
                    && !indentedItemType.equals(IndentedItemType.OTHER_SUBPOINT) && !indentedItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                return null;
            }

            if (originalPosition == 0 && !getTagValueFromTocItemVo(indentedItem.getParentItem()).equals(LEVEL)) {
                return null;
            } else if (originalPosition == 0 && getTagValueFromTocItemVo(indentedItem.getParentItem()).equals(LEVEL)) {
                targetItem = indentedItem.getParentItem();
            } else {
                List<TableOfContentItemVO> parentItemChildren = indentedItem.getParentItem().getChildItemsView();
                targetItem = parentItemChildren.get(originalPosition - 1);
                if (isNumbered && !originalIsNumbered) {
                    // Parent is a point
                    // Gets current parent
                    targetItem = indentedItem.getParentItem();
                }
                if (!isNumbered && !originalIsNumbered && getTagValueFromTocItemVo(targetItem).equals(LIST)) {
                    targetItem = targetItem.getChildItemsView().get(targetItem.getChildItemsView().size() - 1);
                }
            }
            if ((targetItem == null) || hasTocItemSoftAction(targetItem, MOVE_TO)) {
                return null;
            }
        }
        return getTargetAndPosition(targetItem, isNumbered, indentedItem, originalPosition
                , isIndent, indentedItemType, tocItems);
    }

    private void checkListsOrigin(TableOfContentItemVO indentedItem, TableOfContentItemVO targetItem, TableOfContentItemVO originalParent) {
        indentApplyRules.checkListOrigin(targetItem);
        indentApplyRules.checkListOrigin(originalParent);
        indentApplyRules.checkListOrigin(indentedItem);
    }

    public int getIndentedItemPosition(TableOfContentItemVO indentedItem, IndentedItemType indentedItemType) throws IllegalArgumentException {
        int position = 0;
        TableOfContentItemVO originalParentItem = indentedItem.getParentItem();
        if (originalParentItem == null) {
            throw new IllegalArgumentException("Could not indent item, not consistent structure");
        }

        switch (indentedItemType) {
            case POINT:
            case FIRST_SUBPOINT:
                if (!XmlTableOfContentHelper.getTagValueFromTocItemVo(originalParentItem).equals(XmlHelper.LIST)) {
                    throw new IllegalArgumentException("Could not indent item, not consistent structure");
                }
                position = originalParentItem.getChildItemsView().indexOf(indentedItem);
                break;
            case PARAGRAPH:
            case FIRST_SUBPARAGRAPH:
                if (!XmlTableOfContentHelper.getTagValueFromTocItemVo(originalParentItem).equals(XmlHelper.ARTICLE)) {
                    throw new IllegalArgumentException("Could not indent item, not consistent structure");
                }
                position = originalParentItem.getChildItemsView().indexOf(indentedItem);
                break;
            case OTHER_SUBPOINT:
            case OTHER_SUBPARAGRAPH:
                if (XmlTableOfContentHelper.getTagValueFromTocItemVo(originalParentItem).equals(XmlHelper.LIST)) {
                    throw new IllegalArgumentException("Could not indent item, not consistent structure");
                }
                position = originalParentItem.getChildItemsView().indexOf(indentedItem);
                break;
            default:
                throw new IllegalArgumentException("Could not determine indented type");
        }

        return position;
    }

    public Pair<TableOfContentItemVO, Integer> getTargetAndPosition(TableOfContentItemVO targetItem
            , Boolean isNumbered
            , TableOfContentItemVO indentedItem
            , int originalPosition
            , Boolean isIndent
            , IndentedItemType indentedItemType
            , List<TocItem> tocItems) throws IllegalArgumentException {
        if (isNumbered) {  // Target should be a list
            return findListCandidate(targetItem, indentedItem, originalPosition, isIndent, indentedItemType, tocItems);
        } else {           // Target should be a point (paragraph are not taken in account for the moment)
            if (!ArrayUtils.contains(NUMBERED_AND_LEVEL_ITEMS, XmlTableOfContentHelper.getTagValueFromTocItemVo(targetItem))) {
                // Should not happen always point or paragraph as parent
                TableOfContentItemVO parentPoint = findParent(targetItem, POINT);
                if (parentPoint == null) {
                    parentPoint = findParent(targetItem, INDENT);
                    if (parentPoint == null) {
                        parentPoint = findParent(targetItem, PARAGRAPH);
                    }
                    if (parentPoint == null) {
                        parentPoint = findParent(targetItem, LEVEL);
                    }
                }
                if (parentPoint != null) {
                    if (isIndent) {
                        return new Pair<>(parentPoint, parentPoint.getChildItemsView().size());
                    } else {
                        int targetPosition = parentPoint.getParentItem().getChildItemsView().indexOf(parentPoint);
                        return new Pair<>(parentPoint, targetPosition);
                    }
                } else {
                    throw new IllegalArgumentException("Couldn't find parent for indentation");
                }
            } else {
                if (isIndent) {
                    return new Pair<>(targetItem, targetItem.getChildItemsView().size());
                } else {
                    int targetPosition = getOutdentTargetPosition(targetItem, indentedItem) + 1;
                    return new Pair<>(targetItem, targetPosition);
                }
            }
        }
    }

    private Pair<TableOfContentItemVO, Integer> findListCandidate(TableOfContentItemVO targetItem
            , TableOfContentItemVO indentedItem
            , int originalPosition
            , Boolean isIndent
            , IndentedItemType indentedItemType
            , List<TocItem> tocItems) throws IllegalArgumentException {

        if (isIndent) {
            // INDENT
            if (XmlTableOfContentHelper.getTagValueFromTocItemVo(targetItem).equals(XmlHelper.LIST)) {
                // Ok fine, we must check that new next sibling isn't a list as well
                int targetPosition = targetItem.getParentItem().getChildItemsView().indexOf(targetItem);
                TableOfContentItemVO nextSiblingList = null;
                if ((targetPosition + 1) < targetItem.getParentItem().getChildItemsView().size()) {
                    if (getTagValueFromTocItemVo(targetItem.getParentItem().getChildItemsView().get(targetPosition + 1)).equals(LIST)) {
                        nextSiblingList = targetItem.getParentItem().getChildItemsView().get(targetPosition + 1);
                    } else if (indentedItem.equals(targetItem.getParentItem().getChildItemsView().get(targetPosition + 1))
                        && (targetPosition + 2) < targetItem.getParentItem().getChildItemsView().size()) {
                        if (getTagValueFromTocItemVo(targetItem.getParentItem().getChildItemsView().get(targetPosition + 2)).equals(LIST)) {
                            nextSiblingList = targetItem.getParentItem().getChildItemsView().get(targetPosition + 2);
                        }
                    }
                }
                if (nextSiblingList != null) {
                    // Then we must merge lists, take the best one
                    int position = targetItem.getChildItemsView().size();
                    indentApplyRules.mergeTwoLists(targetItem, nextSiblingList);
                    return new Pair<>(targetItem, position);
                } else {
                    return new Pair<>(targetItem, targetItem.getChildItemsView().size()); // For indent always at the end of the list
                }
            } else if (targetItem.containsItem(XmlHelper.LIST)) {
                if (indentedItemType.equals(IndentedItemType.OTHER_SUBPOINT) || indentedItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                    // If target item contains the indented item then
                    //      Takes account if the original position
                    // Else
                    //      indented item will be the last element of the target item
                    if (indentedItem.getParentItem().equals(targetItem)) {
                        // Is there a list before ?
                        TableOfContentItemVO siblingBefore = originalPosition > 0 ? targetItem.getChildItemsView().get(originalPosition - 1) : null;
                        if (siblingBefore != null && XmlTableOfContentHelper.getTagValueFromTocItemVo(siblingBefore).equals(XmlHelper.LIST)) {
                            // Ok found candidate, that's the list before
                            // For indent always at the end of the list

                            // Now check that sibling after is not a list, if it's a list we must merge two lists
                            int position = siblingBefore.getChildItemsView().size();
                            if ((originalPosition + 1) < targetItem.getChildItemsView().size()) {
                                TableOfContentItemVO siblingAfter = targetItem.getChildItemsView().get(originalPosition + 1);
                                if (XmlTableOfContentHelper.getTagValueFromTocItemVo(siblingAfter).equals(XmlHelper.LIST)) {
                                    indentApplyRules.mergeTwoLists(siblingBefore, siblingAfter);
                                }
                            }
                            return new Pair<>(siblingBefore, position);
                        } else {
                            // Is there a list after ?
                            if ((originalPosition + 1) < targetItem.getChildItemsView().size()) {
                                TableOfContentItemVO siblingAfter = targetItem.getChildItemsView().get(originalPosition + 1);
                                if (XmlTableOfContentHelper.getTagValueFromTocItemVo(siblingAfter).equals(XmlHelper.LIST)) {
                                    // Ok found candidate, that's the list after
                                    // This time should be first element of the list
                                    return new Pair<>(siblingAfter, 0);
                                } else {
                                    // Build a list a that position
                                    TableOfContentItemVO newList = indentApplyRules.buildEmptyList(tocItems);
                                    targetItem.addChildItem(originalPosition + 1, newList);
                                    indentConversionHelper.convertOtherItem(tocItems, targetItem);
                                    // Ok didn't find any candidate, that's a new empty list
                                    // This time should be first element of the list (empty list)
                                    return new Pair<>(newList, 0);
                                }
                            } else {
                                // Build a list at last position
                                TableOfContentItemVO newList = indentApplyRules.buildEmptyList(tocItems);
                                targetItem.addChildItem(newList);
                                indentConversionHelper.convertOtherItem(tocItems, targetItem);
                                // Ok didn't find any candidate, that's a new empty list
                                // This time should be first element of the list (empty list)
                                return new Pair<>(newList, 0);
                            }
                        }
                    } else {
                        // Should check that last target child's element is a list
                        TableOfContentItemVO lastTargetChild = targetItem.getChildItemsView().get(targetItem.getChildItemsView().size() - 1);
                        if (XmlTableOfContentHelper.getTagValueFromTocItemVo(lastTargetChild).equals(XmlHelper.LIST)) {
                            // Ok found candidate, that's the last element of the target item
                            // For indent always at the end of the list
                            return new Pair<>(lastTargetChild, lastTargetChild.getChildItemsView().size());
                        } else {
                            // Build a list at last position
                            TableOfContentItemVO newList = indentApplyRules.buildEmptyList(tocItems);
                            targetItem.addChildItem(newList);
                            indentConversionHelper.convertOtherItem(tocItems, targetItem);
                            // Ok didn't find any candidate, that's a new empty list
                            // This time should be first element of the list (empty list)
                            return new Pair<>(newList, 0);
                        }
                    }
                } else { // From a point, then target here is a point with list
                    // Should check that last target child's element is a list
                    TableOfContentItemVO lastTargetChild = targetItem.getChildItemsView().get(targetItem.getChildItemsView().size() - 1);
                    if (XmlTableOfContentHelper.getTagValueFromTocItemVo(lastTargetChild).equals(XmlHelper.LIST)) {
                        // Ok found candidate, that's the last element of the target item
                        // For indent always at the end of the list
                        return new Pair<>(lastTargetChild, lastTargetChild.getChildItemsView().size());
                    } else {
                        // Build a list at last position
                        TableOfContentItemVO newList = indentApplyRules.buildEmptyList(tocItems);
                        targetItem.addChildItem(newList);
                        indentConversionHelper.convertOtherItem(tocItems, targetItem);
                        // Ok didn't find any candidate, that's a new empty list
                        // This time should be first element of the list (empty list)
                        return new Pair<>(newList, 0);
                    }
                }
            } else {
                // Target point doesn't contain any list
                // Build a list at last position
                TableOfContentItemVO newList = indentApplyRules.buildEmptyList(tocItems);
                if (indentedItemType.equals(IndentedItemType.OTHER_SUBPOINT) || indentedItemType.equals(IndentedItemType.OTHER_SUBPARAGRAPH)) {
                   targetItem.addChildItem(originalPosition, newList);
                } else {
                   targetItem.addChildItem(newList);
                }
                indentConversionHelper.convertOtherItem(tocItems, targetItem);
                // Ok didn't find any candidate, that's a new empty list
                // This time should be first element of the list (empty list)
                return new Pair<>(newList, 0);
            }
        } else {
            // OUTDENT
            int targetPosition = getOutdentTargetPosition(targetItem, indentedItem) + 1;
            if (XmlTableOfContentHelper.getTagValueFromTocItemVo(targetItem).equals(XmlHelper.LIST) || XmlTableOfContentHelper.getTagValueFromTocItemVo(targetItem).equals(XmlHelper.ARTICLE)) {
                return new Pair<>(targetItem, targetPosition); // For outdent take in account parent position
            } else { // Should not happen for points ( a point is always included in a list )
                TableOfContentItemVO parentList = ArrayUtils.contains(PARAGRAPH_LEVEL_ITEMS, XmlTableOfContentHelper.getTagValueFromTocItemVo(targetItem)) ? findParent(targetItem,ARTICLE) : findParent(targetItem, LIST);
                if (parentList != null) {
                    targetPosition = parentList.getParentItem().getChildItemsView().indexOf(parentList) + 1;
                    return new Pair<>(parentList, targetPosition);
                } else if (getTagValueFromTocItemVo(targetItem).equalsIgnoreCase(MAIN_BODY)) {
                    return new Pair<>(targetItem, targetPosition);
                } else {
                    throw new IllegalArgumentException("Couldn't find parent for outdentation");
                }
            }
        }
    }

    private int getOutdentTargetPosition(TableOfContentItemVO targetItem, TableOfContentItemVO indentedItem) {
        TableOfContentItemVO parent = indentedItem;
        while (parent.getParentItem() != null && !(parent.getParentItem().equals(targetItem))) {
            parent = parent.getParentItem();
        }
        if (parent.getParentItem().equals(targetItem)) {
            return targetItem.getChildItemsView().indexOf(parent);
        } else {
            return 0;
        }
    }

    private TableOfContentItemVO findParent(TableOfContentItemVO item, String aknTag) {
        TableOfContentItemVO parentTag = item;
        while (parentTag != null && !XmlTableOfContentHelper.getTagValueFromTocItemVo(parentTag).equals(aknTag)) {
            parentTag = parentTag.getParentItem();
        }
        return parentTag;
    }

    private List<TableOfContentItemVO> getDirectNextSiblings(TableOfContentItemVO item) {
        List<TableOfContentItemVO> nextSiblings = new ArrayList<TableOfContentItemVO>();
        int position = item.getParentItem().getChildItemsView().indexOf(item);
        for (int i = position + 1; i < item.getParentItem().getChildItemsView().size(); i++) {
            nextSiblings.add(item.getParentItem().getChildItemsView().get(i));
        }
        return nextSiblings;
    }

    private List<TableOfContentItemVO> getOutOfTheListNextSiblings(TableOfContentItemVO item, boolean isNumbered) {
        List<TableOfContentItemVO> nextSiblings = new ArrayList<TableOfContentItemVO>();
        if (ArrayUtils.contains(NUMBERED_ITEMS, getTagValueFromTocItemVo(item))) {
            if (item.getParentItem() != null && getTagValueFromTocItemVo(item.getParentItem()).equals(LIST)) {
                final TableOfContentItemVO parentList = item.getParentItem();
                int position = parentList.getParentItem().getChildItemsView().indexOf(parentList);
                for (int i = position + 1; i < parentList.getParentItem().getChildItemsView().size(); i++) {
                    nextSiblings.add(parentList.getParentItem().getChildItemsView().get(i));
                }
            }
        } else {
            if (item.getParentItem() != null && !isNumbered && (ArrayUtils.contains(NUMBERED_ITEMS, getTagValueFromTocItemVo(item.getParentItem())))) {
                final TableOfContentItemVO parentPoint = item.getParentItem();
                int position = parentPoint.getParentItem().getChildItemsView().indexOf(parentPoint);
                for (int i = position + 1; i < parentPoint.getParentItem().getChildItemsView().size(); i++) {
                    nextSiblings.add(parentPoint.getParentItem().getChildItemsView().get(i));
                }
            }
        }
        return nextSiblings;
    }
}
