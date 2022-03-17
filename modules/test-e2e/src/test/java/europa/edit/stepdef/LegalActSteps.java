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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

public class LegalActSteps extends BaseDriver {

    @Then("legal act page is displayed")
    public void legalActPageIsDisplayed() {
        E2eUtil.wait(10000);
        Common.verifyElement(driver, LegalActPage.LEGAL_ACT_TEXT);
    }

    @When("click on citation link present in navigation pane")
    public void clickOnCitationLinkPresentInNavigationPane() {
        Common.elementClick(driver, LegalActPage.CITATION_LINK);
    }

    @And("double click on citation {int}")
    public void doubleClickOnFirstCitationParagraph(Integer arg0) {
        E2eUtil.wait(2000);
        Common.doubleClick(driver, By.xpath(LegalActPage.CITATION + "[" + arg0.toString() + "]/aknp"));
    }

    @When("click on preamble toggle link")
    public void clickOnPreamble() {
        Common.elementClick(driver, LegalActPage.PREAMBLE_TOGGLE_LINK);
    }

    @And("first citation is showing as read only")
    public void firstCitationIsShowingAsReadOnly() {
        E2eUtil.wait(5000);
        Common.verifyElement(driver, LegalActPage.CITATION_BEFORE_CKEDITOR);
    }

    @When("mouseHover and click on show all action button and click on edit button of citation {int}")
    public void mouseHoverAndClickOnShowAllActionButtonOfSecondCitation(int arg0) {
        E2eUtil.wait(2000);
        Common.elementClick(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]/aknp"));
        WebElement citation = driver.findElement(By.xpath(LegalActPage.CITATION + "[" + arg0 + "]/aknp"));
        Actions actions = new Actions(driver);
        actions.moveToElement(citation).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllMenu = driver.findElement(By.xpath(LegalActPage.CITATION + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']"));
        actions.moveToElement(showAllMenu).build().perform();
        E2eUtil.wait(2000);
        WebElement editButton = driver.findElement(By.xpath(LegalActPage.CITATION + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        actions.click().build().perform();
    }

    @Then("ck editor window is displayed")
    public void ckEditorWindowIsEnabled() {
        try {
            Common.scrollTo(driver, LegalActPage.CK_EDITOR_WINDOW);
            Boolean bool = Common.elementDisplays(driver, LegalActPage.CK_EDITOR_WINDOW);
            if (!bool) {
                Assert.fail("ck editor is not being displayed");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on close button present in legal act page")
    public void clickOnCloseButtonPresentInLegalActPage() {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            Common.elementClick(driver, LegalActPage.CLOSE_BUTTON);
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @Then("ck editor window is not displayed")
    public void ckEditorWindowIsNotDisplayed() {
        try {
            boolean bool = Common.verifyElementNotPresent(driver, LegalActPage.CK_EDITOR_WINDOW);
            if (!bool) {
                Assert.fail("element is displayed but should not be");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on cancel button of ck editor")
    public void clickOnCancelButtonOfCkEditor() {
        Common.elementClick(driver, LegalActPage.CKEDITOR_CANCEL_BUTTON);
        E2eUtil.wait(2000);
    }

    @And("get text from ck editor text box")
    public void getTextFromCkEditorTextBox() {
        String text = Common.getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        System.out.println("text " + text);
    }

    @And("get text from ck editor li text box")
    public void getTextLiFromCkEditorTextBox() {
        String text = Common.getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_LI_INNERTEXT);
        System.out.println("text " + text);
    }

    @Then("toggle bar moved to right")
    public void toggleBarMovedToRight() {
        Common.verifyElement(driver, LegalActPage.TOGGLE_BAR_IN_RIGHT_SIDE);
    }

    @When("click on toggle bar move to right")
    public void clickOnToggleBarMoveToRight() {
        try {
            //WebElement element = driver.findElement(CN_LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE);
            if (driver.findElements(LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE).size() > 0) {
                Common.elementClick(driver, LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @When("add {string} and delete {string} in the ck editor text box")
    public void addAndDeleteInTheCkEditorTextBox(String arg0, String arg1) {
        String existingText = Common.getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT);
        String deleteText = existingText.replace(arg1, "");
        String newText = arg0 + " " + deleteText + " " + arg0 + ".";
        Common.elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_PARAGRAPH_INNERTEXT, newText);
    }

    @When("add {string} and delete {string} in the ck editor li text box")
    public void addAndDeleteInTheCkEditorLiTextBox(String arg0, String arg1) {
        String existingText = Common.getElementAttributeInnerText(driver, LegalActPage.CK_EDTOR_LI_INNERTEXT);
        String deleteText = existingText.replace(arg1, "");
        String newText = arg0 + " " + deleteText + " " + arg0 + ".";
        Common.elementEcasSendkeys(driver, LegalActPage.CK_EDTOR_LI_INNERTEXT, newText);
    }

    @And("click on save close button of ck editor")
    public void clickOnSaveCloseButtonOfCkEditor() {
        Common.elementClick(driver, LegalActPage.CKEDITOR_SAVECLOSE_BUTTON);
    }

    @And("{string} is added in the text box")
    public void isAddedInTheTextbox(String arg0) {
        try {
            boolean bool = false;
            List<WebElement> elemenetList = driver.findElements(LegalActPage.NEWTEXT_INSIDE_CKEDITOR_CLASS);
            for (WebElement element : elemenetList) {
                String str = Common.getElementText(element);
                if (str.equals(arg0)) {
                    bool = true;
                }
            }
            if (!bool) {
                Assert.fail(arg0 + " is not added in the textbox");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} is deleted with strikeout symbol in the text box")
    public void isDeletedWithStrikeoutSymbolInTheTextbox(String arg0) {
        try {
            boolean bool = false;
            List<WebElement> elemenetList = driver.findElements(LegalActPage.DELETEDTEXT_INSIDE_CKEDITOR_CLASS);
            for (WebElement element : elemenetList) {
                String str = Common.getElementText(element);
                if (str.equals(arg0)) {
                    bool = true;
                }
            }
            if (!bool) {
                Assert.fail(arg0 + " is not deleted in the textbox");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("document is saved")
    public void documentIsSaved() {
        Common.verifyElement(driver, LegalActPage.DOCUMENT_SAVED_TEXT);
    }

    @Then("toc editing button is available")
    public void tocEditingButtonIsAvailable() {
        Common.verifyElement(driver, ExpMemoPage.TOC_EDIT_BUTON);
    }

    @Then("comment, suggest and highlight buttons are not displayed")
    public void commentButtonIsNotDisplayed() {
        WebElement shadowHostElement = driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRootElement = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
        WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.cssSelector("hypothesis-adder-toolbar.annotator-adder[style='visibility: hidden;']"));
    }

    @Then("suggest button is displayed")
    public void suggestButtonIsDisplayed() {
        WebElement shadowHostElement = driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRootElement = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);

        WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
        WebElement hypothesisAdderActions = hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
        WebElement button = hypothesisAdderActions.findElement(By.cssSelector("button.js-suggestion-btn"));
        E2eUtil.highlightElement(driver, button);
    }

    @When("click on the second citation")
    public void clickOnTheFirstCitation() {
        Common.elementActionClick(driver, LegalActPage.CITATION_SECOND_PARAGRAPH);
    }

    @When("click on the first preamble formula")
    public void clickOnTheFirstPremableFormula() {
        Common.elementActionClick(driver, LegalActPage.PREAMBLE_FORMULA_AKNP);
    }

    @When("select content on first preamble formula")
    public void selectContentOnFirstPremableFormula() {
        Common.selectText(driver, LegalActPage.PREAMBLE_FORMULA_AKNP);
    }

    @Then("suggest button is disabled")
    public void suggestButtonIsDisabled() {
        WebElement shadowHostElement = driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRootElement = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);

        WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
        WebElement hypothesisAdderActions = hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
        WebElement button = hypothesisAdderActions.findElement(By.cssSelector("button.js-suggestion-btn.annotator-disabled"));
        E2eUtil.highlightElement(driver, button);
    }

    @When("click on preamble text present in TOC")
    public void clickOnPreambleTextPresentInTOC() {
        Common.elementClick(driver, LegalActPage.PREAMBLE_TEXT);
    }

    @When("select content in citation {int}")
    public void select_content_in_citation(Integer arg0) {
        E2eUtil.wait(2000);
        Common.selectText(driver, By.xpath(LegalActPage.CITATION + "[" + arg0 + "]/aknp"));
    }

    @When("click on comment button")
    public void click_on_comment_button() {
        WebElement shadowHostElement = driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRootElement = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);

        WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
        WebElement hypothesisAdderActions = hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
        WebElement button = hypothesisAdderActions.findElement(By.cssSelector("button.h-icon-annotate"));
        E2eUtil.highlightElement(driver, button);
        button.click();
    }

    @Then("comment box rich text area is displayed")
    public void comment_box_text_area_is_displayed() {
        Common.scrollTo(driver, LegalActPage.COMMENT_RICH_TEXTAREA);
        Common.verifyElement(driver, LegalActPage.COMMENT_RICH_TEXTAREA);
    }

    @When("enter {string} in comment box rich textarea")
    public void enter_in_comment_box_textarea(String string) {
        Common.scrollTo(driver, LegalActPage.COMMENT_HIGHLIGHT_RICH_TEXTAREA_PARAGRAPH);
        Common.elementEcasSendkeys(driver, LegalActPage.COMMENT_HIGHLIGHT_RICH_TEXTAREA_PARAGRAPH, string);
    }

    @When("click on {string} annotation sharing setting")
    public void click_on_annotation_sharing_setting(String string) {
        if (string.equals("comment")) {
            Common.scrollTo(driver, LegalActPage.COMMENT_ARROW_DOWN_BUTTON);
            Common.elementClick(driver, LegalActPage.COMMENT_ARROW_DOWN_BUTTON);
        }
        if (string.equals("suggest")) {
            Common.scrollTo(driver, LegalActPage.SUGGESTION_ARROW_DOWN_BUTTON);
            Common.elementClick(driver, LegalActPage.SUGGESTION_ARROW_DOWN_BUTTON);
        }
        if (string.equals("highlight")) {
            Common.scrollTo(driver, LegalActPage.HIGHLIGHT_ARROW_DOWN_BUTTON);
            Common.elementClick(driver, LegalActPage.HIGHLIGHT_ARROW_DOWN_BUTTON);
        }
    }

    @Then("below groups are displayed in the annotation sharing setting list")
    public void below_groups_are_displayed_in_the_annotation_sharing_setting_list(DataTable dataTable) {
        List<String> givenSharingSettingList = dataTable.asList(String.class);
        List<String> ActualSharingSettingList = new ArrayList<>();
        String text;
        try {
            List<WebElement> elements = driver.findElements(LegalActPage.PUBLISH_ANNOTATION_UL_LI_A);
            if (null != elements && !elements.isEmpty()) {
                for (WebElement element : elements) {
                    text = element.getText();
                    ActualSharingSettingList.add(text);
                }
                if (!ActualSharingSettingList.containsAll(givenSharingSettingList)) {
                    Assert.fail("Given Options are not present in the List");
                }
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on {string} option in the annotation sharing setting list")
    public void click_on_option_in_the_annotation_sharing_setting_list(String string) {
        Common.scrollTo(driver, By.xpath(LegalActPage.PUBLISH_ANNOTATION_UL + LegalActPage.XPATH_TEXT_1 + string + LegalActPage.XPATH_TEXT_2));
        Common.elementClick(driver, By.xpath(LegalActPage.PUBLISH_ANNOTATION_UL + LegalActPage.XPATH_TEXT_1 + string + LegalActPage.XPATH_TEXT_2));
    }

    @When("click on {string} publish button")
    public void click_on_publish_button(String string) {
        if (string.equals("comment")) {
            Common.scrollTo(driver, LegalActPage.COMMENT_PUBLISH_BUTTON);
            E2eUtil.scrollandClick(driver, LegalActPage.COMMENT_PUBLISH_BUTTON);
            E2eUtil.wait(5000);
        }
        if (string.equals("suggest")) {
            //Common.scrollTo(driver, CN_LegalActPage.SUGGESTION_PUBLISH_BUTTON);
            E2eUtil.scrollandClick(driver, LegalActPage.SUGGESTION_PUBLISH_BUTTON);
            E2eUtil.wait(5000);
        }
        if (string.equals("highlight")) {
            //Common.scrollTo(driver, CN_LegalActPage.HIGHLIGHT_PUBLISH_BUTTON);
            E2eUtil.scrollandClick(driver, LegalActPage.HIGHLIGHT_PUBLISH_BUTTON);
            E2eUtil.wait(5000);
        }
    }

    @Then("{string} is showing in the comment text box")
    public void is_showing_in_the_comment_text_box(String string) {
        //Common.scrollTo(driver, CN_LegalActPage.COMMENT_TEXTAREA_PARAGRAPH_INNERTEXT);
        try {
            boolean bool = false;
            String text;
            List<WebElement> elementList = driver.findElements(LegalActPage.COMMENT_TEXTAREA_PARAGRAPH_INNERTEXT);
            if (elementList.size() > 0) {
                for (WebElement element : elementList) {
                    text = element.getText();
                    if (text.contains(string)) {
                        bool = true;
                        break;
                    }
                }
            } else {
                Assert.fail("no comment box present");
            }
            Assert.assertTrue(bool, string + " is not showing in the comment text box");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on suggest button")
    public void click_on_suggest_button() {
        WebElement shadowHostElement = driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRootElement = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);

        WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
        WebElement hypothesisAdderActions = hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
        WebElement button = hypothesisAdderActions.findElement(By.cssSelector("button.js-suggestion-btn"));
        E2eUtil.highlightElement(driver, button);
        button.click();
    }

    @Then("suggest textarea is displayed")
    public void suggest_box_text_area_is_displayed() {
        Common.scrollTo(driver, LegalActPage.SUGGESTION_TEXTAREA);
        Common.verifyElement(driver, LegalActPage.SUGGESTION_TEXTAREA);
    }

    @When("enter {string} in suggest box textarea")
    public void enter_in_suggest_box_textarea(String string) {
        Common.scrollTo(driver, LegalActPage.SUGGESTION_TEXTAREA);
        Common.elementEcasSendkeys(driver, LegalActPage.SUGGESTION_TEXTAREA, string);
    }

    @Then("{string} is showing in the suggest text box")
    public void is_showing_in_the_suggest_text_box(String string) {
        //Common.scrollTo(driver, CN_LegalActPage.SUGGESTION_TEXTAREA_PARAGRAPH_INNERTEXT);
        try {
            boolean bool = false;
            String text;
            List<WebElement> elementList = driver.findElements(LegalActPage.SUGGESTION_TEXTAREA_PARAGRAPH_INNERTEXT);
            if (elementList.size() > 0) {
                for (WebElement element : elementList) {
                    text = element.getText();
                    if (text.contains(string)) {
                        bool = true;
                        break;
                    }
                }
            } else {
                Assert.fail("no suggestion box present");
            }
            Assert.assertTrue(bool, string + " is not present in the suggest text box");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on highlight button")
    public void click_on_highlight_button() {
        WebElement shadowHostElement = driver.findElement(By.xpath("//hypothesis-adder"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement shadowRootElement = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);

        WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
        WebElement hypothesisAdderActions = hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
        WebElement button = hypothesisAdderActions.findElement(By.cssSelector("button.h-icon-highlight"));
        E2eUtil.highlightElement(driver, button);
        button.click();
    }

    @Then("highlight text box is displayed")
    public void highlight_box_is_displayed() {
        Common.scrollTo(driver, LegalActPage.HIGHLIGHT_TEXTBOX);
        boolean bool = driver.findElement(LegalActPage.HIGHLIGHT_TEXTBOX).isDisplayed();
        Assert.assertTrue(bool, "highlight text box is not displayed");
    }

    @When("click on edit button on highlight box")
    public void click_on_edit_button_oh_highlight_box() {
        Common.scrollTo(driver, LegalActPage.HIGHLIGHT_TEXTBOX_EDIT_BUTTON);
        Common.elementClick(driver, LegalActPage.HIGHLIGHT_TEXTBOX_EDIT_BUTTON);
    }

    @Then("highlight rich textarea is displayed")
    public void highlightRichTextareaIsDisplayed() {
        Common.scrollTo(driver, LegalActPage.HIGHLIGHT_RICH_TEXTAREA);
        Common.verifyElement(driver, LegalActPage.HIGHLIGHT_RICH_TEXTAREA);
    }

    @Then("highlight box textarea is displayed")
    public void highlight_box_textarea_is_displayed() {
        Common.scrollTo(driver, LegalActPage.HIGHLIGHT_TEXTAREA);
        Common.verifyElement(driver, LegalActPage.HIGHLIGHT_TEXTAREA);
    }

    @When("enter {string} in highlight box rich textarea")
    public void enter_in_highlight_box_textarea(String string) {
        Common.scrollTo(driver, LegalActPage.COMMENT_HIGHLIGHT_RICH_TEXTAREA_PARAGRAPH);
        Common.elementEcasSendkeys(driver, LegalActPage.COMMENT_HIGHLIGHT_RICH_TEXTAREA_PARAGRAPH, string);
    }

    @Then("{string} is showing in the highlight text box")
    public void is_showing_in_the_highlight_text_box(String string) {
        //Common.scrollTo(driver, CN_LegalActPage.HIGHLIGHT_TEXTAREA_PARAGRAPH_INNERTEXT);
        try {
            boolean bool = false;
            String text;
            List<WebElement> elementList = driver.findElements(LegalActPage.HIGHLIGHT_TEXTAREA_PARAGRAPH_INNERTEXT);
            if (elementList.size() > 0) {
                for (WebElement element : elementList) {
                    text = element.getText();
                    if (text.contains(string)) {
                        bool = true;
                        break;
                    }
                }
            } else {
                Assert.fail("no highlight box present");
            }
            Assert.assertTrue(bool, string + " is not present in the highlight text box");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} button is showing in suggest text box")
    public void ButtonIsShowingInTextBox(String string) {
        if (string.equals("Accept")) {
            Common.scrollTo(driver, LegalActPage.SUGGESTION_ACCEPT_BUTTON);
            Common.verifyElement(driver, LegalActPage.SUGGESTION_ACCEPT_BUTTON);
        }
        if (string.equals("Reject")) {
            Common.scrollTo(driver, LegalActPage.SUGGESTION_REJECT_BUTTON);
            Common.verifyElement(driver, LegalActPage.SUGGESTION_REJECT_BUTTON);
        }
        if (string.equals("Comment")) {
            Common.scrollTo(driver, LegalActPage.SUGGESTION_COMMENT_BUTTON);
            Common.verifyElement(driver, LegalActPage.SUGGESTION_COMMENT_BUTTON);
        }
    }

    @And("switch from main window to iframe {string}")
    public void switchFromMainWindowToIframe(String arg0) {
        driver.switchTo().frame(arg0);
    }

    @And("switch from iframe to main window")
    public void switchFromIframeToMainWindow() {
        driver.switchTo().defaultContent();
    }

    @When("mouse hover on highlight text box")
    public void mouseHoverOnHighlightTextBox() {
        WebElement ele = driver.findElement(LegalActPage.HIGHLIGHT_TEXTBOX);
        Actions act = new Actions(driver);
        act.moveToElement(ele).build().perform();
    }

    @When("mouse hover on comment text box")
    public void mouseHoverOnCommentTextBox() {
        WebElement ele = driver.findElement(LegalActPage.COMMENT_TEXTBOX);
        Actions act = new Actions(driver);
        act.moveToElement(ele).build().perform();
    }

    @When("click on delete icon of comment text box")
    public void deleteCommentTextBox() {
        E2eUtil.scrollandClick(driver, LegalActPage.COMMENT_TEXTBOX_DELETE_BUTTON);
    }

    @When("click on delete icon of highlight text box")
    public void deleteHighlightTextBox() {
        E2eUtil.scrollandClick(driver, LegalActPage.HIGHLIGHT_TEXTBOX_DELETE_BUTTON);
    }

    @When("click on reject button of suggest text box")
    public void rejectSuggestTextBox() {
        E2eUtil.scrollandClick(driver, LegalActPage.SUGGESTION_REJECT_BUTTON);
    }

    @Then("comment text box is not present")
    public void commentTextBoxIsNotPresent() {
        Common.verifyElementNotPresent(driver, LegalActPage.COMMENT_TEXTBOX);
    }

    @Then("highlight text box is not present")
    public void highlightTextBoxIsNotPresent() {
        Common.verifyElementNotPresent(driver, LegalActPage.HIGHLIGHT_TEXTBOX);
    }

    @Then("suggest text box is not present")
    public void suggestTextBoxIsNotPresent() {
        Common.verifyElementNotPresent(driver, LegalActPage.SUGGESTION_TEXTBOX);
    }

    @When("mouseHover and click on show all action button and click on edit button of recital {int}")
    public void mousehoverAndClickOnShowAllActionButtonAndClickOnEditButtonOfSecondRecital(int arg0) {
        E2eUtil.wait(5000);
        Common.elementClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]/aknp"));
        WebElement citation = driver.findElement(By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]/aknp"));
        Actions actions = new Actions(driver);
        actions.moveToElement(citation).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllMenu = driver.findElement(By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllMenu).build().perform();
        E2eUtil.wait(3000);
        WebElement editButton = driver.findElement(By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        E2eUtil.wait(2000);
        actions.click().build().perform();
    }

    @When("click on recital link present in navigation pane")
    public void clickOnRecitalLinkPresentInNavigationPane() {
        Common.elementClick(driver, LegalActPage.TOC_RECITAL_LINK);
    }

    @And("double click on recital {int}")
    public void doubleClickOnRecital(int arg0) {
        E2eUtil.wait(2000);
        Common.doubleClick(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]/aknp"));
    }

    @When("select content in recital {int}")
    public void selectContentInRecital(int arg0) {
        E2eUtil.wait(2000);
        Common.selectText(driver, By.xpath(LegalActPage.RECITAL + "[" + arg0 + "]/aknp"));
    }

    @When("click on ok button present in windows alert pop up")
    public void clickOnOkButtonPresentInWindowsAlertPopUp() {
        driver.switchTo().alert().accept();
        driver.switchTo().defaultContent();
    }

    @When("click on article {int} in navigation pane")
    public void clickOnArticleInNavigatonPane(int arg0) {
        E2eUtil.scrollandClick(driver, By.xpath(LegalActPage.TOC_TABLE_TREE_GRID + "//*[contains(text(),'Article " + arg0 + "')]"));
    }

    @And("double click on paragraph {int}")
    public void doubleClickOnParagraph(int arg0) {
        E2eUtil.wait(2000);
        Common.doubleClick(driver, By.xpath("(" + LegalActPage.PARAGRAPH + ")[" + arg0 + "]" + LegalActPage.SUBPARAGRAPH + "//aknp"));
    }

    @When("mousehover and click on show all action button and click on edit button of point {int}")
    public void mousehoverAndClickOnShowAllActionButtonAndClickOnEditButtonOfPoint(int arg0) {
        E2eUtil.wait(2000);
        Common.elementClick(driver, By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//aknp"));
        WebElement citation = driver.findElement(By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//aknp"));
        Actions actions = new Actions(driver);
        actions.moveToElement(citation).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllMenu = driver.findElement(By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']"));
        actions.moveToElement(showAllMenu).build().perform();
        E2eUtil.wait(3000);
        WebElement editButton = driver.findElement(By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        actions.click().build().perform();
    }

    @When("select content in point {int}")
    public void selectContentInPoint(int arg0) {
        E2eUtil.wait(2000);
        Common.selectText(driver, By.xpath("(" + LegalActPage.POINT + ")[" + arg0 + "]//aknp"));
    }

    @When("click on toc edition")
    public void click_on_toc_edition() {
        Common.elementClick(driver, LegalActPage.TOC_EDIT_BUTON);
        E2eUtil.wait(3000);
    }

    @Then("save button in navigation pane is disabled")
    public void save_button_in_navigation_pane_is_disabled() {
        try {
            String str = driver.findElement(LegalActPage.NAVIGATION_PANE_SAVE_BUTTON).getAttribute("class");
            Assert.assertTrue(str.contains("v-disabled"), "save button in navigation pane is not disabled");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("save and close button in navigation pane is disabled")
    public void save_and_close_button_in_navigation_pane_is_disabled() {
        try {
            String str = driver.findElement(LegalActPage.NAVIGATION_PANE_SAVE_AND_CLOSE_BUTTON).getAttribute("class");
            Assert.assertTrue(str.contains("v-disabled"), "save and close button in navigation pane is not disabled");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("cancel button in navigation pane is displayed and enabled")
    public void close_buton_in_navigation_pane_is_displayed_and_enabled() {
        try {
            boolean bool1 = Common.verifyElement(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
            if (!bool1) {
                Assert.fail("cancel buton in navigation pane is not displayed");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
        try {
            boolean bool2 = Common.verifyElementIsEnabled(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
            if (!bool2) {
                Assert.fail("cancel buton in navigation pane is not enabled");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("selected element section is displayed")
    public void selected_element_section_is_displayed() {
        Common.verifyElement(driver, LegalActPage.SELECTED_ELEMENT_TEXT);
    }

    @Then("input value {string} for element Type is disabled in selected element section")
    public void typeIsDisabledWithValue(String arg0) {
        try {
            String str = Common.getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_TYPE_INPUT);
            Assert.assertEquals(str, arg0);
            String className = driver.findElement(LegalActPage.SELECTED_ELEMENT_TYPE_INPUT).getAttribute("class");
            Assert.assertTrue(className.contains("v-disabled"), "input value for element Type is not disabled");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("input value {int} for element Number is disabled in selected element section")
    public void numberIsDisabledWithValue(Integer arg0) {
        try {
            String str = Common.getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_NUMBER_INPUT);
            Assert.assertEquals(str, arg0.toString());
            String className = driver.findElement(LegalActPage.SELECTED_ELEMENT_NUMBER_INPUT).getAttribute("class");
            Assert.assertTrue(className.contains("v-disabled"), "input value for element Number is not disabled");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("input value {string} for element Heading is editable in selected element section")
    public void heading_of_the_article_is_editable(String arg0) {
        try {
            String str = Common.getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
            Assert.assertEquals(str, arg0);
            boolean bool = Common.verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
            if (!bool) {
                Assert.fail("input value for element Heading is not editable");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("Paragraph Numbering has below options")
    public void paragraph_numbering_has_below_two_options(DataTable dataTable) {
        try {
            List<String> givenParagraphNumberingOptions = dataTable.asList(String.class);
            List<String> actualParagraphNumberingOptions = new ArrayList<>();
            List<WebElement> elementList = driver.findElements(LegalActPage.SELECTED_ELEMENT_PARAGRAPH_NUMBERING_LABEL_LIST);
            for (int i = 0; i < elementList.size(); i++) {
                String str = elementList.get(i).getText();
                actualParagraphNumberingOptions.add(i, str);
            }
            if (!actualParagraphNumberingOptions.containsAll(givenParagraphNumberingOptions)) {
                Assert.fail("one or more options are not present in Paragraph Numbering options");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("both the options of Paragraph Numbering are editable")
    public void both_options_are_editable() {
        try {
            boolean bool1 = Common.verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_PARAGRAPH_NUMBERING_NUMBERED_INPUT);
            boolean bool2 = Common.verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_PARAGRAPH_NUMBERING_UNNUMBERED_INPUT);
            if (!bool1) {
                Assert.fail("option Numbered of element Paragraph Numbering is not editable");
            }
            if (!bool2) {
                Assert.fail("option Unnumbered of element Paragraph Numbering is not editable");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("delete button is displayed and enabled in selected element section")
    public void deleteButtonIsDisplayedAndEnabledInSelectedElementSection() {
        try {
            boolean bool1 = Common.verifyElement(driver, LegalActPage.SELECTED_ELEMENT_DELETE_BUTTON);
            if (!bool1) {
                Assert.fail("delete button in selected element section is not displayed");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
        try {
            boolean bool2 = Common.verifyElementIsEnabled(driver, LegalActPage.SELECTED_ELEMENT_DELETE_BUTTON);
            if (!bool2) {
                Assert.fail("delete button in selected element section is not enabled");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("click on cross symbol of the selected element")
    public void click_on_cross_symbol_for_the_selected_element() {
        Common.elementClick(driver, LegalActPage.SELECTED_ELEMENT_CLOSE_BUTTON);
    }

    @Then("selected element section is not displayed")
    public void selected_element_section_is_not_displayed() {
        try {
            boolean bool = Common.verifyElementNotPresent(driver, LegalActPage.SELECTED_ELEMENT_TEXT);
            if (!bool) {
                Assert.fail("Selected element Section is displayed");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("save button in navigation pane is not displayed")
    public void saveButtonInNavigationPaneIsNotDisplayed() {
        boolean bool = Common.verifyElementNotPresent(driver, LegalActPage.NAVIGATION_PANE_SAVE_BUTTON);
        Assert.assertTrue(bool, "save button in navigation pane is present");
    }

    @Then("save and close button in navigation pane is not displayed")
    public void saveAndCloseButtonInNavigationPaneIsNotDisplayed() {
        boolean bool = Common.verifyElementNotPresent(driver, LegalActPage.NAVIGATION_PANE_SAVE_AND_CLOSE_BUTTON);
        Assert.assertTrue(bool, "save and close button in navigation pane is present");
    }

    @Then("cancel button in navigation pane is not displayed")
    public void cancelButtonInNavigationPaneIsNotDisplayed() {
        boolean bool = Common.verifyElementNotPresent(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
        Assert.assertTrue(bool, "cancel button in navigation pane is present");
    }

    @Then("below element lists are displayed in Elements menu")
    public void below_element_lists_is_displayed_in_element_menu(DataTable dataTable) {
        List<String> givenElementList = dataTable.asList(String.class);
        List<String> actualElementList = new ArrayList<>();
        List<WebElement> elementList = driver.findElements(LegalActPage.NAVIGATION_ELEMENTS_LIST);
        for (int i = 0; i < elementList.size(); i++) {
            String str = elementList.get(i).getText();
            System.out.println("Element: " + str);
            actualElementList.add(i, str);
        }
        System.out.println(givenElementList);
        System.out.println(actualElementList);
        Assert.assertTrue(actualElementList.containsAll(givenElementList), "one or more element types are not present in Elements menu");
    }

/*
    @When("drag Article {int} and drop on Article {int}")
    public void drag_article_and_drop_on_article(Integer int1, Integer int2) {
        WebElement fromRow = driver.findElement(By.xpath("//table[@role='treegrid']/tbody//div[contains(text(),'Article 1')]//ancestor::tr"));
        //WebElement toRow = driver.findElement(By.xpath("//table[@role='treegrid']/tbody//div[contains(text(),'Article 2')]//ancestor::tr"));
        Actions act = new Actions(driver);
//        act.clickAndHold(fromRow).moveToElement(toRow).build().perform();
//        act.release().build().perform();
        act.dragAndDropBy(fromRow, 0, 150).build().perform();
//        //Action dragAndDrop =act.clickAndHold(From).moveToElement(To).release(To).build();
//        act.clickAndHold(From).moveToElement(To).release(To).build().perform();
//        WebElement xxx = driver.findElement(By.xpath("//*[contains(@class,'v-treegrid-scroller-vertical')]"));
//        E2eUtil.scrollElementMouse(driver,xxx,450);

        //dragAndDrop=act.moveToElement(To).release(To).build();
       */
/* JavascriptExecutor js = (JavascriptExecutor) driver;
        String s="function createEvent(typeOfEvent) {" +
                "    var event = document.createEvent(\"CustomEvent\");" +
                "    event.initCustomEvent(typeOfEvent, true, true, null);" +
                "    event.dataTransfer = {" +
                "        data: {}," +
                "        setData: function (key, value) {" +
                "            this.data[key] = value;" +
                "        }," +
                "        getData: function (key) {" +
                "            return this.data[key];" +
                "        }" +
                "    };" +
                "    return event;" +
                "}" +
                "function dispatchEvent(element, event, transferData) {" +
                "    if (transferData !== undefined) {" +
                "        event.dataTransfer = transferData;" +
                "    }" +
                "    if (element.dispatchEvent) {" +
                "        element.dispatchEvent(event);" +
                "    } else if (element.fireEvent) {" +
                "        element.fireEvent(\"on\" + event.type, event);" +
                "    }" +
                "}" +
                "function simulateHTML5DragAndDrop(element, target) {" +
                "    var dragStartEvent = createEvent('dragstart');" +
                "    dispatchEvent(element, dragStartEvent);" +
                "    var dropEvent = createEvent('drop');" +
                "    dispatchEvent(target, dropEvent, dragStartEvent.dataTransfer);" +
                "    var dragEndEvent = createEvent('dragend');" +
                "    dispatchEvent(element, dragEndEvent, dropEvent.dataTransfer);" +
                "}";
        s += "simulateHTML5DragAndDrop(arguments[0], arguments[1])";
        js.executeScript(s, From, To);*//*

        E2eUtil.wait(2000);
    }
*/

    @When("click on {string} button present in navigation pane")
    public void click_on_button_present_in_navigation_pane(String str) {
        if (str.equals("cancel")) {
            E2eUtil.scrollandClick(driver, LegalActPage.NAVIGATION_PANE_CANCEL_BUTTON);
        }
        if (str.equals("save and close")) {
            E2eUtil.scrollandClick(driver, LegalActPage.NAVIGATION_PANE_SAVE_AND_CLOSE_BUTTON);
        }
        if (str.equals("save")) {
            E2eUtil.scrollandClick(driver, LegalActPage.NAVIGATION_PANE_SAVE_BUTTON);
        }
        E2eUtil.wait(2000);
    }

/*    @Then("title of Article {int} at original position is showing as light grey and strikethrough followed by a label stating {string}")
    public void title_of_article_at_original_position_is_showing_as_light_grey_and_strikethrough_followed_by_a_label_stating(Integer int1, String string) {

    }

    @Then("content of Article {int} is hard deleted")
    public void content_of_article_is_hard_deleted(Integer int1) {

    }

    @Then("at new position {string} is showing bold followed by a label stating {string}")
    public void at_new_position_is_showing_bold_followed_by_a_label_stating(String string, String string2) {

    }

    @When("drag Paragraph {int} of Article {int} and drop on Paragraph {int} of Article {int}")
    public void drag_paragraph_of_article_and_drop_on_paragraph_of_article(Integer int1, Integer int2, Integer int3, Integer int4) {

    }

    @Then("paragraph {int} at original position is showing as light grey and strikethrough along with label stating {string}")
    public void paragraph_at_original_position_is_showing_as_light_grey_and_strikethrough_along_with_label_stating(Integer int1, String string) {

    }

    @Then("paragraph number becomes {string} along with label stating {string}")
    public void paragraph_number_becomes_along_with_label_stating(String string, String string2) {

    }*/

/*
    @When("drag a {string} from Elements menu and drop on Article {int}")
    public void drag_a_and_drop_on_article(String string, Integer int1) {
        E2eUtil.wait(2000);
        WebElement fromRow = driver.findElement(By.xpath("//*[text()='Elements']//ancestor::div[contains(@class,'leos-left-slider-panel')]//div[contains(@class,'leos-drag-item') and text()='Article']"));
        WebElement toRow = driver.findElement(By.xpath("//table[@role='treegrid']/tbody//div[contains(text(),'Article 1')]"));
//        Actions act = new Actions(driver);
//        act.clickAndHold(fromRow).pause(Duration.ofSeconds(1)).moveToElement(toRow).pause(Duration.ofSeconds(1)).release().build().perform();
    }

    @When("clear and add text {string} in heading of selected element section")
    public void clear_and_add_text_in_heading_of_selected_element_section(String string) {

    }

    @Then("{string} is showing bold in navigation pane")
    public void is_showing_bold_in_navigation_pane(String string) {

    }

    @Then("content of Article {string} is bold")
    public void content_of_article_is_bold(String string) {

    }
*/

    @When("click on delete button present in selected element section")
    public void click_on_delete_button_present_in_selected_element_section() {
        Common.elementClick(driver, LegalActPage.SELECTED_ELEMENT_DELETE_BUTTON);
    }

/*    @Then("{string} pop up window is displayed")
    public void pop_up_window_is_displayed(String string) {
        Common.verifyElement(driver, CN_LegalActPage.DELETE_ITEM_CONFIRMATION_TEXT);
    }*/

    @Then("message {string} is displayed in the {string} pop up window")
    public void message_is_displayed_in_the_pop_up_window(String string1, String string2) {
        Common.verifyElement(driver, By.xpath("//*[text()='" + string2 + "']//ancestor::div[@class='popupContent']//*[text()='" + string1 + "']"));
    }

    @When("click on continue button present in Delete item: confirmation pop up window")
    public void click_on_button_present_in_pop_up_window() {
        Common.elementClick(driver, LegalActPage.DELETE_ITEM_CONFIRMATION_WINDOW_CONTINUE_BUTTON);
    }

    @Then("successful message {string} is showing above selected element section in navigation pane")
    public void successful_message_is_showing_above_selected_element_section_in_navigation_pane(String string) {
        Common.verifyElement(driver, By.xpath(LegalActPage.XPATH_TEXT_1 + string + LegalActPage.XPATH_TEXT_2));
    }

/*
    @Then("article {int} is showing in light grey and strikethrough in navigation pane")
    public void article_is_showing_in_light_grey_and_strikethrough_in_navigation_pane(Integer int1) {

    }

    @Then("article {int} is showing in light grey and strikethrough in main window")
    public void article_is_showing_in_light_grey_and_strikethrough_in_main_window(Integer int1) {

    }

    @When("drag {string} and drop it on {string} {int}")
    public void drag_and_drop_it_on(String string, String string2, Integer int1) {

    }
*/

    @Then("error message {string} is showing above selected element section in navigation pane")
    public void error_message_is_showing_above_selected_element_section_in_navigation_pane(String string) {
        Common.verifyElement(driver, By.xpath(LegalActPage.XPATH_TEXT_1 + string + LegalActPage.XPATH_TEXT_2));
    }

    @Then("elements section attached to navigation pane is not displayed")
    public void elementsSectionAttachedToNavigationIsNotDisplayed() {
        boolean bool = Common.verifyElementNotPresent(driver, LegalActPage.NAVIGATION_ELEMENTS_LEFT_SLIDER_PANEL);
        Assert.assertTrue(bool, "elements section attached to navigation pane is still displayed");
    }

    @When("switch to {string} rich textarea iframe")
    public void switchToIframe(String arg0) {
        if (arg0.equalsIgnoreCase("comment")) {
            driver.switchTo().frame(driver.findElement(By.xpath(LegalActPage.COMMENT_ANNOTATION + LegalActPage.NG_SHOW_EDITOR + LegalActPage.RICH_TEXTAREA_IFRAME)));
        }
        if (arg0.equalsIgnoreCase("highlight")) {
            driver.switchTo().frame(driver.findElement(By.xpath(LegalActPage.HIGHLIGHT_ANNOTATION + LegalActPage.NG_SHOW_EDITOR + LegalActPage.RICH_TEXTAREA_IFRAME)));
        }
    }

    @And("switch to parent frame")
    public void switchToParentFrame() {
        driver.switchTo().parentFrame();
    }

    @And("legal act content is displayed")
    public void legalActContentIsDisplayed() {
        boolean bool = driver.findElement(LegalActPage.LEGAL_ACT_CONTENT).isDisplayed();
        Assert.assertTrue(bool, "legal act content is not displayed");
    }

    @When("click on actions hamburger icon")
    public void clickOnActionsHamburgerIcon() {
        Common.elementClick(driver, LegalActPage.ACTION_MENU);
    }

    @Then("below options are displayed")
    public void belowOptionsAreDisplayed(DataTable dataTable) {
        List<String> givenOptionList = dataTable.asList(String.class);
        for (String str1 : givenOptionList) {
            System.out.println("str1 " + str1);
        }
        List<String> actualOptionList = new ArrayList<>();
        String text;
        try {
            List<WebElement> elements = driver.findElements(LegalActPage.ACTIONS_MENU_BAR_POP_UP);
            if (null != elements && !elements.isEmpty()) {
                for (WebElement element : elements) {
                    text = element.getText();
                    actualOptionList.add(text);
                }
                for (String str2 : actualOptionList) {
                    System.out.println("str2 " + str2);
                }
                if (!actualOptionList.containsAll(givenOptionList)) {
                    Assert.fail("given options are not present in the actual options list");
                }
            } else
                Assert.fail("no element present for action menu bar pop up");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} option is checked")
    public void optionIsChecked(String arg0) {
        boolean bool = Common.verifyElement(driver, By.xpath(LegalActPage.XPATH_TEXT_1 + arg0 + LegalActPage.XPATH_TEXT_2 + LegalActPage.NAVIGATION_MENU_ITEM_CHECKED));
        Assert.assertTrue(bool, arg0 + " option is not checked");
    }

    @When("click on {string} option")
    public void clickOnOption(String arg0) {
        Common.elementClick(driver, By.xpath(LegalActPage.ACTIONS_SUB_MENU_ITEM + LegalActPage.XPATH_TEXT_1 + arg0 + LegalActPage.XPATH_TEXT_2));
    }


    @When("click on versions pane accordion")
    public void clickOnVersionsPaneAccordian() {
        Common.elementClick(driver, LegalActPage.VERSIONS_PANE);
        E2eUtil.wait(500);
    }

    @And("click on show more button in recent changes section inside version pane")
    public void clickOnShowMoreButtonInRecentChangesSectionInsideVersionPane() {
        Common.elementClick(driver, LegalActPage.RECENT_CHANGES_SHOW_MORE_BUTTON);
        E2eUtil.wait(500);
    }

    @Then("{int} last technical versions are the imports from office journal")
    public void lastTechnicalVersionsAreTheImportsFromOfficeJournal(int arg0) {
        String text;
        for (int i = 1; i <= arg0; i++) {
            text = Common.getElementText(driver, By.xpath(LegalActPage.RECENT_CHANGES_TEXT + "//ancestor::div[@id='versionCard']//*[@id='versionsBlock']//table//tr[" + i + "]/td//div[contains(@class,'v-label-undef-w')]"));
            if (!text.contains("Import element(s) inserted")) {
                Assert.fail("last " + arg0 + " technical versions are not the imports from office journal");
            }
        }
    }

    @When("click on toggle bar move to left")
    public void clickOnToggleBarMoveToLeft() {
        try {
            if (driver.findElements(LegalActPage.TOGGLE_BAR_IN_RIGHT_SIDE).size() > 0) {
                Common.elementClick(driver, LegalActPage.TOGGLE_BAR_IN_RIGHT_SIDE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Then("toggle bar moved to left")
    public void toggleBarMovedToLeft() {
        boolean bool = Common.verifyElement(driver, LegalActPage.TOGGLE_BAR_IN_LEFT_SIDE);
        Assert.assertTrue(bool, "toggle bar is not moved to left");
    }

    @Then("{int} recitals are added at the end of the recitals part")
    public void recitalsAreAddedAtTheEndOfTheRecitalsPart(int arg0) {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            List<WebElement> elementList = driver.findElements(LegalActPage.RECITALS_SOFT_NEW);
            Assert.assertEquals(elementList.size(), arg0, arg0 + " recitals are not added at the end of the recitals part");
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @Then("{int} articles are added at the end of the articles part")
    public void articlesAreAddedAtTheEndOfTheArticlesPart(int arg0) {
        WebElement ele = Common.waitForElementTobePresent(driver, CommonPage.LOADING_PROGRESS);
        if (null != ele) {
            List<WebElement> elementList = driver.findElements(LegalActPage.ARTICLES_SOFT_NEW);
            Assert.assertEquals(elementList.size(), arg0, arg0 + " articles are not added at the end of the recitals part");
        } else
            Assert.fail("unable to load the page in the specified time duration");
    }

    @Then("below comments are showing in the comment text boxes")
    public void belowCommentsAreShowingInTheCommentTextBoxes(DataTable dataTable) {
        List<String> givenCommentList = dataTable.asList(String.class);
        List<String> actualCommentList = new ArrayList<>();
        String text;
        try {
            List<WebElement> elements = driver.findElements(LegalActPage.ANNOTATION_COMMENT_PARAGRAPH);
            if (null != elements && !elements.isEmpty()) {
                for (WebElement element : elements) {
                    text = element.getText();
                    actualCommentList.add(text);
                }
                if (!actualCommentList.containsAll(givenCommentList)) {
                    Assert.fail("below comments are not showing in the comment text boxes");
                }
            } else
                Assert.fail("no comment is present in the annotation list");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("below suggestions are showing in the suggestion text boxes")
    public void belowSuggestionsAreShowingInTheSuggestionTextBoxes(DataTable dataTable) {
        List<String> givenSuggestionList = dataTable.asList(String.class);
        List<String> actualSuggestionList = new ArrayList<>();
        String text;
        try {
            List<WebElement> elements = driver.findElements(LegalActPage.ANNOTATION_SUGGESTION_CONTENT_NEW);
            if (null != elements && !elements.isEmpty()) {
                for (WebElement element : elements) {
                    text = element.getText();
                    actualSuggestionList.add(text);
                }
                if (!actualSuggestionList.containsAll(givenSuggestionList)) {
                    Assert.fail("below suggestions are not showing in the comment text boxes");
                }
            } else
                Assert.fail("no suggestion is present in the annotation list");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("add comment message {string} {int} times to recital {int}")
    public void addCommentMessageTimesToRecital(String arg0, int arg1, int arg2) {
        for (int i = 1; i <= arg1; i++) {
            E2eUtil.wait(2000);
            Common.selectText(driver, By.xpath(LegalActPage.RECITAL + "[" + arg2 + "]/aknp"));

            WebElement shadowHostElement = driver.findElement(By.xpath("//hypothesis-adder"));
            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebElement shadowRootElement = (WebElement) js.executeScript("return arguments[0].shadowRoot", shadowHostElement);
            WebElement hypothesisAdderToolbar = shadowRootElement.findElement(By.tagName("hypothesis-adder-toolbar"));
            WebElement hypothesisAdderActions = hypothesisAdderToolbar.findElement(By.tagName("hypothesis-adder-actions"));
            WebElement button = hypothesisAdderActions.findElement(By.cssSelector("button.h-icon-annotate"));
            button.click();

            driver.switchTo().frame("hyp_sidebar_frame");

            driver.switchTo().frame(driver.findElement(By.xpath(LegalActPage.COMMENT_ANNOTATION + LegalActPage.NG_SHOW_EDITOR + LegalActPage.RICH_TEXTAREA_IFRAME)));


            Common.scrollTo(driver, LegalActPage.COMMENT_HIGHLIGHT_RICH_TEXTAREA_PARAGRAPH);
            Common.elementEcasSendkeys(driver, LegalActPage.COMMENT_HIGHLIGHT_RICH_TEXTAREA_PARAGRAPH, arg0 + " " + i);

            driver.switchTo().parentFrame();

            Common.scrollTo(driver, LegalActPage.COMMENT_PUBLISH_BUTTON);
            E2eUtil.scrollandClick(driver, LegalActPage.COMMENT_PUBLISH_BUTTON);
            E2eUtil.wait(1000);

            driver.switchTo().defaultContent();
        }
    }

    @And("{string} option is ticked in Export to eConsilium window")
    public void optionIsTickedInExportToEConsiliumWindow(String arg0) {
        boolean bool = Common.verifyIsElementSelected(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
        Assert.assertTrue(bool, arg0 + " option is not ticked in Export to eConsilium window");
    }

    @When("provide title {string} in Export to eConsilium window")
    public void provideTitleInExportToEConsiliumWindow(String arg0) {
        Common.elementEcasSendkeys(driver, LegalActPage.TITLE_ECONSILIUM_WINDOW, arg0);
    }

    @And("tick {string} option in Export to eConsilium window")
    public void tickOptionInExportToEConsiliumWindow(String arg0) {
        Common.elementClick(driver, By.xpath("//*[text()='" + arg0 + "']//preceding-sibling::input"));
    }

    @And("click on export button in Export to eConsilium window")
    public void clickOnExportButtonInExportToEConsiliumWindow() {
        Common.elementClick(driver, LegalActPage.EXPORT_BUTTON_ECONSILIUM_WINDOW);
    }

    @And("mouse hover and click on show all action button and click on edit button of article {int}")
    public void mouseHoverAndClickOnShowAllActionButtonAndClickOnEditButtonOfArticle(int arg0) {
        E2eUtil.wait(3000);
        WebElement article1 = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        Actions actions = new Actions(driver);
        actions.moveToElement(article1).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllMenu = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllMenu).build().perform();
        E2eUtil.wait(3000);
        WebElement editButton = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@data-widget-type='edit']"));
        actions.moveToElement(editButton).build().perform();
        E2eUtil.wait(2000);
        actions.click().build().perform();
    }

    @And("save close button is disabled in ck editor")
    public void saveCloseButtonIsDisabledInCkEditor() {
        boolean bool = Common.verifyElementIsEnabled(driver, LegalActPage.SAVE_CLOSE_BUTTON_DISABLED);
        Assert.assertTrue(bool, "save close button is not disabled in ck editor");
    }

    @And("save button is disabled in ck editor")
    public void saveButtonIsDisabledInCkEditor() {
        boolean bool = Common.verifyElementIsEnabled(driver, LegalActPage.SAVE_BUTTON_DISABLED);
        Assert.assertTrue(bool, "save button is not disabled in ck editor");
    }

    @When("double click on article {int}")
    public void doubleClickOnArticle(int arg0) {
        E2eUtil.wait(2000);
        Common.doubleClick(driver, By.xpath(LegalActPage.BILL + LegalActPage.ARTICLE + "[" + arg0 + "]//aknp"));
    }

    @When("append {string} at the end of the paragraph {int} of the article")
    public void appendAtTheEndOfTheParagraphOfArticle(String arg0, int arg1) {
        String existingText = Common.getElementAttributeInnerText(driver, By.xpath(LegalActPage.BILL + LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + "[1]/ol/li[" + arg1 + "]"));
        String newText = existingText + " " + arg0;
        Common.elementEcasSendkeys(driver, By.xpath(LegalActPage.BILL + LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + "[1]/ol/li[" + arg1 + "]"), newText);
    }

    @Then("confirm cancel editing window is displayed")
    public void confirmCancelEditingWindowIsDisplayed() {
        boolean bool = Common.verifyElement(driver, LegalActPage.CONFIRM_CANCEL_EDITING);
        Assert.assertTrue(bool, "confirm cancel editing window is not displayed");
    }

    @When("click on ok button in confirm cancel editing window")
    public void clickOnOkButtonInConfirmCancelEditingWindow() {
        Common.elementClick(driver, LegalActPage.OK_BUTTON);
        E2eUtil.wait(500);
    }

    @Then("{string} is added with colour {string} to the paragraph {int} of article {int}")
    public void isAddedWithColourToTheParagraphOfArticle(String arg0, String arg1, int arg2, int arg3) {
        try {
            boolean bool = false;
            String color = "";
            List<WebElement> elementList = driver.findElements(By.xpath("//bill//article[" + arg3 + "]//paragraph[" + arg2 + "]//aknp//*[@class='leos-content-new']"));
            for (WebElement element : elementList) {
                String str = Common.getElementText(element);
                if (str.contains(arg0)) {
                    bool = true;
                    color = element.getCssValue("color");
                    break;
                }
            }
            if (!bool) {
                Assert.fail(arg0 + " is not added in the article " + arg3 + " paragraph " + arg2);
            }
            Assert.assertEquals(color, arg1, arg0 + " is not added with colour " + arg1);
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("{int} recitals are added in bill content")
    public void recitalsAreAddedInBillContent(int arg0) {
        List<WebElement> elementList = driver.findElements(LegalActPage.RECITAL_NEW);
        Assert.assertEquals(elementList.size(), arg0, arg0 + " recitals are not added at the end of the recitals part");
    }

    @Then("{int} articles are added in bill content")
    public void articlesAreAddedInBillContent(int arg0) {
        List<WebElement> elementList = driver.findElements(LegalActPage.ARTICLE_NEW);
        Assert.assertEquals(elementList.size(), arg0, arg0 + " articles are not added at the end of the recitals part");
    }

    @And("{string} is added to citation {int} in EC legal act")
    public void isAddedInCitationInECLegalAct(String arg0, int arg1) {
        try {
            String text = Common.getElementText(driver, By.xpath(LegalActPage.CITATIONS + LegalActPage.CITATION + "[" + arg1 + "]" + LegalActPage.AKNP));
            Assert.assertTrue(text.contains(arg0), arg0 + " is not added to citation " + arg1 + " in EC legal act");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} is deleted from citation {int} in EC legal act")
    public void isDeletedFromCitationInECLegalAct(String arg0, int arg1) {
        try {
            String text = Common.getElementText(driver, By.xpath(LegalActPage.CITATIONS + LegalActPage.CITATION + "[" + arg1 + "]" + LegalActPage.AKNP));
            Assert.assertFalse(text.contains(arg0), arg0 + " is not deleted from citation " + arg1 + " in EC legal act");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} is added to recital {int} in EC legal act")
    public void isAddedToRecitalInECLegalAct(String arg0, int arg1) {
        try {
            String text = Common.getElementText(driver, By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.AKNP));
            Assert.assertTrue(text.contains(arg0), arg0 + " is not added to recital " + arg1 + " in EC legal act");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} is deleted from recital {int} in EC legal act")
    public void isDeletedFromRecitalInECLegalAct(String arg0, int arg1) {
        try {
            String text = Common.getElementText(driver, By.xpath(LegalActPage.RECITALS + LegalActPage.RECITAL + "[" + arg1 + "]" + LegalActPage.AKNP));
            Assert.assertFalse(text.contains(arg0), arg0 + " is not deleted from recital " + arg1 + " in EC legal act");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} is added to article {int} paragraph {int} in EC legal act")
    public void isAddedToArticleParagraphInECLegalAct(String arg0, int arg1, int arg2) {
        try {
            String text = Common.getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.AKNP));
            Assert.assertTrue(text.contains(arg0), arg0 + " is not added to the article in EC legal act");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("{string} is deleted from article {int} paragraph {int} in EC legal act")
    public void isDeletedFromArticleParagraphInECLegalAct(String arg0, int arg1, int arg2) {
        try {
            String text = Common.getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.AKNP));
            Assert.assertFalse(text.contains(arg0), arg0 + " is not deleted from the article in EC legal act");
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @When("add {string} and delete {string} in the ck editor of article list {int}")
    public void addAndDeleteInTheCkEditorOfArticleList(String arg0, String arg1, int arg2) {
        String existingText = Common.getElementAttributeInnerText(driver, By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.LIST + "[" + arg2 + "]"));
        String deleteText = existingText.replace(arg1, "");
        String newText = arg0 + " " + deleteText + " " + arg0 + ".";
        Common.elementEcasSendkeys(driver, By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.LIST + "[" + arg2 + "]"), newText);
    }

    @When("select content in article {int} paragraph {int}")
    public void selectContentInArticleParagraph(int arg0, int arg1) {
        E2eUtil.wait(2000);
        Common.selectText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.PARAGRAPH + "[" + arg1 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
    }

    @And("total number of article is {int} in enacting terms")
    public void totalNumberOfArticleIsInEnactingTerms(int arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE));
        Assert.assertEquals(elementList.size(), arg0, "total number of article is not " + arg0 + " in enacting terms");
    }

    @Then("article {int} is displayed in bill content")
    public void articleIsDisplayedInBillContent(int arg0) {
        boolean bool = Common.verifyElement(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        Assert.assertTrue(bool, "article " + arg0 + " is not displayed in bill content");
    }

    @When("click on insert before icon present in show all actions icon of article {int}")
    public void clickOnInsertBeforeIconPresentInShowAllActionsIconOfArticle(int arg0) {
        Actions actions = new Actions(driver);
        boolean bool = Common.waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        if (bool) {
            WebElement article = Common.waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
            actions.moveToElement(article).build().perform();
        }
        boolean bool1 = Common.waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        if (bool1) {
            WebElement showAllActionsIcon = Common.waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
            actions.moveToElement(showAllActionsIcon).build().perform();
        }
        boolean bool2 = Common.waitForElementTobeDisPlayed(driver, LegalActPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
        if (bool2) {
            WebElement insertBefore = Common.waitForElementTobePresent(driver, LegalActPage.SHOW_ALL_ACTIONS_INSERT_BEFORE);
            actions.moveToElement(insertBefore).build().perform();
            actions.click().release().build().perform();
        }
    }

    @When("click on insert after icon present in show all actions icon of article {int}")
    public void clickOnInsertAfterIconPresentInShowAllActionsIconOfArticle(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement article = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        actions.moveToElement(article).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllActionsIcon = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllActionsIcon).build().perform();
        E2eUtil.wait(3000);
        WebElement insertBefore = driver.findElement(LegalActPage.SHOW_ALL_ACTIONS_INSERT_AFTER);
        actions.moveToElement(insertBefore).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on edit icon present in show all actions icon of article {int}")
    public void clickOnEditIconPresentInShowAllActionsIconOfArticle(int arg0) {
        E2eUtil.wait(2000);
        Actions actions = new Actions(driver);
        WebElement article = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        actions.moveToElement(article).build().perform();
        E2eUtil.wait(2000);
        WebElement showAllActionsIcon = driver.findElement(By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        actions.moveToElement(showAllActionsIcon).build().perform();
        E2eUtil.wait(3000);
        WebElement insertBefore = driver.findElement(LegalActPage.SHOW_ALL_ACTIONS_EDIT);
        actions.moveToElement(insertBefore).build().perform();
        actions.click().build().perform();
        E2eUtil.wait(2000);
    }

    @When("click on delete icon present in show all actions icon of article {int}")
    public void clickOnDeleteIconPresentInShowAllActionsIconOfArticle(int arg0) {
        Actions actions = new Actions(driver);
        boolean bool = Common.waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
        if (bool) {
            WebElement article = Common.waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]"));
            actions.moveToElement(article).build().perform();
        }
        boolean bool1 = Common.waitForElementTobeDisPlayed(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
        if (bool1) {
            WebElement showAllActionsIcon = Common.waitForElementTobePresent(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg0 + "]" + "//following-sibling::div[1][@class='leos-actions Vaadin-Icons']/span[@title='Show all actions']"));
            actions.moveToElement(showAllActionsIcon).build().perform();
        }
        boolean bool2 = Common.waitForElementTobeDisPlayed(driver, LegalActPage.SHOW_ALL_ACTIONS_DELETE);
        if (bool2) {
            WebElement delete = Common.waitForElementTobePresent(driver, LegalActPage.SHOW_ALL_ACTIONS_DELETE);
            actions.moveToElement(delete).build().perform();
            actions.click().release().build().perform();
        }
    }

    @When("add {string} to CK EDITOR of article paragraph {int}")
    public void addInArticleParagraph(String arg0, int arg1) {
        By xPath = By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + LegalActPage.LIST + "[" + arg1 + "]");
        String text = Common.getElementAttributeInnerText(driver, xPath);
        String newText = text + " " + arg0;
        Common.elementEcasSendkeys(driver, xPath, newText);
    }

    @And("{string} is added to article {int} paragraph {int}")
    public void isAddedToArticleParagraph(String arg0, int arg1, int arg2) {
        String text = Common.getElementText(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        Assert.assertTrue(text.contains(arg0), arg0 + " is not added to article " + arg1 + " paragraph " + arg2);
    }

    @When("remove {string} from CK EDITOR of article paragraph {int}")
    public void removeFromTheContentOfArticleParagraph(String arg0, int arg1) {
        By xPath = By.xpath(LegalActPage.CK_EDITABLE_INLINE + LegalActPage.ARTICLE + LegalActPage.LIST + "[" + arg1 + "]");
        String text = Common.getElementAttributeInnerText(driver, xPath);
        String deletedText = text.replace(arg0, "");
        Common.elementEcasSendkeys(driver, xPath, deletedText);
    }

    @And("{string} is removed from article {int} paragraph {int}")
    public void isRemovedFromArticleParagraph(String arg0, int arg1, int arg2) {
        String text = Common.getElementText(driver, By.xpath(LegalActPage.BILL + LegalActPage.AKNBODY + LegalActPage.ARTICLE + "[" + arg1 + "]" + LegalActPage.PARAGRAPH + "[" + arg2 + "]" + LegalActPage.CONTENT + LegalActPage.AKNP));
        Assert.assertFalse(text.contains(arg0), arg0 + " is not removed from article " + arg1 + " paragraph " + arg2);
    }

    @When("append text {string} to the heading of the element in selected element section")
    public void appendTextToTheHeadingOfTheElementInSelectedElementSection(String arg0) {
        String text = Common.getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
        Common.elementEcasSendkeys(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT, text + arg0);
    }

    @And("click on save button in navigation pane")
    public void clickOnSaveButtonInNavigationPane() {
        Common.elementClick(driver, AnnexPage.TOC_SAVE_BUTTON);
    }

    @Then("heading of article {int} contains {string}")
    public void headingOfArticleContains(int arg0, String arg1) {
        String text = Common.getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.HEADING));
        Assert.assertTrue(text.contains(arg1), "heading of article " + arg0 + " doesn't contain " + arg1);
    }

    @When("remove text {string} from the heading of the element in selected element section")
    public void removeTextFromTheHeadingOfTheElementInSelectedElementSection(String arg0) {
        String text = Common.getElementAttributeValue(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT);
        String deletedText = text.replace(arg0, "");
        Common.elementEcasSendkeys(driver, LegalActPage.SELECTED_ELEMENT_HEADING_INPUT, deletedText);
    }

    @Then("heading of article {int} doesn't contain {string}")
    public void headingOfArticleDoesntContain(int arg0, String arg1) {
        String text = Common.getElementText(driver, By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.HEADING));
        Assert.assertFalse(text.contains(arg1), "heading of article " + arg0 + " contains " + arg1);
    }

    @And("click on element {string}")
    public void clickOnElement(String arg0) {
        Common.elementClick(driver, By.xpath(LegalActPage.XPATH_TEXT_1 + arg0 + LegalActPage.XPATH_TEXT_2));
    }

    @And("heading of Article {int} is {string}")
    public void headingOfArticleIs(int arg0, String arg1) {
        By articleHeading = By.xpath(LegalActPage.ARTICLE + "[" + arg0 + "]" + LegalActPage.HEADING);
        Common.scrollTo(driver, articleHeading);
        String text = Common.getElementText(driver, articleHeading);
        Assert.assertEquals(text, arg1, "heading of Article " + arg0 + " is not " + arg1);
    }
}
