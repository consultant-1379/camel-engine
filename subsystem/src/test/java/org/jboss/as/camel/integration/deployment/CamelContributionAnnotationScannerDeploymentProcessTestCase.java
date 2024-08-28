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

package org.jboss.as.camel.integration.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * The Class CamelContributionAnnotationScannerDeploymentProcessTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamelContributionAnnotationScannerDeploymentProcessTestCase {

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private MountHandle mh;

    @Mock
    private ServiceRegistry serviceRegistry;

    private CamelContributionAnnotationScannerDeploymentProcess scanner;

    /**
     * Creates the test index.
     *
     * @return the index
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Index createTestIndex() throws IOException {
        final Indexer indexer = new Indexer();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        final Index index = indexer.complete();
        return index;
    }

    /**
     * Sets the up ear test case.
     *
     * @throws Exception
     *             the exception
     */
    public void setUpEarTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.ear");
        final ResourceRoot topLevel = new ResourceRoot(topLevelDeployment, mh);
        topLevel.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevel);
    }

    /**
     * Test deploy skip process ear.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDeploySkipProcessEar() throws Exception {
        setUpEarTestCase();
        scanner = new CamelContributionAnnotationScannerDeploymentProcess();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(0)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContributionMetaInfoHolder.class));
    }

    /**
     * Sets the up war test case.
     *
     * @throws Exception
     *             the exception
     */
    public void setUpWarTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.war");
        final ResourceRoot topLevelRoot = new ResourceRoot(topLevelDeployment, mh);
        topLevelRoot.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevelRoot);
        Mockito.when(du.getServiceRegistry()).thenReturn(serviceRegistry);
    }

    /**
     * Test deploy process war.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDeployProcessWar() throws Exception {
        setUpWarTestCase();
        scanner = new CamelContributionAnnotationScannerDeploymentProcess();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContributionMetaInfoHolder.class));
        verifyMetaHolderAndInfo();
        Assert.assertNull(scanner.getCamelContextService(du));
    }

    /**
     * Sets the up war fileter test case.
     *
     * @throws Exception
     *             the exception
     */
    public void setUpWarFileterTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.war");
        final ResourceRoot topLevelRoot = new ResourceRoot(topLevelDeployment, mh);
        final VirtualFile subLevel = VirtualFileFactory.createVirtualFileWithParent("/web-inf/lib/test.jar", topLevelDeployment);
        final ResourceRoot subLevelRoot = new ResourceRoot(subLevel, mh);
        final AttachmentList<ResourceRoot> resourceRoots = new AttachmentList<ResourceRoot>(ResourceRoot.class);
        resourceRoots.add(subLevelRoot);
        // Add annotation index to both sublevel and toplevel
        topLevelRoot.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        subLevelRoot.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());

        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevelRoot);
        Mockito.when(du.getAttachment(Attachments.RESOURCE_ROOTS)).thenReturn(resourceRoots);
    }

    /**
     * Test deploy_ process war veiry web inf lib is filtered.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDeploy_ProcessWarVeiryWebInfLibIsFiltered() throws Exception {
        setUpWarFileterTestCase();
        scanner = new CamelContributionAnnotationScannerDeploymentProcess();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContributionMetaInfoHolder.class));
    }

    /**
     * Test un deploy process war.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUnDeployProcessWar() throws Exception {
        setUpWarFileterTestCase();
        scanner = new CamelContributionAnnotationScannerDeploymentProcess();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContributionMetaInfoHolder.class));
        scanner.undeploy(dpc.getDeploymentUnit());
    }

    /**
     * Verify meta holder and info.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void verifyMetaHolderAndInfo() throws IOException {

        CamelContributionMetaInfoHolder metaInfoHolder = new CamelContributionMetaInfoHolder(du);
        final List<ResourceRoot> resourceRoots = DeploymentUtils.allResourceRoots(metaInfoHolder.getDeploymentUnit());
        final ResourceRoot topLevelRoot = resourceRoots.get(0);
        final Set<CamelContributionBeanMetaInfo> beanMetaInfoSet = processAnnotations(createTestIndex(), topLevelRoot, du);
        metaInfoHolder.setCamelBeanMetaInfo(beanMetaInfoSet);
        Mockito.when(du.getAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER)).thenReturn(metaInfoHolder);
        metaInfoHolder = du.getAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER);
        Assert.assertNotNull(metaInfoHolder);
        final Set<CamelContributionBeanMetaInfo> camelBeanMetaInfo = metaInfoHolder.getCamelBeanMetaInfo();
        Assert.assertNotNull(camelBeanMetaInfo);
        for (final CamelContributionBeanMetaInfo beanMetaInfo : camelBeanMetaInfo) {
            Assert.assertNotNull(beanMetaInfo);
            Assert.assertNotNull(beanMetaInfo.getClassInfo());
            Assert.assertNotNull(beanMetaInfo.getHandlerId());
            Assert.assertNotNull(beanMetaInfo.getContextName());
        }
    }

    /**
     * Process annotations.
     *
     * @param index
     *            the index
     * @param resourceRoot
     *            the resource root
     * @param unit
     *            the unit
     * @return the sets the
     */
    private Set<CamelContributionBeanMetaInfo>
            processAnnotations(final Index index, final ResourceRoot resourceRoot, final DeploymentUnit unit) {
        final Set<CamelContributionBeanMetaInfo> beanMetaInfoSet = new HashSet<CamelContributionBeanMetaInfo>();
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

    /**
     * The Class DummyClass.
     */
    @EventHandler(contextName = "test")
    public class DummyClass implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
