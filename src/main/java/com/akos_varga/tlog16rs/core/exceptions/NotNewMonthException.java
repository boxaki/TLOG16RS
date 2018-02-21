package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class NotNewMonthException extends Exception {

    /**
     * Creates a new instance of <code>NotNewMonthException</code> without
     * detail message.
     */
    public NotNewMonthException() {
    }

    /**
     * Constructs an instance of <code>NotNewMonthException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NotNewMonthException(String msg) {
        super(msg);
    }
}
