/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.camel.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.transaction.TransactionSynchronizationRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor implementation to log transaction ID.
 */
@Interceptor
@TransactionLoggerBinding
public class TransactionIdLoggerInterceptor {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TransactionIdLoggerInterceptor.class);

    private static final String TX_SYNC_REG_JNDI_NAME = "java:comp/TransactionSynchronizationRegistry";

    /**
     * Internal interceptor that logs Transaction key to ensure which
     * transactions are being used/joined.
     *
     * @param ctx
     *            invocation context.
     * @return Proceed to the next interceptor in the interceptor chain. Returns
     *         value of the next method in the chain
     * @throws Exception
     *             Exception.
     */
    @AroundInvoke
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public Object logTransactionId(final InvocationContext ctx)
            throws Exception {
        if (LOGGER.isTraceEnabled()) {
            final InitialContext context = new InitialContext();
            final TransactionSynchronizationRegistry txSyncReg = (TransactionSynchronizationRegistry) context
                    .lookup(TX_SYNC_REG_JNDI_NAME);
            final Object txKey = txSyncReg.getTransactionKey();
            if (txKey != null) {
                LOGGER.trace("Transaction key: {} for method: {}",
                        txKey.toString(), ctx.getMethod());
            } else {
                LOGGER.trace("No Transaction key for method: {}",
                        ctx.getMethod());
            }
        }
        return ctx.proceed();
    }
}
