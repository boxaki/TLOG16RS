package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class EmptyTimeFieldException extends Exception {

    /**
     * Creates a new instance of <code>EmptyTimeField</code> without detail
     * message.
     */
    public EmptyTimeFieldException() {
    }

    /**
     * Constructs an instance of <code>EmptyTimeField</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public EmptyTimeFieldException(String msg) {
        super(msg);
    }
}
