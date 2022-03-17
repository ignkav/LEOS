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
package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.services.numbering.config.NumberingConfigProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface NumberProcessor {

    boolean canRenumber(String elementName, Node element, boolean renumberChildren);

    void renumber(String elementName, Document document, Node firstChild, NumberingConfigProcessor numberProcessor);
}
