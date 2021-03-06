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

var util = require('../../directive/test/util');

describe('annotationShareDialog', function () {
  var element;
  var fakeAnalytics;

  function getCopyBtn() {
    return element.find('.annotation-share-dialog-link__btn');
  }

  before(function () {
    fakeAnalytics = {
      track: sinon.stub(),
      events: {},
    };
    angular.module('app', [])
      .component('annotationShareDialog',
        require('../annotation-share-dialog'))
      .value('analytics', fakeAnalytics)
      .value('urlEncodeFilter', function (val) { return val; });
  });

  beforeEach(function () {
    angular.mock.module('app');
  });

  describe('the share dialog', function () {
    it('has class is-open set when it is open', function () {
      element = util.createDirective(document, 'annotationShareDialog', {
        isOpen: true,
      });

      assert.isOk(element.find('.annotation-share-dialog').hasClass('is-open'));
    });

    it('does not have class is-open set when it is not open', function () {
      element = util.createDirective(document, 'annotationShareDialog', {
        isOpen: false,
      });

      assert.isNotOk(element.find('.annotation-share-dialog').hasClass('is-open'));
    });

    it('tracks the target being shared', function(){
      var clickShareIcon = function(iconName){
        element.find('.' + iconName).click();
      };

      element = util.createDirective(document, 'annotationShareDialog', {
        isOpen: true,
      });

      clickShareIcon('h-icon-twitter');
      assert.equal(fakeAnalytics.track.args[0][1], 'twitter');
      clickShareIcon('h-icon-facebook');
      assert.equal(fakeAnalytics.track.args[1][1], 'facebook');
      clickShareIcon('h-icon-google-plus');
      assert.equal(fakeAnalytics.track.args[2][1], 'googlePlus');
      clickShareIcon('h-icon-mail');
      assert.equal(fakeAnalytics.track.args[3][1], 'email');
    });

    it('focuses and selects the link when the dialog is opened', function (done) {
      var uri = 'https://hyp.is/a/foo';
      element = util.createDirective(document, 'annotationShareDialog', {
        isOpen: true,
        uri: uri,
      });

      setTimeout(function () {
        var shareLink = element.find('input')[0];
        assert.equal(document.activeElement, shareLink);
        assert.equal(shareLink.selectionStart, 0);
        assert.equal(shareLink.selectionEnd, uri.length);
        done();
      }, 1);
    });
  });

  describe('clipboard copy button', function () {
    var stub;

    beforeEach(function () {
      stub = sinon.stub(document, 'execCommand').returns(true);
      element = util.createDirective(document,
        'annotationShareDialog',
        {
          group: {
            name: 'Public',
            type: 'open',
          },
          uri: 'fakeURI',
          isPrivate: false,
        }
      );
    });

    afterEach(function () {
      stub.restore();
    });

    it('displays message after successful copy', function () {
      var expectedMessage = 'Link copied to clipboard!';

      getCopyBtn().click();

      var actualMessage = element.find('.annotation-share-dialog-link__feedback').text();
      assert.include(actualMessage, expectedMessage);
    });

    it('hides message after a delay after a successful copy', function () {
      var clock = sinon.useFakeTimers();
      var expectedMessage = 'Link copied to clipboard!';

      getCopyBtn().click();

      clock.tick(1999);
      clock.restore();

      var actualMessage = element.find('.annotation-share-dialog-link__feedback').text();
      assert.notInclude(actualMessage, expectedMessage);
    });

    it('displays message after failed copy', function () {
      stub.returns(false);
      var expectedMessage = 'Select and copy to share';

      getCopyBtn().click();

      var actualMessage = element.find('.annotation-share-dialog-link__feedback').text();
      assert.include(actualMessage, expectedMessage);
    });
  });

  describe('The message when a user wants to share an annotation shows that the annotation', function () {

    it('is available to a group', function () {
      element = util.createDirective(document, 'annotationShareDialog', {
        group: {
          type: 'private',
        },
        isPrivate: false,
      });

      var actualMessage = element.find('.annotation-share-dialog-msg').text();
      var actualAudience = element.find('.annotation-share-dialog-msg__audience').text();
      var expectedMessage = 'Only group members will be able to view this annotation.';
      var expectedAudience = 'Group.';
      assert.include(actualMessage, expectedMessage);
      assert.include(actualAudience, expectedAudience);
    });

    it('is private', function () {
      element = util.createDirective(document, 'annotationShareDialog', {
        isPrivate: true,
      });

      var actualMessage = element.find('.annotation-share-dialog-msg').text();
      var actualAudience = element.find('.annotation-share-dialog-msg__audience').text();
      var expectedMessage = 'No one else will be able to view this annotation.';
      var expectedAudience = 'Only me.';
      assert.include(actualMessage, expectedMessage);
      assert.include(actualAudience, expectedAudience);
    });
  });
});
