/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

/**
 * The Interface TestConstants.
 */
public final class TestConstants {

    public static final String ROUTE_FOUR = "routeFour";
    public static final String ROUTE_FIVE = "routeFive";
    public static final String CONTRIB_RESULT_FILENAME = "contrib-file.txt";
    public static final String CONTRIB_TEST_MSG_BODY = "This ";
    public static final String CONTRIB_TEST_MSG_BODY_EXPECTED = CONTRIB_TEST_MSG_BODY + " is camel contributed processor";
    public static final String SOME_EVENT_HANDLER_FQCN = "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeEventHandler";
    public static final String SOME_TYPED_EVENT_HANDLER1_FQCN = "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeTypedEventHandler1";
    public static final String SOME_TYPED_EVENT_HANDLER2_FQCN = "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeTypedEventHandler2";
    public static final String HANDLER_PAYLOAD = "<<< HandlerPayload >>>";

    public static final int DEFAULT_RETURN_VALUE = -1;

    private TestConstants() {}
}
