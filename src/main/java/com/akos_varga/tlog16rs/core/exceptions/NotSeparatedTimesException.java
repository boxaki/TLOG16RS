package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class NotSeparatedTimesException extends Exception {

    /**
     * Creates a new instance of <code>NotSeparatedTimesException</code> without
     * detail message.
     */
    public NotSeparatedTimesException() {
    }

    /**
     * Constructs an instance of <code>NotSeparatedTimesException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotSeparatedTimesException(String msg) {
        super(msg);
    }
}
