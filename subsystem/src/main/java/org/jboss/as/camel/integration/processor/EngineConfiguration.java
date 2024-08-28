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

package org.jboss.as.camel.integration.processor;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.common.config.Configuration;

/**
 * Implementation of the <code>Configuration</code> interface. Will be used by the <code>PathElementProcessor</code> object to create
 * <code>com.ericsson.oss.itpf.common.event.handler.EventInputHandler</code> objects.
 *
 * @see com.ericsson.oss.itpf.common.event.handler.EventInputHandler
 * @see com.ericsson.oss.itpf.common.config.Configuration
 */
public class EngineConfiguration implements Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineConfiguration.class);
    private final Map<String, Object> properties = new HashMap<String, Object>();

    @Override
    public Map<String, Object> getAllProperties() {
        return this.properties;
    }

    @Override
    public Boolean getBooleanProperty(final String key) {
        final Object value = properties.get(key);
        if (value != null && value instanceof Boolean) {
            return (Boolean) value;
        } else {
            LOGGER.error("Could not retrieve Boolean type attribute for key {}", key);
            return null;
        }
    }

    @Override
    public Integer getIntProperty(final String key) {
        final Object value = properties.get(key);
        if (value != null && value instanceof Integer) {
            return (Integer) value;
        } else {
            LOGGER.error("Could not retrieve Integer type attribute for key {}", key);
            return null;
        }
    }

    @Override
    public String getStringProperty(final String key) {
        final Object value = properties.get(key);
        if (value != null && value instanceof String) {
            return (String) value;
        } else {
            LOGGER.error("Could not retrieve String type attribute for key {}", key);
            return null;
        }
    }

    /**
     * Setter method provided to allow updates to the <code>properties</code> attribute.
     *
     * @param key
     *            name of the attribute to be added.
     * @param value
     *            value of the attribute to be added.
     */
    public void setProperty(final String key, final Object value) {
        this.properties.put(key, value);
    }
}
