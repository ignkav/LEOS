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

var retry = require('retry');
var EventEmitter = require('tiny-emitter');

// see https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
var CLOSE_NORMAL = 1000;

// Minimum delay, in ms, before reconnecting after an abnormal connection close.
var RECONNECT_MIN_DELAY = 1000;

/**
 * Socket is a minimal wrapper around WebSocket which provides:
 *
 * - Automatic reconnection in the event of an abnormal close
 * - Queuing of messages passed to send() whilst the socket is
 *   connecting
 * - Uses the standard EventEmitter API for reporting open, close, error
 *   and message events.
 */
class Socket extends EventEmitter {
  constructor(url) {
    super();

    var self = this;

    // queue of JSON objects which have not yet been submitted
    var messageQueue = [];

    // the current WebSocket instance
    var socket;

    // a pending operation to connect a WebSocket
    var operation;

    function sendMessages() {
      while (messageQueue.length > 0) {
        var messageString = JSON.stringify(messageQueue.shift());
        socket.send(messageString);
      }
    }

    // Connect the websocket immediately. If a connection attempt is already in
    // progress, do nothing.
    function connect() {
      if (operation) {
        return;
      }

      operation = retry.operation({
        minTimeout: RECONNECT_MIN_DELAY * 2,
        // Don't retry forever -- fail permanently after 10 retries
        retries: 10,
        // Randomize retry times to minimise the thundering herd effect
        randomize: true,
      });

      operation.attempt(function () {
        socket = new WebSocket(url);
        socket.onopen = function (event) {
          onOpen();
          self.emit('open', event);
        };
        socket.onclose = function (event) {
          if (event.code === CLOSE_NORMAL) {
            self.emit('close', event);
            return;
          }
          var err = new Error('WebSocket closed abnormally, code: ' + event.code);
          console.warn(err);
          onAbnormalClose(err);
        };
        socket.onerror = function (event) {
          self.emit('error', event);
        };
        socket.onmessage = function (event) {
          self.emit('message', event);
        };
      });
    }

    // onOpen is called when a websocket connection is successfully established.
    function onOpen() {
      operation = null;
      sendMessages();
    }

    // onAbnormalClose is called when a websocket connection closes abnormally.
    // This may be the result of a failure to connect, or an abnormal close after
    // a previous successful connection.
    function onAbnormalClose(error) {
      // If we're already in a reconnection loop, trigger a retry...
      if (operation) {
        if (!operation.retry(error)) {
          console.error('reached max retries attempting to reconnect websocket');
        }
        return;
      }
      // ...otherwise reconnect the websocket after a short delay.
      var delay = RECONNECT_MIN_DELAY;
      delay += Math.floor(Math.random() * delay);
      operation = setTimeout(function () {
        operation = null;
        connect();
      }, delay);
    }

    /** Close the underlying WebSocket connection */
    this.close = function () {
      socket.close();
    };

    /**
     * Send a JSON object via the WebSocket connection, or queue it
     * for later delivery if not currently connected.
     */
    this.send = function (message) {
      messageQueue.push(message);
      if (this.isConnected()) {
        sendMessages();
      }
    };

    /** Returns true if the WebSocket is currently connected. */
    this.isConnected = function () {
      return socket.readyState === WebSocket.OPEN;
    };

    connect();
  }
}

module.exports = Socket;
