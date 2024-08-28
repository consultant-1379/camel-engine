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

import java.util.HashMap;

/**
 * The Interface MediationServiceMock.
 */
public interface MediationServiceMock {

    String CAMEL_ENGINE_HANDLER_ROUTE = "CAMEL_ENGINE_HANDLER_ROUTE";
    String CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE = "CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE";
    String MS_MOCK_JNDI_NAME = "java:/mediationServiceMock";

    void createFlow(final String name);

    void invokeFlow(final String name, final HashMap<String, Object> headers);

    void invokeTransactionalFlow(final String name, final HashMap<String, Object> headers);

    void createFlowWithChoiceElement(String name);
}
