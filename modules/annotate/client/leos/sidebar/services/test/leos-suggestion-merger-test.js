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

const angular = require('angular');

const fixtures = require('../../components/test/leos-suggestion-fixtures-test');

function createSuggestionWithAnchorInfo({ idSuffix, completeOuterHTML, startOffset } = {}) {
  const suggestion = fixtures.defaultSuggestion();
  if (idSuffix !== undefined) suggestion.id += idSuffix;

  suggestion.anchorInfo = {
    completeOuterHTML: 'a',
    elementId: 'b',
    endOffset: 0,
    newText: 'c',
    origText: 'd',
    parentElementId: 'e',
    startOffset: 1,
  };
  if (completeOuterHTML !== undefined) suggestion.anchorInfo.completeOuterHTML = completeOuterHTML;
  if (startOffset !== undefined) suggestion.anchorInfo.startOffset = startOffset;
  
  return suggestion;
}

describe('leos-suggestion-merger', function() {

  let suggestionMerger;

  before(() =>
    angular.module('h', [])
      .service('suggestionMerger', require('../leos-suggestion-merger'))
  );

  beforeEach(() => {
    const fakeBridge = {};
    angular.mock.module('h', {
      bridge: fakeBridge,
    });
  });

  beforeEach(angular.mock.inject((_suggestionMerger_) => {
    suggestionMerger = _suggestionMerger_;
  }));

  describe('#checkValidityOfSuggestion', () => {

    it('suggestion is okay', (done) => {
      const suggestion = createSuggestionWithAnchorInfo();
      suggestionMerger.checkValidityOfSuggestion(suggestion)
        .then(() => done())
        .catch(() => assert.fails());
    });

    it('undefined suggestion is not okay', () => {
      suggestionMerger.checkValidityOfSuggestion(undefined)
        .then(() => assert.fails())
        .catch((error) => assert.equals(error, 'Suggestion is not valid.'));
    });

    it('suggestion with undefined anchorinfo is not okay', () => {
      const suggestion = createSuggestionWithAnchorInfo();
      suggestion.anchorInfo = undefined;
      suggestionMerger.checkValidityOfSuggestion(suggestion)
        .then(() => assert.fails())
        .catch((error) => assert.equals(error, 'Suggestion is not valid.'));
    });
  });
  
});
