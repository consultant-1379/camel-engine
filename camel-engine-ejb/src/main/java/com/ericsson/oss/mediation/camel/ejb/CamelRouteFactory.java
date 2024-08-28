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

package com.ericsson.oss.mediation.camel.ejb;

import java.util.Collections;
import java.util.List;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.camel.CamelContext;
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.camel.exception.CamelEngineRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.camel.flow.builder.FlowRouteBuilder;
import com.ericsson.oss.mediation.flow.FlowPath;

/**
 * Singleton responsible for route creation.
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class CamelRouteFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelRouteFactory.class);
    private static final String ROUTE_EXISTS = "Route {} already exists";

    @CamelContextService
    private CamelContext camelContext;

    @EJB
    private FlowRouteBuilder routeBuilder;

    /**
     * Create route(s) based on give flow components.
     *
     * @param flowPaths
     *            Flows that will be built as camel routes.
     */
    public synchronized void createFlow(final List<FlowPath> flowPaths) {
        if (flowPaths == null) {
            throw new IllegalStateException("Flow definition can not be null");
        }

        if (flowPaths.isEmpty()) {
            throw new IllegalStateException("Flow definition can not be empty");
        }
        Collections.reverse(flowPaths);
        for (final FlowPath path : flowPaths) {
            final String routeId = path.getId();
            if (camelContext.getRoute(routeId) == null) {
                createRoute(path, routeId);
            } else {
                LOGGER.debug(ROUTE_EXISTS, routeId);
            }
        }
    }

    /**
     * Creates a route in the CamelContext in a thread safe way for each route.
     *
     * @param path
     *            The FlowPath to build the route.
     * @param routeId
     *            The ID of the route.
     */
    private void createRoute(final FlowPath path, final String routeId) {
        addRouteToCamelContext(path, routeId);
    }

    /**
     * Adds a route to the CamelContext. It is up to the designer to ensure this is done in a thread safe way.
     *
     * @param path
     *            The FlowPath to build the route.
     * @param routeId
     *            The ID of the route.
     */
    private void addRouteToCamelContext(final FlowPath path, final String routeId) {
        if (camelContext.getRoute(routeId) == null) {
            LOGGER.debug("Route {} doesn't exist, creating new camel route.", routeId);
            try {
                camelContext.addRoutes(routeBuilder.buildCamelRoute(path));
            } catch (final Exception e) {
                throw new CamelEngineRuntimeException(e.getMessage());
            }
            LOGGER.debug("Added new route with id {}", routeId);
        } else {
            LOGGER.debug(ROUTE_EXISTS, routeId);
        }
    }
}
