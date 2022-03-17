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
// This code is generated code from client/src/annotator/anchoring/xpath.coffee script.

define(function(require, exports, module) {

  'use strict';

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//
// This is a modified copy of
// https://github.com/openannotation/annotator/blob/v1.2.x/src/xpath.coffee

var $ = require('jquery');

// A simple XPath evaluator using jQuery which can evaluate queries of
var simpleXPathJQuery = function (relativeRoot) {
  var jq = this.map(function () {
    var path = '';
    var elem = this;

    while (((typeof elem !== "undefined" && elem !== null) ? elem.nodeType : undefined) === Node.ELEMENT_NODE && elem !== relativeRoot) {
      var tagName = elem.tagName.replace(":", "\\:");
      var idx = $(elem.parentNode).children(tagName).index(elem) + 1;

      idx = `[${idx}]`;
      path = "/" + elem.tagName.toLowerCase() + idx + path;
      elem = elem.parentNode;
    }

    return path;
  });

  return jq.get();
};

// A simple XPath evaluator using only standard DOM methods which can
// evaluate queries of the form /tag[index]/tag[index].
var simpleXPathPure = function (relativeRoot) {

  var getPathSegment = function (node) {
    var name = getNodeName(node);
    var pos = getNodePosition(node);
    return `${name}[${pos}]`;
  };

  var rootNode = relativeRoot;

  var getPathTo = function (node) {
    var xpath;
    xpath = '',
      (() => {
        var result = [];
        while (node !== rootNode) {
          if (!(typeof node !== "undefined" && node !== null)) {
            throw new Error("Called getPathTo on a node which was not a descendant of @rootNode. " + rootNode);
          }
          xpath = (getPathSegment(node)) + '/' + xpath;
          result.push(node = node.parentNode);
        }
        return result;
      })();
    xpath = '/' + xpath;
    xpath = xpath.replace(/\/$/, '');
    return xpath;
  };

  var jq = this.map(function () {
    var path = getPathTo(this);

    return path;
  });

  return jq.get();
};

var findChild = function (node, type, index) {
  if (!node.hasChildNodes()) {
    throw new Error("XPath error: node has no children!");
  }
  var children = node.childNodes;
  var found = 0;
  for (var i = 0, child; i < children.length; i++) {
    child = children[i];
    var name = getNodeName(child);
    if (name === type) {
      found += 1;
      if (found === index) {
        return child;
      }
    }
  }
  throw new Error("XPath error: wanted child not found.");
};

// Get the node name for use in generating an xpath expression.
var getNodeName = function (node) {
  var nodeName = node.nodeName.toLowerCase();
  switch (nodeName) {
    case "#text": return "text()"; break;
    case "#comment": return "comment()"; break;
    case "#cdata-section": return "cdata-section()"; break;
    default: return nodeName;
  }
};

// Get the index of the node as it appears in its parent's child list
var getNodePosition = function (node) {
  var pos = 0;
  var tmp = node;
  while (tmp) {
    if (tmp.nodeName === node.nodeName) {
      pos++;
    }
    tmp = tmp.previousSibling;
  }
  return pos;
};

module.exports = {
  simpleXPathJQuery,
  simpleXPathPure,
};

});