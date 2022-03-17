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

var sessionUtil = require('../util/session-util');
var uiConstants = require('../ui-constants');

//@ngInject
function SelectionTabController(frameSync, session, settings, store) {
  this.TAB_ANNOTATIONS = uiConstants.TAB_ANNOTATIONS;
  this.TAB_NOTES = uiConstants.TAB_NOTES;
  this.TAB_ORPHANS = uiConstants.TAB_ORPHANS;

  this.isThemeClean = settings.theme === 'clean';

  this.enableExperimentalNewNoteButton = settings.enableExperimentalNewNoteButton;

  this.selectTab = function (type) {
    store.clearFilteredAndSelectedAnnotations();
    frameSync.deselectAllAnnotations();
    store.selectTab(type);
  };

  this.showAnnotationsUnavailableMessage = function () {
    return this.selectedTab === this.TAB_ANNOTATIONS &&
      this.totalAnnotations === 0 &&
      !this.isWaitingToAnchorAnnotations;
  };

  this.showNotesUnavailableMessage = function () {
    return this.selectedTab === this.TAB_NOTES &&
      this.totalNotes === 0;
  };

  this.showSidebarTutorial = function () {
    return sessionUtil.shouldShowSidebarTutorial(session.state);
  };
}

module.exports = {
  controllerAs: 'vm',
  controller: SelectionTabController,
  bindings: {
    isLoading: '<',
    isWaitingToAnchorAnnotations: '<',
    selectedTab: '<',
    totalAnnotations: '<',
    totalNotes: '<',
    totalOrphans: '<',
  },
  template: require('../templates/selection-tabs.html'),
};
