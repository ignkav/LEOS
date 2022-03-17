proxyquire = require('proxyquire')

adder = require('../adder')
Observable = require('../util/observable').Observable
Plugin = require('../plugin')

Delegator = require('../delegator')
$ = require('jquery')
Delegator['@noCallThru'] = true

Guest = null
anchoring = {}
highlighter = {}
rangeUtil = null
selections = null

raf = sinon.stub().yields()
raf['@noCallThru'] = true

scrollIntoView = sinon.stub()
scrollIntoView['@noCallThru'] = true

class FakeAdder
  instance: null

  constructor: ->
    FakeAdder::instance = this

    this.hide = sinon.stub()
    this.showAt = sinon.stub()
    this.target = sinon.stub()

class FakePlugin extends Plugin
  instance: null
  events:
    'customEvent': 'customEventHandler'

  constructor: ->
    FakePlugin::instance = this
    super

  pluginInit: sinon.stub()
  customEventHandler: sinon.stub()

# A little helper which returns a promise that resolves after a timeout
timeoutPromise = (millis = 0) ->
  new Promise((resolve) -> setTimeout(resolve, millis))

describe 'Guest', ->
  consoleWarnSpy = null
  CrossFrame = null
  fakeCrossFrame = null
  guestConfig = null

  createGuest = (config={}) ->
    config = Object.assign({}, guestConfig, config)
    element = document.createElement('div')
    return new Guest(element, config)

  beforeEach ->
    consoleWarnSpy = sinon.stub(console, 'warn')

    FakeAdder::instance = null
    rangeUtil = {
      isSelectionBackwards: sinon.stub()
      selectionFocusRect: sinon.stub()
    }
    selections = null
    guestConfig = {pluginClasses: {}}

    Guest = proxyquire('../guest', {
      './adder': {Adder: FakeAdder},
      './anchoring/html': anchoring,
      './highlighter': highlighter,
      './range-util': rangeUtil,
      './selections': (document) ->
        new Observable((obs) ->
          selections = obs
          return () ->
        )
      './delegator': Delegator,
      'raf': raf,
      'scroll-into-view': scrollIntoView,
    })

    fakeCrossFrame = {
      onConnect: sinon.stub()
      on: sinon.stub()
      call: sinon.stub()
      sync: sinon.stub()
      destroy: sinon.stub()
    }

    CrossFrame = sinon.stub().returns(fakeCrossFrame)
    guestConfig.pluginClasses['CrossFrame'] = CrossFrame

  afterEach ->
    sinon.restore()
    consoleWarnSpy.restore()

  describe 'plugins', ->
    fakePlugin = null
    guest = null

    beforeEach ->
      FakePlugin::instance = null
      guestConfig.pluginClasses['FakePlugin'] = FakePlugin
      guest = createGuest(FakePlugin: {})
      fakePlugin = FakePlugin::instance

    it 'load and "pluginInit" gets called', ->
      assert.calledOnce(fakePlugin.pluginInit)

    it 'hold reference to instance', ->
      assert.equal(fakePlugin.annotator, guest)

    it 'subscribe to events', ->
      guest.publish('customEvent', ['1', '2'])
      assert.calledWith(fakePlugin.customEventHandler, '1', '2')

    it 'destroy when instance is destroyed', ->
      sinon.spy(fakePlugin, 'destroy')
      guest.destroy()
      assert.called(fakePlugin.destroy)

  describe 'cross frame', ->

    it 'provides an event bus for the annotation sync module', ->
      guest = createGuest()
      options = CrossFrame.lastCall.args[1]
      assert.isFunction(options.on)
      assert.isFunction(options.emit)

    it 'publishes the "panelReady" event when a connection is established', ->
      handler = sinon.stub()
      guest = createGuest()
      guest.subscribe('panelReady', handler)
      fakeCrossFrame.onConnect.yield()
      assert.called(handler)

    describe 'event subscription', ->
      options = null
      guest = null

      beforeEach ->
        guest = createGuest()
        options = CrossFrame.lastCall.args[1]

      it 'proxies the event into the annotator event system', ->
        fooHandler = sinon.stub()
        barHandler = sinon.stub()

        options.on('foo', fooHandler)
        options.on('bar', barHandler)

        guest.publish('foo', ['1', '2'])
        guest.publish('bar', ['1', '2'])

        assert.calledWith(fooHandler, '1', '2')
        assert.calledWith(barHandler, '1', '2')

    describe 'event publication', ->
      options = null
      guest = null

      beforeEach ->
        guest = createGuest()
        options = CrossFrame.lastCall.args[1]

      it 'detaches annotations on "annotationDeleted"', ->
        ann = {id: 1, $tag: 'tag1'}
        sinon.stub(guest, 'detach')
        options.emit('annotationDeleted', ann)
        assert.calledOnce(guest.detach)
        assert.calledWith(guest.detach, ann)

      it 'anchors annotations on "annotationsLoaded"', ->
        ann1 = {id: 1, $tag: 'tag1'}
        ann2 = {id: 2, $tag: 'tag2'}
        sinon.stub(guest, 'anchor')
        options.emit('annotationsLoaded', [ann1, ann2])
        assert.calledTwice(guest.anchor)
        assert.calledWith(guest.anchor, ann1)
        assert.calledWith(guest.anchor, ann2)

      it 'proxies all other events into the annotator event system', ->
        fooHandler = sinon.stub()
        barHandler = sinon.stub()

        guest.subscribe('foo', fooHandler)
        guest.subscribe('bar', barHandler)

        options.emit('foo', '1', '2')
        options.emit('bar', '1', '2')

        assert.calledWith(fooHandler, '1', '2')
        assert.calledWith(barHandler, '1', '2')

  describe 'annotation UI events', ->
    emitGuestEvent = (event, args...) ->
      fn(args...) for [evt, fn] in fakeCrossFrame.on.args when event == evt

    describe 'on "scrollToAnnotation" event', ->

      beforeEach ->
        scrollIntoView.resetHistory()

      it 'scrolls to the anchor with the matching tag', ->
        highlight = $('<span></span>')
        guest = createGuest()
        guest.anchors = [
          {annotation: {$tag: 'tag1'}, highlights: highlight.toArray()}
        ]
        emitGuestEvent('scrollToAnnotation', 'tag1')
        assert.called(scrollIntoView)
        assert.calledWith(scrollIntoView, highlight[0])

      context 'when dispatching the "scrolltorange" event', ->

        it 'emits with the range', ->
          highlight = $('<span></span>')
          guest = createGuest()
          fakeRange = sinon.stub()
          guest.anchors = [
            {annotation: {$tag: 'tag1'}, highlights: highlight.toArray(), range: fakeRange}
          ]

          return new Promise (resolve) ->
            guest.element.on 'scrolltorange', (event) ->
              assert.equal(event.detail, fakeRange)
              resolve()

            emitGuestEvent('scrollToAnnotation', 'tag1')

        it 'allows the default scroll behaviour to be prevented', ->
          highlight = $('<span></span>')
          guest = createGuest()
          fakeRange = sinon.stub()
          guest.anchors = [
            {annotation: {$tag: 'tag1'}, highlights: highlight.toArray(), range: fakeRange}
          ]

          guest.element.on 'scrolltorange', (event) -> event.preventDefault()
          emitGuestEvent('scrollToAnnotation', 'tag1')
          assert.notCalled(scrollIntoView)


    describe 'on "getDocumentInfo" event', ->
      guest = null

      beforeEach ->
        document.title = 'hi'
        guest = createGuest()
        guest.plugins.PDF =
          uri: sinon.stub().returns(window.location.href)
          getMetadata: sinon.stub()

      afterEach ->
        sinon.restore()

      it 'calls the callback with the href and pdf metadata', (done) ->
        assertComplete = (err, payload) ->
          try
            assert.equal(payload.uri, document.location.href)
            assert.equal(payload.metadata, metadata)
            done()
          catch e
            done(e)

        metadata = {title: 'hi'}
        promise = Promise.resolve(metadata)
        guest.plugins.PDF.getMetadata.returns(promise)

        emitGuestEvent('getDocumentInfo', assertComplete)

      it 'calls the callback with the href and basic metadata if pdf fails', (done) ->
        assertComplete = (err, payload) ->
          try
            assert.equal(payload.uri, window.location.href)
            assert.deepEqual(payload.metadata, metadata)
            done()
          catch e
            done(e)

        metadata = {title: 'hi', link: [{href: window.location.href}]}
        promise = Promise.reject(new Error('Not a PDF document'))
        guest.plugins.PDF.getMetadata.returns(promise)

        emitGuestEvent('getDocumentInfo', assertComplete)

  describe 'when the selection changes', ->
    it 'shows the adder if the selection contains text', ->
      guest = createGuest()
      rangeUtil.selectionFocusRect.returns({left: 0, top: 0, width: 5, height: 5})
      FakeAdder::instance.target.returns({
        left: 0, top: 0, arrowDirection: adder.ARROW_POINTING_UP
      })
      selections.next({})
      assert.called FakeAdder::instance.showAt

    it 'hides the adder if the selection does not contain text', ->
      guest = createGuest()
      rangeUtil.selectionFocusRect.returns(null)
      selections.next({})
      assert.called FakeAdder::instance.hide
      assert.notCalled FakeAdder::instance.showAt

    it 'hides the adder if the selection is empty', ->
      guest = createGuest()
      selections.next(null)
      assert.called FakeAdder::instance.hide

  describe '#getDocumentInfo()', ->
    guest = null

    beforeEach ->
      guest = createGuest()
      guest.plugins.PDF =
        uri: -> 'urn:x-pdf:001122'
        getMetadata: sinon.stub()

    it 'preserves the components of the URI other than the fragment', ->
      guest.plugins.PDF = null
      guest.plugins.Document =
        uri: -> 'http://foobar.com/things?id=42'
        metadata: {}
      return guest.getDocumentInfo().then ({uri}) ->
        assert.equal uri, 'http://foobar.com/things?id=42'

    it 'removes the fragment identifier from URIs', () ->
      guest.plugins.PDF.uri = -> 'urn:x-pdf:aabbcc#'
      return guest.getDocumentInfo().then ({uri}) ->
        assert.equal uri, 'urn:x-pdf:aabbcc'

  describe '#createAnnotation()', ->
    it 'adds metadata to the annotation object', ->
      guest = createGuest()
      sinon.stub(guest, 'getDocumentInfo').returns(Promise.resolve({
        metadata: {title: 'hello'}
        uri: 'http://example.com/'
      }))
      annotation = {}

      guest.createAnnotation(annotation)

      timeoutPromise()
      .then ->
        assert.equal(annotation.uri, 'http://example.com/')
        assert.deepEqual(annotation.document, {title: 'hello'})

    it 'treats an argument as the annotation object', ->
      guest = createGuest()
      annotation = {foo: 'bar'}
      annotation = guest.createAnnotation(annotation)
      assert.equal(annotation.foo, 'bar')

    it 'triggers a beforeAnnotationCreated event', (done) ->
      guest = createGuest()
      guest.subscribe('beforeAnnotationCreated', -> done())

      guest.createAnnotation()

  describe '#createComment()', ->
    it 'adds metadata to the annotation object', ->
      guest = createGuest()
      sinon.stub(guest, 'getDocumentInfo').returns(Promise.resolve({
        metadata: {title: 'hello'}
        uri: 'http://example.com/'
      }))

      annotation = guest.createComment()

      timeoutPromise()
      .then ->
        assert.equal(annotation.uri, 'http://example.com/')
        assert.deepEqual(annotation.document, {title: 'hello'})

    it 'adds a single target with a source property', ->
      guest = createGuest()
      sinon.stub(guest, 'getDocumentInfo').returns(Promise.resolve({
        metadata: {title: 'hello'}
        uri: 'http://example.com/'
      }))

      annotation = guest.createComment()

      timeoutPromise()
      .then ->
        assert.deepEqual(annotation.target, [{source: 'http://example.com/'}])

    it 'triggers a beforeAnnotationCreated event', (done) ->
      guest = createGuest()
      guest.subscribe('beforeAnnotationCreated', -> done())

      guest.createComment()

  describe '#anchor()', ->
    el = null
    txt = null
    range = null

    beforeEach ->
      el = document.createElement('span')
      txt = document.createTextNode('hello')
      txt.id = 'test-element'
      el.appendChild(txt)
      document.body.appendChild(el)
      range = document.createRange()
      range.selectNode(el)

    afterEach ->
      document.body.removeChild(el)

    it "doesn't mark an annotation lacking targets as an orphan", ->
      guest = createGuest()
      annotation = target: []

      guest.anchor(annotation).then ->
        assert.isFalse(annotation.$orphan)

    it "doesn't mark an annotation with a selectorless target as an orphan", ->
      guest = createGuest()
      annotation = {target: [{source: 'wibble'}]}

      guest.anchor(annotation).then ->
        assert.isFalse(annotation.$orphan)

    it "doesn't mark an annotation with only selectorless targets as an orphan", ->
      guest = createGuest()
      annotation = {target: [{source: 'foo'}, {source: 'bar'}]}

      guest.anchor(annotation).then ->
        assert.isFalse(annotation.$orphan)

    it "doesn't mark an annotation in which the target anchors as an orphan", ->
      guest = createGuest()
      annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]},
        ]
      }
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))

      guest.anchor(annotation).then ->
        assert.isFalse(annotation.$orphan)

    it "doesn't mark an annotation in which at least one target anchors as an orphan", ->
      guest = createGuest()
      annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'notinhere'}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]},
        ]
      }
      sinon.stub(anchoring, 'anchor')
        .onFirstCall().returns(Promise.reject())
        .onSecondCall().returns(Promise.resolve(range))

      guest.anchor(annotation).then ->
        assert.isFalse(annotation.$orphan)

    it "marks an annotation in which the target fails to anchor as an orphan", ->
      guest = createGuest()
      annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'notinhere'}]},
        ]
      }
      sinon.stub(anchoring, 'anchor').returns(Promise.reject())

      guest.anchor(annotation).then ->
        assert.isTrue(annotation.$orphan)

    it "marks an annotation in which all (suitable) targets fail to anchor as an orphan", ->
      guest = createGuest()
      annotation = {
        target: [
          {selector: [{type: 'TextQuoteSelector', exact: 'notinhere'}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'neitherami'}]},
        ]
      }
      sinon.stub(anchoring, 'anchor').returns(Promise.reject())

      guest.anchor(annotation).then ->
        assert.isTrue(annotation.$orphan)

    it "marks an annotation where the target has no TextQuoteSelectors as an orphan", ->
      guest = createGuest()
      annotation = {
        target: [
          {selector: [{type: 'TextPositionSelector', start: 0, end: 5}]},
        ]
      }
      # This shouldn't be called, but if it is, we successfully anchor so that
      # this test is guaranteed to fail.
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))

      guest.anchor(annotation).then ->
        assert.isTrue(annotation.$orphan)

    it "#removeRangeIgnoringPrefix(): mark a non locatable comment/annotation as an orphan", ->
      guest = createGuest()
      annotation = {
        tags: [
          'comment'
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'ell', prefix: 'i', suffix: 'o', start: 1, end: 4}]},
        ],
      }

      range = document.createRange()
      range.setStart(txt, 1)
      range.setEnd(txt, 4)
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))

      guest.anchor(annotation).then ->
        assert.isTrue(annotation.$orphan)

    it "#removeRangeWithoutExactTextMatch(): mark a locatable comment/annotation (but containing modified text though) as an orphan", ->
      guest = createGuest()
      annotation = {
        tags: [
          'comment'
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'elx', prefix: 'h', suffix: 'o', start: 1, end: 4}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'elx'}]},
        ],
      }

      range = document.createRange()
      range.setStart(txt, 1)
      range.setEnd(txt, 4)

      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))

      guest.anchor(annotation).then ->
        assert.isTrue(annotation.$orphan)

    it "#removeRangeWithoutExactTextMatch(): don't mark a valid suggestion as an orphan", ->
      guest = createGuest()
      annotation = {
        tags: [
          'suggestion'
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'ell', prefix: 'h', suffix: 'o', start: 1, end: 4}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'ell'}]},
        ],
      }

      range = document.createRange()
      range.setStart(txt, 1)
      range.setEnd(txt, 4)

      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))

      guest.anchor(annotation).then ->
        assert.isFalse(annotation.$orphan)

    it "#removeRangeWithoutExactTextMatch(): mark a locatable suggestion (but containing modified text though) as an orphan", ->
      guest = createGuest()
      annotation = {
        tags: [
          'suggestion'
        ],
        target: [
          {selector: [{type: 'LeosSelector', id: 'test-element', exact: 'elx', prefix: 'h', suffix: 'o', start: 1, end: 4}]},
          {selector: [{type: 'TextQuoteSelector', exact: 'elx'}]},
        ],
      }

      sinon.stub(anchoring, 'anchor').returns(Promise.reject())

      guest.anchor(annotation).then ->
        assert.isTrue(annotation.$orphan)

    it "does not attempt to anchor targets which have no TextQuoteSelector", ->
      guest = createGuest()
      annotation = {
        target: [
          {selector: [{type: 'TextPositionSelector', start: 0, end: 5}]},
        ]
      }
      sinon.spy(anchoring, 'anchor')

      guest.anchor(annotation).then ->
        assert.notCalled(anchoring.anchor)

    it 'updates the cross frame and bucket bar plugins', (done) ->
      guest = createGuest()
      guest.plugins.CrossFrame =
        sync: sinon.stub()
      guest.plugins.BucketBar =
        update: sinon.stub()
      annotation = {}
      guest.anchor(annotation).then ->
        assert.called(guest.plugins.BucketBar.update)
        assert.called(guest.plugins.CrossFrame.sync)
      .then(done, done)

    it 'returns a promise of the anchors for the annotation', (done) ->
      guest = createGuest()
      highlights = [document.createElement('span')]
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))
      sinon.stub(highlighter, 'highlightRange').returns(highlights)
      target = {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]}
      guest.anchor({target: [target]}).then (anchors) ->
        assert.equal(anchors.length, 1)
      .then(done, done)

    it 'adds the anchor to the "anchors" instance property"', (done) ->
      guest = createGuest()
      highlights = [document.createElement('span')]
      sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))
      sinon.stub(highlighter, 'highlightRange').returns(highlights)
      target = {selector: [{type: 'TextQuoteSelector', exact: 'hello'}]}
      annotation = {target: [target]}
      guest.anchor(annotation).then ->
        assert.equal(guest.anchors.length, 1)
        assert.strictEqual(guest.anchors[0].annotation, annotation)
        assert.strictEqual(guest.anchors[0].target, target)
        assert.strictEqual(guest.anchors[0].range, range)
        assert.strictEqual(guest.anchors[0].highlights, highlights)
      .then(done, done)

    it 'destroys targets that have been removed from the annotation', (done) ->
      annotation = {}
      target = {}
      highlights = []
      guest = createGuest()
      guest.anchors = [{annotation, target, highlights}]
      removeHighlights = sinon.stub(highlighter, 'removeHighlights')

      guest.anchor(annotation).then ->
        assert.equal(guest.anchors.length, 0)
        assert.calledOnce(removeHighlights)
        assert.calledWith(removeHighlights, highlights)
      .then(done, done)

    it 'does not reanchor targets that are already anchored', (done) ->
      guest = createGuest()
      annotation = target: [{selector: [{type: 'TextQuoteSelector', exact: 'hello'}]}]
      stub = sinon.stub(anchoring, 'anchor').returns(Promise.resolve(range))
      guest.anchor(annotation).then ->
        guest.anchor(annotation).then ->
          assert.equal(guest.anchors.length, 1)
          assert.calledOnce(stub)
      .then(done, done)

  describe '#detach()', ->
    it 'removes the anchors from the "anchors" instance variable', ->
      guest = createGuest()
      annotation = {}
      guest.anchors.push({annotation})
      guest.detach(annotation)
      assert.equal(guest.anchors.length, 0)

    it 'updates the bucket bar plugin', ->
      guest = createGuest()
      guest.plugins.BucketBar = update: sinon.stub()
      annotation = {}

      guest.anchors.push({annotation})
      guest.detach(annotation)
      assert.calledOnce(guest.plugins.BucketBar.update)

    it 'publishes the "annotationDeleted" event', ->
      guest = createGuest()
      annotation = {}
      publish = sinon.stub(guest, 'publish')

      guest.deleteAnnotation(annotation)

      assert.calledOnce(publish)
      assert.calledWith(publish, 'annotationDeleted', [annotation])

    it 'removes any highlights associated with the annotation', ->
      guest = createGuest()
      annotation = {}
      highlights = [document.createElement('span')]
      removeHighlights = sinon.stub(highlighter, 'removeHighlights')

      guest.anchors.push({annotation, highlights})
      guest.deleteAnnotation(annotation)

      assert.calledOnce(removeHighlights)
      assert.calledWith(removeHighlights, highlights)
