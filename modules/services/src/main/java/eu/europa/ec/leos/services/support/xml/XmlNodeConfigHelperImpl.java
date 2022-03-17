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

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Instance(instances = {InstanceType.OS, InstanceType.COMMISSION})
public class XmlNodeConfigHelperImpl implements XmlNodeConfigHelper {

    private static final Map<LeosCategory, Map<String, XmlNodeConfig>> All_CONFIG_MAP = new HashMap<>();

    static {
        All_CONFIG_MAP.put(LeosCategory.PROPOSAL, createProposalConfig());
        All_CONFIG_MAP.put(LeosCategory.BILL, createBillConfig());
        All_CONFIG_MAP.put(LeosCategory.MEMORANDUM, createMemorandumConfig());
        All_CONFIG_MAP.put(LeosCategory.ANNEX, createAnnexConfig());
        All_CONFIG_MAP.put(LeosCategory.COUNCIL_EXPLANATORY, createExplanatoryConfig());
    }

    private static Map<String, XmlNodeConfig> createProposalConfig() {
        Map<String, XmlNodeConfig> proposalConfigMap = new HashMap<>();

        final Map<String, XmlNodeConfig> metadataConfig = new HashMap<>(8);
        metadataConfig.put(DOC_STAGE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docstage", "leos:docStage"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_TYPE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__doctype", "leos:docType"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_PURPOSE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docpurpose", "leos:docPurpose"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_REF_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:ref", true, Collections.emptyList()));
        metadataConfig.put(DOC_OBJECT_ID, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:objectId", true, Collections.emptyList()));
        metadataConfig.put(DOC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:template", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_template", "leos:template"))));
        metadataConfig.put(DOC_SPECIFIC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docTemplate", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_docTemplate", "leos:docTemplate"))));
        metadataConfig.put(DOC_LANGUAGE, new XmlNodeConfig("//akn:meta/akn:identification/akn:FRBRExpression/akn:FRBRlanguage/@language", false, Collections.emptyList()));
        metadataConfig.put(DOC_VERSION, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docVersion", true, Collections.emptyList()));

        proposalConfigMap.putAll(metadataConfig);

        final Map<String, XmlNodeConfig> coverPageConfig = new HashMap<>(4);
        coverPageConfig.put(DOC_STAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docstage", "docStage"))));
        coverPageConfig.put(DOC_TYPE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_doctype", "docType"))));
        coverPageConfig.put(DOC_PURPOSE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docpurpose", "docPurpose"))));
        coverPageConfig.put(DOC_LANGUAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:container[@name='language']/akn:p", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_language", "container"))));

        proposalConfigMap.putAll(coverPageConfig);

        final Map<String, XmlNodeConfig> otherConfig = new HashMap<>(4);
        otherConfig.put(PROPOSAL_DOC_COLLECTION, new XmlNodeConfig("//akn:documentCollection/@name", false, Collections.emptyList()));
        otherConfig.put(DOC_REF_COVER, new XmlNodeConfig("//akn:coverPage/@xml:id", false, Collections.emptyList()));
        proposalConfigMap.putAll(otherConfig);

        return proposalConfigMap;
    }

    private static Map<String, XmlNodeConfig> createBillConfig() {
        Map<String, XmlNodeConfig> billConfigMap = new HashMap<>();

        final Map<String, XmlNodeConfig> metadataConfig = new HashMap<>(9);
        metadataConfig.put(DOC_STAGE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docstage", "leos:docStage"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_TYPE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__doctype", "leos:docType"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_PURPOSE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docpurpose", "leos:docPurpose"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_REF_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:ref", true, Collections.emptyList()));
        metadataConfig.put(DOC_OBJECT_ID, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:objectId", true, Collections.emptyList()));
        metadataConfig.put(DOC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:template", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_template", "leos:template"))));
        metadataConfig.put(DOC_SPECIFIC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docTemplate", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_docTemplate", "leos:docTemplate"))));
        metadataConfig.put(DOC_LANGUAGE, new XmlNodeConfig("//akn:meta/akn:identification/akn:FRBRExpression/akn:FRBRlanguage/@language", false, Collections.emptyList()));
        metadataConfig.put(DOC_VERSION, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docVersion", true, Collections.emptyList()));
        billConfigMap.putAll(metadataConfig);

        final Map<String, XmlNodeConfig> coverPageConfig = new HashMap<>(4);
        coverPageConfig.put(DOC_STAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docStage", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docstage", "docStage"))));
        coverPageConfig.put(DOC_TYPE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docType", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_doctype", "docType"))));
        coverPageConfig.put(DOC_PURPOSE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docPurpose", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docpurpose", "docPurpose"))));
        coverPageConfig.put(DOC_LANGUAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:container[@name='language']/akn:p", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_language", "container"))));
        billConfigMap.putAll(coverPageConfig);

        final Map<String, XmlNodeConfig> prefaceConfig = new HashMap<>(4);
        prefaceConfig.put(DOC_STAGE_PREFACE, new XmlNodeConfig("//akn:preface/akn:longTitle/akn:p/akn:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "preface_docStage", "docStage"))));
        prefaceConfig.put(DOC_TYPE_PREFACE, new XmlNodeConfig("//akn:preface/akn:longTitle/akn:p/akn:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "preface_doctype", "docType"))));
        prefaceConfig.put(DOC_PURPOSE_PREFACE, new XmlNodeConfig("//akn:preface/akn:longTitle/akn:p/akn:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "preface_docpurpose", "docPurpose"))));
        billConfigMap.putAll(prefaceConfig);

        final Map<String, XmlNodeConfig> otherConfig = new HashMap<>(4);
        otherConfig.put(DOC_REF_COVER, new XmlNodeConfig("//akn:coverPage/@xml:id", false, Collections.emptyList()));
        billConfigMap.putAll(otherConfig);

        return billConfigMap;
    }

    private static Map<String, XmlNodeConfig> createMemorandumConfig() {
        Map<String, XmlNodeConfig> memorandumConfigMap = new HashMap<>();

        final Map<String, XmlNodeConfig> metadataConfig = new HashMap<>(8);
        metadataConfig.put(DOC_STAGE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docstage", "leos:docStage"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_TYPE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__doctype", "leos:docType"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_PURPOSE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docpurpose", "leos:docPurpose"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_REF_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:ref", true, Collections.emptyList()));
        metadataConfig.put(DOC_OBJECT_ID, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:objectId", true, Collections.emptyList()));
        metadataConfig.put(DOC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:template", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_template", "leos:template"))));
        metadataConfig.put(DOC_SPECIFIC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docTemplate", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_docTemplate", "leos:docTemplate"))));
        metadataConfig.put(DOC_LANGUAGE, new XmlNodeConfig("//akn:meta/akn:identification/akn:FRBRExpression/akn:FRBRlanguage/@language", false, Collections.emptyList()));
        metadataConfig.put(DOC_VERSION, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docVersion", true, Collections.emptyList()));
        memorandumConfigMap.putAll(metadataConfig);

        final Map<String, XmlNodeConfig> coverPageConfig = new HashMap<>(4);
        coverPageConfig.put(DOC_STAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docstage", "docStage"))));
        coverPageConfig.put(DOC_TYPE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_doctype", "docType"))));
        coverPageConfig.put(DOC_PURPOSE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docpurpose", "docPurpose"), new XmlNodeConfig.Attribute("xml:id", "em_coverpage__longTitle", "longTitle"), new XmlNodeConfig.Attribute("refersTo", "#bill", "longTitle"))));
        coverPageConfig.put(DOC_LANGUAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:container[@name='language']/akn:p", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_language", "container"))));
        memorandumConfigMap.putAll(coverPageConfig);

        final Map<String, XmlNodeConfig> otherConfig = new HashMap<>(4);
        otherConfig.put(DOC_REF_COVER, new XmlNodeConfig("//akn:coverPage/@xml:id", false, Collections.emptyList()));
        memorandumConfigMap.putAll(otherConfig);

        return memorandumConfigMap;
    }

    private static Map<String, XmlNodeConfig> createExplanatoryConfig() {
        Map<String, XmlNodeConfig> explanatoryConfigMap = new HashMap<>();

        final Map<String, XmlNodeConfig> metadataConfig = new HashMap<>(16);
        metadataConfig.put(DOC_STAGE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docstage", "leos:docStage"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_TYPE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__doctype", "leos:docType"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_PURPOSE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docpurpose", "leos:docPurpose"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_REF_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:ref", true, Collections.emptyList()));
        metadataConfig.put(DOC_TITLE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:title", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__title", "leos:title"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_OBJECT_ID, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:objectId", true, Collections.emptyList()));
        metadataConfig.put(DOC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:template", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_template", "leos:template"))));
        metadataConfig.put(DOC_SPECIFIC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docTemplate", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_docTemplate", "leos:docTemplate"))));
        metadataConfig.put(DOC_LANGUAGE, new XmlNodeConfig("//akn:meta/akn:identification/akn:FRBRExpression/akn:FRBRlanguage/@language", false, Collections.emptyList()));


        metadataConfig.put(DOC_VERSION, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docVersion", true, Collections.emptyList()));
        explanatoryConfigMap.putAll(metadataConfig);

        final Map<String, XmlNodeConfig> coverPageConfig = new HashMap<>(5);
        coverPageConfig.put(DOC_STAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docStage", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docstage", "docStage"))));
        coverPageConfig.put(DOC_TYPE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docType", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_doctype", "docType"))));
        coverPageConfig.put(DOC_PURPOSE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docPurpose", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docpurpose", "docPurpose"), new XmlNodeConfig.Attribute("xml:id", "em_coverpage__longTitle", "longTitle"), new XmlNodeConfig.Attribute("refersTo", "#bill", "longTitle"))));

        coverPageConfig.put(DOC_LANGUAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:container[@name='language']/akn:p", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_language", "container"))));
        explanatoryConfigMap.putAll(coverPageConfig);

        final Map<String, XmlNodeConfig> prefaceConfig = new HashMap<>(4);
        prefaceConfig.put(EXPLANATORY_TITLE_PREFACE, new XmlNodeConfig("//akn:preface/akn:longTitle/akn:p/akn:docTitle", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "preface_doctitle", "docTitle"))));
        explanatoryConfigMap.putAll(prefaceConfig);

        final Map<String, XmlNodeConfig> otherConfig = new HashMap<>(4);
        otherConfig.put(DOC_REF_COVER, new XmlNodeConfig("//akn:coverPage/@xml:id", false, Collections.emptyList()));
        explanatoryConfigMap.putAll(otherConfig);

        return explanatoryConfigMap;
    }

    private static Map<String, XmlNodeConfig> createAnnexConfig() {
        Map<String, XmlNodeConfig> annexConfigMap = new HashMap<>();

        final Map<String, XmlNodeConfig> metadataConfig = new HashMap<>(16);
        metadataConfig.put(DOC_STAGE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docStage", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docstage", "leos:docStage"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_TYPE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docType", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__doctype", "leos:docType"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_PURPOSE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docPurpose", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__docpurpose", "leos:docPurpose"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_REF_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:ref", true, Collections.emptyList()));
        metadataConfig.put(DOC_OBJECT_ID, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:objectId", true, Collections.emptyList()));
        metadataConfig.put(DOC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:template", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_template", "leos:template"))));
        metadataConfig.put(DOC_SPECIFIC_TEMPLATE, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docTemplate", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary_docTemplate", "leos:docTemplate"))));
        metadataConfig.put(DOC_LANGUAGE, new XmlNodeConfig("//akn:meta/akn:identification/akn:FRBRExpression/akn:FRBRlanguage/@language", false, Collections.emptyList()));

        metadataConfig.put(ANNEX_INDEX_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:annexIndex", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__annexIndex", "leos:annexIndex"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(ANNEX_NUMBER_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:annexNumber", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__annexNumber", "leos:annexNumber"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(ANNEX_TITLE_META, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:annexTitle", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "proprietary__annexTitle", "leos:annexTitle"), new XmlNodeConfig.Attribute("source", "~leos", "proprietary"))));
        metadataConfig.put(DOC_VERSION, new XmlNodeConfig("/akn:akomaNtoso//akn:meta/akn:proprietary/leos:docVersion", true, Collections.emptyList()));
        annexConfigMap.putAll(metadataConfig);

        final Map<String, XmlNodeConfig> coverPageConfig = new HashMap<>(5);
        coverPageConfig.put(DOC_STAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docStage", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docstage", "docStage"))));
        coverPageConfig.put(DOC_TYPE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docType", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_doctype", "docType"))));
        coverPageConfig.put(DOC_PURPOSE_COVER, new XmlNodeConfig("//akn:coverPage/akn:longTitle/akn:p/akn:docPurpose", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_docpurpose", "docPurpose"), new XmlNodeConfig.Attribute("xml:id", "em_coverpage__longTitle", "longTitle"), new XmlNodeConfig.Attribute("refersTo", "#bill", "longTitle"))));
        coverPageConfig.put(ANNEX_NUMBER_COVER, new XmlNodeConfig("//akn:coverPage/akn:container[@name='annexNumber']/akn:p", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_annexNumber", "container"))));
        coverPageConfig.put(DOC_LANGUAGE_COVER, new XmlNodeConfig("//akn:coverPage/akn:container[@name='language']/akn:p", false, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "cover_language", "container"))));
        annexConfigMap.putAll(coverPageConfig);

        final Map<String, XmlNodeConfig> prefaceConfig = new HashMap<>(4);
        prefaceConfig.put(ANNEX_TITLE_PREFACE, new XmlNodeConfig("//akn:preface/akn:longTitle/akn:p/akn:docTitle", true, Arrays.asList(new XmlNodeConfig.Attribute("xml:id", "preface_doctitle", "docTitle"))));
        annexConfigMap.putAll(prefaceConfig);

        final Map<String, XmlNodeConfig> otherConfig = new HashMap<>(4);
        otherConfig.put(DOC_REF_COVER, new XmlNodeConfig("//akn:coverPage/@xml:id", false, Collections.emptyList()));
        annexConfigMap.putAll(otherConfig);

        return annexConfigMap;
    }

    public Map<String, XmlNodeConfig> getConfig(LeosCategory category) {
        Map<String, XmlNodeConfig> config = All_CONFIG_MAP.get(category);
        if (config == null) {
            throw new UnsupportedOperationException("There is no configuration present for category " + category);
        }
        return config;
    }

    public String getCollectionBodyComponent(String attributeName, String refersTo) {
        return String.format("//akn:documentCollection/akn:collectionBody/akn:component[@refersTo='#%s']/akn:documentRef/@%s", refersTo, attributeName);
    }
}
