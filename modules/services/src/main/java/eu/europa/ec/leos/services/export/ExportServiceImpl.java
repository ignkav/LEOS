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

import eu.europa.ec.leos.integration.ToolBoxService;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.content.processor.TransformationService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

abstract class ExportServiceImpl implements ExportService {

    protected final LegService legService;
    protected final PackageService packageService;
    protected ToolBoxService toolBoxService;
    protected final SecurityContext securityContext;
    protected final ExportHelper exportHelper;
    protected final BillService billService;
    protected final AnnexService annexService;
    protected final TransformationService transformationService;

    @Autowired
    ExportServiceImpl(LegService legService, PackageService packageService, SecurityContext securityContext, ExportHelper exportHelper,
            BillService billService, AnnexService annexService, TransformationService transformationService) {
        this.legService = legService;
        this.packageService = packageService;
        this.securityContext = securityContext;
        this.exportHelper = exportHelper;
        this.billService = billService;
        this.annexService = annexService;
        this.transformationService = transformationService;
    }

    @Override
    public byte[] createDocuWritePackage(String jobFileName, String documentId, ExportOptions exportOptions) throws Exception{
        return null;
    }

    @Override
    public void createDocumentPackage(String jobFileName, String documentId, ExportOptions exportOptions, User user) throws Exception{
        return;
    }

    @Override
    public byte[] createExportPackage(String jobFileName, String documentId, ExportOptions exportOptions) throws Exception{
        return null;
    }

    @Override
    public byte[] updateExportPackageWithComments(String documentId) throws Exception {
        return null;
    }

    @Override
    public Map<String, byte[]> getExportPackageContent(String documentId, String documentExtension) throws Exception {
        return null;
    }

    @Override
    public String exportToToolboxCoDe(String documentId, ExportOptions exportOptions) throws Exception {
        return null;
    }

    @Override
    public byte[] exportToToolboxCoDe(File legFile, ExportOptions exportOptions) throws Exception {
        return null;
    }

    @Override
    public String exportLegPackage(String proposalId, LegPackage legPackage) throws Exception {
        return null;
    }

    @Override
    public File createCollectionPackage(String jobFileName, String documentId, ExportOptions exportOptions) throws Exception {
        Validate.notNull(jobFileName);
        Validate.notNull(exportOptions);
        Validate.notNull(documentId);
        File legFile = null;
        try {
            LegPackage legPackage = legService.createLegPackage(documentId, exportOptions);
            legFile = legPackage.getFile();
            return createZipFile(legPackage, jobFileName, exportOptions);
        } finally {
            if (legFile != null && legFile.exists()) {
                legFile.delete();
            }
        }
    }

    protected File createZipFile(LegPackage legPackage, String jobFileName, ExportOptions exportOptions) throws Exception{
        Validate.notNull(legPackage);
        Validate.notNull(jobFileName);
        Validate.notNull(exportOptions);
        try (ByteArrayOutputStream contentFileContent = exportHelper.createContentFile(exportOptions, legPackage.getExportResource())) {
            Map<String, Object> contentToZip = new HashMap<>();
            contentToZip.put("content.xml", contentFileContent);
            contentToZip.put(legPackage.getFile().getName(), legPackage.getFile());
            return ZipPackageUtil.zipFiles(jobFileName, contentToZip);
        }
    }
}
