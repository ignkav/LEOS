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
package eu.europa.ec.leos.repository.document;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.repository.LeosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExplanatoryRepositoryImpl implements ExplanatoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(ExplanatoryRepositoryImpl.class);

    private final LeosRepository leosRepository;

    @Autowired
    public ExplanatoryRepositoryImpl(LeosRepository leosRepository) {
        this.leosRepository = leosRepository;
    }

    @Override
    public Explanatory createExplanatory(String templateId, String path, String name, ExplanatoryMetadata metadata) {
        logger.debug("Creating Explanatory... [template=" + templateId + ", path=" + path + ", name=" + name + "]");
        return leosRepository.createDocument(templateId, path, name, metadata, Explanatory.class);
    }

    @Override
    public Explanatory createExplanatoryFromContent(String path, String name, ExplanatoryMetadata metadata, byte[] content) {
        logger.debug("Creating Explanatory From Content... [tpath=" + path + ", name=" + name + "]");
        return leosRepository.createDocumentFromContent(path, name, metadata, Explanatory.class,
                LeosCategory.COUNCIL_EXPLANATORY.name(), content);
    }

    @Override
    public Explanatory updateExplanatory(String id, ExplanatoryMetadata metadata) {
        logger.debug("Updating Explanatory metadata... [id=" + id + "]");
        return leosRepository.updateDocument(id, metadata, Explanatory.class);
    }

    @Override
    public Explanatory updateExplanatory(String id, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating Explanatory content... [id=" + id + "]");
        return leosRepository.updateDocument(id, content, versionType, comment, Explanatory.class);
    }

    @Override
    public Explanatory updateExplanatory(String id, ExplanatoryMetadata metadata, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating Explanatory metadata and content... [id=" + id + "]");
        return leosRepository.updateDocument(id, metadata, content, versionType, comment, Explanatory.class);
    }

    @Override
    public Explanatory updateMilestoneComments(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment) {
        logger.debug("Updating Explanatory milestoneComments and content... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, content, milestoneComments, versionType, comment, Explanatory.class);
    }

    @Override
    public Explanatory updateMilestoneComments(String id, List<String> milestoneComments) {
        logger.debug("Updating Explanatory milestoneComments... [id=" + id + "]");
        return leosRepository.updateMilestoneComments(id, milestoneComments, Explanatory.class);
    }

    @Override
    public Explanatory findExplanatoryById(String id, boolean latest) {
        logger.debug("Finding Explanatory by ID... [id=" + id + ", latest=" + latest + "]");
        return leosRepository.findDocumentById(id, Explanatory.class, latest);
    }

    @Override
    public void deleteExplanatory(String id) {
        logger.debug("Deleting Explanatory... [id=" + id + "]");
        leosRepository.deleteDocumentById(id);
    }

    @Override
    public List<Explanatory> findExplanatoryVersions(String id, boolean fetchContent) {
        logger.debug("Finding Explanatory versions... [id=" + id + "]");
        return leosRepository.findDocumentVersionsById(id, Explanatory.class, fetchContent);
    }

    @Override
    public Explanatory findExplanatoryByRef(String ref) {
        logger.debug("Finding Explanatory by ref... [ref=" + ref + "]");
        return leosRepository.findDocumentByRef(ref, Explanatory.class);
    }

    @Override
    public List<Explanatory> findAllMinorsForIntermediate(String docRef, String currIntVersion, int startIndex, int maxResults) {
        logger.debug("Finding Explanatory versions between intermediates...");
        return leosRepository.findAllMinorsForIntermediate(Explanatory.class, docRef, currIntVersion, startIndex, maxResults);
    }

    @Override
    public int findAllMinorsCountForIntermediate(String docRef, String currIntVersion) {
        logger.debug("Finding Explanatory minor versions count between intermediates...");
        return leosRepository.findAllMinorsCountForIntermediate(Explanatory.class, docRef, currIntVersion);
    }

    @Override
    public Integer findAllMajorsCount(String docRef) {
        return leosRepository.findAllMajorsCount(Explanatory.class, docRef);
    }

    @Override
    public List<Explanatory> findAllMajors(String docRef, int startIndex, int maxResult) {
        return leosRepository.findAllMajors(Explanatory.class, docRef, startIndex, maxResult);
    }

    @Override
    public List<Explanatory> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults) {
        final Explanatory explanatory = leosRepository.findLatestMajorVersionById(Explanatory.class, documentId);
        return leosRepository.findRecentMinorVersions(Explanatory.class, documentRef, explanatory.getCmisVersionLabel(), startIndex, maxResults);
    }

    @Override
    public Integer findRecentMinorVersionsCount(String documentId, String documentRef) {
        final Explanatory explanatory = leosRepository.findLatestMajorVersionById(Explanatory.class, documentId);
        return leosRepository.findRecentMinorVersionsCount(Explanatory.class, documentRef, explanatory.getCmisVersionLabel());
    }

    @Override
    public Explanatory findFirstVersion(String documentRef) {
        return leosRepository.findFirstVersion(Explanatory.class, documentRef);
    }
}
