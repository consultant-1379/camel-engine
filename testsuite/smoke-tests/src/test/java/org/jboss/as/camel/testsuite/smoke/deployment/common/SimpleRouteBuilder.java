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

package org.jboss.as.camel.testsuite.smoke.deployment.common;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.Route;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.camel.tx.TransactionPolicy;

/**
 * The Class SimpleRouteBuilder.
 */
@Stateless
public class SimpleRouteBuilder {

    public static final String NO_TX_EXCHANGE_BODY = "AND GOD SAID, LET THERE BE NO TX";
    public static final String CORE_COMPONENT_TEST_MSG_BODY = "Hello World";
    public static final String ROUTE_FOUR = "routeFour";
    public static final String ROUTE_THREE = "routeThree";
    public static final String ROUTE_TWO = "routeTwo";
    public static final String ROUTE_ONE = "routeOne";
    public static final String TX_SYNC_REG_JNDI_NAME = "java:comp/TransactionSynchronizationRegistry";
    public static final String CORE_COMPONENT_TEST_RESULT_FILENAME = "test.result";
    public static final String TX_REQUIRED_TEST_RESULT_FILENAME = "tx_test.result";
    public static final String TX_REQUIRED_TEST_NO_TX_RESULT_FILENAME = "no_tx_test.result";
    public static final String SINGLETON_BEAN =
            "java:global/test-camel-core-components/LatchSingleton!org.jboss.as.camel.testsuite.smoke.deployment.common.LatchSingleton";
    public static final String METHOD_NAME = "processMessage";

    @CamelContextService
    private CamelContext camelContext;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void testFileComponent() throws Exception {
        final RouteBuilder routeBuilder = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("direct:/" + ROUTE_ONE).process(new Processor() {

                    @Override
                    public void process(final Exchange exchange) throws Exception {
                        final String body = exchange.getIn().getBody(String.class);
                        exchange.getOut().setBody(body);
                    }
                }).to("file:target?fileName=" + CORE_COMPONENT_TEST_RESULT_FILENAME).setId(ROUTE_ONE);
            }
        };
        camelContext.addRoutes(routeBuilder);
        final Route route = camelContext.getRoute(ROUTE_ONE);
        camelContext.startRoute(ROUTE_ONE);
        final Producer producer = route.getEndpoint().createProducer();
        final Exchange exchange = producer.createExchange();
        exchange.getIn().setBody(CORE_COMPONENT_TEST_MSG_BODY);
        exchange.getOut().setBody(CORE_COMPONENT_TEST_MSG_BODY);
        producer.process(exchange);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String testTransactedRouteRequired() throws Exception {

        final InitialContext ctx = new InitialContext();
        final TransactionSynchronizationRegistry txSyncReg = (TransactionSynchronizationRegistry) ctx.lookup(TX_SYNC_REG_JNDI_NAME);
        final Object txKey = txSyncReg.getTransactionKey();
        final RouteBuilder routeBuilder = new RouteBuilder() {

            @Override
            public void configure() throws Exception {

                final RouteDefinition routeDef = from("direct:/" + ROUTE_TWO);
                routeDef.setId(ROUTE_TWO);
                routeDef.transacted(TransactionPolicy.PROPAGATION_REQUIRED).process(new Processor() {

                    @Override
                    public void process(final Exchange exchange) throws Exception {
                        final InitialContext ctx = new InitialContext();
                        final TransactionSynchronizationRegistry txSyncReg = (TransactionSynchronizationRegistry) ctx.lookup(TX_SYNC_REG_JNDI_NAME);
                        final Object txKey = txSyncReg.getTransactionKey();
                        if (txKey != null) {
                            exchange.getIn().setBody(txKey.toString());
                            exchange.getOut().setBody(txKey.toString());
                        } else {
                            exchange.getIn().setBody("NULL");
                            exchange.getOut().setBody("NULL");
                        }
                    }
                }).to("file:target?fileName=" + TX_REQUIRED_TEST_RESULT_FILENAME);
            }
        };
        camelContext.addRoutes(routeBuilder);
        final Route route = camelContext.getRoute(ROUTE_TWO);
        camelContext.startRoute(ROUTE_TWO);
        final Producer producer = route.getEndpoint().createProducer();
        final Exchange exchange = producer.createExchange();
        exchange.getOut().setBody(txKey.toString());
        exchange.getIn().setBody(txKey.toString());
        producer.process(exchange);
        return txKey.toString();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void testTransactedRouteRequiredWhenNoTransaction() throws Exception {
        final RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                final RouteDefinition routeDef = from("direct:/" + ROUTE_THREE);
                routeDef.setId(ROUTE_THREE);
                routeDef.transacted(TransactionPolicy.PROPAGATION_REQUIRED).process(new Processor() {

                    @Override
                    public void process(final Exchange exchange) throws Exception {
                        final InitialContext ctx = new InitialContext();
                        final TransactionSynchronizationRegistry txSyncReg = (TransactionSynchronizationRegistry) ctx.lookup(TX_SYNC_REG_JNDI_NAME);
                        final Object txKey = txSyncReg.getTransactionKey();
                        if (txKey != null) {
                            exchange.getIn().setBody(txKey.toString());
                            exchange.getOut().setBody(txKey.toString());
                        } else {
                            exchange.getIn().setBody("NULL");
                            exchange.getOut().setBody("NULL");
                        }
                    }
                }).to("file:target?fileName=" + TX_REQUIRED_TEST_NO_TX_RESULT_FILENAME);
            }
        };
        camelContext.addRoutes(routeBuilder);
        final Route route = camelContext.getRoute(ROUTE_THREE);
        camelContext.startRoute(ROUTE_THREE);
        final Producer producer = route.getEndpoint().createProducer();
        final Exchange exchange = producer.createExchange();
        exchange.getOut().setBody(NO_TX_EXCHANGE_BODY);
        exchange.getIn().setBody(NO_TX_EXCHANGE_BODY);
        producer.process(exchange);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void testSendJmsTextMessage() throws Exception {
        final RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final RouteDefinition routeDef = from("direct:/" + ROUTE_FOUR);
                routeDef.setId(ROUTE_FOUR);
                routeDef.to("jms:queue:jmsTestQueue?jmsMessageType=Text");
            }
        };
        camelContext.addRoutes(routeBuilder);
        final Route route = camelContext.getRoute(ROUTE_FOUR);
        camelContext.startRoute(ROUTE_FOUR);
        final Producer producer = route.getEndpoint().createProducer();
        final Exchange exchange = producer.createExchange();
        exchange.getIn().setBody(CORE_COMPONENT_TEST_MSG_BODY);
        producer.process(exchange);
    }
}
