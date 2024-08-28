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

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;

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
 * Verify handler deploy/undeploy functionality.
 */
@RunWith(Arquillian.class)
public class HandlerDeployUndeployTest {

    private static final String HANDLER_DID_NOT_CHANGE_THE_MESSAGE = "Handler did not change the message";

    private static final String MESSAGE_BEFORE_HANDLER_EXECUTION = "Message Before Handler Execution";

    private static final String BEANS_XML = "beans.xml";

    private static final String WEB_XML = "web.xml";

    private static final String SINGLETON_APP = "singleton";

    private static final String SINGLETON_WAR = SINGLETON_APP + ".war";

    private static final String DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_APP = "DEPLOY_UNDEPLOY_TEST_engine";

    private static final String DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_ENGINE = DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_APP + ".ear";

    private static final String DEP_NAME_HANDLER_CONTRIB_EAR = "HandlerDeployUndeployTestHandler-ear.ear";

    private static final String DEP_NAME_HANDLER_CONTRIB_JAR = "HandlerDeployUndeployTestHandler-jar.jar";

    private static final String DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE = "DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE";

    private static final String TEST_WAR = "HandlerDeployUndeployTestWar.war";

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerDeployUndeployTest.class);

    @ArquillianResource
    private Deployer deployer;

    @EJB(
            lookup = "java:global/DEPLOY_UNDEPLOY_TEST_engine/engine/SomeAppClientImpl!"
                    + "org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeAppClient")
    private SomeAppClient someAppClient;

    @EJB(lookup = "java:global/" + SINGLETON_APP
            + "/SomeSingleton!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeSingletonRemoteInterface")
    private SomeSingletonRemoteInterface someSingleton;

    @Deployment(name = TEST_WAR, managed = false, testable = true)
    public static Archive<?> createTestDeployment() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, TEST_WAR);
        testWar.addAsWebInfResource(WEB_XML, WEB_XML);
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        testWar.addClass(SomeSingletonRemoteInterface.class);
        testWar.addClass(HandlerDeployUndeployTest.class);
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
        // contribEAR.addAsManifestResource("jboss-deployment-structure.xml");
        contribEar.addAsModule(contribJar);
        return contribEar;
    }

    @Deployment(name = DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_ENGINE, managed = false, testable = false)
    public static Archive<?> createTestEngineWar() {
        final EnterpriseArchive testEngineEar = ShrinkWrap.create(EnterpriseArchive.class, DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_ENGINE);
        final JavaArchive engineJar = ShrinkWrap.create(JavaArchive.class, "engine.jar");
        engineJar.addAsManifestResource(EmptyAsset.INSTANCE, BEANS_XML);
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
        LOGGER.debug("Deploying singleton war");
        deployer.deploy(SINGLETON_WAR);
    }

    @InSequence(2)
    @OperateOnDeployment(DEP_NAME_HANDLER_CONTRIB_EAR)
    @Test
    public void testDeployContribEar() throws Exception {
        LOGGER.debug("Deploying handler contrib war");
        deployer.deploy(DEP_NAME_HANDLER_CONTRIB_EAR);
    }

    @InSequence(3)
    @OperateOnDeployment(DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_ENGINE)
    @Test
    public void testDeployTestEngineWar() throws Exception {
        LOGGER.debug("Deploying test engine war");
        deployer.deploy(DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_ENGINE);
    }

    @InSequence(4)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void testDeployTestWar() throws Exception {
        LOGGER.debug("Deploying test war");
        deployer.deploy(TEST_WAR);
    }

    @InSequence(5)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void createRouteForTypedEventHandlerWithContributedProcessors() throws Exception {
        LOGGER.debug("createRouteForTypedEventHandlerWithContributedProcessors");
        someAppClient.createRouteWithTypedEventHandlerProcessor(DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE);
    }

    @InSequence(6)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForTypedEventHandler() throws Exception {
        LOGGER.debug("applyInputForTypedEventHandler");
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, classData);
        someAppClient.applyInput(DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE, headers);
        Assert.assertEquals(HANDLER_DID_NOT_CHANGE_THE_MESSAGE, TestConstants.HANDLER_PAYLOAD, someSingleton.getMessage());
    }

    @InSequence(7)
    @Test
    public void unDeployContribEar() throws Exception {
        LOGGER.debug("unDeployContribEar");
        deployer.undeploy(DEP_NAME_HANDLER_CONTRIB_EAR);
    }

    @InSequence(8)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForTypedEventHandlerAfterUndeploy() throws Exception {
        LOGGER.debug("applyInputForTypedEventHandlerAfterUndeploy");
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, classData);
        someAppClient.applyInput(DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE, headers);
        Assert.assertEquals(HANDLER_DID_NOT_CHANGE_THE_MESSAGE, MESSAGE_BEFORE_HANDLER_EXECUTION, someSingleton.getMessage());
    }

    @InSequence(9)
    @Test
    public void deployAgainContribEar() throws Exception {
        LOGGER.debug("deployAgainContribEar");
        deployer.deploy(DEP_NAME_HANDLER_CONTRIB_EAR);
    }

    @InSequence(10)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputForTypedEventHandlerAfterRestoringDeployment() throws Exception {
        LOGGER.debug("applyInputForTypedEventHandlerAfterRestoringDeployment");
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, classData);
        someAppClient.applyInput(DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE, headers);
        Assert.assertEquals(HANDLER_DID_NOT_CHANGE_THE_MESSAGE, TestConstants.HANDLER_PAYLOAD, someSingleton.getMessage());
    }

    @InSequence(11)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void applyInputAgainForTypedEventHandlerAfterRestoringDeployment() throws Exception {
        LOGGER.debug("applyInputAgainForTypedEventHandlerAfterRestoringDeployment");
        someSingleton.setMessage(MESSAGE_BEFORE_HANDLER_EXECUTION);
        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> classData = new HashMap<>();
        classData.put(TestConstants.CONTRIB_TEST_MSG_BODY, TestConstants.CONTRIB_TEST_MSG_BODY);
        headers.put(TestConstants.SOME_TYPED_EVENT_HANDLER1_FQCN, classData);
        someAppClient.applyInput(DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE, headers);
        Assert.assertEquals(HANDLER_DID_NOT_CHANGE_THE_MESSAGE, TestConstants.HANDLER_PAYLOAD, someSingleton.getMessage());
        someAppClient.stopRoute(DEPLOY_UNDEPLOY_TEST_HANDLER_ROUTE);
    }

    @InSequence(12)
    @Test
    public void undeployEverything() {
        try {
            deployer.undeploy(TEST_WAR);
        } catch (final Exception e) {
            LOGGER.error("Unable to undeploy " + TEST_WAR, e);
        }

        try {
            deployer.undeploy(DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_ENGINE);
        } catch (final Exception e) {
            LOGGER.error("Unable to undeploy " + DEPLOYMENT_HANDLER_DEPLOY_UNDPLOY_ENGINE, e);
        }

        try {
            deployer.undeploy(DEP_NAME_HANDLER_CONTRIB_EAR);
        } catch (final Exception e) {
            LOGGER.error("Unable to undeploy " + DEP_NAME_HANDLER_CONTRIB_EAR, e);
        }

        try {
            deployer.undeploy(SINGLETON_WAR);
        } catch (final Exception e) {
            LOGGER.error("Unable to undeploy " + SINGLETON_WAR, e);
        }
        LOGGER.warn("Udeploying all ears");
    }
}
