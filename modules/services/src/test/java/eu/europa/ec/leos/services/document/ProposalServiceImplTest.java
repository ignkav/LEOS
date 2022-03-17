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
package eu.europa.ec.leos.services.document;


import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.domain.cmis.document.Bill;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.vo.CloneProposalMetadataVO;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.repository.LeosRepository;
import eu.europa.ec.leos.repository.document.BillRepository;
import eu.europa.ec.leos.repository.document.BillRepositoryImpl;
import eu.europa.ec.leos.repository.document.ProposalRepository;
import eu.europa.ec.leos.repository.store.PackageRepository;
import eu.europa.ec.leos.services.content.TemplateStructureService;
import eu.europa.ec.leos.services.content.processor.AttachmentProcessor;
import eu.europa.ec.leos.services.document.util.DocumentVOProvider;
import eu.europa.ec.leos.services.support.xml.NumberProcessor;
import eu.europa.ec.leos.services.support.xml.SearchEngine;
import eu.europa.ec.leos.services.support.xml.XPathCatalog;
import eu.europa.ec.leos.services.support.xml.XPathV1Catalog;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessorProposal;
import eu.europa.ec.leos.services.support.xml.XmlNodeConfigHelper;
import eu.europa.ec.leos.services.support.xml.XmlNodeProcessor;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.services.toc.StructureServiceImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.services.validation.ValidationService;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Option;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.europa.ec.leos.services.TestVOCreatorUtils.getJohnTestUser;
import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class ProposalServiceImplTest {

    private final static String CLONED_PROPOSAL_DOCUMENT = "/document/clonedProposals/";

    @Mock
    ProposalRepository proposalRepository;
    @Mock
    XmlNodeProcessor xmlNodeProcessor;
    @Mock
    XmlNodeConfigHelper xmlNodeConfigHelper;
    @Mock
    PackageRepository packageRepository;
    @InjectMocks
    private XPathCatalog xPathCatalog = spy(new XPathV1Catalog());

    private XmlContentProcessor xmlContentProcessor = new XmlContentProcessorProposal();
    private ProposalService proposalService;

    @Test
    public void test_getClonedProposalMetadata_should_return_clonedProposalMetadataVO() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //Expected
        String expectedOriginRef = "proposal_ckmezv9qh0002yy56plb1dxhj";
        String expectedLegFileName = "leg_cko1gslfb0002ck56pt3pt5b0.leg";
        String expectedISCRef = "EdiT";
        String expectedObjectId = "afeba3f64617b7ef967c7cb6211be01cb2985a3e";

        proposalService = new ProposalServiceImpl(proposalRepository, xmlNodeProcessor, xmlContentProcessor,
                xmlNodeConfigHelper, packageRepository,
                xPathCatalog);

        //DO the actual call
        CloneProposalMetadataVO cloneProposalMetadataVO = proposalService.getClonedProposalMetadata(xmlContent);

        //Assertions
        assertTrue(cloneProposalMetadataVO.isClonedProposal());
        assertEquals(expectedOriginRef, cloneProposalMetadataVO.getClonedFromRef());
        assertEquals(expectedLegFileName, cloneProposalMetadataVO.getLegFileName());
        assertEquals(expectedISCRef, cloneProposalMetadataVO.getOriginRef());
        assertEquals(expectedObjectId, cloneProposalMetadataVO.getClonedFromObjectId());
    }

}
