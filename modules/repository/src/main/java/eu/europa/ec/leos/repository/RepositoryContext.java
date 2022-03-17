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
package eu.europa.ec.leos.repository;

import eu.europa.ec.leos.cmis.mapping.CmisMapper;
import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.cmis.repository.CmisRepository;
import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Component
@Scope(WebApplicationContext.SCOPE_APPLICATION)
public class RepositoryContext {
    
    private final CmisRepository cmisRepository;
    
    private static final Map<String, String> oldVersions = new HashMap<>();
    
    public RepositoryContext(CmisRepository cmisRepository) {
        this.cmisRepository = cmisRepository;
    }
    
    /**
     * This method is used for Backward compatibility of story LEOS-3661.
     * All versions actually present in the CMIS do not have a value for leos:versionLabel property,
     * and the property value can not be set for an old version.
     *
     * @return map containing old versions and their corresponding versionLabel; as pair [cmis:objectId, leos:versionLabel]
     */
    public Map<String, String> getVersionsWithoutVersionLabel() {
        return oldVersions;
    }
    
    public <D extends LeosDocument> void populateVersionsWithoutVersionLabel(Class<? extends D> type, String documentRef) {
        String primaryType = CmisMapper.cmisPrimaryType(type);
        List<Document> docs = cmisRepository.findVersionsWithoutVersionLabel(primaryType, documentRef);
        updateVersionLabel(docs);
    }
    
    /**
     * Calculates leos:versionLabel for each element of the list and returns a map with the pair [cmis:objectId, leos:versionLabel]
     * List<Document> is format:
     * ID    cmis:label     isMajor leos:milestoneComments  leos:versionLabel
     * 109   3.1                                            1.0.1
     * 108   3.0            Y       "Milestone created"     1.0.0
     * 107   2.2                                            0.2.2
     * 106   2.1                                            0.2.1
     * 105   2.0            Y                               0.2.0
     * 104   1.1                                            0.1.1
     * 103   1.0            Y                               0.1.0
     * 102   0.2                                            0.0.2
     * 101   0.1                                            0.0.1
     */
    //TODO unify logic with VersionVO.VersionNumber
    public static void updateVersionLabel(List<Document> allVersions) {
        String[] version = new String[]{"0", "0", "0"};
        ListIterator<Document> allVersionsIterator = allVersions.listIterator(allVersions.size());
        Document previousVersion;   // Version Label property cannot be updated in document versions, only in last version of the version series.
        String versionLabel;        // This is used only for compatibility, old documents that do not have populated version label property.
        if (allVersionsIterator.hasPrevious()) {
            do {
                previousVersion = allVersionsIterator.previous();
                versionLabel = oldVersions.get(previousVersion.getId());
                if (StringUtils.isEmpty(versionLabel)) {
                    if (!previousVersion.isMajorVersion()) {
                        version[2] = Integer.parseInt(version[2]) + 1 + "";
                    } else if ((previousVersion.getProperty(CmisProperties.MILESTONE_COMMENTS.getId()) != null) &&
                            (!previousVersion.getProperty(CmisProperties.MILESTONE_COMMENTS.getId()).getValues().isEmpty())) {
                        version[0] = Integer.parseInt(version[0]) + 1 + "";
                        version[1] = "0";
                        version[2] = "0";
                    } else {
                        version[1] = Integer.parseInt(version[1]) + 1 + "";
                        version[2] = "0";
                    }
                    oldVersions.put(previousVersion.getId(), version[0] + "." + version[1] + "." + version[2]);
                }
            } while (allVersionsIterator.hasPrevious());
        }
    }
}
