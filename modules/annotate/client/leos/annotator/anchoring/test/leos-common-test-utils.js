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


/**
 * Initializes the document with the sidebar, the client url, and the
 * akn document which is given as an argument by aknHtml.
 * @param {string} aknHtml 
 * @returns {HTMLElement} The container to which aknHtml has been added
 * to as inner html.
 */
function initializeDummyHtml(aknHtml) {
    let sidebarUrl = document.createElement('link');
    sidebarUrl.rel = 'sidebar';
    sidebarUrl.href = 'test/sidebar';
    sidebarUrl.type = 'application/annotator+html';

    let clientUrl = document.createElement('link');
    clientUrl.rel = 'hypothesis-client';
    clientUrl.href = 'test/boot.js';
    clientUrl.type = 'application/annotator+javascript';

    let container = document.createElement('section');
    container.innerHTML = aknHtml;
    document.head.appendChild(sidebarUrl);
    document.head.appendChild(clientUrl);
    document.body.appendChild(container);

    return container;
}

module.exports = {
    initializeDummyHtml
}
