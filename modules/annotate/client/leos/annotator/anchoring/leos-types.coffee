{RangeAnchor, FragmentAnchor, TextPositionAnchor, TextQuoteAnchor} = require('../../../src/annotator/anchoring/types')

domAnchorTextQuote = require('dom-anchor-text-quote')
domAnchorTextPosition = require('dom-anchor-text-position')
fragmentAnchor = require('dom-anchor-fragment')

###*
# Converts between TextPositionSelector selectors and Range objects.
###
class LeosAnchor
  constructor: (root, id, exact, prefix, suffix, start, end) ->
    @id = id
    @root = root
    @exact = exact
    @prefix = prefix
    @suffix = suffix
    @start = start
    @end = end

  @fromRange: (root, range, options) ->
    fragmentSelector = fragmentAnchor.fromRange(root, range)
    domAnchorQuoteSelector = domAnchorTextQuote.fromRange(_getRootElement(root, fragmentSelector.id), range, options)
    domAnchorPositionSelector = domAnchorTextPosition.fromRange(_getRootElement(root, fragmentSelector.id), range, options)
    selector = new LeosAnchor(root, fragmentSelector.id, domAnchorQuoteSelector.exact, domAnchorQuoteSelector.prefix, domAnchorQuoteSelector.suffix,
      domAnchorPositionSelector.start, domAnchorPositionSelector.end)
    LeosAnchor.fromSelector(root, selector)

  @fromSelector: (root, selector) ->
    new LeosAnchor(root, selector.id, selector.exact, selector.prefix, selector.suffix, selector.start, selector.end)

  toSelector: () ->
    {
      type: 'LeosSelector',
      id: @id,
      exact: @exact,
      prefix: @prefix,
      suffix: @suffix,
      start: @start,
      end: @end
    }

  toRange: (options = {}) ->
    try
      # Prioritise matches closer to the start offset. Workaround for bug: https://github.com/tilgovi/dom-anchor-text-quote/issues/13
      options.hint = this.toSelector().start

      #workaround for dom-anchor-text-position framework bug : https://github.com/tilgovi/dom-anchor-text-position/issues/2
      # This issue is still present despite updating to the bug fix version mentioned.
      rootNode = _getRootElement(@root, @id)
      dummyTextNode = document.createTextNode("")
      rootNode.appendChild(dummyTextNode)

      # workaround for selectors that contain a large number of superfluous whitespace:
      # If no range was found with the original selector: retry with a selector, where the
      # superfluous whitespace has been stripped of.
      whitespaceAdjustedAnchor = _adjustAnchorWhitespace(this)

      range = domAnchorTextQuote.toRange(rootNode, this.toSelector(), options)
      
      if range == null
        range = domAnchorTextQuote.toRange(rootNode, whitespaceAdjustedAnchor.toSelector(), options)

      if range == null
        range = domAnchorTextPosition.toRange(rootNode, this.toSelector(), options)
      
      if range == null
        range = domAnchorTextPosition.toRange(rootNode, whitespaceAdjustedAnchor.toSelector(), options)
        
    catch error
      if (error.message.indexOf("Failed to execute 'setEnd' on 'Range'") != -1)
        if !@start? or !@end? or @start == @end == 0
          throw new Error('Range creation failed')
        @end -= 1
        range = domAnchorTextPosition.toRange(_getRootElement(@root, @id), this.toSelector(), options)
    if range == null
      throw new Error('Range creation failed')
    range

  toPositionAnchor: (options = {}) ->
    positionAnchorRoot = _getRootElement(@root, @id)
    anchor = domAnchorTextQuote.toTextPosition(positionAnchorRoot, this.toSelector(), options)
    if anchor == null
      throw new Error('Quote not found')
    new TextPositionAnchor(positionAnchorRoot, anchor.start, anchor.end)

  _getRootElement = (root, id) ->
    if !root? and !id?
      throw new Error('Element not found')
    
    try
      return root.querySelector('#' + id)
    catch
      throw new Error('Element not found')

  ###*
  # Returns a new anchor object where superfluous whitespace
  # of @param anchor.prefix, @param anchor.exact and @param anchor.suffix
  # are removed. Additionally the @param anchor.start and @param anchor.end
  # indicees are adjusted accordingly.
  ###
  _adjustAnchorWhitespace = (anchor) ->
    if !anchor.prefix or !anchor.exact or !anchor.suffix
      return anchor

    adjustedAnchor = new LeosAnchor(anchor?.root, anchor?.id, anchor.exact, anchor.prefix, anchor.suffix, anchor?.start, anchor?.end)

    adjustedAnchor.prefix = _removeSuperfluousWhitespace(adjustedAnchor.prefix)
    adjustedAnchor.exact = _removeSuperfluousWhitespace(adjustedAnchor.exact)
    adjustedAnchor.suffix = _removeSuperfluousWhitespace(adjustedAnchor.suffix)

    textLengthDifferenceStart = anchor.prefix.length - adjustedAnchor.prefix.length
    
    textLengthDifferenceTotal = textLengthDifferenceStart
    textLengthDifferenceTotal += anchor.exact.length - adjustedAnchor.exact.length

    adjustedStartIndex = Math.max(0, anchor.start - textLengthDifferenceStart)
    adjustedEndIndex = Math.max(0, anchor.end - textLengthDifferenceTotal)

    adjustedAnchor.start = adjustedStartIndex
    adjustedAnchor.end = adjustedEndIndex

    return adjustedAnchor


  ###*
  # Removes linefeeds ("\n") and replaces multiple successive space characters with a single one.
  # Note: it does not use "\s" for matching the space characters in order to preserve non-breaking spaces (char code: 160), which turned out to improve matching
  # accuracy.
  ###
  _removeSuperfluousWhitespace = (stringToClean) ->
    if !stringToClean
      return stringToClean

    return stringToClean.replace(/[\n ]+/g, " ")
    

    

exports.LeosAnchor = LeosAnchor
exports.FragmentAnchor = FragmentAnchor
exports.RangeAnchor = RangeAnchor
exports.TextPositionAnchor = TextPositionAnchor
exports.TextQuoteAnchor = TextQuoteAnchor
