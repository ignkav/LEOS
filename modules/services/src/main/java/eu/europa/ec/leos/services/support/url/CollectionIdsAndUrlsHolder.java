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

import java.util.HashMap;
import java.util.Map;

public class CollectionIdsAndUrlsHolder {
    private String proposalId;
    private String proposalUrl;
    private String billId;
    private String billUrl;
    private String memorandumId;
    private String memorandumUrl;
    private Map<String,String> annexIdAndUrl;

    public CollectionIdsAndUrlsHolder() {
        this.annexIdAndUrl = new HashMap<>();
    }

    public String getProposalId() { return proposalId; }

    public void setProposalId(String proposalId) { this.proposalId = proposalId; }

    public String getProposalUrl() {return proposalUrl; }

    public void setProposalUrl(String proposalUrl) { this.proposalUrl = proposalUrl; }

    public String getBillId() { return billId; }

    public void setBillId(String billId) { this.billId = billId; }

    public String getBillUrl() {
        return billUrl;
    }

    public void setBillUrl(String billUrl) {
        this.billUrl = billUrl;
    }

    public String getMemorandumId() { return memorandumId; }

    public void setMemorandumId(String memorandumId) { this.memorandumId = memorandumId; }

    public String getMemorandumUrl() {
        return memorandumUrl;
    }

    public void setMemorandumUrl(String memorandumUrl) {
        this.memorandumUrl = memorandumUrl;
    }

    public Map<String, String> getAnnexIdAndUrl() {
        return annexIdAndUrl;
    }

    public void addAnnexIdAndUrl(String id, String annexUrl) {
        this.annexIdAndUrl.put(id, annexUrl);
    }
}