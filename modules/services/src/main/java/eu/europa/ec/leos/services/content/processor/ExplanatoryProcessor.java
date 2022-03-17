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

import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import io.atlassian.fugue.Pair;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ExplanatoryProcessor {

    /**
     * Inserts a new explanatory block before or after the current block with the given id. And saves the document
     * @param document The document to update
     * @param elementId  The id of the explanatory block before or after
     * @param before true if the new block needs to be inserted before the given block, false if it needs to be inserted after.
     * @return The updated document
     */
    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] insertExplanatoryBlock(Explanatory document, String elementId, String tagName, boolean before);

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] deleteExplanatoryBlock(Explanatory document, String elementId, String tagName) throws Exception;

    Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception;

    Element getMergeOnElement(Explanatory document, String elementContent, String elementName, String elementId) throws Exception;

    Element getTocElement(final Explanatory document, final String elementId, final List<TableOfContentItemVO> toc);

    byte[] mergeElement(Explanatory document, String elementContent, String elementName, String elementId);

    byte[] updateExplanatoryBlock(Explanatory document, String elementId, String tagName, String elementFragment);

}
