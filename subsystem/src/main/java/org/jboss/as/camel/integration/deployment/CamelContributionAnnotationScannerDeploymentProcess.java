/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.integration.deployment;

import static org.jboss.as.server.deployment.Attachments.ANNOTATION_INDEX;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.as.camel.integration.service.CamelContextIntegrationService;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUtils;
import org.jboss.as.server.deployment.Phase;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.event.handler.annotation.EventHandler;

/**
 * Deployment processor that will search through annotation index and find all camel contribution related annotations It will look for these.
 * annotations:
 *
 * @CamelBean
 *            annotation marking bean as camel related one.
 */
public class CamelContributionAnnotationScannerDeploymentProcess extends AbstractAnnotationScannerDeploymentProcess {

    /**
     * See {@link Phase} for a description of the different phases.
     */
    public static final Phase PHASE = Phase.PARSE;
    /**
     * The relative order of this processor within the {@link #PHASE}. The current number is large enough for it to happen after all the standard
     * deployment unit processors that come with JBoss AS.
     */
    public static final int PRIORITY = 0x4000;

    /**
     * Skip all in web-inf/lib.
     */
    private static final String SKIP_WEB_INF_LIB = "/WEB-INF/LIB";

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContributionAnnotationScannerDeploymentProcess.class);

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        List<ResourceRoot> resourceRoots = DeploymentUtils.allResourceRoots(deploymentUnit);
        final ResourceRoot topLevelRoot = resourceRoots.get(0);
        LOGGER.trace("Processing deployment unit: {}", deploymentUnit);

        if (topLevelRoot.getRootName().endsWith(".ear")) {
            LOGGER.trace("Skipping ear resource root, we will process subdeployments");
            return;
        }
        if (topLevelRoot.getRootName().endsWith(".war")) {
            LOGGER.trace("Filtering WEB-INF/lib from war");
            resourceRoots = filterResourceRoots(resourceRoots, SKIP_WEB_INF_LIB);
        }
        final CamelContributionMetaInfoHolder metaInfoHolder = new CamelContributionMetaInfoHolder(deploymentUnit);
        for (final ResourceRoot resourceRoot : resourceRoots) {
            LOGGER.trace("Scanning resource root: {} for annotations.", resourceRoot.getRoot().getPathName());
            final Index index = resourceRoot.getAttachment(ANNOTATION_INDEX);
            if (index != null) {
                try {
                    final Set<CamelContributionBeanMetaInfo> beanMetaInfoSet = processAnnotations(index, resourceRoot, deploymentUnit);
                    if (!beanMetaInfoSet.isEmpty()) {
                        metaInfoHolder.setCamelBeanMetaInfo(beanMetaInfoSet);
                    }
                } catch (final Exception e) {
                    throw new DeploymentUnitProcessingException(e);
                }
            }
        }
        if (!metaInfoHolder.getCamelBeanMetaInfo().isEmpty()) {
            phaseContext.addDependency(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME,
                    CamelContextServiceAttachments.CAMEL_CONTEXT_INTEGRATION_SERVICE);
            deploymentUnit.putAttachment(CamelContextServiceAttachments.CAMEL_CONTRIBUTIONS_META_INFO_HOLDER, metaInfoHolder);
        }
    }

    /**
     * Process annotations from annotation index and find ones we are interested in.
     *
     * @param index
     *            Annotation index prepared by jandex.
     * @param resourceRoot
     *            Resource root for this deployment.
     * @param unit
     *            Deployment unit that we are processing.
     * @return Set of meta info, stored in CamelContributionBeanMetaInfo
     */
    protected final Set<CamelContributionBeanMetaInfo> processAnnotations(final Index index, final ResourceRoot resourceRoot,
            final DeploymentUnit unit) {
        final Set<CamelContributionBeanMetaInfo> beanMetaInfoSet = new HashSet<CamelContributionBeanMetaInfo>();
        // For production handler classes with EventHandler annotation
        final List<AnnotationInstance> annotationEventHandlerList = index.getAnnotations(DotName.createSimple(EventHandler.class.getName()));
        if (!annotationEventHandlerList.isEmpty()) {
            for (final AnnotationInstance inst : annotationEventHandlerList) {
                if (inst.target() instanceof ClassInfo) {
                    final ClassInfo classInfo = (ClassInfo) inst.target();
                    LOGGER.trace("Found @EventHandler annotation on: {}", classInfo.name());
                    final CamelContributionBeanMetaInfo beanMetaInfo = new CamelContributionBeanMetaInfo(inst, inst.target().toString());
                    beanMetaInfoSet.add(beanMetaInfo);
                }
            }
        }
        return beanMetaInfoSet;
    }
}
