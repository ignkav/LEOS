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

var angular = require('angular');

var EventEmitter = require('tiny-emitter');
var immutable = require('seamless-immutable');

var events = require('../../events');
var threadList = require('../thread-list');
var util = require('../../directive/test/util');

var annotFixtures = immutable({
  annotation: {$tag: 't1', id: '1', text: 'text'},
  reply: {
    $tag: 't2',
    id: '2',
    references: ['1'],
    text: 'areply',
  },
  highlight: {$highlight: true, $tag: 't3', id: '3'},
});

var threadFixtures = immutable({
  thread: {
    children: [{
      id: annotFixtures.annotation.id,
      annotation: annotFixtures.annotation,
      children: [{
        id: annotFixtures.reply.id,
        annotation: annotFixtures.reply,
        children: [],
        visible: true,
      }],
      visible: true,
    },{
      id: annotFixtures.highlight.id,
      annotation: annotFixtures.highlight,
    }],
  },
});

var fakeVirtualThread;
var fakeFrameSync = {
  LEOS_selectAnnotation: sinon.stub(),
  scrollToAnnotation: sinon.stub(),
};
var fakeSettings = {};
var fakeStore = {
  isAnnotationSelected: sinon.stub().returns(true),
  toggleSelectedAnnotations: sinon.stub(),
};

class FakeVirtualThreadList extends EventEmitter {
  constructor($scope, $window, rootThread, options) {
    super();

    fakeVirtualThread = this; // eslint-disable-line consistent-this

    var thread = rootThread;

    this.options = options;
    this.setRootThread = function (_thread) {
      thread = _thread;
    };
    this.notify = function () {
      this.emit('changed', {
        offscreenLowerHeight: 10,
        offscreenUpperHeight: 20,
        visibleThreads: thread.children,
      });
    };
    this.detach = sinon.stub();
    this.yOffsetOf = function () {
      return 42;
    };
  }
}

describe('threadList', function () {
  function createThreadList(inputs) {
    var defaultInputs = {
      thread: threadFixtures.thread,
      onForceVisible: sinon.stub(),
      onFocus: sinon.stub(),
      onSelect: sinon.stub(),
      onSetCollapsed: sinon.stub(),
    };

    var parentEl = document.createElement('div');
    document.body.appendChild(parentEl);

    // Create the `<thread-list>` instance
    var element = util.createDirective(
      document,
      'threadList',
      Object.assign({}, defaultInputs, inputs),
      {}, // initialScope
      '', // initialHtml
      { parentElement: parentEl }
    );

    element[0].parentEl = parentEl;
    
    // Make container scrollable.
    element[0].style.display = 'block';
    element[0].style.height = '100px';
    element[0].style.overflowY = 'scroll';

    // Add an element inside the scrollable container which is much taller than the container, so that it actually becomes scrollable.
    var tallDiv = document.createElement('div');
    tallDiv.style.height = '1000px';
    element[0].prepend(tallDiv);

    return element;
  }

  before(function () {
    angular.module('app', [])
      .component('threadList', threadList);
  });

  beforeEach(function () {
    angular.mock.module('app', {
      VirtualThreadList: FakeVirtualThreadList,
      frameSync: fakeFrameSync,
      settings: fakeSettings,
      store: fakeStore,
    });
  });

  it('shows the clean theme when settings contains the clean theme option', function () {
    angular.mock.module('app', {
      VirtualThreadList: FakeVirtualThreadList,
      settings: { theme: 'clean'},
    });
    var element = createThreadList();
    fakeVirtualThread.notify();
    element.scope.$digest();
    assert.equal(element[0].querySelectorAll('.thread-list__card--theme-clean').length, element[0].querySelectorAll('annotation-thread').length);
  });

  it('displays the children of the root thread', function () {
    var element = createThreadList();
    fakeVirtualThread.notify();
    element.scope.$digest();
    var children = element[0].querySelectorAll('annotation-thread');
    assert.equal(children.length, 2);
  });

  describe('when a new annotation is created', function () {
    it('scrolls the annotation into view', function () {
      var element = createThreadList();
      element[0].scrollTop = 500;

      var annot = annotFixtures.annotation;
      element.scope.$broadcast(events.BEFORE_ANNOTATION_CREATED, annot);
      
      assert.isBelow(element[0].scrollTop, 100);
    });

    it('does not scroll the annotation into view if it is a reply', function () {
      var element = createThreadList();
      element[0].scrollTop = 500;

      var reply = annotFixtures.reply;
      element.scope.$broadcast(events.BEFORE_ANNOTATION_CREATED, reply);

      assert.equal(element[0].scrollTop, 500);
    });

    it('does not scroll the annotation into view if it is a highlight', function () {
      var element = createThreadList();
      element[0].scrollTop = 500;

      var highlight = annotFixtures.highlight;
      element.scope.$broadcast(events.BEFORE_ANNOTATION_CREATED, highlight);

      assert.equal(element[0].scrollTop, 500);
    });
  });

  it('calls onFocus() when the user hovers an annotation', function () {
    var inputs = {
      onFocus: {
        args: ['annotation'],
        callback: sinon.stub(),
      },
    };
    var element = createThreadList(inputs);
    fakeVirtualThread.notify();
    element.scope.$digest();
    var annotation = element[0].querySelector('.thread-list__card');
    util.sendEvent(annotation, 'mouseover');
    assert.calledWithMatch(inputs.onFocus.callback,
      sinon.match(annotFixtures.annotation));
  });

  it('calls onSelect() when a user clicks an annotation', function () {
    var inputs = {
      onSelect: {
        args: ['annotation'],
        callback: sinon.stub(),
      },
    };
    var element = createThreadList(inputs);
    fakeVirtualThread.notify();
    element.scope.$digest();
    var annotation = element[0].querySelector('.thread-list__card');
    util.sendEvent(annotation, 'click');
    assert.calledWith(fakeStore.toggleSelectedAnnotations, [annotFixtures.annotation.id]);
  });

  it('uses the correct scroll root', function () {
    createThreadList();
    var scrollRoot = fakeVirtualThread.options.scrollRoot;
    assert.isTrue(scrollRoot.classList.contains('ng-scope'));
  });
});
