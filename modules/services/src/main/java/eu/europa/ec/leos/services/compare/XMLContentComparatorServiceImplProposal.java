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

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.support.xml.XmlUtils;
import eu.europa.ec.leos.services.support.xml.domain.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.ComparisonHelper.getElementFragmentAsString;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.importNodeInDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.insertOrUpdateAttributeValue;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class XMLContentComparatorServiceImplProposal extends XMLContentComparatorServiceImpl {

    protected CloneContext cloneContext;

    @Autowired
    public XMLContentComparatorServiceImplProposal(TextComparator textComparator, CloneContext cloneContext) {
        super(textComparator);
        this.cloneContext = cloneContext;
    }

    private Boolean isClonedProposal() {
        return cloneContext.isClonedProposal();
    }

    @Override
    protected void appendRemovedElementsContent(ContentComparatorContext context) {
        if (isClonedProposal()) {
            super.appendRemovedElementsContent(context);
        } else {
            String xmlFragment = getElementFragmentAsString((Node) context.getOldContentNavigator(), context.getOldElement());
            appendChangedElementsContent(Boolean.FALSE, context.getLeftResultBuilder(), context.getRightResultBuilder(), xmlFragment);
        }
    }

    @Override
    protected void appendAddedElementsContent(ContentComparatorContext context) {
        String xmlFragment = getElementFragmentAsString((Node) context.getNewContentNavigator(), context.getNewElement());
        appendChangedElementsContent(Boolean.TRUE, context.getLeftResultBuilder(), context.getRightResultBuilder(), xmlFragment);
    }

    @Override
    protected Boolean shouldDisplayRemovedContent(Element elementOldContent, int indexOfOldElementInNewContent) {
        return isElementRemovedFromContent(indexOfOldElementInNewContent);
    }

    @Override
    protected Boolean containsIgnoredElements(Node node) {
        if (isClonedProposal()) {
            return containsSoftDeleteAction(node) || containsSoftDeletePlaceholderPrefix(node) || containsSoftMoveToAction(node);
        }
        return Boolean.FALSE;
    }

    @Override
    protected boolean containsDeletedElementInNewContent(ContentComparatorContext context) {
        return Boolean.FALSE;
    }

    @Override
    protected Boolean containsAddedNonIgnoredElements(Node node) {
        if (isClonedProposal()) {
            return super.containsAddedNonIgnoredElements(node);
        }
        return Boolean.FALSE;
    }

    @Override
    protected Boolean isElementInItsOriginalPosition(Element element) {
        if (isClonedProposal()) {
            return super.isElementInItsOriginalPosition(element);
        }
        return Boolean.TRUE;
    }

    @Override
    protected Boolean shouldIgnoreElement(Element element) {
        if (isClonedProposal()) {
            return super.shouldIgnoreElement(element);
        }
        return element == null;
    }

    @Override
    protected Boolean isElementMovedOrTransformed(Element element) {
        if (isClonedProposal()) {
            return super.isElementMovedOrTransformed(element);
        }
        return Boolean.FALSE;
    }

    @Override
    protected Boolean shouldCompareElements(Element oldElement, Element newElement) {
        if (isClonedProposal()) {
            return super.shouldCompareElements(oldElement, newElement);
        }
        return Boolean.TRUE;
    }

    @Override
    protected Boolean shouldIgnoreRenumbering(Element element) {
        if (isClonedProposal()) {
            return super.shouldIgnoreRenumbering(element);
        }
        return Boolean.FALSE;
    }

    @Override
    protected Boolean isActionRoot(Element element) {
        if (isClonedProposal()) {
            return super.isActionRoot(element);
        }
        return Boolean.FALSE;
    }

    @Override
    protected Boolean shouldAppendRemovedElement(ContentComparatorContext context) {
        return Boolean.FALSE;
    }

    @Override
    protected final String getStartTagValueForAddedElement(Element newElement, Element oldElement, ContentComparatorContext context) {
        return EMPTY_STRING;
    }

    @Override
    protected Node getChangedElementContent(Node contentNavigator, Element element, String attrName, String attrValue) {
        Node node;
        if (isClonedProposal()) {
            node = super.getChangedElementContent(contentNavigator, element, attrName, attrValue);
        } else {
            node = (Node) element.getNavigationIndex();
            insertOrUpdateAttributeValue(node, attrName, attrValue);
        }
        return node;
    }

    @Override
    protected void appendAddedElementContent(ContentComparatorContext context) {
        if (isClonedProposal()) {
            appendAddedElement(context);
        } else {
            Node node = (Node) context.getNewElement().getNavigationIndex();
            getChangedElementContent(node, context.getNewElement(), context.getAttrName(), context.getAddedValue());
            context.getResultNode().appendChild(node);
        }
    }

    private void appendAddedElement(ContentComparatorContext context) {
        String newElementTagId = context.getNewElement().getTagId();
        if (context.getDisplayRemovedContentAsReadOnly() && !shouldIgnoreElement(context.getNewElement())) {
            if (newElementTagId != null) {
                if (context.getOldContentElements().containsKey(newElementTagId.replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING))) {
                    //append the soft movedFrom element content compared to the original content and ignore its renumbering
                    Element originalMovedElementInOldContent = context.getOldContentElements().get(context.getNewElement().getTagId().replace(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, EMPTY_STRING));
                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withOldElement(originalMovedElementInOldContent)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.TRUE)
                            .withIgnoreRenumbering(Boolean.TRUE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(context.getAddedValue())
                            .build());

                } else if (!newElementTagId.startsWith(SOFT_MOVE_PLACEHOLDER_ID_PREFIX)
                        && !newElementTagId.startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)
                        && !newElementTagId.startsWith(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX)) {

                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(-1)
                            .withOldElement(null)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.TRUE)
                            .withIgnoreRenumbering(Boolean.TRUE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(context.getAddedValue())
                            .build());
                }
            } else {
//              context.getResultBuilder().append(getChangedElementContent((Node) context.getNewContentNavigator(), context.getNewElement(), context.getAttrName(), context.getAddedValue()));
                Node newNode = getChangedElementContent((Node) context.getNewContentNavigator(), context.getNewElement(), context.getAttrName(), context.getAddedValue());
                context.getResultNode().appendChild(newNode);
            }
        } else {
//            context.getResultBuilder().append(getNonIgnoredChangedElementContent((Node) context.getNewContentNavigator(), context.getNewElement(), context.getAttrName(), context.getAddedValue()));
            Node newNode = getNonIgnoredChangedElementContent((Node) context.getNewContentNavigator(), context.getNewElement(), context.getAttrName(), context.getAddedValue());
            context.getResultNode().appendChild(newNode);
        }
    }

    @Override
    protected void appendRemovedElementContent(ContentComparatorContext context) {
        if (isClonedProposal()) {
            appendSoftRemovedElementContent(context);
        } else {
            Node node = (Node) context.getOldElement().getNavigationIndex();
            getChangedElementContent(node, context.getOldElement(), context.getAttrName(), context.getRemovedValue());
            node = importNodeInDocument(context.getResultNode().getOwnerDocument(), node);
            context.getResultNode().appendChild(node);
        }
    }

    @Override
    protected void appendRemovedContent(ContentComparatorContext context) {
        if (isClonedProposal()) {
            if ((containsSoftAddedAction((Node) context.getOldElement().getNavigationIndex())) ||
                    (containsSoftMoveFromAction((Node) context.getOldElement().getNavigationIndex()) && !context.getNewContentElements().containsKey(context.getOldElement().getTagId()))) {
                //If element is added in old content but deleted in the new one look for leos:softAction="add" in old element OR
                //If element contains soft action move_from in old content but deleted in new content display the move_from element as deleted
//                removedContent = getChangedElementContent((Node) context.getOldContentNavigator(), context.getOldElement(), context.getAttrName(), context.getRemovedValue());
//                context.getResultBuilder().append(addReadOnlyAttributes(context.getNewElement(), removedContent));
                Node node = getChangedElementContent((Node) context.getOldContentNavigator(), context.getOldElement(), context.getAttrName(), context.getRemovedValue());
                addReadOnlyAttributes(node);
                context.getResultNode().appendChild(node);
            } else if (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
                //If element is soft deleted in new content then print the deleted element from new content
                Element softDeletedNewElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
//                removedContent = getElementFragmentAsString((Node) context.getNewContentNavigator(), softDeletedNewElement);
//                removedContent = updateElementAttribute(xml, context.getAttrName(), context.getRemovedValue());
//                context.getResultBuilder().append(addReadOnlyAttributes(context.getNewElement(), removedContent));
                Node node = XmlUtils.getElementById((Node) context.getNewContentNavigator(), softDeletedNewElement.getTagId());
                insertOrUpdateAttributeValue(node, context.getAttrName(), context.getRemovedValue());
                addReadOnlyAttributes(node);
                context.getResultNode().appendChild(node);
            }
        } else {
            appendRemovedElementContent(context);
        }
    }

    @Override
    protected String getStartTagValueForRemovedElementFromAncestor(Element element, ContentComparatorContext context) {
        return context.getRemovedValue();
    }

    @Override
    protected Boolean shouldAddElement(Element oldElement, Map<String, Element> contentElements) {
        return Boolean.FALSE;
    }

    @Override
    protected Boolean isIgnoredElement(Element element) {
        return Boolean.FALSE;
    }

    @Override
    protected boolean isCurrentElementIgnoredInNewContent(ContentComparatorContext context) {
        return Boolean.FALSE;
    }

    @Override
    protected Boolean isCurrentElementNonIgnored(Node node) {
        if (isClonedProposal()) {
            return containsSoftMoveFromAction(node);
        }
        return Boolean.FALSE;
    }

    @Override
    protected Boolean isCurrentElementIgnored(Node node) {
        return Boolean.FALSE;
    }

    @Override
    protected int getIndexOfIgnoredElementInNewContent(ContentComparatorContext context) {
        if (isClonedProposal()) {
            return super.getIndexOfIgnoredElementInNewContent(context);
        }
        return -1;
    }

    private void appendSoftRemovedElementContent(ContentComparatorContext context) {
        if (context.getOldElement() == null) {
            return;
        }

        if (context.getDisplayRemovedContentAsReadOnly() && !shouldIgnoreElement(context.getOldElement())) {
            if (context.getOldElement().getTagId() != null) {
                if (containsSoftMoveToTransformedElement(context.getNewContentElements(), context.getOldElement())) {
                    //append the soft movedTo element content
                    Element softMovedToTansformedElement = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                            context.getOldElement().getParent().getTagId());
                    appendMovedOrTransformedContent(context, softMovedToTansformedElement);
                } else if (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                        context.getOldElement().getParent().getTagId())) {
                    //element was soft deleted, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX
                    Element softDeletedTransformedElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX +
                            context.getOldElement().getParent().getTagId());
                    int indexOfSoftDeletedElementInNewContent = getBestMatchInList(softDeletedTransformedElement.getParent().getChildren(),
                            softDeletedTransformedElement);

                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(indexOfSoftDeletedElementInNewContent)
                            .withNewElement(softDeletedTransformedElement)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.FALSE)
                            .withIgnoreRenumbering(Boolean.FALSE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForRemovedElement(softDeletedTransformedElement, context))
                            .build());
                } else if (containsSoftMoveToElement(context.getNewContentElements(), context.getOldElement())) {
                    //append the soft movedTo element content
                    appendMovedToElementWithoutContent(context);
                } else if (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())) {
                    //element was soft deleted, and it's ID was prepended with SOFT_DELETE_PLACEHOLDER_ID_PREFIX
                    Element softDeletedNewElement = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
                    int indexOfSoftDeletedElementInNewContent = getBestMatchInList(softDeletedNewElement.getParent().getChildren(), softDeletedNewElement);

                    compareElementContents(new ContentComparatorContext.Builder(context)
                            .withIndexOfOldElementInNewContent(indexOfSoftDeletedElementInNewContent)
                            .withNewElement(softDeletedNewElement)
                            .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                            .withIgnoreElements(Boolean.FALSE)
                            .withIgnoreRenumbering(Boolean.FALSE)
                            .withStartTagAttrName(context.getAttrName())
                            .withStartTagAttrValue(getStartTagValueForRemovedElement(softDeletedNewElement, context))
                            .build());
                } else if (!containsSoftTransformAction((Node) context.getNewElement().getNavigationIndex()) && !containsSoftTransformAction((Node) context.getOldElement().getNavigationIndex())
                        && !context.getNewContentElements().containsKey(context.getOldElement().getTagId())) {
                    //Element is added/present in old content but deleted from new content, so just display the deleted content
//                    String removedContent = getChangedElementContent((Node) context.getOldContentNavigator(), context.getOldElement(), context.getAttrName(),
//                            getStartTagValueForRemovedElementFromAncestor(context.getOldElement(), context));
//                    context.getResultBuilder().append(addReadOnlyAttributes(context.getNewElement(), removedContent));
                    String attrVal = getStartTagValueForRemovedElementFromAncestor(context.getOldElement(), context);
                    Node node = getChangedElementContent((Node) context.getOldContentNavigator(), context.getOldElement(), context.getAttrName(), attrVal);
                    addReadOnlyAttributes(node);
                    context.getResultNode().appendChild(node);
                }
            } else {
                appendRemovedContent(context);
            }
        } else if (!context.getNewContentElements().containsKey(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                || !context.getOldElement().getTagId().equals(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                && (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                || (context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())))) {
            //append the soft movedTo element
            appendMovedToElementWithoutContent(context);
        } else if (context.getOldElement().getTagId().equals(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                && (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())
                || context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId()))) {

            Element movedOrDeletedTransformedElement = context.getNewContentElements().get((context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())
                    ? SOFT_MOVE_PLACEHOLDER_ID_PREFIX : SOFT_DELETE_PLACEHOLDER_ID_PREFIX) + context.getOldElement().getTagId());
//            context.getResultBuilder().append(getNonIgnoredChangedElementContent((Node) context.getNewContentNavigator(), movedOrDeletedTransformedElement, context.getAttrName(), getStartTagValueForRemovedElement(movedOrDeletedTransformedElement, context)));
            String attrVal = getStartTagValueForRemovedElement(movedOrDeletedTransformedElement, context);
            Node node = getNonIgnoredChangedElementContent((Node) context.getNewContentNavigator(), movedOrDeletedTransformedElement, context.getAttrName(), attrVal);
            context.getResultNode().appendChild(node);
        }
    }

    private void appendMovedOrTransformedContent(ContentComparatorContext context, Element movedTransformedElement) {
//        StringBuilder tagContent = new StringBuilder(addReadOnlyAttributes(context.getNewElement(), getElementFragmentAsString((Node) context.getNewContentNavigator(), movedTransformedElement)));
//        insertOrUpdateAttributeValue(tagContent, context.getAttrName(), context.getRemovedValue());
//        context.getResultBuilder().append(tagContent.toString());
    }

    private final String getStartTagValueForRemovedElement(Element newElement, ContentComparatorContext context) {
        return context.getRemovedValue();
    }

    private void appendMovedToElementWithoutContent(ContentComparatorContext context) {
//        Element softMovedToElement = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
//        StringBuilder tagContent = new StringBuilder(addReadOnlyAttributes(context.getNewElement(), getElementFragmentAsString((Node) context.getNewContentNavigator(), softMovedToElement)));
//        insertOrUpdateAttributeValue(tagContent, context.getAttrName(), context.getRemovedValue());
//        context.getResultBuilder().append(tagContent.toString());
    }

    private Node getNonIgnoredChangedElementContent(Node contentNavigator, Element element, String attrName, String attrValue) {
//        String elementContent = getElementFragmentAsString(contentNavigator, element);
        Node node = XmlUtils.getElementById(contentNavigator, element.getTagId()); //TODO isn't the same as element.getNode()?
        if (!containsIgnoredElements(node)) {
            node = getChangedElementContent(contentNavigator, element, attrName, attrValue);
        } else if (!shouldIgnoreElement(element)) {
            //TODO implement for CN
//            StringBuilder nonIgoredElementContent = insertOrUpdateAttributeValue(new StringBuilder(element.getTagContent()), attrName, attrValue);
//            for (Element child : element.getChildren()){
//                if (!shouldIgnoreElement(child)) {
//                    // add child without changing the start tag
//                    nonIgoredElementContent.append(getNonIgnoredChangedElementContent(contentNavigator, child, null, null));
//                }
//            }
//            return nonIgoredElementContent.append(buildEndTag(element)).toString();
        }
        return node;
    }

    @Override
    protected int getIndexOfNewElementInOldContent(ContentComparatorContext context) {
        return getBestMatchInList(context.getOldContentRoot().getChildren(), context.getNewElement());
    }

    @Override
    protected int getIndexOfOldElementInNewContent(ContentComparatorContext context) {
        return getBestMatchInList(context.getNewContentRoot().getChildren(), context.getOldElement());
    }
}
