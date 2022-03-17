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
package eu.europa.ec.leos.services.export;

import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
class ExportHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ExportServiceImpl.class);

    private final Configuration freemarkerConfiguration;

    @Value("${leos.freemarker.ftl.export.legiswrite.pdf}")
    private String exportTemplateLW_pdf;

    @Value("${leos.freemarker.ftl.export.legiswrite.word}")
    private String exportTemplateLW_word;
    
    @Value("${leos.freemarker.ftl.export.docuwrite.pdf_legaltext}")
    private String exportTemplateDW_pdfLegalText;

    @Value("${leos.freemarker.ftl.export.docuwrite.word_legaltext}")
    private String exportTemplateDW_wordLegalText;

    @Value("${leos.freemarker.ftl.export.docuwrite.pdf}")
    private String exportTemplateDW_pdf;

    @Value("${leos.freemarker.ftl.export.docuwrite.word}")
    private String exportTemplateDW_word;

    @Value("${leos.freemarker.ftl.export.docuwrite.word_annex}")
    private String exportTemplateDW_wordAnnex;

    @Value("${leos.freemarker.ftl.export.docuwrite.pdf_annex}")
    private String exportTemplateDW_pdfAnnex;

    @Autowired
    public ExportHelper (Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public ByteArrayOutputStream createContentFile(ExportOptions exportOptions, ExportResource exportRootNode) throws Exception {
        Validate.notNull(exportOptions);
        Validate.notNull(exportRootNode);
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        LOG.trace("Creating content file document...");
        try {
            Map<String, ExportResource> resources = new HashMap<>();
            resources.put("resource_tree", exportRootNode);
            String templateName = getTemplateName(exportOptions);
            StringWriter outputWriter = new StringWriter();
            Template template = freemarkerConfiguration.getTemplate(templateName);
            template.process(resources, outputWriter);
            String result = outputWriter.getBuffer().toString();
            byteOutputStream.write(result.getBytes(UTF_8));
        } catch (Exception ex) {
            LOG.error("Error while creating content xml file {}", ex.getMessage());
            throw ex;
        }
        return byteOutputStream;
    }

    public String createJsonOutputDescriptorFile(ExportOptions exportOptions) {
        Validate.notNull(exportOptions);
        String outputDescriptor = new String("{\n");

        LOG.trace("Creating json ouput descriptor...");

        if (exportOptions.getExportOutput().equals(ExportOptions.Output.WORD)) {
            outputDescriptor += "   \"format\":[\"LW\"],\n";
        } else {
            outputDescriptor += "   \"format\":[\"PDF\"],\n";
        }
        outputDescriptor += "   \"mode\":\"individual\",\n";

        if (exportOptions.isWithAnnotations()) {
            outputDescriptor += "    \"convertAnnotations\":\"yes\",\n";
        } else {
            outputDescriptor += "    \"convertAnnotations\":\"no\",\n";
        }
        outputDescriptor += "   \"documents\":[\n";
        outputDescriptor += "      \"" + exportOptions.getExportVersions().getCurrent().getName() + "\"\n";
        outputDescriptor += "   ]\n";
        outputDescriptor += "}";

        return outputDescriptor;
    }

    private String getTemplateName(ExportOptions exportOptions) {
        String templateName;
        if (exportOptions instanceof ExportLW) {
            templateName = getTemplate(exportOptions.getExportOutput(), exportTemplateLW_pdf, exportTemplateLW_word);
        } else if (exportOptions instanceof ExportDW) {
            if (Bill.class.equals(exportOptions.getFileType())) {
                templateName = getTemplate(exportOptions.getExportOutput(), exportTemplateDW_pdfLegalText, exportTemplateDW_wordLegalText);
            } else if (Annex.class.equals(exportOptions.getFileType())) {
                templateName = getTemplate(exportOptions.getExportOutput(), exportTemplateDW_pdfAnnex, exportTemplateDW_wordAnnex);
            } else {
                templateName = getTemplate(exportOptions.getExportOutput(), exportTemplateDW_pdf, exportTemplateDW_word);
            }
        } else {
            throw new IllegalStateException("Not possible!!!");
        }
        return templateName;
    }
    
    protected String getTemplate(ExportOptions.Output exportOutput, String pdfTemplate, String wordTemplate) {
        switch (exportOutput) {
            case PDF:
                return pdfTemplate;
            case WORD:
                return wordTemplate;
            default:
                throw new IllegalStateException("Not possible!!!");
        }
    }
}