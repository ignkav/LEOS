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

var publishAnnotation = require('../../../src/sidebar/components/publish-annotation-btn');
const authorityChecker = require('../../../leos/sidebar/authority-checker');
var OPERATION_MODES = require('../../../leos/shared/operationMode');

// @ngInject
function LeosPublishAnnotationController($injector, groups, settings) {
  const groupsToLoad = authorityChecker.isISC(settings) ? [] : groups;
  $injector.invoke(publishAnnotation.controller, this, {groups: groupsToLoad});

  this.groupCategory = function (group) {
    return group.type === 'open' ? 'public' : 'group';
  };

  this.getAllGroups = function () {
    var searchBarSelectGroup = groups.focused();
    if (settings.operationMode === OPERATION_MODES.PRIVATE) {
      //on private mode, annotations cannot be published to any group, only to self
      return [];
    } else if (searchBarSelectGroup.type === 'open') {
      return groups.all();
    } else {
      return [searchBarSelectGroup];
    }
  };
  
  this.isAuthorityVisible = function() {
    var isVisible = true;
    if(authorityChecker.isISC(settings)) {
      isVisible = false;
    }
    return isVisible;
  };
}

module.exports = {
  controller: LeosPublishAnnotationController,
  bindings: {
    group: '<',
    updateSelectedGroup: '&',
    canPost: '<',
    isShared: '<',
    onCancel: '&',
    onSave: '&',
    onSetPrivacy: '&',
  },
  controllerAs: 'vm',
  template: require('../templates/leos-publish-annotation-btn.html'),
};
