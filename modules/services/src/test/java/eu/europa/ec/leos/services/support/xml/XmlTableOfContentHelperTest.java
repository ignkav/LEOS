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
import eu.europa.ec.leos.services.support.TableOfContentHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.ELEMENTS_TO_HIDE_CONTENT;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.removeTag;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public abstract class XmlTableOfContentHelperTest extends LeosTest {
    
    @Mock
    protected Provider<StructureContext> structureContextProvider;
    @Mock
    protected StructureContext structureContext;
    @Mock
    protected TemplateStructureService templateStructureService;
    @Mock
    protected LanguageHelper languageHelper;
    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());
    @InjectMocks
    protected StructureServiceImpl structureServiceImpl;
    @InjectMocks
    protected XmlTableOfContentHelper tableOfContentHelper = Mockito.spy(new XmlTableOfContentHelperImpl());
    
    protected MessageHelper getMessageHelper() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml");
        MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
        MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
        return messageHelper;
    }
    
    protected List<TocItem> tocItems;
    protected List<NumberingConfig> numberingConfigs;
    protected Map<TocItem, List<TocItem>> tocRules;
    protected String docTemplate;
    protected String configFile;
    
    protected final static String FILE_PREFIX = "/xml-files";
    
    @Before
    public void onSetUp() {
        super.setup();
        getStructureFile();
        
        byte[] bytesFile = TestUtils.getFileContent(configFile);
        when(templateStructureService.getStructure(docTemplate)).thenReturn(bytesFile);
        when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));
        
        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);
        
        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getTocRules()).thenReturn(tocRules);
    }
    
    protected abstract void getStructureFile();
    
    protected void compareTOCs(List<TableOfContentItemVO> expectedTOC, List<TableOfContentItemVO> tocList, boolean compareWith) {
        assertNotNull(expectedTOC);
        assertNotNull(tocList);
        assertEquals(expectedTOC.size(), tocList.size());
        
        for (int i = 0; i < expectedTOC.size(); i++) {
            TableOfContentItemVO expectedElement = expectedTOC.get(i);
            TableOfContentItemVO element = tocList.get(i);
            
            compareTOCItem(expectedElement, element, compareWith);
//            compareTOCItem(expectedElement.getParentItem(), element.getParentItem());
            
            assertNotNull(expectedElement.getChildItems());
            assertNotNull(element.getChildItems());
            assertEquals(expectedElement.getChildItems().size(), element.getChildItems().size());
            compareTOCs(expectedElement.getChildItems(), element.getChildItems(), compareWith);
        }
    }
    
    private void compareTOCItem(TableOfContentItemVO expectedElement, TableOfContentItemVO element, boolean compareWith) {
        if (expectedElement == null && element == null) {
            return;
        }
//        System.out.println("comparing: " + expectedElement.getId());
        assertEquals(expectedElement.getId(), element.getId());
        assertEquals(expectedElement.getTocItem(), element.getTocItem());
        assertEquals(expectedElement.getOriginAttr(), element.getOriginAttr());
        assertEquals(expectedElement.getNumber(), element.getNumber());
        assertEquals(expectedElement.getOriginNumAttr(), element.getOriginNumAttr());
        assertEquals(expectedElement.getHeading(), element.getHeading());
        assertEquals(expectedElement.getOriginHeadingAttr(), element.getOriginHeadingAttr());
        if (compareWith) { // vtd content is in xml format
            if (!ELEMENTS_TO_HIDE_CONTENT.contains(expectedElement.getTocItem().getAknTag().value())) {
                assertEquals(escapeHtml(removeTag(expectedElement.getContent())), escapeHtml(element.getContent()));
            }
        } else {
            assertEquals(expectedElement.getContent(), element.getContent());
        }

        assertEquals(expectedElement.getList(), element.getList());
        assertEquals(expectedElement.isMovedOnEmptyParent(), element.isMovedOnEmptyParent());
        assertEquals(expectedElement.isUndeleted(), element.isUndeleted());
        assertEquals(expectedElement.getSoftActionAttr(), element.getSoftActionAttr());
        assertEquals(expectedElement.isSoftActionRoot(), element.isSoftActionRoot());
        
        assertEquals(expectedElement.getSoftMoveTo(), element.getSoftMoveTo());
        assertEquals(expectedElement.getSoftMoveFrom(), element.getSoftMoveFrom());
        assertEquals(expectedElement.getSoftTransFrom(), element.getSoftTransFrom());
        assertEquals(expectedElement.getSoftUserAttr(), element.getSoftUserAttr());
        assertEquals(expectedElement.getSoftDateAttr(), element.getSoftDateAttr());
        assertEquals(expectedElement.isAffected(), element.isAffected());
        assertEquals(expectedElement.isNumberingToggled(), element.isNumberingToggled());
        assertEquals(expectedElement.getNumSoftActionAttr(), element.getNumSoftActionAttr());
        assertEquals(expectedElement.getHeadingSoftActionAttr(), element.getHeadingSoftActionAttr());
        
        assertEquals(expectedElement.isRestored(), element.isRestored());
        assertEquals(expectedElement.getItemDepth(), element.getItemDepth());
        assertEquals(expectedElement.getOriginalDepth(), element.getOriginalDepth());
        assertEquals(expectedElement.getElementNumberId(), element.getElementNumberId());
        assertEquals(expectedElement.getIndentOriginType(), element.getIndentOriginType());
        assertEquals(expectedElement.getIndentOriginIndentLevel(), element.getIndentOriginIndentLevel());
        assertEquals(expectedElement.getIndentOriginNumId(), element.getIndentOriginNumId());
        assertEquals(expectedElement.getIndentOriginNumValue(), element.getIndentOriginNumValue());
        assertEquals(expectedElement.getIndentOriginNumOrigin(), element.getIndentOriginNumOrigin());
        
        String expectedLabel = TableOfContentHelper.buildItemCaption(expectedElement, TableOfContentHelper.DEFAULT_CAPTION_MAX_SIZE, messageHelper);
        String label = TableOfContentHelper.buildItemCaption(element, TableOfContentHelper.DEFAULT_CAPTION_MAX_SIZE, messageHelper);
//        System.out.println("label: " + expectedLabel);
        assertEquals(expectedLabel.trim(), label.trim());
    }
    
}
