package com.sithumya20220865.OOPCW;

public class SessionAlreadyInitializedException extends RuntimeException{

    public SessionAlreadyInitializedException() {}

    @Override
    public String getMessage() {
        return "Session already initialized";
    }
}
