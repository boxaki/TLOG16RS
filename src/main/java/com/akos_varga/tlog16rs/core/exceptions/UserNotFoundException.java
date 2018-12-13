package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class UserNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>UserNotFoundException</code> without
     * detail message.
     */
    public UserNotFoundException() {
    }

    /**
     * Constructs an instance of <code>UserNotFoundException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UserNotFoundException(String msg) {
        super(msg);
    }
}
