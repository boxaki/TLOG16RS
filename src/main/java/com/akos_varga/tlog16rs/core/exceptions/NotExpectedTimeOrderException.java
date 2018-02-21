package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class NotExpectedTimeOrderException extends Exception {

    /**
     * Creates a new instance of <code>NotExpectedTimeOrderException</code>
     * without detail message.
     */
    public NotExpectedTimeOrderException() {
    }

    /**
     * Constructs an instance of <code>NotExpectedTimeOrderException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotExpectedTimeOrderException(String msg) {
        super(msg);
    }
}
