package com.squad.squad.exception;

public class RosterNotFoundException extends RuntimeException {
    public RosterNotFoundException(String message) {
        super(message);
    }
}
