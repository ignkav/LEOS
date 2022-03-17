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
package eu.europa.ec.leos.services.compare;

import eu.europa.ec.leos.services.support.xml.domain.Element;
import eu.europa.ec.leos.services.support.xml.domain.IntHolder;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ContentComparatorContext {

    private StringBuilder leftResultBuilder;
    private StringBuilder rightResultBuilder;
    private IntHolder modifications;
    private Integer indexOfOldElementInNewContent;
    private Integer indexOfNewElementInOldContent;
    private Element oldElement;
    private Element newElement;
    private Element intermediateElement;
    private String startTagAttrName;
    private String startTagAttrValue;
    private Boolean ignoreElements = Boolean.TRUE;
    private Object oldContentNavigator;
    private Element oldContentRoot;
    private Map<String, Element> oldContentElements;
    private Object newContentNavigator;
    private Element newContentRoot;
    private Map<String, Element> newContentElements;
    private Object intermediateContentNavigator;
    private Element intermediateContentRoot;
    private Map<String, Element> intermediateContentElements;
    private StringBuilder resultBuilder;
    private Node resultNode;
    private Boolean ignoreRenumbering = Boolean.FALSE;
    private String attrName;
    private String removedValue;
    private String addedValue;
    private String removedOriginalValue;
    private String addedOriginalValue;
    private String removedIntermediateValue;
    private String addedIntermediateValue;
    private String retainOriginalValue;
    private Boolean displayRemovedContentAsReadOnly = Boolean.FALSE;
    private Boolean threeWayDiff = Boolean.FALSE;
    private final String[] comparedVersions;
    private Boolean isDocuwriteExport = Boolean.FALSE;

    private ContentComparatorContext(String[] comparedVersions) {
        this.comparedVersions = comparedVersions;
    }

    public ContentComparatorContext resetStartTagAttribute(){
        this.startTagAttrName = null;
        this.startTagAttrValue = null;
        return this;
    }

    public StringBuilder getLeftResultBuilder() {
        return leftResultBuilder;
    }

    public ContentComparatorContext setLeftResultBuilder(StringBuilder leftResultBuilder) {
        this.leftResultBuilder = leftResultBuilder;
        return this;
    }

    public StringBuilder getRightResultBuilder() {
        return rightResultBuilder;
    }

    public ContentComparatorContext setRightResultBuilder(StringBuilder rightResultBuilder) {
        this.rightResultBuilder = rightResultBuilder;
        return this;
    }

    public IntHolder getModifications() {
        return modifications;
    }

    public ContentComparatorContext setModifications(IntHolder modifications) {
        this.modifications = modifications;
        return this;
    }

    public Element getIntermediateElement() {
        return intermediateElement;
    }

    public ContentComparatorContext setIntermediateElement(Element intermediateElement) {
        this.intermediateElement = intermediateElement;
        return this;
    }

    public Integer getIndexOfOldElementInNewContent() {
        return indexOfOldElementInNewContent;
    }

    public ContentComparatorContext setIndexOfOldElementInNewContent(Integer indexOfOldElementInNewContent) {
        this.indexOfOldElementInNewContent = indexOfOldElementInNewContent;
        return this;
    }

    public Integer getIndexOfNewElementInOldContent() {
        return indexOfNewElementInOldContent;
    }

    public ContentComparatorContext setIndexOfNewElementInOldContent(Integer indexOfNewElementInOldContent) {
        this.indexOfNewElementInOldContent = indexOfNewElementInOldContent;
        return this;
    }

    public Element getOldElement() {
        return oldElement;
    }

    public ContentComparatorContext setOldElement(Element oldElement) {
        this.oldElement = oldElement;
        return this;
    }

    public Element getNewElement() {
        return newElement;
    }

    public ContentComparatorContext setNewElement(Element newElement) {
        this.newElement = newElement;
        return this;
    }

    public String getStartTagAttrName() {
        return startTagAttrName;
    }

    public ContentComparatorContext setStartTagAttrName(String startTagAttrName) {
        this.startTagAttrName = startTagAttrName;
        return this;
    }

    public String getStartTagAttrValue() {
        return startTagAttrValue;
    }

    public ContentComparatorContext setStartTagAttrValue(String startTagAttrValue) {
        this.startTagAttrValue = startTagAttrValue;
        return this;
    }

    public Boolean getIgnoreElements() {
        return ignoreElements;
    }

    public ContentComparatorContext setIgnoreElements(Boolean ignoreElements) {
        this.ignoreElements = ignoreElements;
        return this;
    }

    public Object getOldContentNavigator() {
        return oldContentNavigator;
    }

    public ContentComparatorContext setOldContentNavigator(Object oldContentNavigator) {
        this.oldContentNavigator = oldContentNavigator;
        return this;
    }

    public Element getOldContentRoot() {
        return oldContentRoot;
    }

    public ContentComparatorContext setOldContentRoot(Element oldContentRoot) {
        this.oldContentRoot = oldContentRoot;
        return this;
    }

    public Map<String, Element> getOldContentElements() {
        return oldContentElements;
    }

    public ContentComparatorContext setOldContentElements(Map<String, Element> oldContentElements) {
        this.oldContentElements = oldContentElements;
        return this;
    }

    public Object getNewContentNavigator() {
        return newContentNavigator;
    }

    public ContentComparatorContext setNewContentNavigator(Object newContentNavigator) {
        this.newContentNavigator = newContentNavigator;
        return this;
    }

    public Element getNewContentRoot() {
        return newContentRoot;
    }

    public ContentComparatorContext setNewContentRoot(Element newContentRoot) {
        this.newContentRoot = newContentRoot;
        return this;
    }
    public Element getIntermediateContentRoot() {
        return intermediateContentRoot;
    }

    public ContentComparatorContext setIntermediateContentRoot(Element intermediateContentRoot) {
        this.intermediateContentRoot = intermediateContentRoot;
        return this;
    }

    public Map<String, Element> getNewContentElements() {
        return newContentElements;
    }

    public ContentComparatorContext setNewContentElements(Map<String, Element> newContentElements) {
        this.newContentElements = newContentElements;
        return this;
    }

    public Object getIntermediateContentNavigator() {
        return intermediateContentNavigator;
    }

    public ContentComparatorContext setIntermediateContentNavigator(Object intermediateContentNavigator) {
        this.intermediateContentNavigator = intermediateContentNavigator;
        return this;
    }

    public Map<String, Element> getIntermediateContentElements() {
        return intermediateContentElements;
    }

    public ContentComparatorContext setIntermediateContentElements(Map<String, Element> intermediateContentElements) {
        this.intermediateContentElements = intermediateContentElements;
        return this;
    }

    public StringBuilder getResultBuilder() {
        return resultBuilder;
    }

    public ContentComparatorContext setResultBuilder(StringBuilder resultBuilder) {
        this.resultBuilder = resultBuilder;
        return this;
    }

    public Node getResultNode() {
        return resultNode;
    }

    public ContentComparatorContext setResultNode(Node resultNode) {
        this.resultNode = resultNode;
        return this;
    }

    public Boolean getIgnoreRenumbering() {
        return ignoreRenumbering;
    }

    public ContentComparatorContext setIgnoreRenumbering(Boolean ignoreRenumbering) {
        this.ignoreRenumbering = ignoreRenumbering;
        return this;
    }

    public String getAttrName() {
        return attrName;
    }

    public ContentComparatorContext setAttrName(String attrName) {
        this.attrName = attrName;
        return this;
    }

    public String getRemovedValue() {
        return removedValue;
    }

    public ContentComparatorContext setRemovedValue(String removedValue) {
        this.removedValue = removedValue;
        return this;
    }

    public String getAddedValue() {
        return addedValue;
    }

    public ContentComparatorContext setAddedValue(String addedValue) {
        this.addedValue = addedValue;
        return this;
    }

    public String getRemovedOriginalValue() {
        return removedOriginalValue;
    }

    public ContentComparatorContext setRemovedOriginalValue(String removedOriginalValue) {
        this.removedOriginalValue = removedOriginalValue;
        return this;
    }

    public String getAddedOriginalValue() {
        return addedOriginalValue;
    }

    public ContentComparatorContext setAddedOriginalValue(String addedOriginalValue) {
        this.addedOriginalValue = addedOriginalValue;
        return this;
    }

    public String getRemovedIntermediateValue() {
        return removedIntermediateValue;
    }

    public ContentComparatorContext setRemovedIntermediateValue(String removedIntermediateValue) {
        this.removedIntermediateValue = removedIntermediateValue;
        return this;
    }

    public String getAddedIntermediateValue() {
        return addedIntermediateValue;
    }

    public ContentComparatorContext setAddedIntermediateValue(String addedIntermediateValue) {
        this.addedIntermediateValue = addedIntermediateValue;
        return this;
    }
    
    public ContentComparatorContext setRetainOriginalValue(String retainOriginalValue) {
        this.retainOriginalValue = retainOriginalValue;
        return this;
    }
    
    public String getRetainOriginalValue() {
        return retainOriginalValue;
    }

    public Boolean getDisplayRemovedContentAsReadOnly() {
        return displayRemovedContentAsReadOnly;
    }

    public ContentComparatorContext setDisplayRemovedContentAsReadOnly(Boolean displayRemovedContentAsReadOnly) {
        this.displayRemovedContentAsReadOnly = displayRemovedContentAsReadOnly;
        return this;
    }

    public Boolean getIsDocuwriteExport() {
        return isDocuwriteExport;
    }

    public ContentComparatorContext setIsDocuwriteExport(Boolean isDocuwriteExport) {
        this.isDocuwriteExport = isDocuwriteExport;
        return this;
    }

    public Boolean getThreeWayDiff() {
        return threeWayDiff;
    }

    public ContentComparatorContext setThreeWayDiff(Boolean threeWayDiff) {
        this.threeWayDiff = threeWayDiff;
        return this;
    }

    public String[] getComparedVersions() {
        return comparedVersions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentComparatorContext that = (ContentComparatorContext) o;
        return Objects.equals(leftResultBuilder.toString(), that.leftResultBuilder.toString()) &&
                Objects.equals(rightResultBuilder.toString(), that.rightResultBuilder.toString()) &&
                Objects.equals(modifications, that.modifications) &&
                Objects.equals(indexOfOldElementInNewContent, that.indexOfOldElementInNewContent) &&
                Objects.equals(indexOfNewElementInOldContent, that.indexOfNewElementInOldContent) &&
                Objects.equals(oldElement, that.oldElement) &&
                Objects.equals(newElement, that.newElement) &&
                Objects.equals(intermediateElement, that.intermediateElement) &&
                Objects.equals(startTagAttrName, that.startTagAttrName) &&
                Objects.equals(startTagAttrValue, that.startTagAttrValue) &&
                Objects.equals(ignoreElements, that.ignoreElements) &&
                Objects.equals(oldContentNavigator, that.oldContentNavigator) &&
                Objects.equals(oldContentRoot, that.oldContentRoot) &&
                Objects.equals(oldContentElements, that.oldContentElements) &&
                Objects.equals(newContentNavigator, that.newContentNavigator) &&
                Objects.equals(newContentRoot, that.newContentRoot) &&
                Objects.equals(newContentElements, that.newContentElements) &&
                Objects.equals(intermediateContentNavigator, that.intermediateContentNavigator) &&
                Objects.equals(intermediateContentRoot, that.intermediateContentRoot) &&
                Objects.equals(intermediateContentElements, that.intermediateContentElements) &&
                Objects.equals(resultBuilder.toString(), that.resultBuilder.toString()) &&
                Objects.equals(ignoreRenumbering, that.ignoreRenumbering) &&
                Objects.equals(attrName, that.attrName) &&
                Objects.equals(removedValue, that.removedValue) &&
                Objects.equals(addedValue, that.addedValue) &&
                Objects.equals(removedOriginalValue, that.removedOriginalValue) &&
                Objects.equals(addedOriginalValue, that.addedOriginalValue) &&
                Objects.equals(removedIntermediateValue, that.removedIntermediateValue) &&
                Objects.equals(addedIntermediateValue, that.addedIntermediateValue) &&
                Objects.equals(retainOriginalValue, that.retainOriginalValue) &&
                Objects.equals(displayRemovedContentAsReadOnly, that.displayRemovedContentAsReadOnly) &&
                Objects.equals(isDocuwriteExport, that.isDocuwriteExport) &&
                Objects.equals(threeWayDiff, that.threeWayDiff) &&
                Arrays.equals(comparedVersions, that.comparedVersions);
    }

    @Override
    public int hashCode() {
        int result = Objects
                .hash(leftResultBuilder.toString(), rightResultBuilder.toString(), modifications, indexOfOldElementInNewContent, indexOfNewElementInOldContent,
                        oldElement, newElement,
                        intermediateElement, startTagAttrName, startTagAttrValue, ignoreElements, oldContentNavigator, oldContentRoot, oldContentElements,
                        newContentNavigator, newContentRoot, newContentElements, intermediateContentNavigator, intermediateContentRoot,
                        intermediateContentElements,
                        resultBuilder.toString(), ignoreRenumbering, attrName, removedValue, addedValue,
                        removedOriginalValue, addedOriginalValue, removedIntermediateValue, addedIntermediateValue,
                        retainOriginalValue, displayRemovedContentAsReadOnly, threeWayDiff, isDocuwriteExport);
        result = 31 * result + Arrays.hashCode(comparedVersions);
        return result;
    }

    @Override public String toString() {
        return "ContentComparatorContext{" +
                "leftResultBuilder=" + leftResultBuilder +
                ", rightResultBuilder=" + rightResultBuilder +
                ", modifications=" + modifications +
                ", indexOfOldElementInNewContent=" + indexOfOldElementInNewContent +
                ", indexOfNewElementInOldContent=" + indexOfNewElementInOldContent +
                ", oldElement=" + oldElement +
                ", newElement=" + newElement +
                ", intermediateElement=" + intermediateElement +
                ", startTagAttrName='" + startTagAttrName + '\'' +
                ", startTagAttrValue='" + startTagAttrValue + '\'' +
                ", ignoreElements=" + ignoreElements +
                ", oldContentNavigator=" + oldContentNavigator +
                ", oldContentRoot=" + oldContentRoot +
                ", oldContentElements=" + oldContentElements +
                ", newContentNavigator=" + newContentNavigator +
                ", newContentRoot=" + newContentRoot +
                ", newContentElements=" + newContentElements +
                ", intermediateContentNavigator=" + intermediateContentNavigator +
                ", intermediateContentRoot=" + intermediateContentRoot +
                ", intermediateContentElements=" + intermediateContentElements +
                ", resultBuilder=" + resultBuilder +
                ", ignoreRenumbering=" + ignoreRenumbering +
                ", attrName='" + attrName + '\'' +
                ", removedValue='" + removedValue + '\'' +
                ", addedValue='" + addedValue + '\'' +
                ", removedOriginalValue='" + removedOriginalValue + '\'' +
                ", addedOriginalValue='" + addedOriginalValue + '\'' +
                ", removedIntermediateValue='" + removedIntermediateValue + '\'' +
                ", addedIntermediateValue='" + addedIntermediateValue + '\'' +
                ", retainOriginalValue='" + retainOriginalValue + '\'' +
                ", displayRemovedContentAsReadOnly=" + displayRemovedContentAsReadOnly +
                ", isDocuwriteExport =" + isDocuwriteExport +
                ", threeWayDiff=" + threeWayDiff +
                ", comparedVersions=" + Arrays.toString(comparedVersions) +
                '}';
    }

    public static class Builder {

        private StringBuilder leftResultBuilder;
        private StringBuilder rightResultBuilder;
        private IntHolder modifications;
        private Integer indexOfOldElementInNewContent;
        private Integer indexOfNewElementInOldContent;
        private Element oldElement;
        private Element newElement;
        private Element intermediateElement;
        private String startTagAttrName;
        private String startTagAttrValue;
        private Boolean ignoreElements = Boolean.TRUE;
        private Object oldContentNavigator;
        private Element oldContentRoot;
        private Map<String, Element> oldContentElements;
        private Object newContentNavigator;
        private Element newContentRoot;
        private Element intermediateContentRoot;
        private Map<String, Element> newContentElements;
        private Object intermediateContentNavigator;
        private Map<String, Element> intermediateContentElements;
        private StringBuilder resultBuilder;
        private Node resultNode;
        private Boolean ignoreRenumbering = Boolean.FALSE;
        private String attrName;
        private String removedValue;
        private String addedValue;
        private String removedOriginalValue;
        private String addedOriginalValue;
        private String removedIntermediateValue;
        private String addedIntermediateValue;
        private String retainOriginalValue;
        private Boolean displayRemovedContentAsReadOnly = Boolean.FALSE;
        private Boolean threeWayDiff = Boolean.FALSE;
        private final String[] comparedVersions;
        private Boolean isDocuwriteExport = Boolean.FALSE;

        public Builder(ContentComparatorContext anotherContext) {
            this.leftResultBuilder = anotherContext.leftResultBuilder;
            this.rightResultBuilder = anotherContext.rightResultBuilder;
            this.modifications = anotherContext.modifications;
            this.indexOfOldElementInNewContent = anotherContext.indexOfOldElementInNewContent;
            this.indexOfNewElementInOldContent = anotherContext.indexOfNewElementInOldContent;
            this.oldElement = anotherContext.oldElement;
            this.newElement = anotherContext.newElement;
            this.intermediateElement = anotherContext.intermediateElement;
            this.startTagAttrName = anotherContext.startTagAttrName;
            this.startTagAttrValue = anotherContext.startTagAttrValue;
            this.ignoreElements = anotherContext.ignoreElements;
            this.oldContentNavigator = anotherContext.oldContentNavigator;
            this.oldContentRoot = anotherContext.oldContentRoot;
            this.oldContentElements = anotherContext.oldContentElements;
            this.newContentNavigator = anotherContext.newContentNavigator;
            this.newContentRoot = anotherContext.newContentRoot;
            this.newContentElements = anotherContext.newContentElements;
            this.intermediateContentNavigator = anotherContext.intermediateContentNavigator;
            this.intermediateContentRoot = anotherContext.intermediateContentRoot;
            this.intermediateContentElements = anotherContext.intermediateContentElements;
            this.resultBuilder = anotherContext.resultBuilder;
            this.resultNode = anotherContext.resultNode;
            this.ignoreRenumbering = anotherContext.ignoreRenumbering;
            this.attrName = anotherContext.attrName;
            this.removedValue = anotherContext.removedValue;
            this.addedValue = anotherContext.addedValue;
            this.removedOriginalValue = anotherContext.removedOriginalValue;
            this.addedOriginalValue = anotherContext.addedOriginalValue;
            this.removedIntermediateValue = anotherContext.removedIntermediateValue;
            this.addedIntermediateValue = anotherContext.addedIntermediateValue;
            this.retainOriginalValue = anotherContext.retainOriginalValue;
            this.displayRemovedContentAsReadOnly = anotherContext.displayRemovedContentAsReadOnly;
            this.threeWayDiff = anotherContext.threeWayDiff;
            this.comparedVersions = anotherContext.comparedVersions;
            this.isDocuwriteExport = anotherContext.isDocuwriteExport;
        }

        public Builder(String firstVersion, String lastVersion, String... intermediateVersions) {
            this.comparedVersions = new String[]{firstVersion, lastVersion, intermediateVersions.length > 0 ? intermediateVersions[0] : null};
        }

        public Builder withNoStartTagAttribute(){
            this.startTagAttrName = null;
            this.startTagAttrValue = null;
            return this;
        }

        public Builder withLeftResultBuilder(StringBuilder leftResultBuilder) {
            this.leftResultBuilder = leftResultBuilder;
            return this;
        }

        public Builder withRightResultBuilder(StringBuilder rightResultBuilder) {
            this.rightResultBuilder = rightResultBuilder;
            return this;
        }

        public Builder withModifications(IntHolder modifications) {
            this.modifications = modifications;
            return this;
        }

        public Builder withIndexOfOldElementInNewContent(Integer indexOfOldElementInNewContent) {
            this.indexOfOldElementInNewContent = indexOfOldElementInNewContent;
            return this;
        }

        public Builder withIndexOfNewElementInOldContent(Integer indexOfNewElementInOldContent) {
            this.indexOfNewElementInOldContent = indexOfNewElementInOldContent;
            return this;
        }

        public Builder withOldElement(Element oldElement) {
            this.oldElement = oldElement;
            return this;
        }

        public Builder withNewElement(Element newElement) {
            this.newElement = newElement;
            return this;
        }

        public Builder withIntermediateElement(Element intermediateElement) {
            this.intermediateElement = intermediateElement;
            return this;
        }

        public Builder withStartTagAttrName(String startTagAttrName) {
            this.startTagAttrName = startTagAttrName;
            return this;
        }

        public Builder withStartTagAttrValue(String startTagAttrValue) {
            this.startTagAttrValue = startTagAttrValue;
            return this;
        }

        public Builder withIgnoreElements(Boolean ignoreElements) {
            this.ignoreElements = ignoreElements;
            return this;
        }

        public Builder withOldContentNavigator(Object oldContentNavigator) {
            this.oldContentNavigator = oldContentNavigator;
            return this;
        }

        public Builder withOldContentRoot(Element oldContentRoot) {
            this.oldContentRoot = oldContentRoot;
            return this;
        }

        public Builder withOldContentElements(Map<String, Element> oldContentElements) {
            this.oldContentElements = oldContentElements;
            return this;
        }

        public Builder withNewContentNavigator(Object newContentNavigator) {
            this.newContentNavigator = newContentNavigator;
            return this;
        }

        public Builder withNewContentRoot(Element newContentRoot) {
            this.newContentRoot = newContentRoot;
            return this;
        }

        public Builder withIntermediateContentRoot(Element intermediateContentRoot) {
            this.intermediateContentRoot = intermediateContentRoot;
            return this;
        }

        public Builder withNewContentElements(Map<String, Element> newContentElements) {
            this.newContentElements = newContentElements;
            return this;
        }

        public Builder withIntermediateContentNavigator(Object intermediateContentNavigator) {
            this.intermediateContentNavigator = intermediateContentNavigator;
            return this;
        }

        public Builder withIntermediateContentElements(Map<String, Element> intermediateContentElements) {
            this.intermediateContentElements = intermediateContentElements;
            return this;
        }

        public Builder withResultBuilder(StringBuilder resultBuilder) {
            this.resultBuilder = resultBuilder;
            return this;
        }

        public Builder withResultNode(Node resultNode) {
            this.resultNode = resultNode;
            return this;
        }

        public Builder withIgnoreRenumbering(Boolean ignoreRenumbering) {
            this.ignoreRenumbering = ignoreRenumbering;
            return this;
        }

        public Builder withAttrName(String attrName) {
            this.attrName = attrName;
            return this;
        }

        public Builder withRemovedValue(String removedValue) {
            this.removedValue = removedValue;
            return this;
        }

        public Builder withAddedValue(String addedValue) {
            this.addedValue = addedValue;
            return this;
        }

        public Builder withRemovedOriginalValue(String removedOriginalValue) {
            this.removedOriginalValue = removedOriginalValue;
            return this;
        }

        public Builder withAddedOriginalValue(String addedOriginalValue) {
            this.addedOriginalValue = addedOriginalValue;
            return this;
        }

        public Builder withRemovedIntermediateValue(String removedIntermediateValue) {
            this.removedIntermediateValue = removedIntermediateValue;
            return this;
        }

        public Builder withAddedIntermediateValue(String addedIntermediateValue) {
            this.addedIntermediateValue = addedIntermediateValue;
            return this;
        }
        
        public Builder withRetainOriginalValue(String retainOriginalValue) {
            this.retainOriginalValue = retainOriginalValue;
            return this;
        }
        
        public Builder withDisplayRemovedContentAsReadOnly(Boolean displayRemovedContentAsReadOnly) {
            this.displayRemovedContentAsReadOnly = displayRemovedContentAsReadOnly;
            return this;
        }
        
        public Builder withDocuwriteExport(Boolean isDocuwriteExport) {
            this.isDocuwriteExport = isDocuwriteExport;
            return this;
        }

        public Builder withThreeWayDiff(Boolean threeWayDiff) {
            this.threeWayDiff = threeWayDiff;
            return this;
        }

        public ContentComparatorContext build() {
            ContentComparatorContext context = new ContentComparatorContext(this.comparedVersions);

            context.leftResultBuilder = this.leftResultBuilder;
            context.rightResultBuilder = this.rightResultBuilder;
            context.modifications = this.modifications;
            context.indexOfOldElementInNewContent = this.indexOfOldElementInNewContent;
            context.indexOfNewElementInOldContent = this.indexOfNewElementInOldContent;
            context.oldElement = this.oldElement;
            context.newElement = this.newElement;
            context.intermediateElement = this.intermediateElement;
            context.startTagAttrName = this.startTagAttrName;
            context.startTagAttrValue = this.startTagAttrValue;
            context.ignoreElements = this.ignoreElements;
            context.oldContentNavigator = this.oldContentNavigator;
            context.oldContentRoot = this.oldContentRoot;
            context.oldContentElements = this.oldContentElements;
            context.newContentNavigator = this.newContentNavigator;
            context.newContentRoot = this.newContentRoot;
            context.newContentElements = this.newContentElements;
            context.intermediateContentNavigator = this.intermediateContentNavigator;
            context.intermediateContentRoot = this.intermediateContentRoot;
            context.intermediateContentElements = this.intermediateContentElements;
            context.resultBuilder = this.resultBuilder;
            context.resultNode = this.resultNode;
            context.ignoreRenumbering = this.ignoreRenumbering;
            context.attrName = this.attrName;
            context.removedValue = this.removedValue;
            context.removedOriginalValue = this.removedOriginalValue;
            context.addedOriginalValue = this.addedOriginalValue;
            context.removedIntermediateValue = this.removedIntermediateValue;
            context.addedIntermediateValue = this.addedIntermediateValue;
            context.retainOriginalValue = this.retainOriginalValue;
            context.addedValue = this.addedValue;
            context.displayRemovedContentAsReadOnly = this.displayRemovedContentAsReadOnly;
            context.isDocuwriteExport = this.isDocuwriteExport;
            context.threeWayDiff = this.threeWayDiff;

            return context;
        }
    }
}
