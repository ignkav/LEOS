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
package eu.europa.ec.leos.ui.window.toc;

import com.vaadin.data.TreeData;
import com.vaadin.ui.TreeGrid;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.ui.component.toc.TocDropResult;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.model.action.SoftActionType.DELETE;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.getTocItemChildPosition;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BLOCK;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLAUSE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper.getTagValueFromTocItemVo;

public abstract class AbstractTocEditor implements TocEditor {
    protected static final String TEMP_PREFIX = "temp_";

    protected TocDropResult validateAction(final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
            final List<TableOfContentItemVO> droppedItems, final TableOfContentItemVO targetItem, final ItemPosition position) {

        TocDropResult result = new TocDropResult(true, "toc.edit.window.drop.success.message",
                droppedItems.get(0), targetItem);
        TableOfContentItemVO parentItem = tocTree.getTreeData().getParent(targetItem);
        for (TableOfContentItemVO sourceItem : droppedItems) {
            if (isItemDroppedOnSameTarget(result, sourceItem, targetItem) ||
                    !validateAddingItemAsChildOrSibling(result, sourceItem, targetItem, tocTree, tableOfContentRules,
                            parentItem, position)) {
                return result;
            }
        }
        return result;
    }

    private boolean isItemDroppedOnSameTarget(final TocDropResult result, final TableOfContentItemVO sourceItem,
                                              final TableOfContentItemVO targetItem) {
        if (sourceItem.equals(targetItem)) {
            result.setSuccess(false);
            result.setMessageKey("toc.edit.window.drop.error.same.item.message");
            result.setSourceItem(sourceItem);
            result.setTargetItem(targetItem);
            return true;
        }
        return false;
    }

    private boolean validateAddingItemAsChildOrSibling(final TocDropResult result, final TableOfContentItemVO sourceItem,
                                                       final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
            final TableOfContentItemVO parentItem, final ItemPosition position) {

        TocItem targetTocItem = targetItem.getTocItem();
        List<TocItem> targetTocItems = tableOfContentRules.get(targetTocItem);
        if (isDroppedOnPointOrIndent(sourceItem, targetItem) || getTagValueFromTocItemVo(sourceItem).
                equals(getTagValueFromTocItemVo(targetItem))) {
            TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
            return validateAddingToActualTargetItem(result, sourceItem, targetItem, tocTree, tableOfContentRules, actualTargetItem, position);
        } else if (targetTocItems != null && targetTocItems.size() > 0 && targetTocItems.contains(sourceItem.getTocItem())) {
            //If target item type is root, source item will be added as child, else validate dropping item at dragged location
            TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, false);
            return targetTocItem.isRoot() || validateAddingToActualTargetItem(result, sourceItem, targetItem, tocTree, tableOfContentRules, actualTargetItem, position);
        } else { // If child elements not allowed in target validate adding it to its parent
            return validateAddingItemAsSibling(result, sourceItem, targetItem, tocTree, tableOfContentRules, parentItem, position);
        }
    }

    private boolean validateAddingItemAsSibling(final TocDropResult result, final TableOfContentItemVO sourceItem,
                                                final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
            final TableOfContentItemVO parentItem, final ItemPosition position) {

        TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
        return validateAddingToActualTargetItem(result, sourceItem, targetItem, tocTree, tableOfContentRules, actualTargetItem, position);
    }

    private boolean validateParentAndSourceTypeCompatibility(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO parentItem,
            final TocItem parentTocItem, final List<TocItem> parentTocItems) {

        if (parentTocItems == null || parentTocItems.size() == 0 || !parentTocItems.contains(sourceItem.getTocItem())
                || !sourceItem.getTocItem().isSameParentAsChild() && parentTocItem.getAknTag().value().equals(sourceItem.getTocItem().getAknTag().value())){
            result.setSuccess(false);
            result.setMessageKey("toc.edit.window.drop.error.message");
            result.setSourceItem(sourceItem);
            result.setTargetItem(parentItem);
            return false;
        }
        return true;
    }

    protected boolean validateAddingToActualTargetItem(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
            final TableOfContentItemVO actualTargetItem, final ItemPosition position) {

        TocItem parentTocItem = actualTargetItem != null ? actualTargetItem.getTocItem() : null;
        List<TocItem> parentTocItems = tableOfContentRules.get(parentTocItem);
        boolean parentAndSourceTypeCompatible = validateParentAndSourceTypeCompatibility(result, sourceItem, actualTargetItem, parentTocItem, parentTocItems);
        boolean validAddingToItem = validateAddingToItem(result, sourceItem, targetItem, tocTree, actualTargetItem, position);
        boolean maxDepthReached = validateMaxDepth(result, sourceItem, targetItem);
        return parentAndSourceTypeCompatible && validAddingToItem && !maxDepthReached;
    }

    protected abstract boolean validateAddingToItem(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO actualTargetItem, final ItemPosition position);

    protected boolean validateMaxDepth(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem) {
        if(targetItem.getTocItem().getMaxDepth() != null) {
            int maxDepthRule = Integer.valueOf(targetItem.getTocItem().getMaxDepth());
            if (maxDepthRule > 0 && targetItem.getItemDepth() >= maxDepthRule) {
                result.setSuccess(false);
                result.setMessageKey("toc.edit.window.drop.error.depth.message");
                result.setSourceItem(sourceItem);
                result.setTargetItem(targetItem);
                return false;
            }
        }
        return true;
    }

    protected void performAddOrMoveAction(final boolean isAdd, final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tableOfContentRules,
            final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final TableOfContentItemVO parentItem, final ItemPosition position) {

        if (targetItem.getTocItem().isChildrenAllowed()) {
            TocItem targetTocItem = targetItem.getTocItem();
            List<TocItem> targetTocItems = tableOfContentRules.get(targetTocItem);
            if (isDroppedOnPointOrIndent(sourceItem, targetItem) || getTagValueFromTocItemVo(sourceItem).equals(getTagValueFromTocItemVo(targetItem))
                    || !(targetTocItems != null && targetTocItems.size() > 0 && targetTocItems.contains(sourceItem.getTocItem()))) {
                // If items have the same type or if child elements are not allowed in target add it to its parent
                TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
                addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, actualTargetItem, position);
            } else if (!targetTocItem.isRoot()){
                // item is dropped at dragged location
                TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, false);
                addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, actualTargetItem, position);
            } else {
                //If target item type is root, source item will be added as child before clause item if exists
                if (targetItem.containsItem(CLAUSE)) {
                    TableOfContentItemVO clauseItem = targetItem.getChildItems().stream()
                            .filter(x -> x.getTocItem().getAknTag().value().equals(CLAUSE)).findFirst().orElse(null);
                    TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, clauseItem, clauseItem.getParentItem(), ItemPosition.BEFORE, true);
                    addOrMoveItem(isAdd, sourceItem, clauseItem, tocTree, actualTargetItem, ItemPosition.BEFORE);
                } else {
                    addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, targetItem, ItemPosition.AS_CHILDREN);
                }
            }
        } else {
            TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
            addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, actualTargetItem, position);
        }
    }

    protected void addOrMoveItem(final boolean isAdd, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO actualTargetItem, final ItemPosition position) {

        if (isAdd) {
            tocTree.getTreeData().addItem(null, sourceItem);
            if (actualTargetItem == null) {
                sourceItem.setParentItem(null);
                sourceItem.setItemDepth(1);
            }
        } else if (sourceItem.getParentItem() != null) {
            sourceItem.getParentItem().removeChildItem(sourceItem);
            sourceItem.setOriginalDepth(sourceItem.getItemDepth());
        }

        if (actualTargetItem != null) {
            tocTree.getTreeData().setParent(sourceItem, actualTargetItem);
            sourceItem.setParentItem(actualTargetItem);
            if (!actualTargetItem.equals(targetItem)) {
                int indexSiblings = actualTargetItem.getChildItems().indexOf(targetItem);
                tocTree.getTreeData().moveAfterSibling(sourceItem, targetItem);
                if (ItemPosition.BEFORE == position) {
                    tocTree.getTreeData().moveAfterSibling(targetItem, sourceItem);
                    actualTargetItem.getChildItems().add(indexSiblings, sourceItem);
                } else {
                    actualTargetItem.getChildItems().add(indexSiblings + 1, sourceItem);
                }
            } else {
                actualTargetItem.getChildItems().add(sourceItem);
            }
        }
        setItemDepth(sourceItem, targetItem, position);
        setBlockOrCrossHeading(sourceItem);
    }

    private void setBlockOrCrossHeading(TableOfContentItemVO sourceItem) {
        boolean iscrossHeading = getTagValueFromTocItemVo(sourceItem).equalsIgnoreCase(CROSSHEADING)
                || getTagValueFromTocItemVo(sourceItem).equalsIgnoreCase(BLOCK);
        if (iscrossHeading && MAIN_BODY.equals(sourceItem.getParentItem().getTocItem().getAknTag().value())){
            sourceItem.setBlock(true);
        }else if(iscrossHeading){
            sourceItem.setCrossHeading(true);
        }
        if(iscrossHeading && isInList(sourceItem)){
            sourceItem.setCrossHeadingInList(true);
        }
    }

    private boolean isInList(TableOfContentItemVO sourceItem) {
        TableOfContentItemVO parent = sourceItem.getParentItem();
        if (parent != null) {
            if (getTagValueFromTocItemVo(parent).equalsIgnoreCase(LIST)) {
                return true;
            }
            for (TableOfContentItemVO item: parent.getChildItemsView()) {
                if (getTagValueFromTocItemVo(item).equalsIgnoreCase(POINT)
                        || getTagValueFromTocItemVo(item).equalsIgnoreCase(INDENT)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void handleLevelMove(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem, TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO actualTargetItem, ItemPosition position, boolean isRestored) {
        if (!isRestored && position.equals(ItemPosition.AS_CHILDREN)
                && (LEVEL.equals(sourceItem.getTocItem().getAknTag().value()) || isHigherElement(sourceItem))
                && LEVEL.equals(targetItem.getTocItem().getAknTag().value())) {
            TableOfContentItemVO reCalculatedTargetItemVo;
            if (isHigherElement(sourceItem)) {
                reCalculatedTargetItemVo = getSiblingPosition(sourceItem, targetItem);
            } else {
                reCalculatedTargetItemVo = getLastChildLevel(sourceItem, targetItem);
            }
            moveSourceAfterChildOrSibling(sourceItem, tocTree, actualTargetItem, reCalculatedTargetItemVo);
        } else if (!isRestored && position.equals(ItemPosition.AFTER) && LEVEL.equals(targetItem.getTocItem().getAknTag().value())
                && (LEVEL.equals(sourceItem.getTocItem().getAknTag().value()) || isHigherElement(sourceItem))) {
            TableOfContentItemVO siblingPosition = getSiblingPosition(sourceItem, targetItem);
            moveSourceAfterChildOrSibling(sourceItem, tocTree, actualTargetItem, siblingPosition);
        }
    }

    protected TableOfContentItemVO getActualTargetItem(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final TableOfContentItemVO parentItem,
            final ItemPosition position, boolean isTocItemSibling) {

        switch (position) {
            case AS_CHILDREN:
                if ((isTocItemSibling && !targetItem.getTocItem().isSameParentAsChild()
                        || (targetItem.getTocItem().isSameParentAsChild() && targetItem.containsItem(LIST)))
                        || (targetItem.getId().equals(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + sourceItem.getId()))) {
                    return parentItem;
                } else if (!sourceItem.equals(targetItem)) {
                    return targetItem;
                }
                break;
            case BEFORE:
                return parentItem;
            case AFTER:
                return isTocItemSibling ? parentItem : targetItem;
        }
        return null;
    }

    private boolean isDroppedOnPointOrIndent(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {
        String sourceTagValue = getTagValueFromTocItemVo(sourceItem);
        String targetTagValue = getTagValueFromTocItemVo(targetItem);
        return (sourceTagValue.equals(CROSSHEADING) || sourceTagValue.equals(POINT) || sourceTagValue.equals(INDENT)) && (targetTagValue.equals(POINT) || targetTagValue.equals(INDENT));
    }

    private void setItemDepthInHigherElements(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem ) {
        sourceItem.setItemDepth(targetItem.getItemDepth() == 0 ? 1 : targetItem.getItemDepth());
        sourceItem.getChildItems().forEach(child -> setItemDepthInHigherElements(child, targetItem));
    }

    
    private void setItemDepth(final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem, final ItemPosition position) {
        if ((sourceItem.getTocItem().isHigherElement() != null && sourceItem.getTocItem().isHigherElement())
                || targetItem.getTocItem().isHigherElement() != null && targetItem.getTocItem().isHigherElement()) {
            setItemDepthInHigherElements(sourceItem, targetItem);
        } else {
            switch (position) {
                case AS_CHILDREN:
                    sourceItem.setItemDepth(targetItem.getItemDepth() + 1);
                    break;
                case BEFORE:
                    sourceItem.setItemDepth(targetItem.getItemDepth() == 0 ? 1 : targetItem.getItemDepth());
                    break;
                case AFTER:
                    if (targetItem.getTocItem().isRoot()) {
                        sourceItem.setItemDepth(1);
                    } else {
                        sourceItem.setItemDepth(targetItem.getItemDepth() == 0 ? 1 : targetItem.getItemDepth());
                    }
            }
        }
    }

    protected boolean moveSourceAfterChildOrSibling(TableOfContentItemVO sourceItem, TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO actualTargetItem, TableOfContentItemVO newTargetItem) {
        if(newTargetItem != null){
            tocTree.getTreeData().moveAfterSibling(sourceItem, newTargetItem);
            actualTargetItem.removeChildItem(sourceItem);
            int lastChildIndex = actualTargetItem.getChildItems().indexOf(newTargetItem);
            actualTargetItem.getChildItems().add(lastChildIndex + 1, sourceItem);
            return true;
        }
        return false;
    }

    protected TableOfContentItemVO getLastChildLevel(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem){
        List<TableOfContentItemVO> list = sourceItem.getParentItem().getChildItems();
        int sourceItemIndex = list.indexOf(sourceItem);
        TableOfContentItemVO nextTocItemInList = null;
        while(sourceItemIndex < list.size() -1){
            sourceItemIndex++;
            nextTocItemInList = list.get(sourceItemIndex);
            if(LEVEL.equals(getTagValueFromTocItemVo(nextTocItemInList)) || isHigherElement(nextTocItemInList)){
                if(nextTocItemInList.getItemDepth() ==  sourceItem.getItemDepth()){
                    continue;
                }else if(nextTocItemInList.getItemDepth() <  sourceItem.getItemDepth()){
                    nextTocItemInList = getTargetPosition(sourceItem, targetItem, list, sourceItemIndex);
                    break;
                }
            }
        }
        return nextTocItemInList != null ? nextTocItemInList  : targetItem ;
    }

    protected TableOfContentItemVO getSiblingPosition(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem) {
        List<TableOfContentItemVO> list = sourceItem.getParentItem().getChildItems();
        int sourceItemIndex = list.indexOf(sourceItem);
        TableOfContentItemVO nextTocItemInList = null;
        while (sourceItemIndex < list.size() - 1) {
            sourceItemIndex++;
            nextTocItemInList = list.get(sourceItemIndex);
            if (LEVEL.equals(getTagValueFromTocItemVo(nextTocItemInList)) || isHigherElement(nextTocItemInList)) {
                if (nextTocItemInList.getItemDepth() > targetItem.getItemDepth()) {
                    continue;
                }else if (nextTocItemInList.getItemDepth() <= targetItem.getItemDepth()) {
                    nextTocItemInList = getTargetPosition(sourceItem, targetItem, list, sourceItemIndex);
                    break;
                }
            }
        }
        return nextTocItemInList != null ? nextTocItemInList : targetItem ;
    }

    private TableOfContentItemVO getTargetPosition(TableOfContentItemVO sourceItem, TableOfContentItemVO targetItem, List<TableOfContentItemVO> list, int sourceItemIndex) {
        TableOfContentItemVO nextTocItemInList;
        if(sourceItem == list.get(sourceItemIndex -1)){
            nextTocItemInList = targetItem;
        }else{
            nextTocItemInList = list.get(sourceItemIndex -1);
        }
        return nextTocItemInList;
    }

    protected boolean isHigherElement(TableOfContentItemVO tocItemVO){
        return (tocItemVO.getTocItem().isHigherElement() != null && tocItemVO.getTocItem().isHigherElement());
    }

    protected boolean isLastExistingChildElement(TableOfContentItemVO tocItemVO) {
        return tocItemVO.getParentItem().getChildItemsView().size() == 1;
    }

    protected static TableOfContentItemVO getTableOfContentItemVOById(String id, List<TableOfContentItemVO> tableOfContentItemVOS) {
        for (TableOfContentItemVO tableOfContentItemVO: tableOfContentItemVOS){
            if (tableOfContentItemVO.getId().equals(id)) {
                return tableOfContentItemVO;
            } else {
                TableOfContentItemVO childResult = getTableOfContentItemVOById(id, tableOfContentItemVO.getChildItems());
                if (childResult != null) {
                    return childResult;
                }
            }
        }
        return null;
    }

    protected TableOfContentItemVO copyItemToTemp(TableOfContentItemVO originalItem) {
        TableOfContentItemVO temp = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + originalItem.getId(),
                originalItem.getOriginAttr(), originalItem.getNumber(),
                originalItem.getOriginNumAttr(), originalItem.getHeading(), originalItem.getNumTagIndex(), originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(),
                originalItem.getVtdIndex(), originalItem.getList(), originalItem.getListTagIndex(), originalItem.getContent(),
                originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
        temp.setContent(originalItem.getContent());
        temp.setItemDepth(originalItem.getItemDepth());
        temp.setOriginalDepth(originalItem.getOriginalDepth());
        originalItem.getChildItems().forEach(child -> temp.addChildItem(copyItemToTemp(child)));
        return temp;
    }

    protected void dropItemAtOriginalPosition(TableOfContentItemVO originalPosition, TableOfContentItemVO item, TreeData<TableOfContentItemVO> container) {
        TableOfContentItemVO parentItem = originalPosition.getParentItem();
        if (parentItem != null && item != null) {
            int indexSiblings = parentItem.getChildItems().indexOf(originalPosition);
            if (indexSiblings >= 0) {
                container.addItem(parentItem, item);
                item.setParentItem(parentItem);
                parentItem.getChildItems().add(indexSiblings, item);
                container.moveAfterSibling(item, originalPosition);
                container.moveAfterSibling(originalPosition, item);
            } else {
                throw new IllegalStateException("Original element not found in its parent list of children");
            }
            dropChildrenAtOriginalPosition(item, container);
        }
    }

    protected void dropChildrenAtOriginalPosition(TableOfContentItemVO parentItem, TreeData<TableOfContentItemVO> container) {
        List<TableOfContentItemVO> children = parentItem.getChildItems();
        if (children != null) {
            for (TableOfContentItemVO child: children) {
                container.addItem(parentItem, child);
                dropChildrenAtOriginalPosition(child, container);
            }
        }
    }

    protected TableOfContentItemVO copyTempItemToFinalItem(TableOfContentItemVO tempItem){
        TableOfContentItemVO finalItem = new TableOfContentItemVO(tempItem.getTocItem(), tempItem.getId().replace(TEMP_PREFIX, ""),
                tempItem.getOriginAttr(), tempItem.getNumber(),
                tempItem.getOriginNumAttr(), tempItem.getHeading(), tempItem.getNumTagIndex(), tempItem.getHeadingTagIndex(), tempItem.getIntroTagIndex(),
                tempItem.getVtdIndex(), tempItem.getList(), tempItem.getListTagIndex(), tempItem.getContent(),
                tempItem.getSoftActionAttr(), tempItem.isSoftActionRoot(), tempItem.getSoftUserAttr(), tempItem.getSoftDateAttr(),
                tempItem.getSoftMoveFrom(), tempItem.getSoftMoveTo(), tempItem.getSoftTransFrom(), tempItem.isUndeleted(), tempItem.getNumSoftActionAttr());

        finalItem.setContent(tempItem.getContent());
        finalItem.setItemDepth(tempItem.getItemDepth());
        finalItem.setOriginalDepth(tempItem.getOriginalDepth());
        tempItem.getChildItems().forEach(child -> finalItem.addChildItem(copyTempItemToFinalItem(child)));
        return finalItem;
    }

    protected boolean restoreMovedItemOrSetNumber(final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO droppedItem, final TableOfContentItemVO newPosition, final ItemPosition position) {
        List<TableOfContentItemVO> siblings = ItemPosition.AS_CHILDREN.equals(position) ? tocTree.getTreeData().getChildren(newPosition) : tocTree.getTreeData().getChildren(tocTree.getTreeData().getParent(newPosition));
        Integer droppedItemIndex = siblings.indexOf(droppedItem);
        TableOfContentItemVO previousSibling = droppedItemIndex > 0 ? siblings.get(droppedItemIndex - 1) : null;
        TableOfContentItemVO nextSibling = droppedItemIndex < siblings.size() -1 ? siblings.get(droppedItemIndex + 1) : null;
        if (isPlaceholderForDroppedItem(newPosition, droppedItem)) {
            restoreOriginal(droppedItem, newPosition, tocTree);
            if (newPosition.getParentItem() != null) {
                newPosition.getParentItem().removeChildItem(newPosition);
            }
        } else if (isPlaceholderForDroppedItem(previousSibling, droppedItem)) {
            restoreOriginal(droppedItem, previousSibling, tocTree);
            if (newPosition.getParentItem() != null) {
                newPosition.getParentItem().removeChildItem(previousSibling);
            }
        } else if (isPlaceholderForDroppedItem(nextSibling, droppedItem)) {
            restoreOriginal(droppedItem, nextSibling, tocTree);
            if (newPosition.getParentItem() != null) {
                newPosition.getParentItem().removeChildItem(nextSibling);
            }
        } else {
            setNumber(droppedItem, newPosition);
            return false;
        }
        return true;
    }

    protected Boolean isPlaceholderForDroppedItem(TableOfContentItemVO candidate, TableOfContentItemVO droppedItem) {
        if (candidate != null && hasTocItemSoftAction(candidate.getParentItem(), MOVE_TO)) {
            return false;
        }
        return candidate != null && candidate.getId().equals(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + droppedItem.getId());
    }

    protected void restoreOriginal(final TableOfContentItemVO dropData, final TableOfContentItemVO targetItemVO, final TreeGrid<TableOfContentItemVO> tocTree) {
        setAttributesToOriginal(dropData, targetItemVO);
        TreeData<TableOfContentItemVO> container = tocTree.getTreeData();
        List<TableOfContentItemVO> dropDataChildElements = dropData.getChildItems();
        int position = getPositionOfNextNonRootChild(dropDataChildElements, 0);
        int index = 0;
        TableOfContentItemVO targetItemChildElement;
        while (index < targetItemVO.getChildItems().size()) {
            targetItemChildElement= targetItemVO.getChildItems().get(index);
            if (targetItemChildElement.isSoftActionRoot()) {
                if (position >= 0) {
                    moveItem(targetItemChildElement, dropDataChildElements.get(position), tocTree.getTreeData());
                } else {
                    if (hasTocItemSoftAction(targetItemVO, MOVE_TO) && hasTocItemSoftAction(targetItemChildElement, DELETE)) {
                        addSoftDeletedChildrenToTree(targetItemChildElement, dropData, container);
                        index++;
                    } else {
                        moveItemToLastChild(targetItemChildElement, dropData, container);
                    }
                }
                position = getPositionOfNextNonRootChild(dropDataChildElements, dropDataChildElements.indexOf(targetItemChildElement));
            } else {
                if (position >= 0 && position < dropDataChildElements.size()) {
                    restoreOriginal(dropDataChildElements.get(position), targetItemChildElement, tocTree);
                    position = getPositionOfNextNonRootChild(dropDataChildElements, position + 1);
                }
                index++;
            }
        }
        tocTree.getTreeData().removeItem(targetItemVO);
    }

    private void addSoftDeletedChildrenToTree(final TableOfContentItemVO targetItem, TableOfContentItemVO sourceItem, TreeData<TableOfContentItemVO> container) {
        if (hasTocItemSoftAction(targetItem, DELETE)) {
            if (container.contains(targetItem)) {
                container.removeItem(targetItem);
            }
            if (!targetItem.getParentItem().equals(sourceItem)){
                sourceItem.getChildItems().add(targetItem);
            }
            container.addItem(sourceItem, targetItem);
            int position = getTocItemChildPosition(sourceItem, targetItem);
            for (TableOfContentItemVO child: targetItem.getChildItemsView()) {
                addSoftDeletedChildrenToTree(child, sourceItem.getChildItemsView().get(position), container);
            }
        }
    }

    private void moveItemToLastChild(TableOfContentItemVO item, TableOfContentItemVO parent, TreeData<TableOfContentItemVO> container) {
        TableOfContentItemVO tempDeletedItem = copyItemToTemp(item);
        container.removeItem(item);
        container.addItem(parent, tempDeletedItem);
        if (item.getParentItem() != null) {
            item.getParentItem().removeChildItem(item);
        }

        TableOfContentItemVO finalItem = copyTempItemToFinalItem(tempDeletedItem);
        container.removeItem(tempDeletedItem);
        container.addItem(parent, finalItem);
        parent.getChildItems().add(finalItem);
    }

    protected boolean isRootElement(TableOfContentItemVO element) {
        return Boolean.TRUE.equals(element.isSoftActionRoot());
    }

    protected int getPositionOfNextNonRootChild(List<TableOfContentItemVO> childrenElements, int position) {
        while (position < childrenElements.size()){
            if (!isRootElement(childrenElements.get(position))) {
                return position;
            }
            position++;
        }
        return -1;
    }

    protected void setAttributesToOriginal(TableOfContentItemVO dropElement, TableOfContentItemVO targetItemVO) {
        if(targetItemVO.getNumSoftActionAttr() != null){
            dropElement.setNumber(targetItemVO.getNumber());
            dropElement.setNumSoftActionAttr(targetItemVO.getNumSoftActionAttr());
        }else if(targetItemVO.getNumber() != null && !targetItemVO.getNumber().isEmpty()) {
            dropElement.setNumber(targetItemVO.getNumber());
            dropElement.setNumSoftActionAttr(null);
        }else {
            dropElement.setNumber(null);
        }
        dropElement.setSoftActionAttr(null);
        dropElement.setSoftMoveTo(null);
        dropElement.setSoftMoveFrom(null);
        dropElement.setSoftUserAttr(null);
        dropElement.setSoftDateAttr(null);
        dropElement.setSoftActionRoot(null);
        dropElement.setRestored(true);
    }

    protected TableOfContentItemVO moveItem(TableOfContentItemVO item, TableOfContentItemVO moveBefore, TreeData<TableOfContentItemVO> treeData) {
        TableOfContentItemVO tempDeletedItem = copyItemToTemp(item);
        return movingItem(item, moveBefore, treeData, tempDeletedItem);
    }

    protected TableOfContentItemVO movingItem(TableOfContentItemVO item, TableOfContentItemVO moveBefore, TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tempDeletedItem) {
        dropItemAtOriginalPosition(moveBefore, tempDeletedItem, treeData);
        treeData.removeItem(item);
        if (item.getParentItem() != null) {
            item.getParentItem().removeChildItem(item);
        }

        TableOfContentItemVO finalItem = copyTempItemToFinalItem(tempDeletedItem);
        dropItemAtOriginalPosition(tempDeletedItem, finalItem, treeData);
        treeData.removeItem(tempDeletedItem);
        if (tempDeletedItem.getParentItem() != null) {
            tempDeletedItem.getParentItem().removeChildItem(tempDeletedItem);
        }
        return finalItem;
    }

    protected boolean isElementAndTargetOriginDifferent(TableOfContentItemVO element, TableOfContentItemVO parent) {
        boolean isDifferent = false;
        if (element.getOriginAttr() == null) {
            isDifferent = true;
        } else if (!element.getOriginAttr().equals(parent.getOriginAttr())) {
            isDifferent = true;
        }
        return isDifferent;
    }

    protected void setNumber(final TableOfContentItemVO droppedElement, final TableOfContentItemVO targetElement) {
        if (isNumbered(droppedElement, targetElement)) {
            droppedElement.setNumber("#");
            if (isNumSoftDeleted(droppedElement.getNumSoftActionAttr())) {
                droppedElement.setNumSoftActionAttr(null);
            }
        } else {
            droppedElement.setNumber(null);
        }
    }

    protected boolean isNumSoftDeleted(final SoftActionType numSoftACtionAttr) {
        return DELETE.equals(numSoftACtionAttr);
    }

    private boolean isNumbered(TableOfContentItemVO droppedElement, TableOfContentItemVO targetElement) {
        boolean isNumbered = true;
        if (OptionsType.NONE.equals(droppedElement.getTocItem().getItemNumber())) {
            isNumbered = false;
        } else if (OptionsType.OPTIONAL.equals(droppedElement.getTocItem().getItemNumber())) {
            if (getTagValueFromTocItemVo(targetElement).equals(getTagValueFromTocItemVo(droppedElement))) {
                if ((targetElement.getNumber() == null) || isNumSoftDeleted(targetElement.getNumSoftActionAttr())) {
                    isNumbered = false;
                }
            } else if ((targetElement.getChildItems() != null) && (targetElement.getChildItems().size() > 0)) {
                for (TableOfContentItemVO itemVO : targetElement.getChildItems()) {
                    if (getTagValueFromTocItemVo(itemVO).equals(getTagValueFromTocItemVo(droppedElement))) {
                        if ((itemVO.getNumber() == null) || isNumSoftDeleted(itemVO.getNumSoftActionAttr())) {
                            isNumbered = false;
                            break;
                        }
                    }
                }
            }
        }
        return isNumbered;
    }

    protected boolean containsItemOfOrigin(TableOfContentItemVO tableOfContentItemVO, String origin, String elementOrigin) {
        if ((!StringUtils.isEmpty(tableOfContentItemVO.getOriginAttr()) && tableOfContentItemVO.getOriginAttr().equals(origin)) ||
                (StringUtils.isEmpty(tableOfContentItemVO.getOriginAttr()) && origin.equals(elementOrigin))) {
            return true;
        }
        boolean containsItem = false;
        for (TableOfContentItemVO item : tableOfContentItemVO.getChildItems()) {
            containsItem = containsItemOfOrigin(item, origin, elementOrigin);
            if (containsItem) break;
        }
        return containsItem;
    }

    protected TableOfContentItemVO softDeleteItem(TreeGrid<TableOfContentItemVO> tocTree,
                                                  TableOfContentItemVO tableOfContentItemVO, String elementOrigin) {
        Boolean wasRoot = isRootElement(tableOfContentItemVO);
        Boolean wasMoved = MOVE_FROM.equals(tableOfContentItemVO.getSoftActionAttr());
        softDeleteMovedRootItems(tocTree, tableOfContentItemVO);
        TableOfContentItemVO movedTableOfContentItemVO = null;
        if (wasMoved) {
            if (!wasRoot) {
                // all its moved children are now restored to their original position and deleted,
                // but the tableOfContentItemVO element still needs to be restored to its original position and deleted
                revertMoveAndTransformToSoftDeleted(tocTree, tableOfContentItemVO);
            }
        } else {
            // all its moved children are now restored to their original position and deleted,
            // and the tableOfContentItemVO only needs to be deleted
            movedTableOfContentItemVO = transformToSoftDeleted(tocTree.getTreeData(), tableOfContentItemVO);
        }
        if (elementOrigin.equals(tableOfContentItemVO.getOriginAttr())) {
            if(movedTableOfContentItemVO != null){
                tocTree.getTreeData().removeItem(movedTableOfContentItemVO);
                tableOfContentItemVO.getParentItem().removeChildItem(movedTableOfContentItemVO);
            }
            tableOfContentItemVO.getParentItem().removeChildItem(tableOfContentItemVO);
            tocTree.getDataProvider().refreshAll();
        }
        return movedTableOfContentItemVO;
    }

    protected ActionType hardDeleteFromTree(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO tableOfContentItemVO) {
        tocTree.getTreeData().removeItem(tableOfContentItemVO);
        if (tableOfContentItemVO.getParentItem() != null) {
            tableOfContentItemVO.getParentItem().removeChildItem(tableOfContentItemVO);
        }
        return ActionType.DELETED;
    }

    private int softDeleteMovedRootItems(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO tableOfContentItemVO) {
        int index = 0;
        while (index < tableOfContentItemVO.getChildItems().size()) {
            index += softDeleteMovedRootItems(tocTree, tableOfContentItemVO.getChildItems().get(index));
        }
        if (isRootElement(tableOfContentItemVO) && MOVE_FROM.equals(tableOfContentItemVO.getSoftActionAttr())){
            revertMoveAndTransformToSoftDeleted(tocTree, tableOfContentItemVO);
            return 0;
        } else if(CN.equals(tableOfContentItemVO.getOriginAttr())){
            tocTree.getTreeData().removeItem(tableOfContentItemVO);
            tableOfContentItemVO.getParentItem().removeChildItem(tableOfContentItemVO);
            return 0;
        }
        return 1;
    }

    private void revertMoveAndTransformToSoftDeleted(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO tableOfContentItemVO) {
        TableOfContentItemVO originalPosition = getTableOfContentItemVOById(tableOfContentItemVO.getSoftMoveFrom(), tocTree.getTreeData().getRootItems());
        if (originalPosition != null) {
            TableOfContentItemVO movedItem = moveItem(tableOfContentItemVO, originalPosition, tocTree.getTreeData());
            restoreOriginal(movedItem, originalPosition, tocTree);
            if (originalPosition.getParentItem() != null) {
                originalPosition.getParentItem().removeChildItem(originalPosition);
            }
            transformToSoftDeleted(tocTree.getTreeData(), movedItem);
        } else {
            throw new IllegalStateException("Soft-moved element was later hard-deleted or its id was not set in its placeholder");
        }
    }

    private TableOfContentItemVO transformToSoftDeleted(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tableOfContentItemVO) {
        TableOfContentItemVO tempDeletedItem = copyDeletedItemToTemp(tableOfContentItemVO, Boolean.TRUE);
        return movingItem(tableOfContentItemVO, tableOfContentItemVO, treeData, tempDeletedItem);
    }

    private TableOfContentItemVO copyDeletedItemToTemp(TableOfContentItemVO originalItem, Boolean isSoftActionRoot){
        TableOfContentItemVO tempDeletedItem;

        if (!MOVE_TO.equals(originalItem.getSoftActionAttr()) && !DELETE.equals(originalItem.getSoftActionAttr())) {
            tempDeletedItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + SOFT_DELETE_PLACEHOLDER_ID_PREFIX + originalItem.getId(),
                    originalItem.getOriginAttr(), originalItem.getNumber(),
                    EC, originalItem.getHeading(), originalItem.getNumTagIndex(), originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(), originalItem.getVtdIndex(),
                    originalItem.getList(), originalItem.getListTagIndex(),
                    originalItem.getContent(),
                    DELETE, isSoftActionRoot, null, null, originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
        } else {
            tempDeletedItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + originalItem.getId(),
                    originalItem.getOriginAttr(), originalItem.getNumber(),
                    originalItem.getOriginNumAttr(), originalItem.getHeading(), originalItem.getNumTagIndex(), originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(),
                    originalItem.getVtdIndex(), originalItem.getList(), originalItem.getListTagIndex(), originalItem.getContent(),
                    originalItem.getSoftActionAttr(), originalItem.isSoftActionRoot(), originalItem.getSoftUserAttr(), originalItem.getSoftDateAttr(),
                    originalItem.getSoftMoveFrom(), originalItem.getSoftMoveTo(), originalItem.getSoftTransFrom(), originalItem.isUndeleted(), originalItem.getNumSoftActionAttr());
        }

        tempDeletedItem.setContent(originalItem.getContent());
        tempDeletedItem.setItemDepth(originalItem.getItemDepth());
        tempDeletedItem.setOriginalDepth(originalItem.getOriginalDepth());
        originalItem.getChildItems().forEach(child -> tempDeletedItem.addChildItem(copyDeletedItemToTemp(child, Boolean.FALSE)));
        return tempDeletedItem;
    }

    protected TocDropResult validateAgainstSoftDeletedOrMoveToItems(List<TableOfContentItemVO> droppedItems, TableOfContentItemVO targetItem, TableOfContentItemVO parentItem, ItemPosition position) {

        // Check if there are no soft deleted items at first level(not in children) in dropped items
        TocDropResult tocDropResult = new TocDropResult(true, "toc.edit.window.drop.success.message", droppedItems.get(0), targetItem);
        boolean originalFound = false;
        for (TableOfContentItemVO sourceItem : droppedItems) {
            if (isSoftDeletedOrMoveToItem(sourceItem)) {
                tocDropResult.setSuccess(false);
                tocDropResult.setMessageKey("toc.edit.window.drop.error.softdeleted.source.message");
                tocDropResult.setSourceItem(sourceItem);
                return tocDropResult;
            } else if (isPlaceholderForDroppedItem(targetItem, sourceItem)) {
                //if the target is the placeholder for one of the source items skip target validation
                originalFound = true;
            }
        }
        if(!originalFound) {
            tocDropResult.setSuccess(!isSoftDeletedOrMoveToItem(ItemPosition.AS_CHILDREN.equals(position) && targetItem.getTocItem().isChildrenAllowed() ? targetItem : parentItem));
            tocDropResult.setMessageKey("toc.edit.window.drop.error.softdeleted.target.message");
        }
        return tocDropResult;
    }

    private boolean isSoftDeletedOrMoveToItem(TableOfContentItemVO item) {
        return (hasTocItemSoftAction(item, DELETE) || hasTocItemSoftAction(item, MOVE_TO));
    }
}
