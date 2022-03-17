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
package eu.europa.ec.leos.annotate.services.impl.util;

import org.junit.Assert;
import org.junit.Test;

public class TextShortenerTest {

    @Test
    public void testGetFirstGivenNumberOfCharacters_None() throws Exception {

        Assert.assertEquals("", TextShortener.getFirstGivenNumberOfCharacters("abc", 0));
    }

    @Test
    public void testGetFirstGivenNumberOfCharacters_Number() throws Exception {

        Assert.assertEquals("ab", TextShortener.getFirstGivenNumberOfCharacters("abc", 2));
    }

    @Test
    public void testGetFirstGivenNumberOfCharacters_NumberLargerThanTextLength() throws Exception {

        Assert.assertEquals("abc", TextShortener.getFirstGivenNumberOfCharacters("abc", 5));
    }

    @Test
    public void testGetLastGivenNumberOfCharacters_None() throws Exception {

        Assert.assertEquals("", TextShortener.getLastGivenNumberOfCharacters("abc", 0));
    }

    @Test
    public void testGetLastGivenNumberOfCharacters_Number() throws Exception {
        Assert.assertEquals("bc", TextShortener.getLastGivenNumberOfCharacters("abc", 2));
    }

    @Test
    public void testGetLastGivenNumberOfCharacters_NumberLargerThanTextLength() throws Exception {

        Assert.assertEquals("abc", TextShortener.getLastGivenNumberOfCharacters("abc", 5));
    }

}
