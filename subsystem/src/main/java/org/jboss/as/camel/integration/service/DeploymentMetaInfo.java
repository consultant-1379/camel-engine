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

import org.apache.camel.CamelContext;
import org.apache.camel.util.jndi.JndiContext;

/**
 * Deployment meta information.
 */
public class DeploymentMetaInfo {

    private CamelContext camelContext;
    private JndiContext registry;

    /**
     * Full arg constructor.
     *
     * @param camelContext
     *            CamelContext that will be used for this deployment.
     * @param registry
     *            Jndi registry used by this camel context.
     */
    public DeploymentMetaInfo(final CamelContext camelContext, final JndiContext registry) {
        super();
        this.camelContext = camelContext;
        this.registry = registry;
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

    public void setCamelContext(final CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public JndiContext getRegistry() {
        return registry;
    }

    public void setRegistry(final JndiContext registry) {
        this.registry = registry;
    }
}
