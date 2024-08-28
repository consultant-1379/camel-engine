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

package org.jboss.camel.exception;

/**
 * Checked exception thrown from the camel-engine ejb or subsystem.
 */
public class CamelEngineException extends Exception {

    private static final long serialVersionUID = 2150577460701610364L;

    /**
     * Constructs a new CamelEngineException with {@code null} as its detail message.
     */
    public CamelEngineException() {
        super();
    }

    /**
     * Constructs a new CamelEngineException with the specified detail message.
     *
     * @param message
     *            the detail message.
     */
    public CamelEngineException(final String message) {
        super(message);
    }

    /**
     * Constructs a new CamelEngineException with the specified cause and {@code null} as its detail message.
     * <p>
     * Useful for wrapping any exception.
     *
     * @param cause
     *            the specified cause.
     */
    public CamelEngineException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new CamelEngineException with the specified cause and detail message.
     * <p>
     * Useful for wrapping any exception.
     *
     * @param message
     *            the detail message.
     * @param cause
     *            the detail cause.
     */
    public CamelEngineException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
