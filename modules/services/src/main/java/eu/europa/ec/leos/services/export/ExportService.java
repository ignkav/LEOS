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
package eu.europa.ec.leos.services.export;

import eu.europa.ec.leos.model.user.User;

import java.io.File;
import java.util.Map;

public interface ExportService {

    String exportToToolboxCoDe(String documentId, ExportOptions exportOptions) throws Exception;

    byte[] exportToToolboxCoDe(File legFile, ExportOptions exportOptions) throws Exception;

    String exportLegPackage(String proposalId, LegPackage legPackage) throws Exception;

    File createCollectionPackage(String jobFileName, String documentId, ExportOptions exportOptions) throws Exception;
    
    byte[] createDocuWritePackage(String jobFileName, String documentId, ExportOptions exportOptions) throws Exception;

    void createDocumentPackage(String jobFileName, String documentId, ExportOptions exportOptions, User user) throws Exception;

    byte[] createExportPackage(String jobFileName, String documentId, ExportOptions exportOptions) throws Exception;

    byte[] updateExportPackageWithComments(String documentId) throws Exception;

    Map<String, byte[]> getExportPackageContent(String documentId, String documentExtension) throws Exception;

}
