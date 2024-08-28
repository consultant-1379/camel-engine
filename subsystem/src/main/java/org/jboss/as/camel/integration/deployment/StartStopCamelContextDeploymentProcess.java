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

package org.jboss.as.camel.integration.deployment;

import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.camel.integration.service.DeploymentMetaInfo;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.as.server.deployment.Phase;
import org.jboss.camel.exception.CamelEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment processor that will start or stop previously created camel context.
 */
public class StartStopCamelContextDeploymentProcess extends CamelIntegrationServiceAbstractDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.POST_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4004;

    private static final Logger LOGGER = LoggerFactory.getLogger(StartStopCamelContextDeploymentProcess.class);

    @Override
    @SuppressWarnings("PMD.UseProperClassLoader")
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit unit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(unit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            final CamelContextService camelService = getCamelContextService(unit);
            try {
                final DeploymentMetaInfo dmi = toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
                if (dmi.getCamelContext().getStatus().isStarted() || dmi.getCamelContext().getStatus().isStarting()) {
                    LOGGER.trace("Camel context is already started or starting, skiping...");
                    return;
                } else {
                    LOGGER.trace("Starting CamelContext for deployment: {}", unit.getName());
                    startCamelContext(toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO),
                            toplevelDeploymentUnit.getAttachment(Attachments.MODULE).getClassLoader(), camelService);
                }
            } catch (final Exception e) {
                throw new DeploymentUnitProcessingException(e);
            }
        }
    }

    @Override
    @SuppressWarnings("PMD.UseProperClassLoader")
    public void undeploy(final DeploymentUnit unit) {
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(unit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            final CamelContextService camelService = getCamelContextService(unit);
            final DeploymentMetaInfo dmi = toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
            if (dmi.getCamelContext().getStatus().isStopping() || dmi.getCamelContext().getStatus().isStopped()) {
                LOGGER.trace("Camel context is already stopped or is stoppig, skipping");
                return;
            } else {
                LOGGER.trace("Stopping CamelContext for deployment {}", unit.getName());
                stopCamelContext(dmi, toplevelDeploymentUnit.getAttachment(Attachments.MODULE).getClassLoader(), camelService);
            }
        }
    }

    @SuppressWarnings("PMD.UseProperClassLoader")
    private void startCamelContext(final DeploymentMetaInfo dmi, final ClassLoader mcl, final CamelContextService camelService)
            throws CamelEngineException {
        final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mcl);
            camelService.startCamelContext(dmi);
        } catch (final Exception e) {
            throw new CamelEngineException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(tcl);
        }
    }

    @SuppressWarnings("PMD.UseProperClassLoader")
    private void stopCamelContext(final DeploymentMetaInfo dmi, final ClassLoader mcl, final CamelContextService camelService) {
        final ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(mcl);
            camelService.stopCamelContext(dmi);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(tcl);
        }
    }
}
