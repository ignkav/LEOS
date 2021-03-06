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

var observable = require('./util/observable');

//Changed for LEOS: document is the element where selections is allowed

/** Returns the selected `DOMRange` in `document`. */
function selectedRange(document, event) {
  var selection;
  if (document instanceof HTMLElement) { // LEOS Change
    selection = document.ownerDocument.getSelection();
  }
  else {
    selection = document.getSelection();
  }
  if (!selection.rangeCount || selection.getRangeAt(0).collapsed) {
    return null;
  }
  else if (event.detail < 3) { // LEOS Change avoid triple click: LEOS-4432
    return selection.getRangeAt(0);
  }
  else {
    return null;
  }
}

/**
 * Returns an Observable stream of text selections in the current document.
 *
 * New values are emitted when the user finishes making a selection
 * (represented by a `DOMRange`) or clears a selection (represented by `null`).
 *
 * A value will be emitted with the selected range at the time of subscription
 * on the next tick.
 *
 * @return Observable<DOMRange|null>
 */
function selections(document) {

  // Get a stream of selection changes that occur whilst the user is not
  // making a selection with the mouse.
  var isMouseDown;
  var selectionEvents = observable.listen(document,
    ['mousedown', 'mouseup', 'selectionchange'])
    .filter(function (event) {
      if (event.type === 'mousedown' || event.type === 'mouseup') {
        isMouseDown = event.type === 'mousedown';
        return false;
      } else {
        return !isMouseDown;
      }
    });

  // LEOS-2764 filter events containing a type: these events should not be handled by Hypothesis
  // LEOS Change LEOS-2809: Block the mouseup event when previous mousedown event's target is editable
  var blockNextMouseUp;
  var mouseSelectionEvents = observable.listen(document,
     ['mousedown', 'mouseup'])
    .filter(function (event) {
      if (event.type === 'mousedown') {
        blockNextMouseUp = event.hostEventType;
        return false;
      } else {
        return (!blockNextMouseUp) && (!event.hostEventType);
      }
    });

  var events = observable.merge([
    // Add a delay before checking the state of the selection because
    // the selection is not updated immediately after a 'mouseup' event
    // but only on the next tick of the event loop.
    observable.buffer(10, mouseSelectionEvents),

    // Buffer selection changes to avoid continually emitting events whilst the
    // user drags the selection handles on mobile devices
    observable.buffer(100, selectionEvents),

    // Emit an initial event on the next tick
    observable.delay(0, observable.Observable.of({})),
  ]);

  return events.map(function (event) {
    return selectedRange(document, event);
  });
}

module.exports = selections;
