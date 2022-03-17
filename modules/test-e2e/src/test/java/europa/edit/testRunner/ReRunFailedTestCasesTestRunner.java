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
package europa.edit.testRunner;

import europa.edit.util.SuiteListener;
import europa.edit.util.TestListener;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.Listeners;

@CucumberOptions(
        features = {"@target/results/failed-reports/failedTestCases.txt"}
        ,plugin = {"pretty", "html:target/cucumber/ReRunFailedTestCasesReport.html", "json:target/cucumber/ReRunFailedTestCasesReport.json", "junit:target/junit-reports/ReRunFailedTestCasesReport.xml", "rerun:target/results/failed-reports/failedTestCases.txt"}
        ,glue = {"europa/edit/stepdef"}
        ,monochrome = true
/*        ,dryRun = false*/
/*        ,tags = "@VerifyRepositoryBrowserPage"*/
)

@Listeners({SuiteListener.class, TestListener.class})
public class ReRunFailedTestCasesTestRunner extends AbstractTestNGCucumberTests {
}