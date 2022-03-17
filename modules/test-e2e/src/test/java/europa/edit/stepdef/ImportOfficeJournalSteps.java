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

import europa.edit.pages.ImportFromOfficeJournal;
import europa.edit.pages.CommonPage;
import europa.edit.pages.RepositoryBrowserPage;
import europa.edit.util.BaseDriver;
import europa.edit.util.Common;
import europa.edit.util.E2eUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;

public class ImportOfficeJournalSteps extends BaseDriver {

    @And("below options are displayed in Type dropdown")
    public void belowOptionsAreDisplayedInTypeDropdown(DataTable dataTable) {
        List<String> givenOptionList = dataTable.asList(String.class);
        List<String> actualOptionList = new ArrayList<>();
        String text;
        try {
            List<WebElement> elements = driver.findElements(ImportFromOfficeJournal.TYPE_SELECT_CLASS_OPTION);
            if (null != elements && !elements.isEmpty()) {
                for (WebElement element : elements) {
                    text = element.getText();
                    actualOptionList.add(text);
                }
                if (!actualOptionList.containsAll(givenOptionList)) {
                    Assert.fail("given options are not present in the type dropdown values");
                }
            } else
                Assert.fail("no element present for type dropdown");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} option is selected by default in Type field")
    public void optionIsSelectedByDefaultInTypeField(String arg0) {
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.TYPE_SELECT_CLASS));
        WebElement option = select.getFirstSelectedOption();
        String text = option.getText();
        Assert.assertEquals(text, arg0, arg0 + " is not selected by default in Type field.Selected Value is " + text);
    }

    @And("current year is selected by default for Year field")
    public void currentYearIsSelectedByDefaultForYearField() {
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.YEAR_SELECT_CLASS));
        WebElement option = select.getFirstSelectedOption();
        String text = option.getText();
        Assert.assertEquals(text, currentYear, currentYear + " is not selected by default for Year field.Selected Value is " + text);
    }

    @And("blank input box is present for Nr. field")
    public void blankInputBoxIsPresentForNrField() {
        String text = Common.getElementAttributeValue(driver, ImportFromOfficeJournal.NR_INPUT);
        Assert.assertEquals(text, "", "provided value is " + text);
    }

    @And("Search button is displayed and enabled")
    public void searchButtonIsDisplayedAndEnabled() {
        boolean bool = Common.verifyElement(driver, ImportFromOfficeJournal.SEARCH_BUTTON);
        Assert.assertTrue(bool, "Search button is not displayed");
        boolean bool1 = Common.verifyElementIsEnabled(driver, ImportFromOfficeJournal.SEARCH_BUTTON);
        Assert.assertTrue(bool1, "Search button is not enabled");
    }

    @And("i button is displayed")
    public void iButtonIsDisplayed() {
        boolean bool = driver.findElement(ImportFromOfficeJournal.I_BUTTON).isDisplayed();
        Assert.assertTrue(bool, "i button is not displayed");
    }

    @And("select all recitals button is displayed but disabled")
    public void selectAllRecitalsButtonIsDisplayedButDisabled() {
        boolean bool = Common.verifyElement(driver, ImportFromOfficeJournal.SELECT_ALL_RECITALS_BUTTON);
        Assert.assertTrue(bool, "select all recitals button is not displayed");
        boolean bool1 = Common.verifyElementIsEnabled(driver, ImportFromOfficeJournal.SELECT_ALL_RECITALS_BUTTON_DISABLED);
        Assert.assertTrue(bool1, "select all recitals button is not disabled");

    }

    @And("select all articles button is displayed but disabled")
    public void selectAllArticlesButtonIsDisplayedButDisabled() {
        boolean bool = Common.verifyElement(driver, ImportFromOfficeJournal.SELECT_ALL_ARTICLES_BUTTON);
        Assert.assertTrue(bool, "select all articles button is not displayed");
        boolean bool1 = Common.verifyElementIsEnabled(driver, ImportFromOfficeJournal.SELECT_ALL_ARTICLES_BUTTON_DISABLED);
        Assert.assertTrue(bool1, "select all articles button is not disabled");
    }

    @And("import button is displayed but disabled")
    public void importButtonIsDisplayedButDisabled() {
        boolean bool = Common.verifyElement(driver, ImportFromOfficeJournal.IMPORT_BUTTON);
        Assert.assertTrue(bool, "import button is not displayed");
        boolean bool1 = Common.verifyElement(driver, ImportFromOfficeJournal.IMPORT_BUTTON_DISABLED);
        Assert.assertTrue(bool1, "import button is not disabled");
    }

    @And("close button in import office journal window is displayed and enabled")
    public void closeButtonInImportOfficeJournalWindowIsDisplayedAndEnabled() {
        boolean bool = Common.verifyElement(driver, ImportFromOfficeJournal.CLOSE_BUTTON);
        Assert.assertTrue(bool, "close button is not displayed");
        boolean bool1 = Common.verifyElementIsEnabled(driver, ImportFromOfficeJournal.CLOSE_BUTTON);
        Assert.assertTrue(bool1, "close button is not enabled");
    }

    @When("mouse hover on i button")
    public void mouseHoverOnIButton() {
        WebElement ele = driver.findElement(ImportFromOfficeJournal.I_BUTTON);
        Actions actions = new Actions(driver);
        actions.moveToElement(ele).build().perform();
    }

    @Then("tooltip contains messages {string} and {string}")
    public void tooltipMessageIsDisplayed(String arg0, String arg1) {
        String text = Common.getElementAttributeInnerText(driver, ImportFromOfficeJournal.I_MOUSE_HOVER_TEXT);
        Assert.assertTrue(text.contains(arg0), arg0 + " is not part of " + text);
        Assert.assertTrue(text.contains(arg1), arg1 + " is not part of " + text);
    }

    @When("click on search button in import office journal window")
    public void clickOnSearchButtonInImportOfficeJournalWindow() {
        Common.elementClick(driver, ImportFromOfficeJournal.SEARCH_BUTTON);
        E2eUtil.wait(1000);
    }

    @Then("border of input box is showing as {string} color")
    public void borderOfInputBoxIsShowingAsRedColor(String arg0) {
        boolean bool = Common.verifyElement(driver, ImportFromOfficeJournal.NR_INPUT_ERROR_INDICATOR);
        Assert.assertTrue(bool, "no error color present on the border of Nr. input box");
        WebElement ele = driver.findElement(ImportFromOfficeJournal.NR_INPUT_ERROR_INDICATOR);
        String borderColor = ele.getCssValue("border-bottom-color");
        Assert.assertEquals(borderColor, arg0, "border of input box is not showing as per the mentioned color");
    }

    @And("exclamation mark is appeared with {string} color")
    public void exclamationMarkIsAppearedWithRedColor(String arg0) {
        boolean bool = Common.verifyElement(driver, ImportFromOfficeJournal.ERROR_INDICATOR);
        Assert.assertTrue(bool, "exclamation mark is not displayed");
        WebElement ele = driver.findElement(ImportFromOfficeJournal.ERROR_INDICATOR);
        String color = ele.getCssValue("color");
        Assert.assertEquals(color, arg0, "exclamation mark is not showing as per the mentioned color");
    }

    @When("select option {string} in Type field")
    public void selectOptionInTypeField(String arg0) {
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.TYPE_SELECT_CLASS));
        select.selectByVisibleText(arg0);
    }

    @And("select option {string} in Year field")
    public void selectOptionInYearField(String arg0) {
        Select select = new Select(driver.findElement(ImportFromOfficeJournal.YEAR_SELECT_CLASS));
        select.selectByVisibleText(arg0);
    }

    @And("provide value {string} in Nr. field")
    public void provideValueInNrField(String arg0) {
        Common.elementEcasSendkeys(driver, ImportFromOfficeJournal.NR_INPUT, arg0);
    }

    @Then("bill content is appeared in import window")
    public void billContentIsAppearedInImportWindow() {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            boolean bool = driver.findElement(By.xpath(ImportFromOfficeJournal.BILL)).isDisplayed();
            Assert.assertTrue(bool, "bill content is not appeared in import window");
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @And("checkbox is available beside to each recital")
    public void checkboxIsAvailableBesideToEachRecital() {
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER));
        List<WebElement> inputElementList = new ArrayList<>();
        WebElement element;
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                try {
                    element = driver.findElement(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + i + "]//input"));
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    element = null;
                }
                if (null != element)
                    inputElementList.add(element);
            }
            if (elementList.size() != inputElementList.size())
                Assert.fail("checkbox is not available beside to each recital");
        } else
            Assert.fail("No recital present in the bill");
    }

    @And("checkbox is available beside to each article")
    public void checkboxIsAvailableBesideToEachArticle() {
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER));
        List<WebElement> inputElementList = new ArrayList<>();
        WebElement element;
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                try {
                    element = driver.findElement(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + i + "]//input"));
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    element = null;
                }
                if (null != element)
                    inputElementList.add(element);
            }
            if (elementList.size() != inputElementList.size())
                Assert.fail("checkbox is not available beside to each article");
        } else
            Assert.fail("No article present in the bill");
    }

    @When("click on checkbox of recital {int}")
    public void clickOnCheckboxOfRecital(int arg0) {
        Common.elementClick(driver, By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + arg0 + "]//input"));
    }

    @When("click on checkbox of article {int}")
    public void clickOnCheckboxOfArticle(int arg0) {
        Common.elementClick(driver, By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + arg0 + "]//input"));
    }

    @When("click on import button")
    public void clickOnImportButton() {
        Common.elementClick(driver, ImportFromOfficeJournal.IMPORT_BUTTON);
        E2eUtil.wait(3000);
    }

    @Then("message {string} is displayed")
    public void messageIsDisplayed(String arg0) {
        boolean bool = Common.verifyElement(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
        Assert.assertTrue(bool, "message " + arg0 + " is not displayed");
    }

    @When("click on select all recitals button in import office journal window")
    public void clickOnSelectAllRecitalsButtonInImportOfficeJournalWindow() {
        Common.elementClick(driver, ImportFromOfficeJournal.SELECT_ALL_RECITALS_BUTTON);
    }

    @When("click on select all articles button in import office journal window")
    public void clickOnSelectAllArticlesButtonInImportOfficeJournalWindow() {
        Common.elementClick(driver, ImportFromOfficeJournal.SELECT_ALL_ARTICLES_BUTTON);
    }

    @Then("checkboxes of all the recitals are selected")
    public void checkboxesOfAllTheRecitalsAreSelected() {
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER));
        List<WebElement> inputElementList = new ArrayList<>();
        WebElement element;
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                try {
                    element = driver.findElement(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + i + "]//input"));
                    boolean bool = element.isSelected();
                    if (bool)
                        inputElementList.add(element);
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    e.printStackTrace();
                }
            }
            if (elementList.size() != inputElementList.size())
                Assert.fail("checkboxes of all the recitals are not selected");
        } else
            Assert.fail("No recital present in the bill");
    }

    @And("number of recitals selected is {int}")
    public void numberOfRecitalsSelectedIs(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER));
        List<WebElement> inputElementList = new ArrayList<>();
        WebElement element;
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                try {
                    element = driver.findElement(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.RECITALS + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + i + "]//input"));
                    boolean bool = element.isSelected();
                    if (bool)
                        inputElementList.add(element);
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    e.printStackTrace();
                }
            }
            if (arg0 != inputElementList.size())
                Assert.fail(inputElementList.size() + " number of recitals are selected not " + arg0);
        } else
            Assert.fail("No recital present in the bill");
    }

    @Then("checkboxes of all the articles are selected")
    public void checkboxesOfAllTheArticlesAreSelected() {
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER));
        List<WebElement> inputElementList = new ArrayList<>();
        WebElement element;
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                try {
                    element = driver.findElement(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER + "[" + i + "]//input"));
                    boolean bool = element.isSelected();
                    if (bool)
                        inputElementList.add(element);
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    e.printStackTrace();
                }
            }
            if (elementList.size() != inputElementList.size())
                Assert.fail("checkboxes of all the articles are not selected");
        } else
            Assert.fail("No article present in the bill");
    }

    @And("number of articles selected is {int}")
    public void numberOfArticlesSelectedIs(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER));
        List<WebElement> inputElementList = new ArrayList<>();
        WebElement element;
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                try {
                    element = driver.findElement(By.xpath(ImportFromOfficeJournal.BILL + ImportFromOfficeJournal.AKNBODY + ImportFromOfficeJournal.LEOS_IMPORT_WRAPPER+"[" + i + "]//input"));
                    boolean bool = element.isSelected();
                    if (bool)
                        inputElementList.add(element);
                } catch (NoSuchElementException | StaleElementReferenceException e) {
                    e.printStackTrace();
                }
            }
            if (arg0 != inputElementList.size())
                Assert.fail(inputElementList.size() + " number of articles are selected not " + arg0);
        } else
            Assert.fail("No article present in the bill");
    }
}
