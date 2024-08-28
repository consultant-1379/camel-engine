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

package org.jboss.as.camel.integration.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.camel.Processor;
import org.jboss.as.camel.integration.processor.PathElementProcessor;
import org.jboss.as.camel.integration.processor.TypedPathElementProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.ResultEventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.TypedEventInputHandler;

/**
 * CDI Proxy for camel beans<br> Instance of this class will be bound into camel context, and upon first invocation, it will use CDI and BeanManager
 * of the EE component to which this handler belongs, to obtain new instance of the EventHandler.
 * All logging is commented out for best performance. Enable logging *ONLY* on development env.
 */
public class CamelBeanCdiProxy implements InvocationHandler {

    private static final String CREATING_UNTYPED = "Creating org.apache.camel.Processor from {} as PathElementProcessor<EventInputHandler>";

    private static final String CREATING_TYPED = "Creating org.apache.camel.Processor from {} as TypedPathElementProcessor<TypedEventInputHandler>";

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelBeanCdiProxy.class);

    private final ClassLoader classLoader;
    private final String className;
    private final BeanManager beanManager;

    /**
     * Full argument constructor for proxy class.
     *
     * @param className
     *            classname Fully qualified class name of the event handler.
     * @param classLoader
     *            classloader Classloader that will be used to load this class.
     * @param beanManager
     *            bean manager BeanManager instance for this EE component.
     */
    public CamelBeanCdiProxy(final String className, final ClassLoader classLoader, final BeanManager beanManager) {
        LOGGER.trace("Constructing new proxy instance for event handler {}, using classloader {} and bean manager {}", className, classLoader,
                beanManager);
        this.classLoader = classLoader;
        this.className = className;
        this.beanManager = beanManager;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        LOGGER.trace("----->{}--->Proxy is delegating method[{}] call to the real object", this.className, method);

        final ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        LOGGER.trace("Current TCL: {}", origClassLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        Bean bean = null;
        Processor processor = null;
        CreationalContext creationalContext = null;
        Object handler = null;
        Object retVal = null;
        try {
            LOGGER.trace("New TCL is {}", Thread.currentThread().getContextClassLoader());
            bean = obtainBeanRef();
            final Context context = beanManager.getContext(bean.getScope());
            creationalContext = beanManager.createCreationalContext(bean);
            handler = context.get(bean, creationalContext);
            LOGGER.trace("HANDLER: {}", handler);
            processor = createProcessorFromHandler(handler, className);
            LOGGER.trace("About to invoke the method:{} with args:{}", method, args);
            retVal = method.invoke(processor, args);
            LOGGER.trace("Method is returning {}", retVal);
        } finally {
            LOGGER.trace("About to call destroy on the bean for {} {}.", handler, creationalContext);
            bean.destroy(handler, creationalContext);
            creationalContext.release();
            LOGGER.trace("Bean destroyed, now setting back classloader.");
            Thread.currentThread().setContextClassLoader(origClassLoader);
            LOGGER.trace("After restoring, TCL is {}", Thread.currentThread().getContextClassLoader());
        }
        return retVal;
    }

    @SuppressWarnings({ "rawtypes" })
    private Bean obtainBeanRef() throws ClassNotFoundException {
        final Class<?> clazz = classLoader.loadClass(className);
        final Collection<Annotation> qualifiers = new HashSet<Annotation>();
        for (final Annotation annotation : clazz.getAnnotations()) {
            if (beanManager.isQualifier(annotation.annotationType())) {
                LOGGER.trace("Found qualifier annotation: {}", annotation.annotationType());
                qualifiers.add(annotation);
            }
        }
        final Set<Bean<?>> beans = this.beanManager.getBeans(classLoader.loadClass(className), qualifiers.toArray(new Annotation[] {}));
        LOGGER.trace("Number of injectable beans: {}", beans.size());
        return beanManager.resolve(beanManager.getBeans(classLoader.loadClass(className), qualifiers.toArray(new Annotation[] {})));
    }

    private Processor createProcessorFromHandler(final Object handlerInstance, final String classDotName) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        Processor processor = null;
        final Class<?> clazz = handlerInstance.getClass();
        if (TypedEventInputHandler.class.isAssignableFrom(clazz)) {
            LOGGER.trace(CREATING_TYPED, classDotName);
            processor = new TypedPathElementProcessor<TypedEventInputHandler>((TypedEventInputHandler) handlerInstance, className);
        } else if (ResultEventInputHandler.class.isAssignableFrom(clazz)) {
            LOGGER.trace(CREATING_UNTYPED, classDotName);
            processor = new PathElementProcessor<ResultEventInputHandler>((ResultEventInputHandler) handlerInstance, className);
        } else if (EventInputHandler.class.isAssignableFrom(clazz)) {
            LOGGER.trace(CREATING_UNTYPED, classDotName);
            processor = new PathElementProcessor<EventInputHandler>((EventInputHandler) handlerInstance, className);
        } else {
            throw new IllegalStateException("Handler instance " + classDotName
                    + " is not assignable from EventInputHandler or TypedEventInputHandler, "
                    + "unable to create instance of org.apache.camel.Processor from it.");
        }
        return processor;
    }
}
