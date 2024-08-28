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

package org.jboss.vfs;

/**
 * A factory for creating VirtualFile objects.
 */
public final class VirtualFileFactory {
    private VirtualFileFactory() {}

    public static VirtualFile createVirtualFileWithNoParent(final String name) {
        return new VirtualFile(name, null);
    }

    public static VirtualFile createVirtualFileWithParent(final String name, final VirtualFile parent) {
        return new VirtualFile(name, parent);
    }

}
