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

var SearchClient = require('../search-client');

function awaitEvent(emitter, event) {
  return new Promise(function (resolve) {
    emitter.on(event, resolve);
  });
}

describe('SearchClient', function () {
  var RESULTS = [
    {id: 'one'},
    {id: 'two'},
    {id: 'three'},
    {id: 'four'},
  ];

  var fakeSearchFn;

  beforeEach(function () {
    fakeSearchFn = sinon.spy(function (params) {
      return Promise.resolve({
        rows: RESULTS.slice(params.offset,
          params.offset + params.limit),
        total: RESULTS.length,
      });
    });
  });

  it('emits "results"', function () {
    var client = new SearchClient(fakeSearchFn);
    var onResults = sinon.stub();
    client.on('results', onResults);
    client.get({uri: 'http://example.com'});
    return awaitEvent(client, 'end').then(function () {
      assert.calledWith(onResults, RESULTS);
    });
  });

  it('emits "results" with chunks in incremental mode', function () {
    var client = new SearchClient(fakeSearchFn, {chunkSize: 2});
    var onResults = sinon.stub();
    client.on('results', onResults);
    client.get({uri: 'http://example.com'});
    return awaitEvent(client, 'end').then(function () {
      assert.calledWith(onResults, RESULTS.slice(0,2));
      assert.calledWith(onResults, RESULTS.slice(2,4));
    });
  });

  it('stops fetching chunks if the results array is empty', function () {
    // Simulate a situation where the `total` count for the server is incorrect
    // and we appear to have reached the end of the result list even though
    // `total` implies that there should be more results available.
    //
    // In that case the client should stop trying to fetch additional pages.
    fakeSearchFn = sinon.spy(function () {
      return Promise.resolve({
        rows: [],
        total: 1000,
      });
    });
    var client = new SearchClient(fakeSearchFn, {chunkSize: 2});
    var onResults = sinon.stub();
    client.on('results', onResults);

    client.get({uri: 'http://example.com'});

    return awaitEvent(client, 'end').then(function () {
      assert.calledWith(onResults, []);
      assert.calledOnce(fakeSearchFn);
    });
  });

  it('emits "results" once in non-incremental mode', function () {
    var client = new SearchClient(fakeSearchFn,
      {chunkSize: 2, incremental: false});
    var onResults = sinon.stub();
    client.on('results', onResults);
    client.get({uri: 'http://example.com'});
    return awaitEvent(client, 'end').then(function () {
      assert.calledOnce(onResults);
      assert.calledWith(onResults, RESULTS);
    });
  });

  it('does not emit "results" if canceled', function () {
    var client = new SearchClient(fakeSearchFn);
    var onResults = sinon.stub();
    var onEnd = sinon.stub();
    client.on('results', onResults);
    client.on('end', onEnd);
    client.get({uri: 'http://example.com'});
    client.cancel();
    return Promise.resolve().then(function () {
      assert.notCalled(onResults);
      assert.called(onEnd);
    });
  });

  it('emits "error" event if search fails', function () {
    var err = new Error('search failed');
    fakeSearchFn = function () {
      return Promise.reject(err);
    };
    var client = new SearchClient(fakeSearchFn);
    var onError = sinon.stub();
    client.on('error', onError);
    client.get({uri: 'http://example.com'});
    return awaitEvent(client, 'end').then(function () {
      assert.calledWith(onError, err);
    });
  });
});
