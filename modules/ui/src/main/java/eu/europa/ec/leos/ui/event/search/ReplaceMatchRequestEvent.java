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
package eu.europa.ec.leos.ui.event.search;

import eu.europa.ec.leos.domain.vo.SearchMatchVO;

public class ReplaceMatchRequestEvent {

    private final Long searchId;
    private final String searchText;
    private final String replaceText;
    private final SearchMatchVO searchMatchVO;
    private final int matchIndex;

    public ReplaceMatchRequestEvent(Long searchId,
        String searchText, String replaceText, SearchMatchVO searchMatchVO, int matchIndex) {
        this.searchId = searchId;
        this.searchText = searchText;
        this.replaceText = replaceText;
        this.searchMatchVO = searchMatchVO;
        this.matchIndex = matchIndex;
    }

    public Long getSearchId() {
        return searchId;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getReplaceText() {
        return replaceText;
    }

    public SearchMatchVO getSearchMatchVO() {
        return searchMatchVO;
    }

    public int getMatchIndex() {
        return matchIndex;
    }
}
