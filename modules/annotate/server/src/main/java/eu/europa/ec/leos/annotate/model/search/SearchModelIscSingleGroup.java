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
package eu.europa.ec.leos.annotate.model.search;

import eu.europa.ec.leos.annotate.model.AnnotationStatus;
import eu.europa.ec.leos.annotate.model.MetadataIdsAndStatuses;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import eu.europa.ec.leos.annotate.model.helper.AnnotationChecker;
import eu.europa.ec.leos.annotate.model.helper.MetadataHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Search model class for search model ISC.1 and ISC.2: search for ISC system with single, specific group
 * 
 * note: meanwhile, this class is identical to its base class; could be combined once search model development has settled
 */
public class SearchModelIscSingleGroup extends SearchModel {

    private static final Logger LOG = LoggerFactory.getLogger(SearchModelIscSingleGroup.class);

    // -------------------------------------
    // Constructors
    // -------------------------------------

    public SearchModelIscSingleGroup(final ResolvedSearchOptions rso, final List<MetadataIdsAndStatuses> metadataIds) {
        super(rso, metadataIds, Consts.SearchModelMode.ConsiderUserMembership);
        this.hasPostFiltering = true;
        this.addDeletedHistoryItems = true;
    }

    /**
     *  for this search model, we have to filter out annotations:
     *  - if the items with highest responseVersion v_high of the user's group have responseStatus IN_PREPARATION,
     *    we have to filter out linked annotations having responseVersion v_high-1 and responseStatus SENT
     *  - if the items with highest responseVersion v_high of the user's group have responseStatus SENT, 
     *    we don't need to do anything as this case is working by design
     *  - if any item has responseStatus SENT, we filter out non-public annotations (i.e. having shared=false)
     */
    @Override
    public List<Annotation> postFilterSearchResults(final List<Annotation> foundItems) {

        // extract the responseVersions and find their maximum
        final Optional<Long> maxVersionOpt = foundItems.stream().map(ann -> MetadataHandler.getResponseVersion(ann.getMetadata()))
                .max(Comparator.comparing(Long::valueOf));

        long maxVersion;
        if (maxVersionOpt.isPresent()) {
            maxVersion = maxVersionOpt.get();
        } else {
            LOG.info("No need to do post-filtering on ISC search model: no responseVersion(s) found");
            return foundItems;
        }

        if (maxVersion <= 0) { // sometimes -1 is used by ISC for querying, but shouldn't be in the database
            LOG.info("No need to do post-filtering on ISC search model: maximum responseVersion is -1");
            return foundItems;
        }

        final List<String> itemsToRemove = getItemIdsToRemove(foundItems, maxVersion);

        if (itemsToRemove.isEmpty()) {
            // nothing to be filtered out
            return foundItems;
        }

        // now we really have to filter out
        int filteredOut = 0;
        final List<Annotation> filteredItems = new ArrayList<>();
        for (final Annotation ann : foundItems) {

            if (itemsToRemove.contains(ann.getId())) {
                // this particular item is to be filtered out - do nothing
                filteredOut++;
                continue;
            } else {
                // item passes filter -> add to result list
                filteredItems.add(ann);
            }
        }

        LOG.info("{} items filtered out", filteredOut);
        return filteredItems;
    }

    // identify the items that should be removed (=filtered out)
    private List<String> getItemIdsToRemove(final List<Annotation> foundItems, final long maxVersion) {

        final List<String> itemsToRemove = new ArrayList<>();

        // now we look for items of this highest version, having responseStatus IN_PREPARATION and a linkedAnnot set
        for (final Annotation ann : foundItems) {

            boolean addedToList = false;

            if (isLinkedInprepOfVersion(ann, maxVersion)) {

                // match: the linked annotation should be removed
                addedToList = true;
                itemsToRemove.add(ann.getLinkedAnnotationId());
            } else {

                /* 
                 there is another case: we want to filter out DELETED items which are not SentDeleted
                 why: because these are items that were deleted while still being IN_PREPARATION
                 to distinguish them from:
                 - items deleted after having been SENT: those have DELETED, SentDeleted=true
                 - annotations edited after having been SENT: those have DELETED, SentDeleted=false (and linkedAnnotation filled)
                 - annotations edited after having been SENT, then deleted later on: those have DELETED, SentDeleted=true
                 */
                if (isDeletedBeforeBeingSent(ann)) {

                    addedToList = true;
                    itemsToRemove.add(ann.getId());
                }
            }
            
            // further check: if the item has responseStatus SENT and is private, we remove it
            if(!addedToList && isPrivateSent(ann)) {
                
                // setting addedToList to true not needed as this run is over
                itemsToRemove.add(ann.getId());
            }
        }

        return itemsToRemove;
    }
 
    /**
     * return whether an annotation has a given response version, is still IN_PREPARATION 
     * and is linked to another annotation
     * 
     * @param annot the {@link Annotation} to be checked
     * @param version response version that the annotation should have
     */
    private boolean isLinkedInprepOfVersion(final Annotation annot, final long version) {
        
        final Metadata meta = annot.getMetadata();
        
        return MetadataHandler.getResponseVersion(meta) == version &&
                MetadataHandler.isResponseStatusInPreparation(meta) &&
                !StringUtils.isEmpty(annot.getLinkedAnnotationId());
    }
    
    /**
     * return whether an annotation was SENT, but it was already deleted before (and it is not 'SENTdeleted')
     * 
     * @param annot the {@link Annotation} to be checked
     * @return
     */
    private boolean isDeletedBeforeBeingSent(final Annotation annot) {
        
        final Metadata meta = annot.getMetadata();
        
        final boolean isDelBefRespStatusChgd = annot.getStatusUpdated() != null &&
                meta.getResponseStatusUpdated() != null &&
                annot.getStatusUpdated().isBefore(meta.getResponseStatusUpdated());

        return isDelBefRespStatusChgd &&
                annot.getStatus() == AnnotationStatus.DELETED &&
                !annot.isSentDeleted();
    }
    
    /**
     * return whether an annotation is private (shared=false) and has response status SENT
     * 
     * @param annot {@link Annotation} to be checked
     */
    private boolean isPrivateSent(final Annotation annot) {
        
        return AnnotationChecker.isResponseStatusSent(annot) && !annot.isShared();
    }
}