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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.ComponentEvent;
import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.TypedEventInputHandler;
import com.ericsson.oss.mediation.flow.events.MediationComponentEvent;

/**
 * TypedPathElementProcessor implementation.
 *
 * @param <T>
 *            the generic type
 */
public class TypedPathElementProcessor<T extends TypedEventInputHandler> implements Processor {

    private static final String EXCHANGE_OUT_HEADERS_ARE_NOW = "Exchange OUT headers are now: {}";
    private static final String EXCHANGE_IN_HEADERS_ARE_NOW = "Exchange IN headers are now: {}";
    private static final String CREATED_PROCESSOR = "Created Camel Processor for: {}";
    private static final Logger LOGGER = LoggerFactory.getLogger(TypedPathElementProcessor.class);
    private final T flowProcessor;
    private final EngineConfiguration configuration;
    private final String handlerClassName;

    /**
     * Full arg constructor that will create a new TypedPathElementProcessor using manuall instantiation of clazz<br/> This constructor is depricated,
     * as instance of the handler will be provided by CDI.
     *
     * @param clazz
     *            Class to create.
     * @param handlerClassName
     *            Class to instantiate.
     * @throws InstantiationException
     *             if the class cannot be created.
     * @throws IllegalAccessException
     *             if security permissions are missing.
     * @deprecated this method is no longer needed.
     */
    @Deprecated
    public TypedPathElementProcessor(final Class<T> clazz, final String handlerClassName) throws InstantiationException, IllegalAccessException {
        this.flowProcessor = clazz.newInstance();
        this.handlerClassName = handlerClassName;
        configuration = new EngineConfiguration();
        LOGGER.trace(CREATED_PROCESSOR, this.handlerClassName);
    }

    /**
     * Full arg constructor that will create instance of TypedPathElementProcessor using CDI provided instance of handler.
     *
     * @param instance
     *            Handler instance.
     * @param handlerClassName
     *            Class to instantiate.
     * @throws InstantiationException
     *             if the class cannot be created.
     * @throws IllegalAccessException
     *             if security permissions are missing.
     */
    public TypedPathElementProcessor(final T instance, final String handlerClassName) throws InstantiationException, IllegalAccessException {
        if (instance == null) {
            throw new IllegalArgumentException("Unable to create TypedPathElementProcessor with null instance");
        }
        this.flowProcessor = instance;
        this.handlerClassName = handlerClassName;
        configuration = new EngineConfiguration();
        LOGGER.trace(CREATED_PROCESSOR, this.handlerClassName);
    }

    @Override
    public void process(final Exchange exchange) {
        LOGGER.trace("Exchange IN headers: {}", exchange.getIn().getHeaders());
        LOGGER.trace("Exchange OUT headers: {}", exchange.hasOut() ? exchange.getOut().getHeaders() : "THERE IS NO OUT MSG");
        Map<String, Object> headers = new HashMap<String, Object>();
        headers = getFlowProcessorSpecificHeaders(exchange);
        setConfigurationProperties(headers);
        final EventHandlerContext ctx = new EngineEventHandlerContext(configuration);
        flowProcessor.init(ctx);
        LOGGER.trace("Processing FlowProcessor element:{}", this.handlerClassName);
        final Object body = exchange.getIn().getBody();
        handleMultipleInheritance(body);
        if (body == null || !(body instanceof ComponentEvent)) {
            final ComponentEvent event = new MediationComponentEvent(exchange.getIn().getHeaders(), exchange.getIn().getBody());
            final ComponentEvent result = flowProcessor.onEvent(event);
            exchange.getIn().setBody(result);
            exchange.getIn().setHeaders(result.getHeaders());
            logExchangeHeaders(exchange);
        } else {
            final ComponentEvent event = (ComponentEvent) body;
            final ComponentEvent result = flowProcessor.onEvent(event);
            exchange.getIn().setBody(result);
            exchange.getIn().setHeaders(result.getHeaders());
            logExchangeHeaders(exchange);
        }
    }

    /**
     * Log exchange headers.
     *
     * @param exchange
     *            the exchange
     */
    private void logExchangeHeaders(final Exchange exchange) {
        LOGGER.trace(EXCHANGE_IN_HEADERS_ARE_NOW, exchange.getIn().getHeaders());
        LOGGER.trace(EXCHANGE_OUT_HEADERS_ARE_NOW, exchange.hasOut() ? exchange.getOut().getHeaders() : "THERE IS NO OUT MSG.");
    }

    /**
     * Handle multiple inheritance.
     *
     * @param body
     *            the body
     */
    private void handleMultipleInheritance(final Object body) {
        if (EventInputHandler.class.isAssignableFrom(flowProcessor.getClass())) {
            final EventInputHandler handler = (EventInputHandler) flowProcessor;
            handler.onEvent(body);
        }
    }

    /**
     * Return flow processor specific headers, and removes them from overall headers.
     *
     * @param exchange
     *            Exchange to process.
     * @return Map constaining the headers
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getFlowProcessorSpecificHeaders(final Exchange exchange) {
        Map<String, Object> result = new HashMap<>();
        final Map<String, Object> headers = exchange.getIn().getHeaders();
        // Check if there is a map of attributes provided for this class
        LOGGER.trace("Checking if map contains any key that is matching classname:{}", this.handlerClassName);
        if (headers.containsKey(this.handlerClassName)) {
            LOGGER.trace("Map contains value with this key, returning its attributes");
            result = (Map<String, Object>) headers.get(this.handlerClassName);
            headers.remove(this.handlerClassName);
        } else {
            // Go through the map and check is there key that starts with classname.
            // there could be multiples entries in this map with same class name and additional suffix
            LOGGER.trace("Exchange.getIn().getHeaders() does not contain value with the key {}, returning its attributes", this.handlerClassName);
            LOGGER.trace("Will iterate through the Map to find key that starts with classname: {}", this.handlerClassName);
            final Integer min = getSequenceNumber(headers);
            if (min < Integer.MAX_VALUE) {
                final String key = this.handlerClassName + "/" + min;
                result = (Map<String, Object>) headers.get(key);
                headers.remove(key);
            }
        }
        LOGGER.trace("getFLowProcessorSpecificHeaders is returing:{}", result);
        return result;
    }

    /**
     * <p>
     * <b>Added for TORF-15630</b>
     * </p>
     * <p>
     * This method returns the sequence number of a handler which has been reused multiple times in a flow.
     * </p>
     * <p>
     * Consider a handler MyHandler which is used several times in a flow:
     * </p>
     * <pre> {@code
     * <path>
     *  <step>MyHandler</step>
     *  <step>HandlerA</step>
     *  <step>MyHandler</step>
     *  <step>HandlerB</step>
     * </path>
     * } </pre>
     * <p>
     * The first occurrence of MyHandler has a sequence number of 1, the second occurrence has a sequence number of 2 and so on. In the camel headers
     * the handler is stored with its sequence number appended after a / i.e. com.ericsson.MyHandler/1 , com.ericsson.MyHandler/2. This method
     * extracts the sequence number from this key value.
     * </p>
     *
     * @param headers
     *            the headers
     * @return the sequence number of the handler
     */
    private Integer getSequenceNumber(final Map<String, Object> headers) {
        Integer min = Integer.MAX_VALUE;
        for (final Entry<String, Object> entry : headers.entrySet()) {
            final String originalKey = entry.getKey();
            if (originalKey.startsWith(this.handlerClassName)) {
                final String sequence = originalKey.substring(originalKey.lastIndexOf('/') + 1, originalKey.length());
                final Integer seqNumber = new Integer(sequence);
                if (min > seqNumber) {
                    min = seqNumber;
                }
            }
        }
        return min;
    }

    /*
     * @param headers Map of attribute names and values specific to <code>FlowProcessor</code> class T
     */
    private void setConfigurationProperties(final Map<String, Object> headers) {
        LOGGER.trace("Setting headers for:{}", this.handlerClassName);
        for (final Map.Entry<String, Object> entry : headers.entrySet()) {
            configuration.setProperty(entry.getKey(), entry.getValue());
        }
        LOGGER.trace("EngineConfiguration.getProperties()->{}", configuration.getAllProperties());
    }
}
