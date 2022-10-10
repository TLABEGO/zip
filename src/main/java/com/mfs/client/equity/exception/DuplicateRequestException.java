package com.mfs.client.equity.exception;

/**
 * Duplicate Request exception thrown when there is a duplicate request being sent
 */
public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String message) {
        super(message);
    }
}
