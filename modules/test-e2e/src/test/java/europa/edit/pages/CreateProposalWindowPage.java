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

import org.openqa.selenium.By;

public class CreateProposalWindowPage {
    public static final By CREATE_BTN = By.xpath("//*[text()='Create']");
    public static final By PREVIOUS_BTN = By.xpath("//*[@role='button']//span[text()='Previous']");
    public static final By NEXTBTN = By.xpath("//span[text()='Next']");
    public static final By CANCELBTN = By.xpath("//span[text()='Cancel']");
    public static final By DOCUMENT_TITLE_INPUT = By.xpath("//span[text()='Document title:']/ancestor::tr[position() = 1]//input");
    public static final By UPLOAD_BTN_UPLOAD_WINDOW = By.xpath("//*[text()='Upload a legislative document - Upload a leg file (1/2)']//ancestor::div[@class='popupContent']//input[@type='file']");
    public static final By UPLOAD_WINDOW_FIRST_PAGE = By.xpath("//*[text()='Upload a legislative document - Upload a leg file (1/2)']");
    public static final By FILENAME_TXT = By.xpath("//span[text()='File name:']");
    public static final By VALID_ICON = By.xpath("//div[@class='v-label v-widget file-valid v-label-file-valid v-label-undef-w']");
    public static final String INTER_PROCEDURE = "//*[text()='Interinstitutional procedures - Law Initiative (COM/JOIN)']//ancestor::div[@role='treeitem']";
    public static final String XPATH_TEXT_1 = "//*[text()='";
    public static final String XPATH_TEXT_2 = "']";
}
