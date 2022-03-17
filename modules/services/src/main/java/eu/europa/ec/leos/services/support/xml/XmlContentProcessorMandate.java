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

import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

//@Service
//@Instance(instances = {InstanceType.COUNCIL})
public class XmlContentProcessorMandate extends XmlContentProcessorImpl {

    private static final Logger LOG = LoggerFactory.getLogger(XmlContentProcessorMandate.class);

    @Autowired
    private PackageService packageService;

    @Override
    protected Node buildTocItemContent(List<TocItem> tocItems, List<NumberingConfig> numberingConfigs, Map<TocItem, List<TocItem>> tocRules, Document document, Node node, TableOfContentItemVO tocVo, User user) {
        return null;
    }

    @Override
    public byte[] cleanSoftActions(byte[] xmlContent) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public Pair<byte[], Element> getSplittedElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public Element getMergeOnElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public byte[] mergeElement(byte[] xmlContent, String content, String tagName, String idAttributeValue) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public boolean needsToBeIndented(String elementContent) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public byte[] indentElement(byte[] xmlContent, String elementName, String elementId, String elementContent, List<TableOfContentItemVO> toc) throws IllegalArgumentException {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public byte[] removeElementByTagNameAndId(byte[] xmlContent, String tagName, String elementId) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public void removeElement(Node node) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public String getUpdatedContent(Node node) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public void specificInstanceXMLPostProcessing(Node node) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public List<CloneProposalMetadataVO> getClonedProposalsMetadataVO(String proposalId, String legDocumentName) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public byte[] insertAffectedAttributeIntoParentElements(byte[] xmlContent, String idAttributeValue) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    public byte[] updateDepthAttribute(byte[] xmlContent) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    protected Pair<byte[], Element> buildSplittedElementPair(byte[] xmlContent, Element splitElement) {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }

    @Override
    protected byte[] doRemoveElementByTagNameAndId(byte[] xmlContent, String elementId, String originElementId) {
        return new byte[0];
    }
}