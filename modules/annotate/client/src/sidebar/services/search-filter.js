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

/**
 * Splits a search term into filter and data.
 *
 * ie. 'user:johndoe' -> ['user', 'johndoe']
 *     'example:text' -> [null, 'example:text']
 */
function splitTerm(term) {
  let filter = term.slice(0, term.indexOf(':'));
  if (!filter) {
    // The whole term is data
    return [null, term];
  }

  if (['group', 'quote', 'result', 'since',
       'tag', 'text', 'uri', 'user', 'user_name'].includes(filter)) {
    let data = term.slice(filter.length+1);
    return [filter, data];
  } else {
    // The filter is not a power search filter, so the whole term is data
    return [null, term];
  }
}

/**
 * Tokenize a search query.
 *
 * Splits `searchtext` into tokens, separated by spaces.
 * Quoted phrases in `searchtext` are returned as a single token.
 */
function tokenize(searchtext) {
  if (!searchtext) { return []; }

  // Small helper function for removing quote characters
  // from the beginning- and end of a string, if the
  // quote characters are the same.
  // I.e.
  //   'foo' -> foo
  //   "bar" -> bar
  //   'foo" -> 'foo"
  //   bar"  -> bar"
  let _removeQuoteCharacter = function(text) {
    let start = text.slice(0,1);
    let end = text.slice(-1);
    if (((start === '"') || (start === "'")) && (start === end)) {
      text = text.slice(1, text.length - 1);
    }
    return text;
  };

  let tokens = searchtext.match(/(?:[^\s"']+|"[^"]*"|'[^']*')+/g);

  // Cut the opening and closing quote characters
  tokens = tokens.map(_removeQuoteCharacter);

  // Remove quotes for power search.
  // I.e. 'tag:"foo bar"' -> 'tag:foo bar'
  for (let index = 0; index < tokens.length; index++) {
    let token = tokens[index];
    let [filter, data] = splitTerm(token);
    if (filter) {
      tokens[index] = filter + ':' + (_removeQuoteCharacter(data));
    }
  }

  return tokens;
}

/**
 * Parse a search query into a map of search field to term.
 *
 * @param {string} searchtext
 * @return {Object}
 */
function toObject(searchtext) {
  let obj = {};
  let backendFilter = f => f === 'tag' ? 'tags' : f;

  let addToObj = function(key, data) {
    if (obj[key]) {
      return obj[key].push(data);
    } else {
      return obj[key] = [data];
    }
  };

  if (searchtext) {
    let terms = tokenize(searchtext);
    for (let term of terms) {
      let [filter, data] = splitTerm(term);
      if (!filter) {
        filter = 'any';
        data = term;
      }
      addToObj(backendFilter(filter), data);
    }
  }
  return obj;
}

/**
 * @typedef Facet
 * @property {'and'|'or'|'min'} operator
 * @property {boolean} lowercase
 * @property {string[]} terms
 */

/**
 * Parse a search query into a map of filters.
 *
 * Returns an object mapping facet names to Facet.
 *
 * Terms that are not associated with a particular facet are stored in the "any"
 * facet.
 *
 * @param {string} searchtext
 * @return {Object}
 */
function generateFacetedFilter(searchtext) {
  let terms;
  let any = [];
  let quote = [];
  let result = [];
  let since = [];
  let tag = [];
  let text = [];
  let uri = [];
  let user = [];
  //LEOS change
  let group = [];
  let status = [];
  let user_name = [];

  if (searchtext) {
    terms = tokenize(searchtext);
    for (let term of terms) {
      let t;
      let colon_index = term.indexOf(':');
      let filter = term.slice(0, colon_index);
      let value = term.slice(colon_index+1);
      switch (filter) {
      case 'quote':
        quote.push(value);
        break;
      case 'result':
        result.push(value);
        break;
      case 'since':
        {
          // We'll turn this into seconds
          let time = value.toLowerCase();
          if (time.match(/^\d+$/)) {
            // Only digits, assuming seconds
            since.push(time * 1);
          }
          if (time.match(/^\d+sec$/)) {
            // Time given in seconds
            t = /^(\d+)sec$/.exec(time)[1];
            since.push(t * 1);
          }
          if (time.match(/^\d+min$/)) {
            // Time given in minutes
            t = /^(\d+)min$/.exec(time)[1];
            since.push(t * 60);
          }
          if (time.match(/^\d+hour$/)) {
            // Time given in hours
            t = /^(\d+)hour$/.exec(time)[1];
            since.push(t * 60 * 60);
          }
          if (time.match(/^\d+day$/)) {
            // Time given in days
            t = /^(\d+)day$/.exec(time)[1];
            since.push(t * 60 * 60 * 24);
          }
          if (time.match(/^\d+week$/)) {
            // Time given in week
            t = /^(\d+)week$/.exec(time)[1];
            since.push(t * 60 * 60 * 24 * 7);
          }
          if (time.match(/^\d+month$/)) {
            // Time given in month
            t = /^(\d+)month$/.exec(time)[1];
            since.push(t * 60 * 60 * 24 * 30);
          }
          if (time.match(/^\d+year$/)) {
            // Time given in year
            t = /^(\d+)year$/.exec(time)[1];
            since.push(t * 60 * 60 * 24 * 365);
          }
        }
        break;
      case 'tag': tag.push(value); break;
      case 'text': text.push(value); break;
      case 'uri': uri.push(value); break;
      case 'user': user.push(value); break;
      //LEOS change
      case 'group': group.push(value); break;
      case 'status': status.push(value); break;
      case 'user_name': user_name.push(value); break;
      default: any.push(term);
      }
    }
  }

  return {
    any: {
      terms: any,
      operator: 'and',
    },
    quote: {
      terms: quote,
      operator: 'and',
    },
    result: {
      terms: result,
      operator: 'min',
    },
    since: {
      terms: since,
      operator: 'and',
    },
    tag: {
      terms: tag,
      operator: 'and',
    },
    text: {
      terms: text,
      operator: 'and',
    },
    uri: {
      terms: uri,
      operator: 'or',
    },
    user: {
      terms: user,
      operator: 'or',
    },
    //LEOS change
    group: {
      terms: group,
      operator: 'or',
    },
    status: {
      terms: status,
      operator: 'or',
    },
    user_name: {
      terms: user_name,
      operator: 'or',
    },
  };
}

// @ngInject
function searchFilter() {
  return {
    toObject,
    generateFacetedFilter,
  };
}

module.exports = searchFilter;
