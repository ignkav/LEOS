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
package eu.europa.ec.leos.domain.vo;

import java.util.List;
import java.util.Objects;

public class SearchMatchVO {

    private List<ElementMatchVO> matchedElements;
    private boolean replaceable;

    public SearchMatchVO(){
    }

    public SearchMatchVO(List<ElementMatchVO> matchedElements, boolean replaceable) {
        this.matchedElements = matchedElements;
        this.replaceable = replaceable;
    }

    public List<ElementMatchVO> getMatchedElements() {
        return matchedElements;
    }

    public boolean isReplaceable() {
        return replaceable;
    }

    public void setMatchedElements(List<ElementMatchVO> matchedElements) {
        throw new UnsupportedOperationException();// method needed for vaadin reflection to create JSON
    }

    public void setReplaceable(boolean replaceable) {
        throw new UnsupportedOperationException();// method needed for vaadin reflection to create JSON
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchMatchVO that = (SearchMatchVO) o;
        return replaceable == that.replaceable &&
                Objects.equals(matchedElements, that.matchedElements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchedElements, replaceable);
    }

    @Override
    public String toString() {
        return "SearchMatchVO{" +
                "matchedElements=" + matchedElements +
                ", replaceable=" + replaceable +
                '}';
    }
}
