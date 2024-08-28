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

import org.jboss.as.camel.integration.service.CamelContextIntegrationService;
import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.weld.services.BeanManagerService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceListener.Inheritance;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * The Class BindCamelBeansDeploymentProcessTestCase.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BeanManagerService.class)
public class BindCamelBeansDeploymentProcessTestCase {

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private ServiceTarget serviceTarget;

    @Mock
    private CamelContributionMetaInfoHolder contribMeta;

    @Mock
    private CamelContextIntegrationService camelIntegrationService;

    @Mock
    private CamelContextService camelContextService;

    @Mock
    private ServiceRegistry serviceRegistry;

    private BindCamelBeansDeploymentProcess bindCamelBeansDeploymentProcess;

    @SuppressWarnings("unchecked")
    @Test(expected = DeploymentUnitProcessingException.class)
    public void testBindCamelBeansDeploymentProcessException() throws DeploymentUnitProcessingException {

        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(dpc.getServiceTarget()).thenReturn(serviceTarget);
        Mockito.when(du.hasAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)).thenReturn(true);

        Mockito.when(du.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)).thenReturn(true);

        Mockito.when(du.getAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)).thenReturn(contribMeta);

        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);

        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);

        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());

        PowerMockito.mockStatic(BeanManagerService.class);
        PowerMockito.when(BeanManagerService.serviceName(du)).thenReturn(ServiceName.JBOSS);

        bindCamelBeansDeploymentProcess = new BindCamelBeansDeploymentProcess();
        bindCamelBeansDeploymentProcess.deploy(dpc);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBindCamelBeansDeploymentProcess() throws DeploymentUnitProcessingException {

        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(dpc.getServiceTarget()).thenReturn(serviceTarget);
        Mockito.when(du.hasAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)).thenReturn(true);

        Mockito.when(du.hasAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)).thenReturn(true);

        Mockito.when(du.getAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)).thenReturn(contribMeta);

        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);

        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);

        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());

        PowerMockito.mockStatic(BeanManagerService.class);
        PowerMockito.when(BeanManagerService.serviceName(du)).thenReturn(ServiceName.JBOSS);

        Mockito.when(dpc.getServiceRegistry()).thenReturn(serviceRegistry);

        final ServiceControllerStub serviceControllerStub = new ServiceControllerStub();
        Mockito.when(serviceRegistry.getRequiredService(ServiceName.JBOSS)).thenReturn(serviceControllerStub);

        bindCamelBeansDeploymentProcess = new BindCamelBeansDeploymentProcess();
        bindCamelBeansDeploymentProcess.deploy(dpc);

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(du, Mockito.times(1)).hasAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER);
        Mockito.verify(du, Mockito.times(1)).getAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER);
        Mockito.verify(camelIntegrationService, Mockito.times(1)).getCamelService();
        Mockito.verify(serviceTarget, Mockito.times(1)).addDependency(ServiceName.JBOSS);
    }

    @Test
    public void testUndeployBindCamelBeansDeploymentProcess() throws DeploymentUnitProcessingException {

        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);

        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());
        bindCamelBeansDeploymentProcess = new BindCamelBeansDeploymentProcess();
        bindCamelBeansDeploymentProcess.undeploy(dpc.getDeploymentUnit());

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
    }

    @Test
    public void testUndeployBindCamelBeansDeploymentProcessException() throws DeploymentUnitProcessingException {

        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);

        bindCamelBeansDeploymentProcess = new BindCamelBeansDeploymentProcess();
        bindCamelBeansDeploymentProcess.undeploy(dpc.getDeploymentUnit());

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
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
