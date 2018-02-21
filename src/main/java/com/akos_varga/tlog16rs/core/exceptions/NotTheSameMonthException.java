package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class NotTheSameMonthException extends Exception {

    /**
     * Creates a new instance of <code>NotTheSameMonthException</code> without
     * detail message.
     */
    public NotTheSameMonthException(){
    }

    /**
     * Constructs an instance of <code>NotTheSameMonthException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NotTheSameMonthException(String msg) {
        super(msg);
    }
}
