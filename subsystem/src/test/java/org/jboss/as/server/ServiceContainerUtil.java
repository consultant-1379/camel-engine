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

package org.jboss.as.server;

import org.jboss.msc.service.ServiceContainer;

/**
 * The Class ServiceContainerUtil.
 */
public final class ServiceContainerUtil {
    private ServiceContainerUtil() {}

    public static void setCurrentServiceContainer(final ServiceContainer c) {
        CurrentServiceContainer.setServiceContainer(c);
    }
}
