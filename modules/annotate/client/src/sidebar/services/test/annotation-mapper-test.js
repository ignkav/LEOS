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

var angular = require('angular');
var immutable = require('seamless-immutable');

var events = require('../../events');

describe('annotationMapper', function() {
  var $rootScope;
  var store;
  var fakeApi;
  var annotationMapper;

  beforeEach(function () {
    fakeApi = {
      annotation: {
        delete: sinon.stub().returns(Promise.resolve({})),
        deleteMultiple: sinon.stub().returns(Promise.resolve({})),
        flag: sinon.stub().returns(Promise.resolve({})),
      },
      suggestion: {
        accept: sinon.stub().returns(Promise.resolve({})),
        reject: sinon.stub().returns(Promise.resolve({})),
      },
    };
    angular.module('app', [])
      .service('annotationMapper', require('../annotation-mapper'))
      .service('store', require('../../store'))
      .value('api', fakeApi)
      .value('settings', {});
    angular.mock.module('app');

    angular.mock.inject(function (_$rootScope_, _store_, _annotationMapper_) {
      $rootScope = _$rootScope_;
      annotationMapper = _annotationMapper_;
      store = _store_;
    });
  });

  afterEach(function () {
    sinon.restore();
  });

  describe('#loadAnnotations()', function () {
    it('triggers the annotationLoaded event', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = [{id: 1}, {id: 2}, {id: 3}];
      annotationMapper.loadAnnotations(annotations);
      assert.called($rootScope.$broadcast);
      assert.calledWith($rootScope.$broadcast, events.ANNOTATIONS_LOADED,
        [{id: 1}, {id: 2}, {id: 3}]);
    });

    it('also includes replies in the annotationLoaded event', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = [{id: 1}];
      var replies = [{id: 2}, {id: 3}];
      annotationMapper.loadAnnotations(annotations, replies);
      assert.called($rootScope.$broadcast);
      assert.calledWith($rootScope.$broadcast, events.ANNOTATIONS_LOADED,
        [{id: 1}, {id: 2}, {id: 3}]);
    });

    it('triggers the annotationUpdated event for each loaded annotation', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = immutable([{id: 1}, {id: 2}, {id: 3}]);
      store.addAnnotations(angular.copy(annotations));

      annotationMapper.loadAnnotations(annotations);
      assert.called($rootScope.$broadcast);
      assert.calledWith($rootScope.$broadcast, events.ANNOTATION_UPDATED,
        annotations[0]);
    });

    it('also triggers annotationUpdated for cached replies', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = [{id: 1}];
      var replies = [{id: 2}, {id: 3}, {id: 4}];
      store.addAnnotations([{id:3}]);

      annotationMapper.loadAnnotations(annotations, replies);
      assert($rootScope.$broadcast.calledWith(events.ANNOTATION_UPDATED,
        {id: 3}));
    });

    it('replaces the properties on the cached annotation with those from the loaded one', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = [{id: 1, url: 'http://example.com'}];
      store.addAnnotations([{id:1, $tag: 'tag1'}]);

      annotationMapper.loadAnnotations(annotations);
      assert.called($rootScope.$broadcast);
      assert.calledWith($rootScope.$broadcast, events.ANNOTATION_UPDATED, {
        id: 1,
        url: 'http://example.com',
      });
    });

    it('excludes cached annotations from the annotationLoaded event', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = [{id: 1, url: 'http://example.com'}];
      store.addAnnotations([{id: 1, $tag: 'tag1'}]);

      annotationMapper.loadAnnotations(annotations);
      assert.called($rootScope.$broadcast);
      assert.calledWith($rootScope.$broadcast, events.ANNOTATIONS_LOADED, []);
    });
  });

  describe('#unloadAnnotations()', function () {
    it('triggers the annotationsUnloaded event', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = [{id: 1}, {id: 2}, {id: 3}];
      annotationMapper.unloadAnnotations(annotations);
      assert.calledWith($rootScope.$broadcast,
        events.ANNOTATIONS_UNLOADED, annotations);
    });

    it('replaces the properties on the cached annotation with those from the deleted one', function () {
      sinon.stub($rootScope, '$broadcast');
      var annotations = [{id: 1, url: 'http://example.com'}];
      store.addAnnotations([{id: 1, $tag: 'tag1'}]);

      annotationMapper.unloadAnnotations(annotations);
      assert.calledWith($rootScope.$broadcast, events.ANNOTATIONS_UNLOADED, [{
        id: 1,
        url: 'http://example.com',
      }]);
    });
  });

  describe('#flagAnnotation()', function () {
    it('flags an annotation', function () {
      var ann = {id: 'test-id'};
      annotationMapper.flagAnnotation(ann);
      assert.calledOnce(fakeApi.annotation.flag);
      assert.calledWith(fakeApi.annotation.flag, {id: ann.id});
    });

    it('emits the "annotationFlagged" event', function (done) {
      sinon.stub($rootScope, '$broadcast');
      var ann = {id: 'test-id'};
      annotationMapper.flagAnnotation(ann).then(function () {
        assert.calledWith($rootScope.$broadcast,
          events.ANNOTATION_FLAGGED, ann);
      }).then(done, done);
    });
  });

  describe('#createAnnotation()', function () {
    it('creates a new annotation resource', function () {
      var ann = {};
      var ret = annotationMapper.createAnnotation(ann);
      assert.equal(ret, ann);
    });

    it('emits the "beforeAnnotationCreated" event', function () {
      sinon.stub($rootScope, '$broadcast');
      var ann = {};
      annotationMapper.createAnnotation(ann);
      assert.calledWith($rootScope.$broadcast,
        events.BEFORE_ANNOTATION_CREATED, ann);
    });
  });

  describe('#deleteAnnotation()', function () {
    it('deletes the annotation on the server', function () {
      var ann = {id: 'test-id'};
      annotationMapper.deleteAnnotation(ann);
      assert.calledWith(fakeApi.annotation.delete, {id: 'test-id'});
    });

    it('triggers the "annotationDeleted" event on success', function (done) {
      sinon.stub($rootScope, '$broadcast');
      var ann = {};
      annotationMapper.deleteAnnotation(ann).then(function () {
        assert.calledWith($rootScope.$broadcast,
          events.ANNOTATION_DELETED, ann);
      }).then(done, done);
      $rootScope.$apply();
    });

    it('does not emit an event on error', function (done) {
      sinon.stub($rootScope, '$broadcast');
      fakeApi.annotation.delete.returns(Promise.reject());
      var ann = {id: 'test-id'};
      annotationMapper.deleteAnnotation(ann).catch(function () {
        assert.notCalled($rootScope.$broadcast);
      }).then(done, done);
      $rootScope.$apply();
    });
  });

  describe('#deleteAnnotations()', function () {
    const annotations = [
      { id: 'a' },
      { id: 'b' },
    ];

    it('deletes the annotations on the server', function () {
      annotationMapper.deleteAnnotations(annotations);
      assert.calledWith(fakeApi.annotation.deleteMultiple, {}, { ids: ['a', 'b'] });
    });

    it('triggers the "annotationsDeleted" event on success', function (done) {
      sinon.stub($rootScope, '$broadcast');
      annotationMapper.deleteAnnotations(annotations).then(function () {
        assert.calledWith($rootScope.$broadcast, events.ANNOTATIONS_DELETED, annotations);
      }).then(done, done);
      $rootScope.$apply();
    });

    it('does not emit an event on error', function (done) {
      sinon.stub($rootScope, '$broadcast');
      fakeApi.annotation.deleteMultiple.returns(Promise.reject());
      annotationMapper.deleteAnnotations(annotations).catch(function () {
        assert.notCalled($rootScope.$broadcast);
      }).then(done, done);
      $rootScope.$apply();
    });
  });

  describe('#acceptSuggestion()', function () {
    it('accepts an annotation', function () {
      var annotation = {id: 'a'};
      annotationMapper.acceptSuggestion(annotation);
      assert.calledOnce(fakeApi.suggestion.accept);
      assert.calledWith(fakeApi.suggestion.accept, {id: annotation.id});
    });

    it('emits the ANNOTATION_DELETED event', function (done) {
      sinon.stub($rootScope, '$broadcast');
      var annotation = {id: 'a'};
      annotationMapper.acceptSuggestion(annotation).then(function () {
        assert.calledWith($rootScope.$broadcast, events.ANNOTATION_DELETED, annotation);
      }).then(done, done);
    });
  });

  describe('#acceptSuggestions()', function () {
    const annotations = [
      { id: 'a' },
      { id: 'b' },
    ];

    it('accepts two annotations', function () {
      annotationMapper.acceptSuggestions(annotations);
      assert.calledTwice(fakeApi.suggestion.accept);
      assert.calledWith(fakeApi.suggestion.accept, {id: annotations[0].id});
      assert.calledWith(fakeApi.suggestion.accept, {id: annotations[1].id});
    });

    it('emits the ANNOTATIONS_DELETED event', function (done) {
      sinon.stub($rootScope, '$broadcast');
      annotationMapper.acceptSuggestions(annotations).then(function () {
        assert.calledWith($rootScope.$broadcast, events.ANNOTATIONS_DELETED, annotations);
      }).then(done, done);
    });
  });

  describe('#rejectSuggestion()', function () {
    it('accepts an annotation', function () {
      var annotation = {id: 'a'};
      annotationMapper.rejectSuggestion(annotation);
      assert.calledOnce(fakeApi.suggestion.reject);
      assert.calledWith(fakeApi.suggestion.reject, {id: annotation.id});
    });

    it('emits the ANNOTATION_DELETED event', function (done) {
      sinon.stub($rootScope, '$broadcast');
      var annotation = {id: 'a'};
      annotationMapper.rejectSuggestion(annotation).then(function () {
        assert.calledWith($rootScope.$broadcast, events.ANNOTATION_DELETED, annotation);
      }).then(done, done);
    });
  });

  describe('#rejectSuggestions()', function () {
    const annotations = [
      { id: 'a' },
      { id: 'b' },
    ];

    it('rejects two annotations', function () {
      annotationMapper.rejectSuggestions(annotations);
      assert.calledTwice(fakeApi.suggestion.reject);
      assert.calledWith(fakeApi.suggestion.reject, {id: annotations[0].id});
      assert.calledWith(fakeApi.suggestion.reject, {id: annotations[1].id});
    });

    it('emits the ANNOTATIONS_DELETED event', function (done) {
      sinon.stub($rootScope, '$broadcast');
      annotationMapper.rejectSuggestions(annotations).then(function () {
        assert.calledWith($rootScope.$broadcast, events.ANNOTATIONS_DELETED, annotations);
      }).then(done, done);
    });
  });

});
