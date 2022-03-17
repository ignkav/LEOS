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

import com.google.common.collect.Lists;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.HierarchicalQuery;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.TreeGrid;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.ActionType;
import eu.europa.ec.leos.services.support.TableOfContentHelper;
import eu.europa.ec.leos.ui.component.toc.TocDropResult;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;

import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.model.action.SoftActionType.ADD;
import static eu.europa.ec.leos.model.action.SoftActionType.DELETE;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_FROM;
import static eu.europa.ec.leos.model.action.SoftActionType.MOVE_TO;
import static eu.europa.ec.leos.services.support.TableOfContentHelper.hasTocItemSoftAction;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper.getTagValueFromTocItemVo;

@SpringComponent
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class ProposalTocEditor extends AbstractTocEditor {

    private CloneProposalMetadataVO cloneProposalMetadataVO;

    @Override
    public void setTocTreeDataFilter(boolean editionEnabled, TreeDataProvider<TableOfContentItemVO> dataProvider) {
        dataProvider.setFilter(tableOfContentItemVO -> {
            return tableOfContentItemVO.getTocItem().isDisplay();
        });
    }

    @Override
    public void setTocTreeStyling(MultiSelectTreeGrid<TableOfContentItemVO> tocTree, TreeDataProvider<TableOfContentItemVO> dataProvider) {
        tocTree.setStyleGenerator(tableOfContentItemVO -> {
            String itemSoftStyle = "";
            if(isClonedProposal()) {
                itemSoftStyle = TableOfContentHelper.getItemSoftStyle(tableOfContentItemVO);
            }
            if (tocTree.getTreeData().contains(tableOfContentItemVO) && !tableOfContentItemVO.getChildItemsView().isEmpty()) {
                int numChildren = dataProvider.getChildCount(new HierarchicalQuery<>(dataProvider.getFilter(), tableOfContentItemVO));

                if(isClonedProposal()) {
                    return numChildren > 0 ? itemSoftStyle : (itemSoftStyle + " leos-toc-no-expander-icon").trim();
                } else {
                    return numChildren <= 0 ? "leos-toc-no-expander-icon" : itemSoftStyle;
                }
            }
            return itemSoftStyle;
        });
    }

    @Override
    public boolean isDeletableItem(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tableOfContentItemVO) {
        String elementName = getTagValueFromTocItemVo(tableOfContentItemVO);
        return !(PARAGRAPH.equals(elementName) ? isLastExistingChildElement(tableOfContentItemVO) : false);
    }

    @Override
    public boolean isDeletedItem(TableOfContentItemVO tableOfContentItemVO) {
        return false;
    }

    @Override
    public boolean isUndeletableItem(TableOfContentItemVO tableOfContentItemVO) {
        return false;
    }

    @Override
    public boolean checkIfConfirmDeletion(TreeData<TableOfContentItemVO> treeData, TableOfContentItemVO tableOfContentItemVO) {
        return !treeData.getChildren(tableOfContentItemVO).isEmpty();
    }

    @Override
    public ActionType deleteItem(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO tableOfContentItemVO) {
        final ActionType actionType;
        if(isClonedProposal()) {
            if (!containsItemOfOrigin(tableOfContentItemVO, EC, LS)) {
                actionType = hardDeleteFromTree(tocTree, tableOfContentItemVO);
            } else {
                softDeleteItem(tocTree, tableOfContentItemVO, LS);
                actionType = ActionType.SOFTDELETED;
            }
        } else {
            actionType = hardDeleteFromTree(tocTree, tableOfContentItemVO);
        }
        tocTree.getDataProvider().refreshAll();
        tocTree.deselectAll();
        return actionType;
    }


    @Override
    public void undeleteItem(TreeGrid<TableOfContentItemVO> tocTree, TableOfContentItemVO tableOfContentItemVO) {
        throw new UnsupportedOperationException("Soft Delete is possible only for Mandate instance");
    }

    @Override
    public TocDropResult addOrMoveItems(final boolean isAdd, final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem, List<TocItem>> tocRules,
            final List<TableOfContentItemVO> droppedItems, final TableOfContentItemVO targetItem, final ItemPosition position) {

        TocDropResult result = validateAction(tocTree, tocRules, droppedItems, targetItem, position);
        if (result.isSuccess()) {
            TableOfContentItemVO parentItem = tocTree.getTreeData().getParent(targetItem);
            List<TableOfContentItemVO> sourceItems = ((ItemPosition.BEFORE == position) || targetItem.getTocItem().isChildrenAllowed())
                    ? droppedItems : Lists.reverse(droppedItems);
            for (TableOfContentItemVO sourceItem : sourceItems) {
                performAddOrMoveAction(isAdd, tocTree, tocRules, sourceItem, targetItem, parentItem, position);
                if (position.equals(ItemPosition.AS_CHILDREN)
                        && LEVEL.equals(sourceItem.getTocItem().getAknTag().value())
                        && LEVEL.equals(targetItem.getTocItem().getAknTag().value())) {
                    TableOfContentItemVO lastChild = getLastChildLevel(sourceItem, targetItem);
                    TableOfContentItemVO actualTargetItem = getActualTargetItem(sourceItem, targetItem, parentItem, position, true);
                    moveSourceAfterChildOrSibling(sourceItem, tocTree, actualTargetItem, lastChild);
                }
            }
            tocTree.deselectAll();
            tocTree.getDataProvider().refreshAll();
        }
        return result;
    }

    @Override
    protected void addOrMoveItem(final boolean isAdd, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
                                 final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO actualTargetItem, final ItemPosition position) {
        if(isClonedProposal()) {
            boolean isRestored = false;
            if (isAdd) {
                super.addOrMoveItem(true, sourceItem, targetItem, tocTree, actualTargetItem, position);
                moveOriginAttribute(sourceItem, targetItem);
                setNumber(sourceItem, targetItem);
                sourceItem.setSoftActionAttr(ADD);
                sourceItem.setSoftActionRoot(Boolean.TRUE);
            } else {
                handleMoveAction(sourceItem, tocTree);
                super.addOrMoveItem(false, sourceItem, targetItem, tocTree, actualTargetItem, position);
                isRestored = restoreMovedItemOrSetNumber(tocTree, sourceItem, targetItem, position);
            }
            handleLevelMove(sourceItem, targetItem, tocTree, actualTargetItem, position, isRestored);
        } else {
            super.addOrMoveItem(isAdd, sourceItem, targetItem, tocTree, actualTargetItem, position);
        }
    }

    private void moveOriginAttribute(final TableOfContentItemVO droppedElement, final TableOfContentItemVO targetElement) {
        if (isElementAndTargetOriginDifferent(droppedElement, targetElement)) {
            droppedElement.setOriginAttr(LS);
        }
        droppedElement.setOriginNumAttr(LS);
    }

    private void handleMoveAction(TableOfContentItemVO moveFromItem, TreeGrid<TableOfContentItemVO> tocTree) {
        final TreeData<TableOfContentItemVO> container = tocTree.getTreeData();
        if ((moveFromItem.getOriginAttr() != null && moveFromItem.getOriginAttr().equals(EC)) &&
                ((moveFromItem.getSoftActionAttr() == null) || ((!hasTocItemSoftAction(moveFromItem, MOVE_FROM)) &&
                        (!hasTocItemSoftAction(moveFromItem, MOVE_TO)) && (!hasTocItemSoftAction(moveFromItem, ADD))
                        && (!hasTocItemSoftAction(moveFromItem, DELETE))))) {

            TableOfContentItemVO moveToTemp = copyMovingItemToTemp(moveFromItem, Boolean.TRUE, tocTree);

            dropItemAtOriginalPosition(moveFromItem, moveToTemp, container);
            moveFromItem.setOriginNumAttr(LS);
            moveFromItem.setSoftActionRoot(Boolean.TRUE);

            TableOfContentItemVO moveToFinal = copyTempItemToFinalItem(moveToTemp);
            dropItemAtOriginalPosition(moveToTemp, moveToFinal, container);
            container.removeItem(moveToTemp);
            if (moveToTemp.getParentItem() != null) {
                moveToTemp.getParentItem().removeChildItem(moveToTemp);
            }
        }
        moveFromItem.setOriginNumAttr(LS);
        moveFromItem.setSoftActionRoot(Boolean.TRUE);

        if (MOVE_FROM.equals(moveFromItem.getSoftActionAttr())) {
            moveFromItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + moveFromItem.getId());
            TableOfContentItemVO moveToItem = getTableOfContentItemVOById(moveFromItem.getSoftMoveFrom(), container.getRootItems());
            if (moveToItem != null) {
                moveToItem.setSoftActionRoot(Boolean.TRUE);
            }
        }
    }

    private TableOfContentItemVO copyMovingItemToTemp(TableOfContentItemVO originalItem, Boolean isSoftActionRoot, TreeGrid<TableOfContentItemVO> tocTree) {
        TableOfContentItemVO moveToItem;

        moveToItem = new TableOfContentItemVO(originalItem.getTocItem(), TEMP_PREFIX + SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getId(), originalItem.getOriginAttr(), originalItem.getNumber(),
                EC, originalItem.getHeading(), originalItem.getNumTagIndex(), originalItem.getHeadingTagIndex(), originalItem.getIntroTagIndex(), originalItem.getVtdIndex(), originalItem.getList(), originalItem.getListTagIndex(), originalItem.getContent(),
                MOVE_TO, isSoftActionRoot,null, null);

        moveToItem.setSoftMoveTo(originalItem.getId());
        moveToItem.setItemDepth(originalItem.getItemDepth());
        moveToItem.setOriginalDepth(originalItem.getOriginalDepth());
        originalItem.setSoftActionAttr(MOVE_FROM);
        originalItem.setSoftActionRoot(isSoftActionRoot);
        originalItem.setSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalItem.getId());
        return moveToItem;
    }

    private boolean isClonedProposal() {
        return cloneProposalMetadataVO != null && cloneProposalMetadataVO.isClonedProposal();
    }

    protected TocDropResult validateAction(final TreeGrid<TableOfContentItemVO> tocTree, final Map<TocItem,
            List<TocItem>> tableOfContentRules, final List<TableOfContentItemVO> droppedItems,
                                           final TableOfContentItemVO targetItem, final ItemPosition position) {
        if(isClonedProposal()) {
            TocDropResult result = validateAgainstSoftDeletedOrMoveToItems(droppedItems, targetItem, tocTree.getTreeData().getParent(targetItem), position);
            if (result.isSuccess()) {
                return super.validateAction(tocTree, tableOfContentRules, droppedItems, targetItem, position);
            } else {
                return result;
            }
        }
        return super.validateAction(tocTree, tableOfContentRules, droppedItems, targetItem, position);
    }

    @Override
    protected boolean validateAddingToItem(final TocDropResult result, final TableOfContentItemVO sourceItem, final TableOfContentItemVO targetItem,
            final TreeGrid<TableOfContentItemVO> tocTree, final TableOfContentItemVO actualTargetItem, final ItemPosition position) {
        return true;
    }

    @Override
    public String getNotDeletableMessageKey() {
        return "toc.edit.window.not.deletable.message";
    }

    @Override
    public TableOfContentItemVO getSimplifiedTocItem(TableOfContentItemVO item) {
        return item;
    }

    @Override
    public void populateCloneProposalMetadataVO(CloneProposalMetadataVO cloneProposalMetadataVO) {
        this.cloneProposalMetadataVO = cloneProposalMetadataVO;
    }
}
