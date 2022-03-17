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

import europa.edit.pages.CreateProposalWindowPage;
import europa.edit.util.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;

import java.io.File;

public class CreateProposalSteps extends BaseDriver {

    @Then("^Create proposal window is opened$")
    public void verifyCreateProposalWindow() {
        Common.verifyElement(driver, CreateProposalWindowPage.CREATE_BTN);
        Common.verifyElement(driver, By.xpath(CreateProposalWindowPage.XPATH_TEXT_1 + "Create new legislative document - Template selection (1/2)" + CreateProposalWindowPage.XPATH_TEXT_2));
    }

    @And("^previous button is disabled$")
    public void isPreviousBtnDisabled() {
        Common.isElementDisabled(driver, CreateProposalWindowPage.PREVIOUS_BTN);
    }

    @When("select template {string}")
    public void selectOneTemplate(String arg0) {
        String str = CreateProposalWindowPage.INTER_PROCEDURE + CreateProposalWindowPage.XPATH_TEXT_1 + arg0 + CreateProposalWindowPage.XPATH_TEXT_2;
        Common.elementClick(driver, By.xpath(str));
    }

    @Then("^next button is enabled$")
    public void nextBtnIsEnabled() {
        Common.verifyElementIsEnabled(driver, CreateProposalWindowPage.NEXTBTN);
    }

    @When("^click on next button$")
    public void clickNextBtn() {
        Common.elementClick(driver, CreateProposalWindowPage.NEXTBTN);
    }

    @Then("^\"([^\"]*)\" is displayed$")
    public void showDocumentMetaDataPage(String var1) {
        Common.verifyElement(driver, By.xpath(CreateProposalWindowPage.XPATH_TEXT_1 + var1 + CreateProposalWindowPage.XPATH_TEXT_2));
    }

    @And("^previous button is enabled$")
    public void isPreviousBtnEnabled() {
        Common.verifyElementIsEnabled(driver, CreateProposalWindowPage.PREVIOUS_BTN);
    }

    @When("^click on previous button$")
    public void clickPreviousBtn() {
        Common.elementClick(driver, CreateProposalWindowPage.PREVIOUS_BTN);
    }

    @Then("{string} template window is displayed")
    public void showTemplateSelectionPage(String arg0) {
        Common.verifyElement(driver, By.xpath(CreateProposalWindowPage.XPATH_TEXT_1 + arg0 + CreateProposalWindowPage.XPATH_TEXT_2));
    }

    @And("^cancel button is displayed and enabled$")
    public void isCancelBtnDisplayedAndEnabled() {
        Common.verifyElement(driver, CreateProposalWindowPage.CANCELBTN);
        Common.verifyElementIsEnabled(driver, CreateProposalWindowPage.CANCELBTN);
    }

    @When("^click on cancel button$")
    public void clickCancelBtn() {
        Common.elementClick(driver, CreateProposalWindowPage.CANCELBTN);
    }

    @When("^provide document title \"([^\"]*)\" in document metadata page$")
    public void iProvideDocumentTitleInDocumentMetadataPage(String var1) {
        Common.elementEcasSendkeys(driver, CreateProposalWindowPage.DOCUMENT_TITLE_INPUT, var1);
    }

    @And("^click on create button$")
    public void clickOnCreateButton() {
        Common.elementClick(driver, CreateProposalWindowPage.CREATE_BTN);
    }

    @Then("upload window 'Upload a leg file 1/2' is showing$")
    public void uploadWindowShowingInRepoBrowser() {
        Common.verifyElement(driver, CreateProposalWindowPage.UPLOAD_WINDOW_FIRST_PAGE);
    }

    @When("upload a leg file for creating proposal from location {string}")
    public void clickUploadBtnInUploadWindow(String arg0) {
        try {
            String FileAbsolutePath;
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                FileAbsolutePath = config.getProperty("path.remote.download") + File.separator + arg0;
            } else {
                FileAbsolutePath = config.getProperty("path.local.download") + File.separator + arg0;
            }
            Common.elementEcasSendkeys(driver, CreateProposalWindowPage.UPLOAD_BTN_UPLOAD_WINDOW, FileAbsolutePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Then("^file name should be displayed in upload window$")
    public void showFileNameInUploadWindow() {
        Common.verifyElement(driver, CreateProposalWindowPage.FILENAME_TXT);
    }

    @And("^valid icon should be displayed in upload window$")
    public void showValidIconInUploadWindow() {
        Common.verifyElement(driver, CreateProposalWindowPage.VALID_ICON);
    }
}
