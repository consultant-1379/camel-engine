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
import org.jboss.as.server.deployment.module.DelegatingClassFileTransformer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class ClassTransformDeploymentProcessTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassTransformDeploymentProcessTestCase {

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private DelegatingClassFileTransformer transformer;

    @Mock
    private CamelContextServiceMetaInfo metaInfoHolder;

    private ClassTransformerDeploymentProcess classTransformerDeploymentProcess;

    @Test
    public void testDeployTranformerDeploymentProcess() throws DeploymentUnitProcessingException {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(du.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)).thenReturn(true);
        Mockito.when(du.getAttachment(DelegatingClassFileTransformer.ATTACHMENT_KEY)).thenReturn(transformer);
        Mockito.when(du.getAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)).thenReturn(metaInfoHolder);
        classTransformerDeploymentProcess = new ClassTransformerDeploymentProcess();
        classTransformerDeploymentProcess.deploy(dpc);

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(du, Mockito.times(1)).hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
        Mockito.verify(du, Mockito.times(1)).getAttachment(DelegatingClassFileTransformer.ATTACHMENT_KEY);
        Mockito.verify(du, Mockito.times(1)).getAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
    }

    @Test
    public void testUnDeployTranformerDeploymentProcess() throws DeploymentUnitProcessingException {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        classTransformerDeploymentProcess = new ClassTransformerDeploymentProcess();
        classTransformerDeploymentProcess.deploy(dpc);
        classTransformerDeploymentProcess.undeploy(dpc.getDeploymentUnit());
        Mockito.verify(dpc, Mockito.times(2)).getDeploymentUnit();
    }
}
