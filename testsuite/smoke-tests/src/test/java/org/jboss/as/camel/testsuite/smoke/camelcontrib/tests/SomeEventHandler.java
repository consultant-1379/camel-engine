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

import static org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.TestConstants.DEFAULT_RETURN_VALUE;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * The Class SomeEventHandler.
 */
@EventHandler
public class SomeEventHandler implements EventInputHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SomeEventHandler.class);
    private Map<String, Object> msgData;
    @EJB(lookup = "java:global/singleton/SomeSingleton!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeSingletonRemoteInterface")
    private SomeSingletonRemoteInterface someSingleton;

    @Override
    public void init(final EventHandlerContext ctx) {
        LOGGER.trace("init ctx with {}", ctx);
        if (ctx != null) {
            msgData = ctx.getEventHandlerConfiguration().getAllProperties();
        } else {
            LOGGER.trace("ctx is null, initialising msgData with empty hash map");
            msgData = new HashMap<String, Object>();
        }
    }

    @Override
    public void destroy() {}

    @Override
    public void onEvent(final Object inputEvent) {
        onEventWithResults(inputEvent);
    }

    @Override
    public Object onEventWithResults(final Object inputEvent) {
        LOGGER.debug("onEvent called with inputEvent [{}]", inputEvent);
        final String incoming = (String) msgData.get(TestConstants.CONTRIB_TEST_MSG_BODY);
        final String outgoing = incoming + " is camel contributed processor";
        someSingleton.setMessage(outgoing);
        LOGGER.debug("onEvent finished setting message with [{}]", incoming);
        return DEFAULT_RETURN_VALUE;
    }
}
