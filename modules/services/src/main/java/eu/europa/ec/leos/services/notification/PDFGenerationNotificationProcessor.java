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
package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.notification.EmailNotification;
import eu.europa.ec.leos.model.notification.pdfGeneration.PDFGenerationEmailNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PDFGenerationNotificationProcessor implements EmailNotificationProcessor<PDFGenerationEmailNotification> {

    @Autowired
    private MessageHelper messageHelper;

    private final FreemarkerNotificationProcessor processor;

    @Autowired
    public PDFGenerationNotificationProcessor(FreemarkerNotificationProcessor processor, MessageHelper messageHelper) {
        this.processor = processor;
        this.messageHelper = messageHelper;
    }

    @Override
    public void process(PDFGenerationEmailNotification emailNotification) {
        buildEmailBody(emailNotification);
        buildEmailSubject(emailNotification);

    }

    @Override
    public boolean canProcess(EmailNotification emailNotification) {
        return PDFGenerationEmailNotification.class.isAssignableFrom(emailNotification.getClass());
    }

    private void buildEmailBody(PDFGenerationEmailNotification pdfGenerationEmailNotification) {
        pdfGenerationEmailNotification.setEmailBody(processor.processTemplate(pdfGenerationEmailNotification));
    }

    private void buildEmailSubject(PDFGenerationEmailNotification pdfGenerationEmailNotification) {
        String title = pdfGenerationEmailNotification.getTitle();
        pdfGenerationEmailNotification
                .setEmailSubject(messageHelper.getMessage(pdfGenerationEmailNotification.getEmailSubjectKey(), new Object[]{title}));
    }
}
