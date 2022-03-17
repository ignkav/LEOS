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

import java.io.File;

public class Constants {
    public static final int BUFFER_SIZE = 4096;
    public static final String FILE_SEPARATOR = "\\";
    public static final String SELENIUM_PROERTIES = "selenium.properties";
    public static final String USERS_PROERTIES = "users.properties";
    public static final String FILTERS = "filters";
    public static final String DRIVERS = "drivers";
    public static final String CHROMEDRIVER = "chromedriver.exe";
    public static final String ENV = "env";
    public static final String SLASH = "/";
    public static final String TEST_RESOURCES_LOCATION = "src" + File.separator + "test" + File.separator + "resources";
    public static final String DATATABLE_LOCATION = TEST_RESOURCES_LOCATION + File.separator + "datatables";
    public static final String RESULTS_LOCATION = "target" + File.separator + "results";
}
