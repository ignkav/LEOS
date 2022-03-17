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
package eu.europa.ec.leos.services.support.xml;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlContentProcessorHelper.getAllChildTableOfContentItems;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.createDocument;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getFirstElementByName;

@Component
@Instance(instances = {InstanceType.OS, InstanceType.COMMISSION})
public class XmlTableOfContentHelperImpl implements XmlTableOfContentHelper {

    private static final Logger LOG = LoggerFactory.getLogger(XmlTableOfContentHelperImpl.class);

    @Autowired
    protected Provider<StructureContext> structureContextProvider;

    public List<TableOfContentItemVO> buildTableOfContent(String startingNode, byte[] xmlContent, TocMode mode) {
        LOG.trace("Start building TOC from tag {} and mode {}", startingNode, mode);
        long startTime = System.currentTimeMillis();
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        Map<TocItem, List<TocItem>> tocRules = structureContextProvider.get().getTocRules();

        List<TableOfContentItemVO> itemVOList = new ArrayList<>();
        try {
            Document document = createDocument(xmlContent);
            Node node = getFirstElementByName(document, startingNode);
            if (node != null) {
                itemVOList = getAllChildTableOfContentItems(node, tocItems, tocRules, mode);
            }
            LOG.debug("Build table of content completed in {} ms", (System.currentTimeMillis() - startTime));
            return itemVOList;
        } catch (Exception e) {
            throw new RuntimeException("Unable to build the Table of content item list", e);
        }
    }
}
