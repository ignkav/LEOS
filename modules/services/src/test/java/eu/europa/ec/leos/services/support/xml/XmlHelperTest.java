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

import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;

public class XmlHelperTest extends LeosTest {

    @Test
    public void test_removeEnclosingTags() {
        String str = "<aknP id=\"cit_5__p\">Having Regions<authorialNote id=\"authorialnote_2\" marker=\"2\" placement=\"bottom\"><aknP id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</aknP></authorialNote>,</aknP>";
        String expected = "Having Regions<authorialNote id=\"authorialnote_2\" marker=\"2\" placement=\"bottom\"><aknP id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</aknP></authorialNote>,";

        str = XmlHelper.removeEnclosingTags(str);
        assertEquals(expected, str);
    }

    @Test
    public void test_removeEnclosingTags_multiLines() {
        String str = "<aknP\n id=\"cit_5__p\"> Having regard to\n\n the Regions\r\n\n   <authorialNote \nid=\"authorialnote_2\"\n marker=\"2\" placement=\"bottom\"\n> \n     <aknP \n id=\"authorialNote_2__p\">OJ C [...], [...], p. \n[...]\n    </aknP> \n\r </authorialNote>,  \n\n</aknP>";
        String expected = "Having regard to the Regions<authorialNote id=\"authorialnote_2\" marker=\"2\" placement=\"bottom\"><aknP id=\"authorialNote_2__p\">OJ C [...], [...], p. [...]</aknP></authorialNote>,";

        str = XmlHelper.removeEnclosingTags(str);
        assertEquals(squeezeXml(expected), squeezeXml(str));
    }

}
