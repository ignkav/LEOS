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

import europa.edit.util.E2eUtil;
import org.apache.commons.io.FileUtils;
import io.cucumber.java.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import europa.edit.util.TestParameters;
import europa.edit.util.WebDriverFactory;
import org.testng.ITestResult;
import org.testng.Reporter;
import java.io.File;

public class StepsforScenarios {

    @Before
    public void startScenario(Scenario scenario) {
        TestParameters.getInstance().setScenario(scenario);
        WebDriverFactory.getInstance().setWebDriver();
    }

    @After
    public void closeScenario() {
        if (TestParameters.getInstance().getScenario().isFailed()) {
            E2eUtil.takeSnapShot(WebDriverFactory.getInstance().getWebdriver(), "FAIL");
            Reporter.getCurrentTestResult().setStatus(ITestResult.FAILURE);
            TestParameters.getInstance().getScenario().log("Click below for Screenshot");
            try {
                byte[] bytes = FileUtils.readFileToByteArray(new File(TestParameters.getInstance().getScreenshotPath()));
                TestParameters.getInstance().getScenario().attach(bytes, "image/png", "ErrorScreenshot");
            } catch (Exception e) {
                TestParameters.getInstance().getScenario().log("Exception happen while getting screenshot");
            }
            try {
                if(null!=WebDriverFactory.getInstance().getWebdriver()){
                    WebDriverFactory.getInstance().getWebdriver().quit();
                    TestParameters.getInstance().getScenario().log("Close Browser");
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            try {
                if(null!=WebDriverFactory.getInstance().getWebdriver()){
                    WebDriverFactory.getInstance().getWebdriver().quit();
                    TestParameters.getInstance().getScenario().log("Close Browser");
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        TestParameters.getInstance().reset();
    }
}
