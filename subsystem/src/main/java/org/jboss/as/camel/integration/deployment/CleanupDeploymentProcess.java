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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.as.server.deployment.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cleanup all attachments left after deployment process.
 */
public class CleanupDeploymentProcess extends CamelIntegrationServiceAbstractDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.CLEANUP;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4006;
    /**
     * Prefix for removing trace message.
     */
    private static final String REMOVING_ATTACHMENT = "Removing attachment: {}";

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupDeploymentProcess.class);

    /**
     * Prefix for cleanup trace message.
     */
    private static final String CLEANING_UP_AFTER_DEPLOYMENT = "Cleaning up after deployment: {}";

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        LOGGER.trace(CLEANING_UP_AFTER_DEPLOYMENT, deploymentUnit.getName());
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            LOGGER.trace(REMOVING_ATTACHMENT, CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
            toplevelDeploymentUnit.removeAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
        }
        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)) {
            LOGGER.trace(REMOVING_ATTACHMENT, CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
            deploymentUnit.removeAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        LOGGER.trace("Undeploy called for cleanup deployment processor on deployment unit: {}", deploymentUnit.getName());
    }
}
