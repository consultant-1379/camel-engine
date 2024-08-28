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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.common.config.Configuration;

/**
 * The Class EngineEventHandlerContextTestCase.
 */
@RunWith(MockitoJUnitRunner.class)
public class EngineEventHandlerContextTestCase {

    private EngineEventHandlerContext engineEventHandlerContext;

    @Mock
    private Configuration configuration;

    @Test
    public void testEngineEventHandlerContext() {

        engineEventHandlerContext = new EngineEventHandlerContext(configuration);
        engineEventHandlerContext.sendControlEvent(null);
        Assert.assertNotNull(engineEventHandlerContext.getEventHandlerConfiguration());
        Assert.assertNotNull(engineEventHandlerContext.getEventSubscribers());
        Assert.assertNull(engineEventHandlerContext.getContextualData("test"));
    }
}
