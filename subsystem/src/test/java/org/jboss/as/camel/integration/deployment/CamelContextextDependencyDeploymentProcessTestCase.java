/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
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
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class CamelContextextDependencyDeploymentProcessTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamelContextextDependencyDeploymentProcessTestCase {

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private ModuleSpecification moduleSpecification;

    private CamelContextDependencyDeploymentProcess camelContextDependencyDeploymentProcess;

    @Test
    public void testDeployCamelContextDependencyDeploymentProcess() throws DeploymentUnitProcessingException {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(du.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)).thenReturn(true);
        Mockito.when(du.getAttachment(Attachments.MODULE_SPECIFICATION)).thenReturn(moduleSpecification);

        camelContextDependencyDeploymentProcess = new CamelContextDependencyDeploymentProcess();
        camelContextDependencyDeploymentProcess.deploy(dpc);

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(du, Mockito.times(1)).hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
        Mockito.verify(moduleSpecification, Mockito.times(2)).addSystemDependency(Matchers.any(ModuleDependency.class));
    }

    @Test
    public void testUndeployCamelContextDependencyDeploymentProcess() throws DeploymentUnitProcessingException {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        camelContextDependencyDeploymentProcess = new CamelContextDependencyDeploymentProcess();
        camelContextDependencyDeploymentProcess.deploy(dpc);
        camelContextDependencyDeploymentProcess.undeploy(dpc.getDeploymentUnit());
        Mockito.verify(dpc, Mockito.times(2)).getDeploymentUnit();
    }
}
