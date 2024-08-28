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

package org.jboss.as.camel.testsuite.smoke.deployment.composite.tests;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.camel.testsuite.smoke.deployment.common.EnterpriseJavaBeanInTestImpl;
import org.jboss.as.camel.testsuite.smoke.deployment.common.EnterpriseJavaBeanOne;
import org.jboss.as.camel.testsuite.smoke.deployment.common.EnterpriseJavaBeanOneImpl;
import org.jboss.as.camel.testsuite.smoke.deployment.common.EnterpriseJavaBeanTwo;
import org.jboss.as.camel.testsuite.smoke.deployment.common.EnterpriseJavaBeanTwoImpl;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The Class CompositeDeploymentTest.
 */
@RunWith(Arquillian.class)
public class CompositeDeploymentTest {

    private static final String EAR_DEP_NAME = "composite_deployment.ear";
    private static final String WAR_DEP_NAME = "COMPOSITE_TEST_test.war";
    private static final String EJB_DEP_NAME = "composite_deployment.jar";
    private static final String EJB_API_DEP_NAME = "composite_deployment-api.jar";

    @EJB
    private EnterpriseJavaBeanInTestImpl testEjb;

    @Deployment(name = EAR_DEP_NAME)
    public static Archive<?> createTestEarWithWar() {
        final EnterpriseArchive testEar = ShrinkWrap.create(EnterpriseArchive.class, EAR_DEP_NAME);

        final JavaArchive libJar = ShrinkWrap.create(JavaArchive.class, EJB_API_DEP_NAME);
        libJar.addClass(EnterpriseJavaBeanOne.class);
        libJar.addClass(EnterpriseJavaBeanTwo.class);
        testEar.addAsLibraries(libJar);

        final JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, EJB_DEP_NAME);
        testJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        testJar.addClass(EnterpriseJavaBeanOneImpl.class);
        testJar.addClass(EnterpriseJavaBeanTwoImpl.class);
        testEar.addAsModules(testJar);

        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, WAR_DEP_NAME);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(EnterpriseJavaBeanInTestImpl.class);
        testWar.addClass(CompositeDeploymentTest.class);
        testEar.addAsModules(testWar);

        return testEar;
    }

    @Test
    @OperateOnDeployment(EAR_DEP_NAME)
    public void testInjectionEjbNotNull() throws Exception {
        Assert.assertNotNull(testEjb.getEjbOne());
    }

    @Test
    @OperateOnDeployment(EAR_DEP_NAME)
    public void testInjectionEjbTwoNotNull() throws Exception {
        Assert.assertNotNull(testEjb.getEjbTwo());
    }

    @Test
    @OperateOnDeployment(EAR_DEP_NAME)
    public void testInjectedCamelContextIsSame() throws Exception {
        Assert.assertTrue(testEjb.getEjbOne().getContextName().equals(testEjb.getEjbTwo().getContextName()));
    }
}
