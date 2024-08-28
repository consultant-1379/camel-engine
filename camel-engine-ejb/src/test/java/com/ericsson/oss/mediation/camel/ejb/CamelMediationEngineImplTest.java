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

package com.ericsson.oss.mediation.camel.ejb;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Endpoint;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.hamcrest.CoreMatchers;
import org.jboss.camel.constant.GenericConstant;
import org.jboss.camel.exception.CamelEngineRuntimeException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import com.ericsson.oss.mediation.camel.flow.builder.FlowRouteBuilder;
import com.ericsson.oss.mediation.camel.upgrade.CamelEngingRuntimeState;
import com.ericsson.oss.mediation.flow.FlowPath;

/**
 * The Class CamelMediationEngineImplTest.
 */
@RunWith(PowerMockRunner.class)
public class CamelMediationEngineImplTest {

    private static final String MSG_MS_UNDER_UPGRADE = "Unable to process the request because Camel Engine is upgrading...";

    private static final String FLOW_PATH_MOCKED_ID = "flowPathMockedId";

    private final CamelMediationEngineImpl mediationImplementation = new CamelMediationEngineImpl();

    @Mock
    private CamelContext camelContextMock;

    @Mock
    private FlowRouteBuilder flowBuilder;

    @Mock
    private List<FlowPath> flowListPathMocked;

    @Mock
    private Iterator<FlowPath> iteratorMocked;

    @Mock
    private FlowPath flowPathMocked;

    @Mock
    private Map<String, Object> attributes;

    @Mock
    private Route routeMocked;

    @Mock
    private Endpoint endpointMocked;

    @Mock
    private ProducerTemplate producerMocked;

    @Mock
    private Message messageMocked;

    @Mock
    private Object headerMocked;

    @Mock
    private InvocationContext invocationContext;

    @Mock
    private CamelRouteFactory routeFactory;

    @Mock
    private CamelRouteProducerTemplate producerTemplate;

    @Mock
    private Logger logger;

    private final CamelEngingRuntimeState runtimeState = new CamelEngingRuntimeState();

    /**
     * Sets the up.
     *
     * @throws Exception
     *             the exception.
     */
    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils
                .setNonPrimitiveField(CamelMediationEngineImpl.class, CamelEngingRuntimeState.class, mediationImplementation, runtimeState);
        ReflectionTestUtils.setNonPrimitiveField(CamelMediationEngineImpl.class, CamelContext.class, mediationImplementation, camelContextMock);
        ReflectionTestUtils.setNonPrimitiveField(CamelMediationEngineImpl.class, CamelRouteFactory.class, mediationImplementation, routeFactory);
        ReflectionTestUtils.setNonPrimitiveField(CamelMediationEngineImpl.class, CamelRouteProducerTemplate.class, mediationImplementation,
                producerTemplate);
        Mockito.when(producerTemplate.getTemplate()).thenReturn(producerMocked);
        ReflectionTestUtils.setNonPrimitiveField(CamelRouteFactory.class, FlowRouteBuilder.class, routeFactory, flowBuilder);
        ReflectionTestUtils.setNonPrimitiveField(CamelRouteFactory.class, CamelContext.class, routeFactory, camelContextMock);

        ReflectionTestUtils.setFinalNonPrimitiveField(CamelMediationEngineImpl.class, Logger.class, mediationImplementation, logger);
        when(logger.isTraceEnabled()).thenReturn(true);
    }

    /**
     * Tear down.
     *
     * @throws Exception
     *             the exception.
     */
    @After
    public void tearDown() throws Exception {}

    /**
     * Test create flow null flow path.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateFlowNullFlowPath() {
        doCallRealMethod().when(routeFactory).createFlow(Matchers.anyList());
        mediationImplementation.createFlow(null);
    }

    /**
     * Test create flow empty flow path.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateFlowEmptyFlowPath() {
        final List<FlowPath> flowPath = new ArrayList<FlowPath>();
        doCallRealMethod().when(routeFactory).createFlow(Matchers.anyList());
        mediationImplementation.createFlow(flowPath);
    }

    /**
     * Test create flow some flow path.
     */
    @Test(expected = CamelEngineRuntimeException.class)
    public void testCreateFlowSomeFlowPath() {
        setupCreateFlowMocks();
        when(flowBuilder.buildCamelRoute(Matchers.any(FlowPath.class))).thenThrow(Exception.class);
        doCallRealMethod().when(routeFactory).createFlow(Matchers.anyList());
        mediationImplementation.createFlow(flowListPathMocked);
    }

    /**
     * Test create flow some flow path success.
     */
    @Test
    public void testCreateFlowSomeFlowPathSuccess() {
        setupCreateFlowMocks();
        doCallRealMethod().when(routeFactory).createFlow(Matchers.anyList());
        mediationImplementation.createFlow(flowListPathMocked);
        verify(routeFactory, times(1)).createFlow(Matchers.anyList());
        verify(flowBuilder, times(1)).buildCamelRoute(Matchers.any(FlowPath.class));
    }

    /**
     * Test invoke flow with results exception.
     */
    @Test
    public void testInvokeFlowWithResultsException() {
        final String message = "errorMsg";
        when(camelContextMock.getRoute(Matchers.anyString())).thenReturn(routeMocked);
        when(routeMocked.getEndpoint()).thenReturn(endpointMocked);
        when(camelContextMock.createProducerTemplate()).thenReturn(producerMocked);
        doCallRealMethod().when(routeFactory).createFlow(Matchers.anyList());
        when(producerMocked.requestBodyAndHeaders(endpointMocked, null, attributes)).thenThrow(
                new RuntimeException(new Exception(new Exception("errorMsg"))));
        try {
            mediationImplementation.invokeFlowWithResults(FLOW_PATH_MOCKED_ID, attributes);
            Assert.fail("Testcase did not thrown exception as expected.");
        } catch (final CamelEngineRuntimeException e) {
            assertThat("Exception thrown was not of type CamelEngineRuntimeException", e, CoreMatchers.instanceOf(CamelEngineRuntimeException.class));
            Assert.assertTrue("Exception message did not contain nested exception message", e.getMessage().contains(message));
        }
    }

    /**
     * Test invoke flow with results.
     */
    @Test
    public void testInvokeFlowWithResults() {
        setupInvokeFlowResultsMocks();
        mediationImplementation.invokeFlowWithResults(FLOW_PATH_MOCKED_ID, attributes);
        verifyInvokeFlowWithResults();
    }

    /**
     * Test invoke transaction flow with results.
     */
    @Test
    public void testInvokeTransactionFlowWithResults() {
        setupInvokeFlowResultsMocks();
        mediationImplementation.invokeTransactionalFlowWithResults(FLOW_PATH_MOCKED_ID, attributes);
        verifyInvokeFlowWithResults();
    }

    /**
     * Test invoke flow.
     */
    @Test
    public void testInvokeFlow() {
        final Map<String, Object> flowAttributes = setupInvokeFlowMocks();
        mediationImplementation.invokeFlow(FLOW_PATH_MOCKED_ID, flowAttributes);
        verifyInvokeFlow(flowAttributes);
    }

    /**
     * Test invoke flow camel execution exception.
     */
    @Test(expected = CamelEngineRuntimeException.class)
    public void testInvokeFlowCamelExecutionException() {
        final Map<String, Object> flowAttributes = setupInvokeFlowMocks();

        PowerMockito.doThrow(new CamelExecutionException(null, null, new Exception(new Exception()))).when(producerMocked)
                .sendBodyAndHeaders(endpointMocked, ExchangePattern.InOnly, null, flowAttributes);

        mediationImplementation.invokeFlow(FLOW_PATH_MOCKED_ID, flowAttributes);
    }

    /**
     * Test invoke flow camel execution exception cause is null.
     */
    @Test(expected = CamelEngineRuntimeException.class)
    public void testInvokeFlowCamelExecutionExceptionCauseIsNull() {
        final Map<String, Object> flowAttributes = setupInvokeFlowMocks();

        PowerMockito.doThrow(new CamelExecutionException(null, null, null)).when(producerMocked)
                .sendBodyAndHeaders(endpointMocked, ExchangePattern.InOnly, null, flowAttributes);

        mediationImplementation.invokeFlow(FLOW_PATH_MOCKED_ID, flowAttributes);
    }

    /**
     * Test invoke flow camel execution exception cause get cause is null.
     */
    @Test(expected = CamelEngineRuntimeException.class)
    public void testInvokeFlowCamelExecutionExceptionCauseGetCauseIsNull() {
        final Map<String, Object> flowAttributes = setupInvokeFlowMocks();

        PowerMockito.doThrow(new CamelExecutionException(null, null, new Exception())).when(producerMocked)
                .sendBodyAndHeaders(endpointMocked, ExchangePattern.InOnly, null, flowAttributes);

        mediationImplementation.invokeFlow(FLOW_PATH_MOCKED_ID, flowAttributes);
    }

    /**
     * Test invoke transaction flow.
     */
    @Test
    public void testInvokeTransactionFlow() {
        final Map<String, Object> flowAttributes = setupInvokeFlowMocks();
        mediationImplementation.invokeTransactionalFlow(FLOW_PATH_MOCKED_ID, flowAttributes);
        verifyInvokeFlow(flowAttributes);
    }

    /**
     * Test prepare for upgrade.
     *
     * @throws Exception
     *             the exception.
     */
    @Test
    public void testPrepareForUpgrade() throws Exception {
        final CamelMediationEngineImpl camelEngine = mediationImplementation;
        when(invocationContext.proceed()).thenReturn(null);
        // before upgrade
        camelEngine.checkIfIsUnderUpgradeInterceptor(invocationContext);
        // on upgrade event
        runtimeState.setUnderUpgrade(true);
        try {
            camelEngine.checkIfIsUnderUpgradeInterceptor(invocationContext);
        } catch (final Exception e) {
            Assert.assertEquals("Upgrade message was not as expected.", MSG_MS_UNDER_UPGRADE, e.getMessage());
        }
    }

    /**
     * Verify invoke flow with results.
     */
    private void verifyInvokeFlowWithResults() {
        try {
            Mockito.verify(producerMocked).requestBodyAndHeaders(endpointMocked, null, attributes);
        } catch (final Exception e) {
            fail("Exception should not be thrown in here: " + e.getMessage());
        }
    }

    /**
     * Verify invoke flow.
     *
     * @param flowAttributes
     *            the flow attributes.
     */
    private void verifyInvokeFlow(final Map<String, Object> flowAttributes) {
        try {
            Mockito.verify(producerMocked).sendBodyAndHeaders(endpointMocked, ExchangePattern.InOnly, null, flowAttributes);
        } catch (final Exception e) {
            fail("Exception should not be thrown in here: " + e.getMessage());
        }
    }

    /**
     * Setup invoke flow mocks.
     *
     * @return the map
     */
    private Map<String, Object> setupInvokeFlowMocks() {
        when(camelContextMock.getRoute(Matchers.anyString())).thenReturn(routeMocked);
        when(routeMocked.getEndpoint()).thenReturn(endpointMocked);
        doCallRealMethod().when(routeFactory).createFlow(Matchers.anyList());
        try {
            when(camelContextMock.createProducerTemplate()).thenReturn(producerMocked);
        } catch (final Exception e) {
            fail("Exception should not be thrown in here: " + e.getMessage());
        }
        final Map<String, Object> flowAttributes = new HashMap<>();
        flowAttributes.put("fdn", "dps:fdn");
        when(messageMocked.getHeaders()).thenReturn(flowAttributes);
        when(messageMocked.getHeader(Matchers.anyString())).thenReturn(headerMocked);
        return flowAttributes;
    }

    /**
     * Setup invoke flow results mocks.
     */
    private void setupInvokeFlowResultsMocks() {
        when(camelContextMock.getRoute(Matchers.anyString())).thenReturn(routeMocked);
        when(routeMocked.getEndpoint()).thenReturn(endpointMocked);
        doCallRealMethod().when(routeFactory).createFlow(Matchers.anyList());
        try {
            when(camelContextMock.createProducerTemplate()).thenReturn(producerMocked);
        } catch (final Exception e) {
            fail("Exception should not be thrown in here: " + e.getMessage());
        }
        when(messageMocked.getHeaders()).thenReturn(attributes);
        when(messageMocked.getHeader(Matchers.anyString())).thenReturn(headerMocked);
        when(attributes.get(GenericConstant.KEY_OF_RETURN_VALUE_FROM_NODE)).thenReturn(null);
    }

    /**
     * Setup create flow mocks.
     */
    private void setupCreateFlowMocks() {
        when(flowListPathMocked.iterator()).thenReturn(iteratorMocked);
        when(iteratorMocked.hasNext()).thenReturn(true).thenReturn(false);
        when(iteratorMocked.next()).thenReturn(flowPathMocked);
        when(flowPathMocked.getId()).thenReturn(FLOW_PATH_MOCKED_ID);
    }
}
