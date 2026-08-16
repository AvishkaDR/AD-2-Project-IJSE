package com.spms.parkingspaceservice.exception;

public class DuplicateSpaceException extends RuntimeException {
    public DuplicateSpaceException(String message) {
        super(message);
    }
}
