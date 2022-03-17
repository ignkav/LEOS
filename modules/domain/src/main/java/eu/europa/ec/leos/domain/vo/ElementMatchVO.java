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

import java.util.Objects;

public class ElementMatchVO {
    private String elementId;
    private int matchStartIndex;
    private int matchEndIndex;
    private boolean isEditable;

    public ElementMatchVO() {
    }

    public ElementMatchVO(String elementId, int matchStartIndex) {
        this.elementId = elementId;
        this.matchStartIndex = matchStartIndex;
    }

    public ElementMatchVO(String elementId, int matchStartIndex, int matchEndIndex) {
        this.elementId = elementId;
        this.matchStartIndex = matchStartIndex;
        this.matchEndIndex = matchEndIndex;
    }

    public ElementMatchVO(String elementId, int matchStartIndex, boolean isEditable) {
        this.elementId = elementId;
        this.matchStartIndex = matchStartIndex;
        this.isEditable = isEditable;
    }
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {// method needed for vaadin reflection to create JSON
        throw new UnsupportedOperationException();
    }

    public int getMatchStartIndex() {
        return matchStartIndex;
    }

    public void setMatchStartIndex(int matchStartIndex) {
        this.matchStartIndex = matchStartIndex;
    }

    public int getMatchEndIndex() {
        return matchEndIndex;
    }

    public void setMatchEndIndex(int matchEndIndex) {
        this.matchEndIndex = matchEndIndex;
    }

    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElementMatchVO that = (ElementMatchVO) o;
        return matchStartIndex == that.matchStartIndex &&
                matchEndIndex == that.matchEndIndex &&
                Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, matchStartIndex, matchEndIndex);
    }

    @Override
    public String toString() {
        return "ElementMatchVO{" +
                "elementId='" + elementId + '\'' +
                ", matchStartIndex=" + matchStartIndex +
                ", matchEndIndex=" + matchEndIndex +
                '}';
    }
}
