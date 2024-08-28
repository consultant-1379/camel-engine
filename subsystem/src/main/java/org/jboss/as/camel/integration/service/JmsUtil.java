/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.integration.service;

import javax.jms.ConnectionFactory;

/**
 * Utility class for jms conn factory injector.
 */
public final class JmsUtil {

    /**
     * JNDI Binding for the conn fact.
     */
    public static final String CON_FACT_JNDI_BINDING = "java:/ConnectionFactory";

    public static final String JMS_COMPONENT_NAME = "jms";

    private static volatile ConnectionFactory connFact;

    private JmsUtil() {}

    /**
     * Getter for connection factory.
     *
     * @return ConnectionFactory
     */
    public static ConnectionFactory getConnectionFactory() {
        return JmsUtil.connFact;
    }

    /**
     * Setter method for connection factory.
     *
     * @param connFact
     *            Connection Factory value.
     */
    public static void setConnectionFactory(final ConnectionFactory connFact) {
        JmsUtil.connFact = connFact;
    }
}
