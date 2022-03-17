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

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.support.xml.XmlUtils;
import eu.europa.ec.leos.services.support.xml.domain.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static eu.europa.ec.leos.services.support.xml.ComparisonHelper.buildElement;
import static eu.europa.ec.leos.services.support.xml.ComparisonHelper.getContentFragmentAsString;
import static eu.europa.ec.leos.services.support.xml.ComparisonHelper.getElementFragmentAsString;
import static eu.europa.ec.leos.services.support.xml.ComparisonHelper.isElementContentEqual;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CLOSE_TAG;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_DELETABLE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_EDITABLE_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.OPEN_END_TAG;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.UTF_8;

public abstract class XMLContentComparatorServiceImpl implements ContentComparatorService {

    private static final Logger LOG = LoggerFactory.getLogger(XMLContentComparatorServiceImpl.class);

    private final String BLOCK_MODIFIED_LINE_START_TAG = "<span class=\"" + CONTENT_BLOCK_MODIFIED_CLASS + "\">";
    private final String MODIFIED_LINE_MARKER_PART_1 = "<input type=\"hidden\" name=\"modification_";
    private final String MODIFIED_LINE_MARKER_PART_2 = "\"/>";
    private static final String END_TAG = "</span>";

    protected static final String LEOS_SOFT_ACTION_DEL = LEOS_SOFT_ACTION_ATTR + "=\"" + SoftActionType.DELETE.getSoftAction() + "\"";
    protected static final String LEOS_SOFT_ACTION_MOVE_FROM = LEOS_SOFT_ACTION_ATTR + "=\"" + SoftActionType.MOVE_FROM.getSoftAction() + "\"";
    protected static final String LEOS_SOFT_ACTION_MOVE_TO = LEOS_SOFT_ACTION_ATTR + "=\"" + SoftActionType.MOVE_TO.getSoftAction() + "\"";
    protected static final String LEOS_SOFT_ACTION_ADD = LEOS_SOFT_ACTION_ATTR + "=\"" + SoftActionType.ADD.getSoftAction() + "\"";
    protected static final String LEOS_SOFT_ACTION_TRANSFORM = LEOS_SOFT_ACTION_ATTR + "=\"" + SoftActionType.TRANSFORM.getSoftAction() + "\"";
    protected static final String LEOS_SOFT_ACTION_ROOT = LEOS_SOFT_ACTION_ROOT_ATTR + "=\"" + Boolean.TRUE.toString() + "\"";

    protected TextComparator textComparator;

    @Autowired
    public XMLContentComparatorServiceImpl(TextComparator textComparator) {
        this.textComparator = textComparator;
    }

    @Override
    public String compareContents(ContentComparatorContext context) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        context.setResultBuilder(new StringBuilder());

        final String oldXml = context.getComparedVersions()[0];
        final String newXml = context.getComparedVersions()[1];
        final Node oldNode = XmlUtils.createDocument(oldXml.getBytes(UTF_8), false).getDocumentElement();
        final Node newNode = XmlUtils.createDocument(newXml.getBytes(UTF_8), false).getDocumentElement();

        setResultNode(context, newNode);
        context.setOldContentElements(new HashMap<>());
        context.setNewContentElements(new HashMap<>());

        final Element oldElement = buildElement(oldNode, new HashMap<>(), context.getOldContentElements());
        final Element newElement = buildElement(newNode, new HashMap<>(), context.getNewContentElements());

        context.setOldContentNavigator(oldNode)
                .setNewContentNavigator(newNode)
                .setOldContentRoot(oldElement)
                .setNewContentRoot(newElement);

        computeDifferencesAtNodeLevel(context);

        LOG.debug("Comparison finished!  ({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return XmlUtils.nodeToString(context.getResultNode());
    }

    private void setResultNode(ContentComparatorContext context, Node newContentNode) {
        newContentNode = XmlUtils.importNodeInDocument(newContentNode.getOwnerDocument(), newContentNode);
        newContentNode.setTextContent("");
        context.setResultNode(newContentNode);
    }

    private void computeDifferencesAtNodeLevel(ContentComparatorContext context) {
        if (shouldIgnoreElement(context.getOldContentRoot())) {
            return;
        }

        int oldContentChildIndex = 0; // current index in oldContentRoot children list
        int newContentChildIndex = 0; // current index in newContentRoot children list
        int intermediateContentChildIndex = 0; // current index in intermediateContentRoot children list

        while (context.getOldContentRoot() != null
                && oldContentChildIndex < context.getOldContentRoot().getChildren().size()
                && newContentChildIndex < context.getNewContentRoot().getChildren().size()) {
            context.setOldElement(context.getOldContentRoot().getChildren().get(oldContentChildIndex))
                    .setNewElement(context.getNewContentRoot().getChildren().get(newContentChildIndex))
                    .setIndexOfOldElementInNewContent(getIndexOfOldElementInNewContent(context))
                    .setIndexOfNewElementInOldContent(getIndexOfNewElementInOldContent(context));

//            LOG.debug("{} was in position {} in oldDoc, now is in position {} in newDoc; {} previously in position {} in oldDoc, now is in position {} in newDoc;",
//                    context.getNewElement().getTagId(),
//                    context.getIndexOfNewElementInOldContent(),
//                    newContentChildIndex,
//                    context.getOldElement().getTagId(),
//                    oldContentChildIndex,
//                    context.getIndexOfOldElementInNewContent());

            // at each step, check for a particular structural change in this order
            if (shouldIgnoreElement(context.getNewElement())) {
                newContentChildIndex++;
                if (context.getThreeWayDiff() && shouldIgnoreElement(context.getIntermediateElement())) {
                    intermediateContentChildIndex++;
                }
            } else if (shouldIgnoreElement(context.getOldElement())) {
                oldContentChildIndex++;
                if (context.getThreeWayDiff() && shouldIgnoreElement(context.getIntermediateElement())) {
                    intermediateContentChildIndex++;
                }
            } else if (newContentChildIndex == context.getIndexOfOldElementInNewContent()
                    && (!context.getDisplayRemovedContentAsReadOnly()
                    || shouldCompareElements(context.getOldElement(), context.getNewElement())
                    && shouldCompareElements(context.getNewElement(), context.getOldElement()))) {

                if (context.getThreeWayDiff() && isIntermediateElementRemovedInNewContent(context)) {
                    if (isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
                        // LEOS-4392 If old and new contents are same check if structure elements are added/moved/transformed in
                        // intermediate version but reverted in new version for three way diff.
                        if (!isCurrentElementNonIgnored((Node) context.getIntermediateElement().getNavigationIndex())) {
                            compareRevertedChanges(context);
                        }
                        intermediateContentChildIndex++;

                        if (intermediateContentChildIndex < context.getIntermediateContentRoot().getChildren().size() &&
                                isCurrentElementIgnoredInNewContent(context)) {
                            // There are still children to process in intermediate
                            oldContentChildIndex++;
                            newContentChildIndex++;
                        }
                    } else { //No more children in intermediate check for remaining child elements in new/old versions
                        if (isIgnoredElement(context.getIntermediateElement()) && !isCurrentElementIgnoredInNewContent(context)) {
                            // LEOS-4392: compare contents of old and new only as element was moved in intermediate but
                            // restored in new so no change needs to be displayed for its child elements just print as is
                            compareElementContents(new ContentComparatorContext.Builder(context)
                                    .withThreeWayDiff(false)
                                    .build());
                        }
                        oldContentChildIndex++;
                        newContentChildIndex++;
                    }
                } else {
                    if (isThreeWayDiffEnabled(context) && containsAddedNonIgnoredElements((Node) context.getIntermediateElement().getNavigationIndex()) &&
                            !context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) &&
                            isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
                        // Ignore soft added or move_from structured action in intermediate as it is already reverted in new version
                        intermediateContentChildIndex++;
                    } else {
                        // element did not changed relative position so check if it's content is changed and should be compared
                        compareElementContents(context);
                        oldContentChildIndex++;
                        newContentChildIndex++;
                        intermediateContentChildIndex++;
                    }
                }
            } else if (context.getIndexOfNewElementInOldContent() < 0 && context.getIndexOfOldElementInNewContent() < 0) {
                // oldElement was completely replaced with newElement
                boolean newContentIncremented = false;
                int ignoredElementIndex = getIndexOfIgnoredElementInNewContent(context);
                if (shouldAppendAddedElement(context, oldContentChildIndex, newContentChildIndex, ignoredElementIndex)) {
                    appendAddedElementContent(context);
                    newContentChildIndex++;
                    newContentIncremented = true;
                } else {
                    if (ignoredElementIndex != -1 || shouldAppendRemovedElement(context)) {
                        appendRemovedElementContentIfRequired(context);
                    }
                    oldContentChildIndex++;
                }
                if ((shouldIncrementIntermediateIndex(context) && newContentIncremented) || containsDeletedElementInNewContent(context)) {
                    intermediateContentChildIndex++;
                }
            } else if (context.getIndexOfNewElementInOldContent() >= oldContentChildIndex && context.getIndexOfOldElementInNewContent() > newContentChildIndex) {
                // newElement appears to be moved backward to newContentChildIndex and oldElement appears to be moved forward from oldContentChildIndex
                // at the same time
                // so display the element that was moved more positions because it's more likely to be the action the user actually made
                if ((context.getIndexOfNewElementInOldContent() - oldContentChildIndex > context.getIndexOfOldElementInNewContent() - newContentChildIndex)
                        || context.getDisplayRemovedContentAsReadOnly() && !shouldCompareElements(context.getOldElement(), context.getNewElement())) {
                    // newElement was moved backward to newContentChildIndex more positions than oldElement was moved forward from oldContentChildIndex
                    // or the newElement should not be compared with the oldElement
                    // so display the added newElement in the new location for now
                    boolean shouldIncrementIntermediateIndex = shouldIncrementIntermediateIndex(context);
                    appendAddedElementContent(context);
                    newContentChildIndex++;
                    if (shouldIncrementIntermediateIndex) {
                        intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getNewElement(), intermediateContentChildIndex);
                    }
                } else {
                    // oldElement was moved forward from oldContentChildIndex more or just as many positions as newElement was moved backward to newContentChildIndex
                    // so display the removed oldElement in the original location for now
                    appendRemovedElementContentIfRequired(context);
                    oldContentChildIndex++;
                    intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getOldElement(), intermediateContentChildIndex);
                }
            } else if (context.getIndexOfNewElementInOldContent() >= 0 && context.getIndexOfNewElementInOldContent() < oldContentChildIndex) {
                // newElement was moved forward to newContentChildIndex and the removed oldElement is already displayed
                // in the original location so display the added newElement in the new location also
                boolean shouldIncrementIntermediateIndex = shouldIncrementIntermediateIndex(context);
                appendAddedElementContent(context);
                newContentChildIndex++;
                if (shouldIncrementIntermediateIndex) {
                    intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getNewElement(), intermediateContentChildIndex);
                }
            } else if (context.getIndexOfOldElementInNewContent() >= 0 &&
                    context.getIndexOfOldElementInNewContent() < newContentChildIndex &&
                    !(context.getIndexOfNewElementInOldContent() < 0)) {
                // oldElement was moved backward from oldContentChildIndex and the added newElement is already displayed
                // in the new location so display the removed oldElement in the original location also provided there is
                // no new element added in the new content before it.
                appendRemovedElementContentIfRequired(context);
                oldContentChildIndex++;
                intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getOldElement(), intermediateContentChildIndex);
            } else if ((context.getIndexOfNewElementInOldContent() < 0 &&
                    getIndexOfIgnoredElementInNewContent(context) < 0) ||
                    shouldAppendAddedElement(context, oldContentChildIndex,
                            newContentChildIndex, getIndexOfIgnoredElementInNewContent(context))) {
                // newElement is simply added or added before deleted or moved element so display the added element
                boolean shouldIncrementIntermediateIndex = shouldIncrementIntermediateIndex(context);
                appendAddedElementContent(context);
                newContentChildIndex++;
                if (shouldIncrementIntermediateIndex) {
                    intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getNewElement(), intermediateContentChildIndex);
                }
            } else {
                // oldElement was deleted or moved so only display the removed element
                appendRemovedElementContentIfRequired(context);
                oldContentChildIndex++;
                intermediateContentChildIndex = incrementIntermediateIndexIfRequired(context, context.getOldElement(), intermediateContentChildIndex);
            }
        }

        if (context.getOldContentRoot() != null && oldContentChildIndex < context.getOldContentRoot().getChildren().size()) {
            // there are still children in the old root that have not been processed
            // it means they were all moved backward or under a different parent or deleted
            // so display the removed children
            for (int i = oldContentChildIndex; i < context.getOldContentRoot().getChildren().size(); i++) {
                Element oldElementChild = context.getOldContentRoot().getChildren().get(i);
                context.setOldElement(oldElementChild);
                ContentComparatorContext newContext = context;

                if (context.getThreeWayDiff()) {
                    Element intermediateElementChild = context.getIntermediateElement();
                    if (isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), i)) {
                        intermediateElementChild = context.getIntermediateContentRoot().getChildren().get(i);
                        newContext = new ContentComparatorContext.Builder(context)
                                .withIntermediateElement(intermediateElementChild)
                                .build();
                    }
                }

                if (!shouldIgnoreElement(oldElementChild)) {
                    appendRemovedElementContentIfRequired(newContext);
                }
            }
        } else if (newContentChildIndex < context.getNewContentRoot().getChildren().size()) {
            // there are still children in the new root that have not been processed
            // it means they were all moved forward or from a different parent or added
            // so display the added children
            int newContentIndexForChildren = newContentChildIndex;
            int intermediateContentIndexForChildren = intermediateContentChildIndex;
            while ((isElementIndexLessThanRootChildren(context.getNewContentRoot(), newContentIndexForChildren))) {
                Element newElementChild = context.getNewContentRoot().getChildren().get(newContentIndexForChildren);
                if (context.getThreeWayDiff() && isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentIndexForChildren)) {
                    Element intermediateElementChild = context.getIntermediateContentRoot().getChildren().get(intermediateContentIndexForChildren);
                    context.setIntermediateElement(intermediateElementChild);
                    //there are children added in intermediate and kept in new content as well.If any children
                    //deleted in the new content then display the removed content in three way diff as deleted.
                    if (intermediateElementChild != null && !newElementChild.getTagId().equalsIgnoreCase(intermediateElementChild.getTagId())
                            && !context.getNewContentElements().containsKey(intermediateElementChild.getTagId())) {
                        appendRemovedContent(new ContentComparatorContext.Builder(context)
                                .withOldElement(intermediateElementChild)
                                .withOldContentNavigator(context.getIntermediateContentNavigator())
                                .build());
                        intermediateContentIndexForChildren++;
                        continue; //skip to next iteration till deleted content is fully displayed
                    }
                }
                if (!shouldIgnoreElement(newElementChild)) {
                    appendAddedElementContent(context.setIndexOfOldElementInNewContent(newContentIndexForChildren).setNewElement(newElementChild));
                }
                newContentIndexForChildren++;
                intermediateContentIndexForChildren = incrementIntermediateIndexIfRequired(context, newElementChild, intermediateContentIndexForChildren);

            }
            if (context.getThreeWayDiff() && isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentIndexForChildren)) {
                appendIntermediateRemovedElement(context, intermediateContentIndexForChildren);
            }
        } else if (context.getThreeWayDiff() && isElementIndexLessThanRootChildren(context.getIntermediateContentRoot(), intermediateContentChildIndex)) {
            appendIntermediateRemovedElement(context, intermediateContentChildIndex);
        }
    }

    // There are still children remaining in the intermediate but not present in old and new content
    // it means that new element is added in intermediate and removed in current so it should be shown as deleted in comparison
    private void appendIntermediateRemovedElement(ContentComparatorContext context, int intermediateContentIndexForChildren) {
        for (int i = intermediateContentIndexForChildren; i < context.getIntermediateContentRoot().getChildren().size(); i++) {
            Element intermediateElementChild = context.getIntermediateContentRoot().getChildren().get(i);
            if (!isCurrentElementNonIgnored((Node) intermediateElementChild.getNavigationIndex())) {
                appendRemovedContent(new ContentComparatorContext.Builder(context)
                        .withOldElement(intermediateElementChild)
                        .withOldContentNavigator(context.getIntermediateContentNavigator())
                        .build());
            }
        }
    }

    private boolean shouldAppendAddedElement(ContentComparatorContext context, int oldContentChildIndex, int newContentChildIndex, int ignoredElementIndex) {
        return (containsAddedNonIgnoredElements(getNodeFromNavigationIndex(context.getNewElement()))
                && ignoredElementIndex > oldContentChildIndex && ignoredElementIndex > newContentChildIndex) ||
                (ignoredElementIndex == -1 && context.getOldContentElements().containsKey(context.getNewElement().getTagId()) && oldContentChildIndex > newContentChildIndex);
    }

    protected final void compareElementContents(ContentComparatorContext context) {
        long startTime = System.currentTimeMillis();
        Node node = getNodeFromNavigationIndex(context.getNewElement());
//        LOG.debug("Handling {} ", context.getNewElement().getTagId());
        if (!isActionRoot(context.getNewElement()) && !containsAddedNonIgnoredElements(node)
                && ((isElementContentEqual(context) && !containsIgnoredElements(node)) || (context.getIgnoreRenumbering() && shouldIgnoreRenumbering(context.getNewElement())))) {
            if (context.getThreeWayDiff()) {
                String startTagValue = getStartTagValueForAddedElement(context.getNewElement(), context.getIntermediateElement(), context);
                if (EMPTY_STRING.equalsIgnoreCase(startTagValue)) {
                    XmlUtils.insertOrUpdateAttributeValue(node, context.getStartTagAttrName(), context.getStartTagAttrValue());
                } else {
                    XmlUtils.insertOrUpdateAttributeValue(node, context.getAttrName(), startTagValue);
                }
                context.getResultNode().appendChild(node);
                LOG.debug("Added start tag threeWay{} ", context.getNewElement().getTagId());
            } else {
                if (isElementContentEqual(context) && !containsIgnoredElements(node)) {
//                    context.getResultBuilder().append(content);
//                    LOG.debug("Added content of {} AS IS. Didn't change, no need to compare.", context.getNewElement().getTagId());
                } else {
//                    context.getResultBuilder().append(updateElementAttribute(content, context.getStartTagAttrName(), context.getStartTagAttrValue()));
                    XmlUtils.insertOrUpdateAttributeValue(node, context.getStartTagAttrName(), context.getStartTagAttrValue());
                    LOG.debug("Updating attributes before adding content for tag {}", context.getNewElement().getTagId());
                }
                context.getResultNode().appendChild(node);
//                LOG.debug("Added content of {} AS IS. Didn't change, no need to compare.", context.getNewElement().getTagId());
            }
        } else if (!shouldIgnoreElement(context.getOldElement()) && (!context.getIgnoreElements() || !shouldIgnoreElement(context.getNewElement()))) {
            //add the start tag
            if (isThreeWayDiffEnabled(context)) {
//                if(isElementContentEqual(context) && !containsIgnoredElements(content)) {
//                    context.getResultBuilder().append(buildStartTag(context.getNewElement()));
//                } else if(!shouldIgnoreElement(context.getNewElement()) && ((containsAddedNonIgnoredElements(context.getIntermediateElement().getTagContent()) &&
//                        !shouldIgnoreElement(context.getIntermediateElement()) && !isIgnoredElement(context.getIntermediateElement())) ||
//                        shouldIgnoreElement(context.getIntermediateElement()))) { // build start tag for moved/added element with added styles
//                    context.getResultBuilder().append(buildStartTagForAddedElement(context));
//                } else if(shouldIgnoreElement(context.getNewElement())) {
//                    context.getResultBuilder().append(buildStartTagForRemovedElement(context.getNewElement(), context, context.getIntermediateContentElements()));
//                } else if(isActionRoot(context.getNewElement())) {
//                    context.getResultBuilder().append(buildStartTagForAddedElement(context));
//                } else if(context.getOldElement() == null && !shouldIgnoreElement(context.getNewElement()) && !shouldIgnoreElement(context.getIntermediateElement())) { //build start tag for added element in intermediate
//                    context.getResultBuilder().append(buildStartTagForAddedElement(context));
//                } else if(context.getNewElement() != null && context.getIntermediateElement() == null && context.getOldElement() == null) {
//                    context.getResultBuilder().append(buildStartTag(context.getNewElement(), context.getAttrName(), context.getAddedIntermediateValue()));
//                } else {
//                    context.getResultBuilder().append(buildStartTag(context.getNewElement())); //build tag for children without styles
//                }
                LOG.debug("Second check threeWay", context.getNewElement().getTagId());
            } else {
//                context.getResultBuilder().append(buildStartTag(context.getNewElement(), context.getStartTagAttrName(), context.getStartTagAttrValue()));
//                LOG.debug("Added start tag <{}>. Need to compare content", context.getNewElement().getTagId());
            }

//          context.resetStartTagAttribute();
            if (context.getNewElement().getTagName().equalsIgnoreCase(NUM) && shouldIgnoreElement(context.getNewElement())) {
//                context.getResultBuilder().append(getContentFragmentAsString(((VTDNav)context.getOldContentNavigator()), context.getOldElement()));
                String str = getContentFragmentAsString((Node) context.getOldContentNavigator(), context.getOldElement());
                LOG.debug("Added NUM {} ", context.getNewElement().getTagId());
            } else if ((context.getNewElement() != null && context.getNewElement().hasTextChild()) || (context.getOldElement() != null && context.getOldElement().hasTextChild())) {
                String oldContent = XmlUtils.getContentNodeAsXmlFragment(getNodeFromNavigationIndex(context.getOldElement()));
                String newContent = XmlUtils.getContentNodeAsXmlFragment(getNodeFromNavigationIndex(context.getNewElement()));
                String intermediateContent = null;
                if (isThreeWayDiffEnabled(context) && context.getIntermediateElement().hasTextChild()) {
                    intermediateContent = ((Node) context.getIntermediateElement().getNavigationIndex()).getTextContent();
                }
                String result = textComparator.compareTextNodeContents(oldContent, newContent, intermediateContent, context);
                result = "<fake xmlns:leos=\"urn:eu:europa:ec:leos\">" + result + "</fake>";
                Node comparedContentNode = XmlUtils.createNodeFromXmlFragment(node.getOwnerDocument(), result.getBytes(UTF_8));
//                LOG.debug("Added COMPARED createNodeFromXmlFragment result for {} in  {}", context.getNewElement().getTagName(), (startTextCompareTime2 - startTextCompareTime));
                node.setTextContent("");
                node = XmlUtils.copyContent(comparedContentNode, node);
                context.getResultNode().appendChild(node);
            } else {
                Node article = getNodeFromNavigationIndex(context.getNewElement());
                article = XmlUtils.importNodeInDocument(article.getOwnerDocument(), article);
                article.setTextContent("");
                ContentComparatorContext newContext = new ContentComparatorContext.Builder(context)
                        .withOldContentRoot(context.getOldElement())
                        .withNewContentRoot(context.getNewElement())
                        .withResultNode(article)
                        .build();
                context.getResultNode().appendChild(article);
                computeDifferencesAtNodeLevel(newContext);
            }
//            LOG.debug("Added end tag for </{}> ", context.getNewElement().getTagId());
        } else if (shouldDisplayRemovedContent(context.getOldElement(), context.getIndexOfOldElementInNewContent())) {
            //element removed in the new version
            appendRemovedElementContent(context);
        }

        long endTime3 = System.currentTimeMillis();
//        LOG.debug("{}-{} - compare in {}", node.getNodeName(), getId(node), (endTime3 - startTime));
    }

    private Node getNodeFromNavigationIndex(Element element) {
        Node node = null;
        if (element != null) {
            node = (Node) element.getNavigationIndex();
        }
        return node;
    }

    private boolean isThreeWayDiffEnabled(ContentComparatorContext context) {
        return context.getThreeWayDiff() && context.getIntermediateElement() != null;
    }

    private boolean shouldIncrementIntermediateIndex(ContentComparatorContext context) {
        boolean shouldIncrementIndex;
        if (isThreeWayDiffEnabled(context)) {
            if (context.getOldElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) ||
                    !context.getNewElement().getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId())) {
                shouldIncrementIndex = false;
            } else {
                shouldIncrementIndex = true;
            }
        } else {
            shouldIncrementIndex = false;
        }
        return shouldIncrementIndex;
    }

    private int incrementIntermediateIndexIfRequired(ContentComparatorContext context, Element element, int intermediateContentChildIndex) {
        if (isThreeWayDiffEnabled(context) &&
                element.getTagId().equalsIgnoreCase(context.getIntermediateElement().getTagId()) || shouldIgnoreElement(context.getIntermediateElement())) {
            intermediateContentChildIndex++;
        }
        return intermediateContentChildIndex;
    }

    private void compareRevertedChanges(ContentComparatorContext context) {
        //LEOS-4392 If structure is different in intermediate, update context by setting old = intermediate
        //and compare with new version like two version diff
        ContentComparatorContext newContext = new ContentComparatorContext.Builder(context)
                .withOldElement(context.getIntermediateElement())
                .withOldContentRoot(context.getIntermediateContentRoot())
                .withOldContentElements(context.getIntermediateContentElements())
                .withOldContentNavigator(context.getIntermediateContentNavigator())
                .build();
        appendRemovedElementContentIfRequired(newContext);
        if (shouldAddElement(newContext.getOldElement(), newContext.getNewContentElements()) && !containsDeletedElementInNewContent(newContext)) {
            appendAddedElementContentIfRequired(newContext);
        }
    }

    protected final String buildEndTag(Element element) {
        String tagContent = element.getTagContent();
        String endTag = null;
        try {
            if (tagContent.startsWith("<span")) {
                endTag = END_TAG;
            } else {
                //LOG.debug("TAG {}", tagContent);
                endTag = OPEN_END_TAG + tagContent.substring(1, tagContent.indexOf(' ') > 0 ? tagContent.indexOf(' ') : (tagContent.length() - 1)) + CLOSE_TAG;
            }
        } catch (Exception e) {
            LOG.debug("TAG index {} {}", tagContent, tagContent.indexOf(' '));
        }
        return endTag;
    }

    protected Node addReadOnlyAttributes(Node node) {
        XmlUtils.insertOrUpdateAttributeValue(node, LEOS_EDITABLE_ATTR, Boolean.FALSE.toString());
        XmlUtils.insertOrUpdateAttributeValue(node, LEOS_DELETABLE_ATTR, Boolean.FALSE.toString());
        return node;
    }

    @Override
    public String[] twoColumnsCompareContents(ContentComparatorContext context) {
        return new String[]{context.getLeftResultBuilder().toString(), context.getRightResultBuilder().toString()};
    }

    private void computeTwoColumnDifferencesAtNodeLevel(ContentComparatorContext context) {
        if (shouldIgnoreElement(context.getOldContentRoot())) {
            return;
        }

        int oldContentChildIndex = 0; // current index in oldContentRoot children list
        int newContentChildIndex = 0; // current index in newContentRoot children list

        while (oldContentChildIndex < context.getOldContentRoot().getChildren().size()
                && newContentChildIndex < context.getNewContentRoot().getChildren().size()) {

            context.setOldElement(context.getOldContentRoot().getChildren().get(oldContentChildIndex))
                    .setNewElement(context.getNewContentRoot().getChildren().get(newContentChildIndex))
                    .setIndexOfOldElementInNewContent(getBestMatchInList(context.getNewContentRoot().getChildren(), context.getOldElement()))
                    .setIndexOfNewElementInOldContent(getBestMatchInList(context.getOldContentRoot().getChildren(), context.getNewElement()));

            // at each step, check for a particular structural change in this order
            if (shouldIgnoreElement(context.getNewElement())) {
                newContentChildIndex++;
            } else if (shouldIgnoreElement(context.getOldElement())) {
                oldContentChildIndex++;
            } else if (newContentChildIndex == context.getIndexOfOldElementInNewContent()) {
                // element did not changed relative position so check if it's content is changed
                twoColumnsCompareElementContents(context);
                oldContentChildIndex++;
                newContentChildIndex++;
            } else if (context.getIndexOfNewElementInOldContent() < 0 && context.getIndexOfOldElementInNewContent() < 0) {
                // oldElement was completely replaced with newElement
                appendRemovedElementsContent(context);
                appendAddedElementsContent(context);
                oldContentChildIndex++;
                newContentChildIndex++;
            } else if (context.getIndexOfNewElementInOldContent() >= oldContentChildIndex && context.getIndexOfOldElementInNewContent() > newContentChildIndex) {
                // newElement appears to be moved backward to newContentChildIndex and oldElement appears to be moved forward from oldContentChildIndex
                // at the same time
                // so display the element that was moved more positions because it's more likely to be the action the user actually made
                if (context.getIndexOfNewElementInOldContent() - oldContentChildIndex > context.getIndexOfOldElementInNewContent() - newContentChildIndex) {
                    // newElement was moved backward to newContentChildIndex more positions than oldElement was moved forward from oldContentChildIndex
                    // so display the added newElement in the new location for now
                    appendAddedElementsContent(context);
                    newContentChildIndex++;
                } else {
                    // oldElement was moved forward from oldContentChildIndex more or just as many positions as newElement was moved backward to newContentChildIndex
                    // so display the removed oldElement in the original location for now
                    appendRemovedElementsContent(context);
                    oldContentChildIndex++;
                }
            } else if (context.getIndexOfNewElementInOldContent() >= 0 && context.getIndexOfNewElementInOldContent() < oldContentChildIndex) {
                // newElement was moved forward to newContentChildIndex and the removed oldElement is already displayed in the original location
                // so display the added newElement in the new location also
                appendAddedElementsContent(context);
                newContentChildIndex++;
            } else if (context.getIndexOfOldElementInNewContent() >= 0 && context.getIndexOfOldElementInNewContent() < newContentChildIndex) {
                // oldElement was moved backward from oldContentChildIndex and the added newElement is already displayed in the new location
                // so display the removed oldElement in the original location also
                appendRemovedElementsContent(context);
                oldContentChildIndex++;
            } else if (context.getIndexOfNewElementInOldContent() < 0) {
                // newElement was added or moved from a different parent so only display the added element
                appendAddedElementsContent(context);
                newContentChildIndex++;
            } else {
                // oldElement was deleted or moved under a different parent so only display the removed element
                appendRemovedElementsContent(context);
                oldContentChildIndex++;
            }
        }

        if (oldContentChildIndex < context.getOldContentRoot().getChildren().size()) {
            // there are still children in the old root that have not been processed
            // it means they were all moved backward or under a different parent or deleted
            // so display the removed children
            for (int i = oldContentChildIndex; i < context.getOldContentRoot().getChildren().size(); i++) {
                Element child = context.getOldContentRoot().getChildren().get(i);
                if (!shouldIgnoreElement(child)) {
                    appendRemovedElementsContent(context.setOldElement(child));
                }
            }
        } else if (newContentChildIndex < context.getNewContentRoot().getChildren().size()) {
            //obviously, this if test is not necessary, it's only used for clarity
            // there are still children in the new root that have not been processed
            // it means they were all moved forward or from a different parent or added
            // so display the added children
            for (int i = newContentChildIndex; i < context.getNewContentRoot().getChildren().size(); i++) {
                Element child = context.getNewContentRoot().getChildren().get(i);
                if (!shouldIgnoreElement(child)) {
                    appendAddedElementsContent(context.setIndexOfOldElementInNewContent(i).setNewElement(child));
                }
            }
        }
    }

    protected final void twoColumnsCompareElementContents(ContentComparatorContext context) {
//        String content = getElementFragmentAsString((Node) context.getNewContentNavigator(), context.getNewElement());//TODO check if possible working directly using the node

        if (isElementContentEqual(context) && !containsIgnoredElements(getNodeFromNavigationIndex(context.getNewElement()))) {
//            context.getRightResultBuilder().append(content);
//            context.getLeftResultBuilder().append(content);
        } else if (!shouldIgnoreElement(context.getOldElement()) && !shouldIgnoreElement(context.getNewElement())) {

            //add the start tag
            appendChangedElementsStartTag(null, context.getLeftResultBuilder(), context.getRightResultBuilder(), context.getNewElement());

            if (context.getNewElement().hasTextChild() || context.getOldElement().hasTextChild()) {
                //we keep using this blocks for displaying the results aligned, but we won't show the yellow pins.
                context.getRightResultBuilder().append(BLOCK_MODIFIED_LINE_START_TAG);
                //do this specifically for IE8 as it doesn't support querying by name if the name is part of the span. it must be part of an input type element
                context.getRightResultBuilder().append(MODIFIED_LINE_MARKER_PART_1).append(context.getModifications().getValue()).append(MODIFIED_LINE_MARKER_PART_2);
                context.getLeftResultBuilder().append(BLOCK_MODIFIED_LINE_START_TAG);
                context.getLeftResultBuilder().append(MODIFIED_LINE_MARKER_PART_1).append(context.getModifications().getValue()).append(MODIFIED_LINE_MARKER_PART_2);
                context.getModifications().setValue(context.getModifications().getValue() + 1);

                String oldContent = getContentFragmentAsString((Node) context.getOldContentNavigator(), context.getOldElement());
                String newContent = getContentFragmentAsString((Node) context.getOldContentNavigator(), context.getNewElement());
                String[] result = textComparator.twoColumnsCompareTextNodeContents(oldContent, newContent);

                context.getLeftResultBuilder().append(result[0]);
                context.getRightResultBuilder().append(result[1]);

                context.getRightResultBuilder().append(END_TAG);
                context.getLeftResultBuilder().append(END_TAG);
            } else {
                computeTwoColumnDifferencesAtNodeLevel(new ContentComparatorContext.Builder(context)
                        .withOldContentRoot(context.getOldElement())
                        .withNewContentRoot(context.getNewElement())
                        .build());
            }
            //add the end tag
            appendElementsEndTag(context.getLeftResultBuilder(), context.getRightResultBuilder(), context.getNewElement());
        } else if (shouldDisplayRemovedContent(context.getOldElement(), context.getIndexOfOldElementInNewContent())) {
            appendRemovedElementsContent(context);
        }
    }

    protected final void appendChangedElementsStartTag(Boolean isAdded, StringBuilder leftSideBuilder, StringBuilder rightSideBuilder, Element element) {
//        if (isAdded != null) {
//            //on the right side, if isAdded, add the new element start tag otherwise put it as transparent
//            rightSideBuilder.append(insertOrUpdateAttributeValue(new StringBuilder(element.getTagContent()), ATTR_NAME, isAdded ? CONTENT_ADDED_CLASS : CONTENT_BLOCK_REMOVED_CLASS).toString());
//            //on the left side, if isAdded, add this element start tag for keeping the same occupied space, but make it transparent, otherwise put it as removed
//            leftSideBuilder.append(insertOrUpdateAttributeValue(new StringBuilder(element.getTagContent()), ATTR_NAME, isAdded ? CONTENT_BLOCK_ADDED_CLASS : CONTENT_REMOVED_CLASS).toString());
//        } else {
//            //element was not changed
//            leftSideBuilder.append(element.getTagContent());
//            rightSideBuilder.append(element.getTagContent());
//        }
    }

    protected final void appendElementsEndTag(StringBuilder leftSideBuilder, StringBuilder rightSideBuilder, Element element) {
        String endTag = buildEndTag(element);
        leftSideBuilder.append(endTag);
        rightSideBuilder.append(endTag);
    }

    protected final void appendChangedElementsContent(Boolean isAdded, StringBuilder leftSideBuilder, StringBuilder rightSideBuilder, String changedContent) {
        if (isAdded != null) {
            //on the right side, if isAdded, add the new element content otherwise put it as transparent
            rightSideBuilder.append(XmlUtils.updateElementAttribute(changedContent, ATTR_NAME, isAdded ? CONTENT_ADDED_CLASS : CONTENT_BLOCK_REMOVED_CLASS));
            //on the left side, if isAdded, add this element content for keeping the same occupied space, but make it transparent, otherwise put it as removed
            leftSideBuilder.append(XmlUtils.updateElementAttribute(changedContent, ATTR_NAME, isAdded ? CONTENT_BLOCK_ADDED_CLASS : CONTENT_REMOVED_CLASS));
        } else {
            //element was not changed
            rightSideBuilder.append(changedContent);
            leftSideBuilder.append(changedContent);
        }
    }

    protected final void appendRemovedElementContentIfRequired(ContentComparatorContext context) {
        if (shouldIgnoreElement(context.getNewContentRoot()) || isElementInItsOriginalPosition(context.getNewContentRoot())) {
            appendRemovedElementContent(context);
        } else if (!isElementMovedOrTransformed(context.getNewElement()) || isCurrentElementNonIgnored(getNodeFromNavigationIndex(context.getOldElement()))) {
            appendRemovedContent(context);
        }
    }

    protected final void appendAddedElementContentIfRequired(ContentComparatorContext context) {
        appendAddedElementContent(context);
    }

    protected final Boolean isElementRemovedFromContent(int indexOfOldElementInNewContent) {
        return indexOfOldElementInNewContent == -1;
    }

    protected Boolean isActionRoot(Element element) {
        return element.getTagContent().contains(LEOS_SOFT_ACTION_ROOT);
    }

    protected Boolean shouldIgnoreRenumbering(Element element) {
        return NUM.equals(element.getTagName())
                && (containsSoftMoveFromAction((Node) element.getParent().getNavigationIndex())
                || containsSoftAddedAction((Node) element.getParent().getNavigationIndex()));
    }

    protected Boolean containsSoftDeleteAction(Node node) {
        return XmlUtils.containsAttribute(node, LEOS_SOFT_ACTION_DEL);
    }

    protected Boolean containsSoftDeletePlaceholderPrefix(Node node) {
        return XmlUtils.containsAttribute(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX);
    }

    protected Boolean containsSoftMovedPlaceholderPrefix(Node node) {
        return XmlUtils.containsAttribute(node, SOFT_MOVE_PLACEHOLDER_ID_PREFIX);
    }

    protected Boolean containsSoftMoveToAction(Node node) {
        return XmlUtils.containsAttribute(node, LEOS_SOFT_ACTION_MOVE_TO);
    }

    protected Boolean containsSoftMoveFromAction(Node node) {
        return XmlUtils.containsAttribute(node, LEOS_SOFT_ACTION_MOVE_FROM);
    }

    protected Boolean containsSoftAddedAction(Node node) {
        return XmlUtils.containsAttribute(node, LEOS_SOFT_ACTION_ADD);
    }

    protected Boolean containsSoftTransformAction(Node node) {
        return XmlUtils.containsAttribute(node, LEOS_SOFT_ACTION_TRANSFORM);
    }

    protected Boolean isElementInItsOriginalPosition(Element element) {
        return element == null ||
                !containsSoftAddedAction((Node) element.getNavigationIndex())
                        && !containsSoftTransformAction((Node) element.getNavigationIndex())
                        && !containsSoftMoveFromAction((Node) element.getNavigationIndex());
    }

    protected Boolean isElementMovedOrTransformed(Element element) {
        return containsSoftMoveFromAction((Node) element.getNavigationIndex()) || containsSoftTransformAction((Node) element.getNavigationIndex());
    }

    protected Boolean shouldIgnoreElement(Element element) {
        return element != null && (containsSoftDeleteAction((Node) element.getNavigationIndex()) ||
                containsSoftDeletePlaceholderPrefix((Node) element.getNavigationIndex()) ||
                containsSoftMoveToAction((Node) element.getNavigationIndex()));
    }

    protected Node getChangedElementContent(Node contentNavigator, Element element, String attrName, String attrValue) {
        Node node = null;
        if (!shouldIgnoreElement(element)) {
            node = XmlUtils.getElementById(contentNavigator, element.getTagId()); //todo can't we use element.getNode()
            if (attrName != null && attrValue != null) {
                XmlUtils.insertOrUpdateAttributeValue(node, attrName, attrValue);
            }
        }
        return node;
    }

    protected boolean containsSoftDeleteElement(Element element, Map<String, Element> contentElements) {
        return element != null && contentElements.containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + element.getTagId());
    }

    protected void appendNonIgnoredChangedElementsContent(Boolean isAdded, StringBuilder leftSideBuilder, StringBuilder rightSideBuilder,
                                                          Node contentNavigator, Element element) {
        String elementContent = getElementFragmentAsString(contentNavigator, element);
        if (!containsIgnoredElements((Node) element.getNavigationIndex())) {
            appendChangedElementsContent(isAdded, leftSideBuilder, rightSideBuilder, elementContent);
        } else if (!shouldIgnoreElement(element)) {
            appendChangedElementsStartTag(isAdded, leftSideBuilder, rightSideBuilder, element);
            for (Element child : element.getChildren()) {
                if (!shouldIgnoreElement(child)) {
                    // add child without changing the start tag
                    appendNonIgnoredChangedElementsContent(null, leftSideBuilder, rightSideBuilder, contentNavigator, child);
                }
            }
            appendElementsEndTag(leftSideBuilder, rightSideBuilder, element);
        }
    }

    protected void appendRemovedElementsContent(ContentComparatorContext context) {
        if (!context.getNewContentElements().containsKey(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                || !context.getOldElement().getTagId().equals(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                && (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                || (context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())))) {
            appendNonIgnoredChangedElementsContent(Boolean.FALSE, context.getLeftResultBuilder(), context.getRightResultBuilder(), ((Node) context.getOldContentNavigator()), context.getOldElement());
        } else if (context.getOldElement().getTagId().equals(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + context.getOldElement().getParent().getTagId())
                && (containsSoftDeleteElement(context.getOldElement(), context.getNewContentElements())
                || context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId()))) {

            Element movedOrDeletedTransformedElement = context.getNewContentElements().get((context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())
                    ? SOFT_MOVE_PLACEHOLDER_ID_PREFIX : SOFT_DELETE_PLACEHOLDER_ID_PREFIX) + context.getOldElement().getTagId());
            String movedOrDeletedTransformedElementContent = getElementFragmentAsString((Node) context.getNewContentNavigator(), movedOrDeletedTransformedElement);
            appendChangedElementsContent(Boolean.FALSE, context.getLeftResultBuilder(), context.getRightResultBuilder(), movedOrDeletedTransformedElementContent);
        }
    }

    protected boolean containsSoftMoveToTransformedElement(Map<String, Element> contentElements, Element element) {
        return element != null && (contentElements.containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getParent().getTagId())
                && contentElements.containsKey(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getParent().getTagId())
                && !shouldIgnoreElement(contentElements.get(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX + element.getParent().getTagId())));
    }

    protected boolean containsSoftMoveToElement(Map<String, Element> contentElements, Element element) {
        return element != null && (contentElements.containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + element.getTagId())
                && contentElements.containsKey(element.getTagId()) && !shouldIgnoreElement(contentElements.get(element.getTagId())));
    }

    protected Boolean containsAddedNonIgnoredElements(Node node) {
        return containsSoftMoveFromAction(node) || containsSoftAddedAction(node);
    }

    protected Boolean shouldCompareElements(Element oldElement, Element newElement) {
        return newElement == null || oldElement == null
                || !(containsSoftMoveFromAction((Node) newElement.getNavigationIndex()) && !containsSoftMoveFromAction((Node) oldElement.getNavigationIndex()))
                && !(containsSoftTransformAction((Node) newElement.getNavigationIndex()) && !containsSoftTransformAction((Node) oldElement.getNavigationIndex()));
    }

    protected int getIndexOfIgnoredElementInNewContent(ContentComparatorContext context) {
        Element element = null;
        if (context.getNewContentElements().containsKey(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
            element = context.getNewContentElements().get(SOFT_DELETE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        } else if (context.getNewContentElements().containsKey(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId())) {
            element = context.getNewContentElements().get(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + context.getOldElement().getTagId());
        }
        return element != null ? context.getNewContentRoot().getChildren().indexOf(element) : -1;
    }

    protected final int getBestMatchInList(List<Element> childElements, Element element) {
        if (shouldIgnoreElement(element)) {
            return -2;
        } else if (element == null) {
            return -1;
        }
        int foundPosition = -1;
        int rank[] = new int[childElements.size()];
        for (int iCount = 0; iCount < childElements.size(); iCount++) {
            Element listElement = childElements.get(iCount);

            if (listElement.getTagId() != null && element.getTagId() != null
                    && listElement.getTagId().equals(element.getTagId())) {
                rank[iCount] = 1000;
                break;
            } else if ((listElement.getTagId() == null && element.getTagId() == null)
                    && listElement.getTagName().equals(element.getTagName())) {//only try to find match if tagID is not present

                // compute node distance
                int maxDistance = 100;
                int distanceWeight = maxDistance / 5; //after distance of 5 nodes it is discarded
                int nodeDistance = Math.abs(listElement.getNodeIndex() - element.getNodeIndex());
                nodeDistance = Math.min(nodeDistance * distanceWeight, maxDistance); // 0...maxDistance

                // compute node similarity
                int similarityWeight = 2;
                int similarity = (int) (100 * listElement.contentSimilarity(element)); //0...100
                similarity = similarity * similarityWeight;

                // compute node rank
                rank[iCount] = (maxDistance - nodeDistance)  //distance 0=100, 1=80,2=60,..5=0
                        + similarity;
            } else {
                rank[iCount] = 0;
            }
        }

        int bestRank = 0;
        for (int iCount = 0; iCount < rank.length; iCount++) {
            if (bestRank < rank[iCount]) {
                foundPosition = iCount;
                bestRank = rank[iCount];
            }
        }
        return bestRank > 0 ? foundPosition : -1;
    }

    protected abstract String getStartTagValueForAddedElement(Element newElement, Element oldElement, ContentComparatorContext context);

    protected String buildStartTagForAddedElement(ContentComparatorContext context) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    protected String buildStartTagForRemovedElement(Element element, ContentComparatorContext context, Map<String, Element> contentElements) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    protected abstract void appendAddedElementsContent(ContentComparatorContext context);

    protected abstract Boolean shouldDisplayRemovedContent(Element elementOldContent, int indexOfOldElementInNewContent);

    protected abstract Boolean containsIgnoredElements(Node node);

    protected abstract Boolean isCurrentElementNonIgnored(Node node);

    protected abstract Boolean isCurrentElementIgnored(Node node);

    protected abstract boolean isCurrentElementIgnoredInNewContent(ContentComparatorContext context);

    protected abstract boolean containsDeletedElementInNewContent(ContentComparatorContext context);

    protected abstract Boolean isIgnoredElement(Element element);

    protected abstract Boolean shouldAddElement(Element oldElement, Map<String, Element> contentElements);

    protected abstract void appendAddedElementContent(ContentComparatorContext context);

    protected abstract void appendRemovedElementContent(ContentComparatorContext context);

    protected abstract void appendRemovedContent(ContentComparatorContext context);

    protected abstract String getStartTagValueForRemovedElementFromAncestor(Element element, ContentComparatorContext context);

    protected abstract Boolean shouldAppendRemovedElement(ContentComparatorContext context);

    protected abstract int getIndexOfNewElementInOldContent(ContentComparatorContext context);

    protected abstract int getIndexOfOldElementInNewContent(ContentComparatorContext context);
}
