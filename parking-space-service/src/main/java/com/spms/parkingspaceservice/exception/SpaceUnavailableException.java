package com.spms.parkingspaceservice.exception;

public class SpaceUnavailableException extends RuntimeException {
    public SpaceUnavailableException(String message) {
        super(message);
    }
}
