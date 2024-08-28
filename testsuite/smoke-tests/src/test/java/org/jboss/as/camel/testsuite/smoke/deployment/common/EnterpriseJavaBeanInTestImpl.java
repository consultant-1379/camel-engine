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

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * The Class EnterpriseJavaBeanInTestImpl.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class EnterpriseJavaBeanInTestImpl {

    @EJB
    private EnterpriseJavaBeanOne ejbOne;
    @EJB
    private EnterpriseJavaBeanTwo ejbTwo;

    public EnterpriseJavaBeanOne getEjbOne() {
        return ejbOne;
    }

    public EnterpriseJavaBeanTwo getEjbTwo() {
        return ejbTwo;
    }
}
