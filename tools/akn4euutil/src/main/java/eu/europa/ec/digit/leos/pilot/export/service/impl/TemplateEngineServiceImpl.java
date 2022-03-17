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
import eu.europa.ec.digit.leos.pilot.export.exception.TemplateEngineException;
import eu.europa.ec.digit.leos.pilot.export.service.TemplateEngineService;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class TemplateEngineServiceImpl implements TemplateEngineService {
    @Value("${leos.freemarker.xml.to.html}")
    private String renditionTemplate;
    private final FreeMarkerConfigurer freemarkerConfiguration;

    private static final Logger LOG = LoggerFactory.getLogger(TemplateEngineServiceImpl.class);

    TemplateEngineServiceImpl(FreeMarkerConfigurer freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public byte[] xmlToHtml(ConvertDocumentInput convertDocumentInput, String styleSheet) throws TemplateEngineException {
        final Template template;
        final NodeModel nodeModel;
        try {
            template = freemarkerConfiguration.getConfiguration().getTemplate(renditionTemplate);
        } catch (IOException e) {
            LOG.error("Couldn't get template", e);
            throw new TemplateEngineException("Couldn't get template", e);
        }
        try {
            nodeModel = NodeModel.parse(new InputSource(convertDocumentInput.getInputFile().getInputStream()));
        } catch (IOException e) {
            LOG.error("Couldn't load input file", e);
            throw new TemplateEngineException("Couldn't load input file", e);
        } catch (SAXException | ParserConfigurationException e) {
            LOG.error("Couldn't parse input file", e);
            throw new TemplateEngineException("Couldn't parse input file", e);
        }
        final Map<String, Object> root = new HashMap<>();
        root.put("xml_data", nodeModel);
        root.put("toc_file", null);
        root.put("styleSheetName", styleSheet);

        Writer writer = new StringWriter();
        try {
            template.process(root, writer);
        } catch (IOException | TemplateException e) {
            LOG.error("Couldn't process template", e);
            throw new TemplateEngineException("Couldn't process template", e);
        }
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }
}
