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

const ANNOTATION_STATUS = require('../annotation-status');
const OPERATION_MODES = require('../../shared/operationMode');
const RESPONSE_STATUS = require('../../shared/response-status');

const authorizer = require('../authorizer');
const fixtures = require('../components/test/leos-suggestion-fixtures-test');

const permissionsWithoutUserPermissions = {
  getUserPermissions: sinon.stub().returns([]),
  permits: sinon.stub().throws(new Error('Not tested here')),
};

const settingsWithNormalOperationMode = {
  connectedEntity: 'A',
  operationMode: OPERATION_MODES.NORMAL,
};

const settingsWithReadOnlyOperationMode = {
  operationMode: OPERATION_MODES.READ_ONLY,
};

describe('canDeleteAnnotation()', () => {
  const permissionsWithMergeRight = {
    getUserPermissions: sinon.stub().returns(['CAN_DELETE']),
    permits: sinon.stub().throws(new Error('Not tested here')),
  };
  let annotation;

  beforeEach(() => {
    annotation = fixtures.defaultSuggestion();
  });

  it('returns true for annotation when in normal operation mode and with user right', () => {
    assert.isTrue(authorizer.canDeleteAnnotation(annotation, permissionsWithMergeRight, settingsWithNormalOperationMode, 'a'));
  });

  it('returns false for annotation when in read only operation mode and with user right', () => {
    assert.isFalse(authorizer.canDeleteAnnotation(annotation, permissionsWithMergeRight, settingsWithReadOnlyOperationMode, 'a'));
  });

  it('returns false for annotation when in read only operation mode and without user right', () => {
    assert.isFalse(authorizer.canDeleteAnnotation(annotation, permissionsWithoutUserPermissions, settingsWithReadOnlyOperationMode, 'a'));
  });

  context('when in normal operation mode without context and without user right', () => {
    beforeEach(() => {
      annotation.document = {
        metadata: {
          responseId: 'A',
          responseStatus: RESPONSE_STATUS.SENT,
        },
      };
      annotation.references = ['a'];
    });

    it('returns true for SENT suggestion justification from same group', () => {
      assert.isTrue(authorizer.canDeleteAnnotation(annotation, permissionsWithoutUserPermissions, settingsWithNormalOperationMode, 'b', true));
    });

    it('returns false for SENT suggestion justification from different group', () => {
      annotation.document.metadata.responseId = 'B';
      assert.isFalse(authorizer.canDeleteAnnotation(annotation, permissionsWithoutUserPermissions, settingsWithNormalOperationMode, 'b', true));
    });

    it('returns true for non-processed SENT suggestion justification (from same group)', () => {
      annotation.status = {
        status: ANNOTATION_STATUS.NORMAL,
      };
      assert.isTrue(authorizer.canDeleteAnnotation(annotation, permissionsWithoutUserPermissions, settingsWithNormalOperationMode, 'b', true));
    });

    it('returns false for processed SENT suggestion justification (from same group)', () => {
      annotation.status = {
        status: ANNOTATION_STATUS.ACCEPTED,
      };
      assert.isFalse(authorizer.canDeleteAnnotation(annotation, permissionsWithoutUserPermissions, settingsWithNormalOperationMode, 'b', true));
    });
  });
});

describe('canMergeSuggestion()', () => {
  const permissionsWithMergeRight = {
    getUserPermissions: sinon.stub().returns(['CAN_MERGE_SUGGESTION']),
  };
  const suggestion = fixtures.defaultSuggestion();

  context('when in normal operation mode and with user right', () => {
    it('returns true for suggestion', () => {
      assert.isTrue(authorizer.canMergeSuggestion(suggestion, permissionsWithMergeRight, settingsWithNormalOperationMode));
    });

    it('returns false for comment', () => {
      const comment = fixtures.defaultComment();
      assert.isFalse(authorizer.canMergeSuggestion(comment, permissionsWithMergeRight, settingsWithNormalOperationMode));
    });
  });

  context('when there is a suggestion', () => {
    it('returns false for read only operation mode', () => {
      assert.isFalse(authorizer.canMergeSuggestion(suggestion, permissionsWithMergeRight, settingsWithReadOnlyOperationMode));
    });

    it('returns false without permissions', () => {
      assert.isFalse(authorizer.canMergeSuggestion(suggestion, permissionsWithoutUserPermissions, settingsWithNormalOperationMode));
    });
  });
});

describe('canUpdateAnnotation()', () => {
  const annotation = fixtures.defaultSuggestion();

  it('returns false for annotation when in read only operation mode', () => {
    assert.isFalse(authorizer.canUpdateAnnotation(annotation, permissionsWithoutUserPermissions, settingsWithReadOnlyOperationMode, 'a'));
  });
});
