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
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.numbering.config.NumberingConfigProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.CROSSHEADING;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LIST;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getChildren;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getFirstChild;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getId;

@Component
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class NumberProcessorPoint extends NumberProcessorAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorPoint.class);

    @Autowired
    public NumberProcessorPoint(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        super(messageHelper, numberProcessorHandler);
    }

    @Override
    public boolean canRenumber(String elementName, Node element, boolean renumberChildren) {
        if (POINT.equals(elementName) && renumberChildren) {
            return true;
        }
        return false;
    }

    @Override
    public void renumber(String elementName, Document document, Node node, NumberingConfigProcessor numberProcessor) {
        if (node.getNodeName().equalsIgnoreCase(POINT)
                || node.getNodeName().equalsIgnoreCase(INDENT)) { // Lists can now contain other elements than point or indent - skipping cross headings
            LOG.trace("Will number element '{}' inside nodeName '{}', nodeId '{}' ", elementName, node.getNodeName(), getId(node));
            String elementNum = numberProcessor.getPrefix() + numberProcessor.getNextNumberToShow() + numberProcessor.getSuffix();
            buildNumElement(document, node, elementNum);
            LOG.debug("{} '{}' numbered to '{}'", elementName, getId(node), elementNum);

            updatePointAndIndentNumbersDefault(node, document);
        }
    }

    private void updatePointAndIndentNumbersDefault(Node node, Document document) {
        List<Node> listNodes = getChildren(node, LIST);
        LOG.trace("Will number children of {} '{}'. {} LISTs found", node.getNodeName(), getId(node), listNodes.size());
        for (int i = 0; i < listNodes.size(); i++) {
            Node listNode = listNodes.get(i);
            String elementType = checkFirstChildType(listNode, INDENT) ? INDENT : POINT;
            LOG.trace("Will call processor to order {}s, inside nodeName '{}', nodeId '{}' ", elementType, node.getNodeName(), getId(node));
            numberProcessorHandler.renumberChildren(document, node, elementType);
        }
    }

    private boolean checkFirstChildType(Node node, String type) {
        boolean result = false;
        Node firstChild = getFirstChild(node, type);
        if (firstChild != null) {
            result = true;
        }
        return result;
    }
}

