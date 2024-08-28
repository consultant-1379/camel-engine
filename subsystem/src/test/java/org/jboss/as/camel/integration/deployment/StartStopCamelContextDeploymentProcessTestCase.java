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

import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.jboss.as.camel.integration.service.CamelContextIntegrationService;
import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.camel.integration.service.DeploymentMetaInfo;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceListener.Inheritance;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * The Class StartStopCamelContextDeploymentProcessTestCase.
 */
@RunWith(PowerMockRunner.class)
public class StartStopCamelContextDeploymentProcessTestCase {

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private DeploymentUnit topLevelDu;

    @Mock
    private DeploymentMetaInfo dmi;

    @Mock
    private CamelContextIntegrationService camelIntegrationService;

    @Mock
    private CamelContextService camelContextService;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private CamelContext camelContext;

    private StartStopCamelContextDeploymentProcess startStopCamelContextDeploymentProcess;

    /**
     * Test deploy start stop camel deploy exception.
     *
     * @throws DeploymentUnitProcessingException
     *             the deployment unit processing exception
     * @throws StartException
     *             the start exception
     */
    @SuppressWarnings("unchecked")
    @Test(expected = DeploymentUnitProcessingException.class)
    public void testDeployStartStopCamelDeployException() throws DeploymentUnitProcessingException, StartException {

        startStopCamelContextDeploymentProcess = new StartStopCamelContextDeploymentProcess();
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(DeploymentUtils.getTopDeploymentUnit(du)).thenReturn(topLevelDu);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(topLevelDu.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(true);
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);

        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());

        startStopCamelContextDeploymentProcess.deploy(dpc);
    }

    /**
     * Test deploy start stop camel deploy already started.
     *
     * @throws DeploymentUnitProcessingException
     *             the deployment unit processing exception
     * @throws StartException
     *             the start exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDeployStartStopCamelDeployAlreadyStarted() throws DeploymentUnitProcessingException, StartException {

        startStopCamelContextDeploymentProcess = new StartStopCamelContextDeploymentProcess();
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(DeploymentUtils.getTopDeploymentUnit(du)).thenReturn(topLevelDu);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(topLevelDu.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(true);
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);
        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());
        Mockito.when(topLevelDu.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(dmi);
        Mockito.when(dmi.getCamelContext()).thenReturn(camelContext);
        Mockito.when(camelContext.getStatus()).thenReturn(ServiceStatus.Started);
        startStopCamelContextDeploymentProcess.deploy(dpc);
        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(topLevelDu, Mockito.times(1)).getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
        Mockito.verify(camelContext, Mockito.times(1)).getStatus();
    }

    /**
     * Test un deploy start stop camel context deployment process already stopped.
     *
     * @throws DeploymentUnitProcessingException
     *             the deployment unit processing exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUnDeployStartStopCamelContextDeploymentProcessAlreadyStopped() throws DeploymentUnitProcessingException {

        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(DeploymentUtils.getTopDeploymentUnit(du)).thenReturn(topLevelDu);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(topLevelDu.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(true);
        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);
        Mockito.when(topLevelDu.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(dmi);
        Mockito.when(dmi.getCamelContext()).thenReturn(camelContext);
        Mockito.when(camelContext.getStatus()).thenReturn(ServiceStatus.Stopping);

        startStopCamelContextDeploymentProcess = new StartStopCamelContextDeploymentProcess();
        startStopCamelContextDeploymentProcess.undeploy(dpc.getDeploymentUnit());

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(topLevelDu, Mockito.times(1)).getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO);
        Mockito.verify(camelContext, Mockito.times(1)).getStatus();
    }

    /**
     * Test stop camel context deployment process.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testStopCamelContextDeploymentProcess() throws Exception {

        startStopCamelContextDeploymentProcess = new StartStopCamelContextDeploymentProcess();
        Whitebox.invokeMethod(startStopCamelContextDeploymentProcess, "startCamelContext", dmi, Thread.currentThread().getContextClassLoader(),
                camelContextService);
        Mockito.verify(camelContextService, Mockito.times(1)).startCamelContext(dmi);
    }

    /**
     * Test sart camel context deployment process.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testSartCamelContextDeploymentProcess() throws Exception {

        startStopCamelContextDeploymentProcess = new StartStopCamelContextDeploymentProcess();
        Whitebox.invokeMethod(startStopCamelContextDeploymentProcess, "stopCamelContext", dmi, Thread.currentThread().getContextClassLoader(),
                camelContextService);
        Mockito.verify(camelContextService, Mockito.times(1)).stopCamelContext(dmi);
    }

    /**
     * The Class ServiceControllerStub.
     */
    class ServiceControllerStub implements ServiceController {

        @Override
        public void addListener(final ServiceListener listener) {}

        @Override
        public void addListener(final Inheritance inheritance, final ServiceListener listener) {}

        @Override
        public boolean compareAndSetMode(final Mode firstMode, final Mode secondMode) {
            return false;
        }

        @Override
        public ServiceName[] getAliases() {
            return null;
        }

        @Override
        public Set getImmediateUnavailableDependencies() {
            return null;
        }

        @Override
        public Mode getMode() {
            return null;
        }

        @Override
        public ServiceName getName() {
            return null;
        }

        @Override
        public ServiceController getParent() {
            return null;
        }

        @Override
        public Service<CamelContextIntegrationService> getService() throws IllegalStateException {
            return camelIntegrationService;
        }

        @Override
        public ServiceContainer getServiceContainer() {
            return null;
        }

        @Override
        public StartException getStartException() {
            return null;
        }

        @Override
        public State getState() {
            return null;
        }

        @Override
        public Substate getSubstate() {
            return null;
        }

        @Override
        public Object getValue() throws IllegalStateException {
            return null;
        }

        @Override
        public void removeListener(final ServiceListener listener) {}

        @Override
        public void retry() {}

        @Override
        public void setMode(final Mode mode) {}
    }
}
