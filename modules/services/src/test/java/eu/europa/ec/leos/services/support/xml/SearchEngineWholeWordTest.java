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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import eu.europa.ec.leos.domain.vo.ElementMatchVO;
import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class SearchEngineWholeWordTest extends LeosTest {

    protected final static String PREFIX_SEARCH_REPLACE = "/searchReplace";

    @Before
    public void setup() {
        super.setup();
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testForContent_2words_withMetaContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-meta.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("Having regard", false, true);

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
    public void testForContent_halfword_withMetaContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-meta.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("Hav", false, true);

        results.forEach(System.out::println);
        assertThat(results.size(), is(0));
    }
    @Test
    public void testForContent_1word_withSimpleContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("search", false, true);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 11, 17)));
    }
    @Test
    public void testForContent_1word_case_withSimpleContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("SUBSIDIARITY", true, true);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__heading", 13, 25)));

        results = se.searchText("subsidiarity", true, true);

        assertThat(results.size(), is(0));

    }
    @Test
    public void testForContent_halfword_withSimpleContent() {

        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-simple.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("sea", false, true);

        assertThat(results.size(), is(0));
    }
    @Test
    public void testForContent_2words_withCrossElementMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-crossElements.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("regulation on", false, true);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(2));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docType", 8, 18)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("em_coverpage__longTitle__docTitle__p__docPurpose", 0, 2)));
    }
    @Test
    public void testForContent_halfword_withCrossElementMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-crossElements.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("regula", false, true);

        assertThat(results.size(), is(0));
    }
    @Test
    public void testForContent_2words_withAllContentFormatted() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-allContentFormatted.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("simple content", false, true);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(2));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_TErD12", 17, 25)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_Stz06P", 6, 12)));
    }
    @Test
    public void testForContent_halfword_withAllContentFormatted() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-allContentFormatted.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("simple conte", false, true);

        assertThat(results.size(), is(0));
    }

    @Test
    public void testForContent_multiplewords_withInnerTagMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-innerTags.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("is my search content", false, true);

        assertThat(results.size(), is(1));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(3));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 5, 8)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("akn_E3FfJO", 0, 9)));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__tblock_2__blockcontainer__p", 17, 25)));
    }

    @Test
    public void testForContent_halfword_withInnerTagMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-innerTags.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("tent", false, true);

        assertThat(results.size(), is(0));    }

    @Test
    public void testForContent_multiplewords_withMultiLevelTagMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-multiLevelTags.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("is my search content with", false, true);

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
    public void testForContent_halfwords_withMultiLevelTagMatch() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-multiLevelTags.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("earch conten", false, true);

        assertThat(results.size(), is(0));
           }

    @Test
    public void testForContent_multiplewords_withMultipleHits() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-multipleHits.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("is my search content", false, true);

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
    @Test
    public void testForContent_1word_withMultipleHits() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-multipleHits.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("Subsidiarity", false, true);

        assertThat(results.size(), is(4));
        List<ElementMatchVO> matchedElements = results.get(0).getMatchedElements();
        assertThat(matchedElements.size(), is(1));
        assertThat(matchedElements, hasItem(new ElementMatchVO("tblock_2__heading", 13, 25)));
    }
    @Test
    public void testForContent_halfwords_withMultipleHits() {
        byte[] docContent = TestUtils.getFileContent(PREFIX_SEARCH_REPLACE + "/searchContent-multipleHits.xml");
        SearchEngine se = SearchEngineImpl.forContent(docContent);
        List<SearchMatchVO> results = se.searchText("earch content", false, true);

        assertThat(results.size(), is(0));
    }
}