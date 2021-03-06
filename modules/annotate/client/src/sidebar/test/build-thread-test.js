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

var buildThread = require('../build-thread');
var metadata = require('../annotation-metadata');

// Fixture with two top level annotations, one note and one reply
var SIMPLE_FIXTURE = [{
  id: '1',
  text: 'first annotation',
  references: [],
},{
  id: '2',
  text: 'second annotation',
  references: [],
},{
  id: '3',
  text: 'third annotation',
  references: [1],
}];

/**
 * Filter a Thread, keeping only properties in `keys` for each thread.
 *
 * @param {Thread} thread - Annotation thread generated by buildThread()
 * @param {Array<string>} keys - The keys to retain
 */
function filter(thread, keys) {
  var result = {};
  keys.forEach(function (key) {
    if (key === 'children') {
      result[key] = thread[key].map(function (child) {
        return filter(child, keys);
      });
    } else {
      result[key] = thread[key];
    }
  });
  return result;
}

/**
 * Threads a list of annotations and removes keys from the resulting Object
 * which do not match `keys`.
 *
 * @param {Array<Annotation>} fixture - List of annotations to thread
 * @param {Object?} opts - Options to pass to buildThread()
 * @param {Array<string>?} keys - List of keys to keep in the output
 */
function createThread(fixture, opts, keys) {
  opts = opts || {};
  keys = keys || [];

  var rootThread = filter(buildThread(fixture, opts), keys.concat(['annotation', 'children']));
  return rootThread.children;
}

describe('build-thread', function () {

  describe('threading', function () {
    it('arranges parents and children as a thread', function () {
      var thread = createThread(SIMPLE_FIXTURE);
      assert.deepEqual(thread, [{
        annotation: SIMPLE_FIXTURE[0],
        children: [{
          annotation: SIMPLE_FIXTURE[2],
          children: [],
        }],
      },{
        annotation: SIMPLE_FIXTURE[1],
        children: [],
      }]);
    });

    it('threads nested replies', function () {
      var NESTED_FIXTURE = [{
        id: '1',
        references: [],
      },{
        id: '2',
        references: ['1'],
      },{
        id: '3',
        references: ['1','2'],
      }];

      var thread = createThread(NESTED_FIXTURE);
      assert.deepEqual(thread, [{
        annotation: NESTED_FIXTURE[0],
        children: [{
          annotation: NESTED_FIXTURE[1],
          children: [{
            annotation: NESTED_FIXTURE[2],
            children: [],
          }],
        }],
      }]);
    });

    it('handles loops implied by the reply field', function () {
      var LOOPED_FIXTURE = [{
        id: '1',
        references: ['2'],
      },{
        id: '2',
        references: ['1'],
      }];

      var thread = createThread(LOOPED_FIXTURE);
      assert.deepEqual(thread, [{
        annotation: LOOPED_FIXTURE[1],
        children: [{
          annotation: LOOPED_FIXTURE[0],
          children: [],
        }],
      }]);
    });

    it('handles missing parent annotations', function () {
      var fixture = [{
        id: '1',
        references: ['3'],
      }];
      var thread = createThread(fixture);
      assert.deepEqual(thread, [{
        annotation: undefined,
        children: [{annotation: fixture[0], children: []}],
      }]);
    });

    it('handles missing replies', function () {
      var fixture = [{
        id: '1',
        references: ['3','2'],
      },{
        id: '3',
      }];
      var thread = createThread(fixture);
      assert.deepEqual(thread, [{
        annotation: fixture[1],
        children: [{
          annotation: undefined,
          children: [{
            annotation: fixture[0],
            children: [],
          }],
        }],
      }]);
    });

    it('threads new annotations which have tags but not IDs', function () {
      var fixture = [{
        $tag: 't1',
      }];
      var thread = createThread(fixture);
      assert.deepEqual(thread, [{annotation: fixture[0], children: []}]);
    });

    it('threads new replies which have tags but not IDs', function () {
      var fixture = [{
        id: '1',
        $tag: 't1',
      },{
        $tag: 't2',
        references: ['1'],
      }];
      var thread = createThread(fixture, {}, ['parent']);
      assert.deepEqual(thread, [{
        annotation: fixture[0],
        children: [{
          annotation: fixture[1],
          children: [],
          parent: '1',
        }],
        parent: undefined,
      }]);
    });
  });

  describe('collapsed state', function () {
    it('collapses top-level annotations by default', function () {
      var thread = buildThread(SIMPLE_FIXTURE, {});
      assert.isTrue(thread.children[0].collapsed);
    });

    it('expands replies by default', function () {
      var thread = buildThread(SIMPLE_FIXTURE, {});
      assert.isFalse(thread.children[0].children[0].collapsed);
    });

    it('expands threads which have been explicitly expanded', function () {
      var thread = buildThread(SIMPLE_FIXTURE, {
        expanded: {'1': true},
      });
      assert.isFalse(thread.children[0].collapsed);
    });

    it('collapses replies which have been explicitly collapsed', function () {
      var thread = buildThread(SIMPLE_FIXTURE, {
        expanded: {'3': false},
      });
      assert.isTrue(thread.children[0].children[0].collapsed);
    });

    it('expands threads with visible children', function () {
      // Simulate performing a search which only matches the top-level
      // annotation, not its reply, and then clicking
      // 'View N more in conversation' to show the complete discussion thread
      var thread = buildThread(SIMPLE_FIXTURE, {
        filterFn: function (annot) {
          return annot.text.match(/first/);
        },
        forceVisible: ['3'],
      });
      assert.isFalse(thread.children[0].collapsed);
    });
  });

  describe('filtering', function () {
    context('when there is an active filter', function () {
      it('shows only annotations that match the filter', function () {
        var threads = createThread(SIMPLE_FIXTURE, {
          filterFn: function (annot) { return annot.text.match(/first/); },
        }, ['visible']);
        assert.deepEqual(threads, [{
          annotation: SIMPLE_FIXTURE[0],
          children: [{
            annotation: SIMPLE_FIXTURE[2],
            children: [],
            visible: false,
          }],
          visible: true,
        }]);
      });

      it('shows threads containing replies that match the filter', function () {
        var threads = createThread(SIMPLE_FIXTURE, {
          filterFn: function (annot) { return annot.text.match(/third/); },
        }, ['visible']);
        assert.deepEqual(threads, [{
          annotation: SIMPLE_FIXTURE[0],
          children: [{
            annotation: SIMPLE_FIXTURE[2],
            children: [],
            visible: true,
          }],
          visible: false,
        }]);
      });

      it('show annotation being created despite filter', function () {
        const newAnnotation = {
          text: 'New',
          references: [],
        };
        const annotations = SIMPLE_FIXTURE.concat([newAnnotation]);
        const threads = createThread(annotations, {
          filterFn: function (annot) { return annot.text.match(/first/); },
        }, ['visible']);
        assert.deepEqual(threads, [{
          annotation: SIMPLE_FIXTURE[0],
          children: [{
            annotation: SIMPLE_FIXTURE[2],
            children: [],
            visible: false,
          }],
          visible: true,
        }, {
          annotation: newAnnotation,
          children: [],
          visible: true,
        }]);
      });
    });

    describe('thread filtering', function () {
      var fixture = [{
        id: '1',
        text: 'annotation',
        target: [{selector: {}}],
      },{
        id: '2',
        text: 'note',
        target: [{selector: undefined}],
      }];

      it('shows only annotations matching the thread filter', function () {
        var thread = createThread(fixture, {
          threadFilterFn: function (thread) {
            return metadata.isPageNote(thread.annotation);
          },
        });

        assert.deepEqual(thread, [{
          annotation: fixture[1],
          children: [],
        }]);
      });
    });
  });

  describe('sort order', function () {
    var annots = function (threads) {
      return threads.map(function (thread) { return thread.annotation; });
    };

    it('sorts top-level annotations using the comparison function', function () {
      var fixture = [{
        id: '1',
        updated: 100,
        references: [],
      },{
        id: '2',
        updated: 200,
        references: [],
      }];

      var thread = createThread(fixture, {
        sortCompareFn: function (a, b) {
          return a.updated > b.updated;
        },
      });
      assert.deepEqual(annots(thread), [fixture[1], fixture[0]]);
    });

    it('sorts replies by creation date', function () {
      var fixture = [{
        id: '1',
        references: [],
        updated: 0,
      },{
        id: '3',
        references: ['1'],
        created: 100,
      },{
        id: '2',
        references: ['1'],
        created: 50,
      }];
      var thread = createThread(fixture, {
        sortCompareFn: function (a, b) { return a.id < b.id; },
      });
      assert.deepEqual(annots(thread[0].children),
        [fixture[2], fixture[1]]);
    });
  });

  describe('reply counts', function () {
    it('populates the reply count field', function () {
      assert.deepEqual(createThread(SIMPLE_FIXTURE, {}, ['replyCount']), [{
        annotation: SIMPLE_FIXTURE[0],
        children: [{
          annotation: SIMPLE_FIXTURE[2],
          children: [],
          replyCount: 0,
        }],
        replyCount: 1,
      },{
        annotation: SIMPLE_FIXTURE[1],
        children: [],
        replyCount: 0,
      }]);
    });
  });

  describe('depth', function () {
    it('is 0 for annotations', function () {
      var thread = createThread(SIMPLE_FIXTURE, {}, ['depth']);
      assert.deepEqual(thread[0].depth, 0);
    });

    it('is 1 for top-level replies', function () {
      var thread = createThread(SIMPLE_FIXTURE, {}, ['depth']);
      assert.deepEqual(thread[0].children[0].depth, 1);
    });
  });

  describe('highlighting', function () {
    it('does not set highlight state when none are highlighted', function () {
      var thread = createThread(SIMPLE_FIXTURE, {}, ['dimmed']);
      thread.forEach(function (child) {
        assert.equal(child.highlightState, undefined);
      });
    });

    it('highlights annotations', function () {
      var thread = createThread(SIMPLE_FIXTURE, {highlighted: ['1']}, ['highlightState']);
      assert.equal(thread[0].highlightState, 'highlight');
    });

    it('dims annotations which are not highlighted', function () {
      var thread = createThread(SIMPLE_FIXTURE, {highlighted: ['1']}, ['highlightState']);
      assert.equal(thread[1].highlightState, 'dim');
    });
  });
});
