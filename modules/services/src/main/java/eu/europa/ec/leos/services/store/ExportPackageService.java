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
package eu.europa.ec.leos.services.store;

import eu.europa.ec.leos.domain.cmis.LeosExportStatus;
import eu.europa.ec.leos.domain.cmis.document.ExportDocument;

import java.util.List;

public interface ExportPackageService {

    ExportDocument findExportDocumentById(String id);

    ExportDocument findExportDocumentById(String id, boolean latest);

    ExportDocument createExportDocument(String proposalId, List<String> comments, byte[] content);

    ExportDocument updateExportDocument(String id, byte[] content);

    ExportDocument updateExportDocument(String id, LeosExportStatus status);

    ExportDocument updateExportDocument(String id, List<String> comments);

    void deleteExportDocument(String id);
}
