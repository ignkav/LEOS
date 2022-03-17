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
import europa.edit.util.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.io.IOException;


public class CommonSteps extends BaseDriver {

    private final Cryptor td = new Cryptor();
    private final Configuration config = new Configuration();
    @When("^enter username \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void enterCredentials(String name, String pwd) {
        String userName=config.getProperty(name);
        String userPwd=config.getProperty(pwd);
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            if (Common.elementExistsWithOutwait(driver, SignInPage.USER_NAME)) {
                Common.elementEcasSendkeys(driver, SignInPage.USER_NAME, userName);
                Common.elementClick(driver, SignInPage.NEXT_BUTTON);
                Common.elementEcasSendkeys(driver, PasswordPage.PASSWORD, td.decrypt(userPwd));
                Common.elementClick(driver, PasswordPage.SIGN_IN_BUTTON);
            }
        }
    }

    @And("^user name is present in the Top right upper corner$")
    public void VerifyUserNamePresent() {
        Common.verifyElement(driver, RepositoryBrowserPage.USERNAME_ICON);
    }

    @When("^click on home button$")
    public void clickOnHomeButton() {
        Common.elementClick(driver, ProposalViewerPage.HOME_BTN);
    }

    @When("^click on minimize application header button$")
    public void iClickOnMinimizeApplicationHeaderButton() {
        Common.elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
    }

    @Then("^application header is minimized$")
    public void applicationHeaderIsMinimized() {
        Common.verifyElement(driver, CommonPage.SUBTITLE_HEADER_ELEMENT);
    }

    @When("^click on maximize application header button$")
    public void iClickOnMaximizeApplicationHeaderButton() {
        Common.elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
    }

    @Then("^application header is maximized$")
    public void applicationHeaderIsMaximized() {
        Common.verifyElement(driver, CommonPage.TITLE_HEADER_ELEMENT);
    }

    @And("^sleep for (.*) milliseconds")
    public void takeTimerSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @When("find the recent {string} file in download path and unzip it in {string} and get the latest {string} file")
    public void findAndUnzipFile(String fileType, String relativePath, String searchFileType) throws Exception {
        boolean bool = Common.waitForElementTobeDisPlayed(driver, ProposalViewerPage.PROPOSAL_DOWNLOAD_MESSAGE);
        if (bool) {
            E2eUtil.scrollandClick(driver,ProposalViewerPage.PROPOSAL_DOWNLOAD_MESSAGE);
            boolean bool1 = Common.waitUnTillElementIsNotPresent(driver, ProposalViewerPage.PROPOSAL_DOWNLOAD_MESSAGE);
            if (bool1)
                E2eUtil.findAndUnzipFile(fileType, relativePath, searchFileType);
        }
    }

    @Then("print the latest {string} file name in relative location {string}")
    public void printFileName(String arg0, String arg1) throws IOException {
        String legFileNamePath = E2eUtil.findFile(arg0, arg1, null);
        if (null != legFileNamePath) {
            System.out.println("legFileNamePath " + legFileNamePath);
        } else {
            try {
                Assert.fail("Unable to find the file");
            } catch (AssertionError e) {
                e.printStackTrace();
                throw e;
            }
        }

    }

    @And("^\"([^\"]*)\" button is present$")
    public void VerifySpecificButton(String btn) {
        Common.verifyElement(driver, By.xpath(RepositoryBrowserPage.XPATH_TEXT_1 + btn + RepositoryBrowserPage.XPATH_TEXT_2));
    }

    @When("^click on \"([^\"]*)\" button$")
    public void clickOnSpecificButton(String var1) {
        Common.elementClick(driver, By.xpath(RepositoryBrowserPage.XPATH_TEXT_1 + var1 + RepositoryBrowserPage.XPATH_TEXT_2));
    }

    @Then("{string} window is displayed")
    public void windowIsDisplayed(String arg0) {
        Common.verifyElement(driver, By.xpath(RepositoryBrowserPage.XPATH_TEXT_1 + arg0 + RepositoryBrowserPage.XPATH_TEXT_2));
    }

    @When("upload a latest {string} file for creating proposal from location {string}")
    public void uploadALatestFileForCreatingProposalFromLocation(String arg0, String arg1) throws IOException {
        try {
            String FileNamePath = E2eUtil.findFile(arg0, arg1, null);
            if (null != FileNamePath) {
                Common.elementEcasSendkeys(driver, CreateProposalWindowPage.UPLOAD_BTN_UPLOAD_WINDOW, FileNamePath);
            } else {
                Assert.fail("Unable to find the file");
            }
        } catch (Exception | AssertionError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @And("click on logout button")
    public void logoutFromBrowser() {
        Common.elementClick(driver, CommonPage.LOGOUT_BUTTON);
    }

    @When("redirect the browser to ECAS url")
    public void redirectToECASUrl() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            String str = config.getProperty("ecas.appUrl");
            driver.get(str);
        }
    }

    @Then("ECAS successful login page is displayed")
    public void pageIsDisplayed() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = Common.verifyElement(driver, SignInPage.ECAS_SUCCESSFUL_LOGIN_TEXT);
            Assert.assertTrue(bool, "ECAS successful login page is not displayed");
        }
    }

    @When("click on logout button in ECAS logged in page")
    public void clickOnLogoutButtonInECASLoggedInPage() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            Common.elementClick(driver, SignInPage.ECAS_LOG_OUT_BUTTON);
        }
    }

    @Then("user is logged out from ECAS")
    public void userIsLoggedOutFromECAS() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = Common.verifyElement(driver, SignInPage.ECAS_LOGGED_OUT_MESSAGE);
            Assert.assertTrue(bool, "user is not logged out from ECAS");
        }
    }

    @Then("sign in with a different e-mail address page is displayed")
    public void signInWithADifferentEMailAddressPageIsDisplayed() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = Common.verifyElement(driver, SignInPage.ECAS_SIGN_IN_WITH_DIFFERENT_USER);
            Assert.assertTrue(bool, "sign in with a different e-mail address page is not displayed");
        }
    }

    @When("click on sign in with a different e-mail address hyperlink")
    public void clickOnSignInWithADifferentEMailAddressHyperlink() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            Common.elementClick(driver, SignInPage.ECAS_SIGN_IN_WITH_DIFFERENT_USER);
        }
    }

    @Then("sign in to continue page is displayed")
    public void signInToContinuePageIsDisplayed() {
        if (!TestParameters.getInstance().getEnvironment().equals("local")) {
            boolean bool = Common.verifyElement(driver, SignInPage.ECAS_SIGN_IN_USER_TEXT);
            Assert.assertTrue(bool, "sign in to continue page is not displayed");
        }
    }

    @When("double click on minimize maximize button present in the right upper corner of the application")
    public void doubleClickOnMinimizeMaximizeButtonPresentInTheRightUpperCornerOfTheApplication() {
        Common.elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
        E2eUtil.wait(500);
        Common.elementClick(driver, CommonPage.MIN_MAX_APP_HEADER_ICON);
    }

    @And("click on message {string}")
    public void clickOnMessage(String arg0) {
        E2eUtil.scrollandClick(driver, By.xpath(CommonPage.XPATH_TEXT_1 + arg0 + CommonPage.XPATH_TEXT_2));
    }

/*    @Then("print the innerHTML {string}")
    public void printTheInnerHTML(String arg0) {
        List<WebElement> elementList = driver.findElements(By.xpath("//*[@id='VAADIN_COMBOBOX_OPTIONLIST']//table/tbody/tr"));
        String text = null;
        String[] stringArray = new String[3];
        if (elementList.size() > 0) {
            for (int i = 1; i <= elementList.size(); i++) {
                text = driver.findElement(By.xpath("//*[@id='VAADIN_COMBOBOX_OPTIONLIST']//table/tbody/tr" + "[" + i + "]/td/span")).getText();
                System.out.println("text " + text);
                stringArray = text.split(" ");
            }
            System.out.println("stringArray " + stringArray.toString());
            boolean result = Arrays.stream(stringArray).anyMatch(arg0::equals);
            if (!result) {
                Assert.fail(arg0 + " is not present in the search results");
            }
        } else {
            Assert.fail("No lists found in the search results");
        }
    }*/
}
