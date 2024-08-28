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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.camel.testsuite.smoke.dependencies.Dependencies;
import org.jboss.as.camel.testsuite.smoke.deployment.common.Deployments;
import org.jboss.as.camel.testsuite.smoke.mocks.MediationServiceMock;
import org.jboss.as.camel.testsuite.smoke.mocks.MediationServiceMockImpl;
import org.jboss.as.camel.testsuite.smoke.upgrade.util.PibUtils;
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.handler.exception.EventHandlerException;
import com.ericsson.oss.mediation.engine.api.MediationEngine;

/**
 * The Class CamelEngineTest.
 */
@RunWith(Arquillian.class)
public class CamelEngineTest {

    private static final String BEANS_XML = "beans.xml";

    private static final String WEB_XML = "web.xml";

    private static final String DEP_CAMEL_EAR = "camel-engine.ear";

    private static final String DEP_NAME_HANDLER_CONTRIB_WAR = "CamelEngineTestHandlers.war";

    private static final String SINGLETON_APP = "singleton";

    private static final String SINGLETON_WAR = SINGLETON_APP + ".war";

    private static final String MS_MOCK_WAR = "CAMEL_ENGINE_TEST_ms_mock.war";

    private static final String TEST_WAR = "CAMEL_ENGINE_TEST_test.war";

    private static final String MSG_MS_UNDER_UPGRADE = "Unable to process the request because Camel Engine is upgrading...";

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelEngineTest.class);

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @EJB(lookup = "java:global/" + SINGLETON_APP
            + "/SomeSingleton!org.jboss.as.camel.testsuite.smoke.camelcontrib.tests.SomeSingletonRemoteInterface")
    private SomeSingletonRemoteInterface someSingleton;

    @EJB(lookup = "java:/mediationServiceMock")
    private MediationServiceMock medServiceMock;

    @CamelContextService
    private CamelContext ctx;

    @Deployment(name = "PIB", managed = true, testable = false, order = 1)
    public static Archive<?> createPibDeployment() {
        LOGGER.debug("******Creating PIB.ear deployment and deploying it to server******");
        return Deployments.createEnterpriseArchiveDeployment(Dependencies.COM_ERICSSON_OSS_ITPF_PIB);
    }

    @Deployment(name = DEP_CAMEL_EAR, managed = false, testable = false, order = 1)
    public static Archive<?> createCamelEar() {
        final File archiveFile = Dependencies.resolveArtifactWithoutDependencies(Dependencies.CAMEL_EAR);
        return ShrinkWrap.createFromZipFile(EnterpriseArchive.class, archiveFile);
    }

    @Deployment(name = SINGLETON_WAR, managed = false, testable = false, order = 2)
    public static Archive<?> createSingleDeployment() {
        final WebArchive singletonWaR = ShrinkWrap.create(WebArchive.class, SINGLETON_WAR);
        singletonWaR.addAsWebInfResource(WEB_XML, WEB_XML);
        singletonWaR.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        singletonWaR.addClass(SomeSingleton.class);
        singletonWaR.addClass(SomeSingletonLocalInterface.class);
        singletonWaR.addClass(SomeSingletonRemoteInterface.class);
        return singletonWaR;
    }

    @Deployment(name = MS_MOCK_WAR, managed = false, testable = false, order = 3)
    public static Archive<?> createMsMockDeployment() {

        final WebArchive msMockWar = ShrinkWrap.create(WebArchive.class, MS_MOCK_WAR);
        msMockWar.addClass(MediationServiceMock.class);
        msMockWar.addClass(MediationServiceMockImpl.class);
        msMockWar.addClass(MediationEngine.class);
        msMockWar.addAsWebInfResource("web.xml", "web.xml");
        msMockWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        // This is mocking mediation service, and with this it gets camel-subsystem module on the class path
        // which is what is happening today, and SHOULD NOT BE THE CASE!!!
        // THIS HAS TO GO!
        msMockWar.addAsWebInfResource("jboss-deployment-structure.xml");
        msMockWar.addAsLibraries(Dependencies.resolveArtifactWithoutDependencies(Dependencies.MOCKITO_ALL));
        msMockWar.addAsLibraries(Dependencies.resolveArtifactWithoutDependencies(Dependencies.MODEL_SERVICE_API));

        return msMockWar;
    }

    @Deployment(name = DEP_NAME_HANDLER_CONTRIB_WAR, managed = false, testable = false, order = 3)
    public static Archive<?> createHandlerContribDeploymentWar() {
        final WebArchive contribWar = ShrinkWrap.create(WebArchive.class, DEP_NAME_HANDLER_CONTRIB_WAR);
        contribWar.addAsWebInfResource(WEB_XML, WEB_XML);
        contribWar.addAsWebInfResource(EmptyAsset.INSTANCE, BEANS_XML);
        contribWar.addClass(SomeEventHandler.class);
        contribWar.addClass(ExceptionEventHandler.class);
        // contribWar.addClass(SomeTypedEventHandler1.class);
        // contribWar.addClass(SomeTypedEventHandler2.class);
        contribWar.addClass(SomeSingletonRemoteInterface.class);
        return contribWar;
    }

    @Deployment(name = TEST_WAR, managed = false, testable = true, order = 4)
    public static Archive<?> createTestDeployment() {
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, TEST_WAR);
        testWar.addAsWebInfResource("web.xml", "web.xml");
        testWar.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        testWar.addClass(SomeSingletonRemoteInterface.class);
        testWar.addClass(CamelEngineTest.class);
        testWar.addClass(TestConstants.class);
        testWar.addClass(MediationServiceMock.class);
        // Test is using these two to assert exception thrown
        testWar.addClass(EventHandlerException.class);
        testWar.addClass(PibUtils.class);
        return testWar;
    }

    @InSequence(1)
    @Test
    public void test_deploy_camel_engine() {
        LOGGER.debug("{}::DEPLOY CAMEL ENGINE", CamelEngineTest.class.getCanonicalName());
        deployer.deploy(DEP_CAMEL_EAR);
    }

    @InSequence(2)
    @Test
    public void test_deploy_singleton() {
        LOGGER.debug("{}::DEPLOY SINGLETON WAR ", CamelEngineTest.class.getCanonicalName());
        deployer.deploy(SINGLETON_WAR);
    }

    @InSequence(2)
    @Test
    public void test_deploy_ms_mock_war() {
        LOGGER.debug("{}::DEPLOY MS MOCK WAR ", CamelEngineTest.class.getCanonicalName());
        deployer.deploy(MS_MOCK_WAR);
    }

    @InSequence(3)
    @Test
    public void test_deploy_contribution() {
        LOGGER.debug("{}::DEPLOY HANDLERS WAR ", CamelEngineTest.class.getCanonicalName());
        deployer.deploy(DEP_NAME_HANDLER_CONTRIB_WAR);
    }

    @InSequence(4)
    @Test
    public void deploy_test() {
        LOGGER.debug("{}::DEPLOY TEST WAR ", CamelEngineTest.class.getCanonicalName());
        deployer.deploy(TEST_WAR);
    }

    @InSequence(5)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void testExecuteWithContributedProcessorEventHandlerCamelEar() throws Exception {
        LOGGER.debug("CamelEngineTest Smoke Test: Test Invoke Flow with Contributed EventHandler with Real Camel Engine");
        medServiceMock.createFlow("simpleFlowPath");
        final HashMap<String, Object> headers = new HashMap<String, Object>();
        final HashMap<String, Object> header = new HashMap<String, Object>();
        header.put(TestConstants.CONTRIB_TEST_MSG_BODY, "This ");
        headers.put(TestConstants.SOME_EVENT_HANDLER_FQCN, header);
        medServiceMock.invokeFlow(MediationServiceMock.CAMEL_ENGINE_HANDLER_ROUTE, headers);
        Assert.assertEquals(TestConstants.CONTRIB_TEST_MSG_BODY_EXPECTED, someSingleton.getMessage());
    }

    @InSequence(6)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void test_ExceptionThrownDuringTransactionalFlow() throws Exception {
        LOGGER.debug("CamelEngineTest Smoke Test: Test Invoke Flow with Contributed EventHandler with Real Camel Engine, Handler Throws Exception");
        medServiceMock.createFlow("simpleFlowPath");
        medServiceMock.createFlow("exceptionFlowPath");
        final HashMap<String, Object> headers = new HashMap<String, Object>();
        try {
            medServiceMock.invokeTransactionalFlow(MediationServiceMock.CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE, headers);
            Assert.fail();
        } catch (final Exception e) {
            // Nested exceptions thrown from handler
            Assert.assertEquals("Exception thrown was not of type EventHandlerException", EventHandlerException.class, e.getCause().getCause()
                    .getClass());
        }
    }

    @InSequence(7)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void test_ExceptionThrownDuringNonTransactionalFlow() throws Exception {
        LOGGER.debug("CamelEngineTest Smoke Test: Test Invoke Flow with Contributed EventHandler with Real Camel Engine, Handler Throws Exception");
        medServiceMock.createFlow("exceptionFlowPath");
        final HashMap<String, Object> headers = new HashMap<String, Object>();
        try {
            medServiceMock.invokeFlow(MediationServiceMock.CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE, headers);
            Assert.fail();
        } catch (final Exception e) {
            // Nested exceptions thrown from handler
            Assert.assertEquals("Exception thrown was not of type EventHandlerException", EventHandlerException.class, e.getCause().getCause()
                    .getClass());
        }
    }

    @InSequence(8)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void test_ExceptionThrownDuringTransactionalFlowWithChoiceElement() throws Exception {
        LOGGER.debug("CamelEngineTest Smoke Test: Test Invoke Flow with Contributed EventHandler with Real Camel Engine, Handler Throws Exception");
        medServiceMock.createFlowWithChoiceElement("simpleFlowPath");
        medServiceMock.createFlowWithChoiceElement("exceptionFlowPath");
        final HashMap<String, Object> headers = new HashMap<String, Object>();
        try {
            medServiceMock.invokeTransactionalFlow(MediationServiceMock.CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE, headers);
            Assert.fail();
        } catch (final Exception e) {
            // Nested exceptions thrown from handler
            Assert.assertEquals("Exception thrown was not of type EventHandlerException", EventHandlerException.class, e.getCause().getCause()
                    .getClass());
        }
    }

    @InSequence(9)
    @OperateOnDeployment(TEST_WAR)
    @Test
    public void test_ExceptionThrownDuringNonTransactionalFlowWithChoiceElement() throws Exception {
        LOGGER.debug("CamelEngineTest Smoke Test: Test Invoke Flow with Contributed EventHandler with Real Camel Engine, Handler Throws Exception");
        medServiceMock.createFlowWithChoiceElement("exceptionFlowPath");
        final HashMap<String, Object> headers = new HashMap<String, Object>();
        try {
            medServiceMock.invokeFlow(MediationServiceMock.CAMEL_ENGINE_EXCEPTION_HANDLER_ROUTE, headers);
            Assert.fail();
        } catch (final Exception e) {
            // Nested exceptions thrown from handler
            Assert.assertEquals("Exception thrown was not of type EventHandlerException", EventHandlerException.class, e.getCause().getCause()
                    .getClass());
        }
    }

    @Test
    @InSequence(10)
    @OperateOnDeployment(TEST_WAR)
    public void testCamelEngineUpgrade() throws Exception {
        LOGGER.debug("Testing PIB Upgrade REST call");
        try {
            String returnVal;

            returnVal = PibUtils.sendUpgradeRequest();

            Thread.sleep(3000); // Routes have a shutdown timeout of 3sec, have to wait that time.
            testRoutesAreStopped();
            testInvokeCamelEngineUnderUpgrade();

            returnVal = PibUtils.getUpgradeRequestResponse(returnVal);

            // Assert if OK response is received for the upgrade request
            Assert.assertTrue("The Ok respose should be received for the upgarde Request", returnVal.contains(":OK"));
        } catch (final MalformedURLException e) {
            LOGGER.debug("Error in test rest call: {}", e);
            Assert.fail(e.getMessage());
        } catch (final IOException e) {
            LOGGER.debug("Error in test rest call: {}", e);
            Assert.fail(e.getMessage());
        }
    }

    private void testRoutesAreStopped() throws Exception {
        LOGGER.debug("Testing if routes are stopped.");
        final List<Route> routes = ctx.getRoutes();
        LOGGER.debug("There are {} routes", routes.size());
        ServiceStatus status;
        for (final Route route : routes) {
            status = ctx.getRouteStatus(route.getId());
            Assert.assertTrue(status.isStopped());
        }
    }

    private void testInvokeCamelEngineUnderUpgrade() {
        try {
            LOGGER.debug("Invoking Flow while Camel Engine is under upgrade.");
            medServiceMock.invokeFlow("simpleFlowPath", new HashMap<String, Object>());
            Assert.fail("Should have thrown exception.");
        } catch (final Exception ex) {
            LOGGER.debug("Exception thrown invoking flow: " + ex.getMessage());
            Assert.assertTrue("Exception thrown during upgrade test was not as expected", ex.getMessage().contains(MSG_MS_UNDER_UPGRADE));
        }
    }
}
