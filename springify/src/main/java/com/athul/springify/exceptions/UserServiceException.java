package com.athul.springify.exceptions;

public class UserServiceException extends RuntimeException {

    private static final long serialVersionUID = 4484962790813862893L;

    public UserServiceException(String message) {
        super(message);
    }
}
