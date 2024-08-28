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

import java.lang.reflect.Proxy;

import org.apache.camel.CamelContext;

/**
 * Factory for camel context proxies.
 */
@SuppressWarnings({ "PMD.UseSingleton", "PMD.UseProperClassLoader" })
public final class CamelContextProxyFactory {
    private CamelContextProxyFactory() {}

    /**
     * Produce camel context proxy for this deployment.
     *
     * @param deploymentName
     *            Name of the deployment.
     * @return CamelContext proxy instance
     */
    public static CamelContext createProxy(final String deploymentName) {
        return (CamelContext) Proxy.newProxyInstance(CamelContext.class.getClassLoader(), new Class[] { CamelContext.class }, new CamelContextProxy(
                deploymentName));
    }
}
