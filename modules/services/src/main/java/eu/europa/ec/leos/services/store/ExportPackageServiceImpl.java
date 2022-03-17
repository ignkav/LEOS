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

import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.LeosExportStatus;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.ExportDocument;
import eu.europa.ec.leos.repository.store.PackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportPackageServiceImpl implements ExportPackageService {
    private static final Logger LOG = LoggerFactory.getLogger(ExportPackageServiceImpl.class);

    private final PackageRepository packageRepository;

    private static final String EXPORT_FILE_PREFIX = "export_";
    private static final String EXPORT_FILE_EXTENSION = ".zip";

    @Autowired
    ExportPackageServiceImpl(PackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    @Override
    public ExportDocument findExportDocumentById(String id) {
        LOG.trace("Finding Export document by id... [documentId={}]", id);
        return packageRepository.findExportDocumentById(id, true);
    }

    @Override
    public ExportDocument findExportDocumentById(String id, boolean latest) {
        LOG.trace("Finding Export document by id and latest.. [documentId={}, latest={}]", id, latest);
        return packageRepository.findExportDocumentById(id, latest);
    }

    @Override
    public ExportDocument createExportDocument(String proposalId, List<String> comments, byte[] content) {
        LOG.trace("Creating Export Document for Package... [documentId={}]", proposalId);
        return packageRepository.createExportDocumentFromContent(
                packageRepository.findPackageByDocumentId(proposalId).getPath(),
                generateExportName(), comments, content, LeosExportStatus.FILE_READY);
    }

    @Override
    public ExportDocument updateExportDocument(String id, byte[] content) {
        LOG.trace("Updating Export document with id={} status to {} and content ", id, LeosExportStatus.FILE_READY.name());
        return packageRepository.updateExportDocument(id, LeosExportStatus.FILE_READY, content,
                VersionType.MINOR, "Export package updated");
    }

    @Override
    public ExportDocument updateExportDocument(String id, LeosExportStatus status) {
        LOG.trace("Updating Export document status... [id={}, status={}]", id, status.name());
        return packageRepository.updateExportDocument(id, status);
    }

    @Override
    public ExportDocument updateExportDocument(String id, List<String> comments) {
        LOG.trace("Updating Export document comments... [id={}, comments={}]", id, comments);
        return packageRepository.updateExportDocument(id, comments);
    }

    @Override
    public void deleteExportDocument(String id) {
        LOG.trace("Deleting Export document... [id={}]", id);
        packageRepository.deleteExportDocument(id);
    }

    private String generateExportName() {
        return EXPORT_FILE_PREFIX + Cuid.createCuid() + EXPORT_FILE_EXTENSION;
    }
}
