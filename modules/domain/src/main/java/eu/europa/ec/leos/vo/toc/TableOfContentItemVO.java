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
package eu.europa.ec.leos.vo.toc;

import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Stream;

import static eu.europa.ec.leos.util.LeosDomainUtil.addFieldIfNotNull;
import static eu.europa.ec.leos.util.LeosDomainUtil.addListFieldIfNotNull;
import static eu.europa.ec.leos.util.LeosDomainUtil.calculateLeftPadd;

public class TableOfContentItemVO implements Serializable {
    
    public static final long serialVersionUID = -1;

    private TocItem tocItem;
    private String id;
    private String originAttr;
    private String number;
    private String originNumAttr;
    private String heading;
    private String originHeadingAttr;
    private String content;
    private Object numTagIndex;
    private Object headingTagIndex;
    private Object introTagIndex;
    private Object vtdIndex;
    private Object listTagIndex;
    private String list;
    private boolean movedOnEmptyParent;
    private boolean undeleted;
    private boolean isBlock;
    private boolean isCrossHeading;
    private boolean isCrossHeadingInList;

    private final List<TableOfContentItemVO> childItems = new ArrayList<>();
    private TableOfContentItemVO parentItem;
    private SoftActionType softActionAttr;
    private Boolean isSoftActionRoot;
    private String softMoveTo;
    private String softMoveFrom;
    private String softTransFrom;
    private String softUserAttr;
    private GregorianCalendar softDateAttr;
    private final List<CoEditionVO> coEditionVos = new ArrayList<>();
    private boolean isAffected;
    private Boolean isNumberingToggled;
    private SoftActionType numSoftActionAttr;
    private SoftActionType headingSoftActionAttr;
    private Boolean restored;
    private int itemDepth;
    private int originalIndentLevel;
    private String elementNumberId;

    private IndentedItemType indentOriginType = null;
    private int indentOriginIndentLevel = -1;
    private String indentOriginNumId = null;
    private String indentOriginNumValue = null;
    private String indentOriginNumOrigin = null;

    private String listType = null;

    public TableOfContentItemVO(TocItem tocItem, String id, String originAttr, String number, String originNumAttr, String heading, Object numTagIndex,
            Object headingTagIndex, Object introTagIndex, Object vtdIndex, String content) {
        this.tocItem = tocItem;
        this.id = id;
        this.originAttr = originAttr;
        this.number = number;
        this.originNumAttr = originNumAttr;
        this.heading = heading;
        this.numTagIndex = numTagIndex;
        this.headingTagIndex = headingTagIndex;
        this.introTagIndex = introTagIndex;
        this.vtdIndex = vtdIndex;
        this.content = content;
        this.isAffected = false;
        this.itemDepth = 0;
        this.originalIndentLevel = 0;
    }

    public TableOfContentItemVO(TocItem tocItem, String id, String originAttr, String number, String originNumAttr, String heading, Object numTagIndex,
            Object headingTagIndex, Object introTagIndex, Object vtdIndex, String list, Object listTagIndex, String content, SoftActionType softActionAttr, Boolean isSoftActionRoot, String softUserAttr, GregorianCalendar softDateAttr) {
        this(tocItem, id, originAttr, number, originNumAttr, heading, numTagIndex, headingTagIndex, introTagIndex, vtdIndex, content);
        this.list = list;
        this.listTagIndex = listTagIndex;
        this.softActionAttr = softActionAttr;
        this.isSoftActionRoot = isSoftActionRoot;
        this.softUserAttr = softUserAttr;
        this.softDateAttr = softDateAttr;
    }

    public TableOfContentItemVO(TocItem tocItem, String id, String originAttr, String number, String originNumAttr, String heading, Object numTagIndex,
            Object headingTagIndex, Object introTagIndex, Object vtdIndex, String list, Object listTagIndex, String content, SoftActionType softActionAttr, Boolean isSoftActionRoot, String softUserAttr, GregorianCalendar softDateAttr,
            String softMoveFrom, String softMoveTo, String softTransFrom, boolean undeleted,SoftActionType numSoftActionAttr) {
        this(tocItem, id, originAttr, number, originNumAttr, heading, numTagIndex, headingTagIndex, introTagIndex, vtdIndex,
                list, listTagIndex, content, softActionAttr, isSoftActionRoot, softUserAttr, softDateAttr);
        this.softMoveFrom = softMoveFrom;
        this.softMoveTo = softMoveTo;
        this.softTransFrom = softTransFrom;
        this.undeleted = undeleted;
        this.numSoftActionAttr = numSoftActionAttr;
    }
    
    public TableOfContentItemVO(TocItem tocItem, String id, String originAttr, String number, String originNumAttr, String heading, String originHeadingAttr,
            Object numTagIndex, Object headingTagIndex, Object introTagIndex, Object vtdIndex, String list, Object listTagIndex, String content, SoftActionType softActionAttr, Boolean isSoftActionRoot, String softUserAttr,
            GregorianCalendar softDateAttr, String softMoveFrom, String softMoveTo, String softTransFrom, boolean undeleted, SoftActionType numSoftActionAttr, SoftActionType headingSoftActionAttr, int itemDepth,
            String elementNumberId, IndentedItemType indentOriginType, Integer indentOriginIndentLevel, String indentOriginNumId, String indentOriginNumValue, String indentOriginNumOrigin, String listType) {
        this(tocItem, id, originAttr, number, originNumAttr, heading, numTagIndex, headingTagIndex, introTagIndex, vtdIndex,
                list, listTagIndex, content, softActionAttr, isSoftActionRoot, softUserAttr, softDateAttr);
        this.softMoveFrom = softMoveFrom;
        this.softMoveTo = softMoveTo;
        this.softTransFrom = softTransFrom;
        this.undeleted = undeleted;
        this.numSoftActionAttr = numSoftActionAttr;
        this.itemDepth = itemDepth;
        this.originHeadingAttr = originHeadingAttr;
        this.headingSoftActionAttr = headingSoftActionAttr;
        this.elementNumberId = elementNumberId;
        this.indentOriginType = indentOriginType;
        this.indentOriginIndentLevel = indentOriginIndentLevel != null ? indentOriginIndentLevel : -1;
        this.indentOriginNumId = indentOriginNumId;
        this.indentOriginNumValue = indentOriginNumValue;
        this.indentOriginNumOrigin = indentOriginNumOrigin;
        this.listType = listType;
    }

    public void populateIndentInfo(IndentedItemType indentOriginType, int indentOriginIndentLevel, String indentOriginNumId
            , String indentOriginNumValue, String indentOriginNumOrigin) {
        if (!isIndented()) {
            this.indentOriginType = indentOriginType;
            this.indentOriginIndentLevel = indentOriginIndentLevel;
            this.indentOriginNumId = indentOriginNumId;
            this.indentOriginNumValue = indentOriginNumValue;
            this.indentOriginNumOrigin = indentOriginNumOrigin;
        }
    }

    public boolean isIndented() {
        return (this.indentOriginType != null && !this.indentOriginType.equals(IndentedItemType.RESTORED));
    }

    public boolean isIndentedOrRestored() {
        return (this.indentOriginType != null);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getOriginHeadingAttr() {
        return originHeadingAttr;
    }

    public void setOriginHeadingAttr(String originHeadingAttr) {
        this.originHeadingAttr = originHeadingAttr;
    }

    public Object getNumTagIndex() {
        return numTagIndex;
    }

    public void setNumTagIndex(Object numTagIndex) {
        this.numTagIndex = numTagIndex;
    }

    public Object getHeadingTagIndex() {
        return headingTagIndex;
    }

    public void setHeadingTagIndex(Object headingTagIndex) {
        this.headingTagIndex = headingTagIndex;
    }

    public String getId() {
        return id;
    }

    public String getOriginAttr() {
        return originAttr;
    }

    public void setOriginAttr(String originAttr) {
        this.originAttr = originAttr;
    }

    public String getOriginNumAttr() {
        return originNumAttr;
    }

    public void setOriginNumAttr(String originNumAttr) {
        this.originNumAttr = originNumAttr;
    }

    public TocItem getTocItem() {
        return tocItem;
    }

    public void setTocItem(TocItem tocItem) {
        this.tocItem = tocItem;
    }

    public Object getVtdIndex() {
        return vtdIndex;
    }

    public void setVtdIndex(Object vtdIndex) {
        this.vtdIndex = vtdIndex;
    }

    public Object getIntroTagIndex() {
        return introTagIndex;
    }

    public String getList() {
        return list;
    }
    
    public Object getListTagIndex() {
        return listTagIndex;
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAffected() {
        return isAffected;
    }

    public void setAffected(boolean affected) {
        isAffected = affected;
    }

    /**
     * @return the softActionAttr
     */
    public SoftActionType getSoftActionAttr() {
        return softActionAttr;
    }

    /**
     * @param softActionAttr the softActionAttr to set
     */
    public void setSoftActionAttr(SoftActionType softActionAttr) {
        this.softActionAttr = softActionAttr;
    }

    public Boolean isSoftActionRoot() {
        return isSoftActionRoot;
    }

    public void setSoftActionRoot(Boolean softActionRoot) {
        isSoftActionRoot = softActionRoot;
    }

    /**
     * @return the softUserAttr
     */
    public String getSoftUserAttr() {
        return softUserAttr;
    }

    /**
     * @param softUserAttr the softUserAttr to set
     */
    public void setSoftUserAttr(String softUserAttr) {
        this.softUserAttr = softUserAttr;
    }

    /**
     * @return the softDateAttr
     */
    public GregorianCalendar getSoftDateAttr() {
        return softDateAttr;
    }

    /**
     * @param softDateAttr the softDateAttr to set
     */
    public void setSoftDateAttr(GregorianCalendar softDateAttr) {
        this.softDateAttr = softDateAttr;
    }

    /**
     * @return the softMoveTo
     */
    public String getSoftMoveTo() {
        return softMoveTo;
    }

    /**
     * @param softMoveTo the softMoveTo to set
     */
    public void setSoftMoveTo(String softMoveTo) {
        this.softMoveTo = softMoveTo;
    }

    /**
     * @return the softMoveFrom
     */
    public String getSoftMoveFrom() {
        return softMoveFrom;
    }

    /**
     * @param softMoveFrom the softMoveFrom to set
     */
    public void setSoftMoveFrom(String softMoveFrom) {
        this.softMoveFrom = softMoveFrom;
    }

    public TableOfContentItemVO getParentItem() {
        return parentItem;
    }

    public void setParentItem(TableOfContentItemVO parentItem) {
        this.parentItem = parentItem;
    }

    public List<TableOfContentItemVO> getChildItems() {
        return childItems;
    }

    public boolean isMovedOnEmptyParent() {
        return movedOnEmptyParent;
    }

    public void setMovedOnEmptyParent(boolean movedOnEmptyParent) {
        this.movedOnEmptyParent = movedOnEmptyParent;
    }

    public Boolean isNumberingToggled() {
        return isNumberingToggled;
    }

    public void setNumberingToggled(Boolean numberingToggled) {
        this.isNumberingToggled = numberingToggled;
    }

    public Boolean isRestored() {
        return restored;
    }

    public void setRestored(Boolean restored) {
        this.restored = restored;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setBlock(boolean block) {
        isBlock = block;
    }

    public boolean isCrossHeading() {
        return isCrossHeading;
    }

    public void setCrossHeading(boolean crossHeading) {
        isCrossHeading = crossHeading;
    }

    public boolean isCrossHeadingInList() {
        return isCrossHeadingInList;
    }

    public void setCrossHeadingInList(boolean crossHeadingInList) {
        isCrossHeadingInList = crossHeadingInList;
    }

    public void addChildItem(TableOfContentItemVO tableOfContentItemVO) {
        if (tableOfContentItemVO.getTocItem().isRoot()) {
            throw new IllegalArgumentException("Cannot add a root item as a child!");
        }
        childItems.add(tableOfContentItemVO);
        tableOfContentItemVO.parentItem = this;
    }

    public void addChildItem(int index, TableOfContentItemVO tableOfContentItemVO) {
        if (tableOfContentItemVO.getTocItem().isRoot()) {
            throw new IllegalArgumentException("Cannot add a root item as a child!");
        }
        childItems.add(index, tableOfContentItemVO);
        tableOfContentItemVO.parentItem = this;
    }

    public void addAllChildItems(List<TableOfContentItemVO> tableOfContentItemVOList) {
        for (TableOfContentItemVO item : tableOfContentItemVOList) {
            addChildItem(item);
        }
    }

    public void removeChildItem(TableOfContentItemVO tableOfContentItemVO) {
        childItems.remove(tableOfContentItemVO);
    }

    public void removeAllChildItems() {
        childItems.clear();
    }

    public List<TableOfContentItemVO> getChildItemsView() {
        return Collections.unmodifiableList(new ArrayList<>(childItems));
    }
    
    public boolean containsItem(String aknTag) {
        List<TableOfContentItemVO> childItems = this.childItems;
        for(TableOfContentItemVO child : childItems) {
            if(child.getTocItem().getAknTag().value().equals(aknTag)) {
                return true;
            }
        }
        return false;
    }
    
    public List<CoEditionVO> getCoEditionVos() {
        return coEditionVos;
    }
    
    public void addUserCoEdition(CoEditionVO coEditionVO) {
        coEditionVos.add(coEditionVO);
    }
    
    public void removeAllUserCoEdition() {
        coEditionVos.clear();
    }
    
    public boolean isUndeleted() {
        return undeleted;
    }
    
    public void setUndeleted(boolean undeleted) {
        this.undeleted = undeleted;
    }
    
    public int getItemDepth() {
        return itemDepth;
    }

    public void setItemDepth(int depth) {
        this.itemDepth = depth;
    }
    
    public int getOriginalDepth() {
        return originalIndentLevel;
    }

    public void setOriginalDepth(int originalIndentLevel) {
        this.originalIndentLevel = originalIndentLevel;
    }

    public SoftActionType  getNumSoftActionAttr() {
        return numSoftActionAttr;
    }

    public void setNumSoftActionAttr(SoftActionType numSoftActionAttr ) {
        this.numSoftActionAttr = numSoftActionAttr;
    }

    public SoftActionType getHeadingSoftActionAttr() {
        return headingSoftActionAttr;
    }

    public void setHeadingSoftActionAttr(SoftActionType headingSoftActionAttr) {
        this.headingSoftActionAttr = headingSoftActionAttr;
    }

    public String getElementNumberId() {
        return elementNumberId;
    }

    public void setElementNumberId(String elementNumberId) {
        this.elementNumberId = elementNumberId;
    }

    public String getSoftTransFrom() {
        return softTransFrom;
    }

    public void setSoftTransFrom(String softTransFrom) {
        this.softTransFrom= softTransFrom;
    }

    public IndentedItemType getIndentOriginType() { return indentOriginType; }

    public void setIndentOriginType(IndentedItemType indentedItemType) { this.indentOriginType = indentedItemType; }

    public int getIndentOriginIndentLevel() { return indentOriginIndentLevel; }

    public String getIndentOriginNumId() { return indentOriginNumId; }

    public String getIndentOriginNumValue() { return indentOriginNumValue; }

    public String getIndentOriginNumOrigin() { return indentOriginNumOrigin; }
    
    public void setListTagIndex(Object listTagIndex) {
        this.listTagIndex = listTagIndex;
    }
    
    public void setList(String list) {
        this.list = list;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public String getListType() {
        return listType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableOfContentItemVO that = (TableOfContentItemVO) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (tocItem != that.tocItem) return false;
        if (vtdIndex != null ? !vtdIndex.equals(that.vtdIndex) : that.vtdIndex != null) return false;

        return true;
    }
    
    @Override
    public int hashCode() {
        int result = tocItem != null ? tocItem.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (vtdIndex != null ? vtdIndex.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return printTocAsTree(this, 0);
    }

    public String printTocAsTree(TableOfContentItemVO item, int deep) {
        // change RIGHT_CHAR=", " and LEFT_CHAR="", in case you want all in one line. Or overload the method.
        final String RIGHT_CHAR = "\n";
        final String LEFT_CHAR = "\t";
        
        String LEFT_PAD_CLASSNAME = calculateLeftPadd(deep, LEFT_CHAR);
        String LEFT_PAD = calculateLeftPadd(deep + 1, LEFT_CHAR);

        final StringBuilder sb = new StringBuilder(RIGHT_CHAR);
        sb.append(LEFT_PAD_CLASSNAME).append( "TableOfContentItemVO[").append(RIGHT_CHAR);
        addFieldIfNotNull("id", item.id, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("tocItem", item.tocItem != null ? item.tocItem.getAknTag() : null, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("vtdIndex", item.vtdIndex, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("originAttr", item.originAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("number", item.number, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("originNumAttr", item.originNumAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("heading", item.heading, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("originHeadingAttr", item.originHeadingAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("numTagIndex", item.numTagIndex, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("headingTagIndex", item.headingTagIndex, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("introTagIndex", item.introTagIndex, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("listTagIndex", item.listTagIndex, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("list", item.list, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("movedOnEmptyParent", item.movedOnEmptyParent, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("undeleted", item.undeleted, LEFT_PAD, RIGHT_CHAR, sb);
        sb.append(LEFT_PAD + "parentItem=").append(getParentString(item.parentItem)).append(RIGHT_CHAR);
        addFieldIfNotNull("softActionAttr", item.softActionAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("isSoftActionRoot", item.isSoftActionRoot, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("softMoveTo", item.softMoveTo, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("softMoveFrom", item.softMoveFrom, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("softUserAttr", item.softUserAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("softDateAttr", item.softDateAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addListFieldIfNotNull("coEditionVos", item.coEditionVos, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("isAffected", item.isAffected, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("isNumberingToggled", item.isNumberingToggled, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("numSoftActionAttr", item.numSoftActionAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("headingSoftActionAttr", item.headingSoftActionAttr, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("restored", item.restored, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("itemDepth", item.itemDepth, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("originalIndentLevel", item.originalIndentLevel, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("elementNumberId", item.elementNumberId, LEFT_PAD, RIGHT_CHAR, sb);
        
        if(item.childItems.size() > 0) {
            final StringBuilder sbChildren = new StringBuilder();
            for (TableOfContentItemVO child : item.childItems){
                sbChildren.append(printTocAsTree(child, deep + 2));
            }
            sb.append(LEFT_PAD).append("childItems=[").append(sbChildren).append(RIGHT_CHAR);
        }
        
        sb.append(LEFT_PAD_CLASSNAME).append("]");
        return sb.toString();
    }
    
    private String getParentString(TableOfContentItemVO parentItem) {
        StringBuilder sb = new StringBuilder();
        if(parentItem != null){
            sb.append("TableOfContentItemVO[");
            sb.append("id=").append(parentItem.getId());
            sb.append(", tocItem=").append(parentItem.tocItem != null ? parentItem.tocItem.getAknTag() : "null");
            sb.append("]");
        }
        return sb.toString();
    }
    
    public Stream<TableOfContentItemVO> flattened() {
        return Stream.concat(
                Stream.of(this),
                childItems.stream().flatMap(TableOfContentItemVO::flattened)
        );
    }

}
