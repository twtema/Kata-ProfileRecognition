package org.kata.exception;

public class DocumentsNotFoundException extends RuntimeException {
    public DocumentsNotFoundException(String message) {
        super(message);
    }
}
