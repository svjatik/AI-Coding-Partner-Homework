package com.workshop.ticketsystem.exception;

public class InvalidTicketException extends RuntimeException {

    public InvalidTicketException(String message) {
        super(message);
    }

    public InvalidTicketException(String message, Throwable cause) {
        super(message, cause);
    }
}
