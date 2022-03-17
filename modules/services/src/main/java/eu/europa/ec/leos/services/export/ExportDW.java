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

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;

public class ExportDW extends ExportOptions {
    
    public static final String PREFIX_DOCUWRITE = "DW_";
    
    public ExportDW(Output exportOutput, boolean withAnnotations) {
        this.exportOutput = exportOutput;
        this.withAnnotations = withAnnotations;
    }
    
    public ExportDW(Output exportOutput) {
        this.exportOutput = exportOutput;
        this.withAnnotations = true;
    }
    
    public <T extends XmlDocument> ExportDW(Output exportOutput, Class<T> fileType, boolean withAnnotations) {
        this.exportOutput = exportOutput;
        this.fileType = fileType;
        this.withAnnotations = withAnnotations;
    }

    public <T extends XmlDocument> ExportDW(Output exportOutput, Class<T> fileType, boolean withAnnotations, boolean withCleanVersion) {
        this.exportOutput = exportOutput;
        this.fileType = fileType;
        this.withAnnotations = withAnnotations;
        this.isCleanVersion = withCleanVersion;
    }
    
    @Override
    public String getWordPrefix() {
        return PREFIX_DOCUWRITE;
    }
    
    @Override
    public String getExportOutputDescription() {
        switch (exportOutput){
            case PDF: return "Pdf";
            case WORD: return "DocuWrite";
            default: return "-";
        }
    }
    
}
