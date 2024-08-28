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
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.as.server.deployment.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment processor that will register previously created camel context with this deployment.
 */
public class RegisterCamelContextDeploymentProcess extends CamelIntegrationServiceAbstractDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.POST_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4005;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCamelContextDeploymentProcess.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        if (toplevelDeploymentUnit.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)) {
            final CamelContextService camelService = getCamelContextService(deploymentUnit);
            if (camelService.getCamelContextForDeployment(toplevelDeploymentUnit.getName()) != null) {
                LOGGER.trace("This deployment unit {}  already has registered camel context, skipping...", deploymentUnit.getName());
                return;
            } else {
                LOGGER.trace("Registering CamelContext with deployment: {}", deploymentUnit.getName());
                try {
                    final DeploymentMetaInfo dmi = toplevelDeploymentUnit.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
                    camelService.registerCamelContext(dmi, toplevelDeploymentUnit.getName());
                } catch (final Exception e) {
                    throw new DeploymentUnitProcessingException(e);
                }
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        final DeploymentUnit toplevelDeploymentUnit = DeploymentUtils.getTopDeploymentUnit(deploymentUnit);
        final CamelContextService camelService = getCamelContextService(deploymentUnit);
        if (camelService != null) {
            final DeploymentMetaInfo dmi = camelService.getCamelContextForDeployment(toplevelDeploymentUnit.getName());
            if (dmi != null) {
                LOGGER.trace("Deregistering CamelContext for deployment {}", toplevelDeploymentUnit.getName());
                camelService.deregisterCamelContext(toplevelDeploymentUnit.getName());
                toplevelDeploymentUnit.putAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO, dmi);
            }
        }
    }
}
