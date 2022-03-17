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
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorLevel;
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

public class NumberProcessorAnnexProposalTest extends NumberProcessorTest {

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
    private NumberProcessor numberProcessorArticleNoChildren = new NumberProcessorArticleNoChildren(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor numberProcessorRecitalNoChildren = new NumberProcessorRecitalNoChildren(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor numberProcessorLevel = new NumberProcessorLevel(messageHelper, numberProcessorHandler);

    private ElementNumberingHelper elementNumberingHelper;
    private ProposalNumberingProcessor proposalNumberingProcessor;

    protected final static String FILE_PREFIX = "/numbering/annex/";

    @Before
    public void setup() {
        super.setup();
        elementNumberingHelper = new ElementNumberingHelper(numberProcessorHandler, structureContextProvider);
        proposalNumberingProcessor = new ProposalNumberingProcessor(elementNumberingHelper, messageHelper);
        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors",
                Arrays.asList(processorArticle, processorParagraph, processorPoint, numberProcessorArticleNoChildren, numberProcessorRecitalNoChildren, numberProcessorLevel));
    }

    @Override
    protected void getStructureFile() {
        docTemplate = "SG-017";
        configFile = "/structure-test-annex-EC.xml";
    }

    @Test
    public void test_renumbering_new_level_added_as_sibling() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_sibling.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_sibling_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_new_level_added_as_child() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_child.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_as_child_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_new_level_added_at_multiple_sublevel() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_at_multiple_sublevel.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_new_level_added_at_multiple_sublevel_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }

    @Test
    public void test_renumbering_level_with_soft_attributes() {
        final byte[] xmlInput = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_level_with_soft_attr.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FILE_PREFIX, "test_renumbering_level_with_soft_attr_expected.xml");
        byte[] result = proposalNumberingProcessor.renumberLevel(xmlInput);
        assertEquals(squeezeXmlAndRemoveAllNS(new String(xmlExpected)), squeezeXmlAndRemoveAllNS(new String(result)));
    }
}
