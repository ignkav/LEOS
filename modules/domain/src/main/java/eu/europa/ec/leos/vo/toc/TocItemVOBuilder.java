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
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class TocItemVOBuilder {

    private TocItem tocItem;
    private String id;
    private String originAttr;
    private String number;
    private String elementNumberId;
    private String originNumAttr;
    private String heading;
    private String originHeadingAttr;
    private Node numTagIndex;
    private Node headingTagIndex;
    private Node introTagIndex;
    private Node listTagIndex;
    private Node vtdIndex;
    private String content;
    private List<TableOfContentItemVO> childItems = new ArrayList<>();
    private TableOfContentItemVO parentItem;
    private int itemDepth;
    private SoftActionType softActionAttr;
    private Boolean isSoftActionRoot;
    private String softMoveFrom;
    private String softMoveTo;
    private boolean affected;
    private String list;

    private TocItemVOBuilder() {
    }

    public static TocItemVOBuilder getBuilder() {
        return new TocItemVOBuilder();
    }

    public TocItemVOBuilder withChild(TableOfContentItemVO child) {
        childItems.add(child);
        return this;
    }

    public TocItemVOBuilder withChildItems(List<TableOfContentItemVO> childItems) {
        this.childItems = childItems;
        return this;
    }

    public TocItemVOBuilder withTocItem(TocItem tocItem) {
        this.tocItem = tocItem;
        return this;
    }

    public TocItemVOBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public TocItemVOBuilder withOriginAttr(String originAttr) {
        this.originAttr = originAttr;
        return this;
    }

    public TocItemVOBuilder withNumber(String number) {
        this.number = number;
        return this;
    }
    
    public TocItemVOBuilder withElementNumberId(String elementNumberId) {
        this.elementNumberId = elementNumberId;
        return this;
    }

    public TocItemVOBuilder withOriginNumAttr(String originNumAttr) {
        this.originNumAttr = originNumAttr;
        return this;
    }

    public TocItemVOBuilder withHeading(String heading) {
        this.heading = heading;
        return this;
    }

    public TocItemVOBuilder withOriginHeadingAttr(String originHeadingAttr) {
        this.originHeadingAttr = originHeadingAttr;
        return this;
    }

    public TocItemVOBuilder withNumTagIndex(Node numTagIndex) {
        this.numTagIndex = numTagIndex;
        return this;
    }

    public TocItemVOBuilder withHeadingTagIndex(Node headingTagIndex) {
        this.headingTagIndex = headingTagIndex;
        return this;
    }

    public TocItemVOBuilder withIntroTagIndex(Node introTagIndex) {
        this.introTagIndex = introTagIndex;
        return this;
    }
    
    public TocItemVOBuilder withListTagIndex(Node listTagIndex) {
        this.listTagIndex = listTagIndex;
        return this;
    }
    
    public TocItemVOBuilder withList(String list) {
        this.list = list;
        return this;
    }

    public TocItemVOBuilder withVtdIndex(Node vtdIndex) {
        this.vtdIndex = vtdIndex;
        return this;
    }

    public TocItemVOBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public TocItemVOBuilder withItemDepth(int itemDepth) {
        this.itemDepth = itemDepth;
        return this;
    }

    public TocItemVOBuilder withSoftActionAttr(SoftActionType softActionAttr) {
        this.softActionAttr = softActionAttr;
        return this;
    }

    public TocItemVOBuilder withIsSoftActionRoot(Boolean isSoftActionRoot) {
        this.isSoftActionRoot = isSoftActionRoot;
        return this;
    }

    public TocItemVOBuilder withSoftMoveFrom(String softMoveFrom) {
        this.softMoveFrom = softMoveFrom;
        return this;
    }

    public TocItemVOBuilder withSoftMoveTo(String softMoveTo) {
        this.softMoveTo = softMoveTo;
        return this;
    }

    public TocItemVOBuilder withParentItem(TableOfContentItemVO parentItem) {
        this.parentItem = parentItem;
        return this;
    }

    public TocItemVOBuilder withAffected(boolean affected) {
        this.affected = affected;
        return this;
    }

    public TableOfContentItemVO build() {
        TableOfContentItemVO tocVO = new TableOfContentItemVO(tocItem, id, originAttr, number, originNumAttr, heading, numTagIndex,
                headingTagIndex, introTagIndex, vtdIndex, content);
        tocVO.setListTagIndex(listTagIndex);
        tocVO.setElementNumberId(elementNumberId);
        tocVO.addAllChildItems(childItems);
        tocVO.setItemDepth(itemDepth);
        tocVO.setSoftActionAttr(softActionAttr);
        tocVO.setSoftActionRoot(isSoftActionRoot);
        tocVO.setSoftMoveFrom(softMoveFrom);
        tocVO.setSoftMoveTo(softMoveTo);
        tocVO.setOriginHeadingAttr(originHeadingAttr);
        tocVO.setParentItem(parentItem);
        tocVO.setAffected(affected);
        tocVO.setList(list);
        return tocVO;
    }
}
