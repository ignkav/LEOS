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
public class CommonPage {
    public static final By MIN_MAX_APP_HEADER_ICON = By.xpath("//*[@class='resize-button']//span[@class='v-icon Vaadin-Icons']");
    public static final By SUBTITLE_HEADER_ELEMENT = By.xpath("//*[@class='ec-header']/parent::div[contains(@style,'border-style: none; margin: 0px; padding: 0px; width: 100%; height: 48px;')]//div[@class='sub-title']");
    public static final By TITLE_HEADER_ELEMENT = By.xpath("//*[@class='ec-header']/parent::div[contains(@style,'border-style: none; margin: 0px; padding: 0px; width: 100%; height: 123px;')]//div[@class='title']");
    public static final By LOGOUT_BUTTON = By.xpath("//*[@class='logout-button']//*[@role='button']");
    public static final By LOADING_PROGRESS = By.xpath("//*[contains(@class,'v-loading-indicator') and @style='position: absolute; display: none;']");

    public static final String XPATH_TEXT_1 = "//*[text()='";
    public static final String XPATH_TEXT_2 = "']";
}
