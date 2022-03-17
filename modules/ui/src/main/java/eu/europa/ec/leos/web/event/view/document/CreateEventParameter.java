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
package eu.europa.ec.leos.web.event.view.document;

import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.user.User;

public class CreateEventParameter {
    
    public String elementId;
    public String elementTagName;
    public String elementFragment;
    public String docType;
    public User user;
    public String[] authorities;
    public String alternative;
    public LevelItemVO levelItemVo;
    public boolean isCloneProposal;

    public CreateEventParameter(String elementId, String elementTagName, String elementFragment, String docType,
                                User user, String[] authorities) {
        this.elementId = elementId;
        this.elementTagName = elementTagName;
        this.elementFragment = elementFragment;
        this.docType = docType;
        this.user = user;
        this.authorities = authorities;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementTagName() {
        return elementTagName;
    }

    public void setElementTagName(String elementTagName) {
        this.elementTagName = elementTagName;
    }

    public String getElementFragment() {
        return elementFragment;
    }

    public void setElementFragment(String elementFragment) {
        this.elementFragment = elementFragment;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String[] getAuthorities() {
        return authorities;
    }

    public void setAuthorities(String[] authorities) {
        this.authorities = authorities;
    }

    public String getAlternative() {
        return alternative;
    }

    public void setAlternative(String alternative) {
        this.alternative = alternative;
    }

    public LevelItemVO getLevelItemVo() {
        return levelItemVo;
    }

    public void setLevelItemVo(LevelItemVO levelItemVo) {
        this.levelItemVo = levelItemVo;
    }

    public boolean isCloneProposal() { return isCloneProposal; }

    public void setCloneProposal(boolean cloneProposal) { isCloneProposal = cloneProposal;}
}