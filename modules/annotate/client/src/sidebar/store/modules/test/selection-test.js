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

const selection = require('../selection');
const fixtures = require('../../../test/annotation-fixtures');
const leosFixtures = require('../../../../../leos/sidebar/components/test/leos-suggestion-fixtures-test');

const { selectors } = selection;

describe('annotation store', () => {
  
  describe('#getSelectedAnnotationCount', () => {
    it('returns 0 if 0 of 0 annotations are selected', () => {
      const state = {
        annotations: [],
        selectedAnnotationMap: {},
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 0);
    });

    it('returns 1 if 1 of 1 annotation is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const state = {
        annotations: [annotation],
        selectedAnnotationMap: { [annotation.id]: true },
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 1);
    });

    it('returns 1 if 1 of 2 annotations is selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true },
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 1);
    });

    it('returns 2 if 2 of 2 annotations are selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true, [annotation2.id]: true },
      };
      assert.equal(selectors.getSelectedAnnotationCount(state), 2);
    });

  });

  describe('#getSelectedAnnotations', () => {
    it('returns 0 if 0 of 0 annotations are selected', () => {
      const state = {
        annotations: [],
        selectedAnnotationMap: {},
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), []);
    });

    it('returns 0 if 0 of 1 annotation is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const state = {
        annotations: [annotation],
        selectedAnnotationMap: {},
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), []);
    });

    it('returns 1 if 1 of 1 annotation is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const state = {
        annotations: [annotation],
        selectedAnnotationMap: { [annotation.id]: true },
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), [annotation]);
    });

    it('returns 1 if 1 of 2 annotations is selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true },
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), [annotation1]);
    });

    it('returns 2 if 2 of 2 annotations are selected', () => {
      const annotation1 = fixtures.oldAnnotation();
      const annotation2 = fixtures.oldAnnotation();
      annotation2.id += 2;
      const state = {
        annotations: [annotation1, annotation2],
        selectedAnnotationMap: { [annotation1.id]: true, [annotation2.id]: true },
      };
      assert.deepEqual(selectors.getSelectedAnnotations(state), [annotation1, annotation2]);
    });
  });

  describe('#getSelectedSuggestions', () => {
    it('returns 0 if 0 of 0 annotations are selected', () => {
      const state = {
        annotations: [],
        selectedAnnotationMap: {},
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), []);
    });

    it('returns 1 if 1 of 1 suggestion is selected', () => {
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [suggestion],
        selectedAnnotationMap: { [suggestion.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), [suggestion]);
    });

    it('returns 0 if 1 non-suggestion of 2 annotations is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [annotation, suggestion],
        selectedAnnotationMap: { [annotation.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), []);
    });

    it('returns 1 if 1 suggestion of 2 annotations is selected', () => {
      const annotation = fixtures.oldAnnotation();
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [annotation, suggestion],
        selectedAnnotationMap: { [suggestion.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), [suggestion]);
    });

    it('returns 1 if 2 of 2 annotations, one suggestion and one non-suggestion, are selected', () => {
      const annotation = fixtures.oldAnnotation();
      const suggestion = leosFixtures.defaultSuggestion();
      const state = {
        annotations: [annotation, suggestion],
        selectedAnnotationMap: { [annotation.id]: true, [suggestion.id]: true },
      };
      assert.deepEqual(selectors.getSelectedSuggestions(state), [suggestion]);
    });
  });

});
