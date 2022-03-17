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
package eu.europa.ec.leos.ui.window.export;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.export.ExportOptions;
import eu.europa.ec.leos.services.export.RelevantElements;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ExplanatoryExportPackageWindow extends ExportPackageWindow {

    public ExplanatoryExportPackageWindow(MessageHelper messageHelper, EventBus eventBus) {
        this(messageHelper, eventBus, null);
    }

    public ExplanatoryExportPackageWindow(MessageHelper messageHelper, EventBus eventBus, ExportOptions exportOptions) {
        super(messageHelper, eventBus, exportOptions);
    }

    @Override
    protected List<RelevantElements> getRelevantElementsOptions() {
        return Arrays.asList(RelevantElements.ALL);
    }

    @Override
    protected RelevantElements getRelevantElementsFromSelected(Set<RelevantElements> relevantElementsSelected) {
        return relevantElementsSelected.stream().parallel().filter(elt -> !elt.equals(RelevantElements.ANNOTATIONS)).findFirst().orElse(RelevantElements.ALL);
    }
}
