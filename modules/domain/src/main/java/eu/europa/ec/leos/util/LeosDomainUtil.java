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
package eu.europa.ec.leos.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LeosDomainUtil {

    public final static SimpleDateFormat LEOS_REPO_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    public static Date getLeosDateFromString(String dateStr) {
        return getDateFromString(dateStr, LEOS_REPO_DATE_FORMAT);
    }

    public static Date getDateFromString(String dateStr, SimpleDateFormat sdf) {
        try {
            Date date = null;
            if (dateStr != null) {
                date = sdf.parse(dateStr);
            }
            return date;
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Cannot parse '{}' to date", dateStr), e);
        }
    }

    public static String getLeosDateAsString(Date date) {
        return getLeosDateAsString(date, LEOS_REPO_DATE_FORMAT);
    }

    public static String getLeosDateAsString(Date date, DateFormat df) {
        try {
            String dateAsStr = null;
            if (date != null) {
                dateAsStr = df.format(date);
            }
            return dateAsStr;
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot covert date to string", e);
        }
    }

    public static String wrapXmlFragment(String xmlFragment) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><aknFragment xmlns=\"http://docs.oasis-open.org/legaldocml/ns/akn/3.0\" xmlns:leos=\"urn:eu:europa:ec:leos\">" +
                xmlFragment + "</aknFragment>";
    }

    public static void addFieldIfNotNull(String fieldName, Object value, String LEFT_PAD, String RIGHT_CHAR, StringBuilder sb) {
        if(value != null) {
            sb.append(LEFT_PAD).append(fieldName).append("=").append(value).append(RIGHT_CHAR);
        }
    }

    public static void addListFieldIfNotNull(String fieldName, List<?> value, String LEFT_PAD, String RIGHT_CHAR, StringBuilder sb) {
        if(value != null && value.size() > 0) {
            sb.append(LEFT_PAD).append(fieldName).append("=").append(value).append(RIGHT_CHAR);
        }
    }

    public static String calculateLeftPadd(int deep, String LEFT_CHAR) {
        String calc = "";
        for (int i = 0; i < deep; i++) {
            calc = calc + LEFT_CHAR;
        }
        return calc;
    }
}
