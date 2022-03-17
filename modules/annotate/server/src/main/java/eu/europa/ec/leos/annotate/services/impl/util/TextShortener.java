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
package eu.europa.ec.leos.annotate.services.impl.util;

public final class TextShortener {

    private TextShortener() {
        // Prevent instantiation as all methods are static.
    }

    public static String getFirstGivenNumberOfCharacters(final String text, final int limit) {

        final int end = Math.min(text.length(), limit);
        return text.substring(0, end);
    }

    public static String getLastGivenNumberOfCharacters(final String text, final int limit) {

        final int start = text.length() - Math.min(limit, text.length());
        return text.substring(start);
    }
}
