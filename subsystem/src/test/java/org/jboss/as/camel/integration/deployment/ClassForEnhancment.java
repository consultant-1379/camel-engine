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

package org.jboss.as.camel.integration.deployment;

import org.apache.camel.CamelContext;

/**
 * The Class ClassForEnhancment.
 */
public class ClassForEnhancment {
    private CamelContext ctx;

    public CamelContext getCtx() {
        return ctx;
    }

    public void setCtx(final CamelContext ctx) {
        this.ctx = ctx;
    }

}
