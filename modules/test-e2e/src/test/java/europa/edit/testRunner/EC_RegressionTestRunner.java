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
        features = {"classpath:europa/edit/features/01_EC_RepositoryBrowserPage.feature", "classpath:europa/edit/features/03_EC_CreateProposal.feature", "classpath:europa/edit/features/04_EC_ProposalViewer.feature", "classpath:europa/edit/features/05_EC_CloneProposal.feature", "classpath:europa/edit/features/06_EC_ExpMemo.feature", "classpath:europa/edit/features/07_EC_ImportFromOj.feature", "classpath:europa/edit/features/08_EC_LegalAct.feature", "classpath:europa/edit/features/09_EC_Annotations.feature", "classpath:europa/edit/features/10_EC_Annex.feature", "classpath:europa/edit/features/15_EC_CleanUp.feature"}
        ,plugin = {"pretty", "html:target/cucumber/report.html", "json:target/cucumber/reports.json", "junit:target/junit-reports/reports.xml", "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:", "rerun:target/results/failed-reports/failedTestCases.txt"}
        ,glue = {"europa/edit/stepdef"}
        ,monochrome = true
/*        ,dryRun = false,*/
/*        ,tags = "@editArticleInCloneProposal"*/
)

@Listeners({SuiteListener.class, TestListener.class})
public class EC_RegressionTestRunner extends AbstractTestNGCucumberTests {
}
