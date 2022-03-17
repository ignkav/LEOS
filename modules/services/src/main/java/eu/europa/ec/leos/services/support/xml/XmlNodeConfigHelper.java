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
import eu.europa.ec.leos.domain.cmis.metadata.AnnexMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.BillMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ExplanatoryMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.MemorandumMetadata;
import eu.europa.ec.leos.domain.cmis.metadata.ProposalMetadata;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public interface XmlNodeConfigHelper {

    String DOC_LANGUAGE = "docLanguage";
    String DOC_TEMPLATE = "docTemplate";
    String DOC_SPECIFIC_TEMPLATE = "docSpecificTemplate";

    String DOC_REF_META = "docRef";
    String DOC_OBJECT_ID = "objectId";
    String DOC_PURPOSE_META = "docPurposeMeta";
    String DOC_STAGE_META = "docStageMeta";
    String DOC_TYPE_META = "docTypeMeta";

    String DOC_PURPOSE_COVER = "docPurposeCover";
    String DOC_STAGE_COVER = "docStageCover";
    String DOC_TYPE_COVER = "docTypeCover";
    String DOC_LANGUAGE_COVER = "docLanguageCover";

    String DOC_PURPOSE_PREFACE = "docPurposePreface";
    String DOC_STAGE_PREFACE = "docStagePreface";
    String DOC_TYPE_PREFACE = "docTypePreface";

    String DOC_VERSION = "docVersion";
    String DOC_REF_COVER = "coverPage";

    String PROPOSAL_DOC_COLLECTION = "docCollectionName";

    String ANNEX_INDEX_META = "annexIndexMeta";
    String ANNEX_NUMBER_META = "annexNumberMeta";
    String ANNEX_TITLE_META = "annexTitleMeta";
    String ANNEX_NUMBER_COVER = "annexNumberCover";
    String ANNEX_TITLE_PREFACE = "annexTitlePreface";

    String EXPLANATORY_TITLE_PREFACE = "explanatoryTitlePreface";
    String DOC_TITLE_META = "docTitleMeta";

    Map<String, XmlNodeConfig> getConfig(LeosCategory proposal);

    String getCollectionBodyComponent(String attributeName, String refersTo);

    default Map<String, XmlNodeConfig> getProposalComponentsConfig(LeosCategory leosCategory, String attributeName) {
        Validate.notNull(leosCategory);
        Validate.notNull(attributeName);

        Map<String, XmlNodeConfig> componentRefConfig = new HashMap<>();
        String showAs;
        String refersTo = leosCategory.name().toLowerCase();

        //A better way to set showAs as it might be dependent of lang and docType.
        switch (leosCategory) {
            case BILL:
                showAs = "Regulation of the European Parliament and of the Council";
                break;
            case MEMORANDUM:
                showAs = "Explanatory Memorandum";
                break;
            case COUNCIL_EXPLANATORY:
                showAs = "Council Explanatory";
                break;
            default:
                throw new IllegalArgumentException("Invalid configuration");
        }
        componentRefConfig.put(leosCategory.name() + "_" + attributeName,
                new XmlNodeConfig(getCollectionBodyComponent(attributeName, refersTo), true,
                        Arrays.asList(new XmlNodeConfig.Attribute("showAs", showAs, "documentRef"))));
        return componentRefConfig;
    }

    static Map<String, String> createValueMap(AnnexMetadata metadata) {
        Map<String, String> keyValueMap = new HashMap<>();

        keyValueMap.put(DOC_STAGE_META, metadata.getStage());
        keyValueMap.put(DOC_TYPE_META, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_META, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE, metadata.getLanguage().toLowerCase());
        keyValueMap.put(DOC_TEMPLATE, metadata.getTemplate());
        keyValueMap.put(DOC_SPECIFIC_TEMPLATE, metadata.getDocTemplate());
        keyValueMap.put(DOC_REF_META, metadata.getRef());
        keyValueMap.put(DOC_OBJECT_ID, metadata.getObjectId());

        keyValueMap.put(ANNEX_INDEX_META, Integer.toString(metadata.getIndex()));
        keyValueMap.put(ANNEX_NUMBER_META, metadata.getNumber());
        keyValueMap.put(ANNEX_TITLE_META, metadata.getTitle());

        keyValueMap.put(DOC_STAGE_COVER, metadata.getStage());
        keyValueMap.put(DOC_TYPE_COVER, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_COVER, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE_COVER, metadata.getLanguage().toUpperCase());
        keyValueMap.put(DOC_VERSION, metadata.getDocVersion());

        keyValueMap.put(ANNEX_NUMBER_COVER, metadata.getNumber());
        keyValueMap.put(ANNEX_TITLE_PREFACE, metadata.getTitle());

        return keyValueMap;
    }

    static Map<String, String> createValueMap(ExplanatoryMetadata metadata) {
        Map<String, String> keyValueMap = new HashMap<>();

        keyValueMap.put(DOC_STAGE_META, metadata.getStage());
        keyValueMap.put(DOC_TYPE_META, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_META, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE, metadata.getLanguage().toLowerCase());
        keyValueMap.put(DOC_TEMPLATE, metadata.getTemplate());
        keyValueMap.put(DOC_SPECIFIC_TEMPLATE, metadata.getDocTemplate());
        keyValueMap.put(DOC_REF_META, metadata.getRef());

        keyValueMap.put(EXPLANATORY_TITLE_PREFACE, metadata.getTitle());
        keyValueMap.put(DOC_TITLE_META, metadata.getTitle());

        keyValueMap.put(DOC_OBJECT_ID, metadata.getObjectId());

        keyValueMap.put(DOC_STAGE_COVER, metadata.getStage());
        keyValueMap.put(DOC_TYPE_COVER, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_COVER, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE_COVER, metadata.getLanguage().toUpperCase());
        keyValueMap.put(DOC_VERSION, metadata.getDocVersion());

        return keyValueMap;
    }

    static Map<String, String> createValueMap(ProposalMetadata metadata) {
        Map<String, String> keyValueMap = new HashMap<>();

        keyValueMap.put(DOC_STAGE_META, metadata.getStage());
        keyValueMap.put(DOC_TYPE_META, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_META, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE, metadata.getLanguage().toLowerCase());
        keyValueMap.put(DOC_TEMPLATE, metadata.getTemplate());
        keyValueMap.put(DOC_SPECIFIC_TEMPLATE, metadata.getDocTemplate());
        keyValueMap.put(DOC_REF_META, metadata.getRef());
        keyValueMap.put(DOC_OBJECT_ID, metadata.getObjectId());
        keyValueMap.put(DOC_STAGE_COVER, metadata.getStage());
        keyValueMap.put(DOC_TYPE_COVER, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_COVER, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE_COVER, metadata.getLanguage().toUpperCase());
        keyValueMap.put(DOC_VERSION, metadata.getDocVersion());

        return keyValueMap;
    }

    static Map<String, String> createValueMap(BillMetadata metadata) {
        Map<String, String> keyValueMap = new HashMap<>();

        keyValueMap.put(DOC_STAGE_META, metadata.getStage());
        keyValueMap.put(DOC_TYPE_META, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_META, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE, metadata.getLanguage().toLowerCase());
        keyValueMap.put(DOC_TEMPLATE, metadata.getTemplate());
        keyValueMap.put(DOC_SPECIFIC_TEMPLATE, metadata.getDocTemplate());
        keyValueMap.put(DOC_REF_META, metadata.getRef());
        keyValueMap.put(DOC_OBJECT_ID, metadata.getObjectId());

        keyValueMap.put(DOC_STAGE_COVER, metadata.getStage());
        keyValueMap.put(DOC_TYPE_COVER, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_COVER, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE_COVER, metadata.getLanguage().toUpperCase());

        keyValueMap.put(DOC_STAGE_PREFACE, metadata.getStage());
        keyValueMap.put(DOC_TYPE_PREFACE, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_PREFACE, metadata.getPurpose());
        keyValueMap.put(DOC_VERSION, metadata.getDocVersion());

        return keyValueMap;
    }

    static Map<String, String> createValueMap(MemorandumMetadata metadata) {
        Map<String, String> keyValueMap = new HashMap<>();

        keyValueMap.put(DOC_STAGE_META, metadata.getStage());
        keyValueMap.put(DOC_TYPE_META, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_META, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE, metadata.getLanguage().toLowerCase());
        keyValueMap.put(DOC_TEMPLATE, metadata.getTemplate());
        keyValueMap.put(DOC_SPECIFIC_TEMPLATE, metadata.getDocTemplate());
        keyValueMap.put(DOC_REF_META, metadata.getRef());
        keyValueMap.put(DOC_OBJECT_ID, metadata.getObjectId());

        keyValueMap.put(DOC_STAGE_COVER, metadata.getStage());
        keyValueMap.put(DOC_TYPE_COVER, metadata.getType());
        keyValueMap.put(DOC_PURPOSE_COVER, metadata.getPurpose());
        keyValueMap.put(DOC_LANGUAGE_COVER, metadata.getLanguage().toUpperCase());
        keyValueMap.put(DOC_VERSION, metadata.getDocVersion());

        return keyValueMap;
    }
}
