/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.camel.flow.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.FlowAttribute;
import com.ericsson.oss.mediation.flow.FlowPathElement;

/**
 * The Class JmsAdapterConfigurationTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class JmsAdapterConfigurationTest {

    private static final String FROM_JMS = "jms:MyQueue";
    private static final String ATTRIBUTE_URI = "config:false";
    private static final String ATTRIBUTE_SFWK_URI = "config:sfwk";
    private static final String ATTRIBUTE_NAME = "mapJmsMessage";
    private static final String MAP_JMS_MESSAGE = "?mapJmsMessage=false";

    private JmsAdapterConfiguration test;
    @Mock
    private FlowPathElement from;
    @Mock
    private FlowAttribute attribute;
    @Mock
    private FlowAttribute attributeSfwk;

    @Before
    public void setUp() throws Exception {
        System.setProperty(FlowConstants.CONFIG_QUEUE_SYS_PROPERTY, "MyQueue");
        test = new JmsAdapterConfiguration();
        when(from.getURI()).thenReturn(FROM_JMS);
        when(attribute.getSourceURI()).thenReturn(ATTRIBUTE_URI);
        when(attribute.getAttributeName()).thenReturn(ATTRIBUTE_NAME);
        final LinkedList<FlowAttribute> attribs = new LinkedList<FlowAttribute>();
        attribs.add(attribute);
        when(from.getAttributes()).thenReturn(attribs);
    }

    @Test
    public void testConfigAttributes() {
        final String returnValue = test.resolveConfigParametersForEndPoint(from);
        assertEquals(FROM_JMS + MAP_JMS_MESSAGE, returnValue);
    }

    @Test
    public void testNoAttributes() {
        when(from.getAttributes()).thenReturn(null);
        final String returnValue = test.resolveConfigParametersForEndPoint(from);
        assertEquals(FROM_JMS, returnValue);
    }

    @Test
    public void testNoConfigAttributes() {
        when(attribute.getSourceURI()).thenReturn("dps:fdn");
        final String returnValue = test.resolveConfigParametersForEndPoint(from);
        assertEquals(FROM_JMS, returnValue);
    }

    @Test
    public void testSfwkConfigWithStaticQueue() {
        when(attributeSfwk.getSourceURI()).thenReturn(ATTRIBUTE_SFWK_URI);
        when(attributeSfwk.getAttributeName()).thenReturn(FlowConstants.CONFIG_QUEUE_SYS_PROPERTY);
        final LinkedList<FlowAttribute> attribs = new LinkedList<FlowAttribute>();
        attribs.add(attributeSfwk);
        when(from.getAttributes()).thenReturn(attribs);
        final String returnValue = test.resolveConfigParametersForEndPoint(from);
        assertEquals(FROM_JMS, returnValue);
    }
}
