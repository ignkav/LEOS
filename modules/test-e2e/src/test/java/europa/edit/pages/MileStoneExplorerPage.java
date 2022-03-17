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
public class MileStoneExplorerPage {
    public static final By EXP_MEMO_TEXT = By.xpath("//table[@role='presentation']//div[contains(text(),'Explanatory Memorandum')]");
    public static final By LEGAL_ACT_TEXT = By.xpath("//table[@role='presentation']//div[contains(text(),'Legal Act')]");
    public static final By MILESTONE_EXPLORER_TEXT = By.xpath("//*[text()='Milestone explorer']");
    public static final By CLOSE_BUTTON = By.xpath("//div[@class='v-slot v-slot-window-buttons-area']//*[text()='Close']");
}

