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

/**
 * Meta info for camel contributions.
 */
public class CamelContributionMetaInfoHolder {

    private final DeploymentUnit deploymentUnit;

    private Set<CamelContributionBeanMetaInfo> camelBeanMetaInfo;

    /**
     * Full arg constructor.
     *
     * @param deploymentUnit
     *            Deployment unit.
     */
    public CamelContributionMetaInfoHolder(final DeploymentUnit deploymentUnit) {
        this.deploymentUnit = deploymentUnit;
        this.camelBeanMetaInfo = new HashSet<CamelContributionBeanMetaInfo>();
    }

    /**
     * Getter for camel bean meta info.
     *
     * @return the camelBeanMetaInfo
     */
    public Set<CamelContributionBeanMetaInfo> getCamelBeanMetaInfo() {
        return camelBeanMetaInfo;
    }

    /**
     * Setter for camel bean meta info.
     *
     * @param camelBeanMetaInfo
     *            the camelBeanMetaInfo to set.
     */
    public void setCamelBeanMetaInfo(final Set<CamelContributionBeanMetaInfo> camelBeanMetaInfo) {
        this.camelBeanMetaInfo = camelBeanMetaInfo;
    }

    /**
     * Getter for deployment unit.
     *
     * @return the deploymentUnit
     */
    public DeploymentUnit getDeploymentUnit() {
        return deploymentUnit;
    }
}
