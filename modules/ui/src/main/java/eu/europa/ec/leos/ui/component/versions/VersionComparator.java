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
package eu.europa.ec.leos.ui.component.versions;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.model.action.VersionVO;
import eu.europa.ec.leos.web.event.component.CompareRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class VersionComparator {
    
    protected EventBus eventBus;
    
    @Autowired
    public VersionComparator(EventBus eventBus){
        this.eventBus = eventBus;
    }
    
    abstract int getNumberVersionsForComparing();
    
    abstract void doubleCompare(Collection<VersionVO> selectedCheckBoxes);
    
    public boolean isCompareModeAvailable() {
        return true;
    }

    List<String> getOrderedCheckboxes(Collection<VersionVO> checkboxes) {
        return checkboxes
                .stream()
                .sorted(Comparator.comparing(VersionVO::getVersionNumber))
                .map(VersionVO::getDocumentId)
                .collect(Collectors.toList());
    }

    void compare(Collection<VersionVO> selectedCheckBoxes) {
        List<String> orderedCheckboxes = getOrderedCheckboxes(selectedCheckBoxes);
        final String oldVersion = orderedCheckboxes.get(0);
        final String newVersion = orderedCheckboxes.get(1);
        eventBus.post(new CompareRequestEvent(oldVersion, newVersion));
    }
}
