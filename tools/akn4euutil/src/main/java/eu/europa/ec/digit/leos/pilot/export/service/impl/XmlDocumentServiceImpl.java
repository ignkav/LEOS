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

import eu.europa.ec.digit.leos.pilot.export.exception.TemplateEngineException;
import eu.europa.ec.digit.leos.pilot.export.exception.XmlDocumentException;
import eu.europa.ec.digit.leos.pilot.export.model.ConvertDocumentInput;
import eu.europa.ec.digit.leos.pilot.export.service.TemplateEngineService;
import eu.europa.ec.digit.leos.pilot.export.service.XmlDocumentService;
import eu.europa.ec.digit.leos.pilot.export.util.ConvertUtil;
import eu.europa.ec.digit.leos.pilot.export.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class XmlDocumentServiceImpl implements XmlDocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(XmlDocumentServiceImpl.class);

    private final TemplateEngineService templateEngineService;

    public XmlDocumentServiceImpl(TemplateEngineService templateEngineService) {
        this.templateEngineService = templateEngineService;
    }

    public byte[] xmlToHtmlPackage(ConvertDocumentInput convertDocumentInput) {
        try {
            String styleSheet = "css/bill.css";
            String styleSheetFileName = Paths.get(styleSheet).getFileName().toString();
            InputStream styleSheetInputStream = new ClassPathResource(styleSheet).getInputStream();
            byte[] styleSheetOutput = StreamUtils.copyToByteArray(styleSheetInputStream);
            byte[] htmlOutput = templateEngineService.xmlToHtml(convertDocumentInput, styleSheetFileName);
            Map<String, Object> contentToZip = new HashMap<String, Object>();
            contentToZip.put(ConvertUtil.getFilename(convertDocumentInput, "html"), htmlOutput);
            contentToZip.put(styleSheet, styleSheetOutput);
            return ZipUtils.zipByteArray(contentToZip);
        } catch (IOException e) {
            LOG.error("Failed getting stylesheet or generating zip file", e);
            throw new XmlDocumentException("Failed getting stylesheet or generating zip file", e);
        } catch (TemplateEngineException e) {
            LOG.error("Error calling template engine", e);
            throw new XmlDocumentException("Error calling template engine", e);
        }
    }

    public byte[] xmlToPdfPackage(ConvertDocumentInput convertDocumentInput) {
        return "test".getBytes();
    }
}
