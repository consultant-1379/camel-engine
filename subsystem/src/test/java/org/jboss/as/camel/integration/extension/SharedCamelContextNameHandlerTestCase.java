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

package org.jboss.as.camel.integration.extension;

import org.jboss.as.camel.integration.service.CamelContextIntegrationService;
import org.jboss.as.controller.AbstractWriteAttributeHandler.HandbackHolder;
import org.jboss.as.controller.OperationContext;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class SharedCamelContextNameHandlerTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class SharedCamelContextNameHandlerTestCase {

    @Mock
    OperationContext opContext;

    @Mock
    ModelNode operation;

    @Mock
    ModelNode resolvedValue;

    @Mock
    ModelNode currentValue;

    @Mock
    ModelNode valueToRestore;

    @Mock
    ModelNode valueToRevert;

    @Mock
    HandbackHolder<Void> handbackHolder;

    @Mock
    ServiceRegistry serviceRegistry;

    @Mock
    ServiceController serviceController;

    @Mock
    CamelContextIntegrationService camelIntegrationService;

    @Test
    public void testApplyUpdateToRuntime() throws Exception {

        Mockito.when(opContext.getServiceRegistry(true)).thenReturn(serviceRegistry);
        Mockito.when(serviceRegistry.getRequiredService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                serviceController);
        Mockito.when(serviceController.getValue()).thenReturn(camelIntegrationService);
        Mockito.when(resolvedValue.asString()).thenReturn("CamelContext1");
        final String attributeName = "attrbute1";
        final Boolean result = SharedCamelContextNameHandler.INSTANCE.applyUpdateToRuntime(opContext, operation, attributeName, resolvedValue,
                currentValue, handbackHolder);
        Mockito.verify(camelIntegrationService).setCamelContextName("CamelContext1");
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testRevertUpdateToRuntime() throws Exception {
        final Void v = null;
        Mockito.when(opContext.getServiceRegistry(true)).thenReturn(serviceRegistry);
        Mockito.when(serviceRegistry.getRequiredService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME)).thenReturn(
                serviceController);
        Mockito.when(serviceController.getValue()).thenReturn(camelIntegrationService);
        Mockito.when(valueToRestore.asString()).thenReturn("CamelContext1");
        final String attributeName = "attrbute1";
        SharedCamelContextNameHandler.INSTANCE.revertUpdateToRuntime(opContext, operation, attributeName, valueToRestore, valueToRevert, v);
        Mockito.verify(camelIntegrationService).setCamelContextName("CamelContext1");
    }
}
