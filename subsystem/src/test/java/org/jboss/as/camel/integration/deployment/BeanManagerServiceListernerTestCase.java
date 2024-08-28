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

import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.weld.services.BeanManagerService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Transition;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceListener.Inheritance;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * The Class BeanManagerServiceListernerTestCase.
 */
@RunWith(PowerMockRunner.class)
public class BeanManagerServiceListernerTestCase {

    @Mock
    private CamelContextService camelService;

    @Mock
    private CamelContributionMetaInfoHolder contribMeta;

    @Mock
    private BeanManagerService beanManagerService;

    private BeanManagerServiceListener beanManagerServiceListerner;

    private ServiceControllerStub controller;

    @Before
    public void setup() {

        controller = new ServiceControllerStub();
        beanManagerServiceListerner = new BeanManagerServiceListener(ServiceName.JBOSS, camelService, contribMeta);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBeanManagerTransistion() {

        beanManagerServiceListerner.transition(controller, Transition.STARTING_to_UP);
        Mockito.verify(beanManagerService, Mockito.times(1)).getValue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBeanManagerUnimplementedMethods() {

        beanManagerServiceListerner.listenerAdded(controller);
        beanManagerServiceListerner.serviceRemoveRequestCleared(controller);
        beanManagerServiceListerner.serviceRemoveRequested(controller);
        beanManagerServiceListerner.dependencyFailed(controller);
        beanManagerServiceListerner.dependencyFailureCleared(controller);
        beanManagerServiceListerner.immediateDependencyAvailable(controller);
        beanManagerServiceListerner.immediateDependencyUnavailable(controller);
        beanManagerServiceListerner.transitiveDependencyAvailable(controller);
        beanManagerServiceListerner.transitiveDependencyUnavailable(controller);
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
            return ServiceName.JBOSS;
        }

        @Override
        public ServiceController getParent() {
            return null;
        }

        @Override
        public Service<?> getService() throws IllegalStateException {
            return beanManagerService;
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
