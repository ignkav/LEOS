# Do LEOS specific actions in this class
$ = require('jquery')

Sidebar = require('../../src/annotator/sidebar')
HostBridgeManager = require('./host-bridge-manager')
LeosSuggestionSelection = require('./anchoring/leos-suggestion-selection')
events = require('../../src/sidebar/events')
LEOS_config = require('../shared/config')
LEOS_SYSTEM_IDS = require('../shared/system-id')
OPERATION_MODES = require('../shared/operationMode')
ANNOT_POPUP_DEFAULT_STATUS = require('../shared/annotationPopupDefaultStatus')
require('./leos-adder')

FRAME_DEFAULT_WIDTH = LEOS_config.FRAME_DEFAULT_WIDTH
FRAME_DEFAULT_MIN_WIDTH = LEOS_config.FRAME_DEFAULT_MIN_WIDTH

module.exports = class LeosSidebar extends Sidebar
  cachedCoordinates: null
  annotationGuideLineStyles: []
  hoveredAnnotations: []
  selectedAnnotations: []

  constructor: (element, config) ->
    hypothesisUrl = new URL(config.clientUrl)
    @contextRoot = hypothesisUrl.pathname.substring(0, hypothesisUrl.pathname.indexOf("/",2))

    @options.Document.leosDocumentRootNode = config.leosDocumentRootNode
    @options.Document.annotationContainer = config.annotationContainer
    @options.Document.allowedSelectorTags = config.allowedSelectorTags
    @options.Document.editableSelector = config.editableSelector
    @options.Document.notAllowedSuggestSelector = config.notAllowedSuggestSelector
    @options.Document.operationMode = config.operationMode
    @options.Document.annotationPopupDefaultStatus = config.annotationPopupDefaultStatus
    @options.Document.showStatusFilter = config.showStatusFilter
    @options.Document.showGuideLinesButton = config.showGuideLinesButton
    @options.Document.connectedEntity = config.connectedEntity
    @options.Document.context = config.context
    #Set the scrollable element on which the bucket bar should be synced
    @options.BucketBar.scrollables = ["#{@options.Document.annotationContainer}"]
    @options.Document.authority = config.services[0].authority

    #Get first non scrollable parent of the container to get an element on which side bar can be fixed
    @container = $(@options.Document.annotationContainer)
    element = _nearestNonScrollableAncestor @container[0]

    self = @
    @container[0].addEventListener("annotationRefresh", () -> _refresh.call(self))
    @container[0].addEventListener("annotationSidebarResize", () -> _setFramePosition.call(self))
    @container[0].ownerDocument.defaultView.addEventListener("resize", () -> _onResize.call(self, true))
    @container[0].addEventListener("scroll", () -> _onScroll.call(self))
    $(window).click((event) -> _onWindowClick(event, self))
    @container[0].addEventListener("filterHighlights", (event) -> self.options.Document.filterParentElementId = event.filterParentElementId )

    _setupCanvas(@container[0])

    config.docType = 'LeosDocument'

    @annotationPopupStatus = true

    if @options.Document.annotationPopupDefaultStatus == ANNOT_POPUP_DEFAULT_STATUS.OFF
      @annotationPopupStatus = false

    super

    @hostBridgeManager = new HostBridgeManager(@container[0].hostBridge, @crossframe)
    @leosSuggestionSelection =
      new LeosSuggestionSelection(@options.Document.allowedSelectorTags, @options.Document.editableSelector, @options.Document.notAllowedSuggestSelector)
    @crossframe.on 'requestAnnotationAnchor', (annot, callback) =>
      suggestionPositionSelector = self.leosSuggestionSelection.getSuggestionSelectors(annot, self.anchors)
      if suggestionPositionSelector?
        callback(null, suggestionPositionSelector)
      else
        callback("No available highlights")

    @adderCtrl.extend(@adder[0], @container[0].hostBridge, {
      onComment: onComment.bind(self),
      onSuggest: onSuggest.bind(self),
      onHighlight: onHighlight.bind(self)
    })

    @.on 'panelReady', =>
      _setFrameStyling.call(@, element)

    @.on events.ANNOTATIONS_LOADED, =>
      if eval(localStorage.getItem('shouldAnnotationTabOpen'))
        onDefaultLoad.call(@)
        localStorage.setItem('shouldAnnotationTabOpen', false)
        _refreshAnnotationLinkLines(@visibleGuideLines, @)

    @.on 'LEOS_annotationsSynced', =>
      _setupAnchorListeners(@)
      _setupOverlaidAnnotations()
      _refreshAnnotationLinkLines(@visibleGuideLines, @, 500)
      _filterHighlights(@)

    @crossframe.on 'LEOS_cancelFilterHighlights', () => self.options.Document.filterParentElementId = null
    @crossframe.on 'LEOS_clearSelection', () => self._onClearSelection()
    @crossframe.on 'LEOS_updateIdForCreatedAnnotation',
      (annotationTag, createdAnnotationId) => _updateIdForCreatedAnnotation(annotationTag, createdAnnotationId, self)
    @crossframe.on 'LEOS_createdAnnotation', (annotationTag, createdAnnotationId) => _setupOverlaidAnnotations()
    @crossframe.on 'LEOS_refreshAnnotationLinkLines', => _refreshAnnotationLinkLines(@visibleGuideLines, @)
    @crossframe.on 'LEOS_syncCanvasResp', (response) => drawGuideLines(response, @)
    @crossframe.on 'LEOS_selectAnnotation', (annotation) => _crossFrameSelectAnnotation(annotation, @)
    @crossframe.on 'LEOS_selectAnnotations', (annotations) => _crossFrameSelectAnnotations(annotations, @)
    @crossframe.on 'LEOS_deselectAllAnnotations', () => _deselectAllAnnotations(@)
    @crossframe.on 'focusAnnotations', (tags=[]) => focusAnnotations(tags, @)
    _onResize.call(@, false)

    if !@options.Document.showGuideLinesButton
      @plugins.Toolbar.disableGuideLinesBtn()

    _setToolbarMode.call(@)

    if @options.Document.annotationPopupDefaultStatus == ANNOT_POPUP_DEFAULT_STATUS.HIDE
      @plugins.Toolbar.disableAnnotPopupBtn()

  _hasElements = (list) ->
    return list? and list.length > 0

  _onSelection: (range) ->
    if @options.Document.operationMode == OPERATION_MODES.READ_ONLY or not @annotationPopupStatus
      return

    if _hasElements($(range.endContainer.parentNode).closest(".leos-authnote-table"))
      @adderCtrl.disableAnnotations()
      return
    else
      @adderCtrl.enableAnnotations()

    isSuggestionAllowed = @leosSuggestionSelection.isSelectionAllowedForSuggestion(range.cloneContents(), range.commonAncestorContainer)
    if isSuggestionAllowed
      @adderCtrl.enableSuggestionButton()
    else
      @adderCtrl.disableSuggestionButton()

    isHighlightAllowed = @options.Document.authority != LEOS_SYSTEM_IDS.ISC
    if isHighlightAllowed
      @adderCtrl.addHighlightButton()
    else
      @adderCtrl.removeHighlightButton()
    super

  getDocumentInfo: ->
    self = @
    getInfoPromise = super
    return getInfoPromise.then (docInfo) ->
      return self.plugins.Document.getLeosDocumentMetadata()
        .then (leosMetadata) =>
          if leosMetadata?
            docInfo.metadata.metadata = leosMetadata
          return {
            uri: docInfo.uri,
            metadata: docInfo.metadata,
            frameIdentifier: docInfo.frameIdentifier
          }
        .catch () =>
          return {
            uri: docInfo.uri,
            metadata: docInfo.metadata,
            frameIdentifier: docInfo.frameIdentifier
          }

  onSuggest = () ->
    _onResize.call(@, true)
    range = @selectedRanges[0]
    if range?
      annotationText = @leosSuggestionSelection.extractSuggestionTextFromSelection(range.cloneContents())

      @createAnnotation({$suggestion: true, tags: ["suggestion"], text: annotationText})
    document.getSelection().removeAllRanges()

  onComment = () ->
    _onResize.call(@, true)
    @createAnnotation({$comment: true, tags: ["comment"]})
    document.getSelection().removeAllRanges()

  onHighlight = () ->
    _onResize.call(@, true)
    @setVisibleHighlights(true)
    @createAnnotation({$highlight: true, tags: ["highlight"]})
    document.getSelection().removeAllRanges()

  onDefaultLoad = () ->
    @showAnnotations({tags: ["comment","suggestion","highlight"]})

  _isScrollable = (element) ->
    element.scrollWidth > element.clientWidth or element.scrollHeight > element.clientHeight

  _nearestNonScrollableAncestor = (element) ->
    parentEl = element
    while parentEl.parentElement
      if !_isScrollable(parentEl)
        break
      parentEl = parentEl.parentElement
    parentEl

  _setFrameStyling = () ->
    #Set Sidebar specific styling to stick to the container
    @frame.addClass('leos-sidebar')
    _setFramePosition.call(@)

  _setFramePosition = () ->
    position = @element[0].getBoundingClientRect()
    @frame[0].style.top = position.top + "px"
    @frame[0].style.height = position.height + "px"
    @frame[0].style.left = if _isScrollbarVisible then position.right - 18 + "px" else position.right + "px"

  _isMilestoneExplorer = () ->
    return $("#milestonedocContainer").get(0) != undefined

  _getFrameWidth = (sidebarWidth, currentWidth) ->
    if _isMilestoneExplorer()
      currentWidth
    else
      if sidebarWidth > FRAME_DEFAULT_WIDTH
        FRAME_DEFAULT_WIDTH
      else
        if sidebarWidth < FRAME_DEFAULT_MIN_WIDTH
          FRAME_DEFAULT_MIN_WIDTH
        else
          sidebarWidth

  _onResize = (redraw) ->
    collapsed = @crossframe.annotator.frame.hasClass('annotator-collapsed')
    frameWidth = @element[0].ownerDocument.defaultView.outerWidth
    sidebarWidth = frameWidth * Math.exp((frameWidth + 70) * 4.6 / 1920) / 100
    @frameCurrentWidth = _getFrameWidth(sidebarWidth, @frameCurrentWidth)
    if redraw && !collapsed
      @frame.css 'margin-left': "#{-1 * @frameCurrentWidth}px"
      @frame.css 'width': "#{@frameCurrentWidth}px"
    _setupCanvas(@container[0])
    _refreshAnnotationLinkLines(@visibleGuideLines, @)

  _isScrollbarVisible = () ->
    return @container[0].scrollHeight > @container[0].clientHeight

  # LEOS-4588 while on read only operation mode, annotation and new note buttons should be disabled (on milestones explorer and editing toc)
  _setToolbarMode = () ->
    if (@options.Document.operationMode == OPERATION_MODES.READ_ONLY)
      @plugins.Toolbar.disableAnnotPopupBtn()
      @plugins.Toolbar.disableNewNoteBtn()
    else
      @plugins.Toolbar.enableAnnotPopupBtn()
      @plugins.Toolbar.enableNewNoteBtn()

  _refresh = () ->
    self = @
    @options.Document.operationMode = event.operationMode #LEOS-3796
    _setToolbarMode.call(@)
    @crossframe?.call('LEOS_changeOperationMode', event.operationMode)
    # A list of annotations that need to be refreshed.
    refreshAnnotations = new Set
    # Find all the anchors that have been invalidated by page state changes.
    # Would be better to loop on all existing annotations, but didn't find any other way to access annotations from sidebar
    if @anchors?
      for anchor in @anchors when anchor.highlights?
        # The annotations for these anchors need to be refreshed.
        # Remove highlights in the DOM
        for h in anchor.highlights when h.parentNode?
          $(h).replaceWith(h.childNodes)
        delete anchor.highlights
        delete anchor.range
        # Add annotation to be anchored again
        refreshAnnotations.add(anchor.annotation)

      refreshAnnotations.forEach (annotation) ->
        @anchor(annotation)
      , self

    self.setVisibleHighlights(true)
    self.crossframe?.call('reloadAnnotations')

  _setupCanvas = (container) ->
    canvasWidth = container.ownerDocument.defaultView.innerWidth
    canvasHeight = container.ownerDocument.defaultView.innerHeight
    leosCanvas = document.getElementById("leosCanvas")
    if(!leosCanvas)
      canvasElem = '<canvas id="leosCanvas" class="leos-guideline-canvas" width='+canvasWidth+' height='+canvasHeight+'></canvas>'
      $(canvasElem).insertBefore(container)
      leosCanvas = document.getElementById("leosCanvas")
    else
      leosCanvas.width = canvasWidth
      leosCanvas.height = canvasHeight

    if leosCanvas
      context = leosCanvas.getContext("2d")
      context.setLineDash([5,5])
      context.lineWidth="3"
      context.strokeStyle="#FDD7DF" #default color

  _setupAnchorListeners = (scope) ->
    highlights = document.getElementsByTagName('hypothesis-highlight')
    if highlights
      for highlight in highlights
        if !$(highlight).data('has-listeners-set') and highlight.lastElementChild == null
          highlight.addEventListener("mouseover", (event) -> _onHighlightFocus(event, true, scope))
          highlight.addEventListener("mouseout", (event) -> _onHighlightFocus(event, false, scope))
          $(highlight).data('has-listeners-set', true)

  _setupOverlaidAnnotations = () ->
    highlights = document.getElementsByTagName('hypothesis-highlight')
    if highlights
      for highlight in highlights
        annotation = $(highlight).data('annotation')
        previousSiblingAnnotation = $(highlight.parentNode.previousElementSibling).data('annotation')
        nextSiblingAnnotation = $(highlight.parentNode.nextElementSibling).data('annotation')
        if annotation and annotation.id and ((previousSiblingAnnotation and annotation.id == previousSiblingAnnotation.id) or (nextSiblingAnnotation and annotation.id == nextSiblingAnnotation.id))
          $(highlight).addClass("no-pointer-events")

  _onHighlightFocus = (event, isMouseEnter, scope) ->
    annotation = $(event.target).data('annotation')
    if annotation
      anchor = _getAnchorFromAnnotationTag(annotation.$tag, scope)
      if anchor
        $(anchor.highlights).toggleClass('annotator-hl-focused', isMouseEnter)
        scope.hoveredAnnotations[annotation.$tag] = isMouseEnter
        drawGuideLines(null, scope)

  _refreshAnnotationLinkLinesAndUpdateIcon = (visibleGuideLines, scope, delayResp) ->
    _refreshAnnotationLinkLines(visibleGuideLines, scope, delayResp)
    scope.publish 'LEOS_setVisibleGuideLines', visibleGuideLines, false


  _refreshAnnotationLinkLines = (visibleGuideLines, scope, delayResp) ->
    if visibleGuideLines && scope
      hypothesisIFrameOffset = _getHypothesisFrame().offsetParent
      scope.crossframe.call('LEOS_syncCanvas', hypothesisIFrameOffset.offsetLeft, delayResp)
    else
      _clearAnnotationLinkLines()

  focusAnnotations = (tags, scope) ->
    for anchor in scope.anchors when anchor.highlights?
      toggle = anchor.annotation.$tag in tags
      $(anchor.highlights).toggleClass('annotator-hl-focused', toggle)
      scope.hoveredAnnotations[anchor.annotation.$tag] = toggle
    drawGuideLines(null, scope)

#   select annotation from Anchor
  _onWindowClick = (event, scope) ->
    _toggleAnnotationSelectedState($(event.target).data('annotation'), true, scope)

#   select annotation from Annotation on sidebar
  _crossFrameSelectAnnotation = (annotation, scope) ->
    _toggleAnnotationSelectedState(annotation, false, scope)

#   select multiple annotations from Annotation on sidebar
  _crossFrameSelectAnnotations = (annotations, scope) ->
    for annotation in annotations
      _changeAnnotationSelectedState(annotation, false, scope, true)


  _toggleAnnotationSelectedState = (annotation, selectFromDocument, scope) ->

    targetSelectionState = annotation and !scope.selectedAnnotations[annotation.$tag]
    
    _changeAnnotationSelectedState(annotation, selectFromDocument, scope, targetSelectionState)

#   selectFromDocument -> true : click was done on Anchor
#   selectFromDocument -> false : click was done on Annotation on sidebar
  _changeAnnotationSelectedState = (annotation, selectFromDocument, scope, isSelectedTargetState) ->
#      if anchor not selected -> select anchor, drawGuideLines
#      if anchor selected -> de-select anchor, drawGuideLines, clear filtering
#      if click outside annotations -> de-select all anchors, drawGuideLines, clear filtering

    if ! annotation
      _clearSelectedAnnotations(scope)
      _sendClearSelectedAnnotationsToSidebar(scope, true, null)
      _refreshOrDrawGuideLines(selectFromDocument, scope)
      return

    if (_isPageNote(annotation) or _isOrphan(annotation))
      _clearSelectedAnnotations(scope)
      _refreshOrDrawGuideLines(selectFromDocument, scope)
      return

    if isSelectedTargetState and selectFromDocument  #If select on anchor - select only one anchor and filter by it
      _sendClearSelectedAnnotationsToSidebar(scope, false, annotation.id)

    _setAnnotationSelectedState(annotation, scope, isSelectedTargetState)

    if !isSelectedTargetState and selectFromDocument #If anchor de-selected - deselect only one anchor
      _sendClearSelectedAnnotationsToSidebar(scope, true, annotation.id)
    
    _refreshOrDrawGuideLines(selectFromDocument, scope)


  _setAnnotationSelectedState = (annotation, scope, isSelectedTargetState) ->
    scope.selectedAnnotations[annotation.$tag] = isSelectedTargetState
    anchor = _getAnchorFromAnnotationTag(annotation.$tag, scope)
    $(anchor.highlights).toggleClass('annotator-hl-selected', isSelectedTargetState)
    $(anchor.highlights).find(".annotator-hl").toggleClass("transparent-bg", isSelectedTargetState)


  _clearSelectedAnnotations = (scope) ->
    scope.selectedAnnotations = []
    $('hypothesis-highlight')?.toggleClass('annotator-hl-selected', false)
    $('hypothesis-highlight')?.find(".annotator-hl").toggleClass("transparent-bg", false)

  _sendClearSelectedAnnotationsToSidebar = (scope, useTimeout, annotationIdToSelectInstead) ->
    if useTimeout
      setTimeout => scope.crossframe?.call('LEOS_clearSelectedAnnotations', annotationIdToSelectInstead)? 500
    else
      scope.crossframe?.call('LEOS_clearSelectedAnnotations', annotationIdToSelectInstead)

  _refreshOrDrawGuideLines = (selectFromDocument, scope) ->
    if selectFromDocument
      _refreshAnnotationLinkLines(scope.visibleGuideLines, scope, 500)
    else
      drawGuideLines(null, scope)

  _deselectAllAnnotations = (scope) ->
    _clearSelectedAnnotations(scope)
    _refreshOrDrawGuideLines(false, scope)

  _getAnchorFromAnnotationTag = (tag, scope) ->
    for anchor in scope.anchors when anchor.highlights?
      if anchor.annotation.$tag == tag
        return anchor

  _isOrphan = (annotation) ->
    _hasSelector(annotation) && annotation.$orphan
  
  _isPageNote = (annotation) ->
    !_hasSelector(annotation) && !_isReply(annotation)

  _hasSelector = (annotation) ->
    !!(annotation.target && annotation.target.length > 0 && annotation.target[0].selector)

  _isReply = (annotation) ->
    (annotation.references || []).length > 0

  #NOTE If coordinates = null, cached values are used. Useful when redraw is needed but coordinates are sure not to have changed their values
  drawGuideLines = (coordinates, scope) ->
    if coordinates != null
      @cachedCoordinates = coordinates
    # Check whether @ is set as tests fail otherwise.
    if @ and @cachedCoordinates == null
      return
    if not scope.visibleGuideLines
      return
    _clearAnnotationLinkLines()
    leosCanvas = document.getElementById("leosCanvas")
    if leosCanvas
      context = leosCanvas.getContext("2d")
      hypothesisIFrameOffset = _getHypothesisFrame().offsetParent
      endOfPageHorzCoords = _getEndOfPageHorzCoordinates()
      annLineDrawControl = [] #control array used to limit to one guide line annotations composed by more than one lines of text

  #    get all highlight anchors
      highlights = $('hypothesis-highlight')
      if highlights && highlights.length > 0
        for highlight in highlights
          annotation = $(highlight).data('annotation')
          if !annLineDrawControl[annotation.$tag]
            annLineDrawControl[annotation.$tag] = true
            _configCanvasContextForAnnotation(context, highlight, scope)
    #        select the correct annotation that relates to the current highlight in the loop
            annotationCoordinate = _getAnnotationCoordinate(@cachedCoordinates, annotation.id)
            if annotationCoordinate
              docViewportTopLimit = hypothesisIFrameOffset.offsetTop
              footer = document.querySelector('.leos-footer')
              docViewportBottomLimit = (document.documentElement.clientHeight || window.innerHeight) - (if footer then footer.offsetHeight else 0)
              highlightRect = highlight.getBoundingClientRect()
              anchorEndpointTop = annotationCoordinate.y + hypothesisIFrameOffset.offsetTop
    #          highlight is visible on the viewport
              anchorInViewport = highlightRect.top >= docViewportTopLimit + 5 &&
                                 highlightRect.top <= docViewportBottomLimit &&
                                 highlightRect.right <= endOfPageHorzCoords
    #          the annotation on the sidebar is on the viewport
    #          Note: following hardcoded values are hypothesis element dims that cannot be retrieved by document query due to crossDomain issues
              anchorEndpointInViewport = anchorEndpointTop >= (docViewportTopLimit + 40) &&
                                         anchorEndpointTop <= docViewportBottomLimit - 15
              if anchorInViewport && anchorEndpointInViewport
                fromLeft = highlightRect.right - 5
                fromTop = highlightRect.top
                toLeft = annotationCoordinate.x - 10
                _drawGuidLine(context, endOfPageHorzCoords, fromLeft, fromTop, toLeft, anchorEndpointTop)

  _configCanvasContextForAnnotation = (canvasContext, highlight, scope) ->
    annotation = $(highlight).data('annotation')
#    Config line color
    if lineColor = $(highlight).css( "background-color" )
      canvasContext.strokeStyle = lineColor

#    Config line type: dashed for regular, solid for focused
    if scope.selectedAnnotations[annotation.$tag]
      canvasContext.setLineDash([])
      canvasContext.strokeStyle = LEOS_config.LEOS_SELECTED_ANNOTATION_COLOR
    else if scope.hoveredAnnotations[annotation.$tag]
      canvasContext.setLineDash([])
      canvasContext.strokeStyle = LEOS_config.LEOS_HOVER_ANNOTATION_COLOR
    else #Dashed line
      canvasContext.setLineDash([5,5])

  _drawGuidLine = (canvasContext, endOfPageHorzCoords, fromLeft, fromTop, toLeft, toTop) ->
    canvasContext.beginPath()
    canvasContext.moveTo(fromLeft, fromTop)
    canvasContext.lineTo(fromLeft, fromTop - 5)
    canvasContext.lineTo(endOfPageHorzCoords, fromTop - 5)
    canvasContext.lineTo(toLeft, toTop)
    canvasContext.stroke()

  _getAnnotationCoordinate = (coordinates, id) ->
    if coordinates && id
      for annotationCoordinate in coordinates
        if annotationCoordinate.id == id
          return annotationCoordinate

  _getHypothesisFrame = () ->
    $('iframe:first')[0] #TODO infer the correct iframe based on something else other than the index

  _getEndOfPageHorzCoordinates = () ->
    #TOC width plus the document page width
    LEOSTocPanelWidth = $('.v-splitpanel-first-container').outerWidth()
    ISCTocPanelWidth = $('.renditionTocContent').outerWidth()
    endOfDocCoordinates = ( LEOSTocPanelWidth || ISCTocPanelWidth || 0) + ($('doc :first-child').outerWidth() || $('bill :first-child').outerWidth())
    annotFrameLeftOffset = _getHypothesisFrame().offsetParent.offsetLeft - 20
    return Math.min(endOfDocCoordinates, annotFrameLeftOffset)

  _clearAnnotationLinkLines = () ->
    leosCanvas = document.getElementById("leosCanvas")
    if leosCanvas?
      ctx = leosCanvas.getContext("2d")
      ctx.clearRect(0, 0, leosCanvas.width, leosCanvas.height)
      # Check whether @ is set as tests fail otherwise.
      if @
        @hoveredAnnotations = []
        @selectedAnnotations = []


  # Method called for CREATE, UPDATE and DELETE.
  # createdAnnotationId is only != null on CREATE
  _updateIdForCreatedAnnotation = (annotationTag, createdAnnotationId, scope) ->
    highlights = $('hypothesis-highlight')
    if createdAnnotationId && highlights && highlights.length > 0
      for highlight in highlights
        annotation = $(highlight).data('annotation')
        if annotation.$tag == annotationTag
          annotation.id = createdAnnotationId
      _refreshAnnotationLinkLines(scope.visibleGuideLines, scope)
#    refresh lines for all three events

  _onScroll = () ->
    _refreshAnnotationLinkLines(@visibleGuideLines, @)

  _filterHighlights = (scope) ->
    if scope.options.Document.filterParentElementId
      highlights = $("##{scope.options.Document.filterParentElementId}").find('hypothesis-highlight')
      if highlights && highlights.length > 0
        for h in highlights when h.parentNode?
          $(h).replaceWith(h.childNodes)

  _cleanup = () ->
    # TODO : Temporary solution to cleanup references to annotation scripts
    quote = (regex) ->
      regex.replace(/([()[{*+.$^\\|?\/])/g, '\\$1')

    annotateScripts = document.querySelectorAll("script[src*=#{quote(@contextRoot)}]")
    annotateScripts.forEach((annotateScript) ->
      annotateScript.parentNode.removeChild(annotateScript)
    )

    annotateLinks = document.querySelectorAll("link[href*=#{quote(@contextRoot)}]")
    annotateLinks.forEach((annotateLink) ->
      annotateLink.parentNode.removeChild(annotateLink)
    )

    configScripts = document.querySelectorAll("script.js-hypothesis-config")
    configScripts.forEach((configScript) ->
      configScript.parentNode.removeChild(configScript)
    )

  setAllVisibleGuideLines: (shouldShowGuideLines, storePrevState) ->
    if @visibleHighlights
      @crossframe.call('LEOS_setVisibleGuideLines', shouldShowGuideLines, storePrevState)
      @.publish 'LEOS_setVisibleGuideLines', shouldShowGuideLines, storePrevState
      setTimeout (=> _refreshAnnotationLinkLines(shouldShowGuideLines, @)), 500

  restoreAllGuideLinesState: () ->
    @crossframe.call('LEOS_restoreGuideLinesState')
    @.publish 'LEOS_restoreGuideLinesState'
    setTimeout (=> _refreshAnnotationLinkLinesAndUpdateIcon(@visibleGuideLines, @)), 500

  changeAnnotationPopupStatus: (shouldShowAnnotationPopup) ->
    if shouldShowAnnotationPopup != undefined
      @annotationPopupStatus = shouldShowAnnotationPopup
    else
      @annotationPopupStatus = not @annotationPopupStatus

  getAnnotationPopupStatus: () ->
    return @annotationPopupStatus

  show: () ->
    super
    if @frame.width() != @frameCurrentWidth and @frame.width() != 0
      @frameCurrentWidth = @frame.width()
    @frame.css 'margin-left': "#{-1 * @frameCurrentWidth}px"
    @frame.css 'width': "#{@frameCurrentWidth}px"
    if @visibleHighlights
      @restoreAllGuideLinesState()

  hide: () ->
    super
    _clearAnnotationLinkLines()
    if @frameCurrentWidth? and @frame.width() != 0
      @frameCurrentWidth = @frame.width()
    else
      @frameCurrentWidth =
        if _isMilestoneExplorer()
          FRAME_DEFAULT_WIDTH - 150
        else
          FRAME_DEFAULT_WIDTH
    @frame.css 'width': "0px"

  destroy: () ->
    super
    _cleanup.call(@)

#  normalizeURI = (uri, baseURI) ->
# use if required
