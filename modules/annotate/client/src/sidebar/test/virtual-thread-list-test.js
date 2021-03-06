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

var proxyquire = require('proxyquire');

var VirtualThreadList = proxyquire('../virtual-thread-list', {
  'lodash.debounce': function (fn) {
    // Make debounced functions execute immediately
    return fn;
  },
});
var util = require('../../shared/test/util');
var unroll = util.unroll;

describe('VirtualThreadList', function () {
  var lastState;
  var threadList;
  var threadOptions = {
    invisibleThreadFilter: null,
  };

  var fakeScope;
  var fakeScrollRoot;
  var fakeWindow;

  function idRange(start, end) {
    var ary = [];
    for (var i=start; i <= end; i++) {
      ary.push('t' + i.toString());
    }
    return ary;
  }

  function threadIDs(threads) {
    return threads.map(function (thread) { return thread.id; });
  }

  function generateRootThread(count) {
    return {
      annotation: undefined,
      children: idRange(0, count-1).map(function (id) {
        return {id: id, annotation: undefined, children: []};
      }),
    };
  }

  beforeEach(function () {
    fakeScope = {$digest: sinon.stub()};

    fakeScrollRoot = {
      scrollTop: 0,
      listeners: {},
      addEventListener: function (event, listener) {
        this.listeners[event] = this.listeners[event] || [];
        this.listeners[event].push(listener);
      },
      removeEventListener: function (event, listener) {
        this.listeners[event] = this.listeners[event].filter(function (fn) {
          return fn !== listener;
        });
      },
      trigger: function (event) {
        (this.listeners[event] || []).forEach(function (cb) {
          cb();
        });
      },
    };

    fakeWindow = {
      listeners: {},
      addEventListener: function (event, listener) {
        this.listeners[event] = this.listeners[event] || [];
        this.listeners[event].push(listener);
      },
      removeEventListener: function (event, listener) {
        this.listeners[event] = this.listeners[event].filter(function (fn) {
          return fn !== listener;
        });
      },
      trigger: function (event) {
        (this.listeners[event] || []).forEach(function (cb) {
          cb();
        });
      },
      innerHeight: 100,
    };

    threadOptions.invisibleThreadFilter = sinon.stub().returns(false);
    threadOptions.scrollRoot = fakeScrollRoot;

    var rootThread = {annotation: undefined, children: []};
    threadList = new VirtualThreadList(fakeScope, fakeWindow, rootThread, threadOptions);
    threadList.on('changed', function (state) {
      lastState = state;
    });
  });

  unroll('generates expected state when #when', function (testCase) {
    var thread = generateRootThread(testCase.threads);

    fakeScrollRoot.scrollTop = testCase.scrollOffset;
    fakeWindow.innerHeight = testCase.windowHeight;

    // make sure for everything that is not being presented in the
    // visible viewport, we pass it to this function.
    threadOptions.invisibleThreadFilter.returns(true);

    threadList.setRootThread(thread);

    var visibleIDs = threadIDs(lastState.visibleThreads);
    var invisibleIDs = threadIDs(lastState.invisibleThreads);
    assert.deepEqual(visibleIDs, testCase.expectedVisibleThreads);
    assert.equal(invisibleIDs.length, testCase.threads - testCase.expectedVisibleThreads.length);
    assert.equal(lastState.offscreenUpperHeight, testCase.expectedHeightAbove);
    assert.equal(lastState.offscreenLowerHeight, testCase.expectedHeightBelow);
  },[{
    when: 'scrollRoot is scrolled to top of list',
    threads: 100,
    scrollOffset: 0,
    windowHeight: 300,
    expectedVisibleThreads: idRange(0, 5),
    expectedHeightAbove: 0,
    expectedHeightBelow: 18800,
  },{
    when: 'scrollRoot is scrolled to middle of list',
    threads: 100,
    scrollOffset: 2000,
    windowHeight: 300,
    expectedVisibleThreads: idRange(5, 15),
    expectedHeightAbove: 1000,
    expectedHeightBelow: 16800,
  },{
    when: 'scrollRoot is scrolled to bottom of list',
    threads: 100,
    scrollOffset: 18800,
    windowHeight: 300,
    expectedVisibleThreads: idRange(89, 99),
    expectedHeightAbove: 17800,
    expectedHeightBelow: 0,
  }]);

  it('recalculates when a window.resize occurs', function () {
    lastState = null;
    fakeWindow.trigger('resize');
    assert.ok(lastState);
  });

  it('recalculates when a scrollRoot.scroll occurs', function () {
    lastState = null;
    fakeScrollRoot.trigger('scroll');
    assert.ok(lastState);
  });

  it('recalculates when root thread changes', function () {
    threadList.setRootThread({annotation: undefined, children: []});
    assert.ok(lastState);
  });

  describe('#setThreadHeight', function () {
    unroll('affects visible threads', function (testCase) {
      var thread = generateRootThread(10);
      fakeWindow.innerHeight = 500;
      fakeScrollRoot.scrollTop = 0;
      idRange(0,10).forEach(function (id) {
        threadList.setThreadHeight(id, testCase.threadHeight);
      });
      threadList.setRootThread(thread);
      assert.deepEqual(threadIDs(lastState.visibleThreads),
        testCase.expectedVisibleThreads);
    },[{
      threadHeight: 1000,
      expectedVisibleThreads: idRange(0,1),
    },{
      threadHeight: 300,
      expectedVisibleThreads: idRange(0,4),
    }]);
  });

  describe('#detach', function () {
    it('stops listening to window.resize events', function () {
      threadList.detach();
      lastState = null;
      fakeWindow.trigger('resize');
      assert.isNull(lastState);
    });
    it('stops listening to scrollRoot.scroll events', function () {
      threadList.detach();
      lastState = null;
      fakeScrollRoot.trigger('scroll');
      assert.isNull(lastState);
    });
  });

  describe('#yOffsetOf', function () {
    unroll('returns #offset as the Y offset of the #nth thread', function (testCase) {
      var thread = generateRootThread(10);
      threadList.setRootThread(thread);
      idRange(0, 10).forEach(function (id) {
        threadList.setThreadHeight(id, 100);
      });
      var id = idRange(testCase.index, testCase.index)[0];
      assert.equal(threadList.yOffsetOf(id), testCase.offset);
    }, [{
      nth: 'first',
      index: 0,
      offset: 0,
    },{
      nth: 'second',
      index: 1,
      offset: 100,
    },{
      nth: 'last',
      index: 9,
      offset: 900,
    }]);
  });
});
