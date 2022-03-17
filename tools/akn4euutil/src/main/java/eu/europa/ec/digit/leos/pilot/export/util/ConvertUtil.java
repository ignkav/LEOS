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
package eu.europa.ec.digit.leos.pilot.export.util;

import eu.europa.ec.digit.leos.pilot.export.model.ConvertDocumentInput;

public class ConvertUtil {

    private static final String DOT = ".";

    public static String replaceSuffix(String inputString, String suffix) {
        if (inputString == null) {
            throw new IllegalArgumentException("inputString is null");
        }
        if (suffix == null) {
            if (inputString.contains(DOT)) {
                return inputString.substring(0, inputString.lastIndexOf(DOT));
            } else {
                return inputString;
            }
        }
        if (inputString.contains(DOT)) {
            return inputString.substring(0, inputString.lastIndexOf(DOT)) + DOT + suffix;
        } else {
            return inputString + DOT + suffix;
        }
    }

    public static String getFilename(ConvertDocumentInput convertDocumentInput, String suffix) {
        String s = convertDocumentInput.getInputFile().getOriginalFilename();
        if (s != null) {
            return replaceSuffix(s, suffix);
        } else {
            return "noname";
        }
    }
}
