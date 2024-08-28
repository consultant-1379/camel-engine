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
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.module.DelegatingClassFileTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment processor that will add class file transformers for camel context injections.
 */
public class ClassTransformerDeploymentProcess extends CamelIntegrationServiceAbstractDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.CONFIGURE_MODULE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4002;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassTransformerDeploymentProcess.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)) {
            LOGGER.trace("Adding classfile transformers for deployment: {}", deploymentUnit.getName());
            final DelegatingClassFileTransformer transformer = deploymentUnit.getAttachment(DelegatingClassFileTransformer.ATTACHMENT_KEY);
            if (transformer != null) {
                final CamelContextServiceMetaInfo metaInfoHolder = deploymentUnit
                        .getAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
                transformer.addTransformer(new CamelContextClassfileTransformer(metaInfoHolder));
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit context) {
        LOGGER.trace("UnDeploy {}", context.getName());
    }
}
