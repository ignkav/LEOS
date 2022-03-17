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

const OPERATION_MODES = require('../../../leos/shared/operationMode');

const annotationMetadata = require('../../../src/sidebar/annotation-metadata');
const authorityChecker = require('../../sidebar/authority-checker');
const authorizer = require('../../sidebar/authorizer');

//@ngInject
function LeosSuggestionController($scope, $timeout, $window, analytics, annotationMapper, flash, groups, permissions, store, settings, suggestionMerger) {
  
  let self = this;
  self.isAccepting = false;

  /**
   * Initialize the controller instance.
   *
   * All initialization code except for assigning the controller instance's
   * methods goes here.
   */
  this.$onInit = () => {
    if (!store.operationMode && settings.operationMode) {
      store.operationMode = settings.operationMode;
    }

    self.canMergeSuggestion = () => authorizer.canMergeSuggestion(self.annotation, permissions, settings);

    self.isAcceptDisabled = function() {
      return store.hostState === 'OPEN'
          || self.isAccepting;
    };

    self.isAnnotationGroupInUserGroups = function() {
      const userGroups = groups.all().map(group => group.id);
      return userGroups.includes(self.annotation.group);
    };

    self.isCommentShown = function() {
      return !authorityChecker.isISC(settings)
          && store.operationMode !== OPERATION_MODES.READ_ONLY
          && !annotationMetadata.isSent(self.annotation)
          && !annotationMetadata.isProcessed(self.annotation)
          && this.isAnnotationGroupInUserGroups();
    };

    self.isCommentDisabled = function() {
      return store.operationMode === OPERATION_MODES.READ_ONLY
          || self.isAccepting;
    };

    self.isJustifyShown = function() {
      // In ISC context, only one justifying reply is allowed.
      return authorityChecker.isISC(settings)
          && this.replyCount === 0
          && store.operationMode !== OPERATION_MODES.READ_ONLY
          && !annotationMetadata.isSent(self.annotation)
          && !annotationMetadata.isProcessed(self.annotation)
          && this.isAnnotationGroupInUserGroups();
    };

    self.isJustifyDisabled = function() {
      return self.isAccepting;
    };

    self.isRejectDisabled = function() {
      return store.operationMode === OPERATION_MODES.READ_ONLY
          || self.isAccepting;
    };

    self.getAcceptTitle = function() {
      return self.isAcceptDisabled()
          ? 'Not possible to accept suggestion while editing'
          : 'Accept suggestion';
    };

    self.getCommentTitle = function() {
      return self.isCommentDisabled()
          ? 'Not possible to comment suggestion while editing'
          : 'Comment suggestion';
    };

    self.getJustifyTitle = function() {
      return self.isJustifyDisabled()
          ? 'Not possible to justify suggestion while editing'
          : 'Justify suggestion';
    };

    self.getRejectTitle = function() {
      return self.isRejectDisabled()
          ? 'Not possible to reject suggestion while editing'
          : 'Reject suggestion';
    };

    self.accept = function() {
      self.isAccepting = true;
      if (!self.annotation.user) {
        flash.info('Please log in to accept suggestions.');
        return Promise.resolve();
      }

      return suggestionMerger.processSuggestionMerging(self.annotation).then(function(suggestion) {
        annotationMapper.acceptSuggestion(suggestion).then(function() {
          analytics.track(analytics.events.ANNOTATION_DELETED);
          flash.success('Suggestion successfully merged');
          self.isAccepting = false;
        }).catch(function () {
          flash.error('Suggestion content merging failed');
          self.isAccepting = false;
        });
      }).catch(function (err) {
        flash.error(err.message);
        self.isAccepting = false;
      });
    };
  
    self.reject = function() {
      return $timeout(function() {
        const msg = 'Are you sure you want to reject this suggestion?';
        if ($window.confirm(msg)) {
          $scope.$apply(function() {
            annotationMapper.rejectSuggestion(self.annotation).then(function() {
              analytics.track(analytics.events.ANNOTATION_DELETED);
            }).catch(function (err) {
              flash.error(err.message);
            });
          });
        }
      }, true);
    };
  };
}

module.exports = {
  controller: LeosSuggestionController,	
  controllerAs: 'vm',
  bindings: {
    annotation: '<',
    reply: '&',
    replyCount: '<',
  },
  template: require('../templates/leos-suggestion-buttons.html'),
};
