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
package eu.europa.ec.leos.annotate.repository.impl;

import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Search specification class for searching for replies to Annotations
 */
public class AnnotationReplySearchSpec implements Specification<Annotation> {

    // -------------------------------------
    // Private variables
    // -------------------------------------

    private final List<String> annotationIds;
    private final long executingUserId;
    private final List<Long> groupIdsOfExecutingUser;

    // -------------------------------------
    // Constructor
    // -------------------------------------

    /**
     * receives the following parameters:
     * 
     * @param annonationIds
     *        the IDs of the annotations whose replies are wanted
     * @param executingUserId
     *        the ID of the user running the search - influences what is visible to him
     * @param groupIdsOfExecutingUser
     *        the IDs of all groups that the executing user is member of (and thus can see posted content)
     */
    public AnnotationReplySearchSpec(final List<String> annotationIds, final long executingUserId, 
            final List<Long> groupIdsOfExecutingUser){

        this.annotationIds = annotationIds;
        this.executingUserId = executingUserId;
        this.groupIdsOfExecutingUser = groupIdsOfExecutingUser;
    }

    // -------------------------------------
    // Search predicate
    // -------------------------------------
    @Override
    @SuppressWarnings({"PMD.OptimizableToArrayCall"})
    public Predicate toPredicate(final Root<Annotation> root, final CriteriaQuery<?> query, final CriteriaBuilder critBuilder) {

        final List<Predicate> predicates = new ArrayList<>();

        // the rootAnnotationId must be one of our given values - this denotes that an annotation is a reply to any of our given annotations
        predicates.add(root.get("rootAnnotationId").in(this.annotationIds));

        // note: we no longer filter on the statuses here; this will be done in a postprocessing step due to new interrelation with metadata

        // check if the executing user is a member of the group in which the reply was published
        // aka: annot.getMetadata().getGroup().getId() contained in 'all groups of user'
        final Predicate groupsThatUserCanSee = root.<Metadata>get("metadata").<Group>get("group").
                <Long>get("id").in(this.groupIdsOfExecutingUser);

        // predicate stating whether the executing user has the permission to see an annotation reply
        final Predicate hasPermissionToSee = critBuilder.or(

                // first possibility: if the requesting user created the reply annotation, 
                // then he may see it without further restrictions
                // aka: annot.getUser().getId().equals(executingUserId)
                critBuilder.equal(root.get("userId"), this.executingUserId),

                // second possibility: annotation must be
                // a) shared (aka: annot.isShared())
                // and
                // b) user must be member of the group in which the annotation was published
                critBuilder.and(critBuilder.isTrue(root.get("shared")),
                        groupsThatUserCanSee)
                );
        predicates.add(hasPermissionToSee);

        return critBuilder.and(predicates.toArray(new Predicate[0]));
    }

}
