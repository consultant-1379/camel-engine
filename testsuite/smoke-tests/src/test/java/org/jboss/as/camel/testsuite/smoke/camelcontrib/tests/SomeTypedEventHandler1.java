/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.ComponentEvent;
import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.TypedEventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;
import com.ericsson.oss.mediation.flow.events.MediationComponentEvent;

/**
 * The Class SomeTypedEventHandler1.
 */
@EventHandler
public class SomeTypedEventHandler1 implements TypedEventInputHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SomeTypedEventHandler1.class);

    private Map<String, Object> localHeaders;

    @Override
    public void init(final EventHandlerContext ctx) {
        LOGGER.debug("init ctx---->{}", ctx.toString());
        localHeaders = ctx.getEventHandlerConfiguration().getAllProperties();
        LOGGER.debug("Local headers {}", localHeaders);
    }

    @Override
    public void destroy() {}

    @Override
    public ComponentEvent onEvent(final ComponentEvent inputEvent) {
        final Map<String, Object> headers = inputEvent.getHeaders();
        LOGGER.debug("onEvent called with: [{}] and headers: [{}] and payload[{}]", inputEvent, headers, inputEvent.getPayload());
        headers.put(TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED, TestConstants.HANDLER_PAYLOAD);
        LOGGER.debug("Headers after being modified: {}", headers);
        final MediationComponentEvent mce = new MediationComponentEvent(headers, "Passing to SomeTypedEventHandler2...");
        LOGGER.debug("MediationComponentEvent is {}", mce);
        return mce;
    }
}
