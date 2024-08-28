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

import static com.ericsson.oss.mediation.camel.flow.util.LoggingConstants.EXCEPTION_CAUGHT_WHILE_TRYING_TO_INVOKE_METHOD_ON_THE_PROXY;
import static com.ericsson.oss.mediation.camel.flow.util.LoggingConstants.MSG_INVOKE_FLOW_CALLED_FOR_ROUTE_WITH_HEADERS;
import static com.ericsson.oss.mediation.camel.flow.util.LoggingConstants.MSG_MS_UNDER_UPGRADE;
import static com.ericsson.oss.mediation.camel.flow.util.LoggingConstants.MSG_NON_TRANSACTIONAL_INVOCATION;
import static com.ericsson.oss.mediation.camel.flow.util.LoggingConstants.MSG_TRANSACTIONAL_INVOCATION;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Route;
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.camel.exception.CamelEngineRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.camel.flow.builder.FlowRouteBuilder;
import com.ericsson.oss.mediation.camel.upgrade.CamelEngingRuntimeState;
import com.ericsson.oss.mediation.engine.api.MediationEngine;
import com.ericsson.oss.mediation.engine.api.MediationEngineConstants;
import com.ericsson.oss.mediation.flow.FlowPath;

/**
 * Camel implementation of {@link MediationEngine}.
 */
@Stateless
@EJB(name = MediationEngineConstants.CAMEL_MEDIATION_ENGINE_JNDI, beanInterface = MediationEngine.class)
@Local(MediationEngine.class)
@SuppressWarnings("PMD")
public class CamelMediationEngineImpl implements MediationEngine {

    private static final long serialVersionUID = -3477963983337971550L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelMediationEngineImpl.class);

    @Inject
    CamelRouteFactory routeFactory;

    @Inject
    CamelRouteProducerTemplate producerTemplate;

    @CamelContextService
    private CamelContext camelContext;

    @EJB
    private FlowRouteBuilder routeBuilder;

    @EJB
    private CamelEngingRuntimeState runtimeState;

    @Override
    public void createFlow(final List<FlowPath> flowPaths) {
        routeFactory.createFlow(flowPaths);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void invokeFlow(final String flowId, final Map<String, Object> message) {
        LOGGER.trace(MSG_NON_TRANSACTIONAL_INVOCATION, flowId);
        internalInvokeFlow(flowId, message);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void invokeTransactionalFlow(final String flowId, final Map<String, Object> message) {
        LOGGER.trace(MSG_TRANSACTIONAL_INVOCATION, flowId);
        internalInvokeFlow(flowId, message);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Object invokeFlowWithResults(final String flowId, final Map<String, Object> message) {
        LOGGER.trace(MSG_NON_TRANSACTIONAL_INVOCATION, flowId);
        final Object results = internalInvokeFlowWithResults(flowId, message);
        return results;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Object invokeTransactionalFlowWithResults(final String flowId, final Map<String, Object> message) {
        LOGGER.trace(MSG_TRANSACTIONAL_INVOCATION, flowId);
        final Object results = internalInvokeFlowWithResults(flowId, message);
        return results;
    }

    /**
     * Utility method that will invoke the flow.
     *
     * @param flowId
     *            Identifier for this flow.
     * @param message
     *            Message to pass into the flow.
     */
    private void internalInvokeFlow(final String flowId, final Map<String, Object> message) {
        resolveHeadersIdAndLogInvokingFlow(message, flowId);
        try {
            final Route route = camelContext.getRoute(flowId);
            final Endpoint endpoint = route.getEndpoint();
            producerTemplate.getTemplate().sendBodyAndHeaders(endpoint, ExchangePattern.InOnly, null, message);
            LOGGER.trace("invokeFlow method invocation ended...");
        } catch (final Exception e) {
            logErrorAndThrowException(e);
        }
    }

    /**
     * Utility method that will invoke the flow and return results.
     *
     * @param flowId
     *            Identifier for this flow.
     * @param message
     *            Message to pass into the flow.
     * @return the object The expected result from the invocation.
     */
    private Object internalInvokeFlowWithResults(final String flowId, final Map<String, Object> message) {
        Object result = null;
        resolveHeadersIdAndLogInvokingFlow(message, flowId);
        try {
            final Route route = camelContext.getRoute(flowId);
            final Endpoint endpoint = route.getEndpoint();
            result = producerTemplate.getTemplate().requestBodyAndHeaders(endpoint, null, message);
            LOGGER.trace("invokeFlow method invocation ended... return type was {}", result);
        } catch (final Exception e) {
            logErrorAndThrowException(e);
        }
        return result;
    }

    /**
     * Resolve headers.
     *
     * @param headers
     *            To be resolved.
     * @param flowId
     *            To be logged.
     */
    private void resolveHeadersIdAndLogInvokingFlow(final Map<String, Object> headers, final String flowId) {
        final Map<String, Object> headersId = new LinkedHashMap<>();
        for (final Map.Entry<String, Object> entry : headers.entrySet()) {
            final String resolvedKey = removeKeyPrefix(entry.getKey());
            headersId.put(resolvedKey, entry.getValue());
        }
        LOGGER.trace(MSG_INVOKE_FLOW_CALLED_FOR_ROUTE_WITH_HEADERS, flowId, headersId);
    }

    /**
     * Remove key prefix.
     *
     * @param key
     *            Header from which we are removing prefix.
     * @return header without key prefix
     */
    private String removeKeyPrefix(final String key) {
        LOGGER.debug("Resolving header id: {}", key);
        final String result = key.substring(key.lastIndexOf("|") + 1, key.length());
        return result;
    }

    /**
     * Internal interceptor that checks if this component is under upgrade. Also logs Transaction key to ensure which transactions are being
     * used/joined.
     *
     * @param ctx
     *            invocation context.
     * @return proceed if not under upgrade or throw Exception if is under upgrade
     * @throws Exception
     *             a CamelEngineRuntimeException can be expected.
     */
    @AroundInvoke
    public Object checkIfIsUnderUpgradeInterceptor(final InvocationContext ctx) throws Exception {
        LOGGER.trace("Intercepted method: {}", ctx.getMethod());
        if (runtimeState.isUnderUpgrade()) {
            LOGGER.debug("This component is under upgrade. Will throw Exception.");
            throw new CamelEngineRuntimeException(MSG_MS_UNDER_UPGRADE);
        }
        return ctx.proceed();
    }

    /**
     * Here we deal with all exceptions (checked and runtime) that can occur when invoking a handler. Exceptions thrown by a handler are wrapped in an
     * <code>InvocationTargetException</code> so we must call <code>getTargetException()</code> to get the exception thrown by the handler. Other
     * exceptions may occur but both are wrapped in a <code>CamelEngineRuntimeException</code> and thrown up the mediation stack.
     *
     * @param e
     *            the e.
     */
    private void logErrorAndThrowException(final Exception e) {
        CamelEngineRuntimeException camelEx = null;
        final Throwable cause = e.getCause();
        if (cause != null && cause.getCause() != null) {
            Throwable handlerException;
            if (cause.getCause() instanceof InvocationTargetException) {
                handlerException = ((InvocationTargetException) cause.getCause()).getTargetException();
            } else {
                handlerException = cause.getCause();
            }
            camelEx = new CamelEngineRuntimeException("Exception occurred: " + handlerException.getMessage(), handlerException);
            LOGGER.error(EXCEPTION_CAUGHT_WHILE_TRYING_TO_INVOKE_METHOD_ON_THE_PROXY, handlerException);
        } else {
            camelEx = new CamelEngineRuntimeException("Exception occurred: " + e.getMessage(), e);
            LOGGER.error(EXCEPTION_CAUGHT_WHILE_TRYING_TO_INVOKE_METHOD_ON_THE_PROXY, e);
        }
        throw camelEx;
    }
}
