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

import eu.europa.ec.leos.domain.cmis.LeosLegStatus;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.vo.LegDocumentVO;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.LegPackage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface LegService {
    
    LegDocument findLastLegByVersionedReference(String path, String versionedReference);

    LegPackage createLegPackage(String proposalId, ExportOptions exportOptions) throws IOException;

    LegPackage createLegPackage(File legFile, ExportOptions exportOptions) throws IOException;
    
    List<LegDocumentVO> getLegDocumentDetailsByUserId(String userId);
    
    LegDocument createLegDocument(String proposalId, String jobId, LegPackage legPackage, LeosLegStatus status) throws IOException;
    
    LegDocument updateLegDocument(String id, LeosLegStatus status);
    
    LegDocument updateLegDocument(String id, byte[] pdfJobZip, byte[] wordJobZip);
    
    LegDocument findLegDocumentById(String id);
    
    /**
     * Finds the Leg document that has jobId and is in the same package with any document that has @documentId.
     *
     * @param documentId the id of a document that is located in the same package as the Leg file
     * @param jobId      the jobId of the Leg document
     * @return the Leg document if found, otherwise null
     */
    LegDocument findLegDocumentByAnyDocumentIdAndJobId(String documentId, String jobId);
    
    List<LegDocument> findLegDocumentByStatus(LeosLegStatus leosLegStatus);
    
    List<LegDocument> findLegDocumentByProposal(String proposalId);
    
    String doubleCompareXmlContents(XmlDocument originalVersion, XmlDocument intermediateMajor, XmlDocument current, boolean isDocuwrite);
    String simpleCompareXmlContents(XmlDocument versionToCompare, XmlDocument currentXmlContent, boolean isDocuwrite);
    
    byte[] updateLegPackageContentWithComments(byte[] legPackageContent, List<String> comments) throws IOException;

    void addFilteredAnnotationsToZipContent(Map<String, Object> contentToZip, String docName, ExportOptions exportOptions);
}
