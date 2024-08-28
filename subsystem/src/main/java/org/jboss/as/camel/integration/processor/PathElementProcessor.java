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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.jboss.camel.constant.GenericConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.ResultEventInputHandler;

/**
 * Generic class which implements <code>org.apache.camel.Processor</code>. Responsible for implementing the <code>com.ericsson.oss.mediation.flow
 * .FlowProcessor</code> elements in a <code>com.ericsson.oss.mediation
 * .FlowPath</code> as a Camel processor component.
 *
 * @param <T>
 *            subclass of EventInputHandler.
 */
public class PathElementProcessor<T extends EventInputHandler> implements Processor {

    private static final String HANDLER_USING_PREVIOUS_INTERFACE = "Handler {} is not prepared to return results. "
            + "Still implementing the previous interface.";
    private static final String CREATED_PROCESSOR = "Created Camel Processor for: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(PathElementProcessor.class);
    private final T flowProcessor;
    private final String handlerClassName;

    /**
     * Full arg constructor that will create new instance of event handler<br> This should be removed in the next version, as CDI will provide
     * instance of the handler class.
     *
     * @param clazz
     *            flowProcessor class to be instantiated.
     * @param handlerClassName
     *            Fully qualified class name of the handler.
     * @throws InstantiationException
     *             In case we can't instantiate this class.
     * @throws IllegalAccessException
     *             In case default constructor is not visible.
     * @deprecated this will be removed.
     */
    @Deprecated
    public PathElementProcessor(final Class<T> clazz, final String handlerClassName) throws InstantiationException, IllegalAccessException {
        this.flowProcessor = clazz.newInstance();
        this.handlerClassName = handlerClassName;
        LOGGER.trace(CREATED_PROCESSOR, this.handlerClassName);
    }

    /**
     * Full arg constructor that will create instance of PathElementProcessor using CDI supplied instance of handler.
     *
     * @param instance
     *            instance to be used.
     * @param handlerClassName
     *            Fully qualified class name of the handler.
     * @throws IllegalAccessException
     *             In case default constructor is not visible.
     */
    public PathElementProcessor(final T instance, final String handlerClassName) throws IllegalAccessException {
        if (instance == null) {
            throw new IllegalArgumentException("Unabled to create PathElementProcessor with null instance.");
        }
        this.flowProcessor = instance;
        this.handlerClassName = handlerClassName;
        LOGGER.trace(CREATED_PROCESSOR, this.handlerClassName);
    }

    @Override
    public void process(final Exchange exchange) {
        Map<String, Object> headers = new HashMap<String, Object>();
        LOGGER.trace("Processing FlowProcessor element:{}", this.handlerClassName);
        headers = getFlowProcessorSpecificHeaders(exchange);
        final EngineConfiguration engineConfiguration = getConfigurationProperties(headers);
        final EventHandlerContext ctx = new EngineEventHandlerContext(engineConfiguration);
        flowProcessor.init(ctx);
        Object result = null;
        final Method[] methods = flowProcessor.getClass().getDeclaredMethods();
        boolean depreciatedMethodFound = false;
        boolean methodFound = false;
        for (final Method foundMethod : methods) {
            if (GenericConstant.METHOD_WITH_RETURN_VALUE.equalsIgnoreCase(foundMethod.getName())) {
                methodFound = true;
                break;
            } else if (GenericConstant.DEPRECIATED_METHOD_WITH_RETURN_VALUE.equalsIgnoreCase(foundMethod.getName())) {
                depreciatedMethodFound = true;
            }
        }
        // TODO This will be left for a couple of sprints to let other teams update their handlers.
        if (methodFound) {
            result = ((ResultEventInputHandler) flowProcessor).onEventWithResult(null);
        } else if (depreciatedMethodFound) {
            result = flowProcessor.onEventWithResults(null);
        }  else {
            LOGGER.warn(HANDLER_USING_PREVIOUS_INTERFACE, this.handlerClassName);
            flowProcessor.onEvent(null);
        }
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
        exchange.getOut().setBody(result, Object.class);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.debug("Got body: [{}] and result: {}", new Object[] { exchange.getIn().getHeaders(), result });
        }
    }

    /**
     * Return flow processor specific headers.
     *
     * @param exchange
     *            Exchange to process.
     * @return Map constaining the headers
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFlowProcessorSpecificHeaders(final Exchange exchange) {
        final Object obj = exchange.getIn().getHeaders().get(this.handlerClassName);
        if (obj == null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("No headers for key:{} have been found in the exchange.Will return empty HashMap<String,Object>", this.handlerClassName);
            }
            return new HashMap<String, Object>();
        }
        if (obj instanceof HashMap<?, ?>) {
            final HashMap<String, Object> processorHeaders = (HashMap<String, Object>) obj;
            return processorHeaders;
        } else {
            throw new ClassCastException("Expected instance of HashMap<String,Object> under header key:" + this.handlerClassName
                    + ", but instead found " + obj.getClass().getCanonicalName());
        }
    }

    private EngineConfiguration getConfigurationProperties(final Map<String, Object> headers) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting headers {} for handler {}", headers == null ? "NULL" : headers, this.handlerClassName);
        }
        final EngineConfiguration configuration = new EngineConfiguration();
        for (final Map.Entry<String, Object> entry : headers.entrySet()) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Setting propertcy with key {} and value={}", entry.getKey(), entry.getValue());
            }
            configuration.setProperty(entry.getKey(), entry.getValue());
        }
        return configuration;
    }
}
