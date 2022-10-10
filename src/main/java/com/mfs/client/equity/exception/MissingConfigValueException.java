package com.mfs.client.equity.exception;

/**
 * Missing config value exception throw when accessing invalid data from the system config
 */
public class MissingConfigValueException extends RuntimeException {
    public MissingConfigValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingConfigValueException(String message) {
        super(message);
    }

}