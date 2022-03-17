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

public class TocItemBuilder {
    protected AknTag aknTag;
    protected boolean root;
    protected Boolean higherElement;
    protected boolean draggable;
    protected boolean childrenAllowed;
    protected boolean display;
    protected OptionsType itemNumber;
    protected Boolean autoNumbering;
    protected OptionsType itemHeading;
    protected boolean itemDescription;
    protected boolean numberEditable;
    protected boolean contentDisplayed;
    protected boolean deletable;
    protected boolean numWithType;
    protected boolean expandedByDefault;
    protected boolean sameParentAsChild;
    protected NumberingType numberingType;
    protected Profiles profiles;
    protected boolean editable;
    protected String template;
    protected String maxDepth;
    protected ActionPositions actionsPosition;

    private TocItemBuilder() {
    }

    public static TocItemBuilder getBuilder() {
        return new TocItemBuilder();
    }

    public TocItemBuilder withAknTag(AknTag aknTag) {
        this.aknTag = aknTag;
        return this;
    }

    public TocItemBuilder withRoot(boolean root) {
        this.root = root;
        return this;
    }

    public TocItemBuilder withHigherElement(Boolean higherElement) {
        this.higherElement = higherElement;
        return this;
    }

    public TocItemBuilder withDraggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public TocItemBuilder withChildrenAllowed(boolean childrenAllowed) {
        this.childrenAllowed = childrenAllowed;
        return this;
    }

    public TocItemBuilder withDisplay(boolean display) {
        this.display = display;
        return this;
    }

    public TocItemBuilder withItemNumber(OptionsType itemNumber) {
        this.itemNumber = itemNumber;
        return this;
    }

    public TocItemBuilder withAutoNumbering(Boolean autoNumbering) {
        this.autoNumbering = autoNumbering;
        return this;
    }

    public TocItemBuilder withItemHeading(OptionsType itemHeading) {
        this.itemHeading = itemHeading;
        return this;
    }

    public TocItemBuilder withItemDescription(boolean itemDescription) {
        this.itemDescription = itemDescription;
        return this;
    }

    public TocItemBuilder withNumberEditable(boolean numberEditable) {
        this.numberEditable = numberEditable;
        return this;
    }

    public TocItemBuilder withContentDisplayed(boolean contentDisplayed) {
        this.contentDisplayed = contentDisplayed;
        return this;
    }

    public TocItemBuilder withDeletable(boolean deletable) {
        this.deletable = deletable;
        return this;
    }

    public TocItemBuilder withNumWithType(boolean numWithType) {
        this.numWithType = numWithType;
        return this;
    }

    public TocItemBuilder withExpandedByDefault(boolean expandedByDefault) {
        this.expandedByDefault = expandedByDefault;
        return this;
    }

    public TocItemBuilder withSameParentAsChild(boolean sameParentAsChild) {
        this.sameParentAsChild = sameParentAsChild;
        return this;
    }

    public TocItemBuilder withNumberingType(NumberingType numberingType) {
        this.numberingType = numberingType;
        return this;
    }

    public TocItemBuilder withProfiles(Profiles profiles) {
        this.profiles = profiles;
        return this;
    }

    public TocItemBuilder withEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public TocItemBuilder withTemplate(String template) {
        this.template = template;
        return this;
    }

    public TocItemBuilder withMaxDepth(String maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public TocItemBuilder withActionsPosition(ActionPositions actionsPosition) {
        this.actionsPosition = actionsPosition;
        return this;
    }

    public TocItem build() {
        TocItem tocItem = new TocItem();
        tocItem.setAknTag(aknTag);
        tocItem.setRoot(root);
        tocItem.setHigherElement(higherElement);
        tocItem.setDraggable(draggable);
        tocItem.setChildrenAllowed(childrenAllowed);
        tocItem.setDisplay(display);
        tocItem.setItemNumber(itemNumber);
        tocItem.setAutoNumbering(autoNumbering);
        tocItem.setItemHeading(itemHeading);
        tocItem.setItemDescription(itemDescription);
        tocItem.setNumberEditable(numberEditable);
        tocItem.setContentDisplayed(contentDisplayed);
        tocItem.setDeletable(deletable);
        tocItem.setNumWithType(numWithType);
        tocItem.setExpandedByDefault(expandedByDefault);
        tocItem.setSameParentAsChild(sameParentAsChild);
        tocItem.setNumberingType(numberingType);
        tocItem.setProfiles(profiles);
        tocItem.setEditable(editable);
        tocItem.setTemplate(template);
        tocItem.setMaxDepth(maxDepth);
        tocItem.setActionsPosition(actionsPosition);
        return tocItem;
    }
}
