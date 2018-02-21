package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class NoTaskIdException extends Exception {

    /**
     * Creates a new instance of <code>NoTaskIdException</code> without detail
     * message.
     */
    public NoTaskIdException() {
    }

    /**
     * Constructs an instance of <code>NoTaskIdException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NoTaskIdException(String msg) {
        super(msg);
    }
}
