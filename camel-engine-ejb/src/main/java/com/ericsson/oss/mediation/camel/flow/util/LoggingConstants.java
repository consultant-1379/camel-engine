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

/**
 * Class containing constants used when logging.
 */
public final class LoggingConstants {

    public static final String EXCEPTION_CAUGHT_WHILE_TRYING_TO_INVOKE_METHOD_ON_THE_PROXY = "Exception = [{}] caught while trying to "
            + "invoke method on the proxy.";

    public static final String MSG_INVOKE_FLOW_CALLED_FOR_ROUTE_WITH_HEADERS = "invokeFlow called for route=[{}] with headers [{}]";

    public static final String MSG_NON_TRANSACTIONAL_INVOCATION = "Invoking non-transactional flow with flowId [{}].";

    public static final String MSG_TRANSACTIONAL_INVOCATION = "Invoking transactional flow with flowId [{}].";

    public static final String MSG_MS_UNDER_UPGRADE = "Unable to process the request because Camel Engine is upgrading...";

    private LoggingConstants() {}
}
