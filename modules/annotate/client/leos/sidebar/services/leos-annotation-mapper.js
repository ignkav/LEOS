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

let angular = require('angular');

let events = require('../../../src/sidebar/events');

const annotationMapper = require('../../../src/sidebar/services/annotation-mapper');
const authorityChecker = require('../../../leos/sidebar/authority-checker');

function getExistingAnnotation(store, id) {
    return store.getState().annotations.find(function (annot) {
        return annot.id === id;
    });
}

// @ngInject
function leosAnnotationMapper($rootScope, store, api, settings) {

    function LEOS_isToBeProcessed(annotation) {
        return authorityChecker.isISC(settings) && annotation && annotation.document && annotation.document.metadata.responseStatus && annotation.document.metadata.responseStatus == 'SENT';
    }

    let leosAnnotationMapper = annotationMapper($rootScope, store, api);
    leosAnnotationMapper.unloadAnnotations = function (annotations, reset) {
        let unloaded = [];
        let toBeProcessed = false;
        annotations.forEach(function (annotation) {
            let existing = getExistingAnnotation(store, annotation.id);
            if ((reset == null || !reset) && existing && LEOS_isToBeProcessed(existing)) {
                toBeProcessed = true;
            } else if (existing && annotation !== existing) {
                annotation = angular.copy(annotation, existing);
                unloaded.push(annotation);
            } else {
                unloaded.push(annotation);
            }
        });
        if (unloaded.length>0) {
            $rootScope.$broadcast(events.ANNOTATIONS_UNLOADED, unloaded);
        } else if ((reset == null || !reset) && toBeProcessed) {
            $rootScope.$broadcast("reloadAnnotations");
        }
    }

    return leosAnnotationMapper;
}

module.exports = leosAnnotationMapper;
