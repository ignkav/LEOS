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

const OPERATION_MODES = require('../shared/operationMode');
const ORIGIN_MODES = require('../shared/originMode');

const annotationMetadata = require('../../src/sidebar/annotation-metadata');
const authorityChecker = require('./authority-checker');

function canDeleteAnnotation(annotation, permissions, settings, userId, isRootSuggestion = false) {
  if( !isActionAllowedOnOperationMode(annotation, settings, userId)) {
    return false;
  }
  if (annotationMetadata.isSent(annotation)
    && (authorityChecker.isISC(settings) || (annotationMetadata.isReply(annotation) && isRootSuggestion))) {
    if (annotation.document.metadata.responseId !== settings.connectedEntity || annotationMetadata.isProcessed(annotation)) {
      // Annotation not from same group as connected unit, so no edit nor delete are allowed.
      return false;
    } else {
      return true;
    }
  }
  //Allow to delete annotations IN_PREPARATION from same DG if they are contributor annotations
  if(canDeleteOrUpdateContributorAnnotation(annotation, settings)) {
    return true;
  }
  if (permissions.getUserPermissions().includes('CAN_DELETE')) {
    return true;
  }
  return permissions.permits(annotation.permissions, 'delete', userId);
}

function canMergeSuggestion(annotation, permissions, settings) {
  if( !isActionAllowedOnOperationMode(annotation, settings, null)) {
    return false;
  }
  return permissions.getUserPermissions().includes('CAN_MERGE_SUGGESTION')
    && annotationMetadata.isSuggestion(annotation);
}

function canUpdateAnnotation(annotation, permissions, settings, userId) {
  if( !isActionAllowedOnOperationMode(annotation, settings, userId)) {
    return false;
  }
  if (annotationMetadata.isSent(annotation) && authorityChecker.isISC(settings)) {
    if (annotation.document.metadata.responseId !== settings.connectedEntity || annotationMetadata.isProcessed(annotation)) {
      // Annotation not from same group as connected unit, so no edit nor delete are allowed.
      return false;
    } else {
      return true;
    }
  }
  //Allow to update annotations IN_PREPARATION from same DG only if they are contributor annotations
  if(canDeleteOrUpdateContributorAnnotation(annotation, settings)) {
    return true;
  }
  return permissions.permits(annotation.permissions, 'update', userId);
}

function isActionAllowedOnOperationMode(annotation, settings, userId) {
  //In READ ONLY mode no action is allowed
  if (settings.operationMode === OPERATION_MODES.READ_ONLY) {
    return false;
  }
  //In PRIVATE mode user can only operate over his own annotations IN PREPARATION
  if (settings.operationMode === OPERATION_MODES.PRIVATE) {
    return !annotationMetadata.isSent(annotation)
        && authorityChecker.isISC(settings)
        && annotation.document.metadata.responseId === settings.connectedEntity
        && !annotationMetadata.isProcessed(annotation)
        && annotation.user === userId;
  }
  return true;
}

function canReplyToAnnotation(annotation, settings) {
  if( !isActionAllowedOnOperationMode(annotation, settings, null)) {
    return false;
  }
  return !authorityChecker.isISC(settings)
    && !annotationMetadata.isSent(annotation);
}

function canDeleteOrUpdateContributorAnnotation(annotation, settings) {
  //Allow to update & delete annotations IN_PREPARATION from same DG if they are contributor annotations
  return !annotationMetadata.isSent(annotation) &&
          authorityChecker.isISC(settings) &&
          annotation.document.metadata.originMode === ORIGIN_MODES.PRIVATE &&
          annotation.document.metadata.responseId === settings.connectedEntity;
}

module.exports = {
  canDeleteAnnotation,
  canMergeSuggestion,
  canUpdateAnnotation,
  canReplyToAnnotation,
};
