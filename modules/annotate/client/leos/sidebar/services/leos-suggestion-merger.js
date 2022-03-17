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

// Prevent Babel inserting helper code after `@ngInject` comment below which breaks browserify-ngannotate.
var unused; // eslint-disable-line

// @ngInject
function SuggestionMerger(bridge) {

  const TIMEOUT_REQUEST_ANNOTATION_ANCHOR_IN_MS = 3000;
  const TIMEOUT_REQUEST_MERGE_SUGGESTION_IN_MS = 60000;

  const self = this;

  function getAnnotationAnchor(suggestion) {
    return new Promise((resolve, reject) => {
      const timeout = setTimeout((() => reject('timeout')), TIMEOUT_REQUEST_ANNOTATION_ANCHOR_IN_MS);
      bridge.call('requestAnnotationAnchor', suggestion, (error, [anchor]) => {
        clearTimeout(timeout);
        suggestion.anchorInfo = anchor;
        if (error) {
          return reject(error);
        } else {
          return resolve(suggestion);
        }
      });
    });
  }

  self.checkValidityOfSuggestion = suggestion => {
    if (!suggestion || !suggestion.anchorInfo) {
      return Promise.reject('Suggestion is not valid.');
    } else {
      return Promise.resolve(suggestion);
    }
  };

  function sendMergeSuggestionRequest(suggestion) {
    const anchor = suggestion.anchorInfo;
    anchor.newText = suggestion.text;

    return new Promise((resolve, reject) => {
      const timeout = setTimeout((() => reject('timeout')), TIMEOUT_REQUEST_MERGE_SUGGESTION_IN_MS);
      bridge.call('requestMergeSuggestion', anchor, (error, [result]) => {
        clearTimeout(timeout);
        if (result && result.result && result.result !== 'SUCCESS') {
          error = result;
        }
        if (error) {
          return reject(error);
        } else {
          return resolve(suggestion);
        }
      });
    });
  }

  function sendMergeSuggestionsRequest(suggestions) {
    const anchors = [];
    suggestions.forEach(suggestion => {
      const anchor = suggestion.anchorInfo;
      anchor.newText = suggestion.text;
      anchors.push(anchor);
    });
    
    return new Promise((resolve, reject) => {
      const timeout = setTimeout((() => reject('timeout')), TIMEOUT_REQUEST_MERGE_SUGGESTION_IN_MS);
      bridge.call('requestMergeSuggestions', anchors, (error, [results]) => {
        clearTimeout(timeout);
        
        if (! results || ! Array.isArray(results)) {
          reject("No valid result received from editor");
        }

        if (results.length !== suggestions.length) {
          reject('Results have a different length than the provided input.');
        }

        results.forEach((anchor, index) => {
          suggestions[index].hasBeenMerged = anchor.result === 'SUCCESS';
        });

        if (error) {
          return reject(error);
        } else {
          return resolve(suggestions);
        }
      });
    });
  }

  self.removeMergedSuggestions = (suggestions) => {
    return suggestions.filter(suggestion => suggestion.hasBeenMerged)
      .map(suggestion => {
        delete suggestion.hasBeenMerged;
        return suggestion;
      });
  };
  
  self.processSuggestionMerging = (suggestion) => getAnnotationAnchor(suggestion)
    .then(self.checkValidityOfSuggestion)
    .then(sendMergeSuggestionRequest)
    .catch(Promise.reject);

  self.processSuggestionMergings = (suggestions) =>  Promise.all(suggestions.map(getAnnotationAnchor))
    .then(suggestions => Promise.all(suggestions.map(self.checkValidityOfSuggestion)))
    .then(sendMergeSuggestionsRequest)
    .then(self.removeMergedSuggestions)
    .catch(Promise.reject);
}

module.exports = SuggestionMerger;
