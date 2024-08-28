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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.camel.Exchange;
import org.jboss.as.camel.integration.processor.PathElementProcessor;
import org.jboss.as.camel.integration.processor.TypedPathElementProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.oss.itpf.common.event.ComponentEvent;
import com.ericsson.oss.itpf.common.event.handler.EventHandlerContext;
import com.ericsson.oss.itpf.common.event.handler.EventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.ResultEventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.TypedEventInputHandler;
import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * The Class CamelBeanCdiProxyTestCase.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CamelBeanCdiProxy.class)
public class CamelBeanCdiProxyTestCase {

    @Mock
    private ClassLoader cl;

    @Mock
    private BeanManager bm;

    private Class clazz;

    @Mock
    private Bean bean;

    @Mock
    private CreationalContext creationalContext;

    @Mock
    private Context context;

    @Mock
    private Exchange exchange;

    private CamelBeanCdiProxy beanCdiProxy;

    @SuppressWarnings("unchecked")
    @Test
    public void testCamelBeanCdiProxyInvoke() throws Throwable {

        final String eventHandlerClassName = TestEventInputHandler.class.getName();
        beanCdiProxy = new CamelBeanCdiProxy(eventHandlerClassName, cl, bm);

        final TestEventInputHandler eventInputHandler = new TestEventInputHandler();

        clazz = TestEventInputHandler.class;
        Mockito.when(cl.loadClass(eventHandlerClassName)).thenReturn(clazz);
        final Collection<Annotation> qualifiers = new HashSet<Annotation>();

        final Set<Bean<?>> beans = new HashSet<>();
        Mockito.when(bm.getBeans(clazz, qualifiers.toArray(new Annotation[] {}))).thenReturn(beans);
        Mockito.when(bm.resolve(beans)).thenReturn(bean);

        Mockito.when(bm.getContext(bean.getScope())).thenReturn(context);
        Mockito.when(bm.createCreationalContext(bean)).thenReturn(creationalContext);
        Mockito.when(context.get(bean, creationalContext)).thenReturn(eventInputHandler);

        final EventInputHandler flowProcessor = new TestEventInputHandler();
        final PathElementProcessor<EventInputHandler> pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor,
                eventHandlerClassName);

        final Method method = pathElementProcessor.getClass().getMethod("toString");
        beanCdiProxy.invoke(flowProcessor, method, null);

        Mockito.verify(bm, Mockito.times(1)).createCreationalContext(bean);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCamelBeanCdiProxyInvokeResultInputEvent() throws Throwable {

        final String eventHandlerClassName = TestResultEventInputHandler.class.getName();
        beanCdiProxy = new CamelBeanCdiProxy(eventHandlerClassName, cl, bm);

        final TestResultEventInputHandler resultEventInputHandler = new TestResultEventInputHandler();

        clazz = TestResultEventInputHandler.class;
        Mockito.when(cl.loadClass(eventHandlerClassName)).thenReturn(clazz);
        final Collection<Annotation> qualifiers = new HashSet<Annotation>();

        final Set<Bean<?>> beans = new HashSet<>();
        Mockito.when(bm.getBeans(clazz, qualifiers.toArray(new Annotation[] {}))).thenReturn(beans);
        Mockito.when(bm.resolve(beans)).thenReturn(bean);

        Mockito.when(bm.getContext(bean.getScope())).thenReturn(context);
        Mockito.when(bm.createCreationalContext(bean)).thenReturn(creationalContext);
        Mockito.when(context.get(bean, creationalContext)).thenReturn(resultEventInputHandler);

        final ResultEventInputHandler flowProcessor = new TestResultEventInputHandler();
        final PathElementProcessor<EventInputHandler> pathElementProcessor = new PathElementProcessor<EventInputHandler>(flowProcessor,
                eventHandlerClassName);

        final Method method = pathElementProcessor.getClass().getMethod("toString");
        beanCdiProxy.invoke(flowProcessor, method, null);

        Mockito.verify(bm, Mockito.times(1)).createCreationalContext(bean);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCamelBeanCdiProxyInvokeTypedInputEvent() throws Throwable {

        final String eventHandlerClassName = TestEventInputHandler.class.getName();
        beanCdiProxy = new CamelBeanCdiProxy(eventHandlerClassName, cl, bm);

        final TestTypedEventInputHandler eventInputHandler = new TestTypedEventInputHandler();
        clazz = TestTypedEventInputHandler.class;
        Mockito.when(cl.loadClass(eventHandlerClassName)).thenReturn(clazz);
        final Collection<Annotation> qualifiers = new HashSet<Annotation>();

        final Set<Bean<?>> beans = new HashSet<>();
        Mockito.when(bm.getBeans(clazz, qualifiers.toArray(new Annotation[] {}))).thenReturn(beans);
        Mockito.when(bm.resolve(beans)).thenReturn(bean);

        Mockito.when(bm.getContext(bean.getScope())).thenReturn(context);
        Mockito.when(bm.createCreationalContext(bean)).thenReturn(creationalContext);
        Mockito.when(context.get(bean, creationalContext)).thenReturn(eventInputHandler);

        final TypedEventInputHandler flowProcessor = new TestTypedEventInputHandler();
        final TypedPathElementProcessor<TypedEventInputHandler> typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(
                flowProcessor, eventHandlerClassName);

        final Method method = typedPathElementProcessor.getClass().getMethod("toString");
        beanCdiProxy.invoke(flowProcessor, method, null);
        Mockito.verify(bm, Mockito.times(1)).createCreationalContext(bean);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = IllegalStateException.class)
    public void testCamelBeanCdiProxyHandlerNotSupported() throws Throwable {
        final Exception nestedException = new Exception("Msg");
        final IllegalStateException exception = new IllegalStateException(nestedException);
        final String eventHandlerClassName = TestEventInputHandler.class.getName();
        beanCdiProxy = new CamelBeanCdiProxy(eventHandlerClassName, cl, bm);

        clazz = TestTypedEventInputHandler.class;
        Mockito.when(cl.loadClass(eventHandlerClassName)).thenReturn(clazz);
        final Collection<Annotation> qualifiers = new HashSet<Annotation>();

        final Set<Bean<?>> beans = new HashSet<>();
        Mockito.when(bm.getBeans(clazz, qualifiers.toArray(new Annotation[] {}))).thenReturn(beans);
        Mockito.when(bm.resolve(beans)).thenReturn(bean);

        Mockito.when(bm.getContext(bean.getScope())).thenReturn(context);
        Mockito.when(bm.createCreationalContext(bean)).thenReturn(creationalContext);
        Mockito.when(context.get(bean, creationalContext)).thenReturn(new Object());
        PowerMockito.whenNew(IllegalStateException.class).withArguments(Matchers.anyString()).thenReturn(exception);

        final TypedEventInputHandler flowProcessor = new TestTypedEventInputHandler();
        final TypedPathElementProcessor<TypedEventInputHandler> typedPathElementProcessor = new TypedPathElementProcessor<TypedEventInputHandler>(
                flowProcessor, eventHandlerClassName);

        final Method method = typedPathElementProcessor.getClass().getMethod("toString");
        beanCdiProxy.invoke(flowProcessor, method, null);
        Mockito.verify(bm, Mockito.times(1)).createCreationalContext(bean);
    }

    /**
     * The Class TestEventInputHandler.
     */
    @EventHandler(contextName = "test")
    public class TestEventInputHandler implements EventInputHandler {

        private static final long serialVersionUID = 1L;

        @Override
        public void destroy() {}

        @Override
        public void onEvent(final Object object) {}

        @Override
        public void init(final EventHandlerContext context) {}

        @Override
        public Object onEventWithResults(final Object inputEvent) {
            return null;
        }
    }

    /**
     * The Class TestResultEventInputHandler.
     */
    @EventHandler(contextName = "test")
    public class TestResultEventInputHandler implements ResultEventInputHandler {

        private static final long serialVersionUID = 1L;

        @Override
        public void destroy() {}

        @Override
        public void onEvent(final Object object) {}

        @Override
        public void init(final EventHandlerContext context) {}

        @Override
        public Object onEventWithResult(final Object inputEvent) {
            return null;
        }

        @Override
        public Object onEventWithResults(final Object inputEvent) {
            return null;
        }
    }

    /**
     * The Class TestTypedEventInputHandler.
     */
    @EventHandler(contextName = "test")
    public class TestTypedEventInputHandler implements TypedEventInputHandler {

        private static final long serialVersionUID = 1L;

        @Override
        public void destroy() {}

        @Override
        public void init(final EventHandlerContext context) {}

        @Override
        public ComponentEvent onEvent(final ComponentEvent event) {
            return null;
        }
    }
}
