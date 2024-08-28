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

package org.jboss.as.camel.testsuite.smoke.deployment.common;

import java.util.concurrent.CountDownLatch;

import javax.ejb.Singleton;

/**
 * The Class LatchSingleton.
 */
@Singleton
public class LatchSingleton {

    private CountDownLatch latch;

    public void setupLatch(final int times) {
        latch = new CountDownLatch(times);
    }

    public CountDownLatch getLatch() {
        return latch;
    }
}
