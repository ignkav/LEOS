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
package eu.europa.ec.leos.ui.event;

import eu.europa.ec.leos.services.export.RelevantElements;

public class CreateExportPackageCleanVersionRequestEvent {

    private final String title;
    private final RelevantElements relevantElements;
    private final Boolean isWithAnnotations;

    public CreateExportPackageCleanVersionRequestEvent(String title, RelevantElements relevantElements, Boolean isWithAnnotations) {
        this.title = title;
        this.relevantElements = relevantElements;
        this.isWithAnnotations = isWithAnnotations;
    }

    public String getTitle() { return this.title; }

    public RelevantElements getRelevantElements() { return this.relevantElements; }

    public Boolean isWithAnnotations() {
        return isWithAnnotations;
    }
}