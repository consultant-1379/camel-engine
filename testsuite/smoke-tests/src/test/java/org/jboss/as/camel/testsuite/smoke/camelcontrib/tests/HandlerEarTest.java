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
 * The Class HandlerEarTest.
 */
@RunWith(Arquillian.class)
public class HandlerEarTest {

    private static final String MESSAGE_BEFORE_HANDLER_EXECUTION = "Message Before Handler Execution";
    private static final String WEB_XML = "web.xml";
    private static final String BEANS_XML = "beans.xml";
    private static final String ENGINE_JAR = "engine.jar";

    private static final String SINGLETON_APP_NAME = "singleton";

    private static final String SINGLETON_WAR = SINGLETON_APP_NAME + ".war";

    private static final String ENGINE_APP_NAME = "HandlerEarTestEngine";
    private static final String DEP_NAME_ENGINE_MOCK_EAR = ENGINE_APP_NAME + ".ear";

    private static final String HANDLER_EAR_TEST_TYPED_EVENT_ROUTE = "HANDLER_EAR_TEST_TYPED_EVENT_ROUTE";

    private static final String HANDLER_EAR_TEST_EVENT_TYPE_ROUTE = "HANDLER_EAR_TEST_EVENT_ROUTE";
    private static final String DEP_NAME_HANDLER_CONTRIB_EAR = "EAR_TEST_handler-contribution.ear";

    private static final String DEP_NAME_HANDLER_CONTRIB_JAR = "EAR_TEST_handler-contribution.jar";

    private static final String TEST_WAR = "EAR_test.war";

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerEarTest.class);

    @EJB(lookup = "java:global/" + ENGINE_APP_NAME + "/engine/SomeAppClientImpl!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeAppClient")
    private SomeAppClient someAppClient;

    @EJB(lookup = "java:global/" + SINGLETON_APP_NAME
            + "/SomeSingleton!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeSingletonRemoteInterface")
    private SomeSingletonRemoteInterface someSingleton;

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @Deployment(name = TEST_WAR, managed = false, testable = true)
    public static Archive<?> createTestDeployment() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, TEST_WAR);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SomeSingletonRemoteInterface.class);
        testWar.addClass(HandlerEarTest.class);
        testWar.addClass(SomeAppClient.class);
        testWar.addClass(TestConstants.class);
        return testWar;
    }

    @Deployment(name = SINGLETON_WAR, managed = false, testable = false)
    public static Archive<?> createSingleDeployment() {
        final WebArchive singletonWar = ShrinkWrap.create(WebArchive.class, SINGLETON_WAR);
        singletonWar.addAsWebInfResource(WEB_XML, WEB_XML);
        singletonWar.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        singletonWar.addClass(SomeSingleton.class);
        singletonWar.addClass(SomeSingletonLocalInterface.class);
        singletonWar.addClass(SomeSingletonRemoteInterface.class);
        return singletonWar;
    }

    @Deployment(name = DEP_NAME_HANDLER_CONTRIB_EAR, managed = false, testable = false)
    public static Archive<?> createHandlerContribDeploymentEar() {

        final JavaArchive contribJar = ShrinkWrap.create(JavaArchive.class, DEP_NAME_HANDLER_CONTRIB_JAR);
        contribJar.addAsManifestResource(EmptyAsset.INSTANCE, BEANS_XML);
        contribJar.addClass(SomeEventHandler.class);
        contribJar.addClass(SomeTypedEventHandler1.class);
        contribJar.addClass(SomeTypedEventHandler2.class);
        contribJar.addClass(SomeSingletonRemoteInterface.class);
        contribJar.addClass(TriggerEjb.class);
        final EnterpriseArchive contribEar = ShrinkWrap.create(EnterpriseArchive.class, DEP_NAME_HANDLER_CONTRIB_EAR);
        contribEar.addAsModule(contribJar);
        return contribEar;
    }

    @Deployment(name = DEP_NAME_ENGINE_MOCK_EAR, managed = false, testable = false)
    public static Archive<?> createTestEngineWar() {
        final EnterpriseArchive testEngineEar = ShrinkWrap.create(EnterpriseArchive.class, DEP_NAME_ENGINE_MOCK_EAR);
        final JavaArchive engineJar = ShrinkWrap.create(JavaArchive.class, ENGINE_JAR);
        engineJar.addAsManifestResource(EmptyAsset.INSTANCE, BEANS_XML);
        engineJar.addClass(SomeAppClient.class);
        engineJar.addClass(SomeAppClientImpl.class);
        engineJar.addClass(TestConstants.class);
        testEngineEar.addAsModule(engineJar);
        return testEngineEar;
    }

    @InSequence(1)
    @OperateOnDeployment(SINGLETON_WAR)
    @Test
    public void testDeploySingleton() throws Exception {
        LOGGER.info("------>EAR Contribution Smoke Test: Deploy SINGLETON FOR EAR Contribution<------");
        deployer.deploy(SINGLETON_WAR);
    }

    @InSequence(2)
    @OperateOnDeployment(DEP_NAME_HANDLER_CONTRIB_EAR)
    @Test
    public void testDeployContribEar() throws Exception {
        LOGGER.info("------>EAR Contribution Smoke Test: Deploy Handlers in EAR Contribution<------");
        deployer.deploy(DEP_NAME_HANDLER_CONTRIB_EAR);
    }

    @InSequence(3)
    @OperateOnDeployment(DEP_NAME_ENGINE_MOCK_EAR)
    @Test
    public void testDeployTestEngineWar() throws Exception {
        LOGGER.info("------>EAR Contribution Smoke Test: Deploy Engine for EAR Contribution Test<------");
        deployer.deploy(DEP_NAME_ENGINE_MOCK_EAR);
    }

    @InSequence(4)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void testDeployTestWar() throws Exception {
        LOGGER.info("------>EAR Smoke Test: Deploy Test archive<------");
        deployer.deploy(TEST_WAR);
    }

    @InSequence(5)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForTypedEventHandler() throws Exception {
        LOGGER.debug("applyInputForTypedEventHandler");
        Assert.assertNotNull("someAppClient is null", someAppClient);
        someAppClient.createRouteWithTypedEventHandlerProcessor(HANDLER_EAR_TEST_TYPED_EVENT_ROUTE);
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, classData);
        someAppClient.applyInput(HANDLER_EAR_TEST_TYPED_EVENT_ROUTE, headers);
        Assert.assertEquals("Handler was not executed", TestConstants.HANDLER_PAYLOAD, someSingleton.getMessage());
        someAppClient.stopRoute(HANDLER_EAR_TEST_TYPED_EVENT_ROUTE);
    }

    @InSequence(6)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForEventHandler() throws Exception {
        LOGGER.debug("applyInputForEventHandler");
        Assert.assertNotNull("someAppClient is null", someAppClient);
        someAppClient.createRouteWithEventHandlerProcessor(HANDLER_EAR_TEST_EVENT_TYPE_ROUTE);
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_EVENT_HANDLER_FQCN, classData);
        final Object result = someAppClient.applyInputWithResults(HANDLER_EAR_TEST_EVENT_TYPE_ROUTE, headers);
        Assert.assertEquals("Handler was not executed", TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED, someSingleton.getMessage());
        Assert.assertEquals(DEFAULT_RETURN_VALUE, result);
        someAppClient.stopRoute(HANDLER_EAR_TEST_EVENT_TYPE_ROUTE);
    }
}
