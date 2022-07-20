package com.penguineering.cleanuri.apigateway.results;

public class ResultTimeoutException extends Exception{
    public ResultTimeoutException() {
    }

    public ResultTimeoutException(String message) {
        super(message);
    }
}
