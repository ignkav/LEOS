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
package eu.europa.ec.leos.ui.client.search;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VTextField;
import com.vaadin.client.ui.textfield.TextFieldConnector;
import com.vaadin.shared.ui.Connect;
import eu.europa.ec.leos.ui.component.search.MessageOverlayExtension;
import eu.europa.ec.leos.ui.shared.search.MessageOverlayState;

import java.util.Optional;

@Connect(MessageOverlayExtension.class)
public class MessageOverlayConnector extends AbstractExtensionConnector {

    private VTextField textField;
    private final Element wrapper = createWrapper();
    private HandlerRegistration attachHandler;

    @Override
    protected void extend(ServerConnector target) {
        if (getState().message != null && !getState().message.isEmpty()) {
            wrapper.setInnerText(getState().message);
        }
        textField = ((TextFieldConnector) target).getWidget();

        attachHandler = textField.addAttachHandler(event -> {
            if (event.isAttached()) {
                addExtensionElements();
            } else {
                removeExtensionElements();
            }
        });
    }

    private Element createWrapper() {
        Element elem = DOM.createSpan();
        elem.addClassName("leos-match-message");
        return elem;
    }

    @OnStateChange("message")
    private void changeMessage() {
        wrapper.setInnerText(getState().message);
    }

    @Override
    public void onUnregister() {
        super.onUnregister();

        // Remove autocomplete elements
        if (textField.isAttached()) {
            removeExtensionElements();
        }

        // Remove text field attach handler
        Optional.ofNullable(attachHandler)
                .ifPresent(HandlerRegistration::removeHandler);
    }

    private void addExtensionElements() {
        Element textElement = textField.getElement();
        textElement.setAttribute("autocomplete","off");// to stop browser autocomplete
        textElement.getParentElement().appendChild(wrapper);
    }

    private void removeExtensionElements() {
        wrapper.removeFromParent();
    }

    @Override
    public MessageOverlayState getState() {
        return (MessageOverlayState) super.getState();
    }
}
