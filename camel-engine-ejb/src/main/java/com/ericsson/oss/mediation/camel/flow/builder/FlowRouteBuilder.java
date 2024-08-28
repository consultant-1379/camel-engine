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

package com.ericsson.oss.mediation.camel.flow.builder;

import javax.ejb.Stateless;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.flow.FlowPath;

/**
 * Stateless bean that returns implementation of Camel Route Builder based on Flow.
 * Path provided.
 */
@Stateless
public class FlowRouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowRouteBuilder.class);

    /**
     * Build camel route using the supplied flow definition.
     *
     * @param flowPath
     *            Flow definition for the camel route.
     * @return RouteBuilder that can be used in camel context
     */
    public RouteBuilder buildCamelRoute(final FlowPath flowPath) {
        LOGGER.trace("Building camel route for: {}", flowPath.getId());
        final RouteBuilder routeBuilder = new CamelRouteBuilder(flowPath);
        return routeBuilder;
    }
}
