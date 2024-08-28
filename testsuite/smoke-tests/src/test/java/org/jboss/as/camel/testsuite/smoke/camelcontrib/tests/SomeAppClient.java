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

import java.io.Serializable;
import java.util.Map;

import javax.ejb.Remote;

/**
 * The Interface SomeAppClient.
 */
@Remote
public interface SomeAppClient extends Serializable {

    void createRouteWithEventHandlerProcessor(final String routeName) throws Exception;

    void createRouteWithTypedEventHandlerProcessor(final String routeName) throws Exception;

    void applyInput(final String routeName, final Map<String, Object> headers) throws Exception;

    void stopRoute(final String routeName) throws Exception;

    Object applyInputWithResults(String routeName, Map<String, Object> headers) throws Exception;
}
