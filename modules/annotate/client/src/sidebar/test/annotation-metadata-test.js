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
'use strict';

const ANNOTATION_STATUS = require('../../../leos/sidebar/annotation-status');

var annotationMetadata = require('../annotation-metadata');
var fixtures = require('./annotation-fixtures');

var unroll = require('../../shared/test/util').unroll;

var documentMetadata = annotationMetadata.documentMetadata;
var domainAndTitle = annotationMetadata.domainAndTitle;

describe('annotation-metadata', function () {
  describe('.documentMetadata', function() {

    context('when the model has a document property', function() {
      it('returns the hostname from model.uri as the domain', function() {
        var model = {
          document: {},
          uri: 'http://example.com/',
        };

        assert.equal(documentMetadata(model).domain, 'example.com');
      });

      context('when model.uri does not start with "urn"', function() {
        it('uses model.uri as the uri', function() {
          var model = {
            document: {},
            uri: 'http://example.com/',
          };

          assert.equal(
            documentMetadata(model).uri, 'http://example.com/');
        });
      });

      context('when document.title is an available', function() {
        it('uses the first document title as the title', function() {
          var model = {
            uri: 'http://example.com/',
            document: {
              title: ['My Document', 'My Other Document'],
            },
          };

          assert.equal(
            documentMetadata(model).title, model.document.title[0]);
        });
      });

      context('when there is no document.title', function() {
        it('returns the domain as the title', function() {
          var model = {
            document: {},
            uri: 'http://example.com/',
          };

          assert.equal(documentMetadata(model).title, 'example.com');
        });
      });
    });

    context('when the model does not have a document property', function() {
      it('returns model.uri for the uri', function() {
        var model = {uri: 'http://example.com/'};

        assert.equal(documentMetadata(model).uri, model.uri);
      });

      it('returns the hostname of model.uri for the domain', function() {
        var model = {uri: 'http://example.com/'};

        assert.equal(documentMetadata(model).domain, 'example.com');
      });

      it('returns the hostname of model.uri for the title', function() {
        var model = {uri: 'http://example.com/'};

        assert.equal(documentMetadata(model).title, 'example.com');
      });
    });
  });

  describe('.domainAndTitle', function() {
    context('when an annotation has a non-http(s) uri', function () {
      it('returns no title link', function () {
        var model = {
          uri: 'file:///example.pdf',
        };

        assert.equal(domainAndTitle(model).titleLink, null);
      });
    });

    context('when an annotation has a direct link', function () {
      it('returns the direct link as a title link', function () {
        var model = {
          links: {
            incontext: 'https://example.com',
          },
        };

        assert.equal(domainAndTitle(model).titleLink, 'https://example.com');
      });
    });

    context('when an annotation has no direct link but has a http(s) uri', function () {
      it('returns the uri as title link', function () {
        var model = {
          uri: 'https://example.com',
        };

        assert.equal(domainAndTitle(model).titleLink, 'https://example.com');
      });
    });

    context('when the annotation title is shorter than 30 characters', function () {
      it('returns the annotation title as title text', function () {
        var model = {
          document: {
            title: ['A Short Document Title'],
          },
        };

        assert.equal(domainAndTitle(model).titleText, 'A Short Document Title');
      });
    });

    context('when the annotation title is longer than 30 characters', function() {
      it('truncates the title text with "…"', function() {
        var model = {
          uri: 'http://example.com/',
          document: {
            title: ['My Really Really Long Document Title'],
          },
        };

        assert.equal(
          domainAndTitle(model).titleText,
          'My Really Really Long Document…'
        );
      });
    });

    context('when the document uri refers to a filename', function () {
      it('returns the filename as domain text', function () {
        var model = {
          uri: 'file:///path/to/example.pdf',
          document: {
            title: ['Document Title'],
          },
        };

        assert.equal(domainAndTitle(model).domain, 'example.pdf');
      });
    });

    context('when domain and title are the same', function () {
      it('returns an empty domain text string', function() {
        var model = {
          uri: 'https://example.com',
          document : {
            title: ['example.com'],
          },
        };

        assert.equal(domainAndTitle(model).domain, '');
      });
    });

    context('when the document has no domain', function () {
      it('returns an empty domain text string', function() {
        var model = {
          document : {
            title: ['example.com'],
          },
        };

        assert.equal(domainAndTitle(model).domain, '');
      });
    });

    context('when the document is a local file with a title', function () {
      it('returns the filename', function() {
        var model = {
          uri: 'file:///home/seanh/MyFile.pdf',
          document: {
            title: ['example.com'],
          },
        };

        assert.equal(domainAndTitle(model).domain, 'MyFile.pdf');
      });
    });
  });

  describe('.location', function () {
    it('returns the position for annotations with a text position', function () {
      assert.equal(annotationMetadata.location({
        target: [{
          selector: [{
            type: 'TextPositionSelector',
            start: 100,
          }],
        }],
      }), 100);
    });

    it('returns +ve infinity for annotations without a text position', function () {
      assert.equal(annotationMetadata.location({
        target: [{
          selector: undefined,
        }],
      }), Number.POSITIVE_INFINITY);
    });
  });

  describe('.isPageNote', function () {
    it ('returns true for an annotation with an empty target', function () {
      assert.isTrue(annotationMetadata.isPageNote({
        target: [],
      }));
    });
    it ('returns true for an annotation without selectors', function () {
      assert.isTrue(annotationMetadata.isPageNote({
        target: [{selector: undefined}],
      }));
    });
    it ('returns true for an annotation without a target', function () {
      assert.isTrue(annotationMetadata.isPageNote({
        target: undefined,
      }));
    });
    it ('returns false for an annotation which is a reply', function () {
      assert.isFalse(annotationMetadata.isPageNote({
        target: [],
        references: ['xyz'],
      }));
    });
  });

  describe ('.isAnnotation', function () {
    it ('returns true if an annotation is a top level annotation', function () {
      assert.isTrue(annotationMetadata.isAnnotation({
        target: [{selector: []}],
      }));
    });
    it ('returns false if an annotation has no target', function () {
      assert.isFalse(annotationMetadata.isAnnotation({}));
    });
  });

  describe('.hasContent', function() {
    it('returns true for annotation with text', function() {
      const annotation = fixtures.oldAnnotation();
      assert.isTrue(annotationMetadata.hasContent(annotation));
    });

    it('returns false for annotations without text', function() {
      const annotation = fixtures.oldAnnotation();
      annotation.text = '';
      assert.isFalse(annotationMetadata.hasContent(annotation));
    });
  });

  describe('.hasTags', function() {
    it('returns true for annotation with tags', function() {
      const annotation = fixtures.oldAnnotation();
      assert.isTrue(annotationMetadata.hasTags(annotation));
    });

    it('returns false for annotations without tags', function() {
      const annotation = fixtures.oldAnnotation();
      annotation.tags = [];
      assert.isFalse(annotationMetadata.hasTags(annotation));
    });
  });

  describe('.isHighlight', function() {
    it('returns false for page notes', function() {
      var annotation = fixtures.oldPageNote();
      assert.isFalse(annotationMetadata.isHighlight(annotation));
    });

    it('returns false for replies', function() {
      var annotation = fixtures.oldReply();
      assert.isFalse(annotationMetadata.isHighlight(annotation));
    });

    it('returns false for annotations with text but no tags', function() {
      var annotation = fixtures.oldAnnotation();
      annotation.text = 'This is my annotation';
      annotation.tags = [];
      assert.isFalse(annotationMetadata.isHighlight(annotation));
    });

    it('returns false for annotations with tags but no text', function() {
      var annotation = fixtures.oldAnnotation();
      annotation.text = '';
      annotation.tags = ['foo'];
      assert.isFalse(annotationMetadata.isHighlight(annotation));
    });

    it('returns true for annotations with no text or tags', function() {
      var annotation = fixtures.oldAnnotation();
      annotation.text = '';
      annotation.tags = [];
      assert.isTrue(annotationMetadata.isHighlight(annotation));
    });

    it('returns false for censored annotations', function() {
      var annotation = Object.assign(fixtures.oldAnnotation(), {
        hidden: true,
        text: '',
        tags: [],
      });
      assert.isFalse(annotationMetadata.isHighlight(annotation));
    });
  });

  describe('.isPublic', function () {
    it('returns true if an annotation is shared within a group', function () {
      assert.isTrue(annotationMetadata.isPublic(fixtures.publicAnnotation()));
    });

    unroll('returns false if an annotation is not publicly readable', function (testCase) {
      var annotation = Object.assign(fixtures.defaultAnnotation(), {permissions: testCase});
      assert.isFalse(annotationMetadata.isPublic(annotation));
    }, [{
      read:['acct:someemail@localhost'],
    }, {
      read:['something invalid'],
    }]);

    it('returns false if an annotation is missing permissions', function () {
      assert.isFalse(annotationMetadata.isPublic(fixtures.defaultAnnotation()));
    });
  });

  describe('.isOrphan', function () {
    it('returns true if an annotation failed to anchor', function () {
      var annotation = Object.assign(fixtures.defaultAnnotation(), {$orphan: true});
      assert.isTrue(annotationMetadata.isOrphan(annotation));
    });

    it('returns false if an annotation successfully anchored', function() {
      var orphan = Object.assign(fixtures.defaultAnnotation(), {$orphan: false});
      assert.isFalse(annotationMetadata.isOrphan(orphan));
    });
  });

  describe('.isWaitingToAnchor', function () {
    var isWaitingToAnchor = annotationMetadata.isWaitingToAnchor;

    it('returns true for annotations that are not yet anchored', function () {
      assert.isTrue(isWaitingToAnchor(fixtures.defaultAnnotation()));
    });

    it('returns false for annotations that are anchored', function () {
      var anchored = Object.assign({}, fixtures.defaultAnnotation(), {
        $orphan: false,
      });
      assert.isFalse(isWaitingToAnchor(anchored));
    });

    it('returns false for annotations that failed to anchor', function () {
      var anchored = Object.assign({}, fixtures.defaultAnnotation(), {
        $orphan: true,
      });
      assert.isFalse(isWaitingToAnchor(anchored));
    });

    it('returns false for replies', function () {
      assert.isFalse(isWaitingToAnchor(fixtures.oldReply()));
    });

    it('returns false for page notes', function () {
      assert.isFalse(isWaitingToAnchor(fixtures.oldPageNote()));
    });

    it('returns false if the anchoring timeout flag was set', function () {
      var pending = Object.assign({}, fixtures.defaultAnnotation(), {
        $anchorTimeout: true,
      });
      assert.isFalse(isWaitingToAnchor(pending));
    });
  });

  describe('.flagCount', function () {
    var flagCount = annotationMetadata.flagCount;

    it('returns `null` if the user is not a moderator', function () {
      assert.equal(flagCount(fixtures.defaultAnnotation()), null);
    });

    it('returns the flag count if present', function () {
      var ann = fixtures.moderatedAnnotation({ flagCount: 10});
      assert.equal(flagCount(ann), 10);
    });
  });

  describe('.isProcessed', () => {
    it('returns false for annotation without a status', () => {
      const annotation = fixtures.defaultAnnotation();
      assert.isFalse(annotationMetadata.isProcessed(annotation));
    });
    
    it('returns false for a normal annotation', () => {
      const annotation = Object.assign(fixtures.defaultAnnotation(), {
        status: {
          status: 'NORMAL',
        },
      });
      assert.isFalse(annotationMetadata.isProcessed(annotation));
    });
    
    it('returns true for an accepted annotation', () => {
      const annotation = Object.assign(fixtures.defaultAnnotation(), {
        status: {
          status: ANNOTATION_STATUS.ACCEPTED,
        },
      });
      assert.isTrue(annotationMetadata.isProcessed(annotation));
    });
    
    it('returns true for a deleted annotation', () => {
      const annotation = Object.assign(fixtures.defaultAnnotation(), {
        status: {
          status: ANNOTATION_STATUS.DELETED,
        },
      });
      assert.isTrue(annotationMetadata.isProcessed(annotation));
    });
    
    it('returns true for a rejected annotation', () => {
      const annotation = Object.assign(fixtures.defaultAnnotation(), {
        status: {
          status: ANNOTATION_STATUS.REJECTED,
        },
      });
      assert.isTrue(annotationMetadata.isProcessed(annotation));
    });
  });

  describe('.isSent', () => {
    it('returns false for annotation without a response status', () => {
      const annotation = fixtures.defaultAnnotation();
      assert.isFalse(annotationMetadata.isSent(annotation));
    });
    
    it('returns true for an annotation annotation with SENT response status', () => {
      const annotation = Object.assign(fixtures.defaultAnnotation(), {
        document: {
          metadata: {
            responseStatus: 'SENT',
          },
        },
      });
      assert.isTrue(annotationMetadata.isSent(annotation));
    });
  });
});
