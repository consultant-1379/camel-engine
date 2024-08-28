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

package com.ericsson.oss.mediation.camel.ejb;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * The Class CamelRouteProducerTemplateTest.
 */
@RunWith(PowerMockRunner.class)
public class CamelRouteProducerTemplateTest {

    @InjectMocks
    private CamelRouteProducerTemplate camelRouteProducerTemplate;
    @Mock
    private CamelContext camelContext;

    @Mock
    private ProducerTemplate template;

    @Before
    public void setUp() {
        when(camelContext.createProducerTemplate()).thenReturn(template);
    }

    @Test
    public void testCreateProducerTemplate() throws Exception {

        PowerMockito.doNothing().when(template).start();
        camelRouteProducerTemplate.postConstruct();
        verify(camelContext, times(1)).createProducerTemplate();
        verify(template, times(1)).start();
        assertNotNull(camelRouteProducerTemplate.getTemplate());
    }

    @Test
    public void preDestroy() throws Exception {
        camelRouteProducerTemplate.preDestroy();
        PowerMockito.doNothing().when(template).stop();
        verify(template, times(1)).stop();
    }

    @Test
    public void testCreateProducerTemplateException() throws Exception {
        PowerMockito.doThrow(new Exception()).when(template).start();
        camelRouteProducerTemplate.postConstruct();
        verify(camelContext, times(1)).createProducerTemplate();
    }

    @Test
    public void preDestroyException() throws Exception {
        PowerMockito.doThrow(new Exception()).when(template).stop();
        camelRouteProducerTemplate.preDestroy();
        verify(template, times(1)).stop();
    }
}
