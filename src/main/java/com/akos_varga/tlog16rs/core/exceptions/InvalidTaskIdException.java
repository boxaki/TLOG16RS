package com.akos_varga.tlog16rs.core.exceptions; 

/**
 * Thrown when the format of the task Id is not valid
 * 
 * @author Akos Varga
 */
public class InvalidTaskIdException extends Exception {

    /**
     * Creates a new instance of <code>NoTaskIdException</code> without detail
     * message.
     */
    public InvalidTaskIdException() {
    }

    /**
     * Constructs an instance of <code>NoTaskIdException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidTaskIdException(String msg) {
        super(msg);
    }
}
