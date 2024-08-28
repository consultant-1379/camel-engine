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

package org.jboss.as.camel.testsuite.smoke.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.mockito.Mockito;

import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.Choice;
import com.ericsson.oss.mediation.dsl.DSLRouteDefinition;
import com.ericsson.oss.mediation.engine.api.MediationEngine;
import com.ericsson.oss.mediation.engine.api.MediationEngineConstants;
import com.ericsson.oss.mediation.flow.FlowAdapter;
import com.ericsson.oss.mediation.flow.FlowPath;
import com.ericsson.oss.mediation.flow.FlowPathElement;
import com.ericsson.oss.mediation.flow.FlowProcessor;

/**
 * The Class MediationServiceMockImpl.
 */
@Stateless
@EJB(name = MediationServiceMock.MS_MOCK_JNDI_NAME, beanInterface = MediationServiceMock.class)
@Remote(MediationServiceMock.class)
public class MediationServiceMockImpl implements MediationServiceMock {

    @EJB(lookup = MediationEngineConstants.CAMEL_MEDIATION_ENGINE_JNDI)
    private MediationEngine mediationEngine;

    private List<FlowPath> createSimpleFlowPath() {
        final List<FlowPath> pathList = new ArrayList<FlowPath>();
        final FlowPath flowPath =
                createFlowPath(CAMEL_ENGINE_HANDLER_ROUTE, "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeEventHandler");
        pathList.add(flowPath);
        return pathList;
    }

    private List<FlowPath> createSimpleFlowPathWithChoiceElement() {
        final List<FlowPath> pathList = new ArrayList<FlowPath>();
        final FlowPath flowPath =
                createFlowPathWithChoiceElement(CAMEL_ENGINE_HANDLER_ROUTE, "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeEventHandler");
        pathList.add(flowPath);
        return pathList;
    }

    private List<FlowPath> createExceptionFlowPath() {
        final List<FlowPath> pathList = new ArrayList<FlowPath>();
        final FlowPath flowPath = createFlowPath(CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE,
                "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.ExceptionEventHandler");
        pathList.add(flowPath);
        return pathList;
    }

    private List<FlowPath> createExceptionFlowPathWithChoiceElement() {
        final List<FlowPath> pathList = new ArrayList<FlowPath>();
        final FlowPath flowPath = createFlowPathWithChoiceElement(CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE,
                "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.ExceptionEventHandler");
        pathList.add(flowPath);
        return pathList;
    }

    private FlowPath createFlowPath(final String routeName, final String className) {
        final List<FlowPathElement> pathElementList = new ArrayList<FlowPathElement>();
        final FlowPath flowPath = Mockito.mock(FlowPath.class, Mockito.withSettings().extraInterfaces(DSLRouteDefinition.class).serializable());
        final FlowAdapter flowAdapter = Mockito.mock(FlowAdapter.class, Mockito.withSettings().extraInterfaces(FlowPathElement.class).serializable());
        final FlowProcessor flowProcessor = Mockito.mock(FlowProcessor.class, Mockito.withSettings().extraInterfaces(FlowPathElement.class)
                .serializable());
        pathElementList.add(flowAdapter);
        pathElementList.add(flowProcessor);
        Mockito.when(flowPath.getId()).thenReturn(routeName);
        Mockito.when(flowPath.getPathElements()).thenReturn(pathElementList);
        Mockito.when(flowAdapter.getURI()).thenReturn("direct:" + routeName);
        Mockito.when(flowProcessor.getClassName()).thenReturn(className);
        return flowPath;
    }

    private FlowPath createFlowPathWithChoiceElement(final String routeName, final String className) {
        final List<FlowPathElement> pathElementList = new ArrayList<FlowPathElement>();
        final FlowPath flowPath = Mockito.mock(FlowPath.class, Mockito.withSettings().extraInterfaces(DSLRouteDefinition.class).serializable());
        final FlowAdapter flowAdapter = Mockito.mock(FlowAdapter.class, Mockito.withSettings().extraInterfaces(FlowPathElement.class).serializable());
        final FlowProcessor flowProcessor = Mockito.mock(FlowProcessor.class, Mockito.withSettings().extraInterfaces(FlowPathElement.class)
                .serializable());
        final Choice choice = Mockito.mock(Choice.class, Mockito.withSettings().extraInterfaces(FlowPathElement.class).serializable());
        pathElementList.add(flowAdapter);
        pathElementList.add(flowProcessor);
        pathElementList.add((FlowPathElement) choice);
        Mockito.when(flowPath.getId()).thenReturn(routeName);
        Mockito.when(flowPath.getPathElements()).thenReturn(pathElementList);
        Mockito.when(flowAdapter.getURI()).thenReturn("direct:" + routeName);
        Mockito.when(flowProcessor.getClassName()).thenReturn(className);
        return flowPath;
    }

    @Override
    public void createFlow(final String name) {
        if (name.equals("simpleFlowPath")) {
            mediationEngine.createFlow(createSimpleFlowPath());
        }
        if (name.equals("exceptionFlowPath")) {
            mediationEngine.createFlow(createExceptionFlowPath());
        }
    }

    @Override
    public void createFlowWithChoiceElement(final String name) {
        if (name.equals("simpleFlowPath")) {
            mediationEngine.createFlow(createSimpleFlowPathWithChoiceElement());
        }
        if (name.equals("exceptionFlowPath")) {
            mediationEngine.createFlow(createExceptionFlowPathWithChoiceElement());
        }
    }

    @Override
    public void invokeFlow(final String name, final HashMap<String, Object> headers) {
        mediationEngine.invokeFlow(name, headers);
    }

    @Override
    public void invokeTransactionalFlow(final String name, final HashMap<String, Object> headers) {
        mediationEngine.invokeTransactionalFlow(CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE, headers);
    }
}
