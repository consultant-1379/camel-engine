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

import org.jboss.as.camel.integration.service.DeploymentMetaInfo;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.as.server.deployment.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment processor that will create camel context for this deployment.
 */
public class CreateCamelContextDeploymentProcess extends CamelIntegrationServiceAbstractDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.POST_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4003;

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCamelContextDeploymentProcess.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);

        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)
                && !toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            LOGGER.trace("Creating camel context for deployment: {}, using toplevel deployment name: {}", deploymentUnit.getName(),
                    toplevelDeploymentUnit.getName());
            try {
                final DeploymentMetaInfo dmi = getCamelContextService(deploymentUnit).createCamelContextForDeployment(
                        toplevelDeploymentUnit.getName());
                toplevelDeploymentUnit.putAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO, dmi);
            } catch (final Exception e) {
                throw new DeploymentUnitProcessingException(e);
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        LOGGER.trace("Undeploy method called for {}", deploymentUnit.getName());
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            LOGGER.trace("Removing CamelContextInstance attachment");
            toplevelDeploymentUnit.removeAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
        }
    }
}
