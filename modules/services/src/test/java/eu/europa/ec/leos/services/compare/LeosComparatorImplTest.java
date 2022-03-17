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
package eu.europa.ec.leos.services.compare;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_REMOVED_CLASS;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;

public class LeosComparatorImplTest extends LeosTest {

    @Autowired
    protected MessageHelper messageHelper;
    @InjectMocks
    private TextComparator textComparator = new LeosTextComparatorImpl(messageHelper);

    @Test
    public void test_brokenReference() {
        String original = "Text...<mref id=\"art_1_HiRJJI\">Annex, point <ref id=\"art_1_723tGC\">2";
        String revised = "Text...<mref id=\"art_1_HiRJJI\" leos:broken=\"true\">Annex, point <ref id=\"art_1_723tGC\">2";

        String expected = "Text...<spanclass=\"leos-double-compare-removed\"><mrefid=\"dummyId\">Annex,point<refid=\"dummyId\">2</ref></mref></span><spanclass=\"leos-double-compare-added\"><mrefid=\"dummyId\"leos:broken=\"true\">Annex,point<refid=\"dummyId\">2</ref></mref></span>";

        ContentComparatorContext context = getContentComparatorContext(original, revised);
        String diffResult = textComparator.compareTextNodeContents(original, revised, null, context);
        assertEquals(squeezeXml(expected), squeezeXml(diffResult));
    }

    @Test
    public void test_text_comparator_authnote_markers_changed() {
        String original = "Council<authorialNote leos:origin=\"ec\" xml:id=\"recs_rqlwcX\" marker=\"4\"><p leos:origin=\"ec\" xml:id=\"recs_AJOwiR\">Directive 97/67/EC of the European Parliament</p></authorialNote> users.";
        String revised =  "Council<authorialNote leos:origin=\"ec\" xml:id=\"recs_rqlwcX\" marker=\"3\"><p leos:origin=\"ec\" xml:id=\"recs_AJOwiR\">Directive 97/67/EC of the European Parliament</p></authorialNote> users.";

        String expected = "Council<span class=\"leos-double-compare-removed\"><authorialNote leos:origin=\"ec\" xml:id=\"deleted_deleted_recs_rqlwcX\" marker=\"4\">\n" +
                " <p leos:origin=\"ec\" xml:id=\"deleted_deleted_recs_AJOwiR\">Directive 97/67/EC of the European Parliament</p>\n" +
                "</authorialNote></span><span class=\"leos-double-compare-added\"><authorialNote leos:origin=\"ec\" xml:id=\"recs_rqlwcX\" marker=\"3\">\n" +
                " <p leos:origin=\"ec\" xml:id=\"recs_AJOwiR\">Directive 97/67/EC of the European Parliament</p>\n" +
                "</authorialNote></span> users.";

        ContentComparatorContext context = getContentComparatorContext(original, revised);
        String diffResult = textComparator.compareTextNodeContents(original, revised, null, context);
        assertEquals(squeezeXml(expected), squeezeXml(diffResult));
    }

    private ContentComparatorContext getContentComparatorContext(String original, String revised) {
        return new ContentComparatorContext.Builder(original, revised, "")
                .withAttrName(ATTR_NAME)
                .withRemovedValue(DOUBLE_COMPARE_REMOVED_CLASS)
                .withAddedValue(DOUBLE_COMPARE_ADDED_CLASS)
                .build();
    }

}
