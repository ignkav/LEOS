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
package integration.saveToc;

import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MandateMessageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.CloneContext;
import eu.europa.ec.leos.services.content.TemplateStructureService;
import eu.europa.ec.leos.services.support.xml.ref.LabelArticleElementsOnly;
import eu.europa.ec.leos.services.support.xml.ref.LabelArticlesOrRecitalsOnly;
import eu.europa.ec.leos.services.support.xml.ref.LabelCitationsOnly;
import eu.europa.ec.leos.services.support.xml.ref.LabelHandler;
import eu.europa.ec.leos.services.support.xml.ref.LabelHigherOrderElementsOnly;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.validation.handlers.AkomantosoXsdValidator;
import eu.europa.ec.leos.test.support.LeosTest;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJaneTestUser;
import static org.mockito.Mockito.when;

public abstract class SaveTocTest_IT extends LeosTest {

    protected final static String PREFIX_SAVE_TOC_BILL = "/saveToc/bill/";
    protected final static String PREFIX_SAVE_TOC_BILL_EC = "/saveToc/bill/ec/";
    protected final static String PREFIX_SAVE_TOC_BILL_CN = "/saveToc/bill/cn/";
    protected final static String PREFIX_SAVE_TOC_ANNEX = "/saveToc/annex/";
    protected final static String PREFIX_SAVE_TOC_ANNEX_EC = "/saveToc/annex/ec/";
    protected final static String PREFIX_SAVE_TOC_ANNEX_CN = "/saveToc/annex/cn/";

    @Mock
    protected SecurityContext securityContext;
    @Mock
    protected Authentication authentication;
    @Mock
    protected UserDetails userDetails;
    @Mock
    protected Provider<StructureContext> structureContextProvider;
    @Mock
    protected StructureContext structureContext;
    @Mock
    protected TemplateStructureService templateStructureService;
    @Mock
    protected LanguageHelper languageHelper;
    @Mock
    private eu.europa.ec.leos.security.SecurityContext leosSecurityContext;
    @Mock
    protected CloneContext cloneContext;

    protected AkomantosoXsdValidator akomantosoXsdValidator = new AkomantosoXsdValidator();

    @InjectMocks
    protected StructureServiceImpl structureServiceImpl;
    @InjectMocks
    protected List<LabelHandler> labelHandlers = Mockito.spy(Stream.of(new LabelArticleElementsOnly(),
            new LabelArticlesOrRecitalsOnly(),
            new LabelCitationsOnly(),
            new LabelHigherOrderElementsOnly())
            .collect(Collectors.toList()));
    @InjectMocks
    protected MessageHelper messageHelper = Mockito.spy(getMessageHelper());

    protected MessageHelper getMessageHelper() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-servicesContext.xml");
        MessageSource servicesMessageSource = (MessageSource) applicationContext.getBean("servicesMessageSource");
        MessageHelper messageHelper = new MandateMessageHelper(servicesMessageSource);
        return messageHelper;
    }

    protected List<TocItem> tocItems;
    protected String docTemplate;
    protected List<NumberingConfig> numberingConfigs;
    protected Map<TocItem, List<TocItem>> tocRules;

    @Before
    public void onSetUp() throws Exception {
        when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));
        when(userDetails.getUsername()).thenReturn(getJaneTestUser().getLogin());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_PATH", "eu/europa/ec/leos/xsd");
        ReflectionTestUtils.setField(akomantosoXsdValidator, "SCHEMA_NAME", "akomantoso30.xsd");
        akomantosoXsdValidator.initXSD();

        getStructureFile();

        ReflectionTestUtils.setField(structureServiceImpl, "structureSchema", "toc/schema/structure_1.xsd");
        numberingConfigs = structureServiceImpl.getNumberingConfigs(docTemplate);
        tocItems = structureServiceImpl.getTocItems(docTemplate);
        tocRules = structureServiceImpl.getTocRules(docTemplate);

        when(structureContextProvider.get()).thenReturn(structureContext);
        when(structureContext.getNumberingConfigs()).thenReturn(numberingConfigs);
        when(structureContext.getTocItems()).thenReturn(tocItems);
        when(structureContext.getTocRules()).thenReturn(tocRules);
        when(leosSecurityContext.getUserName()).thenReturn("jane");
    }

    protected abstract void getStructureFile();

}
