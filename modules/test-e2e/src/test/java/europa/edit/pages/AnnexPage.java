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
public class AnnexPage {
    public static final By PREFACE = By.xpath("//*[text()='Preface']");
    public static final By BODY = By.xpath("//*[text()='Body']");
    public static final By TOC_CANCEL_BUTTON = By.xpath("//img[contains(@src,'toc-cancel.png')]");
    public static final By TOC_SAVE_BUTTON = By.xpath("//img[contains(@src,'toc-save.png')]");
    public static final By TOC_SAVE_AND_CLOSE_BUTTON = By.xpath("//img[contains(@src,'toc-save-close.png')]");
    public static final By SHOW_ALL_ACTIONS = By.xpath("//*[@title='Show all actions' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_INSERT_BEFORE = By.xpath("//*[@data-widget-type='insert.before' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_INSERT_AFTER = By.xpath("//*[@data-widget-type='insert.after' and @style='transform: rotate(180deg); display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_EDIT = By.xpath("//*[@data-widget-type='edit' and @style='display: inline-block;']");
    public static final By SHOW_ALL_ACTIONS_DELETE = By.xpath("//*[@data-widget-type='delete' and @style='display: inline-block;']");
    public static final By CLOSE_BUTTON = By.xpath("//span[text()='Close']");
    public static final By ANNEX_DELETION_BUTTON = By.xpath("//*[text()='Annex deletion: confirmation']//ancestor::div[@class='popupContent']//*[text()='Delete']");


    public static final String SHOW_ALL_ACTIONS_ICON = "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']";
    public static final String TOC_TABLE_TR = "//table[@role='treegrid']//tbody//tr";
    public static final String LEVEL = "//level";
    public static final String AKNP = "//aknp";
    public static final String XPATH_TEXT_1 = "//*[text()='";
    public static final String XPATH_TEXT_2 = "']";
}

