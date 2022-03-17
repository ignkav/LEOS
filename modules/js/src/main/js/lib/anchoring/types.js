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
// This code is generated code from client/src/annotator/anchoring/types.coffee script.
define(function(require, exports, module) {

  'use strict';

// This module exports a set of classes for converting between DOM `Range`
// objects and different types of selector. It is mostly a thin wrapper around a
// set of anchoring libraries. It serves two main purposes:
//
//  1. Providing a consistent interface across different types of anchor.
//  2. Insulating the rest of the code from API changes in the underyling anchoring
//     libraries.

var domAnchorTextPosition = require('dom-anchor-text-position');
var domAnchorTextQuote = require('dom-anchor-text-quote');

var xpathRange = require('range');

// Helper function for throwing common errors
var missingParameter = function (name) {
  throw new Error('missing required parameter "' + name + '"');
};


/**
 * class:: RangeAnchor(range)
 * This anchor type represents a DOM Range.
 * :param Range range: A range describing the anchor.
 */
class RangeAnchor {
  constructor(root, range) {
    if (!(typeof root !== "undefined" && root !== null)) { missingParameter('root'); }
    if (!(typeof range !== "undefined" && range !== null)) { missingParameter('range'); }
    this.root = root;
    this.range = xpathRange.sniff(range).normalize(this.root);
  }

  static fromRange(root, range) {
    return new RangeAnchor(root, range);
  }

  // Create and anchor using the saved Range selector.
  static fromSelector(root, selector) {
    var data = {
      start: selector.startContainer,
      startOffset: selector.startOffset,
      end: selector.endContainer,
      endOffset: selector.endOffset
    };
    var range = new xpathRange.SerializedRange(data);
    return new RangeAnchor(root, range);
  }

  toRange() {
    return this.range.toRange();
  }

  toSelector(options = {}) {
    var range = this.range.serialize(this.root, options.ignoreSelector);
    return {
      type: 'RangeSelector',
      startContainer: range.start,
      startOffset: range.startOffset,
      endContainer: range.end,
      endOffset: range.endOffset
    };
  }
}

/**
 * Converts between TextPositionSelector selectors and Range objects.
 */
class TextPositionAnchor {
  constructor(root, start, end) {
    this.root = root;
    this.start = start;
    this.end = end;
  }

  static fromRange(root, range) {
    var selector = domAnchorTextPosition.fromRange(root, range);
    return TextPositionAnchor.fromSelector(root, selector);
  }

  static fromSelector(root, selector) {
    return new TextPositionAnchor(root, selector.start, selector.end);
  }

  toSelector() {
    return {
      type: 'TextPositionSelector',
      start: this.start,
      end: this.end,
    };
  }

  toRange() {
    return domAnchorTextPosition.toRange(this.root, { start: this.start, end: this.end });
  }
}

/**
 * Converts between TextQuoteSelector selectors and Range objects.
 */
class TextQuoteAnchor {
  constructor(root, exact, context = {}) {
    this.root = root;
    this.exact = exact;
    this.context = context;
  }

  static fromRange(root, range, options) {
    var selector = domAnchorTextQuote.fromRange(root, range, options);
    return TextQuoteAnchor.fromSelector(root, selector);
  }

  static fromSelector(root, selector) {
    var {prefix, suffix} = selector;
    return new TextQuoteAnchor(root, selector.exact, { prefix, suffix });
  }

  toSelector() {
    return {
      type: 'TextQuoteSelector',
      exact: this.exact,
      prefix: this.context.prefix,
      suffix: this.context.suffix,
    };
  }

  toRange(options = {}) {
    var range = domAnchorTextQuote.toRange(this.root, this.toSelector(), options);
    if (range === null) {
      throw new Error('Quote not found');
    }
    return range;
  }

  toPositionAnchor(options = {}) {
    var anchor = domAnchorTextQuote.toTextPosition(this.root, this.toSelector(), options);
    if (anchor === null) {
      throw new Error('Quote not found');
    }
    return new TextPositionAnchor(this.root, anchor.start, anchor.end);
  }
}


exports.RangeAnchor = RangeAnchor;
exports.FragmentAnchor = require('dom-anchor-fragment');
exports.TextPositionAnchor = TextPositionAnchor;
exports.TextQuoteAnchor = TextQuoteAnchor;

});