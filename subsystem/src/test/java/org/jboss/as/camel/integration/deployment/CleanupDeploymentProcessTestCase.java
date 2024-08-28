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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class CleanupDeploymentProcessTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class CleanupDeploymentProcessTestCase {

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private DeploymentUnit topLevelDu;

    private CleanupDeploymentProcess cleanupDeploymentProcess;

    @Test
    public void testDeployCleanupDeploymentProcess() throws DeploymentUnitProcessingException {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(du.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)).thenReturn(true);
        Mockito.when(DeploymentUtils.getTopDeploymentUnit(du)).thenReturn(topLevelDu);
        Mockito.when(topLevelDu.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(true);
        cleanupDeploymentProcess = new CleanupDeploymentProcess();
        cleanupDeploymentProcess.deploy(dpc);

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(du, Mockito.times(1)).hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
        Mockito.verify(du, Mockito.times(1)).removeAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
        Mockito.verify(topLevelDu, Mockito.times(1)).hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
        Mockito.verify(topLevelDu, Mockito.times(1)).removeAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
    }

    @Test
    public void testUnDeployCleanDeploymentProcess() throws DeploymentUnitProcessingException {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        cleanupDeploymentProcess = new CleanupDeploymentProcess();
        cleanupDeploymentProcess.deploy(dpc);
        cleanupDeploymentProcess.undeploy(dpc.getDeploymentUnit());
        Mockito.verify(dpc, Mockito.times(2)).getDeploymentUnit();
    }
}
