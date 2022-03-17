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
package eu.europa.ec.digit.leos.pilot.export;

import eu.europa.ec.digit.leos.pilot.export.service.MockService;
import eu.europa.ec.digit.leos.pilot.export.util.ConstantsTestsUtil;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LeosExportApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MockService mockService;

    private MockMultipartFile convertToMockMultipartFile(String mockFileName) throws Exception {
        File resource = new ClassPathResource(mockFileName).getFile();
        return new MockMultipartFile("inputFile", resource.getName(), null, Files.readAllBytes(resource.toPath()));
    }

    private ResultActions createGetRenditionsRequest(Boolean isWithAnnotations) throws Exception {
        MockMultipartFile mockMultipartFile = convertToMockMultipartFile(mockService.getMockInputFilename("xml"));
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart("/getRenditions")
                .file(mockMultipartFile);
        if (isWithAnnotations != null) {
            builder.param("isWithAnnotations", Boolean.toString(isWithAnnotations));
        }
        return mvc.perform(builder);
    }

    @Test
    public void getRenditions() throws Exception {
        MvcResult mvcResult = createGetRenditionsRequest(null)
                .andExpect(status().isOk())
                .andExpect(content().contentType(ConstantsTestsUtil.APPLICATION_ZIP_VALUE))
                .andReturn();
        byte[] content = mvcResult.getResponse().getContentAsByteArray();
        Assert.notNull(content, "Content is null");
    }
}
