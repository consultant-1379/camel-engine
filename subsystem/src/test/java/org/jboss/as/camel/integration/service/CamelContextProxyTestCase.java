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

import java.lang.reflect.InvocationTargetException;

import javax.transaction.TransactionManager;

import org.apache.camel.CamelContext;
import org.jboss.as.server.ServiceContainerUtil;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class CamelContextProxyTestCase.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(MockitoJUnitRunner.class)
public class CamelContextProxyTestCase {

    private static final String DEFAULT_CONTEXT_NAME = "CamelContext1";
    private static final String DEPLOYMENT_NAME = "test.war";

    @Mock
    private TransactionManager txMgr;

    @Mock
    private ServiceController serviceController;

    @Mock
    private CamelContextIntegrationService service;

    @Mock
    private ServiceContainer sc;

    private CamelContextService ccs;

    /**
     * Test camel context proxy_when_ came l_ contex t_ i s_ shared.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCamelContextProxyWhenContextIsShared() throws Exception {
        initSharedContext();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        Assert.assertNotNull(proxy);
        Assert.assertNotNull(proxy.camelContext);
        Assert.assertEquals(DEFAULT_CONTEXT_NAME, proxy.camelContext.getName());
    }

    /**
     * Test create proxy_when_ came l_ contex t_ i s_ no t_ shared.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateProxyWhenContextIsNotShared() throws Exception {
        initUnsharedContext();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        Assert.assertNotNull(proxy);
        Assert.assertEquals("test.war-camel-context", proxy.camelContext.getName());
    }

    /**
     * Test proxy_invoke_when_ contex t_ i s_ no t_ shared.
     *
     * @throws Exception
     *             the exception
     * @throws Throwable
     *             the throwable
     */
    @Test
    public void testProxyInvokeWhenContextIsNotShared() throws Exception, Throwable {
        initUnsharedContext();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenReturn(DEPLOYMENT_NAME + "-camel-context");
        final String result = (String) proxy.invoke(this, CamelContext.class.getMethod("getName"), null);
        Mockito.verify(mock, Mockito.times(1)).getName();
        Assert.assertEquals(DEPLOYMENT_NAME + "-camel-context", result);
    }

    /**
     * Test proxy_invoke_when_ contex t_ i s_ shared.
     *
     * @throws Exception
     *             the exception
     * @throws Throwable
     *             the throwable
     */
    @Test
    public void testProxyInvokeWhenContextIsShared() throws Exception, Throwable {
        initSharedContext();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenReturn(DEFAULT_CONTEXT_NAME);
        final String result = (String) proxy.invoke(this, CamelContext.class.getMethod("getName"), null);
        Mockito.verify(mock, Mockito.times(1)).getName();
        Assert.assertEquals(DEFAULT_CONTEXT_NAME, result);
    }

    /**
     * Test proxy_invoke_when_ invocatio n_ targe t_ exception.
     *
     * @throws Exception
     *             the exception
     * @throws Throwable
     *             the throwable
     */
    @Test(expected = InvocationTargetException.class)
    public void testProxyInvokeWhenInvocationTargetException() throws Exception, Throwable {
        initSharedContext();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenThrow(InvocationTargetException.class);
        proxy.invoke(this, CamelContext.class.getMethod("getName"), null);
    }

    /**
     * Test proxy_invoke_when_ exception.
     *
     * @throws Exception
     *             the exception
     * @throws Throwable
     *             the throwable
     */
    @Test(expected = Exception.class)
    public void testProxyInvokeWhenException() throws Exception, Throwable {
        initSharedContext();
        final CamelContextProxy proxy = new CamelContextProxy(DEPLOYMENT_NAME);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        proxy.camelContext = mock;
        Mockito.when(mock.getName()).thenThrow(Exception.class);
        proxy.invoke(this, CamelContext.class.getMethod("getName"), null);
    }

    /**
     * Init_unshared_context.
     *
     * @throws Exception
     *             the exception
     */
    private void initUnsharedContext() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        final CamelContextService camelContext = new CamelContextService(false, DEFAULT_CONTEXT_NAME);
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        Mockito.when(sc.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(serviceController);
        Mockito.when(serviceController.getService()).thenReturn(service);
        Mockito.when(service.getCamelService()).thenReturn(camelContext);
        final DeploymentMetaInfo dmi = camelContext.createCamelContextForDeployment(DEPLOYMENT_NAME);
        camelContext.registerCamelContext(dmi, DEPLOYMENT_NAME);
    }

    /**
     * Init_shared_context.
     *
     * @throws Exception
     *             the exception
     */
    private void initSharedContext() throws Exception {
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        TransactionUtil.setTransactionManager(txMgr);
        ccs = new CamelContextService(true, DEFAULT_CONTEXT_NAME);
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        Mockito.when(sc.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(serviceController);
        Mockito.when(serviceController.getService()).thenReturn(service);
        Mockito.when(service.getCamelService()).thenReturn(ccs);
    }
}
