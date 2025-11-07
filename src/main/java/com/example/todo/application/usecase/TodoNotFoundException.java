package com.example.todo.application.usecase;

/**
 * TodoNotFoundException
 *
 * Exception levée lorsqu'un Todo demandé n'existe pas ou n'est pas accessible.
 *
 * @author Todo Team
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String message) {
        super(message);
    }

    public TodoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
