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

import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;

import java.util.List;

public abstract class ExportOptions {
    
    public static final String PREFIX_PDF = "PDF_";
    
    protected ExportVersions exportVersions;
    protected Output exportOutput;
    protected boolean withAnnotations;
    protected boolean withFilteredAnnotations;
    protected String filteredAnnotations;
    protected Class fileType;
    protected boolean isCleanVersion;
    protected boolean isDocuwrite;
    protected RelevantElements relevantElements;
    protected List<String> comments;

    public boolean isComparisonMode() {
        return getComparisonType() != ComparisonType.NONE;
    }

    public boolean isCleanVersion() {
        return isCleanVersion;
    }

    public enum Output {
        PDF,
        WORD
    }
    
    public ExportVersions getExportVersions() {
        return exportVersions;
    }
    
    public void setExportVersions(ExportVersions exportVersions) {
        this.exportVersions = exportVersions;
    }
    
    public void setWithAnnotations(boolean withAnnotations) {
        this.withAnnotations = withAnnotations;
    }

    public void setWithFilteredAnnotations(boolean withFilteredAnnotations) {
        this.withAnnotations = withFilteredAnnotations;
        this.withFilteredAnnotations = withFilteredAnnotations;
    }

    public void setFilteredAnnotations(String filteredAnnotations) {
        this.filteredAnnotations = filteredAnnotations;
    }

    public boolean isWithAnnotations() {
        return withAnnotations;
    }

    public boolean isWithFilteredAnnotations() {
        return withFilteredAnnotations;
    }

    public String getFilteredAnnotations() {
        return filteredAnnotations;
    }

    protected abstract String getWordPrefix();
    
    public String getFilePrefix() {
        if (exportOutput == Output.PDF) {
            return PREFIX_PDF;
        } else if (exportOutput == Output.WORD) {
            return getWordPrefix();
        } else {
            throw new IllegalStateException("Not possible!!");
        }
    }
    
    public String getFileName(){
        String fileName = getFileType().getSimpleName().toUpperCase();
        if(getFileType().equals(Bill.class)){
            fileName = "LEGALTEXT";
        }
        return fileName;
    }
    
    public Class getFileType() {
        if(fileType == null){
            return XmlDocument.class;
        }
        return fileType;
    }
    
    public String getTechnicalKey(){
        return "";
    }
    
    public abstract String getExportOutputDescription();
    
    public Output getExportOutput() {
        return exportOutput;
    }
    
    public ComparisonType getComparisonType() {
        if (exportVersions == null) {
            return ComparisonType.NONE;
        }
        
        switch (exportVersions.getVersionsPresent()) {
            case 3:
                return ComparisonType.DOUBLE;
            case 2:
                return ComparisonType.SIMPLE;
            default:
                return ComparisonType.NONE;
        }
    }

    public boolean isWithRelevantElements() { return this.relevantElements != null; }

    public RelevantElements getRelevantElements() { return this.relevantElements; }

    public void setRelevantElements(RelevantElements relevantElements) { this.relevantElements = relevantElements; }

    public List<String> getComments() { return comments; }

    public void setComments(List<String> comments) { this.comments = comments; }

    public boolean isDocuwrite() {
        return isDocuwrite;
    }

    public void setDocuwrite(boolean docuwrite) {
        isDocuwrite = docuwrite;
    }
}
