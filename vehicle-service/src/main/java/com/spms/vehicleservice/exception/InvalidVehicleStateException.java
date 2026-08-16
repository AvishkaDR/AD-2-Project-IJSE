package com.spms.vehicleservice.exception;

public class InvalidVehicleStateException extends RuntimeException {
    public InvalidVehicleStateException(String message) {
        super(message);
    }
}
