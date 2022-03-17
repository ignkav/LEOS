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

let RESPONSESTATUSES = require("../../shared/response-status");

let uuid = require('node-uuid');

// @ngInject
function api($rootScope, $document, $http, $location, settings) {
  let hostBridge = $document[0].hostBridge;
  let self = this;
  self.token = "";
  self.clientId = uuid.v4();
  self.retries = 0;
  self.status = RESPONSESTATUSES.SENT;

  function _getToken(token) {
    return $http({
      method: 'POST',
      url: settings.apiUrl + "token",
      data: `assertion=${token}&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer`,
      headers: {
        "X-Client-Id": self.clientId,
        "Content-Type": "application/x-www-form-urlencoded",
      },
      timeout: 30000
    });
  }

  function _changeStatus(token, group, uri, metadata) {
    return $http({
        method: 'POST',
        url: settings.apiUrl + `changeStatus?group=${group}&uri=${uri}&responseStatus=${self.status}`,
        headers: {
          "X-Client-Id": self.clientId,
          "Authorization": "Bearer " + token,
        },
        data: metadata,
        timeout: 30000
    });
  }

  function _parseResult(result, callback, nbrFailure) {
    if (result && result instanceof Array && nbrFailure < result.length) {
      if (nbrFailure === 0 && self.retries === 0) {
        callback({result: "SUCCESS", message: "Annotations successfully set to 'SENT'"});
      }
      else if (nbrFailure === 0 && self.retries > 0) {
        callback({result: "FAIL", message: "Rollback done"});
      }
      else if (self.retries < 5) {
        let newUris = [];
        result.forEach(function (res) {
          if (res.status === "SUCCESS") {
            newUris.push(res.uri);
          }
        });
        self.retries++;
        self.status = RESPONSESTATUSES.IN_PREPARATION;
        self.sendRequest(newUris, callback);  // Rollback
      }
      else {
        callback({result: "FAIL", message: "Consumed all retries for rollback"});
      }
    }
    else if (result && !(result instanceof Array)) { 
      if (result.status === "SUCCESS") {
        callback({result: "SUCCESS", message: result.message});
      } else {
        console.log("Error occurred: " + result.message);
        callback({result: "FAIL", message: result.message});
      }
    }
    else {
      callback({result: "FAIL", message: result.message});
    }
  }

  function _getReason(resp) {
    if (resp) {
      if (resp.data) {
        if (resp.data.reason) {
          return resp.data.reason;
        } else if (resp.data.error_description) {
          return resp.data.error_description;
        } else {
          return "Response data reason: '"+resp.data.reason+"', error_description: '"+resp.data.error_description+"'";
        }
      } else if (resp.status) {
        return resp.status;
      } else {
        return "Response data: '"+resp.data+", status: '"+resp.status+"'";
      }
    } else {
      return "Response: '"+resp+"'";
    }
  }

  self.sendRequest = function(uris, callback) {
    let group;
    let metadata;
    group = settings.parameters.group;
    metadata = settings.metadata;

    let responses = [];
    let nbrFailure = 0;
    let sentapiPromises = [];

    console.log("Request SecurityToken to be sent to host")
    if (hostBridge["requestSecurityToken"] && typeof hostBridge["requestSecurityToken"] == 'function') {
      // Add handler on host bridge to let leos application responds
      hostBridge["responseSecurityToken"] = function(data) {
        console.log("Received message from host for request SecurityToken");

        let timeout = setTimeout((() => _parseResult({status: "FAIL", message: "Couldn't get token, check configuration and server"}, callback)), 30000);

        _getToken(data).then(function(resp) {
          clearTimeout(timeout);
          self.token = resp.data.access_token;
          let timeouts = {};
          uris.forEach(function (uri) {
            timeouts[uri] = setTimeout((() => _parseResult({status: "FAIL", message: "Timeout sending change status request"}, callback)), 30000);
            let sentapiPromise = _changeStatus(self.token, group, uri, metadata).then(function() {
              clearTimeout(timeouts[uri]);
              responses.push({"uri": uri, "status": "SUCCESS"});
            }
            ,function(resp) {
              clearTimeout(timeouts[uri]);
              if (resp.status === 404) {  //Didn't find any annotations for this document
                responses.push({"uri": uri, "status": "SUCCESS"});
              }
              else {
                nbrFailure++;
                if (resp.status >= 500) {  //Didn't find any annotations for this document
                  console.log("Error while sending status request for uri " + uri + "; looks that server is down - " + resp.status); 
                  responses.push({"uri": uri, "status": "FAIL"});
                }
                console.log("Error while sending status request for uri " + uri + "; the reason is " + _getReason(resp));
                responses.push({"uri": uri, "status": "FAIL"});
              }
            });
            sentapiPromises.push(sentapiPromise);
          });
          Promise.all(sentapiPromises).then(function() {
            _parseResult(responses, callback, nbrFailure);
          });
        }
        ,function(resp) {
          clearTimeout(timeout);
          console.log("Error while getting token; the reason is " + _getReason(resp));
          _parseResult({status: "FAIL", message: "Token request failure"}, callback);
        });
      };
      hostBridge["requestSecurityToken"]();
    }
    else {
      console.log("Error while getting token from configuration");
      _parseResult({status: "FAIL", message: "Error while getting token from configuration"}, callback);
    }
  }
}

module.exports = api;
