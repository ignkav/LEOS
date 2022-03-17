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
var events = require('../events');
var isThirdPartyService = require('../util/is-third-party-service');
var memoize = require('../util/memoize');
var tabs = require('../tabs');

const authorityChecker = require('../../../leos/sidebar/authority-checker');
const OPERATION_MODES = require('../../../leos/shared/operationMode');

function firstKey(object) {
  for (var k in object) {
    if (!Object.prototype.hasOwnProperty.call(object, k)) {
      continue;
    } else if (k === 'undefined') {
      return null;
    }
    return k;
  }
  return null;
}

/**
 * Returns the group ID of the first annotation in `results` whose
 * ID is a key in `selection`.
 */
function groupIDFromSelection(selection, results) {
  var id = firstKey(selection);
  var annot = results.find(function (annot) {
    return annot.id === id;
  });
  if (!annot) {
    return null;
  }
  return annot.group;
}

// @ngInject
function SidebarContentController($scope, analytics, bridge, store, annotationMapper, api, frameSync, groups, rootThread, settings, streamer, streamFilter) {
  var self = this;

  angular.element(document.querySelector('thread-list')).bind('scroll', function() {
    bridge.call('LEOS_refreshAnnotationLinkLines');
  });

  function thread() {
    return rootThread.thread(store.getState());
  }

  var unsubscribeAnnotationUI = store.subscribe(function () {
    var state = store.getState();

    self.rootThread = thread();
    self.selectedTab = state.selectedTab;

    var counts = tabs.counts(state.annotations);

    Object.assign(self, {
      totalNotes: counts.notes,
      totalAnnotations: counts.annotations,
      totalOrphans: counts.orphans,
      waitingToAnchorAnnotations: counts.anchoring > 0,
    });
  });

  $scope.$on('$destroy', unsubscribeAnnotationUI);

  function focusAnnotation(annotation) {
    var highlights = [];
    if (annotation) {
      highlights = [annotation.$tag];
    }
    frameSync.focusAnnotations(highlights);
  }

  function scrollToAnnotation(annotation) {
    if (!annotation) {
      return;
    }
    frameSync.scrollToAnnotation(annotation.$tag);
    frameSync.LEOS_selectAnnotation(annotation); //LEOS Change
  }

  /**
   * Returns the Annotation object for the first annotation in the
   * selected annotation set. Note that 'first' refers to the order
   * of annotations passed to store when selecting annotations,
   * not the order in which they appear in the document.
   */
  function firstSelectedAnnotation() {
    if (store.getState().selectedAnnotationMap) {
      var id = Object.keys(store.getState().selectedAnnotationMap)[0];
      return store.getState().annotations.find(function (annot) {
        return annot.id === id;
      });
    } else {
      return null;
    }
  }

  var searchClients = [];

  function _resetAnnotations() {
    annotationMapper.unloadAnnotations(store.savedAnnotations(), true);
  }

  function _loadAnnotationsFor(uris, group) {
    var searchClient = new SearchClient(api.search, {
      // If no group is specified, we are fetching annotations from
      // all groups in order to find out which group contains the selected
      // annotation, therefore we need to load all chunks before processing
      // the results
      incremental: !!group,
    });
    searchClients.push(searchClient);
    searchClient.on('results', function (results) {
      if (store.hasSelectedAnnotations()) {
        // Focus the group containing the selected annotation and filter
        // annotations to those from this group
        var groupID = groupIDFromSelection(
          store.getState().selectedAnnotationMap, results);
        if (!groupID) {
          // If the selected annotation is not available, fall back to
          // loading annotations for the currently focused group
          groupID = groups.focused().id;
        }
        results = results.filter(function (result) {
          return result.group === groupID;
        });
        groups.focus(groupID);
      }

      if (results.length) {
        annotationMapper.loadAnnotations(results);
      }
    });
    searchClient.on('end', function () {
      // Remove client from list of active search clients.
      //
      // $evalAsync is required here because search results are emitted
      // asynchronously. A better solution would be that the loading state is
      // tracked as part of the app state.
      $scope.$evalAsync(function () {
        searchClients.splice(searchClients.indexOf(searchClient), 1);
      });

      store.frames().forEach(function (frame) {
        if (0 <= uris.indexOf(frame.uri)) {
          store.updateFrameAnnotationFetchStatus(frame.uri, true);
        }
      });
    });
    // LEOS Change
    var queryJson = {uri: uris, group: group};
    if(settings.connectedEntity) {
      queryJson.connectedEntity = settings.connectedEntity;
    }
    requestSearchMetadata().then( function([metadatasets]) {
      if (metadatasets !== null) {
        var leosMetadata = JSON.parse(metadatasets);
        queryJson.metadatasets = JSON.stringify(leosMetadata);
        searchClient.get(queryJson);
      }
      else {
        searchClient.get(queryJson);
      }
    }).catch(function() {
      searchClient.get(queryJson);
    });
    // ---------------
  }

  function isLoading() {
    if (!store.frames().some(function (frame) { return frame.uri; })) {
      // The document's URL isn't known so the document must still be loading.
      return true;
    }

    if (searchClients.length > 0) {
      // We're still waiting for annotation search results from the API.
      return true;
    }

    return false;
  }

  /**
   * Load annotations for all URLs associated with `frames`.
   */
  function loadAnnotations() {
    _resetAnnotations();

    searchClients.forEach(function (client) {
      client.cancel();
    });

    // If there is no selection, load annotations only for the focused group.
    //
    // If there is a selection, we load annotations for all groups, find out
    // which group the first selected annotation is in and then filter the
    // results on the client by that group.
    //
    // In the common case where the total number of annotations on
    // a page that are visible to the user is not greater than
    // the batch size, this saves an extra roundtrip to the server
    // to fetch the selected annotation in order to determine which group
    // it is in before fetching the remaining annotations.
    var group = store.hasSelectedAnnotations() ?
      null : groups.focused().id;

    var searchUris = store.searchUris();
    if (searchUris.length > 0) {
      _loadAnnotationsFor(searchUris, group);

      streamFilter.resetFilter().addClause('/uri', 'one_of', searchUris);
      streamer.setConfig('filter', {filter: streamFilter.getFilter()});
    }
  }

  $scope.$on('sidebarOpened', function () {

    analytics.track(analytics.events.SIDEBAR_OPENED);

    streamer.connect();

    //LEOS Change - make default loaded group as Collaborators
    if(!authorityChecker.isISC(settings) &&
        groups.focused() && groups.focused().id !== groups.defaultGroupId()) {
      groups.focus(groups.defaultGroupId());
    }
  });

  this.$onInit = () => {
    // If the user is logged in, we connect nevertheless
    if (this.auth.status === 'logged-in') {
      streamer.connect();
    }
  };

  function extractAnnotationChildren(thread) {
    var annotations = [];
    if (thread.totalChildren > 0) {
      for (let child of thread.children) {
        annotations.push(child.annotation);
      }
    }
    return annotations;
  }

  function extractReplies(thread) {
    var replies = [];
    if (thread.replyCount > 0) {
      for (let child of thread.children) {
        replies.push(child.annotation);
        Array.prototype.push.apply(replies, extractReplies(child));
      }
    }
    return replies;
  }

  function extractAnnotationReplies(thread) {
    var replies = [];
    if (thread.totalChildren > 0) {
      for (let child of thread.children) {
        Array.prototype.push.apply(replies, extractReplies(child));
      }
    }
    return replies;
  }

  $scope.$on("LEOS_requestFilteredAnnotations", function () {
    var replies = extractAnnotationReplies(self.rootThread);
    var annotations = extractAnnotationChildren(self.rootThread);

    var filteredAnnotations = {
      "rows": annotations,
      "total": self.rootThread.children.length,
      "replies": replies
    };
    bridge.call('LEOS_responseFilteredAnnotations', JSON.stringify(filteredAnnotations));
  });

  $scope.$on(events.USER_CHANGED, function () {
    streamer.reconnect();
  });

  $scope.$on(events.ANNOTATIONS_SYNCED, function (event, tags) {
    // When a direct-linked annotation is successfully anchored in the page,
    // focus and scroll to it
    var selectedAnnot = firstSelectedAnnotation();
    if (!selectedAnnot) {
      return;
    }
    var matchesSelection = tags.some(function (tag) {
      return tag === selectedAnnot.$tag;
    });
    if (!matchesSelection) {
      return;
    }
    focusAnnotation(selectedAnnot);
    scrollToAnnotation(selectedAnnot);

    store.selectTab(tabs.tabForAnnotation(selectedAnnot));
  });

  // Re-fetch annotations when focused group, logged-in user or connected frames
  // change.
  $scope.$watch(() => ([
    groups.focused().id,
    store.profile().userid,
    ...store.searchUris(),
  ]), ([currentGroupId], [prevGroupId]) => {

    if (currentGroupId !== prevGroupId) {
      // The focused group may be changed during loading annotations as a result
      // of switching to the group containing a direct-linked annotation.
      //
      // In that case, we don't want to trigger reloading annotations again.
      if (isLoading()) {
        return;
      }
      store.clearFilteredAndSelectedAnnotations();
      frameSync.deselectAllAnnotations();
    }

    loadAnnotations();
  }, true);

  this.setCollapsed = function (id, collapsed) {
    store.setCollapsed(id, collapsed);
  };

  this.forceVisible = function (thread) {
    store.setForceVisible(thread.id, true);
    if (thread.parent) {
      store.setCollapsed(thread.parent.id, false);
    }
  };

  this.focus = focusAnnotation;
  this.scrollTo = scrollToAnnotation;

  //LEOS Change
  this.selectionPaneAvailable = function () {
    return settings.operationMode !== OPERATION_MODES.READ_ONLY;
  };

  this.selectedAnnotationUnavailable = function () {
    var selectedID = firstKey(store.getState().selectedAnnotationMap);
    return !isLoading() &&
           !!selectedID &&
           !store.annotationExists(selectedID);
  };

  this.shouldShowLoggedOutMessage = function () {
    // If user is not logged out, don't show CTA.
    if (self.auth.status !== 'logged-out') {
      return false;
    }

    // If user has not landed on a direct linked annotation
    // don't show the CTA.
    if (!settings.annotations) {
      return false;
    }

    // The CTA text and links are only applicable when using Hypothesis
    // accounts.
    if (isThirdPartyService(settings)) {
      return false;
    }

    // The user is logged out and has landed on a direct linked
    // annotation. If there is an annotation selection and that
    // selection is available to the user, show the CTA.
    var selectedID = firstKey(store.getState().selectedAnnotationMap);
    return !isLoading() &&
           !!selectedID &&
           store.annotationExists(selectedID);
  };

  this.isLoading = isLoading;

  var visibleCount = memoize(function (thread) {
    return thread.children.reduce(function (count, child) {
      return count + visibleCount(child);
    }, thread.visible ? 1 : 0);
  });

  this.visibleCount = function () {
    return visibleCount(thread());
  };

  // LEOS Change
  $scope.$on('reloadAnnotations', function () {
    store.clearFilteredAndSelectedAnnotations();
    frameSync.deselectAllAnnotations();
    loadAnnotations();
  });

  function requestSearchMetadata() {
    return new Promise( function(resolve, reject) {
      var promiseTimeout = setTimeout(() => reject('timeout'), 500);
      bridge.call('requestSearchMetadata', function (error, result) {
        clearTimeout(promiseTimeout);
        if (error) {
          return reject(error);
        } else {
          return resolve(result);
        }
      });
    });
  }

  // -----------
}

module.exports = {
  controller: SidebarContentController,
  controllerAs: 'vm',
  bindings: {
    auth: '<',
    search: '<',
  },
  template: require('../templates/sidebar-content.html'),
};
