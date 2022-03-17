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

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getId;

@Component
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class NumberProcessorArticle extends NumberProcessorAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorArticle.class);

    @Autowired
    public NumberProcessorArticle(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        super(messageHelper, numberProcessorHandler);
    }

    @Override
    public boolean canRenumber(String elementName, Node element, boolean renumberChildren) {
        return (ARTICLE.equals(elementName) && renumberChildren);
    }

    @Override
    public void renumber(String elementName, Document document, Node node, NumberingConfigProcessor numberProcessor) {
        LOG.trace("Will number element '{}' inside nodeName '{}', nodeId '{}' ", elementName, node.getNodeName(), getId(node));
        String elementNum = numberProcessor.getPrefix() + numberProcessor.getNextNumberToShow() + numberProcessor.getSuffix();
        elementNum = messageHelper.getMessage("legaltext.article.num", elementNum);
        buildNumElement(document, node, elementNum);
        LOG.debug("{} '{}' numbered to '{}'", node.getNodeName(), getId(node), elementNum);

        numberProcessorHandler.renumberChildren(document, node, PARAGRAPH);
    }

}
