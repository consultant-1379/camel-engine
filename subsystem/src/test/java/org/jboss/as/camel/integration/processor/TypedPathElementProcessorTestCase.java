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

package org.jboss.as.camel.integration.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.oss.itpf.common.event.ComponentEvent;
import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.TypedEventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * The Class TypedPathElementProcessorTestCase.
 */
@RunWith(PowerMockRunner.class)
public class TypedPathElementProcessorTestCase {

    @Mock
    private Exchange exchange;

    @Mock
    private Message inMessage;

    @Mock
    private Message outMessage;

    private TypedPathElementProcessor<TypedEventInputHandler> typedPathElementProcessor;

    @Mock
    private Map<String, Object> headers;

    @Test(expected = InstantiationException.class)
    public void testTypedPathProcessorDeprecated() throws Exception {

        final String handlerClassName = TestTypedEventInputHandler.class.getName();
        typedPathElementProcessor = new TypedPathElementProcessor(TestTypedEventInputHandler.class, handlerClassName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTypedPathProcessorNull() throws Exception {

        final String handlerClassName = TestTypedEventInputHandler.class.getName();
        final TypedEventInputHandler flowProcessor = null;
        typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(flowProcessor, handlerClassName);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testProcessHeadersEmpty() throws IllegalAccessException, InstantiationException {

        final String handlerClassName = TestTypedEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);
        Mockito.when(inMessage.getHeaders()).thenReturn(headers);

        final TypedEventInputHandler flowProcessor = new TestTypedEventInputHandler();
        typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(flowProcessor, handlerClassName);
        typedPathElementProcessor.process(exchange);

        Mockito.verify(inMessage, Mockito.times(1)).setBody(Matchers.any());
        Mockito.verify(inMessage, Mockito.times(1)).setHeaders(Matchers.anyMap());
    }

    @Test
    public void testProcessHeadersNotEmpty() throws IllegalAccessException, InstantiationException {

        final String handlerClassName = TestTypedEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);

        final Map<String, Object> handlerHeaders = new HashMap<String, Object>();
        handlerHeaders.put("fdn", "test=123");

        Mockito.when(inMessage.getHeaders()).thenReturn(handlerHeaders);
        final TypedEventInputHandler flowProcessor = new TestTypedEventInputHandler();
        typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(flowProcessor, handlerClassName);
        typedPathElementProcessor.process(exchange);

        Mockito.verify(inMessage, Mockito.times(1)).setBody(Matchers.any());
        Mockito.verify(inMessage, Mockito.times(1)).setHeaders(Matchers.anyMap());
    }

    @Test
    public void testProcessHeadersComponentBody() throws IllegalAccessException, InstantiationException {

        final String handlerClassName = TestTypedEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);

        final Map<String, Object> handlerHeaders = new HashMap<String, Object>();
        handlerHeaders.put("fdn", "test=123");
        handlerHeaders.put("mykey", "myValue");

        Mockito.when(inMessage.getHeaders()).thenReturn(handlerHeaders);
        Mockito.when(inMessage.getBody()).thenReturn(new ComponentEvent() {

            @Override
            public String getVersion() {
                return null;
            }

            @Override
            public Object getPayload() {
                return null;
            }

            @Override
            public String getNamespace() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Map<String, Object> getHeaders() {
                return null;
            }

            @Override
            public long eventTypeId() {
                return 0;
            }
        });
        final TypedEventInputHandler flowProcessor = new TestTypedEventInputHandler();
        typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(flowProcessor, handlerClassName);
        typedPathElementProcessor.process(exchange);

        Mockito.verify(inMessage, Mockito.times(1)).setBody(Matchers.any());
        Mockito.verify(inMessage, Mockito.times(1)).setHeaders(Matchers.anyMap());
    }

    @Test
    public void testProcessHeadersContainsHandlerClassKey() throws IllegalAccessException, InstantiationException {

        final String handlerClassName = TestTypedEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);

        final Map<String, Object> handlerHeaders = new HashMap<String, Object>();
        handlerHeaders.put("fdn", "test=123");
        handlerHeaders.put(handlerClassName, new HashMap<>());

        Mockito.when(inMessage.getHeaders()).thenReturn(handlerHeaders);

        final TypedEventInputHandler flowProcessor = new TestTypedEventInputHandler();
        typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(flowProcessor, handlerClassName);
        typedPathElementProcessor.process(exchange);

        Mockito.verify(inMessage, Mockito.times(1)).setBody(Matchers.any());
        Mockito.verify(inMessage, Mockito.times(1)).setHeaders(Matchers.anyMap());
    }

    @Test
    public void testProcessHeadersProperyContainsClassName() throws IllegalAccessException, InstantiationException {

        final String handlerClassName = TestTypedEventInputHandler.class.getName();
        Mockito.when(exchange.getIn()).thenReturn(inMessage);
        Mockito.when(exchange.getOut()).thenReturn(outMessage);

        final Map<String, Object> handlerHeaders = new HashMap<String, Object>();
        handlerHeaders.put("fdn", "test=123");
        handlerHeaders.put(handlerClassName + "/2", new HashMap<>());

        Mockito.when(inMessage.getHeaders()).thenReturn(handlerHeaders);

        final TypedEventInputHandler flowProcessor = new TestTypedEventInputHandler();
        typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(flowProcessor, handlerClassName);
        typedPathElementProcessor.process(exchange);

        Mockito.verify(inMessage, Mockito.times(1)).setBody(Matchers.any());
        Mockito.verify(inMessage, Mockito.times(1)).setHeaders(Matchers.anyMap());
    }

    /**
     * The Class TestTypedEventInputHandler.
     */
    @EventHandler(contextName = "test")
    public class TestTypedEventInputHandler implements TypedEventInputHandler {

        private static final long serialVersionUID = 1L;

        @Override
        public void init(final EventHandlerContext context) {}

        @Override
        public void destroy() {}

        @Override
        public ComponentEvent onEvent(final ComponentEvent componentEvent) {
            return new ComponentEvent() {

                @Override
                public String getVersion() {
                    return null;
                }

                @Override
                public Object getPayload() {
                    return null;
                }

                @Override
                public String getNamespace() {
                    return null;
                }

                @Override
                public String getName() {
                    return null;
                }

                @Override
                public Map<String, Object> getHeaders() {
                    return null;
                }

                @Override
                public long eventTypeId() {
                    return 0;
                }
            };
        }
    }
}
