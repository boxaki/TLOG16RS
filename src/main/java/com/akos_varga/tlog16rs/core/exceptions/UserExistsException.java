package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class UserExistsException extends Exception {

    /**
     * Creates a new instance of <code>UserExistsException</code> without detail
     * message.
     */
    public UserExistsException() {
    }

    /**
     * Constructs an instance of <code>UserExistsException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UserExistsException(String msg) {
        super(msg);
    }
}
