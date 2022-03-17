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
package eu.europa.ec.leos.services.numbering;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorHandler;
import eu.europa.ec.leos.services.support.xml.ElementNumberingAbstract;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.inject.Provider;
import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlUtils.createDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.nodeToByteArray;

@Component
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class ElementNumberingHelper extends ElementNumberingAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(ElementNumberingHelper.class);

    private final NumberProcessorHandler numberProcessorHandler;
    private final Provider<StructureContext> structureContextProvider;
    private List<TocItem> tocItems;

    @Autowired
    public ElementNumberingHelper(NumberProcessorHandler numberProcessorHandler, Provider<StructureContext> structureContextProvider) {
        this.numberProcessorHandler = numberProcessorHandler;
        this.structureContextProvider = structureContextProvider;
    }

    @Override
    public byte[] renumberElements(String elementName, byte[] xmlContent, boolean namespaceEnabled) {
        tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, elementName)) {
            Document document = createDocument(xmlContent, namespaceEnabled);
            numberProcessorHandler.renumberChildren(document, document, elementName);
            return nodeToByteArray(document);
        }
        return xmlContent;
    }

    @Override
    public byte[] renumberElements(String elementName, byte[] xmlContent, MessageHelper messageHelper) {
        tocItems = structureContextProvider.get().getTocItems();
        if (isAutoNumberingEnabled(tocItems, elementName)) {
            Document document = createDocument(xmlContent);
            numberProcessorHandler.renumberElementsByName(document, document, elementName);
            return nodeToByteArray(document);
        }
        return xmlContent;
    }

    @Override
    public byte[] levelComplexNumbering(byte[] xmlContent) {
        throw new IllegalStateException("Not implemented yet!");
    }
}
