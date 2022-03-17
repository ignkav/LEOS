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

import eu.europa.ec.leos.annotate.model.entity.Document;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationDocument;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateDocumentException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotDeleteDocumentException;

import java.net.URI;

public interface DocumentService {

    /**
     * retrieve a document based on its URI
     * 
     * @param uri
     *        the URI of the document
     */
    Document findDocumentByUri(URI uri);

    /**
     * retrieve a document based on its URI as String
     * 
     * @param uri
     *        the URI of the document as String
     */
    Document findDocumentByUri(String uri);

    /**
     * create a new document entry in the database, based on the JSON-wrapped input
     * 
     * @param document
     *        information about the document to be created (URI, title) 
     * @throws CannotCreateDocumentException 
     *        exception is thrown when data cannot be created due to missing data
     */
    Document createNewDocument(JsonAnnotationDocument document) throws CannotCreateDocumentException;

    /**
     * create a new document entry in the database, based on the document URI
     * 
     * @param uri
     *        the document URI  
     * @throws CannotCreateDocumentException 
     *        exception is thrown when data cannot be created due to missing data
     */
    Document createNewDocument(URI uri) throws CannotCreateDocumentException;

    /**
     * looks up if a document already exists, or tries to create it
     * 
     * @param webAnnot 
     *        incoming annotation (JSON-based)
     * @return found {@link Document}
     * @throws CannotCreateAnnotationException 
     *         thrown when document cannot be created
     */
    Document findOrCreateDocument(final JsonAnnotation webAnnot) throws CannotCreateAnnotationException;

    /**
     * delete a given document (e.g. when there are no annotations left referring to the document)
     * 
     * @param doc
     *        the document entry to be deleted
     * @throws CannotDeleteDocumentException
     *        exception is thrown when the persistence layer complains
     */
    void deleteDocument(Document doc) throws CannotDeleteDocumentException;

}
