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

/*
** Adapted from:
** https://github.com/openannotation/annotator/blob/v1.2.x/src/plugin/document.coffee
**
** Annotator v1.2.10
** https://github.com/openannotation/annotator
**
** Copyright 2015, the Annotator project contributors.
** Dual licensed under the MIT and GPLv3 licenses.
** https://github.com/openannotation/annotator/blob/master/LICENSE
*/

const baseURI = require('document-base-uri');

const Plugin = require('../plugin');
const { normalizeURI } = require('../util/url');

/**
 * DocumentMeta reads metadata/links from the current HTML document and
 * populates the `document` property of new annotations.
 */
class DocumentMeta extends Plugin {
  constructor(element, options) {
    super(element, options);

    this.events = {
      'beforeAnnotationCreated': 'beforeAnnotationCreated',
    };
  }

  pluginInit() {
    // Test seams.
    this.baseURI = this.options.baseURI || baseURI;
    this.document = this.options.document || document;

    this.getDocumentMetadata();
  }

  /**
   * Returns the primary URI for the document being annotated
   *
   * @return {string}
   */
  uri() {
    let uri = decodeURIComponent(this._getDocumentHref());
    for (let link of this.metadata.link) {
      if (link.rel === 'canonical') {
        uri = link.href;
      }
    }
    return uri;
  }

  /**
   * Returns all uris for the document being annotated
   *
   * @return {string[]}
   */
  uris() {
    const uniqueUrls = {};
    for (let link of this.metadata.link) {
      if (link.href) { uniqueUrls[link.href] = true; }
    }
    return Object.keys(uniqueUrls);
  }

  /**
   * Hook that augments new annotations with metadata about the document they
   * came from.
   */
  beforeAnnotationCreated(annotation) {
    annotation.document = this.metadata;
  }

  /**
   * Return metadata for the current page.
   */
  getDocumentMetadata() {
    this.metadata = {};

    // first look for some common metadata types
    // TODO: look for microdata/rdfa?
    this._getHighwire();
    this._getDublinCore();
    this._getFacebook();
    this._getEprints();
    this._getPrism();
    this._getTwitter();
    this._getFavicon();

    // extract out/normalize some things
    this._getTitle();
    this._getLinks();

    return this.metadata;
  }

  // LEOS-2789 the reference element 'root' is now defined in the plugin document
  getElement() {
    return this.element[0];
  }

  _getHighwire() {
    this.metadata.highwire = this._getMetaTags('citation', 'name', '_');
  }

  _getFacebook() {
    this.metadata.facebook = this._getMetaTags('og', 'property', ':');
  }

  _getTwitter() {
    this.metadata.twitter = this._getMetaTags('twitter', 'name', ':');
  }

  _getDublinCore() {
    this.metadata.dc = this._getMetaTags('dc', 'name', '.');
  }

  _getPrism() {
    this.metadata.prism = this._getMetaTags('prism', 'name', '.');
  }

  _getEprints() {
    this.metadata.eprints = this._getMetaTags('eprints', 'name', '.');
  }

  _getMetaTags(prefix, attribute, delimiter) {
    const tags = {};
    for (let meta of Array.from(this.document.querySelectorAll('meta'))) {
      const name = meta.getAttribute(attribute);
      const { content } = meta;
      if (name) {
        const match = name.match(RegExp(`^${prefix}${delimiter}(.+)$`, 'i'));
        if (match) {
          const n = match[1];
          if (tags[n]) {
            tags[n].push(content);
          } else {
            tags[n] = [content];
          }
        }
      }
    }
    return tags;
  }

  _getTitle() {
    if (this.metadata.highwire.title) {
      this.metadata.title = this.metadata.highwire.title[0];
    } else if (this.metadata.eprints.title) {
      this.metadata.title = this.metadata.eprints.title[0];
    } else if (this.metadata.prism.title) {
      this.metadata.title = this.metadata.prism.title[0];
    } else if (this.metadata.facebook.title) {
      this.metadata.title = this.metadata.facebook.title[0];
    } else if (this.metadata.twitter.title) {
      this.metadata.title = this.metadata.twitter.title[0];
    } else if (this.metadata.dc.title) {
      this.metadata.title = this.metadata.dc.title[0];
    } else {
      this.metadata.title = this.document.title;
    }
  }

  _getLinks() {
    // we know our current location is a link for the document
    let href;
    let type;
    let values;
    this.metadata.link = [{href: this._getDocumentHref()}];

    // look for some relevant link relations
    for (let link of Array.from(this.document.querySelectorAll('link'))) {
      href = this._absoluteUrl(link.href); // get absolute url
      const { rel } = link;
      ({ type } = link);
      const lang = link.hreflang;

      if (!['alternate', 'canonical', 'bookmark', 'shortlink'].includes(rel)) { continue; }

      if (rel === 'alternate') {
        // Ignore feeds resources
        if (type && type.match(/^application\/(rss|atom)\+xml/)) { continue; }
        // Ignore alternate languages
        if (lang) { continue; }
      }

      this.metadata.link.push({href, rel, type});
    }

    // look for links in scholar metadata
    for (let name of Object.keys(this.metadata.highwire)) {
      values = this.metadata.highwire[name];
      if (name === 'pdf_url') {
        for (let url of values) {
          this.metadata.link.push({
            href: this._absoluteUrl(url),
            type: 'application/pdf',
          });
        }
      }

      // kind of a hack to express DOI identifiers as links but it's a
      // convenient place to look them up later, and somewhat sane since
      // they don't have a type
      if (name === 'doi') {
        for (let doi of values) {
          if (doi.slice(0, 4) !== 'doi:') {
            doi = `doi:${doi}`;
          }
          this.metadata.link.push({href: doi});
        }
      }
    }

    // look for links in dublincore data
    for (let name of Object.keys(this.metadata.dc)) {
      values = this.metadata.dc[name];
      if (name === 'identifier') {
        for (let id of values) {
          if (id.slice(0, 4) === 'doi:') {
            this.metadata.link.push({href: id});
          }
        }
      }
    }

    // look for a link to identify the resource in dublincore metadata
    const dcRelationValues = this.metadata.dc['relation.ispartof'];
    const dcIdentifierValues = this.metadata.dc.identifier;
    if (dcRelationValues && dcIdentifierValues) {
      const dcUrnRelationComponent =
        dcRelationValues[dcRelationValues.length - 1];
      const dcUrnIdentifierComponent =
        dcIdentifierValues[dcIdentifierValues.length - 1];
      const dcUrn = 'urn:x-dc:' +
        encodeURIComponent(dcUrnRelationComponent) + '/' +
        encodeURIComponent(dcUrnIdentifierComponent);
      this.metadata.link.push({href: dcUrn});
      // set this as the documentFingerprint as a hint to include this in search queries
      this.metadata.documentFingerprint = dcUrn;
    }
  }

  _getFavicon() {
    for (let link of Array.from(this.document.querySelectorAll('link'))) {
      if (['shortcut icon', 'icon'].includes(link.rel)) {
        this.metadata.favicon = this._absoluteUrl(link.href);
      }
    }
  }

  // Hack to get a absolute url from a possibly relative one
  _absoluteUrl(url) {
    return normalizeURI(url, this.baseURI);
  }

  // Get the true URI record when it's masked via a different protocol.
  // This happens when an href is set with a uri using the 'blob:' protocol
  // but the document can set a different uri through a <base> tag.
  _getDocumentHref() {
    const { href } = this.document.location;
    const allowedSchemes = ['http:', 'https:', 'file:'];

    // Use the current document location if it has a recognized scheme.
    const scheme = new URL(href).protocol;
    if (allowedSchemes.includes(scheme)) {
      return href;
    }

    // Otherwise, try using the location specified by the <base> element.
    if (this.baseURI && allowedSchemes.includes(new URL(this.baseURI).protocol)) {
      return this.baseURI;
    }

    // Fall back to returning the document URI, even though the scheme is not
    // in the allowed list.
    return href;
  }
}

module.exports = DocumentMeta;
