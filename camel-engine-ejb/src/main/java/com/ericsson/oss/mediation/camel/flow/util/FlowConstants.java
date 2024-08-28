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
 * Constants that are used for resolving JMS address.
 */
@SuppressWarnings("el-syntax")
public final class FlowConstants {

    public static final String PROCESS_METHOD_NAME = "process";
    public static final String CONFIGURATION = "config";
    public static final String JMS_PREFIX = "jms";
    public static final String AMPERSAND = "&";
    public static final String QUESTION_MARK = "?";
    public static final char EQUALS = '=';
    public static final char SPLITTER = ':';
    public static final String CONFIGURABLE = "${";
    public static final String SERVICE_FRAMEWORK = "sfwk";
    public static final String CONFIG_QUEUE_SYS_PROPERTY = "network_element_notifications_channelId";

    private FlowConstants() {}
}
