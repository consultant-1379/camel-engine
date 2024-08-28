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

package org.jboss.as.camel.testsuite.smoke.deployment.common;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.camel.CamelContext;
import org.jboss.camel.annotations.CamelContextService;

/**
 * The Class EnterpriseJavaBeanOneImpl.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Local(EnterpriseJavaBeanOne.class)
public class EnterpriseJavaBeanOneImpl implements EnterpriseJavaBeanOne {

    private static final long serialVersionUID = 4234466666622910402L;
    @CamelContextService
    private CamelContext camelCtx;

    public CamelContext getCamelCtx() {
        return camelCtx;
    }

    public void setCamelCtx(final CamelContext camelCtx) {
        this.camelCtx = camelCtx;
    }

    @Override
    public String getContextName() {
        return getCamelCtx().getName();
    }
}
