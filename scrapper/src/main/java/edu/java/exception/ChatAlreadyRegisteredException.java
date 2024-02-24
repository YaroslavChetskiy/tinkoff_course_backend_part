package edu.java.exception;

public class ChatAlreadyRegisteredException extends RuntimeException {

    public ChatAlreadyRegisteredException(String message) {
        super(message);
    }
}
