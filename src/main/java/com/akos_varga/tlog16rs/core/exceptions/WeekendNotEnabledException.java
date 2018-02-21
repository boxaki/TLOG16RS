package com.akos_varga.tlog16rs.core.exceptions;

/**
 *
 * @author Akos Varga
 */
public class WeekendNotEnabledException extends Exception {

    /**
     * Creates a new instance of <code>WeekendNotEnabledException</code> without
     * detail message.
     */
    public WeekendNotEnabledException() {
    }

    /**
     * Constructs an instance of <code>WeekendNotEnabledException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public WeekendNotEnabledException(String msg) {
        super(msg);
    }
}
