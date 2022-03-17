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

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.integration.AKN4EUService;
import eu.europa.ec.leos.integration.ToolBoxService;
import eu.europa.ec.leos.model.notification.pdfGeneration.PDFGenerationNotification;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.content.processor.TransformationService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.notification.NotificationService;
import eu.europa.ec.leos.services.store.LegService;
import eu.europa.ec.leos.services.store.PackageService;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.ws.WebServiceException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class ProposalExportServiceImpl extends ExportServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(ProposalExportServiceImpl.class);

    protected final AKN4EUService akn4euService;

    @Value("#{integrationProperties['leos.toolBox.converter.jobResultPullingThresholdInSeconds']}")
    protected int jobResultPullingThresholdInSeconds;
    @Value("#{integrationProperties['leos.toolBox.converter.jobResultMaxTries']}")
    protected int jobResultMaxTries;
    @Value("#{integrationProperties['pdf.generation.notification.functional.mailbox']}")
    private String pdfGenerationFunctionalMailBox;

    private NotificationService notificationService;
    protected final static String ZIP_PACKAGE_NAME = "AkomaNtoso2LegisWrite";

    @Autowired
    ProposalExportServiceImpl(LegService legService, PackageService packageService, Optional<ToolBoxService> toolBoxServiceO, 
            SecurityContext securityContext, ExportHelper exportHelper, BillService billService, 
            AnnexService annexService, TransformationService transformationService, NotificationService notificationService,
            AKN4EUService akn4euService) {
        super(legService, packageService, securityContext, exportHelper, billService, annexService, transformationService);
        toolBoxServiceO.ifPresent(service -> this.toolBoxService = service);
        this.notificationService = notificationService;
        this.akn4euService = akn4euService;
    }

    /**
     * Asks to Toolbox the generation of PDF/LegisWrite for the given proposalId and return the jobId.
     * The result will be sent to the email of the logged user.
     *
     * @param proposalId Proposal for which we need to generate the PDF/LegisWrite
     * @param exportOptions
     * @return jobId assigned from Toolbox for this generation.
     */
    @Override
    public String exportToToolboxCoDe(String proposalId, ExportOptions exportOptions) throws Exception {
        Validate.notNull(toolBoxService, "Export Service is not available!!");
        File legisWritePackage = null;
        String jobId;
        try {
            legisWritePackage = createCollectionPackage("job.zip", proposalId, exportOptions);
            String destinationEmail = securityContext.getUser().getEmail();
            Map<String, File> packages = new HashMap<>();
            packages.put(exportOptions.getFilePrefix() + ZIP_PACKAGE_NAME, legisWritePackage);
            jobId = toolBoxService.createJobWithEmail(proposalId, packages, destinationEmail);
        } catch (WebServiceException wse) {
            LOG.error("Webservice error occurred in method exportToToolboxCoDe(): {}", wse.getMessage());
            throw wse;
        } catch (Exception ex) {
            LOG.error("Unexpected error occurred in method exportToToolboxCoDe(): {}", ex.getMessage());
            throw ex;
        } finally {
            if (legisWritePackage != null && legisWritePackage.exists()) {
                legisWritePackage.delete();
            }
        }
        return jobId;
    }

    /**
     * Asks to Toolbox the generation of PDF/LegisWrite for the legFile passed as parameter.
     * The method first send the request to Toolbox then, with the jobId assigned, keep pulling the reply until
     * it get the answer or until the maximum numbers of tries exceed.
     *
     * @param legFile Leg file for which we need to generate the PDF/LegisWrite.
     *                It contains a full structure of a proposal (main.xml, bill, annexes, media, renditions, etc).
     * @param exportOptions
     * @return New zip/leg file returned from Toolbox containing the generated PDF/LegisWrite files
     */
    @Override
    public byte[] exportToToolboxCoDe(File legFile, ExportOptions exportOptions) throws Exception {
        LOG.debug("Calling Toolbox to convert leg file {} to {}", legFile.getName(), exportOptions.getExportOutput().name());
        File legisWritePackage = null;
        String jobId;
        try {
            legisWritePackage = createExportPackage("job.zip", legFile, exportOptions);

            Map<String, File> packages = new HashMap<>();
            packages.put(exportOptions.getFilePrefix() + ZIP_PACKAGE_NAME, legisWritePackage);
            jobId = toolBoxService.createJob(packages);
            LOG.debug("Rendition request with jobId '{}' correctly sent to Toolbox for legFile '{}'. JobFile sent to Toolbox '{}'. Waiting the reply...", jobId,
                    legFile.getName(), legisWritePackage.getName());

            return checkForReply(jobId, exportOptions.getExportOutput(), legFile.getName());
        } catch (WebServiceException wse) {
            LOG.error("Webservice error occurred in method exportToToolboxCoDe(): {}", wse.getMessage());
            throw wse;
        } catch (Exception ex) {
            LOG.error("Unexpected error occurred in method exportToToolboxCoDe(): {}", ex.getMessage());
            throw ex;
        } finally {
            if (legisWritePackage != null && legisWritePackage.exists()) {
                legisWritePackage.delete();
            }
        }
    }

    /**
     * Asks to Toolbox the generation of PDF and LegisWrite for the given proposalId and return the jobId.
     * The reply from Toolbox will be received in the callback.
     *
     * @param proposalId Proposal for which we need to generate the PDF/LegisWrite
     * @param legPackage Leg file structure in LegPackage format for which we need to generate the PDF/LegisWrite.
     * @return jobId assigned from Toolbox for this generation.
     */
    @Override
    public String exportLegPackage(String proposalId, LegPackage legPackage) throws Exception {
        Validate.notNull(toolBoxService, "Export Service is not available!!");
        String jobId;
        File pdfPackage = null;
        File legisWritePackage = null;
        try {
            final ExportLW exportOptionsPDF = new ExportLW(ExportOptions.Output.PDF);
            final ExportLW exportOptionsWord = new ExportLW(ExportOptions.Output.WORD);
            pdfPackage = createZipFile(legPackage, "job1.zip", exportOptionsPDF);
            legisWritePackage = createZipFile(legPackage, "job2.zip", exportOptionsWord);
            Map<String, File> packages = new HashMap<>();
            packages.put(exportOptionsPDF.getFilePrefix() + ZIP_PACKAGE_NAME, pdfPackage);
            packages.put(exportOptionsWord.getFilePrefix() + ZIP_PACKAGE_NAME, legisWritePackage);

            jobId = toolBoxService.createJobWithCallback(proposalId, packages);
        } catch (WebServiceException wse) {
            LOG.error("Webservice error occurred in method exportLegPackage(): {}", wse.getMessage());
            throw wse;
        } catch (Exception ex) {
            LOG.error("Unexpected error occurred in method exportLegPackage(): {}", ex.getMessage());
            throw ex;
        } finally {
            if (legisWritePackage != null && legisWritePackage.exists()) {
                legisWritePackage.delete();
            }
            if (pdfPackage != null && pdfPackage.exists()) {
                pdfPackage.delete();
            }
        }

        LOG.trace("exportLegPackage - Create milestone JobId is {}", jobId);

        return jobId;
    }

    private byte[] checkForReply(String jobId, ExportOptions.Output exportOutput, String fileName) throws IOException, InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int count = 0;
        while (count < jobResultMaxTries) {
            try {
                Pair<byte[], byte[]> zipFiles = toolBoxService.getZipFilesFromLegDocumentJobResult(jobId);
                LOG.debug("Rendition response from Toolbox in {} sec. Nr. tries made {} with a frequency of {} seconds", stopwatch.elapsed(TimeUnit.SECONDS),
                        count, jobResultPullingThresholdInSeconds);
                
                switch (exportOutput){
                    case PDF:
                        byte[] outputPdf = zipFiles.left();
                        if(outputPdf != null && outputPdf.length > 0) {
                           notificationService.sendNotification(new PDFGenerationNotification(pdfGenerationFunctionalMailBox, "PDF generations", outputPdf, fileName));
                        }
                        return outputPdf;
                    case WORD:
                        return zipFiles.right();
                }
            } catch (IllegalStateException e) {
                LOG.error("Exception occoured while retreiving document from toolbox", e);
            }

            count++;
            Thread.sleep(jobResultPullingThresholdInSeconds * 1000);
        }
        LOG.warn("Couldn't generate Rendition for legFile with jobId '{}'. jobResultMaxTries: {}, jobResultPullingThresholdInSeconds: {} . Total time {} secs",
                jobId, jobResultMaxTries, jobResultPullingThresholdInSeconds, stopwatch.elapsed(TimeUnit.SECONDS));
        throw new RuntimeException("Toolbox didn't replied in the established number of tentatives");
    }

    private File createExportPackage(String jobFileName, File legFile, ExportOptions exportOptions) throws Exception {
        Validate.notNull(jobFileName);
        Validate.notNull(exportOptions);
        Validate.notNull(legFile);
        try {
            LegPackage legPackage = legService.createLegPackage(legFile, exportOptions);
            legFile = legPackage.getFile();
            return createZipFile(legPackage, jobFileName, exportOptions);
        } finally {
            if (legFile != null && legFile.exists()) {
                legFile.delete();
            }
        }
    }

    @Override
    public void createDocumentPackage(String jobFileName, String proposalId, ExportOptions exportOptions, User user) throws Exception {
        LOG.debug("calling createLegisWritePackage()....");
        Validate.notNull(exportOptions);
        Validate.notNull(proposalId);
        LegPackage legPackage = null;
        try {
            exportOptions.setDocuwrite(false);
            legPackage = legService.createLegPackage(proposalId, exportOptions);
            akn4euService.convert(legPackage.getFile(), user, exportHelper.createJsonOutputDescriptorFile(exportOptions));
            return;
        } catch(Exception e) {
            LOG.error("An exception occurred while using the Legiswrite service: ", e);
            throw e;
        } finally {
            if(legPackage.getFile() != null && legPackage.getFile().exists()) {
                legPackage.getFile().delete();
            }
            LOG.debug("createLegisWritePackage() end....");
        }
    }
    
}
