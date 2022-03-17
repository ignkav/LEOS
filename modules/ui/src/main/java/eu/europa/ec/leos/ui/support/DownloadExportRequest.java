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
package eu.europa.ec.leos.ui.support;

import eu.europa.ec.leos.services.export.ExportOptions;

public class DownloadExportRequest {
    public enum RequestType {
        DOWNLOAD,
        EXPORT_CLEAN,
        EXPORT
    }

    private final RequestType requestType;
    private final String title;
    private final ExportOptions exportOptions;

    public DownloadExportRequest(RequestType requestType, String title, ExportOptions exportOptions) {
        this.requestType = requestType;
        this.title = title;
        this.exportOptions = exportOptions;
    }

    public String getTitle() {
        return title;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public ExportOptions getExportOptions() {
        return exportOptions;
    }
}

