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
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import lombok.var;

/* 	Author: Satyabrata Das
 * 	Functionality: Configuration class to access the configuration properties file values
 */
@Slf4j
public class Configuration {

    private final Properties properties;
    private static final String MAIN_CONFIG_FILE_PATH = Constants.FILTERS + Constants.SLASH + Constants.SELENIUM_PROERTIES;
    private static final String USERS_FILE_PATH = Constants.FILTERS + Constants.SLASH + Constants.USERS_PROERTIES;
    private static final String ENVIRONMENT_CONFIG_FILE_PATH = Constants.FILTERS + Constants.SLASH + Constants.ENV + Constants.SLASH + TestParameters.getInstance().getEnvironment() + ".properties";

    public Configuration() {
        var configBaseFile = new File(Configuration.class.getClassLoader().getResource(MAIN_CONFIG_FILE_PATH).getFile());
        var configUserFile = new File(Configuration.class.getClassLoader().getResource(USERS_FILE_PATH).getFile());
        var environmentConfigFile = new File(Configuration.class.getClassLoader().getResource(ENVIRONMENT_CONFIG_FILE_PATH).getFile());
        try {
            properties = new Properties();
            properties.load(new FileReader(configBaseFile));
            properties.load(new FileReader(configUserFile));
            properties.load(new FileReader(environmentConfigFile));
        } catch (IOException e) {
            throw new DecisionTestExceptions("The config file format is not as expected", e);
        }
    }

    public String getProperty(String value) {
        var propertyValue = getProperty(value, null);
        if (propertyValue != null) {
            return propertyValue;
        } else {
            throw new DecisionTestExceptions(value + " not specified in the selenium.properties file.");
        }
    }

    public String getProperty(String value, String defaultValue) {
        return properties.getProperty(value, defaultValue);
    }
}