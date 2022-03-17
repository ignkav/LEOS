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

import eu.europa.ec.leos.services.support.xml.XmlNodeConfig;
import eu.europa.ec.leos.services.support.xml.XmlNodeProcessorImpl;
import eu.europa.ec.leos.services.util.TestUtils;
import eu.europa.ec.leos.test.support.LeosTest;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXmlAndRemoveAllNS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class XmlNodeProcessorImplTest extends LeosTest {

    protected final static String FILE_PREFIX = "/nodeProcessor";
    private static final Map<String, XmlNodeConfig> CONFIGURATIONS = setupConfig();

    @InjectMocks
    private XmlNodeProcessorImpl metaDataProcessor = new XmlNodeProcessorImpl();

    @Test
    public void getValues_values_present() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent("/bill-test.xml");
        String[] keys = {"docStage", "template", "language"};

        // When
        Map<String, String> result = metaDataProcessor.getValuesFromXml(xmlContent, keys, CONFIGURATIONS);

        // Then
        assertThat(result.size(), is(3));
        assertThat(result.get("template"), is("SJ-023"));
        assertThat(result.get("language"), is("EN"));
    }

    @Test
    public void getValues_values_not_present() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent("/memorandum-test.xml");
        String[] keys = {"docStage", "template"};

        // When
        Map<String, String> result = metaDataProcessor.getValuesFromXml(xmlContent, keys, CONFIGURATIONS);

        // Then
        assertThat(result.size(), is(0));
    }

    @Test
    public void getValues_AttributeNodeSelector() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/getValues_AttributeNodeSelector.xml");
        String[] keys = {"annexNumberCover"};

        Map<String, XmlNodeConfig> configuration = new HashMap<>();
        configuration.put("annexNumberCover", new XmlNodeConfig("//coverPage/container[@name='annexNumber']/p", false, Collections.emptyList()));

        // When
        Map<String, String> result = metaDataProcessor.getValuesFromXml(xmlContent, keys, configuration);

        // Then
        assertThat(result.get(keys[0]), is("ValidValue &"));
    }

    @Test
    public void setValues_tags_present() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/setValues_tags_present.xml");
        byte[] xmlContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/setValues_tags_present__expected.xml");

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("docPurpose", "NEWVALUE &");
        keyValue.put("docStage", "NEW Stage");
        keyValue.put("language", "EN");

        // When
        byte[] returnedElement = metaDataProcessor.setValuesInXml(xmlContent, keyValue, CONFIGURATIONS);

        // Then
        String result = new String(returnedElement);
        String expected = new String(xmlContentExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void test_setValues_tags_not_present() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/setValues_tags_not_present.xml");
        byte[] xmlContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/setValues_tags_not_present__expected.xml");

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("language", "EN");
        keyValue.put("docStage", "NEW Stage");
        keyValue.put("docPurpose", "NEWVALUE");

        // When
        byte[] returnedElement = metaDataProcessor.setValuesInXml(xmlContent, keyValue, CONFIGURATIONS);

        // Then
        Map<String, String> resultMap = metaDataProcessor.getValuesFromXml(returnedElement, new String[]{"language", "docStage", "docPurpose"}, CONFIGURATIONS);//Using a shortcut to validate test
        assertThat(resultMap.get("language"), is("EN"));
        assertThat(resultMap.get("docStage"), is("NEW Stage"));

        String result = new String(returnedElement);
        String expected = new String(xmlContentExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test(expected = IllegalStateException.class)
    public void test_setValues_wrongXPath() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/setValues_tags_not_present.xml");
        byte[] xmlContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/setValues_tags_not_present.xml");

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("wrong_xpath", "EN");

        // When
        byte[] returnedElement = metaDataProcessor.setValuesInXml(xmlContent, keyValue, CONFIGURATIONS);

        // Then
        String result = new String(returnedElement);
        String expected = new String(xmlContentExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void setValues_tags_not_present_in_big_xml() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent("/memorandum-test.xml");
        byte[] xmlContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/setValues_tags_not_present_in_big_xml__expected.xml");

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("language", "EN");
        keyValue.put("docStage", "NEW Stage");
        keyValue.put("docPurpose", "NEWVALUE");

        // When
        byte[] returnedElement = metaDataProcessor.setValuesInXml(xmlContent, keyValue, CONFIGURATIONS);

        // Then
        String result = new String(returnedElement);
        String expected = new String(xmlContentExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);

        Map<String, String> resultMap = metaDataProcessor.getValuesFromXml(returnedElement, new String[]{"language", "docStage", "docPurpose"}, CONFIGURATIONS);
        assertThat(resultMap.get("language"), is("EN"));
        assertThat(resultMap.get("docStage"), is("NEW Stage"));
    }

    @Test
    public void setValues_AttributeNodeSelectorAndCreateIsTrue() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/setValues_AttributeNodeSelectorAndCreateIsTrue.xml");
        byte[] xmlContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/setValues_AttributeNodeSelectorAndCreateIsTrue__expected.xml");

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("annexNumberCover", "Annex 1");

        Map<String, XmlNodeConfig> configuration = new HashMap<>();
        configuration.put("annexNumberCover", new XmlNodeConfig("//coverPage/container[@name='annexNumber']/p", true, Collections.emptyList()));

        // When
        byte[] returnedElement = metaDataProcessor.setValuesInXml(xmlContent, keyValue, configuration);

        // Then
        String result = new String(returnedElement);
        String expected = new String(xmlContentExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void setValues_AttributeNodeSelectorAndCreateIsFalse() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/setValues_AttributeNodeSelectorAndCreateIsFalse.xml");

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("annexNumberCover", "Annex 1");

        Map<String, XmlNodeConfig> configuration = new HashMap<>();
        configuration.put("annexNumberCover", new XmlNodeConfig("//coverPage/container[@name='annexNumber']/p", false,
                Collections.emptyList()));

        // When
        byte[] returnedElement = metaDataProcessor.setValuesInXml(xmlContent, keyValue, configuration);

        // Then
        String result = new String(returnedElement);
        String expected = new String(xmlContent);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    @Test
    public void setValues_councilExplanatory() {
        // Given
        byte[] xmlContent = TestUtils.getFileContent(FILE_PREFIX + "/setValues_councilExplanatory_proposal.xml");
        byte[] xmlContentExpected = TestUtils.getFileContent(FILE_PREFIX + "/setValues_councilExplanatory_proposal__expected.xml");

        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("council_explanatory", "newValueCouncilExplanatory");

        // When
        byte[] returnedElement = metaDataProcessor.setValuesInXml(xmlContent, keyValue, CONFIGURATIONS);

        // Then
        // TODO how to access to the record created?
//        Map<String, String> resultMap = metaDataProcessor.getValuesFromXml(returnedElement, new String[]{"council_explanatory"}, CONFIGURATIONS);
//        assertThat(resultMap.get("council_explanatory"), is("newValueCouncilExplanatory"));

        String result = new String(returnedElement);
        String expected = new String(xmlContentExpected);
        result = squeezeXmlAndRemoveAllNS(result);
        expected = squeezeXmlAndRemoveAllNS(expected);
        assertEquals(expected, result);
    }

    private static Map<String, XmlNodeConfig> setupConfig() {
        Map<String, XmlNodeConfig> configuration = new HashMap<>();

        configuration.put("docPurpose", new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docPurpose",true,
                Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docpurpose", "leos:docPurpose"),
                        new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        configuration.put("docStage", new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docStage",true,
                Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docstage", "leos:docStage"))));
        configuration.put("docType", new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docType",true,
                Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__doctype", "leos:docType"))));
        configuration.put("template", new XmlNodeConfig("//akn:meta/akn:proprietary/leos:template",true,
                Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__template","leos:template"))));
        configuration.put("language", new XmlNodeConfig("//akn:meta/akn:identification/akn:FRBRExpression/akn:FRBRlanguage/@language",true,
                Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "frbrexpression__frbrlanguage_1","FRBRlanguage"))));
        configuration.put("annexNumberCover", new XmlNodeConfig("//akn:coverPage/container[@name='annexNumber']/p",true,
                Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_annexNumber", "container"))));
        configuration.put("council_explanatory", new XmlNodeConfig("//akn:documentCollection/akn:collectionBody/akn:component[@refersTo='#council_explanatory']/akn:documentRef/@href", true,
                Arrays.asList(new XmlNodeConfig.Attribute("showAs", "Council Explanatory", "documentRef"))));
        configuration.put("wrong_xpath", new XmlNodeConfig("//wrong_xpath", true,
                Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "someId", "wrong_xpath"))));
        return configuration;
    }
}
