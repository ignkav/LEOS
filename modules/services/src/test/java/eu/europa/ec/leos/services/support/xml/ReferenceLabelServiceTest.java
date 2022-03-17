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
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.support.xml.ReferenceLabelServiceImplForProposal;
import eu.europa.ec.leos.services.support.xml.ref.LabelArticleElementsOnly;
import eu.europa.ec.leos.services.support.xml.ref.LabelArticlesOrRecitalsOnly;
import eu.europa.ec.leos.services.support.xml.ref.LabelCitationsOnly;
import eu.europa.ec.leos.services.support.xml.ref.LabelHigherOrderElementsOnly;
import eu.europa.ec.leos.services.support.xml.ref.Ref;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import io.atlassian.fugue.Option;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReferenceLabelServiceTest extends LeosTest {

    protected byte[] docContent;

    protected XmlDocument document;

    @InjectMocks
    protected ReferenceLabelServiceImplForProposal referenceLabelGenerator = new ReferenceLabelServiceImplForProposal();

    @Mock
    protected LanguageHelper languageHelper;

    @Mock
    private WorkspaceService workspaceService;

    @Before
    public void setup() {
        super.setup();

        docContent = TestUtils.getFileContent("/referenceLabel-bill.xml");
        ReflectionTestUtils.setField(referenceLabelGenerator, "labelHandlers",
                Arrays.asList(new LabelHigherOrderElementsOnly(),
                        new LabelArticlesOrRecitalsOnly(),
                        new LabelCitationsOnly(),
                        new LabelArticleElementsOnly()));

        when(languageHelper.getCurrentLocale()).thenReturn(new Locale("en"));

        Content content = mock(Content.class);
        Content.Source source = mock(Content.Source.class);
        when(source.getBytes()).thenReturn(docContent);
        when(content.getSource()).thenReturn(source);
        document = getMockedBill(content);
        when(workspaceService.findDocumentByRef("bill", XmlDocument.class)).thenReturn(document);
    }

    /**
     * The aim of the test is not to compare the result rather than to check if NullPointerException occurs.
     */
    @Test
    public void generateLabel_notSupportedElement_shouldNotThrowNPE() throws Exception {
        referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","preface", "", null)), "", "a5_FeJW6z", document.getContent().get().getSource().getBytes());
        referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","preamble", "", null)), "", "a5_FeJW6z",  document.getContent().get().getSource().getBytes());
        referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","cits", "", null)), "", "a5_FeJW6z", document.getContent().get().getSource().getBytes());
        referenceLabelGenerator.generateLabel(Arrays.asList(new Ref("","recs", "", null)), "", "a5_FeJW6z", document.getContent().get().getSource().getBytes());
    }

    private Bill getMockedBill(Content content) {
        BillMetadata billMetadata = new BillMetadata("", "REGULATION", "", "SJ-023", "EN","", "bill", "", "0.1.0");
        return new Bill("1", "Legaltext", "login", Instant.now(), "login", Instant.now(),
                "", "", "Version 1.0.0", "", VersionType.MAJOR, true, "title", null, Arrays.asList(""), Option.some(content), Option.some(billMetadata));
    }
}
