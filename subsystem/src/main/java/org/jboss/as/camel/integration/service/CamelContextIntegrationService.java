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

package org.jboss.as.camel.integration.service;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subsystem service responsible for managing camel context withing subsystem.
 */
public class CamelContextIntegrationService implements Service<CamelContextIntegrationService> {

    public static final ServiceName CAMEL_CONTEXT_INTERATION_SERVICE_NAME = ServiceName.JBOSS.append("camel-context-integration-service");

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextIntegrationService.class);

    private CamelContextService camelService;

    private boolean sharedCamelDeployment;

    private String camelContextName;

    /**
     * Full arg constructor.
     *
     * @param sharedContext
     *            Is this context shared for all deployments on the server.
     * @param sharedContextName
     *            Name of the shared context.
     */
    public CamelContextIntegrationService(final boolean sharedContext, final String sharedContextName) {
        LOGGER.info("Creating Camel integration service with shared={}, ctxName={}", sharedContext, sharedContextName);
        sharedCamelDeployment = sharedContext;
        camelContextName = sharedContextName;
    }

    @Override
    public CamelContextIntegrationService getValue() {
        return this;
    }

    public CamelContextService getCamelService() {
        return camelService;
    }

    public boolean isSharedCamelDeployment() {
        return sharedCamelDeployment;
    }

    public String getCamelContextName() {
        return camelContextName;
    }

    public void setSharedCamelDeployment(final boolean sharedCamelDeployment) {
        this.sharedCamelDeployment = sharedCamelDeployment;
    }

    public void setCamelContextName(final String camelContextName) {
        this.camelContextName = camelContextName;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        LOGGER.info("Starting camel integration service....");
        camelService = new CamelContextService(sharedCamelDeployment, camelContextName);
    }

    @Override
    public void stop(final StopContext context) {
        LOGGER.info("Stopping camel integration service...");
        camelService.destroy();
    }

    /**
     * Add service to the micro kernel.
     *
     * @param target
     *            Service to add.
     * @param sharedContext
     *            Is the camel context shared.
     * @param sharedContextName
     *            If camel context is shared, what is the name of the context
     * @param listeners
     *            Array of listeners this service needs.
     * @return Service controler
     */
    @SafeVarargs
    public static ServiceController<?> addService(final ServiceTarget target, final boolean sharedContext, final String sharedContextName,
            final ServiceListener<Object>... listeners) {
        final CamelContextIntegrationService camelContextIntegrationService = new CamelContextIntegrationService(sharedContext, sharedContextName);
        return target.addService(CAMEL_CONTEXT_INTERATION_SERVICE_NAME, camelContextIntegrationService).addListener(listeners)
                .setInitialMode(ServiceController.Mode.ACTIVE).addDependency(CamelJmsIntegrationService.CAMEL_JMS_INTERATION_SERVICE_NAME)
                .addDependency(CamelTransactionIntegrationService.CAMEL_TRANSACTION_INTERATION_SERVICE_NAME).install();
    }
}
