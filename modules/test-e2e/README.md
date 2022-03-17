# End to End tests in local environment
End2end tests use Chrome browser to execute the tests.
Is advisable (but not mandatory) to not use input devices (mouse/keyboard) during tests execution.
Chrome driver is present in `/resources/drivers/chromedriver.exe`

## Configure the machine before running tests
1) Create a folder in your filesystem where the application will use to download files.
As of today it is configured to use the path `D:\leos-test\upload` 
See key `selenium.properties` inside `path.local.download`
2) Run LEOS + Annotations locally

## Run Tests (only a baseline)
Baseline is a subset of the full list of tests to make sure the basic functionalities are ok.
Execution time for the baseline tests to be completed is about 5 minutes.
From `../modules/test-e2e` execute below command:
- `mvn clean install -Dbrowser=chrome -Dmode=local -Denvironment=local -DsuiteXmlFile=E2e_BaseLine_Scenarios.xml`

## Run FULL Tests (not advisable!)
Execution time for the tests to be completed should be around 3 hours.
From `../modules/test-e2e` execute below command:
- `mvn clean install -Dbrowser=chrome -Dmode=local -Denvironment=local -DsuiteXmlFile=RegressionTesting_Commission.xml`

## Clean Test data
The deletion of the single Proposal created from the test is deactivated by default for faster execution.
The file can be removed manually after the tests, or uncomment the line number from 95 to 101 in the file `11_E2e_BaseLine_Scenarios.feature`
to delete it as part of the test suite.

## Test results
On build complete, result will be present in:
`../target/results/cucumber-maven-reports/cucumber-html-reports/overview-features.html`
And the output will look like this:  ![Cucumber Result](./src/test/resources/img/cucumber_result.png)
