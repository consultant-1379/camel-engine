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
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.weld.services.BeanManagerService;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Deployment processor that will register previously created camel context with this deployment.
 */
public class BindCamelBeansDeploymentProcess extends CamelIntegrationServiceAbstractDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.INSTALL;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4006;

    private static final Logger LOGGER = LoggerFactory.getLogger(BindCamelBeansDeploymentProcess.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final ServiceTarget serviceTarget = phaseContext.getServiceTarget();
        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)) {
            LOGGER.trace("Binding beans in deployment unit {}", deploymentUnit.getName());
            final CamelContextService camelService = getCamelContextService(deploymentUnit);
            final CamelContributionMetaInfoHolder contribMeta = deploymentUnit
                    .getAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER);

            final ServiceName beanManagerServiceName = BeanManagerService.serviceName(deploymentUnit);
            serviceTarget.addDependency(beanManagerServiceName);
            try {
                LOGGER.trace("Registering BeanManagerService listener.");
                final ServiceController<?> srvcController = phaseContext.getServiceRegistry().getRequiredService(beanManagerServiceName);
                srvcController.addListener(new BeanManagerServiceListener(beanManagerServiceName, camelService, contribMeta));
            } catch (final Exception e) {
                e.printStackTrace();
                throw new DeploymentUnitProcessingException(e);
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        LOGGER.trace("undeploy: {}", deploymentUnit);
        final CamelContextService camelService = getCamelContextService(deploymentUnit);
        if (camelService != null) {
            try {
                camelService.unbindBeans(deploymentUnit.getName());
            } catch (final Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
