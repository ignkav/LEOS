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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import eu.europa.ec.leos.annotate.Authorities;
import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.MetadataIdsAndStatuses;
import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;
import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Document;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.entity.User;
import eu.europa.ec.leos.annotate.model.helper.AnnotationChecker;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchCountOptions;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchOptions;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchResult;
import eu.europa.ec.leos.annotate.model.search.OffsetBasedPageRequest;
import eu.europa.ec.leos.annotate.model.search.ResolvedSearchOptions;
import eu.europa.ec.leos.annotate.model.search.SearchModel;
import eu.europa.ec.leos.annotate.model.search.SearchModelFactory;
import eu.europa.ec.leos.annotate.model.search.helper.AnnotationSearchOptionsBuilder;
import eu.europa.ec.leos.annotate.model.search.helper.AnnotationSearchOptionsHandler;
import eu.europa.ec.leos.annotate.model.web.IncomingSearchOptions;
import eu.europa.ec.leos.annotate.repository.AnnotationRepository;
import eu.europa.ec.leos.annotate.repository.impl.AnnotationByIdSearchSpec;
import eu.europa.ec.leos.annotate.repository.impl.AnnotationReplySearchSpec;
import eu.europa.ec.leos.annotate.services.AnnotationSearchService;
import eu.europa.ec.leos.annotate.services.DocumentService;
import eu.europa.ec.leos.annotate.services.GroupService;
import eu.europa.ec.leos.annotate.services.MetadataMatchingService;
import eu.europa.ec.leos.annotate.services.UserService;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;

/**
 * Service responsible for searching for annotations
 */
@Service
public class AnnotationSearchServiceImpl implements AnnotationSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationSearchServiceImpl.class);

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------

    @Autowired
    @Qualifier("annotationRepos")
    private AnnotationRepository annotRepos;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private MetadataMatchingService metadataMatchingService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private SearchModelFactory searchModelFactory;

    @Autowired
    private UserService userService;

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public int getAnnotationsCount(final AnnotationSearchCountOptions options, final UserInformation userInfo)
            throws MissingPermissionException {

        Assert.notNull(userInfo, "User information not available");

        LOG.debug("User authenticated while searching for annotations: '{}'", userInfo.getLogin());
        if (!Authorities.isIsc(userInfo.getAuthority())) {
            throw new MissingPermissionException("Only permitted for ISC users");
        }

        Assert.notNull(options, "Cannot search without valid search parameters!");

        final int emptyResult = -1;

        // precondition checks - we do them here instead of relying on the search function
        // due to different result in case of precondition failure

        // p1) URI / document
        final Document doc = documentService.findDocumentByUri(options.getUri());
        if (doc == null) {
            LOG.debug("No document registered yet for given search URI: {}", options.getUri());
            return emptyResult;
        }

        // p2) group
        final Group group = groupService.findGroupByName(options.getGroup());
        if (group == null) {
            LOG.debug("No group registered yet with given search group name: {}", options.getGroup());
            return emptyResult;
        }

        // p3) check that the user requesting the search actually is member of the requested group
        // if not, he will not see any result
        final User executingUser = userService.getExecutingUser(userInfo, group);
        if (executingUser == null) {
            LOG.warn("Unable to determine user running search query");
            return emptyResult;
        }

        // unlike previous implementation, we now (ANOT-100) launch a "real search" and then remove some items
        // this way the search logic does not have to be adapted in several places in case of future changes

        // 1) so we transform our counting options to the "external search options"
        final IncomingSearchOptions fakedSearchOpts = new IncomingSearchOptions();
        fakedSearchOpts.setGroup(options.getGroup());
        fakedSearchOpts.setLimit(-1); // we want all items
        fakedSearchOpts.setMetadatasets(options.getMetadatasets());
        fakedSearchOpts.setOffset(0);
        fakedSearchOpts.setUri(options.getUri().toString());
        fakedSearchOpts.setUser(options.getUser());
        fakedSearchOpts.setShared(options.getShared());

        // 2) then have the "external search options" converted to the "internal ones" (which takes care of metadata
        // deserialisation and stuff)
        final AnnotationSearchOptions searchOptions = AnnotationSearchOptionsBuilder.fromIncomingSearchOptions(fakedSearchOpts, false);

        // p4) final precondition: check that ISC authority is requested, or no authority (and we thus assume ISC)
        if (!searchOptions.getMetadataMapsWithStatusesList().isEmpty()) {
            // creating a {@link Metadata} instance by using the given map, the system Id of the map is used
            final Metadata metadataHelp = new Metadata(doc, group, Authorities.ISC);
            throwIfNonIscRequested(metadataHelp, searchOptions.getMetadataMapsWithStatusesList());
        }

        // 3) finally search
        final AnnotationSearchResult asr = searchAnnotations(searchOptions, userInfo);

        // 4) post-filtering: we remove all highlights and items depending on their "shared status"
        final List<Annotation> withoutHighlight = asr.getItems().stream()
                .filter(ann -> !AnnotationChecker.isHighlight(ann))
                .collect(Collectors.toList());
        return withoutHighlight.size();
    }

    @Override
    public long getRepliesCountForAnnotation(final Annotation annotation, final UserInformation userInfo,
            final String group) {

        Assert.notNull(annotation, "Annotation not available");
        Assert.notNull(userInfo, "User information not available");
        Assert.hasText(group, "Group not available");

        final String annotationId = annotation.getId();
        final User executingUser = userService.findByLoginAndContext(userInfo.getLogin(), userInfo.getContext());

        // we need to know in which groups the executing user is member of to know which
        // replies posted in other groups he is allowed to see
        final List<Long> groupsOfExecutingUser = groupService.getGroupIdsOfUser(executingUser);

        final AnnotationReplySearchSpec spec = new AnnotationReplySearchSpec(Arrays.asList(annotationId), executingUser.getId(), 
                groupsOfExecutingUser);
        return annotRepos.count(spec);
    }

    @Override
    @Nonnull
    public AnnotationSearchResult searchAnnotations(final AnnotationSearchOptions options, final UserInformation userInfo) {

        Assert.notNull(userInfo, "User information not available");

        LOG.debug("User authenticated while searching for annotations: '{}'", userInfo.getLogin());

        Assert.notNull(options, "Cannot search without valid search parameters!");

        final AnnotationSearchResult emptyResult = new AnnotationSearchResult();

        // 1) URI / document
        final Document doc = documentService.findDocumentByUri(options.getUri());
        if (doc == null) {
            LOG.debug("No document registered yet for given search URI: {}", options.getUri());
            return emptyResult;
        }

        // 2) group
        final Group group = groupService.findGroupByName(options.getGroup());
        if (group == null) {
            LOG.debug("No group registered yet with given search group name: {}", options.getGroup());
            return emptyResult;
        }

        // 3) check that the user requesting the search actually is member of the requested group
        // if not, he will not see any result
        final User executingUser = userService.getExecutingUser(userInfo, group);
        if (executingUser == null) {
            LOG.warn("Unable to determine user running search query");
            return emptyResult;
        }

        // 4) user (optional)
        User user = null;
        if (!StringUtils.isEmpty(options.getUser())) {
            user = userService.findByLoginAndContext(options.getUser(), userInfo.getContext());
            if (user == null) {
                // break instead of ignoring user (which would produce more search results and open an information leak)
                LOG.debug("No user registered yet with given user name: {}, so there cannot be any matches", options.getUser());
                return emptyResult;
            }
        }

        // 5) sorting, ordering, limit and offset
        // our own Pageable implementation allows handing over sorting, limit and especially offset requirements
        final Pageable pageable = new OffsetBasedPageRequest(options.getItemOffset(), options.getItemLimit(), AnnotationSearchOptionsHandler.getSort(options));

        try {
            // wrap up all available information in order to retrieve matching search model
            final ResolvedSearchOptions rso = new ResolvedSearchOptions();
            rso.setDocument(doc);
            rso.setGroup(group);
            rso.setExecutingUserToken(userInfo.getCurrentToken());
            rso.setExecutingUser(executingUser);
            rso.setFilterUser(user);
            rso.setMetadataWithStatusesList(options.getMetadataMapsWithStatusesList());
            rso.setUserIsMemberOfGroup(groupService.isUserMemberOfGroup(executingUser, group));
            rso.setShared(options.getShared());

            return executeSearch(rso, pageable);

        } catch (Exception ex) {
            LOG.error("Search in annotation repository produced unexpected error!");
            throw ex;
        }
    }

    @Override
    public List<Annotation> searchRepliesForAnnotations(final AnnotationSearchResult annotSearchRes,
            final AnnotationSearchOptions options,
            final UserInformation userInfo) {

        if (annotSearchRes == null) {
            LOG.debug("No annotation search result received to search for answers");
            return null;
        }

        if (CollectionUtils.isEmpty(annotSearchRes.getItems())) {
            LOG.debug("No annotations received to search for belonging answers");
            return annotSearchRes.getItems();
        }

        Assert.notNull(options, "Cannot search for replies without search options!");
        Assert.notNull(userInfo, "Cannot search for replies without authenticated user!");

        // extract all the IDs of the given annotations
        final List<String> annotationIds = annotSearchRes.getItems().stream().map(Annotation::getId).collect(Collectors.toList());

        // notes:
        // - we want to retrieve ALL replies, no matter how many there are!
        // - in the database, replies have ALL their parent IDs filled in the "References" field;
        // the top-most parent element is contained in the computed "Root" field
        // -> so in order to retrieve all replies contained in the subtree of an annotation A,
        // we have to find all annotations having A's ID set in the "Root" field
        // we keep the sorting and ordering options

        final Pageable pageable = new OffsetBasedPageRequest(0, Integer.MAX_VALUE, AnnotationSearchOptionsHandler.getSort(options)); // hand over sorting

        List<Annotation> result;
        try {

            final User executingUser = userService.findByLoginAndContext(userInfo.getLogin(), userInfo.getContext());

            // on the other hand, we need to know in which groups the executing user is member of to know which
            // replies posted in other groups he is allowed to see
            final List<Long> groupsOfExecutingUser = groupService.getGroupIdsOfUser(executingUser);

            final Page<Annotation> resultPage = annotRepos.findAll(
                    new AnnotationReplySearchSpec(annotationIds, executingUser.getId(), groupsOfExecutingUser),
                    pageable);
            result = resultPage.getContent();

            // now we need to filter out such replies which should not be found according to their status
            // note: this would be too complicated to do on the database level, therefore it is performed as a postprocessing step
            final List<Annotation> filteredList = new ArrayList<>();
            final List<MetadataIdsAndStatuses> metaIdsStats = annotSearchRes.getSearchModelUsed().getMetadataAndStatusesList();
            for (final Annotation rep : result) {
                final String parentId = rep.getRootAnnotationId();
                final Annotation parentAnnot = annotSearchRes.getItems().stream()
                        .filter(ann -> ann.getId().equals(parentId))
                        .findFirst()
                        .get(); // must be there by construction

                final long parentMetaId = parentAnnot.getMetadataId();

                final List<AnnotationStatus> allowedStatus = metaIdsStats.stream()
                        .filter(mis -> mis.getMetadataIds().contains(parentMetaId))
                        .map(MetadataIdsAndStatuses::getStatuses)
                        .flatMap(list -> list.stream()) // removes nested lists
                        .distinct() // filter out duplicates
                        .collect(Collectors.toList());

                // now finally if the found reply has one of the found allowed status, if might pass
                if (allowedStatus.contains(rep.getStatus())) {
                    filteredList.add(rep);
                }
            }
            result = filteredList;
        } catch (Exception ex) {
            LOG.error("Search for replies in annotation repository produced unexpected error!");
            throw ex;
        }
        return result;
    }

    /**
     * method that actually executes the search given the final set of options
     * 
     * @param rso 
     *        {@link ResolvedSearchOptions} containing all query parameters
     * @param pageable 
     *        {@link OffsetBasedPageRequest} containing search parameters (limit, offset, sorting)
     * 
     * @return {@link AnnotationSearchResult} containing found results
     */
    @Nonnull
    private AnnotationSearchResult executeSearch(final ResolvedSearchOptions rso, final Pageable pageable) {

        final SearchModel searchModel = searchModelFactory.getSearchModel(rso);
        if (searchModel == null) {
            LOG.warn("No suitable search model found or no matching DB content found");
            return new AnnotationSearchResult(); // empty result
        }

        final AnnotationSearchResult result = new AnnotationSearchResult();
        result.setSearchModelUsed(searchModel);

        // search
        Page<Annotation> resultPage;
        if (searchModel.isHasPostFiltering()) {

            // note: if the post-filtering removes some items, paging is torpedoed - a single page request would return less items
            // therefore, we apply the page only after the post-filtering took place
            final List<Annotation> rawData = annotRepos.findAll(searchModel.getSearchSpecification());
            if (rawData.isEmpty()) {
                result.setItems(new ArrayList<Annotation>());
                result.setTotalItems(0);
            } else {
                final List<Annotation> filtered = searchModel.postFilterSearchResults(rawData);

                if (searchModel.isAddDeletedHistoryItems()) {
                    final List<Annotation> historicalItems = addDeletedHistoryItems(searchModel, rso);
                    if (!CollectionUtils.isEmpty(historicalItems)) {
                        filtered.addAll(historicalItems);
                    }
                }

                // now search again for the IDs and apply the paging
                // not the nicest way, but in that way paging works as expected and is done by the DB
                if (filtered.isEmpty()) {
                    result.setItems(new ArrayList<Annotation>());
                    result.setTotalItems(0);
                } else {
                    resultPage = annotRepos.findAll(new AnnotationByIdSearchSpec(filtered.stream().map(Annotation::getId).collect(Collectors.toList())),
                            pageable);
                    result.setItems(resultPage.getContent());
                    result.setTotalItems(resultPage.getTotalElements());
                }
            }
        } else {

            // hand over the desired pageable directly
            resultPage = annotRepos.findAll(searchModel.getSearchSpecification(), pageable);
            result.setItems(resultPage.getContent());
            result.setTotalItems(resultPage.getTotalElements());
        }

        return result;
    }

    /**
     * add annotations that were already soft-deleted (and are sentDeleted), e.g. when considering historical response versions in ISC
     * 
     * @param searchModel
     *        the search model used; required for accessing metadata IDs
     * @param rso
     *        search parameters, used for inspecting the responseVersions wanted
     * @return list of annotations that were still existing in the response versions asked for
     */
    private List<Annotation> addDeletedHistoryItems(final SearchModel searchModel, final ResolvedSearchOptions rso) {

        // determine the highest responseVersion requested for the search
        final int maxRespVersion = extractHighestResponseVersionRequested(rso);
        if (maxRespVersion <= 0) {
            return new ArrayList<Annotation>();
        }

        // extract all metadata IDs required into a flat list
        final java.util.stream.Stream<Long> emptyStream = java.util.stream.Stream.empty();
        final List<Long> metadataIds = searchModel.getMetadataAndStatusesList().stream()
                .flatMap(item -> CollectionUtils.isEmpty(item.getMetadataIds()) ? emptyStream : item.getMetadataIds().stream())
                .collect(Collectors.toList());

        // check which of these metadata items have NORMAL or DELETED status and are sentDeleted
        final List<Annotation> deletedAnnots = annotRepos.findByMetadataIdIsInAndStatusIsInAndSentDeletedIsTrue(metadataIds,
                Arrays.asList(AnnotationStatus.NORMAL, AnnotationStatus.DELETED));

        // finally check which of these were not yet deleted at the responseVersion wanted
        final List<Annotation> annotsToAdd = new ArrayList<>();
        for (final Annotation annot : deletedAnnots) {

            final long respVersSentDel = annot.getRespVersionSentDeleted();
            if (respVersSentDel == 0) continue;

            // if it was deleted in a later version only (than the ones we ask for), it should be included
            if (maxRespVersion < respVersSentDel) {

                // requested version is before it was deleted -> always add
                annotsToAdd.add(annot);
            }

            // if it was deleted in an earlier version only (than the ones we ask for), we check closer
            if (maxRespVersion >= respVersSentDel) {

                // case: maxRespVersion >= responseVersionSentDeleted -> check Metadata status of the version in which it was deleted
                final Metadata metaCopy = new Metadata(annot.getMetadata());
                MetadataHandler.setResponseVersion(metaCopy, annot.getRespVersionSentDeleted());
                final Metadata metaOfSentDeletedVersion = metadataMatchingService.findExactMetadata(
                        metaCopy.getDocument(), metaCopy.getGroup(), metaCopy.getSystemId(), metaCopy);

                // if the version in which it was deleted is IN_PREPARATION, then we won't add the item (still under preparation, so
                // the deletion already applies);
                // if the version in which it was deleted is SENT however, we add it (historical item, deletion was "committed")
                if (metaOfSentDeletedVersion != null &&
                        MetadataHandler.isResponseStatusSent(metaOfSentDeletedVersion)) {

                    annotsToAdd.add(annot);
                }
            }
        }

        return annotsToAdd;
    }

    /**
     * analyses the metadata requested, and extracts the highest responseVersion asked for
     * 
     * @param rso
     *        {@link ResolvedSearchOptions} containing the search metadata
     * @return extracted responseVersion, or 0
     */
    private int extractHighestResponseVersionRequested(final ResolvedSearchOptions rso) {

        int maxRespVersion = 0;

        // determine the highest responseVersion requested for the search
        for (final SimpleMetadataWithStatuses smws : rso.getMetadataWithStatusesList()) {

            final String respVers = smws.getMetadata().get(Metadata.PROP_RESPONSE_VERSION);
            if (!StringUtils.isEmpty(respVers)) {
                final int respVersVal = Integer.parseInt(respVers);
                if (respVersVal > maxRespVersion) {
                    maxRespVersion = respVersVal;
                }
            }
        }

        return maxRespVersion;
    }

    /**
     * checks if non-ISC data was requested in search options; if so: throws exception
     * 
     * @param metadataHelp 
     *        dummy object having main metadata properties already (time saver)
     * @param rso 
     *        filled {@link ResolvedSearchOptions} containing requested metadata sets
     * 
     * @throws MissingPermissionException thrown if any of the metadata sets requests non-ISC data
     */
    private void throwIfNonIscRequested(final Metadata metadataHelp, final List<SimpleMetadataWithStatuses> metas)
            throws MissingPermissionException {

        for (final SimpleMetadataWithStatuses requested : metas) {

            MetadataHandler.setKeyValuePropertyFromSimpleMetadata(metadataHelp, requested.getMetadata());

            if (!Authorities.isIsc(metadataHelp.getSystemId())) {
                // other authority queried -> refuse
                throw new MissingPermissionException("Only querying ISC is allowed");
            }
        }
    }

}
