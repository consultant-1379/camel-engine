/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.integration.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.transaction.TransactionManager;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.jboss.as.camel.integration.deployment.CamelContributionBeanMetaInfo;
import org.jboss.as.camel.integration.deployment.CamelContributionMetaInfoHolder;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.camel.exception.CamelEngineException;
import org.jboss.camel.tx.TransactionPolicy;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.msc.service.StartException;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * The Class CamelContextServiceTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamelContextServiceTestCase {

    @Rule
    public ExpectedException expectedExceptionForNullContextName = ExpectedException.none();

    @Rule
    public ExpectedException expectedExceptionForNonRegisterContextLookup = ExpectedException.none();
    private CamelContextService service;
    private final String contextName = "test.war";
    private final String key = "test.war";

    @Mock
    private TransactionManager txMgr;

    @Mock
    private DeploymentUnit du;

    @Mock
    private DeploymentUnit topLevelDu;

    @Mock
    private DeploymentMetaInfo sharedContext;

    @Mock
    private CamelContributionMetaInfoHolder metaInfoHolder;

    @Mock
    private BeanManager beanManager;

    @Mock
    private MountHandle mh;

    @Test
    public void testCamelContextServiceInstanceCreationVerifyAllAttributesInitializedWhenSharingContext() throws Exception {
        final String innerContextName = "testContext";
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, innerContextName);
        Assert.assertTrue(service.shareContextEnabled);
        Assert.assertNotNull(service.instantiatedContexts);
        Assert.assertEquals(0, service.instantiatedContexts.size());
        Assert.assertNotNull(service.boundBeans);
        Assert.assertEquals(0, service.boundBeans.size());
        Assert.assertNotNull(service.jtaTransactionManager);
        Assert.assertNotNull(service.getSharedContext());
    }

    @Test
    public void testCamelContextServiceInstanceCreationVerifyAllAttributesInitializedWhenSharingContextWithNullName() throws Exception {
        expectedExceptionForNullContextName.expect(StartException.class);
        expectedExceptionForNullContextName.expectMessage("CamelContext name  must be specified and not empty");
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, null);
        Assert.assertTrue(service.shareContextEnabled);
        Assert.assertNotNull(service.instantiatedContexts);
        Assert.assertEquals(0, service.instantiatedContexts.size());
        Assert.assertNotNull(service.boundBeans);
        Assert.assertEquals(0, service.boundBeans.size());
        Assert.assertNotNull(service.jtaTransactionManager);
        Assert.assertNotNull(service.getSharedContext());
    }

    @Test
    public void testCamelContextServiceInstanceCreationVerifyAllAttributesInitializedWhenNotSharingContext() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, "testContext");
        Assert.assertFalse(service.shareContextEnabled);
        Assert.assertNotNull(service.instantiatedContexts);
        Assert.assertEquals(0, service.instantiatedContexts.size());
        Assert.assertNotNull(service.boundBeans);
        Assert.assertEquals(0, service.boundBeans.size());
        Assert.assertNotNull(service.jtaTransactionManager);
        Assert.assertNull(service.getSharedContext());
    }

    @Test
    public void testCreateCamelContextForDeploymentWhenContextIsShared() throws Exception {
        final String temporalContextName = "test";
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, temporalContextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(temporalContextName);
        Assert.assertNotNull(dmi);
        Assert.assertEquals(service.getSharedContext(), dmi);
    }

    @Test
    public void testCreateCamelContextForDeploymentWhenContextIsSharedNullSupplied() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(null);
        Assert.assertNotNull(dmi);
        Assert.assertEquals(service.getSharedContext(), dmi);
    }

    @Test
    public void testCreateCamelContextForDeploymentWhenContextIsNotShared() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(contextName);
        Assert.assertNotNull(dmi);
        Assert.assertNotSame(service.getSharedContext(), dmi);
    }

    @Test
    public void testCreateCamelContextForDeploymentWhenContextIsNotSharedANDNullSupplied() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        final DeploymentMetaInfo dmi = service.createCamelContextForDeployment(null);
        Assert.assertNotNull(dmi);
        Assert.assertNotSame(service.getSharedContext(), dmi);
        Assert.assertEquals("null-camel-context", dmi.getCamelContext().getName());
    }

    @Test
    public void testStartCamelContextWhenContextIsShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.startCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(0)).getCamelContext();
        Mockito.verify(mock, Mockito.times(0)).start();
    }

    @Test
    public void testStartCamelContextWhenContextIsNotShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.startCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(1)).getCamelContext();
        Mockito.verify(mock, Mockito.times(1)).start();
    }

    @Test
    public void testStopCamelContextWhenContextIsShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.stopCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(0)).getCamelContext();
        Mockito.verify(mock, Mockito.times(0)).stop();
    }

    @Test
    public void testStopCamelContextWhenContextIsNotShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.stopCamelContext(dmi);
        Mockito.verify(dmi, Mockito.times(1)).getCamelContext();
        Mockito.verify(mock, Mockito.times(1)).stop();
    }

    @Test(expected = CamelEngineException.class)
    public void testStopCamelContextExceptionWhenContextIsNotShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        PowerMockito.doThrow(new Exception()).when(mock).stop();

        service = new CamelContextService(false, contextName);
        service.stopCamelContext(dmi);
    }

    @Test
    public void testRegisterCamelContextWhenContextIsShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.registerCamelContext(dmi, key);
        Assert.assertNull(service.instantiatedContexts.get(key));
    }

    @Test
    public void testRegisterCamelContextWhenContextIsNotShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.registerCamelContext(dmi, key);
        Assert.assertNotNull(service.instantiatedContexts.get(key));
        Assert.assertEquals(dmi, service.instantiatedContexts.get(key));
    }

    @Test
    public void testDeregisterCamelContextWhenContextIsShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        service.deregisterCamelContext(key);
        Assert.assertNull(service.instantiatedContexts.get(key));
    }

    @Test
    public void testDeregisterCamelContextWhenContextIsNotShared() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.registerCamelContext(dmi, key);
        service.deregisterCamelContext(key);
        Assert.assertNull(service.instantiatedContexts.get(key));
    }

    @Test
    public void testGetCamelContextForDeploymentWhenContextIsShared() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo dmi = service.getCamelContextForDeployment(key);
        Assert.assertEquals(service.getSharedContext(), dmi);
    }

    @Test
    public void testGetCamelContextForDeploymentWhenContextIsNotSharedAndNonExisting() throws Exception {
        expectedExceptionForNonRegisterContextLookup.expect(IllegalStateException.class);
        expectedExceptionForNonRegisterContextLookup.expectMessage("Deployment " + key + " did not register any camel contexts");
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.getCamelContextForDeployment(key);
    }

    @Test
    public void testGetCamelContextForDeploymentWhenContextIsNotSharedAndExisting() throws Exception {
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        service.registerCamelContext(dmi, key);
        final DeploymentMetaInfo retVal = service.getCamelContextForDeployment(key);
        Assert.assertNotNull(retVal);
        Assert.assertEquals(dmi, retVal);
    }

    @Test
    public void testGetCamelContextForDeploymentWhenContextIsSharedExisting() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo retVal = service.getCamelContextForDeployment(key);
        Assert.assertNotNull(retVal);
        Assert.assertEquals(service.getSharedContext(), retVal);
    }

    @Test
    public void testDestroyWhenContextIsShared() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final CamelContext mock = Mockito.mock(CamelContext.class);
        final JndiContext jndiMock = Mockito.mock(JndiContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(mock);
        Mockito.when(dmi.getRegistry()).thenReturn(jndiMock);
        service = new CamelContextService(true, contextName);
        service.setSharedContext(dmi);
        service.destroy();
        Mockito.verify(mock, Mockito.times(1)).stop();
        Mockito.verify(jndiMock, Mockito.times(1)).close();
        Mockito.verify(dmi, Mockito.times(1)).setCamelContext(null);
        Mockito.verify(dmi, Mockito.times(1)).setRegistry(null);
    }

    @Test
    public void testDestroyWhenContextIsNotShared() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        final DeploymentMetaInfo dmi1 = service.createCamelContextForDeployment("deployment1.war");
        final DeploymentMetaInfo dmi2 = service.createCamelContextForDeployment("deployment2.war");
        final DeploymentMetaInfo dmi3 = service.createCamelContextForDeployment("deployment3.war");
        service.registerCamelContext(dmi1, "deployment1.war");
        service.registerCamelContext(dmi2, "deployment2.war");
        service.registerCamelContext(dmi3, "deployment3.war");
        service.destroy();
        Assert.assertTrue(service.instantiatedContexts.isEmpty());
    }

    @Test
    public void testCreateCamelContext() throws Exception {
        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo dmi = service.getSharedContext();
        Assert.assertNotNull(dmi);
        Assert.assertNotNull(dmi.getCamelContext());
        Assert.assertNotNull(dmi.getRegistry());
        Assert.assertEquals(key, dmi.getCamelContext().getName());
        Assert.assertNotNull(dmi.getRegistry().lookup(TransactionPolicy.PROPAGATION_REQUIRED));
        Assert.assertNotNull(dmi.getRegistry().lookup(TransactionPolicy.PROPAGATION_REQUIRES_NEW));
    }

    @Test
    public void testUnBindBeansWhenContextIsShared() throws Exception {

        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(true, contextName);
        final DeploymentMetaInfo dmi = service.getSharedContext();
        Mockito.when(metaInfoHolder.getDeploymentUnit()).thenReturn(du);
        Mockito.when(du.getName()).thenReturn("Test Deployment Unit");

        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.war");
        final ResourceRoot topLevelRoot = new ResourceRoot(topLevelDeployment, mh);
        final Set<CamelContributionBeanMetaInfo> beanMetaInfoSet = processAnnotations(createTestIndex(), topLevelRoot, du);
        // First bound beans
        service.bindBeans(metaInfoHolder, beanManager);

        Mockito.when(metaInfoHolder.getCamelBeanMetaInfo()).thenReturn(beanMetaInfoSet);
        service.unbindBeans(du.getName());

        Assert.assertNotNull(dmi);
        Assert.assertNotNull(dmi.getCamelContext());
        Assert.assertNotNull(dmi.getRegistry());
        Assert.assertEquals(key, dmi.getCamelContext().getName());
        Assert.assertNotNull(dmi.getRegistry().lookup(TransactionPolicy.PROPAGATION_REQUIRED));
        Assert.assertNotNull(dmi.getRegistry().lookup(TransactionPolicy.PROPAGATION_REQUIRES_NEW));
        Mockito.verify(metaInfoHolder, Mockito.times(2)).getCamelBeanMetaInfo();
    }

    @Test
    public void testUnBindBeansWhenContextIsNotShared() throws Exception {

        TransactionUtil.setTransactionManager(txMgr);
        service = new CamelContextService(false, contextName);
        // final DeploymentMetaInfo dmi = service.getSharedContext();
        Mockito.when(metaInfoHolder.getDeploymentUnit()).thenReturn(du);
        Mockito.when(du.getName()).thenReturn("Test Deployment Unit");

        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.war");
        final ResourceRoot topLevelRoot = new ResourceRoot(topLevelDeployment, mh);
        final Set<CamelContributionBeanMetaInfo> beanMetaInfoSet = processAnnotations(createTestIndex(), topLevelRoot, du);
        // First bound beans
        service.bindBeans(metaInfoHolder, beanManager);

        Mockito.when(metaInfoHolder.getCamelBeanMetaInfo()).thenReturn(beanMetaInfoSet);
        final DeploymentMetaInfo dmi = Mockito.mock(DeploymentMetaInfo.class);
        final DefaultCamelContext camelContext = Mockito.mock(DefaultCamelContext.class);
        final JndiContext jndiMock = Mockito.mock(JndiContext.class);
        Mockito.when(dmi.getCamelContext()).thenReturn(camelContext);
        Mockito.when(camelContext.getName()).thenReturn("test");
        Mockito.when(dmi.getRegistry()).thenReturn(jndiMock);
        service.registerCamelContext(dmi, key);
        Assert.assertNotNull(service.instantiatedContexts.get(key));
        Assert.assertEquals(dmi, service.instantiatedContexts.get(key));

        service.unbindBeans(du.getName());
        Assert.assertNotNull(dmi);
        Mockito.verify(dmi, Mockito.times(1)).getRegistry();
        Mockito.verify(jndiMock, Mockito.times(1)).unbind(Matchers.anyString());
    }

    private Set<CamelContributionBeanMetaInfo>
            processAnnotations(final Index index, final ResourceRoot resourceRoot, final DeploymentUnit unit) {
        final Set<CamelContributionBeanMetaInfo> beanMetaInfoSet = new HashSet<CamelContributionBeanMetaInfo>();
        // For production handler classes with EventHandler annotation
        final List<AnnotationInstance> annotationEventHandlerList = index.getAnnotations(DotName.createSimple(EventHandler.class.getName()));
        if (!annotationEventHandlerList.isEmpty()) {
            for (final AnnotationInstance inst : annotationEventHandlerList) {
                if (inst.target() instanceof ClassInfo) {
                    final ClassInfo classInfo = (ClassInfo) inst.target();
                    final CamelContributionBeanMetaInfo beanMetaInfo = new CamelContributionBeanMetaInfo(inst, inst.target().toString());
                    beanMetaInfoSet.add(beanMetaInfo);
                }
            }
        }
        return beanMetaInfoSet;
    }

    public Index createTestIndex() throws IOException {
        final Indexer indexer = new Indexer();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        final Index index = indexer.complete();
        return index;
    }

    /**
     * The Class DummyClass.
     */
    @EventHandler(contextName = "test")
    public class DummyClass implements Serializable {

        private static final long serialVersionUID = 1L;
    }
}
