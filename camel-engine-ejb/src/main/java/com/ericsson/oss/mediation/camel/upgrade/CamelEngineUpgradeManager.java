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

import static com.ericsson.oss.mediation.camel.upgrade.recorder.RecordingConstants.IS_SERVICE_RESTART_REQUIRED;
import static com.ericsson.oss.mediation.camel.upgrade.recorder.RecordingConstants.PREPARE_FOR_INSTANCE_UPGRADE;
import static com.ericsson.oss.mediation.camel.upgrade.recorder.RecordingConstants.SERVICE_FRAMEWORK;
import static com.ericsson.oss.mediation.camel.upgrade.recorder.RecordingConstants.SQUARE_BRACKET;
import static com.ericsson.oss.mediation.camel.upgrade.recorder.RecordingConstants.UPGRADE_PHASE;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.jboss.camel.annotations.CamelContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.upgrade.UpgradeEvent;

/**
 * This class responds to <code>UpgradeEvent</code> coming in from Service Framework.
 */
@Singleton
@Startup
public class CamelEngineUpgradeManager {

    static final int ROUTE_STOP_TIMEOUT_MS = 3000;
    private static final String OK_MESSAGE = "OK";
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelEngineUpgradeManager.class);
    private static final String APPLICATION_ID = "CAMEL_ENGINE";

    @Inject
    private SystemRecorder systemRecorder;

    @EJB
    private CamelEngingRuntimeState runtimeState;

    @CamelContextService
    private CamelContext camelContext;

    /**
     * Observes upgradeEvents from Service Framework.
     *
     * @param upgradeEvent
     *            event sent by the PIB/ServiceFramework
     * @see com.ericsson.oss.mediation.camel.upgrade.UpgradeManager#onUpgradeEvent(com.ericsson.oss.itpf.sdk.upgrade.UpgradeEvent)
     */
    public void onUpgradeEvent(@Observes final UpgradeEvent upgradeEvent) {

        LOGGER.debug("UpgradeEvent received in application '{}' with Upgrade Phase {}", APPLICATION_ID, upgradeEvent.getPhase());

        switch (upgradeEvent.getPhase()) {
            case SERVICE_INSTANCE_UPGRADE_PREPARE:
                prepareForInstanceUpgrade(upgradeEvent);
                break;
            default:
                upgradeEvent.accept(OK_MESSAGE);
                break;
        }
    }

    private void prepareForInstanceUpgrade(final UpgradeEvent upgradeEvent) {
        LOGGER.info("Preparing for instance upgrade of application '{}'", APPLICATION_ID);
        systemRecorder.recordEvent(PREPARE_FOR_INSTANCE_UPGRADE, EventLevel.COARSE, SERVICE_FRAMEWORK, APPLICATION_ID,
                UPGRADE_PHASE + upgradeEvent.getPhase() + IS_SERVICE_RESTART_REQUIRED + upgradeEvent.isServiceRestartRequired() + SQUARE_BRACKET);

        LOGGER.info("Calling CamelEngineImpl.prepareForUpgrade to stop for upgrade.");
        try {
            prepareForUpgrade();
        } catch (final Exception ex) {
            LOGGER.error("Exception trying to stop camel engine {}", ex);
        }
        LOGGER.info("Will accept upgrade.");
        sendUpgradeAccept(upgradeEvent);
    }

    private void sendUpgradeAccept(final UpgradeEvent upgradeEvent) {
        upgradeEvent.accept(OK_MESSAGE);
    }

    /**
     * Prepare for upgrade.
     */
    void prepareForUpgrade() {
        LOGGER.debug("Stopping for upgrade.");
        runtimeState.setUnderUpgrade(true);
        stopRoutes();
        LOGGER.debug("Stopped for upgrade.");
    }

    /**
     * Stop all routes.
     */
    void stopRoutes() {
        LOGGER.debug("Stopping all routes.");
        final List<Route> routes = camelContext.getRoutes();
        for (final Route route : routes) {
            try {
                camelContext.stopRoute(route.getId(), ROUTE_STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (final Exception ex) {
                LOGGER.error("Exception trying to stop route {}: {}", route.getId(), ex);
            }
        }
    }
}
