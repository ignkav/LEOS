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
package integration.importoj;

import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.integration.ExternalDocumentProvider;
import eu.europa.ec.leos.services.importoj.ConversionHelper;
import eu.europa.ec.leos.services.importoj.ImportServiceImpl;
import eu.europa.ec.leos.services.support.xml.NumberProcessor;
import eu.europa.ec.leos.services.support.xml.XPathCatalog;
import eu.europa.ec.leos.services.support.xml.XPathV1Catalog;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessorProposal;
import eu.europa.ec.leos.services.support.xml.XmlUtils;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static eu.europa.ec.leos.services.support.xml.XmlUtils.createDocument;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static eu.europa.ec.leos.test.support.model.ModelHelper.createBillForBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ImportServiceTest_IT extends LeosTest {

    @Mock
    protected ExternalDocumentProvider externalDocumentProvider;
    @Mock
    protected ConversionHelper conversionHelper;
    @Mock
    protected NumberProcessor numberProcessor;

    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorProposal());
    @InjectMocks
    protected XPathCatalog xPathCatalog = spy(new XPathV1Catalog());
    protected ImportServiceImpl importService;

    protected final static String PREFIX_FILE = "/importoj/";

    @Before
    public void onSetUp() {
        importService = new ImportServiceImpl(externalDocumentProvider, conversionHelper, xmlContentProcessor, numberProcessor, xPathCatalog);
    }

    @Test
    public void test_importElement() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_FILE, "test_importElement_154Articles.xml");
        final byte[] xmlStart = TestUtils.getFileContent(PREFIX_FILE, "test_importElement_start.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(PREFIX_FILE, "test_importElement_expected.xml");

        final Bill originalDocument = createBillForBytes(xmlStart);
        List<String> elementsIds = new ArrayList<>();
        IntStream.range(1, 100).forEach(val -> elementsIds.add("art_" + val));  //total are 155, import only first 100

        when(numberProcessor.renumberImportedArticle(any(), any())).thenAnswer(i -> i.getArguments()[0]);
        when(numberProcessor.renumberArticles(any())).thenAnswer(i -> i.getArguments()[0]);
        when(numberProcessor.renumberRecitals(any())).thenAnswer(i -> i.getArguments()[0]);

        // When
        long startTime = System.currentTimeMillis();
        byte[] xmlResult = importService.insertSelectedElements(originalDocument, xmlInput, elementsIds, "EN");
        long endTime = System.currentTimeMillis();

        // Then
        String result = new String(xmlResult);
        String expected = new String(xmlExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
        assertTrue(endTime-startTime < 20_000);  // check how you are converting Node to String. The time shouldn't go exponential.
    }

    @Test
    public void test_importElement_correctNamespaces() {
        final byte[] xmlInput = TestUtils.getFileContent(PREFIX_FILE, "test_importElement_154Articles.xml");
        final byte[] xmlStart = TestUtils.getFileContent(PREFIX_FILE, "test_importElement_start.xml");

        final Bill originalDocument = createBillForBytes(xmlStart);
        List<String> elementsIds = new ArrayList<>();
        IntStream.range(1, 2).forEach(val -> elementsIds.add("art_" + val));  //total are 155, import only 1

        when(numberProcessor.renumberImportedArticle(any(), any())).thenAnswer(i -> i.getArguments()[0]);
        when(numberProcessor.renumberArticles(any())).thenAnswer(i -> i.getArguments()[0]);
        when(numberProcessor.renumberRecitals(any())).thenAnswer(i -> i.getArguments()[0]);

        // When
        byte[] xmlResult = importService.insertSelectedElements(originalDocument, xmlInput, elementsIds, "EN");

        // Then
        Document document = createDocument(xmlResult);
        String actualResult = XmlUtils.nodeToString(document);
        assertFalse(actualResult.contains("xmlns:fmx"));
        assertFalse(actualResult.contains("xmlns:fn"));
    }

}
