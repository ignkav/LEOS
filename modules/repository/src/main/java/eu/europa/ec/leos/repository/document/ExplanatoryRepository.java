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

import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import org.springframework.security.access.prepost.PostAuthorize;

import java.util.List;

public interface ExplanatoryRepository {
    /**
     * Creates an [Explanatory] document from a given template and with the specified characteristics.
     *
     * @param templateId the ID of the template for the Explanatory.
     * @param path       the path where to create the Explanatory.
     * @param name       the name of the Explanatory.
     * @param metadata   the metadata of the Explanatory.
     * @return the created Explanatory document.
     */
    Explanatory createExplanatory(String templateId, String path, String name, ExplanatoryMetadata metadata);

    /**
     * Creates an [Explanatory] document from a given content and with the specified characteristics.
     *
     * @param path     the path where to create the Explanatory.
     * @param name     the name of the Explanatory.
     * @param metadata the metadata of the Explanatory.
     * @param content  the content of the Explanatory.
     * @return the created Explanatory document.
     */
    Explanatory createExplanatoryFromContent(String path, String name, ExplanatoryMetadata metadata, byte[] content);

    /**
     * Updates an [Explanatory] document with the given metadata.
     *
     * @param id       the ID of the Explanatory document to update.
     * @param metadata the metadata of the Explanatory.
     * @return the updated Explanatory document.
     */
    Explanatory updateExplanatory(String id, ExplanatoryMetadata metadata);

    /**
     * Updates an [Explanatory] document with the given content.
     *
     * @param id      the ID of the Explanatory document to update.
     * @param content the content of the Explanatory.
     * @param versionType  the version type to be created
     * @param comment the comment of the update, optional.
     * @return the updated Explanatory document.
     */
    Explanatory updateExplanatory(String id, byte[] content, VersionType versionType, String comment);

    /**
     * Updates a [Explanatory] document with the given metadata and content.
     *
     * @param id       the ID of the Explanatory document to update.
     * @param metadata the metadata of the Explanatory.
     * @param content  the content of the Explanatory.
     * @param versionType  the version type to be created
     * @param comment  the comment of the update, optional.
     * @return the updated Explanatory document.
     */
    Explanatory updateExplanatory(String id, ExplanatoryMetadata metadata, byte[] content, VersionType versionType, String comment);

    Explanatory updateMilestoneComments(String id, List<String> milestoneComments, byte[] content, VersionType versionType, String comment);

    Explanatory updateMilestoneComments(String id, List<String> milestoneComments);

    /**
     * Finds a [Explanatory] document with the specified characteristics.
     *
     * @param id     the ID of the Explanatory document to retrieve.
     * @param latest retrieves the latest version of the proposal document, when *true*.
     * @return the found Explanatory document.
     */
    Explanatory findExplanatoryById(String id, boolean latest);

    /**
     * Deletes an [Explanatory] document with the specified characteristics.
     *
     * @param id the ID of the Explanatory document to delete.
     */
    void deleteExplanatory(String id);

    /**
     * Finds all versions of a [Explanatory] document with the specified characteristics.
     *
     * @param id           the ID of the Explanatory document to retrieve.
     * @param fetchContent streams the content
     * @return the list of found Explanatory document versions or empty.
     */
    List<Explanatory> findExplanatoryVersions(String id, boolean fetchContent);

    /**
     * Finds a [Explanatory] document with the specified characteristics.
     *
     * @param ref the reference metadata of the Explanatory document to retrieve.
     * @return the found Explanatory document.
     */
    @PostAuthorize("hasPermission(returnObject, 'CAN_READ')")
    Explanatory findExplanatoryByRef(String ref);

    List<Explanatory> findAllMinorsForIntermediate(String docRef, String curr, int startIndex, int maxResults);

    int findAllMinorsCountForIntermediate(String docRef, String currIntVersion);

    Integer findAllMajorsCount(String docRef);

    List<Explanatory> findAllMajors(String docRef, int startIndex, int maxResult);

    List<Explanatory> findRecentMinorVersions(String documentId, String documentRef, int startIndex, int maxResults);

    Integer findRecentMinorVersionsCount(String documentId, String documentRef);

    Explanatory findFirstVersion(String documentRef);
}
