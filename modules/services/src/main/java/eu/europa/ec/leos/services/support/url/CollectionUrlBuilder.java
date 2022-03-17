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
package eu.europa.ec.leos.services.support.url;

import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Properties;

@Component
public class CollectionUrlBuilder {

    private Properties applicationProperties;

    public CollectionUrlBuilder(Properties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }


    public String buildProposalViewUrl(String proposalId) {
        String proposalViewUrl = applicationProperties.getProperty("leos.mapping.url") +
                applicationProperties.getProperty("leos.document.view.proposal.uri");
        return MessageFormat.format(proposalViewUrl, proposalId);
    }


    public String buildBillViewUrl(String billId) {
        return  MessageFormat.format(
                applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view.bill.uri")
                , billId);
    }

    public String buildMemorandumViewUrl(String memorandumId) {
        return  MessageFormat.format(
                applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view.memorandum.uri")
                , memorandumId);
    }

    public String buildAnnexViewUrl(String annexId) {
        return MessageFormat.format(
                applicationProperties.getProperty("leos.mapping.url") +
                        applicationProperties.getProperty("leos.document.view.annex.uri")
                , annexId);
    }
}
