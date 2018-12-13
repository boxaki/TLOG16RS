package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class AuthenticationFailureException extends Exception {

    /**
     * Creates a new instance of <code>AuthenticationFailureException</code>
     * without detail message.
     */
    public AuthenticationFailureException() {
    }

    /**
     * Constructs an instance of <code>AuthenticationFailureException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public AuthenticationFailureException(String msg) {
        super(msg);
    }
}
