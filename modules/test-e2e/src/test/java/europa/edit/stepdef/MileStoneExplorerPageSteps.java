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

import europa.edit.pages.MileStoneExplorerPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.Common;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.testng.Assert;

public class MileStoneExplorerPageSteps extends BaseDriver {
    @Then("milestone explorer page is displayed")
    public void milestoneExplorerPageIsDisplayed() {
        Common.verifyElement(driver, MileStoneExplorerPage.MILESTONE_EXPLORER_TEXT);
    }

    @And("explanatory memorandum section is displayed")
    public void explanatoryMemorandumSectionIsDisplayed() {
        boolean bool = driver.findElement(MileStoneExplorerPage.EXP_MEMO_TEXT).isDisplayed();
        Assert.assertTrue(bool, "explanatory memorandum section is not displayed");
    }

    @And("legal act section is displayed")
    public void legalActSectionIsDisplayed() {
        boolean bool = driver.findElement(MileStoneExplorerPage.LEGAL_ACT_TEXT).isDisplayed();
        Assert.assertTrue(bool, "legal act section is not displayed");
    }

    @And("click on close button present in milestone explorer page")
    public void clickOnCloseButtonPresentInMilestoneExplorerPage() {
        Common.elementClick(driver, MileStoneExplorerPage.CLOSE_BUTTON);
    }
}
