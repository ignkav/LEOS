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
 * Central store of state for the sidebar application, managed using
 * [Redux](http://redux.js.org/).
 *
 * State management in Redux apps work as follows:
 *
 *  1. All important application state is stored in a single, immutable object.
 *  2. The user interface is a presentation of this state. Interaction with the
 *     UI triggers updates by creating `actions`.
 *  3. Actions are plain JS objects which describe some event that happened in
 *     the application. Updates happen by passing actions to a `reducer`
 *     function which takes the current application state, the action and
 *     returns the new application state.
 *
 *     The process of updating the app state using an action is known as
 *     'dispatching' the action.
 *  4. Other parts of the app can subscribe to changes in the app state.
 *     This is used to to update the UI etc.
 *
 * "middleware" functions can wrap the dispatch process in order to implement
 *  logging, trigger side effects etc.
 *
 * Tests for a given action consist of:
 *
 *  1. Checking that the UI (or other event source) dispatches the correct
 *     action when something happens.
 *  2. Checking that given an initial state, and an action, a reducer returns
 *     the correct resulting state.
 *  3. Checking that the UI correctly presents a given state.
 */

var createStore = require('./create-store');
var debugMiddleware = require('./debug-middleware');

var annotations = require('./modules/annotations');
var frames = require('./modules/frames');
var links = require('./modules/links');
var groups = require('./modules/groups');
var selection= require('./modules/selection');
var session = require('./modules/session');
var viewer = require('./modules/viewer');

/**
 * Redux middleware which triggers an Angular change-detection cycle
 * if no cycle is currently in progress.
 *
 * This ensures that Angular UI components are updated after the UI
 * state changes in response to external inputs (eg. WebSocket messages,
 * messages arriving from other frames in the page, async network responses).
 *
 * See http://redux.js.org/docs/advanced/Middleware.html
 */
function angularDigestMiddleware($rootScope) {
  return function (next) {
    return function (action) {
      next(action);

      // '$$phase' is set if Angular is in the middle of a digest cycle already
      if (!$rootScope.$$phase) {
        // $applyAsync() is similar to $apply() but provides debouncing.
        // See http://stackoverflow.com/questions/30789177
        $rootScope.$applyAsync(function () {});
      }
    };
  };
}

/**
 * Factory which creates the sidebar app's state store.
 *
 * Returns a Redux store augmented with methods for each action and selector in
 * the individual state modules. ie. `store.actionName(args)` dispatches an
 * action through the store and `store.selectorName(args)` invokes a selector
 * passing the current state of the store.
 */
// @ngInject
function store($rootScope, settings) {
  var middleware = [
    debugMiddleware,
    angularDigestMiddleware.bind(null, $rootScope),
  ];

  var modules = [
    annotations,
    frames,
    links,
    groups,
    selection,
    session,
    viewer,
  ];
  return createStore(modules, [settings], middleware);
}

module.exports = store;
