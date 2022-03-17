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
public class RepositoryBrowserPage {
    public static final By OPEN_BTN_1STPROPOSAL = By.xpath("//*[text()='Open'][1]");
    public static final By REPOSITORY_BROWSER_TEXT = By.xpath("//*[text()='Repository Browser']");
    public static final By SEARCHBAR = By.xpath("//input[@placeholder='Search by title']");
    public static final By RESET_BTN = By.xpath("//span[text()='Reset']");
    public static final By PROPOSAL_MANDATE_LIST_FIRST_TR = By.xpath("//table[@role='grid']/tbody/tr[1]");
    public static final By PROPOSAL_MANDATE_LIST_TR = By.xpath("//table[@role='grid']/tbody/tr");
    public static final By CREATE_PROPOSAL_BUTTON = By.xpath("//*[text()='Create proposal']");
    public static final By FILTER_SECTION = By.xpath("//*[text()='FILTERS']");
    public static final By UPLOAD_BUTTON = By.xpath("//*[text()='Upload']//ancestor::div[@role='button']");
    public static final By USERNAME_ICON = By.xpath("//*[@location='user']//*[@class='v-label v-widget v-label-undef-w']");
    public static final By FIRSTPROPOSAL = By.xpath("//table[@role='grid']/tbody/tr[1]/td//div[@class='leos-card-title v-widget v-has-width']");

    public static final String LEOS_CARD_TITLE = "//div[contains(@class,'leos-card-title')]";
    public static final String PRECEDING_SIBLING_INPUT = "/preceding-sibling::input";
    public static final String CHECKBOX_NOT_CHECKED = "[@type='checkbox' and not(@checked)]";
    public static final String CHECKBOX_CHECKED = "[@type='checkbox' and (@checked)]";
    public static final String CLONED_PROPOSAL = "//div[contains(@class,'cloned-proposal')]";
    public static final String V_LABEL_CLONED_LABEL = "//*[contains(@class,'v-label-cloned-labels')]";
    public static final String XPATH_TEXT_1 = "//*[text()='";
    public static final String XPATH_TEXT_2 = "']";
}
