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

package org.jboss.as.camel.integration.service;

import java.lang.reflect.*;

import org.apache.camel.CamelContext;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Proxy class that will be injecteded into clients.
 */
public class CamelContextProxy implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextProxy.class);
    CamelContext camelContext;

    /**
     * Full arg constructor.
     *
     * @param deployment
     *            Deployment name.
     */
    public CamelContextProxy(final String deployment) {
        final ServiceController<?> serviceController = CurrentServiceContainer.getServiceContainer().getService(
                CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME);
        final CamelContextIntegrationService camelIntegrationService = (CamelContextIntegrationService) serviceController.getService();
        this.camelContext = camelIntegrationService.getCamelService().getCamelContextForDeployment(deployment).getCamelContext();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            LOGGER.trace("invoking method {} with args {}", method, args);
            return method.invoke(camelContext, args);
        } catch (final InvocationTargetException e) {
            throw e.getTargetException();
        } catch (final Exception e) {
            throw e;
        }
    }
}
