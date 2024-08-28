/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;
import com.ericsson.oss.itpf.common.event.handler.exception.EventHandlerException;

/**
 * The Class ExceptionEventHandler.
 */
@EventHandler
public class ExceptionEventHandler implements EventInputHandler {
    public static final String EXCEPTION_MESSAGE = "Exception thrown from ExceptionEventHandler";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionEventHandler.class);

    @Override
    public void init(final EventHandlerContext context) {
        LOGGER.debug("Initializing with {}", context);
        throw new EventHandlerException(EXCEPTION_MESSAGE);
    }

    @Override
    public void destroy() {}

    @Override
    public void onEvent(final Object inputEvent) {}

    @Override
    public Object onEventWithResults(final Object inputEvent) {
        return "ExceptionEventHandlerReturnValue";
    }
}
