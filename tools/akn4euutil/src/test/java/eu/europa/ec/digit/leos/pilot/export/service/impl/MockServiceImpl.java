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

import eu.europa.ec.digit.leos.pilot.export.exception.MockServiceException;
import eu.europa.ec.digit.leos.pilot.export.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.HTML_OUTPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.LEG_INPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.LW_OUTPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.PDF_OUTPUT_TYPE;
import static eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil.XML_INPUT_TYPE;

@Service
public class MockServiceImpl implements MockService {
    private static final Map<String, String> MOCK_OUTPUT_MAPPING = new HashMap<>();
    private static final Map<String, String> MOCK_INPUT_MAPPING = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(MockServiceImpl.class);

    public MockServiceImpl() {
        MOCK_OUTPUT_MAPPING.put(PDF_OUTPUT_TYPE, "samples/PdfSample.zip");
        MOCK_OUTPUT_MAPPING.put(LW_OUTPUT_TYPE, "samples/LwSample.zip");
        MOCK_OUTPUT_MAPPING.put(HTML_OUTPUT_TYPE, "samples/HtmlSample.zip");

        MOCK_INPUT_MAPPING.put(LEG_INPUT_TYPE, "samples/LegSample.leg");
        MOCK_INPUT_MAPPING.put(XML_INPUT_TYPE, "samples/BillSample.xml");
    }

    private String getMockOutputFilename(String output) {
        return MOCK_OUTPUT_MAPPING.get(output.toUpperCase());
    }

    public String getMockInputFilename(String input) {
        return MOCK_INPUT_MAPPING.get(input.toUpperCase());
    }

    public byte[] getMockData(String output) throws MockServiceException {
        String mockFilename = getMockOutputFilename(output);
        byte[] documentInput;
        try {
            InputStream resource = new ClassPathResource(mockFilename).getInputStream();
            documentInput = StreamUtils.copyToByteArray(resource);
        } catch (IOException e) {
            String errorMessage = String.format("Can't read resource: %s", mockFilename);
            LOG.error(errorMessage);
            throw new MockServiceException(errorMessage, e);
        }
        return documentInput;
    }
}
