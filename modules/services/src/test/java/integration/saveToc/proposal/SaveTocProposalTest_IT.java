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
package integration.saveToc.proposal;

import eu.europa.ec.leos.services.content.ReferenceLabelService;
import eu.europa.ec.leos.services.numbering.ElementNumberingHelper;
import eu.europa.ec.leos.services.numbering.config.NumberingConfigProcessorFactory;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessor;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorArticle;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorArticleNoChildren;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorHandler;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorLevel;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorParagraph;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorPoint;
import eu.europa.ec.leos.services.numbering.processor.NumberProcessorRecitalNoChildren;
import eu.europa.ec.leos.services.support.xml.ProposalNumberingProcessor;
import eu.europa.ec.leos.services.support.xml.ReferenceLabelServiceImplForProposal;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelperImpl;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessor;
import eu.europa.ec.leos.services.support.xml.XmlContentProcessorProposal;
import eu.europa.ec.leos.services.support.xml.XmlTableOfContentHelper;
import integration.saveToc.SaveTocTest_IT;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

public abstract class SaveTocProposalTest_IT extends SaveTocTest_IT {

    @InjectMocks
    protected XmlContentProcessor xmlContentProcessor = Mockito.spy(new XmlContentProcessorProposal());
    @InjectMocks
    protected XmlTableOfContentHelper xmlTableOfContentHelper = Mockito.spy(new XmlTableOfContentHelperImpl());
    @InjectMocks
    private NumberingConfigProcessorFactory numberingConfigProcessorFactory = Mockito.spy(new NumberingConfigProcessorFactory());
    @InjectMocks
    private NumberProcessorHandler numberProcessorHandler = new NumberProcessorHandler();

    @InjectMocks
    private NumberProcessor processorArticle = new NumberProcessorArticle(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorParagraph = new NumberProcessorParagraph(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorPoint = new NumberProcessorPoint(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorArticleSingle = new NumberProcessorArticleNoChildren(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorRecitalSingle = new NumberProcessorRecitalNoChildren(messageHelper, numberProcessorHandler);
    @InjectMocks
    private NumberProcessor processorLevel = new NumberProcessorLevel(messageHelper, numberProcessorHandler);
    @InjectMocks
    protected ReferenceLabelService referenceLabelService = Mockito.spy(new ReferenceLabelServiceImplForProposal());

    protected eu.europa.ec.leos.services.support.xml.NumberProcessor numberingProcessor;

    private ElementNumberingHelper elementNumberingHelper;

    @Before
    public void onSetUp() throws Exception {
        super.onSetUp();
        elementNumberingHelper = new ElementNumberingHelper(numberProcessorHandler, structureContextProvider);
        numberingProcessor = new ProposalNumberingProcessor(elementNumberingHelper, getMessageHelper());

        ReflectionTestUtils.setField(numberProcessorHandler, "numberProcessors",
                Arrays.asList(processorArticle, processorParagraph, processorPoint, processorArticleSingle, processorRecitalSingle, processorLevel));
    }
}