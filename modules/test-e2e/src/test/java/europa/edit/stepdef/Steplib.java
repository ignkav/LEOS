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

import org.openqa.selenium.WebDriver;
import europa.edit.util.Configuration;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Steplib {

    //private static final int TIMEOUT = 10;
    //private static final int TIMEOUTDELAY = 30;
    private final Configuration config = new Configuration();

    public void startApp(WebDriver driver, String applicationType) {
        String applicationURL = getAppUrl(applicationType);
        if (!driver.getCurrentUrl().trim().contains(applicationURL)) {
            driver.get(applicationURL);
        }
    }
    private String getAppUrl(String applicationType) {
        String appUrl = "";
        if (applicationType.equalsIgnoreCase("Commission")) {
            appUrl = config.getProperty("edit.appUrl.ec");
        }
        if (applicationType.equalsIgnoreCase("Council")) {
            appUrl = config.getProperty("edit.appUrl.cn");
        }
        logger.info("Open application {} url {}", applicationType, appUrl);
        return appUrl;
    }
}
