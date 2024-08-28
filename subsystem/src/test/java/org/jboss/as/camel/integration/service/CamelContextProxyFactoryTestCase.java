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
 * The Class CamelContextProxyFactoryTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
// for service controller
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CamelContextProxyFactoryTestCase {

    private static final String DEFAULT_CONTEXT_NAME = "CamelContext1";

    private static final String DEPLOYMENT_NAME = "test.war";

    @Mock
    private ServiceContainer sc;

    @Mock
    private ServiceController serviceController;

    @Mock
    private CamelContextIntegrationService service;

    @Mock
    private TransactionManager txMgr;

    private CamelContextService camelContextService;

    /**
     * Test create proxy when camel context is shared.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateProxyWhenCamelContextIsShared() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        final CamelContextService ccs = new CamelContextService(true, DEFAULT_CONTEXT_NAME);
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        Mockito.when(sc.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(serviceController);
        Mockito.when(serviceController.getService()).thenReturn(service);
        Mockito.when(service.getCamelService()).thenReturn(ccs);
        final CamelContext ctx = CamelContextProxyFactory.createProxy(DEPLOYMENT_NAME);
        Assert.assertNotNull(ctx);
        Assert.assertEquals(DEFAULT_CONTEXT_NAME, ctx.getName());
    }

    /**
     * Test create proxy when camel context is not shared.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateProxyWhenCamelContextIsNotShared() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        final CamelContextService ccs = new CamelContextService(false, DEFAULT_CONTEXT_NAME);
        ServiceContainerUtil.setCurrentServiceContainer(sc);
        Mockito.when(sc.getService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(serviceController);
        Mockito.when(serviceController.getService()).thenReturn(service);
        Mockito.when(service.getCamelService()).thenReturn(ccs);
        final DeploymentMetaInfo dmi = ccs.createCamelContextForDeployment(DEPLOYMENT_NAME);
        ccs.registerCamelContext(dmi, DEPLOYMENT_NAME);
        final CamelContext ctx = CamelContextProxyFactory.createProxy(DEPLOYMENT_NAME);
        Assert.assertNotNull(ctx);
        Assert.assertEquals(1, ccs.instantiatedContexts.size());
        Assert.assertEquals("test.war-camel-context", ctx.getName());
    }
}
