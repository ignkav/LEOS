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

import europa.edit.pages.CreateMandatePage;
import europa.edit.pages.RepositoryBrowserPage;
import europa.edit.util.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;

public class CreateMandateSteps extends BaseDriver {

    @Then("upload screen is showing with \"([^\"]*)\" page$")
    public void verifyUploadPage(String var2) {
        Common.verifyElement(driver, By.xpath(RepositoryBrowserPage.XPATH_TEXT_1 + var2 + RepositoryBrowserPage.XPATH_TEXT_2));
    }

    @When("^upload a leg file for creating a mandate$")
    public void uploadLegFileCouncil() throws IOException {
        String legFileNamePath = E2eUtil.findFile("leg", "council" + Constants.SLASH + "createMandate", null);
        if (null != legFileNamePath) {
            Common.elementEcasSendkeys(driver, CreateMandatePage.UPLOAD_ICON_INPUT, legFileNamePath);
        } else {
            try {
                Assert.fail("Unable to upload the file");
            } catch (Exception | AssertionError e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    @And("^title of the mandate is stored in the context map$")
    public void titleOfTheMandateIsStoredInTheContextMap() {
    }

    @When("upload a leg file for creating mandate from location {string}")
    public void uploadALegFileForCreatingMandateFromLocation(String arg0) {
        String fileAbsolutePath;
        try {
            if (TestParameters.getInstance().getMode().equalsIgnoreCase("remote")) {
                fileAbsolutePath = config.getProperty("path.remote.download") + File.separator + arg0;
            } else {
                fileAbsolutePath = config.getProperty("path.local.download") + File.separator + arg0;
            }
            Common.elementEcasSendkeys(driver, CreateMandatePage.UPLOAD_ICON_INPUT, fileAbsolutePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
