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

import eu.europa.ec.leos.services.support.xml.SearchEngine;
import eu.europa.ec.leos.services.support.xml.SearchEngineImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SearchEngineFactory {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @Cacheable(value = "searchEngineCache", cacheManager = "cacheManager", key = "T(java.util.Arrays).hashCode(#p0)")
    public SearchEngine getInstance(byte[] content) {
        return SearchEngineImpl.forContent(content);
    }
}
