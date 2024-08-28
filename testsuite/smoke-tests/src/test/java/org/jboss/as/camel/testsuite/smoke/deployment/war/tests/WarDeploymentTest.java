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

package org.jboss.as.camel.testsuite.smoke.deployment.war.tests;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.camel.testsuite.smoke.deployment.common.SomeManagedBean;
import org.jboss.as.camel.testsuite.smoke.deployment.common.SomeSingletonEjb;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The Class WarDeploymentTest.
 */
@RunWith(Arquillian.class)
public class WarDeploymentTest {

    private static final String DEP_NAME = "WAR_DEPLOYMENT_TEST_test.war";

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @EJB
    private SomeSingletonEjb ejb;

    @Deployment(name = DEP_NAME, managed = true, testable = true)
    public static Archive<?> createTestWar() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, DEP_NAME);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SomeManagedBean.class);
        testWar.addClass(SomeSingletonEjb.class);
        testWar.addClass(WarDeploymentTest.class);
        return testWar;
    }

    @Test
    public void assertInjectionNotNull() throws Exception {
        Assert.assertNotNull(ejb.getSomeBean().getCtx());
    }
}
