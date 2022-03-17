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
package eu.europa.ec.leos.ui.view;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.security.SecurityContext;
import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.compare.ContentComparatorService;
import eu.europa.ec.leos.services.content.processor.TransformationService;
import eu.europa.ec.leos.web.support.UrlBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.ATTR_NAME;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_REMOVED_CLASS;

@Component
@Scope("prototype")
@Instance(instances = {InstanceType.OS, InstanceType.COMMISSION})
public class ComparisonDelegateProposal<T extends XmlDocument> extends ComparisonDelegate<T> {

    public ComparisonDelegateProposal(TransformationService transformerService, ContentComparatorService compareService, UrlBuilder urlBuilder, SecurityContext securityContext) {
        super(transformerService, compareService, urlBuilder, securityContext);
    }

    @Override
    protected String getComparedContent(T oldVersion, T newVersion) {
        final String firstItemHtml = getDocumentAsHtml(oldVersion);
        final String secondItemHtml = getDocumentAsHtml(newVersion);
        return compareService.compareContents(new ContentComparatorContext.Builder(firstItemHtml, secondItemHtml)
                .withAttrName(ATTR_NAME)
                .withRemovedValue(CONTENT_REMOVED_CLASS)
                .withAddedValue(CONTENT_ADDED_CLASS)
                .build());
    }
}
