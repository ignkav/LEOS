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

import europa.edit.util.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class InvokeAndCloseBrowserSteps {

    @When("^navigate to ([^\"]*) edit application$")
    @Given("^navigate to \"([^\"]*)\" edit application$")
    public void invokeApp(String appType) {
        Steplib.startApp(WebDriverFactory.getInstance().getWebdriver(), appType);
    }

    @And("close the browser")
    public void quitTheBrowser() {
        WebDriverFactory.getInstance().getWebdriver().quit();
    }

}
