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
package eu.europa.ec.digit.leos.pilot.export.service.impl;

import eu.europa.ec.digit.leos.pilot.export.model.ConvertDocumentInput;
import eu.europa.ec.digit.leos.pilot.export.service.LeosDocumentService;
import eu.europa.ec.digit.leos.pilot.export.service.XmlDocumentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LeosDocumentServiceImpl implements LeosDocumentService {
    
    private final XmlDocumentService xmlDocumentService;

    public LeosDocumentServiceImpl(XmlDocumentService xmlDocumentService) {
        this.xmlDocumentService = xmlDocumentService;
    }

    public ConvertDocumentInput createDocumentInput(MultipartFile inputFile, boolean isWithAnnotations) {
        final ConvertDocumentInput convertDocumentInput = new ConvertDocumentInput();
        convertDocumentInput.setInputFile(inputFile);
        convertDocumentInput.setWithAnnotations(isWithAnnotations);
        return convertDocumentInput;
        
    }

    public byte[] getRenditions(ConvertDocumentInput convertDocumentInput) {
        return xmlDocumentService.xmlToHtmlPackage(convertDocumentInput);
    }
}