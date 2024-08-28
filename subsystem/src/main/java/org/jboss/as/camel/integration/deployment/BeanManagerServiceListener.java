/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.integration.deployment;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.as.camel.integration.service.CamelContextService;
import org.jboss.as.weld.services.BeanManagerService;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Transition;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;

/**
 * Service listener that will wait for BeanManagerService to become available, and change it's status to UP.
 */
public class BeanManagerServiceListener implements ServiceListener<Object> {

    private static final Logger LOGGER = Logger.getLogger(BeanManagerServiceListener.class);
    private final ServiceName serviceName;
    private final CamelContextService camelService;
    private final CamelContributionMetaInfoHolder contribMeta;

    /**
     * Full arg constructor.
     *
     * @param serviceName
     *            service name that we are listening for (in this case BeanManagerService)
     * @param camelService
     *            CamelContextService instance that we will use for binding CDI beans camel service.
     * @param contribMeta
     *            contrib meta Contribution metadata as gathered by deployer.
     */
    public BeanManagerServiceListener(final ServiceName serviceName, final CamelContextService camelService,
            final CamelContributionMetaInfoHolder contribMeta) {
        this.serviceName = serviceName;
        this.camelService = camelService;
        this.contribMeta = contribMeta;
    }

    @Override
    public void listenerAdded(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty listenerAdded method");
    }

    @Override
    public void transition(final ServiceController<? extends Object> controller, final Transition transition) {
        if (controller.getName().compareTo(serviceName) == 0 && transition.compareTo(Transition.STARTING_to_UP) == 0) {
            LOGGER.trace("BeanManagerService is now up, will start binding beans.");
            final BeanManagerService beanManagerService = (BeanManagerService) controller.getService();
            final BeanManager beanManager = beanManagerService.getValue();
            try {
                camelService.bindBeans(contribMeta, beanManager);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void serviceRemoveRequested(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty serviceRemoveRequested method");
    }

    @Override
    public void serviceRemoveRequestCleared(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty serviceRemoveRequestCleared method");
    }

    @Override
    public void dependencyFailed(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty dependencyFailed method");
    }

    @Override
    public void dependencyFailureCleared(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty dependencyFailureCleared method");
    }

    @Override
    public void immediateDependencyUnavailable(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty immediateDependencyUnavailable method");
    }

    @Override
    public void immediateDependencyAvailable(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty immediateDependencyAvailable method");
    }

    @Override
    public void transitiveDependencyUnavailable(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty transitiveDependencyUnavailable method");
    }

    @Override
    public void transitiveDependencyAvailable(final ServiceController<? extends Object> controller) {
        LOGGER.trace("Invoked empty transitiveDependencyAvailable method");
    }
}
