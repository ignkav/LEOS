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

public class ExportLW extends ExportOptions {
    
    public static final String PREFIX_LEGISWRITE = "LW_";
    
    public ExportLW(Output exportOutput) {
        this.exportOutput = exportOutput;
        this.withAnnotations = true;
    }
    
    public ExportLW(Output exportOutput, boolean withAnnotations) {
        this.exportOutput = exportOutput;
        this.withAnnotations = withAnnotations;
    }
    
    public ExportLW(String output) {
        switch (output.toLowerCase()) {
            case "pdf":
                exportOutput = Output.PDF;
                break;
            case "lw":
                exportOutput = Output.WORD;
                break;
            default:
                throw new IllegalArgumentException("Wrong value on parameter output type '" + output + "'");
        }
    }
    
    @Override
    public String getWordPrefix() {
        return PREFIX_LEGISWRITE;
    }
    
    @Override
    public String getExportOutputDescription() {
        switch (exportOutput){
            case PDF: return "Pdf";
            case WORD: return "Legiswrite";
            default: return "-";
        }
    }
}
