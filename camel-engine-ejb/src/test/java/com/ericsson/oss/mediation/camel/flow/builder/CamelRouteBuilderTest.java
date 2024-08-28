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

package com.ericsson.oss.mediation.camel.flow.builder;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.camel.model.RouteDefinition;
import org.jboss.camel.exception.CamelEngineRuntimeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Choice;
import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Expression;
import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.FlowAttribute;
import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Handler;
import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Option;
import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Step;
import com.ericsson.oss.mediation.flow.FlowAdapter;
import com.ericsson.oss.mediation.flow.FlowPath;
import com.ericsson.oss.mediation.flow.FlowPathElement;
import com.ericsson.oss.mediation.flow.FlowProcessor;

/**
 * The Class CamelRouteBuilderTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamelRouteBuilderTest {

    private static final String PATH_ID = "id1";
    private static final String FROM_JMS = "jms:MyQueue";
    private static final String MAP_JMS_MESSAGE = "?mapJmsMessage=false";
    private static final String TO_CLASS_NAME = "MyClass";
    private static final String TRANSACTED = "transacted";
    private static final String TRANSACTION_ATTRIBUTE = "PROPAGATION_REQUIRED";
    private static final String ATTRIBUTE_URI = "config:false";
    private static final String ATTRIBUTE_NAME = "mapJmsMessage";

    private static final String WHEN_EXPRESSION = "flow=true";
    private static final String CHOICE_HANDLER_NAME = "com.ericsson.handler";

    @InjectMocks
    private CamelRouteBuilder camelRouteBuilder;

    @Mock
    private FlowPath path;
    @Mock
    private FlowPathElement from;
    @Mock
    private FlowProcessor to;
    @Mock
    private RouteDefinition definition;
    @Mock
    private FlowAttribute attribute;
    @Mock
    private FlowAdapter flowAdapter;
    @Mock
    private Logger loggerMock;

    @Mock
    private Option option;
    @Mock
    private Expression expressionValue;
    @Mock
    private Handler handlerValue;
    @Mock
    private Step pathElementValueAsStep;
    @Mock(extraInterfaces = { Choice.class })
    private FlowPathElement flowChoiceImpl;

    private List<Option> options;
    private List<FlowPathElement> routeElements;

    @Before
    public void setUp() throws Exception {
        routeElements = new ArrayList<>();
        when(path.getId()).thenReturn(PATH_ID);
        when(from.getURI()).thenReturn(FROM_JMS);
        when(attribute.getSourceURI()).thenReturn(ATTRIBUTE_URI);
        when(attribute.getAttributeName()).thenReturn(ATTRIBUTE_NAME);
        final LinkedList<FlowAttribute> attribs = new LinkedList<FlowAttribute>();
        attribs.add(attribute);
        when(from.getAttributes()).thenReturn(attribs);
        routeElements.add(from);
        when(to.getClassName()).thenReturn(TO_CLASS_NAME);
        routeElements.add(to);
        routeElements.add(flowAdapter);
        when(path.getPathElements()).thenReturn(routeElements);
        options = new ArrayList<>();
        when(option.getExpression()).thenReturn(expressionValue);
        when(expressionValue.getExpression()).thenReturn(WHEN_EXPRESSION);
        when(option.getPathElement()).thenReturn(pathElementValueAsStep);
        when(pathElementValueAsStep.getHandler()).thenReturn(handlerValue);
        when(handlerValue.getClassName()).thenReturn(CHOICE_HANDLER_NAME);
        options.add(option);
        when(((Choice) flowChoiceImpl).getOptions()).thenReturn(options);
        routeElements.add(flowChoiceImpl);
    }

    @Test
    public void testBuildRouteDefinitionNonTransaction() {
        when(path.isTransactional()).thenReturn(false);
        final RouteDefinition def = (RouteDefinition) invokePrivateMethod("buildRouteDefinition", Iterator.class, routeElements.iterator());
        Assert.assertEquals(PATH_ID, def.getId());
        Assert.assertEquals(def.getInputs().get(0).getLabel(), FROM_JMS + MAP_JMS_MESSAGE);
        Assert.assertEquals(true, def.getOutputs().get(0).getLabel().contains(TO_CLASS_NAME));
    }

    @Test
    public void testBuildRouteDefinitionTransaction() {
        when(path.isTransactional()).thenReturn(true);
        final RouteDefinition def = (RouteDefinition) invokePrivateMethod("buildRouteDefinition", Iterator.class, routeElements.iterator());
        Assert.assertEquals(PATH_ID, def.getId());
        Assert.assertEquals(def.getInputs().get(0).getLabel(), FROM_JMS + MAP_JMS_MESSAGE);
        Assert.assertEquals(true, def.getOutputs().get(0).getLabel().contains(TRANSACTED));
        Assert.assertEquals(true, def.getOutputs().get(0).getLabel().contains(TRANSACTION_ATTRIBUTE));
    }

    @Test
    public void testChoiceImplementationElementCode() {
        when(path.isTransactional()).thenReturn(true);
        final RouteDefinition def = (RouteDefinition) invokePrivateMethod("buildRouteDefinition", Iterator.class, routeElements.iterator());
        Assert.assertEquals(PATH_ID, def.getId());
        Assert.assertEquals(true, def.getOutputs().get(0).getOutputs().toString().contains(CHOICE_HANDLER_NAME));
        final String left = WHEN_EXPRESSION.split("=")[0];
        final String right = WHEN_EXPRESSION.split("=")[1];
        final String whenExpression = "header{" + left + "} == " + right;
        Assert.assertEquals(true, def.getOutputs().get(0).getOutputs().toString().contains(whenExpression));
    }

    @Test
    public void testBuildRouteConfig() {
        camelRouteBuilder.configure();
        verify(path, times(1)).getPathElements();
    }

    @Test(expected = CamelEngineRuntimeException.class)
    public void testBuildRouteConfigResourceException() {
        when(path.getPathElements()).thenReturn(new ArrayList<FlowPathElement>());
        camelRouteBuilder.configure();
    }

    private Object invokePrivateMethod(final String methodName, final Class<?> methodAttribute, final Object attributeValue) {
        Object result = null;
        try {
            final Method privateMethod = camelRouteBuilder.getClass().getDeclaredMethod(methodName, methodAttribute);
            privateMethod.setAccessible(true);
            result = privateMethod.invoke(camelRouteBuilder, attributeValue);
        } catch (final NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }
}
