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

import javax.enterprise.inject.spi.BeanManager;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;

/**
 * CDI Proxy Factory implementation<br> This class will create instances of CDI Proxies that will be bound into Camel Context, instead of real
 * handler class.
 */
@SuppressWarnings({ "PMD.UseSingleton", "PMD.UseProperClassLoader" })
public final class CamelBeanCdiProxyFactory {
    private CamelBeanCdiProxyFactory() {}

    /**
     * Creates CDI Proxy for given event handler class.
     *
     * @param className
     *            Fully qualified class name of the event handler.
     * @param classLoader
     *            classloader Classloader used to load this class.
     * @param beanManager
     *            beanManager BeanManager instance for this EE component.
     * @return Processor instance Proxy implementing org.apache.camel.Processor interface
     */
    public static Processor createProxy(final String className, final ClassLoader classLoader, final BeanManager beanManager) {
        return (Processor) Proxy.newProxyInstance(CamelContext.class.getClassLoader(), new Class[] { Processor.class }, new CamelBeanCdiProxy(
                className, classLoader, beanManager));
    }

}
