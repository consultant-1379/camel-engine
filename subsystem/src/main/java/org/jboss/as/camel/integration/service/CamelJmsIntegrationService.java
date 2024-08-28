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

package org.jboss.as.camel.integration.service;

import javax.jms.ConnectionFactory;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ValueManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.msc.inject.CastingInjector;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;

/**
 * Camel jms integration service.
 */
public class CamelJmsIntegrationService implements Service<CamelJmsIntegrationService> {

    public static final ServiceName CAMEL_JMS_INTERATION_SERVICE_NAME = ServiceName.JBOSS.append("camel-jms-integration-service");

    @Override
    public CamelJmsIntegrationService getValue() {

        return null;
    }

    @Override
    public void start(final StartContext context) throws StartException {}

    @Override
    public void stop(final StopContext context) {}

    /**
     * Add service to micro kernel.
     *
     * @param target
     *            Service to add.
     * @param listeners
     *            Listeners for this service.
     * @return ServiceController
     */
    @SafeVarargs
    public static ServiceController<?> addService(final ServiceTarget target, final ServiceListener<Object>... listeners) {

        final CamelJmsIntegrationService cmaelJmsIntegrationService = new CamelJmsIntegrationService();

        // set the conn factory to be accessible via JmsUtil
        final Injector<ValueManagedReferenceFactory> connFactInjector = new Injector<ValueManagedReferenceFactory>() {
            @Override
            public void inject(final ValueManagedReferenceFactory value) {
                final ManagedReference ref = value.getReference();
                JmsUtil.setConnectionFactory((ConnectionFactory) ref.getInstance());
                ref.release();
            }

            @Override
            public void uninject() {
                JmsUtil.setConnectionFactory(null);
            }
        };

        return target
                .addService(CAMEL_JMS_INTERATION_SERVICE_NAME, cmaelJmsIntegrationService)
                .addListener(listeners)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .addDependency(ContextNames.bindInfoFor(JmsUtil.CON_FACT_JNDI_BINDING).getBinderServiceName(),
                        new CastingInjector<ValueManagedReferenceFactory>(connFactInjector, ValueManagedReferenceFactory.class)).install();
    }
}
