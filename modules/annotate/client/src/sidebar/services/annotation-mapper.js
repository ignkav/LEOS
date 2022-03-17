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

var events = require('../events');

function getExistingAnnotation(store, id) {
  return store.getState().annotations.find(function (annot) {
    return annot.id === id;
  });
}

//LEOS Change
function LEOS_processAnnotations(annotations, _rootScope) {
  annotations.forEach(function (annotation) {
    if (annotation.group) {
      annotation.group = annotation.group.replace(' ', _rootScope.ANNOTATION_GROUP_SPACE_REPLACE_TOKEN);
    }
  });
}

// Wraps the annotation store to trigger events for the CRUD actions
// @ngInject
function annotationMapper($rootScope, store, api) {
  function loadAnnotations(annotations, replies) {
    annotations = annotations.concat(replies || []);
    //LEOS Change : remove white spaces from GROUP names
    LEOS_processAnnotations(annotations, $rootScope);

    var loaded = [];
    annotations.forEach(function (annotation) {
      var existing = getExistingAnnotation(store, annotation.id);
      if (existing) {
        $rootScope.$broadcast(events.ANNOTATION_UPDATED, annotation);
        return;
      }
      loaded.push(annotation);
    });

    $rootScope.$broadcast(events.ANNOTATIONS_LOADED, loaded);
  }

  function unloadAnnotations(annotations) {
    var unloaded = annotations.map(function (annotation) {
      var existing = getExistingAnnotation(store, annotation.id);
      if (existing && annotation !== existing) {
        annotation = angular.copy(annotation, existing);
      }
      return annotation;
    });
    $rootScope.$broadcast(events.ANNOTATIONS_UNLOADED, unloaded);
  }

  function createAnnotation(annotation) {
    $rootScope.$broadcast(events.BEFORE_ANNOTATION_CREATED, annotation);
    return annotation;
  }

  function deleteAnnotation(annotation) {
    return api.annotation.delete({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function deleteAnnotations(annotations) {
    return api.annotation.deleteMultiple({}, {
      ids: annotations.map(a => a.id),
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  function flagAnnotation(annot) {
    return api.annotation.flag({
      id: annot.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_FLAGGED, annot);
      return annot;
    });
  }

  function acceptSuggestion(annotation) {
    return api.suggestion.accept({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function acceptSuggestions(annotations) {
    return Promise.all(annotations.map(annotation => api.suggestion.accept({
      id: annotation.id,
    }))).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  function rejectSuggestion(annotation) {
    return api.suggestion.reject({
      id: annotation.id,
    }).then(function () {
      $rootScope.$broadcast(events.ANNOTATION_DELETED, annotation);
      return annotation;
    });
  }

  function rejectSuggestions(annotations) {
    return Promise.all(annotations.map(annotation => api.suggestion.reject({
      id: annotation.id,
    }))).then(function () {
      $rootScope.$broadcast(events.ANNOTATIONS_DELETED, annotations);
      return annotations;
    });
  }

  return {
    acceptSuggestion,
    acceptSuggestions,
    loadAnnotations,
    unloadAnnotations,
    createAnnotation,
    deleteAnnotation,
    deleteAnnotations,
    flagAnnotation,
    rejectSuggestion,
    rejectSuggestions,
  };
}

module.exports = annotationMapper;
