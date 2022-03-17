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

import eu.europa.ec.leos.services.numbering.config.NumberingConfigProcessorFactory;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessor;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorArticle;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorArticleNoChildren;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorParagraph;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorPoint;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorRecitalNoChildren;
import eu.europa.ec.leos.services.support.xml.NumberProcessorTest;
import eu.europa.ec.leos.services.support.xml.ProposalNumberingProcessor;
import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.junit.Assert.assertEquals;

public class NumberProcessorBillProposalTest extends NumberProcessorTest {

    @InjectMocks
    private NumberingConfigProcessorFactory numberingConfigProcessorFactory = Mockito.spy(new NumberingConfigProcessorFactory());
    @InjectMocks
    private NumberProcessorHandler numberProcessorHandler = new NumberProcessorHandler();

    @InjectMocks
    private NumberProcessor processorArticle = new NumberProcessorArticle(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorParagraph = new NumberProcessorParagraph(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorPoint = new NumberProcessorPoint(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorArticleSingle = new NumberProcessorArticleNoChildren(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorRecitalSingle = new NumberProcessorRecitalNoChildren(messageHelper, numberProcessorHandler);

    private ElementNumberingHelper elementNumberingHelper;
    private ProposalNumberingProcessor proposalNumberingProcessor;

    protected final static String FILE_PREFIX = "/numbering/bill/";
    protected final static String FILE_PREFIX_OJ = "/numbering/bill/import/";

    @Before
    public void setup() {
        super.setup();
        elementNumberingHelper = new ElementNumberingHelper(numberProcessorHandler, structureContextProvider);
        proposalNumberingProcessor = new ProposalNumberingProcessor(elementNumberingHelper, messageHelper);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors",
                Arrays.asList(processorArticle, processorParagraph, processorPoint, processorArticleSingle, processorRecitalSingle));
    }

    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-EC.xml";
    }

    @Test
    public void test_numbering_recitals() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_recitals_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberRecitals(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_articles() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberArticles(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_articles_with_soft_attributes() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberArticles(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_articles_with_soft_attributes_with_1st_element() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr_1st_element.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_numbering_articles_with_soft_attr_1st_element_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberArticles(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_article_importFromOJ_ec() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_importFromOJ_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_importFromOJ_ec_expected.xml");
        String result = proposalNumberingProcessor.renumberImportedArticle(new String(xmlInput), null);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_numbering_article_definition_importFromOJ_ec() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_definition_importFromOJ_ec.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX_OJ, "test_numbering_article_definition_importFromOJ_ec_expected.xml");
        String result = proposalNumberingProcessor.renumberImportedArticle(new String(xmlInput), null);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }
}
