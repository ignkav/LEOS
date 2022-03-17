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
package eu.europa.ec.leos.services.numbering.config;

import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;
import java.util.List;

import static eu.europa.ec.leos.vo.toc.TocItemUtils.getNumberingByName;
import static eu.europa.ec.leos.vo.toc.TocItemUtils.getNumberingTypeByDepth;
import static eu.europa.ec.leos.vo.toc.TocItemUtils.getTocItemByName;

@Configuration
public class NumberingConfigProcessorFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NumberingConfigProcessorFactory.class);

    @Autowired
    protected Provider<StructureContext> structureContextProvider;

    public NumberingConfigProcessor getNumberProcessor(NumberingType numberingType, NumberingConfig numberingConfig) {
        String prefix = numberingConfig.getPrefix();
        String suffix = numberingConfig.getSuffix();
        switch (numberingType) {
            case ALPHA:
                return new NumberingConfigProcessorAlpha(prefix, suffix);
            case ROMAN_LOWER:
                return new NumberingConfigProcessorRoman(false, prefix, suffix);
            case ROMAN_UPPER:
                return new NumberingConfigProcessorRoman(true, prefix, suffix);
            case ARABIC:
            case ARABIC_POSTFIX:
            case ARABIC_PARENTHESIS:
            case ARABIC_POSTFIX_DEPTH:
                return new NumberingConfigProcessorArabic(prefix, suffix);
            case INDENT:
                return new NumberingConfigProcessorSymbol(numberingConfig.getSequence(), prefix, suffix);

            default:
                throw new IllegalStateException("No configuration found for numbering: " + numberingConfig.getType());
        }
    }

    public NumberingConfigProcessor getNumberProcessor(String elementName, int depth) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        List<NumberingConfig> numberingConfigs = structureContextProvider.get().getNumberingConfigs();

        TocItem tocItem = getTocItemByName(tocItems, elementName);
        NumberingConfig numberingConfig = getNumberingByName(numberingConfigs, tocItem.getNumberingType());
        NumberingType numberingType = numberingConfig.getType();

        if (depth != 0) { // if is a POINT, different config depending on the depth
            numberingType = getNumberingTypeByDepth(numberingConfig, depth);
            numberingConfig = getNumberingByName(numberingConfigs, numberingType); // update config
        }

        NumberingConfigProcessor numberProcessor = getNumberProcessor(numberingType, numberingConfig);
//        LOG.trace("Got from factory numberProcessor: {}, for numberingType: {} , depth: {}", numberProcessor.getClass(), numberingType, depth);
        return numberProcessor;
    }
}
