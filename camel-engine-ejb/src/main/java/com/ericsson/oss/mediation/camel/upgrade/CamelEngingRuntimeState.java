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

package com.ericsson.oss.mediation.camel.upgrade;

import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Singleton bean describing runtime state of this engine. It stores state info about upgrade.
 */
@Singleton
@Startup
public class CamelEngingRuntimeState {

    private boolean isUnderUpgrade;

    /**
     * Is this engine under upgrade.
     *
     * @return the isUnderUpgrade
     */
    public boolean isUnderUpgrade() {
        return isUnderUpgrade;
    }

    /**
     * Set the flag to true when under upgrade.
     *
     * @param isUnderUpgrade
     *            the isUnderUpgrade to set.
     */
    public void setUnderUpgrade(final boolean isUnderUpgrade) {
        this.isUnderUpgrade = isUnderUpgrade;
    }
}
