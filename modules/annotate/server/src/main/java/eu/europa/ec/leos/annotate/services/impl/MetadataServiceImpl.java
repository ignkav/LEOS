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
package eu.europa.ec.leos.annotate.services.impl;

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Document;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.model.web.StatusUpdateRequest;
import eu.europa.ec.leos.annotate.repository.MetadataRepository;
import eu.europa.ec.leos.annotate.services.AnnotationPermissionService;
import eu.europa.ec.leos.annotate.services.DocumentService;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.MetadataMatchingService;
import eu.europa.ec.leos.annotate.services.MetadataService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateMetadataException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotUpdateAnnotationStatusException;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetadataServiceImpl implements MetadataService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataServiceImpl.class);

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private AnnotationPermissionService annotPermService;

    @Autowired
    private MetadataMatchingService metadataMatchingService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private MetadataRepository metadataRepos;

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    @Nonnull
    public List<Metadata> findMetadataOfDocumentGroupSystemid(final Document document, final Group group, final String systemId) {

        return metadataRepos.findByDocumentAndGroupAndSystemId(document, group, systemId);
    }

    @Override
    @Nonnull
    public List<Metadata> findMetadataOfDocumentGroupSystemid(final String docUri, final String groupName, final String systemId) {

        final Document document = documentService.findDocumentByUri(docUri);
        if (document == null) {
            LOG.debug("No document found -> no metadata found");
            return new ArrayList<Metadata>();
        }

        final Group group = groupService.findGroupByName(groupName);
        if (group == null) {
            LOG.debug("No group found -> no metadata found");
            return new ArrayList<Metadata>();
        }

        return findMetadataOfDocumentGroupSystemid(document, group, systemId);
    }

    @Override
    @Nonnull
    public List<Metadata> findMetadataOfDocumentGroupSystemidSent(final Document document, final Group group, final String systemId) {

        return metadataRepos.findByDocumentAndGroupAndSystemIdAndResponseStatus(document, group, systemId, ResponseStatus.SENT);
    }

    @Override
    @Nonnull
    public List<Metadata> findMetadataOfDocumentSystemidGroupIds(final Document document, final String systemId, final List<Long> groupIds) {

        // null will provoke a Hibernate exception - we return an exception before, which is better understandable
        Assert.notNull(groupIds, "groupIds must not be null");

        return metadataRepos.findByDocumentAndSystemIdAndGroupIdIsIn(document, systemId, groupIds);
    }

    @Override
    @Nonnull
    public List<Metadata> findMetadataOfDocumentSystemidSent(final Document document, final String systemId) {

        return metadataRepos.findByDocumentAndSystemIdAndResponseStatus(document, systemId, ResponseStatus.SENT);
    }

    @Override
    @Nonnull
    public List<Metadata> findMetadataOfDocumentGroupSystemidInPreparation(final Document document, final Group group, final String systemId) {

        return metadataRepos.findByDocumentAndGroupAndSystemIdAndResponseStatus(document, group, systemId, ResponseStatus.IN_PREPARATION);
    }

    @Override
    public Metadata saveMetadata(final Metadata metadata) throws CannotCreateMetadataException {

        if (metadata == null) {
            LOG.error("Received metadata for saving is null!");
            throw new CannotCreateMetadataException(new IllegalArgumentException("Metadata is null"));
        }

        Metadata modifiedMetadata;
        try {
            modifiedMetadata = metadataRepos.save(metadata);
        } catch (Exception e) {
            LOG.error("Exception upon saving metadata");
            throw new CannotCreateMetadataException(e);
        }

        return modifiedMetadata;
    }

    @Override
    public void deleteMetadataById(final long metadataId) {

        try {
            metadataRepos.delete(metadataId);
        } catch (Exception e) {
            LOG.error("Could not delete metadata set with ID " + metadataId, e);
        }
    }

    @Override
    @Nonnull
    public List<Metadata> updateMetadata(final StatusUpdateRequest updateRequest, 
            final UserInformation userInfo,
            final LocalDateTime timestamp)
            throws CannotUpdateAnnotationStatusException, MissingPermissionException {

        if (userInfo == null) {
            throw new CannotUpdateAnnotationStatusException(new IllegalArgumentException("userInfo is null"));
        }

        // create a Metadata instance from given parameters - throws exception if required mandatory information is missing
        @SuppressWarnings("PMD.PrematureDeclaration") // variable is declared here to throw exception early, if needed
        final Metadata metaFromUpdateRequest = createMetadataFromStatusUpdateRequest(updateRequest);

        if (!annotPermService.userMayUpdateMetadata(userInfo)) {
            throw new MissingPermissionException(String.format("%s (%s)", userInfo.getLogin(), userInfo.getAuthority()));
        }

        // finally search for the items requiring to be updated
        // first step: get all candidates associated to document, group and system ID
        final String authority = userInfo.getAuthority();
        final List<Metadata> metaCandidates = findMetadataOfDocumentGroupSystemid(metaFromUpdateRequest.getDocument(), metaFromUpdateRequest.getGroup(),
                authority);
        if (metaCandidates.isEmpty()) {
            LOG.warn("No annotations are assigned to the group, document, authority/systemId; thus no metadata can be updated");
            return new ArrayList<Metadata>(); // we don't throw an exception any more since the caller might have more tasks to execute
        }

        // second step: keep only those having at least the required metadata
        final List<Metadata> filteredMetadata = metaCandidates.stream()
                .filter(meta -> metadataMatchingService.areAllMetadataContainedInDbMetadata(updateRequest.getMetadataToMatch(), meta))
                .collect(Collectors.toList());
        if (filteredMetadata.isEmpty()) {
            LOG.warn("No annotations assigned to the group, document, authority/systemId have the required properties; thus no metadata can be updated");
            return new ArrayList<Metadata>(); // we don't throw an exception any more since the caller might have more tasks to execute
        }

        // third step: finally do the update
        final ResponseStatus targetRespStatus = updateRequest.getResponseStatus();
        final Long userId = userInfo.getUser().getId();
        final List<Metadata> metadataSetsUpdated = new ArrayList<>();
        for (final Metadata metaFound : filteredMetadata) {

            if (updateRequest.isMigrateVersion()) {
                if (!MetadataHandler.increaseResponseVersion(metaFound)) {
                    LOG.debug("Skipping metadata set with id '{}' since it has no response version");
                    continue;
                }
            } else {
                if (!MetadataHandler.updateMetadataIfDifferentResponseStatus(metaFound, targetRespStatus, userId,
                        timestamp)) {
                    LOG.debug("Skipping metadata set with id '{}' since it already has target response status", metaFound.getId());
                    continue;
                }
            }

            try {
                saveMetadata(metaFound);
            } catch (CannotCreateMetadataException e) {
                throw new CannotUpdateAnnotationStatusException(
                        String.format("Metadata with id '%s' could not be updated: %s", metaFound.getId(), e.getMessage()), e);
            }
            // keep track of successfully updated metadata sets
            metadataSetsUpdated.add(metaFound);
        }
        return metadataSetsUpdated;

    }

    @Override
    public Metadata createMetadataFromStatusUpdateRequest(final StatusUpdateRequest updateRequest) throws CannotUpdateAnnotationStatusException {

        Assert.notNull(updateRequest, "information about the metadata to be updated is required");
        Assert.isTrue(!StringUtils.isEmpty(updateRequest.getGroup()), "group assigned to metadata to be updated is required");
        Assert.isTrue(!StringUtils.isEmpty(updateRequest.getUri()), "document URI assigned to metadata to be updated is required");

        final Document foundDocument = documentService.findDocumentByUri(updateRequest.getUri());
        if (foundDocument == null) {
            LOG.error("No document found with given URI; thus no metadata can be updated");
            throw new CannotUpdateAnnotationStatusException("Given URI is unknown");
        }

        final Group foundGroup = groupService.findGroupByName(updateRequest.getGroup());
        if (foundGroup == null) {
            LOG.error("No group with given name found; thus no metadata can be updated");
            throw new CannotUpdateAnnotationStatusException("Given group is unknown");
        }

        // we use ISC here as status update requests are valid for ISC only
        final Metadata metadata = new Metadata(foundDocument, foundGroup, Authorities.ISC);
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(metadata, updateRequest.getMetadataToMatch());
        return metadata;
    }

}
