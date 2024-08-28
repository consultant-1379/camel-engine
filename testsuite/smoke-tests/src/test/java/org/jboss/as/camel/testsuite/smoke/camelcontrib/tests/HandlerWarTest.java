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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HandlerWarTest.
 */
@RunWith(Arquillian.class)
public class HandlerWarTest {

    private static final String HANDLER_DID_NOT_CHANGE_THE_MESSAGE = "Handler did not change the message";
    private static final String MESSAGE_BEFORE_HANDLER_EXECUTION = "Message Before Handler Execution";
    private static final String BEANS_XML = "beans.xml";
    private static final String WEB_XML = "web.xml";
    private static final String DEP_NAME_ENGINE_MOCK_WAR = "WAR_TEST_engine.war";

    private static final String DEP_NAME_HANDLER_CONTRIB_WAR = "WAR_TEST_handler-contribution.war";

    private static final String SINGLETON_WAR = "singleton.war";
    private static final String TEST_WAR = "test.war";
    private static final String HANDLER_WAR_TEST_EVENT_TYPE_ROUTE = "HANDLER_WAR_TEST_EVENT_TYPE_ROUTE";
    private static final String HANDLER_WAR_TEST_TYPED_EVENT_TYPE_ROUTE = "HANDLER_WAR_TEST_TYPED_EVENT_TYPE_ROUTE";

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerWarTest.class);

    @EJB(lookup = "java:global/WAR_TEST_engine/SomeAppClientImpl!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeAppClient")
    private SomeAppClient someAppClient;

    @EJB(lookup = "java:global/singleton/SomeSingleton!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeSingletonRemoteInterface")
    private SomeSingletonRemoteInterface someSingleton;

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @Deployment(name = SINGLETON_WAR, managed = false, testable = false, order = 1)
    public static Archive<?> createSingleDeployment() {
        final WebArchive singletonWaR = ShrinkWrap.create(WebArchive.class, SINGLETON_WAR);
        singletonWaR.addAsWebInfResource(WEB_XML, WEB_XML);
        singletonWaR.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        singletonWaR.addClass(SomeSingleton.class);
        singletonWaR.addClass(SomeSingletonLocalInterface.class);
        singletonWaR.addClass(SomeSingletonRemoteInterface.class);
        return singletonWaR;
    }

    @Deployment(name = DEP_NAME_HANDLER_CONTRIB_WAR, managed = false, testable = false, order = 2)
    public static Archive<?> createHandlerContribDeploymentWar() {
        final WebArchive contribWar = ShrinkWrap.create(WebArchive.class, DEP_NAME_HANDLER_CONTRIB_WAR);
        contribWar.addAsWebInfResource(WEB_XML, WEB_XML);
        contribWar.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        contribWar.addClass(SomeEventHandler.class);
        contribWar.addClass(SomeTypedEventHandler1.class);
        contribWar.addClass(SomeTypedEventHandler2.class);
        contribWar.addClass(SomeSingletonRemoteInterface.class);
        // contribWar.addAsWebInfResource("jboss-deployment-structure.xml");
        return contribWar;
    }

    @Deployment(name = DEP_NAME_ENGINE_MOCK_WAR, managed = false, testable = false, order = 3)
    public static Archive<?> createTestEngineWar() {
        final WebArchive testEngineWar = ShrinkWrap.create(WebArchive.class, DEP_NAME_ENGINE_MOCK_WAR);
        testEngineWar.addAsWebInfResource(WEB_XML, WEB_XML);
        testEngineWar.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        testEngineWar.addClass(SomeAppClient.class);
        testEngineWar.addClass(SomeAppClientImpl.class);
        testEngineWar.addClass(TestConstants.class);
        // testEngineWar.addAsWebInfResource("jboss-deployment-structure.xml");
        return testEngineWar;
    }

    @Deployment(name = TEST_WAR, managed = false, testable = true, order = 4)
    public static Archive<?> createTestDeployment() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, TEST_WAR);
        testWar.addAsWebInfResource(WEB_XML, WEB_XML);
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        testWar.addClass(SomeSingletonRemoteInterface.class);
        testWar.addClass(HandlerWarTest.class);
        testWar.addClass(SomeAppClient.class);
        testWar.addClass(TestConstants.class);
        return testWar;
    }

    @InSequence(1)
    @OperateOnDeployment(SINGLETON_WAR)
    @Test
    public void testDeploySingletonWar() throws Exception {
        LOGGER.debug("------>Smoke Test: Deploy Singleton  Test<------");
        deployer.deploy(SINGLETON_WAR);
    }

    @InSequence(2)
    @OperateOnDeployment(DEP_NAME_HANDLER_CONTRIB_WAR)
    @Test
    public void testDeployContribWar() throws Exception {
        LOGGER.debug("------>Smoke Test: Deploy Handler Contribution Test<------");
        deployer.deploy(DEP_NAME_HANDLER_CONTRIB_WAR);
    }

    @InSequence(3)
    @OperateOnDeployment(DEP_NAME_ENGINE_MOCK_WAR)
    @Test
    public void testDeployTestEngineWar() throws Exception {
        LOGGER.debug("------>Smoke Test: Deploy TestEngine  Test<------");
        deployer.deploy(DEP_NAME_ENGINE_MOCK_WAR);
    }

    @InSequence(4)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void testDeployTestWar() throws Exception {
        LOGGER.debug("------>Smoke Test: Deploy Test  Test<------");
        deployer.deploy(TEST_WAR);
    }

    @InSequence(5)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForTypedEventHandler() throws Exception {
        LOGGER.debug("applyInputForTypedEventHandler");
        Assert.assertNotNull("someAppClient was not injected", someAppClient);
        someAppClient.createRouteWithTypedEventHandlerProcessor(HANDLER_WAR_TEST_EVENT_TYPE_ROUTE);
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, classData);
        someAppClient.applyInput(HANDLER_WAR_TEST_EVENT_TYPE_ROUTE, headers);
        Assert.assertEquals(HANDLER_DID_NOT_CHANGE_THE_MESSAGE, TestConstants.HANDLER_PAYLOAD, someSingleton.getMessage());
        someAppClient.stopRoute(HANDLER_WAR_TEST_EVENT_TYPE_ROUTE);
    }

    @InSequence(6)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForEventHandler() throws Exception {
        LOGGER.debug("applyInputForEventHandler");
        someAppClient.createRouteWithEventHandlerProcessor(HANDLER_WAR_TEST_TYPED_EVENT_TYPE_ROUTE);
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_EVENT_HANDLER_FQCN, classData);
        final Object result = someAppClient.applyInputWithResults(HANDLER_WAR_TEST_TYPED_EVENT_TYPE_ROUTE, headers);
        Assert.assertEquals(DEFAULT_RETURN_VALUE, result);
        Assert.assertEquals(HANDLER_DID_NOT_CHANGE_THE_MESSAGE, TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED, someSingleton.getMessage());
    }

    @InSequence(7)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForTypedEventHandler2() throws Exception {
        LOGGER.debug("applyInputForTypedEventHandler2");
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_EVENT_HANDLER_FQCN, classData);
        someAppClient.applyInput(HANDLER_WAR_TEST_TYPED_EVENT_TYPE_ROUTE, headers);
        Assert.assertEquals(HANDLER_DID_NOT_CHANGE_THE_MESSAGE, TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED, someSingleton.getMessage());
        someAppClient.stopRoute(HANDLER_WAR_TEST_TYPED_EVENT_TYPE_ROUTE);
    }
}
