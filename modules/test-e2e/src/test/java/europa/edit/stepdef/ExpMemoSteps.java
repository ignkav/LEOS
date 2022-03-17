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

import europa.edit.pages.ExpMemoPage;
import europa.edit.pages.ProposalViewerPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.Common;
import europa.edit.util.E2eUtil;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class ExpMemoSteps extends BaseDriver {

    @When("click on open button for explanatory memorandum")
    public void clickOnOpenButtonForExplanatoryMemorandum() {
        Common.elementClick(driver, ProposalViewerPage.EXP_MEMO_OPEN_BUTTON);
    }

    @Then("explanatory memorandum page is displayed")
    public void explanatoryMemorandumPageIsDisplayed() {
        Common.verifyElement(driver, ExpMemoPage.EXP_MEMO_TEXT);
    }

    @And("toc editing button is not displayed")
    public void tocEditingButtonIsNotDisplayed() {
        Common.verifyElementNotPresent(driver, ExpMemoPage.TOC_EDIT_BUTON);
    }

    @When("click on {string} present in the navigation pane")
    public void clickOnPresentInTheNavigationPane(String arg0) {
        Common.elementClick(driver, By.xpath(ExpMemoPage.XPATH_TEXT_1+arg0+ ExpMemoPage.XPATH_TEXT_2));
    }

    @Then("page is redirected to {string}")
    public void pageIsRedirectedTo(String arg0) {
        Common.verifyElement(driver,By.xpath(ExpMemoPage.XPATH_TEXT_1+arg0+ ExpMemoPage.XPATH_TEXT_2));
    }

    @When("click on annotation pop up button")
    public void clickOnAnnotationPopUpButton() {
        Common.elementClick(driver, ExpMemoPage.ENABLE_ANNOTATION_POPUP);
    }

    @When("select {string} in the page")
    public void selectInThePage(String arg0) {
        try{
            boolean bool=Common.selectTextThroughDoubleClick(driver,By.xpath(ExpMemoPage.XPATH_TEXT_1+arg0+ ExpMemoPage.XPATH_TEXT_2));
            if(!bool){
                Assert.fail("Text is not selected");
            }
        }
        catch(Exception | AssertionError e){
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on close button in explanatory memorandum page")
    public void clickOnCloseButtonInExplanatoryMemorandumPage() {
        Common.elementClick(driver, ExpMemoPage.CLOSE_BUTON);
    }

    @Then("comment button is displayed")
    public void commentButtonShouldBeDisplayed() {
        WebElement shadowHostElement=driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js=(JavascriptExecutor)driver;
        WebElement shadowRootElement= (WebElement)js.executeScript("return arguments[0].shadowRoot",shadowHostElement);

        WebElement hypothesisAdderToolbar=shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
        WebElement hypothesisAdderActions=hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
        WebElement button=hypothesisAdderActions.findElement(By.cssSelector("button.h-icon-annotate"));
        E2eUtil.highlightElement(driver,button);
    }

    @Then("highlight button is displayed")
    public void highlightButtonShouldBeDisplayed() {
        WebElement shadowHostElement=driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js=(JavascriptExecutor)driver;
        WebElement shadowRootElement= (WebElement)js.executeScript("return arguments[0].shadowRoot",shadowHostElement);

        WebElement hypothesisAdderToolbar=shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
        WebElement hypothesisAdderActions=hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
        WebElement button=hypothesisAdderActions.findElement(By.cssSelector("button.h-icon-highlight"));
        E2eUtil.highlightElement(driver,button);
    }

    @And("navigation pane is displayed")
    public void navigationPaneIsDisplayed() {
        boolean bool=driver.findElement(ExpMemoPage.NAVIGATION_PANE).isDisplayed();
        Assert.assertTrue(bool, "navigation pane is not displayed");
    }

    @And("explanatory memorandum content is displayed")
    public void explanatoryMemorandumContentIsDisplayed() {
        Common.verifyElement(driver, ExpMemoPage.EXP_MEMO_CONTENT);
    }
}
