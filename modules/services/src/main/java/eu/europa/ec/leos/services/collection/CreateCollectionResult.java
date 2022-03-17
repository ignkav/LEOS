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
package eu.europa.ec.leos.services.collection;

import eu.europa.ec.leos.services.support.url.CollectionIdsAndUrlsHolder;
import java.util.Map;

public class CreateCollectionResult {

    private String proposalId;
    private String billId;
    private String proposalUrl;
    private String billUrl;
    private String memorandumUrl;
    private String memorandumId;
    private Map<String, String> annexIdUrl;
    private boolean collectionCreated;
    private CreateCollectionError error;

    // For Jackson
    public CreateCollectionResult() {
    }

    public CreateCollectionResult(CollectionIdsAndUrlsHolder idsAndUrlsHolder,
                                  boolean collectionCreated,
                                  CreateCollectionError error) {
        this.proposalId = idsAndUrlsHolder.getProposalId();
        this.proposalUrl = idsAndUrlsHolder.getProposalUrl();
        this.billId = idsAndUrlsHolder.getBillId();
        this.billUrl = idsAndUrlsHolder.getBillUrl();
        this.memorandumId = idsAndUrlsHolder.getMemorandumId();
        this.memorandumUrl = idsAndUrlsHolder.getMemorandumUrl();
        this.annexIdUrl = idsAndUrlsHolder.getAnnexIdAndUrl();
        this.collectionCreated = collectionCreated;
        this.error = error;
    }

    public String getProposalId() {
        return proposalId;
    }

    public String getProposalUrl() {
        return proposalUrl;
    }

    public String getBillId() {
        return billId;
    }

    public String getBillUrl() {
        return billUrl;
    }

    public String getMemorandumId() { return memorandumId; }

    public String getMemorandumUrl() { return memorandumUrl; }

    public Map<String, String> getAnnexIdUrl() { return annexIdUrl; }

    public boolean isCollectionCreated() {
        return collectionCreated;
    }

    public CreateCollectionError getError() {
        return error;
    }
}