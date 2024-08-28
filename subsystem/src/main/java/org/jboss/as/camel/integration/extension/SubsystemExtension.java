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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * SubsystemExtension class.
 */
public class SubsystemExtension implements Extension {

    /**
     * The name space used for the {@code substystem} element.
     */
    public static final String NAMESPACE = "urn:org.jboss.as:camel-subsystem:1.0";

    /**
     * The name of our subsystem within the model.
     */
    public static final String SUBSYSTEM_NAME = "camel-subsystem";

    /**
     * Name of the tag in xml that controles the sharing.
     */
    public static final String SHARED_CAMEL_CONTEXT = "shared-camel-context";

    /**
     * Attribute enabled=[true|false] that controls if we have one context for all deployments, or we create camel context per deployment.
     */
    public static final String ENABLED = "enabled";
    /**
     * Name of the shared camel context.
     */
    public static final String CONTEXT_NAME = "context-name";

    /**
     * Default name used for shared context.
     */
    public static final String DEFAULT_SHARED_CONTEXT_NAME = "SharedContext-1";

    /**
     * Subsystem path made up from subsystem and camel-subsystem.
     */
    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);

    /**
     * Resource bundle file name.
     */
    private static final String RESOURCE_NAME = SubsystemExtension.class.getPackage().getName() + ".LocalDescriptions";

    /**
     * The parser used for parsing our subsystem.
     */
    private final SubsystemParser parser = new SubsystemParser();

    /**
     * Create static instance of resource description resolver.
     *
     * @param keyPrefix
     *            Prefix to use for this description resolver.
     * @return StandardResourceDescriptionResolver that will be used to resolve descriptions
     */
    @SuppressWarnings("PMD.UseProperClassLoader")
    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        final String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, SubsystemExtension.class.getClassLoader(), true, false);
    }

    @Override
    public void initializeParsers(final ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    @Override
    public void initialize(final ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SubsystemDefinition.INSTANCE);
        registration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE, GenericSubsystemDescribeHandler.INSTANCE, false,
                OperationEntry.EntryType.PRIVATE);

        subsystem.registerXMLElementWriter(parser);
    }

    /**
     * Static method for add subsystem operation.
     *
     * @return ModelNode representing this add subsystem operation
     */
    static ModelNode createAddSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }

    /**
     * The subsystem parser, which uses stax to read and write to and from xml.
     */
    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
            XMLElementWriter<SubsystemMarshallingContext> {
        @Override
        public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
            context.startSubsystemElement(SubsystemExtension.NAMESPACE, false);
            final ModelNode model = context.getModelNode();
            writer.writeStartElement(SHARED_CAMEL_CONTEXT);
            for (final ModelNode node : model.asList()) {
                if (node.has(ENABLED)) {
                    SubsystemDefinition.ENABLED.marshallAsAttribute(node, true, writer);
                }
                if (node.has(CONTEXT_NAME)) {
                    SubsystemDefinition.CONTEXT_NAME.marshallAsAttribute(node, true, writer);
                }
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }

        @Override
        public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
            final ModelNode subsystem = createAddSubsystemOperation();

            // Read the children
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (!reader.getLocalName().equals(SHARED_CAMEL_CONTEXT)) {
                    throw ParseUtils.unexpectedElement(reader);
                }
                readDeploymentType(reader, list, subsystem);
            }
            ParseUtils.requireNoContent(reader);
        }

        private void readDeploymentType(final XMLExtendedStreamReader reader, final List<ModelNode> list, final ModelNode subsystem)
                throws XMLStreamException {
            if (!reader.getLocalName().equals(SHARED_CAMEL_CONTEXT)) {
                throw ParseUtils.unexpectedElement(reader);
            }
            ParseUtils.requireAttributes(reader, CONTEXT_NAME, ENABLED);
            String contextName = null;
            String enabled = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                final String attr = reader.getAttributeLocalName(i);
                final String value = reader.getAttributeValue(i);
                if (attr.equals(CONTEXT_NAME)) {
                    SubsystemDefinition.CONTEXT_NAME.parseAndSetParameter(value, subsystem, reader);
                    contextName = value;
                } else if (attr.equals(ENABLED)) {
                    enabled = value;
                    SubsystemDefinition.ENABLED.parseAndSetParameter(value, subsystem, reader);
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            if (contextName == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(CONTEXT_NAME));
            }
            if (enabled == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton(ENABLED));
            }
            list.add(subsystem);
        }
    }
}
