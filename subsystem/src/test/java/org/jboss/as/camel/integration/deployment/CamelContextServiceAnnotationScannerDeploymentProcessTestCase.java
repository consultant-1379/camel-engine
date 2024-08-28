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

import org.apache.camel.CamelContext;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.AttachmentList;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.as.server.deployment.module.MountHandle;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.vfs.VirtualFile;
import org.jboss.vfs.VirtualFileFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class CamelContextServiceAnnotationScannerDeploymentProcessTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class CamelContextServiceAnnotationScannerDeploymentProcessTestCase {

    @Mock
    private DeploymentPhaseContext dpc;

    @Mock
    private DeploymentUnit du;

    @Mock
    private MountHandle mh;

    private CamelContextServiceAnnotationScannerDeploymentProcess scanner;

    public Index createTestIndex() throws IOException {
        final Indexer indexer = new Indexer();
        final InputStream stream = getClass().getClassLoader().getResourceAsStream(DummyClass.class.getName().replace('.', '/') + ".class");
        indexer.index(stream);
        final Index innerIndex = indexer.complete();
        return innerIndex;
    }

    public void setUpEarTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.ear");
        final ResourceRoot topLevel = new ResourceRoot(topLevelDeployment, mh);
        topLevel.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevel);
    }

    @Test
    public void testDeploySkipProcessingEar() throws Exception {
        setUpEarTestCase();
        final CamelContextServiceAnnotationScannerDeploymentProcess scannerService = new CamelContextServiceAnnotationScannerDeploymentProcess();
        scannerService.deploy(dpc);
        Mockito.verify(du, Mockito.times(0)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContextServiceMetaInfo.class));
    }

    public void setUpWarTestCase() throws Exception {
        Mockito.when(dpc.getDeploymentUnit()).thenReturn(du);
        final VirtualFile topLevelDeployment = VirtualFileFactory.createVirtualFileWithNoParent("test.war");
        final ResourceRoot topLevelRoot = new ResourceRoot(topLevelDeployment, mh);
        topLevelRoot.putAttachment(Attachments.ANNOTATION_INDEX, createTestIndex());
        Mockito.when(du.getAttachment(Attachments.DEPLOYMENT_ROOT)).thenReturn(topLevelRoot);
    }

    @Test
    public void testDeployProcessWar() throws Exception {
        setUpWarTestCase();
        scanner = new CamelContextServiceAnnotationScannerDeploymentProcess();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContextServiceMetaInfo.class));
        verifCamelContextInfo();
    }

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

    @Test
    public void testDeployProcessWarAndVerifyWebInfLibIsFiltered() throws Exception {
        setUpWarFileterTestCase();
        scanner = new CamelContextServiceAnnotationScannerDeploymentProcess();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContextServiceMetaInfo.class));
    }

    @Test
    public void testUnDeployProcessWar() throws Exception {
        setUpWarFileterTestCase();
        scanner = new CamelContextServiceAnnotationScannerDeploymentProcess();
        scanner.deploy(dpc);
        Mockito.verify(du, Mockito.times(1)).putAttachment(Matchers.any(AttachmentKey.class), Matchers.any(CamelContextServiceMetaInfo.class));
        scanner.undeploy(dpc.getDeploymentUnit());
    }

    private void verifCamelContextInfo() throws IOException {

        CamelContextServiceMetaInfo contextServiceMetaInfo = new CamelContextServiceMetaInfo(du);
        contextServiceMetaInfo.setDu(du);

        final List<ResourceRoot> resourceRoots = DeploymentUtils.allResourceRoots(contextServiceMetaInfo.getDu());
        final ResourceRoot topLevelRoot = resourceRoots.get(0);
        Set<FieldInfo> fieldInfoSet = processAnnotations(createTestIndex());
        contextServiceMetaInfo.setInjectionPoints(fieldInfoSet);
        contextServiceMetaInfo.setRr(topLevelRoot);

        Mockito.when(du.getAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO)).thenReturn(contextServiceMetaInfo);

        contextServiceMetaInfo = du.getAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO);
        Assert.assertNotNull(contextServiceMetaInfo);
        Assert.assertNotNull(contextServiceMetaInfo.getRr());
        Assert.assertNotNull(contextServiceMetaInfo.toString());
        fieldInfoSet = contextServiceMetaInfo.getInjectionPoints();
        Assert.assertNotNull(fieldInfoSet);
        for (final FieldInfo fieldInfo : fieldInfoSet) {
            Assert.assertNotNull(fieldInfo);
        }
    }

    private Set<FieldInfo> processAnnotations(final Index innerIndex) {
        final List<AnnotationInstance> annotationList = innerIndex.getAnnotations(DotName.createSimple(CamelContextService.class.getName()));
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        if (!annotationList.isEmpty()) {

            for (final AnnotationInstance inst : annotationList) {
                if (inst.target() instanceof FieldInfo) {
                    final FieldInfo fieldInfo = (FieldInfo) inst.target();
                    fieldInfoSet.add(fieldInfo);
                }
            }
        }
        return fieldInfoSet;
    }

    /**
     * The Class DummyClass.
     */
    public class DummyClass implements Serializable {

        private static final long serialVersionUID = 1L;
        @CamelContextService
        private CamelContext ctx;
    }
}
