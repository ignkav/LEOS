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

var diff_match_patch = require('diff-match-patch');
import 'diff-match-patch-line-and-word';

var annotation = require('../../../src/sidebar/components/annotation');
const annotationMetadata = require('../../../src/sidebar/annotation-metadata');
const authorityChecker = require('../../sidebar/authority-checker');
const authorizer = require('../../sidebar/authorizer');
const ORIGIN_MODES = require('../../shared/originMode');

// @ngInject
function LeosAnnotationController($injector, $rootScope, $scope, $timeout, $window, analytics, store, annotationMapper, drafts, flash, groups, permissions,
  serviceUrl, session, settings, api, streamer) {

  $injector.invoke(annotation.controller, this, { $rootScope, $scope, $timeout, $window, analytics, store, annotationMapper, api, drafts, flash, groups,
                                                  permissions, serviceUrl, session, settings, streamer });

  let parentOnInit = this.$onInit;

  let metadataToDisplay = {};

  this.$onInit = () => {
    parentOnInit();

    if (this.annotation.document !== undefined && this.annotation.document.metadata !== null && !this.isReplyOfRootSuggestion()) {
      metadataToDisplay = Object.keys(this.annotation.document.metadata)
        .filter(key => Object.keys(settings.displayMetadataCondition).indexOf(key) !== -1)
        .reduce((obj, key) => {
          obj[settings.displayMetadataCondition[key]] = this.annotation.document.metadata[key];
          return obj;
        }, {});
    }

    this.annotation.hovered = false;
  };

  this.isHovered = function() {
    return this.annotation.hovered;
  };
  
  this.deleteOrphanSuggestion = function() {
    var isOrphanTab = store.getState().selectedTab === 'orphan';
    if(!isOrphanTab){
      return this.isSuggestion();
    } else {
      return false;
    }
  };

  this.showButtons = function() {
    this.annotation.hovered = true;
  };

  this.hideButtons = function() {
    this.annotation.hovered = false;
  };

  this.isSuggestion = function() {
    return annotationMetadata.isSuggestion(this.state());
  };

  /**
   * Checks whether the supllied text ist valid for submission.
   * For annotations at least some text has to be provided.
   * For suggestions text does not have to be provided necessarily
   * (i.e. no text represents a deletion).
   * @returns 
   */
  this.isTextValid = function() {
    return this.isSuggestion() || this.hasContent();
  }

  this.updateSelectedGroup = function(group) {
    this.annotation.group = group.id;
  };

  this.getMetadata = function() {
    return metadataToDisplay;
  };

  this.shouldDisplayMetadata = function() {
    return (Object.keys(metadataToDisplay).length >= 0);
  };

  this.getMetadataInfoStyle = function(keytoFind) {
    var index = Object.keys(metadataToDisplay).indexOf(keytoFind);
    return `leos-metadata-info-${index}`;
  };

  this.diffText = function() {
    var htmlDiff = this.state().text;
    if (this.editing() || !this.isTextValid()) {
      return htmlDiff;
    }
    var origText = this.quote();
    if (this.isSuggestion() && origText) {
      var dmp = new diff_match_patch();
      var textDiff = dmp.diff_wordMode(origText, this.state().text);
      htmlDiff='<span class="leos-content-modified">';
      for (let d of textDiff) {
        if (d[0] === -1) {
          htmlDiff+=`<span class="leos-content-removed">${d[1]}</span>`;
        }
        else if (d[0] === 0) {
          htmlDiff+=d[1];
        }
        else if (d[0] === 1) {
          htmlDiff+=`<span class="leos-content-new">${d[1]}</span>`;
        }
      }
      htmlDiff+='</span>';
    }
    return htmlDiff;
  };
  
  this.isReplyOfRootSuggestion = function() {
    return this.isRootSuggestion && this.isReply();
  };

  this.replyLabel = function() {
    let label = this.isCollapsed ? 'Show' : 'Hide';
    label += ' ';
    if (authorityChecker.isISC(settings) || this.isSuggestion()) {
      label += 'justification';
      if (this.replyCount !== 1) label += 's';
    } else {
      if (this.replyCount === 1) { // eslint-disable-line no-lonely-if
        label += 'reply';
      } else {
        label += 'replies';
      }
    }
    return label;
  };

  this.isDeleteButtonShown = function() {
    return this.isHovered()
      && !this.isSaving
      && authorizer.canDeleteAnnotation(this.annotation, permissions, settings, session.state.userid, this.isRootSuggestion)
      && !(authorizer.canMergeSuggestion(this.annotation, permissions, settings) && this.deleteOrphanSuggestion());
  };

  this.isEditButtonShown = function() {
    return this.isHovered()
      && !this.isSaving
      && authorizer.canUpdateAnnotation(this.annotation, permissions, settings, session.state.userid);
  };

  this.isReplyButtonShown = function() {
    return this.isHovered()
      && !this.isSaving
      && authorizer.canReplyToAnnotation(this.annotation, settings)
      && !this.state().isPrivate
      && !this.isSuggestion()
      && !this.isReplyOfRootSuggestion();
  };

  this.isContributionLabelShown = function() {
    return this.isHovered()
        && !this.isSaving
        && this.annotation.document.metadata.originMode === ORIGIN_MODES.PRIVATE
        && this.annotation.user_info.display_name !== this.annotation.document.metadata.ISCReference
        && authorizer.canUpdateAnnotation(this.annotation, permissions, settings, session.state.userid);
  }

  this.user = function() {
    if ((!this.annotation.user) && (this.session().state.userid)) {
      this.annotation.user = this.session().state.userid;
      this.annotation.user_info = this.session().state.user_info;
    }
    return this.annotation.user;
  };

}

module.exports = {
  controller: LeosAnnotationController,
  controllerAs: 'vm',
  bindings: annotation.bindings,
  template: require('../templates/leos-annotation.html'),

  // Private helper exposed for use in unit tests.
  updateModel: annotation.updateModel,
};
