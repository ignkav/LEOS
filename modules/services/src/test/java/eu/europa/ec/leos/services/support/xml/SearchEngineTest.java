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

import eu.europa.ec.leos.domain.vo.ElementMatchVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class SearchEngineTest extends LeosTest {

    protected final static String PREFIX_SEARCH_REPLACE = "/searchReplace";

    @Before
    public void setup() {
        super.setup();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void searchReplace_stressTess_500Articles() {

        byte[] docContentGlobal = TestUtils.getFileContent("/xml-files/bill_500ArticlesComplexStructure.xml");
        byte[] docContent = docContentGlobal;
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        final String searchTextGlobal = "Having regard";
        final String replaceTextGlobal = "";
        List<SearchMatchVO> matches = se.searchText(searchTextGlobal, false, false);
        docContent = se.replace(docContent, matches, searchTextGlobal, replaceTextGlobal, true);
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-bill_500ArticlesComplexStructure-replace-expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(docContentExpected)), squeezeXmlAndRemoveAllNS(new String(docContent)));
    }

    @Test
    public void searchReplace_textElement_more_than_one_replace() {

        byte[] docContentGlobal = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple-same-text-element.xml");
        byte[] docContent = docContentGlobal;
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        final String searchTextGlobal = "search";
        final String replaceTextGlobal = "";
        List<SearchMatchVO> matches = se.searchText(searchTextGlobal, false, false);
        docContent = se.replace(docContent, matches, searchTextGlobal, replaceTextGlobal, true);
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple-same-text-element_expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(docContentExpected)), squeezeXmlAndRemoveAllNS(new String(docContent)));
    }

    @Test
    public void testReplace_InlineTags() {

        byte[] docContentGlobal = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-meta-replace-inline.xml");
        byte[] docContent = docContentGlobal;
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        final String searchTextGlobal = "Having regard";
        final String replaceTextGlobal = "";
        List<SearchMatchVO> matches = se.searchText(searchTextGlobal, false, false);
        docContent = se.replace(docContent, matches, searchTextGlobal, replaceTextGlobal, true);
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-meta-replace-inline-expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(docContentExpected)), squeezeXmlAndRemoveAllNS(new String(docContent)));
    }

    @Test
    public void testReplace_InlineTags_with_space() {

        byte[] docContentGlobal = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-meta-replace-inline-with-space.xml");
        byte[] docContent = docContentGlobal;
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        final String searchTextGlobal = "Having regard";
        final String replaceTextGlobal = "";
        List<SearchMatchVO> matches = se.searchText(searchTextGlobal, false, false);
        docContent = se.replace(docContent, matches, searchTextGlobal, replaceTextGlobal, true);
        byte[] docContentExpected = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-meta-replace-inline-with-space-expected.xml");
        assertEquals(squeezeXmlAndRemoveAllNS(new String(docContentExpected)), squeezeXmlAndRemoveAllNS(new String(docContent)));
    }

    @Test
    public void testForContent_withMetaContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-meta.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("Having regard", false, false);

        results.forEach(System.out::println);
        assertThat(results.size(), is(4));
        List<ElementMatchVO> matchedElements1 = results.get(0).getMatchedElements();
        assertThat(matchedElements1.size(), is(1));
        assertThat(matchedElements1, hasItem(new ElementMatchVO("cit_1__p", 0, 13)));

        List<ElementMatchVO> matchedElements2 = results.get(1).getMatchedElements();
        assertThat(matchedElements2.size(), is(1));
        assertThat(matchedElements2, hasItem(new ElementMatchVO("cit_2__p", 0, 13)));

        List<ElementMatchVO> matchedElements3 = results.get(2).getMatchedElements();
        assertThat(matchedElements3.size(), is(1));
        assertThat(matchedElements3, hasItem(new ElementMatchVO("cit_4__p", 5, 18)));

        List<ElementMatchVO> matchedElements4 = results.get(3).getMatchedElements();
        assertThat(matchedElements4.size(), is(1));
        assertThat(matchedElements4, hasItem(new ElementMatchVO("cit_5__p", 0, 13)));
    }

    @Test
    public void testForContent_withAuthorialContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-authorial.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        {
            List<SearchMatchVO> results = se.searchText("NoteContent", false, false);
            assertThat(results.size(), is(1));
            List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
            assertThat(matchedElements.size(), is(1));
            assertThat(matchedElements,
                    hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 317, 328)));

        }

        {
            List<SearchMatchVO> results = se.searchText("my searchEnd", false, false);
            assertThat(results.size(), is(1));
            List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
            assertThat(matchedElements.size(), is(1));
            assertThat(matchedElements,
                    hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p_2", 8, 20)));
        }
    }

    @Test
    public void testForContent_withSimpleContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("my search content", false, false);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 8, 25)));
    }

    @Test
    public void testForContent_uppercase_withSimpleContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("MY SEARCH CONTENT", true, false);

        assertThat(results.size(), is(0));
    }

    @Test
    public void testForContent_lowercase_withSimpleContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("my search content", true, false);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 8, 25)));
    }

    @Test
    public void testForContent_withCrossElementMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-crossElements.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("regulation on", false, false);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(2));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docType", 8, 18)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docPurpose", 0, 2)));
    }

    @Test
    public void testForContent_withCrossElementMatch_replace() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-crossElements-replace.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("regulation on", false, false);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(2));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docType", 8, 18)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docPurpose", 0, 2)));
        assertThat(matchedElements.get(0).isEditable(), is(false));
        assertThat(matchedElements.get(1).isEditable(), is(false));

        byte[] resultDoc = se.replace(docContent, results, "regulation on", "bazzinga", true);
        se = SearchEngineImpl.forContent(resultDoc);

        results = se.searchText("regulation on", false, false);
        assertThat(results.size(), is(1));

        matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(2));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docType", 8, 18)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docPurpose", 0, 2)));
        assertThat(matchedElements.get(0).isEditable(), is(false));
        assertThat(matchedElements.get(1).isEditable(), is(false));

        results = se.searchText("bazzinga", false, false);
        assertThat(results.size(), is(0));
    }

    @Test
    public void testForContent_replaceInEditableArticle() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-bill-with-articles.xml");
        byte[] expectedDocContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-bill-with-articles_expected.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("Regulation", true, false);

        assertThat(results.size(), is(2));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements.get(0).isEditable(), is(true));

        byte[] resultDoc = se.replace(docContent, results, "Regulation", "Directive", true);
        se = SearchEngineImpl.forContent(resultDoc);

        results = se.searchText("Regulation", true, false);
        assertThat(results.size(), is(1));

        matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements.get(0).isEditable(), is(false));

        results = se.searchText("Directive", true, false);
        assertThat(results.size(), is(1));
        assertEquals(squeezeXmlAndRemoveAllNS(new String(resultDoc, UTF_8)), squeezeXmlAndRemoveAllNS(new String(expectedDocContent, UTF_8)));
    }

    @Test
    public void testForContent_replaceInReadOnlyArticle() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-bill-with-readonly-articles.xml");
        byte[] expectedDocContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-bill-with-readonly-articles_expected.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("Regulation", true, false);

        assertThat(results.size(), is(2));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements.get(0).isEditable(), is(false));

        byte[] resultDoc = se.replace(docContent, results, "Regulation", "Directive", true);
        se = SearchEngineImpl.forContent(resultDoc);

        results = se.searchText("Regulation", true, false);
        assertThat(results.size(), is(2));

        matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements.get(0).isEditable(), is(false));

        results = se.searchText("Directive", true, false);
        assertThat(results.size(), is(0));
        assertEquals(squeezeXmlAndRemoveAllNS(new String(resultDoc, UTF_8)), squeezeXmlAndRemoveAllNS(new String(expectedDocContent, UTF_8)));
    }

    @Test
    public void testForContent_withAllContentFormatted() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-allContentFormatted.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("simple content", false, false);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(2));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_Stz06P", 6, 12)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_TErD12", 17, 25)));
    }

    @Test
    public void testForContent_withInnerTagMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-innerTags.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("is my search content", false, false);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(3));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 5, 8)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_E3FfJO", 0, 9)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 17, 25)));


        List<SearchMatchVO> resultsb = se.searchText("only Bold", false, false);

        matchedElements = resultsb.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_E3FfJO1", 0, 9)));

        results = se.searchText("Regulation", false, false);
        matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(5));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer_5", 0, 2)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_E3FfJO4", 0, 3)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer_5", 5, 7)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("italic2", 0, 2)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer_5", 9, 10)));


        results = se.searchText("exploraratory", false, false);
        matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(5));
        assertThat(matchedElements, hasItem(new ElementMatchVO("bol_id_1", 0, 2)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_E3FfJO5", 0, 3)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer_6", 15, 18)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("italic3", 0, 2)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer_6", 20, 23)));


        results = se.searchText("Following", false, false);
        matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(3));
        assertThat(matchedElements, hasItem(new ElementMatchVO("art_2__para_1__content__p", 56, 58)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("art_2_LDYF8e", 0, 4)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("art_2__para_1__content__p", 62, 65)));

    }

    @Test
    public void testForContent_withMultiLevelTagMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-multiLevelTags.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("is my search content with",false,false);;

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(6));

        //This does not look right
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 5, 8)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_TErD12", 0, 2)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 10, 18)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_E3FfJO", 0, 3)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_lgMXsf", 0, 4)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 25, 30)));
    }

    @Test
    public void testForContent_withMultipleHits() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-multipleHits.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("is my search content",false,false);;

        assertThat(results.size(), is(2));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 5, 25)));

        matchedElements = results.get(1).getMatchedElements();
        assertThat(matchedElements.size(), is(3));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_3__blockcontainer__p", 5, 8)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_E3FfJO3", 0, 9)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_3__blockcontainer__p", 17, 25)));
    }
}