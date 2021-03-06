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
package eu.europa.ec.leos.annotate.repository;

import eu.europa.ec.leos.annotate.model.ResponseStatus;
import eu.europa.ec.leos.annotate.model.entity.Document;
import eu.europa.ec.leos.annotate.model.entity.Group;
import eu.europa.ec.leos.annotate.model.entity.Metadata;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * the repository for all {@link Metadata} objects related to documents and groups
 */
public interface MetadataRepository extends CrudRepository<Metadata, Long>, JpaSpecificationExecutor<Metadata> {

    // finding metadata sets assigned to a document, a group and having a given system id
    List<Metadata> findByDocumentAndGroupAndSystemId(Document document, Group group, String systemId);

    // finding metadata sets assigned to a document and a group, having a given system id and response status
    List<Metadata> findByDocumentAndGroupAndSystemIdAndResponseStatus(Document document, Group group, String systemId, ResponseStatus responseStatus);

    // finding metadata sets assigned to a document and system, having one of the given groups
    List<Metadata> findByDocumentAndSystemIdAndGroupIdIsIn(Document document, String systemId, List<Long> groupIds);

    // finding metadata sets assigned to a document and system, having a certain response status
    List<Metadata> findByDocumentAndSystemIdAndResponseStatus(Document document, String systemId, ResponseStatus responseStatus);
    
    // finding all metadata sets having an exact version and one of given metadata IDs
    List<Metadata> findByVersionAndIdIsIn(String version, List<Long> metadataIds);
}
