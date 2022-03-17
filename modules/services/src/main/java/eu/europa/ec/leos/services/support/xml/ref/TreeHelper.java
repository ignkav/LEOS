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
package eu.europa.ec.leos.services.support.xml.ref;

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.services.support.xml.XmlUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.AKOMANTOSO;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BILL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CONTENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.MAIN_BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREAMBLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.RECITALS;

public class TreeHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TreeHelper.class);

    private static List<String> NOT_SIGNIFICANT_NODES = Arrays.asList(BILL, CONTENT, LIST, PREFACE, PREAMBLE, BODY, MAIN_BODY, RECITALS);

    public static TreeNode createTree(Node node, TreeNode root, List<Ref> refs) {
        Validate.isTrue(refs != null && !refs.isEmpty(), "refs can not be empty");
        Stopwatch watch = Stopwatch.createStarted();

        for (Ref ref : refs) {
            Node elementNode = XmlUtils.getElementById(node, ref.getHref());
            if (elementNode == null) {
                //probably it is broken reference
                LOG.debug("Element with id: {} does not exists. Skipping", ref.getHref());
                continue;
            }
            
            // This block finds all ancestors till some already exists in tree
            TreeNode parent = null;
            String tagName;
            Stack<Node> nodeStack = new Stack<>();
            do {
                tagName = elementNode.getNodeName();
                if (NOT_SIGNIFICANT_NODES.contains(tagName)) {
                    continue;
                }
                //find if any ancestor of current node exists in tree. If it does, break and attach all children to this node in tree. 
                else if ((parent = find(root, TreeNode::getIdentifier, XmlUtils.getId(elementNode))) != null) {
                    break;
                }
                nodeStack.push(elementNode);
            } while (((elementNode = elementNode.getParentNode()) != null) && !AKOMANTOSO.equals(tagName));

            //this block creates node subtree and attaches them in tree 
            int depth;
            while (!nodeStack.isEmpty()) {
                depth = (parent == null) ? 0 : parent.getDepth() + 1;
                TreeNode currentNode = createNode(nodeStack.pop(), parent, depth, ref.getDocumentref(), ref.getOrigin());

                //if tree root is not assigned
                if (root == null) {
                    root = currentNode;
                }

                //if this is leaf, set ref
                if (currentNode.getIdentifier() != null && currentNode.getIdentifier().equals(ref.getHref())) {
                    currentNode.setRefId(ref.getId());
                }
                //add at appropriate place
                if (parent != null) {
                    parent.addChildren(currentNode);
                    parent.getChildren().sort(Comparator.comparingInt(TreeNode::getSiblingNumber));
                }

                parent = currentNode;
            }
        }

        LOG.trace("Create tree in {} ms", watch.elapsed(TimeUnit.MILLISECONDS));
        return root;
    }

    public static TreeNode find(TreeNode start, Function<TreeNode, Object> condition, Object value) {
        if (start == null) {
            return null;
        }
        Object nodeValue = condition.apply(start);
        if ((nodeValue == null && value == null) ||
                (nodeValue != null && nodeValue.equals(value))) {
            return start;
        }
        for (TreeNode child : start.getChildren()) {
            TreeNode result = find(child, condition, value);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static List<TreeNode> getLeaves(TreeNode start) {
        List<TreeNode> leaves = new ArrayList<>();
        if (start == null) {
            return leaves;
        }
        if (start.getChildren().isEmpty()) {
            leaves.add(start);
        } else {
            for (TreeNode child : start.getChildren()) {
                leaves.addAll(getLeaves(child));
            }
        }
        return leaves;
    }

    private static TreeNode createNode(Node node, TreeNode parent, int depth, String documentRef, String origin) {
        String tagName = null;
        String tagId = null;
        
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            tagName = node.getNodeName();
            tagId = XmlUtils.getId(node);
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            tagName = "text";
        }
        
        //find num
        String numValue = findNum(node);
        int childSeq = findSeq(node, tagName);
        return new TreeNode(tagName, depth, childSeq, tagId, numValue, parent, documentRef, origin);
    }

    public static List<TreeNode> findCommonAncestor(Node sourceNode, String sourceRefId, String sourceDocumentRef, TreeNode targetTree) {
        List<TreeNode> nodes = new ArrayList<>();

        if (sourceDocumentRef.equals(targetTree.getDocumentRef())) {
            List<String> ancestorsIdsOfMrefParent = getAncestorsIdsForElementId(sourceNode, sourceRefId);

            for (String ancestorIdOfMrefParent : ancestorsIdsOfMrefParent) {
                TreeNode node = find(targetTree, TreeNode::getIdentifier, ancestorIdOfMrefParent);
                if (node != null) {
                    nodes.add(node);
                }
            }
        }

        return nodes;
    }

    private static List<String> getAncestorsIdsForElementId(Node node, String idAttributeValue) {
        LinkedList<String> ancestorsIds = new LinkedList<String>();
        Node elementNode = XmlUtils.getElementById(node, idAttributeValue);
        if (elementNode != null) {
            ancestorsIds.add(idAttributeValue);
            while (elementNode.getParentNode() != null) {
                ancestorsIds.addFirst(XmlUtils.getParentId(elementNode));
                elementNode = elementNode.getParentNode();
            }
        }
        return ancestorsIds;
    }

    private static int findSeq(Node node, String tagName) {
        int childSeq = 1;
        while ((node = XmlUtils.getSibling(node, true)) != null) {
            if ((tagName == null || tagName.equals(node.getNodeName())) //this considers elements of same type only.
                    && !XmlUtils.toBeSkippedForNumbering(node)) {
                childSeq++;
            }
        }
        return childSeq;
    }

    private static String findNum(Node node) {
        Node numNode = XmlUtils.getFirstChild(node, NUM);
        return numNode != null ? parseNum(numNode.getTextContent()) : null;
    }

    private static String parseNum(String xmlNum) {
        //remove type if part/section/
        String[] s = xmlNum.split(" ", 2);
        String num = s.length > 1 ? s[1] : s[0];
        //clean spaces, .,(), etc
        return num.replaceAll("[\\s+|\\(|\\)]|\\.$", "");
    }
}
