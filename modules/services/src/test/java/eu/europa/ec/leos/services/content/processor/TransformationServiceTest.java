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
package eu.europa.ec.leos.services.content.processor;

import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TransformationServiceTest extends LeosTest {

    @InjectMocks
    private Configuration freemarkerConfiguration = new Configuration();
    @Mock
    private TemplateHashModel enumModels;
    @InjectMocks
    private TransformationServiceImpl transformationService = new TransformationServiceImpl(freemarkerConfiguration, enumModels);

    private final String FILE_PREFIX = "/transformation/";

    @Before
    public void setUp() {
        super.setup();
        String path = "/src/main/resources/eu/europa/ec/leos/freemarker/templates/";
        String templateName = "legalText/akn_fragment_xml_wrapper.ftl";
        ReflectionTestUtils.setField(transformationService, "editableXHtmlTemplate", path + templateName);
    }

    @Test
    public void test_transformation_citation_elementWithoutNamespace() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "test_transformation_citation_elementWithoutNamespace.xml");
        InputStream contentStream = new ByteArrayInputStream(documentXml);
        transformationService.formatToHtml(contentStream, "", null);
    }

    /**
     *  For now this error is being avoided calling removeAllNameSpaces(byte[]).
     *  @See XmlContentProcessor.getElementByNameAndId(byte[], String, String)
     */
    @Test(expected = RuntimeException.class)
    public void test_transformation_citation_elementWithNamespace() {
        byte[] documentXml = TestUtils.getFileContent(FILE_PREFIX + "test_transformation_citation_elementWithNamespace.xml");
        InputStream contentStream = new ByteArrayInputStream(documentXml);
        transformationService.formatToHtml(contentStream, "", null);
    }
}
