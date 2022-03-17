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
package eu.europa.ec.leos.services.messaging;

import eu.europa.ec.leos.model.messaging.UpdateInternalReferencesMessage;
import eu.europa.ec.leos.services.messaging.conf.Base64Serializer;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;

import static eu.europa.ec.leos.services.messaging.conf.JmsDestinations.QUEUE_UPDATE_INTERNAL_REFERENCE;

@Component
public class UpdateInternalReferencesProducer {

    private static final Logger logger = LoggerFactory.getLogger(UpdateInternalReferencesProducer.class);
    private static final ActiveMQQueue UPDATE_INTER_REF_QUEUE = new ActiveMQQueue(QUEUE_UPDATE_INTERNAL_REFERENCE);
    private final JmsTemplate jmsTemplate;

    public UpdateInternalReferencesProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void send(UpdateInternalReferencesMessage message) {
        logger.debug("Sending message: " + message);
        jmsTemplate.convertAndSend(UPDATE_INTER_REF_QUEUE, message, this::attachAuthenticationContext);
    }

    private Message attachAuthenticationContext(Message message) throws JMSException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String serialized = Base64Serializer.serialize(authentication);
        message.setStringProperty("authcontext", serialized);
        return message;
    }

}
