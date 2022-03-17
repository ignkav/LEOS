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
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.SimpleMetadata;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Document;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.model.helper.MetadataListHelper;
import eu.europa.ec.leos.annotate.repository.MetadataRepository;
import eu.europa.ec.leos.annotate.repository.impl.MetadataVersionUpToSearchSpec;
import eu.europa.ec.leos.annotate.services.AnnotationService;
import eu.europa.ec.leos.annotate.services.MetadataMatchingService;
import eu.europa.ec.leos.annotate.services.MetadataService;
import eu.europa.ec.leos.annotate.services.impl.util.MetadataChecker;
import eu.europa.ec.leos.annotate.services.impl.util.VersionSearchType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * formerly part of the {@link MetadataService}, this service contains functionality around {@link Metadata} matching
 */
@Service
public class MetadataMatchingServiceImpl implements MetadataMatchingService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataMatchingServiceImpl.class);

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    private MetadataRepository metadataRepos;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private AnnotationService annotService;

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public boolean areAllMetadataContainedInDbMetadata(final SimpleMetadata metadataRequired, final Metadata candidateMeta) {

        return areAllMetadataContainedInDbMetadata(metadataRequired, candidateMeta, true);
    }

    @Override
    public boolean areAllMetadataContainedInDbMetadata(final SimpleMetadata metadataRequired, final Metadata candidateMeta, final boolean checkVersion) {

        // no requirements -> ok
        if (metadataRequired == null) {
            LOG.debug("Did not get any metadata that should be searched -> approve");
            return true;
        }

        if (candidateMeta == null && metadataRequired.isEmpty()) {
            // empty requirements, no DB content -> ok
            LOG.debug("Got empty metadata that should be searched -> approve");
            return true;
        } else if (candidateMeta == null) {
            // requirements present, but no DB content -> fail
            LOG.debug("Cannot compare when one or more of comparees are null");
            return false;
        }

        // note: the preprocessing checks may remove entries from the given map
        // but the callee shall not notice; therefore, we copy the map and operate on the copy
        final SimpleMetadata metaReqCopy = new SimpleMetadata(metadataRequired);

        // preprocessing for systemId (has its own field in DB entity)
        if (!MetadataChecker.checkSystemId(metaReqCopy, candidateMeta)) {
            return false;
        }

        // similar preprocessing for response status (has its own field in DB entity)
        if (!MetadataChecker.checkResponseStatus(metaReqCopy, candidateMeta)) {
            return false;
        }

        if (checkVersion) {
            // and same for version (has its own field in DB entity)
            if (!MetadataChecker.checkVersion(metaReqCopy, candidateMeta)) {
                return false;
            }
        } else {
            metaReqCopy.remove(Metadata.PROP_VERSION);
        }

        return MetadataChecker.checkKeyValueProperties(metaReqCopy, candidateMeta);
    }

    @Override
    public List<Long> getIdsOfMatchingMetadatas(final List<Metadata> candidates, final SimpleMetadata requested) {

        // no candidates -> no matches!
        if (candidates == null) {
            return null;
        }

        // no restriction given -> all match!
        if (requested == null || requested.isEmpty()) {
            return MetadataListHelper.getMetadataSetIds(candidates);
        }

        return getIdsOfMatchingMetadatas(candidates, Collections.singletonList(requested));
    }

    @Override
    public List<Long> getIdsOfMatchingMetadatas(final List<Metadata> candidates, final List<SimpleMetadata> requested) {

        // no candidates -> no matches!
        if (candidates == null) {
            return null;
        }

        // no restriction given -> all match!
        if (requested == null || requested.isEmpty() ||
                requested.size() == 1 && requested.get(0).isEmpty()) {
            return MetadataListHelper.getMetadataSetIds(candidates);
        }

        final List<Metadata> semifilteredMetadata = new ArrayList<>();
        for (final SimpleMetadata requestedMeta : requested) {

            final List<Metadata> candidates2 = filterCandidatesByVersion(candidates, requestedMeta.get(Metadata.PROP_VERSION));

            semifilteredMetadata.addAll(candidates2.stream()
                    .filter(meta -> areAllMetadataContainedInDbMetadata(requestedMeta, meta, false))
                    .collect(Collectors.toList()));
        }
        return MetadataListHelper.getMetadataSetIds(semifilteredMetadata);
    }

    @Override
    public Metadata findExactMetadata(final Document document, final Group group, final String systemId, final Metadata otherMetadata) {

        SimpleMetadata metadataMap = null;
        if (otherMetadata != null) {
            metadataMap = MetadataHandler.getKeyValuePropertyAsSimpleMetadata(otherMetadata);
            if (otherMetadata.getResponseStatus() != null) {
                metadataMap.put(Metadata.PROP_RESPONSE_STATUS, otherMetadata.getResponseStatus().toString());
            }
        }

        return findExactMetadata(document, group, systemId, metadataMap);
    }

    @Override
    public Metadata findExactMetadata(final Document document, final Group group, final String systemId, final SimpleMetadata otherMetadataProps) {

        if (document == null || group == null || StringUtils.isEmpty(systemId)) {
            LOG.warn("Cannot search for metadata item when document, group or systemId is missing");
            return null;
        }

        final List<Metadata> candidates = metadataService.findMetadataOfDocumentGroupSystemid(document, group, systemId);
        if (candidates.isEmpty()) {
            LOG.debug("Did not find any metadata sets matching given document/group/systemId");
            return null;
        }

        // find equality by matching inclusion in both directions (A includes B, B includes A)
        final List<Metadata> candidatesOneDirection = candidates.stream()
                .filter(meta -> areAllMetadataContainedInDbMetadata(otherMetadataProps, meta))
                .collect(Collectors.toList());

        final Metadata metaHelp = new Metadata(document, group, systemId);
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(metaHelp, otherMetadataProps);

        for (final Metadata candidateMeta : candidatesOneDirection) {
            if (areAllMetadataContainedInDbMetadata(MetadataHandler.getKeyValuePropertyAsSimpleMetadata(candidateMeta), metaHelp)) {
                return candidateMeta; // perfect match
            }
        }

        return null;
    }

    @Override
    public List<Long> findExactMetadataWithoutResponseVersion(final Document document, final Group group, final String systemId,
            final Metadata otherMetadata) {

        if (document == null || group == null || StringUtils.isEmpty(systemId)) {
            LOG.warn("Cannot search for metadata item when document, group or systemId is missing");
            throw new IllegalArgumentException("Mandatory parameter(s) is/are empty");
        }

        SimpleMetadata otherMetadataProps;
        if (otherMetadata == null) {
            otherMetadataProps = new SimpleMetadata();
        } else {
            otherMetadataProps = MetadataHandler.getKeyValuePropertyAsSimpleMetadata(otherMetadata);
            if (otherMetadata.getResponseStatus() != null) {
                otherMetadataProps.put(Metadata.PROP_RESPONSE_STATUS, otherMetadata.getResponseStatus().toString());
            }
        }
        otherMetadataProps.remove(Metadata.PROP_RESPONSE_VERSION);

        final List<Metadata> candidates = metadataService.findMetadataOfDocumentGroupSystemid(document, group, systemId);
        if (candidates.isEmpty()) {
            LOG.debug("Did not find any metadata sets matching given document/group/systemId");
            return null;
        }

        // find equality by matching inclusion in both directions (A includes B, B includes A)
        final List<Metadata> candidatesOneDirection = candidates.stream()
                .filter(meta -> areAllMetadataContainedInDbMetadata(otherMetadataProps, meta))
                .collect(Collectors.toList());

        final Metadata metaHelp = new Metadata(document, group, systemId);
        MetadataHandler.setKeyValuePropertyFromSimpleMetadata(metaHelp, otherMetadataProps);

        final List<Metadata> finalCandidates = new ArrayList<>();
        for (final Metadata candidateMetaItem : candidatesOneDirection) {

            final Metadata candidateMeta = new Metadata(candidateMetaItem); // create a copy to prevent the item being saved
            candidateMeta.setId(candidateMetaItem.getId() * -1); // we however need the item's ID; make it negative to "keep" it (hack!)
            MetadataHandler.removeResponseVersion(candidateMeta);

            if (areAllMetadataContainedInDbMetadata(MetadataHandler.getKeyValuePropertyAsSimpleMetadata(candidateMeta), metaHelp)) {
                finalCandidates.add(candidateMeta);
            }
        }

        return finalCandidates.stream().mapToLong(meta -> meta.getId() * -1).boxed().collect(Collectors.toList());
    }

    @Override
    public long getHighestResponseVersion(final Annotation annot) {

        final long unknownVersion = -1L;

        // method is not applicable to Non-ISC annotations
        if (!Authorities.isIsc(annot.getMetadata().getSystemId())) {
            LOG.debug("Cannot check highest known responseVersion for non-ISC annotations");
            return unknownVersion;
        }

        /* 1) based on metadata for annotation's ISC review procedure:
         *    a) if there is none, assume 1 (though this case should not occur)
         *    b) if there is a metadata set being IN_PREPARATION, this must host the highest response version
         *    c) if there is none, examine the SENT items and take the highest response version
         *    
         * 2) SENT items need to be compared to the responseVersionSentDeleted of all related annotations
         *    (since annotations might have been deleted and SENT again, which does not create new metadata,
         *    but is only saved in responseVersionSentDeleted)
         */
        final Metadata metaToMatch = new Metadata(annot.getMetadata());
        metaToMatch.setResponseStatus(null);

        final List<Long> metadataIds = findExactMetadataWithoutResponseVersion(annot.getDocument(), annot.getGroup(),
                metaToMatch.getSystemId(), metaToMatch);
        if (CollectionUtils.isEmpty(metadataIds)) {

            // 1a) probably this case cannot occur as we (currently) can only get into this method
            // when deleting a SENT annotation - so there must be matches
            LOG.debug("No metadata available for annotation yet; must be responseVersion 1");
            return -1L;
        }

        // 1b) now check for IN_PREPARATION item
        final List<Metadata> matchingMetas = (List<Metadata>) metadataRepos.findAll(metadataIds);
        final Optional<Metadata> inPrepItem = matchingMetas.stream()
                .filter(MetadataHandler::isResponseStatusInPreparation)
                .findFirst();
        if (inPrepItem.isPresent()) {
            // there may be only one response being currently IN_PREPARATION
            // -> this must correspond to the highest known response version
            return MetadataHandler.getResponseVersion(inPrepItem.get());
        }

        long result = 0L;

        // 1c) extract the highest response version of SENT items
        final OptionalLong maxSentRespVers = matchingMetas.stream()
                .filter(MetadataHandler::isResponseStatusSent)
                .mapToLong(MetadataHandler::getResponseVersion)
                .max();
        if (maxSentRespVers.isPresent()) {
            result = maxSentRespVers.getAsLong();
        }

        // 2) extract the maximum responseVersionSentDeleted from all related annotations
        // that were really deleted already (i.e. must have been SENT)
        final List<Annotation> relatedAnnots = annotService.findSentDeletedByMetadataIdAndStatus(
                metadataIds, Collections.singletonList(AnnotationStatus.DELETED));
        final Optional<Annotation> annotMaxRespSentDeleted = relatedAnnots.stream()
                .max(Comparator.comparing(Annotation::getRespVersionSentDeleted));

        long maxAnnotRespSentDeleted = 0;
        if (annotMaxRespSentDeleted.isPresent()) {
            maxAnnotRespSentDeleted = annotMaxRespSentDeleted.get().getRespVersionSentDeleted();
        }

        if (maxAnnotRespSentDeleted > result) {
            // there are already annotations that were (sent)Deleted and then SENT (and thus DELETED) - increase their responseVersion
            result = maxAnnotRespSentDeleted + 1;
        } else {
            // no already deleted items present -> use highest responseVersion found in metadata and increase
            result += 1;
        }
        return result;
    }

    @Override
    public Metadata findOrCreateInPrepItemForAnnotToDelete(final Annotation annot) {

        final Metadata metaToMatch = new Metadata(annot.getMetadata());
        metaToMatch.setResponseStatus(null);

        final List<Long> metadataIds = findExactMetadataWithoutResponseVersion(annot.getDocument(), annot.getGroup(),
                metaToMatch.getSystemId(), metaToMatch);
        if (CollectionUtils.isEmpty(metadataIds)) {

            // probably this case cannot occur as we (currently) can only get into this method
            // when deleting a SENT annotation - so there must be matches
            LOG.debug("No metadata available for annotation yet; must be responseVersion 1");
            return null;
        }

        // if there is already an IN_PREPARATION item, this must be "the one" as there cannot be several at once
        final List<Metadata> matchingMetas = (List<Metadata>) metadataRepos.findAll(metadataIds);
        final Optional<Metadata> inPrepItem = matchingMetas.stream()
                .filter(MetadataHandler::isResponseStatusInPreparation)
                .findFirst();
        if (inPrepItem.isPresent()) {
            return inPrepItem.get();
        }

        // create new item
        final long newVersion = getHighestResponseVersion(annot);
        if (newVersion == 0) {
            // this case should not occur either
            LOG.debug("No highest response version could be determined; must be responseVersion 1");
            return null;
        }
        metaToMatch.setResponseStatus(ResponseStatus.IN_PREPARATION);
        MetadataHandler.setResponseVersion(metaToMatch, newVersion);

        return metadataRepos.save(metaToMatch);
    }

    /**
     * filter a given list of metadata sets by keeping only those having a certain version
     * 
     * @param candidates list of metadata sets
     * @param version the desired version; may contain a "<=" prefix to retrieve several versions at once
     * @return filtered list of items
     */
    @Nonnull
    private List<Metadata> filterCandidatesByVersion(final List<Metadata> candidates, final String version) {

        if (StringUtils.isEmpty(version)) {
            LOG.debug("No version received for version filtering; return input unfiltered");
            return candidates;
        }

        if (CollectionUtils.isEmpty(candidates)) {
            LOG.debug("No candidates received for version filtering; return empty input");
            return candidates;
        }

        final VersionSearchType versSearch = VersionSearchType.getVersionSearchType(version);
        final List<Long> candidateIds = MetadataListHelper.getMetadataSetIds(candidates);
        List<Metadata> filtered;

        switch (versSearch) {
            case UP_TO:
                final String theVersion = version.substring(Metadata.VERSION_SEARCH_UP_TO.length());
                filtered = metadataRepos.findAll(new MetadataVersionUpToSearchSpec(theVersion, candidateIds));
                break;

            case EQUALITY:
            default:
                filtered = metadataRepos.findByVersionAndIdIsIn(version, candidateIds);
                break;
        }

        return filtered;
    }

}
