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
import org.jboss.camel.annotations.CamelContextService;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment processor that will search through annotation index and find all camel integration related annotations. It will look for annotations:
 * <pre>
 * CamelContextService injection point for camel context.
 * </pre>
 */
public class CamelContextServiceAnnotationScannerDeploymentProcess extends AbstractAnnotationScannerDeploymentProcess {

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

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextServiceAnnotationScannerDeploymentProcess.class);

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
        final CamelContextServiceMetaInfo metaInfoHolder = new CamelContextServiceMetaInfo(deploymentUnit);
        for (final ResourceRoot resourceRoot : resourceRoots) {
            LOGGER.trace("Scanning resource root: {} for annotations.", resourceRoot.getRoot().getPathName());
            final Index index = resourceRoot.getAttachment(ANNOTATION_INDEX);
            if (index != null) {
                try {
                    final Set<FieldInfo> fieldInfoSet = processAnnotations(index);
                    if (!fieldInfoSet.isEmpty()) {
                        metaInfoHolder.setInjectionPoints(fieldInfoSet);
                    }
                } catch (final Exception e) {
                    throw new DeploymentUnitProcessingException(e);
                }
            }
        }
        if (!metaInfoHolder.getInjectionPoints().isEmpty()) {
            deploymentUnit.putAttachment(CamelContextServiceAttachments.CAMEL_SERVICE_META_INFO, metaInfoHolder);
            phaseContext.addDependency(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME,
                    CamelContextServiceAttachments.CAMEL_CONTEXT_INTEGRATION_SERVICE);
        }
    }

    private Set<FieldInfo> processAnnotations(final Index index) {
        final List<AnnotationInstance> annotationList = index.getAnnotations(DotName.createSimple(CamelContextService.class.getName()));
        final Set<FieldInfo> fieldInfoSet = new HashSet<FieldInfo>();
        if (!annotationList.isEmpty()) {

            for (final AnnotationInstance inst : annotationList) {
                if (inst.target() instanceof FieldInfo) {
                    final FieldInfo fieldInfo = (FieldInfo) inst.target();
                    LOGGER.trace("Found injection point {} into field {}", fieldInfo.declaringClass(), fieldInfo.name());
                    fieldInfoSet.add(fieldInfo);
                }
            }
        }
        return fieldInfoSet;
    }
}
