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
package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.services.numbering.config.NumberingConfigProcessor;
import eu.europa.ec.leos.services.numbering.config.NumberingConfigProcessorFactory;
import eu.europa.ec.leos.services.support.xml.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeForSoftAction;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getId;
import static java.util.Arrays.asList;

@Component
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class NumberProcessorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorHandler.class);

    @Autowired
    protected List<NumberProcessor> numberProcessors;
    @Autowired
    protected NumberingConfigProcessorFactory numberConfigProcessorFactory;

    private static List<SoftActionType> softActionTypesToSkip = asList(SoftActionType.DELETE, SoftActionType.MOVE_TO);

    public void renumberElementsByName(Document document, Node node, String elementName) {
        LOG.trace("renumberElementsByName - Will process elements '{}' inside tree structure of nodeName '{}', nodeId '{}'", elementName, node.getNodeName(), getId(node));
        NodeList elements = document.getElementsByTagName(elementName);
        List<Node> nodeList = getNodesAsList(elements);
        LOG.trace("renumberElementsByName - Found {} '{}' to number inside nodeName '{}', nodeId '{}'", nodeList.size(), elementName, node.getNodeName(), getId(node));
        renumber(document, nodeList, false);
    }

    public void renumberChildren(Document document, Node node, String childrenName) {
        LOG.trace("renumberChildren - Will process elements '{}' inside nodeName '{}', nodeId '{}'", childrenName, node.getNodeName(), getId(node));
        List<Node> children = getChildren(node, childrenName);
        LOG.trace("renumberChildren - Found {} '{}' to number inside nodeName '{}', nodeId '{}'", children.size(), childrenName, node.getNodeName(), getId(node));
        renumber(document, children, true);
    }

    private void renumber(Document document, List<Node> nodeList, boolean renumberChildren) {
        if (nodeList.size() > 0) {
            final Node firstChild = nodeList.get(0);
            final int elementDepth = getElementDepth(firstChild);
            final NumberingConfigProcessor numberingConfigProcessor = numberConfigProcessorFactory.getNumberProcessor(firstChild.getNodeName(), elementDepth);
            LOG.trace("Initialised {}'s numberingConfigProcessor {} ", firstChild.getNodeName(), numberingConfigProcessor);

            if (firstChild.getNodeName().equals(LEVEL)) {
                // TODO: logic of LEVEL is done inside the processor. We can chain the logic as done for the other elements
                callNumberProcessors(firstChild.getNodeName(), document, firstChild, numberingConfigProcessor, renumberChildren);
            } else {
                for (int i = 0; i < nodeList.size(); i++) {
                    if (!skipAutoRenumbering(nodeList.get(i))) {
                        callNumberProcessors(firstChild.getNodeName(), document, nodeList.get(i), numberingConfigProcessor, renumberChildren);
                    }
                }
            }
        }
    }

    private void callNumberProcessors(String elementName, Document document, Node node, NumberingConfigProcessor numberingConfigProcessor, boolean numberChildren) {
        for (NumberProcessor numberProcessor : numberProcessors) {
            if (numberProcessor.canRenumber(elementName, node, numberChildren)) {
                numberProcessor.renumber(elementName, document, node, numberingConfigProcessor);
                break;
            }
        }
    }

    private List<Node> getChildren(Node node, String elementName) {
        if (elementName.equals(POINT)) {
            List<Node> listNodes = XmlUtils.getChildren(node, LIST);
            LOG.trace("getChildren. Found {} LISTs inside nodeName {}, nodeId {}", listNodes.size(), node.getNodeName(), getId(node));
            for (int i = 0; i < listNodes.size(); i++) {
                Node listNode = listNodes.get(i);
                return XmlUtils.getChildren(listNode, POINT);
            }
        } else {
            return XmlUtils.getChildren(node, elementName);
        }

        throw new IllegalStateException("Check what is wrong with nodeName: " + node.getNodeName() + ", nodeId: " + getId(node));
    }

    public static int getElementDepth(Node node) {
        int pointDepth = 0;
        if (node.getNodeName().equals(POINT)) {
            Node parentNode = node.getParentNode();
            while (parentNode != null) {
                String parentName = parentNode.getNodeName();
                if (LIST.equals(parentName)) {
                    pointDepth++;
                } else if (PARAGRAPH.equals(parentName)) {
                    break;
                }
                parentNode = parentNode.getParentNode();
            }
        }
        return pointDepth;
    }

    private List<Node> getNodesAsList(NodeList nodeList) {
        List<Node> children = new ArrayList<>();
        for (int i=0; i<nodeList.getLength(); i++){
            children.add(nodeList.item(i));
        }
        return children;
    }

    public static boolean skipAutoRenumbering(Node node) {
        SoftActionType actionType = getAttributeForSoftAction(node, LEOS_SOFT_ACTION_ATTR);
        return softActionTypesToSkip.contains(actionType);
    }
}
