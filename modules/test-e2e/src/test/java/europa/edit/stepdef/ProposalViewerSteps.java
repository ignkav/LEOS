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

import europa.edit.pages.*;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ProposalViewerSteps extends BaseDriver {

    @When("^click on add button in milestones section$")
    public void clickOnAddButtonInMileStonesSection() {
        try {
            WebElement element = driver.findElement(ProposalViewerPage.MILESTONE_ADD_ICON);
            E2eUtil.scrollElement(driver, element);
            Common.elementClick(driver, ProposalViewerPage.MILESTONE_ADD_ICON);
        } catch (AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("{string} option is selected by default")
    public void optionIsSelectedByDefault(String arg0) {
        String text = Common.getElementText(driver, ProposalViewerPage.MILESTONE_OPTIONS_SELECTED);
        try {
            Assert.assertEquals(text, arg0, arg0 + " is not selected by default");
        } catch (AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("^milestone title textbox is disabled$")
    public void titleTextboxIsDisabled() {
        try {
            boolean bool = driver.findElement(ProposalViewerPage.MILESTONE_TITLE_TEXTAREA).isEnabled();
            if (bool) {
                Assert.fail("Element is enabled but should be disabled");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("^click on milestone dropdown icon$")
    public void clickOnOptionInWindow() {
        Common.elementClick(driver, ProposalViewerPage.MILESTONE_DROPDOWN_ICON);
    }

    @And("^There are two options displayed for MileStone$")
    public void thereAreTwoOptionsDisplayedForMileStone() {
    }

    @When("^click on milestone option as Other$")
    public void clickOptionOther() {
        Common.elementClick(driver, ProposalViewerPage.MILESTONE_OPTION_OTHER);
    }

    @And("type {string} in title box")
    public void typeInTitleBox(String arg0) {
        boolean bool=Common.waitForElementTobeDisPlayed(driver,ProposalViewerPage.MILESTONE_TITLE_TEXTAREA);
        if(bool){
            Common.elementEcasSendkeys(driver, ProposalViewerPage.MILESTONE_TITLE_TEXTAREA, arg0);
        }
        else
            Assert.fail("title box is not displayed");
    }

    @When("^click on title of the mandate$")
    public void clickOnTitleOfTheMandate() {
        Common.elementClick(driver, ProposalViewerPage.TITLE_ELEMENT);
    }

    @Then("^title save button is displayed and enabled$")
    public void tileSaveButtonIsDisplayedAndEnabled() {
        Common.verifyElement(driver, By.xpath(ProposalViewerPage.TITLE_SAVE_BTN));
    }

    @And("^title cancel button is displayed and enabled$")
    public void titleCancelButtonIsDisplayedAndEnabled() {
        Common.verifyElement(driver, By.xpath(ProposalViewerPage.TITLE_CANCEL_BTN));
    }

    @When("^append \"([^\"]*)\" keyword in the title of the proposal/mandate$")
    public void addKeywordInTheTitleOfTheMandate(String arg0) {
        try {
            String text = Common.getElementAttributeValue(driver, ProposalViewerPage.TITLE_ELEMENT);
            Common.elementEcasSendkeys(driver, ProposalViewerPage.TITLE_ELEMENT, text.concat(arg0));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("^click on title save button$")
    public void clickOnSaveButton() {
        Common.elementClick(driver, By.xpath(ProposalViewerPage.TITLE_SAVE_BTN));
    }

    @Then("^title of the proposal/mandate contains \"([^\"]*)\" keyword$")
    public void titleOfTheMandateContainsKeyword(String arg0) {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            try {
                String text = Common.getElementAttributeValue(driver, ProposalViewerPage.TITLE_ELEMENT);
                if (!text.contains(arg0)) {
                    Assert.fail(arg0 + " is not present in the title of the proposal/mandate");
                }
            } catch (Exception | AssertionError e) {
                e.printStackTrace();
                throw e;
            }
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @Then("these are below options displayed for milestone dropdown")
    public void verifyMileStoneOptions(DataTable mileStoneOptions) {
        List<String> details = mileStoneOptions.asList(String.class);
        try {
            List<WebElement> elements = driver.findElements(By.xpath(ProposalViewerPage.MILESTONE_DROPDOWN_LIST_TR));
            if (null != elements && !elements.isEmpty()) {
                for (int i = 1; i <= elements.size(); i++) {
                    String text = driver.findElement(By.xpath(ProposalViewerPage.MILESTONE_DROPDOWN_LIST_TR + "[" + i + "]" + ProposalViewerPage.MILESTONE_DROPDOWN_LIST_TD_SPAN)).getText();
                    if (!details.contains(text)) {
                        Assert.fail(text + " is not present in the data provided");
                    }
                }
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on open button of legal act")
    public void clickOnOpenButtonOfLegalAct() {
        E2eUtil.scrollandClick(driver, ProposalViewerPage.LEGAL_ACT_OPEN_BUTTON);
    }

    @And("{string} section is displayed")
    public void sectionIsDisplayed(String arg0) {
        Common.verifyElement(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg0 + ProposalViewerPage.XPATH_TEXT_2));
    }

    @And("add new explanatory button is displayed and enabled")
    public void addNewExplantoryButtonIsDisplayedAndEnabled() {
        Common.verifyElement(driver, ProposalViewerPage.ADD_NEW_EXPLANATORY_BUTTON);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.ADD_NEW_EXPLANATORY_BUTTON);
    }

    @And("there are two default explanatories present in council explantory")
    public void thereAreTwoDefaultExplanatoriesPresentInCouncilExplantory() {
        try {
            List<WebElement> element = driver.findElements(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR);
            if (element.size() != 2) {
                Assert.fail("Two default explanatory is not present in Council Explantory");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("title of both default explanatories are")
    public void titleOfBothDefaultExplantoriesAre(DataTable dataTable) {
        try {
            List<String> titleDetails = dataTable.asList(String.class);
            List<WebElement> elements = driver.findElements(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR);
            if (null != elements) {
                for (int i = 1; i <= elements.size(); i++) {
                    String titleText = driver.findElement(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + i + "]" + "//input")).getAttribute("value");
                    System.out.println("titleText " + titleText);
                    if (!titleDetails.contains(titleText)) {
                        try {
                            Assert.fail("Title " + titleText + " is not present in Council Explanatory List");
                            break;
                        } catch (AssertionError e) {
                            e.printStackTrace();
                            throw e;
                        }
                    }
                }
            } else {
                try {
                    Assert.fail("No row found in the Council Explanatory List");
                } catch (AssertionError e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("there are below columns displayed under council explanatory section")
    public void thereAreBelowColumnsDisplayedUnderCouncilExplantorySection(DataTable dataTable) {
        List<String> headerDetails = dataTable.asList(String.class);
        List<WebElement> elements = driver.findElements(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_THEAD_TR_TH));
        if (null != elements) {
            for (int i = 1; i <= 4; i++) {
                String headerText = driver.findElement(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_THEAD_TR_TH + "[" + i + "]" + "//div[1]")).getText();
                System.out.println("headerText " + headerText);
                if (!headerDetails.contains(headerText)) {
                    try {
                        Assert.fail(headerText + " is not present in the header of Council Explanatory List");
                        break;
                    } catch (AssertionError e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            }
        } else {
            try {
                Assert.fail("No header found in the Council Explanatory List");
            } catch (AssertionError e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    @And("delete button is disabled for both default explanatories")
    public void deleteButtonIsDisabledForBothDefaultExplanatories() {
        try {
            List<WebElement> elements = driver.findElements(ProposalViewerPage.DEFAULT_EXPLANATORY_DELETE_BUTTON_DISABLED);
            if (null != elements) {
                if (elements.size() != 2) {
                    try {
                        Assert.fail("delete button is not disabled for both default explanatories");
                    } catch (AssertionError e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            } else {
                try {
                    Assert.fail("No row found in the Council Explanatory List");
                } catch (AssertionError e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on open button of {string} explanatory")
    public void clickOnOpenButtonOfExplanatory(String arg0) {
        try {
            E2eUtil.scrollandClick(driver, By.xpath("//*[@id='" + arg0 + "']" + ProposalViewerPage.FONTAWESOME));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on add new explanatory button")
    public void clickOnAddNewExplantoryButton() {
        Common.elementClick(driver, ProposalViewerPage.ADD_NEW_EXPLANATORY_BUTTON);
    }

    @Then("new council explanatory is added to council explanatory section")
    public void newCouncilExplanatoryIsAddedToCouncilExplanatorySection() {
        E2eUtil.wait(20000);
        try {
            List<WebElement> element = driver.findElements(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR);
            if (element.size() <= 2) {
                Assert.fail("new council explanatory is not added to Council Explantory Section");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("delete button is enabled for council explanatory {int}")
    public void deleteButtonIsEnabledForNewCouncilExplanatory(int arg0) {
        try {
            List<WebElement> elements = driver.findElements(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR);
            if (null != elements) {
                WebElement element = driver.findElement(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.DELETE_BUTTON_NOT_DISABLED));
                if (null == element || !element.isEnabled()) {
                    try {
                        Assert.fail("delete button is not enabled for New Council Explantory");
                    } catch (AssertionError e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            } else {
                try {
                    Assert.fail("No row found in the Council Explanatory List");
                } catch (AssertionError e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on delete button of council explanatory {int}")
    public void clickOnDeleteButtonOfNewExplanatoryWithTitle(int arg0) {
        E2eUtil.scrollandClick(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.DELETE_BUTTON_NOT_DISABLED));
    }

    @Then("{string} pop up should be displayed with cancel and delete button enabled")
    public void popUpShouldBeDisplayedWithCancelAndDeleteButtonEnabled(String arg0) {
        Common.verifyElement(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg0 + ProposalViewerPage.XPATH_TEXT_2));
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.COUNCIL_EXPLANATORY_CONFIRM_PAGE_CANCEL_BUTTON);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.COUNCIL_EXPLANATORY_CONFIRM_PAGE_DELETE_BUTTON);
    }

    @And("messages {string} and {string} are displayed in explanatory deletion : confirmation pop up window")
    public void messagesAndAreDisplayedInPopUpWindow(String arg0, String arg1) {
        Common.verifyElement(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg0 + ProposalViewerPage.XPATH_TEXT_2));
        Common.verifyElement(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg1 + ProposalViewerPage.XPATH_TEXT_2));
    }

    @When("click on delete button in Explanatory deletion : confirmation pop up")
    public void clickOnDeleteButtonInPopUp() {
        Common.elementClick(driver, ProposalViewerPage.COUNCIL_EXPLANATORY_CONFIRM_PAGE_DELETE_BUTTON);
    }

    @Then("non default explanatory is removed from council explanatory")
    public void newExplantoryAddedManuallyToCouncilExplanatoryIsRemoved() {
        E2eUtil.wait(15000);
        try {
            List<WebElement> element = driver.findElements(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR);
            if (element.size() > 2) {
                Assert.fail("non default explanatory is not removed from Council Explanatory");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on close button present in proposal viewer page")
    public void click_on_close_button_present_in_proposal_viewer_page() {
        Common.elementClick(driver, ProposalViewerPage.CLOSE_BUTTON);
    }

    @And("{string} is added as {string} in collaborators section")
    public void isAddedAsInCollaboratorsSection(String arg0, String arg1) {
        try {
            List<WebElement> elements = driver.findElements(By.xpath(ProposalViewerPage.COLLABORATORS_TABLE_TBODY_TR));
            if (elements.size() > 0) {
                String name = driver.findElement(By.xpath(ProposalViewerPage.COLLABORATORS_TABLE_TBODY_TR + "[1]/td[1]")).getText();
                Assert.assertEquals(name, arg0, "Name is not " + arg0);
                String role = driver.findElement(By.xpath(ProposalViewerPage.COLLABORATORS_TABLE_TBODY_TR + "[1]/td[3]//input")).getAttribute("value");
                Assert.assertEquals(role, arg1, "Role is not " + arg1);
            } else
                Assert.fail("No rows found in collaborators section");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("no milestone exists in milestones section")
    public void noMilestoneExistsInMilestonesSection() {
        Common.verifyElementNotPresent(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR));
    }

    @And("{string} is showing in title column of milestones table")
    public void isShowingInTitleColumnOfMilestonesTable(String arg0) {
        Common.scrollTo(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[1]" + ProposalViewerPage.ROLE_BUTTON + ProposalViewerPage.V_BUTTON_CAPTION));
        String title = Common.getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[1]" + ProposalViewerPage.ROLE_BUTTON + ProposalViewerPage.V_BUTTON_CAPTION));
        Assert.assertEquals(title, arg0, "title is not equal to " + arg0);
    }

    @And("today's date is showing in date column of milestones table")
    public void todaySDateIsShowingInDateColumnOfMilestonesTable() {
        String pattern = "dd/MM/yyyy";
        String date = Common.getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[2]"));
        if (null != date) {
            String subStringDate = date.substring(0, date.length() - 6);
            String dateInString = new SimpleDateFormat(pattern).format(new Date());
            Assert.assertEquals(subStringDate, dateInString, "date mentioned is not today's date");
        } else {
            Assert.fail("unable to retrieve date in string format");
        }
    }

    @And("{string} is showing in status column of milestones table")
    public void isShowingInStatusColumnOfMilestonesTable(String arg0) {
        boolean bool=Common.waitForElementTobeDisPlayed(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[3]"));
        if(bool){
            String status = Common.getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[3]"));
            Assert.assertEquals(status, arg0, "status is not equal to " + arg0);
        }
        else{
            Assert.fail("status column of milestones table is not "+arg0+" with in maximum time provided");
        }
    }

    @When("click on the link in title column of the first milestone")
    public void clickOnTheLinkInTitleColumnOfTheFirstMilestone() {
        Common.elementClick(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[1]/td[1]"+ ProposalViewerPage.ROLE_BUTTON));
    }

    @Then("Add a milestone window is displayed")
    public void addAMilestoneWindowIsDisplayed() {
        Common.verifyElement(driver, ProposalViewerPage.ADD_A_MILESTONE_TEXT);
    }

    @When("click on add collaborator button")
    public void clickOnAddCollaboratorButton() {
        Common.elementClick(driver, ProposalViewerPage.COLLABORATORS_ADD_BUTTON);
    }

    @Then("collaborator save button is displayed")
    public void collaboratorSaveButtonIsDisplayed() {
        boolean bool = Common.verifyElement(driver, ProposalViewerPage.COLLABORATORS_SAVE_BUTTON);
        Assert.assertTrue(bool, "collaborator save button is not displayed");
    }

    @And("collaborator cancel button is displayed")
    public void collaboratorCancelButtonIsDisplayed() {
        boolean bool = Common.verifyElement(driver, ProposalViewerPage.COLLABORATORS_CANCEL_BUTTON);
        Assert.assertTrue(bool, "collaborator cancel button is not displayed");
    }

    @And("search input box is enabled for name column in Collaborator section")
    public void searchInputBoxIsEnabledForNameColumnInCollaboratorSection() {
        boolean bool = Common.verifyElement(driver, ProposalViewerPage.COLLABORATORS_NAME_1ST_INPUT_BOX);
        Assert.assertTrue(bool, "search input box is not displayed for name column in Collaborator section");
    }

    @When("search {string} in the name input field")
    public void searchInTheNameInputField(String arg0) {
        Common.elementEcasSendkeys(driver, ProposalViewerPage.COLLABORATORS_NAME_1ST_INPUT_BOX, arg0);
        E2eUtil.wait(3000);
    }

    @Then("{string} user is showing in the list")
    public void stringUserIsShowingInTheList(String arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(ProposalViewerPage.COLLABORATORS_SEARCH_RESULTS_TR));
        String name;
        String[] nameList = new String[3];
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                name = driver.findElement(By.xpath(ProposalViewerPage.COLLABORATORS_SEARCH_RESULTS_TR + "[" + i + "]/td/span")).getText();
                nameList = name.split(" ");
            }
            boolean result = Arrays.asList(nameList).contains(arg0);
            if (!result) {
                Assert.fail(arg0 + " is not present in the search results");
            }
        } else {
            Assert.fail("No lists found in the search results");
        }
    }

    @When("click on first user showing in the list")
    public void clickOnUser() {
        Common.elementClick(driver, By.xpath(ProposalViewerPage.COLLABORATORS_SEARCH_RESULTS_TR + "[1]/td/span"));
    }

    @Then("{string} user is selected in the name input field")
    public void userIsSelectedInTheNameInputField(String arg0) {
        String text = Common.getElementAttributeValue(driver, ProposalViewerPage.COLLABORATORS_NAME_1ST_INPUT_BOX);
        Assert.assertTrue(text.contains(arg0), arg0 + " user is not selected in the name input field");
    }

    @When("click on save button in Collaborator section")
    public void clickOnSaveButtonInCollaboratorSection() {
        Common.elementClick(driver, ProposalViewerPage.COLLABORATORS_SAVE_BUTTON);
        E2eUtil.wait(5000);
    }

    @Then("{string} user is showing in the collaborator list")
    public void userIsShowingInTheCollaboratorList(String arg0) {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            List<WebElement> elementList = driver.findElements(By.xpath(ProposalViewerPage.COLLABORATORS_TABLE_TBODY_TR));
            String name;
            boolean bool = false;
            if (elementList.size() > 0) {
                for (int i = 1; i <= elementList.size(); i++) {
                    name = driver.findElement(By.xpath(ProposalViewerPage.COLLABORATORS_TABLE_TBODY_TR + "[" + i + "]/td[1]")).getText();
                    if (name.contains(arg0)) {
                        bool = true;
                        break;
                    }
                }
                if (!bool)
                    Assert.fail(arg0 + " user is not showing in the collaborator list");
            } else {
                Assert.fail("No rows present in the collaborator table");
            }
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @Then("^mandate deletion confirmation page should be displayed$")
    public void mandateDeletionConfirmationPageShouldBeDisplayed() {
        Common.verifyElement(driver, ProposalViewerPage.MANDATE_DELETION_CONFIRMATION_POPUP);
    }

    @When("click on add a new annex button")
    public void clickOnAddANewAnnexButton() {
        E2eUtil.scrollandClick(driver, ProposalViewerPage.ANNEXES_ADD_BUTTON);
    }

    @Then("{string} is added to Annexes")
    public void isAddedToAnnexes(String arg0) {
        WebElement ele=Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if(null!=ele){
            boolean bool = Common.verifyElement(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg0 + ProposalViewerPage.XPATH_TEXT_2));
            Assert.assertTrue(bool, arg0 + " is not added to annexes");
        }
        else{
            Assert.fail("unable to load the page in the specified time duration");
        }
    }

    @When("click on open button of Annex {int}")
    public void clickOnOpenButtonOfAnnex(int arg0) {
        E2eUtil.scrollandClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]"+ ProposalViewerPage.OPEN_TEXT));
    }

    @When("click on delete button of annex {int}")
    public void clickOnDeleteButtonOfAnnex(int arg0) {
        E2eUtil.scrollandClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]"+ ProposalViewerPage.ICON_ONLY_DELETE_BUTTON));
    }

    @When("click on delete button in annex deletion confirmation page")
    public void clickOnDeleteButtonInAnnexDeletionConfirmationPage() {
        Common.elementClick(driver, AnnexPage.ANNEX_DELETION_BUTTON);
    }

    @Then("{string} is changed to {string}")
    public void isChangedTo(String arg0, String arg1) {
        boolean bool = Common.verifyElement(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg1 + ProposalViewerPage.XPATH_TEXT_2));
        Assert.assertTrue(bool, arg1 + " is not displayed");
        boolean bool1 = Common.verifyElementNotPresent(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg0 + ProposalViewerPage.XPATH_TEXT_2));
        Assert.assertTrue(bool1, arg0 + " is displayed");
    }

    @When("click on title of the Annex {int}")
    public void clickOnTitleOfTheAnnex(int arg0) {
        Common.elementClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.ANNEX_TITLE_INPUT));
    }

    @Then("title save button of Annex {int} is displayed and enabled")
    public void titleSaveButtonOfAnnexIsDisplayedAndEnabled(int arg0) {
        boolean bool = Common.verifyElement(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.TITLE_SAVE_BTN));
        Assert.assertTrue(bool, "title save button of Annex " + arg0 + " is not displayed");
    }

    @And("title cancel button of Annex {int} is displayed and enabled")
    public void titleCancelButtonOfAnnexIsDisplayedAndEnabled(int arg0) {
        boolean bool = Common.verifyElement(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.TITLE_CANCEL_BTN));
        Assert.assertTrue(bool, "title cancel button of Annex " + arg0 + " is not displayed");
    }

    @When("add title {string} to Annex {int}")
    public void addTitleToAnnex(String arg0, int arg1) {
        Common.elementEcasSendkeys(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg1 + "]" + ProposalViewerPage.ANNEX_TITLE_INPUT), arg0);
    }

    @And("click on title save button of Annex {int}")
    public void clickOnTitleSaveButtonOfAnnex(int arg0) {
        Common.elementClick(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.TITLE_SAVE_BTN));
    }

    @Then("title of Annex {int} contains {string}")
    public void titleOfAnnexContains(int arg0, String arg1) {
        try {
            String text = Common.getElementAttributeValue(driver, By.xpath(ProposalViewerPage.ANNEX_BLOCK + "[" + arg0 + "]" + ProposalViewerPage.ANNEX_TITLE_INPUT));
            Assert.assertTrue(text.contains(arg1), "title of Annex " + arg0 + " doesn't contain " + arg1);
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("numbers of annex present in proposal viewer screen is {int}")
    public void numbersOfAnnexPresentInProposalViewerScreenIs(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(ProposalViewerPage.ANNEX_BLOCK));
        Assert.assertEquals(elementList.size(), arg0, "numbers of annex present in proposal viewer screen is not " + arg0);
    }

    @And("placeholder value of council explanatory {int} is {string}")
    public void placeholderValueOfNewCouncilExplanatoryIs(int arg0, String arg1) {
        String placeHolderValue = Common.getValueFromElementAttribute(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]"+ ProposalViewerPage.INPUT), "placeholder");
        Assert.assertEquals(placeHolderValue, arg1, "placeholder value of new council explanatory is not " + arg1);
    }

    @When("Add title {string} to council explanatory {int}")
    public void addTitleToCouncilExplanatory(String arg0, int arg1) {
        Common.setValueToElementAttribute(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg1 + "]"+ ProposalViewerPage.INPUT), arg0);
    }

    @And("click on save button for council explanatory {int}")
    public void clickOnSaveButtonForCouncilExplanatory(int arg0) {
        Common.elementClick(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]"+ ProposalViewerPage.SAVE_BUTTON));
    }

    @And("title of council explanatory {int} is {string}")
    public void titleOfCouncilExplanatoryIs(int arg0, String arg1) {
        String newTitleText = driver.findElement(By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.INPUT)).getAttribute("value");
        Assert.assertEquals(newTitleText, arg1, "Title of new council explanatory is not " + arg1);
    }

    @When("click on title input element of council explanatory {int}")
    public void clickOnTitleInputElementOfCouncilExplanatory(int arg0) {
        Common.elementClick(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]" + ProposalViewerPage.INPUT));
    }

    @Then("save button is displayed in title input element of council explanatory {int}")
    public void saveButtonIsDisplayedInTitleInputElementOfCouncilExplanatory(int arg0) {
        boolean bool = Common.verifyElement(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]"+ ProposalViewerPage.SAVE_BUTTON));
        Assert.assertTrue(bool, "save button is displayed in title input element of council explanatory " + arg0);
    }

    @And("cancel button is displayed in title input element of council explanatory {int}")
    public void cancelButtonIsDisplayedInTitleInputElementOfCouncilExplanatory(int arg0) {
        boolean bool = Common.verifyElement(driver, By.xpath(ProposalViewerPage.COUNCIL_EXPLANATORY_TABLE_TBODY_TR_STRING + "[" + arg0 + "]"+ ProposalViewerPage.CANCEL_BUTTON));
        Assert.assertTrue(bool, "cancel button is displayed in title input element of council explanatory " + arg0);
    }

    @Then("{string} is showing under title column row {int} in Export to eConsilium section")
    public void isShowingUnderTitleColumnRowInExportToEConsiliumSection(String arg0, int arg1) {
        String text = Common.getElementAttributeValue(driver, By.xpath(ProposalViewerPage.ECONSILIUM_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]//textarea"));
        Assert.assertEquals(text, arg0, arg0 + " is showing under title column row " + arg1 + " in Export to eConsilium section");
    }

    @And("today's date is showing under date column row {int} in Export to eConsilium section")
    public void todaySDateIsShowingUnderDateColumnRowInExportToEConsiliumSection(int arg0) {
        String pattern = "dd/MM/yyyy";
        String date = Common.getElementText(driver, By.xpath(ProposalViewerPage.ECONSILIUM_TABLE_TBODY_TR + "[" + arg0 + "]/td[2]"));
        if (null != date) {
            String subStringDate = date.substring(0, date.length() - 6);
            String dateInString = new SimpleDateFormat(pattern).format(new Date());
            Assert.assertEquals(subStringDate, dateInString, "date mentioned is not today's date");
        } else {
            Assert.fail("unable to retrieve date in string format");
        }
    }

    @And("{string} is showing under status column row {int} in Export to eConsilium section")
    public void isShowingUnderStatusColumnRowInExportToEConsiliumSection(String arg0, int arg1) {
        String status = Common.getElementText(driver, By.xpath(ProposalViewerPage.ECONSILIUM_TABLE_TBODY_TR + "[" + arg1 + "]/td[3]"));
        Assert.assertEquals(status, arg0, "status is not equal to " + arg0);
    }

    @Then("^Proposal Viewer screen is displayed$")
    public void proposalViewerScreenIsDisplayed() {
        Common.verifyElement(driver, ProposalViewerPage.PROPOSALVIEWERTEXT);
    }

    @And("^export button is displayed and enabled$")
    public void exportButtonIsDisplayedAndEnabled() {
        Common.verifyElement(driver, ProposalViewerPage.EXPORT_BTN);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.EXPORT_BTN);
    }

    @And("^download button is displayed and enabled$")
    public void downloadButtonIsDisplayedAndEnabled() {
        Common.verifyElement(driver, ProposalViewerPage.DOWNLOAD_BTN);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.DOWNLOAD_BTN);
    }

    @And("^delete button is displayed and enabled$")
    public void deleteButtonIsDisplayedAndEnabled() {
        Common.verifyElement(driver, ProposalViewerPage.DELETE_BTN);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.DELETE_BTN);
    }

    @And("^close button is displayed and enabled$")
    public void closeButtonIsDisplayedAndEnabled() {
        Common.verifyElement(driver, ProposalViewerPage.CLOSE_BTN);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.CLOSE_BTN);
    }

    @And("^explanatory memorandum section is present$")
    public void explanatoryMemorandumSectionIsPresent() {
        Common.verifyElement(driver, ProposalViewerPage.EXPLN_MEMORANDUM_TEXT);
    }

    @And("^legal act section is present$")
    public void legalActSectionIsPresent() {
        Common.verifyElement(driver, ProposalViewerPage.LEGALACTTEXT);
    }

    @And("^annexes section is present$")
    public void annexesSectionIsPresent() {
        Common.verifyElement(driver, ProposalViewerPage.ANNEXESTEXT);
    }

    @And("^collaborators section is Present$")
    public void collaboratorsSectionIsPresent() {
        Common.verifyElement(driver, ProposalViewerPage.COLLABORATORSTEXT);
    }

    @And("^milestones section is present$")
    public void milestonesSectionIsPresent() {
        Common.verifyElement(driver, ProposalViewerPage.MILESTONESTEXT);
    }

    @When("^click on close button$")
    public void clickOnCloseButton() {
        Common.elementClick(driver, ProposalViewerPage.CLOSE_BTN);
    }

    @When("^click on delete button$")
    public void clickOnDeleteButton() {
        Common.elementClick(driver, ProposalViewerPage.DELETE_BTN);
    }

    @Then("^proposal deletion confirmation page should be displayed$")
    public void proposalDeletionConfirmationPageShouldBeDisplayed() {
        Common.verifyElement(driver, ProposalViewerPage.PROPOSAL_DELETION_CONFIRMATION_POPUP);
    }

    @And("^cancel button is displayed and enabled in proposal deletion confirmation pop up$")
    public void cancelButtonIsDisplayedAndEnabledInProposalDeletionConfirmationPopUp() {
        Common.verifyElement(driver, ProposalViewerPage.CONFIRM_POPUP_CANCEL_BTN);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.CONFIRM_POPUP_CANCEL_BTN);
    }

    @And("^delete button is displayed and enabled in proposal deletion confirmation pop up$")
    public void deleteButtonIsDisplayedAndEnabledInProposalDeletionConfirmationPopUp() {
        Common.verifyElement(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
        Common.verifyElementIsEnabled(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
    }

    @When("^click on delete button present in confirmation pop up$")
    public void clickOnDeleteButtonPresentInConfirmationPopUp() {
        Common.elementClick(driver, ProposalViewerPage.CONFIRM_POPUP_DELETE_BTN);
    }

    @When("^click on download button$")
    public void clickOnDownloadBtn() {
        Common.elementClick(driver, ProposalViewerPage.DOWNLOAD_BTN);
    }

    @And("close button is not displayed")
    public void closeButtonIsNotDisplayed() {
        Common.verifyElementNotPresent(driver, ProposalViewerPage.CLOSE_BTN);
    }

    @When("click on actions hamburger icon of first milestone")
    public void clickOnActionsHamburgerIconOfFirstMilestone() {
        E2eUtil.scrollandClick(driver, ProposalViewerPage.MILESTONE_ACTIONS_MENU_ITEM);
    }

    @Then("below options are displayed under milestone actions hamburger icon")
    public void belowOptionsAreDisplayedUnderMilestoneActionsHamburgerIcon(DataTable dataTable) {
        List<String> givenMenuItemList = dataTable.asList(String.class);
        List<String> actualMenuItemList = new ArrayList<>();
        String text;
        try {
            List<WebElement> elements = driver.findElements(ProposalViewerPage.MILESTONE_ACTIONS_MENU_ITEM_CAPTION);
            if (null != elements && !elements.isEmpty()) {
                for (WebElement element : elements) {
                    text = element.getText();
                    actualMenuItemList.add(text);
                }
                if (!actualMenuItemList.containsAll(givenMenuItemList)) {
                    Assert.fail("given options are not present in the action menu list");
                }
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on send a copy for revision option")
    public void clickOnSendACopyForRevisionOption() {
        Common.elementClick(driver, ProposalViewerPage.MILESTONE_SEND_COPY_FOR_REVISION);
    }

    @Then("share milestone window is displayed")
    public void shareMilestoneWindowIsDisplayed() {
        boolean bool = Common.verifyElement(driver, ProposalViewerPage.SHARE_MILESTONE_WINDOW);
        Assert.assertTrue(bool, "share milestone window is not displayed");
    }

    @And("{string} is mentioned in target user input field")
    public void targetUserFieldIsBlank(String arg0) {
        String text = Common.getElementAttributeValue(driver, ProposalViewerPage.SHARE_MILESTONE_TARGET_USER_INPUT);
        Assert.assertEquals(text, arg0, "target user field is not blank");

    }

    @And("send for revision button is displayed but disabled")
    public void sendForRevisionButtonIsDisplayedButDisabled() {
        boolean bool = Common.verifyElement(driver, ProposalViewerPage.SEND_FOR_REVISION_BUTTON);
        Assert.assertTrue(bool, "send for revision button is not displayed");
        boolean bool1 = Common.verifyElementIsEnabled(driver, ProposalViewerPage.SEND_FOR_REVISION_DISABLED_BUTTON);
        Assert.assertTrue(bool1, "send for revision button is not disabled");
    }

    @And("close button is displayed and enabled in share milestone window")
    public void closeButtonIsDisplayedAndEnabledInShareMilestoneWindow() {
        boolean bool = Common.verifyElement(driver, ProposalViewerPage.SHARE_MILESTONE_CLOSE_BUTTON);
        Assert.assertTrue(bool, "close button is not displayed in share milestone window");
        boolean bool1 = Common.verifyElementIsEnabled(driver, ProposalViewerPage.SHARE_MILESTONE_CLOSE_BUTTON);
        Assert.assertTrue(bool1, "close button is not enabled in share milestone window");
    }

    @When("search {string} in the target user field")
    public void searchInTheTargetUserField(String arg0) {
        Common.elementEcasSendkeys(driver, ProposalViewerPage.SHARE_MILESTONE_TARGET_USER_INPUT, arg0);
        E2eUtil.wait(3000);
    }

    @Then("{string} user is selected in the target user input field")
    public void userIsSelectedInTheTargetUserInputField(String arg0) {
        String text = Common.getElementAttributeValue(driver, ProposalViewerPage.SHARE_MILESTONE_TARGET_USER_INPUT);
        Assert.assertTrue(text.contains(arg0), arg0 + " user is not selected in the target user input field");
    }

    @And("send for revision button is displayed and enabled")
    public void sendForRevisionButtonIsDisplayedAndEnabled() {
        boolean bool = Common.verifyElement(driver, ProposalViewerPage.SEND_FOR_REVISION_BUTTON);
        Assert.assertTrue(bool, "send for revision button is not displayed");
        boolean bool1 = Common.verifyElementIsEnabled(driver, ProposalViewerPage.SEND_FOR_REVISION_BUTTON);
        Assert.assertTrue(bool1, "send for revision button is not enabled");
    }

    @When("click on send for revision button")
    public void clickOnSendForRevisionButton() {
        Common.elementClick(driver, ProposalViewerPage.SEND_FOR_REVISION_BUTTON);
    }

    @And("^\"([^\\\"]*)\" message is displayed$")
    @Then("^([^\\\"]*) message is displayed$")
    public void messageIsDisplayed(String arg0) {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            boolean bool = Common.verifyElement(driver, By.xpath(ProposalViewerPage.XPATH_TEXT_1 + arg0 + ProposalViewerPage.XPATH_TEXT_2));
            Assert.assertTrue(bool, arg0 + " message is not displayed");
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @Then("{string} is showing under title column row {int} of milestones table")
    public void isShowingUnderTitleColumnRowOfMilestonesTable(String arg0, int arg1) {
        String text = Common.getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]" + ProposalViewerPage.GWT_HTML));
        Assert.assertEquals(text, arg0, arg0 + " is showing under title column row " + arg1 + " of milestones table");
    }

    @And("today's date is showing under date column row {int} of milestones table")
    public void todaySDateIsShowingUnderDateColumnRowOfMilestonesTable(int arg0) {
        String pattern = "dd/MM/yyyy";
        String date = Common.getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg0 + "]/td[2]"));
        if (null != date) {
            String subStringDate = date.substring(0, date.length() - 6);
            String dateInString = new SimpleDateFormat(pattern).format(new Date());
            Assert.assertEquals(subStringDate, dateInString, "date mentioned is not today's date");
        } else {
            Assert.fail("unable to retrieve date in string format");
        }
    }

    @And("{string} is showing under status column row {int} of milestones table")
    public void isShowingUnderStatusColumnRowOfMilestonesTable(String arg0, int arg1) {
        String status = Common.getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[3]"));
        Assert.assertEquals(status, arg0, "status is not equal to " + arg0);
    }

    @And("proposal title has a label REVISION EdiT in proposal viewer page")
    public void proposalHasALabelREVISIONEdiTInProposalViewerPage() {
        String text;
        boolean bool = true;
        List<WebElement> elementList = driver.findElements(ProposalViewerPage.CLONED_LABELS);
        if (elementList.size() > 0) {
            for (WebElement element : elementList) {
                text = element.getText();
                if (!(text.equals("REVISION") || text.equals("EdiT"))) {
                    bool = false;
                }
            }
            Assert.assertTrue(bool, "proposal has no label REVISION or EdiT in proposal viewer page");
        } else
            Assert.fail("proposal has no label REVISION EdiT in proposal viewer page");
    }

    @And("{string} is showing in row {int} of title column in milestones table")
    public void isShowingInTitleColumnOfMilestonesTable(String arg0, int arg1) {
        System.out.println("arg0 " + arg0);
        System.out.println("arg1 " + arg1);
        Common.scrollTo(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]" + ProposalViewerPage.GWT_HTML));
        String title = Common.getElementText(driver, By.xpath(ProposalViewerPage.MILESTONE_TABLE_TBODY_TR + "[" + arg1 + "]/td[1]" + ProposalViewerPage.GWT_HTML));
        System.out.println("title " + title);
        Assert.assertEquals(title, arg0, "title is not equal to " + arg0);
    }

    @Then("user {string} is showing in the list")
    public void userStringIsShowingInTheList(String arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(ProposalViewerPage.COLLABORATORS_SEARCH_RESULTS_TR));
        String name;
        boolean result = false;
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                name = driver.findElement(By.xpath(ProposalViewerPage.COLLABORATORS_SEARCH_RESULTS_TR + "[" + i + "]/td/span")).getText();
                if (name.contains(arg0)) {
                    result = true;
                    break;
                }
            }
            Assert.assertTrue(result, arg0 + " is not present in the search results");
        } else {
            Assert.fail("No lists found in the search results");
        }
    }
}
