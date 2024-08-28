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

package org.jboss.as.camel.integration.service;

import javax.transaction.TransactionManager;

import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class CamelIntegrationServiceTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamelIntegrationServiceTestCase {

    CamelContextIntegrationService service;

    @Mock
    private StartContext context;

    @Mock
    private StopContext stopContext;

    @Mock
    private TransactionManager transactionManager;

    /**
     * Test camel integration service instance creation_verify_all_attributes_initialized_ whe n_ sharin g_ context.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCamelIntegrationServiceInstanceCreationVerifyAllAttributesInitializedWhenSharingContext() throws Exception {
        final String contextName = "testContext";
        service = new CamelContextIntegrationService(true, contextName);
        service.setSharedCamelDeployment(true);
        service.setCamelContextName(contextName);
        Assert.assertNotNull(service.getValue());
        Assert.assertNull(service.getCamelService());
        Assert.assertTrue(service.isSharedCamelDeployment());
        Assert.assertEquals(contextName, service.getCamelContextName());
    }

    /**
     * Test camel integration service_ start.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCamelIntegrationServiceStart() throws Exception {
        final String contextName = "testContext";
        TransactionUtil.setTransactionManager(transactionManager);
        service = new CamelContextIntegrationService(true, contextName);
        service.start(context);
        service.setCamelContextName(contextName);
        Assert.assertNotNull(service.getValue());
        Assert.assertNotNull(service.getCamelService());
        Assert.assertTrue(service.isSharedCamelDeployment());
        Assert.assertEquals(contextName, service.getCamelContextName());
    }

    /**
     * Test camel integration service_ stop.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCamelIntegrationServiceStop() throws Exception {
        final String contextName = "testContext";
        TransactionUtil.setTransactionManager(transactionManager);
        service = new CamelContextIntegrationService(true, contextName);
        service.start(context);

        service.stop(stopContext);
        Assert.assertNotNull(service.getValue());
        Assert.assertNotNull(service.getCamelService());
        Assert.assertTrue(service.isSharedCamelDeployment());
        Assert.assertEquals(contextName, service.getCamelContextName());
    }
}
