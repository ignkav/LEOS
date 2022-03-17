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
package eu.europa.ec.leos.services.collection;

import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;

import java.io.File;
import java.util.List;

public interface CollectionService {

    /**
     * Create a collection from a Leg document file
     *
     * @param legDocument
     * @return The collection creation result containing the proposal view url and the bill view url
     */
    CreateCollectionResult createCollection(File legDocument) throws CreateCollectionException;
    
    /**
     * Clone an existing collection from a Leg document file
     *
     * @param legDocument
     * @param iscRef
     * @param connectedEntity
     * @return The collection cloned result containing the documents url and id
     */
    CreateCollectionResult cloneCollection(File legDocument, String iscRef, String user, String connectedEntity) throws CreateCollectionException;

    List<CloneProposalMetadataVO> getClonedProposalsMetadataVO(String propsalId, String legDocumentName);
}
