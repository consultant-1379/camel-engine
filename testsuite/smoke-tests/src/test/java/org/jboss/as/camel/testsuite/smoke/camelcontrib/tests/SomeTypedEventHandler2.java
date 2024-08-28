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

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.ComponentEvent;
import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.TypedEventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * The Class SomeTypedEventHandler2.
 */
@EventHandler
public class SomeTypedEventHandler2 implements TypedEventInputHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SomeTypedEventHandler2.class);

    @EJB(lookup = "java:global/singleton/SomeSingleton!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeSingletonRemoteInterface")
    SomeSingletonRemoteInterface someSingleton;

    @Override
    public void init(final EventHandlerContext ctx) {
        LOGGER.debug("init ctx---->{}", ctx.toString());
    }

    @Override
    public void destroy() {}

    @Override
    public ComponentEvent onEvent(final ComponentEvent inputEvent) {
        LOGGER.debug("onEvent SomeTypedEventHandler2 called");
        final Map<String, Object> eventHeaders = inputEvent.getHeaders();
        LOGGER.debug("Handler2 recieved headers:{}", eventHeaders);
        someSingleton.setMessage((String) eventHeaders.get(TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED));
        LOGGER.debug("onEvent SomeTypedEventHandler2 finished");
        return inputEvent;
    }
}
