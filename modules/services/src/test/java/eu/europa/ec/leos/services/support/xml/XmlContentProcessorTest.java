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

import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.content.ReferenceLabelService;
import eu.europa.ec.leos.services.content.TemplateStructureService;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.TocItemUtils;
import io.atlassian.fugue.Option;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.BODY;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CHAPTER;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.CONCLUSIONS;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PART;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREAMBLE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.PREFACE;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SECTION;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.TITLE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class XmlContentProcessorTest extends LeosTest {
    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());
    @Mock
    protected LanguageHelper languageHelper;
    @Mock
    protected ReferenceLabelService referenceLabelService;
    @Mock
    protected Provider<StructureContext> structureContextProvider;
    @Mock
    protected StructureContext structureContext;
    @Mock
    protected TemplateStructureService templateStructureService;
    @Mock
    protected PackageService packageService;
    @Mock
    protected CloneContext cloneContext;
    @Mock
    protected SecurityContext securityContext;

    @InjectMocks
    protected StructureServiceImpl structureServiceImpl;

    protected TocItem tocItemConclusions;
    protected TocItem tocItemArticle;
    protected TocItem tocItemBody;
    protected TocItem tocItemChapter;
    protected TocItem tocItemPart;
    protected TocItem tocItemPreface;
    protected TocItem tocItemPreamble;
    protected TocItem tocItemSection;
    protected TocItem tocItemTitle;

    protected List<TocItem> tocItems;
    protected List<NumberingConfig> numberingConfigs;
    protected Map<TocItem, List<TocItem>> tocRules;
    protected String docTemplate;
    protected String configFile;
    protected byte[] docContent;

    protected final static String FILE_PREFIX = "/contentProcessor";

    @Before
    public void setup() {
        super.setup();
        getStructureFile();

        byte[] bytesFile = TestUtils.getFileContent(configFile);
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);

        tocItemConclusions = TocItemUtils.getTocItemByName(tocItems, CONCLUSIONS);
        tocItemArticle = TocItemUtils.getTocItemByName(tocItems, ARTICLE);
        tocItemBody = TocItemUtils.getTocItemByName(tocItems, BODY);
        tocItemChapter = TocItemUtils.getTocItemByName(tocItems, CHAPTER);
        tocItemPart = TocItemUtils.getTocItemByName(tocItems, PART);
        tocItemPreface = TocItemUtils.getTocItemByName(tocItems, PREFACE);
        tocItemPreamble = TocItemUtils.getTocItemByName(tocItems, PREAMBLE);
        tocItemSection = TocItemUtils.getTocItemByName(tocItems, SECTION);
        tocItemTitle = TocItemUtils.getTocItemByName(tocItems, TITLE);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocRules()).thenReturn(tocRules);
        when(securityContext.getUserName()).thenReturn("demo");

        docContent = TestUtils.getFileContent(FILE_PREFIX + "/docContent.xml");
    }

    protected abstract void getStructureFile();

    private MessageHelper getMessageHelper() {
        try (ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml")) {
            MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
            MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
            return messageHelper;
        }
    }

    protected void setupClonedProposal(String proposalId) {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/test_getClonedProposalsMetadataVO.xml");
        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        LeosPackage leosPackage = new LeosPackage(proposalId, "pkg_name", "/leos/workspaces/pkg_name");
        XmlDocument xmlDocument = getMockedBill(proposalId, content);
        when(source.getBytes()).thenReturn(documentXml);
        when(content.getSource()).thenReturn(source);
        when(packageService.findPackageByDocumentId(proposalId)).thenReturn(leosPackage);
        when(packageService.findDocumentsByPackagePath(leosPackage.getPath(), XmlDocument.class, true)).thenReturn(Arrays.asList(xmlDocument));
    }

    private Proposal getMockedBill(String proposalId, Content content) {
        ProposalMetadata proposalMetadata = new ProposalMetadata("", "REGULATION for EC", "", "PR-00.xml", "EN", "", "proposal-id", "", "0.1.0");
        Proposal leosProposal = new Proposal(proposalId, "Proposal", "login", Instant.now(), "login", Instant.now(),
                "", "", "", "", VersionType.MAJOR, true,
                "REGULATION for EC", Collections.emptyList(), Arrays.asList(""), "login", Instant.now(),
                Option.some(content), Option.some(proposalMetadata), false, "", "", "");
        return leosProposal;
    }

}
