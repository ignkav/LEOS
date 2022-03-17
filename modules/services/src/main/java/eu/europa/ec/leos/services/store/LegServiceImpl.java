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
package eu.europa.ec.leos.services.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cool.graph.cuid.Cuid;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosLegStatus;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.document.LegDocument;
import eu.europa.ec.leos.domain.cmis.document.MediaDocument;
import eu.europa.ec.leos.domain.cmis.document.Memorandum;
import eu.europa.ec.leos.domain.cmis.document.Proposal;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.domain.vo.DocumentVO;
import eu.europa.ec.leos.domain.vo.LegDocumentVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.rendition.RenderedDocument;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.repository.store.WorkspaceRepository;
import eu.europa.ec.leos.security.LeosPermissionAuthorityMapHelper;
import eu.europa.ec.leos.services.Annotate.AnnotateService;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.content.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.converter.ProposalConverterService;
import eu.europa.ec.leos.services.document.AnnexService;
import eu.europa.ec.leos.services.document.BillService;
import eu.europa.ec.leos.services.document.MemorandumService;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.ExportResource;
import eu.europa.ec.leos.services.export.ExportVersions;
import eu.europa.ec.leos.services.export.LegPackage;
import eu.europa.ec.leos.services.export.RelevantElements;
import eu.europa.ec.leos.services.export.ZipPackageUtil;
import eu.europa.ec.leos.services.rendition.HtmlRenditionProcessor;
import eu.europa.ec.leos.services.support.TableOfContentHelper;
import eu.europa.ec.leos.services.support.xml.XPathCatalog;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlHelper;
import eu.europa.ec.leos.services.support.xml.XmlNodeConfig;
import eu.europa.ec.leos.services.support.xml.XmlNodeConfigHelper;
import eu.europa.ec.leos.services.support.xml.XmlNodeProcessor;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.TableOfContentItemHtmlVO;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_INTERMEDIATE_STYLE;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ORIGINAL_STYLE;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_REMOVED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_RETAIN_CLASS;
import static eu.europa.ec.leos.services.support.xml.XmlNodeConfigHelper.createValueMap;
import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class LegServiceImpl implements LegService {
    private static final Logger LOG = LoggerFactory.getLogger(LegServiceImpl.class);
    
    private final PackageRepository packageRepository;
    private final WorkspaceRepository workspaceRepository;
    private final AttachmentProcessor attachmentProcessor;
    private final XmlNodeProcessor xmlNodeProcessor;
    private final XmlNodeConfigHelper xmlNodeConfigHelper;
    private final AnnotateService annotateService;
    private final HtmlRenditionProcessor htmlRenditionProcessor;
    private final ProposalConverterService proposalConverterService;
    private final LeosPermissionAuthorityMapHelper authorityMapHelper;
    private final ContentComparatorService compareService;
    private final MessageHelper messageHelper;
    private final Provider<StructureContext> structureContextProvider;
    private final BillService billService;
    private final AnnexService annexService;
    private final MemorandumService memorandumService;
    private final XmlContentProcessor xmlContentProcessor;

    private static final String MEDIA_DIR = "media/";
    private static final String ANNOT_FILE_EXT = ".json";
    private static final String ANNOT_FILE_PREFIX = "annot_";
    private static final String LEG_FILE_PREFIX = "leg_";
    private static final String LEG_FILE_EXTENSION = ".leg";
    private static final String STYLE_SHEET_EXT = ".css";
    private static final String JS_EXT = ".js";
    private static final String STYLE_DEST_DIR = "renditions/html/css/";
    private static final String JS_DEST_DIR = "renditions/html/js/";
    private static final String STYLES_SOURCE_PATH = "META-INF/resources/assets/css/";
    private static final String JS_SOURCE_PATH = "META-INF/resources/js/";
    private static final String JQUERY_SOURCE_PATH = "META-INF/resources/lib/jquery_3.2.1/";
    private static final String JQTREE_SOURCE_PATH = "META-INF/resources/lib/jqTree_1.4.9/";
    private static final String HTML_RENDITION = "renditions/html/";
    private static final String PDF_RENDITION = "renditions/pdf/";
    private static final String WORD_RENDITION = "renditions/word/";
    
    private static final String annexStyleSheet = LeosCategory.ANNEX.name().toLowerCase() + STYLE_SHEET_EXT;
    private static final String memoStyleSheet = LeosCategory.MEMORANDUM.name().toLowerCase() + STYLE_SHEET_EXT;
    private static final String billStyleSheet = LeosCategory.BILL.name().toLowerCase() + STYLE_SHEET_EXT;

    @Autowired
    private XPathCatalog xPathCatalog;

    @Autowired
    LegServiceImpl(PackageRepository packageRepository,
                   WorkspaceRepository workspaceRepository,
                   AttachmentProcessor attachmentProcessor,
                   XmlNodeProcessor xmlNodeProcessor,
                   XmlNodeConfigHelper xmlNodeConfigHelper,
                   AnnotateService annotateService,
                   HtmlRenditionProcessor htmlRenditionProcessor,
                   ProposalConverterService proposalConverterService,
                   LeosPermissionAuthorityMapHelper authorityMapHelper,
                   ContentComparatorService compareService,
                   MessageHelper messageHelper,
                   Provider<StructureContext> structureContextProvider,
                   BillService billService,
                   MemorandumService memorandumService,
                   AnnexService annexService, XmlContentProcessor xmlContentProcessor) {
        this.packageRepository = packageRepository;
        this.workspaceRepository = workspaceRepository;
        this.attachmentProcessor = attachmentProcessor;
        this.xmlNodeProcessor = xmlNodeProcessor;
        this.xmlNodeConfigHelper = xmlNodeConfigHelper;
        this.annotateService = annotateService;
        this.htmlRenditionProcessor = htmlRenditionProcessor;
        this.proposalConverterService = proposalConverterService;
        this.authorityMapHelper = authorityMapHelper;
        this.messageHelper = messageHelper;
        this.compareService = compareService;
        this.structureContextProvider = structureContextProvider;
        this.billService = billService;
        this.memorandumService = memorandumService;
        this.annexService = annexService;
        this.xmlContentProcessor = xmlContentProcessor;
    }
    
    @Override
    public LegDocument findLastLegByVersionedReference(String path, String versionedReference) {
        return packageRepository.findLastLegByVersionedReference(path, versionedReference);
    }
    
    @Override
    public List<LegDocumentVO> getLegDocumentDetailsByUserId(String userId) {
        List<Proposal> proposals = packageRepository.findDocumentsByUserId(userId, Proposal.class, authorityMapHelper.getRoleForDocCreation());
        List<LegDocumentVO> legDocumentVOs = new ArrayList<>();
        for (Proposal proposal : proposals) {
            LeosPackage leosPackage = packageRepository.findPackageByDocumentId(proposal.getId());
            List<LegDocument> legDocuments = packageRepository.findDocumentsByPackagePath(leosPackage.getPath(), LegDocument.class, false);
            if (!legDocuments.isEmpty()) {
                LegDocument leg = legDocuments.get(0);
                LegDocumentVO legDocumentVO = new LegDocumentVO();
                legDocumentVO.setProposalId(proposal.getMetadata().getOrError(() -> "Proposal metadata is not available!").getRef());
                legDocumentVO.setDocumentTitle(proposal.getTitle());
                legDocumentVO.setLegFileId(leg.getId());
                legDocumentVO.setLegFileName(leg.getName());
                legDocumentVO.setLegFileStatus(leg.getStatus().name());
                legDocumentVO.setMilestoneComments(leg.getMilestoneComments());
                legDocumentVO.setCreationDate(Date.from(leg.getInitialCreationInstant()).toString());
                legDocumentVO.setClonedProposal(proposal.isClonedProposal());
                legDocumentVOs.add(legDocumentVO);
            }
        }
        return legDocumentVOs;
    }
    
    private String generateLegName() {
        return LEG_FILE_PREFIX + Cuid.createCuid() + LEG_FILE_EXTENSION;
    }
    
    private byte[] addMetadataToProposal(Proposal proposal) {
        byte[] xmlContent = proposal.getContent().get().getSource().getBytes();
        ProposalMetadata metadata = proposal.getMetadata().get();
        metadata = metadata.withObjectId(proposal.getId()).withDocVersion(proposal.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigHelper.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    private byte[] addMetadataToMemorandum(Memorandum memorandum) {
        byte[] xmlContent = memorandum.getContent().get().getSource().getBytes();
        MemorandumMetadata metadata = memorandum.getMetadata().get();
        metadata = metadata.withObjectId(memorandum.getId()).withDocVersion(memorandum.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigHelper.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    private byte[] addMetadataToBill(Bill bill) {
        byte[] xmlContent = bill.getContent().get().getSource().getBytes();
        BillMetadata metadata = bill.getMetadata().get();
        metadata = metadata.withObjectId(bill.getId()).withDocVersion(bill.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigHelper.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    private byte[] addMetadataToAnnex(Annex annex) {
        byte[] xmlContent = annex.getContent().get().getSource().getBytes();
        AnnexMetadata metadata = annex.getMetadata().get();
        metadata = metadata.withObjectId(annex.getId()).withDocVersion(annex.getVersionLabel());
        xmlContent = xmlNodeProcessor.setValuesInXml(xmlContent, createValueMap(metadata), xmlNodeConfigHelper.getConfig(metadata.getCategory()));
        return xmlContent;
    }
    
    /**
     * Creates the LegPackage for the given leg file.
     *
     * @param legFile legFile for which we need to create the LegPackage
     * @param exportOptions
     * @return LegPackage used to be sent to Toolbox for PDF/LegisWrite generation.
     */
    @Override
    public LegPackage createLegPackage(File legFile, ExportOptions exportOptions) throws IOException {
        // legFile will be deleted after createProposalFromLegFile(), so we save the bytes in a temporary file
        File legFileTemp = File.createTempFile("RENDITION_", ".leg");
        FileUtils.copyFile(legFile, legFileTemp);
        
        final DocumentVO proposalVO = proposalConverterService.createProposalFromLegFile(legFile, new DocumentVO(LeosCategory.PROPOSAL), false);
        final byte[] proposalXmlContent = proposalVO.getSource();
        ExportResource proposalExportResource = new ExportResource(LeosCategory.PROPOSAL);
        final Map<String, String> proposalRefsMap = buildProposalExportResource(proposalExportResource, proposalXmlContent);
        proposalExportResource.setExportOptions(exportOptions);
        final DocumentVO memorandumVO = proposalVO.getChildDocument(LeosCategory.MEMORANDUM);
        final byte[] memorandumXmlContent = memorandumVO.getSource();
        final ExportResource memorandumExportResource = buildExportResourceMemorandum(proposalRefsMap, memorandumXmlContent);
        proposalExportResource.addChildResource(memorandumExportResource);
        
        final DocumentVO billVO = proposalVO.getChildDocument(LeosCategory.BILL);
        final byte[] billXmlContent = billVO.getSource();
        final ExportResource billExportResource = buildExportResourceBill(proposalRefsMap, billXmlContent);
        
        // add annexes to billExportResource
        final Map<String, String> attachmentIds = attachmentProcessor.getAttachmentsIdFromBill(billXmlContent);
        final List<DocumentVO> annexesVO = billVO.getChildDocuments(LeosCategory.ANNEX);
        annexesVO.forEach((annexVO) -> {
            final byte[] annexXmlContent = annexVO.getSource();
            final int docNumber = Integer.parseInt(annexVO.getMetadata().getIndex());
            final String resourceId = attachmentIds.entrySet()
                    .stream()
                    .filter(e -> e.getKey().equals(annexVO.getId()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .get();
            final ExportResource annexExportResource = buildExportResourceAnnex(docNumber, resourceId, annexXmlContent);
            billExportResource.addChildResource(annexExportResource);
        });
        proposalExportResource.addChildResource(billExportResource);
        LegPackage legPackage = new LegPackage();
        legPackage.setFile(legFileTemp);
        legPackage.setExportResource(proposalExportResource);
        return legPackage;
    }
    
    /**
     * Creates the LegPackage, which is the logical representation of the leg file, for the given proposalId.
     *
     * @param proposalId       proposalId for which we need to create the LegPackage
     * @param exportOptions
     * @return LegPackage used to be sent to Toolbox for PDF/LegisWrite generation.
     */
    @Override
    public LegPackage createLegPackage(String proposalId, ExportOptions exportOptions) throws IOException {
        LOG.trace("Creating Leg Package... [documentId={}]", proposalId);
        final LegPackage legPackage = new LegPackage();
        final LeosPackage leosPackage = packageRepository.findPackageByDocumentId(proposalId);
        final Map<String, Object> contentToZip = new HashMap<>();
        final ExportResource exportProposalResource = new ExportResource(LeosCategory.PROPOSAL);
        exportProposalResource.setExportOptions(exportOptions);
        
        //1. Add Proposal to package
        final Proposal proposal = workspaceRepository.findDocumentById(proposalId, Proposal.class, true);
        final Map<String, String> proposalRefsMap = enrichZipWithProposal(contentToZip, exportProposalResource, proposal);

        //2. Depending on ExportOptions FileType add documents to package
        final Bill bill;
        ExportResource exportBillResource;
        if(exportOptions.isComparisonMode() || exportOptions.isCleanVersion()) {
            if (Bill.class.equals(exportOptions.getFileType())) {
                bill = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(),
                        proposalRefsMap.get(LeosCategory.BILL.name() + "_href"), Bill.class);
                byte[] xmlContent;
                if(exportOptions.isComparisonMode()){
                    xmlContent = getComparedContent(exportOptions);
                } else if(exportOptions.isCleanVersion()){
                    xmlContent = xmlContentProcessor.cleanSoftActions(bill.getContent().get().getSource().getBytes());
                } else {
                    xmlContent = addMetadataToBill(bill);
                }
                if (exportOptions.isWithRelevantElements()) {
                    structureContextProvider.get().useDocumentTemplate(bill.getMetadata().get().getDocTemplate());
                    xmlContent = addRelevantElements(exportOptions, bill.getVersionLabel(), xmlContent);
                    xmlContent = addCommentsMetadata(exportOptions.getComments(), xmlContent);
                }
                enrichZipWithBill(contentToZip, exportProposalResource, proposalRefsMap, bill, proposal.getMetadata().getOrNull().getRef(), xmlContent);
            } else if (Annex.class.equals(exportOptions.getFileType())) {
                bill = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(),
                        proposalRefsMap.get(LeosCategory.BILL.name() + "_href"), Bill.class);
                byte[] billXmlContent = bill.getContent().get().getSource().getBytes();
                exportBillResource = buildExportResourceBill(proposalRefsMap, billXmlContent);
                exportBillResource.setExportOptions(exportOptions);
                exportProposalResource.addChildResource(exportBillResource);
                addAnnexToPackage(leosPackage, contentToZip, exportOptions, exportBillResource, legPackage, proposal.getMetadata().getOrNull().getRef(), billXmlContent);
            } else {
                throw new IllegalStateException("Not implemented for type: " + exportOptions.getFileType());
            }
        } else {
            bill = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(),
                    proposalRefsMap.get(LeosCategory.BILL.name() + "_href"), Bill.class);
            byte[] billXmlContent;
            if(exportOptions.isComparisonMode()){
                billXmlContent = getComparedContent(exportOptions);
            } else {
                billXmlContent = addMetadataToBill(bill);
            }
            exportBillResource = enrichZipWithBill(contentToZip, exportProposalResource, proposalRefsMap, bill, proposal.getMetadata().getOrNull().getRef(), billXmlContent);
            addMemorandumToPackage(leosPackage, contentToZip, exportProposalResource, proposalRefsMap, legPackage, proposal.getMetadata().getOrNull().getRef());
            addAnnexToPackage(leosPackage, contentToZip, exportOptions, exportBillResource, legPackage, proposal.getMetadata().getOrNull().getRef(), billXmlContent);
            enrichZipWithToc(contentToZip);
            enrichZipWithMedia(contentToZip, leosPackage);
        }
        
        legPackage.setFile(ZipPackageUtil.zipFiles(proposalRefsMap.get(XmlNodeConfigHelper.PROPOSAL_DOC_COLLECTION) + ".leg", contentToZip));
        legPackage.addContainedFile(bill.getVersionedReference());
        legPackage.setExportResource(exportProposalResource);
        return legPackage;
    }

    private void addMemorandumToPackage(final LeosPackage leosPackage, final Map<String, Object> contentToZip, ExportResource exportProposalResource,
                                        final Map<String, String> proposalRefsMap, LegPackage legPackage, String proposalRef) {
        final Memorandum memorandum = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), proposalRefsMap.get(LeosCategory.MEMORANDUM.name() + "_href"), Memorandum.class);
        enrichZipWithMemorandum(contentToZip, exportProposalResource, proposalRefsMap, memorandum, proposalRef);
        legPackage.addContainedFile(memorandum.getVersionedReference());
    }
    
    /**
     * Used to add a Single annex to package, or all annexes if @param annexId is null
     */
    private void addAnnexToPackage(final LeosPackage leosPackage, final Map<String, Object> contentToZip,
                                   ExportOptions exportOptions, ExportResource exportProposalResource, LegPackage legPackage,
                                   String proposalRef, byte[] xmlContent) {
        // if we are in comparison mode, we don't need to fetch the document from CMIS, is already present in exportVersions
        final String annexId = exportOptions.isComparisonMode() ?
                exportOptions.getExportVersions().getCurrent().getMetadata().get().getRef()
                : null;
        
        final Map<String, String> attachmentIds = attachmentProcessor.getAttachmentsIdFromBill(xmlContent);
        if (!attachmentIds.isEmpty()){
            for (Map.Entry<String, String> entry : attachmentIds.entrySet()) {
                String href = entry.getKey();
                String id = entry.getValue();
                Annex annex;
                if (annexId == null) {
                    annex = packageRepository.findDocumentByPackagePathAndName(leosPackage.getPath(), href, Annex.class);
                } else if(href.contains(annexId)){
                    annex = (Annex) exportOptions.getExportVersions().getCurrent();
                } else {
                    continue;
                }
    
                enrichZipWithAnnex(contentToZip, exportProposalResource, annex, exportOptions, id, href, proposalRef);
                legPackage.addContainedFile(annex.getVersionedReference());
            }
            
            if (annexId != null) { // only if we are not in comparison mode
                addResourceToZipContent(contentToZip, annexStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            }
        }
    }
    
    private Map<String, String> enrichZipWithProposal(final Map<String, Object> contentToZip, ExportResource exportProposalResource, Proposal proposal) {
        byte[] xmlContent = addMetadataToProposal(proposal);
        contentToZip.put("main.xml", xmlContent);
        return buildProposalExportResource(exportProposalResource, xmlContent);
    }
    
    private void enrichZipWithToc(final Map<String, Object> contentToZip) {
        addResourceToZipContent(contentToZip, "jquery" + JS_EXT, JQUERY_SOURCE_PATH, JS_DEST_DIR);
        addResourceToZipContent(contentToZip, "jqtree" + JS_EXT, JQTREE_SOURCE_PATH, JS_DEST_DIR);
        addResourceToZipContent(contentToZip, "jqtree" + STYLE_SHEET_EXT, JQTREE_SOURCE_PATH + "css/", STYLE_DEST_DIR);
        addResourceToZipContent(contentToZip, "leos-toc-rendition" + JS_EXT, JS_SOURCE_PATH + "rendition/", JS_DEST_DIR);
        addResourceToZipContent(contentToZip, "leos-toc-rendition" + STYLE_SHEET_EXT, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
    }
    
    private void enrichZipWithMemorandum(final Map<String, Object> contentToZip, ExportResource exportProposalResource, Map<String, String> proposalRefsMap, Memorandum memorandum, String proposalRef) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();
        
        byte[] xmlContent = addMetadataToMemorandum(memorandum);
        contentToZip.put(memorandum.getName(), xmlContent);
        
        addAnnotateToZipContent(contentToZip, memorandum.getMetadata().get().getRef(), memorandum.getName(), exportOptions, proposalRef);
        addFilteredAnnotationsToZipContent(contentToZip, memorandum.getName(), exportOptions);

        addResourceToZipContent(contentToZip, memoStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
        structureContextProvider.get().useDocumentTemplate(memorandum.getMetadata().get().getDocTemplate());
        final String memoTocJson = getTocAsJson(memorandumService.getTableOfContent(memorandum, TocMode.SIMPLIFIED_CLEAN));
        addHtmlRendition(contentToZip, memorandum, memoStyleSheet, memoTocJson);
        
        final ExportResource memorandumExportResource = buildExportResourceMemorandum(proposalRefsMap, xmlContent);
        exportProposalResource.addChildResource(memorandumExportResource);
    }
    
    private ExportResource enrichZipWithBill(final Map<String, Object> contentToZip, ExportResource exportProposalResource, Map<String, String> proposalRefsMap,
                                             Bill bill, String proposalRef, byte[] xmlContent) {
        ExportOptions exportOptions = exportProposalResource.getExportOptions();
        contentToZip.put(bill.getName(), xmlContent);

        addAnnotateToZipContent(contentToZip, bill.getMetadata().get().getRef(), bill.getName(), exportOptions, proposalRef);
        addFilteredAnnotationsToZipContent(contentToZip, bill.getName(), exportOptions);

        if(!exportOptions.isComparisonMode()) {
            addResourceToZipContent(contentToZip, billStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            structureContextProvider.get().useDocumentTemplate(bill.getMetadata().get().getDocTemplate());
            final String billTocJson = getTocAsJson(billService.getTableOfContent(bill, TocMode.SIMPLIFIED_CLEAN));
            addHtmlRendition(contentToZip, bill, billStyleSheet, billTocJson);
        }
        
        final ExportResource exportBillResource = buildExportResourceBill(proposalRefsMap, xmlContent);
        exportBillResource.setExportOptions(exportOptions);
        exportProposalResource.addChildResource(exportBillResource);
        return exportBillResource;
    }

    private byte[] getComparedContent(ExportOptions exportOptions) {
        ExportVersions exportVersions = exportOptions.getExportVersions();
        String resultContent;
        switch (exportOptions.getComparisonType()) {
            case DOUBLE:
                resultContent = doubleCompareXmlContents(exportVersions.getOriginal(), exportVersions.getIntermediate(),
                        exportVersions.getCurrent(), exportOptions.isDocuwrite());
                break;
            case SIMPLE:
                resultContent = simpleCompareXmlContents(exportVersions.getOriginal(), exportVersions.getCurrent(), exportOptions.isDocuwrite());
                break;
            default:
                throw new IllegalStateException("Shouldn't happen!!! ExportVersions: " + exportVersions);
        }
        return resultContent.getBytes(UTF_8);
    }

    public String doubleCompareXmlContents(XmlDocument originalVersion, XmlDocument intermediateMajor, XmlDocument current, boolean isDocuwrite) {
        byte[] currentXmlContent = current.getContent().get().getSource().getBytes();
        String originalXml = originalVersion.getContent().getOrError(() -> "Original document content is required!")
                .getSource().toString();
        String intermediateMajorXml = intermediateMajor.getContent().getOrError(() -> "Intermadiate Major Version document content is required!")
                .getSource().toString();
        String currentXml = new String(currentXmlContent, UTF_8);

        return compareService.compareContents(new ContentComparatorContext.Builder(originalXml, currentXml, intermediateMajorXml)
                .withAttrName(ATTR_NAME)
                .withRemovedValue(DOUBLE_COMPARE_REMOVED_CLASS)
                .withAddedValue(DOUBLE_COMPARE_ADDED_CLASS)
                .withRemovedIntermediateValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                .withAddedIntermediateValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE)
                .withRemovedOriginalValue(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                .withAddedOriginalValue(DOUBLE_COMPARE_ADDED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                .withRetainOriginalValue(DOUBLE_COMPARE_RETAIN_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .withThreeWayDiff(true)
                .withDocuwriteExport(isDocuwrite)
                .build());
    }

    
    public String simpleCompareXmlContents(XmlDocument versionToCompare, XmlDocument currentXmlContent, boolean isDocuwrite) {
        String versionToCompareXml = versionToCompare.getContent().get().getSource().toString();
        String currentXmlContentXml = currentXmlContent.getContent().get().getSource().toString();

        return compareService.compareContents(new ContentComparatorContext.Builder(versionToCompareXml, currentXmlContentXml)
                .withAttrName(ATTR_NAME)
                .withRemovedValue(DOUBLE_COMPARE_REMOVED_CLASS)
                .withAddedValue(DOUBLE_COMPARE_ADDED_CLASS)
                .withDisplayRemovedContentAsReadOnly(Boolean.TRUE)
                .withThreeWayDiff(false)
                .withDocuwriteExport(isDocuwrite)
                .build());
    }
    
    private void enrichZipWithAnnex(final Map<String, Object> contentToZip, ExportResource exportBillResource,
                                    Annex annex, ExportOptions exportOptions, String resourceId, String href,
                                    String proposalRef) {
        byte[] xmlContent;
        if(exportOptions.isComparisonMode()){
            xmlContent = getComparedContent(exportOptions);
        } else if(exportOptions.isCleanVersion()){
            xmlContent = xmlContentProcessor.cleanSoftActions(annex.getContent().get().getSource().getBytes());
        } else {
            xmlContent = addMetadataToAnnex(annex);
        }
        if (exportOptions.isWithRelevantElements()) {
            structureContextProvider.get().useDocumentTemplate(annex.getMetadata().get().getDocTemplate());
            xmlContent = addRelevantElements(exportOptions, annex.getVersionLabel(), xmlContent);
            xmlContent = addCommentsMetadata(exportOptions.getComments(), xmlContent);
        }
        contentToZip.put(annex.getName(), xmlContent);
        
        addAnnotateToZipContent(contentToZip, annex.getMetadata().get().getRef(), annex.getName(), exportOptions, proposalRef);
        addFilteredAnnotationsToZipContent(contentToZip, annex.getName(), exportOptions);

        if (!exportOptions.isComparisonMode()) {
            addResourceToZipContent(contentToZip, annexStyleSheet, STYLES_SOURCE_PATH, STYLE_DEST_DIR);
            structureContextProvider.get().useDocumentTemplate(annex.getMetadata().get().getDocTemplate());
            final String annexTocJson = getTocAsJson(annexService.getTableOfContent(annex, TocMode.SIMPLIFIED_CLEAN));
            addHtmlRendition(contentToZip, annex, annexStyleSheet, annexTocJson);
        }
        
        int docNumber = annex.getMetadata().get().getIndex();
        final ExportResource annexExportResource = buildExportResourceAnnex(docNumber, resourceId, href, xmlContent);
        exportBillResource.addChildResource(annexExportResource);
    }
    
    private List<TableOfContentItemHtmlVO> buildTocHtml(List<TableOfContentItemVO> tableOfContents) {
        List<TableOfContentItemHtmlVO> tocHtml = new ArrayList<>();
        for (TableOfContentItemVO item : tableOfContents) {
            String name = TableOfContentHelper.buildItemCaption(item, TableOfContentHelper.DEFAULT_CAPTION_MAX_SIZE, messageHelper);
            TableOfContentItemHtmlVO itemHtml = new TableOfContentItemHtmlVO(name, "#" + item.getId());
            if (item.getChildItems().size() > 0) {
                itemHtml.setChildren(buildTocHtml(item.getChildItems()));
            }
            tocHtml.add(itemHtml);
        }
        return tocHtml;
    }
    
    private String getTocAsJson(List<TableOfContentItemVO> tableOfContent) {
        final String json;
        try {
            List<TableOfContentItemHtmlVO> tocHtml = buildTocHtml(tableOfContent);
            json = new ObjectMapper().writeValueAsString(tocHtml);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Exception while converting 'tableOfContent' in json format.", e);
        }
        return json;
    }
    
    private Map<String, String> buildProposalExportResource(ExportResource exportResource, byte[] xmlContent) {
        Map<String, XmlNodeConfig> config = new HashMap<>();
        config.putAll(xmlNodeConfigHelper.getProposalComponentsConfig(LeosCategory.MEMORANDUM, "xml:id"));
        config.putAll(xmlNodeConfigHelper.getProposalComponentsConfig(LeosCategory.MEMORANDUM, "href"));
        config.putAll(xmlNodeConfigHelper.getProposalComponentsConfig(LeosCategory.BILL, "xml:id"));
        config.putAll(xmlNodeConfigHelper.getProposalComponentsConfig(LeosCategory.BILL, "href"));
        config.putAll(xmlNodeConfigHelper.getConfig(LeosCategory.PROPOSAL));
        Map<String, String> proposalRefsMap = xmlNodeProcessor.getValuesFromXml(xmlContent,
                new String[]{XmlNodeConfigHelper.PROPOSAL_DOC_COLLECTION, XmlNodeConfigHelper.DOC_REF_COVER,
                        LeosCategory.MEMORANDUM.name() + "_xml:id",
                        LeosCategory.MEMORANDUM.name() + "_href",
                        LeosCategory.BILL.name() + "_xml:id",
                        LeosCategory.BILL.name() + "_href"
                },
                config);
        
        Map<String, String> proposalComponentRefs = new HashMap<>();
        proposalComponentRefs.put(XmlNodeConfigHelper.DOC_REF_COVER, proposalRefsMap.get(XmlNodeConfigHelper.DOC_REF_COVER));
        
        exportResource.setResourceId(proposalRefsMap.get(XmlNodeConfigHelper.PROPOSAL_DOC_COLLECTION));
        exportResource.setComponentsIdsMap(proposalComponentRefs);
        return proposalRefsMap;
    }
    
    private ExportResource buildExportResourceMemorandum(Map<String, String> proposalRefsMap, byte[] xmlContent) {
        ExportResource memorandumExportResource = new ExportResource(LeosCategory.MEMORANDUM);
        memorandumExportResource.setResourceId(proposalRefsMap.get(LeosCategory.MEMORANDUM.name() + "_xml:id"));
        setComponentsRefs(LeosCategory.MEMORANDUM, memorandumExportResource, xmlContent);
        return memorandumExportResource;
    }
    
    private ExportResource buildExportResourceBill(Map<String, String> proposalRefsMap, byte[] xmlContent) {
        ExportResource billExportResource = new ExportResource(LeosCategory.BILL);
        billExportResource.setResourceId(proposalRefsMap.get(LeosCategory.BILL.name() + "_xml:id"));
        setComponentsRefs(LeosCategory.BILL, billExportResource, xmlContent);
        return billExportResource;
    }
    
    private ExportResource buildExportResourceAnnex(int docNumber, String resourceId, byte[] xmlContent) {
        //TODO : FIXME : populate href for Proposal export
        return buildExportResourceAnnex(docNumber, resourceId, null, xmlContent);
    }
    
    private ExportResource buildExportResourceAnnex(int docNumber, String resourceId, String href, byte[] xmlContent) {
        ExportResource annexExportResource = new ExportResource(LeosCategory.ANNEX);
        annexExportResource.setResourceId(resourceId);
        annexExportResource.setHref(href);
        annexExportResource.setDocNumber(docNumber);
        setComponentsRefs(LeosCategory.ANNEX, annexExportResource, xmlContent);
        return annexExportResource;
    }
    
    private void enrichZipWithMedia(final Map<String, Object> contentToZip, LeosPackage leosPackage) {
        final List<MediaDocument> mediaDocs = packageRepository.findDocumentsByPackagePath(leosPackage.getPath(), MediaDocument.class, true);
        for (MediaDocument mediaDoc : mediaDocs) {
            byte[] byteContent = mediaDoc.getContent().getOrError(() -> "Document content is required!").getSource().getBytes();
            contentToZip.put(MEDIA_DIR + mediaDoc.getName(), byteContent);
        }
    }
    
    @Override
    public LegDocument createLegDocument(String proposalId, String jobId, LegPackage legPackage, LeosLegStatus status) throws IOException {
        LOG.trace("Creating Leg Document for Package... [documentId={}]", proposalId);
        return packageRepository.createLegDocumentFromContent(packageRepository.findPackageByDocumentId(proposalId).getPath(), generateLegName(),
                jobId, legPackage.getMilestoneComments(), getFileContent(legPackage.getFile()), status, legPackage.getContainedFiles());
    }
    
    @Override
    public LegDocument updateLegDocument(String id, LeosLegStatus status) {
        LOG.trace("Updating Leg document status... [id={}, status={}]", id, status.name());
        return packageRepository.updateLegDocument(id, status);
    }
    
    @Override
    public LegDocument updateLegDocument(String id, byte[] pdfJobZip, byte[] wordJobZip) {
        LOG.trace("Updating Leg document with id={} status to {} and content with pdf and word renditions", id, LeosLegStatus.FILE_READY.name());
        LegDocument document = findLegDocumentById(id);
        try {
            byte[] content = updateContentWithPdfAndWordRenditions(pdfJobZip, wordJobZip, document.getContent().getOrNull());
            return packageRepository.updateLegDocument(document.getId(), LeosLegStatus.FILE_READY, content, VersionType.INTERMEDIATE, "Milestone is now validated");
        } catch (Exception e) {
            LOG.error("Error while updating the content of the Leg Document with id=" + id, e);
            return packageRepository.updateLegDocument(document.getId(), LeosLegStatus.FILE_ERROR);
        }
    }
    
    @Override
    public LegDocument findLegDocumentById(String id) {
        LOG.trace("Finding Leg Document by id... [documentId={}]", id);
        return packageRepository.findLegDocumentById(id, true);
    }
    
    @Override
    public LegDocument findLegDocumentByAnyDocumentIdAndJobId(String documentId, String jobId) {
        LOG.trace("Finding Leg Document by proposal id and job id... [proposalId={}, jobId={}]", documentId, jobId);
        LeosPackage leosPackage = packageRepository.findPackageByDocumentId(documentId);
        List<LegDocument> legDocuments = packageRepository.findDocumentsByPackageId(leosPackage.getId(), LegDocument.class, false, false);
        return legDocuments.stream()
                .filter(legDocument -> jobId.equals(legDocument.getJobId()))
                .findAny()
                .orElse(null);
    }
    
    @Override
    public List<LegDocument> findLegDocumentByStatus(LeosLegStatus leosLegStatus) {
        return packageRepository.findDocumentsByStatus(leosLegStatus, LegDocument.class);
    }
    
    @Override
    public List<LegDocument> findLegDocumentByProposal(String proposalId) {
        LeosPackage leosPackage = packageRepository.findPackageByDocumentId(proposalId);
        return packageRepository.findDocumentsByPackageId(leosPackage.getId(), LegDocument.class, false, false);
    }
    
    private byte[] updateContentWithPdfAndWordRenditions(byte[] pdfJobZip, byte[] wordJobZip, Content content) throws IOException {
        Map<String, Object> legContent = ZipPackageUtil.unzipByteArray(content.getSource().getBytes());
        addPdfRendition(pdfJobZip, legContent);
        addWordRenditions(wordJobZip, legContent);
        return ZipPackageUtil.zipByteArray(legContent);
    }
    
    private void addPdfRendition(byte[] pdfJobZip, Map<String, Object> legContent) throws IOException {
        Map.Entry<String, Object> neededEntry = unzipJobResult(pdfJobZip).entrySet().stream()
                .filter(pdfEntry -> !pdfEntry.getKey().endsWith("_pdfa.pdf"))
                .findAny()
                .orElseThrow(() -> new FileNotFoundException("Pdf rendition not found in the pdf document job file"));
        legContent.put(PDF_RENDITION + neededEntry.getKey(), neededEntry.getValue());
    }
    
    private void addWordRenditions(byte[] wordJobZip, Map<String, Object> legContent) throws IOException {
        List<String> wordEntries = new ArrayList<>();
        unzipJobResult(wordJobZip).entrySet().stream()
                .filter(wordEntity -> !wordEntity.getKey().isEmpty())
                .forEach(wordEntry -> {
                    legContent.put(WORD_RENDITION + wordEntry.getKey(), wordEntry.getValue());
                    wordEntries.add(wordEntry.getKey());
                });
        if (wordEntries.isEmpty()) {
            throw new FileNotFoundException("No word rendition found in the word document job file");
        }
    }
    
    private Map<String, Object> unzipJobResult(byte[] jobZip) throws IOException {
        Map<String, Object> jobContent = ZipPackageUtil.unzipByteArray(jobZip);
        for (Map.Entry<String, Object> entry : jobContent.entrySet()) {
            if (entry.getKey().endsWith("_out.zip")) {
                return ZipPackageUtil.unzipByteArray((byte[]) entry.getValue());
            }
        }
        throw new FileNotFoundException("The job result zip file is not present in the job file");
    }
    
    private byte[] getFileContent(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            byte[] content = new byte[(int) file.length()];
            is.read(content);
            return content;
        }
    }
    
    private void addHtmlRendition(Map<String, Object> contentToZip, XmlDocument xmlDocument, String styleSheetName, String tocJson) {
        try {
            RenderedDocument htmlDocument = new RenderedDocument();
            htmlDocument.setContent(new ByteArrayInputStream(getContent(xmlDocument)));
            htmlDocument.setStyleSheetName(styleSheetName);
            String htmlName = HTML_RENDITION + xmlDocument.getName().replaceAll(".xml", ".html");
            contentToZip.put(htmlName, htmlRenditionProcessor.processTemplate(htmlDocument).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UnsupportedEncodingException while processing document " + xmlDocument.getName(), exception);
        }
        
        try {
            // Build toc_docName.js file
            RenderedDocument tocHtmlDocument = new RenderedDocument();
            tocHtmlDocument.setContent(new ByteArrayInputStream(getContent(xmlDocument)));
            tocHtmlDocument.setStyleSheetName(styleSheetName);
            final String tocJsName = xmlDocument.getName().substring(0, xmlDocument.getName().indexOf(".xml")) + "_toc" + ".js";
            final String tocJsFile = JS_DEST_DIR + tocJsName;
            contentToZip.put(tocJsFile, htmlRenditionProcessor.processJsTemplate(tocJson).getBytes("UTF-8"));
            
            //build html_docName_toc.html
            tocHtmlDocument = new RenderedDocument();
            tocHtmlDocument.setContent(new ByteArrayInputStream(getContent(xmlDocument)));
            tocHtmlDocument.setStyleSheetName(styleSheetName);
            String tocHtmlFile = HTML_RENDITION + xmlDocument.getName();
            tocHtmlFile = tocHtmlFile.substring(0, tocHtmlFile.indexOf(".xml")) + "_toc" + ".html";
            contentToZip.put(tocHtmlFile, htmlRenditionProcessor.processTocTemplate(tocHtmlDocument, tocJsName).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UnsupportedEncodingException while processing document " + xmlDocument.getName(), exception);
        }
    }
    
    /**
     * Add resource to exported .leg file in renditions/html/css or js folder
     */
    private void addResourceToZipContent(Map<String, Object> contentToZip, String resourceName, String sourcePath, String destPath) {
        try {
            Resource resource = new ClassPathResource(sourcePath + resourceName);
            contentToZip.put(destPath + resourceName, IOUtils.toByteArray(resource.getInputStream()));
        } catch (IOException io) {
            LOG.error("Error occurred while getting styles ", io);
        }
    }
    
    /**
     * Calls service to get Annotations per document
     */
    private void addAnnotateToZipContent(Map<String, Object> contentToZip, String ref, String docName, ExportOptions exportOptions, String proposalRef) {
        if (exportOptions.isWithAnnotations()) {
            String annotations = annotateService.getAnnotations(ref, proposalRef);
            final byte[] xmlAnnotationContent = annotations.getBytes(UTF_8);
            contentToZip.put(creatAnnotationFileName(docName), xmlAnnotationContent);
        }
    }

    public void addFilteredAnnotationsToZipContent(Map<String, Object> contentToZip, String docName, ExportOptions exportOptions) {
        if (exportOptions.isWithFilteredAnnotations()) {
            String annotations = exportOptions.getFilteredAnnotations();
            final byte[] xmlAnnotationContent = annotations.getBytes(UTF_8);
            contentToZip.put(creatAnnotationFileName(docName), xmlAnnotationContent);
        }
    }

    private String creatAnnotationFileName(String docName) {
        return MEDIA_DIR + ANNOT_FILE_PREFIX + docName + ANNOT_FILE_EXT;
    }
    
    private void setComponentsRefs(LeosCategory leosCategory, final ExportResource exportResource, byte[] xmlContent) {
        Map<String, String> componentMap = xmlNodeProcessor.getValuesFromXml(xmlContent,
                new String[]{XmlNodeConfigHelper.DOC_REF_COVER},
                xmlNodeConfigHelper.getConfig(leosCategory));
        exportResource.setComponentsIdsMap(componentMap);
    }
    
    private byte[] getContent(XmlDocument xmlDocument) {
        final Content content = xmlDocument.getContent().getOrError(() -> "xml content is required!");
        return content.getSource().getBytes();
    }

    private byte[] addRelevantElements(ExportOptions exportOptions, String currentVersion, byte[] xmlContent) {
        final List<String> rootElements = structureContextProvider.get().getTocItems().stream().filter(x -> x.isRoot() && x.getProfiles() == null).map(x -> x.getAknTag().value()).collect(Collectors.toList());
        final List<Element> relevantXmlElements = getRelevantElementsFromXml(exportOptions.getRelevantElements(), rootElements, xmlContent);
        xmlContent = addRelevantElementsMetadata(exportOptions, relevantXmlElements, currentVersion, xmlContent);
        xmlContent = xmlContentProcessor.ignoreNotSelectedElements(xmlContent, rootElements, relevantXmlElements.stream().map(x -> x.getElementId()).collect(Collectors.toList()));
        return xmlContent;
    }

    private List<Element> getRelevantElementsFromXml(RelevantElements relevantElements, List<String> rootElements, byte[] xmlContent) {
        List<Element> relevantXmlElements;
        switch (relevantElements) {
            case RECITALS:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, Arrays.asList(XmlHelper.RECITALS), false);
                break;
            case ENACTING_TERMS:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, Arrays.asList(XmlHelper.BODY), false);
                break;
            case RECITALS_AND_ENACTING_TERMS:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, Arrays.asList(XmlHelper.RECITALS, XmlHelper.BODY), false);
                break;
            case ALL:
                relevantXmlElements = xmlContentProcessor.getElementsByTagName(xmlContent, rootElements, false);
                break;
            default:
                throw new IllegalArgumentException("No supported element " + relevantElements);
        }
        return relevantXmlElements;
    }

    private byte[] addRelevantElementsMetadata(ExportOptions exportOptions, List<Element> relevantXmlElements, String currentVersion, byte[] xmlContent) {
        StringBuilder relevantElementsBuilder = new StringBuilder("<leos:relevantElements");
        if (exportOptions.isComparisonMode()) {
            relevantElementsBuilder.append((exportOptions.getExportVersions().getOriginal() != null) ? " leos:originalVersion=\"" + exportOptions.getExportVersions().getOriginal().getVersionLabel() + "\"" : "");
            relevantElementsBuilder.append((exportOptions.getExportVersions().getIntermediate() != null) ? " leos:intermediateVersion=\"" + exportOptions.getExportVersions().getIntermediate().getVersionLabel() + "\"" : "");
            relevantElementsBuilder.append((exportOptions.getExportVersions().getCurrent() != null) ? " leos:currentVersion=\"" + exportOptions.getExportVersions().getCurrent().getVersionLabel() + "\"" : "");
        } else {
            relevantElementsBuilder.append(" leos:currentVersion=\"" + currentVersion + "\"");
        }
        relevantElementsBuilder.append(">");
        relevantXmlElements.forEach(element -> {
            relevantElementsBuilder.append("<leos:relevantElement leos:ref=\"" + element.getElementId() + "\" leos:tagName=\"" + element.getElementTagName() + "\"/>");
        });
        relevantElementsBuilder.append("</leos:relevantElements>");
        return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathDocTemplate(), true, relevantElementsBuilder.toString());
    }

    @Override
    public byte[] updateLegPackageContentWithComments(byte[] legPackageContent, List<String> comments) throws IOException {
        File legPackageZipFile = null;
        try {
            Map<String, Object> legPackageZipContent = ZipPackageUtil.unzipByteArray(legPackageContent);
            Map.Entry<String, Object> legPackageXmlDocument = legPackageZipContent.entrySet().stream()
                    .filter(x -> x.getKey().startsWith(LeosCategory.BILL.name().toLowerCase()) || x.getKey().startsWith(LeosCategory.ANNEX.name().toLowerCase()))
                    .findAny().orElseThrow(() -> new RuntimeException("No document file inside leg package!"));
            byte[] xmlContentUpdated = replaceCommentsMetadata(comments, (byte[])legPackageXmlDocument.getValue());
            legPackageZipContent.put(legPackageXmlDocument.getKey(), xmlContentUpdated);
            legPackageZipFile = ZipPackageUtil.zipFiles(System.currentTimeMillis() + ".zip", legPackageZipContent);
            return FileUtils.readFileToByteArray(legPackageZipFile);
        } finally {
            if ((legPackageZipFile != null) && (legPackageZipFile.exists())) {
                legPackageZipFile.delete();
            }
        }
    }

    private byte[] addCommentsMetadata(List<String> comments, byte[] xmlContent) {
        StringBuilder commentsBuilder = new StringBuilder("<leos:comments>");
        comments.forEach(comment -> commentsBuilder.append("<leos:comment><![CDATA[" + comment + "]]></leos:comment>"));
        commentsBuilder.append("</leos:comments>");
        return xmlContentProcessor.insertElement(xmlContent, xPathCatalog.getXPathRelevantElements(), true, commentsBuilder.toString());
    }

    private byte[] replaceCommentsMetadata(List<String> comments, byte[] xmlContent) {
        StringBuilder commentsBuilder = new StringBuilder("<leos:comments>");
        comments.forEach(comment -> commentsBuilder.append("<leos:comment><![CDATA[" + comment + "]]></leos:comment>"));
        commentsBuilder.append("</leos:comments>");
        return xmlContentProcessor.replaceElement(xmlContent, xPathCatalog.getXPathComments(), true, commentsBuilder.toString());
    }

}
