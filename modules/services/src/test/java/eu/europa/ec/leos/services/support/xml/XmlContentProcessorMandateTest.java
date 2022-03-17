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

import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;

public class XmlContentProcessorMandateTest extends XmlContentProcessorTest {

    @InjectMocks
    private XmlContentProcessorImpl xmlContentProcessor = new XmlContentProcessorMandate();

    @Override
    protected void getStructureFile() {
        docTemplate = "BL-023";
        configFile = "/structure-test-bill-CN.xml";
    }

    @Ignore // missing logic
    @Test
    public void test_removeEmptyHeading() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "/test_removeEmptyHeading.xml");
        String returnedElement = xmlContentProcessor.removeEmptyHeading(new String(documentXml, UTF_8));
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_removeEmptyHeading_cn_expected.xml");
        assertEquals(squeezeXml(new String(expected, UTF_8)), squeezeXml(returnedElement));
    }

    @Ignore // missing logic
    @Test
    public void test_removeElementByTagNameAndId_should_match_returnedTagContent() {
        byte[] returnedElement = xmlContentProcessor.removeElementByTagNameAndId(docContent, ARTICLE, "art486");
        byte[] expected = TestUtils.getFileContent(FILE_PREFIX + "/test_deleteElementByTagNameAndId_should_match_returnedTagContent.xml");
        assertEquals(squeezeXml(new String(expected)), squeezeXml(new String(returnedElement)));
    }

    @Test
    public void test_cleanSoftActions() {

    }

    @Test
    public void test_getSplittedElement() {

    }

    @Test
    public void test_getMergeOnElement() {

    }

    @Test
    public void test_mergeElement() {

    }

    @Test
    public void test_needsToBeIndented() {

    }

    @Test
    public void test_indentElement() {

    }

    @Test
    public void test_removeElementByTagNameAndId() {

    }

    @Test
    public void test_getClonedProposalsMetadataVO() {

    }
}
