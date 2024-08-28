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

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SomeSingleton.
 */
@Startup
@Singleton
public class SomeSingleton implements SomeSingletonLocalInterface, SomeSingletonRemoteInterface {

    private static final long serialVersionUID = -5940054741208093273L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SomeSingleton.class);

    private String theMessage;

    @Override
    public void setMessage(final String message) {
        LOGGER.trace("Setting message {}", message);
        theMessage = message;
    }

    @Override
    public String getMessage() {
        LOGGER.trace("Gettign message {}", theMessage);
        return theMessage;
    }

    @PostConstruct
    private void startUp() {
        LOGGER.trace("SomeSingleton loaded");
    }
}
