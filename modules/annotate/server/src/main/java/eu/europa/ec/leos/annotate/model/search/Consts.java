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
package eu.europa.ec.leos.annotate.model.search;

public final class Consts {

    // -------------------------------------
    // Default values for parameters, (also) used by controller
    // -------------------------------------
    @SuppressWarnings("PMD.LongVariable")
    public static final String DEFAULT_SEARCH_SEPARATE_REPLIES = "false";
    public static final int DEFAULT_SEARCH_LIMIT = 20;
    public static final int DEFAULT_SEARCH_OFFSET = 0;
    public static final String DEFAULT_SEARCH_SORT = "updated";
    public static final String DEFAULT_SEARCH_ORDER = "desc";

    /**
     * search mode to be used
     */
    public enum SearchModelMode {

        /**
         *  normal search
         */
        StandardSearch,

        /**
         *  user is member of the group that is being searched for - requires special filtering, e.g. for ISC search
         */
        ConsiderUserMembership
    }

    /**
     * user type executing a search
     */
    public enum SearchUserType {

        /**
         * EdiT user executing the search
         */
        EdiT,

        /**
         * ISC user executing the search
         */
        ISC,
        
        /**
         * contributor to an ISC procedure executing the search
         */
        Contributor,
        
        /**
         * user type not yet determined
         */
        Unknown
    }

    private Consts() {
        // private constructor to prevent class instantiation
    }
}
