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

const debounce = require('lodash.debounce');
require('ckeditor');

const markdown = require('../../../src/sidebar/components/markdown');
const mediaEmbedder = require('../../../src/sidebar/media-embedder');
const renderMarkdown = require('../../../src/sidebar/render-markdown');


// @ngInject
function LeosMarkdownController($element, $sanitize, $scope) {
  const input = $element.find('textarea')[0];
  const output = $element[0].querySelector('.js-markdown-preview');

  const self = this;

  self.$onInit = function() {
    const editor = CKEDITOR.replace(input, {
      extraPlugins: 'autolink',
      startupFocus: 'end',
      height: '8em',
      toolbar: [
        { name: 'basicstyles', items: [ 'Bold', 'Italic' ] },
        { name: 'links', items: [ 'Link', 'Unlink' ] },
      ],
    });

    const handleInputChange = debounce(function () {
      $scope.$apply(function () {
        self.onEditText({text: editor.getData()});
      });
    }, 100);

    editor.on('change', handleInputChange);

    editor.on('instanceReady', function() {
      // Somehow, an on event listener can not be directly added to the editor instance. Therefore, the editor is retrieved anew.
      document.getElementById('cke_' + editor.name).onclick = function(event) {
        event.stopPropagation();
      };
    });

    $scope.$watch('vm.showEditor()', function (show) {
      if (show) {
        editor.setData(self.text || '');
      }
    });
    
    CKEDITOR.on('dialogDefinition', function(event) {
      const dialog = event.data.dialog;
      const dialogName = event.data.name;
      const dialogDefinition = event.data.definition;

      if (dialogName === 'link') {
        // Set "New Windows (_blank)" as default target option.
        const targetTab = dialogDefinition.getContents('target');
        const targetField = targetTab.get('linkTargetType');
        targetField['default'] = '_blank';

        // Hide tab row.
        dialog.parts.tabs.$.style.display = 'none';
        const dialogContents = dialog.parts.dialog.$.querySelector('.cke_dialog_contents');
        dialogContents.style['margin-top'] = 0;
        dialogContents.style['border-top'] = 'none';
        
        dialogDefinition.dialog.on('show', function () {
          // Make height adjust automaticaly.
          const dialogContentsBody = dialog.parts.dialog.$.querySelector('.cke_dialog_contents_body');
          dialogContentsBody.style.height = 'auto';

          // Hide link type field.
          const linkTypeFieldRow = dialog.parts.dialog.$.querySelector('div > table tr:nth-child(2)');
          linkTypeFieldRow.style.display = 'none';

          // Hide protocol field.
          const protocolFieldCell = dialog.parts.dialog.$.querySelector('div > table tr:nth-child(3) tr tr td:first-child');
          protocolFieldCell.style.display = 'none';
        });

        // Disable resizing dialog window.
        dialogDefinition.resizable = CKEDITOR.DIALOG_RESIZE_NONE;
      }
    });
  };

  self.showEditor = function () {
    return !self.readOnly;
  };

  // Re-render the markdown when the view needs updating.
  $scope.$watch('vm.text', function () {
    output.innerHTML = renderMarkdown(self.text || '', $sanitize);
    mediaEmbedder.replaceLinksWithEmbeds(output);
  });
}

module.exports = {
  controller: LeosMarkdownController,
  controllerAs: 'vm',
  bindings: markdown.bindings,
  template: require('../templates/leos-ckeditor-markdown.html'),
};
