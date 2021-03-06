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
// Tests that the expected parts of the page are highlighted when annotations
// with various combinations of selector are anchored.

'use strict';

var unroll = require('../../../shared/test/util').unroll;
var Guest = require('../../guest');

function quoteSelector(quote) {
  return {
    type: 'TextQuoteSelector',
    exact: quote,
  };
}

/** Generate an annotation that matches a text quote in a page. */
function annotateQuote(quote) {
  return {
    target: [{
      selector: [quoteSelector(quote)],
    }],
  };
}

/**
 * Return the text of all highlighted phrases in `container`.
 *
 * @param {Element} container
 */
function highlightedPhrases(container) {
  return Array.from(container.querySelectorAll('.annotator-hl')).map(function (el) {
    return el.textContent;
  });
}

function simplifyWhitespace(quote) {
  return quote.replace(/\s+/g, ' ');
}

function FakeCrossFrame() {
  this.destroy = sinon.stub();
  this.onConnect = sinon.stub();
  this.on = sinon.stub();
  this.sync = sinon.stub();
}

describe('anchoring', function () {
  var guest;
  var guestConfig;
  var container;

  before(function () {
    guestConfig = {pluginClasses: {CrossFrame: FakeCrossFrame}};
  });

  beforeEach(function () {
    sinon.stub(console, 'warn');
    container = document.createElement('div');
    container.innerHTML = require('./test-page.html');
    document.body.appendChild(container);
    guest = new Guest(container, guestConfig);
  });

  afterEach(function () {
    guest.destroy();
    container.parentNode.removeChild(container);
    console.warn.restore();
  });

  unroll('should highlight #tag when annotations are loaded', function (testCase) {
    var normalize = function (quotes) {
      return quotes.map(function (q) { return simplifyWhitespace(q); });
    };

    var annotations = testCase.quotes.map(function (q) {
      return annotateQuote(q);
    });

    var anchored = annotations.map(function (ann) {
      return guest.anchor(ann);
    });

    return Promise.all(anchored).then(function () {
      var assertFn = testCase.expectFail ? assert.notDeepEqual : assert.deepEqual;
      assertFn(normalize(highlightedPhrases(container)),
               normalize(testCase.quotes));
    });
  }, [{
    tag: 'a simple quote',
    quotes: ['This has not been a scientist\'s war'],
  },{
    // Known failure with nested annotations that are anchored via quotes
    // or positions. See https://github.com/hypothesis/h/pull/3313 and
    // https://github.com/hypothesis/h/issues/3278
    tag: 'nested quotes',
    quotes: [
      'This has not been a scientist\'s war;' +
        ' it has been a war in which all have had a part',
      'scientist\'s war',
    ],
    expectFail: true,
  }]);
});
