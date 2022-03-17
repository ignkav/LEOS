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
package eu.europa.ec.leos.annotate.model;

/**
 *  enum representing response status of annotation
 */
public enum ResponseStatus {
    UNKNOWN(0), IN_PREPARATION(1), SENT(2);

    private int enumValue;

    ResponseStatus(final int value) {
        this.enumValue = value;
    }

    public int getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(final int enumValue) {
        if (enumValue >= 0 && enumValue <= 2) {
            this.enumValue = enumValue;
        }
    }
}
