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
package eu.europa.ec.leos.services.support.xml;

import eu.europa.ec.leos.services.support.xml.XPathCatalog;
import eu.europa.ec.leos.services.support.xml.XPathV1Catalog;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessorProposal;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class XPathV1CatalogTest extends LeosTest {

    private final static String CLONED_PROPOSAL_DOCUMENT = "/document/clonedProposals/";
    private final static String ORIGINAL_PROPOSAL_DOCUMENT = "/document/originalProposals/";

    private XmlContentProcessor xmlContentProcessor = new XmlContentProcessorProposal();
    @InjectMocks
    private XPathCatalog xPathCatalog = spy(new XPathV1Catalog());

    @Test
    public void test_getXPathRefOrigin_should_return_refOrigin() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //Given
        String expectedOriginRef = "proposal_ckoij71xv0002cm5634gf3jdw";
        //When
        String refOrigin = xmlContentProcessor.getElementValue(xmlContent, xPathCatalog.getXPathRefOrigin(), true);
        //Then
        assertEquals(expectedOriginRef, refOrigin);
    }

    @Test
    public void test_getXPathRefOriginForCloneRefAttr_should_return_refOriginForClone() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //Given
        String expectedOriginRef = "proposal_ckmezv9qh0002yy56plb1dxhj";
        //When
        String clonedFromRef = xmlContentProcessor.getElementValue(xmlContent, xPathCatalog.getXPathRefOriginForCloneRefAttr(), true);
        //Then
        assertEquals(expectedOriginRef, clonedFromRef);
    }

    @Test
    public void test_getXPathRefOriginForCloneOriginalMilestone_should_return_orginMilestone() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //Given
        String expectedOriginMilestone = "leg_cko1gslfb0002ck56pt3pt5b0.leg";
        //When
        String originMilestone = xmlContentProcessor.getElementValue(xmlContent, xPathCatalog.getXPathRefOriginForCloneOriginalMilestone(), true);
        //Then
        assertEquals(expectedOriginMilestone, originMilestone);
    }

    @Test
    public void test_getXPathRefOriginForCloneIscRef_should_return_iscRef() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //Given
        String expectedIscRef = "EdiT";
        //When
        String iscRef = xmlContentProcessor.getElementValue(xmlContent, xPathCatalog.getXPathRefOriginForCloneIscRef(), true);
        //Then
        assertEquals(expectedIscRef, iscRef);
    }

    @Test
    public void test_getXPathRefOriginForCloneObjectId_should_return_objectId() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //Given
        String expectedObjectId = "afeba3f64617b7ef967c7cb6211be01cb2985a3e";
        //When
        String objectId = xmlContentProcessor.getElementValue(xmlContent, xPathCatalog.getXPathRefOriginForCloneObjectId(), true);
        //Then
        assertEquals(expectedObjectId, objectId);
    }

    @Test
    public void test_getXPathClonedProposal_should_return_true() {
        byte[] xmlContent = TestUtils.getFileContent(CLONED_PROPOSAL_DOCUMENT, "proposal_cloned.xml");
        //When
        boolean result = xmlContentProcessor.evalXPath(xmlContent, xPathCatalog.getXPathClonedProposal(), true);
        //Then
        assertTrue(result);
    }

    @Test
    public void test_getXPathClonedProposals_should_return_true() {
        byte[] xmlContent = TestUtils.getFileContent(ORIGINAL_PROPOSAL_DOCUMENT, "proposal_original.xml");
        //When
        boolean result = xmlContentProcessor.evalXPath(xmlContent, xPathCatalog.getXPathClonedProposals(), true);
        //Then
        assertTrue(result);
    }

    @Test
    public void test_getXPathCPMilestoneRefByNameAttr_should_return_true() {
        byte[] xmlContent = TestUtils.getFileContent(ORIGINAL_PROPOSAL_DOCUMENT, "proposal_original.xml");
        String legFileName = "leg_ckn979flq0017wn56ctye7scp.leg";
        //When
        boolean result = xmlContentProcessor.evalXPath(xmlContent, xPathCatalog.getXPathCPMilestoneRefByNameAttr(legFileName), true);
        //Then
        assertTrue(result);
    }

    @Test
    public void test_getXPathCPMilestoneRefClonedProposalRefByRefAttr_should_return_true() {
        byte[] xmlContent = TestUtils.getFileContent(ORIGINAL_PROPOSAL_DOCUMENT, "proposal_original.xml");
        String legFileName = "leg_ckn979flq0017wn56ctye7scp.leg";
        String cloneProposalId = "proposal_ckn979vhj001awn561vl9dzcs";
        //When
        boolean result = xmlContentProcessor.evalXPath(xmlContent,
                xPathCatalog.getXPathCPMilestoneRefClonedProposalRefByRefAttr(legFileName, cloneProposalId), true);
        //Then
        assertTrue(result);
    }
}