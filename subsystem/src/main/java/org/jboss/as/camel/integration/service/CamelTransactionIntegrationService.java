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

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.as.txn.service.TransactionManagerService;
import org.jboss.as.txn.service.TransactionSynchronizationRegistryService;
import org.jboss.msc.inject.CastingInjector;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.*;

/**
 * Camel subsystem transaction integration service.
 */
public class CamelTransactionIntegrationService implements Service<CamelTransactionIntegrationService> {

    public static final ServiceName CAMEL_TRANSACTION_INTERATION_SERVICE_NAME = ServiceName.JBOSS.append("camel-transaction-integration-service");

    @Override
    public CamelTransactionIntegrationService getValue() {
        return null;
    }

    @Override
    public void start(final StartContext context) throws StartException {}

    @Override
    public void stop(final StopContext context) {}

    /**
     * Add service into micro kernel.
     *
     * @param target
     *            Service to add.
     * @param listeners
     *            Listeners needed.
     * @return Service controler
     */
    @SafeVarargs
    public static ServiceController<?> addService(final ServiceTarget target, final ServiceListener<Object>... listeners) {

        final CamelTransactionIntegrationService camelTransactionIntegrationService = new CamelTransactionIntegrationService();

        // set the transaction manager to be accessible via TransactionUtil
        final Injector<TransactionManager> transactionManagerInjector = new Injector<TransactionManager>() {
            @Override
            public void inject(final TransactionManager value) {
                TransactionUtil.setTransactionManager(value);
            }

            @Override
            public void uninject() {
                TransactionUtil.setTransactionManager(null);
            }
        };

        // set the transaction service registry to be accessible via TransactionUtil (after service is installed below)
        final Injector<TransactionSynchronizationRegistry> txRegistryInjector = new Injector<TransactionSynchronizationRegistry>() {
            @Override
            public void inject(final TransactionSynchronizationRegistry value) {
                TransactionUtil.setTransactionSynchronizationRegistry(value);
            }

            @Override
            public void uninject() {
                TransactionUtil.setTransactionSynchronizationRegistry(null);
            }
        };

        return target
                .addService(CAMEL_TRANSACTION_INTERATION_SERVICE_NAME, camelTransactionIntegrationService)
                .addListener(listeners)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .addDependency(TransactionManagerService.SERVICE_NAME,
                        new CastingInjector<TransactionManager>(transactionManagerInjector, TransactionManager.class))
                .addDependency(TransactionSynchronizationRegistryService.SERVICE_NAME,
                        new CastingInjector<TransactionSynchronizationRegistry>(txRegistryInjector, TransactionSynchronizationRegistry.class))
                .install();
    }
}
