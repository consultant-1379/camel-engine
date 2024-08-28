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

package org.jboss.as.camel.integration.deployment;

import java.util.HashSet;
import java.util.Set;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.jandex.FieldInfo;

/**
 * CamelContext injection metainfo.
 */
public class CamelContextServiceMetaInfo {

    private DeploymentUnit deploymentUnit;
    private ResourceRoot resourceRoot;
    private Set<FieldInfo> injectionPoints;

    /**
     * Full arg constructor.
     *
     * @param deploymentUnit
     *            Deployment unit.
     */
    public CamelContextServiceMetaInfo(final DeploymentUnit deploymentUnit) {
        this.injectionPoints = new HashSet<FieldInfo>();
        this.deploymentUnit = deploymentUnit;
    }

    public DeploymentUnit getDu() {
        return deploymentUnit;
    }

    public void setDu(final DeploymentUnit deploymentUnit) {
        this.deploymentUnit = deploymentUnit;
    }

    public ResourceRoot getRr() {
        return resourceRoot;
    }

    public void setRr(final ResourceRoot resourceRoot) {
        this.resourceRoot = resourceRoot;
    }

    public Set<FieldInfo> getInjectionPoints() {
        return injectionPoints;
    }

    public void setInjectionPoints(final Set<FieldInfo> injectionPoints) {
        this.injectionPoints = injectionPoints;
    }

    @Override
    public String toString() {
        return "CamelContextServiceMetaInfo [du=" + deploymentUnit + ", rr=" + resourceRoot + ", injectionPoints=" + injectionPoints + "]";
    }
}
