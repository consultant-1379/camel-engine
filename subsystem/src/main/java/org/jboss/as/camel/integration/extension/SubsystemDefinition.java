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

package org.jboss.as.camel.integration.extension;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Subsystem definition.
 */
public final class SubsystemDefinition extends SimpleResourceDefinition {

    public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();

    static final SimpleAttributeDefinition ENABLED = new SimpleAttributeDefinitionBuilder(SubsystemExtension.ENABLED, ModelType.BOOLEAN)
            .setAllowExpression(true).setXmlName(SubsystemExtension.ENABLED).setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(true)).setAllowNull(false).build();

    static final SimpleAttributeDefinition CONTEXT_NAME = new SimpleAttributeDefinitionBuilder(SubsystemExtension.CONTEXT_NAME, ModelType.STRING)
            .setAllowExpression(true).setXmlName(SubsystemExtension.CONTEXT_NAME).setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(SubsystemExtension.DEFAULT_SHARED_CONTEXT_NAME)).setAllowNull(false).build();

    private SubsystemDefinition() {
        super(SubsystemExtension.SUBSYSTEM_PATH, SubsystemExtension.getResourceDescriptionResolver(null),
        // We always need to add an 'add' operation
                SubsystemAdd.INSTANCE,
                // Every resource that is added, normally needs a remove operation
                SubsystemRemove.INSTANCE);
    }

    @Override
    public void registerOperations(final ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        // you can register aditional operations here
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(ENABLED, null, SharedCamelContextEnableHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(CONTEXT_NAME, null, SharedCamelContextNameHandler.INSTANCE);
    }
}
