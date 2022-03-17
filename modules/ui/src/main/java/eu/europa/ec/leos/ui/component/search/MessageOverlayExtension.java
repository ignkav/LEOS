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
package eu.europa.ec.leos.ui.component.search;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.TextField;
import eu.europa.ec.leos.ui.shared.search.MessageOverlayState;

public class MessageOverlayExtension extends AbstractExtension {
    protected MessageOverlayExtension(TextField field) {
        extend(field);
    }
    @Override
    protected MessageOverlayState getState() {
        return (MessageOverlayState) super.getState();
    }

    @Override
    protected MessageOverlayState getState(boolean markAsDirty) {
        return (MessageOverlayState) super.getState(markAsDirty);
    }

    public void setMessage(String message) {
        getState().message = message;
    }

}
