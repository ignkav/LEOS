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
 * Fallback in-memory store if `localStorage` is not read/writable.
 */
class InMemoryStorage {
  constructor() {
    this._store = {};
  }

  getItem(key) {
    return (key in this._store) ? this._store[key] : null;
  }

  setItem(key, value) {
    this._store[key] = value;
  }

  removeItem(key) {
    delete this._store[key];
  }
}

/**
 * A wrapper around the `localStorage` API which provides a fallback to
 * in-memory storage in browsers that block access to `window.localStorage`.
 * in third-party iframes.
 */
// @ngInject
function localStorage($window) {
  let storage;
  let testKey = 'hypothesis.testKey';

  try {
    // Test whether we can read/write localStorage.
    storage = $window.localStorage;
    $window.localStorage.setItem(testKey, testKey);
    $window.localStorage.getItem(testKey);
    $window.localStorage.removeItem(testKey);
  } catch (e) {
    storage = new InMemoryStorage();
  }

  return {
    getItem(key) {
      return storage.getItem(key);
    },

    getObject(key) {
      var item = storage.getItem(key);
      return item ? JSON.parse(item) : null;
    },

    setItem(key, value) {
      storage.setItem(key, value);
    },

    setObject(key, value) {
      var repr = JSON.stringify(value);
      storage.setItem(key, repr);
    },

    removeItem(key) {
      storage.removeItem(key);
    },
  };
}

module.exports = localStorage;
