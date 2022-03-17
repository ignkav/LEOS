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
package eu.europa.ec.leos.ui.extension;

import com.google.common.eventbus.EventBus;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.AbstractComponent;
import eu.europa.ec.leos.ui.shared.js.LeosJavaScriptExtensionState;
import eu.europa.ec.leos.web.support.LeosCacheToken;

@StyleSheet({"vaadin://../js/lib/sliderPins/css/sliderPins.css" + LeosCacheToken.TOKEN,
             "vaadin://../assets/css/leos-sliderPins.css"+ LeosCacheToken.TOKEN})
@JavaScript({"vaadin://../js/ui/extension/searchTargetConnector.js" + LeosCacheToken.TOKEN })
public class SearchTargetExtension<T extends AbstractComponent> extends LeosJavaScriptExtension {

    private static final long serialVersionUID = 1L;
    private EventBus eventBus;

    public SearchTargetExtension(T target, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        extend(target);
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        eventBus.unregister(this);
        super.detach();
    }

    @Override
    protected LeosJavaScriptExtensionState getState() {
        return (LeosJavaScriptExtensionState)super.getState();
    }
}
