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

package org.jboss.camel.tx;

/**
 * Transaction policies available.
 */
public final class TransactionPolicy {

    /**
     * REQUIRES_NEW propagation.
     */
    public static final String PROPAGATION_REQUIRES_NEW = "PROPAGATION_REQUIRES_NEW";
    /**
     * REQUIRED propagation.
     */
    public static final String PROPAGATION_REQUIRED = "PROPAGATION_REQUIRED";

    private TransactionPolicy() {}
}
