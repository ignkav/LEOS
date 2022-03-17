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
package europa.edit.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.testng.ISuite;
import org.testng.ISuiteListener;

@Slf4j
public class SuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        logger.debug("onStart");
        val browser = suite.getParameter("browser");
        val environment = suite.getParameter("environment");
        val mode = suite.getParameter("mode");
        TestParameters.getInstance().setEnvironment(environment); //Set environment
        TestParameters.getInstance().setBrowser(browser); //Set browser
        TestParameters.getInstance().setMode(mode); //Set mode
        //WebDriverFactory.getInstance().setTestBrowser(browser); // set the browser to the webdriver class
        //new TestData().readExcelWorkBook();
    }

    @Override
    public void onFinish(ISuite suite) {
        logger.debug("onFinish");
    }
}