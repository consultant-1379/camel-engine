/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package org.jboss.as.camel.integration.processor;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * The Class EngineConfigurationTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class EngineConfigurationTestCase {

    private EngineConfiguration engineConfiguration;

    @Test
    public void testBooleanProperty() {
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setProperty("myBooleanKey", new Boolean(false));
        final Boolean expected = engineConfiguration.getBooleanProperty("myBooleanKey");
        Assert.assertEquals(expected, new Boolean(false));
    }

    @Test
    public void testBooleanPropertyNotABoolean() {
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setProperty("myBooleanKey", new Integer(234));
        final Boolean expected = engineConfiguration.getBooleanProperty("myBooleanKey");
        Assert.assertNull(expected);
    }

    @Test
    public void testIntProperty() {
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setProperty("myIntKey", new Integer(123));
        final Integer expected = engineConfiguration.getIntProperty("myIntKey");
        Assert.assertEquals(expected, new Integer(123));
    }

    @Test
    public void testIntPropertyNotAInt() {
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setProperty("myIntKey", new Boolean(false));
        final Integer expected = engineConfiguration.getIntProperty("myIntKey");
        Assert.assertNull(expected);
    }

    @Test
    public void testStringProperty() {
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setProperty("myStrKey", "myString");
        final String expected = engineConfiguration.getStringProperty("myStrKey");
        Assert.assertEquals(expected, "myString");
    }

    @Test
    public void testStringPropertyNotAString() {
        engineConfiguration = new EngineConfiguration();
        engineConfiguration.setProperty("myStrKey", new Boolean(false));
        final String expected = engineConfiguration.getStringProperty("myStrKey");
        Assert.assertNull(expected);
    }
}
