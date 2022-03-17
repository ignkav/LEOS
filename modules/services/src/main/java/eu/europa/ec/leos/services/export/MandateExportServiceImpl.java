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

import eu.europa.ec.leos.domain.cmis.document.ExportDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.integration.DocuWriteService;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.content.processor.TransformationService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.store.ExportPackageService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Instance(InstanceType.COUNCIL)
public class MandateExportServiceImpl extends ExportServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(MandateExportServiceImpl.class);

    protected final DocuWriteService docuwriteService;
    protected final ExportPackageService exportPackageService;
    
    @Autowired
    MandateExportServiceImpl(LegService legService, PackageService packageService, SecurityContext securityContext,
                             ExportHelper exportHelper, DocuWriteService docuwriteService,
                             BillService billService, AnnexService annexService,
                             TransformationService transformationService, ExportPackageService exportPackageService) {
        super(legService, packageService, securityContext, exportHelper, billService, annexService, transformationService);
        this.docuwriteService = docuwriteService;
        this.exportPackageService = exportPackageService;
    }
    
    @Override
    public byte[] createDocuWritePackage(String jobFileName, String proposalId, ExportOptions exportOptions) throws Exception {
        LOG.trace("calling createDocuWritePackage()....");
        Validate.notNull(exportOptions);
        Validate.notNull(proposalId);
        LegPackage legPackage = null;
        File zipFile = null;
        try {
            exportOptions.setDocuwrite(true);
            legPackage = legService.createLegPackage(proposalId, exportOptions);
            zipFile = createZipFile(legPackage, jobFileName, exportOptions);
            byte[] docuwriteResponse = docuwriteService.convert(zipFile);
            return docuwriteResponse;
        } catch(Exception e) {
            LOG.error("An exception occurred while using the Docuwrite service: ", e);
            throw e;
        } finally {
            if(legPackage.getFile() != null && legPackage.getFile().exists()) {
                legPackage.getFile().delete();
            }
            if(zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
            LOG.trace("createDocuWritePackage() end....");
        }
    }

    @Override
    public byte[] createExportPackage(String jobFileName, String proposalId, ExportOptions exportOptions) throws Exception {
        LOG.trace("calling createExportPackage()....");
        Validate.notNull(exportOptions);
        Validate.notNull(proposalId);
        LegPackage legPackage = null;
        File zipFile = null;
        File exportPackageZipFile = null;
        try {
            legPackage = legService.createLegPackage(proposalId, exportOptions);
            zipFile = createZipFile(legPackage, jobFileName, exportOptions);
            byte[] docuwriteResponse = docuwriteService.convert(zipFile);
            exportPackageZipFile = createExportPackageZipFile(jobFileName, legPackage, docuwriteResponse);
            return FileUtils.readFileToByteArray(exportPackageZipFile);
        } catch(Exception e) {
            LOG.error("An exception occurred while using the export package service: ", e);
            if (ExceptionUtils.indexOfThrowable(e, java.net.UnknownHostException.class) != -1) {
                return FileUtils.readFileToByteArray(zipFile);
            }
            throw e;
        } finally {
            if(legPackage.getFile() != null && legPackage.getFile().exists()) {
                legPackage.getFile().delete();
            }
            if(zipFile != null && zipFile.exists()) {
                zipFile.delete();
            }
            if ((exportPackageZipFile != null) && (exportPackageZipFile.exists())) {
                exportPackageZipFile.delete();
            }
            LOG.trace("createExportPackage() end....");
        }
    }

    private File createExportPackageZipFile(String jobFileName, LegPackage legPackage, byte[] docuwriteResponse) throws Exception {
        Map<String, Object> exportPackageZipContent = ZipPackageUtil.unzipByteArray(docuwriteResponse);
        exportPackageZipContent.put(legPackage.getFile().getName(), legPackage.getFile());
        return ZipPackageUtil.zipFiles(jobFileName, exportPackageZipContent);
    }

    @Override
    public byte[] updateExportPackageWithComments(String documentId) throws Exception {
        LOG.trace("calling updateExportPackageWithComments()....");
        File exportPackageZipFile = null;
        try {
            ExportDocument exportDocument = exportPackageService.findExportDocumentById(documentId);
            Map<String, Object> exportPackageZipContent = ZipPackageUtil.unzipByteArray(exportDocument.getContent().get().getSource().getBytes());
            Map.Entry<String, Object> legPackage = exportPackageZipContent.entrySet().stream().filter(x -> x.getKey().endsWith(".leg"))
                    .findAny().orElseThrow(() -> new RuntimeException("No leg file inside export package!"));
            byte[] legPackageContentUpdated = legService.updateLegPackageContentWithComments((byte[])legPackage.getValue(), exportDocument.getComments());
            exportPackageZipContent.put(legPackage.getKey(), legPackageContentUpdated);
            exportPackageZipFile = ZipPackageUtil.zipFiles(System.currentTimeMillis() + "_" + exportDocument.getName(), exportPackageZipContent);
            return FileUtils.readFileToByteArray(exportPackageZipFile);
        } catch(Exception e) {
            LOG.error("An exception occurred while using the export package service: ", e);
            throw e;
        } finally {
            if ((exportPackageZipFile != null) && (exportPackageZipFile.exists())) {
                exportPackageZipFile.delete();
            }
            LOG.trace("updateExportPackageWithComments() end....");
        }
    }

    @Override
    public Map<String, byte[]> getExportPackageContent(String documentId, String documentExtension) throws Exception {
        ExportDocument exportDocument = exportPackageService.findExportDocumentById(documentId);
        Map<String, Object> exportPackageZipContent = ZipPackageUtil.unzipByteArray(exportDocument.getContent().get().getSource().getBytes());
        Map.Entry<String, Object> docuwriteResponse = exportPackageZipContent.entrySet().stream().filter(x -> x.getKey().endsWith(documentExtension))
                .findAny().orElseThrow(() -> new RuntimeException("No document inside export package!"));
        Map<String, byte[]> exportPackageContent = new HashMap<>();
        exportPackageContent.put(docuwriteResponse.getKey(), (byte[])docuwriteResponse.getValue());
        return exportPackageContent;
    }
}
