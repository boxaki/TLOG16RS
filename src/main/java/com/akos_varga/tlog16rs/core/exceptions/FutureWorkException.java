package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class FutureWorkException extends Exception {

    /**
     * Creates a new instance of <code>FutureWorkException</code> without detail
     * message.
     */
    public FutureWorkException() {
    }

    /**
     * Constructs an instance of <code>FutureWorkException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public FutureWorkException(String msg) {
        super(msg);
    }
}
