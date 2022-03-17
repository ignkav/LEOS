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
package eu.europa.ec.leos.test.support;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.mockito.MockitoAnnotations;

public class LeosTest {

    protected static final Charset UTF_8 = Charset.forName("UTF-8");

    @Rule
    public Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
}
