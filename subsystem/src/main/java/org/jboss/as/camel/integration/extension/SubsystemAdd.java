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

import java.util.List;

import org.jboss.as.camel.integration.deployment.BindCamelBeansDeploymentProcess;
import org.jboss.as.camel.integration.deployment.CamelContextDependencyDeploymentProcess;
import org.jboss.as.camel.integration.deployment.CamelContextServiceAnnotationScannerDeploymentProcess;
import org.jboss.as.camel.integration.deployment.CamelContributionAnnotationScannerDeploymentProcess;
import org.jboss.as.camel.integration.deployment.ClassTransformerDeploymentProcess;
import org.jboss.as.camel.integration.deployment.CleanupDeploymentProcess;
import org.jboss.as.camel.integration.deployment.CreateCamelContextDeploymentProcess;
import org.jboss.as.camel.integration.deployment.RegisterCamelContextDeploymentProcess;
import org.jboss.as.camel.integration.deployment.StartStopCamelContextDeploymentProcess;
import org.jboss.as.camel.integration.service.CamelContextIntegrationService;
import org.jboss.as.camel.integration.service.CamelJmsIntegrationService;
import org.jboss.as.camel.integration.service.CamelTransactionIntegrationService;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceTarget;

/**
 * Handler responsible for adding the subsystem resource to the model.
 */
final class SubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final SubsystemAdd INSTANCE = new SubsystemAdd();
    private final ParametersValidator runtimeValidator = new ParametersValidator();

    /**
     * Instantiates a new subsystem add.
     */
    private SubsystemAdd() {}

    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        model.setEmptyObject();
        SubsystemDefinition.ENABLED.validateAndSet(operation, model);
        SubsystemDefinition.CONTEXT_NAME.validateAndSet(operation, model);
    }

    @Override
    public void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model,
            final ServiceVerificationHandler verificationHandler, final List<ServiceController<?>> newControllers) throws OperationFailedException {
        /**
         * Scan for annotations.
         */
        runtimeValidator.validate(operation.resolve());
        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            public void execute(final DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME,
                        CamelContextServiceAnnotationScannerDeploymentProcess.PHASE,
                        CamelContextServiceAnnotationScannerDeploymentProcess.PRIORITY, new CamelContextServiceAnnotationScannerDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, CamelContributionAnnotationScannerDeploymentProcess.PHASE,
                        CamelContributionAnnotationScannerDeploymentProcess.PRIORITY, new CamelContributionAnnotationScannerDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, CamelContextDependencyDeploymentProcess.PHASE,
                        CamelContextDependencyDeploymentProcess.PRIORITY, new CamelContextDependencyDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, ClassTransformerDeploymentProcess.PHASE,
                        ClassTransformerDeploymentProcess.PRIORITY,
                        new ClassTransformerDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, CreateCamelContextDeploymentProcess.PHASE,
                        CreateCamelContextDeploymentProcess.PRIORITY,
                        new CreateCamelContextDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, StartStopCamelContextDeploymentProcess.PHASE,
                        StartStopCamelContextDeploymentProcess.PRIORITY, new StartStopCamelContextDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, RegisterCamelContextDeploymentProcess.PHASE,
                        RegisterCamelContextDeploymentProcess.PRIORITY, new RegisterCamelContextDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, BindCamelBeansDeploymentProcess.PHASE,
                        BindCamelBeansDeploymentProcess.PRIORITY,
                        new BindCamelBeansDeploymentProcess());
                processorTarget.addDeploymentProcessor(SubsystemExtension.SUBSYSTEM_NAME, CleanupDeploymentProcess.PHASE,
                        CleanupDeploymentProcess.PRIORITY, new CleanupDeploymentProcess());
            }
        }, OperationContext.Stage.RUNTIME);

        final ServiceTarget target = context.getServiceTarget();
        final Boolean sharedContext = SubsystemDefinition.ENABLED.resolveModelAttribute(context, model).asBoolean();
        final String sharedContextName = SubsystemDefinition.CONTEXT_NAME.resolveModelAttribute(context, model).asString();
        newControllers.add(CamelContextIntegrationService.addService(target, sharedContext, sharedContextName, verificationHandler));
        newControllers.add(CamelTransactionIntegrationService.addService(target, verificationHandler));
        newControllers.add(CamelJmsIntegrationService.addService(target, verificationHandler));
    }
}
