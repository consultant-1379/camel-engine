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

package org.jboss.as.camel.integration.deployment;

import org.jboss.as.server.deployment.*;

/**
 * Abstraction that adds ability to locate camel integration service via camel context service locator.
 */
public abstract class CamelIntegrationServiceAbstractDeploymentProcess extends CamelContextServiceLocator implements DeploymentUnitProcessor {

    @Override
    public abstract void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException;

    @Override
    public abstract void undeploy(DeploymentUnit context);
}
