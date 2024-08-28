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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Producer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.jboss.camel.annotations.CamelContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SomeAppClientImpl.
 */
@Stateless
public class SomeAppClientImpl implements SomeAppClient {

    private static final String PROPAGATION_REQUIRED = "PROPAGATION_REQUIRED";
    private static final String DIRECT = "direct:/";
    private static final String PROCESS_METHOD = "process";
    private static final long serialVersionUID = 7977285921545665045L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SomeAppClientImpl.class);

    @CamelContextService
    private CamelContext ctx;
    private ProducerTemplate createProducerTemplate;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void createRouteWithEventHandlerProcessor(final String routeName) throws Exception {
        LOGGER.trace("createRouteWithEventHandlerProcessor");
        final RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final RouteDefinition routeDef = from(DIRECT + routeName);
                routeDef.setId(routeName);
                routeDef.transacted(PROPAGATION_REQUIRED).beanRef(TestConstants.SOME_EVENT_HANDLER_FQCN, PROCESS_METHOD, false);
            }
        };

        ctx.addRoutes(routeBuilder);
        ctx.startRoute(routeName);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void applyInput(final String routeName, final Map<String, Object> headers) throws Exception {
        LOGGER.trace("applyInput on route {} with headers: {}", routeName, headers);
        final Route route = ctx.getRoute(routeName);
        final Producer producer = route.getEndpoint().createProducer();
        final Exchange exchange = producer.createExchange(ExchangePattern.InOut);
        exchange.getIn().setHeaders(headers);
        producer.process(exchange);
        LOGGER.error("invokeFlow method invocation ended.");
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public Object applyInputWithResults(final String routeName, final Map<String, Object> headers) throws Exception {
        LOGGER.trace("applyInput on route {} with headers: {}", routeName, headers);
        final Route route = ctx.getRoute(routeName);
        final Endpoint endpoint = route.getEndpoint();
        final Object result = createProducerTemplate.requestBodyAndHeaders(endpoint, null, headers);
        LOGGER.error("invokeFlow method invocation ended... return type was {}", result);
        return result;
    }

    @Override
    public void createRouteWithTypedEventHandlerProcessor(final String routeName) throws Exception {
        LOGGER.trace("createRouteWithTypedEventHandlerProcessor with [{}] ", routeName);
        if (createProducerTemplate == null) {
            createProducerTemplate = ctx.createProducerTemplate();
        }

        final RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final RouteDefinition routeDef = from(DIRECT + routeName);
                routeDef.setId(routeName);
                routeDef.transacted(PROPAGATION_REQUIRED).beanRef(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, PROCESS_METHOD, false)
                        .beanRef(TestConstants.SOME_TYPED_EVENT_HANDLER2_FQCN, PROCESS_METHOD, false);
            }
        };
        ctx.addRoutes(routeBuilder);
        ctx.startRoute(routeName);
    }

    @Override
    public void stopRoute(final String routeName) throws Exception {
        LOGGER.trace("stopping route {}", routeName);
        ctx.stopRoute(routeName);
        ctx.removeRoute(routeName);
    }
}
