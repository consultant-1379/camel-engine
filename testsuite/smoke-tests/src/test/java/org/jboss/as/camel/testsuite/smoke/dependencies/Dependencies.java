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

package org.jboss.as.camel.testsuite.smoke.dependencies;

import java.io.File;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * The Class Dependencies.
 */
public final class Dependencies {

    public static final String ORG_APACHE_CAMEL_CORE = "org.apache.camel:camel-core:jar:?";
    public static final String ORG_JBOSS_CAMEL_INTEGRATION_API = "org.jboss.as.camel:camel-integration-api-module:jar:?";
    public static final String CAMEL_EAR = "org.jboss.as.camel:camel-engine-ear:ear:?";
    public static final String CORE_MEDIATION_FLOW_API = "com.ericsson.nms.mediation:core-mediation-flow-api:jar:?";
    public static final String CORE_MEDIATION_DSL_API = "com.ericsson.nms.mediation:core-mediation-dsl-api:jar:?";
    public static final String MOCKITO_ALL = "org.mockito:mockito-all:jar:?";
    public static final String MODEL_SERVICE_API = "com.ericsson.oss.itpf.modeling:model-service-api-jar:jar:?";

    /* Platform Integration Bridge Artifacts */
    public static final String COM_ERICSSON_OSS_ITPF_PIB = "com.ericsson.oss.itpf.common:PlatformIntegrationBridge-ear:ear:?";

    private Dependencies() {}

    /**
     * Resolve artifact with given coordinates without any dependencies, this method should be used to resolve just the artifact with given name, and
     * it can be used for adding artifacts as modules into EAR.
     * If artifact can not be resolved, or the artifact was resolved into more then one file then the IllegalStateException will be thrown
     *
     * @param artifactCoordinates
     *            in usual maven format.
     *            <pre> {@code<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} </pre>
     * @return File representing resolved artifact
     */
    public static File resolveArtifactWithoutDependencies(final String artifactCoordinates) {
        return Maven.configureResolver().loadPomFromFile("pom.xml").resolve(artifactCoordinates).withoutTransitivity().asSingleFile();
    }

    /**
     * Resolve dependencies for artifact with given coordinates, if artifact can not be resolved IllegalState exception will be thrown.
     *
     * @param artifactCoordinates
     *            in usual maven format.
     *            <pre> {@code<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>} </pre>
     * @return resolved dependencies
     */
    public static File[] resolveArtifactDependencies(final String artifactCoordinates) {
        return Maven.configureResolver().loadPomFromFile("pom.xml").resolve(artifactCoordinates).withTransitivity().asFile();
    }
}
