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

public final class InternalGroupName {

    private InternalGroupName() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * computes the internal name of a group, which is URL-safe
     * 
     * @param groupDisplayName
     *        nice name of the group
     * @return URL-safe internal group name
     */
    public static String getInternalGroupName(final String groupDisplayName) {

        // idea: drop all characters not contained in any of the above ranges - this implicitly also removes spaces
        final StringBuilder simpleName = new StringBuilder();
        for (int i = 0; i < groupDisplayName.length(); i++) {

            final int codePoint = Character.codePointAt(groupDisplayName, i);
            if (isUnreservedCharacter(codePoint)) {
                simpleName.append(groupDisplayName.charAt(i));
            }
        }

        return simpleName.toString();
    }

    /**
     * we want to keep it simple and allow those characters denoted "unreserved characters" in section 2.3 of RFC 3986:
     * ALPHA / DIGIT / "-" / "." / "_" / "~"
     * (where we dropped "~")
     * corresponds to character ranges:
     * ALPHA: [a-z] = 97-122, [A-Z] = 65-90
     * DIGIT: [0-9] = 48-57
     * "-" = 45, "." = 46, "_" = 95
     * @param codePoint the character code to analyse
     * @return flag indicating if it is an "allowed character"
     */
    public static boolean isUnreservedCharacter(final int codePoint) {

        return 97 <= codePoint && codePoint <= 122 ||
                65 <= codePoint && codePoint <= 90 ||
                48 <= codePoint && codePoint <= 57 ||
                codePoint == 45 || codePoint == 46 || codePoint == 95;
    }

}
