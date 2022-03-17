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
var proxyquire = require('proxyquire');

var fixtures = require('./leos-suggestion-fixtures-test');
var testUtil = require('../../../../src/shared/test/util');
var util = require('../../../../src/sidebar/directive/test/util');

var inject = angular.mock.inject;

function isSuggestButtonHidden(annotationEl) {
    var btn = annotationEl[0].querySelector('.suggestion-merge-action');
    if (!btn) {
      return true;
    }
    return btn && btn.classList.contains('ng-hide');;
  }

function findSuggestButton(annotationEl) {
  var btns = Array.from(annotationEl[0].querySelectorAll('.suggestion-merge-action'));
  return btns.length == 0 ? false : true;
}

describe('leos-suggestion', function() {
  describe('AnnotationController', function() {
    var $rootScope;
    // Unfortunately fakeAccountID needs to be initialised here because it
    // gets passed into proxyquire() _before_ the beforeEach() that initializes
    // the rest of the fakes runs.
    var fakeAccountID = {
      isThirdPartyUser: sinon.stub(),
    };
    var fakeAnalytics;
    let fakeAnnotationMapper;
    var fakeFlash;
    let fakeGroups;
    var fakePermissions;
    let fakeStore;
    let fakeSettings;
    let fakeSuggestionMerger;

    /**
     * Returns the annotation directive with helpers stubbed out.
     */
    function suggestionButtonsComponent() {
      return proxyquire('../leos-suggestion-buttons', {
        angular: testUtil.noCallThru(angular),
        '../../../src/sidebar/util/account-id': fakeAccountID,
        '@noCallThru': true,
      });
    }

    function createDirective(annotation) {
      annotation = annotation || fixtures.defaultAnnotation();
      var element = util.createDirective(document, 'leosSuggestionButtons', {
        annotation: annotation,
      });

      // A new annotation won't have any saved drafts yet.
      if (!annotation.id) {
        fakeDrafts.get.returns(null);
      }

      return {
        annotation: annotation,
        controller: element.ctrl,
        element: element,
        scope: element.scope,
      };
    }

    before(function() {
      angular.module('h', [])
        .component('leosSuggestionButtons', suggestionButtonsComponent())
        .service('permissions', require('../../../../src/sidebar/services/permissions'))
    });

    beforeEach(angular.mock.module('h'));
    beforeEach(angular.mock.module(function($provide) {

      fakeAnalytics = {};
      fakeAnnotationMapper = {};
      fakeFlash = {};
      fakeGroups = {
        all: sinon.stub().returns([]),
      };
      fakeStore = {};
      fakeSettings = {};
      fakeSuggestionMerger = {};

      fakeAccountID.isThirdPartyUser.resetHistory();
      fakeAccountID.isThirdPartyUser.returns(false);

      fakePermissions = {
        getUserPermissions: function() {
            return ['CAN_MERGE_SUGGESTION'];
        },
        getHostState: function() {
            return false;
        },
      };

      $provide.value('analytics', fakeAnalytics);
      $provide.value('annotationMapper', fakeAnnotationMapper);
      $provide.value('flash', fakeFlash);
      $provide.value('groups', fakeGroups);
      $provide.value('permissions', fakePermissions);
      $provide.value('store', fakeStore);
      $provide.value('settings', fakeSettings);
      $provide.value('suggestionMerger', fakeSuggestionMerger);
    }));

    beforeEach(
      inject(
        function(_$q_, _$rootScope_, _$timeout_,
                _$window_) {
          $window = _$window_;
          $q = _$q_;
          $timeout = _$timeout_;
          $rootScope = _$rootScope_;
          $scope = $rootScope.$new();
        }
      )
    );

    afterEach(function() {
      sinon.restore();
    });
    it('checks presence of suggest button for suggestions', function() {
      var annotation = fixtures.defaultSuggestion();
      var el = createDirective(annotation).element;
      assert.isTrue(findSuggestButton(el));
      assert.isFalse(isSuggestButtonHidden(el));
    });
    it('checks that suggest button is disabled while no right', function() {
      fakePermissions.getUserPermissions = function() {
        return [];
      };
      var annotation = fixtures.defaultSuggestion();
      var el = createDirective(annotation).element;
      assert.isTrue(findSuggestButton(el));
      assert.isTrue(isSuggestButtonHidden(el));
    });
  });
});
