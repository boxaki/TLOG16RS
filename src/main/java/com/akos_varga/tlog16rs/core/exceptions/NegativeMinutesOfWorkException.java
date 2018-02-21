package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class NegativeMinutesOfWorkException extends Exception {

    /**
     * Creates a new instance of <code>NegativeMinutesOfWorkException</code>
     * without detail message.
     */
    public NegativeMinutesOfWorkException() {
    }

    /**
     * Constructs an instance of <code>NegativeMinutesOfWorkException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public NegativeMinutesOfWorkException(String msg) {
        super(msg);
    }
}
