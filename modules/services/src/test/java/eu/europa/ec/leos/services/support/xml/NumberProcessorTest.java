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

import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.content.TemplateStructureService;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public abstract class NumberProcessorTest extends LeosTest {

    protected final static String PREFIX_SAVE_TOC_CN = "/saveToc/bill/cn/";

    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());
    @Mock
    protected LanguageHelper languageHelper;
    @Mock
    protected Provider<StructureContext> structureContextProvider;
    @Mock
    protected StructureContext structureContext;
    @InjectMocks
    protected StructureServiceImpl structureServiceImpl;
    @Mock
    protected TemplateStructureService templateStructureService;

    protected List<TocItem> tocItemList;
    protected List<NumberingConfig> numberingConfigs;
    protected String docTemplate;
    protected String configFile;

    @Before
    public void setup() {
        super.setup();
        getStructureFile();

        byte[] bytesFile = TestUtils.getFileContent(configFile);
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        tocItemList = structureServiceImpl.getTocItems(docTemplate);
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getTocItems()).thenReturn(tocItemList);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
    }

    protected abstract void getStructureFile();

    protected MessageHelper getMessageHelper() {
        try (ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml")) {
            MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
            MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
            return messageHelper;
        }
    }

    protected void compare(byte[] actual, String expected) {
        String xmlActual = new String(actual)
                .replaceAll("\\s+", " ")
                .replaceAll("> ", ">")
                .replaceAll(" >", ">")
                .replaceAll("< ", "<")
                .replaceAll(" <", "<");
        expected = expected
                .replaceAll("\\s+", " ")
                .replaceAll("> ", ">")
                .replaceAll(" >", ">")
                .replaceAll("< ", "<")
                .replaceAll(" <", "<");
        assertThat(xmlActual, is(expected));
    }

}
