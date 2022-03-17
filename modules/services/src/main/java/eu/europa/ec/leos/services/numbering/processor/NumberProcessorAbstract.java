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

import eu.europa.ec.leos.i18n.MessageHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createElementAsFirstChildOfNode;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getFirstChild;

public abstract class NumberProcessorAbstract implements NumberProcessor {

    final protected MessageHelper messageHelper;
    final protected NumberProcessorHandler numberProcessorHandler;

    public NumberProcessorAbstract(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        this.messageHelper = messageHelper;
        this.numberProcessorHandler = numberProcessorHandler;
    }

    protected static Node buildNumElement(Document document, Node node, String numLabel) {
        Node numNode = getFirstChild(node, NUM);
        if (numNode != null) {
            numNode.setTextContent(numLabel);
        } else {
            numNode = createElementAsFirstChildOfNode(document, node, NUM, numLabel);
        }
        return numNode;
    }
}
