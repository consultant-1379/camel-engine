/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
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

/**
 * Utility class used to set up transaction manager glue code.
 */
@SuppressWarnings("PMD.UseSingleton")
public final class TransactionUtil {

    private static volatile TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    private static volatile TransactionManager transactionManager;

    private TransactionUtil() {}

    /**
     * Static setter for transaction manager.
     *
     * @param txManager
     *            TransactionManager to set.
     */
    public static void setTransactionManager(final TransactionManager txManager) {
        if (transactionManager == null) {
            transactionManager = txManager;
        }
    }

    /**
     * Getter for transaction manager.
     *
     * @return TransactionManager
     */
    public static TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Setter for transaction synchronization registry.
     *
     * @param transactionSynchronizationRegistry
     *            Instance of TransactionSynchronizationRegistry.
     */
    public static void setTransactionSynchronizationRegistry(final TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        if (TransactionUtil.transactionSynchronizationRegistry == null) {
            TransactionUtil.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
        }
    }
}
