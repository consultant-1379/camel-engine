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

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

/**
 * Meta info holder for camel bean annotation.
 */
public class CamelContributionBeanMetaInfo {

    private final AnnotationInstance annotationInstance;
    private final String handlerClassName;

    /**
     * Full arg constructor.
     *
     * @param annotationInstance
     *            Annotation instance for which we are storing info.
     * @param handlerClassName
     *            The fully qualified handler class name.
     */
    public CamelContributionBeanMetaInfo(final AnnotationInstance annotationInstance, final String handlerClassName) {
        this.annotationInstance = annotationInstance;
        this.handlerClassName = handlerClassName;
    }

    /**
     * ClassInfo for this annotation.
     *
     * @return ClassInfo related to this annotation
     */
    public ClassInfo getClassInfo() {
        return (ClassInfo) this.annotationInstance.target();
    }

    /**
     * Getter for handler id.
     *
     * @return Id of this handler which is the fully qualified handler class name.
     */
    public String getHandlerId() {
        final AnnotationValue annotationValue = this.annotationInstance.value("id"); // if its @CamelBean
        if (annotationValue != null) {
            return annotationValue.asString();
        } else {
            return handlerClassName;
        }
    }

    /**
     * Getter for context name.
     *
     * @return Name of the camel context where this bean will be bound
     */
    public String getContextName() {
        final AnnotationValue annotationValue = this.annotationInstance.value("contextName");
        return annotationValue.asString();
    }
}
