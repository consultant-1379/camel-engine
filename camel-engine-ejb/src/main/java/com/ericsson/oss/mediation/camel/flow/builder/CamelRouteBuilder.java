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

package com.ericsson.oss.mediation.camel.flow.builder;

import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.JMS_PREFIX;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.PROCESS_METHOD_NAME;

import java.util.Iterator;
import java.util.List;

import javax.resource.ResourceException;

import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.TransactedDefinition;
import org.jboss.camel.exception.CamelEngineRuntimeException;
import org.jboss.camel.tx.TransactionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Choice;
import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Option;
import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Step;
import com.ericsson.oss.mediation.camel.flow.util.JmsAdapterConfiguration;
import com.ericsson.oss.mediation.flow.FlowAdapter;
import com.ericsson.oss.mediation.flow.FlowPath;
import com.ericsson.oss.mediation.flow.FlowPathElement;
import com.ericsson.oss.mediation.flow.FlowProcessor;

/**
 * This class is a Custom implementation of Camels RouteBuilder abstract class. It is responsible for building the Camel based route from the
 * <code>com.ericsson.oss.mediation.flow.FlowPathElements</code> of the supplied <code>com.ericsson.oss.mediation.flow.FlowPath</code>. The Camel
 * route is constructed using Camels Java based DSL.
 */
public class CamelRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelRouteBuilder.class);

    private final FlowPath flowPath;
    private final JmsAdapterConfiguration jmsConfig;

    /**
     * CamelRouteBuilder constructor that requires flow path to create RouteDefinition based on it.
     *
     * @param flowPath
     *            Flow definition for the camel route.
     */
    public CamelRouteBuilder(final FlowPath flowPath) {
        super();
        this.flowPath = flowPath;
        jmsConfig = new JmsAdapterConfiguration();
    }

    @Override
    public void configure() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Configure method invoked on CamelRouteBuilder for: {}", flowPath.getId());
        }
        try {
            final Iterator<FlowPathElement> elementIterator = flowPath.getPathElements().iterator();
            if (!elementIterator.hasNext()) {
                throw new ResourceException("Unable to create camel route, path element list is empty.");
            }
            final RouteDefinition route = buildRouteDefinition(elementIterator);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Route created in CamelBuilder: {}", route.getId());
            }
        } catch (final Exception e) {
            LOGGER.error("Exception caught while trying to construct camel route, stack trace is {}", e);
            throw new CamelEngineRuntimeException(e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Construction of camel route complete, [{}]", toString());
        }
    }

    /**
     * Creates the route definition for elements of the flow.
     *
     * @param pathElementIterator
     *            the path element iterator.
     * @return RouteDefinition
     */
    private RouteDefinition buildRouteDefinition(final Iterator<FlowPathElement> pathElementIterator) {
        final FlowPathElement from = pathElementIterator.next();
        String fromString = from.getURI();
        if (fromString.toLowerCase().startsWith(JMS_PREFIX)) {
            fromString = jmsConfig.resolveConfigParametersForEndPoint(from);
        }
        final RouteDefinition routeDef = from(fromString);
        routeDef.setId(flowPath.getId());
        // Is flow transactional or not?
        /*
         * This whole concept needs to evolve more, at the moment flows are either transactional or not, where we might need more ie: (no tx)
         * A->B->C->(TX_START(D->E->F))->(TX_START_NEW(G->I->J))
         */
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Flow {} is marked as transactional: {}", new Object[] { flowPath.getId(), flowPath.isTransactional() });
        }
        if (flowPath.isTransactional()) {
            final TransactedDefinition policyDef = routeDef.transacted(TransactionPolicy.PROPAGATION_REQUIRED);
            populateRouteElements(pathElementIterator, policyDef);
        } else {
            populateRouteElements(pathElementIterator, routeDef);
        }

        return routeDef;
    }

    /**
     * Resolves rest of the flow after the from element is finished.
     *
     * @param <T>
     *            the generic type.
     * @param pathElementIterator
     *            the path element iterator.
     * @param policyDef
     *            the policy def.
     */
    private <T extends ProcessorDefinition<?>> void populateRouteElements(final Iterator<FlowPathElement> pathElementIterator, final T policyDef) {
        while (pathElementIterator.hasNext()) {
            final FlowPathElement element = pathElementIterator.next();
            if (element instanceof FlowProcessor) {
                policyDef.beanRef(element.getClassName(), PROCESS_METHOD_NAME, false);
            } else if (element instanceof FlowAdapter) {
                policyDef.to(element.getURI());
            } else if (element instanceof Choice) {
                final List<Option> options = ((Choice) element).getOptions();
                for (final Option option : options) {
                    final String thisWhenString = option.getExpression().getExpression();
                    final String thisChoiceHandlerName = ((Step) option.getPathElement()).getHandler().getClassName();
                    final String separator = "=";
                    if (thisWhenString.contains(separator)) {
                        final String thisLeft = thisWhenString.split(separator)[0];
                        final String thisRight = thisWhenString.split(separator)[1];
                        final Predicate predicate = header(thisLeft).isEqualTo(thisRight);
                        final String message = "Header with key: " + thisLeft
                                + " does not exist or it's value does not match to the value specified in the flow model which is " + thisRight
                                + " for the Handler: " + thisChoiceHandlerName;
                        policyDef.choice().when(predicate).to(thisChoiceHandlerName).otherwise().log(message);
                    } else {
                        LOGGER.info("Choice Element when expression: {} ,was not written correctly, did not contain equals sign ", thisWhenString);
                    }
                }
            }
        }
    }
}
