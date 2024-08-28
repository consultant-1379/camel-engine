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

package org.jboss.as.camel.testsuite.smoke.deployment.ear.tests;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.camel.testsuite.smoke.deployment.common.SomeManagedBean;
import org.jboss.as.camel.testsuite.smoke.deployment.common.SomeSingletonEjb;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EARDeploymentTest.
 */
@RunWith(Arquillian.class)
public class EarDeploymentTest {

    public static final Logger LOG = LoggerFactory.getLogger(EarDeploymentTest.class);
    private static final String EAR_DEP_NAME_WITH_WAR = "test1.ear";
    private static final String EAR_DEP_NAME_WITH_EJB_JAR = "test2.ear";
    private static final String WAR_DEP_NAME = "test.war";
    private static final String JAR_DEP_NAME = "test.jar";

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @EJB
    private SomeSingletonEjb ejb;

    @Deployment(name = EAR_DEP_NAME_WITH_WAR, managed = true, testable = true, order = 1)
    public static Archive<?> createTestEarWithWar() {
        final EnterpriseArchive testEar = ShrinkWrap.create(EnterpriseArchive.class, EAR_DEP_NAME_WITH_WAR);
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, WAR_DEP_NAME);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SomeManagedBean.class);
        testWar.addClass(SomeSingletonEjb.class);
        testWar.addClass(EarDeploymentTest.class);
        testEar.addAsModules(testWar);
        return testEar;
    }

    @Deployment(name = EAR_DEP_NAME_WITH_EJB_JAR, managed = true, testable = true, order = 2)
    public static Archive<?> createTestEarWithEjbJar() {
        final EnterpriseArchive testEar = ShrinkWrap.create(EnterpriseArchive.class, EAR_DEP_NAME_WITH_EJB_JAR);
        final JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, JAR_DEP_NAME);
        testJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        testJar.addClass(SomeManagedBean.class);
        testJar.addClass(SomeSingletonEjb.class);
        testEar.addAsModules(testJar);
        return testEar;
    }

    @Test
    @OperateOnDeployment(EAR_DEP_NAME_WITH_WAR)
    @InSequence(1)
    public void testInjectionWarNotNull() throws Exception {
        Assert.assertNotNull(ejb.getSomeBean().getCtx());
    }

    @Test
    @OperateOnDeployment(EAR_DEP_NAME_WITH_EJB_JAR)
    @InSequence(2)
    public void testInjectionEjbNotNull() throws Exception {
        Assert.assertNotNull(ejb.getSomeBean().getCtx());
    }
}
