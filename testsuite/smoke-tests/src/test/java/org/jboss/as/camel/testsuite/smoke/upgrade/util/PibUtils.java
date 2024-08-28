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

package org.jboss.as.camel.testsuite.smoke.upgrade.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle calls to Platform Integration Bridge (PIB).
 */
public final class PibUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PibUtils.class);
    private static final String SERVICE_IDENTIFIER = "camel-engine";

    private PibUtils() {}

    /**
     * Sends an HTTP upgrade request to PIB.
     *
     * @return value returned by the call to PIB
     * @throws Exception
     *             the exception
     */
    public static String sendUpgradeRequest() throws Exception {
        String returnVal = "";
        final URL url = new URL(generateRestUrl());
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        LOGGER.debug(".............Http upgrade request sent..........");
        LOGGER.info(" RESPONSE IS  {} ", conn.getResponseCode());
        LOGGER.debug(".............Http upgrade response received..........");
        if (conn.getResponseCode() != 200) {
            returnVal = "Failed : HTTP error code : " + conn.getResponseCode();
        }
        final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;
        while ((output = br.readLine()) != null) {
            returnVal += output;
        }
        LOGGER.debug("Upgrade id : {}", returnVal);
        conn.disconnect();

        return returnVal;
    }

    /**
     * Return the response from PIB for a given previously sent request.
     *
     * @param returnVal
     *            the previous request response
     * @return response
     * @throws Exception
     *             the exception
     */
    public static String getUpgradeRequestResponse(String returnVal) throws Exception {
        final URL urlresp = new URL(generateRestUrlForResponse(returnVal));
        final HttpURLConnection connResp = (HttpURLConnection) urlresp.openConnection();
        connResp.setRequestMethod("GET");
        LOGGER.debug(".............Http upgrade request sent..........");
        LOGGER.info(" RESPONSE IS  {} ", connResp.getResponseCode());
        LOGGER.debug(".............Http upgrade response received..........");
        if (connResp.getResponseCode() != 200) {
            returnVal = "Failed : HTTP error code : " + connResp.getResponseCode();
        }
        final BufferedReader br1 = new BufferedReader(new InputStreamReader(connResp.getInputStream()));
        String output;
        while ((output = br1.readLine()) != null) {
            returnVal += output;
        }
        LOGGER.debug("Upgrade Response : {}", returnVal);
        connResp.disconnect();

        return returnVal;
    }

    private static String generateRestUrl() {
        final String instanceId = System.getProperty("com.ericsson.oss.sdk.node.identifier");
        final String portOffset = System.getProperty("jboss.socket.binding.port-offset");
        final Integer port = new Integer(portOffset) + 8080;
        final StringBuilder triggerUrl = new StringBuilder();
        triggerUrl.append("http://localhost:");
        triggerUrl.append(port.toString());
        triggerUrl.append("/pib/upgradeService/startUpgrade?app_server_identifier=");
        triggerUrl.append(instanceId);
        triggerUrl.append("&service_identifier=" + SERVICE_IDENTIFIER
                + "&upgrade_operation_type=service&upgrade_phase=SERVICE_INSTANCE_UPGRADE_PREPARE");
        LOGGER.info("URL constructed: " + triggerUrl.toString());
        return triggerUrl.toString();
    }

    private static String generateRestUrlForResponse(final String upgradeId) {
        final String portOffset = System.getProperty("jboss.socket.binding.port-offset");
        final Integer port = new Integer(portOffset) + 8080;
        final StringBuilder triggerUrl = new StringBuilder();
        triggerUrl.append("http://localhost:");
        triggerUrl.append(port.toString());
        triggerUrl.append("/pib/upgradeService/getUpgradeResponse?id=" + upgradeId);
        return triggerUrl.toString();
    }
}
