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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Navigate;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.Service;
import org.apache.camel.spi.RouteContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.oss.mediation.camel.flow.builder.FlowRouteBuilder;
import com.ericsson.oss.mediation.flow.FlowPath;

/**
 * The Class CamelRouteFactoryTest.
 */
@RunWith(PowerMockRunner.class)
public class CamelRouteFactoryTest {
    private static final String FLOW_PATH_MOCKED_ID = "flowPathMockedId";

    @InjectMocks
    private CamelRouteFactory camelRouteFactory;

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

    /**
     * Test create flow route exists.
     */
    @Test
    public void testCreateFlowRouteExists() {
        setupCreateFlowMocks();
        when(camelContextMock.getRoute(FLOW_PATH_MOCKED_ID)).thenReturn(new MockRoute());
        camelRouteFactory.createFlow(flowListPathMocked);
        verify(camelContextMock, times(1)).getRoute(FLOW_PATH_MOCKED_ID);
    }

    /**
     * Test create flow route exists race condition.
     */
    @Test
    public void testCreateFlowRouteExistsRaceCondition() {
        setupCreateFlowMocks();
        PowerMockito.doAnswer(new Answer<Object>() {
            private int count;

            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                if (++count == 1) {
                    return null;
                }
                return new MockRoute();
            }
        }).when(camelContextMock).getRoute(FLOW_PATH_MOCKED_ID);
        camelRouteFactory.createFlow(flowListPathMocked);
        verify(camelContextMock, times(2)).getRoute(FLOW_PATH_MOCKED_ID);
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

    /**
     * The Class MockRoute.
     */
    class MockRoute implements Route {
        @Override
        public void addService(final Service arg) {}

        @Override
        public Consumer getConsumer() {
            return null;
        }

        @Override
        public Endpoint getEndpoint() {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public Map<String, Object> getProperties() {
            return null;
        }

        @Override
        public RouteContext getRouteContext() {
            return null;
        }

        @Override
        public List<Service> getServices() {
            return null;
        }

        @Override
        public Navigate<Processor> navigate() {
            return null;
        }

        @Override
        public void onStartingServices(final List<Service> arg) throws Exception {}

        @Override
        public boolean supportsSuspension() {
            return false;
        }

        @Override
        public void warmUp() {}
    }
}
