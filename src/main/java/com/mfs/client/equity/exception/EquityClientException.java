package com.mfs.client.equity.exception;

/**
 * @author Godwin Tavirimirwa | Created at 11/8/2021 21:55
 */
public class EquityClientException extends RuntimeException {
    public EquityClientException(String message) {
        super(message);
    }

    public EquityClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
