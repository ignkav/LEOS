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

var extend = require('extend');

var RPC = require('./frame-rpc');

/**
 * The Bridge service sets up a channel between frames and provides an events
 * API on top of it.
 */
class Bridge {
  constructor() {
    this.links = [];
    this.channelListeners = {};
    this.onConnectListeners = [];
  }

  /**
   * Destroy all channels created with `createChannel`.
   *
   * This removes the event listeners for messages arriving from other windows.
   */
  destroy() {
    Array.from(this.links).map((link) =>
      link.channel.destroy());
  }

  /**
   * Create a communication channel between this window and `source`.
   *
   * The created channel is added to the list of channels which `call`
   * and `on` send and receive messages over.
   *
   * @param {Window} source - The source window.
   * @param {string} origin - The origin of the document in `source`.
   * @param {string} token
   * @return {RPC} - Channel for communicating with the window.
   */
  createChannel(source, origin, token) {
    var channel = null;
    var connected = false;

    var ready = () => {
      if (connected) { return; }
      connected = true;
      Array.from(this.onConnectListeners).forEach((cb) =>
        cb.call(null, channel, source)
      );
    };

    var connect = (_token, cb) => {
      if (_token === token) {
        cb();
        ready();
      }
    };

    var listeners = extend({connect}, this.channelListeners);

    // Set up a channel
    channel = new RPC(window, source, origin, listeners);

    // Fire off a connection attempt
    channel.call('connect', token, ready);

    // Store the newly created channel in our collection
    this.links.push({
      channel,
      window: source,
    });

    return channel;
  }

  /**
   * Make a method call on all channels, collect the results and pass them to a
   * callback when all results are collected.
   *
   * @param {string} method - Name of remote method to call.
   * @param {any[]} args - Arguments to method.
   * @param [Function] callback - Called with an array of results.
   */
  call(method, ...args) {
    var cb;
    if (typeof(args[args.length - 1]) === 'function') {
      cb = args[args.length - 1];
      args = args.slice(0, -1);
    }

    var _makeDestroyFn = c => {
      return error => {
        c.destroy();
        this.links = (Array.from(this.links).filter((l) => l.channel !== c).map((l) => l));
        throw error;
      };
    };

    var promises = this.links.map(function(l) {
      var p = new Promise(function(resolve, reject) {
        var timeout = setTimeout((() => resolve(null)), 60000); //LEOS Change: increase timeout
        try {
          return l.channel.call(method, ...Array.from(args), function(err, result) {
            clearTimeout(timeout);
            if (err) { return reject(err); } else { return resolve(result); }
          });
        } catch (error) {
          var err = error;
          return reject(err);
        }
      });

      // Don't assign here. The disconnect is handled asynchronously.
      return p.catch(_makeDestroyFn(l.channel));
    });

    var resultPromise = Promise.all(promises);

    if (cb) {
      resultPromise = resultPromise
        .then(results => cb(null, results))
        .catch(error => cb(error));
    }

    return resultPromise;
  }

  /**
   * Register a callback to be invoked when any connected channel sends a
   * message to this `Bridge`.
   *
   * @param {string} method
   * @param {Function} callback
   */
  on(method, callback) {
    if (this.channelListeners[method]) {
      throw new Error(`Listener '${method}' already bound in Bridge`);
    }
    this.channelListeners[method] = callback;
    return this;
  }

  /**
   * Unregister any callbacks registered with `on`.
   *
   * @param {string} method
   */
  off(method) {
    delete this.channelListeners[method];
    return this;
  }

  /**
   * Add a function to be called upon a new connection.
   *
   * @param {Function} callback
   */
  onConnect(callback) {
    this.onConnectListeners.push(callback);
    return this;
  }
}

module.exports = Bridge;
