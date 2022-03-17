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
package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

/**
 * The type Cn council explanatory page.
 */
@UtilityClass
public class CouncilExplanatoryPage {
    /**
     * The constant XPATH_TEXT_1.
     */
    public static final String XPATH_TEXT_1 = "//*[text()='";
    /**
     * The constant XPATH_TEXT_2.
     */
    public static final String XPATH_TEXT_2 = "']";
    /**
     * The constant CLOSE_BUTTON.
     */
    public static final By CLOSE_BUTTON = By.xpath("//*[text()='Close']");
}

