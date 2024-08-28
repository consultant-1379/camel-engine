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

package com.ericsson.oss.mediation.camel.flow.builder;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.mediation.flow.FlowPath;
import com.ericsson.oss.mediation.flow.FlowPathElement;
import com.ericsson.oss.mediation.flow.FlowProcessor;

/**
 * The Class FlowRouteBuilderTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class FlowRouteBuilderTest {

    private static final String PATH_ID = "id1";
    private static final String FROM_JMS = "jms:MyQueue";
    private static final String TO_CLASS_NAME = "MyClass";
    @Mock
    private FlowPath path;
    @Mock
    private FlowPathElement from;
    @Mock
    private FlowProcessor to;
    @Mock
    private RouteDefinition definition;

    private FlowRouteBuilder testClass;

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception.
     */
    @Before
    public void setUp() throws Exception {
        testClass = new FlowRouteBuilder();
        when(path.getId()).thenReturn(PATH_ID);
        final List<FlowPathElement> routeElements = new ArrayList<>();
        when(from.getURI()).thenReturn(FROM_JMS);
        routeElements.add(from);
        when(to.getClassName()).thenReturn(TO_CLASS_NAME);
        routeElements.add(to);
        when(path.getPathElements()).thenReturn(routeElements);
    }

    /**
     * Test build camel route.
     *
     * @throws Exception
     *             the exception.
     */
    @Test
    public void testBuildCamelRoute() throws Exception {
        when(path.isTransactional()).thenReturn(true);
        final RouteBuilder routeB = testClass.buildCamelRoute(path);
        Assert.assertNotNull(routeB);
        Assert.assertEquals(true, routeB instanceof CamelRouteBuilder);
    }
}
