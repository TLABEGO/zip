package com.mfs.client.equity.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * No transaction exists exception throw when a client queries
 * transaction with an id that does not exist
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoSuchTransactionExistsException extends RuntimeException {
    public NoSuchTransactionExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchTransactionExistsException(String message) {
        super(message);
    }
}
