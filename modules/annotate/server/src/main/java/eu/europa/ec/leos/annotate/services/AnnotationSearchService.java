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
package eu.europa.ec.leos.annotate.services;

import java.util.List;

import eu.europa.ec.leos.annotate.model.UserInformation;
import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchCountOptions;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchOptions;
import eu.europa.ec.leos.annotate.model.search.AnnotationSearchResult;
import eu.europa.ec.leos.annotate.services.exceptions.MissingPermissionException;

public interface AnnotationSearchService {

    /**
     * retrieve the number of annotations for a given document/group/metadata
     * hereby, only public annotations are counted, and highlights are ignored
     * 
     * @param options 
     *        the options for the retrieval of annotations
     * @param userInfo 
     *        information about the user requesting the number of annotations
     * 
     * @throws MissingPermissionException
     *         this exception is thrown when requesting user is no ISC user, 
     *         or when other than ISC annotations are wanted
     * @return number of annotations, or -1 when retrieval could not even be launched
     *         due to failing precondition checks
     */
    int getAnnotationsCount(AnnotationSearchCountOptions options, UserInformation userInfo)
            throws MissingPermissionException;

    /**
     * Retrieve the number of replies given to an annotation.
     * 
     * @param annotation
     *        annotation of which the replies need to be counted
     * @param userInfo
     *        information about the user requesting the search ({@link UserInformation})
     * @param group
     *        group of the user requesting the search
     * @return number of replies, or -1 when counting could not even be launched due to failing precondition checks
     */
    long getRepliesCountForAnnotation(Annotation annotation, UserInformation userInfo, String group);

    /** 
     * search for annotations meeting certain criteria and for a given user
     * (user influences which other users' annotations are included)
     * 
     * NOTE: search for tags is currently not supported - seems a special functionality from hypothesis client, but does not respect documented API
     * 
     * @param options
     *        the {@link AnnotationSearchOptions} detailing search criteria like group, number of results, ...
     * @param userInfo
     *        information about the user for which the search is being executed
     *        
     * @return returns a list of {@link Annotation} objects meeting the search criteria
     *         returns an empty list in case search could not be run due to unfulfilled requirements  
     */
    AnnotationSearchResult searchAnnotations(AnnotationSearchOptions options, UserInformation userInfo);

    /**
     * search for the replies belonging to a given set of annotations
     * (given user influences which other users' replies are included)
     * 
     * NOTE: search for tags is currently not supported - seems a special functionality from hypothesis client, but does not respect documented API
     * 
     * @param annotSearchRes
     *        the {@link AnnotationSearchResult} resulting from a previously executed search for (root) annotations
     * @param options
     *        the search options - required for sorting and ordering
     * @param userInfo
     *        information about the user requesting the search ({@link UserInformation})
     *        
     * @return returns a list of Annotation objects meeting belonging to the annotations and visible for the user
     */
    List<Annotation> searchRepliesForAnnotations(AnnotationSearchResult searchRes, AnnotationSearchOptions options, UserInformation userInfo);

}
