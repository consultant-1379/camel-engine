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

package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import java.io.Serializable;

import javax.ejb.Remote;

/**
 * The Interface SomeSingletonRemoteInterface.
 */
@Remote
public interface SomeSingletonRemoteInterface extends Serializable {

    void setMessage(final String message);

    String getMessage();
}
