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
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class RegisterCamelContextDeploymentProcessTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class RegisterCamelContextDeploymentProcessTestCase {

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

    private RegisterCamelContextDeploymentProcess registerCamelContextDeploymentProcess;

    @SuppressWarnings("unchecked")
    @Test
    public void testDeployRegisterCamelContextAlreadyRegistered() throws DeploymentUnitProcessingException, StartException {

        registerCamelContextDeploymentProcess = new RegisterCamelContextDeploymentProcess();
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(DeploymentUtils.getTopDeploymentUnit(du)).thenReturn(topLevelDu);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(topLevelDu.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(true);
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);
        Mockito.when(camelContextService.getCamelContextForDeployment(topLevelDu.getName())).thenReturn(dmi);

        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());

        registerCamelContextDeploymentProcess.deploy(dpc);

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(camelContextService, Mockito.times(0)).registerCamelContext(dmi, topLevelDu.getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeployRegisterCamelContext() throws DeploymentUnitProcessingException, StartException {

        registerCamelContextDeploymentProcess = new RegisterCamelContextDeploymentProcess();
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(DeploymentUtils.getTopDeploymentUnit(du)).thenReturn(topLevelDu);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(topLevelDu.hasAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(true);
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);
        Mockito.when(camelContextService.getCamelContextForDeployment(topLevelDu.getName())).thenReturn(null);

        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());
        Mockito.when(topLevelDu.getAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO)).thenReturn(dmi);

        registerCamelContextDeploymentProcess.deploy(dpc);

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(camelContextService, Mockito.times(1)).registerCamelContext(dmi, topLevelDu.getName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUnDeployRegisterCamelContextDeploymentProcess() throws DeploymentUnitProcessingException {

        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        Mockito.when(DeploymentUtils.getTopDeploymentUnit(du)).thenReturn(topLevelDu);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
        Mockito.when(serviceRegistry.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                new ServiceControllerStub());
        Mockito.when(camelIntegrationService.getCamelService()).thenReturn(camelContextService);
        Mockito.when(camelContextService.getCamelContextForDeployment(topLevelDu.getName())).thenReturn(dmi);

        registerCamelContextDeploymentProcess = new RegisterCamelContextDeploymentProcess();
        registerCamelContextDeploymentProcess.undeploy(dpc.getDeploymentUnit());

        Mockito.verify(dpc, Mockito.times(1)).getDeploymentUnit();
        Mockito.verify(topLevelDu, Mockito.times(1)).putAttachment(CamelContextServiceAttachments.DEPLOYMENT_META_INFO, dmi);
        Mockito.verify(camelContextService, Mockito.times(1)).deregisterCamelContext(topLevelDu.getName());
    }

    /**
     * The Class ServiceControllerStub.
     */
    class ServiceControllerStub implements ServiceController {

        @Override
        public void addListener(final ServiceListener listener) {}

        @Override
        public void addListener(final Inheritance object, final ServiceListener listener) {}

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
