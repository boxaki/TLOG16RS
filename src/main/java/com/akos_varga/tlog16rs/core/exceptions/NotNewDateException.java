package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class NotNewDateException extends Exception {

    /**
     * Creates a new instance of <code>NotNewDateException</code> without detail
     * message.
     */
    public NotNewDateException() {
    }

    /**
     * Constructs an instance of <code>NotNewDateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NotNewDateException(String msg) {
        super(msg);
    }
}
