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

import java.util.ArrayList;
import java.util.List;

/**
 *  enum representing the "existence status" of the annotation
 */
public enum AnnotationStatus {

    // probably not needed, but let's have it as a bit-mask
    // maybe using https://stackoverflow.com/questions/1414755/can-enums-be-subclassed-to-add-new-elements we could design it nicer
    NORMAL(0), DELETED(1), ACCEPTED(2), REJECTED(4), ALL(7);

    private int enumValue;

    AnnotationStatus(final int value) {
        this.enumValue = value;
    }

    public int getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(final int enumValue) {
        if (enumValue >= 0 && enumValue <= ALL.getEnumValue()) {
            this.enumValue = enumValue;
        }
    }
    
    public static List<AnnotationStatus> getAllValues() {
        final List<AnnotationStatus> allStatuses = new ArrayList<>();
        allStatuses.add(NORMAL);
        allStatuses.add(DELETED);
        allStatuses.add(ACCEPTED);
        allStatuses.add(REJECTED);
        return allStatuses;
    }
    
    public static List<AnnotationStatus> getDefaultStatus() {
        final List<AnnotationStatus> allStatuses = new ArrayList<>();
        allStatuses.add(NORMAL);
        return allStatuses;
    }

    public static List<AnnotationStatus> getNonDeleted() {
        final List<AnnotationStatus> allNonDeleted = new ArrayList<>();
        allNonDeleted.add(NORMAL);
        allNonDeleted.add(ACCEPTED);
        allNonDeleted.add(REJECTED);
        return allNonDeleted;
    }
}
