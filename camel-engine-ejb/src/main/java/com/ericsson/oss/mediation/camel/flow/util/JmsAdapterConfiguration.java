/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.mediation.camel.flow.util;

import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.AMPERSAND;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.CONFIGURATION;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.CONFIG_QUEUE_SYS_PROPERTY;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.EQUALS;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.JMS_PREFIX;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.QUESTION_MARK;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.SERVICE_FRAMEWORK;
import static com.ericsson.oss.mediation.camel.flow.util.FlowConstants.SPLITTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.flow.modeling.modelservice.typed.FlowAttribute;
import com.ericsson.oss.mediation.flow.FlowPathElement;

/**
 * Util class used for resolving JMS configuration attributes.
 */
public class JmsAdapterConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(JmsAdapterConfiguration.class);

    public String queueName;

    /**
     * Resolve config attributes and creates config string for jms component.
     *
     * @param from
     *            FlowPathElement.
     * @return String configString
     */
    public String resolveConfigParametersForEndPoint(final FlowPathElement from) {
        LOG.trace("Route starts with JMS component, creating configuration parameters");
        String result = "";
        queueName = from.getURI();
        if (from.getAttributes() != null) {
            for (final FlowAttribute attrib : from.getAttributes()) {
                if (attrib.getSourceURI() != null) {
                    result += resolveConfigAttribute(attrib);
                }
            }
        }
        if (!result.isEmpty()) {
            result = result.replaceFirst(AMPERSAND, QUESTION_MARK);
        }
        return queueName + result;
    }

    private String resolveConfigAttribute(final FlowAttribute attrib) {
        String result = "";
        final String attributeSourceUri = attrib.getSourceURI();
        if (attributeSourceUri.toLowerCase().startsWith(CONFIGURATION)) {
            if (attributeSourceUri.toLowerCase().contains(SERVICE_FRAMEWORK)) {
                queueName = JMS_PREFIX + SPLITTER + System.getProperty(CONFIG_QUEUE_SYS_PROPERTY);
            } else {
                result += AMPERSAND + attrib.getAttributeName() + EQUALS +
                        attributeSourceUri.substring(attributeSourceUri.lastIndexOf(SPLITTER) + 1);
            }
        }
        return result;
    }
}
