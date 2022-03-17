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
package eu.europa.ec.leos.services.content.processor;


import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import io.atlassian.fugue.Pair;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface BillProcessor {

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] insertNewElement(Bill document, String elementId, boolean before, String tagName);

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] deleteElement(Bill document, String elementId, String tagName, User user) throws Exception;

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] mergeElement(Bill document, String elementContent, String elementName, String elementId);

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] updateElement(Bill document, String elementName, String elementId, String elementContent) throws Exception;

    Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception;

    Element getMergeOnElement(Bill document, String elementContent, String elementName, String elementId) throws Exception;

    Element getTocElement(final Bill document, final String elementId, final List<TableOfContentItemVO> toc);
}
