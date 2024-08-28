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

package org.jboss.as.camel.integration.extension;

import org.jboss.as.camel.integration.service.CamelContextIntegrationService;
import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 * Handler for enable shared camel context attribute.
 */
public final class SharedCamelContextEnableHandler extends AbstractWriteAttributeHandler<Void> {

    /**
     * Return instance of this singleton.
     */
    public static final SharedCamelContextEnableHandler INSTANCE = new SharedCamelContextEnableHandler();

    private SharedCamelContextEnableHandler() {
        super(SubsystemDefinition.ENABLED);
    }

    @Override
    protected boolean applyUpdateToRuntime(final OperationContext context, final ModelNode operation, final String attributeName,
            final ModelNode resolvedValue, final ModelNode currentValue, final HandbackHolder<Void> handbackHolder) throws OperationFailedException {
        modifyIsSharedContext(context, resolvedValue.asBoolean());
        return false;
    }

    @Override
    protected void revertUpdateToRuntime(final OperationContext context, final ModelNode operation, final String attributeName,
            final ModelNode valueToRestore, final ModelNode valueToRevert, final Void handback) throws OperationFailedException {
        modifyIsSharedContext(context, valueToRestore.asBoolean());
    }

    private void modifyIsSharedContext(final OperationContext context, final boolean value) throws OperationFailedException {
        final CamelContextIntegrationService service = (CamelContextIntegrationService) context.getServiceRegistry(true)
                .getRequiredService(CamelContextIntegrationService.CAMEL_CONTEXT_INTERATION_SERVICE_NAME).getValue();
        service.setSharedCamelDeployment(value);
    }
}
