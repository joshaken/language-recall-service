package com.recall.enums;

import java.io.Serializable;

/**
 * Interface for result messages (e.g., error codes and messages).
 **/
public interface IResultMsg extends Serializable {

    /**
     * Gets the error code.
     * @return The error code
     */
    Integer getCode();

    /**
     * Gets the error message.
     * @return The error message
     */
    String getMessage();


}
