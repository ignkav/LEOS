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

var configFuncSettingsFrom = require('./config-func-settings-from');
var isBrowserExtension = require('./is-browser-extension');
var sharedSettings = require('../../shared/settings');

function settingsFrom(window_) {

  var jsonConfigs = sharedSettings.jsonConfigsFrom(window_.document);
  var configFuncSettings = configFuncSettingsFrom(window_);

  /**
   * Return the href URL of the first annotator sidebar link in the given document.
   *
   * Return the value of the href attribute of the first
   * `<link type="application/annotator+html" rel="sidebar">` element in the given document.
   *
   * This URL is used as the src of the sidebar's iframe.
   *
   * @return {string} - The URL to use for the sidebar's iframe.
   *
   * @throws {Error} - If there's no annotator link or the first annotator has
   *   no href.
   *
   */
  function sidebarAppUrl() {
    var link = window_.document.querySelector('link[type="application/annotator+html"][rel="sidebar"]');

    if (!link) {
      throw new Error('No application/annotator+html (rel="sidebar") link in the document');
    }

    if (!link.href) {
      throw new Error('application/annotator+html (rel="sidebar") link has no href');
    }

    return link.href;
  }


  /**
   * Return the href URL of the first annotator client link in the given document.
   *
   * Return the value of the href attribute of the first
   * `<link type="application/annotator+html" rel="hypothesis-client">` element in the given document.
   *
   * This URL is used to identify where the client is from and what url should be
   *    used inside of subframes
   *
   * @return {string} - The URL that the client is hosted from
   *
   * @throws {Error} - If there's no annotator link or the first annotator has
   *   no href.
   *
   */
  function clientUrl() {
    var link = window_.document.querySelector('link[type="application/annotator+javascript"][rel="hypothesis-client"]');

    if (!link) {
      throw new Error('No application/annotator+javascript (rel="hypothesis-client") link in the document');
    }

    if (!link.href) {
      throw new Error('application/annotator+javascript (rel="hypothesis-client") link has no href');
    }

    return link.href;
  }

  /**
   * Return the `#annotations:*` ID from the given URL's fragment.
   *
   * If the URL contains a `#annotations:<ANNOTATION_ID>` fragment then return
   * the annotation ID extracted from the fragment. Otherwise return `null`.
   *
   * @return {string|null} - The extracted ID, or null.
   */
  function annotations() {

    /** Return the annotations from the URL, or null. */
    function annotationsFromURL() {
      // Annotation IDs are url-safe-base64 identifiers
      // See https://tools.ietf.org/html/rfc4648#page-7
      var annotFragmentMatch = window_.location.href.match(/#annotations:([A-Za-z0-9_-]+)$/);
      if (annotFragmentMatch) {
        return annotFragmentMatch[1];
      }
      return null;
    }

    return jsonConfigs.annotations || annotationsFromURL();
  }

  function showHighlights() {
    var showHighlights_ = hostPageSetting('showHighlights');

    if (showHighlights_ === null) {
      showHighlights_ = 'always';  // The default value is 'always'.
    }

    // Convert legacy keys/values to corresponding current configuration.
    if (typeof showHighlights_ === 'boolean') {
      return showHighlights_ ? 'always' : 'never';
    }

    return showHighlights_;
  }

  /**
   * Return the config.query setting from the host page or from the URL.
   *
   * If the host page contains a js-hypothesis-config script containing a
   * query setting then return that.
   *
   * Otherwise if the host page's URL has a `#annotations:query:*` (or
   * `#annotations:q:*`) fragment then return the query value from that.
   *
   * Otherwise return null.
   *
   * @return {string|null} - The config.query setting, or null.
   */
  function query() {

    /** Return the query from the URL, or null. */
    function queryFromURL() {
      var queryFragmentMatch = window_.location.href.match(/#annotations:(query|q):(.+)$/i);
      if (queryFragmentMatch) {
        try {
          return decodeURIComponent(queryFragmentMatch[2]);
        } catch (err) {
          // URI Error should return the page unfiltered.
        }
      }
      return null;
    }

    return jsonConfigs.query || queryFromURL();
  }

  function hostPageSetting(name, options = {}) {
    var allowInBrowserExt = options.allowInBrowserExt || false;
    var hasDefaultValue = typeof options.defaultValue !== 'undefined';

    if (!allowInBrowserExt && isBrowserExtension(sidebarAppUrl())) {
      return hasDefaultValue ? options.defaultValue : null;
    }

    if (configFuncSettings.hasOwnProperty(name)) {
      return configFuncSettings[name];
    }

    if (jsonConfigs.hasOwnProperty(name)) {
      return jsonConfigs[name];
    }

    if (hasDefaultValue) {
      return options.defaultValue;
    }

    return null;
  }

  return {
    get annotations() { return annotations(); },
    get clientUrl() { return clientUrl(); },
    get showHighlights() { return showHighlights(); },
    get sidebarAppUrl() { return sidebarAppUrl(); },
    get query() { return query(); },
    hostPageSetting: hostPageSetting,
  };
}

module.exports = settingsFrom;
