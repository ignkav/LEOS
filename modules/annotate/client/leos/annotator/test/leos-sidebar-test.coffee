proxyquire = require('proxyquire')

adder = require('../../../src/annotator/adder')
Observable = require('../../../src/annotator/util/observable').Observable

Delegator = require('../../../src/annotator/delegator')
$ = require('jquery')
Delegator['@noCallThru'] = true

LeosSidebar = null
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
    this.extend = sinon.stub()

describe 'LEOS Sidebar', ->
  consoleWarnSpy = null
  CrossFrame = null
  fakeCrossFrame = null
  guestConfig = null

  createGuest = (config={}) ->
    config = Object.assign({}, guestConfig, config)
    element = document.createElement('div')
    return new LeosSidebar(element, config)

  beforeEach ->
    consoleWarnSpy = sinon.stub(console, 'warn')

    FakeAdder::instance = null
    rangeUtil = {
      isSelectionBackwards: sinon.stub()
      selectionFocusRect: sinon.stub()
    }
    selections = null
    guestConfig = {
      annotationContainer: '#docContainer'
      clientUrl: 'http://example.com/app.html'
      leosDocumentRootNode: 'akomantoso'
      pluginClasses: {}
      services: [{
        authority: 'hypothes.is'
      }]
    }

    LeosSidebar = proxyquire('../leos-sidebar', {
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

    fakeToolbar = {
      destroy: sinon.stub()
      disableCloseBtn: sinon.spy()
      disableGuideLinesBtn: sinon.spy()
      disableHighlightsBtn: sinon.spy()
      disableMinimizeBtn: sinon.spy()
      disableNewNoteBtn: sinon.spy()
      enableAnnotPopupBtn: sinon.spy()
      enableNewNoteBtn: sinon.spy()
      getWidth: sinon.stub()
      hideCloseBtn: sinon.spy()
      showCloseBtn: sinon.spy()
      showCollapseSidebarBtn: sinon.spy()
      showExpandSidebarBtn: sinon.spy()
    }
    
    Toolbar = sinon.stub()
    Toolbar.returns(fakeToolbar)

    guestConfig.pluginClasses['Toolbar'] = Toolbar
    
  afterEach ->
    sinon.restore()
    consoleWarnSpy.restore()
  
  describe 'annotation UI events', ->
    emitGuestEvent = (event, args...) ->
      fn(args...) for [evt, fn] in fakeCrossFrame.on.args when event == evt

    describe 'on "focusAnnotations" event', ->
      it 'focuses any annotations with a matching tag', ->
        highlight0 = $('<span></span>')
        highlight1 = $('<span></span>')
        guest = createGuest()
        guest.anchors = [
          {annotation: {$tag: 'tag1'}, highlights: highlight0.toArray()}
          {annotation: {$tag: 'tag2'}, highlights: highlight1.toArray()}
        ]
        emitGuestEvent('focusAnnotations', ['tag1'])
        assert.isTrue(highlight0.hasClass('annotator-hl-focused'))

      it 'unfocuses any annotations without a matching tag', ->
        highlight0 = $('<span class="annotator-hl-focused"></span>')
        highlight1 = $('<span class="annotator-hl-focused"></span>')
        guest = createGuest()
        guest.anchors = [
          {annotation: {$tag: 'tag1'}, highlights: highlight0.toArray()}
          {annotation: {$tag: 'tag2'}, highlights: highlight1.toArray()}
        ]
        emitGuestEvent('focusAnnotations', 'ctx', ['tag1'])
        assert.isFalse(highlight1.hasClass('annotator-hl-focused'))
