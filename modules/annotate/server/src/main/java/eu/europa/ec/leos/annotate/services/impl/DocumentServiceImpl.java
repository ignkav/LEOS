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

import eu.europa.ec.leos.annotate.model.entity.Document;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotation;
import eu.europa.ec.leos.annotate.model.web.annotation.JsonAnnotationDocument;
import eu.europa.ec.leos.annotate.repository.DocumentRepository;
import eu.europa.ec.leos.annotate.services.DocumentService;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateAnnotationException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotCreateDocumentException;
import eu.europa.ec.leos.annotate.services.exceptions.CannotDeleteDocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;

import javax.annotation.Nonnull;

/**
 * Service responsible for managing annotated documents 
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentServiceImpl.class);

    // -------------------------------------
    // Required services and repositories
    // -------------------------------------
    @Autowired
    private DocumentRepository documentRepos;

    // -------------------------------------
    // Service functionality
    // -------------------------------------

    @Override
    public Document findDocumentByUri(final URI uri) {

        if (uri == null) {
            LOG.error("Cannot search for document as given URI is empty");
            return null;
        }

        return findDocumentByUri(uri.toString());
    }

    @Override
    public Document findDocumentByUri(final String uri) {

        final Document doc = documentRepos.findByUri(uri);
        LOG.debug("Found document with uri '{}':{} ", uri, doc != null);
        return doc;
    }

    @Override
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Document createNewDocument(final JsonAnnotationDocument document) throws CannotCreateDocumentException {

        if (document == null) {
            LOG.error("Cannot create document, given document is null");
            throw new CannotCreateDocumentException(new IllegalArgumentException("No document information given"));
        }

        if (document.getLink() == null || document.getLink().size() == 0) {
            LOG.error("Cannot create document, found no usable URIs");
            throw new CannotCreateDocumentException(new IllegalArgumentException("No document URI given"));
        }

        if (document.getLink().size() > 1) {
            LOG.error("UNEXPECTED: Received more than one link for a document; to be checked!");
        }

        final Document doc = new Document(document.getLink().get(0).getHref(), document.getTitle());
        try {
            documentRepos.save(doc);
        } catch (Exception e) {
            throw new CannotCreateDocumentException(e);
        }

        return doc;
    }

    @Override
    public Document createNewDocument(final URI uri) throws CannotCreateDocumentException {

        if (uri == null) {
            LOG.error("Cannot create document from URI, given URI is null");
            throw new CannotCreateDocumentException(new IllegalArgumentException("No document URI given"));
        }

        // create a new document without title
        final Document doc = new Document(uri, null);
        try {
            documentRepos.save(doc);
        } catch (Exception e) {
            throw new CannotCreateDocumentException(e);
        }

        return doc;
    }

    @Override
    @Nonnull
    public Document findOrCreateDocument(final JsonAnnotation webAnnot) throws CannotCreateAnnotationException {

        Document document = findDocumentByUri(webAnnot.getUri());
        if (document == null) {

            // register new document
            try {
                if (webAnnot.getDocument() == null) {
                    // replies to annotations do not have the document object, but still feature the URI
                    document = createNewDocument(webAnnot.getUri());
                } else {
                    // new top-level annotations have a document object containing URI and title
                    document = createNewDocument(webAnnot.getDocument());
                }
            } catch (CannotCreateDocumentException e) {
                LOG.error("Cannot create annotation as associate document could not be registered");
                throw new CannotCreateAnnotationException(e);
            }
        }
        return document;
    }

    @Override
    public void deleteDocument(final Document doc) throws CannotDeleteDocumentException {

        if (doc == null) {
            LOG.error("Cannot delete document, given document is null");
            throw new CannotDeleteDocumentException(new IllegalArgumentException("No document URI given"));
        }

        try {
            documentRepos.delete(doc);
        } catch (Exception e) {
            throw new CannotDeleteDocumentException(e);
        }
    }
}
