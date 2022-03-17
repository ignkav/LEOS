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
package eu.europa.ec.leos.domain.cmis.metadata;

import eu.europa.ec.leos.domain.cmis.LeosCategory;

public class ExplanatoryMetadata extends LeosMetadata{
    private final String title;

    public String getTitle() {
        return title;
    }

    public ExplanatoryMetadata(String stage, String type, String purpose, String template, String language, String docTemplate, String ref, String title, String objectId, String docVersion) {
        super(LeosCategory.COUNCIL_EXPLANATORY, stage, type, purpose, template, language, docTemplate, ref, objectId, docVersion);
        this.title = title;
    }

    public final ExplanatoryMetadata withPurpose(String purpose) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }

    public final ExplanatoryMetadata withRef(String ref) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }

    public final ExplanatoryMetadata withObjectId(String objectId) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }

    public final ExplanatoryMetadata withType(String type) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }

    public final ExplanatoryMetadata withTemplate(String template) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }

    public final ExplanatoryMetadata withDocVersion(String docVersion) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }

    public final ExplanatoryMetadata withDocTemplate(String docTemplate) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }

    public final ExplanatoryMetadata withTitle(String title) {
        return new ExplanatoryMetadata(stage, type, purpose, template, language, docTemplate, ref, title, objectId, docVersion);
    }
}