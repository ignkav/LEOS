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

import io.cucumber.java.Scenario;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.Data;
import lombok.Getter;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)

public class TestParameters {

    @Getter(lazy = true)
    private static final TestParameters instance = new TestParameters();
    private String screenshotPath;
    private String environment;
    private String browser;
    private String mode;
    private XSSFWorkbook testDataFile;
    private Scenario scenario;

    public void reset() {
        screenshotPath = null;
        scenario = null;
    }
}