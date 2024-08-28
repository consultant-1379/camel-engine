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

package com.ericsson.oss.mediation.camel.upgrade;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.upgrade.UpgradeEvent;
import com.ericsson.oss.itpf.sdk.upgrade.UpgradePhase;
import com.ericsson.oss.mediation.camel.ejb.CamelMediationEngineImpl;
import com.ericsson.oss.mediation.camel.ejb.ReflectionTestUtils;

/**
 * The Class CamelEngineUpgradeManagerTest.
 */
@RunWith(PowerMockRunner.class)
public class CamelEngineUpgradeManagerTest {

    private CamelEngineUpgradeManager upgradeManager;

    @Mock
    private CamelMediationEngineImpl camelEngine;

    @Mock
    private SystemRecorder systemRecorder;

    @Mock
    private UpgradeEvent upgradeEvent;

    @Mock
    private CamelContext camelContextMock;

    @Mock
    private CamelEngingRuntimeState runtimeState;

    @Mock
    private Route route;

    @Before
    public void setupTestCase() throws Exception {
        upgradeManager = new CamelEngineUpgradeManager();
        ReflectionTestUtils.setNonPrimitiveField(CamelEngineUpgradeManager.class, CamelContext.class, upgradeManager, camelContextMock);
        ReflectionTestUtils.setNonPrimitiveField(CamelEngineUpgradeManager.class, SystemRecorder.class, upgradeManager, systemRecorder);
        ReflectionTestUtils.setNonPrimitiveField(CamelEngineUpgradeManager.class, CamelEngingRuntimeState.class, upgradeManager, runtimeState);
        final List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        when(camelContextMock.getRoutes()).thenReturn(routeList);
        when(route.getId()).thenReturn("ROUTE1");
    }

    @Test
    public void testOnUpgradeEvent() throws Exception {
        when(upgradeEvent.getPhase()).thenReturn(UpgradePhase.SERVICE_INSTANCE_UPGRADE_PREPARE);
        upgradeManager.onUpgradeEvent(upgradeEvent);
        verify(systemRecorder).recordEvent(anyString(), any(EventLevel.class), anyString(), anyString(), anyString());
        verify(runtimeState).setUnderUpgrade(true);
        verify(camelContextMock).stopRoute("ROUTE1", CamelEngineUpgradeManager.ROUTE_STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        verify(upgradeEvent).accept("OK");
    }

    @Test
    public void testOnUpgradeEventDefaultPhase() throws Exception {
        when(upgradeEvent.getPhase()).thenReturn(UpgradePhase.SERVICE_INSTANCE_UPGRADE_FINISHED_SUCCESSFULLY);
        upgradeManager.onUpgradeEvent(upgradeEvent);
        verify(upgradeEvent).accept("OK");
    }

    @Test
    public void testOnUpgradeEventStopRouteException() throws Exception {
        when(upgradeEvent.getPhase()).thenReturn(UpgradePhase.SERVICE_INSTANCE_UPGRADE_PREPARE);
        PowerMockito.doThrow(new Exception()).when(camelContextMock)
                .stopRoute("ROUTE1", CamelEngineUpgradeManager.ROUTE_STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        upgradeManager.onUpgradeEvent(upgradeEvent);
        verify(systemRecorder).recordEvent(anyString(), any(EventLevel.class), anyString(), anyString(), anyString());
        verify(runtimeState).setUnderUpgrade(true);
        verify(camelContextMock).stopRoute("ROUTE1", CamelEngineUpgradeManager.ROUTE_STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        verify(upgradeEvent).accept("OK");
    }

    @Test
    public void testOnUpgradeEventException() throws Exception {
        when(upgradeEvent.getPhase()).thenReturn(UpgradePhase.SERVICE_INSTANCE_UPGRADE_PREPARE);
        when(camelContextMock.getRoutes()).thenThrow(new RuntimeException());
        upgradeManager.onUpgradeEvent(upgradeEvent);
        verify(systemRecorder).recordEvent(anyString(), any(EventLevel.class), anyString(), anyString(), anyString());
        verify(runtimeState).setUnderUpgrade(true);
        verify(upgradeEvent).accept("OK");
    }
}
