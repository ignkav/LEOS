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

import europa.edit.pages.CommonPage;
import europa.edit.pages.CreateProposalWindowPage;
import europa.edit.pages.ProposalViewerPage;
import europa.edit.pages.RepositoryBrowserPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.Common;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.util.List;


public class RepositoryBrowserPageSteps extends BaseDriver {

    @And("^wait for created mandate to show in the Repository Browser Page$")
    public void waitForCreatedMandateInRepositoryBrowserPage() {

    }

    @And("^proposal/mandate list is displayed$")
    public void proposalMandateListIsDisplayed() {
        Common.verifyElement(driver, RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_FIRST_TR);
    }

    @When("search {string} in Repository Browser Search Bar")
    public void searchProposal(String arg0) {
        Common.elementEcasSendkeys(driver, RepositoryBrowserPage.SEARCHBAR, arg0);
    }

    @And("^delete all the mandate containing keyword$")
    public void deleteAllTheMandateMandateContainingKeyword(DataTable dataTable) {
        List<String> details = dataTable.asList(String.class);
        for (String keyword : details) {
            while (findNumberOfRowsRepoPage(keyword)) {
                Common.elementClick(driver, RepositoryBrowserPage.OPEN_BTN_1STPROPOSAL);
                Common.elementClick(driver, ProposalViewerPage.DELETE_BTN);
                Common.verifyElement(driver, ProposalViewerPage.PROPOSAL_DELETION_CONFIRMATION_POPUP);
                Common.verifyElement(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
                Common.verifyElementIsEnabled(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
                Common.elementClick(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
                Common.verifyElement(driver, RepositoryBrowserPage.REPOSITORY_BROWSER_TEXT);
                E2eUtil.wait(20000);
            }
        }
    }

    @When("search keyword {string} in the search bar of repository browser page")
    public void searchKeywordInTheSearchBarOfRepositoryBrowserPage(String arg0) {
        try {
            boolean bool = findNumberOfRowsRepoPage(arg0);
            if (!bool) {
                Assert.fail("No results found with keyword : " + arg0);
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("^navigate to Repository Browser page$")
    public void NavigateToRepositoryBrowserPage() {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            Common.verifyElement(driver, RepositoryBrowserPage.REPOSITORY_BROWSER_TEXT);
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @And("^filter section is present$")
    public void VerifyFilterSectionPresent() {
        Common.verifyElement(driver, RepositoryBrowserPage.FILTER_SECTION);
    }

    @And("^search bar is present$")
    public void VerifySearchBarPresent() {
        Common.verifyElement(driver, RepositoryBrowserPage.SEARCHBAR);
    }

    @And("^upload button is present$")
    public void VerifyUploadBtnPresent() {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            Common.verifyElement(driver, RepositoryBrowserPage.UPLOAD_BUTTON);
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @And("^create proposal button is displayed and enabled$")
    public void IsCreateProposalBtnPresent() {
        Common.verifyElement(driver, RepositoryBrowserPage.CREATE_PROPOSAL_BUTTON);
        Common.verifyElementIsEnabled(driver, RepositoryBrowserPage.CREATE_PROPOSAL_BUTTON);
    }

    @When("^click on create proposal button$")
    public void clickCreateProposalBtn() {
        Common.elementClick(driver, RepositoryBrowserPage.CREATE_PROPOSAL_BUTTON);
    }

    @When("^untick \"([^\"]*)\" in act category under filter section$")
    public void untickElement(String var1) {
        Common.elementClick(driver, By.xpath(CreateProposalWindowPage.XPATH_TEXT_1 + var1 + CreateProposalWindowPage.XPATH_TEXT_2 + RepositoryBrowserPage.PRECEDING_SIBLING_INPUT));
    }

    @Then("^\"([^\"]*)\" in act category is unticked$")
    public void verifyElementIsUntickedOrNot(String var1) {
        Common.verifyElement(driver, By.xpath(CreateProposalWindowPage.XPATH_TEXT_1 + var1 + CreateProposalWindowPage.XPATH_TEXT_2 + RepositoryBrowserPage.PRECEDING_SIBLING_INPUT + RepositoryBrowserPage.CHECKBOX_NOT_CHECKED));
    }

    @When("^click on reset button$")
    public void clickResetBtn() {
        E2eUtil.scrollandClick(driver, RepositoryBrowserPage.RESET_BTN);
    }

    @Then("^\"([^\"]*)\" is ticked in act category under filter section$")
    public void getDataTicked(String var1) {
        Common.verifyElement(driver, By.xpath(CreateProposalWindowPage.XPATH_TEXT_1 + var1 + CreateProposalWindowPage.XPATH_TEXT_2 + RepositoryBrowserPage.PRECEDING_SIBLING_INPUT + RepositoryBrowserPage.CHECKBOX_CHECKED));
    }

    @And("^created document is showing on the top of the document list$")
    public void createdDocumentIsShowingOnTheTopOfTheDocumentList() {
        Common.verifyStringContainsText(driver, RepositoryBrowserPage.FIRSTPROPOSAL);
    }

    @When("^click on the open button of first proposal/mandate$")
    public void iClickOnOpenButton() {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            Common.scrollTo(driver, RepositoryBrowserPage.OPEN_BTN_1STPROPOSAL);
            Common.elementClick(driver, RepositoryBrowserPage.OPEN_BTN_1STPROPOSAL);
        } else {
            Assert.fail("unable to load the page in the specified time duration");
        }
    }

    @When("^double click on first proposal$")
    public void doubleClickOnProposal() {
        Common.doubleClick(driver, RepositoryBrowserPage.OPEN_BTN_1STPROPOSAL);
    }

    @When("^click on upload button present in the Repository Browser page$")
    public void clickUploadBtnInRepositoryBrowser() {
        Common.elementClick(driver, RepositoryBrowserPage.UPLOAD_BUTTON);
    }

    @And("^upload button is not present in Repository Browser page$")
    public void VerifyUploadBtnPresentIsNotPresent() {
        boolean isElementPresent = Common.verifyElementNotPresent(driver, RepositoryBrowserPage.UPLOAD_BUTTON);
        try {
            Assert.assertTrue(isElementPresent);
            E2eUtil.takeSnapShot(driver, "PASS");
            Reporter.getCurrentTestResult().setStatus(ITestResult.SUCCESS);
        } catch (AssertionError e) {
            E2eUtil.takeSnapShot(driver, "FAIL");
            Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
            e.printStackTrace();
            throw e;
        }
    }

    @And("^delete all the proposal containing keyword$")
    public void deleteAllTheProposalMandateContainingKeyword(DataTable dataTable) {
        List<String> details = dataTable.asList(String.class);
        for (String keyword : details) {
            while (findNumberOfRowsRepoPage(keyword)) {
                Common.elementClick(driver, RepositoryBrowserPage.OPEN_BTN_1STPROPOSAL);
                Common.elementClick(driver, ProposalViewerPage.DELETE_BTN);
                Common.verifyElement(driver, ProposalViewerPage.PROPOSAL_DELETION_CONFIRMATION_POPUP);
                Common.verifyElement(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
                Common.elementClick(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
                WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
                if (null != ele) {
                    Common.verifyElement(driver, RepositoryBrowserPage.REPOSITORY_BROWSER_TEXT);
                } else
                    Assert.fail("unable to load the page in the specified time duration");
            }
        }
    }

    public boolean findNumberOfRowsRepoPage(String str) {
        Common.elementEcasSendkeys(driver, RepositoryBrowserPage.SEARCHBAR, str);
        E2eUtil.wait(2000);
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            try {
                List<WebElement> elements = driver.findElements(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR);
                return null != elements && !elements.isEmpty();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else
            return false;
    }

    @Then("^each proposal/mandate in the search results contain keyword \"([^\"]*)\"$")
    public void searchResultsContainKeywordForEachProposalMandateS(String arg0) {
        boolean bool = Common.waitForElementTobeDisPlayed(driver, By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR + "[1]" + "//div[contains(@class,'leos-card-title') and contains(text(),'" + arg0 + "')]"));
        if (bool) {
            try {
                List<WebElement> elements = driver.findElements(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR);
                String text;
                if (null != elements && !elements.isEmpty()) {
                    for (int i = 1; i <= elements.size(); i++) {
                        text = driver.findElement(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR + "[" + i + "]" + RepositoryBrowserPage.LEOS_CARD_TITLE)).getText();
                        if (!text.contains(arg0)) {
                            Assert.fail("name of this proposal/mandate doesn't contain string " + arg0);
                        }
                    }
                } else
                    Assert.fail("No results found");
            } catch (Exception | AssertionError e) {
                e.printStackTrace();
                throw e;
            }
        }
        else{
            Assert.fail("Proposal name doesnot contain keyword "+arg0);
        }
    }

    @And("first proposal name contains {string}")
    public void firstProposalNameContains(String arg0) {
        String proposalName = Common.getElementText(driver, By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR + "[1]" + RepositoryBrowserPage.LEOS_CARD_TITLE));
        Assert.assertTrue(proposalName.contains(arg0), "first proposal doesn't contain " + arg0);
    }

    @And("colour of first proposal is {string}")
    public void colourOfFirstProposalIsGrey(String arg0) {
        String colour = driver.findElement(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR + "[1]/td" + RepositoryBrowserPage.CLONED_PROPOSAL)).getCssValue("background-color");
        Assert.assertEquals(colour, arg0, "colour of first proposal is not " + arg0);
    }

    @And("first proposal contains keyword Revision status: For revision")
    public void firstProposalContainsKeywordRevisionStatusForRevision() {
        String text1 = driver.findElement(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR + "[1]/td//div[contains(@class,'v-slot-leos-caption') and not(contains(@class,'leos-card-language'))]//span[@class='v-captiontext']")).getText();
        Assert.assertEquals(text1, "Revision status:", "first proposal doesn't contains keyword Revision status:");
        String text2 = driver.findElement(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR + "[1]/td//div[contains(@class,'v-slot-leos-caption') and not(contains(@class,'leos-card-language'))]//div[contains(@class,'v-label-undef-w')]")).getText();
        Assert.assertEquals(text2, "For revision", "first proposal doesn't contains keyword For revision");
    }

    @And("first proposal contains keyword REVISION EdiT")
    public void firstProposalContainsKeywordREVISIONEdiT() {
        String text;
        boolean bool = true;
        List<WebElement> elementList = driver.findElements(By.xpath(RepositoryBrowserPage.PROPOSAL_MANDATE_LIST_TR + "[1]" + RepositoryBrowserPage.V_LABEL_CLONED_LABEL));
        if (elementList.size() > 0) {
            for (WebElement element : elementList) {
                text = element.getText();
                if (!(text.equals("REVISION") || text.equals("EdiT"))) {
                    bool = false;
                }
            }
            Assert.assertTrue(bool, "first proposal has no label REVISION or EdiT");
        } else
            Assert.fail("first proposal has no label REVISION EdiT");
    }
}
