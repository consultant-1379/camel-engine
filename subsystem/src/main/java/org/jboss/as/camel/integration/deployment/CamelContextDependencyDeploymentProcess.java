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

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment processor that will add all required dependencies onto deployment classpath.
 */
public class CamelContextDependencyDeploymentProcess extends CamelIntegrationServiceAbstractDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.DEPENDENCIES;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4001;

    private static final ModuleIdentifier ORG_JBOSS_AS_CAMEL_CAMEL_SUBSYSTEM = ModuleIdentifier.create("org.jboss.as.camel.camel-subsystem");
    private static final ModuleIdentifier ORG_APACHE_CAMEL = ModuleIdentifier.create("org.apache.camel");

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextDependencyDeploymentProcess.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)
                || deploymentUnit.hasAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)) {
            LOGGER.trace("Adding camel integration module dependency to deployment unit: {}", deploymentUnit.getName());
            final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
            addDependency(moduleSpecification, ORG_JBOSS_AS_CAMEL_CAMEL_SUBSYSTEM);
            addDependency(moduleSpecification, ORG_APACHE_CAMEL);
            /*
             * Do we need to add dependency to top level deployment as well, in case we have ear that contains war and war uses subsystem?
             */
        }
    }

    private void addDependency(final ModuleSpecification moduleSpecification, final ModuleIdentifier moduleIdentifier) {
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();
        final ModuleDependency moduleDependency = new ModuleDependency(moduleLoader, moduleIdentifier, false, false, false, false);
        if (moduleSpecification.getSystemDependencies().contains(moduleDependency)) {
            LOGGER.trace("Already has this system dependency, will not add...");
        } else {
            LOGGER.trace("Adding system dependency...");
            moduleSpecification.addSystemDependency(moduleDependency);
        }
    }

    @Override
    public void undeploy(final DeploymentUnit deploymentUnit) {
        LOGGER.trace("undeploy called for {}", deploymentUnit.getName());
    }
}
