/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.ResultEventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * The Class PathElementProcessorTestCase.
 */
@RunWith(PowerMockRunner.class)
public class PathElementProcessorTestCase {

    @Mock
    private Exchange exchange;

    @Mock
    private Message inMessage;

    @Mock
    private Message outMessage;

    private PathElementProcessor<EventInputHandler> pathElementProcessor;

    @Mock
    private Map<String, Object> headers;

    @Mock
    private EventInputHandler handler;

    @Mock
    private ResultEventInputHandler resultHandler;

    @Test(expected = InstantiationException.class)
    public void testPathProcessorDeprecated() throws Exception {

        final String handlerClassName = TestEventInputHandler.class.getName();
        pathElementProcessor = new PathElementProcessor(TestEventInputHandler.class, handlerClassName);
    }

    @Test
    public void testPathProcessor() throws Exception {

        final String handlerClassName = TestEventInputHandler.class.getName();
        final EventInputHandler flowProcessor = handler;
        pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor, handlerClassName);
        Assert.assertNotNull(pathElementProcessor);
    }

    @Test
    public void testPathProcessor_ResultEventInput() throws Exception {

        final String handlerClassName = TestResultEventInputHandler.class.getName();
        final ResultEventInputHandler flowProcessor = resultHandler;
        pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor, handlerClassName);
        Assert.assertNotNull(pathElementProcessor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPathProcessorNull() throws Exception {

        final String handlerClassName = TestEventInputHandler.class.getName();
        final EventInputHandler flowProcessor = null;
        pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor, handlerClassName);
    }

    @Test
    public void testProcessHeadersEmpty() throws IllegalAccessException {

        final String handlerClassName = TestEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);
        Mockito.when(inMessage.getHeaders()).thenReturn(headers);
        Mockito.when(headers.get(handlerClassName)).thenReturn(null);

        final EventInputHandler flowProcessor = new TestEventInputHandler();
        pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor, handlerClassName);
        pathElementProcessor.process(exchange);

        Mockito.verify(headers, Mockito.times(1)).get(handlerClassName);
        Mockito.verify(outMessage, Mockito.times(1)).setHeaders(headers);
    }

    @Test
    public void testProcessHeadersNotEmpty() throws IllegalAccessException {

        final String handlerClassName = TestEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);
        Mockito.when(inMessage.getHeaders()).thenReturn(headers);
        final Map<String, Object> handlerHeaders = new HashMap<String, Object>();
        handlerHeaders.put("fdn", "test=123");

        Mockito.when(headers.get(handlerClassName)).thenReturn(handlerHeaders);
        final EventInputHandler flowProcessor = new TestEventInputHandler();
        pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor, handlerClassName);
        pathElementProcessor.process(exchange);

        Mockito.verify(headers, Mockito.times(1)).get(handlerClassName);
        Mockito.verify(outMessage, Mockito.times(1)).setHeaders(headers);
    }

    @Test
    public void testProcessHeadersNotEmpty_ResultEventInput() throws IllegalAccessException {

        final String handlerClassName = TestResultEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);
        Mockito.when(inMessage.getHeaders()).thenReturn(headers);
        final Map<String, Object> handlerHeaders = new HashMap<String, Object>();
        handlerHeaders.put("fdn", "test=123");

        Mockito.when(headers.get(handlerClassName)).thenReturn(handlerHeaders);
        final ResultEventInputHandler flowProcessor = new TestResultEventInputHandler();
        pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor, handlerClassName);
        pathElementProcessor.process(exchange);

        Mockito.verify(headers, Mockito.times(1)).get(handlerClassName);
        Mockito.verify(outMessage, Mockito.times(1)).setHeaders(headers);
    }

    @Test(expected = ClassCastException.class)
    public void testProcessHeadersNotAMap() throws IllegalAccessException {

        final String handlerClassName = TestEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);
        Mockito.when(inMessage.getHeaders()).thenReturn(headers);

        Mockito.when(headers.get(handlerClassName)).thenReturn(new ArrayList<>());
        final EventInputHandler flowProcessor = new TestEventInputHandler();
        pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor, handlerClassName);
        pathElementProcessor.process(exchange);
    }

    /**
     * The Class TestEventInputHandler.
     */
    @EventHandler(contextName = "test")
    public class TestEventInputHandler implements EventInputHandler {

        private static final long serialVersionUID = 1L;

        @Override
        public void init(final EventHandlerContext object) {}

        @Override
        public void destroy() {}

        @Override
        public void onEvent(final Object object) {}

        @Override
        public Object onEventWithResults(final Object inputEvent) {
            return null;
        }
    }

    /**
     * The Class TestResultEventInputHandler.
     */
    @EventHandler(contextName = "test")
    public class TestResultEventInputHandler implements ResultEventInputHandler {

        private static final long serialVersionUID = 1L;

        @Override
        public void init(final EventHandlerContext object) {}

        @Override
        public void destroy() {}

        @Override
        public void onEvent(final Object object) {}

        @Override
        public Object onEventWithResult(final Object inputEvent) {
            return null;
        }

        @Override
        public Object onEventWithResults(final Object inputEvent) {
            return null;
        }
    }
}
