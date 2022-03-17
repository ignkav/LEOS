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
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import java.util.List;

public class AnnexSteps extends BaseDriver {
    @Then("{string} Annex page is displayed")
    public void annexPageIsDisplayed(String arg0) {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            boolean bool = Common.verifyElement(driver, By.xpath(AnnexPage.XPATH_TEXT_1 + arg0 + AnnexPage.XPATH_TEXT_2));
            Assert.assertTrue(bool, arg0 + " Annex page is not displayed");
        } else {
            Assert.fail("unable to load the page in the specified time duration");
        }
    }

    @And("preface and body is present in annex navigation pane")
    public void prefaceAndBodyIsPresentInAnnexNavigationPane() {
        boolean bool = Common.verifyElement(driver, AnnexPage.PREFACE);
        Assert.assertTrue(bool, "preface is not present in annex navigation pane");
        boolean bool1 = Common.verifyElement(driver, AnnexPage.BODY);
        Assert.assertTrue(bool1, "body is not present in annex navigation pane");
    }

    @Then("show all actions icon is displayed")
    public void showAllActionsIconIsDisplayed() {
        boolean bool = Common.verifyElement(driver, AnnexPage.SHOW_ALL_ACTIONS);
        Assert.assertTrue(bool, "show all actions icon is not displayed");
    }

    @When("click on show all actions icon")
    public void clickOnShowAllActionsIcon() {
        Actions actions = new Actions(driver);
        WebElement showAllMenu = driver.findElement(AnnexPage.SHOW_ALL_ACTIONS);
        actions.moveToElement(showAllMenu).build().perform();
    }

    @When("click on insert before icon present in show all actions icon of level {int}")
    public void clickOnInsertBeforeIcon(int arg0) {
        Actions actions = new Actions(driver);
        boolean bool = Common.waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
        if (bool) {
            WebElement level = Common.waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
            actions.moveToElement(level).build().perform();
        }
        boolean bool1 = Common.waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON));
        if (bool1) {
            WebElement showAllActionsIcon = Common.waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON));
            actions.moveToElement(showAllActionsIcon).build().perform();
        }
        boolean bool2 = Common.waitForElementTobeDisPlayed(driver, AnnexPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
        if (bool2) {
            WebElement insertBefore = Common.waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
            actions.moveToElement(insertBefore).build().perform();
            actions.click().release().build().perform();
        }
    }

    @When("click on edit icon present in show all actions icon of level {int}")
    public void clickOnEditIconPresentInShowAllActionsIcon(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement level = driver.findElement(By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
        actions.moveToElement(level).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllActionsIcon = driver.findElement(By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON));
        actions.moveToElement(showAllActionsIcon).build().perform();
        E2eUtil.wait(3000);
        WebElement edit = driver.findElement(AnnexPage.SHOW_ALL_ACTIONS_EDIT);
        actions.moveToElement(edit).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on insert after icon present in show all actions icon of level {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIcon(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement level = driver.findElement(By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
        actions.moveToElement(level).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllActionsIcon = driver.findElement(By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON));
        actions.moveToElement(showAllActionsIcon).build().perform();
        E2eUtil.wait(3000);
        WebElement insertAfter = driver.findElement(AnnexPage.SHOW_ALL_ACTIONS_INSERT_AFTER);
        actions.moveToElement(insertAfter).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @And("{int} level is present in the body of annex page")
    public void levelPresentInTheBodyOfAnnexPage(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.LEVEL));
        Assert.assertEquals(elementList.size(), arg0, arg0 + " level is not present in the body of annex page");
    }

    @When("append {string} at the end of the content of level {int}")
    public void appendAtTheEndOfTheContentOfLevel(String arg0, int arg1) {
        String existingText = Common.getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String newText = existingText + " " + arg0;
        Common.elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, newText);
    }

    @And("{string} is added to content of level {int}")
    public void isAddedToContentOfLevel(String arg0, int arg1) {
        try {
            String text = Common.getElementText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg1 + "]" + AnnexPage.AKNP));
            Assert.assertTrue(text.contains(arg0), arg0 + " is not added to content of level " + arg1);
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("double click on level {int}")
    public void doubleClickOnLevel(int arg0) {
        E2eUtil.wait(1000);
        Common.doubleClick(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
        E2eUtil.wait(2000);
    }

    @When("remove {string} from the content of level {int}")
    public void removeFromTheContentOfLevel(String arg0, int arg1) {
        String existingText = Common.getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String deleteText = existingText.replace(arg0, "");
        Common.elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, deleteText);
    }

    @And("{string} is removed from content of level {int}")
    public void isRemovedFromContentOfLevel(String arg0, int arg1) {
        try {
            String text = Common.getElementText(driver, By.xpath(AnnexPage.LEVEL + "[" + arg1 + "]" + AnnexPage.AKNP));
            Assert.assertFalse(text.contains(arg0), arg0 + " is not removed from content of level " + arg1);
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on close button present in annex page")
    public void clickOnCloseButtonPresentInAnnexPage() {
        Common.elementClick(driver, AnnexPage.CLOSE_BUTTON);
    }

    @And("total number of level is {int}")
    public void totalNumberOfLevelIs(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(AnnexPage.LEVEL));
        Assert.assertEquals(elementList.size(), arg0, "total number of level is " + elementList.size());
    }

    @When("click on delete icon present in show all actions icon of level {int}")
    public void clickOnDeleteIconPresentInShowAllActionsIcon(int arg0) {
        Actions actions = new Actions(driver);
        boolean bool = Common.waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
        if (bool) {
            WebElement level = Common.waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
            actions.moveToElement(level).build().perform();
        }
        boolean bool1 = Common.waitForElementTobeDisPlayed(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON));
        if (bool1) {
            WebElement showAllActionsIcon = Common.waitForElementTobePresent(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]" + AnnexPage.SHOW_ALL_ACTIONS_ICON));
            actions.moveToElement(showAllActionsIcon).build().perform();
        }
        boolean bool2 = Common.waitForElementTobeDisPlayed(driver, AnnexPage.SHOW_ALL_ACTIONS_DELETE);
        if (bool2) {
            WebElement insertBefore = Common.waitForElementTobePresent(driver, AnnexPage.SHOW_ALL_ACTIONS_DELETE);
            actions.moveToElement(insertBefore).build().perform();
            actions.click().release().build().perform();
        }
    }

    @When("click on cancel button in navigation pane")
    public void clickOnCancelButtonInNavigationPane() {
        Common.elementClick(driver, AnnexPage.TOC_CANCEL_BUTTON);
    }

    @When("click on save and close button in navigation pane")
    public void clickOnSaveAndCloseButtonInNavigationPane() {
        Common.elementClick(driver, AnnexPage.TOC_SAVE_AND_CLOSE_BUTTON);
    }

    @Then("elements menu lists are not displayed")
    public void elementsMenuListsAreNotDisplayed() {
        boolean bool = Common.verifyElementNotPresent(driver, LegalActPage.NAVIGATION_ELEMENTS_LIST);
        Assert.assertFalse(bool, "elements menu lists are displayed");
    }

    @When("scroll to level {int} in the content page")
    public void scrollToLevelInTheContentPage(int arg0) {
        Common.scrollTo(driver, By.xpath(AnnexPage.LEVEL + "[" + arg0 + "]"));
    }

    @When("click on element {int} in annex")
    public void clickOnElementInAnnex(int arg0) {
        int index = arg0 + 2;
        E2eUtil.scrollandClick(driver, By.xpath(AnnexPage.TOC_TABLE_TR + "[" + index + "]" + "//div[contains(@class,'gwt-HTML')]"));
    }
}
