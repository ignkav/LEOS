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
package eu.europa.ec.leos.services.toc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europa.ec.leos.vo.toc.AlternateConfig;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;

/**
 * POJO to wrap java objects configured in TOC XML.
 */
public class TocStructure {
    
    private List<TocItem> tocItems;
    private Map<TocItem, List<TocItem>> tocRules = new HashMap<>();
    private List<NumberingConfig> numberingConfigs;
    private List<AlternateConfig> alternateConfigs;
    private String structureName;
    private String structureVersion;
    private String structureDescription;
    
    public List<TocItem> getTocItems() {
        return tocItems;
    }
    
    public void setTocItems(List<TocItem> tocItems) {
        this.tocItems = tocItems;
    }
    
    public Map<TocItem, List<TocItem>> getTocRules() {
        return tocRules;
    }
    
    public void setTocRules(Map<TocItem, List<TocItem>> tocRules) {
        this.tocRules = tocRules;
    }
    
    public List<NumberingConfig> getNumberingConfigs() {
        return numberingConfigs;
    }
    
    public void setNumberingConfigs(List<NumberingConfig> numberingConfigs) {
        this.numberingConfigs = numberingConfigs;
    }
    
    public List<AlternateConfig> getAlternateConfigs() {
        return alternateConfigs;
    }

    public void setAlternateConfigs(List<AlternateConfig> alternateConfigs) {
        this.alternateConfigs = alternateConfigs;
    }

    public String getStructureName() {
        return structureName;
    }
    
    public void setStructureName(String structureName) {
        this.structureName = structureName;
    }
    
    public String getStructureVersion() {
        return structureVersion;
    }
    
    public void setStructureVersion(String structureVersion) {
        this.structureVersion = structureVersion;
    }

    public void setStructureDescription(String structureDescription) {
        this.structureDescription = structureDescription;
    }

    public String getStructureDescription() {
        return structureDescription;
    }
}
