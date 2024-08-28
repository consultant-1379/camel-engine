/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.testsuite.smoke.deployment.common;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class MessageDrivenBean.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/jmsTestQueue")
        })
public class MessageDrivenBean implements MessageListener {
    public static final Logger LOG = LoggerFactory.getLogger(MessageDrivenBean.class);

    @EJB
    private LatchSingleton latchSingleton;

    @Override
    public void onMessage(final Message message) {
        if (message instanceof TextMessage) {
            try {
                final TextMessage msg = (TextMessage) message;
                LOG.debug("Recieved msg is {}", msg.getText());
                latchSingleton.getLatch().countDown();
            } catch (final JMSException jmse) {
                LOG.error("Exception caught processing message:", jmse);
            }
        }
    }
}
