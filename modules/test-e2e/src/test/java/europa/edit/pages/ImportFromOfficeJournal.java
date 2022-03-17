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
public class ImportFromOfficeJournal {
    public static final By SEARCH_BUTTON = By.xpath("//*[text()='Search']//ancestor::div[@role='button']");
    public static final By I_BUTTON = By.xpath("//*[contains(@class,'v-icon-info_circle')]");
    public static final By IMPORT_BUTTON = By.xpath("//*[text()='Import']//ancestor::div[@role='button']");
    public static final By IMPORT_BUTTON_DISABLED = By.xpath("//*[text()='Import']//ancestor::div[@role='button' and @aria-disabled='true']");
    public static final By SELECT_ALL_RECITALS_BUTTON = By.xpath("//*[text()='Select all recitals']//ancestor::div[@role='button']");
    public static final By SELECT_ALL_RECITALS_BUTTON_DISABLED = By.xpath("//*[text()='Select all recitals']//ancestor::div[@role='button' and @aria-disabled='true']");
    public static final By SELECT_ALL_ARTICLES_BUTTON = By.xpath("//*[text()='Select all articles']//ancestor::div[@role='button']");
    public static final By SELECT_ALL_ARTICLES_BUTTON_DISABLED = By.xpath("//*[text()='Select all articles']//ancestor::div[@role='button' and @aria-disabled='true']");
    public static final By CLOSE_BUTTON = By.xpath("//*[text()='Close']//ancestor::div[@role='button']");
    public static final By I_MOUSE_HOVER_TEXT = By.xpath("//*[@class='v-tooltip-text']");
    public static final By NR_INPUT = By.xpath("//*[text()='Nr.']//ancestor::div[contains(@class,'v-caption-on-top')]//input");
    public static final By ERROR_INDICATOR = By.xpath("//*[contains(@class,'v-errorindicator-error')]");
    public static final By NR_INPUT_ERROR_INDICATOR = By.xpath("//input[contains(@class,'v-textfield-error-error')]");
    public static final By TYPE_SELECT_CLASS = By.xpath("//*[text()='Type']//ancestor::div[contains(@class,'v-caption-on-top')]//select");
    public static final By TYPE_SELECT_CLASS_OPTION = By.xpath("//*[text()='Type']//ancestor::div[contains(@class,'v-caption-on-top')]//select/option");
    public static final By YEAR_SELECT_CLASS = By.xpath("//*[text()='Year']//ancestor::div[contains(@class,'v-caption-on-top')]//select");

    public static final String BILL = "//*[@id='akomaNtoso']/bill";
    public static final String LEOS_IMPORT_WRAPPER = "//div[@class='leos-import-wrapper']";
    public static final String RECITALS = "//recitals";
    public static final String AKNBODY = "//aknbody";





}
