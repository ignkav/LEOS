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

@UtilityClass
public class ExpMemoPage {
    public static final By EXP_MEMO_TEXT = By.xpath("//span[text()='Explanatory Memorandum']");
    public static final By TOC_EDIT_BUTON = By.xpath("//img[contains(@src,'toc-edit.png')]");
    public static final By ENABLE_ANNOTATION_POPUP = By.xpath("//*[@title=\"Enable Annotations' Popup\"]");
    public static final By CLOSE_BUTON = By.xpath("//*[text()='Close']");
    public static final By NAVIGATION_PANE = By.xpath("//*[text()='Navigation pane']");
    public static final By EXP_MEMO_CONTENT = By.xpath("//*[@id='docContainer']");
    public static final String XPATH_TEXT_1 = "//*[text()='";
    public static final String XPATH_TEXT_2 = "']";
}

