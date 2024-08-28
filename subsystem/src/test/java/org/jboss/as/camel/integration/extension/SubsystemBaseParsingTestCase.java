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

import java.io.IOException;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

/**
 * This is the barebone test example that tests subsystem It does same things that {@link SubsystemParsingTestCase} does but most of internals are
 * already done in AbstractSubsystemBaseTest If you need more control over what happens in tests look at {@link SubsystemParsingTestCase}.
 */
public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(SubsystemExtension.SUBSYSTEM_NAME, new SubsystemExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return "<subsystem xmlns=\"" + SubsystemExtension.NAMESPACE + "\">"
                + "<shared-camel-context enabled=\"true\" context-name=\"Camel-Context1\">" + "</shared-camel-context>" + "</subsystem>";
    }
}
