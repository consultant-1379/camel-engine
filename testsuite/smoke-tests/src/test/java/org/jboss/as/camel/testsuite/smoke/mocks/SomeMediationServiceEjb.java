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

import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.mediation.dsl.engine.connection.EngineConnectionFactory;

/**
 * The Class SomeMediationServiceEjb.
 */
@Singleton
@Startup
public class SomeMediationServiceEjb {

    private static final Logger LOG = LoggerFactory.getLogger(SomeMediationServiceEjb.class);
    private static final String EXPECTED_ERROR_MSG =
            "Unable to create connection without connection request information";

    public boolean isEngineConnectionFactoryLookupSuccessful(final String jndi) {
        LOG.info("Inside isEngineConnectionFactoryLookupSuccessful");
        boolean result = false;
        final EngineConnectionFactory engineConnFactory = resolveBean(jndi);
        try {
            engineConnFactory.getConnection(null);
        } catch (final ResourceException e) {
            LOG.error(e.getMessage());
            if (e.getMessage().contains(EXPECTED_ERROR_MSG)) {
                result = true; // At least remote call to factory was successfull
            }
        }
        return result;
    }

    private static <T> T resolveBean(final String jndiName) {
        final Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
        jndiProperties.put(Context.URL_PKG_PREFIXES,
                "org.jboss.ejb.client.naming");
        try {
            final InitialContext ctx = new InitialContext(jndiProperties);
            @SuppressWarnings("unchecked")
            final T bean = (T) ctx.lookup(jndiName);
            return bean;
        } catch (final NamingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @PostConstruct
    public void load() {
        LOG.info("Loaded....");
    }
}
