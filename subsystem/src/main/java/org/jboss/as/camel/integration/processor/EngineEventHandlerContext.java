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

package org.jboss.as.camel.integration.processor;

import java.util.ArrayList;
import java.util.Collection;

import com.ericsson.oss.itpf.common.config.Configuration;
import com.ericsson.oss.itpf.common.event.ControlEvent;
import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.EventSubscriber;

/**
 * Implementation of the <code>EvenHandlerContext</code> interface. Will be used to provide attributes to
 * <code>com.ericsson.oss.mediation.flow.FlowProcessor</code> objects.
 *
 * @see com.ericsson.oss.itpf.common.event.handler.EventHandlerContext
 */
public class EngineEventHandlerContext implements EventHandlerContext {

    private final Configuration configuration;

    /**
     * Constructor with configuration argument.
     *
     * @param configuration
     *            Configuration to use, @see com.ericsson.oss.itpf.common.config.Configuration
     */
    public EngineEventHandlerContext(final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getEventHandlerConfiguration() {
        return configuration;
    }

    @Override
    public Collection<EventSubscriber> getEventSubscribers() {
        return new ArrayList<EventSubscriber>();
    }

    @Override
    public void sendControlEvent(final ControlEvent event) {}

    @Override
    public Object getContextualData(final String value) {
        return null;
    }
}
