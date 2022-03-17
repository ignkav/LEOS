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

const frameSync = require('../../src/sidebar/services/frame-sync');

// @ngInject
function LeosFrameSync($injector, $rootScope, $window, Discovery, store, bridge, rootThread) {
  $injector.invoke(frameSync.default, this, {$rootScope, $window, Discovery, store, bridge});
  const oldConnect = this.connect;
  const self = this;
  this.connect = function() {
    oldConnect();
    
    bridge.on('stateChangeHandler', function (state) {
      store.hostState = state;
      if (store.hostState === 'OPEN') {
        bridge.call('LEOS_clearSelection');
      } else {
        bridge.call('LEOS_cancelFilterHighlights');
      }
    });

    bridge.on('reloadAnnotations', function () {
      $rootScope.$broadcast('reloadAnnotations');
    });

    bridge.on('LEOS_requestFilteredAnnotations', function () {
      $rootScope.$broadcast('LEOS_requestFilteredAnnotations');
    });

    bridge.on('LEOS_responseFilteredAnnotations', function (annotations) {
      bridge.call('LEOS_responseFilteredAnnotations', annotations);
    });

    bridge.on('LEOS_clearSelectedAnnotations', function (annotationIdToSelectInstead) {
      if (annotationIdToSelectInstead) {
        const thread = rootThread.thread(store.getState());
        if (thread.children.some(child => child.id === annotationIdToSelectInstead)) {
          store.toggleSelectedAnnotations([annotationIdToSelectInstead]);
        } else {
          // Ignore selection of annotations from document that are currently not shown in the sidebar, e.g. when a search filter is applied.
          // Therefore, deselect the annotation clicked on again in the document.
          const annot = store.findAnnotationByID(annotationIdToSelectInstead);
          self.LEOS_selectAnnotation(annot);
        }
      } else {
        store.deselectAllAnnotations();
      }
    });

    bridge.on('LEOS_changeOperationMode', function (operationMode) {
      store.operationMode = operationMode;
    });

    bridge.on('LEOS_syncCanvas', function (iFrameOffsetLeft, delayResp) {
      $rootScope.$broadcast('LEOS_syncCanvas', iFrameOffsetLeft, delayResp);
    });

    bridge.on('LEOS_syncCanvasResp', function () {
      bridge.call('LEOS_syncCanvasResp');
    });

    bridge.on('LEOS_setVisibleGuideLines', function (state, storePrevState) {
      bridge.call('LEOS_setVisibleGuideLines', state, storePrevState);
    });

    bridge.on('LEOS_restoreGuideLinesState', function () {
      bridge.call('LEOS_restoreGuideLinesState');
    });

    bridge.on('LEOS_updateIdForCreatedAnnotation', function (annotationTag, createdAnnotationId) {
      bridge.call('LEOS_updateIdForCreatedAnnotation', annotationTag, createdAnnotationId);
    });

    bridge.on('LEOS_refreshAnnotationLinkLines', function () {
      bridge.call('LEOS_refreshAnnotationLinkLines');
    });

  };
}

module.exports = {
  default: LeosFrameSync,
  formatAnnot: frameSync.formatAnnot,
};
