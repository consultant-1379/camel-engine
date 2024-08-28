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

package org.jboss.as.camel.testsuite.smoke.camelcontrib.tests;

import static org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.TestConstants.DEFAULT_RETURN_VALUE;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
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
 * Test Flow Creation and Invocation when handlers are deployed in separate EAR deployments.
 */
@RunWith(Arquillian.class)
public class MultipleHandlerEarTest {

    private static final String SINGLETON_WAR = "singleton.war";

    private static final String EAR_ENGINE = "MULTIPLE_EAR_TEST_engine";

    private static final String ENGINE_MOCK_EAR = EAR_ENGINE + ".ear";

    private static final String DEP_NAME_HANDLER_ONE_CONTRIB_EAR = "MULTIPLE_EAR_TEST_handler-one-contribution.ear";

    private static final String DEP_NAME_HANDLER_TWO_CONTRIB_EAR = "MULTIPLE_EAR_TEST_handler-two-contribution.ear";

    private static final String DEP_NAME_HANDLER_CONTRIB_ONE_JAR = "MULTIPLE_EAR_TEST_handler-contribution-one.jar";

    private static final String DEP_NAME_HANDLER_CONTRIB_TWO_JAR = "MULTIPLE_EAR_TEST_handler-contribution-two.jar";

    private static final String MULTIPLE_EAR_HANDLER_ROUTE = "MULTIPLE_EAR_HANDLER_ROUTE";

    private static final String MULTIPLE_EAR_HANDLER_ROUTE_NON_TYPED = "MULTIPLE_EAR_HANDLER_ROUTE_NON_TYPED";

    private static final String TEST_WAR = "MULTIPLE_EAR_test.war";

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleHandlerEarTest.class);

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @EJB(lookup = "java:global/" + EAR_ENGINE + "/engine/SomeAppClientImpl!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeAppClient")
    private SomeAppClient camelContextContainer;

    @EJB(lookup = "java:global/singleton/SomeSingleton!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeSingletonRemoteInterface")
    private SomeSingletonRemoteInterface someSingleton;

    @Deployment(name = TEST_WAR, managed = false, testable = true)
    public static Archive<?> createTestDeployment() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, TEST_WAR);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SomeSingletonRemoteInterface.class);
        testWar.addClass(MultipleHandlerEarTest.class);
        testWar.addClass(SomeAppClient.class);
        testWar.addClass(TestConstants.class);
        return testWar;
    }

    @Deployment(name = SINGLETON_WAR, managed = false, testable = false)
    public static Archive<?> createSingleDeployment() {
        final WebArchive singletonWar = ShrinkWrap.create(WebArchive.class, SINGLETON_WAR);
        singletonWar.addAsWebInfResource("web.xml", "web.xml");
        singletonWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        singletonWar.addClass(SomeSingleton.class);
        singletonWar.addClass(SomeSingletonLocalInterface.class);
        singletonWar.addClass(SomeSingletonRemoteInterface.class);
        return singletonWar;
    }

    @Deployment(name = DEP_NAME_HANDLER_ONE_CONTRIB_EAR, managed = false, testable = false)
    public static Archive<?> createFirstHandlerContribDeploymentEar() {
        final JavaArchive contribJar = ShrinkWrap.create(JavaArchive.class, DEP_NAME_HANDLER_CONTRIB_ONE_JAR);
        contribJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        contribJar.addClass(SomeEventHandler.class);
        contribJar.addClass(SomeTypedEventHandler2.class);
        contribJar.addClass(SomeSingletonRemoteInterface.class);
        contribJar.addClass(TriggerEjb.class);
        final EnterpriseArchive contribEar = ShrinkWrap.create(EnterpriseArchive.class, DEP_NAME_HANDLER_ONE_CONTRIB_EAR);
        // contribEAR.addAsManifestResource("jboss-deployment-structure.xml");
        contribEar.addAsModule(contribJar);
        return contribEar;
    }

    @Deployment(name = DEP_NAME_HANDLER_TWO_CONTRIB_EAR, managed = false, testable = false)
    public static Archive<?> createSecondHandlerContribDeploymentEar() {
        final JavaArchive contribJar = ShrinkWrap.create(JavaArchive.class, DEP_NAME_HANDLER_CONTRIB_TWO_JAR);
        contribJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        contribJar.addClass(SomeTypedEventHandler1.class);
        contribJar.addClass(TriggerEjb.class);
        final EnterpriseArchive contribEar = ShrinkWrap.create(EnterpriseArchive.class, DEP_NAME_HANDLER_TWO_CONTRIB_EAR);
        contribEar.addAsModule(contribJar);
        return contribEar;
    }

    @Deployment(name = ENGINE_MOCK_EAR, managed = false, testable = false)
    public static Archive<?> createTestEngineWar() {
        final EnterpriseArchive testEngineEar = ShrinkWrap.create(EnterpriseArchive.class, ENGINE_MOCK_EAR);
        final JavaArchive engineJar = ShrinkWrap.create(JavaArchive.class, "engine.jar");
        engineJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        engineJar.addClass(SomeAppClient.class);
        engineJar.addClass(SomeAppClientImpl.class);
        engineJar.addClass(TestConstants.class);
        testEngineEar.addAsModule(engineJar);
        // testEngineEar.addAsManifestResource("jboss-deployment-structure.xml");
        return testEngineEar;
    }

    @InSequence(1)
    @OperateOnDeployment(SINGLETON_WAR)
    @Test
    public void testDeploySingleton() throws Exception {
        LOGGER.info("testDeploySingleton");
        deployer.deploy(SINGLETON_WAR);
    }

    @InSequence(2)
    @OperateOnDeployment(DEP_NAME_HANDLER_ONE_CONTRIB_EAR)
    @Test
    public void testDeployFirstContribEar() throws Exception {
        LOGGER.info("testDeployFirstContribEar");
        deployer.deploy(DEP_NAME_HANDLER_ONE_CONTRIB_EAR);
    }

    @InSequence(2)
    @OperateOnDeployment(DEP_NAME_HANDLER_TWO_CONTRIB_EAR)
    @Test
    public void testDeploySecondContribEar() throws Exception {
        LOGGER.info("testDeploySecondContribEar");
        deployer.deploy(DEP_NAME_HANDLER_TWO_CONTRIB_EAR);
    }

    @InSequence(3)
    @OperateOnDeployment(ENGINE_MOCK_EAR)
    @Test
    public void testDeployTestEngineWar() throws Exception {
        LOGGER.info("testDeployTestEngineWAR");
        deployer.deploy(ENGINE_MOCK_EAR);
    }

    @InSequence(4)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void testDeployTestWar() throws Exception {
        LOGGER.info("testDeployTestWAR");
        deployer.deploy(TEST_WAR);
    }

    @InSequence(5)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void createRouteForTypedEventHandlerWithContributedProcessors() throws Exception {
        LOGGER.info("createRouteForTypedEventHandlerWithContributedProcessors");
        Assert.assertNotNull("Camel context ejb is null", camelContextContainer);
        camelContextContainer.createRouteWithTypedEventHandlerProcessor(MULTIPLE_EAR_HANDLER_ROUTE);
    }

    @InSequence(6)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForTypedEventHandlerWithContributedProcessors() throws Exception {
        LOGGER.info("applyInputForTypedEventHandlerWithContributedProcessors");
        Assert.assertNotNull("Camel context ejb is null", camelContextContainer);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, classData);
        camelContextContainer.applyInput(MULTIPLE_EAR_HANDLER_ROUTE, headers);
        Assert.assertEquals(TestConstants.HANDLER_PAYLOAD, someSingleton.getMessage());
    }

    @InSequence(7)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void createRouteForEventHandlerWithContributedProcessors() throws Exception {
        LOGGER.info("createRouteForEventHandlerWithContributedProcessors");
        Assert.assertNotNull("Camel context ejb is null", camelContextContainer);
        camelContextContainer.createRouteWithEventHandlerProcessor(MULTIPLE_EAR_HANDLER_ROUTE_NON_TYPED);
    }

    @InSequence(8)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForEventHandlerWithContributedProcessors() throws Exception {
        LOGGER.info("applyInputForEventHandlerWithContributedProcessors");
        Assert.assertNotNull("Camel context ejb is null", camelContextContainer);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_EVENT_HANDLER_FQCN, classData);
        final Object result = camelContextContainer.applyInputWithResults(MULTIPLE_EAR_HANDLER_ROUTE_NON_TYPED, headers);
        Assert.assertEquals(DEFAULT_RETURN_VALUE, result);
        Assert.assertEquals(TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED, someSingleton.getMessage());
    }
}
