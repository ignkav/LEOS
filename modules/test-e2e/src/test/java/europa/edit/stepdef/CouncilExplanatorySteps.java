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
package europa.edit.stepdef;

import europa.edit.pages.CouncilExplanatoryPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.Common;
import europa.edit.util.E2eUtil;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;

public class CouncilExplanatorySteps extends BaseDriver {

    @Then("{string} council explanatory page is displayed")
    public void councilExplanatoryPageIsDisplayed(String arg0) {
        E2eUtil.wait(5000);
        if (arg0.equals("COUNCIL EXPLANATORY")) {
            Common.verifyElement(driver, By.xpath(CouncilExplanatoryPage.XPATH_TEXT_1 + "Explanatory" + CouncilExplanatoryPage.XPATH_TEXT_2));
        } else {
            Common.verifyElement(driver, By.xpath(CouncilExplanatoryPage.XPATH_TEXT_1 + arg0 + CouncilExplanatoryPage.XPATH_TEXT_2));
        }
    }

    @When("click on close button in Council Explanatory page")
    public void clickOnCloseButtonInCouncilExplanatoryPage() {
        Common.elementClick(driver, CouncilExplanatoryPage.CLOSE_BUTTON);
    }
}
